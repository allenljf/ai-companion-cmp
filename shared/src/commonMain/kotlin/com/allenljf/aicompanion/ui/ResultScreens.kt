package com.allenljf.aicompanion.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.allenljf.aicompanion.model.QuizCompletionResult
import com.allenljf.aicompanion.model.QuizHistoryRecord
import com.allenljf.aicompanion.platform.rememberShareImageToInstagramStory
import com.allenljf.aicompanion.platform.rememberShareText
import com.allenljf.aicompanion.theme.Tokens
import com.allenljf.aicompanion.ui.components.AppButton
import com.allenljf.aicompanion.ui.components.ButtonSizeType
import com.allenljf.aicompanion.ui.components.ButtonState
import com.allenljf.aicompanion.ui.components.ButtonType
import com.allenljf.aicompanion.ui.components.DragHandle
import com.allenljf.aicompanion.viewmodel.AiCompanionViewModel
import com.allenljf.aicompanion.viewmodel.AnalysisState
import com.allenljf.aicompanion.viewmodel.CompanionCreationState
import com.allenljf.aicompanion.viewmodel.ShareImageV2State
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import aicompanion.shared.generated.resources.Res
import aicompanion.shared.generated.resources.companion_stamp_fallback_generic
import aicompanion.shared.generated.resources.ic_arrow_right_line
import aicompanion.shared.generated.resources.ic_cross_line
import aicompanion.shared.generated.resources.ic_copy_line
import aicompanion.shared.generated.resources.ic_delete_line
import aicompanion.shared.generated.resources.ic_globe_fill
import aicompanion.shared.generated.resources.ic_list_view_line
import aicompanion.shared.generated.resources.ic_map_location_line
import aicompanion.shared.generated.resources.ic_message_line
import aicompanion.shared.generated.resources.ic_note_line
import aicompanion.shared.generated.resources.ic_people_line
import aicompanion.shared.generated.resources.ic_share_android_line

// ---------- 結果頁（C-1 統一畫面；海報產圖/分享圖鏈路整段不搬，見 migration/02-ledger.md）----------

private fun withoutKkdayHashtag(text: String): String =
    text.replace(Regex("""\s*#kkday\b""", RegexOption.IGNORE_CASE), "").trim()

