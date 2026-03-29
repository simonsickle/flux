package dev.simonsickle.flux.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.simonsickle.flux.core.model.CatalogRow
import dev.simonsickle.flux.core.model.ContentType
import dev.simonsickle.flux.domain.repository.AddonRepository
import dev.simonsickle.flux.domain.usecase.GetAggregatedCatalogUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val catalogRows: List<CatalogRow> = emptyList(),
    val selectedContentType: ContentType = ContentType.MOVIE,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAggregatedCatalogUseCase: GetAggregatedCatalogUseCase,
    private val addonRepository: AddonRepository
) : ViewModel() {

    private val _selectedContentType = MutableStateFlow(ContentType.MOVIE)
    private val _manualRefresh = MutableStateFlow(0)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Re-fetch catalogs whenever selected type changes, addons change, or manual refresh
        viewModelScope.launch {
            combine(
                _selectedContentType,
                addonRepository.getInstalledAddons(),
                _manualRefresh
            ) { type, addons, _ -> Pair(type, addons) }
                .distinctUntilChangedBy { (type, addons) -> type to addons.map { it.manifest.id } }
                .collect { (type, _) ->
                    loadCatalogs(type)
                }
        }
    }

    fun selectContentType(contentType: ContentType) {
        _selectedContentType.value = contentType
    }

    fun refresh() {
        _manualRefresh.value++
    }

    private fun loadCatalogs(contentType: ContentType) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                selectedContentType = contentType
            )
            try {
                val rows = getAggregatedCatalogUseCase(contentType)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    catalogRows = rows
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load content"
                )
            }
        }
    }
}
