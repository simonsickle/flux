package dev.simonsickle.flux.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileSettingsScreen(
    uiState: SettingsUiState,
    onNavigateUp: () -> Unit,
    onSetRealDebridToken: (String) -> Unit,
    onSetDefaultContentType: (String) -> Unit,
    onSetPreferredPlayer: (String) -> Unit,
    onSetSubtitleLanguage: (String) -> Unit,
    onSetHardwareAcceleration: (Boolean) -> Unit,
    onTestDebridConnection: () -> Unit,
    initialToken: String
) {
    var tokenVisible by rememberSaveable { mutableStateOf(false) }
    var tokenInput by rememberSaveable(initialToken) { mutableStateOf(initialToken) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SettingsDebridSection(
                    tokenInput = tokenInput,
                    tokenVisible = tokenVisible,
                    onTokenInputChange = { tokenInput = it },
                    onToggleTokenVisibility = { tokenVisible = !tokenVisible },
                    onSaveToken = { onSetRealDebridToken(tokenInput) },
                    onTestConnection = onTestDebridConnection,
                    connectionResult = uiState.connectionTestResult,
                    username = uiState.debridUserInfo?.username,
                    isTestingConnection = uiState.isTestingConnection
                )
            }
            item {
                HorizontalDivider()
            }
            item {
                SettingsPickerRow(
                    title = "Default content type",
                    value = uiState.defaultContentType,
                    options = listOf("movie", "series", "channel"),
                    onValueSelected = onSetDefaultContentType
                )
            }
            item {
                SettingsPickerRow(
                    title = "Preferred player",
                    value = uiState.preferredPlayer,
                    options = listOf("media3", "vlc"),
                    onValueSelected = onSetPreferredPlayer
                )
            }
            item {
                SettingsPickerRow(
                    title = "Subtitle language",
                    value = uiState.subtitleLanguage,
                    options = listOf("eng", "spa", "fre", "ger", "ita", "por"),
                    onValueSelected = onSetSubtitleLanguage
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Hardware acceleration")
                    Switch(
                        checked = uiState.hardwareAcceleration,
                        onCheckedChange = onSetHardwareAcceleration
                    )
                }
            }
            item {
                HorizontalDivider()
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("About", style = MaterialTheme.typography.titleLarge)
                    Text("Flux v1.0.0", style = MaterialTheme.typography.bodyMedium)
                    Text("Stremio-compatible streaming app", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
internal fun SettingsDebridSection(
    tokenInput: String,
    tokenVisible: Boolean,
    onTokenInputChange: (String) -> Unit,
    onToggleTokenVisibility: () -> Unit,
    onSaveToken: () -> Unit,
    onTestConnection: () -> Unit,
    connectionResult: String?,
    username: String?,
    isTestingConnection: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Real-Debrid", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = tokenInput,
            onValueChange = onTokenInputChange,
            label = { Text("API Token") },
            visualTransformation = if (tokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = onToggleTokenVisibility) {
                    Icon(
                        if (tokenVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle visibility"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onSaveToken) {
                Text("Save Token")
            }
            TextButton(onClick = onTestConnection, enabled = !isTestingConnection) {
                Text(if (isTestingConnection) "Testing..." else "Test Connection")
            }
        }
        username?.let {
            Text("Signed in as $it", style = MaterialTheme.typography.bodyMedium)
        }
        connectionResult?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = if (it.contains("success", ignoreCase = true)) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }
    }
}

@Composable
internal fun SettingsPickerRow(
    title: String,
    value: String,
    options: List<String>,
    onValueSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(
            text = "Current: $value",
            style = MaterialTheme.typography.bodyMedium
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                Button(onClick = { onValueSelected(option) }) {
                    Text(option)
                }
            }
        }
    }
}
