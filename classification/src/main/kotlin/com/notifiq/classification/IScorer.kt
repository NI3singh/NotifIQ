package com.notifiq.classification

import com.notifiq.core.model.ClassificationLabel

interface IScorer {
    suspend fun score(context: ScoringContext): ScoringResult
}