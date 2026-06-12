package tech.ti.social.feature.blufi.data.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import tech.ti.social.feature.blufi.data.bluetooth.BlufiGattAttributes.CONTROL_CHAR_UUID
import tech.ti.social.feature.blufi.data.bluetooth.BlufiGattAttributes.DATA_CHAR_UUID
import tech.ti.social.feature.blufi.data.bluetooth.BlufiGattAttributes.SERVICE_UUID
import tech.ti.social.feature.blufi.data.protocol.BlufiMessage
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val TAG = "BlufiConnector"

/**
 * Manages GATT connection to a BluFi device.
 * Handles connection, service discovery, characteristic read/write, and notification handling.
 */
@SuppressLint("MissingPermission")
class BlufiDeviceConnector(
    private val context: Context,
    private val frameEncoder: BlufiFrameEncoder
) {

    private var bluetoothGatt: BluetoothGatt? = null
    private var controlCharacteristic: BluetoothGattCharacteristic? = null
    private var dataCharacteristic: BluetoothGattCharacteristic? = null

    private val messageChannel = Channel<BlufiMessage>(Channel.UNLIMITED)

    /**
     * Flow of decoded messages received from the device.
     */
    val messageFlow: Flow<BlufiMessage> = messageChannel.receiveAsFlow()

    /**
     * Connect to a BluFi device by MAC address.
     */
    suspend fun connect(address: String): Result<Unit> {
        return try {
            val device = bluetoothGatt?.device ?: run {
                val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE)
                    as android.bluetooth.BluetoothManager
                bluetoothManager.adapter.getRemoteDevice(address)
            }

            suspendCancellableCoroutine { continuation ->
                val callback = object : BluetoothGattCallback() {
                    override fun onConnectionStateChange(
                        gatt: BluetoothGatt,
                        status: Int,
                        newState: Int
                    ) {
                        when (newState) {
                            BluetoothProfile.STATE_CONNECTED -> {
                                Log.d(TAG, "connected to $address, discovering services")
                                gatt.discoverServices()
                            }

                            BluetoothProfile.STATE_DISCONNECTED -> {
                                Log.w(TAG, "disconnected from $address")
                                cleanup()
                                if (continuation.isActive) {
                                    continuation.resumeWithException(
                                        Exception("Device disconnected")
                                    )
                                }
                            }
                        }
                    }

                    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            val service = gatt.getService(SERVICE_UUID)
                            if (service == null) {
                                Log.e(TAG, "BluFi service not found")
                                if (continuation.isActive) {
                                    continuation.resumeWithException(
                                        Exception("BluFi service not found")
                                    )
                                }
                                return
                            }

                            controlCharacteristic = service.getCharacteristic(CONTROL_CHAR_UUID)
                            dataCharacteristic = service.getCharacteristic(DATA_CHAR_UUID)

                            if (controlCharacteristic == null || dataCharacteristic == null) {
                                Log.e(TAG, "BluFi characteristics not found")
                                if (continuation.isActive) {
                                    continuation.resumeWithException(
                                        Exception("BluFi characteristics not found")
                                    )
                                }
                                return
                            }

                            // Enable notifications for both characteristics
                            enableNotification(gatt, controlCharacteristic!!)
                            enableNotification(gatt, dataCharacteristic!!)

                            // Request MTU - continuation will resume in onMtuChanged
                            Log.d(TAG, "service discovery complete, requesting MTU")
                            gatt.requestMtu(BlufiGattAttributes.MAX_MTU)
                        } else {
                            Log.e(TAG, "service discovery failed, status=$status")
                            if (continuation.isActive) {
                                continuation.resumeWithException(
                                    Exception("Service discovery failed: $status")
                                )
                            }
                        }
                    }

                    override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
                        Log.d(TAG, "MTU changed to $mtu, status=$status")
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            if (continuation.isActive) {
                                continuation.resume(Result.success(Unit))
                            }
                        } else {
                            // MTU negotiation failed, but we can still proceed with default MTU
                            Log.w(TAG, "MTU negotiation failed, proceeding with default MTU")
                            if (continuation.isActive) {
                                continuation.resume(Result.success(Unit))
                            }
                        }
                    }

                    override fun onCharacteristicChanged(
                        gatt: BluetoothGatt,
                        characteristic: BluetoothGattCharacteristic
                    ) {
                        val value = characteristic.value ?: return
                        Log.d(TAG, "received ${value.size} bytes from ${characteristic.uuid}")

                        val message = frameEncoder.decode(value)
                        if (message != null) {
                            Log.d(TAG, "decoded message type=0x${message.type.toString(16)}")
                            messageChannel.trySend(message)
                        } else {
                            Log.w(TAG, "failed to decode message")
                        }
                    }

                    @Deprecated("Deprecated in API 33")
                    override fun onCharacteristicWrite(
                        gatt: BluetoothGatt?,
                        characteristic: BluetoothGattCharacteristic?,
                        status: Int
                    ) {
                        if (status != BluetoothGatt.GATT_SUCCESS) {
                            Log.e(TAG, "write failed with status $status")
                        }
                    }
                }

                bluetoothGatt = device.connectGatt(context, false, callback)
                Log.d(TAG, "connecting to $address")

                continuation.invokeOnCancellation {
                    disconnect()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "connection failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Write a frame to the appropriate characteristic.
     * Control messages go to control characteristic, data messages go to data characteristic.
     */
    suspend fun writeFrame(type: Int, data: ByteArray? = null): Result<Unit> {
        val frame = frameEncoder.encode(type, data)
        val characteristic = when (type) {
            BlufiGattAttributes.TYPE_HELLO,
            BlufiGattAttributes.TYPE_ACK,
            BlufiGattAttributes.TYPE_BYE -> controlCharacteristic

            else -> dataCharacteristic
        }

        if (characteristic == null) {
            return Result.failure(IllegalStateException("Characteristic not available"))
        }

        return suspendCancellableCoroutine { continuation ->
            // Android 33+ uses the new write method
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                bluetoothGatt?.writeCharacteristic(
                    characteristic,
                    frame,
                    BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                )
                continuation.resume(Result.success(Unit))
            } else {
                @Suppress("DEPRECATION")
                characteristic.value = frame
                @Suppress("DEPRECATION")
                characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                @Suppress("DEPRECATION")
                val success = bluetoothGatt?.writeCharacteristic(characteristic) ?: false
                continuation.resume(
                    if (success) Result.success(Unit)
                    else Result.failure(Exception("Write characteristic failed"))
                )
            }
        }
    }

    /**
     * Disconnect from the device.
     */
    fun disconnect() {
        Log.d(TAG, "disconnecting")
        cleanup()
    }

    private fun cleanup() {
        bluetoothGatt?.close()
        bluetoothGatt = null
        controlCharacteristic = null
        dataCharacteristic = null
        frameEncoder.resetSequence()
    }

    @SuppressLint("MissingPermission")
    private fun enableNotification(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
        gatt.setCharacteristicNotification(characteristic, true)

        // Enable notification descriptor
        val descriptor = characteristic.getDescriptor(
            java.util.UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
        )
        if (descriptor != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
            } else {
                @Suppress("DEPRECATION")
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                @Suppress("DEPRECATION")
                gatt.writeDescriptor(descriptor)
            }
        }
    }
}
