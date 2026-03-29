package dev.simonsickle.flux.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = FluxPrimary,
    onPrimary = FluxOnSurface,
    primaryContainer = FluxPrimaryDark,
    secondary = FluxAccent,
    background = FluxBackground,
    surface = FluxSurface,
    surfaceVariant = FluxSurfaceVariant,
    onBackground = FluxOnSurface,
    onSurface = FluxOnSurface,
    onSurfaceVariant = FluxOnSurfaceVariant,
    error = FluxError,
)

@Composable
fun FluxTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
