package com.allenljf.aicompanion.domain

import com.allenljf.aicompanion.data.CompanionRepository
import com.allenljf.aicompanion.model.TravelSummaryFromOrdersResult
import com.allenljf.aicompanion.model.TripOrderMaterial

/**
 * Phase 2「帶訂單開場」（POST /v3/companion/travel-summary-from-orders，後端尚未實作）。
 * 把近期訂單材料（最多 3 筆）丟給 LLM 判斷可能的目的地選項。
 */
class FetchTravelSummaryFromOrdersUseCase(private val repository: CompanionRepository) {
    suspend operator fun invoke(
        orders: List<TripOrderMaterial>,
        companionName: String? = null,
        personality: List<String> = emptyList(),
        speechStyle: String? = null
    ): Result<TravelSummaryFromOrdersResult> = repository.fetchTravelSummaryFromOrders(
        orders = orders,
        companionName = companionName,
        personality = personality,
        speechStyle = speechStyle
    )
}
