package com.kkday.domain.companion

import com.kkday.library.common.repository.CompanionRepository
import com.kkday.model.companion.ShareImageV2Result
import org.koin.core.annotation.Factory

@Factory
class FetchShareImageV2UseCase(
    private val repository: CompanionRepository
) {
    suspend operator fun invoke(completionUuid: String, partnerImageUrl: String?): Result<ShareImageV2Result> =
        repository.fetchShareImageV2(completionUuid, partnerImageUrl)
}
