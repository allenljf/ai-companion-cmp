package com.allenljf.aicompanion.viewmodel

import com.allenljf.aicompanion.model.AiPartnerResult
import com.allenljf.aicompanion.model.CompanionAppearanceOption
import com.allenljf.aicompanion.model.CompanionTraitOption
import com.allenljf.aicompanion.model.QuizCompletionResult
import com.allenljf.aicompanion.model.QuizGalleryResult
import com.allenljf.aicompanion.model.QuizResult
import com.allenljf.aicompanion.model.SavedTripRecord
import com.allenljf.aicompanion.model.ShareImageV2Result
import com.allenljf.aicompanion.model.TravelDestinationOption

sealed interface PartnerState {
    data object Idle : PartnerState
    data object Loading : PartnerState
    data class Loaded(val partner: AiPartnerResult) : PartnerState
    data object Empty : PartnerState
    data object Error : PartnerState
}

data class CompanionCreationState(
    val selectedGender: CompanionAppearanceOption? = null,
    val selectedOutfit: CompanionAppearanceOption? = null,
    val selectedHairStyle: CompanionAppearanceOption? = null,
    val selectedHairColor: CompanionAppearanceOption? = null,
    val selectedPersonality: List<CompanionTraitOption> = emptyList(),
    val selectedSpeechStyle: CompanionTraitOption? = null,
    val companionName: String = "",
    // 由四個外觀維度 tag 組 key 從 ai-partner.avatars 解析的頭像 URL；未選齊為空字串
    val avatarUrl: String = ""
) {
    val isAppearanceComplete: Boolean
        get() = selectedGender != null &&
            selectedOutfit != null &&
            selectedHairStyle != null &&
            selectedHairColor != null

    val isAllDimensionsSelected: Boolean
        get() = isAppearanceComplete &&
            selectedPersonality.isNotEmpty() &&
            selectedSpeechStyle != null &&
            companionName.isNotBlank() &&
            companionName.length in NAME_MIN_LENGTH..NAME_MAX_LENGTH

    val isNameValid: Boolean
        get() = companionName.isEmpty() || companionName.length in NAME_MIN_LENGTH..NAME_MAX_LENGTH

    companion object {
        const val NAME_MIN_LENGTH = 1
        const val NAME_MAX_LENGTH = 10
        // 個性改為單選（API input 與本地儲存仍維持 List 型態）
        const val MAX_PERSONALITY_COUNT = 1
    }
}

sealed interface IntroductionState {
    data object Idle : IntroductionState
    data object Loading : IntroductionState
    // 後端軟失敗時 introduction 為固定文案，一樣直接顯示（isAiGenerated 僅供區分來源）
    data class Loaded(val introduction: String, val isAiGenerated: Boolean) : IntroductionState
    data object Error : IntroductionState // 硬失敗（網路錯誤/9999）→ UI 以本地固定文案顯示
}

sealed interface QuizState {
    data object Idle : QuizState
    data object Loading : QuizState
    data class Loaded(val quiz: QuizResult) : QuizState
    data object Error : QuizState
}

sealed interface AnalysisState {
    data object Idle : AnalysisState
    data object Analyzing : AnalysisState
    data class Success(val result: QuizCompletionResult) : AnalysisState
    data object SoftFailed : AnalysisState // fail_reason 有值 → 可重試
    data object Error : AnalysisState
}

sealed interface QuizGalleryState {
    data object Idle : QuizGalleryState
    data object Loading : QuizGalleryState
    data class Loaded(val result: QuizGalleryResult) : QuizGalleryState
    data object Error : QuizGalleryState
}

/**
 * 海報 hero 圖（T19）：不接輪詢，簡化版狀態機——測驗分析成功後在背景觸發一次，
 * 80 秒左右才會有結果，結果頁其餘內容不必等它（軟失敗契約：Failed 只代表無圖，其餘顯示不受影響）。
 * T20：Ready 帶完整 [ShareImageV2Result]（不只 heroUrl），結果頁沈浸式版面需要 stamp/tag icon/文案。
 */
sealed interface ShareImageV2State {
    data object Idle : ShareImageV2State
    data object Loading : ShareImageV2State
    data class Ready(val result: ShareImageV2Result) : ShareImageV2State
    data object Failed : ShareImageV2State
}

