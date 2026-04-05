package dev.simonsickle.flux.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "installed_addons")
data class InstalledAddonEntity(
    @PrimaryKey val id: String,
    val transportUrl: String,
    val manifestJson: String,
    val enabled: Boolean = true,
    val orderIndex: Int = 0,
    val installedAt: Long = System.currentTimeMillis(),
    val timeoutMs: Long = DEFAULT_ADDON_TIMEOUT_MS
) {
    companion object {
        const val DEFAULT_ADDON_TIMEOUT_MS = 15_000L
    }
}
