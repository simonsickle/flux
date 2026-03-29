package dev.simonsickle.flux.data.debrid

import dev.simonsickle.flux.core.common.SettingsRepository
import dev.simonsickle.flux.core.model.ResolvedStream
import dev.simonsickle.flux.core.model.StreamInfo
import dev.simonsickle.flux.data.debrid.api.RealDebridApi
import dev.simonsickle.flux.domain.repository.DebridRepository
import dev.simonsickle.flux.domain.repository.DebridUserInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebridRepositoryImpl @Inject constructor(
    private val realDebridApi: RealDebridApi,
    private val settingsRepository: SettingsRepository
) : DebridRepository {

    override suspend fun isConfigured(): Boolean {
        return settingsRepository.realDebridToken.first() != null
    }

    override suspend fun getUserInfo(): DebridUserInfo? {
        return runCatching {
            val user = realDebridApi.getUser()
            DebridUserInfo(
                username = user.username,
                email = user.email,
                premium = user.premium,
                expiration = user.expiration,
                points = user.points
            )
        }.getOrNull()
    }

    override suspend fun resolveStream(stream: StreamInfo): ResolvedStream {
        return if (stream.infoHash != null) {
            resolveTorrentStream(stream)
        } else if (stream.url != null) {
            resolveRestrictedUrl(stream)
        } else {
            throw IllegalArgumentException("Stream has neither URL nor infoHash")
        }
    }

    private suspend fun resolveTorrentStream(stream: StreamInfo): ResolvedStream {
        val magnet = buildMagnetUrl(stream.infoHash!!, stream.sources)
        val addResponse = realDebridApi.addMagnet(magnet)
        val torrentId = addResponse.id

        // Select files
        if (stream.fileIdx != null) {
            realDebridApi.selectFiles(torrentId, stream.fileIdx.toString())
        } else {
            realDebridApi.selectFiles(torrentId, "all")
        }

        // Poll for ready status
        var torrentInfo = realDebridApi.getTorrentInfo(torrentId)
        var attempts = 0
        while (torrentInfo.status != "downloaded" && attempts < 30) {
            delay(2000)
            torrentInfo = realDebridApi.getTorrentInfo(torrentId)
            attempts++
        }

        val linkToUnrestrict = if (stream.fileIdx != null && torrentInfo.links.size > stream.fileIdx) {
            torrentInfo.links[stream.fileIdx]
        } else {
            torrentInfo.links.firstOrNull()
                ?: throw IllegalStateException("No links available in torrent")
        }

        val unrestricted = realDebridApi.unrestrictLink(linkToUnrestrict)
        return ResolvedStream(
            url = unrestricted.download,
            filename = unrestricted.filename,
            filesize = unrestricted.filesize,
            mimeType = unrestricted.mimeType,
            isDebridCached = true,
            originalStream = stream
        )
    }

    private suspend fun resolveRestrictedUrl(stream: StreamInfo): ResolvedStream {
        val unrestricted = realDebridApi.unrestrictLink(stream.url!!)
        return ResolvedStream(
            url = unrestricted.download,
            filename = unrestricted.filename,
            filesize = unrestricted.filesize,
            mimeType = unrestricted.mimeType,
            isDebridCached = false,
            originalStream = stream
        )
    }

    override suspend fun checkInstantAvailability(infoHashes: List<String>): Map<String, Boolean> {
        val result = mutableMapOf<String, Boolean>()
        for (hash in infoHashes) {
            runCatching {
                val response = realDebridApi.getInstantAvailability(hash)
                val hashData = response[hash.lowercase()]
                result[hash] = hashData != null && hashData.toString() != "{}"
            }
        }
        return result
    }

    private fun buildMagnetUrl(infoHash: String, sources: List<String>): String {
        val trackers = sources.filter { it.startsWith("tracker:") }
            .map { it.removePrefix("tracker:") }
        val trackersParam = trackers.joinToString("") { "&tr=$it" }
        return "magnet:?xt=urn:btih:$infoHash&dn=stream$trackersParam"
    }
}