/**
 * 測驗結果頁（T21：恢復原版產圖等待頁，結果不再自動顯示）。
 * 分析成功後先進 [PosterGeneratingContent] 打字機等待頁（[posterRevealed]=false），使用者點擊
 * 「一起去看看」（[AiCompanionViewModel.revealPosterResult]）才真正顯示以下內容：
 * hero 就緒（[ShareImageV2State.Ready]）時走沈浸式版面：hero 滿版無 padding、往上頂到狀態列下方
 * （此時根 Box 不能吃 [Modifier.statusBarsPadding]，否則頂部會多一截留白）；hero 底部疊黑色半透明
 * 資訊卡（目的地＋tagline＋stamp 圓圖），下方接 tag 文字 pill＋既有推薦理由／分享文案卡
 * （API 只回 hero 與 stamp，不回 tag icon，故 tag 維持純文字，不做圓圖）。
 * hero 未就緒（Idle/Loading/Failed）維持 T19 的簡化文字展示 fallback（原始碼「無海報」分支）。
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
    val posterRevealed by viewModel.posterRevealed.collectAsStateWithLifecycle()
    var showBottomSheet by remember { mutableStateOf(false) }
    val shareText = rememberShareText()
    val shareImageToInstagramStory = rememberShareImageToInstagramStory()
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    // 分享／分享到 IG 限時動態用：截「Hero + 黑色資訊卡」容器當下畫面的截圖，而非重新下載 hero URL，
    // 確保分享出去的圖跟畫面上看到的一致（比照原始碼做法，見任務 brief）；未就緒時這個 layer 沒錄到內容，
    // 分享按鈕會直接退回純文字分享（見下方 onShareToInstagramStories）
    val shareableGraphicsLayer = rememberGraphicsLayer()
    // 分享文案：優先用後端 social_post，空則退回人格＋命定城市句型（BottomSheet「分享我的旅行 DNA」
    // 與「分享到 IG 限時動態」共用同一句）
    val shareCaption = withoutKkdayHashtag(result?.socialPost.orEmpty()).ifBlank {
        if (result != null) "我的旅行人格是${result.travelIdentity}，命定城市是${result.destinationCn}！" else "" // TODO: i18n
    }

    val isPosterReady = shareImageV2 is ShareImageV2State.Ready
    // 產圖等待頁／沈浸式 hero 兩者都自己吃 statusBarsPadding，根 Box 這裡不能重複套一次
    val showingSelfPaddedContent = (analysis is AnalysisState.Success && !posterRevealed) || isPosterReady

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("companion_result_screen")
            .background(Tokens.colorWhite)
            .then(if (showingSelfPaddedContent) Modifier else Modifier.statusBarsPadding()),
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

            analysis is AnalysisState.Success && !posterRevealed -> {
                // T21：分析結果已到手但海報還沒揭曉——打字機依序播完思考過程/身份摘要/推薦理由/分享文案，
                // 不自動跳結果頁；Failed 也視為「可繼續」（軟失敗契約：無海報不能讓使用者卡在等待頁）
                PosterGeneratingContent(
                    avatarUrl = creation.avatarUrl,
                    companionName = creation.companionName.ifBlank { "旅伴" }, // TODO: i18n fallback
                    result = result,
                    isPosterReady = shareImageV2 is ShareImageV2State.Ready || shareImageV2 is ShareImageV2State.Failed,
                    onViewResult = { viewModel.revealPosterResult() },
                )
            }

            else -> {
                val currentShareImageV2 = shareImageV2
                if (currentShareImageV2 is ShareImageV2State.Ready) {
                    // 沈浸式版面：hero 滿版頂到狀態列下方，內容依序往下排列（不疊加），下方接 tag 圓圖列／
                    // 既有推薦理由卡；不再重複顯示目的地/tagline（hero 上的黑色資訊卡已經有）
                    val shareResult = currentShareImageV2.result
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                    ) {
                        Box(
                            modifier = Modifier.drawWithContent {
                                shareableGraphicsLayer.record { this@drawWithContent.drawContent() }
                                drawLayer(shareableGraphicsLayer)
                            },
                        ) {
                            ImmersiveShareHeroWithBadge(
                                heroUrl = shareResult.heroUrl.orEmpty(),
                                destinationCn = shareResult.content.destinationCn,
                                destinationEn = shareResult.content.destinationEn,
                                tagline = shareResult.content.tagline,
                                stampUrl = shareResult.stampUrl,
                            )
                        }
                        Spacer(Modifier.height(Tokens.spacing200))
                        // 後端暫不回傳 tag_icon_urls（decorations 缺漏）：拿掉圓圖列，
                        // 直接沿用下面 CompanionResultHighlightContent 的純文字 tag pill
                        CompanionResultHighlightContent(
                            result = result,
                            showDestinationAndTagline = false,
                        )
                        CompanionResultDetailsContent(result = result, bottomSafeArea = 96.dp)
                    }
                } else {
                    // 人格 + 命定城市展示（原始碼「無海報」fallback 分支，hero 尚未就緒時的內容路徑）
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                    ) {
                        // T19：海報 hero 圖很慢（約 80 秒），文字內容不等它顯示——Failed/Idle 不佔版位，
                        // Loading 顯示輕量佔位，版面不會因為缺圖而破
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
                }

                // 底部單顆「更多動作」按鈕：兩種版面共用（原版 Ready／fallback 兩分支皆為單顆按鈕，見任務規格第 4 點，
                // 「分享我的旅行 DNA」搬進 BottomSheet 第 2 項）
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Tokens.colorWhite)
                        .navigationBarsPadding()
                        .padding(horizontal = Tokens.spacing300, vertical = Tokens.spacing200),
                ) {
                    Box(Modifier.testTag("companion_result_more_actions_btn")) {
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

        // BottomSheet：繼續規劃／分享我的旅行 DNA／分享到 IG 限時動態／看其他人／回到旅伴
        // （新排序見任務規格第 5 點；查看完整測驗結果/下載圖片列沿用既有定案不顯示）
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
                onShareDna = {
                    showBottomSheet = false
                    shareText(shareCaption)
                },
                onShareToInstagramStories = {
                    showBottomSheet = false
                    // IG 限動不支援帶入文字文案，點擊當下先複製文案到剪貼簿方便貼上（原版行為）；
                    // commonMain 沒有 Toast，這裡先省略提示（見任務報告）
                    if (shareCaption.isNotBlank()) clipboardManager.setText(AnnotatedString(shareCaption))
                    val readyResult = shareImageV2 as? ShareImageV2State.Ready
                    if (readyResult != null) {
                        scope.launch {
                            val bitmap = shareableGraphicsLayer.toImageBitmap()
                            val shared = shareImageToInstagramStory(bitmap, shareCaption)
                            if (!shared) shareText(shareCaption)
                        }
                    } else {
                        // hero 還沒就緒，沒有畫面可截，直接退回純文字分享
                        shareText(shareCaption)
                    }
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
 * 海報 hero 圖（hero 尚未就緒時的 fallback 版面用）：Loading 顯示輕量佔位（不是 shimmer，demo 未移植
 * 那套元件，見 CompanionRootScreen 對 CompanionAsyncImage 的說明）；Failed/Idle 不佔版面直接跳過。
 * hero 就緒（Ready）改由 [ImmersiveShareHeroWithBadge] 處理沈浸式版面，這裡的 Ready 分支不會被呼叫到。
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

        is ShareImageV2State.Ready -> Unit
        ShareImageV2State.Idle, ShareImageV2State.Failed -> Unit
    }
}

/** 後端 hero 圖是 9:16 直式（IG 限動規格） */
private const val HERO_ASPECT_RATIO = 9f / 16f

