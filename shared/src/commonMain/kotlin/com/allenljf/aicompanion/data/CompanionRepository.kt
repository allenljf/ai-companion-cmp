package com.allenljf.aicompanion.data

import com.allenljf.aicompanion.model.AiPartnerResult
import com.allenljf.aicompanion.model.CompanionProfile
import com.allenljf.aicompanion.model.QuizCompletionResult
import com.allenljf.aicompanion.model.QuizGalleryResult
import com.allenljf.aicompanion.model.QuizHistoryRecord
import com.allenljf.aicompanion.model.QuizResult
import com.allenljf.aicompanion.model.CityChatMessage
import com.allenljf.aicompanion.model.RecommendCityResult
import com.allenljf.aicompanion.model.SavedTripRecord
import com.allenljf.aicompanion.model.SelfIntroductionResult
import com.allenljf.aicompanion.model.TravelGuideDay
import com.allenljf.aicompanion.model.TravelGuideResult
import com.allenljf.aicompanion.model.TravelReviseResult
import com.allenljf.aicompanion.model.TravelSummaryFromOrdersResult
import com.allenljf.aicompanion.model.TravelSummaryFromProductsResult
import com.allenljf.aicompanion.model.TravelSummaryResult
import com.allenljf.aicompanion.model.TripOrderMaterial
import com.allenljf.aicompanion.model.TripProductMaterial

/**
 * 原始碼移植自 KKday `CompanionRepository`（domain-contract）：簽章與方法命名保持一致，
 * 方便對照 reference/android-src 移植 UseCase/ViewModel 時不用改呼叫端。
 *
 * 去 B2C 化調整（見 migration/API_CONTRACT.md）：
 * - `fetchShareImageV2` 已移除——海報產圖全鏈路不做（demo 範圍排除，見 CLAUDE.md 核心約束）
 * - `isMockEnabled` 已移除——原本是 KKday `CompanionMockSwitch`（Retrofit interceptor 開發用切換）的
 *   網路層概念，本專案資料來源本身就是 mock，且原專案中無 UseCase/ViewModel 實際呼叫它
 */
interface CompanionRepository {

    suspend fun getAiPartner(): Result<AiPartnerResult>

    suspend fun fetchQuiz(
        shownQuestionCounts: Map<String, Int>,
        personality: List<String>,
        speechStyle: String
    ): Result<QuizResult>

    // partnerAvatarUrl：使用者當時捏好的旅伴頭像 URL，後端存進 quiz-gallery 記錄供清單顯示；可為 null
    suspend fun completeQuiz(
        completionUuid: String,
        personality: List<String>,
        speechStyle: String,
        selectedTags: List<String>,
        shownCities: List<String>,
        companionName: String?,
        partnerAvatarUrl: String?
    ): Result<QuizCompletionResult>

    // 其他人做過的測驗結果清單（無資料庫，純讀 Redis LIST，最新 100 筆、1 天 TTL）
    suspend fun getQuizGallery(): Result<QuizGalleryResult>

    // 旅伴自我介紹（LLM 生成；後端軟失敗時 introduction 為固定文案，以 failReason 判斷）
    suspend fun fetchSelfIntroduction(
        companionName: String,
        personality: List<String>,
        speechStyle: String,
        gender: String
    ): Result<SelfIntroductionResult>

    // 本地旅伴（純前端持久化，無後端端點；demo 單一使用者，不做 memberUuid 區隔）
    suspend fun getLocalCompanion(): Result<CompanionProfile?>

    suspend fun saveLocalCompanion(profile: CompanionProfile): Result<Unit>

    suspend fun clearLocalCompanion(): Result<Unit>

    // 出現過的題目次數（key = "{dimension_id}-{index}" 即題目 id，value = 出現次數）
    // 作為 quiz API 的 shown_question_counts input，降低重複出題機率（邏輯在後端）
    suspend fun getShownQuestionCounts(): Result<Map<String, Int>>

    suspend fun saveShownQuestionCounts(counts: Map<String, Int>): Result<Unit>

    // 測驗結果歷史（純前端持久化，最新在前）
    // shown_cities 只帶目前使用者語系對應的單一城市名稱，避免重複推薦（判斷在後端 LLM）
    suspend fun getQuizHistory(): Result<List<QuizHistoryRecord>>

