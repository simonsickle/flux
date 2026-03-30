package dev.simonsickle.flux.core.player

import android.content.Context.AUDIO_SERVICE
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
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
    private val audioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager
    private var positionJob: Job? = null
    private var hasAudioFocus = false

    private val focusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pause()

            AudioManager.AUDIOFOCUS_GAIN -> {
                exoPlayer.volume = 1f
                if (_state.value == PlaybackState.PAUSED) {
                    play()
                }
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                exoPlayer.volume = 0.2f
            }
        }
    }

    private val audioFocusRequest: AudioFocusRequest by lazy {
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                    .build()
            )
            .setAcceptsDelayedFocusGain(false)
            .setWillPauseWhenDucked(false)
            .setOnAudioFocusChangeListener(focusChangeListener)
            .build()
    }

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
        requestAudioFocus()
        val mediaItem = MediaItem.fromUri(uri)
        exoPlayer.setMediaItem(mediaItem)
        if (startPosition > 0) exoPlayer.seekTo(startPosition)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    override fun play() {
        if (!hasAudioFocus) {
            requestAudioFocus()
        }
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
        abandonAudioFocus()
        exoPlayer.release()
    }

    private fun requestAudioFocus() {
        if (hasAudioFocus) return
        hasAudioFocus = audioManager.requestAudioFocus(audioFocusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun abandonAudioFocus() {
        if (!hasAudioFocus) return
        audioManager.abandonAudioFocusRequest(audioFocusRequest)
        hasAudioFocus = false
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
