package dev.simonsickle.flux.feature.settings.sync

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.simonsickle.flux.core.common.tvContentPadding
import dev.simonsickle.flux.core.sync.SyncDiscovery
import dev.simonsickle.flux.core.sync.SyncImportOptions
import dev.simonsickle.flux.core.sync.SyncManager.ReceiverState
import dev.simonsickle.flux.core.sync.SyncPayload

@Composable
internal fun TvSyncReceiveScreen(
    state: ReceiverState,
    onStartDiscovery: () -> Unit,
    onSelectDevice: (SyncDiscovery.DiscoveredDevice) -> Unit,
    onConnectManually: (String, Int) -> Unit,
    onSubmitPin: (SyncDiscovery.DiscoveredDevice, String) -> Unit,
    onImport: (SyncPayload, SyncImportOptions) -> Unit,
    onReset: () -> Unit,
    onNavigateUp: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(tvContentPadding()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Button(onClick = {
                onReset()
                onNavigateUp()
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Text("Back")
            }
        }

        item {
            Text("Receive Settings", style = MaterialTheme.typography.headlineMedium)
        }

        when (state) {
            is ReceiverState.Idle -> item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "Receive settings from another device on the same WiFi network.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Button(onClick = onStartDiscovery) {
                        Text("Search for Devices")
                    }
                }
            }

            is ReceiverState.Discovering -> {
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator()
                        Text("Searching for devices...", style = MaterialTheme.typography.titleMedium)
                    }
                }

                if (state.devices.isNotEmpty()) {
                    item { Text("Found devices:", style = MaterialTheme.typography.titleMedium) }
                    state.devices.forEach { device ->
                        item {
                            Card(
                                onClick = { onSelectDevice(device) },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
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
                }

                item {
                    TvManualConnect(onConnectManually)
                }
            }

            is ReceiverState.EnterPin -> item {
                TvPinEntry(state.device, null, onSubmitPin)
            }

            is ReceiverState.PinError -> item {
                TvPinEntry(state.device, state.message, onSubmitPin)
            }

            is ReceiverState.Connecting -> item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator()
                    Text("Connecting...", style = MaterialTheme.typography.titleLarge)
                }
            }

            is ReceiverState.ReviewPayload -> item {
                TvReviewContent(state.payload, onImport)
            }

            is ReceiverState.Importing -> item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator()
                    Text("Importing settings...", style = MaterialTheme.typography.titleLarge)
                }
            }

            is ReceiverState.Complete -> item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "Settings imported successfully!",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Button(onClick = onNavigateUp) {
                        Text("Done")
                    }
                }
            }

            is ReceiverState.Error -> item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Error", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error)
                    Text(state.message, style = MaterialTheme.typography.bodyLarge)
                    Button(onClick = onReset) {
                        Text("Try Again")
                    }
                }
            }
        }
    }
}

@Composable
private fun TvManualConnect(onConnect: (String, Int) -> Unit) {
    var host by rememberSaveable { mutableStateOf("") }
    var port by rememberSaveable { mutableStateOf("") }

    Card(onClick = {}, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Manual Connection", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = host,
                onValueChange = { host = it },
                label = { Text("IP Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = port,
                onValueChange = { port = it.filter { c -> c.isDigit() } },
                label = { Text("Port") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Button(
                onClick = {
                    val p = port.toIntOrNull() ?: return@Button
                    onConnect(host, p)
                },
                enabled = host.isNotBlank() && port.isNotBlank()
            ) {
                Text("Connect")
            }
        }
    }
}

@Composable
private fun TvPinEntry(
    device: SyncDiscovery.DiscoveredDevice,
    errorMessage: String?,
    onSubmit: (SyncDiscovery.DiscoveredDevice, String) -> Unit
) {
    var pin by rememberSaveable { mutableStateOf("") }

    Card(onClick = {}, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Enter the PIN shown on ${device.name}",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )

            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
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
                enabled = pin.length == 6
            ) {
                Text("Connect")
            }
        }
    }
}

@Composable
private fun TvReviewContent(
    payload: SyncPayload,
    onImport: (SyncPayload, SyncImportOptions) -> Unit
) {
    var importSettings by rememberSaveable { mutableStateOf(true) }
    var importToken by rememberSaveable { mutableStateOf(true) }
    var importAddons by rememberSaveable { mutableStateOf(true) }
    var importBookmarks by rememberSaveable { mutableStateOf(false) }
    var importHistory by rememberSaveable { mutableStateOf(false) }

    Card(onClick = {}, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Review & Import", style = MaterialTheme.typography.titleLarge)
            Text(
                "Choose what to import:",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            TvImportOption("Settings (player, subtitles, content type)", importSettings) { importSettings = it }
            TvImportOption(
                "Real-Debrid token${if (payload.settings.realDebridToken != null) " (found)" else " (none)"}",
                importToken && payload.settings.realDebridToken != null,
                enabled = payload.settings.realDebridToken != null
            ) { importToken = it }
            TvImportOption("Addons (${payload.addons.size})", importAddons, enabled = payload.addons.isNotEmpty()) { importAddons = it }
            TvImportOption("Bookmarks (${payload.bookmarks.size})", importBookmarks, enabled = payload.bookmarks.isNotEmpty()) { importBookmarks = it }
            TvImportOption("Watch history (${payload.watchHistory.size})", importHistory, enabled = payload.watchHistory.isNotEmpty()) { importHistory = it }

            Spacer(Modifier.height(8.dp))
            Button(onClick = {
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
            }) {
                Text("Import Selected")
            }
        }
    }
}

@Composable
private fun TvImportOption(
    label: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
