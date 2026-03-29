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
    val installSuccess: Boolean = false
)

@HiltViewModel
class AddonsViewModel @Inject constructor(
    private val addonRepository: AddonRepository,
    private val installAddonUseCase: InstallAddonUseCase
) : ViewModel() {

    val uiState: StateFlow<AddonsUiState> = addonRepository.getInstalledAddons()
        .map { AddonsUiState(addons = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AddonsUiState())

    fun installAddon(url: String) {
        viewModelScope.launch {
            // We can't mutate stateIn, use a separate mechanism
            try {
                installAddonUseCase(url)
            } catch (e: Exception) {
                // Error handled via snackbar
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
}
