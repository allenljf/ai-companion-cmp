package com.allenljf.aicompanion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.allenljf.aicompanion.model.TravelDestinationOption
import com.allenljf.aicompanion.theme.Tokens
import com.allenljf.aicompanion.ui.components.AppButton
import com.allenljf.aicompanion.ui.components.AppDialog
import com.allenljf.aicompanion.ui.components.ButtonSizeType
import com.allenljf.aicompanion.ui.components.ButtonState
import com.allenljf.aicompanion.ui.components.ButtonType
import com.allenljf.aicompanion.ui.components.DialogHeaderType
import com.allenljf.aicompanion.viewmodel.AiCompanionViewModel
import com.allenljf.aicompanion.viewmodel.CityChatUiMessage
import com.allenljf.aicompanion.viewmodel.OrderOpeningState
import com.allenljf.aicompanion.viewmodel.PreferenceChatState
import com.allenljf.aicompanion.viewmodel.RecommendCityState
import com.allenljf.aicompanion.viewmodel.TravelGuideState
import com.allenljf.aicompanion.viewmodel.TravelSummaryState
import org.jetbrains.compose.resources.painterResource
import aicompanion.shared.generated.resources.Res
import aicompanion.shared.generated.resources.ic_arrow_left_line
import aicompanion.shared.generated.resources.ic_arrow_right_line
import aicompanion.shared.generated.resources.ic_cross_line
import aicompanion.shared.generated.resources.ic_image_line
import kotlinx.coroutines.delay

/**
 * Phase 2 行程規劃（試驗階段，三支無狀態 API）：
 * travel-summary 開場摘要（可補充重新生成）→（從零開始可走 recommend-city 多輪收斂城市，上限 5 輪）
 * → Q1~Q6 偏好問卷（聊天式一次一題，純前端）→ travel-guide 一次生成完整行程。
 */

internal object CompanionPlanFeatureFlags {
    // 截圖上傳：後端尚無可取得外部可讀 URL 的上傳端點（phase2 API 文件未定案事項 1），
    // 對齊前僅開放 source_type=text；UI 保留，flag 開啟後接檔案選擇器與上傳串接
    // TODO: 後端上傳方案定案後開啟並補 image_urls 串接
    const val IMAGE_IMPORT_ENABLED = false
}

// ---------- 共用元件 ----------

/** 規劃對話頂列（56dp）：返回＋旅伴頭像＋標題。 */
@Composable
internal fun PlanTopBar(
    title: String,
    avatarUrl: String,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Tokens.colorWhite)
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = Tokens.spacing100),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack, modifier = Modifier.testTag("companion_plan_back_btn")) {
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_left_line),
                contentDescription = null,
                tint = Tokens.colorTextDarker,
            )
        }
        CompanionAsyncImage(
            url = avatarUrl,
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(Tokens.colorBackgroundPrimaryLighter),
            placeholder = { Text("?", color = Tokens.colorTextPrimaryDark) },
        )
        Spacer(Modifier.width(Tokens.spacing100))
        Text(
            title,
            fontWeight = FontWeight(Tokens.fontWeightBold),
            fontSize = Tokens.fontSize4,
            color = Tokens.colorTextDarker,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .testTag("companion_plan_progress"),
        )
    }
}

/** 對話氣泡：旅伴在左（帶頭像）、使用者在右。 */
@Composable
internal fun PlanChatBubble(
    fromMe: Boolean,
    avatarUrl: String,
    text: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Tokens.spacing150),
        horizontalArrangement = if (fromMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top,
    ) {
        if (!fromMe) {
            CompanionAsyncImage(
                url = avatarUrl,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Tokens.colorBackgroundPrimaryLighter),
                placeholder = { Text("?", color = Tokens.colorTextPrimaryDark) },
            )
            Spacer(Modifier.width(Tokens.spacing100))
        }
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = if (fromMe) Tokens.radiusLg else Tokens.radiusSm,
                        topEnd = if (fromMe) Tokens.radiusSm else Tokens.radiusLg,
                        bottomStart = Tokens.radiusLg,
                        bottomEnd = Tokens.radiusLg,
                    ),
                )
                .background(
                    if (fromMe) Tokens.colorBackgroundPrimaryLighter
                    else Tokens.colorWhite,
                )
                .border(
                    1.dp,
                    if (fromMe) Tokens.colorBorderPrimaryLight else Tokens.colorBorderLight,
                    RoundedCornerShape(
                        topStart = if (fromMe) Tokens.radiusLg else Tokens.radiusSm,
                        topEnd = if (fromMe) Tokens.radiusSm else Tokens.radiusLg,
                        bottomStart = Tokens.radiusLg,
                        bottomEnd = Tokens.radiusLg,
                    ),
                )
                .padding(horizontal = Tokens.spacing150, vertical = Tokens.spacing100),
        ) {
            Text(
                text,
                color = Tokens.colorTextDarker,
                fontSize = Tokens.fontSize3,
            )
        }
    }
}

