package com.kkday.domain.companion

import com.kkday.library.common.repository.CompanionRepository
import com.kkday.model.companion.QuizResult
import org.koin.core.annotation.Factory

@Factory
class FetchQuizUseCase(
    private val repository: CompanionRepository
) {
    suspend operator fun invoke(
        shownQuestionCounts: Map<String, Int>,
        personality: List<String>,
        speechStyle: String
    ): Result<QuizResult> = repository.fetchQuiz(shownQuestionCounts, personality, speechStyle)
}
