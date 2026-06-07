package tech.ti.social.feature.blufi.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.ti.social.feature.blufi.data.model.BlufiDevice
import tech.ti.social.feature.blufi.data.model.WifiAccessPoint
import tech.ti.social.feature.blufi.domain.usecase.ConnectToDeviceUseCase
import tech.ti.social.feature.blufi.domain.usecase.ProvisionDeviceUseCase
import tech.ti.social.feature.blufi.domain.usecase.ScanDevicesUseCase
import javax.inject.Inject

@HiltViewModel
class BlufiViewModel @Inject constructor(
    private val scanDevicesUseCase: ScanDevicesUseCase,
    private val connectToDeviceUseCase: ConnectToDeviceUseCase,
    private val provisionDeviceUseCase: ProvisionDeviceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<BlufiUiState>(BlufiUiState.Idle)
    val uiState: StateFlow<BlufiUiState> = _uiState

    /**
     * Start scanning for BluFi devices.
     */
    fun startScan() {
        viewModelScope.launch {
            _uiState.update { BlufiUiState.Scanning }
            scanDevicesUseCase()
                .catch { e ->
                    _uiState.update { BlufiUiState.Error(e.message ?: "Scan failed") }
                }
                .collect { devices ->
                    _uiState.update { BlufiUiState.DevicesFound(devices) }
                }
        }
    }

    /**
     * Connect to a selected device.
     */
    fun connectToDevice(device: BlufiDevice) {
        viewModelScope.launch {
            _uiState.update { BlufiUiState.Connecting(device) }
            connectToDeviceUseCase(device.address)
                .onSuccess {
                    _uiState.update { BlufiUiState.Handshaking(device) }
                    startProvisioning(device)
                }
                .onFailure { e ->
                    _uiState.update { BlufiUiState.Error(e.message ?: "Connection failed") }
                }
        }
    }

    /**
     * Start provisioning with selected SSID and password.
     */
    fun provision(ssid: String, password: String) {
        val current = _uiState.value
        val device = when (current) {
            is BlufiUiState.SelectWifi -> current.device
            is BlufiUiState.EnterPassword -> current.device
            else -> return
        }

        viewModelScope.launch {
            _uiState.update { BlufiUiState.SendingCredentials(device, ssid) }
            provisionDeviceUseCase(ssid, password)
                .onSuccess {
                    _uiState.update { BlufiUiState.Success(device, ssid) }
                }
                .onFailure { e ->
                    _uiState.update { BlufiUiState.Error(e.message ?: "Provisioning failed") }
                }
        }
    }

    /**
     * Select a WiFi network from the list.
     */
    fun selectWifi(ap: WifiAccessPoint) {
        val current = _uiState.value
        val device = when (current) {
            is BlufiUiState.WaitingForWifiList -> current.device
            else -> return
        }
        _uiState.update { BlufiUiState.SelectWifi(device, ap) }
    }

    /**
     * Show password input for selected AP.
     */
    fun showPasswordInput() {
        val current = _uiState.value
        if (current is BlufiUiState.SelectWifi) {
            _uiState.update { BlufiUiState.EnterPassword(current.device, current.ap) }
        }
    }

    /**
     * Go back to previous state.
     */
    fun goBack() {
        val current = _uiState.value
        when (current) {
            is BlufiUiState.EnterPassword -> {
                _uiState.update { BlufiUiState.SelectWifi(current.device, current.ap) }
            }
            is BlufiUiState.SelectWifi -> {
                _uiState.update { BlufiUiState.WaitingForWifiList(current.device) }
            }
            else -> {
                _uiState.update { BlufiUiState.Idle }
            }
        }
    }

    /**
     * Reset to idle state.
     */
    fun reset() {
        _uiState.update { BlufiUiState.Idle }
    }

    /**
     * Start provisioning flow after connection (internal).
     */
    private suspend fun startProvisioning(device: BlufiDevice) {
        // The protocol handles the full flow internally,
        // but we expose WiFi list for user selection first.
        // For now, we'll let the user select WiFi after connection.
        _uiState.update { BlufiUiState.WaitingForWifiList(device) }
    }
}

/**
 * UI state for the BluFi provisioning flow.
 */
sealed class BlufiUiState {
    data object Idle : BlufiUiState()
    data object Scanning : BlufiUiState()
    data class DevicesFound(val devices: List<BlufiDevice>) : BlufiUiState()
    data class Connecting(val device: BlufiDevice) : BlufiUiState()
    data class Handshaking(val device: BlufiDevice) : BlufiUiState()
    data class WaitingForWifiList(val device: BlufiDevice) : BlufiUiState()
    data class SelectWifi(val device: BlufiDevice, val ap: WifiAccessPoint) : BlufiUiState()
    data class EnterPassword(val device: BlufiDevice, val ap: WifiAccessPoint) : BlufiUiState()
    data class SendingCredentials(val device: BlufiDevice, val ssid: String) : BlufiUiState()
    data class Success(val device: BlufiDevice, val ssid: String) : BlufiUiState()
    data class Error(val message: String) : BlufiUiState()
}
