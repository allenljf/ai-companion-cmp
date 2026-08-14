package com.kkday.feature.ai_companion.presentation.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.kkday.feature.ai_companion.viewModel.CityChatUiMessage
import com.kkday.feature.ai_companion.viewModel.PLAN_PREFERENCE_QUESTIONS
import com.kkday.feature.ai_companion.viewModel.PreferenceChatState
import com.kkday.feature.ai_companion.viewModel.RecommendCityState
import com.kkday.feature.ai_companion.viewModel.TravelGuideState
import com.kkday.feature.ai_companion.viewModel.TravelSummaryState
import com.kkday.feature.ai_companion.viewModel.TripReviseState
import com.kkday.model.companion.SavedTripRecord
import com.kkday.model.companion.TravelGuideDay
import com.kkday.model.companion.TravelGuideDayItem

/**
 * Phase 2 行程規劃各畫面/狀態的 Compose Preview（Android Studio 直接預覽，不需跑模擬器）。
 * 頭像 URL 給空字串會落到占位符呈現；假資料僅供 Preview，正式資料來自 travel-summary / travel-guide API。
 */

private const val PREVIEW_W = 390
private const val PREVIEW_H = 844

private val PREVIEW_TRIP = SavedTripRecord(
    title = "小旅 × 你的大阪",
    city = "大阪",
    totalDays = 3,
    days = listOf(
        TravelGuideDay(
            day = 1, status = "planned", kind = "arrival", halfDay = true,
            items = listOf(
                TravelGuideDayItem(text = "抵達大阪並辦理入境", type = "logistics", note = "依航班調整"),
                TravelGuideDayItem(
                    text = "道頓堀晚間漫遊，燈牌配章魚燒", type = "spot", timeBand = "晚上",
                    lat = 34.6687, lng = 135.5013,
                ),
            ),
        ),
        TravelGuideDay(
            day = 2, status = "planned", kind = "normal",
            items = listOf(
                TravelGuideDayItem(text = "黑門市場邊走邊吃當早餐", type = "meal", timeBand = "上午"),
                TravelGuideDayItem(
                    text = "大阪城公園散步", type = "spot", timeBand = "下午",
                    note = "天守閣配護城河，散步消食剛剛好", lat = 34.6873, lng = 135.5262,
                ),
            ),
        ),
        TravelGuideDay(
            day = 3, status = "planned", kind = "departure", halfDay = true,
            items = listOf(
                TravelGuideDayItem(
                    text = "心齋橋筋商店街最後採買", type = "spot", timeBand = "上午",
                    lat = 34.6737, lng = 135.5010,
                ),
                TravelGuideDayItem(text = "前往機場", type = "logistics", timeBand = "下午", note = "依航班調整"),
            ),
        ),
    ),
    createdAt = 0L,
)

// ---------- ②-1 匯入你的 AI 行程 ----------

@Preview(name = "匯入・貼上", showBackground = true, widthDp = PREVIEW_W, heightDp = PREVIEW_H)
@Composable
private fun PreviewImportEmpty() {
    ImportItineraryScreen(avatarUrl = "", onClose = {}, onImport = {})
}

// ---------- ②-2 開場摘要（travel-summary） ----------

@Preview(name = "摘要・生成中", showBackground = true, widthDp = PREVIEW_W, heightDp = PREVIEW_H)
@Composable
private fun PreviewPlanChatLoading() {
    PlanChatScreen(
        state = TravelSummaryState.Loading(),
        cityState = RecommendCityState(),
        avatarUrl = "",
        onBack = {}, onSendInput = {}, onRetrySummary = {}, onRetryCity = {},
        onPlayQuiz = {}, onStartCityChat = {}, onSwapCity = {}, onRestartCityChat = {}, onStartPreferences = {},
    )
}

@Preview(name = "摘要・就緒", showBackground = true, widthDp = PREVIEW_W, heightDp = PREVIEW_H)
@Composable
private fun PreviewPlanChatReady() {
    PlanChatScreen(
        state = TravelSummaryState.Ready(
            summary = "大阪根本是為你這種吃貨開的城市！要不要就從這裡開始排？",
            city = "大阪",
            isSoftFail = false,
        ),
        cityState = RecommendCityState(),
        avatarUrl = "",
        onBack = {}, onSendInput = {}, onRetrySummary = {}, onRetryCity = {},
        onPlayQuiz = {}, onStartCityChat = {}, onSwapCity = {}, onRestartCityChat = {}, onStartPreferences = {},
    )
}

@Preview(name = "從零開始・玩測驗／直接開始規劃", showBackground = true, widthDp = PREVIEW_W, heightDp = PREVIEW_H)
@Composable
private fun PreviewPlanChatFromZero() {
    PlanChatScreen(
        state = TravelSummaryState.Ready(
            summary = "好～從零開始最好玩了。我們先玩個三分鐘的測驗，我幫你找出命定城市，找到再一起排？",
            city = "",
            isSoftFail = false,
            isFromZero = true,
        ),
        cityState = RecommendCityState(),
        avatarUrl = "",
        onBack = {}, onSendInput = {}, onRetrySummary = {}, onRetryCity = {},
        onPlayQuiz = {}, onStartCityChat = {}, onSwapCity = {}, onRestartCityChat = {}, onStartPreferences = {},
    )
}

