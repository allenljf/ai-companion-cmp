package com.allenljf.aicompanion.domain

import com.allenljf.aicompanion.data.CompanionRepository
import com.allenljf.aicompanion.model.TravelSummaryFromProductsResult
import com.allenljf.aicompanion.model.TripProductMaterial

/**
 * Phase 2「願望清單開場」（POST /v3/companion/travel-summary-from-wish）。
 * 把收藏商品材料（1~20 筆）丟給 LLM 聚合判斷最多 3 個城市選項。
 */
class FetchTravelSummaryFromWishUseCase(private val repository: CompanionRepository) {
    suspend operator fun invoke(
        products: List<TripProductMaterial>,
        companionName: String? = null,
        personality: List<String> = emptyList(),
        speechStyle: String? = null
    ): Result<TravelSummaryFromProductsResult> = repository.fetchTravelSummaryFromWish(
        products = products,
        companionName = companionName,
        personality = personality,
        speechStyle = speechStyle
    )
}
