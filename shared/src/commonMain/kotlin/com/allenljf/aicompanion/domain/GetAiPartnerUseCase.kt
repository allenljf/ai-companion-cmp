package com.allenljf.aicompanion.domain

import com.allenljf.aicompanion.data.CompanionRepository
import com.allenljf.aicompanion.model.AiPartnerResult

class GetAiPartnerUseCase(
    private val repository: CompanionRepository
) {
    suspend operator fun invoke(): Result<AiPartnerResult> = repository.getAiPartner()
}
