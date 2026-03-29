package dev.simonsickle.flux.domain.usecase

import dev.simonsickle.flux.core.model.MetaDetail
import dev.simonsickle.flux.domain.repository.AddonRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetContentDetailUseCase @Inject constructor(
    private val addonRepository: AddonRepository
) {
    suspend operator fun invoke(type: String, id: String): MetaDetail? {
        val addons = addonRepository.getInstalledAddons().first()
            .filter { it.enabled }

        // First try addons that support this id prefix
        val matchingAddon = addons.firstOrNull { addon ->
            addon.manifest.idPrefixes.any { prefix -> id.startsWith(prefix) } &&
            addon.manifest.resources.contains("meta")
        }

        if (matchingAddon != null) {
            val result = runCatching { addonRepository.getMeta(matchingAddon, type, id) }
            if (result.isSuccess && result.getOrNull() != null) {
                return result.getOrNull()
            }
        }

        // Fallback: query all addons that support meta
        for (addon in addons) {
            if (!addon.manifest.resources.contains("meta")) continue
            val result = runCatching { addonRepository.getMeta(addon, type, id) }
            val meta = result.getOrNull()
            if (meta != null) return meta
        }

        return null
    }
}
