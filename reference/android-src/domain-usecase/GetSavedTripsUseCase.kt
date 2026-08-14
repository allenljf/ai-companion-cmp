package com.kkday.domain.companion

import com.kkday.library.common.repository.CompanionRepository
import com.kkday.model.companion.SavedTripRecord
import org.koin.core.annotation.Factory

/** 「我的旅程」本地清單（後端不儲存行程，純前端持久化，最新在前）。 */
@Factory
class GetSavedTripsUseCase(private val repository: CompanionRepository) {
    suspend operator fun invoke(): Result<List<SavedTripRecord>> = repository.getSavedTrips()
}
