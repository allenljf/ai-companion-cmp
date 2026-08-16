package com.allenljf.aicompanion.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.allenljf.aicompanion.model.QuizCompletionResult
import com.allenljf.aicompanion.model.QuizHistoryRecord
import com.allenljf.aicompanion.platform.rememberShareText
import com.allenljf.aicompanion.theme.Tokens
import com.allenljf.aicompanion.ui.components.AppButton
import com.allenljf.aicompanion.ui.components.AppDialog
import com.allenljf.aicompanion.ui.components.ButtonSizeType
import com.allenljf.aicompanion.ui.components.ButtonState
import com.allenljf.aicompanion.ui.components.ButtonType
import com.allenljf.aicompanion.ui.components.DialogHeaderType
import com.allenljf.aicompanion.ui.components.DragHandle
import com.allenljf.aicompanion.viewmodel.AiCompanionViewModel
import com.allenljf.aicompanion.viewmodel.AnalysisState
import com.allenljf.aicompanion.viewmodel.CompanionCreationState
import com.allenljf.aicompanion.viewmodel.ShareImageV2State
import org.jetbrains.compose.resources.painterResource
import aicompanion.shared.generated.resources.Res
import aicompanion.shared.generated.resources.ic_arrow_right_line
import aicompanion.shared.generated.resources.ic_copy_line
import aicompanion.shared.generated.resources.ic_delete_line
import aicompanion.shared.generated.resources.ic_globe_fill
import aicompanion.shared.generated.resources.ic_list_view_line
import aicompanion.shared.generated.resources.ic_map_location_line
import aicompanion.shared.generated.resources.ic_message_line
import aicompanion.shared.generated.resources.ic_note_line
import aicompanion.shared.generated.resources.ic_people_line

// ---------- 結果頁（C-1 統一畫面；海報產圖/分享圖鏈路整段不搬，見 migration/02-ledger.md）----------

/**
 * 測驗結果頁：只做到「人格＋命定城市」文字展示，不含原始碼的海報產圖等待動畫／海報 Hero／
 * IG 限動分享／下載圖片（CLAUDE.md 核心約束：海報全鏈路不做）。
 * 原本 else 分支（`shareImageV2` 非 Ready 時的 fallback 文字內容）現在變成唯一內容分支。
 */
