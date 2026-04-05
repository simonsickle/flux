package dev.simonsickle.flux.core.sync

import kotlinx.serialization.Serializable

/**
 * The complete data blob transferred during a P2P sync.
 * Serialized to JSON, then encrypted with AES-256-GCM before transmission.
 */
@Serializable
data class SyncPayload(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val settings: SyncSettings = SyncSettings(),
    val addons: List<SyncAddon> = emptyList(),
    val bookmarks: List<SyncBookmark> = emptyList(),
    val watchHistory: List<SyncWatchHistoryEntry> = emptyList()
)

@Serializable
data class SyncSettings(
    val realDebridToken: String? = null,
    val defaultContentType: String = "movie",
    val preferredPlayer: String = "media3",
    val subtitleLanguage: String = "eng",
    val hardwareAcceleration: Boolean = true
)

@Serializable
data class SyncAddon(
    val id: String,
    val transportUrl: String,
    val manifestJson: String,
    val enabled: Boolean = true,
    val orderIndex: Int = 0
)

@Serializable
data class SyncBookmark(
    val contentId: String,
    val contentType: String,
    val title: String,
    val poster: String? = null,
    val addedAt: Long = 0L
)

@Serializable
data class SyncWatchHistoryEntry(
    val contentId: String,
    val contentType: String,
    val title: String,
    val poster: String? = null,
    val lastPosition: Long = 0L,
    val duration: Long = 0L,
    val lastWatchedAt: Long = 0L,
    val videoId: String? = null
)

/** What the receiver chose to import. */
data class SyncImportOptions(
    val settings: Boolean = true,
    val realDebridToken: Boolean = true,
    val addons: Boolean = true,
    val bookmarks: Boolean = false,
    val watchHistory: Boolean = false
)

/** Protocol messages exchanged over local HTTP. */
@Serializable
internal data class PairRequest(val pinHash: String)

@Serializable
internal data class PairResponse(val salt: String, val accepted: Boolean)

@Serializable
internal data class SyncRequest(val encryptedPayload: String)

@Serializable
internal data class SyncResponse(val encryptedPayload: String)
