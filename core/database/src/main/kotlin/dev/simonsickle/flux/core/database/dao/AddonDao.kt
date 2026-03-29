package dev.simonsickle.flux.core.database.dao

import androidx.room.*
import dev.simonsickle.flux.core.database.entity.InstalledAddonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AddonDao {
    @Query("SELECT * FROM installed_addons ORDER BY orderIndex ASC")
    fun getAllAddons(): Flow<List<InstalledAddonEntity>>

    @Query("SELECT * FROM installed_addons WHERE enabled = 1 ORDER BY orderIndex ASC")
    fun getEnabledAddons(): Flow<List<InstalledAddonEntity>>

    @Query("SELECT * FROM installed_addons WHERE id = :id")
    suspend fun getAddonById(id: String): InstalledAddonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddon(addon: InstalledAddonEntity)

    @Delete
    suspend fun deleteAddon(addon: InstalledAddonEntity)

    @Query("DELETE FROM installed_addons WHERE id = :id")
    suspend fun deleteAddonById(id: String)

    @Update
    suspend fun updateAddon(addon: InstalledAddonEntity)
}