/**
 * 沈浸式 hero：滿版無左右 padding、無圓角（呼叫端 [ResultScreen] 也拿掉了根 Box 的 statusBarsPadding），
 * 讓圖片往上頂到狀態列下方；底部疊一層黑色半透明資訊卡（目的地＋tagline 白字 + 目的地 stamp 圓圖），
 * 比照原始碼 `PosterHeroWithBadge`（reference SixZonePosterComposer.kt:161-224），
 * 只是把浮動圓角卡片改成貼齊 hero 邊緣的滿版長條，呼應「沈浸式」的滿版訴求。
 * T21：stamp 圓圖改為恆顯示——[stampUrl] 空時用內建 fallback icon 佔位（原本是 stampUrl 空就整塊
 * 不顯示，現在比照 tag icon 走 per-slot fallback，見任務規格 C）。stamp 只有一顆 generic fallback
 * 素材（無 category 對應表，比照 reference `PosterFallbackAssets.stampDrawable()` 不吃參數），
 * 故 `stamp_fallback_category` 欄位雖已補進 domain model，這裡不需要引用。
 */
@Composable
private fun ImmersiveShareHeroWithBadge(
    heroUrl: String,
    destinationCn: String,
    destinationEn: String,
    tagline: String,
    stampUrl: String?,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        CompanionAsyncImage(
            url = heroUrl,
            modifier = Modifier
                .fillMaxWidth()
                .background(Tokens.colorBackgroundPrimaryLighter)
                .testTag("companion_result_hero_image"),
            placeholderAspectRatio = HERO_ASPECT_RATIO,
            blurInOnLoad = true,
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.4f))
                .padding(horizontal = Tokens.spacing300, vertical = Tokens.spacing200)
                .testTag("companion_result_hero_badge"),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    listOf(destinationCn, destinationEn).filter { it.isNotBlank() }.joinToString(" · "),
                    color = Color.White,
                    fontWeight = FontWeight(Tokens.fontWeightBold),
                    fontSize = Tokens.fontSize5,
                    modifier = Modifier.testTag("companion_result_hero_destination"),
                )
                if (tagline.isNotBlank()) {
                    Spacer(Modifier.height(Tokens.spacing050))
                    Text(
                        tagline,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = Tokens.fontSize2,
                    )
                }
            }
            Spacer(Modifier.width(Tokens.spacing200))
            CompanionAsyncImage(
                url = stampUrl.orEmpty(),
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .testTag("companion_result_hero_stamp"),
                placeholder = {
                    Icon(
                        painter = painterResource(PosterFallbackAssets.stampDrawable()),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.fillMaxSize(),
                    )
                },
            )
        }
    }
}

