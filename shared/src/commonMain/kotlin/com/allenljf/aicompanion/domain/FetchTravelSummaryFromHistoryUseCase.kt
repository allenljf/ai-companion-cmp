package com.allenljf.aicompanion.domain

import com.allenljf.aicompanion.data.CompanionRepository
import com.allenljf.aicompanion.model.TravelSummaryFromProductsResult
import com.allenljf.aicompanion.model.TripProductMaterial

/**
 * Phase 2「瀏覽紀錄開場」（POST /v3/companion/travel-summary-from-history）。
 * 把瀏覽/購買商品材料（1~20 筆）丟給 LLM 聚合判斷最多 3 個城市選項。
 */
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
