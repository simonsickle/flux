package dev.simonsickle.flux.feature.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.simonsickle.flux.core.player.Media3PlayerEngine
import dev.simonsickle.flux.core.player.PlaybackState
import dev.simonsickle.flux.domain.repository.WatchHistoryEntry
import dev.simonsickle.flux.domain.repository.WatchHistoryRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    val playerEngine: Media3PlayerEngine,
    private val watchHistoryRepository: WatchHistoryRepository
) : ViewModel() {

    val streamUrl: String = savedStateHandle["url"] ?: ""
    private val contentId: String = savedStateHandle["contentId"] ?: ""
    private val contentType: String = savedStateHandle["contentType"] ?: "movie"
    val contentTitle: String = savedStateHandle["title"] ?: ""
    private val contentPoster: String? = savedStateHandle["poster"]

    val playbackState: StateFlow<PlaybackState> = playerEngine.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlaybackState.IDLE)

    val currentPosition: StateFlow<Long> = playerEngine.currentPosition
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val duration: StateFlow<Long> = playerEngine.duration
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val error: StateFlow<dev.simonsickle.flux.core.player.PlaybackError?> = playerEngine.error
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private var progressSaveJob: Job? = null

    private val _showResumeDialog = MutableStateFlow(false)
    val showResumeDialog: StateFlow<Boolean> = _showResumeDialog.asStateFlow()

    private val _savedPosition = MutableStateFlow(0L)
    val savedPosition: StateFlow<Long> = _savedPosition.asStateFlow()

    init {
        if (streamUrl.isNotEmpty()) {
            viewModelScope.launch {
                val saved = if (contentId.isNotEmpty()) {
                    watchHistoryRepository.getEntry(contentId)?.lastPosition ?: 0L
                } else 0L
                if (saved > 5000L) { // More than 5 seconds in - offer resume
                    _savedPosition.value = saved
                    _showResumeDialog.value = true
                } else {
                    playerEngine.prepare(streamUrl, 0L)
                }
            }
        }
        startProgressSaving()
    }

    fun resumePlayback() {
        _showResumeDialog.value = false
        playerEngine.prepare(streamUrl, _savedPosition.value)
    }

    fun startFromBeginning() {
        _showResumeDialog.value = false
        playerEngine.prepare(streamUrl, 0L)
    }

    fun play() = playerEngine.play()
    fun pause() = playerEngine.pause()
    fun seekTo(position: Long) = playerEngine.seekTo(position)
    fun setPlaybackSpeed(speed: Float) = playerEngine.setPlaybackSpeed(speed)

    fun retry() {
        if (streamUrl.isNotEmpty()) {
            viewModelScope.launch {
                val savedPosition = if (contentId.isNotEmpty()) {
                    watchHistoryRepository.getEntry(contentId)?.lastPosition ?: 0L
                } else 0L
                playerEngine.prepare(streamUrl, savedPosition)
            }
        }
    }

    private fun startProgressSaving() {
        progressSaveJob = viewModelScope.launch {
            while (isActive) {
                delay(5000) // save every 5 seconds
                val pos = playerEngine.currentPosition.value
                val dur = playerEngine.duration.value
                if (contentId.isNotEmpty() && pos > 0) {
                    watchHistoryRepository.upsertEntry(
                        WatchHistoryEntry(
                            contentId = contentId,
                            contentType = contentType,
                            title = contentTitle,
                            poster = contentPoster,
                            lastPosition = pos,
                            duration = dur,
                            lastWatchedAt = System.currentTimeMillis(),
                            videoId = null
                        )
                    )
                }
            }
        }
    }

    override fun onCleared() {
        progressSaveJob?.cancel()
        playerEngine.release()
        super.onCleared()
    }
}
