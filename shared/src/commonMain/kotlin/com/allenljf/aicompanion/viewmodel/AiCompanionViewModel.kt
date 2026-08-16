package com.allenljf.aicompanion.viewmodel

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
import com.allenljf.aicompanion.domain.SearchTripProductsUseCase
import com.allenljf.aicompanion.domain.GetAiPartnerUseCase
import com.allenljf.aicompanion.domain.GetLocalCompanionUseCase
import com.allenljf.aicompanion.domain.GetQuizGalleryUseCase
import com.allenljf.aicompanion.domain.GetQuizHistoryUseCase
import com.allenljf.aicompanion.domain.GetHistoryProductMaterialsUseCase
import com.allenljf.aicompanion.domain.GetSavedTripsUseCase
import com.allenljf.aicompanion.domain.GetUpcomingOrderMaterialsUseCase
import com.allenljf.aicompanion.domain.GetWishProductMaterialsUseCase
import com.allenljf.aicompanion.domain.GetShownQuestionCountsUseCase
import com.allenljf.aicompanion.domain.IsChineseLanguageUseCase
import com.allenljf.aicompanion.domain.SaveLocalCompanionUseCase
import com.allenljf.aicompanion.domain.SaveQuizHistoryUseCase
import com.allenljf.aicompanion.domain.SaveSavedTripsUseCase
import com.allenljf.aicompanion.domain.SaveShownQuestionCountsUseCase
import com.allenljf.aicompanion.model.CompanionAppearanceOption
import com.allenljf.aicompanion.model.CompanionProfile
import com.allenljf.aicompanion.model.CompanionSnapshot
import com.allenljf.aicompanion.model.CompanionTraitOption
import com.allenljf.aicompanion.model.CityChatMessage
import com.allenljf.aicompanion.model.QuizCompletionResult
import com.allenljf.aicompanion.model.QuizHistoryRecord
import com.allenljf.aicompanion.model.SavedTripRecord
import com.allenljf.aicompanion.model.TravelDestinationOption
import com.allenljf.aicompanion.model.TravelGuideDay
import com.allenljf.aicompanion.model.TravelPlanValidationException
import com.allenljf.aicompanion.model.TravelSummaryEntryType
import com.allenljf.aicompanion.model.TravelSummaryFromProductsResult
import com.allenljf.aicompanion.model.TravelSummarySourceType
import com.allenljf.aicompanion.model.TripOrderMaterial
import com.allenljf.aicompanion.model.TripProductMaterial
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// java.util.UUID／System.currentTimeMillis() 為 JVM-only，commonMain 需雙平台可編譯的等價 API
@OptIn(ExperimentalUuidApi::class)
private fun newCompletionUuid(): String = Uuid.random().toString()

@OptIn(ExperimentalTime::class)
private fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()

/**
 * AI 旅伴測驗流程的狀態機。聚焦工程骨架：
 * - completion_uuid 生命週期：每次真正送出 quiz-completions（submitQuiz）都會重新生成一組，
 *   同一輪內 share-image-v2 沿用同一組 uuid
 * - quiz-completions 軟失敗判定（fail_reason）
 * - share-image-v2（T19，簡化版，不接輪詢）：實測後端同步回 hero 圖，不是 pending 起輪詢那套；
 *   但很慢（首次約 80 秒），分析成功當下就在背景另開一個 coroutine 觸發，不擋結果頁文字內容顯示，
 *   圖 ready 後才補上（見 fetchShareImageV2InBackground）
 */
