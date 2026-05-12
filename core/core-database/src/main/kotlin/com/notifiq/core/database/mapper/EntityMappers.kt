package com.notifiq.core.database.mapper

import com.notifiq.core.database.entity.AnalyticsDailyEntity
import com.notifiq.core.database.entity.AppPreferenceEntity
import com.notifiq.core.database.entity.FeedbackEntity
import com.notifiq.core.database.entity.NotificationEntity
import com.notifiq.core.database.entity.RuleEntity
import com.notifiq.core.database.entity.SenderEntity
import com.notifiq.core.database.entity.SummaryEntity
import com.notifiq.core.model.AppRule
import com.notifiq.core.model.ClassificationLabel
import com.notifiq.core.model.ClassificationResult
import com.notifiq.core.model.ClassificationRule
import com.notifiq.core.model.DailyAnalytics
import com.notifiq.core.model.FeedbackRecord
import com.notifiq.core.model.FeedbackType
import com.notifiq.core.model.NotificationAction
import com.notifiq.core.model.NotificationRecord
import com.notifiq.core.model.RuleAction
import com.notifiq.core.model.RuleType
import com.notifiq.core.model.SenderRule
import com.notifiq.core.model.SummaryReport
import com.notifiq.core.model.SummaryType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

fun NotificationEntity.toDomainModel(): NotificationRecord {
    return NotificationRecord(
        id = id,
        key = key,
        packageName = packageName,
        appName = appName,
        title = title,
        text = text,
        subText = subText,
        bigText = bigText,
        postTime = postTime,
        channelId = channelId,
        channelName = channelName,
        groupKey = groupKey,
        category = category,
        priority = priority,
        importance = importance,
        classificationLabel = ClassificationLabel.valueOf(classificationLabel),
        classificationScore = classificationScore,
        classificationReasons = try {
            json.decodeFromString(classificationReasons)
        } catch (e: Exception) {
            emptyList()
        },
        action = NotificationAction.valueOf(action),
        isRead = isRead,
        isSuppressed = isSuppressed,
        isArchived = isArchived,
        rawPayloadHash = rawPayloadHash,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun NotificationRecord.toEntity(): NotificationEntity {
    return NotificationEntity(
        id = id,
        key = key,
        packageName = packageName,
        appName = appName,
        title = title,
        text = text,
        subText = subText,
        bigText = bigText,
        postTime = postTime,
        channelId = channelId,
        channelName = channelName,
        groupKey = groupKey,
        category = category,
        priority = priority,
        importance = importance,
        classificationLabel = classificationLabel.name,
        classificationScore = classificationScore,
        classificationReasons = json.encodeToString(classificationReasons),
        action = action.name,
        isRead = isRead,
        isSuppressed = isSuppressed,
        isArchived = isArchived,
        rawPayloadHash = rawPayloadHash,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun AppPreferenceEntity.toDomainModel(): AppRule {
    return AppRule(
        packageName = packageName,
        appName = appName,
        isAllowlisted = isAllowlisted,
        isBlocklisted = isBlocklisted,
        trustScore = trustScore,
        totalNotifications = totalNotifications,
        importantCount = importantCount,
        spamCount = spamCount
    )
}

fun SenderEntity.toDomainModel(): SenderRule {
    return SenderRule(
        id = id,
        displayName = displayName,
        identifier = identifier,
        isAllowlisted = isAllowlisted,
        isBlocklisted = isBlocklisted,
        trustScore = trustScore
    )
}

fun RuleEntity.toDomainModel(): ClassificationRule {
    return ClassificationRule(
        id = id,
        type = RuleType.valueOf(type),
        condition = conditionValue,
        action = RuleAction.valueOf(action),
        weight = weight,
        priority = priority,
        isUserCreated = isUserCreated,
        isActive = isActive,
        hitCount = hitCount,
        createdAt = createdAt
    )
}

fun FeedbackEntity.toDomainModel(): FeedbackRecord {
    return FeedbackRecord(
        id = id,
        notificationId = notificationId,
        packageName = packageName,
        feedbackType = FeedbackType.valueOf(feedbackType),
        createdAt = createdAt
    )
}

fun AnalyticsDailyEntity.toDomainModel(): DailyAnalytics {
    return DailyAnalytics(
        date = date,
        totalNotifications = totalNotifications,
        importantCount = importantCount,
        usefulCount = usefulCount,
        normalCount = normalCount,
        lowValueCount = lowValueCount,
        spamCount = spamCount,
        suppressedCount = suppressedCount,
        averageConfidence = averageConfidence
    )
}

fun SummaryEntity.toDomainModel(): SummaryReport {
    return SummaryReport(
        id = id,
        type = SummaryType.valueOf(type),
        title = title,
        totalCount = totalCount,
        importantCount = importantCount,
        usefulCount = usefulCount,
        spamCount = spamCount,
        suppressedCount = suppressedCount,
        noiseReductionPercent = noiseReductionPercent,
        createdAt = createdAt
    )
}