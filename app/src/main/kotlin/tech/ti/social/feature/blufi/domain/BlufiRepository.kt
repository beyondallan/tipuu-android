package tech.ti.social.feature.blufi.domain

import kotlinx.coroutines.flow.Flow
import tech.ti.social.feature.blufi.data.model.BlufiDevice
import tech.ti.social.feature.blufi.data.model.WifiAccessPoint

interface BlufiRepository {

    /**
     * Scan for BluFi devices.
     */
    fun scanDevices(): Flow<List<BlufiDevice>>

    /**
     * Connect to a specific device by MAC address.
     */
    suspend fun connectToDevice(address: String): Result<Unit>

    /**
     * Execute the full provisioning flow.
     */
    suspend fun provisionDevice(ssid: String, password: String): Result<Unit>

    /**
     * Disconnect from the current device.
     */
    fun disconnect()

    /**
     * Check if BLE permissions are granted.
     */
    fun hasBlePermissions(): Boolean

    /**
     * Get missing BLE permissions.
     */
    fun getMissingPermissions(): Array<String>

    /**
     * Check if Bluetooth is enabled.
     */
    fun isBluetoothEnabled(): Boolean
}
