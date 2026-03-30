package dev.simonsickle.flux.feature.addons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dev.simonsickle.flux.core.common.tvContentPadding
import dev.simonsickle.flux.core.model.InstalledAddon

@Composable
fun TvAddonsScreen(
    uiState: AddonsUiState,
    onNavigateUp: () -> Unit,
    onInstallAddon: (String) -> Unit,
    onToggleAddon: (String, Boolean) -> Unit,
    onRemoveAddon: (String) -> Unit,
    onMoveAddon: (String, Int) -> Unit
) {
    var installUrl by rememberSaveable { mutableStateOf("") }
    val installAction = {
        onInstallAddon(installUrl)
        installUrl = ""
    }

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
            Text("Addon Manager", style = MaterialTheme.typography.headlineMedium)
        }
        item {
            Card {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Install addon", style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(
                        value = installUrl,
                        onValueChange = { installUrl = it },
                        label = { Text("Transport URL") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Done,
                            autoCorrectEnabled = false,
                            capitalization = KeyboardCapitalization.None
                        ),
                        keyboardActions = KeyboardActions(onDone = { installAction() }),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(onClick = installAction, enabled = !uiState.isInstalling) {
                        Text(if (uiState.isInstalling) "Installing..." else "Install")
                    }
                    uiState.installSuccessMessage?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.primary)
                    }
                    uiState.installError?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
        itemsIndexed(uiState.addons, key = { _, item -> item.manifest.id }) { index, addon ->
            TvAddonCard(
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

@Composable
private fun TvAddonCard(
    addon: InstalledAddon,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onToggle: (Boolean) -> Unit,
    onRemove: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(addon.manifest.name, style = MaterialTheme.typography.titleLarge)
            Text(addon.manifest.description, style = MaterialTheme.typography.bodyMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onMoveUp, enabled = canMoveUp) {
                        Text("Move Up")
                    }
                    Button(onClick = onMoveDown, enabled = canMoveDown) {
                        Text("Move Down")
                    }
                    Button(onClick = onRemove) {
                        Text("Remove")
                    }
                }
                Switch(
                    checked = addon.enabled,
                    onCheckedChange = onToggle
                )
            }
        }
    }
}
