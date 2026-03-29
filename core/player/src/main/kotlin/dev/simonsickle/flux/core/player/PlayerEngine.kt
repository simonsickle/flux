package dev.simonsickle.flux.core.player

import dev.simonsickle.flux.core.model.SubtitleInfo
import kotlinx.coroutines.flow.StateFlow

enum class PlaybackState {
    IDLE,
    BUFFERING,
    READY,
    PLAYING,
    PAUSED,
    ENDED,
    ERROR
}

data class PlaybackError(
    val code: Int,
    val message: String,
    val cause: Exception? = null
)

interface PlayerEngine {
    val state: StateFlow<PlaybackState>
    val currentPosition: StateFlow<Long>
    val duration: StateFlow<Long>
    val error: StateFlow<PlaybackError?>

    fun prepare(uri: String, startPosition: Long = 0L)
    fun play()
    fun pause()
    fun seekTo(positionMs: Long)
    fun setSubtitle(subtitle: SubtitleInfo?)
    fun setPlaybackSpeed(speed: Float)
    fun release()
}
