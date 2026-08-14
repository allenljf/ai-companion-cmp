package com.allenljf.aicompanion.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * AI 旅伴行程頁「用景點名稱找商品」的搜尋請求：沿用既有 `v2.1/search/product_list` 端點形狀
 * （必填欄位組合，實測驗證過——少帶 page_name 會被後端擋成 HTTP 400 `The page name field is required.`）。
 * 回應形狀（原本重用 KKday B2C 的 SearchProductResult）改由 mock repository 層自訂，不在此檔範圍。
 */
@Serializable
data class TripProductSearchRequest(
    @SerialName("start") val start: Int = 0,
    // 僅用第一項商品做卡片縮圖，「還有 N 項」改用 metadata.pagination.total_count，
    // 這裡的 count 只影響縮圖來源筆數，比照既有搜尋頁預設值即可，不需要、也不應該拿來當總數上限
    @SerialName("count") val count: Int = 10,
    @SerialName("q") val keyword: String,
    @SerialName("rewrite") val rewrite: String = "1",
    @SerialName("translate_status") val translateStatus: Int = 1,
    // 必填：後端用來識別呼叫來源／頁面情境；沿用既有搜尋頁的預設值，避免自訂值不在後端允許清單內
    @SerialName("page_name") val pageName: String = "product_list_mobile"
)
