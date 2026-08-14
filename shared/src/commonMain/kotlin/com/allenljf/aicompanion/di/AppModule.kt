package com.allenljf.aicompanion.di

import com.allenljf.aicompanion.data.CompanionOrderRepository
import com.allenljf.aicompanion.data.CompanionRepository
import com.allenljf.aicompanion.data.LocalCompanionStore
import com.allenljf.aicompanion.data.TripProductSearchRepository
import com.allenljf.aicompanion.data.mock.MockCompanionOrderRepository
import com.allenljf.aicompanion.data.mock.MockCompanionRepository
import com.allenljf.aicompanion.data.mock.MockTripProductSearchRepository
import com.allenljf.aicompanion.domain.ClearLocalCompanionUseCase
import com.allenljf.aicompanion.domain.CompleteQuizUseCase
import com.allenljf.aicompanion.domain.FetchQuizUseCase
import com.allenljf.aicompanion.domain.FetchRecommendCityUseCase
import com.allenljf.aicompanion.domain.FetchSelfIntroductionUseCase
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
import com.allenljf.aicompanion.domain.SearchTripProductsUseCase
import com.allenljf.aicompanion.viewmodel.AiCompanionViewModel
import com.russhwolf.settings.Settings
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * 手寫取代原始碼的 @ComponentScan annotation module（reference/android-src/feature/di/
 * AiCompanionAnnotationModule.kt）——本專案未接 koin-annotations/ksp，改逐一手綁。
 *
 * AiCompanionViewModel 建構子 26 個參數，超過 viewModelOf 的 reified 上限（22），
 * 改用具名參數的 lambda 形式，同時避免 26 個 get() 因型別重複（多個 UseCase 共用
 * CompanionRepository）而互相對錯位。
 */
val appModule = module {
    // ---------- data：3 個 mock repository（介面型別綁定）+ 本地儲存 ----------
    single<CompanionRepository> { MockCompanionRepository(get()) }
    single<CompanionOrderRepository> { MockCompanionOrderRepository() }
    single<TripProductSearchRepository> { MockTripProductSearchRepository() }
    single { LocalCompanionStore(Settings()) }

    // ---------- domain：26 個 UseCase ----------
    factoryOf(::ClearLocalCompanionUseCase)
    factoryOf(::CompleteQuizUseCase)
    factoryOf(::FetchQuizUseCase)
    factoryOf(::FetchRecommendCityUseCase)
    factoryOf(::FetchSelfIntroductionUseCase)
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
    factoryOf(::SearchTripProductsUseCase)

    // ---------- viewmodel ----------
    viewModel {
        AiCompanionViewModel(
            getAiPartnerUseCase = get(),
            fetchQuizUseCase = get(),
            completeQuizUseCase = get(),
            fetchSelfIntroductionUseCase = get(),
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
            searchTripProductsUseCase = get(),
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
