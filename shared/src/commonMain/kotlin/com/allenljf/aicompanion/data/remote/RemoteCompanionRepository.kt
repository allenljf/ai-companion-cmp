package com.allenljf.aicompanion.data.remote

import com.allenljf.aicompanion.data.CompanionRepository
import com.allenljf.aicompanion.data.LocalCompanionStore
import com.allenljf.aicompanion.model.AiPartnerResult
import com.allenljf.aicompanion.model.CityChatMessage
import com.allenljf.aicompanion.model.CompanionProfile
import com.allenljf.aicompanion.model.QuizCompletionRequest
import com.allenljf.aicompanion.model.QuizCompletionResult
import com.allenljf.aicompanion.model.QuizGalleryResult
import com.allenljf.aicompanion.model.QuizHistoryRecord
import com.allenljf.aicompanion.model.QuizRequest
import com.allenljf.aicompanion.model.QuizResult
import com.allenljf.aicompanion.model.RecommendCityMessageRequest
import com.allenljf.aicompanion.model.RecommendCityRequest
import com.allenljf.aicompanion.model.RecommendCityResult
import com.allenljf.aicompanion.model.SavedTripRecord
import com.allenljf.aicompanion.model.SelfIntroductionRequest
import com.allenljf.aicompanion.model.SelfIntroductionResult
import com.allenljf.aicompanion.model.ShareImageV2Request
import com.allenljf.aicompanion.model.ShareImageV2Result
import com.allenljf.aicompanion.model.TravelGuideDay
import com.allenljf.aicompanion.model.TravelGuideRequest
import com.allenljf.aicompanion.model.TravelGuideResult
import com.allenljf.aicompanion.model.TravelReviseRequest
import com.allenljf.aicompanion.model.TravelReviseResult
import com.allenljf.aicompanion.model.TravelSummaryFromOrdersRequest
import com.allenljf.aicompanion.model.TravelSummaryFromOrdersResult
import com.allenljf.aicompanion.model.TravelSummaryFromProductsRequest
import com.allenljf.aicompanion.model.TravelSummaryFromProductsResult
import com.allenljf.aicompanion.model.TravelSummaryOrderRequest
import com.allenljf.aicompanion.model.TravelSummaryRequest
import com.allenljf.aicompanion.model.TravelSummaryResult
import com.allenljf.aicompanion.model.TripOrderMaterial
import com.allenljf.aicompanion.model.TripProductMaterial
import com.allenljf.aicompanion.model.toDomain
import com.allenljf.aicompanion.model.toRequestModel
import com.allenljf.aicompanion.model.toTripPlanOrderRequest

/**
 * [CompanionRepository] 的真後端實作：遠端方法走 [CompanionApiClient] + `toDomain()`；
 * 本地持久化方法（getLocalCompanion 等）委派給 [LocalCompanionStore]，行為與 mock 相同
 * （這幾支後端本來就沒有對應端點，見 CompanionRepository 介面上的註解）。
 *
 * API 規格 personality 為單一字串（`List<String>` 是 ViewModel 這端的多選歷史包袱，見
 * migration/API_CONTRACT.md），一律取第一個。
 */
