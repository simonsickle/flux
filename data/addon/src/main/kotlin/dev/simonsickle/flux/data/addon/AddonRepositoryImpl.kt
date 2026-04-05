package dev.simonsickle.flux.data.addon

import dev.simonsickle.flux.core.database.dao.AddonDao
import dev.simonsickle.flux.core.database.entity.InstalledAddonEntity
import dev.simonsickle.flux.core.model.InstalledAddon
import dev.simonsickle.flux.core.model.AddonManifest
import dev.simonsickle.flux.core.model.MetaDetail
import dev.simonsickle.flux.core.model.MetaPreview
import dev.simonsickle.flux.core.model.StreamInfo
import dev.simonsickle.flux.data.addon.mapper.toDomain
import dev.simonsickle.flux.domain.repository.AddonRepository
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddonRepositoryImpl @Inject constructor(
    private val addonApi: StremioAddonApi,
    private val addonDao: AddonDao,
    private val json: Json
) : AddonRepository {

    override fun getInstalledAddons(): Flow<List<InstalledAddon>> =
        addonDao.getAllAddons().map { entities ->
            entities.mapNotNull { entity ->
                runCatching {
                    val manifestDto = json.decodeFromString<dev.simonsickle.flux.data.addon.dto.AddonManifestDto>(entity.manifestJson)
                    InstalledAddon(
                        manifest = manifestDto.toDomain(),
                        transportUrl = entity.transportUrl,
                        enabled = entity.enabled,
                        orderIndex = entity.orderIndex,
                        timeoutMs = entity.timeoutMs
                    )
                }.onFailure { e ->
                    Log.w(TAG, "Failed to parse manifest for addon '${entity.id}', skipping: ${e.message}")
                }.getOrNull()
            }
        }

    override suspend fun fetchManifest(transportUrl: String): AddonManifest {
        val dto = addonApi.fetchManifest(transportUrl)
        return dto.toDomain()
    }

    override suspend fun installAddon(transportUrl: String) {
        val dto = addonApi.fetchManifest(transportUrl)
        val entity = InstalledAddonEntity(
            id = dto.id,
            transportUrl = transportUrl,
            manifestJson = json.encodeToString(dto)
        )
        addonDao.insertAddon(entity)
    }

    companion object {
        private const val TAG = "AddonRepository"
    }

    override suspend fun removeAddon(addonId: String) {
        addonDao.deleteAddonById(addonId)
    }

    override suspend fun updateAddonOrder(addonId: String, newIndex: Int) {
        val currentAddons = addonDao.getAllAddons().first()
        val currentIndex = currentAddons.indexOfFirst { it.id == addonId }
        if (currentIndex == -1) return
        val boundedIndex = newIndex.coerceIn(0, currentAddons.lastIndex)
        if (currentIndex == boundedIndex) return

        val reordered = currentAddons.toMutableList().apply {
            add(boundedIndex, removeAt(currentIndex))
        }

        reordered.forEachIndexed { index, addon ->
            addonDao.updateAddon(addon.copy(orderIndex = index))
        }
    }

    override suspend fun setAddonEnabled(addonId: String, enabled: Boolean) {
        val entity = addonDao.getAddonById(addonId) ?: return
        addonDao.updateAddon(entity.copy(enabled = enabled))
    }

    override suspend fun setAddonTimeout(addonId: String, timeoutMs: Long) {
        addonDao.updateAddonTimeout(addonId, timeoutMs)
    }

    override suspend fun getCatalog(
        addon: InstalledAddon,
        type: String,
        catalogId: String,
        extra: Map<String, String>
    ): List<MetaPreview> {
        val response = addonApi.fetchCatalog(addon.transportUrl, type, catalogId, extra)
        return response.metas.map { it.toDomain() }
    }

    override suspend fun getMeta(addon: InstalledAddon, type: String, id: String): MetaDetail? {
        val response = addonApi.fetchMeta(addon.transportUrl, type, id)
        return response.meta?.toDomain()
    }

    override suspend fun getStreams(addon: InstalledAddon, type: String, id: String): List<StreamInfo> {
        val response = addonApi.fetchStreams(addon.transportUrl, type, id)
        return response.streams.map { it.toDomain() }
    }
}
