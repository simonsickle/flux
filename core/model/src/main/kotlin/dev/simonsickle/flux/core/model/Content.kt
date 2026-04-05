package dev.simonsickle.flux.core.model

enum class PosterShape { POSTER, LANDSCAPE, SQUARE }

data class MetaPreview(
    val id: String,
    val type: ContentType,
    val name: String,
    val poster: String? = null,
    val posterShape: PosterShape = PosterShape.POSTER,
    val background: String? = null,
    val description: String? = null,
    val year: String? = null,
    val imdbRating: String? = null,
    val genres: List<String> = emptyList()
)

data class MetaDetail(
    val id: String,
    val type: ContentType,
    val name: String,
    val poster: String? = null,
    val posterShape: PosterShape = PosterShape.POSTER,
    val background: String? = null,
    val logo: String? = null,
    val description: String? = null,
    val released: String? = null,
    val releaseInfo: String? = null,
    val imdbRating: String? = null,
    val runtime: String? = null,
    val language: String? = null,
    val country: String? = null,
    val genres: List<String> = emptyList(),
    val director: List<String> = emptyList(),
    val cast: List<String> = emptyList(),
    val awards: String? = null,
    val website: String? = null,
    val trailerStreams: List<TrailerStream> = emptyList(),
    val links: List<MetaLink> = emptyList(),
    val videos: List<Video> = emptyList(),
    val behaviorHints: ContentBehaviorHints = ContentBehaviorHints()
)

data class TrailerStream(
    val title: String,
    val ytId: String
)

data class MetaLink(
    val name: String,
    val category: String,
    val url: String
)

data class Video(
    val id: String,
    val title: String,
    val released: String? = null,
    val thumbnail: String? = null,
    val streams: List<StreamInfo> = emptyList(),
    val available: Boolean = false,
    val episode: Int? = null,
    val season: Int? = null,
    val trailers: List<TrailerStream> = emptyList(),
    val overview: String? = null
)

data class ContentBehaviorHints(
    val defaultVideoId: String? = null,
    val hasScheduledVideos: Boolean = false
)

data class CatalogRow(
    val addonId: String,
    val addonName: String,
    val catalogId: String,
    val catalogName: String,
    val type: ContentType,
    val items: List<MetaPreview>
)
