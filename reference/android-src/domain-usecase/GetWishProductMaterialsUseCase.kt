package com.kkday.domain.companion

import com.kkday.library.common.repository.CompanionOrderRepository
import com.kkday.model.companion.TripProductMaterial
import org.koin.core.annotation.Factory

/** AI 旅伴「願望清單開場」用：取收藏商品材料（GET wish_list，最多 20 筆）。 */
@Factory
class GetWishProductMaterialsUseCase(private val repository: CompanionOrderRepository) {
    suspend operator fun invoke(): Result<List<TripProductMaterial>> = repository.getWishProductMaterials()
}
