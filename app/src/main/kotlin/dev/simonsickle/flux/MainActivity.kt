package dev.simonsickle.flux

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import dev.simonsickle.flux.core.common.LocalIsTv
import dev.simonsickle.flux.core.common.PlatformDetector
import dev.simonsickle.flux.ui.FluxNavHost
import dev.simonsickle.flux.ui.theme.FluxTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val isTv = PlatformDetector.isTv(this)
        setContent {
            CompositionLocalProvider(LocalIsTv provides isTv) {
                FluxTheme {
                    FluxNavHost(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}
