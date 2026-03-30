package dev.simonsickle.flux.feature.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import dev.simonsickle.flux.core.common.LocalIsTv

@Composable
fun SearchRoute(
    onNavigateToDetail: (type: String, id: String) -> Unit,
    onNavigateUp: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isTv = LocalIsTv.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    if (isTv) {
        TvSearchScreen(
            uiState = uiState,
            onQueryChange = viewModel::onQueryChange,
            onNavigateUp = onNavigateUp,
            onNavigateToDetail = onNavigateToDetail
        )
    } else {
        MobileSearchScreen(
            uiState = uiState,
            onQueryChange = viewModel::onQueryChange,
            onNavigateUp = onNavigateUp,
            onNavigateToDetail = onNavigateToDetail,
            modifier = Modifier.focusRequester(focusRequester)
        )
    }
}
