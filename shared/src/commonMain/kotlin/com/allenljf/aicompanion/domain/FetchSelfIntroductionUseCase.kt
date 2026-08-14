package com.allenljf.aicompanion.domain

import com.allenljf.aicompanion.data.CompanionRepository
import com.allenljf.aicompanion.model.SelfIntroductionResult

class FetchSelfIntroductionUseCase(
    private val repository: CompanionRepository
) {
    suspend operator fun invoke(
        companionName: String,
        personality: List<String>,
        speechStyle: String,
        gender: String
    ): Result<SelfIntroductionResult> =
        repository.fetchSelfIntroduction(companionName, personality, speechStyle, gender)
}
