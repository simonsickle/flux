package dev.simonsickle.flux

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dev.simonsickle.flux.domain.usecase.InstallAddonUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DebugInstallAddonActivity : ComponentActivity() {

    @Inject
    lateinit var installAddonUseCase: InstallAddonUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val url = intent.getStringExtra(EXTRA_URL)?.trim().orEmpty()
        if (url.isBlank()) {
            Log.e(TAG, "Missing addon URL extra")
            Toast.makeText(this, "Missing addon URL", Toast.LENGTH_SHORT).show()
            finishWithResult(Activity.RESULT_CANCELED)
            return
        }

        lifecycleScope.launch {
            runCatching {
                installAddonUseCase(url)
            }.onSuccess {
                Log.i(TAG, "Installed addon from URL: $url")
                Toast.makeText(this@DebugInstallAddonActivity, "Addon installed", Toast.LENGTH_SHORT).show()
                finishWithResult(Activity.RESULT_OK)
            }.onFailure { error ->
                Log.e(TAG, "Failed to install addon from URL: $url", error)
                Toast.makeText(
                    this@DebugInstallAddonActivity,
                    error.message ?: "Install failed",
                    Toast.LENGTH_LONG
                ).show()
                finishWithResult(Activity.RESULT_CANCELED)
            }
        }
    }

    private fun finishWithResult(resultCode: Int) {
        setResult(resultCode)
        finish()
    }

    companion object {
        const val EXTRA_URL = "url"
        private const val TAG = "DebugInstallAddon"
    }
}
