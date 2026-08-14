package com.kkday.library.networking.service.companion

import com.kkday.library.networking.core.api.B2CApiResponse
import com.kkday.library.networking.resource.order.OrderListData
import com.kkday.model.companion.AiPartnerDataResponse
import com.kkday.model.companion.CompanionProductListResponse
import com.kkday.model.companion.QuizCompletionDataResponse
import com.kkday.model.companion.QuizCompletionRequest
import com.kkday.model.companion.QuizDataResponse
import com.kkday.model.companion.QuizGalleryDataResponse
import com.kkday.model.companion.QuizRequest
import com.kkday.model.companion.SelfIntroductionDataResponse
import com.kkday.model.companion.SelfIntroductionRequest
import com.kkday.model.companion.RecommendCityDataResponse
import com.kkday.model.companion.RecommendCityRequest
import com.kkday.model.companion.ShareImageV2DataResponse
import com.kkday.model.companion.ShareImageV2Request
import com.kkday.model.companion.TravelGuideDataResponse
import com.kkday.model.companion.TravelGuideRequest
import com.kkday.model.companion.TravelReviseDataResponse
import com.kkday.model.companion.TravelReviseRequest
import com.kkday.model.companion.TravelSummaryDataResponse
import com.kkday.model.companion.TravelSummaryFromOrdersDataResponse
import com.kkday.model.companion.TravelSummaryFromOrdersRequest
import com.kkday.model.companion.TravelSummaryFromProductsDataResponse
import com.kkday.model.companion.TravelSummaryFromProductsRequest
import com.kkday.model.companion.TravelSummaryRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ICompanionApiService {

    @GET("v3/companion/ai-partner")
    suspend fun getAiPartner(): B2CApiResponse<AiPartnerDataResponse>

    @POST("v3/companion/quiz")
    suspend fun fetchQuiz(@Body body: QuizRequest): B2CApiResponse<QuizDataResponse>

    @POST("v3/companion/quiz-completions")
    suspend fun completeQuiz(@Body body: QuizCompletionRequest): B2CApiResponse<QuizCompletionDataResponse>

    @POST("v3/companion/share-image-v2")
    suspend fun shareImageV2(@Body body: ShareImageV2Request): B2CApiResponse<ShareImageV2DataResponse>

    @POST("v3/companion/self-introduction")
    suspend fun selfIntroduction(@Body body: SelfIntroductionRequest): B2CApiResponse<SelfIntroductionDataResponse>

    @GET("v3/companion/quiz-gallery")
    suspend fun getQuizGallery(): B2CApiResponse<QuizGalleryDataResponse>

    // 帶訂單開場用：目前回傳跟 v2.2/orders 一樣形狀的 mock 資料，走目前 build 環境正常登入態即可，不需帶 body
    @GET("v3/companion/orders")
    suspend fun getUpcomingOrders(): B2CApiResponse<OrderListData>

    // 願望清單開場用：目前為假資料端點，不需帶任何參數
    @GET("v3/companion/wish_list")
    suspend fun getWishList(): B2CApiResponse<CompanionProductListResponse>

    // 瀏覽/購買紀錄開場用：目前為假資料端點，不需帶任何參數
    @GET("v3/companion/history")
    suspend fun getBrowseHistory(): B2CApiResponse<CompanionProductListResponse>

    // 願望清單開場：從收藏商品（1~20 筆）聚合判斷最多 3 個城市選項
    @POST("v3/companion/travel-summary-from-wish")
    suspend fun travelSummaryFromWish(
        @Body body: TravelSummaryFromProductsRequest
    ): B2CApiResponse<TravelSummaryFromProductsDataResponse>

    // 瀏覽/購買紀錄開場：從瀏覽商品（1~20 筆）聚合判斷最多 3 個城市選項
    @POST("v3/companion/travel-summary-from-history")
    suspend fun travelSummaryFromHistory(
        @Body body: TravelSummaryFromProductsRequest
    ): B2CApiResponse<TravelSummaryFromProductsDataResponse>

    @POST("v3/companion/travel-summary")
    suspend fun travelSummary(@Body body: TravelSummaryRequest): B2CApiResponse<TravelSummaryDataResponse>

    // 後端尚未實作（見隨附 handoff prompt）；帶訂單開場：依近期訂單材料判斷目的地選項
    @POST("v3/companion/travel-summary-from-orders")
    suspend fun travelSummaryFromOrders(
        @Body body: TravelSummaryFromOrdersRequest
    ): B2CApiResponse<TravelSummaryFromOrdersDataResponse>

    @POST("v3/companion/travel-guide")
    suspend fun travelGuide(@Body body: TravelGuideRequest): B2CApiResponse<TravelGuideDataResponse>

    @POST("v3/companion/recommend-city")
    suspend fun recommendCity(@Body body: RecommendCityRequest): B2CApiResponse<RecommendCityDataResponse>

    @POST("v3/companion/travel-revise")
    suspend fun travelRevise(@Body body: TravelReviseRequest): B2CApiResponse<TravelReviseDataResponse>
}
