package com.kkday.data.repository.companion

import android.content.Context
import com.kkday.data.consts.KoinConstant
import com.kkday.data.datastore.DataStoreHelper
import com.kkday.library.common.repository.CompanionRepository
import com.kkday.library.common.storage.PersistentManager
import com.kkday.library.networking.core.api.BaseApiRepository
import com.kkday.library.networking.core.api.CompanionMockSwitch
import com.kkday.library.networking.service.companion.ICompanionApiService
import com.kkday.model.companion.AiPartnerResult
import com.kkday.model.companion.CompanionProfile
import com.kkday.model.companion.QuizCompletionRequest
import com.kkday.model.companion.QuizCompletionResult
import com.kkday.model.companion.QuizGalleryResult
import com.kkday.model.companion.QuizHistoryRecord
import com.kkday.model.companion.QuizRequest
import com.kkday.model.companion.QuizResult
import com.kkday.model.companion.CityChatMessage
import com.kkday.model.companion.RecommendCityMessageRequest
import com.kkday.model.companion.RecommendCityRequest
import com.kkday.model.companion.RecommendCityResult
import com.kkday.model.companion.SavedTripRecord
import com.kkday.model.companion.SelfIntroductionRequest
import com.kkday.model.companion.SelfIntroductionResult
import com.kkday.model.companion.ShareImageV2Request
import com.kkday.model.companion.ShareImageV2Result
import com.kkday.model.companion.TravelGuideDay
import com.kkday.model.companion.TravelGuideRequest
import com.kkday.model.companion.TravelGuideResult
import com.kkday.model.companion.TravelPlanValidationException
import com.kkday.model.companion.TravelReviseRequest
import com.kkday.model.companion.TravelReviseResult
import com.kkday.model.companion.toRequestModel
import com.kkday.model.companion.TravelSummaryFromOrdersRequest
import com.kkday.model.companion.TravelSummaryFromOrdersResult
import com.kkday.model.companion.TravelSummaryRequest
import com.kkday.model.companion.TravelSummaryResult
import com.kkday.model.companion.TravelSummaryFromProductsRequest
import com.kkday.model.companion.TravelSummaryFromProductsResult
import com.kkday.model.companion.TravelSummaryOrderRequest
import com.kkday.model.companion.TripOrderMaterial
import com.kkday.model.companion.TripProductMaterial
import com.kkday.model.companion.toTripPlanOrderRequest
import com.kkday.model.companion.toDomain
import com.kkday.shared_core.util.LocaleUtil
import okhttp3.internal.http2.StreamResetException
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import retrofit2.HttpException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException

