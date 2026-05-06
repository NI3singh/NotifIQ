package com.notifiq.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.notifiq.core.database.entity.SenderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SenderDao {
    @Query("SELECT * FROM sender_rules")
    fun getAll(): Flow<List<SenderEntity>>

    @Query("SELECT * FROM sender_rules WHERE is_allowlisted = 1")
    fun getAllowlisted(): Flow<List<SenderEntity>>

    @Query("SELECT * FROM sender_rules WHERE is_blocklisted = 1")
    fun getBlocklisted(): Flow<List<SenderEntity>>

    @Query("SELECT * FROM sender_rules WHERE identifier = :identifier")
    suspend fun getByIdentifier(identifier: String): SenderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sender: SenderEntity)

    @Query("DELETE FROM sender_rules WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM sender_rules")
    suspend fun deleteAll()
}