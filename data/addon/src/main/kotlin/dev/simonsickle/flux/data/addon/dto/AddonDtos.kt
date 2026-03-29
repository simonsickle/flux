package dev.simonsickle.flux.data.addon.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddonManifestDto(
    val id: String = "",
    val version: String = "0.0.1",
    val name: String = "",
    val description: String = "",
    val logo: String? = null,
    val background: String? = null,
    val resources: List<String> = emptyList(),
    val types: List<String> = emptyList(),
    val idPrefixes: List<String> = emptyList(),
    val catalogs: List<CatalogEntryDto> = emptyList(),
    val behaviorHints: BehaviorHintsDto = BehaviorHintsDto()
)

@Serializable
data class CatalogEntryDto(
    val type: String = "",
    val id: String = "",
    val name: String = "",
    val extra: List<ExtraEntryDto> = emptyList()
)

@Serializable
data class ExtraEntryDto(
    val name: String = "",
    val isRequired: Boolean = false,
    val options: List<String> = emptyList()
)

@Serializable
data class BehaviorHintsDto(
    val adult: Boolean = false,
    val p2p: Boolean = false,
    val configurable: Boolean = false,
    val configurationRequired: Boolean = false
)

@Serializable
data class CatalogResponseDto(
    val metas: List<MetaPreviewDto> = emptyList()
)

@Serializable
data class MetaPreviewDto(
    val id: String = "",
    val type: String = "",
    val name: String = "",
    val poster: String? = null,
    val posterShape: String? = null,
    val background: String? = null,
    val description: String? = null,
    val year: String? = null,
    @SerialName("imdbRating") val imdbRating: String? = null,
    val genres: List<String> = emptyList()
)

@Serializable
data class MetaResponseDto(
    val meta: MetaDetailDto? = null
)

@Serializable
data class MetaDetailDto(
    val id: String = "",
    val type: String = "",
    val name: String = "",
    val poster: String? = null,
    val posterShape: String? = null,
    val background: String? = null,
    val logo: String? = null,
    val description: String? = null,
    val releaseInfo: String? = null,
    val imdbRating: String? = null,
    val runtime: String? = null,
    val genres: List<String> = emptyList(),
    val director: List<String> = emptyList(),
    val cast: List<String> = emptyList(),
    val awards: String? = null,
    val website: String? = null,
    val trailerStreams: List<TrailerStreamDto> = emptyList(),
    val links: List<MetaLinkDto> = emptyList(),
    val videos: List<VideoDto> = emptyList(),
    val behaviorHints: ContentBehaviorHintsDto = ContentBehaviorHintsDto()
)

@Serializable
data class TrailerStreamDto(
    val title: String = "",
    val ytId: String = ""
)

@Serializable
data class MetaLinkDto(
    val name: String = "",
    val category: String = "",
    val url: String = ""
)

@Serializable
data class VideoDto(
    val id: String = "",
    val title: String = "",
    val released: String? = null,
    val thumbnail: String? = null,
    val available: Boolean = false,
    val episode: Int? = null,
    val season: Int? = null,
    val overview: String? = null
)

@Serializable
data class ContentBehaviorHintsDto(
    val defaultVideoId: String? = null,
    val hasScheduledVideos: Boolean = false
)

@Serializable
data class StreamResponseDto(
    val streams: List<StreamInfoDto> = emptyList()
)

@Serializable
data class StreamInfoDto(
    val url: String? = null,
    val ytId: String? = null,
    val infoHash: String? = null,
    val fileIdx: Int? = null,
    val externalUrl: String? = null,
    val name: String? = null,
    val description: String? = null,
    val subtitles: List<SubtitleInfoDto> = emptyList(),
    val sources: List<String> = emptyList(),
    val behaviorHints: StreamBehaviorHintsDto = StreamBehaviorHintsDto()
)

@Serializable
data class StreamBehaviorHintsDto(
    val notWebReady: Boolean = false,
    val bingeGroup: String? = null,
    val countryWhitelist: List<String> = emptyList()
)

@Serializable
data class SubtitleInfoDto(
    val id: String = "",
    val url: String = "",
    val lang: String = ""
)
