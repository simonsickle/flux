package dev.simonsickle.flux.feature.addons

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.simonsickle.flux.core.model.InstalledAddon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddonsRoute(
    onNavigateUp: () -> Unit,
    viewModel: AddonsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showInstallDialog by remember { mutableStateOf(false) }
    var installUrl by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Addons") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                modifier = Modifier.fillMaxSize().padding(paddingValues),
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
            LazyColumn(modifier = Modifier.padding(paddingValues)) {
                items(uiState.addons, key = { it.manifest.id }) { addon ->
                    AddonItem(
                        addon = addon,
                        onToggle = { viewModel.toggleAddon(addon.manifest.id, it) },
                        onRemove = { viewModel.removeAddon(addon.manifest.id) }
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
                TextButton(onClick = {
                    viewModel.installAddon(installUrl)
                    installUrl = ""
                    showInstallDialog = false
                }) {
                    Text("Install")
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
private fun AddonItem(
    addon: InstalledAddon,
    onToggle: (Boolean) -> Unit,
    onRemove: () -> Unit
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
                Switch(checked = addon.enabled, onCheckedChange = onToggle)
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove")
                }
            }
        }
    )
}
