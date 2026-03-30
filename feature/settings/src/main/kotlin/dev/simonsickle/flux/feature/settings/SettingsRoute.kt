package dev.simonsickle.flux.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import dev.simonsickle.flux.core.common.LocalIsTv

@Composable
fun SettingsRoute(
    onNavigateUp: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isTv = LocalIsTv.current
    val initialToken = uiState.realDebridToken

    if (isTv) {
        TvSettingsScreen(
            uiState = uiState,
            onNavigateUp = onNavigateUp,
            onSetRealDebridToken = viewModel::setRealDebridToken,
            onSetDefaultContentType = viewModel::setDefaultContentType,
            onSetPreferredPlayer = viewModel::setPreferredPlayer,
            onSetSubtitleLanguage = viewModel::setSubtitleLanguage,
            onSetHardwareAcceleration = viewModel::setHardwareAcceleration,
            onTestDebridConnection = viewModel::testDebridConnection,
            initialToken = initialToken
        )
    } else {
        MobileSettingsScreen(
            uiState = uiState,
            onNavigateUp = onNavigateUp,
            onSetRealDebridToken = viewModel::setRealDebridToken,
            onSetDefaultContentType = viewModel::setDefaultContentType,
            onSetPreferredPlayer = viewModel::setPreferredPlayer,
            onSetSubtitleLanguage = viewModel::setSubtitleLanguage,
            onSetHardwareAcceleration = viewModel::setHardwareAcceleration,
            onTestDebridConnection = viewModel::testDebridConnection,
            initialToken = initialToken
        )
    }
}
