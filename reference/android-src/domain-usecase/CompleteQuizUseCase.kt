package com.kkday.domain.companion

import com.kkday.library.common.repository.CompanionRepository
import com.kkday.model.companion.QuizCompletionResult
import org.koin.core.annotation.Factory

@Factory
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
