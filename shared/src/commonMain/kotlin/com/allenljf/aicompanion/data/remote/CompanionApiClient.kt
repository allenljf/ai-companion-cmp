package com.allenljf.aicompanion.data.remote

import com.allenljf.aicompanion.data.CompanionApiException
import com.allenljf.aicompanion.model.AiPartnerDataResponse
import com.allenljf.aicompanion.model.CompanionProductListResponse
import com.allenljf.aicompanion.model.OrdersDataResponse
import com.allenljf.aicompanion.model.QuizCompletionDataResponse
import com.allenljf.aicompanion.model.QuizCompletionRequest
import com.allenljf.aicompanion.model.QuizDataResponse
import com.allenljf.aicompanion.model.QuizGalleryDataResponse
import com.allenljf.aicompanion.model.QuizRequest
import com.allenljf.aicompanion.model.RecommendCityDataResponse
import com.allenljf.aicompanion.model.RecommendCityRequest
import com.allenljf.aicompanion.model.SelfIntroductionDataResponse
import com.allenljf.aicompanion.model.SelfIntroductionRequest
import com.allenljf.aicompanion.model.ShareImageV2DataResponse
import com.allenljf.aicompanion.model.ShareImageV2Request
import com.allenljf.aicompanion.model.TravelGuideDataResponse
import com.allenljf.aicompanion.model.TravelGuideRequest
import com.allenljf.aicompanion.model.TravelReviseDataResponse
import com.allenljf.aicompanion.model.TravelReviseRequest
import com.allenljf.aicompanion.model.TravelSummaryDataResponse
import com.allenljf.aicompanion.model.TravelSummaryFromOrdersDataResponse
import com.allenljf.aicompanion.model.TravelSummaryFromOrdersRequest
import com.allenljf.aicompanion.model.TravelSummaryFromProductsDataResponse
import com.allenljf.aicompanion.model.TravelSummaryFromProductsRequest
import com.allenljf.aicompanion.model.TravelSummaryRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * 真後端信封：`{metadata:{status,desc}, data:{...}}`（見 T17 brief 與 migration/API_CONTRACT.md
 * 「實際部署差異」一節）。`desc` 成功時常是字串，失敗時常是字串陣列，統一用 JsonElement 收、
 * 用 [ApiMetadata.descText] 拉成一行塞進 [CompanionApiException]。
 */
@Serializable
data class ApiMetadata(
    val status: String,
    val desc: JsonElement? = null
) {
    fun descText(): String = when (val d = desc) {
        null -> ""
        is JsonArray -> d.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }.joinToString("; ")
        is JsonPrimitive -> d.contentOrNull.orEmpty()
        else -> ""
    }
}

@Serializable
data class ApiEnvelope<T>(
    val metadata: ApiMetadata,
    val data: T? = null
)

private const val SUCCESS_STATUS = "0000"

/** 信封拆殼：非成功狀態或 data 缺漏一律丟 [CompanionApiException]，呼叫端用 runCatching 收斂成 Result。 */
fun <T> ApiEnvelope<T>.unwrap(): T {
    if (metadata.status != SUCCESS_STATUS) {
        throw CompanionApiException(metadata.status, metadata.descText())
    }
    return data ?: throw CompanionApiException(metadata.status, "empty data")
}

/**
 * Ktor 端點函式集合，對照 scratchpad/sdd/api-schemas.md 的 request 形狀逐支移植。
 * Base URL 無驗證 header（demo 後端公開），逾時拉長到 60 秒因為 LLM 端點可能要 10~30 秒才回。
 */
