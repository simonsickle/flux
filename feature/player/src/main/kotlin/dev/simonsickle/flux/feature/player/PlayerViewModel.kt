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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
    private val contentTitle: String = savedStateHandle["title"] ?: ""
    private val contentPoster: String? = savedStateHandle["poster"]

    val playbackState: StateFlow<PlaybackState> = playerEngine.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlaybackState.IDLE)

    val currentPosition: StateFlow<Long> = playerEngine.currentPosition
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val duration: StateFlow<Long> = playerEngine.duration
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    private var progressSaveJob: Job? = null

    init {
        if (streamUrl.isNotEmpty()) {
            viewModelScope.launch {
                val savedPosition = if (contentId.isNotEmpty()) {
                    watchHistoryRepository.getEntry(contentId)?.lastPosition ?: 0L
                } else 0L
                playerEngine.prepare(streamUrl, savedPosition)
            }
        }
        startProgressSaving()
    }

    fun play() = playerEngine.play()
    fun pause() = playerEngine.pause()
    fun seekTo(position: Long) = playerEngine.seekTo(position)

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
