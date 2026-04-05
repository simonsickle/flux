package dev.simonsickle.flux.feature.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.simonsickle.flux.core.model.ContentType
import dev.simonsickle.flux.core.model.MetaPreview
import dev.simonsickle.flux.domain.repository.WatchHistoryEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileHomeScreen(
    uiState: HomeUiState,
    onSelectContentType: (ContentType) -> Unit,
    onItemClick: (MetaPreview) -> Unit,
    onRefresh: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAddons: () -> Unit,
    onNavigateToDetail: (type: String, id: String) -> Unit = { _, _ -> },
    onRemoveFromHistory: (String) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Flux") },
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = onNavigateToAddons) {
                        Icon(Icons.Default.Extension, contentDescription = "Addons")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // Content type filter chips
            ScrollableTabRow(
                selectedTabIndex = ContentType.entries.indexOf(uiState.selectedContentType),
                modifier = Modifier.fillMaxWidth()
            ) {
                ContentType.entries.forEach { type ->
                    Tab(
                        selected = uiState.selectedContentType == type,
                        onClick = { onSelectContentType(type) },
                        text = { Text(type.value.replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(uiState.error, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onRefresh) { Text("Retry") }
                    }
                }
                uiState.catalogRows.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("No content found. Install an addon to get started.")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onNavigateToAddons) { Text("Install Addon") }
                    }
                }
                else -> {
                    PullToRefreshBox(
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = onRefresh,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            if (uiState.continueWatching.isNotEmpty()) {
                                item {
                                    ContinueWatchingRow(
                                        items = uiState.continueWatching,
                                        onItemClick = { entry ->
                                            onNavigateToDetail(entry.contentType, entry.contentId)
                                        },
                                        onRemoveItem = { entry ->
                                            onRemoveFromHistory(entry.contentId)
                                        }
                                    )
                                }
                            }
                            items(uiState.catalogRows) { row ->
                                CatalogRowSection(
                                    row = row,
                                    onItemClick = onItemClick
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogRowSection(
    row: dev.simonsickle.flux.core.model.CatalogRow,
    onItemClick: (MetaPreview) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = "${row.addonName} - ${row.catalogName}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(row.items) { item ->
                PosterCard(item = item, onClick = { onItemClick(item) })
            }
        }
    }
}

@Composable
private fun PosterCard(
    item: MetaPreview,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = item.poster,
            contentDescription = item.name,
            modifier = Modifier
                .width(120.dp)
                .height(180.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ContinueWatchingRow(
    items: List<WatchHistoryEntry>,
    onItemClick: (WatchHistoryEntry) -> Unit,
    onRemoveItem: (WatchHistoryEntry) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = "Continue Watching",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items, key = { it.contentId }) { entry ->
                var showRemove by remember { mutableStateOf(false) }
                Column(
                    modifier = Modifier
                        .width(160.dp)
                        .combinedClickable(
                            onClick = { onItemClick(entry) },
                            onLongClick = { showRemove = !showRemove }
                        )
                ) {
                    Box {
                        AsyncImage(
                            model = entry.poster,
                            contentDescription = entry.title,
                            modifier = Modifier
                                .width(160.dp)
                                .height(100.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        if (entry.duration > 0) {
                            LinearProgressIndicator(
                                progress = { entry.progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .align(Alignment.BottomStart)
                            )
                        }
                        // Remove button (visible on long press)
                        if (showRemove) {
                            IconButton(
                                onClick = { onRemoveItem(entry) },
                                modifier = Modifier.align(Alignment.TopEnd).size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = MaterialTheme.colorScheme.onError,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        // Remaining time label
                        if (entry.duration > 0 && entry.lastPosition > 0) {
                            val remainingMin = ((entry.duration - entry.lastPosition) / 60000).toInt()
                            if (remainingMin > 0) {
                                Surface(
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        text = "${remainingMin}m left",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
