package tech.ti.social.feature.blufi.domain.usecase

import kotlinx.coroutines.flow.Flow
import tech.ti.social.feature.blufi.data.model.BlufiDevice
import tech.ti.social.feature.blufi.domain.BlufiRepository
import javax.inject.Inject

class ScanDevicesUseCase @Inject constructor(
    private val repository: BlufiRepository
) {
    operator fun invoke(): Flow<List<BlufiDevice>> = repository.scanDevices()
}
