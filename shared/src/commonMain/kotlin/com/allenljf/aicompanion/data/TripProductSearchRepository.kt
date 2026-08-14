package com.allenljf.aicompanion.data

import com.allenljf.aicompanion.model.TripProductSearchResult

/**
 * AI 旅伴行程頁「用景點名稱找可訂商品」：獨立成一支輕量 repository，
 * 不走 domain/usecase 對 CompanionRepository 的擴充（避免混進不相關職責）。
 *
 * 原始碼重用 KKday B2C 的 `SearchProductResult`/`B2CProductCardData`；去 B2C 化後
 * 回應型別改成中性命名的 [TripProductSearchResult]（定義於 model/TripProductSearchModels.kt）。
 */
interface TripProductSearchRepository {
    suspend fun searchProductsByKeyword(keyword: String): Result<TripProductSearchResult>
}