@Composable
internal fun ResultScreen(
    viewModel: AiCompanionViewModel,
    creation: CompanionCreationState,
    onViewDetail: () -> Unit,
    onGoHome: () -> Unit,
    onRetry: () -> Unit,
    onRetakeQuiz: () -> Unit,
    onViewOthers: () -> Unit,
    onStartPlanning: () -> Unit,
) {
    val analysis by viewModel.analysisState.collectAsStateWithLifecycle()
    val result = (analysis as? AnalysisState.Success)?.result
    val shareImageV2 by viewModel.shareImageV2State.collectAsStateWithLifecycle()
    var showBottomSheet by remember { mutableStateOf(false) }
    val shareText = rememberShareText()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("companion_result_screen")
            .background(Tokens.colorWhite)
            // 標題直接貼齊畫面頂端會被瀏海／動態島遮住，整頁內容退到狀態列下方
            .statusBarsPadding(),
    ) {
        when {
            analysis is AnalysisState.SoftFailed || analysis is AnalysisState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        "分析失敗，請重試", // TODO: i18n
                        color = Tokens.colorTextDarker,
                        fontWeight = FontWeight(Tokens.fontWeightMediumAndroid),
                        modifier = Modifier.testTag("companion_analyzing_error"),
                    )
                    Spacer(Modifier.height(Tokens.spacing300))
                    Box(Modifier.padding(horizontal = Tokens.spacing300)) {
                        PrimaryButton(text = "重試", testTag = "companion_analyzing_retry_btn", onClick = onRetry) // TODO: i18n
                    }
                }
            }

            analysis is AnalysisState.Analyzing -> {
                CompanionLoadingContent(
                    avatarUrl = creation.avatarUrl,
                    companionName = creation.companionName.ifBlank { "旅伴" }, // TODO: i18n fallback
                    title = "正在解析你的旅行 DNA", // TODO: i18n
                    subtitle = "分析你的選擇 ▸ 配對目的地 ▸ 描繪場景", // TODO: i18n
                    testTag = "companion_analyzing_loading",
                )
            }

            else -> {
                // 人格 + 命定城市展示（原始碼「無海報」fallback 分支，現為唯一內容路徑）
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                ) {
                    // T19：海報 hero 圖很慢（約 80 秒），文字內容不等它顯示——Failed/Idle 不佔版位，
                    // Loading 顯示輕量佔位，Ready 才補上圖，版面不會因為缺圖而破
                    ShareImageV2HeroContent(shareImageV2)
                    if (shareImageV2 != ShareImageV2State.Idle && shareImageV2 != ShareImageV2State.Failed) {
                        Spacer(Modifier.height(Tokens.spacing200))
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Tokens.spacing300),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                result?.travelIdentity.orEmpty(),
                                fontWeight = FontWeight(Tokens.fontWeightBold),
                                fontSize = Tokens.fontSize6,
                                color = Tokens.colorTextPrimaryDark,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(Modifier.height(Tokens.spacing150))
                            Text(
                                "${result?.destinationCn.orEmpty()} · ${result?.destinationCountry.orEmpty()}",
                                color = Tokens.colorTextDark,
                                fontSize = Tokens.fontSize3,
                            )
                            Spacer(Modifier.height(Tokens.spacing200))
                            Text(
                                result?.companionQuote?.ifBlank { result.recommendation.firstOrNull().orEmpty() }.orEmpty(),
                                color = Tokens.colorTextMedium,
                                fontSize = Tokens.fontSize3,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                    CompanionResultHighlightContent(result = result)
                    CompanionResultDetailsContent(result = result, bottomSafeArea = 96.dp)
                }

                // 底部雙按鈕：純文字分享（T15 才接 shareText）＋更多動作（開啟導覽選單）
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Tokens.colorWhite)
                        .navigationBarsPadding()
                        .padding(horizontal = Tokens.spacing300, vertical = Tokens.spacing200),
                    horizontalArrangement = Arrangement.spacedBy(Tokens.spacing150),
                ) {
                    Box(Modifier.weight(1f).testTag("companion_result_share_btn")) {
                        AppButton(
                            buttonText = "分享我的旅行 DNA", // TODO: i18n
                            buttonType = ButtonType.PRIMARY_SUBTLE,
                            buttonState = ButtonState.ENABLED,
                            buttonSizeType = ButtonSizeType.Lg,
                            isFullWidth = true,
                            onClick = {
                                // 分享文字用結果頁現有文案風格組出人格稱號＋命定城市
                                shareText(
                                    "我的旅行人格是${result?.travelIdentity.orEmpty()}，" +
                                        "命定城市是${result?.destinationCn.orEmpty()}！", // TODO: i18n
                                )
                            },
                        )
                    }
                    Box(Modifier.weight(1f).testTag("companion_result_more_actions_btn")) {
                        AppButton(
                            buttonText = "更多動作", // TODO: i18n
                            buttonType = ButtonType.PRIMARY,
                            buttonState = ButtonState.ENABLED,
                            buttonSizeType = ButtonSizeType.Lg,
                            isFullWidth = true,
                            onClick = { showBottomSheet = true },
                        )
                    }
                }
            }
        }

        // BottomSheet：導覽選單（繼續規劃／看其他人／回到旅伴），移除原本的分享圖片/IG限動/下載圖片/探索行程列
        // （皆依附海報 bitmap，海報鏈路整段不做，見 migration/02-ledger.md）
        if (showBottomSheet) {
            ResultActionsBottomSheet(
                result = result,
                onDismiss = { showBottomSheet = false },
                // 定案版：無「查看完整測驗結果」列
                showViewDetail = false,
                onViewDetail = {
                    showBottomSheet = false
                    onViewDetail()
                },
                onGoHome = {
                    showBottomSheet = false
                    onGoHome()
                },
                // 入口 A：帶測驗結果打 travel-summary（entry_type=quiz_completion），進規劃聊天室
                onContinuePlanning = {
                    showBottomSheet = false
                    onStartPlanning()
                },
                onViewOthers = {
                    showBottomSheet = false
                    onViewOthers()
                },
            )
        }
    }
}

/**
 * 海報 hero 圖（T19）：Loading 顯示輕量佔位（不是 shimmer，demo 未移植那套元件，見
 * CompanionRootScreen 對 CompanionAsyncImage 的說明）；Failed/Idle 不佔版面直接跳過。
 *
 * 比例固定 [HERO_ASPECT_RATIO]：後端 hero 是 IG 限動規格的直式長圖（實測 1536x2752／1152x2048），
 * 用固定高度的橫幅框會 centerCrop 只露出中間一條（原版 SixZonePosterSpec 也特別註明 hero 要完整
 * 顯示不裁切）。Loading 佔位用同一比例，圖載入後版面才不會跳動。
 */
