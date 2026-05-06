package com.notifiq.core.database.mapper

import com.notifiq.core.database.entity.NotificationEntity
import com.notifiq.core.model.ClassificationLabel
import com.notifiq.core.model.NotificationAction
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EntityMappersTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun createTestNotificationEntity(): NotificationEntity {
        return NotificationEntity(
            id = "notif_123",
            key = "pkg_123_user_456",
            packageName = "com.whatsapp",
            appName = "WhatsApp",
            title = "New message from John",
            text = "Hey, how are you?",
            subText = "",
            bigText = "",
            postTime = System.currentTimeMillis(),
            channelId = "messages",
            channelName = "Messages",
            groupKey = "",
            category = "msg",
            priority = 0,
            importance = 4,
            classificationLabel = "USEFUL",
            classificationScore = 0.75f,
            classificationReasons = json.encodeToString(listOf("Messaging app", "Direct message")),
            action = "SHOW_AND_INBOX",
            isRead = false,
            isSuppressed = false,
            isArchived = false,
            rawPayloadHash = "abc123hash",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    @Test
    fun `toDomainModel converts all fields correctly`() {
        val entity = createTestNotificationEntity()

        val domainModel = entity.toDomainModel()

        assertEquals("ID should match", entity.id, domainModel.id)
        assertEquals("Package name should match", entity.packageName, domainModel.packageName)
        assertEquals("App name should match", entity.appName, domainModel.appName)
        assertEquals("Title should match", entity.title, domainModel.title)
        assertEquals("Text should match", entity.text, domainModel.text)
        assertEquals("Classification label should match", ClassificationLabel.USEFUL, domainModel.classificationLabel)
        assertEquals("Classification score should match", entity.classificationScore, domainModel.classificationScore)
        assertTrue("Reasons should contain expected values", domainModel.classificationReasons.contains("Messaging app"))
        assertEquals("Action should match", NotificationAction.SHOW_AND_INBOX, domainModel.action)
        assertEquals("isRead should match", entity.isRead, domainModel.isRead)
    }

    @Test
    fun `toEntity converts all fields correctly`() {
        val entity = createTestNotificationEntity()

        val domainModel = entity.toDomainModel()
        val convertedBack = domainModel.toEntity()

        assertEquals("ID should match", entity.id, convertedBack.id)
        assertEquals("Package name should match", entity.packageName, convertedBack.packageName)
        assertEquals("App name should match", entity.appName, convertedBack.appName)
        assertEquals("Title should match", entity.title, convertedBack.title)
        assertEquals("Text should match", entity.text, convertedBack.text)
        assertEquals("Classification label should match", entity.classificationLabel, convertedBack.classificationLabel)
        assertEquals("Classification score should match", entity.classificationScore, convertedBack.classificationScore)
        assertEquals("Action should match", entity.action, convertedBack.action)
    }

    @Test
    fun `round trip conversion preserves data`() {
        val original = createTestNotificationEntity()

        val domainModel = original.toDomainModel()
        val roundTripped = domainModel.toEntity()

        // Verify all fields are preserved through round trip
        assertEquals("id", original.id, roundTripped.id)
        assertEquals("key", original.key, roundTripped.key)
        assertEquals("packageName", original.packageName, roundTripped.packageName)
        assertEquals("appName", original.appName, roundTripped.appName)
        assertEquals("title", original.title, roundTripped.title)
        assertEquals("text", original.text, roundTripped.text)
        assertEquals("subText", original.subText, roundTripped.subText)
        assertEquals("bigText", original.bigText, roundTripped.bigText)
        assertEquals("postTime", original.postTime, roundTripped.postTime)
        assertEquals("channelId", original.channelId, roundTripped.channelId)
        assertEquals("channelName", original.channelName, roundTripped.channelName)
        assertEquals("classificationLabel", original.classificationLabel, roundTripped.classificationLabel)
        assertEquals("classificationScore", original.classificationScore, roundTripped.classificationScore)
        assertEquals("action", original.action, roundTripped.action)
        assertEquals("isRead", original.isRead, roundTripped.isRead)
        assertEquals("isSuppressed", original.isSuppressed, roundTripped.isSuppressed)
        assertEquals("isArchived", original.isArchived, roundTripped.isArchived)
    }

    @Test
    fun `JSON serialization of classificationReasons works correctly`() {
        val reasons = listOf("Banking keyword: credited", "Protected app", "High importance")
        val encoded = json.encodeToString(reasons)

        val decoded: List<String> = json.decodeFromString(encoded)

        assertEquals("Should decode to same list size", reasons.size, decoded.size)
        assertTrue("Should contain all original reasons", decoded.containsAll(reasons))
    }

    @Test
    fun `toDomainModel handles empty classificationReasons`() {
        val entity = createTestNotificationEntity().copy(classificationReasons = "")

        val domainModel = entity.toDomainModel()

        assertNotNull("Should return empty list instead of throwing", domainModel.classificationReasons)
        assertTrue("Should be empty list", domainModel.classificationReasons.isEmpty())
    }

    @Test
    fun `toDomainModel handles malformed JSON in classificationReasons`() {
        val entity = createTestNotificationEntity().copy(classificationReasons = "{ invalid json }")

        val domainModel = entity.toDomainModel()

        assertNotNull("Should return empty list for malformed JSON", domainModel.classificationReasons)
        assertTrue("Should be empty list", domainModel.classificationReasons.isEmpty())
    }
}