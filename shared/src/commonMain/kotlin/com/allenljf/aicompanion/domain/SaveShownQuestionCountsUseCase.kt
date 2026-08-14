package com.allenljf.aicompanion.domain

import com.allenljf.aicompanion.data.CompanionRepository

class SaveShownQuestionCountsUseCase(
    private val repository: CompanionRepository
) {
    suspend operator fun invoke(counts: Map<String, Int>): Result<Unit> =
        repository.saveShownQuestionCounts(counts)
}
