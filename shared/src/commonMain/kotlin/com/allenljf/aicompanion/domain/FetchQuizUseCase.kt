package com.allenljf.aicompanion.domain

import com.allenljf.aicompanion.data.CompanionRepository
import com.allenljf.aicompanion.model.QuizResult

class FetchQuizUseCase(
    private val repository: CompanionRepository
) {
    suspend operator fun invoke(
        shownQuestionCounts: Map<String, Int>,
        personality: List<String>,
        speechStyle: String
    ): Result<QuizResult> = repository.fetchQuiz(shownQuestionCounts, personality, speechStyle)
}
