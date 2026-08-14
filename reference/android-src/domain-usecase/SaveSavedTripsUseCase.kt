package com.kkday.domain.companion

import com.kkday.library.common.repository.CompanionRepository
import com.kkday.model.companion.SavedTripRecord
import org.koin.core.annotation.Factory

/** 覆寫「我的旅程」本地清單（新增/刪除由呼叫端組好完整清單後存入）。 */
@Factory
class SaveSavedTripsUseCase(private val repository: CompanionRepository) {
    suspend operator fun invoke(records: List<SavedTripRecord>): Result<Unit> =
        repository.saveSavedTrips(records)
}
