package com.allenljf.aicompanion.domain

import com.allenljf.aicompanion.data.CompanionOrderRepository
import com.allenljf.aicompanion.model.TripProductMaterial

/** AI 旅伴「願望清單開場」用：取收藏商品材料（GET wish_list，最多 20 筆）。 */
class GetWishProductMaterialsUseCase(private val repository: CompanionOrderRepository) {
    suspend operator fun invoke(): Result<List<TripProductMaterial>> = repository.getWishProductMaterials()
}