// ---------- Phase 2：行程規劃 ----------

/**
 * travel-summary 聊天室開場摘要狀態。
 * 補充資訊重打時新 summary「取代」前一版（畫面上只保留最新一句），App 端不需保存對話歷史。
 */
sealed interface TravelSummaryState {
    data object Idle : TravelSummaryState

    /** 生成中：[previousSummary]/[pendingNote] 非空 = 補充重打，畫面保留前一版摘要與使用者補充泡泡 */
    data class Loading(
        val previousSummary: String = "",
        val pendingNote: String = ""
    ) : TravelSummaryState

    /**
     * isSoftFail = fail_reason 有值，summary 為後端兜底文案（仍可顯示），可原樣重打重新生成。
     * isFromZero = 從零開始入口：顯示「玩測驗／我想直接開始規劃」雙按鈕（後者啟動 recommend-city 對話）。
     */
    data class Ready(
        val summary: String,
        val city: String,
        val isSoftFail: Boolean,
        val isFromZero: Boolean = false
    ) : TravelSummaryState

    data object Error : TravelSummaryState // 網路等硬失敗 → 可原樣重試

    data object InvalidRequest : TravelSummaryState // 422 驗證錯誤（client bug）→ 通用錯誤，不提供重試
}

/** recommend-city 聊天泡泡（UI 渲染用；完整 role 歷史由 ViewModel 內部保存供全量帶回）。 */
data class CityChatUiMessage(val fromMe: Boolean, val text: String)

/**
 * recommend-city 城市推薦多輪對話狀態（上限 5 輪由伺服器控管，收斂只看 isFinal）。
 * [fallbackReply]：軟失敗兜底文案，渲染泡泡但不進對話歷史（避免污染下一輪上下文）。
 * 收斂輪（isFinal=true）的 [quickReplies] 為後端強制覆寫的固定三顆：
 * 「就去{城市}！」／「換一個城市」／「重新聊聊」，UI 依文字綁定行為；
 * [canSwapCity]=false 表示已達換城上限（shown_cities 5 個）或 messages 20 則上限，隱藏換城入口；
 * [swapLimitText] 非空 = 後端回 swap_limit_reached 保底固定文案，UI 跳 dialog 引導重新聊聊。
 */
data class RecommendCityState(
    val active: Boolean = false,
    val messages: List<CityChatUiMessage> = emptyList(),
    val quickReplies: List<String> = emptyList(),
    val isWaiting: Boolean = false,
    val isError: Boolean = false, // 硬失敗 → 顯示重試（原樣重打同一份 messages）
    val fallbackReply: String = "",
    val recommendedCity: String = "",
    val cityReason: String = "",
    val isFinal: Boolean = false,
    val remainingRounds: Int = 0,
    val canSwapCity: Boolean = true,
    val swapLimitText: String = ""
)

/**
 * 「請{旅伴}幫我改」修改行程對話狀態（travel-revise）。
 * 每次開啟都是新對話；每成功一輪，變動的天立即 merge 進本地行程（[hasRevised] 供顯示「回去看修改結果」）。
 * [fallbackReply]：軟失敗兜底文案，渲染泡泡但不進對話歷史、不 merge（可原樣重打）。
 */
data class TripReviseState(
    val active: Boolean = false,
    val dayNumber: Int = 0,
    val messages: List<CityChatUiMessage> = emptyList(),
    val isWaiting: Boolean = false,
    val isError: Boolean = false, // 硬失敗 → 顯示重試（原樣重打）
    val fallbackReply: String = "",
    val hasRevised: Boolean = false
)

// ---------- 偏好問卷（聊天式，一次一題；純前端不打 API） ----------

// TODO: 題目與選項之後改接 DCS 動態設定（後端不驗證固定 key），目前依 phase2 API 文件範例寫死
data class PlanPreferenceQuestion(
    val key: String,
    val title: String,
    val options: List<String>
)

const val PREFERENCE_NOTES_KEY = "notes"

// notes 題（自由輸入）的「跳過」選項：點選時不把該題放進 preferences
const val PREFERENCE_SKIP_NOTES = "沒有特別需求" // TODO: replace with stringResource