    suspend fun saveQuizHistory(records: List<QuizHistoryRecord>): Result<Unit>

    // Phase 2 聊天室初始化摘要（無狀態；重複呼叫補充資訊時帶 previousSummary + note）
    // entryType/sourceType 常數見 TravelSummaryEntryType / TravelSummarySourceType
    // quiz_completion／from_orders 入口 city 必填（權威回傳）；from_orders 可帶 order 訂單材料
    suspend fun fetchTravelSummary(
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
    ): Result<TravelSummaryResult>

    // Phase 2「帶訂單開場」：把近期訂單材料（最多 3 筆）丟給後端 LLM 判斷可能的目的地選項（1-3 個）
    suspend fun fetchTravelSummaryFromOrders(
        orders: List<TripOrderMaterial>,
        companionName: String? = null,
        personality: List<String> = emptyList(),
        speechStyle: String? = null
    ): Result<TravelSummaryFromOrdersResult>

    // Phase 2「願望清單開場」：把收藏商品材料（1~20 筆）丟給 LLM 聚合判斷最多 3 個城市選項
    suspend fun fetchTravelSummaryFromWish(
        products: List<TripProductMaterial>,
        companionName: String? = null,
        personality: List<String> = emptyList(),
        speechStyle: String? = null
    ): Result<TravelSummaryFromProductsResult>

    // Phase 2「瀏覽紀錄開場」：把瀏覽/購買商品材料（1~20 筆）丟給 LLM 聚合判斷最多 3 個城市選項
    suspend fun fetchTravelSummaryFromHistory(
        products: List<TripProductMaterial>,
        companionName: String? = null,
        personality: List<String> = emptyList(),
        speechStyle: String? = null
    ): Result<TravelSummaryFromProductsResult>

    // Phase 2 城市推薦多輪對話（無狀態：每輪把完整對話歷史全量帶入，上限 5 輪由伺服器控管）
    // shownCities：本次對話已推薦過的城市，「換一個城市」時帶入讓 LLM 排除（≤30 個）
    suspend fun fetchRecommendCity(
        messages: List<CityChatMessage>,
        shownCities: List<String> = emptyList(),
        companionName: String? = null,
        personality: List<String> = emptyList(),
        speechStyle: String? = null
    ): Result<RecommendCityResult>

    // Phase 2 產生結構化行程（無狀態、可原樣重打重試）
    // orders 選填 ≤3 筆已預訂訂單，帶了後端必排入行程（item 回填 oid、該天 booked_anchor 有值）；
    // products 選填 ≤10 筆願望清單/瀏覽紀錄商品，帶了必排入行程（item 回填 prod_id，不影響 booked_anchor）
    suspend fun fetchTravelGuide(
        summary: String,
        city: String,
        preferences: Map<String, String>,
        orders: List<TripOrderMaterial> = emptyList(),
        products: List<TripProductMaterial> = emptyList(),
        companionName: String? = null,
        personality: List<String> = emptyList(),
        speechStyle: String? = null
    ): Result<TravelGuideResult>

    // Phase 2 修改既有行程（無狀態：帶「當前最新行程」＋完整聊天室對話紀錄，回應只含變動的天，App 以天為單位 merge）
    // 沒有獨立的 request 欄位——這次的修改需求＝messages 最後一則 role=user
    suspend fun fetchTravelRevise(
        itineraryDays: List<TravelGuideDay>,
        city: String? = null,
        targetDay: Int? = null,
        messages: List<CityChatMessage>,
        preferences: Map<String, String> = emptyMap(),
        companionName: String? = null,
        personality: List<String> = emptyList(),
        speechStyle: String? = null
    ): Result<TravelReviseResult>

    // 「我的旅程」（純前端持久化，後端不儲存行程；最新在前）
    suspend fun getSavedTrips(): Result<List<SavedTripRecord>>

    suspend fun saveSavedTrips(records: List<SavedTripRecord>): Result<Unit>

    // 目前使用者選擇的顯示語系是否為中文（決定 destinationCn/destinationEn 等雙語欄位要取哪一個）
    fun isChineseLanguage(): Boolean
}
