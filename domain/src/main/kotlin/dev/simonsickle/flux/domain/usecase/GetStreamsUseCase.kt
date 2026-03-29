package dev.simonsickle.flux.domain.usecase

import dev.simonsickle.flux.core.model.StreamInfo
import dev.simonsickle.flux.domain.repository.AddonRepository
import dev.simonsickle.flux.domain.repository.DebridRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetStreamsUseCase @Inject constructor(
    private val addonRepository: AddonRepository,
    private val debridRepository: DebridRepository
) {
    suspend operator fun invoke(type: String, id: String): List<StreamInfo> = coroutineScope {
        val addons = addonRepository.getInstalledAddons().first()
            .filter { it.enabled && it.manifest.resources.contains("stream") }

        val allStreams = addons.map { addon ->
            async {
                runCatching { addonRepository.getStreams(addon, type, id) }
                    .getOrDefault(emptyList())
            }
        }.awaitAll().flatten()

        // Check instant availability for torrent streams if debrid configured
        if (debridRepository.isConfigured()) {
            val infoHashes = allStreams.mapNotNull { it.infoHash }
            if (infoHashes.isNotEmpty()) {
                val availability = runCatching {
                    debridRepository.checkInstantAvailability(infoHashes)
                }.getOrDefault(emptyMap())

                return@coroutineScope allStreams.sortedWith(
                    compareByDescending { stream ->
                        stream.infoHash?.let { availability[it] } ?: false
                    }
                )
            }
        }

        allStreams
    }
}