@Preview(name = "城市收斂對話・進行中", showBackground = true, widthDp = PREVIEW_W, heightDp = PREVIEW_H)
@Composable
private fun PreviewPlanChatCityChatting() {
    PlanChatScreen(
        state = TravelSummaryState.Ready(
            summary = "好～從零開始最好玩了。",
            city = "",
            isSoftFail = false,
            isFromZero = true,
        ),
        cityState = RecommendCityState(
            active = true,
            messages = listOf(
                CityChatUiMessage(fromMe = false, text = "想去哪裡有頭緒了嗎？跟我說說你的想法。"),
                CityChatUiMessage(fromMe = true, text = "京都大阪在猶豫"),
                CityChatUiMessage(fromMe = false, text = "京都跟大阪啊？兩個我都愛。你想要安靜慢步調，還是熱鬧吃到飽？"),
            ),
            quickReplies = listOf("安靜慢步調", "熱鬧吃到飽"),
            remainingRounds = 3,
        ),
        avatarUrl = "",
        onBack = {}, onSendInput = {}, onRetrySummary = {}, onRetryCity = {},
        onPlayQuiz = {}, onStartCityChat = {}, onSwapCity = {}, onRestartCityChat = {}, onStartPreferences = {},
    )
}

@Preview(name = "城市收斂對話・已收斂", showBackground = true, widthDp = PREVIEW_W, heightDp = PREVIEW_H)
@Composable
private fun PreviewPlanChatCityFinal() {
    PlanChatScreen(
        state = TravelSummaryState.Ready(
            summary = "好～從零開始最好玩了。",
            city = "",
            isSoftFail = false,
            isFromZero = true,
        ),
        cityState = RecommendCityState(
            active = true,
            messages = listOf(
                CityChatUiMessage(fromMe = true, text = "比較想放鬆一點"),
                CityChatUiMessage(fromMe = false, text = "那我推你去京都——町家、寺院跟巷弄最適合想慢下來的人。就定京都！"),
            ),
            quickReplies = listOf("就去京都！", "換一個城市", "重新聊聊"),
            recommendedCity = "京都",
            cityReason = "安靜慢步調最適合京都",
            isFinal = true,
        ),
        avatarUrl = "",
        onBack = {}, onSendInput = {}, onRetrySummary = {}, onRetryCity = {},
        onPlayQuiz = {}, onStartCityChat = {}, onSwapCity = {}, onRestartCityChat = {}, onStartPreferences = {},
    )
}

@Preview(name = "摘要・補充重新生成中", showBackground = true, widthDp = PREVIEW_W, heightDp = PREVIEW_H)
@Composable
private fun PreviewPlanChatSupplementing() {
    PlanChatScreen(
        state = TravelSummaryState.Loading(
            previousSummary = "大阪根本是為你這種吃貨開的城市！要不要就從這裡開始排？",
            pendingNote = "我還想多排一些自然景觀",
        ),
        cityState = RecommendCityState(),
        avatarUrl = "",
        onBack = {}, onSendInput = {}, onRetrySummary = {}, onRetryCity = {},
        onPlayQuiz = {}, onStartCityChat = {}, onSwapCity = {}, onRestartCityChat = {}, onStartPreferences = {},
    )
}

@Preview(name = "摘要・軟失敗（兜底文案＋重新生成）", showBackground = true, widthDp = PREVIEW_W, heightDp = PREVIEW_H)
@Composable
private fun PreviewPlanChatSoftFail() {
    PlanChatScreen(
        state = TravelSummaryState.Ready(
            summary = "嗯…我這邊看得不太清楚，能再多說一點你的計畫嗎？",
            city = "",
            isSoftFail = true,
        ),
        cityState = RecommendCityState(),
        avatarUrl = "",
        onBack = {}, onSendInput = {}, onRetrySummary = {}, onRetryCity = {},
        onPlayQuiz = {}, onStartCityChat = {}, onSwapCity = {}, onRestartCityChat = {}, onStartPreferences = {},
    )
}

@Preview(name = "摘要・硬失敗（重試）", showBackground = true, widthDp = PREVIEW_W, heightDp = PREVIEW_H)
@Composable
private fun PreviewPlanChatError() {
    PlanChatScreen(
        state = TravelSummaryState.Error,
        cityState = RecommendCityState(),
        avatarUrl = "",
        onBack = {}, onSendInput = {}, onRetrySummary = {}, onRetryCity = {},
        onPlayQuiz = {}, onStartCityChat = {}, onSwapCity = {}, onRestartCityChat = {}, onStartPreferences = {},
    )
}

// ---------- ②-3 偏好問卷（聊天式一次一題） ----------

