package com.allenljf.aicompanion.domain

import com.allenljf.aicompanion.data.CompanionOrderRepository
import com.allenljf.aicompanion.model.TripProductMaterial

/** AI 旅伴「瀏覽紀錄開場」用：取瀏覽/購買商品材料（GET history，最多 20 筆）。 */
class GetHistoryProductMaterialsUseCase(private val repository: CompanionOrderRepository) {
    suspend operator fun invoke(): Result<List<TripProductMaterial>> = repository.getHistoryProductMaterials()
}