class CompanionApiClient(
    private val baseUrl: String = BASE_URL,
    private val httpClient: HttpClient = createHttpClient()
) {

    // ---------- companion ----------

    suspend fun getAiPartner(): ApiEnvelope<AiPartnerDataResponse> =
        httpClient.get("$baseUrl/v1/companion/ai-partner").body()

    suspend fun fetchQuiz(body: QuizRequest): ApiEnvelope<QuizDataResponse> =
        httpClient.post("$baseUrl/v1/companion/quiz") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun completeQuiz(body: QuizCompletionRequest): ApiEnvelope<QuizCompletionDataResponse> =
        httpClient.post("$baseUrl/v1/companion/quiz-completions") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    // 實測很慢（首次約 80 秒、同 uuid 重打約 35 秒，後端未快取），沿用全域 60 秒逾時會提早炸掉，
    // 只放寬這支到 [SHARE_IMAGE_TIMEOUT_MILLIS]（其餘端點不受影響）
    suspend fun fetchShareImageV2(body: ShareImageV2Request): ApiEnvelope<ShareImageV2DataResponse> =
        httpClient.post("$baseUrl/v1/companion/share-image-v2") {
            contentType(ContentType.Application.Json)
            setBody(body)
            // socketTimeoutMillis 也要一併放寬——回應是產完圖才一次回傳，80 秒間沒有任何 socket
            // 資料往來，只放寬 requestTimeoutMillis 的話會先被預設 60 秒的 socket 逾時打斷
            timeout {
                requestTimeoutMillis = SHARE_IMAGE_TIMEOUT_MILLIS
                socketTimeoutMillis = SHARE_IMAGE_TIMEOUT_MILLIS
            }
        }.body()

    suspend fun getQuizGallery(): ApiEnvelope<QuizGalleryDataResponse> =
        httpClient.get("$baseUrl/v1/companion/quiz-gallery").body()

    suspend fun fetchSelfIntroduction(body: SelfIntroductionRequest): ApiEnvelope<SelfIntroductionDataResponse> =
        httpClient.post("$baseUrl/v1/companion/self-introduction") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun getOrders(): ApiEnvelope<OrdersDataResponse> =
        httpClient.get("$baseUrl/v1/companion/orders").body()

    suspend fun getWishList(): ApiEnvelope<CompanionProductListResponse> =
        httpClient.get("$baseUrl/v1/companion/wish_list").body()

    suspend fun getHistory(): ApiEnvelope<CompanionProductListResponse> =
        httpClient.get("$baseUrl/v1/companion/history").body()

    // ---------- plan ----------

    suspend fun fetchTravelSummary(body: TravelSummaryRequest): ApiEnvelope<TravelSummaryDataResponse> =
        httpClient.post("$baseUrl/v1/plan/travel-summary") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun fetchTravelSummaryFromOrders(
        body: TravelSummaryFromOrdersRequest
    ): ApiEnvelope<TravelSummaryFromOrdersDataResponse> =
        httpClient.post("$baseUrl/v1/plan/travel-summary-from-orders") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun fetchTravelSummaryFromWish(
        body: TravelSummaryFromProductsRequest
    ): ApiEnvelope<TravelSummaryFromProductsDataResponse> =
        httpClient.post("$baseUrl/v1/plan/travel-summary-from-wish") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun fetchTravelSummaryFromHistory(
        body: TravelSummaryFromProductsRequest
    ): ApiEnvelope<TravelSummaryFromProductsDataResponse> =
        httpClient.post("$baseUrl/v1/plan/travel-summary-from-history") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun fetchRecommendCity(body: RecommendCityRequest): ApiEnvelope<RecommendCityDataResponse> =
        httpClient.post("$baseUrl/v1/plan/recommend-city") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun fetchTravelGuide(body: TravelGuideRequest): ApiEnvelope<TravelGuideDataResponse> =
        httpClient.post("$baseUrl/v1/plan/travel-guide") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun fetchTravelRevise(body: TravelReviseRequest): ApiEnvelope<TravelReviseDataResponse> =
        httpClient.post("$baseUrl/v1/plan/travel-revise") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    companion object {
        const val BASE_URL = "https://ai-companion-api-30568057620.asia-east1.run.app"
    }
}

private const val REQUEST_TIMEOUT_MILLIS = 60_000L

// share-image-v2 首次約 80 秒；120 秒留緩衝，避免卡在剛好超過的邊界情況
private const val SHARE_IMAGE_TIMEOUT_MILLIS = 120_000L

private fun createHttpClient(): HttpClient = HttpClient {
    install(ContentNegotiation) {
        // contentType = Any：後端實測偶爾在錯誤情況下回 text/plain 但 body 仍是 JSON，
        // 不放寬的話 Ktor 會直接丟 NoTransformationFoundException 蓋掉真正的錯誤內容
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
                // 後端把 shown_question_counts/shown_cities 等欄位標成必填，即使值是空 map/list
                // 也要序列化出來——kotlinx.serialization 預設「等於欄位預設值就省略」會被 400 擋掉
                encodeDefaults = true
                // 軟失敗（fail_reason 有值）時，後端會把部分非 nullable 欄位（例如 travel-guide 的
                // days）改回傳 null 而非省略；coerceInputValues 讓這種情況退回欄位預設值，不直接炸 decode
                coerceInputValues = true
            },
            contentType = ContentType.Any
        )
    }
    install(HttpTimeout) {
        requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
        connectTimeoutMillis = REQUEST_TIMEOUT_MILLIS
        socketTimeoutMillis = REQUEST_TIMEOUT_MILLIS
    }
}
