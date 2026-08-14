package com.allenljf.aicompanion.domain

import com.allenljf.aicompanion.data.CompanionRepository
import com.allenljf.aicompanion.model.SavedTripRecord

/** 覆寫「我的旅程」本地清單（新增/刪除由呼叫端組好完整清單後存入）。 */
class SaveSavedTripsUseCase(private val repository: CompanionRepository) {
    suspend operator fun invoke(records: List<SavedTripRecord>): Result<Unit> =
        repository.saveSavedTrips(records)
}