/** 旅伴打字中氣泡（三點輪播）。 */
@Composable
internal fun PlanTypingBubble(avatarUrl: String) {
    var dots by remember { mutableStateOf(1) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(400)
            dots = dots % 3 + 1
        }
    }
    PlanChatBubble(fromMe = false, avatarUrl = avatarUrl, text = "・".repeat(dots))
}

/** 快速回覆 chips：一般為白底描邊、主行動為實心強調。 */
@Composable
internal fun PlanChoiceChip(
    label: String,
    solid: Boolean = false,
    testTag: String,
    onClick: () -> Unit,
) {
    Text(
        label,
        color = if (solid) Tokens.colorWhite else Tokens.colorTextPrimaryDark,
        fontSize = Tokens.fontSize3,
        fontWeight = FontWeight(Tokens.fontWeightMediumAndroid),
        modifier = Modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(999.dp))
            .background(
                if (solid) Tokens.colorBackgroundPrimaryButton
                else Tokens.colorWhite,
            )
            .border(
                1.dp,
                Tokens.colorBorderPrimaryMedium,
                RoundedCornerShape(999.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = Tokens.spacing200, vertical = Tokens.spacing150),
    )
}

/** 底部輸入列：「資訊不夠，我想補充」的補充說明輸入（送出後帶 previous_summary+note 重打 travel-summary）。 */
@Composable
internal fun PlanInputBar(
    enabled: Boolean,
    onSend: (String) -> Unit,
    placeholder: String = "想補充什麼跟我說…", // TODO: i18n
) {
    var text by remember { mutableStateOf("") }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Tokens.colorWhite)
            .navigationBarsPadding()
            .padding(horizontal = Tokens.spacing200, vertical = Tokens.spacing150),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 44.dp)
                .clip(RoundedCornerShape(22.dp))
                .border(1.dp, Tokens.colorBorderLight, RoundedCornerShape(22.dp))
                .background(Tokens.colorBackgroundSurfaceLight)
                .padding(horizontal = Tokens.spacing200, vertical = Tokens.spacing100),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = text,
                onValueChange = { if (it.length <= SUPPLEMENT_MAX_LENGTH) text = it },
                enabled = enabled,
                textStyle = TextStyle(
                    color = Tokens.colorTextDarker,
                    fontSize = Tokens.fontSize3,
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("companion_plan_input"),
                decorationBox = { innerTextField ->
                    if (text.isEmpty()) {
                        Text(
                            placeholder,
                            color = Tokens.colorTextMedium,
                            fontSize = Tokens.fontSize3,
                        )
                    }
                    innerTextField()
                },
            )
            Text(
                "${text.length}/$SUPPLEMENT_MAX_LENGTH",
                color = Tokens.colorTextMedium,
                fontSize = Tokens.fontSize1,
            )
        }
        Spacer(Modifier.width(Tokens.spacing100))
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    if (enabled && text.isNotBlank()) Tokens.colorBackgroundPrimaryButton
                    else Tokens.colorBackgroundSurfaceMedium,
                )
                .clickable(enabled = enabled && text.isNotBlank()) {
                    onSend(text)
                    text = ""
                }
                .testTag("companion_plan_send_btn"),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_right_line),
                contentDescription = null,
                tint = Tokens.colorWhite,
                modifier = Modifier.size(Tokens.dimensionIconSm),
            )
        }
    }
}

private const val SUPPLEMENT_MAX_LENGTH = 300

// ---------- ②-1 匯入你的 AI 行程 ----------

