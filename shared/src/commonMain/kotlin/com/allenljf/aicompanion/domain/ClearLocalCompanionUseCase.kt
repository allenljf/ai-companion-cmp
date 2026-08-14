package com.allenljf.aicompanion.domain

import com.allenljf.aicompanion.data.CompanionRepository

class ClearLocalCompanionUseCase(
    private val repository: CompanionRepository
) {
    suspend operator fun invoke(): Result<Unit> = repository.clearLocalCompanion()
}
