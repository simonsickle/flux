package dev.simonsickle.flux.feature.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import dev.simonsickle.flux.core.model.MetaPreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchRoute(
    onNavigateToDetail: (type: String, id: String) -> Unit,
    onNavigateUp: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    SearchBar(
                        inputField = {
                            SearchBarDefaults.InputField(
                                query = uiState.query,
                                onQueryChange = viewModel::onQueryChange,
                                onSearch = {},
                                expanded = false,
                                onExpandedChange = {},
                                placeholder = { Text("Search...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                            )
                        },
                        expanded = false,
                        onExpandedChange = {},
                        modifier = Modifier.fillMaxWidth()
                    ) {}
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.results.isEmpty() && uiState.query.isNotEmpty() -> {
                    Text(
                        "No results found",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.results) { item ->
                            SearchResultItem(
                                item = item,
                                onClick = { onNavigateToDetail(item.type.value, item.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultItem(item: MetaPreview, onClick: () -> Unit) {
    Column(modifier = Modifier.clickable(onClick = onClick)) {
        AsyncImage(
            model = item.poster,
            contentDescription = item.name,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
