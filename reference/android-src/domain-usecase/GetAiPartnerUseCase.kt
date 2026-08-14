package com.kkday.domain.companion

import com.kkday.library.common.repository.CompanionRepository
import com.kkday.model.companion.AiPartnerResult
import org.koin.core.annotation.Factory

@Factory
class GetAiPartnerUseCase(
    private val repository: CompanionRepository
) {
    suspend operator fun invoke(): Result<AiPartnerResult> = repository.getAiPartner()
}
