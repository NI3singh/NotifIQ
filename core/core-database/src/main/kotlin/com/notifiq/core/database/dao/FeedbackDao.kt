package com.notifiq.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.notifiq.core.database.entity.FeedbackEntity

@Dao
interface FeedbackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(feedback: FeedbackEntity)

    @Query("SELECT * FROM feedback WHERE notification_id = :notificationId")
    suspend fun getByNotificationId(notificationId: String): List<FeedbackEntity>

    @Query("SELECT * FROM feedback WHERE package_name = :packageName")
    suspend fun getByPackage(packageName: String): List<FeedbackEntity>

    @Query("SELECT * FROM feedback WHERE created_at > :since")
    suspend fun getRecentFeedback(since: Long): List<FeedbackEntity>

    @Query("SELECT * FROM feedback WHERE package_name = :packageName AND created_at > :since")
    suspend fun getRecentByPackage(packageName: String, since: Long): List<FeedbackEntity>

    @Query("DELETE FROM feedback")
    suspend fun deleteAll()
}