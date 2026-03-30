package dev.simonsickle.flux.feature.addons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.simonsickle.flux.core.model.InstalledAddon
import dev.simonsickle.flux.domain.usecase.InstallAddonUseCase
import dev.simonsickle.flux.domain.repository.AddonRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddonsUiState(
    val addons: List<InstalledAddon> = emptyList(),
    val isInstalling: Boolean = false,
    val installError: String? = null,
    val installSuccessMessage: String? = null
)

@HiltViewModel
class AddonsViewModel @Inject constructor(
    private val addonRepository: AddonRepository,
    private val installAddonUseCase: InstallAddonUseCase
) : ViewModel() {

    private val isInstalling = MutableStateFlow(false)
    private val installError = MutableStateFlow<String?>(null)
    private val installSuccessMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AddonsUiState> = combine(
        addonRepository.getInstalledAddons(),
        isInstalling,
        installError,
        installSuccessMessage
    ) { addons, installing, error, successMessage ->
        AddonsUiState(
            addons = addons,
            isInstalling = installing,
            installError = error,
            installSuccessMessage = successMessage
        )
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AddonsUiState())

    fun installAddon(url: String) {
        val trimmedUrl = url.trim()
        if (trimmedUrl.isBlank()) {
            installError.value = "Transport URL is required"
            installSuccessMessage.value = null
            return
        }

        viewModelScope.launch {
            try {
                isInstalling.value = true
                installError.value = null
                installSuccessMessage.value = null
                installAddonUseCase(trimmedUrl)
                installSuccessMessage.value = "Addon installed successfully"
            } catch (e: Exception) {
                installError.value = e.message ?: "Failed to install addon"
                installSuccessMessage.value = null
            } finally {
                isInstalling.value = false
            }
        }
    }

    fun removeAddon(addonId: String) {
        viewModelScope.launch {
            addonRepository.removeAddon(addonId)
        }
    }

    fun toggleAddon(addonId: String, enabled: Boolean) {
        viewModelScope.launch {
            addonRepository.setAddonEnabled(addonId, enabled)
        }
    }

    fun moveAddon(addonId: String, newIndex: Int) {
        viewModelScope.launch {
            addonRepository.updateAddonOrder(addonId, newIndex.coerceAtLeast(0))
        }
    }
}
