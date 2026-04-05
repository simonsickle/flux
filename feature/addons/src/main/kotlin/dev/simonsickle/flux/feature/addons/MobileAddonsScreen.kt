package dev.simonsickle.flux.feature.addons

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.simonsickle.flux.core.model.InstalledAddon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileAddonsScreen(
    uiState: AddonsUiState,
    onNavigateUp: () -> Unit,
    onInstallAddon: (String) -> Unit,
    onToggleAddon: (String, Boolean) -> Unit,
    onRemoveAddon: (String) -> Unit,
    onMoveAddon: (String, Int) -> Unit,
    onSetTimeout: (String, Long) -> Unit = { _, _ -> }
) {
    var showInstallDialog by rememberSaveable { mutableStateOf(false) }
    var installUrl by rememberSaveable { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.installError, uiState.installSuccessMessage) {
        uiState.installError?.let { snackbarHostState.showSnackbar(it) }
        uiState.installSuccessMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Addons") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showInstallDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Install addon")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.addons.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No addons installed")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { showInstallDialog = true }) {
                        Text("Install Addon")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                itemsIndexed(uiState.addons, key = { _, item -> item.manifest.id }) { index, addon ->
                    MobileAddonItem(
                        addon = addon,
                        canMoveUp = index > 0,
                        canMoveDown = index < uiState.addons.lastIndex,
                        onToggle = { onToggleAddon(addon.manifest.id, it) },
                        onRemove = { onRemoveAddon(addon.manifest.id) },
                        onMoveUp = { onMoveAddon(addon.manifest.id, index - 1) },
                        onMoveDown = { onMoveAddon(addon.manifest.id, index + 1) },
                        onSetTimeout = { onSetTimeout(addon.manifest.id, it) }
                    )
                }
            }
        }
    }

    if (showInstallDialog) {
        val isValidUrl = remember(installUrl) {
            installUrl.isNotBlank() && runCatching {
                val uri = java.net.URI(installUrl.trim())
                uri.scheme?.lowercase() in listOf("http", "https") && !uri.host.isNullOrBlank()
            }.getOrDefault(false)
        }

        AlertDialog(
            onDismissRequest = {
                if (!uiState.isInstalling) {
                    showInstallDialog = false
                }
            },
            title = { Text("Install Addon") },
            text = {
                Column {
                    OutlinedTextField(
                        value = installUrl,
                        onValueChange = { installUrl = it },
                        label = { Text("Transport URL") },
                        placeholder = { Text("https://addon-host.com/manifest.json") },
                        isError = installUrl.isNotBlank() && !isValidUrl,
                        supportingText = if (installUrl.isNotBlank() && !isValidUrl) {
                            { Text("Enter a valid HTTP or HTTPS URL") }
                        } else null,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (uiState.installError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.installError,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !uiState.isInstalling && isValidUrl,
                    onClick = { onInstallAddon(installUrl.trim()) }
                ) {
                    Text(if (uiState.isInstalling) "Installing..." else "Install")
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !uiState.isInstalling,
                    onClick = { showInstallDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Close dialog and clear URL on successful install
    LaunchedEffect(uiState.installSuccessMessage) {
        if (uiState.installSuccessMessage != null) {
            installUrl = ""
            showInstallDialog = false
        }
    }
}

@Composable
private fun MobileAddonItem(
    addon: InstalledAddon,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onToggle: (Boolean) -> Unit,
    onRemove: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onSetTimeout: (Long) -> Unit
) {
    var showTimeoutDialog by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = { Text(addon.manifest.name) },
        supportingContent = {
            Column {
                Text("v${addon.manifest.version}")
                if (addon.manifest.resources.isNotEmpty()) {
                    Text(
                        addon.manifest.resources.joinToString(" | ") { it.replaceFirstChar(Char::titlecase) },
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(addon.manifest.description, maxLines = 2)
                Text(
                    "Timeout: ${addon.timeoutMs / 1000}s",
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { showTimeoutDialog = true }
                )
            }
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onMoveUp, enabled = canMoveUp) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move up")
                }
                IconButton(onClick = onMoveDown, enabled = canMoveDown) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move down")
                }
                Switch(checked = addon.enabled, onCheckedChange = onToggle)
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove")
                }
            }
        }
    )

    if (showTimeoutDialog) {
        TimeoutDialog(
            currentTimeoutMs = addon.timeoutMs,
            onDismiss = { showTimeoutDialog = false },
            onConfirm = { newTimeout ->
                onSetTimeout(newTimeout)
                showTimeoutDialog = false
            }
        )
    }
}

@Composable
private fun TimeoutDialog(
    currentTimeoutMs: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var timeoutSeconds by remember { mutableStateOf((currentTimeoutMs / 1000).toString()) }
    val isValid = remember(timeoutSeconds) {
        timeoutSeconds.toLongOrNull()?.let { it in 5..60 } ?: false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Request Timeout") },
        text = {
            Column {
                Text("How long to wait for this addon to respond (5-60 seconds).")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = timeoutSeconds,
                    onValueChange = { timeoutSeconds = it.filter(Char::isDigit) },
                    label = { Text("Timeout (seconds)") },
                    isError = timeoutSeconds.isNotBlank() && !isValid,
                    supportingText = if (timeoutSeconds.isNotBlank() && !isValid) {
                        { Text("Must be between 5 and 60") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = isValid,
                onClick = { onConfirm(timeoutSeconds.toLong() * 1000) }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
