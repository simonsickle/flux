package dev.simonsickle.flux.feature.addons

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
    onMoveAddon: (String, Int) -> Unit
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
                        onMoveDown = { onMoveAddon(addon.manifest.id, index + 1) }
                    )
                }
            }
        }
    }

    if (showInstallDialog) {
        AlertDialog(
            onDismissRequest = { showInstallDialog = false },
            title = { Text("Install Addon") },
            text = {
                OutlinedTextField(
                    value = installUrl,
                    onValueChange = { installUrl = it },
                    label = { Text("Transport URL") },
                    placeholder = { Text("https://...") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !uiState.isInstalling,
                    onClick = {
                        onInstallAddon(installUrl)
                        installUrl = ""
                        showInstallDialog = false
                    }
                ) {
                    Text(if (uiState.isInstalling) "Installing..." else "Install")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInstallDialog = false }) {
                    Text("Cancel")
                }
            }
        )
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
    onMoveDown: () -> Unit
) {
    ListItem(
        headlineContent = { Text(addon.manifest.name) },
        supportingContent = {
            Column {
                Text("v${addon.manifest.version}")
                Text(addon.manifest.description, maxLines = 2)
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
}
