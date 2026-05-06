package com.notifiq.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.notifiq.core.database.entity.AppPreferenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppPreferenceDao {
    @Query("SELECT * FROM app_preferences")
    fun getAll(): Flow<List<AppPreferenceEntity>>

    @Query("SELECT * FROM app_preferences WHERE package_name = :packageName")
    suspend fun getByPackage(packageName: String): AppPreferenceEntity?

    @Query("SELECT * FROM app_preferences WHERE is_allowlisted = 1")
    fun getAllowlistedApps(): Flow<List<AppPreferenceEntity>>

    @Query("SELECT * FROM app_preferences WHERE is_blocklisted = 1")
    fun getBlocklistedApps(): Flow<List<AppPreferenceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pref: AppPreferenceEntity)

    @Query("UPDATE app_preferences SET trust_score = :trustScore, updated_at = :updatedAt WHERE package_name = :packageName")
    suspend fun updateTrustScore(packageName: String, trustScore: Double, updatedAt: Long)

    @Query("UPDATE app_preferences SET is_allowlisted = :allowed, updated_at = :updatedAt WHERE package_name = :packageName")
    suspend fun setAllowlisted(packageName: String, allowed: Boolean, updatedAt: Long)

    @Query("UPDATE app_preferences SET is_blocklisted = :blocked, updated_at = :updatedAt WHERE package_name = :packageName")
    suspend fun setBlocklisted(packageName: String, blocked: Boolean, updatedAt: Long)

    @Query("UPDATE app_preferences SET total_notifications = total_notifications + 1, updated_at = :updatedAt WHERE package_name = :packageName")
    suspend fun incrementNotificationCount(packageName: String, updatedAt: Long)

    @Query("UPDATE app_preferences SET important_count = important_count + 1, updated_at = :updatedAt WHERE package_name = :packageName")
    suspend fun incrementImportantCount(packageName: String, updatedAt: Long)

    @Query("UPDATE app_preferences SET spam_count = spam_count + 1, updated_at = :updatedAt WHERE package_name = :packageName")
    suspend fun incrementSpamCount(packageName: String, updatedAt: Long)

    @Query("DELETE FROM app_preferences")
    suspend fun deleteAll()
}