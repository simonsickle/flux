package dev.simonsickle.flux.core.database.dao

import androidx.room.*
import dev.simonsickle.flux.core.database.entity.BookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY addedAt DESC")
    fun getAll(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE contentId = :contentId")
    suspend fun getByContentId(contentId: String): BookmarkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE contentId = :contentId")
    suspend fun deleteByContentId(contentId: String)
}
