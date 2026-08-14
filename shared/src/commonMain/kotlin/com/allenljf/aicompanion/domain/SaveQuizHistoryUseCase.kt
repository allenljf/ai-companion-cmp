package com.allenljf.aicompanion.domain

import com.allenljf.aicompanion.data.CompanionRepository
import com.allenljf.aicompanion.model.QuizHistoryRecord

class SaveQuizHistoryUseCase(
    private val repository: CompanionRepository
) {
    suspend operator fun invoke(records: List<QuizHistoryRecord>): Result<Unit> =
        repository.saveQuizHistory(records)
}
