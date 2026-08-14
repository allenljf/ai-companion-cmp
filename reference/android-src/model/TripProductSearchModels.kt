package com.kkday.model.companion

import com.google.gson.annotations.SerializedName

/**
 * AI 旅伴行程頁「用景點名稱找商品」的搜尋請求：沿用既有 `v2.1/search/product_list` 端點
 * （同 SearchRepository.getSearchProductListRequestBody 的必填欄位組合，實測驗證過的形狀——
 * 少帶 page_name 會被後端擋成 HTTP 400 `The page name field is required.`），
 * 回應直接重用既有的 [com.kkday.library.common.model.SearchProductResult]（含 B2CProductCardData）。
 */
data class TripProductSearchRequest(
    @SerializedName("start") val start: Int = 0,
    // 僅用第一項商品做卡片縮圖，「還有 N 項」改用 metadata.pagination.total_count，
    // 這裡的 count 只影響縮圖來源筆數，比照既有搜尋頁預設值即可，不需要、也不應該拿來當總數上限
    @SerializedName("count") val count: Int = 10,
    @SerializedName("q") val keyword: String,
    @SerializedName("rewrite") val rewrite: String = "1",
    @SerializedName("translate_status") val translateStatus: Int = 1,
    // 必填：後端用來識別呼叫來源／頁面情境；沿用既有搜尋頁的預設值，避免自訂值不在後端允許清單內
    @SerializedName("page_name") val pageName: String = "product_list_mobile"
)
