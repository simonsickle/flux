package dev.simonsickle.flux.core.common

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "flux_settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val REAL_DEBRID_TOKEN = stringPreferencesKey("real_debrid_token")
        val DEFAULT_CONTENT_TYPE = stringPreferencesKey("default_content_type")
        val PREFERRED_PLAYER = stringPreferencesKey("preferred_player")
        val SUBTITLE_LANGUAGE = stringPreferencesKey("subtitle_language")
        val HARDWARE_ACCELERATION = booleanPreferencesKey("hardware_acceleration")
    }

    val realDebridToken: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[Keys.REAL_DEBRID_TOKEN]
    }

    val defaultContentType: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.DEFAULT_CONTENT_TYPE] ?: "movie"
    }

    val preferredPlayer: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.PREFERRED_PLAYER] ?: "media3"
    }

    val subtitleLanguage: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.SUBTITLE_LANGUAGE] ?: "eng"
    }

    val hardwareAcceleration: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.HARDWARE_ACCELERATION] ?: true
    }

    suspend fun setRealDebridToken(token: String?) {
        context.dataStore.edit { prefs ->
            if (token == null) prefs.remove(Keys.REAL_DEBRID_TOKEN)
            else prefs[Keys.REAL_DEBRID_TOKEN] = token
        }
    }

    suspend fun setDefaultContentType(type: String) {
        context.dataStore.edit { prefs -> prefs[Keys.DEFAULT_CONTENT_TYPE] = type }
    }

    suspend fun setPreferredPlayer(player: String) {
        context.dataStore.edit { prefs -> prefs[Keys.PREFERRED_PLAYER] = player }
    }

    suspend fun setSubtitleLanguage(lang: String) {
        context.dataStore.edit { prefs -> prefs[Keys.SUBTITLE_LANGUAGE] = lang }
    }

    suspend fun setHardwareAcceleration(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.HARDWARE_ACCELERATION] = enabled }
    }
}
