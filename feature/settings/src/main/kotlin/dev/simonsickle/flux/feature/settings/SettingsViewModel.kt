package dev.simonsickle.flux.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.simonsickle.flux.core.common.SettingsRepository
import dev.simonsickle.flux.domain.repository.DebridRepository
import dev.simonsickle.flux.domain.repository.DebridUserInfo
import kotlinx.coroutines.flow.MutableStateFlow
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

    private val debridUserInfo = MutableStateFlow<DebridUserInfo?>(null)
    private val isTestingConnection = MutableStateFlow(false)
    private val connectionTestResult = MutableStateFlow<String?>(null)

    private data class SettingsPreferencesState(
        val token: String?,
        val defaultContentType: String,
        val preferredPlayer: String,
        val subtitleLanguage: String,
        val hardwareAcceleration: Boolean
    )

    private val preferencesState: Flow<SettingsPreferencesState> = combine(
        settingsRepository.realDebridToken,
        settingsRepository.defaultContentType,
        settingsRepository.preferredPlayer,
        settingsRepository.subtitleLanguage,
        settingsRepository.hardwareAcceleration
    ) { token, contentType, player, subtitleLang, hwAccel ->
        SettingsPreferencesState(
            token = token,
            defaultContentType = contentType,
            preferredPlayer = player,
            subtitleLanguage = subtitleLang,
            hardwareAcceleration = hwAccel
        )
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesState,
        debridUserInfo,
        isTestingConnection,
        connectionTestResult
    ) { preferences, userInfo, testing, testResult ->
        SettingsUiState(
            realDebridToken = preferences.token ?: "",
            debridUserInfo = userInfo,
            isTestingConnection = testing,
            connectionTestResult = testResult,
            defaultContentType = preferences.defaultContentType,
            preferredPlayer = preferences.preferredPlayer,
            subtitleLanguage = preferences.subtitleLanguage,
            hardwareAcceleration = preferences.hardwareAcceleration
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun setRealDebridToken(token: String) {
        viewModelScope.launch {
            settingsRepository.setRealDebridToken(token.ifEmpty { null })
        }
    }

    fun testDebridConnection() {
        viewModelScope.launch {
            isTestingConnection.value = true
            connectionTestResult.value = null
            val result = runCatching { debridRepository.getUserInfo() }
            result.onSuccess { userInfo ->
                debridUserInfo.value = userInfo
                connectionTestResult.value = if (userInfo != null) {
                    "Connection successful"
                } else {
                    "No Real-Debrid account found for this token"
                }
            }.onFailure { error ->
                debridUserInfo.value = null
                connectionTestResult.value = error.message ?: "Connection test failed"
            }
            isTestingConnection.value = false
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
