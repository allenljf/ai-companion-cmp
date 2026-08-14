package com.kkday.library.common.repository

import com.kkday.library.networking.resource.product.B2CProductCardData

/**
 * AI 旅伴行程頁「用景點名稱找 KKday 可訂商品」：沿用既有搜尋 API，不走 domain/usecase 對
 * CompanionRepository 的擴充（避免混進不相關職責），獨立成一支輕量 repository。
 */
interface TripProductSearchRepository {
    suspend fun searchProductsByKeyword(keyword: String): Result<TripProductSearchResult>
}

/**
 * @param totalCount 該關鍵字實際符合的商品總數（來自 API `metadata.pagination.total_count`），
 * 用於「還有 N 項相關商品」文案——不可用 [products].size 代替，因為那只是本次抓回的筆數上限。
 */
data class TripProductSearchResult(
    val products: List<B2CProductCardData>,
    val totalCount: Int,
)
