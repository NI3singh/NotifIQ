package com.notifiq.core.database.dao

import androidx.paging.PagingSource
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.notifiq.core.database.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

data class AppInfo(
    @ColumnInfo(name = "package_name")
    val packageName: String,
    @ColumnInfo(name = "app_name")
    val appName: String
)

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE is_archived = 0 ORDER BY post_time DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE is_archived = 0 ORDER BY post_time DESC")
    fun getNotificationsPaged(): PagingSource<Int, NotificationEntity>

    @Query("SELECT * FROM notifications WHERE classification_label IN ('IMPORTANT', 'USEFUL') AND is_archived = 0 ORDER BY post_time DESC")
    fun getPriorityNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE classification_label = :label ORDER BY post_time DESC")
    fun getByLabel(label: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE package_name = :packageName ORDER BY post_time DESC")
    fun getByPackage(packageName: String): Flow<List<NotificationEntity>>

    @Query("""
        SELECT * FROM notifications
        WHERE (title LIKE '%' || :query || '%'
        OR text LIKE '%' || :query || '%'
        OR app_name LIKE '%' || :query || '%')
        AND is_archived = 0
        ORDER BY post_time DESC
    """)
    fun search(query: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE id = :id")
    suspend fun getById(id: String): NotificationEntity?

    @Query("SELECT * FROM notifications WHERE is_read = 0 AND is_archived = 0 ORDER BY post_time DESC")
    fun getUnread(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE is_read = 0 AND is_archived = 0 ORDER BY post_time DESC")
    fun getUnreadPaged(): PagingSource<Int, NotificationEntity>

    @Query("SELECT * FROM notifications WHERE classification_label = :label AND is_archived = 0 ORDER BY post_time DESC")
    fun getByLabelPaged(label: String): PagingSource<Int, NotificationEntity>

    @Query("""
        SELECT * FROM notifications
        WHERE (title LIKE '%' || :query || '%'
        OR text LIKE '%' || :query || '%'
        OR app_name LIKE '%' || :query || '%')
        AND is_archived = 0
        ORDER BY post_time DESC
    """)
    fun searchPaged(query: String): PagingSource<Int, NotificationEntity>

    @Query("SELECT COUNT(*) FROM notifications WHERE is_read = 0 AND is_archived = 0")
    fun getUnreadCount(): Flow<Int>

    @Query("SELECT * FROM notifications WHERE post_time BETWEEN :startTime AND :endTime")
    suspend fun getNotificationsBetween(startTime: Long, endTime: Long): List<NotificationEntity>

    @Query("SELECT COUNT(*) FROM notifications WHERE package_name = :packageName AND created_at >= :since")
    suspend fun getCountByPackageSince(packageName: String, since: Long): Int

    // ADDED — InboxViewModel calls this to populate filter chip counts
    @Query("SELECT COUNT(*) FROM notifications WHERE classification_label = :label AND is_archived = 0")
    suspend fun getCountByLabel(label: String): Int

    @Query("SELECT DISTINCT package_name, app_name FROM notifications")
    fun getAllApps(): Flow<List<AppInfo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity)

    @Update
    suspend fun update(notification: NotificationEntity)

    @Query("UPDATE notifications SET is_read = 1 WHERE id = :id")
    suspend fun markRead(id: String)

    @Query("UPDATE notifications SET is_read = 1")
    suspend fun markAllRead()

    @Query("UPDATE notifications SET is_archived = 1 WHERE id = :id")
    suspend fun archive(id: String)

    @Query("UPDATE notifications SET classification_label = :label, classification_score = :score, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateClassification(id: String, label: String, score: Float, updatedAt: Long)

    @Query("DELETE FROM notifications WHERE created_at < :before")
    suspend fun deleteOlderThan(before: Long)

    @Query("DELETE FROM notifications")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM notifications WHERE post_time >= :startOfDay")
    fun getTodayCount(startOfDay: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM notifications WHERE classification_label = :label AND post_time >= :startOfDay")
    fun getTodayCountByLabel(label: String, startOfDay: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM notifications WHERE is_suppressed = 1 AND post_time >= :startOfDay")
    fun getSuppressedTodayCount(startOfDay: Long): Flow<Int>
}