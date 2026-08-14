package com.kkday.domain.companion

import com.kkday.library.common.repository.CompanionRepository
import org.koin.core.annotation.Factory

@Factory
class GetShownQuestionCountsUseCase(
    private val repository: CompanionRepository
) {
    suspend operator fun invoke(): Result<Map<String, Int>> = repository.getShownQuestionCounts()
}
