package tech.ti.social.feature.blufi.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tech.ti.social.feature.blufi.data.model.WifiAccessPoint

@Composable
fun CredentialInputSheet(
    ap: WifiAccessPoint,
    onConfirm: (String) -> Unit,
    onBack: () -> Unit
) {
    var password by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "连接到 WiFi",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = ap.displayName,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("WiFi 密码") },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null)
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            maxLines = 1
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Button(
                onClick = { onConfirm(password) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("连接")
            }
            TextButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("返回")
            }
        }
    }
}
