package com.kkday.domain.companion

import com.kkday.library.common.repository.CompanionRepository
import org.koin.core.annotation.Factory

@Factory
class SaveShownQuestionCountsUseCase(
    private val repository: CompanionRepository
) {
    suspend operator fun invoke(counts: Map<String, Int>): Result<Unit> =
        repository.saveShownQuestionCounts(counts)
}