/**
 * fallback_category → 內建 bundled 素材，對應 spec「Per-Slot Asset Fallback」（比照 reference
 * `poster/PosterFallbackAssets.kt`）。類別字串直接來自後端 stamp_fallback_category，
 * 任何未知類別一律退到 generic 版本，避免 App 端因後端新增分類而崩潰。
 * tag 圓圖那組 fallback（`tag_fallback_categories`）已隨後端不再回傳 tag_icon_urls 一併移除——
 * 沈浸式版面現在跟其他版面一樣，只顯示 highlight_tags 純文字 pill。
 */
private object PosterFallbackAssets {
    fun stampDrawable() = Res.drawable.companion_stamp_fallback_generic
}

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

// ---------- T21：海報產圖等待頁（分析成功後、揭曉結果前）----------

private const val TYPEWRITER_CHAR_DELAY_MS = 50L

/** 逐字打字機效果：每 [TYPEWRITER_CHAR_DELAY_MS] ms 多顯示一個字，打字中結尾帶游標；[onFinished] 於整段顯示完後呼叫一次。 */
@Composable
private fun TypewriterText(
    text: String,
    color: Color,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
    onFinished: () -> Unit = {},
) {
    var visibleCount by remember(text) { mutableIntStateOf(0) }
    LaunchedEffect(text) {
        visibleCount = 0
        for (i in 1..text.length) {
            visibleCount = i
            delay(TYPEWRITER_CHAR_DELAY_MS)
        }
        onFinished()
    }
    val isTyping = visibleCount < text.length
    Text(
        text = text.take(visibleCount) + if (isTyping) "▏" else "",
        color = color,
        fontSize = fontSize,
        modifier = modifier,
    )
}

private const val TAG_STAGGER_DELAY_MS = 120L

private const val SEQUENTIAL_ITEM_GAP_MS = 1000L

/**
 * 序列逐項顯示：每項由 [itemContent] 以打字機打完（回呼 onFinished）後，
 * 間隔 [SEQUENTIAL_ITEM_GAP_MS] 再顯示下一項；最後一項打完同樣間隔後呼叫一次 [onAllFinished]，
 * 讓下一個區塊（如 recommendation → social_post）維持相同節奏銜接。
 */
@Composable
private fun SequentialTypewriterItems(
    items: List<String>,
    itemSpacing: Dp,
    modifier: Modifier = Modifier,
    onAllFinished: () -> Unit = {},
    itemContent: @Composable (text: String, onFinished: () -> Unit) -> Unit,
) {
    var typedCount by remember(items) { mutableIntStateOf(0) }
    var visibleCount by remember(items) { mutableIntStateOf(if (items.isEmpty()) 0 else 1) }
    LaunchedEffect(typedCount) {
        when {
            typedCount in 1 until items.size -> {
                delay(SEQUENTIAL_ITEM_GAP_MS)
                visibleCount = typedCount + 1
            }
            typedCount == items.size && items.isNotEmpty() -> {
                delay(SEQUENTIAL_ITEM_GAP_MS)
                onAllFinished()
            }
        }
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(itemSpacing),
    ) {
        items.take(visibleCount).forEachIndexed { index, text ->
            itemContent(text) { typedCount = maxOf(typedCount, index + 1) }
        }
    }
}

