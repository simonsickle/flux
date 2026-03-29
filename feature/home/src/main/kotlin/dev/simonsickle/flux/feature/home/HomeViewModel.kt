package dev.simonsickle.flux.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.simonsickle.flux.core.model.CatalogRow
import dev.simonsickle.flux.core.model.ContentType
import dev.simonsickle.flux.domain.usecase.GetAggregatedCatalogUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val catalogRows: List<CatalogRow> = emptyList(),
    val selectedContentType: ContentType = ContentType.MOVIE,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAggregatedCatalogUseCase: GetAggregatedCatalogUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadCatalogs()
    }

    fun selectContentType(contentType: ContentType) {
        _uiState.value = _uiState.value.copy(selectedContentType = contentType)
        loadCatalogs()
    }

    fun refresh() {
        loadCatalogs()
    }

    private fun loadCatalogs() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val rows = getAggregatedCatalogUseCase(_uiState.value.selectedContentType)
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
