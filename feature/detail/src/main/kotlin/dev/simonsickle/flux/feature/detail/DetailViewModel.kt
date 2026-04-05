package dev.simonsickle.flux.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.simonsickle.flux.core.model.MetaDetail
import dev.simonsickle.flux.core.model.StreamInfo
import dev.simonsickle.flux.domain.usecase.GetContentDetailUseCase
import dev.simonsickle.flux.domain.usecase.GetStreamsUseCase
import dev.simonsickle.flux.domain.usecase.ResolveAndPlayStreamUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val isLoading: Boolean = false,
    val meta: MetaDetail? = null,
    val streams: List<StreamInfo> = emptyList(),
    val isLoadingStreams: Boolean = false,
    val error: String? = null,
    val resolvedStreamUrl: String? = null,
    val isResolvingStream: Boolean = false,
    val resolvingStreamUrl: String? = null,
    val selectedSeason: Int? = null
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getContentDetailUseCase: GetContentDetailUseCase,
    private val getStreamsUseCase: GetStreamsUseCase,
    private val resolveAndPlayStreamUseCase: ResolveAndPlayStreamUseCase
) : ViewModel() {

    private val type: String = checkNotNull(savedStateHandle["type"])
    private val id: String = checkNotNull(savedStateHandle["id"])

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadDetail()
    }

    private fun loadDetail() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val meta = getContentDetailUseCase(type, id)
                _uiState.value = _uiState.value.copy(isLoading = false, meta = meta)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load details"
                )
            }
        }
    }

    fun loadStreams(videoId: String = id) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingStreams = true)
            try {
                val streams = getStreamsUseCase(type, videoId)
                _uiState.value = _uiState.value.copy(isLoadingStreams = false, streams = streams)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingStreams = false,
                    error = e.message
                )
            }
        }
    }

    fun playStream(stream: StreamInfo) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isResolvingStream = true,
                resolvingStreamUrl = stream.url,
                error = null
            )
            try {
                val resolved = resolveAndPlayStreamUseCase(stream)
                _uiState.value = _uiState.value.copy(
                    isResolvingStream = false,
                    resolvingStreamUrl = null,
                    resolvedStreamUrl = resolved.url
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isResolvingStream = false,
                    resolvingStreamUrl = null,
                    error = e.message ?: "Failed to resolve stream"
                )
            }
        }
    }

    fun selectSeason(season: Int) {
        _uiState.value = _uiState.value.copy(selectedSeason = season)
    }

    fun clearResolvedStream() {
        _uiState.value = _uiState.value.copy(resolvedStreamUrl = null)
    }

    fun retry() {
        loadDetail()
    }
}
