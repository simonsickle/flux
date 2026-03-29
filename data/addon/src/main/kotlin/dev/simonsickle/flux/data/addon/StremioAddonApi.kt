package dev.simonsickle.flux.data.addon

import dev.simonsickle.flux.core.network.FluxHttpClient
import dev.simonsickle.flux.data.addon.dto.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StremioAddonApi @Inject constructor(
    private val httpClient: FluxHttpClient
) {
    suspend fun fetchManifest(baseUrl: String): AddonManifestDto {
        val url = "${baseUrl.trimEnd('/')}/manifest.json"
        return httpClient.getJson(url)
    }

    suspend fun fetchCatalog(
        baseUrl: String,
        type: String,
        catalogId: String,
        extra: Map<String, String> = emptyMap()
    ): CatalogResponseDto {
        val extraStr = if (extra.isNotEmpty()) {
            "/" + extra.entries.joinToString("&") { (k, v) -> "$k=$v" }
        } else ""
        val url = "${baseUrl.trimEnd('/')}/catalog/$type/$catalogId$extraStr.json"
        return httpClient.getJson(url)
    }

    suspend fun fetchMeta(baseUrl: String, type: String, id: String): MetaResponseDto {
        val url = "${baseUrl.trimEnd('/')}/meta/$type/$id.json"
        return httpClient.getJson(url)
    }

    suspend fun fetchStreams(baseUrl: String, type: String, id: String): StreamResponseDto {
        val url = "${baseUrl.trimEnd('/')}/stream/$type/$id.json"
        return httpClient.getJson(url)
    }
}
