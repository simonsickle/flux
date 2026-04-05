package dev.simonsickle.flux.domain.usecase

import dev.simonsickle.flux.domain.repository.AddonRepository
import kotlinx.coroutines.flow.first
import java.net.URI
import javax.inject.Inject

class InstallAddonUseCase @Inject constructor(
    private val addonRepository: AddonRepository
) {
    suspend operator fun invoke(transportUrl: String) {
        validateUrl(transportUrl)

        val manifest = addonRepository.fetchManifest(transportUrl)

        if (manifest.id.isBlank()) {
            throw IllegalArgumentException("Addon manifest is missing a valid ID")
        }
        if (manifest.name.isBlank()) {
            throw IllegalArgumentException("Addon manifest is missing a name")
        }

        // Check for duplicates
        val existing = addonRepository.getInstalledAddons().first()
        if (existing.any { it.manifest.id == manifest.id }) {
            throw IllegalStateException("Addon '${manifest.name}' is already installed")
        }

        addonRepository.installAddon(transportUrl)
    }

    private fun validateUrl(url: String) {
        val uri = runCatching { URI(url) }.getOrElse {
            throw IllegalArgumentException("Invalid URL format")
        }
        val scheme = uri.scheme?.lowercase()
        if (scheme != "http" && scheme != "https") {
            throw IllegalArgumentException("Only HTTP and HTTPS URLs are supported")
        }
        if (uri.host.isNullOrBlank()) {
            throw IllegalArgumentException("URL must have a valid host")
        }
    }
}
