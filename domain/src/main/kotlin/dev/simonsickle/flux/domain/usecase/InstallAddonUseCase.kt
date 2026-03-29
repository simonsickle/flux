package dev.simonsickle.flux.domain.usecase

import dev.simonsickle.flux.domain.repository.AddonRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class InstallAddonUseCase @Inject constructor(
    private val addonRepository: AddonRepository
) {
    suspend operator fun invoke(transportUrl: String) {
        // Validate by fetching manifest
        val manifest = addonRepository.fetchManifest(transportUrl)

        // Check for duplicates
        val existing = addonRepository.getInstalledAddons().first()
        if (existing.any { it.manifest.id == manifest.id }) {
            throw IllegalStateException("Addon '${manifest.name}' is already installed")
        }

        addonRepository.installAddon(transportUrl)
    }
}
