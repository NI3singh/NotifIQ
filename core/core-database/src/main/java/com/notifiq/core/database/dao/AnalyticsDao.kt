package com.notifiq.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.notifiq.core.database.entity.AnalyticsDailyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalyticsDao {
    @Query("SELECT * FROM analytics_daily")
    fun getAll(): Flow<List<AnalyticsDailyEntity>>

    @Query("SELECT * FROM analytics_daily WHERE date = :date")
    suspend fun getByDate(date: String): AnalyticsDailyEntity?

    @Query("SELECT * FROM analytics_daily ORDER BY date DESC LIMIT :n")
    suspend fun getLastNDays(n: Int): List<AnalyticsDailyEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(analytics: AnalyticsDailyEntity)

    @Query("DELETE FROM analytics_daily WHERE date < :date")
    suspend fun deleteOlderThan(date: String)

    @Query("DELETE FROM analytics_daily")
    suspend fun deleteAll()
}