package dev.simonsickle.flux.feature.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import coil3.compose.AsyncImage
import dev.simonsickle.flux.core.model.StreamInfo

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvDetailScreen(
    uiState: DetailUiState,
    onNavigateUp: () -> Unit,
    onLoadStreams: (videoId: String) -> Unit,
    onPlayStream: (StreamInfo) -> Unit
) {
    var showStreams by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        uiState.meta?.background?.let { bg ->
            AsyncImage(
                model = bg,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.3f
            )
        }

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.meta != null -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 48.dp, vertical = 27.dp)
                ) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            uiState.meta.poster?.let { poster ->
                                AsyncImage(
                                    model = poster,
                                    contentDescription = uiState.meta.name,
                                    modifier = Modifier.width(200.dp).height(300.dp),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(32.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = uiState.meta.name,
                                    style = MaterialTheme.typography.headlineLarge
                                )
                                uiState.meta.releaseInfo?.let {
                                    Text(it, style = MaterialTheme.typography.bodyLarge)
                                }
                                uiState.meta.imdbRating?.let {
                                    Text("★ $it", style = MaterialTheme.typography.bodyLarge)
                                }
                                uiState.meta.description?.let {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(it, style = MaterialTheme.typography.bodyMedium, maxLines = 5)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        showStreams = true
                                        onLoadStreams(uiState.meta.id)
                                    }
                                ) {
                                    Text("Play")
                                }
                            }
                        }
                    }

                    if (showStreams) {
                        item {
                            Text(
                                "Streams",
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                            )
                            if (uiState.isLoadingStreams) {
                                CircularProgressIndicator()
                            } else {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(uiState.streams) { stream ->
                                        Card(onClick = { onPlayStream(stream) }) {
                                            Column(modifier = Modifier.padding(16.dp).width(200.dp)) {
                                                Text(
                                                    stream.name ?: "Stream",
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                                stream.description?.let {
                                                    Text(it, style = MaterialTheme.typography.bodySmall, maxLines = 3)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
