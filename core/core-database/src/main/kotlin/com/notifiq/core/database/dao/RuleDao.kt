package com.notifiq.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.notifiq.core.database.entity.RuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RuleDao {
    @Query("SELECT * FROM classification_rules")
    fun getAll(): Flow<List<RuleEntity>>

    @Query("SELECT * FROM classification_rules WHERE is_active = 1 ORDER BY priority DESC")
    suspend fun getActiveRules(): List<RuleEntity>

    @Query("SELECT * FROM classification_rules WHERE type = :type")
    suspend fun getByType(type: String): List<RuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: RuleEntity)

    @Update
    suspend fun update(rule: RuleEntity)

    @Query("UPDATE classification_rules SET hit_count = hit_count + 1 WHERE id = :id")
    suspend fun incrementHitCount(id: String)

    @Query("DELETE FROM classification_rules WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM classification_rules")
    suspend fun deleteAll()
}