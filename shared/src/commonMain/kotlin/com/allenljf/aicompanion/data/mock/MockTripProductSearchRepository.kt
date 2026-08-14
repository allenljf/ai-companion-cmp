package com.allenljf.aicompanion.data.mock

import com.allenljf.aicompanion.data.TripProductSearchRepository
import com.allenljf.aicompanion.model.TripProductSearchResult

/**
 * [TripProductSearchRepository] 的 mock 實作：用關鍵字比對固定的 catalog（見 [MockData.searchCatalog]）。
 * 找不到符合的商品時退回整個 catalog 當作「相關建議」，避免行程頁景點卡因為關鍵字沒對到就整卡消失
 * （這只是錦上添花功能，找不到就不顯示卡片，但 demo 展示上完全沒有商品比較不好看）。
 */
class MockTripProductSearchRepository : TripProductSearchRepository {

    private companion object {
        const val RETURN_LIMIT = 4
    }

    override suspend fun searchProductsByKeyword(keyword: String): Result<TripProductSearchResult> {
        MockData.networkDelay()
        val matched = MockData.searchCatalog.filter { keyword.isNotBlank() && it.name.contains(keyword, ignoreCase = true) }
        val effective = matched.ifEmpty { MockData.searchCatalog }
        return Result.success(
            TripProductSearchResult(
                products = effective.take(RETURN_LIMIT),
                // 真實情境下 total_count 來自 metadata.pagination；mock 用符合的完整筆數模擬「還有 N 項」
                totalCount = effective.size,
            )
        )
    }
}
