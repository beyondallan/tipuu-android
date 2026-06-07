package tech.ti.social.feature.blufi.domain.usecase

import tech.ti.social.feature.blufi.domain.BlufiRepository
import javax.inject.Inject

class ProvisionDeviceUseCase @Inject constructor(
    private val repository: BlufiRepository
) {
    suspend operator fun invoke(ssid: String, password: String): Result<Unit> {
        return repository.provisionDevice(ssid, password)
    }
}
