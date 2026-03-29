package dev.simonsickle.flux.feature.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.simonsickle.flux.core.player.Media3PlayerEngine
import dev.simonsickle.flux.core.player.PlaybackState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    val playerEngine: Media3PlayerEngine
) : ViewModel() {

    val streamUrl: String = savedStateHandle["url"] ?: ""

    val playbackState: StateFlow<PlaybackState> = playerEngine.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlaybackState.IDLE)

    val currentPosition: StateFlow<Long> = playerEngine.currentPosition
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val duration: StateFlow<Long> = playerEngine.duration
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    init {
        if (streamUrl.isNotEmpty()) {
            playerEngine.prepare(streamUrl)
        }
    }

    fun play() = playerEngine.play()
    fun pause() = playerEngine.pause()
    fun seekTo(position: Long) = playerEngine.seekTo(position)

    override fun onCleared() {
        playerEngine.release()
        super.onCleared()
    }
}
