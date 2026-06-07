package tech.ti.social.feature.blufi.domain.usecase

import tech.ti.social.feature.blufi.domain.BlufiRepository
import javax.inject.Inject

class ConnectToDeviceUseCase @Inject constructor(
    private val repository: BlufiRepository
) {
    suspend operator fun invoke(address: String): Result<Unit> {
        return repository.connectToDevice(address)
    }
}
