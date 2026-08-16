package com.allenljf.aicompanion.data.mock

import com.allenljf.aicompanion.data.CompanionRepository
import com.allenljf.aicompanion.data.LocalCompanionStore
import com.allenljf.aicompanion.model.AiPartnerResult
import com.allenljf.aicompanion.model.CityChatMessage
import com.allenljf.aicompanion.model.CompanionProfile
import com.allenljf.aicompanion.model.QuizCompletionResult
import com.allenljf.aicompanion.model.QuizGalleryResult
import com.allenljf.aicompanion.model.QuizHistoryRecord
import com.allenljf.aicompanion.model.QuizResult
import com.allenljf.aicompanion.model.RecommendCityResult
import com.allenljf.aicompanion.model.SavedTripRecord
import com.allenljf.aicompanion.model.SelfIntroductionResult
import com.allenljf.aicompanion.model.ShareImageV2Result
import com.allenljf.aicompanion.model.TravelGuideDay
import com.allenljf.aicompanion.model.TravelGuideDayItem
import com.allenljf.aicompanion.model.TravelGuideResult
import com.allenljf.aicompanion.model.TravelReviseResult
import com.allenljf.aicompanion.model.TravelSummaryEntryType
import com.allenljf.aicompanion.model.TravelSummaryFromOrdersResult
import com.allenljf.aicompanion.model.TravelSummaryFromProductsResult
import com.allenljf.aicompanion.model.TravelSummaryResult
import com.allenljf.aicompanion.model.TripOrderMaterial
import com.allenljf.aicompanion.model.TripProductMaterial

/**
 * [CompanionRepository] 的 mock 實作：不打真的網路，直接回 [MockData] 準備好的寫實假資料，
 * 形狀依 migration/API_CONTRACT.md。每支方法都先 `MockData.networkDelay()`（300~800ms）模擬網路。
 *
 * 本地持久化方法（getLocalCompanion/getQuizHistory/getSavedTrips 等）委派給注入的 [LocalCompanionStore]
 * （multiplatform-settings 持久化），跨 App session 保留。
 */
class MockCompanionRepository(private val localStore: LocalCompanionStore) : CompanionRepository {

    // travel-revise 在同一個聊天室內會被連續呼叫多次；用呼叫次數讓找不到關鍵字時的 fallback 情境輪替，
    // 避免每次都回一模一樣的內容（見 MockData.travelRevise 的 callCount 參數）。
    private var reviseCallCount = 0

    override suspend fun getAiPartner(): Result<AiPartnerResult> {
        MockData.networkDelay()
        return Result.success(MockData.aiPartner)
    }

    override suspend fun fetchQuiz(
        shownQuestionCounts: Map<String, Int>,
        personality: List<String>,
        speechStyle: String
    ): Result<QuizResult> {
        MockData.networkDelay()
        val result = if (CompanionMockConfig.forceFailReason) MockData.quizResultSoftFailure() else MockData.quizResult()
        return Result.success(result)
    }

    override suspend fun completeQuiz(
        completionUuid: String,
        personality: List<String>,
        speechStyle: String,
        selectedTags: List<String>,
        shownCities: List<String>,
        companionName: String?,
        partnerAvatarUrl: String?
    ): Result<QuizCompletionResult> {
        MockData.networkDelay()
        val result = if (CompanionMockConfig.forceFailReason) {
            MockData.quizCompletionSoftFailure()
        } else {
            MockData.quizCompletion(selectedTags)
        }
        return Result.success(result)
    }

    // 固定假 hero URL + 略長 delay（示意真後端的慢），demo 不需要每次都不一樣
    override suspend fun fetchShareImageV2(
        completionUuid: String,
        partnerImageUrl: String?
    ): Result<ShareImageV2Result> {
        MockData.networkDelay()
        if (CompanionMockConfig.forceFailReason) {
            return Result.success(ShareImageV2Result(failReason = "llm_error"))
        }
        return Result.success(
            ShareImageV2Result(
                heroUrl = MockData.SHARE_IMAGE_HERO_URL,
                stampUrl = MockData.SHARE_IMAGE_STAMP_URL,
                tagIconUrls = MockData.SHARE_IMAGE_TAG_ICON_URLS,
                content = MockData.shareImageV2Content,
            )
        )
    }

    override suspend fun getQuizGallery(): Result<QuizGalleryResult> {
        MockData.networkDelay()
        return Result.success(QuizGalleryResult(count = MockData.quizGalleryItems.size, items = MockData.quizGalleryItems))
    }

