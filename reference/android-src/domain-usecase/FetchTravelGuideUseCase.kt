package com.kkday.domain.companion

import com.kkday.library.common.repository.CompanionRepository
import com.kkday.model.companion.TravelGuideResult
import com.kkday.model.companion.TripOrderMaterial
import com.kkday.model.companion.TripProductMaterial
import org.koin.core.annotation.Factory

/**
 * Phase 2 產生結構化行程（POST /v3/companion/travel-guide）。
 * 無狀態、無副作用：軟失敗（failReason 有值）時可原樣帶同一份參數重打。
 * 2026-08 改版：orders 選填 ≤3 筆已預訂訂單，帶了後端必排入行程（item 回填 oid）；
 * products 選填 ≤10 筆願望清單/瀏覽紀錄商品，帶了必排入行程（item 回填 prod_id）。
 */
@Factory
class FetchTravelGuideUseCase(private val repository: CompanionRepository) {
    suspend operator fun invoke(
        summary: String,
        city: String,
        preferences: Map<String, String>,
        orders: List<TripOrderMaterial> = emptyList(),
        products: List<TripProductMaterial> = emptyList(),
        companionName: String? = null,
        personality: List<String> = emptyList(),
        speechStyle: String? = null
    ): Result<TravelGuideResult> = repository.fetchTravelGuide(
        summary = summary,
        city = city,
        preferences = preferences,
        orders = orders,
        products = products,
        companionName = companionName,
        personality = personality,
        speechStyle = speechStyle
    )
}
