package dev.simonsickle.flux.feature.detail

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import dev.simonsickle.flux.core.common.LocalIsTv

@Composable
fun DetailRoute(
    type: String,
    id: String,
    onNavigateUp: () -> Unit,
    onNavigateToPlayer: (streamUrl: String) -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isTv = LocalIsTv.current

    LaunchedEffect(uiState.resolvedStreamUrl) {
        uiState.resolvedStreamUrl?.let { url ->
            onNavigateToPlayer(url)
            viewModel.clearResolvedStream()
        }
    }

    if (isTv) {
        TvDetailScreen(
            uiState = uiState,
            onNavigateUp = onNavigateUp,
            onLoadStreams = viewModel::loadStreams,
            onPlayStream = viewModel::playStream
        )
    } else {
        MobileDetailScreen(
            uiState = uiState,
            onNavigateUp = onNavigateUp,
            onLoadStreams = viewModel::loadStreams,
            onPlayStream = viewModel::playStream
        )
    }
}
