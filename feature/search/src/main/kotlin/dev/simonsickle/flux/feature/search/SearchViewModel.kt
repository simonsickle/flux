package dev.simonsickle.flux.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.simonsickle.flux.core.model.MetaPreview
import dev.simonsickle.flux.domain.repository.AddonRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<MetaPreview> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val addonRepository: AddonRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        searchJob?.cancel()
        if (query.length < 2) {
            _uiState.value = _uiState.value.copy(results = emptyList(), isLoading = false)
            return
        }
        searchJob = viewModelScope.launch {
            delay(300) // debounce
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val addons = addonRepository.getInstalledAddons().first()
                    .filter { it.enabled && it.manifest.resources.contains("catalog") }

                val results = addons.flatMap { addon ->
                    addon.manifest.catalogs
                        .filter { catalog ->
                            catalog.extra.any { extra -> extra.name == "search" }
                        }
                        .flatMap { catalog ->
                            runCatching {
                                addonRepository.getCatalog(
                                    addon, catalog.type, catalog.id,
                                    mapOf("search" to query)
                                )
                            }.getOrDefault(emptyList())
                        }
                }.distinctBy { it.id }

                _uiState.value = _uiState.value.copy(isLoading = false, results = results)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Search failed"
                )
            }
        }
    }
}
