package com.allenljf.aicompanion.domain

import com.allenljf.aicompanion.data.CompanionOrderRepository
import com.allenljf.aicompanion.model.TripOrderMaterial

/** AI 旅伴「帶訂單開場」用：取即將出發訂單中最近的最多 3 筆材料。 */
class GetUpcomingOrderMaterialsUseCase(private val repository: CompanionOrderRepository) {
    suspend operator fun invoke(): Result<List<TripOrderMaterial>> = repository.getUpcomingOrderMaterials()
}
