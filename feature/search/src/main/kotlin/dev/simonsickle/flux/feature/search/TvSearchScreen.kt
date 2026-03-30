package dev.simonsickle.flux.feature.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dev.simonsickle.flux.core.common.tvContentPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvSearchScreen(
    uiState: SearchUiState,
    onQueryChange: (String) -> Unit,
    onNavigateUp: () -> Unit,
    onNavigateToDetail: (type: String, id: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(tvContentPadding()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(onClick = onNavigateUp) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            Text("Back")
        }

        Text(
            text = "Search",
            style = MaterialTheme.typography.headlineMedium
        )

        TextField(
            value = uiState.query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search movies and series") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        )

        SearchContent(
            uiState = uiState,
            paddingValues = PaddingValues(0.dp),
            columns = 5,
            onItemClick = { item -> onNavigateToDetail(item.type.value, item.id) }
        )
    }
}
