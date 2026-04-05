package dev.simonsickle.flux.domain.usecase

import dev.simonsickle.flux.core.model.ResolvedStream
import dev.simonsickle.flux.core.model.StreamInfo
import dev.simonsickle.flux.domain.repository.DebridRepository
import java.net.URI
import javax.inject.Inject

class ResolveAndPlayStreamUseCase @Inject constructor(
    private val debridRepository: DebridRepository
) {
    suspend operator fun invoke(stream: StreamInfo): ResolvedStream {
        val streamUrl = stream.url
        val needsDebrid = stream.infoHash != null || (streamUrl != null && isRestrictedUrl(streamUrl))

        return if (needsDebrid && debridRepository.isConfigured()) {
            debridRepository.resolveStream(stream)
        } else if (streamUrl != null) {
            ResolvedStream(
                url = streamUrl,
                originalStream = stream
            )
        } else {
            throw IllegalArgumentException("Stream has no playable URL")
        }
    }

    private fun isRestrictedUrl(url: String): Boolean {
        val host = runCatching { URI(url).host?.lowercase() }.getOrNull() ?: return false
        return RESTRICTED_HOSTS.any { restricted ->
            host == restricted || host.endsWith(".$restricted")
        }
    }

    companion object {
        private val RESTRICTED_HOSTS = listOf(
            "real-debrid.com", "uptostream.com", "1fichier.com",
            "nitroflare.com", "rapidgator.net", "uploadgig.com"
        )
    }
}
