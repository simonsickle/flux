package dev.simonsickle.flux.core.model

data class StreamInfo(
    val url: String? = null,
    val ytId: String? = null,
    val infoHash: String? = null,
    val fileIdx: Int? = null,
    val externalUrl: String? = null,
    val name: String? = null,
    val description: String? = null,
    val subtitles: List<SubtitleInfo> = emptyList(),
    val sources: List<String> = emptyList(),
    val behaviorHints: StreamBehaviorHints = StreamBehaviorHints()
)

data class StreamBehaviorHints(
    val notWebReady: Boolean = false,
    val bingeGroup: String? = null,
    val countryWhitelist: List<String> = emptyList()
)

data class SubtitleInfo(
    val id: String,
    val url: String,
    val lang: String
)

data class ResolvedStream(
    val url: String,
    val filename: String? = null,
    val filesize: Long? = null,
    val mimeType: String? = null,
    val isDebridCached: Boolean = false,
    val originalStream: StreamInfo
)