@Single(binds = [CompanionRepository::class])
class CompanionRepositoryImpl(
    private val apiService: ICompanionApiService,
    @Named(KoinConstant.APP_DATA_STORE) private val dataStore: DataStoreHelper,
    private val persistentManager: PersistentManager,
    private val context: Context
) : BaseApiRepository(), CompanionRepository {

    override fun isMockEnabled(): Boolean = CompanionMockSwitch.isEnabled(context)

    override fun isChineseLanguage(): Boolean = LocaleUtil.isChineseLanguageType(persistentManager.getLanguage())

    override suspend fun getAiPartner(): Result<AiPartnerResult> {
        return try {
            val response = apiResultData(apiService.getAiPartner())
            response.checkCompanionMetadata()
            Result.success(response.data!!.toDomain())
        } catch (e: Exception) {
            Result.failure(e.toCompanionApiException())
        }
    }

    override suspend fun fetchQuiz(
        shownQuestionCounts: Map<String, Int>,
        personality: List<String>,
        speechStyle: String
    ): Result<QuizResult> {
        return try {
            // API 規格：personality 為單一字串；domain 層維持 List<String>（UI 已限定單選），這裡取第一個轉發
            val response = apiResultData(
                apiService.fetchQuiz(QuizRequest(shownQuestionCounts, personality.firstOrNull().orEmpty(), speechStyle))
            )
            response.checkCompanionMetadata()
            Result.success(response.data!!.toDomain())
        } catch (e: Exception) {
            Result.failure(e.toCompanionApiException())
        }
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
        return try {
            val response = apiResultData(
                apiService.completeQuiz(
                    QuizCompletionRequest(
                        completionUuid = completionUuid,
                        personality = personality.firstOrNull().orEmpty(),
                        speechStyle = speechStyle,
                        selectedTags = selectedTags,
                        shownCities = shownCities,
                        companionName = companionName,
                        partnerAvatarUrl = partnerAvatarUrl?.takeIf { it.isNotBlank() }
                    )
                )
            )
            response.checkCompanionMetadata()
            // 即使成功，data.fail_reason 仍可能有值（軟失敗）；由上層以 isAnalysisSuccess 判斷。
            Result.success(response.data!!.toDomain())
        } catch (e: Exception) {
            Result.failure(e.toCompanionApiException())
        }
    }

    override suspend fun fetchShareImageV2(completionUuid: String, partnerImageUrl: String?): Result<ShareImageV2Result> {
        return try {
            val response = apiResultData(
                apiService.shareImageV2(ShareImageV2Request(completionUuid, partnerImageUrl))
            )
            response.checkCompanionMetadata()
            Result.success(response.data!!.toDomain())
        } catch (e: HttpException) {
            when {
                // 首發產圖 >30s 仍可能遇到 gateway 逾時：server 仍在背景產圖 → 視為 processing 續輪詢
                e.code() == HttpURLConnection.HTTP_GATEWAY_TIMEOUT ->
                    Result.success(ShareImageV2Result.processing)
                // 429：踩到 server throttle（10 次/分），產圖仍在背景進行 → 視為 processing 續輪詢
                e.code() == HTTP_TOO_MANY_REQUESTS ->
                    Result.success(ShareImageV2Result.processing)
                else -> {
                    // errorBody 僅能讀一次：一次解析出業務錯誤碼再判斷
                    val mapped = CompanionApiException.fromHttpException(e)
                    if (mapped?.code == CompanionApiException.QUIZ_SESSION_NOT_FOUND) {
                        Result.success(ShareImageV2Result.sessionExpired)
                    } else {
                        Result.failure(mapped ?: e)
                    }
                }
            }
        } catch (e: SocketTimeoutException) {
            // client timeout：同樣視為 processing 續輪詢
            Result.success(ShareImageV2Result.processing)
        } catch (e: StreamResetException) {
            // HTTP/2 RST_STREAM(CANCEL)：gateway 硬切或 throttle 提前斷線，
            // 後端仍會把素材產完上 CDN → 視同 504/processing 續輪詢
            Result.success(ShareImageV2Result.processing)
        } catch (e: CompanionApiException) {
            // 巢狀 200 業務錯誤（checkCompanionMetadata 拋出）
            if (e.code == CompanionApiException.QUIZ_SESSION_NOT_FOUND) {
                Result.success(ShareImageV2Result.sessionExpired)
            } else {
                Result.failure(e)
            }
        } catch (e: Exception) {
            Result.failure(e.toCompanionApiException())
        }
    }

    override suspend fun fetchSelfIntroduction(
        companionName: String,
        personality: List<String>,
        speechStyle: String,
        gender: String
    ): Result<SelfIntroductionResult> {
        return try {
            val response = apiResultData(
                apiService.selfIntroduction(
                    SelfIntroductionRequest(
                        companionName = companionName,
                        personality = personality.firstOrNull().orEmpty(),
                        speechStyle = speechStyle,
                        gender = gender
                    )
                )
            )
            response.checkCompanionMetadata()
            // 即使成功，data.fail_reason 仍可能有值（LLM 軟失敗，introduction 為後端固定文案）
            Result.success(response.data!!.toDomain())
        } catch (e: Exception) {
            Result.failure(e.toCompanionApiException())
        }
    }

    override suspend fun getQuizGallery(): Result<QuizGalleryResult> {
        return try {
            val response = apiResultData(apiService.getQuizGallery())
            response.checkCompanionMetadata()
            Result.success(response.data!!.toDomain())
        } catch (e: Exception) {
            Result.failure(e.toCompanionApiException())
        }
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
        return try {
            val response = apiResultData(
                apiService.travelSummary(
                    TravelSummaryRequest(
                        entryType = entryType,
                        city = city?.takeIf { it.isNotBlank() },
                        order = order?.let {
                            TravelSummaryOrderRequest(
                                prodName = it.prodName,
                                packageName = it.packageName.takeIf { name -> name.isNotBlank() },
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
                )
            )
            response.checkCompanionMetadata()
            // 即使成功，data.fail_reason 仍可能有值（LLM 軟失敗，summary 為兜底文案）
            Result.success(response.data!!.toDomain())
        } catch (e: Exception) {
            Result.failure(e.toTravelPlanException())
        }
    }

    override suspend fun fetchTravelSummaryFromOrders(
        orders: List<TripOrderMaterial>,
        companionName: String?,
        personality: List<String>,
        speechStyle: String?
    ): Result<TravelSummaryFromOrdersResult> {
        return try {
            val response = apiResultData(
                apiService.travelSummaryFromOrders(
                    TravelSummaryFromOrdersRequest(
                        orders = orders.map { it.toRequestModel() },
                        companionName = companionName,
                        personality = personality.firstOrNull(),
                        speechStyle = speechStyle
                    )
                )
            )
            response.checkCompanionMetadata()
            Result.success(response.data!!.toDomain())
        } catch (e: Exception) {
            Result.failure(e.toTravelPlanException())
        }
    }

    override suspend fun fetchTravelSummaryFromWish(
        products: List<TripProductMaterial>,
        companionName: String?,
        personality: List<String>,
        speechStyle: String?
    ): Result<TravelSummaryFromProductsResult> {
        return try {
            val response = apiResultData(
                apiService.travelSummaryFromWish(buildFromProductsRequest(products, companionName, personality, speechStyle))
            )
            response.checkCompanionMetadata()
            Result.success(response.data!!.toDomain())
        } catch (e: Exception) {
            Result.failure(e.toTravelPlanException())
        }
    }

    override suspend fun fetchTravelSummaryFromHistory(
        products: List<TripProductMaterial>,
        companionName: String?,
        personality: List<String>,
        speechStyle: String?
    ): Result<TravelSummaryFromProductsResult> {
        return try {
            val response = apiResultData(
                apiService.travelSummaryFromHistory(buildFromProductsRequest(products, companionName, personality, speechStyle))
            )
            response.checkCompanionMetadata()
            Result.success(response.data!!.toDomain())
        } catch (e: Exception) {
            Result.failure(e.toTravelPlanException())
        }
    }

    private fun buildFromProductsRequest(
        products: List<TripProductMaterial>,
        companionName: String?,
        personality: List<String>,
        speechStyle: String?
    ) = TravelSummaryFromProductsRequest(
        products = products.map { it.toRequestModel() },
        companionName = companionName,
        personality = personality.firstOrNull(),
        speechStyle = speechStyle
    )

    override suspend fun fetchRecommendCity(
        messages: List<CityChatMessage>,
        shownCities: List<String>,
        companionName: String?,
        personality: List<String>,
        speechStyle: String?
    ): Result<RecommendCityResult> {
        return try {
            val response = apiResultData(
                apiService.recommendCity(
                    RecommendCityRequest(
                        messages = messages.map { RecommendCityMessageRequest(it.role, it.content) },
                        shownCities = shownCities.takeIf { it.isNotEmpty() },
                        companionName = companionName,
                        personality = personality.firstOrNull(),
                        speechStyle = speechStyle
                    )
                )
            )
            response.checkCompanionMetadata()
            // 即使成功，data.fail_reason 仍可能有值（LLM 軟失敗，reply 為兜底文案；不應 append 回 messages）
            Result.success(response.data!!.toDomain())
        } catch (e: Exception) {
            Result.failure(e.toTravelPlanException())
        }
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
        return try {
            val response = apiResultData(
                apiService.travelGuide(
                    TravelGuideRequest(
                        summary = summary,
                        city = city,
                        preferences = preferences,
                        orders = orders.map { it.toTripPlanOrderRequest() }.takeIf { it.isNotEmpty() },
                        // 「必須排入行程」的商品上限 10 筆（比 summary 的 20 筆少，放太多會把行程塞爆）
                        products = products.take(TRAVEL_GUIDE_PRODUCTS_MAX)
                            .map { it.toRequestModel() }
                            .takeIf { it.isNotEmpty() },
                        companionName = companionName,
                        personality = personality.firstOrNull(),
                        speechStyle = speechStyle
                    )
                )
            )
            response.checkCompanionMetadata()
            // 即使成功，data.fail_reason 仍可能有值（軟失敗：overview=null、days=[]，UI 顯示重試）
            Result.success(response.data!!.toDomain())
        } catch (e: Exception) {
            Result.failure(e.toTravelPlanException())
        }
    }

    /**
     * 驗證錯誤映射為 domain 可見的 [TravelPlanValidationException]，供 UI 與可重試的硬失敗區分。
     * Phase 2 實測為 HTTP 400 + metadata.status=110001（全站 invalid_params）；422 為舊版文件推測值，保留兼容。
     */
    private fun Exception.toTravelPlanException(): Exception {
        val mapped = toCompanionApiException()
        return if (mapped is CompanionApiException &&
            (mapped.code == CompanionApiException.INVALID_PARAMS || mapped.code == CompanionApiException.VALIDATION_ERROR)
        ) {
            TravelPlanValidationException(mapped.message.orEmpty())
        } else {
            mapped
        }
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
        return try {
            val response = apiResultData(
                apiService.travelRevise(
                    TravelReviseRequest(
                        city = city?.takeIf { it.isNotBlank() },
                        targetDay = targetDay,
                        // 2026-08 改版：itinerary 為扁平陣列，不包 {"days":[...]}
                        itinerary = itineraryDays.map { it.toRequestModel() },
                        // 2026-08 改版（二）：不再有獨立的 request 欄位，這次需求＝messages 最後一則
                        messages = messages.map { RecommendCityMessageRequest(it.role, it.content) },
                        preferences = preferences.takeIf { it.isNotEmpty() },
                        companionName = companionName,
                        personality = personality.firstOrNull(),
                        speechStyle = speechStyle
                    )
                )
            )
            response.checkCompanionMetadata()
            // 即使成功，data.fail_reason 仍可能有值（軟失敗：reply 為兜底文案，不 merge、不進歷史）
            Result.success(response.data!!.toDomain())
        } catch (e: Exception) {
            Result.failure(e.toTravelPlanException())
        }
    }

    override suspend fun getSavedTrips(): Result<List<SavedTripRecord>> {
        return try {
            val record = dataStore.getAsync<SavedTripListRecord>(savedTripsKey())
            Result.success(record?.records.orEmpty())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveSavedTrips(records: List<SavedTripRecord>): Result<Unit> {
        return try {
            dataStore.setAsync(savedTripsKey(), SavedTripListRecord(records))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLocalCompanion(): Result<CompanionProfile?> {
        return try {
            Result.success(dataStore.getAsync<CompanionProfile>(companionProfileKey()))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveLocalCompanion(profile: CompanionProfile): Result<Unit> {
        return try {
            dataStore.setAsync(companionProfileKey(), profile)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearLocalCompanion(): Result<Unit> {
        return try {
            dataStore.remove(companionProfileKey())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getShownQuestionCounts(): Result<Map<String, Int>> {
        return try {
            val record = dataStore.getAsync<ShownQuestionCountsRecord>(shownQuestionCountsKey())
            Result.success(record?.counts.orEmpty())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveShownQuestionCounts(counts: Map<String, Int>): Result<Unit> {
        return try {
            dataStore.setAsync(shownQuestionCountsKey(), ShownQuestionCountsRecord(counts))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getQuizHistory(): Result<List<QuizHistoryRecord>> {
        return try {
            val record = dataStore.getAsync<QuizHistoryListRecord>(quizHistoryKey())
            Result.success(record?.records.orEmpty())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveQuizHistory(records: List<QuizHistoryRecord>): Result<Unit> {
        return try {
            dataStore.setAsync(quizHistoryKey(), QuizHistoryListRecord(records))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** 每個會員各自一份旅伴設定檔（memberUuid 即 API header 使用的值）；未登入以 guest 區隔。 */
    private fun companionProfileKey(): String = memberScopedKey(KEY_COMPANION_PROFILE_PREFIX)

    private fun quizHistoryKey(): String = memberScopedKey(KEY_QUIZ_HISTORY_PREFIX)

    private fun shownQuestionCountsKey(): String = memberScopedKey(KEY_SHOWN_QUESTIONS_PREFIX)

    private fun savedTripsKey(): String = memberScopedKey(KEY_SAVED_TRIPS_PREFIX)

    private fun memberScopedKey(prefix: String): String {
        val memberUuid = persistentManager.getMemberUuid().ifBlank { MEMBER_UUID_GUEST }
        return "${prefix}_$memberUuid"
    }

    // Gson 直接反序列化 Map 會把數值變 Double，包一層 class 讓欄位泛型正確還原 Int
    private data class ShownQuestionCountsRecord(
        val counts: Map<String, Int> = emptyMap()
    )

    // 同上：包一層 class 讓 List 泛型正確還原
    private data class QuizHistoryListRecord(
        val records: List<QuizHistoryRecord> = emptyList()
    )

    // 同上：「我的旅程」本地清單
    private data class SavedTripListRecord(
        val records: List<SavedTripRecord> = emptyList()
    )

    companion object {
        // HttpURLConnection 沒有 429 常數（RFC 6585）
        private const val HTTP_TOO_MANY_REQUESTS = 429
        // travel-guide products 上限（「必須排入行程」的商品，超過會被 400 擋掉）
        private const val TRAVEL_GUIDE_PRODUCTS_MAX = 10
        private const val KEY_COMPANION_PROFILE_PREFIX = "companion_profile"
        private const val KEY_SHOWN_QUESTIONS_PREFIX = "companion_shown_questions"
        private const val KEY_QUIZ_HISTORY_PREFIX = "companion_quiz_history"
        private const val KEY_SAVED_TRIPS_PREFIX = "companion_saved_trips"
        private const val MEMBER_UUID_GUEST = "guest"
    }
}
