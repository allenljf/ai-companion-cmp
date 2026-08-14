package com.kkday.domain.companion

import com.kkday.library.common.repository.CompanionRepository
import com.kkday.model.companion.TravelSummaryFromProductsResult
import com.kkday.model.companion.TripProductMaterial
import org.koin.core.annotation.Factory

/**
 * Phase 2「瀏覽紀錄開場」（POST /v3/companion/travel-summary-from-history）。
 * 把瀏覽/購買商品材料（1~20 筆）丟給 LLM 聚合判斷最多 3 個城市選項。
 */
@Factory
class FetchTravelSummaryFromHistoryUseCase(private val repository: CompanionRepository) {
    suspend operator fun invoke(
        products: List<TripProductMaterial>,
        companionName: String? = null,
        personality: List<String> = emptyList(),
        speechStyle: String? = null
    ): Result<TravelSummaryFromProductsResult> = repository.fetchTravelSummaryFromHistory(
        products = products,
        companionName = companionName,
        personality = personality,
        speechStyle = speechStyle
    )
}