/**
 * 海報產圖等待畫面（60s 起跳、可達 180s）：分析結果已到手，
 * 頂部固定頭像/名字/標題，中段可捲動內容依序播
 * reasoning（AI 思考過程逐句對話框，最後一句主色外框強調）→ 身份摘要 → recommendation → social_post，
 * 每項間隔 2 秒、逐字打字機顯示，內容增長時自動捲到底。
 * 底部固定白底列：產圖中顯示呼吸文字；[isPosterReady] 後改顯示「一起去看看」按鈕（[onViewResult]），
 * 不自動跳結果頁（與 iOS 一致）。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PosterGeneratingContent(
    avatarUrl: String,
    companionName: String,
    result: QuizCompletionResult?,
    isPosterReady: Boolean,
    onViewResult: () -> Unit,
) {
    val reasoning = result?.reasoning.orEmpty().filter { it.isNotBlank() }
    val recommendations = result?.recommendation.orEmpty().filter { it.isNotBlank() }
    val socialPost = withoutKkdayHashtag(result?.socialPost.orEmpty())
    val highlightTags = result?.highlightTags.orEmpty()
    val clipboardManager = LocalClipboardManager.current
    // 區塊序列：reasoning 全部播完才輪到身份摘要與 recommendation，recommendation 播完才輪到 social_post
    // reasoning 可能為空陣列（LLM 失敗或舊版 prompt），此時直接從 recommendation 開始
    var reasoningDone by remember(result) { mutableStateOf(reasoning.isEmpty()) }
    var recommendationDone by remember(result) { mutableStateOf(recommendations.isEmpty()) }

    // 打字機持續增高內容：高度一變就自動捲到底，使用者不用自己往下滑
    val scrollState = rememberScrollState()
    LaunchedEffect(scrollState.maxValue) {
        if (scrollState.maxValue > 0) scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .testTag("companion_share_poster_generating"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 頂部固定 header：頭像 + 名字 + 標題（內容捲動時不動）
        Spacer(Modifier.height(Tokens.spacing300))
        CompanionAsyncImage(
            url = avatarUrl,
            blurInOnLoad = true,
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Tokens.colorBackgroundPrimaryLighter),
            placeholder = {
                Text("?", color = Tokens.colorTextPrimaryDark, fontSize = Tokens.fontSize9)
            },
        )
        Spacer(Modifier.height(Tokens.spacing150))
        Text(
            companionName,
            color = Tokens.colorTextPrimaryDark,
            fontWeight = FontWeight(Tokens.fontWeightBold),
            fontSize = Tokens.fontSize5,
        )
        Spacer(Modifier.height(Tokens.spacing100))
        Text(
            "正在解析你的旅行 DNA", // TODO: i18n
            fontWeight = FontWeight(Tokens.fontWeightBold),
            fontSize = Tokens.fontSize4,
            color = Tokens.colorTextDarker,
        )
        Spacer(Modifier.height(Tokens.spacing200))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = Tokens.spacing300),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (reasoning.isNotEmpty()) {
                // 思考過程逐句對話框呈現；最後一句（揭曉目的地）以主色外框與文字強調
                SequentialTypewriterItems(
                    items = reasoning,
                    itemSpacing = Tokens.spacing150,
                    onAllFinished = { reasoningDone = true },
                    modifier = Modifier.testTag("companion_share_poster_generating_reasoning"),
                ) { text, onFinished ->
                    val isHighlight = text == reasoning.last()
                    ReasoningBubble(isHighlight = isHighlight) {
                        TypewriterText(
                            text = text,
                            color = if (isHighlight) Tokens.colorTextPrimaryDark else Tokens.colorTextDarker,
                            fontSize = Tokens.fontSize3,
                            onFinished = onFinished,
                        )
                    }
                }
                Spacer(Modifier.height(Tokens.spacing300))
            }

            // 分析結果摘要（travel_identity / destination / tagline / highlight_tags）：思考過程播完才亮出
            if (reasoningDone) {
                if (result?.travelIdentity.orEmpty().isNotBlank()) {
                    Text(
                        result?.travelIdentity.orEmpty(),
                        fontWeight = FontWeight(Tokens.fontWeightBold),
                        fontSize = Tokens.fontSize6,
                        color = Tokens.colorTextPrimaryDark,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("companion_share_poster_generating_travel_identity"),
                    )
                    Spacer(Modifier.height(Tokens.spacing100))
                }
                if (result?.destinationCn.orEmpty().isNotBlank()) {
                    Text(
                        "${result?.destinationCn.orEmpty()} · ${result?.destinationCountry.orEmpty()}",
                        color = Tokens.colorTextDark,
                        fontSize = Tokens.fontSize3,
                        modifier = Modifier.testTag("companion_share_poster_generating_destination"),
                    )
                    Spacer(Modifier.height(Tokens.spacing100))
                }
                if (result?.tagline.orEmpty().isNotBlank()) {
                    Text(
                        result?.tagline.orEmpty(),
                        color = Tokens.colorTextMedium,
                        fontSize = Tokens.fontSize3,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("companion_share_poster_generating_tagline"),
                    )
                    Spacer(Modifier.height(Tokens.spacing150))
                }
                if (highlightTags.isNotEmpty()) {
                    // 標籤逐一淡入出現，取代一次性全部顯示的乾硬感
                    var revealedTagCount by remember(highlightTags) { mutableIntStateOf(0) }
                    LaunchedEffect(highlightTags) {
                        highlightTags.indices.forEach { index ->
                            delay(TAG_STAGGER_DELAY_MS)
                            revealedTagCount = index + 1
                        }
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(Tokens.spacing100, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(Tokens.spacing100),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("companion_share_poster_generating_highlight_tags"),
                    ) {
                        highlightTags.forEachIndexed { index, tag ->
                            AnimatedVisibility(
                                visible = index < revealedTagCount,
                                enter = fadeIn(tween(200)) + scaleIn(initialScale = 0.85f, animationSpec = tween(200)),
                            ) {
                                SelectablePill(
                                    label = tag,
                                    isSelected = true,
                                    enabled = false,
                                    testTag = "poster_generating_tag_$tag",
                                    onClick = {},
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(Tokens.spacing200))
                }
            }

            if (recommendations.isNotEmpty() && reasoningDone) {
                TitledInfoCard(
                    title = "推薦理由", // TODO: i18n
                    testTag = "companion_share_poster_generating_recommendation",
                ) {
                    SequentialTypewriterItems(
                        items = recommendations,
                        itemSpacing = Tokens.spacing100,
                        onAllFinished = { recommendationDone = true },
                    ) { text, onFinished ->
                        TypewriterText(
                            text = text,
                            color = Tokens.colorTextDarker,
                            fontSize = Tokens.fontSize3,
                            onFinished = onFinished,
                        )
                    }
                }
                Spacer(Modifier.height(Tokens.spacing150))
            }
            if (socialPost.isNotBlank() && reasoningDone && recommendationDone) {
                TitledInfoCard(
                    title = "分享文案", // TODO: i18n
                    testTag = "companion_share_poster_generating_social_post",
                    onCopyClick = { clipboardManager.setText(AnnotatedString(socialPost)) }, // TODO: 複製成功提示
                ) {
                    TypewriterText(
                        text = socialPost,
                        color = Tokens.colorTextDarker,
                        fontSize = Tokens.fontSize3,
                    )
                }
            }
            Spacer(Modifier.height(Tokens.spacing300))
        }

        // 底部固定列：白底、不遮擋上方可捲動內容（內容區以 weight 撐滿，非疊在其上）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Tokens.colorWhite)
                .navigationBarsPadding()
                .padding(horizontal = Tokens.spacing300, vertical = Tokens.spacing200),
            contentAlignment = Alignment.Center,
        ) {
            if (isPosterReady) {
                PosterReadyBanner(onClick = onViewResult)
            } else {
                BreathingLoadingText(
                    text = "繼續描繪你的專屬旅行場景⋯", // TODO: i18n
                )
            }
        }
    }
}

/** 思考過程對話框：一般為白底淺灰外框；[isHighlight]（最後一句揭曉目的地）改為主色外框與底色。 */
@Composable
private fun ReasoningBubble(
    isHighlight: Boolean,
    content: @Composable () -> Unit,
) {
    val bgColor = if (isHighlight) Tokens.colorBackgroundPrimaryLighter else Tokens.colorWhite
    val borderColor = if (isHighlight) Tokens.colorBorderPrimaryLight else Tokens.colorBorderLight
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(Tokens.radiusLg))
                .border(1.dp, borderColor, RoundedCornerShape(Tokens.radiusLg))
                .background(bgColor)
                .padding(Tokens.spacing200),
        ) {
            content()
        }
    }
}

