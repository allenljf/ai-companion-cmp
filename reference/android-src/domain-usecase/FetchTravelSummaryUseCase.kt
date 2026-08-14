package com.kkday.domain.companion

import com.kkday.library.common.repository.CompanionRepository
import com.kkday.model.companion.TravelSummaryResult
import com.kkday.model.companion.TripOrderMaterial
import org.koin.core.annotation.Factory

/**
 * Phase 2 聊天室初始化摘要（POST /v3/companion/travel-summary）。
 * 無狀態：補充資訊時帶 previousSummary + note 重打，取得整合後的新版摘要。
 * 2026-08 改版：quiz_completion／from_orders 入口 city 必填（權威回傳）；from_orders 可帶 order 訂單材料。
 */
@Factory
class FetchTravelSummaryUseCase(private val repository: CompanionRepository) {
    suspend operator fun invoke(
        entryType: String,
        city: String? = null,
        order: TripOrderMaterial? = null,
        cityImageUrl: String? = null,
        introText: String? = null,
        sourceType: String? = null,
        content: String? = null,
        imageUrls: List<String>? = null,
        note: String? = null,
        previousSummary: String? = null,
        companionName: String? = null,
        personality: List<String> = emptyList(),
        speechStyle: String? = null
    ): Result<TravelSummaryResult> = repository.fetchTravelSummary(
        entryType = entryType,
        city = city,
        order = order,
        cityImageUrl = cityImageUrl,
        introText = introText,
        sourceType = sourceType,
        content = content,
        imageUrls = imageUrls,
        note = note,
        previousSummary = previousSummary,
        companionName = companionName,
        personality = personality,
        speechStyle = speechStyle
    )
}