@Composable
private fun ShareImageV2HeroContent(state: ShareImageV2State) {
    when (state) {
        is ShareImageV2State.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Tokens.spacing300)
                    .aspectRatio(HERO_ASPECT_RATIO)
                    .clip(RoundedCornerShape(Tokens.radiusLg))
                    .background(Tokens.colorBackgroundPrimaryLighter)
                    .testTag("companion_result_hero_loading"),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Tokens.colorBackgroundPrimaryMedium)
            }
        }

        is ShareImageV2State.Ready -> {
            CompanionAsyncImage(
                url = state.heroUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Tokens.spacing300)
                    .clip(RoundedCornerShape(Tokens.radiusLg))
                    .testTag("companion_result_hero_image"),
                placeholderAspectRatio = HERO_ASPECT_RATIO,
                blurInOnLoad = true,
            )
        }

        ShareImageV2State.Idle, ShareImageV2State.Failed -> Unit
    }
}

/** 後端 hero 圖是 9:16 直式（IG 限動規格） */
private const val HERO_ASPECT_RATIO = 9f / 16f

/**
 * 統一的旅伴讀取畫面：頭像 + 名字 + 主標題 + 副標題 + 三點漸變輪播。
 * 答題前（準備問卷）與答題後（分析結果）共用同一套視覺。
 */
@Composable
internal fun CompanionLoadingContent(
    avatarUrl: String,
    companionName: String,
    title: String,
    subtitle: String,
    testTag: String,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CompanionAsyncImage(
            url = avatarUrl,
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Tokens.colorBackgroundPrimaryLighter),
            placeholder = {
                Text("?", color = Tokens.colorTextPrimaryDark, fontSize = Tokens.fontSize9)
            },
        )
        Spacer(Modifier.height(Tokens.spacing200))
        Text(
            companionName,
            color = Tokens.colorTextPrimaryDark,
            fontWeight = FontWeight(Tokens.fontWeightBold),
            fontSize = Tokens.fontSize5,
        )
        Spacer(Modifier.height(Tokens.spacing200))
        Text(
            title,
            fontWeight = FontWeight(Tokens.fontWeightBold),
            fontSize = Tokens.fontSize4,
            color = Tokens.colorTextDarker,
        )
        Spacer(Modifier.height(Tokens.spacing100))
        Text(
            subtitle,
            color = Tokens.colorTextMedium,
            fontSize = Tokens.fontSize3,
        )
        Spacer(Modifier.height(Tokens.spacing300))
        LoadingDotsRow()
    }
}

/** 三點漸變輪播，讀取畫面共用。 */
@Composable
private fun LoadingDotsRow() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading_dots")
    val dotAlphas = List(3) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.3f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 1200
                    0.3f at 0
                    1f at 400
                    0.3f at 800
                    0.3f at 1200
                },
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(index * 400),
            ),
            label = "dot_$index",
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        dotAlphas.forEach { alpha ->
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(Tokens.colorBackgroundPrimaryMedium.copy(alpha = alpha.value)),
            )
        }
    }
}

