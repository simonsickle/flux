package dev.simonsickle.flux.core.player

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import dev.simonsickle.flux.core.model.SubtitleInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@UnstableApi
class Media3PlayerEngine(private val context: Context) : PlayerEngine {

    private val _state = MutableStateFlow(PlaybackState.IDLE)
    override val state: StateFlow<PlaybackState> = _state

    private val _currentPosition = MutableStateFlow(0L)
    override val currentPosition: StateFlow<Long> = _currentPosition

    private val _duration = MutableStateFlow(0L)
    override val duration: StateFlow<Long> = _duration

    private val _error = MutableStateFlow<PlaybackError?>(null)
    override val error: StateFlow<PlaybackError?> = _error

    private val scope = CoroutineScope(Dispatchers.Main)
    private var positionJob: Job? = null

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build().also { player ->
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                _state.value = when (playbackState) {
                    Player.STATE_IDLE -> PlaybackState.IDLE
                    Player.STATE_BUFFERING -> PlaybackState.BUFFERING
                    Player.STATE_READY -> if (player.playWhenReady) PlaybackState.PLAYING else PlaybackState.PAUSED
                    Player.STATE_ENDED -> PlaybackState.ENDED
                    else -> PlaybackState.IDLE
                }
                if (playbackState == Player.STATE_READY) {
                    _duration.value = player.duration.coerceAtLeast(0L)
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (_state.value != PlaybackState.BUFFERING && _state.value != PlaybackState.IDLE) {
                    _state.value = if (isPlaying) PlaybackState.PLAYING else PlaybackState.PAUSED
                }
                if (isPlaying) startPositionTracking() else stopPositionTracking()
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                _state.value = PlaybackState.ERROR
                _error.value = PlaybackError(error.errorCode, error.message ?: "Unknown error", error)
            }
        })
    }

    override fun prepare(uri: String, startPosition: Long) {
        _error.value = null
        val mediaItem = MediaItem.fromUri(uri)
        exoPlayer.setMediaItem(mediaItem)
        if (startPosition > 0) exoPlayer.seekTo(startPosition)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    override fun play() {
        exoPlayer.play()
    }

    override fun pause() {
        exoPlayer.pause()
    }

    override fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
    }

    override fun setSubtitle(subtitle: SubtitleInfo?) {
        // Re-prepare with subtitle configuration
        if (subtitle != null) {
            val currentUri = exoPlayer.currentMediaItem?.localConfiguration?.uri ?: return
            val subtitleConfig = MediaItem.SubtitleConfiguration.Builder(
                android.net.Uri.parse(subtitle.url)
            )
                .setMimeType(androidx.media3.common.MimeTypes.TEXT_VTT)
                .setLanguage(subtitle.lang)
                .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                .build()

            val mediaItem = MediaItem.Builder()
                .setUri(currentUri)
                .setSubtitleConfigurations(listOf(subtitleConfig))
                .build()

            val currentPos = exoPlayer.currentPosition
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.seekTo(currentPos)
            exoPlayer.prepare()
        }
    }

    override fun setPlaybackSpeed(speed: Float) {
        exoPlayer.setPlaybackSpeed(speed)
    }

    override fun release() {
        stopPositionTracking()
        exoPlayer.release()
    }

    private fun startPositionTracking() {
        positionJob?.cancel()
        positionJob = scope.launch {
            while (isActive) {
                _currentPosition.value = exoPlayer.currentPosition
                delay(500)
            }
        }
    }

    private fun stopPositionTracking() {
        positionJob?.cancel()
    }
}
