package dev.simonsickle.flux.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.simonsickle.flux.core.common.SettingsRepository
import dev.simonsickle.flux.domain.repository.DebridRepository
import dev.simonsickle.flux.domain.repository.DebridUserInfo
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val realDebridToken: String = "",
    val debridUserInfo: DebridUserInfo? = null,
    val isTestingConnection: Boolean = false,
    val connectionTestResult: String? = null,
    val defaultContentType: String = "movie",
    val preferredPlayer: String = "media3",
    val subtitleLanguage: String = "eng",
    val hardwareAcceleration: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val debridRepository: DebridRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.realDebridToken,
        settingsRepository.defaultContentType,
        settingsRepository.preferredPlayer,
        settingsRepository.subtitleLanguage,
        settingsRepository.hardwareAcceleration
    ) { token, contentType, player, subtitleLang, hwAccel ->
        SettingsUiState(
            realDebridToken = token ?: "",
            defaultContentType = contentType,
            preferredPlayer = player,
            subtitleLanguage = subtitleLang,
            hardwareAcceleration = hwAccel
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun setRealDebridToken(token: String) {
        viewModelScope.launch {
            settingsRepository.setRealDebridToken(token.ifEmpty { null })
        }
    }

    fun testDebridConnection() {
        viewModelScope.launch {
            // Update token first if needed
            val userInfo = runCatching { debridRepository.getUserInfo() }
            // We can't mutate uiState directly since it's derived from flows
            // Just trigger a re-fetch
        }
    }

    fun setDefaultContentType(type: String) {
        viewModelScope.launch { settingsRepository.setDefaultContentType(type) }
    }

    fun setPreferredPlayer(player: String) {
        viewModelScope.launch { settingsRepository.setPreferredPlayer(player) }
    }

    fun setSubtitleLanguage(lang: String) {
        viewModelScope.launch { settingsRepository.setSubtitleLanguage(lang) }
    }

    fun setHardwareAcceleration(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setHardwareAcceleration(enabled) }
    }
}
