package dev.simonsickle.flux.domain.usecase

import dev.simonsickle.flux.core.model.ResolvedStream
import dev.simonsickle.flux.core.model.StreamInfo
import dev.simonsickle.flux.domain.repository.DebridRepository
import javax.inject.Inject

class ResolveAndPlayStreamUseCase @Inject constructor(
    private val debridRepository: DebridRepository
) {
    suspend operator fun invoke(stream: StreamInfo): ResolvedStream {
        val needsDebrid = stream.infoHash != null || (stream.url != null && isRestrictedUrl(stream.url))

        return if (needsDebrid && debridRepository.isConfigured()) {
            debridRepository.resolveStream(stream)
        } else if (stream.url != null) {
            ResolvedStream(
                url = stream.url,
                originalStream = stream
            )
        } else {
            throw IllegalArgumentException("Stream has no playable URL")
        }
    }

    private fun isRestrictedUrl(url: String): Boolean {
        val restrictedHosts = listOf(
            "real-debrid.com", "uptostream.com", "1fichier.com",
            "nitroflare.com", "rapidgator.net", "uploadgig.com"
        )
        return restrictedHosts.any { url.contains(it) }
    }
}
