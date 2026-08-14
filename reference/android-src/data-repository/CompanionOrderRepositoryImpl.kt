package com.kkday.data.repository.companion

import com.kkday.library.common.repository.CompanionOrderRepository
import com.kkday.library.networking.core.api.BaseApiRepository
import com.kkday.library.networking.service.companion.ICompanionApiService
import com.kkday.model.companion.TripOrderMaterial
import com.kkday.model.companion.TripProductMaterial
import com.kkday.model.companion.toMaterial
import org.koin.core.annotation.Single
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 走目前 build 環境正常登入態的 `v3/companion/orders`（demo：後端目前回傳跟 v2.2/orders 一樣
 * 形狀的 mock 資料，不需帶 body），不再需要固定打某環境／某測試帳號。
 */
@Single(binds = [CompanionOrderRepository::class])
class CompanionOrderRepositoryImpl(
    private val apiService: ICompanionApiService
) : BaseApiRepository(), CompanionOrderRepository {

    override suspend fun getUpcomingOrderMaterials(): Result<List<TripOrderMaterial>> {
        return try {
            val response = apiResultData(apiService.getUpcomingOrders())
            val materials = response.data?.orders.orEmpty()
                .filter { it.goDate != null }
                .sortedBy { it.goDate }
                .take(MAX_ORDER_MATERIAL_COUNT)
                .map {
                    TripOrderMaterial(
                        prodName = it.productName,
                        packageName = it.packageName,
                        destinationName = it.destination.destinations.firstOrNull()?.name.orEmpty(),
                        oid = it.id,
                        goDt = it.goDate?.let(::formatGoDate).orEmpty()
                    )
                }
            Result.success(materials)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getWishProductMaterials(): Result<List<TripProductMaterial>> {
        return try {
            val response = apiResultData(apiService.getWishList())
            Result.success(extractProductMaterials(response.data?.prods.orEmpty().map { it.toMaterial() }))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getHistoryProductMaterials(): Result<List<TripProductMaterial>> {
        return try {
            val response = apiResultData(apiService.getBrowseHistory())
            Result.success(extractProductMaterials(response.data?.prods.orEmpty().map { it.toMaterial() }))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 名稱或編號空白的過濾（prod_id/prod_name 皆為必填，空值會被後端 400）；假資料無時間戳，依原始順序取前 20 筆
    private fun extractProductMaterials(materials: List<TripProductMaterial>): List<TripProductMaterial> =
        materials.filter { it.prodName.isNotBlank() && it.prodId.isNotBlank() }.take(MAX_PRODUCT_MATERIAL_COUNT)

    // lst_dt_go 為 epoch timestamp → travel-guide 要的 yyyy-MM-dd；秒/毫秒兩種單位都相容
    private fun formatGoDate(epoch: Long): String {
        val millis = if (epoch < EPOCH_MILLIS_THRESHOLD) epoch * 1000 else epoch
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(millis))
    }

    companion object {
        // 「距離今天最接近的前三筆」
        private const val MAX_ORDER_MATERIAL_COUNT = 3

        // epoch 若小於此值視為「秒」單位（10^12 毫秒 ≈ 2001 年，訂單出發日必大於此）
        private const val EPOCH_MILLIS_THRESHOLD = 1_000_000_000_000L

        // travel-summary-from-wish／-from-history 的 products 上限（超過會被 400 擋掉）
        private const val MAX_PRODUCT_MATERIAL_COUNT = 20
    }
}
