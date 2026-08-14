package com.kkday.domain.companion

import com.kkday.library.common.repository.CompanionRepository
import com.kkday.model.companion.QuizHistoryRecord
import org.koin.core.annotation.Factory

@Factory
class GetQuizHistoryUseCase(
    private val repository: CompanionRepository
) {
    suspend operator fun invoke(): Result<List<QuizHistoryRecord>> = repository.getQuizHistory()
}