    override suspend fun fetchSelfIntroduction(
        companionName: String,
        personality: List<String>,
        speechStyle: String,
        gender: String
    ): Result<SelfIntroductionResult> {
        MockData.networkDelay()
        val result = if (CompanionMockConfig.forceFailReason) {
            MockData.selfIntroductionSoftFailure(companionName)
        } else {
            MockData.selfIntroduction(
                companionName = companionName,
                speechStyleLabel = speechStyle.takeIf { it.isNotBlank() }?.let { MockData.speechStyleLabel(it) } ?: "自在",
                personalityLabel = personality.firstOrNull()?.takeIf { it.isNotBlank() }
                    ?.let { MockData.personalityLabel(it) } ?: "隨和",
            )
        }
        return Result.success(result)
    }

    override suspend fun getLocalCompanion(): Result<CompanionProfile?> {
        MockData.networkDelay()
        return Result.success(localStore.getLocalCompanion())
    }

    override suspend fun saveLocalCompanion(profile: CompanionProfile): Result<Unit> {
        MockData.networkDelay()
        localStore.saveLocalCompanion(profile)
        return Result.success(Unit)
    }

    override suspend fun clearLocalCompanion(): Result<Unit> {
        MockData.networkDelay()
        localStore.clearLocalCompanion()
        return Result.success(Unit)
    }

    override suspend fun getShownQuestionCounts(): Result<Map<String, Int>> {
        MockData.networkDelay()
        return Result.success(localStore.getShownQuestionCounts())
    }

    override suspend fun saveShownQuestionCounts(counts: Map<String, Int>): Result<Unit> {
        MockData.networkDelay()
        localStore.saveShownQuestionCounts(counts)
        return Result.success(Unit)
    }

    override suspend fun getQuizHistory(): Result<List<QuizHistoryRecord>> {
        MockData.networkDelay()
        return Result.success(localStore.getQuizHistory())
    }

    override suspend fun saveQuizHistory(records: List<QuizHistoryRecord>): Result<Unit> {
        MockData.networkDelay()
        localStore.saveQuizHistory(records)
        return Result.success(Unit)
    }

    override suspend fun fetchTravelSummary(
        entryType: String,
        city: String?,
        order: TripOrderMaterial?,
        cityImageUrl: String?,
        introText: String?,
        sourceType: String?,
        content: String?,
        imageUrls: List<String>?,
        note: String?,
        previousSummary: String?,
        companionName: String?,
        personality: List<String>,
        speechStyle: String?
    ): Result<TravelSummaryResult> {
        MockData.networkDelay()
        if (CompanionMockConfig.forceFailReason) return Result.success(MockData.travelSummarySoftFailure(city))
        val result = when {
            // 「資訊不夠想補充」重複呼叫：整合成新的一份取代前版
            !previousSummary.isNullOrBlank() -> MockData.travelSummaryMerge(previousSummary, note.orEmpty(), city, companionName)
            entryType == TravelSummaryEntryType.FROM_ZERO -> MockData.travelSummaryFromZero(companionName)
            entryType == TravelSummaryEntryType.IMPORTED_ITINERARY -> MockData.travelSummaryFromImported(content, companionName)
            // quiz_completion / from_orders：city 為權威回傳值，原樣返回
            else -> MockData.travelSummaryEcho(city.orEmpty(), companionName, order)
        }
        return Result.success(result)
    }

    override suspend fun fetchTravelSummaryFromOrders(
        orders: List<TripOrderMaterial>,
        companionName: String?,
        personality: List<String>,
        speechStyle: String?
    ): Result<TravelSummaryFromOrdersResult> {
        MockData.networkDelay()
        if (CompanionMockConfig.forceFailReason) {
            return Result.success(
                TravelSummaryFromOrdersResult(
                    greeting = "剛剛分析你的訂單時卡了一下，要不要直接告訴我你想去哪？",
                    options = emptyList(),
                    failReason = "llm_error",
                )
            )
        }
        return Result.success(MockData.travelSummaryFromOrders(orders, companionName))
    }

    override suspend fun fetchTravelSummaryFromWish(
        products: List<TripProductMaterial>,
        companionName: String?,
        personality: List<String>,
        speechStyle: String?
    ): Result<TravelSummaryFromProductsResult> {
        MockData.networkDelay()
        if (CompanionMockConfig.forceFailReason) {
            return Result.success(
                TravelSummaryFromProductsResult(
                    greeting = "看你收藏清單時卡了一下，要不要直接告訴我你想去哪？",
                    cities = emptyList(),
                    failReason = "llm_error",
                )
            )
        }
        return Result.success(MockData.travelSummaryFromProducts(products))
    }

