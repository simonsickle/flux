package dev.simonsickle.flux.domain.repository

import dev.simonsickle.flux.core.model.MetaPreview
import kotlinx.coroutines.flow.Flow

data class WatchHistoryEntry(
    val contentId: String,
    val contentType: String,
    val title: String,
    val poster: String?,
    val lastPosition: Long,
    val duration: Long,
    val lastWatchedAt: Long,
    val videoId: String?
) {
    val progressFraction: Float
        get() = if (duration > 0) (lastPosition.toFloat() / duration).coerceIn(0f, 1f) else 0f
}

interface WatchHistoryRepository {
    fun getRecentlyWatched(limit: Int = 20): Flow<List<WatchHistoryEntry>>
    suspend fun getEntry(contentId: String): WatchHistoryEntry?
    suspend fun upsertEntry(entry: WatchHistoryEntry)
    suspend fun deleteEntry(contentId: String)
    suspend fun clearAll()
}