@Preview(name = "偏好問卷・第 2 題進行中", showBackground = true, widthDp = PREVIEW_W, heightDp = PREVIEW_H)
@Composable
private fun PreviewPreferenceChatting() {
    PlanChatScreen(
        state = TravelSummaryState.Ready(summary = "大阪根本是為你這種吃貨開的城市！", city = "大阪", isSoftFail = false),
        cityState = RecommendCityState(),
        preferenceState = PreferenceChatState(
            active = true,
            messages = listOf(
                CityChatUiMessage(fromMe = false, text = "（1/6）這次旅行預計幾天？"),
                CityChatUiMessage(fromMe = true, text = "6~9 天（一般旅遊）"),
                CityChatUiMessage(fromMe = false, text = "（2/6）這次旅行的預算大約是多少？"),
            ),
            options = PLAN_PREFERENCE_QUESTIONS[1].options,
            currentIndex = 1,
        ),
        avatarUrl = "",
        onBack = {}, onSendInput = {}, onRetrySummary = {}, onRetryCity = {},
        onPlayQuiz = {}, onStartCityChat = {}, onSwapCity = {}, onRestartCityChat = {}, onStartPreferences = {},
    )
}

@Preview(name = "偏好問卷・答完（開始規劃）", showBackground = true, widthDp = PREVIEW_W, heightDp = PREVIEW_H)
@Composable
private fun PreviewPreferenceCompleted() {
    PlanChatScreen(
        state = TravelSummaryState.Ready(summary = "大阪根本是為你這種吃貨開的城市！", city = "大阪", isSoftFail = false),
        cityState = RecommendCityState(),
        preferenceState = PreferenceChatState(
            active = true,
            messages = listOf(
                CityChatUiMessage(fromMe = false, text = "（6/6）還有什麼想法或特殊需求嗎？"),
                CityChatUiMessage(fromMe = true, text = "想帶長輩同行，不想自駕"),
                CityChatUiMessage(fromMe = false, text = "都記下來了！按「開始規劃」，我馬上把行程排出來～"),
            ),
            currentIndex = PLAN_PREFERENCE_QUESTIONS.size,
            isCompleted = true,
        ),
        avatarUrl = "",
        onBack = {}, onSendInput = {}, onRetrySummary = {}, onRetryCity = {},
        onPlayQuiz = {}, onStartCityChat = {}, onSwapCity = {}, onRestartCityChat = {}, onStartPreferences = {},
    )
}

// ---------- ③ 行程成果頁 / ④ 每日詳細 ----------

@Preview(name = "完成時刻（聊天室：摘要卡＋宣告＋主行動）", showBackground = true, widthDp = PREVIEW_W, heightDp = PREVIEW_H)
@Composable
private fun PreviewPlanFinishInChat() {
    PlanChatScreen(
        state = TravelSummaryState.Ready(summary = "大阪根本是為你這種吃貨開的城市！", city = "大阪", isSoftFail = false),
        cityState = RecommendCityState(),
        preferenceState = PreferenceChatState(
            active = true,
            messages = listOf(CityChatUiMessage(fromMe = true, text = "想帶長輩同行，不想自駕")),
            currentIndex = PLAN_PREFERENCE_QUESTIONS.size,
            isCompleted = true,
        ),
        guideState = TravelGuideState.Loaded(
            trip = PREVIEW_TRIP,
            messages = listOf("行程排好了！這三天以美食為主軸，順路排了黑門市場跟道頓堀。"),
            progressLabel = "已排 3/3 天",
            mainActionLabel = "看看完整行程",
        ),
        avatarUrl = "",
        onBack = {}, onSendInput = {}, onRetrySummary = {}, onRetryCity = {},
        onPlayQuiz = {}, onStartCityChat = {}, onSwapCity = {}, onRestartCityChat = {}, onStartPreferences = {},
    )
}

@Preview(name = "成果頁・已生成", showBackground = true, widthDp = PREVIEW_W, heightDp = PREVIEW_H)
@Composable
private fun PreviewTripLoaded() {
    TripResultScreen(
        state = TravelGuideState.Loaded(PREVIEW_TRIP),
        reviseState = TripReviseState(),
        avatarUrl = "",
        companionName = "小旅",
        onClose = {}, onRetry = {}, onSave = {},
        onStartRevise = {}, onSendRevise = {}, onRetryRevise = {}, onDismissRevise = {},
    )
}

@Preview(name = "成果頁・生成失敗（重試）", showBackground = true, widthDp = PREVIEW_W, heightDp = PREVIEW_H)
@Composable
private fun PreviewTripFailed() {
    TripResultScreen(
        state = TravelGuideState.SoftFailed,
        reviseState = TripReviseState(),
        avatarUrl = "",
        companionName = "小旅",
        onClose = {}, onRetry = {}, onSave = {},
        onStartRevise = {}, onSendRevise = {}, onRetryRevise = {}, onDismissRevise = {},
    )
}

