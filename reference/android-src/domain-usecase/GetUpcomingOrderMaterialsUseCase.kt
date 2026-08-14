package com.kkday.domain.companion

import com.kkday.library.common.repository.CompanionOrderRepository
import com.kkday.model.companion.TripOrderMaterial
import org.koin.core.annotation.Factory

/** AI 旅伴「帶訂單開場」用：取即將出發訂單中最近的最多 3 筆材料。 */
@Factory
class GetUpcomingOrderMaterialsUseCase(private val repository: CompanionOrderRepository) {
    suspend operator fun invoke(): Result<List<TripOrderMaterial>> = repository.getUpcomingOrderMaterials()
}
