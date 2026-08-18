package com.allenljf.aicompanion.di

import com.allenljf.aicompanion.data.CompanionOrderRepository
import com.allenljf.aicompanion.data.CompanionRepository
import com.allenljf.aicompanion.data.LocalCompanionStore
import com.allenljf.aicompanion.data.mock.MockCompanionOrderRepository
import com.allenljf.aicompanion.data.mock.MockCompanionRepository
import com.allenljf.aicompanion.data.remote.CompanionApiClient
import com.allenljf.aicompanion.data.remote.RemoteCompanionOrderRepository
import com.allenljf.aicompanion.data.remote.RemoteCompanionRepository
import com.allenljf.aicompanion.domain.ClearLocalCompanionUseCase
import com.allenljf.aicompanion.domain.CompleteQuizUseCase
import com.allenljf.aicompanion.domain.FetchQuizUseCase
import com.allenljf.aicompanion.domain.FetchRecommendCityUseCase
import com.allenljf.aicompanion.domain.FetchSelfIntroductionUseCase
import com.allenljf.aicompanion.domain.FetchShareImageV2UseCase
import com.allenljf.aicompanion.domain.FetchTravelGuideUseCase
import com.allenljf.aicompanion.domain.FetchTravelReviseUseCase
import com.allenljf.aicompanion.domain.FetchTravelSummaryFromHistoryUseCase
import com.allenljf.aicompanion.domain.FetchTravelSummaryFromOrdersUseCase
import com.allenljf.aicompanion.domain.FetchTravelSummaryFromWishUseCase
import com.allenljf.aicompanion.domain.FetchTravelSummaryUseCase
import com.allenljf.aicompanion.domain.GetAiPartnerUseCase
import com.allenljf.aicompanion.domain.GetHistoryProductMaterialsUseCase
import com.allenljf.aicompanion.domain.GetLocalCompanionUseCase
import com.allenljf.aicompanion.domain.GetQuizGalleryUseCase
import com.allenljf.aicompanion.domain.GetQuizHistoryUseCase
import com.allenljf.aicompanion.domain.GetSavedTripsUseCase
import com.allenljf.aicompanion.domain.GetShownQuestionCountsUseCase
import com.allenljf.aicompanion.domain.GetUpcomingOrderMaterialsUseCase
import com.allenljf.aicompanion.domain.GetWishProductMaterialsUseCase
import com.allenljf.aicompanion.domain.IsChineseLanguageUseCase
import com.allenljf.aicompanion.domain.SaveLocalCompanionUseCase
import com.allenljf.aicompanion.domain.SaveQuizHistoryUseCase
import com.allenljf.aicompanion.domain.SaveSavedTripsUseCase
import com.allenljf.aicompanion.domain.SaveShownQuestionCountsUseCase
import com.allenljf.aicompanion.viewmodel.AiCompanionViewModel
import com.russhwolf.settings.Settings
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Handwritten Koin module. AiCompanionViewModel has more constructor parameters than
 * viewModelOf supports, so this uses named arguments to prevent dependency ordering mistakes.
 *
 * AiCompanionViewModel 建構子 26 個參數，超過 viewModelOf 的 reified 上限（22），
 * 改用具名參數的 lambda 形式，同時避免 26 個 get() 因型別重複（多個 UseCase 共用
 * CompanionRepository）而互相對錯位。
 */
// T17 真後端接入：單一開關決定 CompanionRepository/CompanionOrderRepository 走真後端還是 mock
// Set false to run the demo against deterministic local data.
private const val useRemoteApi = true

val appModule = module {
    // ---------- data：3 個 repository（介面型別綁定，依 useRemoteApi 切換）+ 本地儲存 ----------
    single { CompanionApiClient() }
    single<CompanionRepository> {
        if (useRemoteApi) RemoteCompanionRepository(get(), get()) else MockCompanionRepository(get())
    }
    single<CompanionOrderRepository> {
        if (useRemoteApi) RemoteCompanionOrderRepository(get()) else MockCompanionOrderRepository()
    }
    single { LocalCompanionStore(Settings()) }

    // ---------- domain：26 個 UseCase ----------
    factoryOf(::ClearLocalCompanionUseCase)
    factoryOf(::CompleteQuizUseCase)
    factoryOf(::FetchQuizUseCase)
    factoryOf(::FetchRecommendCityUseCase)
    factoryOf(::FetchSelfIntroductionUseCase)
    factoryOf(::FetchShareImageV2UseCase)
    factoryOf(::FetchTravelGuideUseCase)
    factoryOf(::FetchTravelReviseUseCase)
    factoryOf(::FetchTravelSummaryFromHistoryUseCase)
    factoryOf(::FetchTravelSummaryFromOrdersUseCase)
    factoryOf(::FetchTravelSummaryFromWishUseCase)
    factoryOf(::FetchTravelSummaryUseCase)
    factoryOf(::GetAiPartnerUseCase)
    factoryOf(::GetHistoryProductMaterialsUseCase)
    factoryOf(::GetLocalCompanionUseCase)
    factoryOf(::GetQuizGalleryUseCase)
    factoryOf(::GetQuizHistoryUseCase)
    factoryOf(::GetSavedTripsUseCase)
    factoryOf(::GetShownQuestionCountsUseCase)
    factoryOf(::GetUpcomingOrderMaterialsUseCase)
    factoryOf(::GetWishProductMaterialsUseCase)
    factoryOf(::IsChineseLanguageUseCase)
    factoryOf(::SaveLocalCompanionUseCase)
    factoryOf(::SaveQuizHistoryUseCase)
    factoryOf(::SaveSavedTripsUseCase)
    factoryOf(::SaveShownQuestionCountsUseCase)

    // ---------- viewmodel ----------
    viewModel {
        AiCompanionViewModel(
            getAiPartnerUseCase = get(),
            fetchQuizUseCase = get(),
            completeQuizUseCase = get(),
            fetchSelfIntroductionUseCase = get(),
            fetchShareImageV2UseCase = get(),
            getQuizGalleryUseCase = get(),
            saveLocalCompanionUseCase = get(),
            getLocalCompanionUseCase = get(),
            clearLocalCompanionUseCase = get(),
            getShownQuestionCountsUseCase = get(),
            saveShownQuestionCountsUseCase = get(),
            getQuizHistoryUseCase = get(),
            saveQuizHistoryUseCase = get(),
            isChineseLanguageUseCase = get(),
            fetchTravelSummaryUseCase = get(),
            fetchRecommendCityUseCase = get(),
            fetchTravelGuideUseCase = get(),
            fetchTravelReviseUseCase = get(),
            getSavedTripsUseCase = get(),
            saveSavedTripsUseCase = get(),
            getUpcomingOrderMaterialsUseCase = get(),
            fetchTravelSummaryFromOrdersUseCase = get(),
            getWishProductMaterialsUseCase = get(),
            fetchTravelSummaryFromWishUseCase = get(),
            getHistoryProductMaterialsUseCase = get(),
            fetchTravelSummaryFromHistoryUseCase = get(),
        )
    }
}
