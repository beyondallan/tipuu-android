package tech.ti.social.core.ble

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * BLE permission helper for Android 12+ and older versions.
 */
class BlePermissionHandler(
    private val context: Context
) {

    /**
     * Check if all required BLE permissions are granted.
     */
    fun hasPermissions(): Boolean {
        val missing = getMissingPermissions()
        return missing.isEmpty()
    }

    /**
     * Get list of permissions that need to be requested.
     */
    fun getMissingPermissions(): Array<String> {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
                permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }

        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        return permissions.toTypedArray()
    }

    /**
     * Check if Bluetooth adapter is enabled.
     */
    fun isBluetoothEnabled(): Boolean {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE)
            as BluetoothManager
        return bluetoothManager.adapter?.isEnabled == true
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) ==
            PackageManager.PERMISSION_GRANTED
    }
}