    override suspend fun fetchTravelSummaryFromHistory(
        products: List<TripProductMaterial>,
        companionName: String?,
        personality: List<String>,
        speechStyle: String?
    ): Result<TravelSummaryFromProductsResult> {
        MockData.networkDelay()
        if (CompanionMockConfig.forceFailReason) {
            return Result.success(
                TravelSummaryFromProductsResult(
                    greeting = "看你的瀏覽紀錄時卡了一下，要不要直接告訴我你想去哪？",
                    cities = emptyList(),
                    failReason = "llm_error",
                )
            )
        }
        return Result.success(MockData.travelSummaryFromProducts(products))
    }

    override suspend fun fetchRecommendCity(
        messages: List<CityChatMessage>,
        shownCities: List<String>,
        companionName: String?,
        personality: List<String>,
        speechStyle: String?
    ): Result<RecommendCityResult> {
        MockData.networkDelay()
        if (CompanionMockConfig.forceFailReason) {
            val round = messages.count { it.role == CityChatMessage.ROLE_USER }
            return Result.success(MockData.recommendCitySoftFailure(round))
        }
        return Result.success(MockData.recommendCity(messages, shownCities))
    }

    override suspend fun fetchTravelGuide(
        summary: String,
        city: String,
        preferences: Map<String, String>,
        orders: List<TripOrderMaterial>,
        products: List<TripProductMaterial>,
        companionName: String?,
        personality: List<String>,
        speechStyle: String?
    ): Result<TravelGuideResult> {
        MockData.networkDelay()
        if (CompanionMockConfig.forceFailReason) return Result.success(MockData.travelGuideSoftFailure())
        val template = MockData.travelGuideFor(city)
        return Result.success(bindMaterials(template, orders, products))
    }

    override suspend fun fetchTravelRevise(
        itineraryDays: List<TravelGuideDay>,
        city: String?,
        targetDay: Int?,
        messages: List<CityChatMessage>,
        preferences: Map<String, String>,
        companionName: String?,
        personality: List<String>,
        speechStyle: String?
    ): Result<TravelReviseResult> {
        MockData.networkDelay()
        if (CompanionMockConfig.forceFailReason) return Result.success(MockData.travelReviseSoftFailure(itineraryDays))
        val lastUserMessage = messages.lastOrNull { it.role == CityChatMessage.ROLE_USER }?.content.orEmpty()
        val result = MockData.travelRevise(itineraryDays, targetDay, lastUserMessage, reviseCallCount)
        reviseCallCount++
        return Result.success(result)
    }

    override suspend fun getSavedTrips(): Result<List<SavedTripRecord>> {
        MockData.networkDelay()
        return Result.success(localStore.getSavedTrips())
    }

    override suspend fun saveSavedTrips(records: List<SavedTripRecord>): Result<Unit> {
        MockData.networkDelay()
        localStore.saveSavedTrips(records)
        return Result.success(Unit)
    }

    // TODO: locale — demo 寫死繁中，之後要接平台語系判斷（見 T7 IsChineseLanguageUseCase）
    override fun isChineseLanguage(): Boolean = true

    /**
     * travel-guide 的「必排入行程」語意：orders 有值時，第一天插入一個帶 oid 的已預訂項目並設定
     * booked_anchor；products 有值時，把最後一天最後一個項目標上 prod_id。只做最小示範，
     * 不追求跟真後端排程演算法一致（demo 目的：讓 UI 能看到「已預訂」/「感興趣」的標記邏輯）。
     */
    private fun bindMaterials(
        template: TravelGuideResult,
        orders: List<TripOrderMaterial>,
        products: List<TripProductMaterial>
    ): TravelGuideResult {
        if (orders.isEmpty() && products.isEmpty()) return template
        var days = template.days
        val firstOrder = orders.firstOrNull()
        if (firstOrder != null && days.isNotEmpty()) {
            val bookedItem = TravelGuideDayItem(
                name = firstOrder.prodName,
                text = "已預訂項目，直接前往體驗",
                type = TravelGuideDayItem.TYPE_SPOT,
                time = "10:00",
                timeBand = "上午",
                oid = firstOrder.oid,
            )
            days = days.mapIndexed { index, day ->
                if (index == 0) {
                    day.copy(
                        bookedAnchorOids = orders.map { it.oid }.filter { it.isNotBlank() },
                        items = listOf(bookedItem) + day.items,
                    )
                } else {
                    day
                }
            }
        }
        val firstProduct = products.firstOrNull()
        if (firstProduct != null && days.isNotEmpty()) {
            val lastDayIndex = days.lastIndex
            val lastDay = days[lastDayIndex]
            if (lastDay.items.isNotEmpty()) {
                val newItems = lastDay.items.toMutableList()
                val lastItemIndex = newItems.lastIndex
                newItems[lastItemIndex] = newItems[lastItemIndex].copy(prodId = firstProduct.prodId)
                days = days.mapIndexed { index, day -> if (index == lastDayIndex) day.copy(items = newItems) else day }
            }
        }
        return template.copy(days = days)
    }
}
