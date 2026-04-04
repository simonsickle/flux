package dev.simonsickle.flux.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.simonsickle.flux.core.common.SettingsRepository
import dev.simonsickle.flux.core.model.CatalogRow
import dev.simonsickle.flux.core.model.ContentType
import dev.simonsickle.flux.domain.repository.AddonRepository
import dev.simonsickle.flux.domain.repository.WatchHistoryEntry
import dev.simonsickle.flux.domain.repository.WatchHistoryRepository
import dev.simonsickle.flux.domain.usecase.GetAggregatedCatalogUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val catalogRows: List<CatalogRow> = emptyList(),
    val continueWatching: List<WatchHistoryEntry> = emptyList(),
    val selectedContentType: ContentType = ContentType.MOVIE,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAggregatedCatalogUseCase: GetAggregatedCatalogUseCase,
    private val addonRepository: AddonRepository,
    private val watchHistoryRepository: WatchHistoryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _selectedContentType = MutableStateFlow(ContentType.MOVIE)
    private val _manualRefresh = MutableStateFlow(0)
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.defaultContentType.collect { savedType ->
                _selectedContentType.value = ContentType.fromValue(savedType)
            }
        }

        // Reload catalogs on type/addon change
        viewModelScope.launch {
            combine(
                _selectedContentType,
                addonRepository.getInstalledAddons(),
                _manualRefresh
            ) { type, addons, _ -> Pair(type, addons) }
                .distinctUntilChangedBy { (type, addons) -> type to addons.map { it.manifest.id } }
                .collect { (type, _) -> loadCatalogs(type) }
        }

        // Observe watch history
        viewModelScope.launch {
            watchHistoryRepository.getRecentlyWatched(20).collect { history ->
                _uiState.value = _uiState.value.copy(continueWatching = history)
            }
        }
    }

    fun selectContentType(contentType: ContentType) {
        _selectedContentType.value = contentType
    }

    fun refresh() {
        _manualRefresh.value++
    }

    fun removeFromHistory(contentId: String) {
        viewModelScope.launch {
            watchHistoryRepository.deleteEntry(contentId)
        }
    }

    private fun loadCatalogs(contentType: ContentType) {
        viewModelScope.launch {
            val isRefresh = _uiState.value.catalogRows.isNotEmpty()
            _uiState.value = _uiState.value.copy(
                isLoading = !isRefresh,
                isRefreshing = isRefresh,
                error = null,
                selectedContentType = contentType
            )
            try {
                val rows = getAggregatedCatalogUseCase(contentType)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    catalogRows = rows
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = e.message ?: "Failed to load content"
                )
            }
        }
    }
}
