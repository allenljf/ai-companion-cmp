package com.allenljf.aicompanion.data

import com.allenljf.aicompanion.model.TripOrderMaterial
import com.allenljf.aicompanion.model.TripProductMaterial

/**
 * AI 旅伴「帶材料開場」的資料來源：訂單／願望清單／瀏覽紀錄，各自整理成開場材料。
 * 獨立成一支輕量 repository，不擴充 CompanionRepository（避免混進不相關職責）。
 */
interface CompanionOrderRepository {
    // 即將出發的訂單，取最近的最多 3 筆
    suspend fun getUpcomingOrderMaterials(): Result<List<TripOrderMaterial>>

    // 願望清單商品，最多 20 筆（travel-summary-from-wish 上限）
    suspend fun getWishProductMaterials(): Result<List<TripProductMaterial>>

    // 瀏覽/購買紀錄商品，最多 20 筆（travel-summary-from-history 上限）
    suspend fun getHistoryProductMaterials(): Result<List<TripProductMaterial>>
}
