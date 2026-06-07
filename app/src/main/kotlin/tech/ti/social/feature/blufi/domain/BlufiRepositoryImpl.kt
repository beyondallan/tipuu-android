package tech.ti.social.feature.blufi.domain

import kotlinx.coroutines.flow.Flow
import tech.ti.social.feature.blufi.data.bluetooth.BleScanner
import tech.ti.social.feature.blufi.data.bluetooth.BlufiDeviceConnector
import tech.ti.social.feature.blufi.data.model.BlufiDevice
import tech.ti.social.feature.blufi.data.protocol.BlufiProtocol

class BlufiRepositoryImpl(
    private val bleScanner: BleScanner,
    private val connector: BlufiDeviceConnector,
    private val protocol: BlufiProtocol
) : BlufiRepository {

    override fun scanDevices(): Flow<List<BlufiDevice>> = bleScanner.scan()

    override suspend fun connectToDevice(address: String): Result<Unit> {
        return connector.connect(address)
    }

    override suspend fun provisionDevice(
        ssid: String,
        password: String
    ): Result<Unit> {
        return protocol.provision(ssid, password)
    }

    override fun disconnect() {
        connector.disconnect()
    }

    override fun hasBlePermissions(): Boolean = bleScanner.hasPermissions()

    override fun getMissingPermissions(): Array<String> = bleScanner.getMissingPermissions()

    override fun isBluetoothEnabled(): Boolean = bleScanner.isBluetoothEnabled()
}
