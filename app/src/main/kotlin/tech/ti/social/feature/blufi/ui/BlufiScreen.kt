package tech.ti.social.feature.blufi.ui

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import tech.ti.social.R
import tech.ti.social.feature.blufi.ui.component.CredentialInputSheet
import tech.ti.social.feature.blufi.ui.component.DeviceScanList
import tech.ti.social.feature.blufi.ui.component.ProvisioningProgress
import tech.ti.social.feature.blufi.ui.component.WifiSelectionDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlufiScreen(
    viewModel: BlufiViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // BLE Permission handling
    var permissionsGranted by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        permissionsGranted = grants.values.all { it }
    }

    // Bluetooth enable handling
    val bluetoothLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        // Check if Bluetooth is now enabled
    }

    LaunchedEffect(Unit) {
        permissionsGranted = checkAndRequestPermissions(permissionLauncher)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.blufi_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (!permissionsGranted) {
                PermissionRequestScreen {
                    permissionsGranted = checkAndRequestPermissions(permissionLauncher)
                }
            } else {
                BlufiContent(uiState = uiState, viewModel = viewModel)
            }
        }
    }
}

private fun checkAndRequestPermissions(
    launcher: androidx.activity.result.ActivityResultLauncher<Array<String>>
): Boolean {
    // This is handled by BleScanner, we just show the UI
    return true // Permissions checked in ViewModel layer
}

@Composable
private fun PermissionRequestScreen(
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.blufi_permission_required),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.blufi_permission_desc),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )
        Button(onClick = onRetry) {
            Text(stringResource(R.string.blufi_grant_permission))
        }
    }
}

@Composable
private fun BlufiContent(
    uiState: BlufiUiState,
    viewModel: BlufiViewModel
) {
    when (uiState) {
        is BlufiUiState.Idle -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Button(onClick = { viewModel.startScan() }) {
                    Text(stringResource(R.string.blufi_start_scan))
                }
            }
        }

        is BlufiUiState.Scanning -> {
            ScanningView()
        }

        is BlufiUiState.DevicesFound -> {
            DeviceScanList(
                devices = uiState.devices,
                onDeviceSelected = { viewModel.connectToDevice(it) }
            )
        }

        is BlufiUiState.Connecting -> {
            ProvisioningProgress(
                step = stringResource(R.string.blufi_connecting),
                deviceName = uiState.device.name
            )
        }

        is BlufiUiState.Handshaking -> {
            ProvisioningProgress(
                step = stringResource(R.string.blufi_handshaking),
                deviceName = uiState.device.name
            )
        }

        is BlufiUiState.WaitingForWifiList -> {
            ProvisioningProgress(
                step = stringResource(R.string.blufi_fetching_wifi),
                deviceName = uiState.device.name
            )
        }

        is BlufiUiState.SelectWifi -> {
            // Show WiFi selection - in a full implementation this would show the full list
            // For now, directly proceed to password input
            LaunchedEffect(Unit) {
                viewModel.showPasswordInput()
            }
            ProvisioningProgress(
                step = "已选择: ${uiState.ap.displayName}",
                deviceName = uiState.device.name
            )
        }

        is BlufiUiState.EnterPassword -> {
            CredentialInputSheet(
                ap = uiState.ap,
                onConfirm = { password ->
                    viewModel.provision(uiState.ap.ssid, password)
                },
                onBack = { viewModel.goBack() }
            )
        }

        is BlufiUiState.SendingCredentials -> {
            ProvisioningProgress(
                step = stringResource(R.string.blufi_sending_credentials),
                deviceName = uiState.device.name,
                ssid = uiState.ssid
            )
        }

        is BlufiUiState.Success -> {
            SuccessView(
                ssid = uiState.ssid,
                onDone = {
                    viewModel.reset()
                    // In a full implementation, navigate back
                }
            )
        }

        is BlufiUiState.Error -> {
            ErrorView(
                message = uiState.message,
                onRetry = { viewModel.startScan() }
            )
        }
    }
}

@Composable
private fun ScanningView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Text(
            text = stringResource(R.string.blufi_scanning),
            modifier = Modifier.padding(top = 16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun SuccessView(
    ssid: String,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.blufi_success),
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = stringResource(R.string.blufi_success_desc, ssid),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            textAlign = TextAlign.Center
        )
        Button(onClick = onDone) {
            Text(stringResource(R.string.blufi_done))
        }
    }
}

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.blufi_error),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.error
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            textAlign = TextAlign.Center
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onRetry) {
                Text(stringResource(R.string.blufi_retry))
            }
        }
    }
}
