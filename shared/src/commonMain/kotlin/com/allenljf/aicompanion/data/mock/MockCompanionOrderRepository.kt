package com.allenljf.aicompanion.data.mock

import com.allenljf.aicompanion.data.CompanionOrderRepository
import com.allenljf.aicompanion.model.TripOrderMaterial
import com.allenljf.aicompanion.model.TripProductMaterial

/** [CompanionOrderRepository] 的 mock 實作：材料層目前後端多半也是假資料，直接回 [MockData] 的固定清單。 */
class MockCompanionOrderRepository : CompanionOrderRepository {

    override suspend fun getUpcomingOrderMaterials(): Result<List<TripOrderMaterial>> {
        MockData.networkDelay()
        return Result.success(MockData.upcomingOrders)
    }

    override suspend fun getWishProductMaterials(): Result<List<TripProductMaterial>> {
        MockData.networkDelay()
        return Result.success(MockData.wishProducts)
    }

    override suspend fun getHistoryProductMaterials(): Result<List<TripProductMaterial>> {
        MockData.networkDelay()
        return Result.success(MockData.historyProducts)
    }
}
