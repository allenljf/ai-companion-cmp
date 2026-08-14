package com.allenljf.aicompanion.domain

import com.allenljf.aicompanion.data.CompanionRepository
import com.allenljf.aicompanion.model.QuizHistoryRecord

class GetQuizHistoryUseCase(
    private val repository: CompanionRepository
) {
    suspend operator fun invoke(): Result<List<QuizHistoryRecord>> = repository.getQuizHistory()
}