/**
 * 旅行人格/tag 標籤——即「推薦理由」以上的區塊。
 * [showDestinationAndTagline]：原始碼 v2 海報路徑會關閉此參數避免與海報 Hero 上的資訊重複；
 * 本檔沒有海報 Hero，呼叫端一律開啟。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CompanionResultHighlightContent(
    result: QuizCompletionResult?,
    showDestinationAndTagline: Boolean = true,
) {
    val highlightTags = result?.highlightTags.orEmpty()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = Tokens.spacing300,
                end = Tokens.spacing300,
                top = Tokens.spacing200,
            )
            .testTag("companion_share_result_highlight"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (showDestinationAndTagline && result?.destinationCn.orEmpty().isNotBlank()) {
            Text(
                "${result?.destinationCn.orEmpty()} · ${result?.destinationCountry.orEmpty()}",
                fontWeight = FontWeight(Tokens.fontWeightBold),
                fontSize = Tokens.fontSize5,
                color = Tokens.colorTextDarker,
                modifier = Modifier.testTag("companion_share_result_destination"),
            )
            Spacer(Modifier.height(Tokens.spacing100))
        }
        if (showDestinationAndTagline && result?.tagline.orEmpty().isNotBlank()) {
            Text(
                result?.tagline.orEmpty(),
                color = Tokens.colorTextDark,
                fontSize = Tokens.fontSize4,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("companion_share_result_tagline"),
            )
            Spacer(Modifier.height(Tokens.spacing150))
        }
        if (result?.travelIdentity.orEmpty().isNotBlank()) {
            Text(
                result?.travelIdentity.orEmpty(),
                fontWeight = FontWeight(Tokens.fontWeightBold),
                fontSize = Tokens.fontSize3,
                color = Tokens.colorTextPrimaryDark,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("companion_share_result_travel_identity"),
            )
            Spacer(Modifier.height(Tokens.spacing100))
        }
        if (highlightTags.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Tokens.spacing100, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(Tokens.spacing100),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("companion_share_result_highlight_tags"),
            ) {
                highlightTags.forEach { tag ->
                    SelectablePill(
                        label = tag,
                        isSelected = true,
                        enabled = false,
                        testTag = "result_tag_$tag",
                        fontSize = Tokens.fontSize1,
                        onClick = {},
                    )
                }
            }
        }
    }
}

/** 「推薦理由」「分享文案」卡片。 */
@Composable
internal fun CompanionResultDetailsContent(
    result: QuizCompletionResult?,
    bottomSafeArea: Dp,
) {
    // 資料已完整到手的靜態摘要：分段推薦理由以空行合併成單一卡片內文
    val recommendation = result?.recommendationText.orEmpty()
    val socialPost = result?.socialPost.orEmpty()
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = Tokens.spacing300,
                end = Tokens.spacing300,
                bottom = Tokens.spacing300,
                top = Tokens.spacing200,
            )
            .testTag("companion_share_result_details"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (recommendation.isNotBlank()) {
            TitledInfoCard(
                title = "推薦理由", // TODO: i18n
                content = recommendation,
                testTag = "companion_share_result_recommendation",
            )
            Spacer(Modifier.height(Tokens.spacing150))
        }
        if (socialPost.isNotBlank()) {
            TitledInfoCard(
                title = "分享文案", // TODO: i18n
                content = socialPost,
                testTag = "companion_share_result_social_post",
                // 原用 Context.copyToClipboard + Toast；KMP 改用 commonMain 的 LocalClipboardManager，Toast 提示先省略
                onCopyClick = { clipboardManager.setText(AnnotatedString(socialPost)) }, // TODO: 複製成功提示
            )
        }
        Spacer(Modifier.height(bottomSafeArea))
    }
}

