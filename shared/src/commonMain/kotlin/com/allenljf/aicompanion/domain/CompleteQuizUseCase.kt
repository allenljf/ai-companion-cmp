package com.allenljf.aicompanion.domain

import com.allenljf.aicompanion.data.CompanionRepository
import com.allenljf.aicompanion.model.QuizCompletionResult

class CompleteQuizUseCase(
    private val repository: CompanionRepository
) {
    suspend operator fun invoke(
        completionUuid: String,
        personality: List<String>,
        speechStyle: String,
        selectedTags: List<String>,
        shownCities: List<String> = emptyList(),
        companionName: String? = null,
        partnerAvatarUrl: String? = null
    ): Result<QuizCompletionResult> = repository.completeQuiz(
        completionUuid = completionUuid,
        personality = personality,
        speechStyle = speechStyle,
        selectedTags = selectedTags,
        shownCities = shownCities,
        companionName = companionName,
        partnerAvatarUrl = partnerAvatarUrl
    )
}
