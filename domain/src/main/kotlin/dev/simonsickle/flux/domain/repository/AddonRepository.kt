package dev.simonsickle.flux.domain.repository

import dev.simonsickle.flux.core.model.AddonManifest
import dev.simonsickle.flux.core.model.CatalogRow
import dev.simonsickle.flux.core.model.InstalledAddon
import dev.simonsickle.flux.core.model.MetaDetail
import dev.simonsickle.flux.core.model.StreamInfo
import kotlinx.coroutines.flow.Flow

interface AddonRepository {
    fun getInstalledAddons(): Flow<List<InstalledAddon>>
    suspend fun fetchManifest(transportUrl: String): AddonManifest
    suspend fun installAddon(transportUrl: String)
    suspend fun removeAddon(addonId: String)
    suspend fun updateAddonOrder(addonId: String, newIndex: Int)
    suspend fun setAddonEnabled(addonId: String, enabled: Boolean)
    suspend fun setAddonTimeout(addonId: String, timeoutMs: Long)
    suspend fun getCatalog(addon: InstalledAddon, type: String, catalogId: String, extra: Map<String, String> = emptyMap()): List<dev.simonsickle.flux.core.model.MetaPreview>
    suspend fun getMeta(addon: InstalledAddon, type: String, id: String): MetaDetail?
    suspend fun getStreams(addon: InstalledAddon, type: String, id: String): List<StreamInfo>
}