class RemoteCompanionRepository(
    private val client: CompanionApiClient,
    private val localStore: LocalCompanionStore
) : CompanionRepository {

    override suspend fun getAiPartner(): Result<AiPartnerResult> = runCatching {
        client.getAiPartner().unwrap().toDomain()
    }

    override suspend fun fetchQuiz(
        shownQuestionCounts: Map<String, Int>,
        personality: List<String>,
        speechStyle: String
    ): Result<QuizResult> = runCatching {
        client.fetchQuiz(
            QuizRequest(
                shownQuestionCounts = shownQuestionCounts,
                personality = personality.firstOrNull().orEmpty(),
                speechStyle = speechStyle
            )
        ).unwrap().toDomain()
    }

    override suspend fun completeQuiz(
        completionUuid: String,
        personality: List<String>,
        speechStyle: String,
        selectedTags: List<String>,
        shownCities: List<String>,
        companionName: String?,
        partnerAvatarUrl: String?
    ): Result<QuizCompletionResult> = runCatching {
        client.completeQuiz(
            QuizCompletionRequest(
                completionUuid = completionUuid,
                personality = personality.firstOrNull().orEmpty(),
                speechStyle = speechStyle,
                selectedTags = selectedTags,
                shownCities = shownCities,
                companionName = companionName,
                partnerAvatarUrl = partnerAvatarUrl
            )
        ).unwrap().toDomain()
    }

    override suspend fun fetchShareImageV2(
        completionUuid: String,
        partnerImageUrl: String?
    ): Result<ShareImageV2Result> = runCatching {
        client.fetchShareImageV2(
            ShareImageV2Request(completionUuid = completionUuid, partnerImageUrl = partnerImageUrl)
        ).unwrap().toDomain()
    }

    override suspend fun getQuizGallery(): Result<QuizGalleryResult> = runCatching {
        client.getQuizGallery().unwrap().toDomain()
    }

    override suspend fun fetchSelfIntroduction(
        companionName: String,
        personality: List<String>,
        speechStyle: String,
        gender: String
    ): Result<SelfIntroductionResult> = runCatching {
        client.fetchSelfIntroduction(
            SelfIntroductionRequest(
                companionName = companionName,
                personality = personality.firstOrNull().orEmpty(),
                speechStyle = speechStyle,
                gender = gender
            )
        ).unwrap().toDomain()
    }

    override suspend fun getLocalCompanion(): Result<CompanionProfile?> = runCatching {
        localStore.getLocalCompanion()
    }

    override suspend fun saveLocalCompanion(profile: CompanionProfile): Result<Unit> = runCatching {
        localStore.saveLocalCompanion(profile)
    }

    override suspend fun clearLocalCompanion(): Result<Unit> = runCatching {
        localStore.clearLocalCompanion()
    }

    override suspend fun getShownQuestionCounts(): Result<Map<String, Int>> = runCatching {
        localStore.getShownQuestionCounts()
    }

    override suspend fun saveShownQuestionCounts(counts: Map<String, Int>): Result<Unit> = runCatching {
        localStore.saveShownQuestionCounts(counts)
    }

    override suspend fun getQuizHistory(): Result<List<QuizHistoryRecord>> = runCatching {
        localStore.getQuizHistory()
    }

    override suspend fun saveQuizHistory(records: List<QuizHistoryRecord>): Result<Unit> = runCatching {
        localStore.saveQuizHistory(records)
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
    ): Result<TravelSummaryResult> = runCatching {
        client.fetchTravelSummary(
            TravelSummaryRequest(
                entryType = entryType,
                city = city,
                order = order?.let {
                    TravelSummaryOrderRequest(
                        prodName = it.prodName,
                        packageName = it.packageName.takeIf { pkg -> pkg.isNotBlank() },
                        goDt = it.goDt.takeIf { dt -> dt.isNotBlank() }
                    )
                },
                cityImageUrl = cityImageUrl,
                introText = introText,
                sourceType = sourceType,
                content = content,
                imageUrls = imageUrls,
                note = note,
                previousSummary = previousSummary,
                companionName = companionName,
                personality = personality.firstOrNull(),
                speechStyle = speechStyle
            )
        ).unwrap().toDomain()
    }

    override suspend fun fetchTravelSummaryFromOrders(
        orders: List<TripOrderMaterial>,
        companionName: String?,
        personality: List<String>,
        speechStyle: String?
    ): Result<TravelSummaryFromOrdersResult> = runCatching {
        client.fetchTravelSummaryFromOrders(
            TravelSummaryFromOrdersRequest(
                orders = orders.map { it.toRequestModel() },
                companionName = companionName,
                personality = personality.firstOrNull(),
                speechStyle = speechStyle
            )
        ).unwrap().toDomain()
    }

    override suspend fun fetchTravelSummaryFromWish(
        products: List<TripProductMaterial>,
        companionName: String?,
        personality: List<String>,
        speechStyle: String?
    ): Result<TravelSummaryFromProductsResult> = runCatching {
        client.fetchTravelSummaryFromWish(
            TravelSummaryFromProductsRequest(
                products = products.map { it.toRequestModel() },
                companionName = companionName,
                personality = personality.firstOrNull(),
                speechStyle = speechStyle
            )
        ).unwrap().toDomain()
    }

    override suspend fun fetchTravelSummaryFromHistory(
        products: List<TripProductMaterial>,
        companionName: String?,
        personality: List<String>,
        speechStyle: String?
    ): Result<TravelSummaryFromProductsResult> = runCatching {
        client.fetchTravelSummaryFromHistory(
            TravelSummaryFromProductsRequest(
                products = products.map { it.toRequestModel() },
                companionName = companionName,
                personality = personality.firstOrNull(),
                speechStyle = speechStyle
            )
        ).unwrap().toDomain()
    }

    override suspend fun fetchRecommendCity(
        messages: List<CityChatMessage>,
        shownCities: List<String>,
        companionName: String?,
        personality: List<String>,
        speechStyle: String?
    ): Result<RecommendCityResult> = runCatching {
        client.fetchRecommendCity(
            RecommendCityRequest(
                messages = messages.map { RecommendCityMessageRequest(role = it.role, content = it.content) },
                shownCities = shownCities.takeIf { it.isNotEmpty() },
                companionName = companionName,
                personality = personality.firstOrNull(),
                speechStyle = speechStyle
            )
        ).unwrap().toDomain()
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
    ): Result<TravelGuideResult> = runCatching {
        client.fetchTravelGuide(
            TravelGuideRequest(
                summary = summary,
                city = city,
                preferences = preferences,
                orders = orders.map { it.toTripPlanOrderRequest() }.takeIf { it.isNotEmpty() },
                products = products.map { it.toRequestModel() }.takeIf { it.isNotEmpty() },
                companionName = companionName,
                personality = personality.firstOrNull(),
                speechStyle = speechStyle
            )
        ).unwrap().toDomain()
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
    ): Result<TravelReviseResult> = runCatching {
        client.fetchTravelRevise(
            TravelReviseRequest(
                city = city,
                targetDay = targetDay,
                itinerary = itineraryDays.map { it.toRequestModel() },
                messages = messages.map { RecommendCityMessageRequest(role = it.role, content = it.content) },
                preferences = preferences.takeIf { it.isNotEmpty() },
                companionName = companionName,
                personality = personality.firstOrNull(),
                speechStyle = speechStyle
            )
        ).unwrap().toDomain()
    }

    override suspend fun getSavedTrips(): Result<List<SavedTripRecord>> = runCatching {
        localStore.getSavedTrips()
    }

    override suspend fun saveSavedTrips(records: List<SavedTripRecord>): Result<Unit> = runCatching {
        localStore.saveSavedTrips(records)
    }

    // TODO: locale — demo 寫死繁中，之後要接平台語系判斷（同 mock，見 T7 GetLocalCompanionUseCase）
    override fun isChineseLanguage(): Boolean = true
}
