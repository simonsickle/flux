package dev.simonsickle.flux.core.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp

object TvLayout {
    val ContentPadding = PaddingValues(horizontal = 48.dp, vertical = 27.dp)
}

fun tvContentPadding(): PaddingValues = TvLayout.ContentPadding
