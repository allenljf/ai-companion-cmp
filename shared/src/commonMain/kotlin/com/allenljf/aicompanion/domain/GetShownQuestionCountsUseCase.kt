package com.allenljf.aicompanion.domain

import com.allenljf.aicompanion.data.CompanionRepository

class GetShownQuestionCountsUseCase(
    private val repository: CompanionRepository
) {
    suspend operator fun invoke(): Result<Map<String, Int>> = repository.getShownQuestionCounts()
}