class AiCompanionViewModel(
    private val getAiPartnerUseCase: GetAiPartnerUseCase,
    private val fetchQuizUseCase: FetchQuizUseCase,
    private val completeQuizUseCase: CompleteQuizUseCase,
    private val fetchSelfIntroductionUseCase: FetchSelfIntroductionUseCase,
    private val fetchShareImageV2UseCase: FetchShareImageV2UseCase,
    private val getQuizGalleryUseCase: GetQuizGalleryUseCase,
    private val saveLocalCompanionUseCase: SaveLocalCompanionUseCase,
    private val getLocalCompanionUseCase: GetLocalCompanionUseCase,
    private val clearLocalCompanionUseCase: ClearLocalCompanionUseCase,
    private val getShownQuestionCountsUseCase: GetShownQuestionCountsUseCase,
    private val saveShownQuestionCountsUseCase: SaveShownQuestionCountsUseCase,
    private val getQuizHistoryUseCase: GetQuizHistoryUseCase,
    private val saveQuizHistoryUseCase: SaveQuizHistoryUseCase,
    private val isChineseLanguageUseCase: IsChineseLanguageUseCase,
    private val fetchTravelSummaryUseCase: FetchTravelSummaryUseCase,
    private val fetchRecommendCityUseCase: FetchRecommendCityUseCase,
    private val fetchTravelGuideUseCase: FetchTravelGuideUseCase,
    private val fetchTravelReviseUseCase: FetchTravelReviseUseCase,
    private val searchTripProductsUseCase: SearchTripProductsUseCase,
    private val getSavedTripsUseCase: GetSavedTripsUseCase,
    private val saveSavedTripsUseCase: SaveSavedTripsUseCase,
    private val getUpcomingOrderMaterialsUseCase: GetUpcomingOrderMaterialsUseCase,
    private val fetchTravelSummaryFromOrdersUseCase: FetchTravelSummaryFromOrdersUseCase,
    private val getWishProductMaterialsUseCase: GetWishProductMaterialsUseCase,
    private val fetchTravelSummaryFromWishUseCase: FetchTravelSummaryFromWishUseCase,
    private val getHistoryProductMaterialsUseCase: GetHistoryProductMaterialsUseCase,
    private val fetchTravelSummaryFromHistoryUseCase: FetchTravelSummaryFromHistoryUseCase
) : ViewModel() {

    // null = 尚未讀取完成；true/false = 本地是否已有旅伴（決定起始頁）
    private val _hasLocalCompanion = MutableStateFlow<Boolean?>(null)
    val hasLocalCompanion: StateFlow<Boolean?> = _hasLocalCompanion.asStateFlow()

    private val _partnerState = MutableStateFlow<PartnerState>(PartnerState.Idle)
    val partnerState: StateFlow<PartnerState> = _partnerState.asStateFlow()

    private val _creationState = MutableStateFlow(CompanionCreationState())
    val creationState: StateFlow<CompanionCreationState> = _creationState.asStateFlow()

    private val _quizState = MutableStateFlow<QuizState>(QuizState.Idle)
    val quizState: StateFlow<QuizState> = _quizState.asStateFlow()

    private val _analysisState = MutableStateFlow<AnalysisState>(AnalysisState.Idle)
    val analysisState: StateFlow<AnalysisState> = _analysisState.asStateFlow()

    private val _shareImageV2State = MutableStateFlow<ShareImageV2State>(ShareImageV2State.Idle)
    val shareImageV2State: StateFlow<ShareImageV2State> = _shareImageV2State.asStateFlow()

    private val _introductionState = MutableStateFlow<IntroductionState>(IntroductionState.Idle)
    val introductionState: StateFlow<IntroductionState> = _introductionState.asStateFlow()

    private val _quizGalleryState = MutableStateFlow<QuizGalleryState>(QuizGalleryState.Idle)
    val quizGalleryState: StateFlow<QuizGalleryState> = _quizGalleryState.asStateFlow()

    // 測驗結果歷史（回顧頁用；最新在前）
    private val _quizHistory = MutableStateFlow<List<QuizHistoryRecord>>(emptyList())
    val quizHistory: StateFlow<List<QuizHistoryRecord>> = _quizHistory.asStateFlow()

    var completionUuid: String = newCompletionUuid()
        private set

    private var personality: List<String> = emptyList()
    private var speechStyle: String = ""
    private var companionName: String? = null

    // 本地已存的自我介紹（buildCompanionProfile 預設沿用，避免其他存檔動作把它覆蓋成空字串）
    private var lastKnownIntroduction: String = ""

    // 答題紀錄（question.id → 選中的 option.id）：跨頁保留、回上一頁可依 key 覆寫；重新答題清空
    private val _quizAnswers = MutableStateFlow<Map<String, String>>(emptyMap())
    val quizAnswers: StateFlow<Map<String, String>> = _quizAnswers.asStateFlow()

    // ---------- 建立旅伴維度選擇 ----------

    fun selectGender(option: CompanionAppearanceOption) {
        _creationState.update { it.copy(selectedGender = option).withResolvedAvatar() }
    }

    fun selectOutfit(option: CompanionAppearanceOption) {
        _creationState.update { it.copy(selectedOutfit = option).withResolvedAvatar() }
    }

    fun selectHairStyle(option: CompanionAppearanceOption) {
        _creationState.update { it.copy(selectedHairStyle = option).withResolvedAvatar() }
    }

    fun selectHairColor(option: CompanionAppearanceOption) {
        _creationState.update { it.copy(selectedHairColor = option).withResolvedAvatar() }
    }

    /** 外觀四維度選齊時以 {gender}_{outfit}_{hair_style}_{hair_color} 組 key 解析頭像 URL。 */
    private fun CompanionCreationState.withResolvedAvatar(): CompanionCreationState {
        val partner = (_partnerState.value as? PartnerState.Loaded)?.partner
            ?: return copy(avatarUrl = "")
        return copy(
            avatarUrl = partner.avatarUrl(
                gender = selectedGender?.tag,
                outfit = selectedOutfit?.tag,
                hairStyle = selectedHairStyle?.tag,
                hairColor = selectedHairColor?.tag,
            )
        )
    }

    /** 個性單選：點已選的取消，點其他的直接取代。 */
    fun togglePersonality(option: CompanionTraitOption) {
        _creationState.update { state ->
            if (state.selectedPersonality.any { it.tag == option.tag }) {
                state.copy(selectedPersonality = emptyList())
            } else {
                state.copy(selectedPersonality = listOf(option))
            }
        }
    }

    fun selectSpeechStyle(option: CompanionTraitOption) {
        _creationState.update { it.copy(selectedSpeechStyle = option) }
    }

    fun updateCompanionName(name: String) {
        if (name.length <= CompanionCreationState.NAME_MAX_LENGTH) {
            _creationState.update { it.copy(companionName = name) }
        }
    }

    fun confirmCompanionCreation() {
        val state = _creationState.value
        personality = state.selectedPersonality.map { it.tag }
        speechStyle = state.selectedSpeechStyle?.tag.orEmpty()
        companionName = state.companionName.takeIf { it.isNotBlank() }
        viewModelScope.launch { saveLocalCompanionUseCase(buildCompanionProfile()) }
        // 旅伴設定完成即開始生成自我介紹（LLM 要時間，先打，自介頁進場時盡量已就緒）
        fetchSelfIntroduction()
    }

    /** 依目前 creationState 組出完整本地設定檔；[introduction] 留空則沿用本地已存的自我介紹，避免覆蓋成空字串。 */
    private fun buildCompanionProfile(introduction: String = lastKnownIntroduction): CompanionProfile {
        val state = _creationState.value
        return CompanionProfile(
            name = state.companionName,
            personality = state.selectedPersonality.map { it.tag },
            personalityLabels = state.selectedPersonality.map { it.label.ifBlank { it.tag } },
            speechStyle = state.selectedSpeechStyle?.tag.orEmpty(),
            speechStyleLabel = state.selectedSpeechStyle?.label.orEmpty().ifBlank { state.selectedSpeechStyle?.tag.orEmpty() },
            gender = state.selectedGender?.tag.orEmpty(),
            outfit = state.selectedOutfit?.tag.orEmpty(),
            hairStyle = state.selectedHairStyle?.tag.orEmpty(),
            hairColor = state.selectedHairColor?.tag.orEmpty(),
            avatarUrl = state.avatarUrl,
            introduction = introduction,
        )
    }

    /** 打 self-introduction 取得旅伴自我介紹；硬失敗由 UI 以本地固定文案 fallback。成功即補存回本地設定檔。 */
    fun fetchSelfIntroduction() {
        val state = _creationState.value
        viewModelScope.launch {
            _introductionState.value = IntroductionState.Loading
            fetchSelfIntroductionUseCase(
                companionName = state.companionName.ifBlank { DEFAULT_COMPANION_NAME },
                personality = state.selectedPersonality.map { it.tag },
                speechStyle = state.selectedSpeechStyle?.tag.orEmpty(),
                gender = state.selectedGender?.tag.orEmpty(),
            ).fold(
                onSuccess = { result ->
                    // 軟失敗時 introduction 為後端固定文案，一樣直接顯示
                    _introductionState.value = IntroductionState.Loaded(
                        introduction = result.introduction,
                        isAiGenerated = result.isAiGenerated,
                    )
                    lastKnownIntroduction = result.introduction
                    saveLocalCompanionUseCase(buildCompanionProfile(result.introduction))
                },
                onFailure = { _introductionState.value = IntroductionState.Error }
            )
        }
    }

    /** 重新製作旅伴：清空本地設定檔並重置建立流程狀態。 */
    fun recreateCompanion() {
        _creationState.value = CompanionCreationState()
        _hasLocalCompanion.value = false
        _introductionState.value = IntroductionState.Idle
        viewModelScope.launch { clearLocalCompanionUseCase() }
    }

    /** 讀取本地旅伴設定檔（依 memberUuid）：有→hydrate 名字/頭像/測驗參數，供入口決定起始頁。 */
    fun loadLocalCompanion() {
        viewModelScope.launch {
            val profile = getLocalCompanionUseCase().getOrNull()
            if (profile != null) {
                personality = profile.personality
                speechStyle = profile.speechStyle
                companionName = profile.name.takeIf { it.isNotBlank() }
                _creationState.update {
                    it.copy(
                        selectedGender = profile.gender.takeIf { tag -> tag.isNotBlank() }
                            ?.let { tag -> CompanionAppearanceOption(tag = tag, label = tag) },
                        selectedOutfit = profile.outfit.takeIf { tag -> tag.isNotBlank() }
                            ?.let { tag -> CompanionAppearanceOption(tag = tag, label = tag) },
                        selectedHairStyle = profile.hairStyle.takeIf { tag -> tag.isNotBlank() }
                            ?.let { tag -> CompanionAppearanceOption(tag = tag, label = tag) },
                        selectedHairColor = profile.hairColor.takeIf { tag -> tag.isNotBlank() }
                            ?.let { tag -> CompanionAppearanceOption(tag = tag, label = tag) },
                        selectedPersonality = profile.personality.mapIndexed { index, tag ->
                            CompanionTraitOption(
                                tag = tag,
                                label = profile.personalityLabels.getOrNull(index).orEmpty().ifBlank { tag },
                            )
                        },
                        selectedSpeechStyle = profile.speechStyle.takeIf { tag -> tag.isNotBlank() }?.let { tag ->
                            CompanionTraitOption(
                                tag = tag,
                                label = profile.speechStyleLabel.ifBlank { tag },
                            )
                        },
                        companionName = profile.name,
                        avatarUrl = profile.avatarUrl,
                    )
                }
                lastKnownIntroduction = profile.introduction
                if (profile.introduction.isNotBlank()) {
                    // 本地已有自我介紹：先顯示，不用一定要重打 API 才有內容（首頁/頭像 bottom sheet 可直接用）
                    _introductionState.value = IntroductionState.Loaded(
                        introduction = profile.introduction,
                        isAiGenerated = true,
                    )
                } else {
                    // 舊資料（此功能上線前建立的旅伴）或曾存檔失敗，本地沒有自我介紹：補打一次 API 並存回本地
                    fetchSelfIntroduction()
                }
            }
            _hasLocalCompanion.value = profile != null
        }
    }

    /** 隨機外觀：四個決定長相的維度各亂數挑一個（選項已在本地），個性/風格/名字不動。 */
    fun randomizeAppearance() {
        val partner = (_partnerState.value as? PartnerState.Loaded)?.partner ?: return
        _creationState.update {
            it.copy(
                selectedGender = partner.gender.randomOrNull(),
                selectedOutfit = partner.outfit.randomOrNull(),
                selectedHairStyle = partner.hairStyle.randomOrNull(),
                selectedHairColor = partner.hairColor.randomOrNull(),
            ).withResolvedAvatar()
        }
    }

    private fun <T> List<T>.randomOrNull(): T? = if (isEmpty()) null else random()

    fun configureCompanion(personality: List<String>, speechStyle: String, companionName: String?) {
        this.personality = personality
        this.speechStyle = speechStyle
        this.companionName = companionName
    }

    fun loadAiPartner() {
        viewModelScope.launch {
            _partnerState.value = PartnerState.Loading
            getAiPartnerUseCase().fold(
                onSuccess = { partner ->
                    _partnerState.value =
                        if (partner.isEmpty) PartnerState.Empty else PartnerState.Loaded(partner)
                },
                onFailure = { _partnerState.value = PartnerState.Error }
            )
        }
    }

    /** 讀取「其他人做過的測驗結果」清單（查看社群頁），純讀取、不觸發 LLM。 */
    fun loadQuizGallery() {
        viewModelScope.launch {
            _quizGalleryState.value = QuizGalleryState.Loading
            getQuizGalleryUseCase().fold(
                onSuccess = { gallery -> _quizGalleryState.value = QuizGalleryState.Loaded(gallery) },
                onFailure = { _quizGalleryState.value = QuizGalleryState.Error }
            )
        }
    }

    /**
     * 取題（可重覆呼叫換題）。shown_question_counts 以 DataStore 持久化（依 memberUuid）：
     * 送出前先讀取，成功後對本輪題目 id（"{dimension_id}-{index}"）逐一 +1 再存回。
     */
    fun fetchQuiz() {
        viewModelScope.launch {
            _quizState.value = QuizState.Loading
            val shownCounts = getShownQuestionCountsUseCase().getOrNull().orEmpty()
            fetchQuizUseCase(shownCounts, personality, speechStyle).fold(
                onSuccess = { quiz ->
                    val updated = shownCounts.toMutableMap()
                    quiz.questions.forEach { q ->
                        updated[q.id] = (updated[q.id] ?: 0) + 1
                    }
                    saveShownQuestionCountsUseCase(updated)
                    _quizAnswers.value = emptyMap() // 新一輪題組，舊答案作廢
                    _quizState.value = QuizState.Loaded(quiz)
                },
                onFailure = { _quizState.value = QuizState.Error }
            )
        }
    }

    /** 記錄／覆寫某一題的作答（回上一頁修改時以 questionId 為 key 直接替換）。 */
    fun answerQuestion(questionId: String, optionId: String) {
        _quizAnswers.update { it + (questionId to optionId) }
    }

    /**
     * 依作答紀錄組出 selected_tags（照題目順序取 option 的 tag.id）後提交分析。
     * shown_cities 取自本地測驗歷史的目的地名稱，依目前使用者語系只挑一個（Cn 或 En），避免重複推薦。
     */
    fun submitQuiz() {
        val quiz = (_quizState.value as? QuizState.Loaded)?.quiz ?: return
        val answers = _quizAnswers.value
        val selectedTags = quiz.questions.mapNotNull { question ->
            val optionId = answers[question.id]
            question.options.firstOrNull { it.id == optionId }?.tagId
        }.filter { it.isNotEmpty() }
        viewModelScope.launch {
            _analysisState.value = AnalysisState.Analyzing
            val isChinese = isChineseLanguageUseCase()
            val shownCities = getQuizHistoryUseCase().getOrNull().orEmpty()
                .map { record ->
                    if (isChinese) {
                        record.result.destinationCn.ifBlank { record.result.destinationEn }
                    } else {
                        record.result.destinationEn.ifBlank { record.result.destinationCn }
                    }
                }
                .filter { it.isNotBlank() }
                .distinct()
            submitQuiz(selectedTags, shownCities)
        }
    }

    /** 提交答案取得文字分析；成功即在背景觸發 share-image-v2 產圖（見 fetchShareImageV2InBackground）。 */
    fun submitQuiz(selectedTags: List<String>, shownCities: List<String> = emptyList()) {
        // 每次真正送出 quiz-completions 都要用新的一組 uuid，不能沿用上一輪（否則 share-image
        // 可能命中上一輪已產好的圖，秒回同一張海報）；uuid 生命週期從這裡開始，share-image 沿用同一組直到本輪結束
        completionUuid = newCompletionUuid()
        _shareImageV2State.value = ShareImageV2State.Idle // 新一輪重置，避免殘留上一輪的海報狀態
        viewModelScope.launch {
            _analysisState.value = AnalysisState.Analyzing
            completeQuizWithSoftFailRetry(selectedTags, shownCities).fold(
                onSuccess = { result ->
                    if (result.isAnalysisSuccess) {
                        _analysisState.value = AnalysisState.Success(result)
                        appendQuizHistory(result)
                        fetchShareImageV2InBackground(completionUuid)
                    } else {
                        // fail_reason 仍有值（重試後依然軟失敗）→ 才真的顯示失敗，提供手動重試
                        _analysisState.value = AnalysisState.SoftFailed
                    }
                },
                onFailure = { _analysisState.value = AnalysisState.Error }
            )
        }
    }

    /**
     * share-image-v2 實測很慢（首次約 80 秒、同 uuid 重打約 35 秒，後端未快取），不能放在
     * submitQuiz 的主流程裡等——另開一個 coroutine，讓結果頁文字內容先顯示，圖 ready 後才補上。
     * 失敗（含 fail_reason 軟失敗）只影響這個狀態本身，不影響其餘已顯示的分析結果（軟失敗契約）。
     */
    private fun fetchShareImageV2InBackground(uuid: String) {
        viewModelScope.launch {
            _shareImageV2State.value = ShareImageV2State.Loading
            fetchShareImageV2UseCase(completionUuid = uuid).fold(
                onSuccess = { result ->
                    if (result.isReady) {
                        _shareImageV2State.value = ShareImageV2State.Ready(result)
                        backfillQuizHistoryHeroUrl(uuid, result.heroUrl.orEmpty())
                    } else {
                        _shareImageV2State.value = ShareImageV2State.Failed
                    }
                },
                onFailure = { _shareImageV2State.value = ShareImageV2State.Failed }
            )
        }
    }

    /** 產圖成功後回填對應那筆歷史紀錄的 heroImageUrl，回顧列表/詳情頁才不用重打一次 35 秒的 API。 */
    private suspend fun backfillQuizHistoryHeroUrl(uuid: String, heroUrl: String) {
        val records = getQuizHistoryUseCase().getOrNull().orEmpty()
        val updated = records.map { record ->
            if (record.completionUuid == uuid) record.copy(heroImageUrl = heroUrl) else record
        }
        saveQuizHistoryUseCase(updated)
        _quizHistory.value = updated
    }

    /**
     * quiz-completions 偶發軟失敗（`fail_reason` 有值，最常見是 LLM 回應無法解析為預期 JSON）在原始
     * completion_uuid 換一組新的重打即可大機率成功，屬於瞬時問題。這裡在回報失敗給使用者前，
     * 於同一次 submitQuiz 內先自動重試最多 [QUIZ_COMPLETION_MAX_ATTEMPTS] 次（換新 uuid，避免沿用
     * 可能被後端快取住失敗結果的舊 uuid），全程仍停留在 Analyzing（loading）畫面，使用者不需自己重新作答。
     * 例外（Result.failure，如網路錯誤）不重試，直接回傳讓上層走 Error 分支。
     */
    private suspend fun completeQuizWithSoftFailRetry(
        selectedTags: List<String>,
        shownCities: List<String>,
    ): Result<QuizCompletionResult> {
        var lastResult: Result<QuizCompletionResult>
        var attempt = 1
        while (true) {
            lastResult = completeQuizUseCase(
                completionUuid = completionUuid,
                personality = personality,
                speechStyle = speechStyle,
                selectedTags = selectedTags,
                shownCities = shownCities,
                companionName = companionName,
                // 使用者當時捏好的頭像 URL 一併帶上，供 quiz-gallery 清單顯示；未捏頭像時為 null
                partnerAvatarUrl = _creationState.value.avatarUrl.takeIf { it.isNotBlank() }
            )
            val isSoftFailed = lastResult.getOrNull()?.isAnalysisSuccess == false
            if (!isSoftFailed || attempt >= QUIZ_COMPLETION_MAX_ATTEMPTS) {
                return lastResult
            }
            attempt++
            completionUuid = newCompletionUuid()
        }
    }

    /** 讀取本地測驗歷史供回顧頁顯示。 */
    fun loadQuizHistory() {
        viewModelScope.launch {
            _quizHistory.value = getQuizHistoryUseCase().getOrNull().orEmpty()
        }
    }

    /** 分析成功即寫入歷史（海報素材路徑待產圖完成回填），最新在前、上限 [QUIZ_HISTORY_MAX] 筆。 */
    private suspend fun appendQuizHistory(result: QuizCompletionResult) {
        val records = getQuizHistoryUseCase().getOrNull().orEmpty()
        val record = QuizHistoryRecord(
            result = result,
            companionSnapshot = currentCompanionSnapshot(),
            completionUuid = completionUuid,
            createdAt = currentTimeMillis(),
        )
        val updated = (listOf(record) + records).take(QUIZ_HISTORY_MAX)
        saveQuizHistoryUseCase(updated)
        _quizHistory.value = updated
    }

    private fun currentCompanionSnapshot(): CompanionSnapshot {
        val state = _creationState.value
        val loadedIntroduction = (introductionState.value as? IntroductionState.Loaded)?.introduction.orEmpty()
        val personalityLabels = state.selectedPersonality.map { option ->
            option.label.ifBlank { option.tag }
        }.ifEmpty { personality }
        val speechStyleLabel = state.selectedSpeechStyle?.let { option ->
            option.label.ifBlank { option.tag }
        }.orEmpty().ifBlank { speechStyle }
        return CompanionSnapshot(
            name = state.companionName.ifBlank { companionName.orEmpty() },
            introduction = loadedIntroduction,
            personalityTags = personality,
            personalityLabels = personalityLabels,
            speechStyleTag = speechStyle,
            speechStyleLabel = speechStyleLabel,
            avatarUrl = state.avatarUrl,
        )
    }

    fun deleteQuizHistoryRecord(createdAt: Long) {
        viewModelScope.launch {
            val records = getQuizHistoryUseCase().getOrNull().orEmpty()
            val updated = records.filterNot { it.createdAt == createdAt }
            saveQuizHistoryUseCase(updated)
            _quizHistory.value = updated
        }
    }

    /** 重跑測驗：重產 completion_uuid、清空作答紀錄、重置分析狀態。 */
    fun startNewQuizRound() {
        completionUuid = newCompletionUuid()
        _quizAnswers.value = emptyMap()
        _analysisState.value = AnalysisState.Idle
        _shareImageV2State.value = ShareImageV2State.Idle
    }

    // ---------- Phase 2：行程規劃（travel-summary / travel-guide，皆無狀態 API） ----------

    private val _travelSummaryState = MutableStateFlow<TravelSummaryState>(TravelSummaryState.Idle)
    val travelSummaryState: StateFlow<TravelSummaryState> = _travelSummaryState.asStateFlow()

    private val _travelGuideState = MutableStateFlow<TravelGuideState>(TravelGuideState.Idle)
    val travelGuideState: StateFlow<TravelGuideState> = _travelGuideState.asStateFlow()

    // 「我的旅程」本地清單（後端不儲存，最新在前）
    private val _savedTrips = MutableStateFlow<List<SavedTripRecord>>(emptyList())
    val savedTrips: StateFlow<List<SavedTripRecord>> = _savedTrips.asStateFlow()

    /** 本次規劃的原始輸入（無狀態 API：補充/重試都要原樣帶齊） */
    private data class TravelSummaryInput(
        val entryType: String,
        // quiz_completion／from_orders 必填（權威回傳）
        val city: String? = null,
        // from_orders 選填：被點選那筆訂單的材料，開場白會呼應訂購商品
        val order: TripOrderMaterial? = null,
        val cityImageUrl: String? = null,
        val introText: String? = null,
        val sourceType: String? = null,
        val content: String? = null
    )

    private var travelSummaryInput: TravelSummaryInput? = null
    private var travelSummaryLastNote: String? = null

    // App 端只需一直保存「目前最新的 summary/city」，補充或打 travel-guide 時帶上
    private var latestPlanSummary: String = ""
    private var latestPlanCity: String = ""
    private var lastPreferences: Map<String, String> = emptyMap()

    // 2026-08 travel-revise 改版（二）：貫穿整個聊天室（travel-summary/recommend-city/travel-guide/
    // travel-revise）的完整對話紀錄，畫面上出現的旅伴／使用者訊息都要 append 進來，travel-revise 全量帶入
    // 當作 LLM 的完整上下文。軟失敗的兜底文案不算「真的一輪對話」，不 append（避免污染上下文）。
    // 只在開新的一輪規劃時清空（startTravelSummary／selectOrderDestination），「重新聊聊」等同一 session
    // 內的動作不清空。
    private val companionTranscript = mutableListOf<CityChatMessage>()

    /** 入口 A：測驗結果頁「繼續規劃」。intro_text 以本輪分析結果組成（App 已持有，後端不回查快取）。 */
    fun startPlanFromQuizResult() {
        val result = (_analysisState.value as? AnalysisState.Success)?.result ?: return
        val destination = if (isChineseLanguageUseCase()) result.destinationCn else result.destinationEn
        val introText = buildString {
            append("你是${result.travelIdentity}，命定城市是${destination}。")
            if (result.tagline.isNotBlank()) append(result.tagline)
            if (result.recommendationText.isNotBlank()) {
                append("\n")
                append(result.recommendationText)
            }
        }
        startTravelSummary(
            TravelSummaryInput(
                entryType = TravelSummaryEntryType.QUIZ_COMPLETION,
                // 2026-08 改版：quiz_completion 入口 city 必填（缺少回 400），回傳 city 權威等於此值
                city = destination,
                introText = introText
            )
        )
    }

    /** 入口 B：匯入行程（目前僅 source_type=text；截圖上傳待後端提供上傳端點，見 CompanionPlanFeatureFlags）。 */
    fun startPlanFromImport(content: String) {
        startTravelSummary(
            TravelSummaryInput(
                entryType = TravelSummaryEntryType.IMPORTED_ITINERARY,
                sourceType = TravelSummarySourceType.TEXT,
                content = content
            )
        )
    }

    /** 入口 C：從零開始（後端不打 LLM、回固定文案；仍走 API 統一載入/錯誤路徑）。 */
    fun startPlanFromZero() {
        startTravelSummary(TravelSummaryInput(entryType = TravelSummaryEntryType.FROM_ZERO))
    }

    // ---------- 入口 D：帶訂單開場（travel-summary-from-orders，後端尚未實作） ----------

    private val _orderOpeningState = MutableStateFlow<OrderOpeningState>(OrderOpeningState.Idle)
    val orderOpeningState: StateFlow<OrderOpeningState> = _orderOpeningState.asStateFlow()

    // 選定目的地選項後（selectOrderDestination）要帶回該筆訂單材料組 intro_text，orderIndex 對應這裡的索引；
    // 願望清單/瀏覽紀錄入口的選項沒有逐筆對應（orderIndex = -1），這份清單為空
    private var orderOpeningMaterials: List<TripOrderMaterial> = emptyList()

    // 願望清單/瀏覽紀錄入口：每個城市對應「是哪幾筆商品推導出這個城市」的完整材料
    // （回應只回 prod_id/prod_name 引用，這裡以 prodId 回查原始萃取材料補齊 introduction/destination_names，
    // 讓 travel-guide 的 products[] 有完整材料可帶）；帶訂單入口這份為空
    private var openingCityProducts: Map<String, List<TripProductMaterial>> = emptyMap()

    // 重試時要重跑哪一條開場流程（帶訂單/願望清單/瀏覽紀錄共用同一個開場畫面與狀態）
    private var orderOpeningRequest: () -> Unit = {}

    /** 點「一起規劃旅遊行程（帶訂單）」：抓即將出發訂單材料 → 丟給 LLM 判斷目的地選項，供使用者挑一個開始。 */
    fun startPlanFromOrders() {
        orderOpeningRequest = ::startPlanFromOrders
        _orderOpeningState.value = OrderOpeningState.Loading
        viewModelScope.launch {
            getUpcomingOrderMaterialsUseCase()
                .onSuccess { materials ->
                    if (materials.isEmpty()) {
                        _orderOpeningState.value = OrderOpeningState.NotAvailable
                        return@onSuccess
                    }
                    orderOpeningMaterials = materials
                    fetchTravelSummaryFromOrdersUseCase(
                        orders = materials,
                        companionName = companionName,
                        personality = personality,
                        speechStyle = speechStyle.takeIf { it.isNotBlank() }
                    ).onSuccess { result ->
                        _orderOpeningState.value = OrderOpeningState.Ready(
                            greeting = result.greeting,
                            options = result.options
                        )
                    }.onFailure {
                        _orderOpeningState.value = OrderOpeningState.Error
                    }
                }
                .onFailure {
                    _orderOpeningState.value = OrderOpeningState.Error
                }
        }
    }

    /** 點「一起規劃旅遊行程（從心願清單）」：GET wish_list 萃取商品材料 → LLM 聚合判斷最多 3 個城市。 */
    fun startPlanFromWish() {
        orderOpeningRequest = ::startPlanFromWish
        startPlanFromProducts(
            fetchMaterials = { getWishProductMaterialsUseCase() },
            fetchSummary = { products ->
                fetchTravelSummaryFromWishUseCase(
                    products = products,
                    companionName = companionName,
                    personality = personality,
                    speechStyle = speechStyle.takeIf { it.isNotBlank() }
                )
            },
        )
    }

    /** 點「一起規劃旅遊行程（從瀏覽記錄）」：GET history 萃取商品材料 → LLM 聚合判斷最多 3 個城市。 */
    fun startPlanFromHistory() {
        orderOpeningRequest = ::startPlanFromHistory
        startPlanFromProducts(
            fetchMaterials = { getHistoryProductMaterialsUseCase() },
            fetchSummary = { products ->
                fetchTravelSummaryFromHistoryUseCase(
                    products = products,
                    companionName = companionName,
                    personality = personality,
                    speechStyle = speechStyle.takeIf { it.isNotBlank() }
                )
            },
        )
    }

    /**
     * 願望清單/瀏覽紀錄兩條流程完全對稱（見 ai-companion-wish-history-integration.md），共用這段：
     * 抓商品材料 → 聚合判斷城市（每個城市附「是哪幾筆商品推導出這個城市」）→ 沿用帶訂單開場的畫面狀態。
     * cities 沒有 order_index（非逐筆對應），映射成 orderIndex=-1 的選項；每個城市的商品引用以 prodId
     * 回查原始材料補齊完整欄位存起來，選定城市後帶進 travel-guide 的 products[] 讓商品必被排入行程。
     * LLM 軟失敗（cities 為空）時 Ready 帶空選項，畫面出「改用一般規劃」逃生門。
     */
    private fun startPlanFromProducts(
        fetchMaterials: suspend () -> Result<List<TripProductMaterial>>,
        fetchSummary: suspend (List<TripProductMaterial>) -> Result<TravelSummaryFromProductsResult>,
    ) {
        orderOpeningMaterials = emptyList()
        openingCityProducts = emptyMap()
        _orderOpeningState.value = OrderOpeningState.Loading
        viewModelScope.launch {
            fetchMaterials()
                .onSuccess { materials ->
                    if (materials.isEmpty()) {
                        _orderOpeningState.value = OrderOpeningState.NotAvailable
                        return@onSuccess
                    }
                    fetchSummary(materials)
                        .onSuccess { result ->
                            val materialsById = materials.associateBy { it.prodId }
                            openingCityProducts = result.cities.associate { cityProducts ->
                                cityProducts.city to cityProducts.products.map { ref ->
                                    // 回應的 prod_id 保證是送出去的值之一；仍保底用引用組最小材料
                                    materialsById[ref.prodId]
                                        ?: TripProductMaterial(prodId = ref.prodId, prodName = ref.prodName)
                                }
                            }
                            _orderOpeningState.value = OrderOpeningState.Ready(
                                greeting = result.greeting,
                                options = result.cities.map { cityProducts ->
                                    TravelDestinationOption(orderIndex = -1, city = cityProducts.city)
                                }
                            )
                        }
                        .onFailure { _orderOpeningState.value = OrderOpeningState.Error }
                }
                .onFailure { _orderOpeningState.value = OrderOpeningState.Error }
        }
    }

    fun retryPlanFromOrders() = orderOpeningRequest()

    // 材料開場（帶訂單/心願清單/瀏覽紀錄）選定後，聊天室最上方要接續顯示的開場對話
    // （greeting＋使用者的選擇），讓對話「往下增長」而不是換頁重來
    private val _planChatPrelude = MutableStateFlow<List<CityChatUiMessage>>(emptyList())
    val planChatPrelude: StateFlow<List<CityChatUiMessage>> = _planChatPrelude.asStateFlow()

    /**
     * 使用者在「帶訂單開場」點了某個目的地選項：2026-08 改版後後端支援 entry_type=from_orders，
     * 帶必填 city（權威回傳）＋選填 order（該筆訂單材料，開場白會呼應訂購商品）正式打 travel-summary。
     * 選定的訂單另存起來，之後打 travel-guide 時帶進 orders[] 讓後端把已預訂項目排入行程。
     * 開場對話（greeting＋選的城市）保留為聊天室前導訊息，不清除、接續往下長。
     */
    fun selectOrderDestination(option: TravelDestinationOption) {
        val material = orderOpeningMaterials.getOrNull(option.orderIndex)
        val cityProducts = openingCityProducts[option.city].orEmpty()
        val greeting = (_orderOpeningState.value as? OrderOpeningState.Ready)?.greeting.orEmpty()
        startTravelSummary(
            TravelSummaryInput(
                entryType = TravelSummaryEntryType.FROM_ORDERS,
                city = option.city,
                order = material
            )
        )
        // startTravelSummary 內會先清空 selectedTripOrders／selectedTripProducts／prelude／transcript，故都在其後設定
        selectedTripOrders = listOfNotNull(material?.takeIf { it.oid.isNotBlank() })
        // 願望清單/瀏覽紀錄入口：被點選城市對應的商品帶進 travel-guide 的 products[]，商品必被排入行程
        selectedTripProducts = cityProducts
        setPlanChatPrelude(greeting = greeting, userChoice = option.city)
    }

    /**
     * 材料開場（帶訂單/心願清單/瀏覽紀錄）的「我想直接開始規劃」：不選任何城市，
     * 走 from_zero 拿開場文案並直接啟動城市收斂對話；開場對話同樣保留接續顯示。
     */
    fun startCityChatFromOpening() {
        val greeting = (_orderOpeningState.value as? OrderOpeningState.Ready)?.greeting.orEmpty()
        startTravelSummary(TravelSummaryInput(entryType = TravelSummaryEntryType.FROM_ZERO))
        setPlanChatPrelude(greeting = greeting, userChoice = OPENING_DIRECT_PLAN_LABEL)
        startCityRecommendation()
    }

    /** 開場對話進前導訊息＋統一對話紀錄（startTravelSummary 剛清空過，這裡是新一輪的最前面兩句）。 */
    private fun setPlanChatPrelude(greeting: String, userChoice: String) {
        val prelude = buildList {
            if (greeting.isNotBlank()) add(CityChatMessage.assistant(greeting))
            if (userChoice.isNotBlank()) add(CityChatMessage.user(userChoice))
        }
        companionTranscript += prelude
        _planChatPrelude.value = prelude.map {
            CityChatUiMessage(fromMe = it.role == CityChatMessage.ROLE_USER, text = it.content)
        }
    }

    // 帶訂單入口選定的訂單（≤3 筆）：travel-guide 帶進 orders[] 讓後端排入行程；
    // 其他入口為空。revise 時行程內既有 oid 由後端自動保護，不需重帶
    private var selectedTripOrders: List<TripOrderMaterial> = emptyList()

    // 願望清單/瀏覽紀錄入口選定城市對應的商品（≤10 筆）：travel-guide 帶進 products[] 讓後端排入行程；
    // 其他入口為空。revise 時行程內既有 prod_id 由後端自動併入白名單保護，不需重帶
    private var selectedTripProducts: List<TripProductMaterial> = emptyList()

    private fun startTravelSummary(input: TravelSummaryInput) {
        travelSummaryInput = input
        travelSummaryLastNote = null
        latestPlanSummary = ""
        // from_orders/quiz 入口 city 已知（權威），先寫入避免 LLM 軟失敗時拿不到城市
        latestPlanCity = input.city.orEmpty()
        selectedTripOrders = emptyList()
        selectedTripProducts = emptyList()
        _planChatPrelude.value = emptyList()
        _travelGuideState.value = TravelGuideState.Idle
        cityChatHistory.clear()
        cityShownCities.clear()
        _recommendCityState.value = RecommendCityState()
        preferenceAnswers.clear()
        _preferenceChatState.value = PreferenceChatState()
        companionTranscript.clear()
        fetchTravelSummary(note = null)
    }

    /** 「資訊不夠，我想補充」：帶 previous_summary + note 重打，新 summary 取代前一版。 */
    fun supplementTravelSummary(note: String) {
        if (note.isBlank()) return
        fetchTravelSummary(note = note.trim())
    }

    /** 軟失敗「重新生成」或硬失敗「重試」：原樣重打上一次的請求（無狀態、無副作用）。 */
    fun retryTravelSummary() {
        fetchTravelSummary(note = travelSummaryLastNote)
    }

    private fun fetchTravelSummary(note: String?) {
        val input = travelSummaryInput ?: return
        travelSummaryLastNote = note
        _travelSummaryState.value = TravelSummaryState.Loading(
            previousSummary = latestPlanSummary,
            pendingNote = note.orEmpty()
        )
        // 補充重打的話，補充泡泡在 Loading 就顯示了，先進統一對話紀錄
        if (!note.isNullOrBlank()) {
            companionTranscript += CityChatMessage.user(note.trim())
        }
        viewModelScope.launch {
            fetchTravelSummaryUseCase(
                entryType = input.entryType,
                city = input.city,
                order = input.order,
                cityImageUrl = input.cityImageUrl,
                introText = input.introText,
                sourceType = input.sourceType,
                content = input.content,
                note = note,
                previousSummary = latestPlanSummary.takeIf { it.isNotBlank() },
                companionName = companionName,
                personality = personality,
                speechStyle = speechStyle.takeIf { it.isNotBlank() }
            ).onSuccess { result ->
                latestPlanSummary = result.summary
                // 補充重打若判斷不出城市，保留前一次的判斷
                latestPlanCity = result.city.ifBlank { latestPlanCity }
                // Ready 一律顯示這句摘要泡泡（即使 isSoftFail，summary 是後端兜底文案仍會渲染），進統一對話紀錄
                companionTranscript += CityChatMessage.assistant(result.summary)
                _travelSummaryState.value = TravelSummaryState.Ready(
                    summary = result.summary,
                    city = latestPlanCity,
                    isSoftFail = !result.isGenerateSuccess,
                    isFromZero = input.entryType == TravelSummaryEntryType.FROM_ZERO
                )
            }.onFailure { e ->
                _travelSummaryState.value =
                    if (e is TravelPlanValidationException) TravelSummaryState.InvalidRequest
                    else TravelSummaryState.Error
            }
        }
    }

    // ---------- recommend-city：城市推薦多輪對話（從零開始入口，上限 5 輪） ----------

    private val _recommendCityState = MutableStateFlow(RecommendCityState())
    val recommendCityState: StateFlow<RecommendCityState> = _recommendCityState.asStateFlow()

    // API 參數用的對話歷史（含 role，無狀態 API 每輪全量帶回）——與畫面顯示的
    // RecommendCityState.messages 刻意分離：收斂輪推薦文與「換一個城市」「重新聊聊」只進畫面不進這裡，
    // 「重新聊聊」清這份歷史但保留畫面文字
    private val cityChatHistory = mutableListOf<CityChatMessage>()

    // 本次對話中已被推薦過的城市（換一個城市時帶入 shown_cities 讓 LLM 排除）；
    // 只存活於 ViewModel：重新開始規劃 / 重新聊聊時清除，離開頁面自然銷毀
    private val cityShownCities = mutableListOf<String>()

    /** 「我想直接開始規劃」：啟動城市收斂對話，先出本地開場引導（不打 API、不占輪次）。 */
    fun startCityRecommendation() {
        if (_recommendCityState.value.active) return
        cityChatHistory.clear()
        cityShownCities.clear()
        cityChatHistory += CityChatMessage.assistant(CITY_CHAT_OPENING)
        companionTranscript += CityChatMessage.assistant(CITY_CHAT_OPENING)
        _recommendCityState.value = RecommendCityState(
            active = true,
            messages = listOf(CityChatUiMessage(fromMe = false, text = CITY_CHAT_OPENING)),
            quickReplies = CITY_CHAT_OPENING_CHIPS
        )
    }

    /** 送出一輪使用者輸入（打字或點 chip 皆同）：append 歷史後全量呼叫 recommend-city。 */
    fun sendCityMessage(text: String) {
        val state = _recommendCityState.value
        if (text.isBlank() || !state.active || state.isFinal || state.isWaiting) return
        cityChatHistory += CityChatMessage.user(text.trim())
        companionTranscript += CityChatMessage.user(text.trim())
        _recommendCityState.update {
            it.copy(
                messages = it.messages + CityChatUiMessage(fromMe = true, text = text.trim()),
                quickReplies = emptyList(),
                isWaiting = true,
                isError = false,
                fallbackReply = ""
            )
        }
        requestRecommendCity()
    }

    /** 軟失敗（兜底文案）或硬失敗後重試：原樣重打同一份 messages（無狀態、無副作用）。 */
    fun retryRecommendCity() {
        if (cityChatHistory.lastOrNull()?.role != CityChatMessage.ROLE_USER) return
        _recommendCityState.update { it.copy(isWaiting = true, isError = false, fallbackReply = "") }
        requestRecommendCity()
    }

    private fun requestRecommendCity() {
        viewModelScope.launch {
            fetchRecommendCityUseCase(
                messages = cityChatHistory.toList(),
                shownCities = cityShownCities.toList(),
                companionName = companionName,
                personality = personality,
                speechStyle = speechStyle.takeIf { it.isNotBlank() }
            ).onSuccess { result ->
                if (result.swapLimitReached) {
                    // 換城超限保底（正常流程有 guard 不會走到）：未打 LLM 的固定文案，
                    // 不 append 進歷史，跳 dialog 引導「重新聊聊」
                    _recommendCityState.update {
                        it.copy(isWaiting = false, canSwapCity = false, swapLimitText = result.reply)
                    }
                    return@onSuccess
                }
                if (result.isGenerateSuccess) {
                    if (!result.isFinal) {
                        // 一般輪的回覆 append 進 API 歷史（供下一輪全量帶回）；
                        // 收斂輪的推薦文只進畫面不進歷史——排除已推薦城市由 shown_cities 負責，
                        // 不需要多餘文字給 LLM 思考，換城時原樣重打同一份 messages 即可
                        cityChatHistory += CityChatMessage.assistant(result.reply)
                    }
                    // 統一對話紀錄不管一般輪或收斂輪，只要畫面有出現這句泡泡就記，跟 cityChatHistory 邏輯刻意不同
                    companionTranscript += CityChatMessage.assistant(result.reply)
                    if (result.isFinal && result.recommendedCity.isNotBlank()) {
                        // 收斂完成：城市帶進後續 travel-guide，並記進已推薦清單（換一個城市時作為 shown_cities 排除）
                        latestPlanCity = result.recommendedCity
                        if (result.recommendedCity !in cityShownCities && cityShownCities.size < SHOWN_CITIES_MAX) {
                            cityShownCities += result.recommendedCity
                        }
                    }
                    _recommendCityState.update {
                        it.copy(
                            messages = it.messages + CityChatUiMessage(fromMe = false, text = result.reply),
                            // 收斂輪 chips 由後端強制覆寫為固定三顆（就去{城市}！／換一個城市／重新聊聊），照渲染
                            quickReplies = result.quickReplies,
                            isWaiting = false,
                            recommendedCity = result.recommendedCity,
                            cityReason = result.cityReason,
                            isFinal = result.isFinal,
                            remainingRounds = result.remainingRounds,
                            // 換城上限：以 shown_cities 長度為依據——帶 5 個是最後一次換城（下一次換城的
                            // request 會帶目前清單，≤5 個才允許）；換城不增加 messages（原樣重打同一份），
                            // 最後一次換城的回應後端 chips 也不會再遞「換一個城市」
                            canSwapCity = cityShownCities.size <= CITY_SWAP_MAX
                        )
                    }
                } else {
                    // 軟失敗：reply 為兜底文案，渲染但不進歷史（避免污染上下文），可原樣重打
                    _recommendCityState.update { it.copy(isWaiting = false, fallbackReply = result.reply) }
                }
            }.onFailure {
                _recommendCityState.update { it.copy(isWaiting = false, isError = true) }
            }
        }
    }

    /**
     * 收斂後點「換一個城市」：原樣重打同一份 messages，只靠 shown_cities 排除已推薦城市——
     * 推薦文與「換一個城市」不進 API body（多餘文字不需要 LLM 思考），只在畫面顯示 chip 選擇。
     * 回應同為 isFinal + 固定三顆 chips，可連換，每換一次新城市會累加進 shown_cities。
     */
    fun swapRecommendedCity() {
        val state = _recommendCityState.value
        if (!state.active || !state.isFinal || state.isWaiting) return
        // 換城上限 5 次（shown_cities 長度即換城次數依據）；超過後端會回 swap_limit_reached 保底
        if (cityShownCities.size > CITY_SWAP_MAX) return
        companionTranscript += CityChatMessage.user(CITY_CHIP_SWAP)
        _recommendCityState.update {
            it.copy(
                messages = it.messages + CityChatUiMessage(fromMe = true, text = CITY_CHIP_SWAP),
                quickReplies = emptyList(),
                isWaiting = true,
                isError = false,
                fallbackReply = "",
                isFinal = false,
                recommendedCity = "",
                cityReason = ""
            )
        }
        requestRecommendCity()
    }

    /**
     * 收斂後點「重新聊聊」：純 App 端行為不打 API。只清 API 參數資料
     * （messages 歷史、shown_cities、已收斂城市）回到第 1 輪；聊天室畫面文字保留，
     * 僅追加「重新聊聊」選擇與新的開場引導。
     */
    fun restartCityChat() {
        latestPlanCity = ""
        cityChatHistory.clear()
        cityShownCities.clear()
        cityChatHistory += CityChatMessage.assistant(CITY_CHAT_OPENING)
        companionTranscript += CityChatMessage.user(CITY_CHIP_RESTART)
        companionTranscript += CityChatMessage.assistant(CITY_CHAT_OPENING)
        _recommendCityState.update {
            it.copy(
                active = true,
                messages = it.messages +
                    CityChatUiMessage(fromMe = true, text = CITY_CHIP_RESTART) +
                    CityChatUiMessage(fromMe = false, text = CITY_CHAT_OPENING),
                quickReplies = CITY_CHAT_OPENING_CHIPS,
                isWaiting = false,
                isError = false,
                fallbackReply = "",
                recommendedCity = "",
                cityReason = "",
                isFinal = false,
                remainingRounds = 0,
                canSwapCity = true,
                swapLimitText = ""
            )
        }
    }

    // ---------- 偏好問卷（聊天式，一次一題；純前端不打 API） ----------

    private val _preferenceChatState = MutableStateFlow(PreferenceChatState())
    val preferenceChatState: StateFlow<PreferenceChatState> = _preferenceChatState.asStateFlow()

    // 收集到的問卷答案（API 參數資料，與畫面顯示的 messages 分開存取）
    private val preferenceAnswers = mutableMapOf<String, String>()

    /** 「開始規劃」：啟動聊天式問卷，出第一題（泡泡＋選項 chips，也可打字回答）。 */
    fun startPreferenceChat() {
        if (_preferenceChatState.value.active) return
        preferenceAnswers.clear()
        _preferenceChatState.value = PreferenceChatState(
            active = true,
            messages = listOf(CityChatUiMessage(fromMe = false, text = preferenceQuestionText(0))),
            options = PLAN_PREFERENCE_QUESTIONS.first().options,
            currentIndex = 0
        )
    }

    /** 回答目前題目（點選項 chip 或打字皆同）：記錄答案後出下一題；最後一題答完進入 isCompleted。 */
    fun answerPreference(text: String) {
        val state = _preferenceChatState.value
        if (!state.active || state.isCompleted || text.isBlank()) return
        val question = PLAN_PREFERENCE_QUESTIONS.getOrNull(state.currentIndex) ?: return
        // notes 題點「沒有特別需求」＝跳過，不放進 preferences
        if (!(question.key == PREFERENCE_NOTES_KEY && text == PREFERENCE_SKIP_NOTES)) {
            preferenceAnswers[question.key] = text.trim()
        }
        val userMessage = CityChatUiMessage(fromMe = true, text = text.trim())
        val nextIndex = state.currentIndex + 1
        _preferenceChatState.value = if (nextIndex < PLAN_PREFERENCE_QUESTIONS.size) {
            state.copy(
                messages = state.messages + userMessage +
                    CityChatUiMessage(fromMe = false, text = preferenceQuestionText(nextIndex)),
                options = PLAN_PREFERENCE_QUESTIONS[nextIndex].options,
                currentIndex = nextIndex
            )
        } else {
            state.copy(
                messages = state.messages + userMessage +
                    CityChatUiMessage(fromMe = false, text = PREFERENCE_COMPLETED_MESSAGE),
                options = emptyList(),
                currentIndex = nextIndex,
                isCompleted = true
            )
        }
    }

    /** 問卷答完按「開始規劃」：帶收集到的 preferences 打 travel-guide。 */
    fun submitPreferences() {
        if (!_preferenceChatState.value.isCompleted) return
        fetchTravelGuide(preferenceAnswers.toMap())
    }

    private fun preferenceQuestionText(index: Int): String =
        "（${index + 1}/${PLAN_PREFERENCE_QUESTIONS.size}）${PLAN_PREFERENCE_QUESTIONS[index].title}"

    /**
     * 聊天室輸入列統一入口：問卷進行中 → 回答目前題目；城市對話進行中 → recommend-city 下一輪；
     * 從零開始尚未啟動對話 → 以這句話直接開啟城市對話；其餘（A/B 入口）→ 補充重新生成摘要。
     */
    fun sendPlanInput(text: String) {
        val preferenceState = _preferenceChatState.value
        val cityState = _recommendCityState.value
        val summaryState = _travelSummaryState.value
        when {
            preferenceState.active && !preferenceState.isCompleted -> answerPreference(text)
            cityState.active && !cityState.isFinal -> sendCityMessage(text)
            (summaryState as? TravelSummaryState.Ready)?.isFromZero == true && !cityState.active -> {
                startCityRecommendation()
                sendCityMessage(text)
            }
            else -> supplementTravelSummary(text)
        }
    }

    /** 問卷填完「開始規劃」：帶最新 summary/city + preferences 一次生成完整行程。 */
    fun fetchTravelGuide(preferences: Map<String, String>) {
        lastPreferences = preferences
        runTravelGuide()
    }

    /** 生成失敗重試：原樣帶同一份 summary/preferences 重打（無副作用，可安全重試）。 */
    fun retryTravelGuide() = runTravelGuide()

    private fun runTravelGuide() {
        if (latestPlanSummary.isBlank()) return
        _travelGuideState.value = TravelGuideState.Loading
        viewModelScope.launch {
            fetchTravelGuideUseCase(
                summary = latestPlanSummary,
                city = latestPlanCity,
                preferences = lastPreferences,
                // 帶訂單入口選定的訂單：後端必排入行程（item 回填 oid、該天 booked_anchor 有值）
                orders = selectedTripOrders,
                // 願望清單/瀏覽紀錄入口選定城市對應的商品：後端必排入行程（item 回填 prod_id）
                products = selectedTripProducts,
                companionName = companionName,
                personality = personality,
                speechStyle = speechStyle.takeIf { it.isNotBlank() }
            ).onSuccess { result ->
                if (result.isGenerateSuccess) {
                    // 完成宣告泡泡（可能不只一句）依序進統一對話紀錄
                    result.messages.forEach { companionTranscript += CityChatMessage.assistant(it) }
                }
                _travelGuideState.value = if (result.isGenerateSuccess) {
                    TravelGuideState.Loaded(
                        trip = SavedTripRecord(
                            // 成果頁標題照設計稿「{旅伴名} × 你的{城市}」，儲存當下組好
                            title = "${companionName ?: DEFAULT_COMPANION_NAME} × 你的${result.city}", // TODO: replace with stringResource
                            city = result.city,
                            totalDays = result.totalDays,
                            days = result.days,
                            // 偏好一併保存：回訪後繼續請旅伴修改時帶原偏好維持風格
                            preferences = lastPreferences,
                            heroImageUrl = result.heroImageUrl,
                            createdAt = currentTimeMillis()
                        ),
                        messages = result.messages,
                        progressLabel = result.progressLabel,
                        mainActionLabel = result.mainActionLabel
                    )
                } else {
                    TravelGuideState.SoftFailed
                }
            }.onFailure { e ->
                _travelGuideState.value =
                    if (e is TravelPlanValidationException) TravelGuideState.InvalidRequest
                    else TravelGuideState.Error
            }
        }
    }

    // ---------- travel-revise：「請{旅伴}幫我改」修改既有行程 ----------

    private val _tripReviseState = MutableStateFlow(TripReviseState())
    val tripReviseState: StateFlow<TripReviseState> = _tripReviseState.asStateFlow()

    // 是否有一句尚未成功的修改需求，供 retryTripRevise 判斷（文字本身已經在 companionTranscript 最後一則）
    private var hasPendingReviseRequest = false

    /**
     * 開啟修改對話（從某個 Day 的 FAB 進入）：聊天紀錄不分天、跨開關 sheet 皆保留在 ViewModel
     * （companionTranscript，離開頁面 ViewModel 銷毀才消失），開啟時從統一對話紀錄重建畫面訊息——
     * 包含進入完整行程之前的規劃對話（開場摘要、城市收斂、完成宣告）。dayNumber 只決定 target_day。
     */
    fun startTripRevise(dayNumber: Int) {
        _tripReviseState.update {
            it.copy(
                active = true,
                dayNumber = dayNumber,
                messages = companionTranscript.map { message ->
                    CityChatUiMessage(fromMe = message.role == CityChatMessage.ROLE_USER, text = message.content)
                },
            )
        }
    }

    /** 關閉修改對話（sheet 收起）：只收起畫面，聊天紀錄與等待中/錯誤狀態保留，再開時原樣接續。 */
    fun endTripRevise() {
        _tripReviseState.update { it.copy(active = false) }
    }

    /**
     * 送出一句修改需求：2026-08 改版（二）不再有獨立的 request 欄位，這次需求即為
     * companionTranscript（貫穿整個聊天室的完整對話紀錄）最後一則 role=user 的內容。
     */
    fun sendTripRevise(text: String) {
        val state = _tripReviseState.value
        if (text.isBlank() || !state.active || state.isWaiting) return
        companionTranscript += CityChatMessage.user(text.trim())
        hasPendingReviseRequest = true
        _tripReviseState.update {
            it.copy(
                messages = it.messages + CityChatUiMessage(fromMe = true, text = text.trim()),
                isWaiting = true,
                isError = false,
                fallbackReply = ""
            )
        }
        requestTravelRevise()
    }

    /** 軟失敗（兜底文案）或硬失敗後重試：原樣重打同一份 messages（無狀態、無副作用）。 */
    fun retryTripRevise() {
        if (!hasPendingReviseRequest) return
        _tripReviseState.update { it.copy(isWaiting = true, isError = false, fallbackReply = "") }
        requestTravelRevise()
    }

    private fun requestTravelRevise() {
        if (!hasPendingReviseRequest) return
        val loaded = _travelGuideState.value as? TravelGuideState.Loaded ?: return
        viewModelScope.launch {
            fetchTravelReviseUseCase(
                itineraryDays = loaded.trip.days,
                city = loaded.trip.city,
                targetDay = _tripReviseState.value.dayNumber.takeIf { it > 0 },
                // 上限 100 則，優先保留最近的訊息（見改版文件第 3 節）
                messages = companionTranscript.takeLast(TRAVEL_REVISE_MESSAGES_MAX),
                // 回訪的本地行程有自帶偏好；同一 session 內用問卷收集到的最新答案
                preferences = loaded.trip.preferences.ifEmpty { lastPreferences },
                companionName = companionName,
                personality = personality,
                speechStyle = speechStyle.takeIf { it.isNotBlank() }
            ).onSuccess { result ->
                // SIT 實測：後端目前只回有變動的那幾天（未如文件承諾回完整行程）。
                // 以天為單位 upsert merge：回應裡有的天覆蓋、沒回來的天原樣保留、新天號附加，
                // 這樣無論後端這次回一天還是全部天，App 端都不會弄丟其他天的資料。
                if (result.days.isNotEmpty()) {
                    mergeRevisedDays(loaded, result.days)
                }
                if (result.isGenerateSuccess) {
                    // 成功一輪才進統一對話紀錄（軟失敗的兜底文案不算「真的一輪對話」）
                    companionTranscript += CityChatMessage.assistant(result.reply)
                    hasPendingReviseRequest = false
                    _tripReviseState.update {
                        it.copy(
                            messages = it.messages + CityChatUiMessage(fromMe = false, text = result.reply),
                            isWaiting = false,
                            // 離題輪行程沒真的變動（changedDayNumbers 為空），不給「回去看修改結果」
                            hasRevised = it.hasRevised || result.changedDayNumbers.isNotEmpty()
                        )
                    }
                } else {
                    // 軟失敗：reply 為兜底文案，渲染但不進歷史，可原樣重打（行程已原樣替換，內容不變）
                    _tripReviseState.update { it.copy(isWaiting = false, fallbackReply = result.reply) }
                }
            }.onFailure {
                _tripReviseState.update { it.copy(isWaiting = false, isError = true) }
            }
        }
    }

    /**
     * 以天為單位 upsert 合併 travel-revise 回應的天數進本地行程：
     * 回應含有的天號覆蓋既有內容，未出現的天原樣保留，回應中本地沒有的天號視為新增（依天號排序插入）。
     * 天數增減時 totalDays 同步更新（目前無法表達「刪除某一天」——後端若真的回完整行程時無此限制）。
     */
    private fun mergeRevisedDays(loaded: TravelGuideState.Loaded, revisedDays: List<TravelGuideDay>) {
        val revisedByDay = revisedDays.associateBy { it.day }
        val existingDayNumbers = loaded.trip.days.map { it.day }.toSet()
        val merged = (loaded.trip.days.map { revisedByDay[it.day] ?: it } +
            revisedDays.filter { it.day !in existingDayNumbers })
            .sortedBy { it.day }
        updateActiveTrip(loaded, loaded.trip.copy(days = merged, totalDays = merged.size))
    }

    /** 長按拖拉交換當日項目位置：放開即定案，直接更新 ViewModel 內的行程（已儲存的同一筆同步更新）。 */
    fun reorderDayItems(dayNumber: Int, fromIndex: Int, toIndex: Int) {
        val loaded = _travelGuideState.value as? TravelGuideState.Loaded ?: return
        val day = loaded.trip.days.firstOrNull { it.day == dayNumber } ?: return
        if (fromIndex == toIndex ||
            fromIndex !in day.items.indices || toIndex !in day.items.indices
        ) return
        val reordered = day.items.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
        updateActiveTrip(
            loaded,
            loaded.trip.copy(
                days = loaded.trip.days.map { if (it.day == dayNumber) it.copy(items = reordered) else it }
            )
        )
    }

    /** 行程本地變更的統一出口（AI 修改 merge／拖拉排序）：更新畫面，已存進「我的旅程」的同一筆也同步存回。 */
    private fun updateActiveTrip(loaded: TravelGuideState.Loaded, mergedTrip: SavedTripRecord) {
        _travelGuideState.value = loaded.copy(trip = mergedTrip)
        val saved = _savedTrips.value
        if (saved.any { it.createdAt == mergedTrip.createdAt }) {
            viewModelScope.launch {
                val updated = saved.map { if (it.createdAt == mergedTrip.createdAt) mergedTrip else it }
                saveSavedTripsUseCase(updated).onSuccess { _savedTrips.value = updated }
            }
        }
    }

    // ---------- 行程景點卡：背景用景點名稱搜尋商品 ----------

    private val _tripProductStates = MutableStateFlow<Map<String, TripProductState>>(emptyMap())
    val tripProductStates: StateFlow<Map<String, TripProductState>> = _tripProductStates.asStateFlow()

    /** 以景點名稱為 key 快取查詢結果，同一名稱只查一次（跨換天/重組不重複打 API）。 */
    fun searchTripProduct(spotName: String) {
        if (spotName.isBlank() || _tripProductStates.value.containsKey(spotName)) return
        _tripProductStates.update { it + (spotName to TripProductState.Loading) }
        // 帶目的地城市一起搜尋，避免同名景點在其他城市誤配（例如「中央市場」）；
        // 快取 key 仍用純景點名稱（畫面查詢用同一份 key），城市只影響實際打出去的關鍵字
        val city = (_travelGuideState.value as? TravelGuideState.Loaded)?.trip?.city.orEmpty()
        val keyword = if (city.isNotBlank()) "$city $spotName" else spotName
        viewModelScope.launch {
            searchTripProductsUseCase(keyword)
                .onSuccess { result ->
                    _tripProductStates.update {
                        it + (
                            spotName to (
                                result.products.takeIf { p -> p.isNotEmpty() }
                                    ?.let { products -> TripProductState.Found(products, result.totalCount) }
                                    ?: TripProductState.NotFound
                                )
                            )
                    }
                }
                .onFailure {
                    // 搜尋失敗不視為錯誤（純附加功能）：收斂成 NotFound，畫面直接不顯示卡片
                    _tripProductStates.update { it + (spotName to TripProductState.NotFound) }
                }
        }
    }

    // ---------- 「我的旅程」（純本地功能） ----------

    fun loadSavedTrips() {
        viewModelScope.launch {
            getSavedTripsUseCase().onSuccess { _savedTrips.value = it }
        }
    }

    /** 成果頁「儲存到我的旅程」：寫入本地清單（最新在前，同 createdAt 去重）。 */
    fun saveActiveTrip() {
        val trip = (_travelGuideState.value as? TravelGuideState.Loaded)?.trip ?: return
        viewModelScope.launch {
            val updated = (listOf(trip) + _savedTrips.value.filterNot { it.createdAt == trip.createdAt })
                .take(SAVED_TRIPS_MAX)
            saveSavedTripsUseCase(updated).onSuccess { _savedTrips.value = updated }
        }
    }

    fun deleteSavedTrip(trip: SavedTripRecord) {
        viewModelScope.launch {
            val updated = _savedTrips.value.filterNot { it.createdAt == trip.createdAt }
            saveSavedTripsUseCase(updated).onSuccess { _savedTrips.value = updated }
        }
    }

    /** 從「我的旅程」點開本地行程：直接以 Loaded 呈現成果頁（不打 API）。 */
    fun openSavedTrip(trip: SavedTripRecord) {
        // 換到別筆已存的行程：清掉統一對話紀錄，避免不同行程的對話混在一起傳給 travel-revise
        companionTranscript.clear()
        _travelGuideState.value = TravelGuideState.Loaded(trip)
    }

    companion object {
        const val DEFAULT_COMPANION_NAME = "旅伴" // TODO: replace with stringResource - 未命名時的預設名
        // quiz-completions 軟失敗（LLM 回應解析失敗等瞬時問題）自動重試上限次數（含首次呼叫）
        const val QUIZ_COMPLETION_MAX_ATTEMPTS = 2
        const val QUIZ_HISTORY_MAX = 20
        const val SAVED_TRIPS_MAX = 20

        // recommend-city 本地開場引導（不打 API、不占 5 輪額度）；chips 為起手建議，點了即為第 1 輪 user 輸入
        // TODO: replace with stringResource
        const val CITY_CHAT_OPENING = "想去哪裡有頭緒了嗎？跟我說說你的想法——有幾個候選在猶豫、想看海還是想吃美食，都可以直接講。"
        val CITY_CHAT_OPENING_CHIPS = listOf("我有幾個候選在猶豫", "想放鬆看海", "想吃美食逛街") // TODO: replace with stringResource

        // 收斂輪固定三顆 chips 中的兩顆固定文字（後端強制覆寫，App 依文字穩定綁定行為）；
        // 第三顆「就去{城市}！」文字含城市名，UI 以「非這兩顆」判定為接受
        const val CITY_CHIP_SWAP = "換一個城市"
        const val CITY_CHIP_RESTART = "重新聊聊"

        // recommend-city messages 驗證上限（1~20 則）
        const val CITY_CHAT_MESSAGES_MAX = 20

        // recommend-city shown_cities 驗證上限（≤30 個）
        const val SHOWN_CITIES_MAX = 30

        // 換城上限：shown_cities 帶 5 個 = 最後一次換城（超過後端回 swap_limit_reached 固定文案不打 LLM）
        const val CITY_SWAP_MAX = 5

        // travel-revise messages 驗證上限（≤100 則），超過 400；優先保留最近的訊息
        const val TRAVEL_REVISE_MESSAGES_MAX = 100

        // 材料開場選項的「我想直接開始規劃」使用者泡泡文字
        // TODO: replace with stringResource
        const val OPENING_DIRECT_PLAN_LABEL = "我想直接開始規劃"

        // 問卷全部答完的收尾泡泡（之後出「開始規劃」主行動）
        // TODO: replace with stringResource
        const val PREFERENCE_COMPLETED_MESSAGE = "都記下來了！按「開始規劃」，我馬上把行程排出來～"
    }
}