/**
 * 導覽選單 bottom sheet：原始碼還包含「分享我的旅行 DNA」「分享到 IG 限時動態」「下載到我的裝置」
 * 「探索 {destination} 行程」四列，皆依附海報 bitmap／SearchResultRouter，海報全鏈路不做故整段移除，
 * 只保留與海報無關的導覽項目。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResultActionsBottomSheet(
    result: QuizCompletionResult?,
    onDismiss: () -> Unit,
    onViewDetail: () -> Unit,
    onGoHome: () -> Unit,
    showViewDetail: Boolean = true,
    showGoHome: Boolean = true,
    onContinuePlanning: (() -> Unit)? = null,
    onViewOthers: (() -> Unit)? = null,
    onBackToList: (() -> Unit)? = null,
    onDeleteRecord: (() -> Unit)? = null,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        shape = RoundedCornerShape(
            topStart = Tokens.radiusXl,
            topEnd = Tokens.radiusXl,
        ),
        dragHandle = { DragHandle() },
        scrimColor = Color.Black.copy(alpha = 0.5f),
    ) {
        // 依 Phase 1 mockup 定案版排序，列與列之間加分隔線
        val rows = buildList<@Composable () -> Unit> {
            if (onContinuePlanning != null) {
                add {
                    ActionRow(
                        // TODO: 尚未有專屬指南針 icon，暫用地圖 icon，待設計提供後替換
                        icon = painterResource(Res.drawable.ic_map_location_line),
                        title = "繼續規劃我的行程", // TODO: i18n
                        description = "回到規劃對話，和小旅一起排", // TODO: i18n
                        testTag = "companion_action_continue_planning",
                        hero = true,
                        onClick = onContinuePlanning,
                    )
                }
            }
            if (onViewOthers != null) {
                add {
                    ActionRow(
                        icon = painterResource(Res.drawable.ic_people_line),
                        title = "看其他人的測試結果", // TODO: i18n
                        description = "逛逛別人的命定城市", // TODO: i18n
                        testTag = "companion_action_view_others",
                        onClick = onViewOthers,
                    )
                }
            }
            if (showViewDetail) {
                add {
                    ActionRow(
                        icon = painterResource(Res.drawable.ic_note_line),
                        title = "查看完整測驗結果", // TODO: i18n
                        description = "旅行人格與命定旅程解析", // TODO: i18n
                        testTag = "companion_action_detail",
                        onClick = onViewDetail,
                    )
                }
            }
            if (showGoHome) {
                add {
                    ActionRow(
                        icon = painterResource(Res.drawable.ic_message_line),
                        title = "回到我的旅伴", // TODO: i18n
                        description = "和小旅繼續聊", // TODO: i18n
                        testTag = "companion_action_go_home",
                        onClick = onGoHome,
                    )
                }
            }
            if (onBackToList != null) {
                add {
                    ActionRow(
                        icon = painterResource(Res.drawable.ic_list_view_line),
                        title = "回到旅行 DNA 回顧列表", // TODO: i18n
                        description = "查看其他測驗紀錄", // TODO: i18n
                        testTag = "companion_action_back_to_list",
                        onClick = onBackToList,
                    )
                }
            }
            if (onDeleteRecord != null) {
                add {
                    ActionRow(
                        icon = painterResource(Res.drawable.ic_delete_line),
                        title = "刪除這筆紀錄", // TODO: i18n
                        description = "刪除後無法復原", // TODO: i18n
                        testTag = "companion_action_delete",
                        onClick = onDeleteRecord,
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Tokens.spacing400),
        ) {
            rows.forEachIndexed { index, row ->
                if (index > 0) {
                    HorizontalDivider(
                        color = Tokens.colorBorderLight,
                        modifier = Modifier.padding(horizontal = Tokens.spacing300),
                    )
                }
                row()
            }
        }
    }
}

@Composable
private fun ActionRow(
    icon: Painter,
    title: String,
    description: String,
    testTag: String,
    enabled: Boolean = true,
    hero: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = Tokens.spacing300, vertical = Tokens.spacing200)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (hero) Tokens.colorBackgroundPrimaryButton
                    else Tokens.colorBackgroundPrimaryLighter,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (hero) Tokens.colorWhite else Tokens.colorTextPrimaryDark,
            )
        }
        Spacer(Modifier.width(Tokens.spacing200))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontWeight = FontWeight(Tokens.fontWeightBold),
                fontSize = Tokens.fontSize3,
                color = if (enabled) Tokens.colorTextDarker else Tokens.colorTextLight,
            )
            Text(
                description,
                fontSize = Tokens.fontSize2,
                color = if (enabled) Tokens.colorTextMedium else Tokens.colorTextLight,
            )
        }
        Icon(
            painter = painterResource(Res.drawable.ic_arrow_right_line),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = if (enabled) Tokens.colorTextMedium else Tokens.colorTextLight,
        )
    }
}

// ---------- 結果詳情（原 ResultScreen 內容）----------

// 執行期暫不可達（showViewDetail 寫死 false）；T17 接真後端時再決定去留
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ResultDetailScreen(
    viewModel: AiCompanionViewModel,
    creation: CompanionCreationState,
    onBack: () -> Unit,
) {
    val analysis by viewModel.analysisState.collectAsStateWithLifecycle()
    val result = (analysis as? AnalysisState.Success)?.result
    val shareImageV2 by viewModel.shareImageV2State.collectAsStateWithLifecycle()
    val companionName = creation.companionName.ifBlank { "旅伴" } // TODO: i18n fallback
    val clipboardManager = LocalClipboardManager.current

    ScreenScaffold(title = "完整測驗結果", screenTag = "companion_result_detail_screen", onBack = onBack) { // TODO: i18n
        if (result != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CompanionAsyncImage(
                    url = creation.avatarUrl,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Tokens.colorBackgroundPrimaryLighter),
                    placeholder = {
                        Text("?", color = Tokens.colorTextPrimaryDark)
                    },
                )
                Spacer(Modifier.width(Tokens.spacing150))
                Text(
                    companionName,
                    fontWeight = FontWeight(Tokens.fontWeightMediumAndroid),
                    color = Tokens.colorTextDarker,
                )
            }

            Spacer(Modifier.height(Tokens.spacing300))

            ChatBubble(isCompanion = true) {
                Text(
                    result.companionQuote.ifBlank { result.recommendation.firstOrNull().orEmpty() },
                    color = Tokens.colorTextDarker,
                    fontSize = Tokens.fontSize3,
                    modifier = Modifier.testTag("companion_result_quote"),
                )
            }

            Spacer(Modifier.height(Tokens.spacing150))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Tokens.spacing100),
                verticalArrangement = Arrangement.spacedBy(Tokens.spacing100),
            ) {
                result.highlightTags.forEach { tag ->
                    SelectablePill(label = tag, isSelected = true, enabled = false, testTag = "result_tag_$tag", onClick = {})
                }
            }

            Spacer(Modifier.height(Tokens.spacing150))

            ChatBubble(isCompanion = true) {
                Column {
                    Text(
                        "最適合你的旅行身份是", // TODO: i18n
                        color = Tokens.colorTextDarker,
                        fontSize = Tokens.fontSize3,
                    )
                    Spacer(Modifier.height(Tokens.spacing100))
                    Text(
                        result.travelIdentity,
                        fontWeight = FontWeight(Tokens.fontWeightBold),
                        fontSize = Tokens.fontSize6,
                        color = Tokens.colorTextPrimaryDark,
                        modifier = Modifier.testTag("companion_result_title"),
                    )
                }
            }

            Spacer(Modifier.height(Tokens.spacing200))

            // T19：hero 圖 ready 就顯示；否則（Loading/Failed/Idle）沿用命定城市文字佔位，版面不破
            val heroUrl = (shareImageV2 as? ShareImageV2State.Ready)?.heroUrl
            if (heroUrl != null) {
                CompanionAsyncImage(
                    url = heroUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(Tokens.radiusLg))
                        .testTag("companion_result_detail_hero_image"),
                    blurInOnLoad = true,
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(Tokens.radiusLg))
                        .background(Tokens.colorBackgroundPrimaryLighter)
                        .testTag("companion_result_detail_poster_placeholder"),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "${result.destinationCn} · ${result.destinationCountry}",
                        color = Tokens.colorTextDark,
                        fontSize = Tokens.fontSize2,
                    )
                }
            }

            // 海報下方完整呈現分析文字（與產圖等待頁相同的 recommendation / social_post 內容）
            if (result.recommendationText.isNotBlank()) {
                Spacer(Modifier.height(Tokens.spacing200))
                TitledInfoCard(
                    title = "推薦理由", // TODO: i18n
                    content = result.recommendationText,
                    testTag = "companion_result_detail_recommendation",
                )
            }
            if (result.socialPost.isNotBlank()) {
                Spacer(Modifier.height(Tokens.spacing150))
                TitledInfoCard(
                    title = "分享文案", // TODO: i18n
                    content = result.socialPost,
                    testTag = "companion_result_detail_social_post",
                    onCopyClick = { clipboardManager.setText(AnnotatedString(result.socialPost)) }, // TODO: 複製成功提示
                )
            }
        }
    }
}

@Composable
internal fun ChatBubble(
    isCompanion: Boolean,
    content: @Composable () -> Unit,
) {
    val bgColor = if (isCompanion) Tokens.colorBackgroundSurfaceLight
    else Tokens.colorBackgroundPrimaryLighter
    val alignment = if (isCompanion) Alignment.CenterStart else Alignment.CenterEnd

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(Tokens.radiusLg))
                .background(bgColor)
                .padding(Tokens.spacing200),
        ) {
            content()
        }
    }
}

/** 圓角卡片：標題（依 API 欄位語意命名）+ 內文，用於呈現 recommendation / social_post 這類長文字說明。 */
@Composable
internal fun TitledInfoCard(
    title: String,
    content: String,
    testTag: String,
    onCopyClick: (() -> Unit)? = null,
) {
    TitledInfoCard(title = title, testTag = testTag, onCopyClick = onCopyClick) {
        Text(
            content,
            color = Tokens.colorTextDarker,
            fontSize = Tokens.fontSize3,
        )
    }
}

