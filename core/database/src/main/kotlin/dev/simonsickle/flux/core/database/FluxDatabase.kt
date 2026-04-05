package dev.simonsickle.flux.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.simonsickle.flux.core.database.dao.AddonDao
import dev.simonsickle.flux.core.database.dao.BookmarkDao
import dev.simonsickle.flux.core.database.dao.WatchHistoryDao
import dev.simonsickle.flux.core.database.entity.BookmarkEntity
import dev.simonsickle.flux.core.database.entity.InstalledAddonEntity
import dev.simonsickle.flux.core.database.entity.WatchHistoryEntity

@Database(
    entities = [InstalledAddonEntity::class, WatchHistoryEntity::class, BookmarkEntity::class],
    version = 2,
    exportSchema = true
)
abstract class FluxDatabase : RoomDatabase() {
    abstract fun addonDao(): AddonDao
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun bookmarkDao(): BookmarkDao
}
