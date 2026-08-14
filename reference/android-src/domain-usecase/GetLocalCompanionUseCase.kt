package com.kkday.domain.companion

import com.kkday.library.common.repository.CompanionRepository
import com.kkday.model.companion.CompanionProfile
import org.koin.core.annotation.Factory

@Factory
class GetLocalCompanionUseCase(
    private val repository: CompanionRepository
) {
    suspend operator fun invoke(): Result<CompanionProfile?> = repository.getLocalCompanion()
}
