package dev.simonsickle.flux.core.database.dao

import androidx.room.*
import dev.simonsickle.flux.core.database.entity.WatchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchHistoryDao {
    @Query("SELECT * FROM watch_history ORDER BY lastWatchedAt DESC")
    fun getAll(): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history ORDER BY lastWatchedAt DESC LIMIT :limit")
    fun getRecent(limit: Int = 20): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE contentId = :contentId")
    suspend fun getByContentId(contentId: String): WatchHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE contentId = :contentId")
    suspend fun deleteByContentId(contentId: String)

    @Query("DELETE FROM watch_history")
    suspend fun clearAll()
}
