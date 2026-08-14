package com.allenljf.aicompanion.domain

import com.allenljf.aicompanion.data.CompanionRepository
import com.allenljf.aicompanion.model.CompanionProfile

class SaveLocalCompanionUseCase(
    private val repository: CompanionRepository
) {
    suspend operator fun invoke(profile: CompanionProfile): Result<Unit> =
        repository.saveLocalCompanion(profile)
}
