package com.kkday.data.repository.companion

import com.kkday.library.common.repository.TripProductSearchRepository
import com.kkday.library.common.repository.TripProductSearchResult
import com.kkday.library.networking.core.api.BaseApiRepository
import com.kkday.library.networking.service.companion.ITripProductSearchApiService
import com.kkday.model.companion.TripProductSearchRequest
import org.koin.core.annotation.Single

@Single(binds = [TripProductSearchRepository::class])
class TripProductSearchRepositoryImpl(
    private val apiService: ITripProductSearchApiService
) : BaseApiRepository(), TripProductSearchRepository {

    override suspend fun searchProductsByKeyword(keyword: String): Result<TripProductSearchResult> {
        return try {
            val response = apiResultData(apiService.searchProducts(TripProductSearchRequest(keyword = keyword)))
            Result.success(
                TripProductSearchResult(
                    products = response.data?.prods.orEmpty(),
                    totalCount = response.metadata?.pagination?.totalCount ?: 0,
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