/** 呼吸效果文字：透明度深淺來回漸變，用於置底產圖等待提示。 */
@Composable
private fun BreathingLoadingText(text: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "breathing_loading")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breathing_alpha",
    )
    Text(
        text,
        color = Tokens.colorTextMedium.copy(alpha = alpha),
        fontSize = Tokens.fontSize3,
        modifier = Modifier.testTag("companion_share_poster_generating_breathing"),
    )
}

/** 海報就緒的置底入口：主色橫幅按鈕，點擊才前往結果頁（不自動跳頁，與 iOS 一致）。 */
@Composable
private fun PosterReadyBanner(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Tokens.radiusLg))
            .background(Tokens.colorBackgroundPrimaryMedium)
            .clickable(onClick = onClick)
            .padding(Tokens.spacing200)
            .testTag("companion_share_poster_ready_banner"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "我找到你的旅行 DNA 了", // TODO: i18n
                color = Tokens.colorTextPrimaryLighter,
                fontSize = Tokens.fontSize2,
            )
            Spacer(Modifier.height(Tokens.spacing050))
            Text(
                "我們一起去看看吧！", // TODO: i18n
                color = Tokens.colorWhite,
                fontWeight = FontWeight(Tokens.fontWeightBold),
                fontSize = Tokens.fontSize4,
            )
        }
        Spacer(Modifier.width(Tokens.spacing150))
        Box(
            modifier = Modifier
                .size(Tokens.dimensionIconXl)
                .clip(CircleShape)
                .background(Tokens.colorBackgroundPrimaryLight),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_right_line),
                contentDescription = null,
                tint = Tokens.colorBackgroundPrimaryMedium,
                modifier = Modifier.size(Tokens.dimensionIconSm),
            )
        }
    }
}