/**
 * 圓角卡片：標題（依 API 欄位語意命名）+ 任意內容，供需要自訂內文樣式的呼叫端使用。
 * [onCopyClick] 有給值時，標題右側會多一顆複製按鈕（複製內文到系統剪貼簿）。
 */
@Composable
internal fun TitledInfoCard(
    title: String,
    testTag: String,
    onCopyClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Tokens.radiusLg))
            .border(1.dp, Tokens.colorBorderLight, RoundedCornerShape(Tokens.radiusLg))
            .background(Tokens.colorWhite)
            .padding(Tokens.spacing200)
            .testTag(testTag),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                title,
                fontWeight = FontWeight(Tokens.fontWeightBold),
                fontSize = Tokens.fontSize3,
                color = Tokens.colorTextPrimaryDark,
                modifier = Modifier.weight(1f),
            )
            if (onCopyClick != null) {
                IconButton(
                    onClick = onCopyClick,
                    modifier = Modifier
                        .size(Tokens.dimensionIconLg)
                        .testTag("${testTag}_copy_btn"),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_copy_line),
                        contentDescription = null,
                        tint = Tokens.colorTextMedium,
                        modifier = Modifier.size(Tokens.dimensionIconSm),
                    )
                }
            }
        }
        Spacer(Modifier.height(Tokens.spacing100))
        content()
    }
}

