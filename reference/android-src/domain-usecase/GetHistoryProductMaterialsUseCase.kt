package com.kkday.domain.companion

import com.kkday.library.common.repository.CompanionOrderRepository
import com.kkday.model.companion.TripProductMaterial
import org.koin.core.annotation.Factory

/** AI 旅伴「瀏覽紀錄開場」用：取瀏覽/購買商品材料（GET history，最多 20 筆）。 */
@Factory
class GetHistoryProductMaterialsUseCase(private val repository: CompanionOrderRepository) {
    suspend operator fun invoke(): Result<List<TripProductMaterial>> = repository.getHistoryProductMaterials()
}
