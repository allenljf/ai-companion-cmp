package com.allenljf.aicompanion.domain

import com.allenljf.aicompanion.data.CompanionRepository
import com.allenljf.aicompanion.model.CompanionProfile

class GetLocalCompanionUseCase(
    private val repository: CompanionRepository
) {
    suspend operator fun invoke(): Result<CompanionProfile?> = repository.getLocalCompanion()
}