/**
 * 旅行人格/tag 標籤——即「推薦理由」以上的區塊。
 * [showDestinationAndTagline]：hero 沈浸式版面會關閉此參數避免與 hero 上的黑色資訊卡重複顯示。
 * [showHighlightTagPills]：hero 沈浸式版面改用 [ShareImageV2TagIconsRow]（tag 圓圖）取代這排純文字
 * chip，此時關閉避免重複；其餘呼叫端（hero 未就緒的 fallback／回顧詳情頁）維持顯示。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CompanionResultHighlightContent(
    result: QuizCompletionResult?,
    showDestinationAndTagline: Boolean = true,
    showHighlightTagPills: Boolean = true,
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
        if (showHighlightTagPills && highlightTags.isNotEmpty()) {
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
    val socialPost = withoutKkdayHashtag(result?.socialPost.orEmpty())
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
 * 導覽選單 bottom sheet（T20 恢復完整版）：繼續規劃／分享我的旅行 DNA／分享到 IG 限時動態／
 * 看其他人／查看完整測驗結果／回到旅伴／回到回顧列表／刪除紀錄（相對順序不變，見任務 brief）。
 * 「下載到我的裝置」原始碼依附海報 bitmap
 * 且本專案不落地存檔，維持不做。
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
    onShareDna: (() -> Unit)? = null,
    onShareToInstagramStories: (() -> Unit)? = null,
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
        // 依任務規格第 5 點排序，列與列之間加分隔線
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
            if (onShareDna != null) {
                add {
                    ActionRow(
                        icon = painterResource(Res.drawable.ic_share_android_line),
                        title = "分享我的旅行 DNA", // TODO: i18n
                        description = "分享給好友或其他社群", // TODO: i18n
                        testTag = "companion_action_share_dna",
                        onClick = onShareDna,
                    )
                }
            }
            if (onShareToInstagramStories != null) {
                add {
                    ActionRow(
                        // TODO: 尚未有專屬 IG 限動 icon，暫沿用既有分享 icon，待設計提供後替換
                        icon = painterResource(Res.drawable.ic_share_android_line),
                        title = "分享到 IG 限時動態", // TODO: i18n
                        description = "一鍵貼到限動", // TODO: i18n
                        testTag = "companion_action_share_ig_stories",
                        onClick = onShareToInstagramStories,
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
            val heroUrl = (shareImageV2 as? ShareImageV2State.Ready)?.result?.heroUrl
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
            val socialPost = withoutKkdayHashtag(result.socialPost)
            if (socialPost.isNotBlank()) {
                Spacer(Modifier.height(Tokens.spacing150))
                TitledInfoCard(
                    title = "分享文案", // TODO: i18n
                    content = socialPost,
                    testTag = "companion_result_detail_social_post",
                    onCopyClick = { clipboardManager.setText(AnnotatedString(socialPost)) }, // TODO: 複製成功提示
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
    onGoHome: () -> Unit,
    onViewOthers: () -> Unit,
    onStartPlanning: (QuizCompletionResult) -> Unit,
) {
    val history by viewModel.quizHistory.collectAsStateWithLifecycle()
    var selectedCreatedAt by remember { mutableStateOf<Long?>(null) }

    val current = history.firstOrNull { it.createdAt == selectedCreatedAt }
    if (current != null) {
        CompanionHistoryDetailContent(
            record = current,
            onBack = {
                viewModel.loadQuizHistory()
                selectedCreatedAt = null
            },
            onGoHome = onGoHome,
            onViewOthers = onViewOthers,
            onStartPlanning = { onStartPlanning(current.result) },
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
 * 單筆歷史詳情：使用已回填的遠端 hero，沿用結果頁的沈浸式海報與更多動作。
 */
