package tech.ti.social.feature.blufi.data.bluetooth

import android.Manifest
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import tech.ti.social.feature.blufi.data.bluetooth.BlufiGattAttributes.DEVICE_NAME_PREFIX
import tech.ti.social.feature.blufi.data.model.BlufiDevice

private const val TAG = "BleScanner"

/**
 * BLE scanner for discovering BluFi devices.
 * Scans for devices whose name contains "Xiaozhi" or "Blufi".
 */
class BleScanner(
    private val context: Context
) {

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter
    private val scanner = bluetoothAdapter.bluetoothLeScanner

    private val discoveredDevices = mutableMapOf<String, BlufiDevice>()

    /**
     * Check if BLE permissions are granted.
     */
    fun hasPermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return hasPermission(Manifest.permission.BLUETOOTH_SCAN) &&
                hasPermission(Manifest.permission.BLUETOOTH_CONNECT) &&
                hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        return hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    /**
     * Get list of missing permissions.
     */
    fun getMissingPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            ).filter { !hasPermission(it) }.toTypedArray()
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
                .filter { !hasPermission(it) }.toTypedArray()
        }
    }

    /**
     * Check if BLE is enabled on the device.
     */
    fun isBluetoothEnabled(): Boolean = bluetoothAdapter.isEnabled

    /**
     * Start scanning for BluFi devices.
     * Returns a Flow that emits the current list of discovered devices.
     */
    fun scan(): Flow<List<BlufiDevice>> = callbackFlow {
        if (!hasPermissions()) {
            close(IllegalStateException("BLE permissions not granted"))
            return@callbackFlow
        }

        if (!bluetoothAdapter.isEnabled) {
            close(IllegalStateException("Bluetooth is not enabled"))
            return@callbackFlow
        }

        discoveredDevices.clear()

        val filters = listOf(
            ScanFilter.Builder()
                .setDeviceName("Xiaozhi-Blufi")
                .build()
        )

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
            .build()

        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = result.device
                val name = device.name ?: return

                // Filter by name prefix
                if (!name.contains(DEVICE_NAME_PREFIX, ignoreCase = true) &&
                    !name.contains("Blufi", ignoreCase = true)) {
                    return
                }

                val blufiDevice = BlufiDevice(
                    name = name,
                    address = device.address,
                    rssi = result.rssi,
                    bluetoothDevice = device
                )

                discoveredDevices[device.address] = blufiDevice
                trySend(discoveredDevices.values.toList())
                Log.d(TAG, "discovered: $name (${device.address}) rssi=${result.rssi}")
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e(TAG, "scan failed with error code: $errorCode")
                close(IllegalStateException("BLE scan failed with error code: $errorCode"))
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                results?.forEach { result ->
                    onScanResult(ScanSettings.CALLBACK_TYPE_ALL_MATCHES, result)
                }
            }
        }

        scanner.startScan(filters, settings, scanCallback)
        Log.d(TAG, "scan started")

        awaitClose {
            scanner.stopScan(scanCallback)
            Log.d(TAG, "scan stopped")
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) ==
            PackageManager.PERMISSION_GRANTED
    }
}