val PLAN_PREFERENCE_QUESTIONS = listOf(
    PlanPreferenceQuestion(
        key = "duration",
        title = "這次旅行預計幾天？", // TODO: replace with stringResource / DCS
        options = listOf(
            "1~2 天（週末小旅行）",
            "3~5 天（短期旅遊）",
            "6~9 天（一般旅遊）",
            "10 天以上（深度旅遊）",
        ),
    ),
    PlanPreferenceQuestion(
        key = "budget",
        title = "這次旅行的預算大約是多少？", // TODO: replace with stringResource / DCS
        options = listOf(
            "經濟型（NT$10,000 以下）",
            "小資型（NT$10,000 ~ NT$30,000）",
            "舒適型（NT$30,000 ~ NT$80,000）",
            "豪華型（NT$80,000 以上）",
        ),
    ),
    PlanPreferenceQuestion(
        key = "pace",
        title = "你喜歡什麼樣的行程節奏？", // TODO: replace with stringResource / DCS
        options = listOf(
            "悠閒放鬆（每天 1~2 個景點）",
            "平衡探索（每天 3~4 個景點）",
            "緊湊充實（每天 5 個以上景點）",
        ),
    ),
    PlanPreferenceQuestion(
        key = "theme",
        title = "最吸引你的旅行主題是？", // TODO: replace with stringResource / DCS
        options = listOf(
            "城市文化（歷史、建築、美食）",
            "自然風景（山海、公園、步道）",
            "主題樂園與親子",
            "購物與時尚",
        ),
    ),
    PlanPreferenceQuestion(
        key = "priority",
        title = "你最在意什麼？", // TODO: replace with stringResource / DCS
        options = listOf(
            "美食與購物",
            "風景與拍照",
            "文化體驗",
            "放鬆休息",
        ),
    ),
    PlanPreferenceQuestion(
        key = PREFERENCE_NOTES_KEY,
        title = "還有什麼想法或特殊需求嗎？可以直接打字告訴我（例如：想帶長輩同行，不想自駕）", // TODO: replace with stringResource / DCS
        options = listOf(PREFERENCE_SKIP_NOTES),
    ),
)

/**
 * 聊天式偏好問卷狀態：一次出一題（泡泡＋選項 chips＋可打字），答完換下一題；
 * 全部答完（[isCompleted]）出現「開始規劃」主行動 → travel-guide。
 * 顯示文字（[messages]）與收集到的答案（ViewModel 內部 map）分開存取。
 */
data class PreferenceChatState(
    val active: Boolean = false,
    val messages: List<CityChatUiMessage> = emptyList(),
    val options: List<String> = emptyList(),
    val currentIndex: Int = 0,
    val isCompleted: Boolean = false
)

/**
 * travel-guide 行程生成狀態；SoftFailed 可原樣帶同一份 summary/preferences 重打。
 * Loaded 的 [messages]/[progressLabel]/[mainActionLabel] 供聊天室「完成時刻」渲染
 * （完成宣告泡泡＋行程摘要卡＋「看看完整行程」主行動）；從「我的旅程」點開時皆為空。
 */
sealed interface TravelGuideState {
    data object Idle : TravelGuideState
    data object Loading : TravelGuideState
    data class Loaded(
        val trip: SavedTripRecord,
        val messages: List<String> = emptyList(),
        val progressLabel: String = "",
        val mainActionLabel: String? = null
    ) : TravelGuideState
    data object SoftFailed : TravelGuideState // fail_reason 有值（days=[]）→ 顯示重試
    data object Error : TravelGuideState // 網路等硬失敗 → 可重試
    data object InvalidRequest : TravelGuideState // 400/110001 → 通用錯誤，不提供重試
}

/** 「帶訂單開場」狀態：抓訂單材料 → 丟 LLM 判斷目的地選項（travel-summary-from-orders，後端尚未實作）。 */
sealed interface OrderOpeningState {
    data object Idle : OrderOpeningState
    data object Loading : OrderOpeningState
    data class Ready(val greeting: String, val options: List<TravelDestinationOption>) : OrderOpeningState
    // 沒有即將出發的訂單（或訂單 API 取材料失敗）——非硬錯誤，畫面提示「改用一般規劃」
    data object NotAvailable : OrderOpeningState
    data object Error : OrderOpeningState
}
