package tech.ti.social.feature.blufi.data.protocol

import android.util.Log
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import tech.ti.social.feature.blufi.data.bluetooth.BlufiDeviceConnector
import tech.ti.social.feature.blufi.data.bluetooth.BlufiFrameEncoder
import tech.ti.social.feature.blufi.data.bluetooth.BlufiGattAttributes
import tech.ti.social.feature.blufi.data.crypto.AesEncryptor
import tech.ti.social.feature.blufi.data.crypto.DhKeyExchange
import tech.ti.social.feature.blufi.data.model.WifiAccessPoint
import java.nio.charset.StandardCharsets

private const val TAG = "BluFiProtocol"

/**
 * BluFi protocol orchestrator.
 * Executes the full provisioning flow:
 *   1. HELLO handshake
 *   2. Security negotiation (DH key exchange)
 *   3. Get WiFi list
 *   4. Send credentials (SSID + password)
 *   5. Request WiFi connection
 *   6. Wait for connection report
 */
class BlufiProtocol(
    private val connector: BlufiDeviceConnector,
    private val frameEncoder: BlufiFrameEncoder,
    private val dhKeyExchange: DhKeyExchange,
    private val aesEncryptor: AesEncryptor
) {

    private var securityEnabled = false

    /**
     * Execute the complete BluFi provisioning flow.
     */
    suspend fun provision(ssid: String, password: String): Result<Unit> {
        try {
            Log.d(TAG, "starting provisioning flow for SSID: $ssid")

            // Step 1: HELLO handshake
            sendHello()

            // Step 2: Security negotiation (optional but recommended)
            negotiateSecurity()

            // Step 3: Get WiFi list
            val wifiList = getWifiList()
            Log.d(TAG, "received ${wifiList.size} WiFi networks")

            // Step 4: Send WiFi credentials
            sendCredentials(ssid, password)

            // Step 5: Request connection
            requestConnectToAp()

            // Step 6: Wait for connection report
            waitForConnectionReport()

            // Step 7: Disconnect BLE
            connector.disconnect()

            Log.d(TAG, "provisioning completed successfully")
            return Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "provisioning failed: ${e.message}", e)
            connector.disconnect()
            return Result.failure(e)
        }
    }

    /**
     * Step 1: Send HELLO and wait for ACK.
     */
    private suspend fun sendHello() {
        Log.d(TAG, "sending HELLO")
        connector.writeFrame(BlufiGattAttributes.TYPE_HELLO)

        val response = waitForMessage(
            BlufiGattAttributes.TYPE_ACK,
            BlufiGattAttributes.TIMEOUT_HELLO
        ) ?: throw Exception("HELLO timeout - no ACK received")

        Log.d(TAG, "HELLO acknowledged")
    }

    /**
     * Step 2: DH key exchange for secure communication.
     *
     * Flow:
     *   App -> NEG_SEC_START
     *   Device -> NEG_SEC_RSP (DH params P, G)
     *   App -> NEG_SEC_START (our public key, subtype=0x01)
     *   Device -> NEG_SEC_RSP (device public key, subtype=0x01)
     */
    private suspend fun negotiateSecurity() {
        Log.d(TAG, "starting security negotiation")

        // Send NEG_SEC_START to initiate
        connector.writeFrame(BlufiGattAttributes.TYPE_NEG_SEC_START)

        // Wait for DH parameters
        val paramsMsg = waitForMessage(
            BlufiGattAttributes.TYPE_NEG_SEC_RSP,
            BlufiGattAttributes.TIMEOUT_SECURITY
        ) ?: throw Exception("Security negotiation timeout - no DH params")

        // Parse DH parameters (P, G)
        val dhParams = dhKeyExchange.parseDhParams(paramsMsg.data)
        Log.d(TAG, "received DH params: P bits=${dhParams.p.bitLength()}, G bits=${dhParams.g.bitLength()}")

        // Generate our key pair
        val ourPubKey = dhKeyExchange.generateKeyPair(dhParams.p, dhParams.g)
        Log.d(TAG, "generated public key (${ourPubKey.size} bytes)")

        // Send our public key (subtype=0x01 for DH_PUB_KEY)
        val pubKeyFrame = buildSecPubKeyFrame(ourPubKey)
        connector.writeFrame(BlufiGattAttributes.TYPE_NEG_SEC_START, pubKeyFrame)

        // Wait for device's public key
        val deviceKeyMsg = waitForMessage(
            BlufiGattAttributes.TYPE_NEG_SEC_RSP,
            BlufiGattAttributes.TIMEOUT_SECURITY
        ) ?: throw Exception("DH exchange timeout - no device public key")

        // Parse device public key and compute shared secret
        val devicePubKey = dhKeyExchange.parseDevicePublicKey(deviceKeyMsg.data)
        val sharedSecret = dhKeyExchange.computeSharedSecret(devicePubKey)
        val aesKey = DhKeyExchange.deriveAesKey(sharedSecret)

        // Configure AES encryptor
        aesEncryptor.setKey(aesKey)
        securityEnabled = true

        Log.d(TAG, "security negotiation complete, AES encryption enabled")
    }

    /**
     * Step 3: Request and receive WiFi list.
     */
    private suspend fun getWifiList(): List<WifiAccessPoint> {
        Log.d(TAG, "requesting WiFi list")
        connector.writeFrame(BlufiGattAttributes.TYPE_GET_WIFI_LIST)

        val wifiMsg = waitForMessage(
            BlufiGattAttributes.TYPE_RECV_WIFI_LIST,
            BlufiGattAttributes.TIMEOUT_WIFI_LIST
        ) ?: throw Exception("WiFi list timeout")

        // If security is enabled, decrypt the data
        val data = if (securityEnabled) {
            aesEncryptor.decrypt(wifiMsg.data, wifiMsg.sequence)
        } else {
            wifiMsg.data
        }

        return WifiInfoParser.parseWifiList(data)
    }

    /**
     * Step 4: Send WiFi SSID and password.
     */
    private suspend fun sendCredentials(ssid: String, password: String) {
        Log.d(TAG, "sending credentials for SSID: $ssid")

        // Send SSID
        val ssidBytes = ssid.toByteArray(StandardCharsets.UTF_8)
        val ssidData = if (securityEnabled) {
            aesEncryptor.encrypt(ssidBytes, frameEncoder.hashCode()) // sequence will be managed by encoder
        } else {
            ssidBytes
        }
        connector.writeFrame(BlufiGattAttributes.TYPE_RECV_STA_SSID, ssidData)

        // Send password (if not empty)
        if (password.isNotEmpty()) {
            val passwordBytes = password.toByteArray(StandardCharsets.UTF_8)
            val pwdData = if (securityEnabled) {
                aesEncryptor.encrypt(passwordBytes, frameEncoder.hashCode())
            } else {
                passwordBytes
            }
            connector.writeFrame(BlufiGattAttributes.TYPE_RECV_STA_PASSWD, pwdData)
        }

        Log.d(TAG, "credentials sent")
    }

    /**
     * Step 5: Request connection to the AP.
     */
    private suspend fun requestConnectToAp() {
        Log.d(TAG, "requesting connection to AP")
        connector.writeFrame(BlufiGattAttributes.TYPE_REQ_CONNECT_TO_AP)
    }

    /**
     * Step 6: Wait for connection report.
     */
    private suspend fun waitForConnectionReport() {
        Log.d(TAG, "waiting for connection report...")

        val reportMsg = waitForMessage(
            BlufiGattAttributes.TYPE_REPORT_WIFI_CONNECTION,
            BlufiGattAttributes.TIMEOUT_CONNECTION_REPORT
        ) ?: throw Exception("Connection report timeout")

        val data = if (securityEnabled) {
            aesEncryptor.decrypt(reportMsg.data, reportMsg.sequence)
        } else {
            reportMsg.data
        }

        val report = WifiInfoParser.parseConnectionReport(data)

        when {
            report.isSuccess -> Log.d(TAG, "WiFi connection successful")
            report.isFailure -> throw Exception("WiFi connection failed (status=${report.status})")
            report.isConnecting -> Log.d(TAG, "WiFi still connecting")
            else -> Log.d(TAG, "WiFi connection status: ${report.status}")
        }
    }

    /**
     * Wait for a specific message type with timeout.
     */
    private suspend fun waitForMessage(type: Int, timeoutMs: Long): BlufiMessage? {
        return try {
            withTimeout(timeoutMs) {
                connector.messageFlow.first { it.type == type }
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Log.w(TAG, "timeout waiting for message type 0x${type.toString(16)}")
            null
        }
    }

    /**
     * Build a security frame containing DH public key.
     * Format: subtype(1B) + length(2B LE) + key_data
     */
    private fun buildSecPubKeyFrame(pubKey: ByteArray): ByteArray {
        val frame = ByteArray(3 + pubKey.size)
        frame[0] = 0x01 // subtype: DH_PUB_KEY
        frame[1] = (pubKey.size and 0xFF).toByte()
        frame[2] = ((pubKey.size shr 8) and 0xFF).toByte()
        System.arraycopy(pubKey, 0, frame, 3, pubKey.size)
        return frame
    }
}
