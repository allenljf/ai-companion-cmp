package com.kkday.domain.companion

import com.kkday.library.common.repository.CompanionRepository
import com.kkday.model.companion.CityChatMessage
import com.kkday.model.companion.RecommendCityResult
import org.koin.core.annotation.Factory

/**
 * Phase 2 城市推薦多輪對話（POST /v3/companion/recommend-city）。
 * 無狀態：每輪把完整對話歷史全量帶入（最後一則為本次使用者輸入）；
 * 收斂只看 isFinal（LLM 可能提前收斂），上限 5 輪由伺服器控管。
 * shownCities：本次對話已推薦過的城市，「換一個城市」時帶入讓 LLM 排除。
 */
@Factory
class FetchRecommendCityUseCase(private val repository: CompanionRepository) {
    suspend operator fun invoke(
        messages: List<CityChatMessage>,
        shownCities: List<String> = emptyList(),
        companionName: String? = null,
        personality: List<String> = emptyList(),
        speechStyle: String? = null
    ): Result<RecommendCityResult> = repository.fetchRecommendCity(
        messages = messages,
        shownCities = shownCities,
        companionName = companionName,
        personality = personality,
        speechStyle = speechStyle
    )
}
