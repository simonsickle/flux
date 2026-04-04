package dev.simonsickle.flux.feature.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.simonsickle.flux.core.model.StreamInfo
import dev.simonsickle.flux.core.model.Video

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileDetailScreen(
    uiState: DetailUiState,
    onNavigateUp: () -> Unit,
    onLoadStreams: (videoId: String) -> Unit,
    onPlayStream: (StreamInfo) -> Unit,
    onRetry: () -> Unit = {},
    onSelectSeason: (Int) -> Unit = {}
) {
    var showStreams by remember { mutableStateOf(false) }
    var selectedVideoId by remember { mutableStateOf<String?>(null) }

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
            uiState.error != null && uiState.meta == null -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        uiState.error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRetry) { Text("Retry") }
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
                                // Release info + runtime on same row
                                val infoLine = listOfNotNull(
                                    uiState.meta.releaseInfo,
                                    uiState.meta.runtime
                                ).joinToString(" · ")
                                if (infoLine.isNotEmpty()) {
                                    Text(infoLine, style = MaterialTheme.typography.bodyMedium)
                                }
                                uiState.meta.imdbRating?.let {
                                    Text(
                                        "★ $it",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                if (uiState.meta.genres.isNotEmpty()) {
                                    Text(
                                        uiState.meta.genres.joinToString(", "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Button(
                            onClick = {
                                showStreams = true
                                selectedVideoId = uiState.meta.id
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
                    // Cast & crew section
                    if (uiState.meta.cast.isNotEmpty() || uiState.meta.director.isNotEmpty()) {
                        item {
                            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                if (uiState.meta.director.isNotEmpty()) {
                                    Text(
                                        text = "Director: ${uiState.meta.director.joinToString(", ")}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                                if (uiState.meta.cast.isNotEmpty()) {
                                    Text(
                                        text = "Cast: ${uiState.meta.cast.take(6).joinToString(", ")}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                uiState.meta.awards?.let {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "🏆 $it",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                    // Error snackbar inline
                    if (uiState.error != null) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                modifier = Modifier.fillMaxWidth().padding(16.dp)
                            ) {
                                Text(
                                    text = uiState.error,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                    // Streams section
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
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator()
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "Finding streams...",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else if (uiState.streams.isEmpty() && !uiState.isLoadingStreams) {
                            item {
                                Text(
                                    "No streams found for this content.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        } else {
                            items(uiState.streams) { stream ->
                                StreamItem(
                                    stream = stream,
                                    isResolving = uiState.isResolvingStream,
                                    resolvingStreamUrl = uiState.resolvingStreamUrl,
                                    onClick = { onPlayStream(stream) }
                                )
                            }
                        }
                    }
                    // Season/episode picker for series
                    if (uiState.meta.videos.isNotEmpty()) {
                        val seasons = uiState.meta.videos
                            .mapNotNull { it.season }
                            .distinct()
                            .sorted()

                        if (seasons.size > 1) {
                            item {
                                SeasonSelector(
                                    seasons = seasons,
                                    selectedSeason = uiState.selectedSeason ?: seasons.first(),
                                    onSeasonSelected = onSelectSeason
                                )
                            }
                        }

                        item {
                            Text(
                                "Episodes",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        val displayVideos = if (seasons.size > 1) {
                            val activeSeason = uiState.selectedSeason ?: seasons.first()
                            uiState.meta.videos.filter { it.season == activeSeason }
                        } else {
                            uiState.meta.videos
                        }

                        items(displayVideos) { video ->
                            EpisodeItem(
                                video = video,
                                isSelected = selectedVideoId == video.id,
                                onPlay = {
                                    showStreams = true
                                    selectedVideoId = video.id
                                    onLoadStreams(video.id)
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
private fun SeasonSelector(
    seasons: List<Int>,
    selectedSeason: Int,
    onSeasonSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterChip(
            selected = true,
            onClick = { expanded = true },
            label = { Text("Season $selectedSeason") }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            seasons.forEach { season ->
                DropdownMenuItem(
                    text = { Text("Season $season") },
                    onClick = {
                        onSeasonSelected(season)
                        expanded = false
                    },
                    leadingIcon = if (season == selectedSeason) {
                        { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
            }
        }
    }
}

@Composable
private fun EpisodeItem(
    video: Video,
    isSelected: Boolean,
    onPlay: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                video.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Column {
                video.season?.let { s ->
                    video.episode?.let { e ->
                        Text("S${s}E${e}", style = MaterialTheme.typography.labelSmall)
                    }
                }
                video.overview?.let {
                    Text(
                        it,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        leadingContent = {
            video.thumbnail?.let { thumb ->
                AsyncImage(
                    model = thumb,
                    contentDescription = null,
                    modifier = Modifier
                        .width(80.dp)
                        .height(45.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        },
        trailingContent = {
            IconButton(onClick = onPlay) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = if (isSelected) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurface
                )
            }
        },
        tonalElevation = if (isSelected) 2.dp else 0.dp
    )
}

@Composable
private fun StreamItem(
    stream: StreamInfo,
    isResolving: Boolean,
    resolvingStreamUrl: String? = null,
    onClick: () -> Unit
) {
    val isThisResolving = isResolving && resolvingStreamUrl == stream.url
    val streamTitle = stream.name ?: "Stream"

    // Parse quality hints from name/description
    val qualityBadge = parseQualityBadge(streamTitle, stream.description)
    val sourceBadge = parseSourceBadge(stream)

    ListItem(
        headlineContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(streamTitle, modifier = Modifier.weight(1f, fill = false))
                qualityBadge?.let {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(it, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.height(24.dp)
                    )
                }
                sourceBadge?.let {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(it, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.height(24.dp)
                    )
                }
            }
        },
        supportingContent = { stream.description?.let { Text(it, maxLines = 2) } },
        trailingContent = {
            if (isThisResolving) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                IconButton(onClick = onClick) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play stream")
                }
            }
        }
    )
}

private fun parseQualityBadge(name: String, description: String?): String? {
    val combined = "$name ${description ?: ""}".lowercase()
    return when {
        "2160p" in combined || "4k" in combined || "uhd" in combined -> "4K"
        "1080p" in combined || "full hd" in combined -> "1080p"
        "720p" in combined || "hd" in combined -> "720p"
        "480p" in combined || "sd" in combined -> "480p"
        else -> null
    }
}

private fun parseSourceBadge(stream: StreamInfo): String? {
    return when {
        stream.infoHash != null -> "Torrent"
        stream.behaviorHints.bingeGroup != null -> stream.behaviorHints.bingeGroup
        else -> null
    }
}
