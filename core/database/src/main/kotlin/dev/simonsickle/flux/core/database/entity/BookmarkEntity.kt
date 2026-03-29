package dev.simonsickle.flux.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val contentId: String,
    val contentType: String,
    val title: String,
    val poster: String? = null,
    val addedAt: Long = System.currentTimeMillis()
)
