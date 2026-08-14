package com.allenljf.aicompanion.domain

import com.allenljf.aicompanion.data.TripProductSearchRepository
import com.allenljf.aicompanion.model.TripProductSearchResult

/** 行程頁景點卡背景搜尋：以景點名稱為關鍵字打既有商品搜尋 API，找到商品才顯示卡片。 */
class SearchTripProductsUseCase(private val repository: TripProductSearchRepository) {
    suspend operator fun invoke(keyword: String): Result<TripProductSearchResult> =
        repository.searchProductsByKeyword(keyword)
}
