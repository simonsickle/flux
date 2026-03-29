package dev.simonsickle.flux.core.common

import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.compositionLocalOf

val LocalIsTv = compositionLocalOf { false }

object PlatformDetector {
    fun isTv(context: Context): Boolean {
        val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        return uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
    }
}
