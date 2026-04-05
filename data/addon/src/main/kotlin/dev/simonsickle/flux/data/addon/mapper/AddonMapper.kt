package dev.simonsickle.flux.data.addon.mapper

import dev.simonsickle.flux.core.model.*
import dev.simonsickle.flux.data.addon.dto.*

fun AddonManifestDto.toDomain() = AddonManifest(
    id = id,
    version = version,
    name = name,
    description = description,
    logo = logo,
    background = background,
    contactEmail = contactEmail,
    resources = resources,
    types = types,
    idPrefixes = idPrefixes,
    catalogs = catalogs.map { it.toDomain() },
    addonCatalogs = addonCatalogs.map { it.toDomain() },
    behaviorHints = behaviorHints.toDomain(),
    config = config.map { it.toDomain() }
)

fun AddonConfigDto.toDomain() = AddonConfig(
    key = key,
    type = type,
    title = title,
    default = default,
    options = options,
    required = required
)

fun CatalogEntryDto.toDomain() = CatalogEntry(
    type = type,
    id = id,
    name = name,
    extra = extra.map { ExtraEntry(it.name, it.isRequired, it.options, it.optionsLimit) }
)

fun BehaviorHintsDto.toDomain() = BehaviorHints(
    adult = adult,
    p2p = p2p,
    configurable = configurable,
    configurationRequired = configurationRequired
)

fun MetaPreviewDto.toDomain() = MetaPreview(
    id = id,
    type = ContentType.fromValue(type),
    name = name,
    poster = poster,
    posterShape = when (posterShape?.lowercase()) {
        "landscape" -> PosterShape.LANDSCAPE
        "square" -> PosterShape.SQUARE
        else -> PosterShape.POSTER
    },
    background = background,
    description = description,
    year = year,
    imdbRating = imdbRating,
    genres = genres
)

fun MetaDetailDto.toDomain() = MetaDetail(
    id = id,
    type = ContentType.fromValue(type),
    name = name,
    poster = poster,
    posterShape = when (posterShape?.lowercase()) {
        "landscape" -> PosterShape.LANDSCAPE
        "square" -> PosterShape.SQUARE
        else -> PosterShape.POSTER
    },
    background = background,
    logo = logo,
    description = description,
    released = released,
    releaseInfo = releaseInfo,
    imdbRating = imdbRating,
    runtime = runtime,
    language = language,
    country = country,
    genres = genres,
    director = director,
    cast = cast,
    awards = awards,
    website = website,
    trailerStreams = trailerStreams.map { TrailerStream(it.title, it.ytId) },
    links = links.map { MetaLink(it.name, it.category, it.url) },
    videos = videos.map { it.toDomain() },
    behaviorHints = ContentBehaviorHints(
        defaultVideoId = behaviorHints.defaultVideoId,
        hasScheduledVideos = behaviorHints.hasScheduledVideos
    )
)

fun VideoDto.toDomain() = Video(
    id = id,
    title = title,
    released = released,
    thumbnail = thumbnail,
    available = available,
    episode = episode,
    season = season,
    overview = overview
)

fun StreamInfoDto.toDomain() = StreamInfo(
    url = url,
    ytId = ytId,
    infoHash = infoHash,
    fileIdx = fileIdx,
    externalUrl = externalUrl,
    name = name,
    description = description ?: title,
    subtitles = subtitles.map { SubtitleInfo(it.id, it.url, it.lang) },
    sources = sources,
    behaviorHints = StreamBehaviorHints(
        notWebReady = behaviorHints.notWebReady,
        bingeGroup = behaviorHints.bingeGroup,
        countryWhitelist = behaviorHints.countryWhitelist,
        proxyHeaders = behaviorHints.proxyHeaders?.let {
            ProxyHeaders(request = it.request, response = it.response)
        },
        videoHash = behaviorHints.videoHash,
        videoSize = behaviorHints.videoSize,
        filename = behaviorHints.filename
    )
)
