package dev.simonsickle.flux.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import dev.simonsickle.flux.core.common.LocalIsTv

@Composable
fun HomeRoute(
    onNavigateToDetail: (type: String, id: String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAddons: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isTv = LocalIsTv.current

    if (isTv) {
        TvHomeScreen(
            uiState = uiState,
            onSelectContentType = viewModel::selectContentType,
            onItemClick = { item -> onNavigateToDetail(item.type.value, item.id) },
            onNavigateToSearch = onNavigateToSearch,
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToAddons = onNavigateToAddons
        )
    } else {
        MobileHomeScreen(
            uiState = uiState,
            onSelectContentType = viewModel::selectContentType,
            onItemClick = { item -> onNavigateToDetail(item.type.value, item.id) },
            onRefresh = viewModel::refresh,
            onNavigateToSearch = onNavigateToSearch,
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToAddons = onNavigateToAddons,
            onNavigateToDetail = onNavigateToDetail
        )
    }
}
