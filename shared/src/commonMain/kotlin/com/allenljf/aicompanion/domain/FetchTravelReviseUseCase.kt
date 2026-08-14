package com.allenljf.aicompanion.domain

import com.allenljf.aicompanion.data.CompanionRepository
import com.allenljf.aicompanion.model.CityChatMessage
import com.allenljf.aicompanion.model.TravelGuideDay
import com.allenljf.aicompanion.model.TravelReviseResult

/**
 * Phase 2 修改既有行程（POST /v3/companion/travel-revise）。
 * 無狀態：每次帶「當前最新完整行程」＋完整聊天室對話紀錄（連續修改帶上一輪 merge 後的結果），
 * 回應只含有變動的天（整天覆蓋），App 以天為單位 merge 後本地立即生效。
 * 2026-08 改版（二）：不再有獨立的 request 參數，這次的修改需求＝messages 最後一則 role=user。
 */
class FetchTravelReviseUseCase(private val repository: CompanionRepository) {
    suspend operator fun invoke(
        itineraryDays: List<TravelGuideDay>,
        city: String? = null,
        targetDay: Int? = null,
        messages: List<CityChatMessage>,
        preferences: Map<String, String> = emptyMap(),
        companionName: String? = null,
        personality: List<String> = emptyList(),
        speechStyle: String? = null
    ): Result<TravelReviseResult> = repository.fetchTravelRevise(
        itineraryDays = itineraryDays,
        city = city,
        targetDay = targetDay,
        messages = messages,
        preferences = preferences,
        companionName = companionName,
        personality = personality,
        speechStyle = speechStyle
    )
}
