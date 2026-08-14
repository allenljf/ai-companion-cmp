package com.kkday.domain.companion

import com.kkday.library.common.repository.TripProductSearchRepository
import com.kkday.library.common.repository.TripProductSearchResult
import org.koin.core.annotation.Factory

/** 行程頁景點卡背景搜尋：以景點名稱為關鍵字打既有商品搜尋 API，找到商品才顯示卡片。 */
@Factory
class SearchTripProductsUseCase(private val repository: TripProductSearchRepository) {
    suspend operator fun invoke(keyword: String): Result<TripProductSearchResult> =
        repository.searchProductsByKeyword(keyword)
}
