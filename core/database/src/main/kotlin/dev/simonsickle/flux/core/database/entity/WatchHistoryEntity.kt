package dev.simonsickle.flux.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val contentId: String,
    val contentType: String,
    val title: String,
    val poster: String? = null,
    val lastPosition: Long = 0L,
    val duration: Long = 0L,
    val lastWatchedAt: Long = System.currentTimeMillis(),
    val videoId: String? = null
)
