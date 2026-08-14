package com.kkday.domain.companion

import com.kkday.library.common.repository.CompanionRepository
import com.kkday.model.companion.SelfIntroductionResult
import org.koin.core.annotation.Factory

@Factory
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
