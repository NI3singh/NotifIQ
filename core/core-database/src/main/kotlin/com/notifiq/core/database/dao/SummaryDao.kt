package com.notifiq.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.notifiq.core.database.entity.SummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SummaryDao {
    @Query("SELECT * FROM summaries")
    fun getAll(): Flow<List<SummaryEntity>>

    @Query("SELECT * FROM summaries WHERE type = :type ORDER BY created_at DESC LIMIT 1")
    suspend fun getLatest(type: String): SummaryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(summary: SummaryEntity)

    @Query("DELETE FROM summaries")
    suspend fun deleteAll()
}