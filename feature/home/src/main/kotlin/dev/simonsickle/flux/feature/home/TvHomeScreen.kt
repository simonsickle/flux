package dev.simonsickle.flux.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.*
import coil3.compose.AsyncImage
import dev.simonsickle.flux.core.model.ContentType
import dev.simonsickle.flux.core.model.MetaPreview

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
            .padding(horizontal = 48.dp, vertical = 27.dp)
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
                }
            }
            else -> {
                TvLazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(uiState.catalogRows) { row ->
                        Column {
                            Text(
                                text = "${row.addonName} - ${row.catalogName}",
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            TvLazyRow(
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
