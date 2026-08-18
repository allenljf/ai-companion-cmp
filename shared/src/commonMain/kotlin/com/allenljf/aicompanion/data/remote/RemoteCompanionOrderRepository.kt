package com.allenljf.aicompanion.data.remote

import com.allenljf.aicompanion.data.CompanionOrderRepository
import com.allenljf.aicompanion.model.TripOrderMaterial
import com.allenljf.aicompanion.model.TripProductMaterial
import com.allenljf.aicompanion.model.toMaterial

/**
 * [CompanionOrderRepository] 的真後端實作：orders/wish_list/history 三支 GET，無 request body。
 * orders 依出發日排序取最近 3 筆；wish_list/history 過濾名稱或編號空白的髒資料後取前 20 筆。
 */
class RemoteCompanionOrderRepository(
    private val client: CompanionApiClient
) : CompanionOrderRepository {

    override suspend fun getUpcomingOrderMaterials(): Result<List<TripOrderMaterial>> = runCatching {
        client.getOrders().unwrap().orders
            .filter { it.goDt != null }
            .sortedBy { it.goDt }
            .take(MAX_ORDER_MATERIAL_COUNT)
            .map { it.toMaterial() }
    }

    override suspend fun getWishProductMaterials(): Result<List<TripProductMaterial>> = runCatching {
        extractProductMaterials(client.getWishList().unwrap().prods.map { it.toMaterial() })
    }

    override suspend fun getHistoryProductMaterials(): Result<List<TripProductMaterial>> = runCatching {
        extractProductMaterials(client.getHistory().unwrap().prods.map { it.toMaterial() })
    }

    // 名稱或編號空白的過濾（prod_id/prod_name 皆為必填，空值會被後端 400）
    private fun extractProductMaterials(materials: List<TripProductMaterial>): List<TripProductMaterial> =
        materials.filter { it.prodName.isNotBlank() && it.prodId.isNotBlank() }.take(MAX_PRODUCT_MATERIAL_COUNT)

    private companion object {
        // 「距離今天最接近的前三筆」
        const val MAX_ORDER_MATERIAL_COUNT = 3

        // travel-summary-from-wish／-from-history 的 products 上限（超過會被 400 擋掉）
        const val MAX_PRODUCT_MATERIAL_COUNT = 20
    }
}