// ---------- 旅行 DNA 回顧 ----------

/**
 * 測驗結果歷史回顧：列表（縮圖 + 旅行人格 + 目的地 + 日期）；
 * 點擊單筆進入詳情（本檔簡化為文字佔位，海報鏈路不做）。
 */
@Composable
internal fun CompanionHistoryScreen(
    viewModel: AiCompanionViewModel,
    onBack: () -> Unit,
) {
    val history by viewModel.quizHistory.collectAsStateWithLifecycle()
    var selectedCreatedAt by remember { mutableStateOf<Long?>(null) }

    val current = history.firstOrNull { it.createdAt == selectedCreatedAt }
    if (current != null) {
        CompanionHistoryDetailContent(
            viewModel = viewModel,
            record = current,
            onBack = {
                viewModel.loadQuizHistory()
                selectedCreatedAt = null
            },
            onDeleted = { selectedCreatedAt = null },
        )
    } else {
        ScreenScaffold(
            title = "旅行 DNA 回顧", // TODO: i18n
            screenTag = "companion_history_screen",
            onBack = onBack,
        ) {
            if (history.isEmpty()) {
                // ScreenScaffold 已有 verticalScroll，這裡不能再放可捲動/無限高度元件
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Tokens.spacing600)
                        .testTag("companion_history_empty"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        "還沒有測驗紀錄", // TODO: i18n
                        fontWeight = FontWeight(Tokens.fontWeightBold),
                        fontSize = Tokens.fontSize4,
                        color = Tokens.colorTextDarker,
                    )
                    Spacer(Modifier.height(Tokens.spacing100))
                    Text(
                        "完成旅行 DNA 測驗後，結果會保留在這裡", // TODO: i18n
                        color = Tokens.colorTextMedium,
                        fontSize = Tokens.fontSize3,
                    )
                }
            } else {
                // 上限 20 筆，直接用 Column 靠外層 ScreenScaffold 捲動（LazyColumn 會撞上外層 verticalScroll 的無限高度約束）
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Tokens.spacing150),
                ) {
                    history.forEach { record ->
                        CompanionHistoryItem(record = record, onClick = { selectedCreatedAt = record.createdAt })
                    }
                }
            }
        }
    }
}

@Composable
private fun CompanionHistoryItem(
    record: QuizHistoryRecord,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("companion_history_item")
            .clip(RoundedCornerShape(Tokens.radiusLg))
            .border(1.dp, Tokens.colorBorderLight, RoundedCornerShape(Tokens.radiusLg))
            .background(Tokens.colorWhite)
            .clickable { onClick() }
            .padding(Tokens.spacing200),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompanionAsyncImage(
            url = record.displayPosterUrl(),
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(Tokens.radiusMd)),
            placeholder = {
                Icon(
                    painter = painterResource(Res.drawable.ic_globe_fill),
                    contentDescription = null,
                    tint = Tokens.colorTextMedium,
                    modifier = Modifier.size(Tokens.dimensionIconSm),
                )
            },
        )
        Spacer(Modifier.width(Tokens.spacing150))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                record.result.travelIdentity.ifBlank { record.result.travelIdentityEn },
                fontWeight = FontWeight(Tokens.fontWeightMediumAndroid),
                color = Tokens.colorTextDarker,
                fontSize = Tokens.fontSize3,
            )
            Spacer(Modifier.height(Tokens.spacing050))
            Text(
                listOf(
                    record.result.destinationCn.ifBlank { record.result.destinationEn },
                    formatHistoryDate(record.createdAt),
                ).filter { it.isNotBlank() }.joinToString(" · "),
                color = Tokens.colorTextMedium,
                fontSize = Tokens.fontSize2,
            )
        }
        Icon(
            painter = painterResource(Res.drawable.ic_arrow_right_line),
            contentDescription = null,
            tint = Tokens.colorTextMedium,
            modifier = Modifier.size(Tokens.dimensionIconSm),
        )
    }
}

/**
 * 單筆歷史詳情：原始碼從本機檔案重組海報 Hero 供檢視/分享，海報鏈路不做後
 * 一律走原本「素材缺漏」的 fallback 排版（命定城市文字佔位 + 人格標籤 + 推薦理由/分享文案卡片）。
 */
