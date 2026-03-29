package dev.simonsickle.flux.core.database

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.simonsickle.flux.core.database.dao.AddonDao
import dev.simonsickle.flux.core.database.dao.BookmarkDao
import dev.simonsickle.flux.core.database.dao.WatchHistoryDao
import dev.simonsickle.flux.core.database.repository.WatchHistoryRepositoryImpl
import dev.simonsickle.flux.domain.repository.WatchHistoryRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideFluxDatabase(@ApplicationContext context: Context): FluxDatabase =
        Room.databaseBuilder(context, FluxDatabase::class.java, "flux.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideAddonDao(db: FluxDatabase): AddonDao = db.addonDao()

    @Provides
    fun provideWatchHistoryDao(db: FluxDatabase): WatchHistoryDao = db.watchHistoryDao()

    @Provides
    fun provideBookmarkDao(db: FluxDatabase): BookmarkDao = db.bookmarkDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DatabaseBindingModule {

    @Binds
    @Singleton
    abstract fun bindWatchHistoryRepository(impl: WatchHistoryRepositoryImpl): WatchHistoryRepository
}
