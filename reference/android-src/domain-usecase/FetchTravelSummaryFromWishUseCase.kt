package com.kkday.domain.companion

import com.kkday.library.common.repository.CompanionRepository
import com.kkday.model.companion.TravelSummaryFromProductsResult
import com.kkday.model.companion.TripProductMaterial
import org.koin.core.annotation.Factory

/**
 * Phase 2「願望清單開場」（POST /v3/companion/travel-summary-from-wish）。
 * 把收藏商品材料（1~20 筆）丟給 LLM 聚合判斷最多 3 個城市選項。
 */
@Factory
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
