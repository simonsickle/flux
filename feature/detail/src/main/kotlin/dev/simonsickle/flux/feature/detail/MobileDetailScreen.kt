package dev.simonsickle.flux.feature.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.simonsickle.flux.core.model.StreamInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileDetailScreen(
    uiState: DetailUiState,
    onNavigateUp: () -> Unit,
    onLoadStreams: (videoId: String) -> Unit,
    onPlayStream: (StreamInfo) -> Unit
) {
    var showStreams by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.meta?.name ?: "Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.meta != null -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues)
                ) {
                    item {
                        // Backdrop
                        uiState.meta.background?.let { bg ->
                            AsyncImage(
                                model = bg,
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth().height(220.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    item {
                        Row(modifier = Modifier.padding(16.dp)) {
                            uiState.meta.poster?.let { poster ->
                                AsyncImage(
                                    model = poster,
                                    contentDescription = uiState.meta.name,
                                    modifier = Modifier
                                        .width(100.dp)
                                        .height(150.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                            }
                            Column {
                                Text(
                                    text = uiState.meta.name,
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                uiState.meta.releaseInfo?.let {
                                    Text(it, style = MaterialTheme.typography.bodyMedium)
                                }
                                uiState.meta.imdbRating?.let {
                                    Text("★ $it", style = MaterialTheme.typography.bodyMedium)
                                }
                                if (uiState.meta.genres.isNotEmpty()) {
                                    Text(
                                        uiState.meta.genres.joinToString(", "),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Button(
                            onClick = {
                                showStreams = true
                                onLoadStreams(uiState.meta.id)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Play")
                        }
                    }
                    item {
                        uiState.meta.description?.let { desc ->
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    if (showStreams) {
                        item {
                            Text(
                                "Streams",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        if (uiState.isLoadingStreams) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        } else {
                            items(uiState.streams) { stream ->
                                StreamItem(
                                    stream = stream,
                                    isResolving = uiState.isResolvingStream,
                                    onClick = { onPlayStream(stream) }
                                )
                            }
                        }
                    }
                    // Season/episode picker for series
                    if (uiState.meta.videos.isNotEmpty()) {
                        item {
                            Text(
                                "Episodes",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        items(uiState.meta.videos) { video ->
                            ListItem(
                                headlineContent = { Text(video.title) },
                                supportingContent = {
                                    video.season?.let { s ->
                                        video.episode?.let { e ->
                                            Text("S${s}E${e}")
                                        }
                                    }
                                },
                                trailingContent = {
                                    IconButton(onClick = { onLoadStreams(video.id) }) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StreamItem(
    stream: StreamInfo,
    isResolving: Boolean,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(stream.name ?: "Stream") },
        supportingContent = { stream.description?.let { Text(it, maxLines = 2) } },
        trailingContent = {
            if (isResolving) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                IconButton(onClick = onClick) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play stream")
                }
            }
        }
    )
}