/**
 * 匯入畫面：貼上行程文字 →「開始解析」→ travel-summary（entry_type=imported_itinerary, source_type=text）。
 * 截圖上傳 UI 以 [CompanionPlanFeatureFlags.IMAGE_IMPORT_ENABLED] 擋住（後端上傳端點未定案）。
 */
@Composable
internal fun ImportItineraryScreen(
    avatarUrl: String,
    onClose: () -> Unit,
    onImport: (String) -> Unit,
) {
    var pastedText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Tokens.colorBackgroundSurfaceLight)
            .testTag("companion_import_screen"),
    ) {
        // 標題列
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Tokens.colorWhite)
                .statusBarsPadding()
                .padding(horizontal = Tokens.spacing200, vertical = Tokens.spacing100),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "匯入你的 AI 行程", // TODO: i18n
                fontWeight = FontWeight(Tokens.fontWeightBold),
                fontSize = Tokens.fontSize4,
                color = Tokens.colorTextDarker,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onClose, modifier = Modifier.testTag("companion_import_close_btn")) {
                Icon(
                    painter = painterResource(Res.drawable.ic_cross_line),
                    contentDescription = null,
                    tint = Tokens.colorTextMedium,
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(Tokens.spacing200),
        ) {
            PlanChatBubble(
                fromMe = false,
                avatarUrl = avatarUrl,
                text = "把 ChatGPT 或其他 AI 排好的行程貼進來，我看完就更懂你的旅行胃口，直接接著一起排。", // TODO: i18n
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 160.dp)
                    .clip(RoundedCornerShape(Tokens.radiusLg))
                    .border(1.dp, Tokens.colorBorderLight, RoundedCornerShape(Tokens.radiusLg))
                    .background(Tokens.colorWhite)
                    .padding(Tokens.spacing200),
            ) {
                BasicTextField(
                    value = pastedText,
                    onValueChange = { pastedText = it },
                    textStyle = TextStyle(
                        color = Tokens.colorTextDarker,
                        fontSize = Tokens.fontSize3,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("companion_import_paste_input"),
                    decorationBox = { innerTextField ->
                        if (pastedText.isEmpty()) {
                            Text(
                                "把你的行程貼在這裡…", // TODO: i18n
                                color = Tokens.colorTextMedium,
                                fontSize = Tokens.fontSize3,
                            )
                        }
                        innerTextField()
                    },
                )
            }

            if (CompanionPlanFeatureFlags.IMAGE_IMPORT_ENABLED) {
                Spacer(Modifier.height(Tokens.spacing150))
                Box(Modifier.testTag("companion_import_upload_btn")) {
                    AppButton(
                        buttonText = "上傳截圖", // TODO: i18n
                        buttonType = ButtonType.SECONDARY,
                        buttonState = ButtonState.ENABLED,
                        buttonSizeType = ButtonSizeType.Sm,
                        leadingIcon = painterResource(Res.drawable.ic_image_line),
                        onClick = {
                            // TODO: 待後端提供圖片上傳端點後，接檔案選擇器 → 上傳取得 URL → source_type=image
                        },
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Tokens.colorWhite)
                .navigationBarsPadding()
                .padding(horizontal = Tokens.spacing300, vertical = Tokens.spacing200)
                .testTag("companion_import_parse_btn"),
        ) {
            AppButton(
                buttonText = "開始解析", // TODO: i18n
                buttonType = ButtonType.PRIMARY,
                buttonState = if (pastedText.isNotBlank()) ButtonState.ENABLED else ButtonState.DISABLED,
                buttonSizeType = ButtonSizeType.Md,
                isFullWidth = true,
                onClick = { onImport(pastedText) },
            )
        }
    }
}

// ---------- ②-1b 帶訂單開場（travel-summary-from-orders，demo，後端尚未實作） ----------

/**
 * demo「帶訂單開場」：抓即將出發訂單材料 → LLM 判斷目的地選項（1-3 個城市）→ 使用者點一個開始規劃。
 * NotAvailable／Error 都提供「改用一般規劃」逃生門，避免卡在無法完成的畫面（後端端點尚未實作）。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun OrderOpeningScreen(
    avatarUrl: String,
    state: OrderOpeningState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onSelectOption: (TravelDestinationOption) -> Unit,
    onDirectPlan: () -> Unit,
    onPlanFromZeroInstead: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Tokens.colorBackgroundSurfaceLight)
            .testTag("companion_order_opening_screen"),
    ) {
        PlanTopBar(
            // 帶訂單／心願清單／瀏覽記錄三種材料開場共用此畫面，標題用通用文案
            title = "一起規劃旅遊行程", // TODO: i18n
            avatarUrl = avatarUrl,
            onBack = onBack,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Tokens.spacing200, vertical = Tokens.spacing100),
        ) {
            when (state) {
                is OrderOpeningState.Idle, is OrderOpeningState.Loading -> {
                    PlanTypingBubble(avatarUrl = avatarUrl)
                }

                is OrderOpeningState.Ready -> {
                    PlanChatBubble(fromMe = false, avatarUrl = avatarUrl, text = state.greeting)
                    if (state.options.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(Tokens.spacing100),
                            verticalArrangement = Arrangement.spacedBy(Tokens.spacing100),
                        ) {
                            state.options.forEachIndexed { index, option ->
                                PlanChoiceChip(
                                    label = option.city,
                                    testTag = "companion_order_opening_option_$index",
                                    onClick = { onSelectOption(option) },
                                )
                            }
                            // 不選推薦城市：直接進城市收斂對話（開場對話保留接續）
                            PlanChoiceChip(
                                label = "我想直接開始規劃", // TODO: i18n
                                testTag = "companion_order_opening_direct_chip",
                                onClick = onDirectPlan,
                            )
                        }
                    }
                }

                is OrderOpeningState.NotAvailable -> {
                    PlanChatBubble(
                        fromMe = false,
                        avatarUrl = avatarUrl,
                        // TODO: i18n
                        text = "咦，我沒找到可以參考的資料耶，那我們直接開始規劃新行程吧！",
                    )
                }

                is OrderOpeningState.Error -> {
                    PlanChatBubble(
                        fromMe = false,
                        avatarUrl = avatarUrl,
                        text = "連線好像出了點問題，再試一次好嗎？", // TODO: i18n
                    )
                    Spacer(Modifier.height(Tokens.spacing100))
                    Box(Modifier.testTag("companion_order_opening_retry_btn")) {
                        AppButton(
                            buttonText = "重試", // TODO: i18n
                            buttonType = ButtonType.SECONDARY,
                            buttonState = ButtonState.ENABLED,
                            buttonSizeType = ButtonSizeType.Sm,
                            onClick = onRetry,
                        )
                    }
                }
            }

            // Ready 但沒有任何城市選項（LLM 軟失敗，greeting 為兜底文案）也給逃生門
            if (state is OrderOpeningState.NotAvailable || state is OrderOpeningState.Error ||
                (state is OrderOpeningState.Ready && state.options.isEmpty())
            ) {
                Spacer(Modifier.height(Tokens.spacing100))
                Box(Modifier.testTag("companion_order_opening_plan_from_zero_btn")) {
                    AppButton(
                        buttonText = "改用一般規劃", // TODO: i18n
                        buttonType = ButtonType.PRIMARY,
                        buttonState = ButtonState.ENABLED,
                        buttonSizeType = ButtonSizeType.Sm,
                        onClick = onPlanFromZeroInstead,
                    )
                }
            }
        }
    }
}

// ---------- ②-2 開場摘要（travel-summary） ----------

/**
 * 聊天室開場摘要：第一則訊息 = travel-summary 的 summary。
 * - A/B 入口：補充說明由底部輸入列送出（帶 previous_summary+note 重打，新摘要「取代」前一句），
 *   確認後「開始規劃」啟動聊天式問卷。
 * - 從零開始（isFromZero）：雙按鈕「玩測驗」（同旅伴主頁測驗入口）／「我想直接開始規劃」
 *   （啟動 recommend-city 多輪城市收斂，上限 5 輪、收斂只看 isFinal），收斂後「就去{城市}！」啟動問卷。
 * - 偏好問卷：聊天式一次一題（泡泡＋選項 chips＋可打字），答完換下一題；
 *   全部答完出「開始規劃」主行動 → travel-guide。
 * - 軟失敗（fail_reason）顯示兜底文案＋「重新生成」chip。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PlanChatScreen(
    state: TravelSummaryState,
    cityState: RecommendCityState,
    preferenceState: PreferenceChatState = PreferenceChatState(),
    guideState: TravelGuideState = TravelGuideState.Idle,
    // 材料開場（帶訂單/心願清單/瀏覽紀錄）帶過來的前導對話（greeting＋使用者的選擇），
    // 顯示在聊天室最上方讓對話接續往下長，而不是換頁重來
    preludeMessages: List<CityChatUiMessage> = emptyList(),
    avatarUrl: String,
    onBack: () -> Unit,
    onSendInput: (String) -> Unit,
    onRetrySummary: () -> Unit,
    onRetryCity: () -> Unit,
    onPlayQuiz: () -> Unit,
    onStartCityChat: () -> Unit,
    onSwapCity: () -> Unit,
    onRestartCityChat: () -> Unit,
    onStartPreferences: () -> Unit,
    onSubmitPreferences: () -> Unit = {},
    onRetryGuide: () -> Unit = {},
    onViewTrip: () -> Unit = {},
) {
    val scrollState = rememberScrollState()
    LaunchedEffect(
        state,
        cityState.messages.size,
        cityState.isWaiting,
        cityState.fallbackReply,
        preferenceState.messages.size,
        guideState,
    ) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Tokens.colorBackgroundSurfaceLight)
            .testTag("companion_plan_chat_screen"),
    ) {
        PlanTopBar(
            title = "一起規劃旅遊行程", // TODO: i18n
            avatarUrl = avatarUrl,
            onBack = onBack,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = Tokens.spacing200, vertical = Tokens.spacing100),
        ) {
            preludeMessages.forEach { message ->
                PlanChatBubble(fromMe = message.fromMe, avatarUrl = avatarUrl, text = message.text)
            }
            when (state) {
                is TravelSummaryState.Loading -> {
                    // 補充重打：保留前一版摘要與使用者補充泡泡；首次載入只顯示打字中
                    if (state.previousSummary.isNotBlank()) {
                        PlanChatBubble(fromMe = false, avatarUrl = avatarUrl, text = state.previousSummary)
                    }
                    if (state.pendingNote.isNotBlank()) {
                        PlanChatBubble(fromMe = true, avatarUrl = avatarUrl, text = state.pendingNote)
                    }
                    PlanTypingBubble(avatarUrl = avatarUrl)
                }

                is TravelSummaryState.Ready -> {
                    PlanChatBubble(fromMe = false, avatarUrl = avatarUrl, text = state.summary)
                    if (!cityState.active && !preferenceState.active) {
                        FlowRow(
                            modifier = Modifier.padding(start = 36.dp, top = Tokens.spacing050),
                            horizontalArrangement = Arrangement.spacedBy(Tokens.spacing100),
                            verticalArrangement = Arrangement.spacedBy(Tokens.spacing100),
                        ) {
                            if (state.isFromZero) {
                                // 玩測驗＝旅伴主頁「找到我的旅行 DNA 及命定旅程」同一條路
                                PlanChoiceChip(
                                    label = "玩測驗", // TODO: i18n
                                    solid = true,
                                    testTag = "companion_plan_quiz_chip",
                                    onClick = onPlayQuiz,
                                )
                                PlanChoiceChip(
                                    label = "我想直接開始規劃", // TODO: i18n
                                    testTag = "companion_plan_main_chip",
                                    onClick = onStartCityChat,
                                )
                            } else {
                                PlanChoiceChip(
                                    label = "開始規劃", // TODO: i18n
                                    solid = true,
                                    testTag = "companion_plan_main_chip",
                                    onClick = onStartPreferences,
                                )
                                if (state.isSoftFail) {
                                    PlanChoiceChip(
                                        label = "重新生成", // TODO: i18n
                                        testTag = "companion_plan_retry_chip",
                                        onClick = onRetrySummary,
                                    )
                                }
                            }
                        }
                    }
                }

                TravelSummaryState.Error -> {
                    PlanChatBubble(
                        fromMe = false,
                        avatarUrl = avatarUrl,
                        text = "連線好像出了點問題，再試一次好嗎？", // TODO: i18n
                    )
                    Box(Modifier.padding(start = 36.dp, top = Tokens.spacing050)) {
                        PlanChoiceChip(
                            label = "重試", // TODO: i18n
                            solid = true,
                            testTag = "companion_plan_retry_chip",
                            onClick = onRetrySummary,
                        )
                    }
                }

                TravelSummaryState.InvalidRequest -> {
                    // 400/110001：request 本身有問題（client bug），重試也不會成功，只給通用錯誤
                    PlanChatBubble(
                        fromMe = false,
                        avatarUrl = avatarUrl,
                        text = "發生錯誤，請稍後再試。", // TODO: i18n
                    )
                }

                TravelSummaryState.Idle -> Unit
            }

            // recommend-city 多輪城市收斂對話
            if (cityState.active) {
                cityState.messages.forEach { message ->
                    PlanChatBubble(fromMe = message.fromMe, avatarUrl = avatarUrl, text = message.text)
                }
                when {
                    cityState.isWaiting -> PlanTypingBubble(avatarUrl = avatarUrl)

                    // 軟失敗兜底文案：照常渲染泡泡（不進歷史），可原樣重打
                    cityState.fallbackReply.isNotBlank() -> {
                        PlanChatBubble(fromMe = false, avatarUrl = avatarUrl, text = cityState.fallbackReply)
                        Box(Modifier.padding(start = 36.dp, top = Tokens.spacing050)) {
                            PlanChoiceChip(
                                label = "重試", // TODO: i18n
                                solid = true,
                                testTag = "companion_city_retry_chip",
                                onClick = onRetryCity,
                            )
                        }
                    }

                    cityState.isError -> {
                        PlanChatBubble(
                            fromMe = false,
                            avatarUrl = avatarUrl,
                            text = "連線好像出了點問題，再試一次好嗎？", // TODO: i18n
                        )
                        Box(Modifier.padding(start = 36.dp, top = Tokens.spacing050)) {
                            PlanChoiceChip(
                                label = "重試", // TODO: i18n
                                solid = true,
                                testTag = "companion_city_retry_chip",
                                onClick = onRetryCity,
                            )
                        }
                    }

                    // 已收斂：後端強制覆寫的固定三顆 chips，依文字綁定行為（問卷啟動後隱藏，城市已定案）
                    // 「就去{城市}！」接受推薦啟動問卷／「換一個城市」原樣重打／「重新聊聊」清參數重來
                    cityState.isFinal -> if (!preferenceState.active) {
                        FlowRow(
                            modifier = Modifier.padding(start = 36.dp, top = Tokens.spacing050),
                            horizontalArrangement = Arrangement.spacedBy(Tokens.spacing100),
                            verticalArrangement = Arrangement.spacedBy(Tokens.spacing100),
                        ) {
                            val chips = cityState.quickReplies.ifEmpty {
                                // 後端 chips 異常缺漏時的保底：仍給接受入口
                                listOf("就去${cityState.recommendedCity}！") // TODO: i18n
                            }
                            chips.forEachIndexed { index, chip ->
                                when (chip) {
                                    AiCompanionViewModel.CITY_CHIP_SWAP -> if (cityState.canSwapCity) {
                                        PlanChoiceChip(
                                            label = chip,
                                            testTag = "companion_city_swap_chip",
                                            onClick = onSwapCity,
                                        )
                                    }
                                    AiCompanionViewModel.CITY_CHIP_RESTART -> PlanChoiceChip(
                                        label = chip,
                                        testTag = "companion_city_restart_chip",
                                        onClick = onRestartCityChat,
                                    )
                                    else -> PlanChoiceChip(
                                        label = chip,
                                        solid = true,
                                        testTag = "companion_city_accept_chip_$index",
                                        onClick = onStartPreferences,
                                    )
                                }
                            }
                        }
                    }

                    cityState.quickReplies.isNotEmpty() -> {
                        FlowRow(
                            modifier = Modifier.padding(start = 36.dp, top = Tokens.spacing050),
                            horizontalArrangement = Arrangement.spacedBy(Tokens.spacing100),
                            verticalArrangement = Arrangement.spacedBy(Tokens.spacing100),
                        ) {
                            cityState.quickReplies.forEachIndexed { index, chip ->
                                PlanChoiceChip(
                                    label = chip,
                                    testTag = "companion_city_chip_$index",
                                    onClick = { onSendInput(chip) },
                                )
                            }
                        }
                    }
                }
            }
            // 偏好問卷（聊天式一次一題，純前端）：泡泡＋當前題選項 chips，也可打字回答
            if (preferenceState.active) {
                preferenceState.messages.forEach { message ->
                    PlanChatBubble(fromMe = message.fromMe, avatarUrl = avatarUrl, text = message.text)
                }
                if (preferenceState.isCompleted) {
                    // 全部答完 → travel-guide 完成時刻（照設計稿：留在聊天室，摘要卡＋完成宣告＋主行動）
                    when (guideState) {
                        TravelGuideState.Idle -> Box(
                            Modifier.padding(start = 36.dp, top = Tokens.spacing050),
                        ) {
                            PlanChoiceChip(
                                label = "開始規劃", // TODO: i18n
                                solid = true,
                                testTag = "companion_preference_submit_chip",
                                onClick = onSubmitPreferences,
                            )
                        }

                        TravelGuideState.Loading -> PlanTypingBubble(avatarUrl = avatarUrl)

                        is TravelGuideState.Loaded -> {
                            // 完整行程預覽已移除：聊天室只留完成宣告＋主行動，細節請進成果頁看
                            guideState.messages.forEach { message ->
                                PlanChatBubble(fromMe = false, avatarUrl = avatarUrl, text = message)
                            }
                            Box(Modifier.padding(start = 36.dp, top = Tokens.spacing050)) {
                                PlanChoiceChip(
                                    label = guideState.mainActionLabel ?: "看看完整行程", // TODO: i18n
                                    solid = true,
                                    testTag = "companion_plan_view_trip_chip",
                                    onClick = onViewTrip,
                                )
                            }
                        }

                        TravelGuideState.SoftFailed, TravelGuideState.Error -> {
                            PlanChatBubble(
                                fromMe = false,
                                avatarUrl = avatarUrl,
                                text = "行程生成失敗了，再讓我試一次好嗎？", // TODO: i18n
                            )
                            Box(Modifier.padding(start = 36.dp, top = Tokens.spacing050)) {
                                PlanChoiceChip(
                                    label = "重試", // TODO: i18n
                                    solid = true,
                                    testTag = "companion_guide_retry_chip",
                                    onClick = onRetryGuide,
                                )
                            }
                        }

                        TravelGuideState.InvalidRequest -> PlanChatBubble(
                            fromMe = false,
                            avatarUrl = avatarUrl,
                            text = "發生錯誤，請稍後再試。", // TODO: i18n
                        )
                    }
                } else if (preferenceState.options.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier.padding(start = 36.dp, top = Tokens.spacing050),
                        horizontalArrangement = Arrangement.spacedBy(Tokens.spacing100),
                        verticalArrangement = Arrangement.spacedBy(Tokens.spacing100),
                    ) {
                        preferenceState.options.forEachIndexed { index, option ->
                            PlanChoiceChip(
                                label = option,
                                testTag = "companion_preference_chip_$index",
                                onClick = { onSendInput(option) },
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(Tokens.spacing200))
        }

        PlanInputBar(
            enabled = state is TravelSummaryState.Ready && !cityState.isWaiting &&
                if (preferenceState.active) !preferenceState.isCompleted else !cityState.isFinal,
            onSend = onSendInput,
        )
    }

    // 換城超限保底（shown_cities 超過 5 個，後端未打 LLM 回固定文案）：跳 dialog 引導重新聊聊
    if (cityState.swapLimitText.isNotBlank()) {
        AppDialog(
            headerType = DialogHeaderType.Text(title = "換太多次啦", useScrollableContent = false), // TODO: i18n
            showHeaderCloseButton = false,
            showFooterShadow = false,
            onDismissRequest = onRestartCityChat,
            content = {
                Text(
                    cityState.swapLimitText,
                    color = Tokens.colorTextMedium,
                    fontSize = Tokens.fontSize3,
                    modifier = Modifier.padding(
                        horizontal = Tokens.spacing300,
                        vertical = Tokens.spacing150,
                    ),
                )
            },
            onClickPrimaryButton = {
                AppButton(
                    buttonText = "重新聊聊", // TODO: i18n
                    buttonType = ButtonType.PRIMARY,
                    buttonState = ButtonState.ENABLED,
                    buttonSizeType = ButtonSizeType.Lg,
                    isFullWidth = true,
                    onClick = onRestartCityChat,
                )
            },
        )
    }
}
