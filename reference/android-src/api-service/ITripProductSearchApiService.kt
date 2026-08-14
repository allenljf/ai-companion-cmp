package com.kkday.library.networking.service.companion

import com.kkday.library.common.model.SearchProductResult
import com.kkday.library.networking.core.api.B2CApiResponse
import com.kkday.model.companion.TripProductSearchRequest
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * AI 旅伴行程頁專用的輕量商品搜尋介面：沿用既有 `v2.1/search/product_list` 端點，
 * 只帶關鍵字（景點名稱）搜尋，回應重用既有 [SearchProductResult]／[com.kkday.library.networking.resource.product.B2CProductCardData]。
 */
interface ITripProductSearchApiService {

    @POST("v2.1/search/product_list")
    suspend fun searchProducts(@Body body: TripProductSearchRequest): B2CApiResponse<SearchProductResult>
}
