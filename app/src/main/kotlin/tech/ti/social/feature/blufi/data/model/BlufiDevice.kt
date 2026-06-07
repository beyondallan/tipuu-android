package tech.ti.social.feature.blufi.data.model

import android.bluetooth.BluetoothDevice

data class BlufiDevice(
    val name: String,
    val address: String,
    val rssi: Int,
    val bluetoothDevice: BluetoothDevice
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as BlufiDevice
        return address == other.address
    }

    override fun hashCode(): Int = address.hashCode()
}
