package dev.simonsickle.flux.feature.settings.sync

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.simonsickle.flux.core.sync.SyncDiscovery
import dev.simonsickle.flux.core.sync.SyncImportOptions
import dev.simonsickle.flux.core.sync.SyncManager.ReceiverState
import dev.simonsickle.flux.core.sync.SyncPayload

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MobileSyncReceiveScreen(
    state: ReceiverState,
    onStartDiscovery: () -> Unit,
    onSelectDevice: (SyncDiscovery.DiscoveredDevice) -> Unit,
    onConnectManually: (String, Int) -> Unit,
    onSubmitPin: (SyncDiscovery.DiscoveredDevice, String) -> Unit,
    onImport: (SyncPayload, SyncImportOptions) -> Unit,
    onReset: () -> Unit,
    onNavigateUp: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Receive Settings") },
                navigationIcon = {
                    IconButton(onClick = {
                        onReset()
                        onNavigateUp()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (state) {
                is ReceiverState.Idle -> item {
                    IdleContent(onStartDiscovery)
                }

                is ReceiverState.Discovering -> item {
                    DiscoveringContent(state.devices, onSelectDevice, onConnectManually)
                }

                is ReceiverState.EnterPin -> item {
                    PinEntryContent(state.device, null, onSubmitPin)
                }

                is ReceiverState.PinError -> item {
                    PinEntryContent(state.device, state.message, onSubmitPin)
                }

                is ReceiverState.Connecting -> item {
                    ConnectingContent()
                }

                is ReceiverState.ReviewPayload -> item {
                    ReviewContent(state.payload, onImport)
                }

                is ReceiverState.Importing -> item {
                    ImportingContent()
                }

                is ReceiverState.Complete -> item {
                    CompleteContent(onNavigateUp)
                }

                is ReceiverState.Error -> item {
                    ErrorContent(state.message, onReset)
                }
            }
        }
    }
}

@Composable
private fun IdleContent(onStart: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(Modifier.height(32.dp))
        Text(
            "Receive settings from another device",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Text(
            "Make sure the other device has started sharing, " +
                "and both devices are on the same WiFi network.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) {
            Text("Search for Devices")
        }
    }
}

@Composable
private fun DiscoveringContent(
    devices: List<SyncDiscovery.DiscoveredDevice>,
    onSelect: (SyncDiscovery.DiscoveredDevice) -> Unit,
    onConnectManually: (String, Int) -> Unit
) {
    var manualHost by rememberSaveable { mutableStateOf("") }
    var manualPort by rememberSaveable { mutableStateOf("") }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CircularProgressIndicator()
            Text("Searching for devices...", style = MaterialTheme.typography.titleMedium)
        }

        if (devices.isNotEmpty()) {
            Text("Found devices:", style = MaterialTheme.typography.labelLarge)
            devices.forEach { device ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(device) },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(device.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${device.host}:${device.port}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text("Or connect manually:", style = MaterialTheme.typography.labelLarge)

        OutlinedTextField(
            value = manualHost,
            onValueChange = { manualHost = it },
            label = { Text("IP Address") },
            placeholder = { Text("192.168.1.100") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = manualPort,
            onValueChange = { manualPort = it.filter { c -> c.isDigit() } },
            label = { Text("Port") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Button(
            onClick = {
                val port = manualPort.toIntOrNull() ?: return@Button
                onConnectManually(manualHost, port)
            },
            enabled = manualHost.isNotBlank() && manualPort.isNotBlank()
        ) {
            Text("Connect")
        }
    }
}

@Composable
private fun PinEntryContent(
    device: SyncDiscovery.DiscoveredDevice,
    errorMessage: String?,
    onSubmit: (SyncDiscovery.DiscoveredDevice, String) -> Unit
) {
    var pin by rememberSaveable { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            "Enter the PIN shown on ${device.name}",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )

        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        OutlinedTextField(
            value = pin,
            onValueChange = { if (it.length <= 6) pin = it.filter { c -> c.isDigit() } },
            label = { Text("6-digit PIN") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { onSubmit(device, pin) },
            enabled = pin.length == 6,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Connect")
        }
    }
}

@Composable
private fun ConnectingContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(Modifier.height(32.dp))
        CircularProgressIndicator()
        Text("Connecting and fetching settings...", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun ReviewContent(
    payload: SyncPayload,
    onImport: (SyncPayload, SyncImportOptions) -> Unit
) {
    var importSettings by rememberSaveable { mutableStateOf(true) }
    var importToken by rememberSaveable { mutableStateOf(true) }
    var importAddons by rememberSaveable { mutableStateOf(true) }
    var importBookmarks by rememberSaveable { mutableStateOf(false) }
    var importHistory by rememberSaveable { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Review & Import", style = MaterialTheme.typography.titleLarge)
        Text(
            "Choose what to import from the other device:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        ImportCheckbox("Settings (player, subtitles, content type)", importSettings) { importSettings = it }
        ImportCheckbox(
            "Real-Debrid token${if (payload.settings.realDebridToken != null) " (token found)" else " (none)"}",
            importToken && payload.settings.realDebridToken != null,
            enabled = payload.settings.realDebridToken != null
        ) { importToken = it }
        ImportCheckbox("Installed addons (${payload.addons.size})", importAddons, enabled = payload.addons.isNotEmpty()) { importAddons = it }
        ImportCheckbox("Bookmarks (${payload.bookmarks.size})", importBookmarks, enabled = payload.bookmarks.isNotEmpty()) { importBookmarks = it }
        ImportCheckbox("Watch history (${payload.watchHistory.size})", importHistory, enabled = payload.watchHistory.isNotEmpty()) { importHistory = it }

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                onImport(
                    payload,
                    SyncImportOptions(
                        settings = importSettings,
                        realDebridToken = importToken,
                        addons = importAddons,
                        bookmarks = importBookmarks,
                        watchHistory = importHistory
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Import Selected")
        }
    }
}

@Composable
private fun ImportCheckbox(
    label: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ImportingContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(Modifier.height(32.dp))
        CircularProgressIndicator()
        Text("Importing settings...", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun CompleteContent(onDone: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(Modifier.height(32.dp))
        Text(
            "Settings imported successfully!",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Text(
            "Your device is now configured.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Button(onClick = onDone) {
            Text("Done")
        }
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(Modifier.height(32.dp))
        Text(
            "Connection failed",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.error
        )
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Button(onClick = onRetry) {
            Text("Try Again")
        }
    }
}
