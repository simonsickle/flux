package dev.simonsickle.flux.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import dev.simonsickle.flux.core.common.tvContentPadding

@Composable
fun TvSettingsScreen(
    uiState: SettingsUiState,
    onNavigateUp: () -> Unit,
    onSetRealDebridToken: (String) -> Unit,
    onSetDefaultContentType: (String) -> Unit,
    onSetPreferredPlayer: (String) -> Unit,
    onSetSubtitleLanguage: (String) -> Unit,
    onSetHardwareAcceleration: (Boolean) -> Unit,
    onTestDebridConnection: () -> Unit,
    onNavigateToSyncSend: () -> Unit = {},
    onNavigateToSyncReceive: () -> Unit = {},
    initialToken: String
) {
    var tokenInput by rememberSaveable(initialToken) { mutableStateOf(initialToken) }
    var tokenVisible by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(tvContentPadding()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Button(onClick = onNavigateUp) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Text("Back")
            }
        }
        item {
            Text("Settings", style = MaterialTheme.typography.headlineMedium)
        }
        item {
            Card(onClick = {}) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Real-Debrid", style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(
                        value = tokenInput,
                        onValueChange = { tokenInput = it },
                        label = { Text("API token") },
                        visualTransformation = if (tokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = { onSetRealDebridToken(tokenInput) }) {
                            Text("Save")
                        }
                        Button(onClick = { tokenVisible = !tokenVisible }) {
                            Text(if (tokenVisible) "Hide Token" else "Show Token")
                        }
                        Button(onClick = onTestDebridConnection, enabled = !uiState.isTestingConnection) {
                            Text(if (uiState.isTestingConnection) "Testing..." else "Test")
                        }
                    }
                    uiState.connectionTestResult?.let {
                        Text(it, color = if (it.contains("success", true)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                    }
                    uiState.debridUserInfo?.let {
                        Text("User: ${it.username} • Points: ${it.points}")
                    }
                }
            }
        }
        item {
            TvOptionCard(
                title = "Default content type",
                value = uiState.defaultContentType,
                options = listOf("movie", "series", "channel"),
                onSelect = onSetDefaultContentType
            )
        }
        item {
            TvOptionCard(
                title = "Preferred player",
                value = uiState.preferredPlayer,
                options = listOf("media3", "vlc"),
                onSelect = onSetPreferredPlayer
            )
        }
        item {
            TvOptionCard(
                title = "Subtitle language",
                value = uiState.subtitleLanguage,
                options = listOf("eng", "spa", "fre", "ger", "ita", "por"),
                onSelect = onSetSubtitleLanguage
            )
        }
        item {
            Card(onClick = {}) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Hardware acceleration", style = MaterialTheme.typography.titleMedium)
                    Switch(
                        checked = uiState.hardwareAcceleration,
                        onCheckedChange = onSetHardwareAcceleration
                    )
                }
            }
        }
        item {
            Card(onClick = {}) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Sync Settings", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Transfer settings between your devices over WiFi",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = onNavigateToSyncSend) {
                            Text("Share to Device")
                        }
                        Button(onClick = onNavigateToSyncReceive) {
                            Text("Receive from Device")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TvOptionCard(
    title: String,
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    Card(onClick = {}) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text("Current: $value", style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                options.forEach { option ->
                    Button(onClick = { onSelect(option) }) {
                        Text(option)
                    }
                }
            }
        }
    }
}
