package tech.ti.social.feature.blufi.data.crypto

import android.util.Log
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom

private const val TAG = "BluFiDhKeyExchange"

/**
 * Diffie-Hellman key exchange for BluFi security negotiation.
 *
 * Flow:
 * 1. Device sends DH parameters (P, G) via NEG_SEC_RSP
 * 2. We generate a private key and compute public key = G^privateKey mod P
 * 3. We send our public key to device
 * 4. Device sends its public key
 * 5. Both compute shared secret = peerPubKey^ourPrivateKey mod P
 * 6. AES key = MD5(sharedSecret)[0:16]
 */
class DhKeyExchange {

    private var privateKey: BigInteger? = null
    private var p: BigInteger? = null
    private var g: BigInteger? = null

    private val secureRandom = SecureRandom()

    /**
     * Parse DH parameters from the device's NEG_SEC_RSP data.
     * Format: subtype(1B) + length(2B LE) + P data, subtype(1B) + length(2B LE) + G data
     * Or: raw P and G concatenated.
     */
    fun parseDhParams(data: ByteArray): DhParameters {
        // Try to parse: the BluFi protocol sends P, G, and L (key length) packed together.
        // The data starts with a subtype byte, then length, then the actual param bytes.
        // Common format: P (128 bytes) + G (128 bytes) + L (1 byte)
        // But some implementations use a TLV format.

        // First, try TLV format
        var pos = 0
        var pBytes: ByteArray? = null
        var gBytes: ByteArray? = null

        while (pos < data.size - 3) {
            val subtype = data[pos].toInt() and 0xFF
            val len = (data[pos + 1].toInt() and 0xFF) or ((data[pos + 2].toInt() and 0xFF) shl 8)
            pos += 3

            if (pos + len > data.size) {
                break
            }

            val paramData = ByteArray(len)
            System.arraycopy(data, pos, paramData, 0, len)
            pos += len

            when (subtype) {
                0x00 -> pBytes = paramData   // P parameter
                0x01 -> gBytes = paramData   // G parameter
            }
        }

        if (pBytes != null && gBytes != null) {
            return DhParameters(
                p = BigInteger(1, pBytes),
                g = BigInteger(1, gBytes)
            )
        }

        // Fallback: treat entire data as P (common for simple implementations)
        // This is unlikely but provides a fallback
        Log.w(TAG, "parseDhParams: could not parse TLV format, data size=${data.size}")
        throw IllegalArgumentException("Unable to parse DH parameters from data")
    }

    /**
     * Generate local DH public key from device-provided P and G.
     * Returns the public key bytes.
     */
    fun generateKeyPair(p: BigInteger, g: BigInteger, privateKeyBits: Int = 256): ByteArray {
        this.p = p
        this.g = g

        // Generate a random private key
        privateKey = BigInteger(privateKeyBits, secureRandom)

        // Compute public key = G^privateKey mod P
        val publicKey = g.modPow(privateKey!!, p)

        Log.d(TAG, "generated DH key pair (P bits=${p.bitLength()}, G bits=${g.bitLength()})")
        return publicKey.toByteArray()
    }

    /**
     * Parse device's public key from NEG_SEC_RSP data (subtype=0x01, TLV format).
     */
    fun parseDevicePublicKey(data: ByteArray): ByteArray {
        var pos = 0
        while (pos < data.size - 3) {
            val subtype = data[pos].toInt() and 0xFF
            val len = (data[pos + 1].toInt() and 0xFF) or ((data[pos + 2].toInt() and 0xFF) shl 8)
            pos += 3

            if (pos + len > data.size) break

            if (subtype == 0x01) {
                val pubKey = ByteArray(len)
                System.arraycopy(data, pos, pubKey, 0, len)
                Log.d(TAG, "parsed device public key (${pubKey.size} bytes)")
                return pubKey
            }
            pos += len
        }
        // Fallback: return all data as the public key
        Log.w(TAG, "parseDevicePublicKey: TLV parse failed, using raw data")
        return data
    }

    /**
     * Compute shared secret from device's public key.
     * sharedSecret = devicePubKey^privateKey mod P
     */
    fun computeSharedSecret(devicePublicKey: ByteArray): ByteArray {
        val devicePubKeyInt = BigInteger(1, devicePublicKey)
        val sharedSecret = devicePubKeyInt.modPow(privateKey!!, p!!)
        Log.d(TAG, "computed shared secret (${sharedSecret.bitLength()} bits)")
        return sharedSecret.toByteArray()
    }

    /**
     * Derive AES-128 key from shared secret using MD5.
     * AES_Key = MD5(sharedSecret)[0:16]
     */
    companion object {
        fun deriveAesKey(sharedSecret: ByteArray): ByteArray {
            val md5 = MessageDigest.getInstance("MD5")
            val hash = md5.digest(sharedSecret)
            return hash.copyOf(16)
        }
    }

    data class DhParameters(
        val p: BigInteger,
        val g: BigInteger
    )
}
