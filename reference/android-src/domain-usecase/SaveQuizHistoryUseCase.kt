package com.kkday.domain.companion

import com.kkday.library.common.repository.CompanionRepository
import com.kkday.model.companion.QuizHistoryRecord
import org.koin.core.annotation.Factory

@Factory
class SaveQuizHistoryUseCase(
    private val repository: CompanionRepository
) {
    suspend operator fun invoke(records: List<QuizHistoryRecord>): Result<Unit> =
        repository.saveQuizHistory(records)
}