@Composable
private fun CompanionHistoryDetailContent(
    viewModel: AiCompanionViewModel,
    record: QuizHistoryRecord,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    var bottomBarHeight by remember { mutableStateOf(0.dp) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("companion_history_detail_screen")
            .background(Tokens.colorWhite),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = Tokens.spacing600),
        ) {
            // T19：heroImageUrl 有值（產圖成功回填過）就直接顯示，不必重打一次 35 秒的 share-image-v2
            if (record.heroImageUrl.isNotBlank()) {
                CompanionAsyncImage(
                    url = record.heroImageUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Tokens.spacing300)
                        .clip(RoundedCornerShape(Tokens.radiusLg))
                        .testTag("companion_history_hero_image"),
                    // 同結果頁：hero 是 9:16 直式，固定高度會把圖裁成一條
                    placeholderAspectRatio = HERO_ASPECT_RATIO,
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Tokens.spacing300)
                        .height(200.dp)
                        .clip(RoundedCornerShape(Tokens.radiusLg))
                        .background(Tokens.colorBackgroundPrimaryLighter)
                        .testTag("companion_history_poster_fallback"),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "${record.result.destinationCn} · ${record.result.destinationCountry}",
                        color = Tokens.colorTextDark,
                        fontSize = Tokens.fontSize2,
                    )
                }
            }
            CompanionResultHighlightContent(record.result, showDestinationAndTagline = true)
            CompanionResultDetailsContent(result = record.result, bottomSafeArea = bottomBarHeight)
        }

        // 底部固定「更多動作」按鈕列，比照 ResultScreen；量測高度供上方內容留白，避免最後一張卡片被蓋住
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    bottomBarHeight = with(density) { coordinates.size.height.toDp() }
                }
                .background(Tokens.colorWhite)
                .navigationBarsPadding()
                .padding(horizontal = Tokens.spacing300, vertical = Tokens.spacing200),
        ) {
            PrimaryButton(
                text = "更多動作", // TODO: i18n
                testTag = "companion_history_more_actions_btn",
                onClick = { showBottomSheet = true },
            )
        }
    }

    if (showBottomSheet) {
        ResultActionsBottomSheet(
            result = record.result,
            onDismiss = { showBottomSheet = false },
            onViewDetail = {},
            onGoHome = {},
            showViewDetail = false,
            showGoHome = false,
            onBackToList = {
                showBottomSheet = false
                onBack()
            },
            onDeleteRecord = {
                showBottomSheet = false
                showDeleteConfirm = true
            },
        )
    }

    if (showDeleteConfirm) {
        AppDialog(
            headerType = DialogHeaderType.Text(title = "刪除這筆紀錄？", useScrollableContent = false), // TODO: i18n
            showHeaderCloseButton = false,
            showFooterShadow = false,
            onDismissRequest = { showDeleteConfirm = false },
            content = {
                Text(
                    "刪除後無法復原", // TODO: i18n
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
                    buttonText = "刪除", // TODO: i18n
                    buttonType = ButtonType.PRIMARY,
                    buttonState = ButtonState.ENABLED,
                    buttonSizeType = ButtonSizeType.Lg,
                    isFullWidth = true,
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteQuizHistoryRecord(record.createdAt)
                        onDeleted()
                    },
                )
            },
            onClickCancelButton = {
                AppButton(
                    buttonText = "取消", // TODO: i18n
                    buttonType = ButtonType.TEXT_SECONDARY,
                    buttonState = ButtonState.ENABLED,
                    buttonSizeType = ButtonSizeType.Lg,
                    isFullWidth = true,
                    onClick = { showDeleteConfirm = false },
                )
            },
        )
    }
}

/**
 * 回顧列表縮圖來源：T19 起優先用 [QuizHistoryRecord.heroImageUrl]（後端遠端圖，產圖成功後回填）；
 * posterLocalPath/heroLocalPath 是原始碼的本機合成檔路徑，本專案海報 Bitmap 合成不做，恆為空字串，
 * 留著只是不動既有欄位形狀。三者皆空時 CompanionAsyncImage 顯示 placeholder。
 */
private fun QuizHistoryRecord.displayPosterUrl(): String =
    heroImageUrl.takeIf { it.isNotBlank() }
        ?: posterLocalPath.takeIf { it.isNotBlank() }?.let { "file://$it" }
        ?: heroLocalPath.takeIf { it.isNotBlank() }?.let { "file://$it" }
        ?: ""

// 曆法換算抽到 CompanionRootScreen.formatEpochMillisAsDate 共用（見該處說明），比照 TripListScreen.formatSavedAtDate。
private fun formatHistoryDate(epochMillis: Long): String {
    if (epochMillis <= 0L) return ""
    return formatEpochMillisAsDate(epochMillis)
}
