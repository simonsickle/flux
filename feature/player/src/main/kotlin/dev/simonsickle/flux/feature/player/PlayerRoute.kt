package dev.simonsickle.flux.feature.player

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import dev.simonsickle.flux.core.common.LocalIsTv

@Composable
fun PlayerRoute(
    streamUrl: String,
    onNavigateUp: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val isTv = LocalIsTv.current

    if (isTv) {
        TvPlayerScreen(
            viewModel = viewModel,
            onNavigateUp = onNavigateUp
        )
    } else {
        MobilePlayerScreen(
            viewModel = viewModel,
            onNavigateUp = onNavigateUp
        )
    }
}
