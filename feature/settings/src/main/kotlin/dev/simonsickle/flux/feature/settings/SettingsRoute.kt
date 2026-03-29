package dev.simonsickle.flux.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsRoute(
    onNavigateUp: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var tokenVisible by remember { mutableStateOf(false) }
    var tokenInput by remember(uiState.realDebridToken) { mutableStateOf(uiState.realDebridToken) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text("Real-Debrid", style = MaterialTheme.typography.titleLarge)
            }
            item {
                OutlinedTextField(
                    value = tokenInput,
                    onValueChange = { tokenInput = it },
                    label = { Text("API Token") },
                    visualTransformation = if (tokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { tokenVisible = !tokenVisible }) {
                            Icon(
                                if (tokenVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle visibility"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Button(
                    onClick = { viewModel.setRealDebridToken(tokenInput) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Token")
                }
            }
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Player", style = MaterialTheme.typography.titleLarge)
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Hardware Acceleration")
                    Switch(
                        checked = uiState.hardwareAcceleration,
                        onCheckedChange = viewModel::setHardwareAcceleration
                    )
                }
            }
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text("About", style = MaterialTheme.typography.titleLarge)
            }
            item {
                Text("Flux v1.0.0", style = MaterialTheme.typography.bodyMedium)
                Text("Stremio-compatible streaming app", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
