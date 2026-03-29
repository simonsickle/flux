package dev.simonsickle.flux.core.database.repository

import dev.simonsickle.flux.core.database.dao.WatchHistoryDao
import dev.simonsickle.flux.core.database.entity.WatchHistoryEntity
import dev.simonsickle.flux.domain.repository.WatchHistoryEntry
import dev.simonsickle.flux.domain.repository.WatchHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchHistoryRepositoryImpl @Inject constructor(
    private val dao: WatchHistoryDao
) : WatchHistoryRepository {

    override fun getRecentlyWatched(limit: Int): Flow<List<WatchHistoryEntry>> =
        dao.getRecent(limit).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getEntry(contentId: String): WatchHistoryEntry? =
        dao.getByContentId(contentId)?.toDomain()

    override suspend fun upsertEntry(entry: WatchHistoryEntry) {
        dao.upsert(entry.toEntity())
    }

    override suspend fun deleteEntry(contentId: String) {
        dao.deleteByContentId(contentId)
    }

    override suspend fun clearAll() {
        dao.clearAll()
    }

    private fun WatchHistoryEntity.toDomain() = WatchHistoryEntry(
        contentId = contentId,
        contentType = contentType,
        title = title,
        poster = poster,
        lastPosition = lastPosition,
        duration = duration,
        lastWatchedAt = lastWatchedAt,
        videoId = videoId
    )

    private fun WatchHistoryEntry.toEntity() = WatchHistoryEntity(
        contentId = contentId,
        contentType = contentType,
        title = title,
        poster = poster,
        lastPosition = lastPosition,
        duration = duration,
        lastWatchedAt = lastWatchedAt,
        videoId = videoId
    )
}
