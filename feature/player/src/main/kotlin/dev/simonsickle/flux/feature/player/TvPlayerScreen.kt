package dev.simonsickle.flux.feature.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import dev.simonsickle.flux.core.player.PlaybackState

@OptIn(UnstableApi::class)
@Composable
fun TvPlayerScreen(
    viewModel: PlayerViewModel,
    onNavigateUp: () -> Unit
) {
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val currentPosition by viewModel.currentPosition.collectAsStateWithLifecycle()
    val duration by viewModel.duration.collectAsStateWithLifecycle()
    var showControls by remember { mutableStateOf(true) }

    LaunchedEffect(showControls) {
        if (showControls) {
            kotlinx.coroutines.delay(5000)
            showControls = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onKeyEvent { keyEvent ->
                when {
                    keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.DirectionCenter -> {
                        if (playbackState == PlaybackState.PLAYING) viewModel.pause()
                        else viewModel.play()
                        showControls = true
                        true
                    }
                    keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.DirectionRight -> {
                        viewModel.seekTo(currentPosition + 10_000)
                        showControls = true
                        true
                    }
                    keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.DirectionLeft -> {
                        viewModel.seekTo((currentPosition - 10_000).coerceAtLeast(0))
                        showControls = true
                        true
                    }
                    keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Back -> {
                        onNavigateUp()
                        true
                    }
                    else -> false
                }
            }
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = viewModel.playerEngine.exoPlayer
                    useController = false
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (showControls) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            ) {
                if (playbackState == PlaybackState.BUFFERING) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White
                    )
                } else {
                    Icon(
                        if (playbackState == PlaybackState.PLAYING) Icons.Default.Pause
                        else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.align(Alignment.Center).size(80.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(48.dp)
                ) {
                    if (duration > 0) {
                        LinearProgressIndicator(
                            progress = { if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f },
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(formatTime(currentPosition), color = Color.White)
                            Text(formatTime(duration), color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val seconds = ms / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes % 60, seconds % 60)
    } else {
        "%d:%02d".format(minutes, seconds % 60)
    }
}
