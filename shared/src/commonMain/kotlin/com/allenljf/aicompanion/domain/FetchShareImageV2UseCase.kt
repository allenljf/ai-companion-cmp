package com.allenljf.aicompanion.domain

import com.allenljf.aicompanion.data.CompanionRepository
import com.allenljf.aicompanion.model.ShareImageV2Result

/**
 * 海報 hero 圖（T19）：completionUuid 需與同一輪 quiz-completions 用的那組相同。
 * 呼叫端注意——這支很慢（首次約 80 秒、同 uuid 重打約 35 秒），不可在主流程同步等待，
 * 見 AiCompanionViewModel 的背景觸發時機。
 */
class FetchShareImageV2UseCase(private val repository: CompanionRepository) {
    suspend operator fun invoke(completionUuid: String, partnerImageUrl: String? = null): Result<ShareImageV2Result> =
        repository.fetchShareImageV2(completionUuid, partnerImageUrl)
}
