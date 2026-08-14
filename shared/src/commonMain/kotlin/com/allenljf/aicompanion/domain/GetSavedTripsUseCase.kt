package com.allenljf.aicompanion.domain

import com.allenljf.aicompanion.data.CompanionRepository
import com.allenljf.aicompanion.model.SavedTripRecord

/** 「我的旅程」本地清單（後端不儲存行程，純前端持久化，最新在前）。 */
class GetSavedTripsUseCase(private val repository: CompanionRepository) {
    suspend operator fun invoke(): Result<List<SavedTripRecord>> = repository.getSavedTrips()
}
