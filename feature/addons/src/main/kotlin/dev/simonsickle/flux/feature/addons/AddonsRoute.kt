package dev.simonsickle.flux.feature.addons

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import dev.simonsickle.flux.core.common.LocalIsTv

@Composable
fun AddonsRoute(
    onNavigateUp: () -> Unit,
    viewModel: AddonsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isTv = LocalIsTv.current

    if (isTv) {
        TvAddonsScreen(
            uiState = uiState,
            onNavigateUp = onNavigateUp,
            onInstallAddon = viewModel::installAddon,
            onToggleAddon = viewModel::toggleAddon,
            onRemoveAddon = viewModel::removeAddon,
            onMoveAddon = viewModel::moveAddon
        )
    } else {
        MobileAddonsScreen(
            uiState = uiState,
            onNavigateUp = onNavigateUp,
            onInstallAddon = viewModel::installAddon,
            onToggleAddon = viewModel::toggleAddon,
            onRemoveAddon = viewModel::removeAddon,
            onMoveAddon = viewModel::moveAddon
        )
    }
}
