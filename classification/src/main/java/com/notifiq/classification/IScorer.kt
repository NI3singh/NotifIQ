package com.notifiq.classification

import com.notifiq.core.model.ClassificationLabel

interface IScorer {
    fun score(context: ScoringContext): ScoringResult
}