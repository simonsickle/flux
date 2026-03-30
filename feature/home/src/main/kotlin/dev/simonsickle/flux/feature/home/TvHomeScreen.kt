package dev.simonsickle.flux.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import coil3.compose.AsyncImage
import dev.simonsickle.flux.core.common.tvContentPadding
import dev.simonsickle.flux.core.model.ContentType
import dev.simonsickle.flux.core.model.MetaPreview
import dev.simonsickle.flux.domain.repository.WatchHistoryEntry

@Suppress("UNUSED_PARAMETER")
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvHomeScreen(
    uiState: HomeUiState,
    onSelectContentType: (ContentType) -> Unit,
    onItemClick: (MetaPreview) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAddons: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(tvContentPadding())
    ) {
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.catalogRows.isEmpty() && !uiState.isLoading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("No content found. Install an addon in Settings.")
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(onClick = onNavigateToAddons) {
                            Text("Open Addons")
                        }
                        Button(onClick = onNavigateToSettings) {
                            Text("Open Settings")
                        }
                        Button(onClick = onNavigateToSearch) {
                            Text("Search")
                        }
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    if (uiState.continueWatching.isNotEmpty()) {
                        item {
                            TvContinueWatchingRow(
                                items = uiState.continueWatching,
                                onItemClick = { entry -> onItemClick(entry.toMetaPreview()) }
                            )
                        }
                    }
                    items(uiState.catalogRows) { row ->
                        Column {
                            Text(
                                text = "${row.addonName} - ${row.catalogName}",
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(row.items) { item ->
                                    TvPosterCard(
                                        item = item,
                                        onClick = { onItemClick(item) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun WatchHistoryEntry.toMetaPreview(): MetaPreview = MetaPreview(
    id = contentId,
    type = ContentType.fromValue(contentType),
    name = title,
    poster = poster
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvPosterCard(
    item: MetaPreview,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(160.dp)
    ) {
        Column {
            AsyncImage(
                model = item.poster,
                contentDescription = item.name,
                modifier = Modifier
                    .width(160.dp)
                    .height(240.dp),
                contentScale = ContentScale.Crop
            )
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvContinueWatchingRow(
    items: List<WatchHistoryEntry>,
    onItemClick: (WatchHistoryEntry) -> Unit
) {
    Column {
        Text(
            text = "Continue Watching",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(items) { entry ->
                Card(
                    onClick = { onItemClick(entry) },
                    modifier = Modifier.width(220.dp)
                ) {
                    Column {
                        AsyncImage(
                            model = entry.poster,
                            contentDescription = entry.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(124.dp),
                            contentScale = ContentScale.Crop
                        )
                        LinearProgressIndicator(
                            progress = { entry.progressFraction },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = entry.title,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}