@Composable
private fun CompanionHistoryDetailContent(
    record: QuizHistoryRecord,
    onBack: () -> Unit,
    onGoHome: () -> Unit,
    onViewOthers: () -> Unit,
    onStartPlanning: () -> Unit,
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    var bottomBarHeight by remember { mutableStateOf(0.dp) }
    val shareText = rememberShareText()
    val shareImageToInstagramStory = rememberShareImageToInstagramStory()
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val shareableGraphicsLayer = rememberGraphicsLayer()
    val shareCaption = withoutKkdayHashtag(record.result.socialPost).ifBlank {
        "我的旅行人格是${record.result.travelIdentity}，命定城市是${record.result.destinationCn}！" // TODO: i18n
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("companion_history_detail_screen")
            .background(Tokens.colorWhite),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier.drawWithContent {
                        shareableGraphicsLayer.record { this@drawWithContent.drawContent() }
                        drawLayer(shareableGraphicsLayer)
                    },
                ) {
                    ImmersiveShareHeroWithBadge(
                        heroUrl = record.heroImageUrl,
                        destinationCn = record.result.destinationCn,
                        destinationEn = record.result.destinationEn,
                        tagline = record.result.tagline,
                        stampUrl = record.stampImageUrl,
                    )
                }
                HistoryDetailCloseButton(
                    modifier = Modifier.align(Alignment.TopEnd),
                    onClick = onBack,
                )
            }
            Spacer(Modifier.height(Tokens.spacing200))
            CompanionResultHighlightContent(record.result, showDestinationAndTagline = false)
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
            Box(Modifier.testTag("companion_history_more_actions_btn")) {
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

    if (showBottomSheet) {
        ResultActionsBottomSheet(
            result = record.result,
            onDismiss = { showBottomSheet = false },
            onViewDetail = {},
            onGoHome = {
                showBottomSheet = false
                onGoHome()
            },
            showViewDetail = false,
            onContinuePlanning = {
                showBottomSheet = false
                onStartPlanning()
            },
            onShareDna = {
                showBottomSheet = false
                shareText(shareCaption)
            },
            onShareToInstagramStories = {
                showBottomSheet = false
                if (shareCaption.isNotBlank()) clipboardManager.setText(AnnotatedString(shareCaption))
                scope.launch {
                    val bitmap = shareableGraphicsLayer.toImageBitmap()
                    val shared = shareImageToInstagramStory(bitmap, shareCaption)
                    if (!shared) shareText(shareCaption)
                }
            },
            onViewOthers = {
                showBottomSheet = false
                onViewOthers()
            },
        )
    }
}

@Composable
private fun HistoryDetailCloseButton(modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .statusBarsPadding()
            .padding(top = Tokens.spacing150, end = Tokens.spacing150)
            .size(34.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.4f))
            .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
            .clickable(onClick = onClick)
            .testTag("companion_history_detail_close_btn"),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_cross_line),
            contentDescription = null,
            tint = Tokens.colorWhite,
            modifier = Modifier.size(Tokens.dimensionIconXs),
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
