package dev.simonsickle.flux.feature.detail

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import dev.simonsickle.flux.core.common.LocalIsTv

@Composable
fun DetailRoute(
    type: String,
    id: String,
    onNavigateUp: () -> Unit,
    onNavigateToPlayer: (streamUrl: String, contentId: String, contentType: String, title: String, poster: String?) -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isTv = LocalIsTv.current

    LaunchedEffect(uiState.resolvedStreamUrl) {
        uiState.resolvedStreamUrl?.let { url ->
            val meta = uiState.meta
            onNavigateToPlayer(
                url,
                meta?.id ?: id,
                type,
                meta?.name ?: "",
                meta?.poster
            )
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
            onPlayStream = viewModel::playStream,
            onRetry = viewModel::retry,
            onSelectSeason = viewModel::selectSeason
        )
    }
}
