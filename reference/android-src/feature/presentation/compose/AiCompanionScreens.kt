package com.kkday.feature.ai_companion.presentation.compose

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.app.Activity
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kkday.design.StyleDictionary
import com.kkday.design.dialog.v3.KKModalBottomSheetDragHandle
import com.kkday.design.button.ButtonSizeType
import com.kkday.design.button.ButtonState
import com.kkday.design.button.ButtonType
import com.kkday.design.button.KKButton
import com.kkday.design.font.fontH6
import com.kkday.design.kkTag.KKTag
import com.kkday.design.kkTag.KKTagSolidColor
import com.kkday.design.model.StringType
import com.kkday.design.textfield.KKTextField
import com.kkday.feature.ai_companion.viewModel.AiCompanionViewModel
import com.kkday.feature.ai_companion.viewModel.AnalysisState
import com.kkday.feature.ai_companion.viewModel.CompanionCreationState
import com.kkday.feature.ai_companion.viewModel.IntroductionState
import com.kkday.feature.ai_companion.viewModel.PartnerState
import com.kkday.feature.ai_companion.viewModel.QuizGalleryState
import com.kkday.feature.ai_companion.viewModel.QuizState
import com.kkday.feature.ai_companion.viewModel.ShareImageV2State
import com.kkday.feature.ai_companion.viewModel.TravelGuideState
import com.kkday.design.dialog.DialogHeaderType
import com.kkday.design.dialog.KKDialog
import com.kkday.feature.ai_companion.presentation.poster.OffscreenSixZonePoster
import com.kkday.feature.ai_companion.presentation.poster.PosterBelowHeroContent
import com.kkday.feature.ai_companion.presentation.poster.PosterHeroWithBadge
import com.kkday.feature.ai_companion.presentation.poster.PosterHistoryStorage
import com.kkday.feature.ai_companion.presentation.poster.PosterOverlayMetrics
import com.kkday.feature.ai_companion.presentation.poster.ResolvedPosterAssets
import com.kkday.model.companion.AiPartnerResult
import com.kkday.model.companion.CompanionAppearanceOption
import com.kkday.model.companion.CompanionTraitOption
import com.kkday.model.companion.QuizGalleryItem
import com.kkday.library.common.extension.copyToClipboard
import com.kkday.library.common.extension.showToast
import com.kkday.model.companion.QuizHistoryRecord
import com.kkday.model.companion.SavedTripRecord
import com.kkday.model.companion.ShareImageV2Content
import com.kkday.router.SearchResultRouter
import org.koin.compose.koinInject

enum class AiCompanionStep {
    CreateCompanion, Intro, Home, Quiz, Result, ResultDetail, History, QuizGallery, QuizGalleryDetail,
    // Phase 2 行程規劃：開場摘要（travel-summary）→ 聊天式偏好問卷（純前端，在 PlanChat 內）
    // → 行程成果（travel-guide；總覽/每日細節都在 TripResult 的分頁內，無獨立細節頁）
    // TripList = 回顧我的旅程列表（純本地資料）
    // OrderOpening = demo「帶訂單開場」：抓即將出發訂單材料 → LLM 判斷目的地選項 → 選定後進 PlanChat
    ImportItinerary, PlanChat, TripResult, TripList, OrderOpening,
}

/**
 * AI 旅伴測驗流程根組件。
 * 注意：此為功能性 fallback UI —— 使用 material3 primitives + StyleDictionary token +
 * 硬編字串（標 TODO），待設計素材與 PM 字串到位再替換為設計系統元件（KKButton 等）與 anime 背景。
 * E2E 選擇器一律用 testTag（不用會被 PM 替換的中文字串）。
 */
@Composable
fun AiCompanionRoot(
    viewModel: AiCompanionViewModel,
    startStep: AiCompanionStep,
    onFinish: () -> Unit,
) {
    var step by remember { mutableStateOf(startStep) }
    // 查看社群若是從其他 step（如旅伴主頁）點進來，記錄返回目的地；直接以 QuizGallery 為起始頁進入時維持 null，
    // 返回改為 onFinish()（沒有可返回的旅伴流程 step）
    var quizGalleryReturnStep by remember { mutableStateOf<AiCompanionStep?>(null) }
    // 查看社群項目詳情：從 bottomsheet 改為獨立頁面後，選取的項目需在 QuizGallery/QuizGalleryDetail 間跨 step 保留
    var selectedGalleryItem by remember { mutableStateOf<QuizGalleryItem?>(null) }
    // Phase 2 行程規劃：成果頁返回目的地（首頁/聊天室/我的旅程列表，依進入來源）
    var tripReturnStep by remember { mutableStateOf(AiCompanionStep.Home) }

    when (step) {
        AiCompanionStep.CreateCompanion -> {
            LaunchedEffect(Unit) { viewModel.loadAiPartner() }
            CreateCompanionScreen(
                viewModel = viewModel,
                onCreated = {
                    viewModel.confirmCompanionCreation()
                    step = AiCompanionStep.Intro
                },
                onBack = onFinish,
            )
        }

        AiCompanionStep.Intro -> {
            val creation by viewModel.creationState.collectAsStateWithLifecycle()
            val introduction by viewModel.introductionState.collectAsStateWithLifecycle()
            // 從入口直接進 Intro 等 confirmCompanionCreation 沒被呼叫的情境，補打一次
            LaunchedEffect(Unit) {
                if (introduction is IntroductionState.Idle) viewModel.fetchSelfIntroduction()
            }
            CompanionBornScreen(
                creation = creation,
                introduction = introduction,
                onGoHome = { step = AiCompanionStep.Home },
                onRecreate = {
                    viewModel.recreateCompanion()
                    step = AiCompanionStep.CreateCompanion
                },
            )
        }

        AiCompanionStep.Home -> {
            val creation by viewModel.creationState.collectAsStateWithLifecycle()
            val introductionState by viewModel.introductionState.collectAsStateWithLifecycle()
            val savedTrips by viewModel.savedTrips.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) { viewModel.loadSavedTrips() }
            CompanionHomeScreen(
                creation = creation,
                introduction = (introductionState as? IntroductionState.Loaded)?.introduction.orEmpty(),
                onTravelDna = {
                    viewModel.fetchQuiz()
                    step = AiCompanionStep.Quiz
                },
                onHistory = {
                    viewModel.loadQuizHistory()
                    step = AiCompanionStep.History
                },
                onQuizGallery = {
                    quizGalleryReturnStep = AiCompanionStep.Home
                    step = AiCompanionStep.QuizGallery
                },
                onImportItinerary = { step = AiCompanionStep.ImportItinerary },
                onTripList = { step = AiCompanionStep.TripList },
                onPlanTrip = {
                    viewModel.startPlanFromZero()
                    step = AiCompanionStep.PlanChat
                },
                onPlanTripWithOrders = {
                    viewModel.startPlanFromOrders()
                    step = AiCompanionStep.OrderOpening
                },
                onPlanTripWithWish = {
                    viewModel.startPlanFromWish()
                    step = AiCompanionStep.OrderOpening
                },
                onPlanTripWithHistory = {
                    viewModel.startPlanFromHistory()
                    step = AiCompanionStep.OrderOpening
                },
                savedTrips = savedTrips,
                onOpenSavedTrip = { trip ->
                    viewModel.openSavedTrip(trip)
                    tripReturnStep = AiCompanionStep.Home
                    step = AiCompanionStep.TripResult
                },
                onBack = onFinish,
                onRecreate = {
                    viewModel.recreateCompanion()
                    step = AiCompanionStep.CreateCompanion
                },
            )
        }

        AiCompanionStep.ImportItinerary -> {
            val creation by viewModel.creationState.collectAsStateWithLifecycle()
            ImportItineraryScreen(
                avatarUrl = creation.avatarUrl,
                onClose = { step = AiCompanionStep.Home },
                onImport = { content ->
                    viewModel.startPlanFromImport(content)
                    step = AiCompanionStep.PlanChat
                },
            )
        }

        AiCompanionStep.OrderOpening -> {
            val creation by viewModel.creationState.collectAsStateWithLifecycle()
            val openingState by viewModel.orderOpeningState.collectAsStateWithLifecycle()
            OrderOpeningScreen(
                avatarUrl = creation.avatarUrl,
                state = openingState,
                onBack = { step = AiCompanionStep.Home },
                onRetry = { viewModel.retryPlanFromOrders() },
                onSelectOption = { option ->
                    viewModel.selectOrderDestination(option)
                    step = AiCompanionStep.PlanChat
                },
                onDirectPlan = {
                    viewModel.startCityChatFromOpening()
                    step = AiCompanionStep.PlanChat
                },
                onPlanFromZeroInstead = {
                    viewModel.startPlanFromZero()
                    step = AiCompanionStep.PlanChat
                },
            )
        }

        AiCompanionStep.PlanChat -> {
            val creation by viewModel.creationState.collectAsStateWithLifecycle()
            val summaryState by viewModel.travelSummaryState.collectAsStateWithLifecycle()
            val cityState by viewModel.recommendCityState.collectAsStateWithLifecycle()
            val preferenceState by viewModel.preferenceChatState.collectAsStateWithLifecycle()
            val guideState by viewModel.travelGuideState.collectAsStateWithLifecycle()
            val preludeMessages by viewModel.planChatPrelude.collectAsStateWithLifecycle()
            PlanChatScreen(
                state = summaryState,
                cityState = cityState,
                preferenceState = preferenceState,
                guideState = guideState,
                preludeMessages = preludeMessages,
                avatarUrl = creation.avatarUrl,
                onBack = { step = AiCompanionStep.Home },
                onSendInput = { text -> viewModel.sendPlanInput(text) },
                onRetrySummary = { viewModel.retryTravelSummary() },
                onRetryCity = { viewModel.retryRecommendCity() },
                onPlayQuiz = {
                    // 同旅伴主頁「找到我的旅行 DNA 及命定旅程」入口
                    viewModel.fetchQuiz()
                    step = AiCompanionStep.Quiz
                },
                onStartCityChat = { viewModel.startCityRecommendation() },
                onSwapCity = { viewModel.swapRecommendedCity() },
                onRestartCityChat = { viewModel.restartCityChat() },
                onStartPreferences = { viewModel.startPreferenceChat() },
                // 完成時刻留在聊天室（摘要卡＋完成宣告＋主行動），點「看看完整行程」才進成果頁
                onSubmitPreferences = { viewModel.submitPreferences() },
                onRetryGuide = { viewModel.retryTravelGuide() },
                onViewTrip = {
                    tripReturnStep = AiCompanionStep.PlanChat
                    step = AiCompanionStep.TripResult
                },
            )
        }

        AiCompanionStep.TripResult -> {
            val creation by viewModel.creationState.collectAsStateWithLifecycle()
            val guideState by viewModel.travelGuideState.collectAsStateWithLifecycle()
            val reviseState by viewModel.tripReviseState.collectAsStateWithLifecycle()
            val productStates by viewModel.tripProductStates.collectAsStateWithLifecycle()
            TripResultScreen(
                state = guideState,
                reviseState = reviseState,
                productStates = productStates,
                avatarUrl = creation.avatarUrl,
                companionName = creation.companionName.ifBlank { "旅伴" }, // TODO: replace with stringResource fallback
                onClose = { step = tripReturnStep },
                onRetry = { viewModel.retryTravelGuide() },
                // 儲存後留在本頁（不導回首頁），可繼續編輯／請旅伴修改
                onSave = { viewModel.saveActiveTrip() },
                onStartRevise = { dayNumber -> viewModel.startTripRevise(dayNumber) },
                onSendRevise = { text -> viewModel.sendTripRevise(text) },
                onRetryRevise = { viewModel.retryTripRevise() },
                onDismissRevise = { viewModel.endTripRevise() },
                onReorderItems = { dayNumber, from, to -> viewModel.reorderDayItems(dayNumber, from, to) },
                onSearchProduct = { spotName -> viewModel.searchTripProduct(spotName) },
            )
        }

        AiCompanionStep.TripList -> {
            val savedTrips by viewModel.savedTrips.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) { viewModel.loadSavedTrips() }
            SavedTripListScreen(
                trips = savedTrips,
                onBack = { step = AiCompanionStep.Home },
                onOpen = { trip ->
                    viewModel.openSavedTrip(trip)
                    tripReturnStep = AiCompanionStep.TripList
                    step = AiCompanionStep.TripResult
                },
                onDelete = { viewModel.deleteSavedTrip(it) },
            )
        }

        AiCompanionStep.History -> {
            CompanionHistoryScreen(
                viewModel = viewModel,
                onBack = { step = AiCompanionStep.Home },
            )
        }

        AiCompanionStep.QuizGallery -> {
            LaunchedEffect(Unit) { viewModel.loadQuizGallery() }
            val gallery by viewModel.quizGalleryState.collectAsStateWithLifecycle()
            QuizGalleryScreen(
                state = gallery,
                onBack = {
                    val returnStep = quizGalleryReturnStep
                    if (returnStep != null) step = returnStep else onFinish()
                },
                onItemClick = { item ->
                    selectedGalleryItem = item
                    step = AiCompanionStep.QuizGalleryDetail
                },
            )
        }

        AiCompanionStep.QuizGalleryDetail -> {
            selectedGalleryItem?.let { item ->
                QuizGalleryDetailScreen(
                    item = item,
                    onBack = { step = AiCompanionStep.QuizGallery },
                )
            }
        }

        AiCompanionStep.Quiz -> {
            val creation by viewModel.creationState.collectAsStateWithLifecycle()
            QuizScreen(
                viewModel = viewModel,
                creation = creation,
                onSubmit = {
                    viewModel.submitQuiz()
                    step = AiCompanionStep.Result
                },
                onBack = { step = AiCompanionStep.Home },
            )
        }

        AiCompanionStep.Result -> {
            val creation by viewModel.creationState.collectAsStateWithLifecycle()
            ResultScreen(
                viewModel = viewModel,
                creation = creation,
                onViewDetail = { step = AiCompanionStep.ResultDetail },
                onGoHome = { step = AiCompanionStep.Home },
                onRetry = { step = AiCompanionStep.Quiz },
                onRetakeQuiz = {
                    viewModel.startNewQuizRound()
                    step = AiCompanionStep.Home
                },
                onViewOthers = {
                    quizGalleryReturnStep = AiCompanionStep.Result
                    step = AiCompanionStep.QuizGallery
                },
                onStartPlanning = {
                    viewModel.startPlanFromQuizResult()
                    tripReturnStep = AiCompanionStep.PlanChat
                    step = AiCompanionStep.PlanChat
                },
            )
        }

        AiCompanionStep.ResultDetail -> {
            val creation by viewModel.creationState.collectAsStateWithLifecycle()
            ResultDetailScreen(
                viewModel = viewModel,
                creation = creation,
                onBack = { step = AiCompanionStep.Result },
            )
        }
    }
}

// ---------- 共用小元件 ----------

// testTag 命名慣例：各步驟根容器 companion_<step>_screen；按鈕 _btn；狀態容器以語意命名。
@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ScreenScaffold(
    title: String,
    screenTag: String,
    onBack: (() -> Unit)? = null,
    action: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    SideEffect {
        val window = (view.context as Activity).window
        window.statusBarColor = Color.Transparent.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .semantics { testTagsAsResourceId = true }
            .testTag(screenTag)
            .background(StyleDictionary.kkColorBackgroundSurfaceLight)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(StyleDictionary.kkSpacing300),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("companion_back_btn"),
                ) {
                    Icon(
                        painter = painterResource(com.kkday.design.R.drawable.ic_arrow_left_line),
                        contentDescription = null,
                        tint = StyleDictionary.kkColorTextDarker,
                    )
                }
                Spacer(Modifier.width(StyleDictionary.kkSpacing100))
            }
            Text(
                text = title,
                style = fontH6,
                modifier = Modifier.weight(1f),
            )
            action?.invoke()
        }
        Spacer(Modifier.height(StyleDictionary.kkSpacing300))
        content()
    }
}

@Composable
private fun PrimaryButton(
    text: String,
    enabled: Boolean = true,
    testTag: String? = null,
    onClick: () -> Unit,
) {
    Box(modifier = if (testTag != null) Modifier.testTag(testTag) else Modifier) {
        KKButton(
            buttonText = text,
            buttonType = ButtonType.PRIMARY,
            buttonState = if (enabled) ButtonState.ENABLED else ButtonState.DISABLED,
            buttonSizeType = ButtonSizeType.Lg,
            isFullWidth = true,
            onClick = onClick,
        )
    }
}

/**
 * 圖片載入中的 shimmer 佔位動畫。
 * 註：不用設計系統的 Modifier.shimmerEffect() —— 它宣告了 size 但沒接 onGloballyPositioned 回填，
 * 漸層起訖永遠是 (0,0)，實際呈現靜態灰色（不會動）。
 */
private fun Modifier.companionShimmer(): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "companion_shimmer")
    val startOffsetX by transition.animateFloat(
        initialValue = -2f * size.width,
        targetValue = 2f * size.width,
        animationSpec = infiniteRepeatable(animation = tween(1200)),
        label = "companion_shimmer_offset",
    )
    background(
        brush = Brush.linearGradient(
            colors = listOf(
                StyleDictionary.kkColorBackgroundSurfaceMedium,
                StyleDictionary.kkColorBackgroundSurfaceLight,
                StyleDictionary.kkColorBackgroundSurfaceMedium,
            ),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.width, size.height.toFloat()),
        )
    ).onGloballyPositioned { size = it.size }
}

private const val BLUR_IN_DURATION_MS = 2000
private val BLUR_IN_START_RADIUS = 20.dp

// 記錄已經跑過模糊轉清晰動畫的圖片網址：同一張圖從其他頁 back 回來時直接顯示清晰圖，不重播動畫。
// 存活範圍等同於當次 App 進程（不需跨進程持久化），只有首次出現才需要動畫效果。
private val blurAnimatedUrls = mutableSetOf<String>()

/**
 * 統一的旅伴/題目圖片載入元件：
 * - 載入中：預設 shimmer 佔位（不留白）；可用 [loadingContent] 覆寫（如結果頁的讀取中樣式）
 * - 載入失敗：預設顯示 [placeholder]；可用 [errorContent] 覆寫並取得 retry callback 重新載入
 * - 沒有 URL：顯示 [placeholder]
 * - [blurInOnLoad]：圖片第一次載入完成瞬間從模糊漸變為清晰（[BLUR_IN_DURATION_MS] ms），
 *   同一張圖之後再次出現（如返回上一頁再進來）直接顯示清晰圖；
 *   Modifier.blur 在 API 31 以下無效果，舊機型會直接顯示清晰圖，沒有動畫過程
 * - [placeholderAspectRatio]：載入中先以此比例佔位，載入完成後改用圖片實際比例撐高度，
 *   避免產圖比例與預期不符時被 letterbox 上下留白（海報頂部/底部出現白邊的根因）
 * modifier 決定外框大小/形狀/testTag。
 */
@Composable
internal fun CompanionAsyncImage(
    url: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    contentDescription: String? = null,
    blurInOnLoad: Boolean = false,
    placeholderAspectRatio: Float? = null,
    placeholder: @Composable () -> Unit = {},
    loadingContent: (@Composable () -> Unit)? = null,
    errorContent: (@Composable (retry: () -> Unit) -> Unit)? = null,
) {
    if (url.isEmpty()) {
        val emptyModifier = placeholderAspectRatio?.let { modifier.aspectRatio(it) } ?: modifier
        Box(modifier = emptyModifier, contentAlignment = Alignment.Center) { placeholder() }
        return
    }
    // retryAttempt 改變 → ImageRequest 參數改變 → Coil 重新發起載入
    var retryAttempt by remember(url) { mutableIntStateOf(0) }
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .setParameter("companion_retry", retryAttempt, memoryCacheKey = null)
            .build()
    )
    val state = painter.state
    val shouldBlurIn = blurInOnLoad && url !in blurAnimatedUrls
    val blurRadius = remember(url) { Animatable(if (shouldBlurIn) BLUR_IN_START_RADIUS.value else 0f) }
    LaunchedEffect(state, url) {
        if (shouldBlurIn && state is AsyncImagePainter.State.Success) {
            blurRadius.animateTo(0f, animationSpec = tween(BLUR_IN_DURATION_MS))
            blurAnimatedUrls.add(url)
        }
    }
    val sizedModifier = if (placeholderAspectRatio != null) {
        val intrinsicRatio = (state as? AsyncImagePainter.State.Success)
            ?.painter?.intrinsicSize
            ?.takeIf { it.height > 0f && it.width > 0f }
            ?.let { it.width / it.height }
        modifier.aspectRatio(intrinsicRatio ?: placeholderAspectRatio)
    } else {
        modifier
    }
    Box(modifier = sizedModifier) {
        if (state is AsyncImagePainter.State.Error) {
            Box(modifier = Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                errorContent?.invoke { retryAttempt++ } ?: placeholder()
            }
        } else {
            Image(
                painter = painter,
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = Modifier.matchParentSize().blur(blurRadius.value.dp),
            )
            if (state is AsyncImagePainter.State.Loading) {
                if (loadingContent != null) {
                    Box(modifier = Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                        loadingContent()
                    }
                } else {
                    Box(modifier = Modifier.matchParentSize().companionShimmer())
                }
            }
        }
    }
}

// ---------- 各步驟畫面（功能性 fallback）----------

// Phase 1 mockup 採用版順序：先定個性（靈魂）→ 打造外觀 → 產圖預覽（含命名）→ 召喚
private const val STEP_PERSONALITY = 0
private const val STEP_APPEARANCE = 1
private const val STEP_PREVIEW = 2

@OptIn(ExperimentalLayoutApi::class, ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun CreateCompanionScreen(
    viewModel: AiCompanionViewModel,
    onCreated: () -> Unit,
    onBack: () -> Unit,
) {
    val partner by viewModel.partnerState.collectAsStateWithLifecycle()
    val creation by viewModel.creationState.collectAsStateWithLifecycle()
    var currentStep by remember { mutableIntStateOf(STEP_PERSONALITY) }

    val view = LocalView.current
    SideEffect {
        val window = (view.context as Activity).window
        window.statusBarColor = Color.Transparent.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
    }

    val p = partner
    if (p is PartnerState.Loaded) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .semantics { testTagsAsResourceId = true }
                .testTag("companion_create_screen"),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StyleDictionary.kkColorWhite)
                    .statusBarsPadding(),
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = "打造你的專屬旅伴", // TODO: replace with stringResource - 建立旅伴標題
                            style = fontH6,
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                when {
                                    currentStep == STEP_PREVIEW -> currentStep = STEP_APPEARANCE
                                    currentStep == STEP_APPEARANCE -> currentStep = STEP_PERSONALITY
                                    else -> onBack()
                                }
                            },
                            modifier = Modifier.testTag("companion_back_btn"),
                        ) {
                            Icon(
                                painter = painterResource(com.kkday.design.R.drawable.ic_arrow_left_line),
                                contentDescription = null,
                                tint = StyleDictionary.kkColorTextDarker,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(StyleDictionary.kkColorWhite),
                )
                Box(Modifier.padding(horizontal = StyleDictionary.kkSpacing200)) {
                    StepIndicator(currentStep)
                }
                Spacer(Modifier.height(StyleDictionary.kkSpacing150))
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(StyleDictionary.kkColorBackgroundSurfaceLight)
                    .verticalScroll(rememberScrollState())
                    .padding(StyleDictionary.kkSpacing300),
            ) {
                when (currentStep) {
                    STEP_PERSONALITY -> PersonalityStepContent(
                        partner = p.partner,
                        creation = creation,
                        viewModel = viewModel,
                    )
                    STEP_APPEARANCE -> AppearanceStepContent(
                        partner = p.partner,
                        creation = creation,
                        viewModel = viewModel,
                    )
                    STEP_PREVIEW -> PreviewStepContent(
                        creation = creation,
                        viewModel = viewModel,
                        onReselect = { currentStep = STEP_APPEARANCE },
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StyleDictionary.kkColorWhite)
                    .drawBehind {
                        drawLine(
                            color = StyleDictionary.kkColorBorderLighter,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx(),
                        )
                    }
                    .navigationBarsPadding()
                    .padding(StyleDictionary.kkSpacing200),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                when (currentStep) {
                    STEP_PERSONALITY -> {
                        val isPersonalityComplete =
                            creation.selectedPersonality.isNotEmpty() && creation.selectedSpeechStyle != null
                        Box(Modifier.fillMaxWidth().testTag("companion_personality_next_btn")) {
                            KKButton(
                                buttonText = "下一步：打造外觀", // TODO: replace with stringResource
                                buttonType = ButtonType.PRIMARY,
                                buttonState = if (isPersonalityComplete) ButtonState.ENABLED else ButtonState.DISABLED,
                                buttonSizeType = ButtonSizeType.Md,
                                onClick = { currentStep = STEP_APPEARANCE },
                                isFullWidth = true,
                            )
                        }
                        Spacer(Modifier.height(StyleDictionary.kkSpacing050))
                        Text(
                            "先定個性，再幫它畫一張臉", // TODO: replace with stringResource
                            color = StyleDictionary.kkColorTextMedium,
                            fontSize = StyleDictionary.kkFontSize2,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                    }
                    STEP_APPEARANCE -> {
                        Box(Modifier.fillMaxWidth().testTag("companion_next_btn")) {
                            KKButton(
                                buttonText = "看看它的樣子", // TODO: replace with stringResource
                                buttonType = ButtonType.PRIMARY,
                                buttonState = if (creation.isAppearanceComplete) ButtonState.ENABLED else ButtonState.DISABLED,
                                buttonSizeType = ButtonSizeType.Md,
                                onClick = { currentStep = STEP_PREVIEW },
                                isFullWidth = true,
                            )
                        }
                        Spacer(Modifier.height(StyleDictionary.kkSpacing200))
                        Text(
                            "不知道怎麼選？", // TODO: replace with stringResource
                            color = StyleDictionary.kkColorTextMedium,
                            fontSize = StyleDictionary.kkFontSize3,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(StyleDictionary.kkSpacing100))
                        Box(Modifier.testTag("companion_random_btn")) {
                            KKButton(
                                buttonText = "隨機配一個", // TODO: replace with stringResource
                                buttonType = ButtonType.PRIMARY_SUBTLE,
                                buttonState = ButtonState.ENABLED,
                                buttonSizeType = ButtonSizeType.Sm,
                                onClick = {
                                    // 選項已由 ai-partner API 取回，本地亂數選好四個外觀維度後直接進外觀預覽
                                    viewModel.randomizeAppearance()
                                    currentStep = STEP_PREVIEW
                                },
                                leadingIcon = com.kkday.design.R.drawable.ic_intersect_square_line,
                            )
                        }
                    }
                    STEP_PREVIEW -> {
                        Box(Modifier.fillMaxWidth().testTag("companion_create_btn")) {
                            KKButton(
                                buttonText = "召喚我的旅伴", // TODO: replace with stringResource
                                buttonType = ButtonType.PRIMARY,
                                buttonState = if (creation.isAllDimensionsSelected) ButtonState.ENABLED else ButtonState.DISABLED,
                                buttonSizeType = ButtonSizeType.Md,
                                onClick = onCreated,
                                isFullWidth = true,
                            )
                        }
                        Spacer(Modifier.height(StyleDictionary.kkSpacing050))
                        Text(
                            "✦ 結合外觀＋個性，召喚會說話的旅伴", // TODO: replace with stringResource
                            color = StyleDictionary.kkColorTextMedium,
                            fontSize = StyleDictionary.kkFontSize2,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    } else {
        ScreenScaffold(title = "打造你的專屬旅伴", screenTag = "companion_create_screen", onBack = onBack) { // TODO: replace with stringResource - 建立旅伴標題
            // ScreenScaffold 的外層 Column 套用 verticalScroll，會把高度限制變成無限，Modifier.fillMaxSize()
            // 在此情境下對高度沒有效果（無額外空間可置中）。改用 heightIn(min = ...) 強制保留至少一個螢幕高度
            // （扣除標題列估計高度）的空間，讓 contentAlignment = Center 能真正把內容置中，而非貼齊左上角。
            val minContentHeight = LocalConfiguration.current.screenHeightDp.dp - COMPANION_CREATE_HEADER_HEIGHT
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minContentHeight),
                contentAlignment = Alignment.Center,
            ) {
                when (p) {
                    is PartnerState.Loading, PartnerState.Idle ->
                        CircularProgressIndicator(modifier = Modifier.testTag("companion_create_loading"))
                    is PartnerState.Empty ->
                        Text(
                            "目前無法載入旅伴設定，請稍後再試", // TODO: replace with stringResource - 空狀態
                            modifier = Modifier.testTag("companion_create_empty"),
                        )
                    is PartnerState.Error ->
                        Text(
                            "載入失敗，請稍後再試", // TODO: replace with stringResource - 錯誤狀態
                            modifier = Modifier.testTag("companion_create_error"),
                        )
                    else -> {}
                }
            }
        }
    }
}

// ScreenScaffold 標題列（含 padding）估計高度，用於建立旅伴 loading/empty/error 置中的最小高度計算
private val COMPANION_CREATE_HEADER_HEIGHT = 120.dp

@Composable
private fun StepIndicator(currentStep: Int) {
    // 個性為第一步；外觀與產圖預覽（含命名）同屬第二步
    val visualStep = if (currentStep == STEP_PERSONALITY) 0 else 1
    val steps = listOf("1. 個性", "2. 外觀") // TODO: replace with stringResource
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        steps.forEachIndexed { index, label ->
            val isActive = index <= visualStep
            Column(modifier = Modifier.weight(1f)) {
                LinearProgressIndicator(
                    progress = { if (isActive) 1f else 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(StyleDictionary.kkSpacing050)
                        .clip(RoundedCornerShape(StyleDictionary.kkRadiusXs)),
                    color = StyleDictionary.kkColorBackgroundPrimaryMedium,
                    trackColor = StyleDictionary.kkColorBorderLight,
                    strokeCap = StrokeCap.Round,
                )
                Spacer(Modifier.height(StyleDictionary.kkSpacing050))
                Text(
                    text = label,
                    fontSize = StyleDictionary.kkFontSize2,
                    fontWeight = if (index == visualStep) FontWeight(StyleDictionary.kkFontWeightMediumAndroid) else FontWeight.Normal,
                    color = if (isActive) StyleDictionary.kkColorTextPrimaryDark else StyleDictionary.kkColorTextMedium,
                )
            }
            if (index < steps.lastIndex) Spacer(Modifier.width(StyleDictionary.kkSpacing100))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AppearanceStepContent(
    partner: AiPartnerResult,
    creation: CompanionCreationState,
    viewModel: AiCompanionViewModel,
) {
    // 上一步（個性）已完成的小結
    PersonalityDoneCard(creation)

    Spacer(Modifier.height(StyleDictionary.kkSpacing200))

    Text(
        "選好以下四項外觀，就能看看它的樣子", // TODO: replace with stringResource
        fontSize = StyleDictionary.kkFontSize3,
        color = StyleDictionary.kkColorTextMedium,
    )
    Spacer(Modifier.height(StyleDictionary.kkSpacing200))
    Text(
        "打造外觀", // TODO: replace with stringResource
        fontWeight = FontWeight(StyleDictionary.kkFontWeightBold),
        fontSize = StyleDictionary.kkFontSize4,
        color = StyleDictionary.kkColorTextDarker,
    )
    Spacer(Modifier.height(StyleDictionary.kkSpacing200))

    DimensionCard(title = "性別", testTag = "companion_dim_gender", icon = com.kkday.design.R.drawable.ic_gender_male_line) { // TODO: replace with stringResource
        AppearancePills(partner.gender, creation.selectedGender, viewModel::selectGender, "gender")
    }
    DimensionCard(title = "髮型", testTag = "companion_dim_hair_style", icon = com.kkday.design.R.drawable.ic_neutral_face_line) { // TODO: replace with stringResource
        AppearancePills(partner.hairStyle, creation.selectedHairStyle, viewModel::selectHairStyle, "hair_style")
    }
    DimensionCard(title = "髮色", testTag = "companion_dim_hair_color", icon = com.kkday.design.R.drawable.ic_people_line) { // TODO: replace with stringResource
        AppearancePills(partner.hairColor, creation.selectedHairColor, viewModel::selectHairColor, "hair_color")
    }
    DimensionCard(title = "服裝", testTag = "companion_dim_outfit", icon = com.kkday.design.R.drawable.ic_suitcase_line) { // TODO: replace with stringResource
        AppearancePills(partner.outfit, creation.selectedOutfit, viewModel::selectOutfit, "outfit")
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PreviewStepContent(
    creation: CompanionCreationState,
    viewModel: AiCompanionViewModel,
    onReselect: () -> Unit,
) {
    val selectedTags = listOfNotNull(
        creation.selectedGender?.label,
        creation.selectedHairStyle?.label,
        creation.selectedHairColor?.label,
        creation.selectedOutfit?.label,
    )

    CompanionAsyncImage(
        url = creation.avatarUrl,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(StyleDictionary.kkRadiusLg))
            .border(2.dp, StyleDictionary.kkColorBackgroundPrimaryMedium, RoundedCornerShape(StyleDictionary.kkRadiusLg))
            .background(StyleDictionary.kkColorBackgroundSurfaceLight)
            .testTag("companion_preview_image"),
        placeholder = {
            Text(
                "旅伴頭像預覽", // TODO: replace with stringResource
                color = StyleDictionary.kkColorTextMedium,
                fontSize = StyleDictionary.kkFontSize6,
            )
        },
    )

    Spacer(Modifier.height(StyleDictionary.kkSpacing300))

    Text(
        "喜歡這個樣子嗎？", // TODO: replace with stringResource
        fontWeight = FontWeight(StyleDictionary.kkFontWeightBold),
        fontSize = StyleDictionary.kkFontSize5,
        color = StyleDictionary.kkColorTextDarker,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
    )

    Spacer(Modifier.height(StyleDictionary.kkSpacing150))

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(StyleDictionary.kkSpacing100, Alignment.CenterHorizontally),
    ) {
        selectedTags.forEach { tag ->
            SelectablePill(label = tag, isSelected = true, enabled = false, testTag = "preview_tag_$tag", onClick = {})
        }
    }

    Spacer(Modifier.height(StyleDictionary.kkSpacing300))

    // 產完外觀圖才命名（mockup 採用版：命名放在產圖之後）
    DimensionCard(title = "幫它取個名字", badge = "必填", testTag = "companion_dim_name", icon = com.kkday.design.R.drawable.ic_id_card_line) { // TODO: replace with stringResource
        val nameState = remember { mutableStateOf(creation.companionName) }
        LaunchedEffect(creation.companionName) {
            if (nameState.value != creation.companionName) {
                nameState.value = creation.companionName
            }
        }
        Box(modifier = Modifier.testTag("companion_name_input")) {
            KKTextField(
                text = nameState,
                placeholderTextStringType = StringType.Text("例：迷路也不怕"), // TODO: replace with stringResource
                textLengthLimit = CompanionCreationState.NAME_MAX_LENGTH,
                isNextAction = false,
                textListener = viewModel::updateCompanionName,
            )
        }
        Spacer(Modifier.height(StyleDictionary.kkSpacing050))
        Text(
            "${creation.companionName.length} / ${CompanionCreationState.NAME_MAX_LENGTH}",
            fontSize = StyleDictionary.kkFontSize2,
            color = if (creation.isNameValid) StyleDictionary.kkColorTextMedium
            else StyleDictionary.kkColorTextCriticalDark,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End,
        )
    }

    Box(modifier = Modifier.fillMaxWidth().testTag("companion_reselect_btn")) {
        KKButton(
            buttonText = "換個樣子？回上一步重選外觀", // TODO: replace with stringResource
            buttonType = ButtonType.PRIMARY_SUBTLE,
            buttonState = ButtonState.ENABLED,
            buttonSizeType = ButtonSizeType.Md,
            onClick = onReselect,
            leadingIcon = com.kkday.design.R.drawable.ic_reload_line_semibold,
            isFullWidth = true,
        )
    }

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PersonalityStepContent(
    partner: AiPartnerResult,
    creation: CompanionCreationState,
    viewModel: AiCompanionViewModel,
) {
    Text(
        "先決定它的靈魂", // TODO: replace with stringResource
        fontWeight = FontWeight(StyleDictionary.kkFontWeightBold),
        fontSize = StyleDictionary.kkFontSize5,
        color = StyleDictionary.kkColorTextDarker,
    )
    Spacer(Modifier.height(StyleDictionary.kkSpacing050))
    Text(
        "選說話風格與個性，等等再幫它打造外觀", // TODO: replace with stringResource
        fontSize = StyleDictionary.kkFontSize3,
        color = StyleDictionary.kkColorTextMedium,
    )
    Spacer(Modifier.height(StyleDictionary.kkSpacing200))

    DimensionCard(title = "個性", badge = "必選", testTag = "companion_dim_personality", icon = com.kkday.design.R.drawable.ic_sparkles_fill) { // TODO: replace with stringResource
        PersonalityPills(partner.personality, creation.selectedPersonality, viewModel::togglePersonality)
    }

    DimensionCard(title = "說話風格", badge = "必選", testTag = "companion_dim_speech_style", icon = com.kkday.design.R.drawable.ic_sparkles_fill) { // TODO: replace with stringResource
        SpeechStylePills(partner.speechStyle, creation.selectedSpeechStyle, viewModel::selectSpeechStyle)
    }
}

/** 外觀步驟頂部的「個性完成」小結卡：左側打勾圓標 + 已選說話風格・個性摘要。 */
@Composable
private fun PersonalityDoneCard(creation: CompanionCreationState) {
    val summary = listOfNotNull(
        creation.selectedSpeechStyle?.label?.ifBlank { creation.selectedSpeechStyle?.tag },
        creation.selectedPersonality.firstOrNull()?.let { it.label.ifBlank { it.tag } },
    ).joinToString(" · ")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("companion_personality_done_card")
            .clip(RoundedCornerShape(StyleDictionary.kkRadiusLg))
            .border(1.dp, StyleDictionary.kkColorBorderLight, RoundedCornerShape(StyleDictionary.kkRadiusLg))
            .background(StyleDictionary.kkColorWhite)
            .padding(horizontal = StyleDictionary.kkSpacing150, vertical = StyleDictionary.kkSpacing100),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(StyleDictionary.kkColorBackgroundPrimaryLighter),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(com.kkday.design.R.drawable.ic_sparkles_fill),
                contentDescription = null,
                tint = StyleDictionary.kkColorTextPrimaryDark,
                modifier = Modifier.size(StyleDictionary.kkDimensionIconSm),
            )
        }
        Spacer(Modifier.width(StyleDictionary.kkSpacing150))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(com.kkday.design.R.drawable.ic_check_circle_fill),
                    contentDescription = null,
                    tint = StyleDictionary.kkColorTextPrimaryDark,
                    modifier = Modifier.size(StyleDictionary.kkDimensionIcon2xs),
                )
                Spacer(Modifier.width(StyleDictionary.kkSpacing050))
                Text(
                    "個性完成", // TODO: replace with stringResource
                    fontWeight = FontWeight(StyleDictionary.kkFontWeightMediumAndroid),
                    color = StyleDictionary.kkColorTextPrimaryDark,
                    fontSize = StyleDictionary.kkFontSize2,
                )
            }
            if (summary.isNotBlank()) {
                Spacer(Modifier.height(StyleDictionary.kkSpacing025))
                Text(
                    summary,
                    color = StyleDictionary.kkColorTextMedium,
                    fontSize = StyleDictionary.kkFontSize2,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun DimensionCard(
    title: String,
    testTag: String,
    badge: String? = null,
    @DrawableRes icon: Int? = null,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .padding(bottom = StyleDictionary.kkSpacing150)
            .border(1.dp, StyleDictionary.kkColorBorderLight, RoundedCornerShape(StyleDictionary.kkRadiusLg))
            .clip(RoundedCornerShape(StyleDictionary.kkRadiusLg))
            .background(StyleDictionary.kkColorWhite)
            .padding(StyleDictionary.kkSpacing200),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                        modifier = Modifier.size(StyleDictionary.kkDimensionIconSm),
                        tint = StyleDictionary.kkColorTextMedium,
                    )
                    Spacer(Modifier.width(StyleDictionary.kkSpacing075))
                }
                Text(
                    text = title,
                    fontWeight = FontWeight(StyleDictionary.kkFontWeightMediumAndroid),
                    color = StyleDictionary.kkColorTextDarker,
                )
                if (badge != null) {
                    Spacer(Modifier.width(StyleDictionary.kkSpacing075))
                    Text(
                        text = badge,
                        fontSize = StyleDictionary.kkFontSize1,
                        fontWeight = FontWeight(StyleDictionary.kkFontWeightMediumAndroid),
                        color = StyleDictionary.kkColorTextCriticalDark, // TODO: 確認 badge 顏色 token（A2）
                    )
                }
            }
            Spacer(Modifier.height(StyleDictionary.kkSpacing100))
            content()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AppearancePills(
    options: List<CompanionAppearanceOption>,
    selected: CompanionAppearanceOption?,
    onSelect: (CompanionAppearanceOption) -> Unit,
    testTagPrefix: String,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(StyleDictionary.kkSpacing100),
        verticalArrangement = Arrangement.spacedBy(StyleDictionary.kkSpacing100),
    ) {
        options.forEach { option ->
            val isSelected = selected?.tag == option.tag
            SelectablePill(
                label = option.label,
                isSelected = isSelected,
                enabled = true,
                testTag = "${testTagPrefix}_${option.tag}",
                onClick = { onSelect(option) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PersonalityPills(
    options: List<CompanionTraitOption>,
    selected: List<CompanionTraitOption>,
    onToggle: (CompanionTraitOption) -> Unit,
) {
    val selectedTags = selected.map { it.tag }.toSet()
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(StyleDictionary.kkSpacing100),
        verticalArrangement = Arrangement.spacedBy(StyleDictionary.kkSpacing100),
    ) {
        options.forEach { option ->
            val isSelected = option.tag in selectedTags
            SelectablePill(
                label = option.label,
                isSelected = isSelected,
                // 單選取代式：未選中的維持可點，點了直接換
                enabled = true,
                testTag = "personality_${option.tag}",
                onClick = { onToggle(option) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SpeechStylePills(
    options: List<CompanionTraitOption>,
    selected: CompanionTraitOption?,
    onSelect: (CompanionTraitOption) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(StyleDictionary.kkSpacing100),
        verticalArrangement = Arrangement.spacedBy(StyleDictionary.kkSpacing100),
    ) {
        options.forEach { option ->
            val isSelected = selected?.tag == option.tag
            SelectablePill(
                label = option.label,
                isSelected = isSelected,
                enabled = true,
                testTag = "speech_style_${option.tag}",
                onClick = { onSelect(option) },
            )
        }
    }
}

@Composable
private fun SelectablePill(
    label: String,
    isSelected: Boolean,
    enabled: Boolean,
    testTag: String,
    onClick: () -> Unit,
    fontSize: TextUnit = TextUnit.Unspecified,
) {
    val bgColor = when {
        isSelected -> StyleDictionary.kkColorBackgroundPrimaryLighter
        !enabled -> StyleDictionary.kkColorBackgroundSurfaceLight
        else -> StyleDictionary.kkColorWhite
    }
    val textColor = when {
        isSelected -> StyleDictionary.kkColorTextPrimaryDark
        !enabled -> StyleDictionary.kkColorTextMedium
        else -> StyleDictionary.kkColorTextDark
    }
    val borderColor = if (isSelected) StyleDictionary.kkColorBackgroundPrimaryMedium
    else StyleDictionary.kkColorBorderLight

    Text(
        text = label,
        color = textColor,
        fontSize = fontSize,
        modifier = Modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(StyleDictionary.kkRadiusXl))
            .then(if (enabled) Modifier.clickable { onClick() } else Modifier)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(StyleDictionary.kkRadiusXl),
            )
            .background(bgColor, RoundedCornerShape(StyleDictionary.kkRadiusXl))
            .padding(
                horizontal = StyleDictionary.kkSpacing200,
                vertical = StyleDictionary.kkSpacing100,
            ),
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun CompanionBornScreen(
    creation: CompanionCreationState,
    introduction: IntroductionState,
    onGoHome: () -> Unit,
    onRecreate: () -> Unit,
) {
    val appearanceTags = listOfNotNull(
        creation.selectedGender?.label,
        creation.selectedHairStyle?.label,
        creation.selectedHairColor?.label,
        creation.selectedOutfit?.label,
    )
    val traitTags = listOfNotNull(
        creation.selectedSpeechStyle?.label,
    ) + creation.selectedPersonality.map { it.label }

    val view = LocalView.current
    SideEffect {
        val window = (view.context as Activity).window
        window.statusBarColor = Color.Transparent.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StyleDictionary.kkColorWhite)
            .semantics { testTagsAsResourceId = true }
            .testTag("companion_born_screen"),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .background(StyleDictionary.kkColorBackgroundPrimaryLighter),
                contentAlignment = Alignment.Center,
            ) {
                CompanionAsyncImage(
                    url = creation.avatarUrl,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("companion_born_avatar"),
                    placeholder = {
                        Text(
                            "旅伴頭像", // TODO: replace with stringResource
                            color = StyleDictionary.kkColorTextPrimaryDark,
                            fontSize = StyleDictionary.kkFontSize6,
                        )
                    },
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(StyleDictionary.kkSpacing200)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(com.kkday.design.R.drawable.ic_share_android_line),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-24).dp)
                    .clip(RoundedCornerShape(topStart = StyleDictionary.kkRadiusXl, topEnd = StyleDictionary.kkRadiusXl))
                    .background(StyleDictionary.kkColorWhite)
                    .padding(horizontal = StyleDictionary.kkSpacing300),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                KKModalBottomSheetDragHandle()
                Spacer(Modifier.height(StyleDictionary.kkSpacing150))
                Text(
                    "你的專屬旅伴誕生了", // TODO: replace with stringResource
                    color = StyleDictionary.kkColorTextMedium,
                    fontSize = StyleDictionary.kkFontSize3,
                )
                Spacer(Modifier.height(StyleDictionary.kkSpacing100))
                Text(
                    creation.companionName.ifBlank { "旅伴" }, // TODO: replace with stringResource fallback
                    fontWeight = FontWeight(StyleDictionary.kkFontWeightBold),
                    fontSize = StyleDictionary.kkFontSize8,
                    color = StyleDictionary.kkColorTextDarker,
                    modifier = Modifier.testTag("companion_born_name"),
                )
                Spacer(Modifier.height(StyleDictionary.kkSpacing150))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(StyleDictionary.kkSpacing100, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(StyleDictionary.kkSpacing100),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    traitTags.forEach { tag ->
                        SelectablePill(label = tag, isSelected = true, enabled = false, testTag = "born_tag_$tag", onClick = {})
                    }
                    if (appearanceTags.isNotEmpty()) {
                        SelectablePill(
                            label = appearanceTags.joinToString(" · "),
                            isSelected = true,
                            enabled = false,
                            testTag = "born_tag_appearance",
                            onClick = {},
                        )
                    }
                }
                Spacer(Modifier.height(StyleDictionary.kkSpacing200))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(StyleDictionary.kkRadiusLg))
                        .background(StyleDictionary.kkColorBackgroundSurfaceLight)
                        .padding(StyleDictionary.kkSpacing200),
                ) {
                    Column {
                        Text(
                            "\u201C",
                            color = StyleDictionary.kkColorTextPrimaryDark,
                            fontSize = StyleDictionary.kkFontSize9,
                            lineHeight = StyleDictionary.kkFontSize9,
                            fontWeight = FontWeight(StyleDictionary.kkFontWeightBold),
                        )
                        Spacer(Modifier.height(StyleDictionary.kkSpacing050))
                        val companionName = creation.companionName.ifBlank { "旅伴" } // TODO: replace with stringResource fallback
                        val introductionText = when (introduction) {
                            is IntroductionState.Loaded -> introduction.introduction
                            is IntroductionState.Loading -> "${companionName}正在想怎麼介紹自己⋯" // TODO: replace with stringResource
                            // Idle / Error（硬失敗）→ 本地固定文案 fallback，不擋流程
                            else -> "嗨！我是$companionName，一個喜歡隨性探索、享受旅途驚喜的旅伴。我們會一起發現那些地圖上找不到的角落，用你自己的步調感受每一個城市的溫度。準備好了嗎？" // TODO: replace with stringResource
                        }
                        Text(
                            introductionText,
                            color = StyleDictionary.kkColorTextDark,
                            fontSize = StyleDictionary.kkFontSize3,
                            modifier = Modifier.testTag("companion_born_quote"),
                        )
                    }
                }
                Spacer(Modifier.height(StyleDictionary.kkSpacing300))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StyleDictionary.kkColorWhite)
                .navigationBarsPadding()
                .padding(horizontal = StyleDictionary.kkSpacing300, vertical = StyleDictionary.kkSpacing200),
            horizontalArrangement = Arrangement.spacedBy(StyleDictionary.kkSpacing150),
        ) {
            Box(Modifier.weight(3f).testTag("companion_born_home_btn")) {
                KKButton(
                    buttonText = "進入旅伴主頁", // TODO: replace with stringResource
                    buttonType = ButtonType.PRIMARY,
                    buttonState = ButtonState.ENABLED,
                    buttonSizeType = ButtonSizeType.Md,
                    onClick = onGoHome,
                    isFullWidth = true,
                )
            }
            Box(Modifier.weight(2f).testTag("companion_born_recreate_btn")) {
                KKButton(
                    buttonText = "重新打造", // TODO: replace with stringResource
                    buttonType = ButtonType.PRIMARY_SUBTLE,
                    buttonState = ButtonState.ENABLED,
                    buttonSizeType = ButtonSizeType.Md,
                    onClick = onRecreate,
                    isFullWidth = true,
                    leadingIcon = com.kkday.design.R.drawable.ic_reload_line_semibold,
                )
            }
        }
    }
}

/** 旅伴主頁意圖卡：圓角方塊 icon + 標題/副標 + chevron；comingSoon 時右側鎖頭、標題旁「即將推出」chip、不可點擊。 */
@Composable
private fun HomeIntentCard(
    @DrawableRes iconRes: Int,
    iconTint: Color,
    iconBackground: Color,
    title: String,
    subtitle: String,
    testTag: String,
    comingSoon: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .clip(RoundedCornerShape(StyleDictionary.kkRadiusXl))
            .border(1.dp, StyleDictionary.kkColorBorderLight, RoundedCornerShape(StyleDictionary.kkRadiusXl))
            .background(StyleDictionary.kkColorWhite)
            .then(if (onClick != null && !comingSoon) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(StyleDictionary.kkSpacing200),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(StyleDictionary.kkRadiusLg))
                    .background(iconBackground)
                    .alpha(if (comingSoon) 0.55f else 1f),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(StyleDictionary.kkDimensionIconMd),
                )
            }
            Spacer(Modifier.width(StyleDictionary.kkSpacing150))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .alpha(if (comingSoon) 0.72f else 1f),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        title,
                        fontWeight = FontWeight(StyleDictionary.kkFontWeightMediumAndroid),
                        color = StyleDictionary.kkColorTextDarker,
                        fontSize = StyleDictionary.kkFontSize3,
                    )
                    if (comingSoon) {
                        Spacer(Modifier.width(StyleDictionary.kkSpacing075))
                        Text(
                            "即將推出", // TODO: replace with stringResource
                            color = StyleDictionary.kkColorWhite,
                            fontSize = StyleDictionary.kkFontSize1,
                            fontWeight = FontWeight(StyleDictionary.kkFontWeightMediumAndroid),
                            modifier = Modifier
                                .clip(RoundedCornerShape(StyleDictionary.kkRadiusSm))
                                .background(StyleDictionary.kkColorBackgroundCriticalMedium)
                                .padding(horizontal = StyleDictionary.kkSpacing075, vertical = StyleDictionary.kkSpacing025),
                        )
                    }
                }
                Spacer(Modifier.height(StyleDictionary.kkSpacing050))
                Text(
                    subtitle,
                    color = StyleDictionary.kkColorTextMedium,
                    fontSize = StyleDictionary.kkFontSize2,
                )
            }
            Icon(
                painter = painterResource(
                    if (comingSoon) com.kkday.design.R.drawable.ic_lock_line
                    else com.kkday.design.R.drawable.ic_arrow_right_line,
                ),
                contentDescription = null,
                tint = StyleDictionary.kkColorTextMedium,
                modifier = Modifier.size(StyleDictionary.kkDimensionIconSm),
            )
        }
    }
}

/** 旅伴主頁「我的旅程」小卡：城市縮圖占位＋行程名，點擊直達成果頁（純本地資料，後端不儲存）。 */
@Composable
private fun SavedTripCard(trip: SavedTripRecord, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(168.dp)
            .testTag("companion_home_saved_trip_card")
            .clip(RoundedCornerShape(StyleDictionary.kkRadiusLg))
            .border(1.dp, StyleDictionary.kkColorBorderLight, RoundedCornerShape(StyleDictionary.kkRadiusLg))
            .background(StyleDictionary.kkColorWhite)
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp)
                // 城市縮圖占位：場景圖 API 尚未提供
                .background(
                    Brush.verticalGradient(
                        listOf(
                            StyleDictionary.kkColorBackgroundPrimaryMedium,
                            StyleDictionary.kkColorBackgroundPrimaryDarker,
                        ),
                    ),
                ),
        ) {
            Text(
                "共 ${trip.totalDays} 天", // TODO: replace with stringResource
                fontSize = StyleDictionary.kkFontSize1,
                fontWeight = FontWeight(StyleDictionary.kkFontWeightBold),
                color = StyleDictionary.kkColorWhite,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(StyleDictionary.kkSpacing075)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(horizontal = StyleDictionary.kkSpacing075, vertical = StyleDictionary.kkSpacing025),
            )
        }
        Column(modifier = Modifier.padding(StyleDictionary.kkSpacing150)) {
            Text(
                trip.title,
                fontWeight = FontWeight(StyleDictionary.kkFontWeightBold),
                fontSize = StyleDictionary.kkFontSize2,
                color = StyleDictionary.kkColorTextDarker,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(StyleDictionary.kkSpacing025))
            Text(
                "${trip.city}・一起排的", // TODO: replace with stringResource
                fontSize = StyleDictionary.kkFontSize1,
                color = StyleDictionary.kkColorTextMedium,
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun CompanionHomeScreen(
    creation: CompanionCreationState,
    introduction: String,
    onTravelDna: () -> Unit,
    onHistory: () -> Unit,
    onQuizGallery: () -> Unit,
    onBack: () -> Unit,
    onRecreate: () -> Unit,
    onImportItinerary: () -> Unit = {},
    onPlanTrip: () -> Unit = {},
    onPlanTripWithOrders: () -> Unit = {},
    onPlanTripWithWish: () -> Unit = {},
    onPlanTripWithHistory: () -> Unit = {},
    onTripList: () -> Unit = {},
    savedTrips: List<SavedTripRecord> = emptyList(),
    onOpenSavedTrip: (SavedTripRecord) -> Unit = {},
) {
    val companionName = creation.companionName.ifBlank { "旅伴" } // TODO: replace with stringResource fallback
    var showProfileSheet by remember { mutableStateOf(false) }
    val personaTags = creation.selectedPersonality.map { it.label.ifBlank { it.tag } } +
        listOfNotNull(creation.selectedSpeechStyle?.label?.ifBlank { creation.selectedSpeechStyle?.tag }?.takeIf { it.isNotBlank() })

    val view = LocalView.current
    SideEffect {
        val window = (view.context as Activity).window
        window.statusBarColor = Color.Transparent.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .semantics { testTagsAsResourceId = true }
            .testTag("companion_home_screen")
            .background(StyleDictionary.kkColorBackgroundSurfaceLight)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding(),
    ) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = StyleDictionary.kkSpacing200, vertical = StyleDictionary.kkSpacing150),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("companion_back_btn"),
            ) {
                Icon(
                    painter = painterResource(com.kkday.design.R.drawable.ic_arrow_left_line),
                    contentDescription = null,
                    tint = StyleDictionary.kkColorTextDarker,
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                "我的 AI 旅伴", // TODO: replace with stringResource
                fontWeight = FontWeight(StyleDictionary.kkFontWeightMediumAndroid),
                fontSize = StyleDictionary.kkFontSize4,
                color = StyleDictionary.kkColorTextDarker,
            )
            Spacer(Modifier.weight(1f))
            // overflow menu — DS 無垂直三點 icon，暫用水平版
            var showMenu by remember { mutableStateOf(false) }
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.testTag("companion_home_menu_btn"),
                ) {
                    Icon(
                        painter = painterResource(com.kkday.design.R.drawable.ic_dots_three_line_semibold),
                        contentDescription = null,
                        tint = StyleDictionary.kkColorTextDarker,
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                ) {
                    // TODO: 重新命名旅伴（設計稿 A13 有此項，rename 流程尚未實作）
                    DropdownMenuItem(
                        text = {
                            Text(
                                "重新打造旅伴", // TODO: replace with stringResource
                                fontSize = StyleDictionary.kkFontSize3,
                                color = StyleDictionary.kkColorTextPrimaryDark,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(com.kkday.design.R.drawable.ic_reload_line_semibold),
                                contentDescription = null,
                                tint = StyleDictionary.kkColorTextPrimaryDark,
                                modifier = Modifier.size(StyleDictionary.kkDimensionIconSm),
                            )
                        },
                        onClick = {
                            showMenu = false
                            onRecreate()
                        },
                        modifier = Modifier.testTag("companion_home_recreate_btn"),
                    )
                }
            }
        }

        // 頭像 + 名稱 + 個性/說話風格 Tag（點頭像看大圖頭像 + 完整自我介紹）
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CompanionAsyncImage(
                url = creation.avatarUrl,
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .border(3.dp, StyleDictionary.kkColorWhite, CircleShape)
                    .background(StyleDictionary.kkColorBackgroundPrimaryLighter)
                    .clickable { showProfileSheet = true }
                    .testTag("companion_home_avatar"),
                placeholder = {
                    Text("?", color = StyleDictionary.kkColorTextPrimaryDark, fontSize = StyleDictionary.kkFontSize9)
                },
            )
            Spacer(Modifier.height(StyleDictionary.kkSpacing150))
            Text(
                companionName,
                fontWeight = FontWeight(StyleDictionary.kkFontWeightBold),
                fontSize = StyleDictionary.kkFontSize5,
                color = StyleDictionary.kkColorTextDarker,
                modifier = Modifier.testTag("companion_home_name"),
            )
            if (personaTags.isNotEmpty()) {
                Spacer(Modifier.height(StyleDictionary.kkSpacing100))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(StyleDictionary.kkSpacing100, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(StyleDictionary.kkSpacing100),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = StyleDictionary.kkSpacing300)
                        .testTag("companion_home_persona_tags"),
                ) {
                    personaTags.forEach { tag ->
                        SelectablePill(
                            label = tag,
                            isSelected = true,
                            enabled = false,
                            testTag = "companion_home_persona_tag_$tag",
                            onClick = {},
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(StyleDictionary.kkSpacing300))

        Column(modifier = Modifier.padding(horizontal = StyleDictionary.kkSpacing300)) {
            // 理解度卡片（demo 暫時隱藏，先不刪程式碼，之後要恢復顯示把這個 if(false) 拿掉即可）
            if (false) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, StyleDictionary.kkColorBorderLight, RoundedCornerShape(StyleDictionary.kkRadiusLg))
                    .clip(RoundedCornerShape(StyleDictionary.kkRadiusLg))
                    .background(StyleDictionary.kkColorWhite)
                    .padding(StyleDictionary.kkSpacing200),
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "${companionName}對你的理解度", // TODO: replace with stringResource
                                fontWeight = FontWeight(StyleDictionary.kkFontWeightMediumAndroid),
                                color = StyleDictionary.kkColorTextDarker,
                                fontSize = StyleDictionary.kkFontSize3,
                            )
                            Spacer(Modifier.width(StyleDictionary.kkSpacing075))
                            KKTag(
                                text = "即將推出", // TODO: replace with stringResource
                                kkTagColor = KKTagSolidColor.RED,
                            )
                        }
                        Text(
                            "-%", // TODO: replace with API data（B6）
                            color = StyleDictionary.kkColorTextPrimaryDark,
                            fontWeight = FontWeight(StyleDictionary.kkFontWeightMediumAndroid),
                            fontSize = StyleDictionary.kkFontSize3,
                        )
                    }
                    Spacer(Modifier.height(StyleDictionary.kkSpacing100))
                    LinearProgressIndicator(
                        progress = { 0f }, // TODO: replace with API data（B6）
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = StyleDictionary.kkColorBackgroundPrimaryMedium,
                        trackColor = StyleDictionary.kkColorBackgroundSurfaceMedium,
                        strokeCap = StrokeCap.Round,
                    )
                    Spacer(Modifier.height(StyleDictionary.kkSpacing100))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(com.kkday.design.R.drawable.ic_sparkles_fill),
                            contentDescription = null,
                            tint = StyleDictionary.kkColorTextPrimaryDark,
                            modifier = Modifier.size(StyleDictionary.kkDimensionIconXs),
                        )
                        Spacer(Modifier.width(StyleDictionary.kkSpacing050))
                        Text(
                            "玩測驗、日後多聊天，${companionName}更懂你", // TODO: replace with stringResource
                            color = StyleDictionary.kkColorTextMedium,
                            fontSize = StyleDictionary.kkFontSize1,
                        )
                    }
                }
            }
            } // if (false) 理解度卡片

            Spacer(Modifier.height(StyleDictionary.kkSpacing300))

            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = StyleDictionary.kkColorTextDarker)) {
                        append("想跟") // TODO: replace with stringResource
                    }
                    withStyle(SpanStyle(color = StyleDictionary.kkColorTextPrimaryDark)) {
                        append(companionName)
                    }
                    withStyle(SpanStyle(color = StyleDictionary.kkColorTextDarker)) {
                        append("一起做什麼呢？")
                    }
                },
                fontWeight = FontWeight(StyleDictionary.kkFontWeightBold),
                fontSize = StyleDictionary.kkFontSize5,
            )

            // 我的旅程：本地儲存的行程清單（後端不儲存，最新在前、橫向捲動），放標題正下方
            if (savedTrips.isNotEmpty()) {
                Spacer(Modifier.height(StyleDictionary.kkSpacing150))
                Text(
                    "我的旅程", // TODO: replace with stringResource
                    fontWeight = FontWeight(StyleDictionary.kkFontWeightBold),
                    fontSize = StyleDictionary.kkFontSize3,
                    color = StyleDictionary.kkColorTextDarker,
                )
                Spacer(Modifier.height(StyleDictionary.kkSpacing100))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(StyleDictionary.kkSpacing150),
                ) {
                    savedTrips.forEach { trip ->
                        SavedTripCard(trip = trip, onClick = { onOpenSavedTrip(trip) })
                    }
                }
            }

            Spacer(Modifier.height(StyleDictionary.kkSpacing200))

            // 五張意圖卡（依 Phase 1 mockup 旅伴主頁定案順序）
            HomeIntentCard(
                iconRes = com.kkday.design.R.drawable.ic_sparkles_fill,
                iconTint = StyleDictionary.kkColorTextPrimaryDark,
                iconBackground = StyleDictionary.kkColorBackgroundPrimaryLighter,
                title = "找到我的旅行 DNA 及命定旅程", // TODO: replace with stringResource
                subtitle = "玩個測驗，發現你的旅行性格與最適合的目的地", // TODO: replace with stringResource
                testTag = "companion_home_quiz_btn",
                onClick = onTravelDna,
            )

            Spacer(Modifier.height(StyleDictionary.kkSpacing150))

            HomeIntentCard(
                iconRes = com.kkday.design.R.drawable.ic_download_line,
                iconTint = StyleDictionary.kkColorTextPrimaryDark,
                iconBackground = StyleDictionary.kkColorBackgroundPrimaryLighter,
                title = "匯入你的 AI 行程", // TODO: replace with stringResource
                subtitle = "把 ChatGPT／其他 AI 排好的貼給我，或傳截圖，我幫你對上可訂體驗", // TODO: replace with stringResource
                testTag = "companion_home_import_btn",
                onClick = onImportItinerary,
            )

            Spacer(Modifier.height(StyleDictionary.kkSpacing150))

            HomeIntentCard(
                iconRes = com.kkday.design.R.drawable.ic_message_line,
                iconTint = StyleDictionary.kkColorBackgroundHighlightDarker,
                iconBackground = StyleDictionary.kkColorBackgroundHighlightLighter,
                title = "社群旅伴貼文", // TODO: replace with stringResource
                subtitle = "看別人的旅伴與命定城市，逆向找旅行靈感", // TODO: replace with stringResource
                testTag = "companion_home_quiz_gallery_btn",
                onClick = onQuizGallery,
            )

            Spacer(Modifier.height(StyleDictionary.kkSpacing150))

            HomeIntentCard(
                iconRes = com.kkday.design.R.drawable.ic_note_line,
                iconTint = StyleDictionary.kkColorTextPrimaryDark,
                iconBackground = StyleDictionary.kkColorBackgroundPrimaryLighter,
                title = "回顧我的旅行 DNA", // TODO: replace with stringResource
                subtitle = "看過去的測驗結果與海報，隨時再分享", // TODO: replace with stringResource
                testTag = "companion_home_history_btn",
                onClick = onHistory,
            )

            Spacer(Modifier.height(StyleDictionary.kkSpacing150))

            HomeIntentCard(
                iconRes = com.kkday.design.R.drawable.ic_road_map_line,
                iconTint = StyleDictionary.kkColorTextPrimaryDark,
                iconBackground = StyleDictionary.kkColorBackgroundPrimaryLighter,
                title = "一起規劃旅遊行程", // TODO: replace with stringResource
                subtitle = "還沒有想法？沒關係，從頭聊，一步步排出來", // TODO: replace with stringResource
                testTag = "companion_home_plan_btn",
                onClick = onPlanTrip,
            )

            Spacer(Modifier.height(StyleDictionary.kkSpacing150))

            // demo：帶訂單開場，抓即將出發的訂單材料丟給 LLM 判斷目的地選項（見 startPlanFromOrders）
            HomeIntentCard(
                iconRes = com.kkday.design.R.drawable.ic_road_map_line,
                iconTint = StyleDictionary.kkColorTextPrimaryDark,
                iconBackground = StyleDictionary.kkColorBackgroundPrimaryLighter,
                title = "一起規劃旅遊行程(帶訂單)", // TODO: replace with stringResource
                subtitle = "還沒有想法？沒關係，從頭聊，一步步排出來", // TODO: replace with stringResource
                testTag = "companion_home_plan_with_orders_btn",
                onClick = onPlanTripWithOrders,
            )

            Spacer(Modifier.height(StyleDictionary.kkSpacing150))

            // demo：願望清單開場，抓收藏商品材料丟給 LLM 聚合判斷城市選項（見 startPlanFromWish）
            HomeIntentCard(
                iconRes = com.kkday.design.R.drawable.ic_road_map_line,
                iconTint = StyleDictionary.kkColorTextPrimaryDark,
                iconBackground = StyleDictionary.kkColorBackgroundPrimaryLighter,
                title = "一起規劃旅遊行程(從心願清單)", // TODO: replace with stringResource
                subtitle = "從你收藏過的商品，找出你想去的地方", // TODO: replace with stringResource
                testTag = "companion_home_plan_with_wish_btn",
                onClick = onPlanTripWithWish,
            )

            Spacer(Modifier.height(StyleDictionary.kkSpacing150))

            // demo：瀏覽紀錄開場，抓瀏覽/購買商品材料丟給 LLM 聚合判斷城市選項（見 startPlanFromHistory）
            HomeIntentCard(
                iconRes = com.kkday.design.R.drawable.ic_road_map_line,
                iconTint = StyleDictionary.kkColorTextPrimaryDark,
                iconBackground = StyleDictionary.kkColorBackgroundPrimaryLighter,
                title = "一起規劃旅遊行程(從瀏覽記錄)", // TODO: replace with stringResource
                subtitle = "從你最近看過的商品，找出你想去的地方", // TODO: replace with stringResource
                testTag = "companion_home_plan_with_history_btn",
                onClick = onPlanTripWithHistory,
            )
        }

        Spacer(Modifier.weight(1f))

        // 底部聊天 bar（即將推出，demo 暫時隱藏，先不刪程式碼，之後要恢復顯示把這個 if(false) 拿掉即可）
        if (false) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(StyleDictionary.kkSpacing200),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(StyleDictionary.kkRadiusXl))
                    .border(1.dp, StyleDictionary.kkColorBorderLight, RoundedCornerShape(StyleDictionary.kkRadiusXl))
                    .background(StyleDictionary.kkColorWhite)
                    .padding(horizontal = StyleDictionary.kkSpacing200, vertical = StyleDictionary.kkSpacing150),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(com.kkday.design.R.drawable.ic_message_line),
                    contentDescription = null,
                    tint = StyleDictionary.kkColorTextMedium,
                    modifier = Modifier.size(StyleDictionary.kkDimensionIconMd),
                )
                Spacer(Modifier.width(StyleDictionary.kkSpacing100))
                Text(
                    "直接和${companionName}聊聊", // TODO: replace with stringResource
                    color = StyleDictionary.kkColorTextMedium,
                    fontSize = StyleDictionary.kkFontSize3,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "即將推出", // TODO: replace with stringResource
                    color = StyleDictionary.kkColorWhite,
                    fontSize = StyleDictionary.kkFontSize1,
                    fontWeight = FontWeight(StyleDictionary.kkFontWeightMediumAndroid),
                    modifier = Modifier
                        .clip(RoundedCornerShape(StyleDictionary.kkRadiusSm))
                        .background(StyleDictionary.kkColorBackgroundCriticalMedium)
                        .padding(horizontal = StyleDictionary.kkSpacing075, vertical = StyleDictionary.kkSpacing025),
                )
            }
        }
        } // if (false) 底部聊天 bar

        // 首頁底部留白，避免最後一個區塊貼齊畫面底緣
        Spacer(Modifier.height(StyleDictionary.kkSpacing200))
    }

    if (showProfileSheet) {
        CompanionProfileBottomSheet(
            avatarUrl = creation.avatarUrl,
            companionName = companionName,
            personaTags = personaTags,
            introduction = introduction,
            onDismiss = { showProfileSheet = false },
        )
    }
}

/** 頭像點擊後的旅伴檔案：大頭貼 + 個性/說話風格 Tag + 自我介紹（圓角文字框）。 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun CompanionProfileBottomSheet(
    avatarUrl: String,
    companionName: String,
    personaTags: List<String>,
    introduction: String,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        shape = RoundedCornerShape(
            topStart = StyleDictionary.kkRadiusXl,
            topEnd = StyleDictionary.kkRadiusXl,
        ),
        dragHandle = { KKModalBottomSheetDragHandle() },
        scrimColor = Color.Black.copy(alpha = 0.5f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = StyleDictionary.kkSpacing400)
                .testTag("companion_profile_sheet"),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 滿版方形大圖：左右各留 16dp、1:1 正方形圓角，寬度撐滿後高度等比放大（Crop 裁滿）
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current).data(avatarUrl).build()
                ),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = StyleDictionary.kkSpacing200)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(StyleDictionary.kkRadiusLg))
                    .testTag("companion_profile_sheet_avatar"),
            )
            Spacer(Modifier.height(StyleDictionary.kkSpacing150))
            Text(
                companionName,
                fontWeight = FontWeight(StyleDictionary.kkFontWeightBold),
                fontSize = StyleDictionary.kkFontSize5,
                color = StyleDictionary.kkColorTextDarker,
            )
            if (personaTags.isNotEmpty()) {
                Spacer(Modifier.height(StyleDictionary.kkSpacing150))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(StyleDictionary.kkSpacing100, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(StyleDictionary.kkSpacing100),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = StyleDictionary.kkSpacing300),
                ) {
                    personaTags.forEach { tag ->
                        SelectablePill(
                            label = tag,
                            isSelected = true,
                            enabled = false,
                            testTag = "companion_profile_sheet_tag_$tag",
                            onClick = {},
                        )
                    }
                }
            }
            if (introduction.isNotBlank()) {
                Spacer(Modifier.height(StyleDictionary.kkSpacing200))
                Text(
                    introduction,
                    color = StyleDictionary.kkColorTextDarker,
                    fontSize = StyleDictionary.kkFontSize3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = StyleDictionary.kkSpacing300)
                        .clip(RoundedCornerShape(StyleDictionary.kkRadiusLg))
                        .border(1.dp, StyleDictionary.kkColorBorderLight, RoundedCornerShape(StyleDictionary.kkRadiusLg))
                        .background(StyleDictionary.kkColorBackgroundSurfaceLight)
                        .padding(StyleDictionary.kkSpacing200)
                        .testTag("companion_profile_sheet_introduction"),
                )
            }
        }
    }
}

/**
 * 查看社群：無資料庫下「其他人做過的測驗結果」清單，兩欄瀑布流呈現，不重用 [ScreenScaffold]
 * ——其 verticalScroll 會讓 LazyVerticalStaggeredGrid 撞上無限高度約束，故自建不含 verticalScroll 的頂層容器。
 */
@Composable
private fun QuizGalleryScreen(
    state: QuizGalleryState,
    onBack: () -> Unit,
    onItemClick: (QuizGalleryItem) -> Unit,
) {

    val view = LocalView.current
    SideEffect {
        val window = (view.context as Activity).window
        window.statusBarColor = Color.Transparent.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .semantics { testTagsAsResourceId = true }
            .testTag("companion_quiz_gallery_screen")
            .background(StyleDictionary.kkColorBackgroundSurfaceLight),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(StyleDictionary.kkSpacing300),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("companion_quiz_gallery_back_btn")) {
                Icon(
                    painter = painterResource(com.kkday.design.R.drawable.ic_arrow_left_line),
                    contentDescription = null,
                    tint = StyleDictionary.kkColorTextDarker,
                )
            }
            Spacer(Modifier.width(StyleDictionary.kkSpacing100))
            Text("查看社群", style = fontH6) // TODO: replace with stringResource - 查看社群標題
        }

        when (state) {
            is QuizGalleryState.Idle, QuizGalleryState.Loading ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.testTag("companion_quiz_gallery_loading"))
                }

            is QuizGalleryState.Error ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "載入失敗，請稍後再試", // TODO: replace with stringResource
                        color = StyleDictionary.kkColorTextMedium,
                        modifier = Modifier.testTag("companion_quiz_gallery_error"),
                    )
                }

            is QuizGalleryState.Loaded -> {
                val items = state.result.items
                if (items.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "目前還沒有人分享測驗結果，成為第一個吧！", // TODO: replace with stringResource
                            color = StyleDictionary.kkColorTextMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(horizontal = StyleDictionary.kkSpacing300)
                                .testTag("companion_quiz_gallery_empty"),
                        )
                    }
                } else {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("companion_quiz_gallery_grid"),
                        contentPadding = PaddingValues(
                            start = StyleDictionary.kkSpacing300,
                            end = StyleDictionary.kkSpacing300,
                            bottom = StyleDictionary.kkSpacing300 +
                                WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
                        ),
                        horizontalArrangement = Arrangement.spacedBy(StyleDictionary.kkSpacing150),
                        verticalItemSpacing = StyleDictionary.kkSpacing150,
                    ) {
                        items(items) { item ->
                            QuizGalleryCard(item = item, onClick = { onItemClick(item) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizGalleryCard(item: QuizGalleryItem, onClick: () -> Unit) {
    val destination = item.destinationCn.ifBlank { item.destinationEn }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("companion_quiz_gallery_card")
            .clip(RoundedCornerShape(StyleDictionary.kkRadiusLg))
            .border(1.dp, StyleDictionary.kkColorBorderLight, RoundedCornerShape(StyleDictionary.kkRadiusLg))
            .background(StyleDictionary.kkColorWhite)
            .clickable(onClick = onClick),
    ) {
        // share_image_url 為 null 是正常狀態（尚未或未成功產過分享圖），以無圖漸層卡呈現，不當作載入失敗
        if (item.hasPoster) {
            CompanionAsyncImage(
                url = item.shareImageUrl.orEmpty(),
                placeholderAspectRatio = 9f / 16f,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = StyleDictionary.kkRadiusLg, topEnd = StyleDictionary.kkRadiusLg))
                    .testTag("companion_quiz_gallery_card_poster"),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                StyleDictionary.kkColorBackgroundPrimaryLighter,
                                StyleDictionary.kkColorBackgroundPrimaryLight,
                            ),
                        ),
                    )
                    .testTag("companion_quiz_gallery_card_no_poster"),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    item.travelIdentity,
                    fontWeight = FontWeight(StyleDictionary.kkFontWeightBold),
                    color = StyleDictionary.kkColorBackgroundPrimaryDarker,
                    fontSize = StyleDictionary.kkFontSize4,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(StyleDictionary.kkSpacing200),
                )
            }
        }
        Column(modifier = Modifier.padding(StyleDictionary.kkSpacing150)) {
            Text(
                item.travelIdentity,
                fontWeight = FontWeight(StyleDictionary.kkFontWeightMediumAndroid),
                color = StyleDictionary.kkColorTextDarker,
                fontSize = StyleDictionary.kkFontSize3,
            )
            if (destination.isNotBlank()) {
                Spacer(Modifier.height(StyleDictionary.kkSpacing050))
                Text(destination, color = StyleDictionary.kkColorTextMedium, fontSize = StyleDictionary.kkFontSize2)
            }
            if (item.tagline.isNotBlank()) {
                Spacer(Modifier.height(StyleDictionary.kkSpacing050))
                Text(item.tagline, color = StyleDictionary.kkColorTextMedium, fontSize = StyleDictionary.kkFontSize2)
            }
            if (!item.companionName.isNullOrBlank() || !item.partnerAvatarUrl.isNullOrBlank()) {
                Spacer(Modifier.height(StyleDictionary.kkSpacing050))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // partner_avatar_url 可能為 null（舊資料、或當時沒傳），以 "?" 圓形佔位圖 fallback
                    CompanionAsyncImage(
                        url = item.partnerAvatarUrl.orEmpty(),
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(StyleDictionary.kkColorBackgroundPrimaryLighter)
                            .testTag("companion_quiz_gallery_card_avatar"),
                        placeholder = {
                            Text("?", color = StyleDictionary.kkColorTextPrimaryDark, fontSize = StyleDictionary.kkFontSize1)
                        },
                    )
                    if (!item.companionName.isNullOrBlank()) {
                        Spacer(Modifier.width(StyleDictionary.kkSpacing050))
                        Text(
                            "by ${item.companionName}",
                            color = StyleDictionary.kkColorTextMedium,
                            fontSize = StyleDictionary.kkFontSize1,
                        )
                    }
                }
            }
        }
    }
}

/**
 * 查看社群項目詳情：獨立頁面（原為 bottomsheet），沈浸式滿版——
 * 海報比照 [ResultScreen] 的做法：滿版寬度、延伸至狀態列下方，不裁角、不留左右邊距。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuizGalleryDetailScreen(item: QuizGalleryItem, onBack: () -> Unit) {
    val destination = item.destinationCn.ifBlank { item.destinationEn }

    // 海報色調不定，就緒時固定白色圖示；無海報時背景為純白，維持深色圖示
    val view = LocalView.current
    SideEffect {
        val window = (view.context as Activity).window
        window.statusBarColor = Color.Transparent.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !item.hasPoster
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { testTagsAsResourceId = true }
            .testTag("companion_quiz_gallery_detail_screen")
            .background(StyleDictionary.kkColorWhite),
    ) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            // share_image_url 為 null 時略過圖片區塊，只呈現文字內容
            if (item.hasPoster) {
                CompanionAsyncImage(
                    url = item.shareImageUrl.orEmpty(),
                    contentScale = ContentScale.FillWidth,
                    placeholderAspectRatio = 9f / 16f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("companion_quiz_gallery_detail_poster"),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = StyleDictionary.kkSpacing300)
                    .padding(top = StyleDictionary.kkSpacing300, bottom = StyleDictionary.kkSpacing400)
                    .navigationBarsPadding()
                    .then(if (item.hasPoster) Modifier else Modifier.statusBarsPadding()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // partner_avatar_url 可能為 null（舊資料、或當時沒傳），以 "?" 圓形佔位圖 fallback
                CompanionAsyncImage(
                    url = item.partnerAvatarUrl.orEmpty(),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(StyleDictionary.kkColorBackgroundPrimaryLighter)
                        .testTag("companion_quiz_gallery_detail_avatar"),
                    placeholder = {
                        Text("?", color = StyleDictionary.kkColorTextPrimaryDark, fontSize = StyleDictionary.kkFontSize4)
                    },
                )
                Spacer(Modifier.height(StyleDictionary.kkSpacing100))
                if (!item.companionName.isNullOrBlank()) {
                    Text(
                        item.companionName.orEmpty(),
                        fontWeight = FontWeight(StyleDictionary.kkFontWeightBold),
                        fontSize = StyleDictionary.kkFontSize5,
                        color = StyleDictionary.kkColorTextDarker,
                    )
                    Spacer(Modifier.height(StyleDictionary.kkSpacing100))
                }
                Text(
                    item.travelIdentity,
                    fontWeight = FontWeight(StyleDictionary.kkFontWeightMediumAndroid),
                    fontSize = StyleDictionary.kkFontSize4,
                    color = StyleDictionary.kkColorTextPrimaryDark,
                )
                if (destination.isNotBlank()) {
                    Spacer(Modifier.height(StyleDictionary.kkSpacing050))
                    Text(destination, color = StyleDictionary.kkColorTextMedium, fontSize = StyleDictionary.kkFontSize3)
                }
                if (item.tagline.isNotBlank()) {
                    Spacer(Modifier.height(StyleDictionary.kkSpacing100))
                    Text(item.tagline, color = StyleDictionary.kkColorTextDarker, fontSize = StyleDictionary.kkFontSize3)
                }
                if (item.highlightTags.isNotEmpty()) {
                    Spacer(Modifier.height(StyleDictionary.kkSpacing150))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(StyleDictionary.kkSpacing100, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(StyleDictionary.kkSpacing100),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        item.highlightTags.forEach { tag ->
                            SelectablePill(
                                label = tag,
                                isSelected = true,
                                enabled = false,
                                testTag = "companion_quiz_gallery_detail_tag_$tag",
                                onClick = {},
                            )
                        }
                    }
                }
                if (item.companionQuote.isNotBlank()) {
                    Spacer(Modifier.height(StyleDictionary.kkSpacing200))
                    Text(
                        item.companionQuote,
                        color = StyleDictionary.kkColorTextDarker,
                        fontSize = StyleDictionary.kkFontSize3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(StyleDictionary.kkRadiusLg))
                            .border(1.dp, StyleDictionary.kkColorBorderLight, RoundedCornerShape(StyleDictionary.kkRadiusLg))
                            .background(StyleDictionary.kkColorBackgroundSurfaceLight)
                            .padding(StyleDictionary.kkSpacing200)
                            .testTag("companion_quiz_gallery_detail_quote"),
                    )
                }
            }
        }

        // 返回按鈕：浮在海報上，比照 CompanionBornScreen 分享 icon 的半透明圓形寫法，不受海報底色影響可讀性
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(StyleDictionary.kkSpacing200)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(onClick = onBack)
                .testTag("companion_quiz_gallery_detail_back_btn"),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(com.kkday.design.R.drawable.ic_arrow_left_line),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun QuizScreen(
    viewModel: AiCompanionViewModel,
    creation: CompanionCreationState,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
) {
    val quiz by viewModel.quizState.collectAsStateWithLifecycle()
    val quizAnswers by viewModel.quizAnswers.collectAsStateWithLifecycle()
    val companionName = creation.companionName.ifBlank { "旅伴" } // TODO: replace with stringResource fallback

    val view = LocalView.current
    SideEffect {
        val window = (view.context as Activity).window
        window.statusBarColor = Color.Transparent.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .semantics { testTagsAsResourceId = true }
            .testTag("companion_quiz_screen")
            .background(StyleDictionary.kkColorBackgroundSurfaceLight),
    ) {
        when (val q = quiz) {
            is QuizState.Loading, QuizState.Idle ->
                CompanionLoadingContent(
                    avatarUrl = creation.avatarUrl,
                    companionName = companionName,
                    title = "正在準備你的旅行問卷中", // TODO: replace with stringResource
                    subtitle = "挑選題目 ▸ 客製化語氣 ▸ 準備選項", // TODO: replace with stringResource
                    testTag = "companion_quiz_loading",
                )
            is QuizState.Error ->
                Box(Modifier.fillMaxSize().padding(StyleDictionary.kkSpacing300), contentAlignment = Alignment.Center) {
                    Text(
                        "題庫載入失敗，請稍後再試", // TODO: replace with stringResource
                        modifier = Modifier.testTag("companion_quiz_error"),
                    )
                }
            is QuizState.Loaded -> {
                val questions = q.quiz.questions
                var currentIndex by remember { mutableIntStateOf(0) }
                val question = questions.getOrNull(currentIndex)
                val isLast = currentIndex >= questions.lastIndex

                if (question != null) {
                    QuizQuestionContent(
                        question = question,
                        questionIndex = currentIndex,
                        totalCount = questions.size,
                        // 作答紀錄存於 ViewModel（question.id → option.id），回上一頁時可直接覆寫
                        selectedOptionId = quizAnswers[question.id],
                        onSelectOption = { optionId -> viewModel.answerQuestion(question.id, optionId) },
                        onNext = {
                            if (isLast) onSubmit() else currentIndex++
                        },
                        onBack = {
                            if (currentIndex > 0) currentIndex-- else onBack()
                        },
                        isLast = isLast,
                        avatarUrl = creation.avatarUrl,
                    )
                }
            }
        }
    }
}

/**
 * 陪考小旅（mockup A-4）：右下角輕晃的旅伴頭像＋定時輪播的鼓勵泡泡，不攔截選項點擊。
 * 泡泡顯示約 6 秒、間隔 1 秒換下一句，循環播放。
 */
@Composable
private fun QuizBuddy(
    avatarUrl: String,
    modifier: Modifier = Modifier,
) {
    val bubbles = listOf(
        "這題沒有標準答案，選你最想去的就好", // TODO: replace with stringResource
        "憑直覺～我在旁邊陪你", // TODO: replace with stringResource
        "偷偷說，我也喜歡第二張", // TODO: replace with stringResource
        "再幾題，我就更懂你了！", // TODO: replace with stringResource
    )
    var bubbleIndex by remember { mutableIntStateOf(0) }
    var bubbleVisible by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        while (true) {
            bubbleVisible = true
            delay(6_000)
            bubbleVisible = false
            delay(1_000)
            bubbleIndex = (bubbleIndex + 1) % bubbles.size
        }
    }

    // 桌寵式輕晃：上下 6dp、左右 ±2 度，來回 2.4 秒
    val bobTransition = rememberInfiniteTransition(label = "quizBuddyBob")
    val bobProgress by bobTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1_200), RepeatMode.Reverse),
        label = "quizBuddyBobProgress",
    )

    Column(
        modifier = modifier.testTag("companion_quiz_buddy"),
        horizontalAlignment = Alignment.End,
    ) {
        AnimatedVisibility(
            visible = bubbleVisible,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 210.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = StyleDictionary.kkRadiusLg,
                            topEnd = StyleDictionary.kkRadiusLg,
                            bottomStart = StyleDictionary.kkRadiusLg,
                            bottomEnd = StyleDictionary.kkRadiusSm,
                        ),
                    )
                    .background(StyleDictionary.kkColorTextDarker)
                    .padding(horizontal = StyleDictionary.kkSpacing150, vertical = StyleDictionary.kkSpacing100),
            ) {
                Text(
                    bubbles[bubbleIndex],
                    color = StyleDictionary.kkColorWhite,
                    fontSize = StyleDictionary.kkFontSize2,
                )
            }
        }
        Spacer(Modifier.height(StyleDictionary.kkSpacing100))
        CompanionAsyncImage(
            url = avatarUrl,
            modifier = Modifier
                .offset(y = (-6 * bobProgress).dp)
                .graphicsLayer { rotationZ = -2f + 4f * bobProgress }
                .size(72.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .border(3.dp, StyleDictionary.kkColorWhite, CircleShape)
                .background(StyleDictionary.kkColorBackgroundPrimaryLighter),
            placeholder = {
                Text("?", color = StyleDictionary.kkColorTextPrimaryDark)
            },
        )
    }
}

@Composable
private fun QuizQuestionContent(
    question: com.kkday.model.companion.QuizQuestion,
    questionIndex: Int,
    totalCount: Int,
    selectedOptionId: String?,
    onSelectOption: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    isLast: Boolean,
    avatarUrl: String = "",
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // 頂部：返回 + 進度條 + 題號
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StyleDictionary.kkColorWhite)
                .statusBarsPadding()
                .padding(horizontal = StyleDictionary.kkSpacing200, vertical = StyleDictionary.kkSpacing150),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("companion_quiz_back_btn"),
            ) {
                Icon(
                    painter = painterResource(com.kkday.design.R.drawable.ic_arrow_left_line),
                    contentDescription = null,
                    tint = StyleDictionary.kkColorTextDarker,
                )
            }
            Spacer(Modifier.width(StyleDictionary.kkSpacing150))
            LinearProgressIndicator(
                progress = { (questionIndex + 1).toFloat() / totalCount },
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = StyleDictionary.kkColorBackgroundPrimaryMedium,
                trackColor = StyleDictionary.kkColorBackgroundSurfaceMedium,
                strokeCap = StrokeCap.Round,
                drawStopIndicator = {},
            )
            Spacer(Modifier.width(StyleDictionary.kkSpacing150))
            Text(
                "${questionIndex + 1}/$totalCount",
                color = StyleDictionary.kkColorTextPrimaryDark,
                fontWeight = FontWeight(StyleDictionary.kkFontWeightMediumAndroid),
                fontSize = StyleDictionary.kkFontSize3,
            )
        }

        // 題目 + 選項（可捲動）
        val scrollState = rememberScrollState()
        val options = question.options
        val showScrollHint by remember {
            derivedStateOf { scrollState.maxValue > 0 && scrollState.value < scrollState.maxValue }
        }
        val remainingOptions = maxOf(0, options.size - 4)

        Box(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = StyleDictionary.kkSpacing300),
            ) {
                Spacer(Modifier.height(StyleDictionary.kkSpacing200))
                Text(
                    question.text,
                    fontWeight = FontWeight(StyleDictionary.kkFontWeightBold),
                    fontSize = StyleDictionary.kkFontSize5,
                    color = StyleDictionary.kkColorTextDarker,
                    modifier = Modifier.testTag("companion_quiz_question_text"),
                )
                Spacer(Modifier.height(StyleDictionary.kkSpacing200))

                // 2 欄圖卡 grid
                val rows = options.chunked(2)
                rows.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(StyleDictionary.kkSpacing150),
                    ) {
                        rowItems.forEach { option ->
                            val isSelected = option.id == selectedOptionId
                            QuizOptionCard(
                                option = option,
                                isSelected = isSelected,
                                onClick = { onSelectOption(option.id) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (rowItems.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                    Spacer(Modifier.height(StyleDictionary.kkSpacing150))
                }
            }

            if (showScrollHint && remainingOptions > 0) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = StyleDictionary.kkSpacing100)
                        .clip(RoundedCornerShape(StyleDictionary.kkRadiusMd))
                        .background(StyleDictionary.kkColorWhite.copy(alpha = 0.9f))
                        .padding(horizontal = StyleDictionary.kkSpacing150, vertical = StyleDictionary.kkSpacing075),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "下面還有 $remainingOptions 個選項", // TODO: replace with stringResource
                        color = StyleDictionary.kkColorTextPrimaryDark,
                        fontSize = StyleDictionary.kkFontSize2,
                        fontWeight = FontWeight(StyleDictionary.kkFontWeightMediumAndroid),
                    )
                    Spacer(Modifier.width(StyleDictionary.kkSpacing050))
                    Icon(
                        painter = painterResource(com.kkday.design.R.drawable.ic_arrow_down_line),
                        contentDescription = null,
                        tint = StyleDictionary.kkColorTextPrimaryDark,
                        modifier = Modifier.size(StyleDictionary.kkDimensionIconXs),
                    )
                }
            }

            if (avatarUrl.isNotBlank()) {
                QuizBuddy(
                    avatarUrl = avatarUrl,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = StyleDictionary.kkSpacing150, bottom = StyleDictionary.kkSpacing150),
                )
            }
        }

        // 底部按鈕
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(StyleDictionary.kkColorWhite)
                .navigationBarsPadding()
                .padding(horizontal = StyleDictionary.kkSpacing300, vertical = StyleDictionary.kkSpacing200),
        ) {
            PrimaryButton(
                text = if (isLast) "提交答案" else "下一題", // TODO: replace with stringResource
                enabled = selectedOptionId != null,
                testTag = if (isLast) "companion_quiz_submit_btn" else "companion_quiz_next_btn",
                onClick = onNext,
            )
        }
    }
}

@Composable
private fun QuizOptionCard(
    option: com.kkday.model.companion.QuizOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (isSelected) StyleDictionary.kkColorBackgroundPrimaryMedium else StyleDictionary.kkColorBorderLight
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Column(
        modifier = modifier
            .testTag("quiz_option_${option.id}")
            .clip(RoundedCornerShape(StyleDictionary.kkRadiusLg))
            .border(borderWidth, borderColor, RoundedCornerShape(StyleDictionary.kkRadiusLg))
            .background(StyleDictionary.kkColorWhite)
            .clickable { onClick() },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f) // 選項圖片本身是直式 4:3（高:寬），對齊 iOS 呈現，容器比例需一致才不會裁切
                .background(StyleDictionary.kkColorBackgroundSurfaceMedium),
        ) {
            CompanionAsyncImage(
                url = option.imageUrl,
                contentDescription = option.text,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = StyleDictionary.kkRadiusLg, topEnd = StyleDictionary.kkRadiusLg)),
            )
            if (isSelected) {
                Icon(
                    painter = painterResource(com.kkday.design.R.drawable.ic_check_circle_fill),
                    contentDescription = null,
                    tint = StyleDictionary.kkColorBackgroundPrimaryMedium,
                    modifier = Modifier
                        .padding(StyleDictionary.kkSpacing075)
                        .size(28.dp),
                )
            }
//            Box(
//                modifier = Modifier
//                    .align(Alignment.TopEnd)
//                    .padding(StyleDictionary.kkSpacing075)
//                    .size(28.dp)
//                    .clip(CircleShape)
//                    .background(Color.Black.copy(alpha = 0.4f)),
//                contentAlignment = Alignment.Center,
//            ) {
//                Icon(
//                    painter = painterResource(com.kkday.design.R.drawable.ic_eye_line),
//                    contentDescription = null,
//                    tint = StyleDictionary.kkColorWhite,
//                    modifier = Modifier.size(StyleDictionary.kkDimensionIconXs),
//                )
//            }
        }
        Text(
            text = option.text,
            color = StyleDictionary.kkColorBlack10,
            fontWeight = FontWeight(StyleDictionary.kkFontWeightBold),
            fontSize = StyleDictionary.kkFontSize2,
            modifier = Modifier.padding(StyleDictionary.kkSpacing150),
        )
    }
}

// ---------- 結果頁（C-1 統一畫面）----------

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ResultScreen(
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
    val shareImageV2 by viewModel.shareImageV2State.collectAsStateWithLifecycle()
    val posterRevealed by viewModel.posterRevealed.collectAsStateWithLifecycle()
    val companionName = creation.companionName.ifBlank { "旅伴" }
    val result = (analysis as? AnalysisState.Success)?.result
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val searchResultRouter: SearchResultRouter = koinInject()
    var showBottomSheet by remember { mutableStateOf(false) }
    // 量測底部浮動「更多動作」按鈕實際高度，讓上方可捲動內容留出等高空白，避免最後一張卡片被蓋住
    val density = LocalDensity.current
    var bottomBarHeight by remember { mutableStateOf(0.dp) }
    // 分享／分享到 IG 限時動態用：截「Hero + 推薦理由以上區塊」這一整塊畫面當下實際顯示的內容，
    // 而非另外離屏合成一張圖，確保分享出去的圖片跟使用者在畫面上看到的完全一致
    val shareableGraphicsLayer = rememberGraphicsLayer()

    val isAnalyzing = analysis is AnalysisState.Analyzing
    // 分析已回來、海報還在等（觸發後等待 30s + 輪詢，總長可達 90s；ready 後還要離屏合成六區海報）：
    // 此時 recommendation/social_post 已就緒，可墊檔。
    val isPosterPending = analysis is AnalysisState.Success && result?.canGenerateShareImage == true &&
        (shareImageV2 is ShareImageV2State.Idle || shareImageV2 is ShareImageV2State.Polling || shareImageV2 is ShareImageV2State.Composing)
    // 海報就緒後不自動切頁：停在產圖等待頁顯示「一起去看看」按鈕，點擊才揭曉（與 iOS 一致）
    val isPosterReady = analysis is AnalysisState.Success && shareImageV2 is ShareImageV2State.Ready

    // 沈浸式滿版：海報就緒時海報色調不定，固定白色圖示；其餘狀態背景為純白，維持深色圖示
    val view = LocalView.current
    SideEffect {
        val window = (view.context as Activity).window
        window.statusBarColor = Color.Transparent.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !(isPosterReady && posterRevealed)
        // 系統預設會在狀態列疊一層對比 scrim（即使已透明），視覺上會像頂部留白，需關閉才能讓海報真正頂到狀態列下方
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { testTagsAsResourceId = true }
            .testTag("companion_result_screen")
            .background(StyleDictionary.kkColorWhite),
    ) {
        // share-image-v2 素材就緒後，離屏合成六區海報（本身不可見），完成後回呼 ViewModel 轉入 Ready；
        // 這張 Export 尺寸（1152px 寬）合成圖同時供分享/下載與「旅行 DNA 回顧」列表縮圖使用。
        // 回顧詳情頁改用個別素材（assets.hero/stamp/tagIcons）即時重組排版，不再另外離屏擷取 Live 版本。
        val composingV2 = shareImageV2 as? ShareImageV2State.Composing
        if (composingV2 != null) {
            val posterGraphicsLayer = rememberGraphicsLayer()
            OffscreenSixZonePoster(assets = composingV2.assets, graphicsLayer = posterGraphicsLayer)
            LaunchedEffect(composingV2.assets) {
                // drawWithContent 的 record{} 在每次繪製時同步執行；等一個 frame 確保至少繪製過一次再擷取
                withFrameNanos { }
                viewModel.onPosterComposed(
                    composingV2.assets,
                    posterGraphicsLayer.toImageBitmap().asAndroidBitmap(),
                )
            }
        }

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
                        "分析失敗，請重試", // TODO: replace with stringResource
                        color = StyleDictionary.kkColorTextDarker,
                        fontWeight = FontWeight(StyleDictionary.kkFontWeightMediumAndroid),
                        modifier = Modifier.testTag("companion_analyzing_error"),
                    )
                    Spacer(Modifier.height(StyleDictionary.kkSpacing300))
                    Box(Modifier.padding(horizontal = StyleDictionary.kkSpacing300)) {
                        PrimaryButton(text = "重試", testTag = "companion_analyzing_retry_btn", onClick = onRetry) // TODO: replace with stringResource
                    }
                }
            }

            analysis is AnalysisState.Success && shareImageV2 is ShareImageV2State.SessionExpired -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .testTag("companion_share_session_expired"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text("分析已過期，請重跑測驗", color = StyleDictionary.kkColorTextDarker) // TODO: stringResource
                    Spacer(Modifier.height(StyleDictionary.kkSpacing300))
                    Box(Modifier.padding(horizontal = StyleDictionary.kkSpacing300)) {
                        PrimaryButton(text = "重跑測驗", testTag = "companion_share_retake_btn", onClick = onRetakeQuiz) // TODO: stringResource
                    }
                }
            }

            analysis is AnalysisState.Success && shareImageV2 is ShareImageV2State.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .testTag("companion_share_error"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text("海報生成失敗，請稍後再試", color = StyleDictionary.kkColorTextDarker) // TODO: stringResource
                    Spacer(Modifier.height(StyleDictionary.kkSpacing300))
                    Box(Modifier.padding(horizontal = StyleDictionary.kkSpacing300)) {
                        PrimaryButton(text = "查看完整測驗結果", testTag = "companion_result_view_detail_btn", onClick = onViewDetail) // TODO: stringResource
                    }
                    Spacer(Modifier.height(StyleDictionary.kkSpacing150))
                    Box(
                        modifier = Modifier
                            .padding(horizontal = StyleDictionary.kkSpacing300)
                            .testTag("companion_share_error_go_home_btn"),
                    ) {
                        KKButton(
                            buttonText = "回到旅伴首頁", // TODO: replace with stringResource
                            buttonType = ButtonType.PRIMARY_SUBTLE,
                            buttonState = ButtonState.ENABLED,
                            buttonSizeType = ButtonSizeType.Lg,
                            isFullWidth = true,
                            onClick = onGoHome,
                        )
                    }
                }
            }

            isPosterPending || (isPosterReady && !posterRevealed) -> {
                // 分析結果已到手：用旅伴口吻先講 reasoning/recommendation/social_post，取代乾等；
                // 海報就緒後底部改顯示「一起去看看」按鈕，點擊才進結果頁
                PosterGeneratingContent(
                    avatarUrl = creation.avatarUrl,
                    companionName = companionName,
                    result = result,
                    isPosterReady = isPosterReady,
                    onViewResult = { viewModel.revealPosterResult() },
                )
            }

            isAnalyzing -> {
                CompanionLoadingContent(
                    avatarUrl = creation.avatarUrl,
                    companionName = companionName,
                    title = "正在解析你的旅行 DNA", // TODO: replace with stringResource
                    subtitle = "分析你的選擇 ▸ 配對目的地 ▸ 描繪場景", // TODO: replace with stringResource
                    testTag = "companion_analyzing_loading",
                )
            }

            else -> {
                // C-1：海報就緒或 fallback（例如 share_image_status=skipped，本輪不產圖）
                val currentShareImageV2 = shareImageV2
                if (currentShareImageV2 is ShareImageV2State.Ready) {
                    // 主路徑：Hero 完整顯示於頂部，內容依序排列在下方（不疊加）；
                    // 底部「探索這趟旅程」按鈕開啟原本的分享/探索/下載/查看詳情/回首頁 BottomSheet
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                    ) {
                        // 分享素材範圍：僅 Hero + 疊加的黑色半透明資訊卡，用 GraphicsLayer 持續錄製當前畫面，
                        // 分享／分享到 IG 限時動態時直接截這個容器當下畫面當圖片，不另外離屏合成
                        Box(
                            modifier = Modifier.drawWithContent {
                                shareableGraphicsLayer.record { this@drawWithContent.drawContent() }
                                drawLayer(shareableGraphicsLayer)
                            },
                        ) {
                            PosterHeroWithBadge(
                                assets = currentShareImageV2.assets,
                                metrics = PosterOverlayMetrics.Live,
                            )
                        }
                        PosterBelowHeroContent(
                            assets = currentShareImageV2.assets,
                            showFooter = false,
                            metrics = PosterOverlayMetrics.Live,
                        )
                        CompanionResultHighlightContent(result, showDestinationAndTagline = false)
                        CompanionResultDetailsContent(
                            result = result,
                            bottomSafeArea = bottomBarHeight,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                bottomBarHeight = with(density) { coordinates.size.height.toDp() }
                            }
                            .background(StyleDictionary.kkColorWhite)
                            .navigationBarsPadding()
                            .padding(horizontal = StyleDictionary.kkSpacing300, vertical = StyleDictionary.kkSpacing200),
                    ) {
                        PrimaryButton(
                            text = "探索這趟旅程", // TODO: replace with stringResource
                            testTag = "companion_result_explore_btn",
                            onClick = { showBottomSheet = true },
                        )
                    }
                } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(StyleDictionary.kkSpacing300),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                result?.travelIdentity.orEmpty(),
                                fontWeight = FontWeight(StyleDictionary.kkFontWeightBold),
                                fontSize = StyleDictionary.kkFontSize6,
                                color = StyleDictionary.kkColorTextPrimaryDark,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(Modifier.height(StyleDictionary.kkSpacing150))
                            Text(
                                "${result?.destinationCn.orEmpty()} · ${result?.destinationCountry.orEmpty()}",
                                color = StyleDictionary.kkColorTextDark,
                                fontSize = StyleDictionary.kkFontSize3,
                            )
                            Spacer(Modifier.height(StyleDictionary.kkSpacing200))
                            Text(
                                result?.companionQuote?.ifBlank { result.recommendation.firstOrNull().orEmpty() }.orEmpty(),
                                color = StyleDictionary.kkColorTextMedium,
                                fontSize = StyleDictionary.kkFontSize3,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }

                // 底部「更多動作」按鈕：白底鋪滿寬度，避免捲動內容從按鈕周圍透出來
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            bottomBarHeight = with(density) { coordinates.size.height.toDp() }
                        }
                        .background(StyleDictionary.kkColorWhite)
                        .navigationBarsPadding()
                        .padding(horizontal = StyleDictionary.kkSpacing300, vertical = StyleDictionary.kkSpacing200),
                ) {
                    PrimaryButton(
                        text = "更多動作", // TODO: replace with stringResource
                        testTag = "companion_result_more_actions_btn",
                        onClick = { showBottomSheet = true },
                    )
                }
                }
            }
        }

        // BottomSheet
        if (showBottomSheet) {
            val posterBitmap = (shareImageV2 as? ShareImageV2State.Ready)?.posterBitmap
            ResultActionsBottomSheet(
                result = result,
                isImageReady = posterBitmap != null,
                onDismiss = { showBottomSheet = false },
                onShareDna = {
                    showBottomSheet = false
                    val caption = result?.socialPost.orEmpty()
                    scope.launch {
                        // 分享素材：截「Hero + 推薦理由以上」容器當下畫面的截圖，而非另外離屏合成的圖
                        val shared = posterBitmap != null &&
                            CompanionShareActions.sharePosterBitmap(
                                context,
                                shareableGraphicsLayer.toImageBitmap().asAndroidBitmap(),
                                caption,
                            )
                        if (!shared) CompanionShareActions.shareText(context, caption)
                    }
                },
                onShareToInstagramStories = {
                    showBottomSheet = false
                    val caption = result?.socialPost.orEmpty()
                    // IG 限時動態不支援帶入文字文案，故點擊分享的同時把分享文案複製到剪貼簿，方便使用者貼上
                    if (caption.isNotBlank()) context.copySocialPostToClipboard(caption)
                    scope.launch {
                        val shared = posterBitmap != null &&
                            CompanionShareActions.shareBitmapToInstagramStories(
                                context,
                                shareableGraphicsLayer.toImageBitmap().asAndroidBitmap(),
                                caption,
                            )
                        if (!shared) CompanionShareActions.shareText(context, caption)
                    }
                },
                onExploreDestination = {
                    showBottomSheet = false
                    val keyword = result?.destinationCn.orEmpty().ifBlank { result?.destinationEn.orEmpty() }
                    searchResultRouter.launchSearchResult(context, keyword)
                },
                onDownloadImage = {
                    showBottomSheet = false
                    if (posterBitmap != null) {
                        scope.launch { CompanionShareActions.downloadBitmap(context, posterBitmap) }
                    }
                },
                onViewDetail = {
                    showBottomSheet = false
                    onViewDetail()
                },
                onGoHome = {
                    showBottomSheet = false
                    onGoHome()
                },
                // 定案版：無「查看完整測驗結果」「探索行程」列
                showViewDetail = false,
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
 * 統一的旅伴讀取畫面：頭像 + 名字 + 主標題 + 副標題 + 三點漸變輪播。
 * 答題前（準備問卷）與答題後（分析結果）共用同一套視覺。
 */
@Composable
private fun CompanionLoadingContent(
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
                .background(StyleDictionary.kkColorBackgroundPrimaryLighter),
            placeholder = {
                Text("?", color = StyleDictionary.kkColorTextPrimaryDark, fontSize = StyleDictionary.kkFontSize9)
            },
        )
        Spacer(Modifier.height(StyleDictionary.kkSpacing200))
        Text(
            companionName,
            color = StyleDictionary.kkColorTextPrimaryDark,
            fontWeight = FontWeight(StyleDictionary.kkFontWeightBold),
            fontSize = StyleDictionary.kkFontSize5,
        )
        Spacer(Modifier.height(StyleDictionary.kkSpacing200))
        Text(
            title,
            fontWeight = FontWeight(StyleDictionary.kkFontWeightBold),
            fontSize = StyleDictionary.kkFontSize4,
            color = StyleDictionary.kkColorTextDarker,
        )
        Spacer(Modifier.height(StyleDictionary.kkSpacing100))
        Text(
            subtitle,
            color = StyleDictionary.kkColorTextMedium,
            fontSize = StyleDictionary.kkFontSize3,
        )
        Spacer(Modifier.height(StyleDictionary.kkSpacing300))
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
                    .background(StyleDictionary.kkColorBackgroundPrimaryMedium.copy(alpha = alpha.value)),
            )
        }
    }
}

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
    result: com.kkday.model.companion.QuizCompletionResult?,
    isPosterReady: Boolean,
    onViewResult: () -> Unit,
) {
    val reasoning = result?.reasoning.orEmpty().filter { it.isNotBlank() }
    val recommendations = result?.recommendation.orEmpty().filter { it.isNotBlank() }
    val socialPost = result?.socialPost.orEmpty()
    val highlightTags = result?.highlightTags.orEmpty()
    val context = LocalContext.current
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
        Spacer(Modifier.height(StyleDictionary.kkSpacing300))
        CompanionAsyncImage(
            url = avatarUrl,
            blurInOnLoad = true,
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(StyleDictionary.kkColorBackgroundPrimaryLighter),
            placeholder = {
                Text("?", color = StyleDictionary.kkColorTextPrimaryDark, fontSize = StyleDictionary.kkFontSize9)
            },
        )
        Spacer(Modifier.height(StyleDictionary.kkSpacing150))
        Text(
            companionName,
            color = StyleDictionary.kkColorTextPrimaryDark,
            fontWeight = FontWeight(StyleDictionary.kkFontWeightBold),
            fontSize = StyleDictionary.kkFontSize5,
        )
        Spacer(Modifier.height(StyleDictionary.kkSpacing100))
        Text(
            "正在解析你的旅行 DNA", // TODO: replace with stringResource
            fontWeight = FontWeight(StyleDictionary.kkFontWeightBold),
            fontSize = StyleDictionary.kkFontSize4,
            color = StyleDictionary.kkColorTextDarker,
        )
        Spacer(Modifier.height(StyleDictionary.kkSpacing200))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = StyleDictionary.kkSpacing300),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (reasoning.isNotEmpty()) {
                // 思考過程逐句對話框呈現；最後一句（揭曉目的地）以主色外框與文字強調
                SequentialTypewriterItems(
                    items = reasoning,
                    itemSpacing = StyleDictionary.kkSpacing150,
                    onAllFinished = { reasoningDone = true },
                    modifier = Modifier.testTag("companion_share_poster_generating_reasoning"),
                ) { text, onFinished ->
                    val isHighlight = text == reasoning.last()
                    ReasoningBubble(isHighlight = isHighlight) {
                        TypewriterText(
                            text = text,
                            color = if (isHighlight) StyleDictionary.kkColorTextPrimaryDark else StyleDictionary.kkColorTextDarker,
                            fontSize = StyleDictionary.kkFontSize3,
                            onFinished = onFinished,
                        )
                    }
                }
                Spacer(Modifier.height(StyleDictionary.kkSpacing300))
            }

            // 分析結果摘要（travel_identity / destination / tagline / highlight_tags）：思考過程播完才亮出
            if (reasoningDone) {
                if (result?.travelIdentity.orEmpty().isNotBlank()) {
                    Text(
                        result?.travelIdentity.orEmpty(),
                        fontWeight = FontWeight(StyleDictionary.kkFontWeightBold),
                        fontSize = StyleDictionary.kkFontSize6,
                        color = StyleDictionary.kkColorTextPrimaryDark,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("companion_share_poster_generating_travel_identity"),
                    )
                    Spacer(Modifier.height(StyleDictionary.kkSpacing100))
                }
                if (result?.destinationCn.orEmpty().isNotBlank()) {
                    Text(
                        "${result?.destinationCn.orEmpty()} · ${result?.destinationCountry.orEmpty()}",
                        color = StyleDictionary.kkColorTextDark,
                        fontSize = StyleDictionary.kkFontSize3,
                        modifier = Modifier.testTag("companion_share_poster_generating_destination"),
                    )
                    Spacer(Modifier.height(StyleDictionary.kkSpacing100))
                }
                if (result?.tagline.orEmpty().isNotBlank()) {
                    Text(
                        result?.tagline.orEmpty(),
                        color = StyleDictionary.kkColorTextMedium,
                        fontSize = StyleDictionary.kkFontSize3,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("companion_share_poster_generating_tagline"),
                    )
                    Spacer(Modifier.height(StyleDictionary.kkSpacing150))
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
                        horizontalArrangement = Arrangement.spacedBy(StyleDictionary.kkSpacing100, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(StyleDictionary.kkSpacing100),
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
                    Spacer(Modifier.height(StyleDictionary.kkSpacing200))
                }
            }

            if (recommendations.isNotEmpty() && reasoningDone) {
                TitledInfoCard(
                    title = "推薦理由", // TODO: replace with stringResource
                    testTag = "companion_share_poster_generating_recommendation",
                ) {
                    SequentialTypewriterItems(
                        items = recommendations,
                        itemSpacing = StyleDictionary.kkSpacing100,
                        onAllFinished = { recommendationDone = true },
                    ) { text, onFinished ->
                        TypewriterText(
                            text = text,
                            color = StyleDictionary.kkColorTextDarker,
                            fontSize = StyleDictionary.kkFontSize3,
                            onFinished = onFinished,
                        )
                    }
                }
                Spacer(Modifier.height(StyleDictionary.kkSpacing150))
            }
            if (socialPost.isNotBlank() && reasoningDone && recommendationDone) {
                TitledInfoCard(
                    title = "分享文案", // TODO: replace with stringResource
                    testTag = "companion_share_poster_generating_social_post",
                    onCopyClick = { context.copySocialPostToClipboard(socialPost) },
                ) {
                    TypewriterText(
                        text = socialPost,
                        color = StyleDictionary.kkColorTextDarker,
                        fontSize = StyleDictionary.kkFontSize3,
                    )
                }
            }
            Spacer(Modifier.height(StyleDictionary.kkSpacing300))
        }

        // 底部固定列：白底、不遮擋上方可捲動內容（內容區以 weight 撐滿，非疊在其上）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(StyleDictionary.kkColorWhite)
                .navigationBarsPadding()
                .padding(horizontal = StyleDictionary.kkSpacing300, vertical = StyleDictionary.kkSpacing200),
            contentAlignment = Alignment.Center,
        ) {
            if (isPosterReady) {
                PosterReadyBanner(onClick = onViewResult)
            } else {
                BreathingLoadingText(
                    text = "繼續描繪你的專屬旅行場景⋯", // TODO: replace with stringResource
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
    val bgColor = if (isHighlight) StyleDictionary.kkColorBackgroundPrimaryLighter else StyleDictionary.kkColorWhite
    val borderColor = if (isHighlight) StyleDictionary.kkColorBorderPrimaryLight else StyleDictionary.kkColorBorderLight
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(StyleDictionary.kkRadiusLg))
                .border(1.dp, borderColor, RoundedCornerShape(StyleDictionary.kkRadiusLg))
                .background(bgColor)
                .padding(StyleDictionary.kkSpacing200),
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
        color = StyleDictionary.kkColorTextMedium.copy(alpha = alpha),
        fontSize = StyleDictionary.kkFontSize3,
        modifier = Modifier.testTag("companion_share_poster_generating_breathing"),
    )
}

/** 海報就緒的置底入口：主色橫幅按鈕，點擊才前往結果頁（不自動跳頁，與 iOS 一致）。 */
@Composable
private fun PosterReadyBanner(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(StyleDictionary.kkRadiusLg))
            .background(StyleDictionary.kkColorBackgroundPrimaryMedium)
            .clickable(onClick = onClick)
            .padding(StyleDictionary.kkSpacing200)
            .testTag("companion_share_poster_ready_banner"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "我找到你的旅行 DNA 了", // TODO: replace with stringResource
                color = StyleDictionary.kkColorTextPrimaryLighter,
                fontSize = StyleDictionary.kkFontSize2,
            )
            Spacer(Modifier.height(StyleDictionary.kkSpacing050))
            Text(
                "我們一起去看看吧！", // TODO: replace with stringResource
                color = StyleDictionary.kkColorWhite,
                fontWeight = FontWeight(StyleDictionary.kkFontWeightBold),
                fontSize = StyleDictionary.kkFontSize4,
            )
        }
        Spacer(Modifier.width(StyleDictionary.kkSpacing150))
        Box(
            modifier = Modifier
                .size(StyleDictionary.kkDimensionIconXl)
                .clip(CircleShape)
                .background(StyleDictionary.kkColorBackgroundPrimaryLight),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(com.kkday.design.R.drawable.ic_arrow_right_line),
                contentDescription = null,
                tint = StyleDictionary.kkColorWhite,
                modifier = Modifier.size(StyleDictionary.kkDimensionIconSm),
            )
        }
    }
}

/**
 * 旅行人格/tag 標籤——即「推薦理由」以上的區塊。
 * v2 海報路徑的目的地/tagline 已顯示於 Hero 底部的黑色半透明資訊卡（見 [PosterHeroWithBadge]），
 * 故 [showDestinationAndTagline] 預設為 true 僅供 legacy v1 分支使用，v2 呼叫時關閉以避免重複。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CompanionResultHighlightContent(
    result: com.kkday.model.companion.QuizCompletionResult?,
    showDestinationAndTagline: Boolean = true,
) {
    val highlightTags = result?.highlightTags.orEmpty()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = StyleDictionary.kkSpacing300,
                end = StyleDictionary.kkSpacing300,
                top = StyleDictionary.kkSpacing200, // 與海報間距只需 16dp，比其餘留白窄
            )
            .testTag("companion_share_result_highlight"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (showDestinationAndTagline && result?.destinationCn.orEmpty().isNotBlank()) {
            Text(
                "${result?.destinationCn.orEmpty()} · ${result?.destinationCountry.orEmpty()}",
                fontWeight = FontWeight(StyleDictionary.kkFontWeightBold),
                fontSize = StyleDictionary.kkFontSize5,
                color = StyleDictionary.kkColorTextDarker,
                modifier = Modifier.testTag("companion_share_result_destination"),
            )
            Spacer(Modifier.height(StyleDictionary.kkSpacing100))
        }
        if (showDestinationAndTagline && result?.tagline.orEmpty().isNotBlank()) {
            Text(
                result?.tagline.orEmpty(),
                color = StyleDictionary.kkColorTextDark,
                fontSize = StyleDictionary.kkFontSize4,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("companion_share_result_tagline"),
            )
            Spacer(Modifier.height(StyleDictionary.kkSpacing150))
        }
        if (result?.travelIdentity.orEmpty().isNotBlank()) {
            Text(
                result?.travelIdentity.orEmpty(),
                fontWeight = FontWeight(StyleDictionary.kkFontWeightBold),
                fontSize = StyleDictionary.kkFontSize3,
                color = StyleDictionary.kkColorTextPrimaryDark,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("companion_share_result_travel_identity"),
            )
            Spacer(Modifier.height(StyleDictionary.kkSpacing100))
        }
        if (highlightTags.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(StyleDictionary.kkSpacing100, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(StyleDictionary.kkSpacing100),
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
                        fontSize = StyleDictionary.kkFontSize1,
                        onClick = {},
                    )
                }
            }
        }
    }
}

/** 「推薦理由」「分享文案」卡片——排在分享截圖範圍之外，維持一般滾動內容。 */
@Composable
private fun CompanionResultDetailsContent(
    result: com.kkday.model.companion.QuizCompletionResult?,
    bottomSafeArea: Dp,
) {
    // 資料已完整到手的靜態摘要：分段推薦理由以空行合併成單一卡片內文
    val recommendation = result?.recommendationText.orEmpty()
    val socialPost = result?.socialPost.orEmpty()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = StyleDictionary.kkSpacing300,
                end = StyleDictionary.kkSpacing300,
                bottom = StyleDictionary.kkSpacing300,
                top = StyleDictionary.kkSpacing200,
            )
            .testTag("companion_share_result_details"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (recommendation.isNotBlank()) {
            TitledInfoCard(
                title = "推薦理由", // TODO: replace with stringResource
                content = recommendation,
                testTag = "companion_share_result_recommendation",
            )
            Spacer(Modifier.height(StyleDictionary.kkSpacing150))
        }
        if (socialPost.isNotBlank()) {
            TitledInfoCard(
                title = "分享文案", // TODO: replace with stringResource
                content = socialPost,
                testTag = "companion_share_result_social_post",
                onCopyClick = { context.copySocialPostToClipboard(socialPost) },
            )
        }
        Spacer(Modifier.height(bottomSafeArea))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResultActionsBottomSheet(
    result: com.kkday.model.companion.QuizCompletionResult?,
    isImageReady: Boolean,
    onDismiss: () -> Unit,
    onShareDna: () -> Unit,
    onShareToInstagramStories: () -> Unit,
    onExploreDestination: () -> Unit,
    onDownloadImage: () -> Unit,
    onViewDetail: () -> Unit,
    onGoHome: () -> Unit,
    showViewDetail: Boolean = true,
    showGoHome: Boolean = true,
    showExplore: Boolean = false,
    onContinuePlanning: (() -> Unit)? = null,
    onViewOthers: (() -> Unit)? = null,
    onBackToList: (() -> Unit)? = null,
    onDeleteRecord: (() -> Unit)? = null,
) {
    val destinationName = result?.destinationCn.orEmpty()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        shape = RoundedCornerShape(
            topStart = StyleDictionary.kkRadiusXl,
            topEnd = StyleDictionary.kkRadiusXl,
        ),
        dragHandle = { KKModalBottomSheetDragHandle() },
        scrimColor = Color.Black.copy(alpha = 0.5f),
    ) {
        // 依 Phase 1 mockup 定案版排序，列與列之間加分隔線
        val rows = buildList<@Composable () -> Unit> {
            if (onContinuePlanning != null) {
                add {
                    ActionRow(
                        // TODO: 尚未有專屬指南針 icon，暫用地圖 icon，待設計提供後替換
                        iconRes = com.kkday.design.R.drawable.ic_map_location_line,
                        title = "繼續規劃我的行程", // TODO: replace with stringResource
                        description = "回到規劃對話，和小旅一起排", // TODO: replace with stringResource
                        testTag = "companion_action_continue_planning",
                        hero = true,
                        onClick = onContinuePlanning,
                    )
                }
            }
            if (onViewOthers != null) {
                add {
                    ActionRow(
                        iconRes = com.kkday.design.R.drawable.ic_people_line,
                        title = "看其他人的測試結果", // TODO: replace with stringResource
                        description = "逛逛別人的命定城市", // TODO: replace with stringResource
                        testTag = "companion_action_view_others",
                        onClick = onViewOthers,
                    )
                }
            }
            add {
                ActionRow(
                    // TODO: 尚未有專屬 IG 限動 icon，暫沿用既有分享 icon，待設計提供後替換
                    iconRes = com.kkday.design.R.drawable.ic_share_android_line,
                    title = "分享到 IG 限時動態", // TODO: replace with stringResource
                    description = "一鍵貼到限動", // TODO: replace with stringResource
                    testTag = "companion_action_share_ig_stories",
                    onClick = onShareToInstagramStories,
                )
            }
            add {
                ActionRow(
                    iconRes = com.kkday.design.R.drawable.ic_share_android_line,
                    title = "分享我的旅行 DNA", // TODO: replace with stringResource
                    description = "分享給好友或其他社群", // TODO: replace with stringResource
                    testTag = "companion_action_share",
                    onClick = onShareDna,
                )
            }
            if (showExplore) {
                add {
                    ActionRow(
                        iconRes = com.kkday.design.R.drawable.ic_map_line,
                        title = "探索${destinationName}行程", // TODO: replace with stringResource
                        description = "看小旅推薦的${destinationName}行程", // TODO: replace with stringResource
                        testTag = "companion_action_explore",
                        onClick = onExploreDestination,
                    )
                }
            }
            add {
                ActionRow(
                    iconRes = com.kkday.design.R.drawable.ic_download_line,
                    title = "下載到我的裝置", // TODO: replace with stringResource
                    description = "把測驗結果存成圖片", // TODO: replace with stringResource
                    testTag = "companion_action_download",
                    enabled = isImageReady,
                    onClick = onDownloadImage,
                )
            }
            if (showViewDetail) {
                add {
                    ActionRow(
                        iconRes = com.kkday.design.R.drawable.ic_note_line,
                        title = "查看完整測驗結果", // TODO: replace with stringResource
                        description = "旅行人格與命定旅程解析", // TODO: replace with stringResource
                        testTag = "companion_action_detail",
                        onClick = onViewDetail,
                    )
                }
            }
            if (showGoHome) {
                add {
                    ActionRow(
                        iconRes = com.kkday.design.R.drawable.ic_message_line,
                        title = "回到我的旅伴", // TODO: replace with stringResource
                        description = "和小旅繼續聊", // TODO: replace with stringResource
                        testTag = "companion_action_go_home",
                        onClick = onGoHome,
                    )
                }
            }
            if (onBackToList != null) {
                add {
                    ActionRow(
                        iconRes = com.kkday.design.R.drawable.ic_list_view_line,
                        title = "回到旅行 DNA 回顧列表", // TODO: replace with stringResource
                        description = "查看其他測驗紀錄", // TODO: replace with stringResource
                        testTag = "companion_action_back_to_list",
                        onClick = onBackToList,
                    )
                }
            }
            if (onDeleteRecord != null) {
                add {
                    ActionRow(
                        iconRes = com.kkday.design.R.drawable.ic_delete_line,
                        title = "刪除這筆紀錄", // TODO: replace with stringResource
                        description = "刪除後無法復原", // TODO: replace with stringResource
                        testTag = "companion_action_delete",
                        onClick = onDeleteRecord,
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = StyleDictionary.kkSpacing400),
        ) {
            rows.forEachIndexed { index, row ->
                if (index > 0) {
                    HorizontalDivider(
                        color = StyleDictionary.kkColorBorderLight,
                        modifier = Modifier.padding(horizontal = StyleDictionary.kkSpacing300),
                    )
                }
                row()
            }
        }
    }
}

@Composable
private fun ActionRow(
    @DrawableRes iconRes: Int,
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
            .padding(horizontal = StyleDictionary.kkSpacing300, vertical = StyleDictionary.kkSpacing200)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (hero) StyleDictionary.kkColorBackgroundPrimaryButton
                    else StyleDictionary.kkColorBackgroundPrimaryLighter,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (hero) StyleDictionary.kkColorWhite else StyleDictionary.kkColorTextPrimaryDark,
            )
        }
        Spacer(Modifier.width(StyleDictionary.kkSpacing200))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontWeight = FontWeight(StyleDictionary.kkFontWeightBold),
                fontSize = StyleDictionary.kkFontSize3,
                color = if (enabled) StyleDictionary.kkColorTextDarker else StyleDictionary.kkColorTextLight,
            )
            Text(
                description,
                fontSize = StyleDictionary.kkFontSize2,
                color = if (enabled) StyleDictionary.kkColorTextMedium else StyleDictionary.kkColorTextLight,
            )
        }
        Icon(
            painter = painterResource(com.kkday.design.R.drawable.ic_arrow_right_line),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = if (enabled) StyleDictionary.kkColorTextMedium else StyleDictionary.kkColorTextLight,
        )
    }
}

// ---------- 結果詳情（原 ResultScreen 內容）----------

@OptIn(ExperimentalLayoutApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun ResultDetailScreen(
    viewModel: AiCompanionViewModel,
    creation: CompanionCreationState,
    onBack: () -> Unit,
) {
    val analysis by viewModel.analysisState.collectAsStateWithLifecycle()
    val result = (analysis as? AnalysisState.Success)?.result
    val companionName = creation.companionName.ifBlank { "旅伴" }
    val context = LocalContext.current

    ScreenScaffold(title = "完整測驗結果", screenTag = "companion_result_detail_screen", onBack = onBack) { // TODO: stringResource
        if (result != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CompanionAsyncImage(
                    url = creation.avatarUrl,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(StyleDictionary.kkColorBackgroundPrimaryLighter),
                    placeholder = {
                        Text("?", color = StyleDictionary.kkColorTextPrimaryDark)
                    },
                )
                Spacer(Modifier.width(StyleDictionary.kkSpacing150))
                Text(
                    companionName,
                    fontWeight = FontWeight(StyleDictionary.kkFontWeightMediumAndroid),
                    color = StyleDictionary.kkColorTextDarker,
                )
            }

            Spacer(Modifier.height(StyleDictionary.kkSpacing300))

            ChatBubble(isCompanion = true) {
                Text(
                    result.companionQuote.ifBlank { result.recommendation.firstOrNull().orEmpty() },
                    color = StyleDictionary.kkColorTextDarker,
                    fontSize = StyleDictionary.kkFontSize3,
                    modifier = Modifier.testTag("companion_result_quote"),
                )
            }

            Spacer(Modifier.height(StyleDictionary.kkSpacing150))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(StyleDictionary.kkSpacing100),
                verticalArrangement = Arrangement.spacedBy(StyleDictionary.kkSpacing100),
            ) {
                result.highlightTags.forEach { tag ->
                    SelectablePill(label = tag, isSelected = true, enabled = false, testTag = "result_tag_$tag", onClick = {})
                }
            }

            Spacer(Modifier.height(StyleDictionary.kkSpacing150))

            ChatBubble(isCompanion = true) {
                Column {
                    Text(
                        "最適合你的旅行身份是", // TODO: replace with stringResource
                        color = StyleDictionary.kkColorTextDarker,
                        fontSize = StyleDictionary.kkFontSize3,
                    )
                    Spacer(Modifier.height(StyleDictionary.kkSpacing100))
                    Text(
                        result.travelIdentity,
                        fontWeight = FontWeight(StyleDictionary.kkFontWeightBold),
                        fontSize = StyleDictionary.kkFontSize6,
                        color = StyleDictionary.kkColorTextPrimaryDark,
                        modifier = Modifier.testTag("companion_result_title"),
                    )
                }
            }

            Spacer(Modifier.height(StyleDictionary.kkSpacing200))

            // 沿用 ResultScreen 已合成好的同一份 ViewModel state，不重新觸發合成
            val shareImageV2 by viewModel.shareImageV2State.collectAsStateWithLifecycle()
            val posterBitmap = (shareImageV2 as? ShareImageV2State.Ready)?.posterBitmap
            if (posterBitmap != null) {
                // spec「Share and Export Output Integrity」：預覽用 Fit/CenterInside，不 Crop
                Image(
                    bitmap = posterBitmap.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(StyleDictionary.kkRadiusLg))
                        .testTag("companion_result_detail_poster"),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(StyleDictionary.kkRadiusLg))
                        .background(StyleDictionary.kkColorBackgroundPrimaryLighter),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "${result.destinationCn} · ${result.destinationCountry}",
                        color = StyleDictionary.kkColorTextDark,
                        fontSize = StyleDictionary.kkFontSize2,
                    )
                }
            }

            // 海報下方完整呈現分析文字（與產圖等待頁相同的 recommendation / social_post 內容）
            if (result.recommendationText.isNotBlank()) {
                Spacer(Modifier.height(StyleDictionary.kkSpacing200))
                TitledInfoCard(
                    title = "推薦理由", // TODO: replace with stringResource
                    content = result.recommendationText,
                    testTag = "companion_result_detail_recommendation",
                )
            }
            if (result.socialPost.isNotBlank()) {
                Spacer(Modifier.height(StyleDictionary.kkSpacing150))
                TitledInfoCard(
                    title = "分享文案", // TODO: replace with stringResource
                    content = result.socialPost,
                    testTag = "companion_result_detail_social_post",
                    onCopyClick = { context.copySocialPostToClipboard(result.socialPost) },
                )
            }
        }
    }
}

@Composable
private fun ChatBubble(
    isCompanion: Boolean,
    content: @Composable () -> Unit,
) {
    val bgColor = if (isCompanion) StyleDictionary.kkColorBackgroundSurfaceLight
    else StyleDictionary.kkColorBackgroundPrimaryLighter
    val alignment = if (isCompanion) Alignment.CenterStart else Alignment.CenterEnd

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(StyleDictionary.kkRadiusLg))
                .background(bgColor)
                .padding(StyleDictionary.kkSpacing200),
        ) {
            content()
        }
    }
}

/** 圓角卡片：標題（依 API 欄位語意命名）+ 內文，用於呈現 recommendation / social_post 這類長文字說明。 */
@Composable
private fun TitledInfoCard(
    title: String,
    content: String,
    testTag: String,
    onCopyClick: (() -> Unit)? = null,
) {
    TitledInfoCard(title = title, testTag = testTag, onCopyClick = onCopyClick) {
        Text(
            content,
            color = StyleDictionary.kkColorTextDarker,
            fontSize = StyleDictionary.kkFontSize3,
        )
    }
}

/**
 * 圓角卡片：標題（依 API 欄位語意命名）+ 任意內容，供需要自訂內文樣式（如逐字打字機效果）的呼叫端使用。
 * [onCopyClick] 有給值時，標題右側會多一顆複製按鈕（複製內文到系統剪貼簿）。
 */
@Composable
private fun TitledInfoCard(
    title: String,
    testTag: String,
    onCopyClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(StyleDictionary.kkRadiusLg))
            .border(1.dp, StyleDictionary.kkColorBorderLight, RoundedCornerShape(StyleDictionary.kkRadiusLg))
            .background(StyleDictionary.kkColorWhite)
            .padding(StyleDictionary.kkSpacing200)
            .testTag(testTag),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                title,
                fontWeight = FontWeight(StyleDictionary.kkFontWeightBold),
                fontSize = StyleDictionary.kkFontSize3,
                color = StyleDictionary.kkColorTextPrimaryDark,
                modifier = Modifier.weight(1f),
            )
            if (onCopyClick != null) {
                IconButton(
                    onClick = onCopyClick,
                    modifier = Modifier
                        .size(StyleDictionary.kkDimensionIconLg)
                        .testTag("${testTag}_copy_btn"),
                ) {
                    Icon(
                        painter = painterResource(com.kkday.design.R.drawable.ic_copy_line),
                        contentDescription = null,
                        tint = StyleDictionary.kkColorTextMedium,
                        modifier = Modifier.size(StyleDictionary.kkDimensionIconSm),
                    )
                }
            }
        }
        Spacer(Modifier.height(StyleDictionary.kkSpacing100))
        content()
    }
}

// ---------- 旅行 DNA 回顧 ----------

/**
 * 測驗結果歷史回顧：列表（縮圖 + 旅行人格 + 目的地 + 日期）；
 * 點擊單筆進入海報檢視，可再次分享（IG 限動 / 系統分享面板）。
 */
@Composable
private fun CompanionHistoryScreen(
    viewModel: AiCompanionViewModel,
    onBack: () -> Unit,
) {
    val history by viewModel.quizHistory.collectAsStateWithLifecycle()
    // 用 createdAt 當 key 而非直接持有 QuizHistoryRecord：這樣海報素材補打成功回填後，
    // viewModel.quizHistory 更新時詳情頁能跟著拿到最新的素材路徑，不會卡在舊的空值快照
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
            title = "旅行 DNA 回顧", // TODO: replace with stringResource
            screenTag = "companion_history_screen",
            onBack = onBack,
        ) {
            if (history.isEmpty()) {
                // ScreenScaffold 已有 verticalScroll，這裡不能再放可捲動/無限高度元件
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = StyleDictionary.kkSpacing600)
                        .testTag("companion_history_empty"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        "還沒有測驗紀錄", // TODO: replace with stringResource
                        fontWeight = FontWeight(StyleDictionary.kkFontWeightBold),
                        fontSize = StyleDictionary.kkFontSize4,
                        color = StyleDictionary.kkColorTextDarker,
                    )
                    Spacer(Modifier.height(StyleDictionary.kkSpacing100))
                    Text(
                        "完成旅行 DNA 測驗後，結果會保留在這裡", // TODO: replace with stringResource
                        color = StyleDictionary.kkColorTextMedium,
                        fontSize = StyleDictionary.kkFontSize3,
                    )
                }
            } else {
                // 上限 20 筆，直接用 Column 靠外層 ScreenScaffold 捲動（LazyColumn 會撞上外層 verticalScroll 的無限高度約束）
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(StyleDictionary.kkSpacing150),
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
            .clip(RoundedCornerShape(StyleDictionary.kkRadiusLg))
            .border(1.dp, StyleDictionary.kkColorBorderLight, RoundedCornerShape(StyleDictionary.kkRadiusLg))
            .background(StyleDictionary.kkColorWhite)
            .clickable { onClick() }
            .padding(StyleDictionary.kkSpacing200),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompanionAsyncImage(
            url = record.displayPosterUrl(),
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(StyleDictionary.kkRadiusMd)),
            placeholder = {
                Icon(
                    painter = painterResource(com.kkday.design.R.drawable.ic_globe_fill),
                    contentDescription = null,
                    tint = StyleDictionary.kkColorTextMedium,
                    modifier = Modifier.size(StyleDictionary.kkDimensionIconSm),
                )
            },
        )
        Spacer(Modifier.width(StyleDictionary.kkSpacing150))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                record.result.travelIdentity.ifBlank { record.result.travelIdentityEn },
                fontWeight = FontWeight(StyleDictionary.kkFontWeightMediumAndroid),
                color = StyleDictionary.kkColorTextDarker,
                fontSize = StyleDictionary.kkFontSize3,
            )
            Spacer(Modifier.height(StyleDictionary.kkSpacing050))
            Text(
                listOf(
                    record.result.destinationCn.ifBlank { record.result.destinationEn },
                    formatHistoryDate(record.createdAt),
                ).filter { it.isNotBlank() }.joinToString(" · "),
                color = StyleDictionary.kkColorTextMedium,
                fontSize = StyleDictionary.kkFontSize2,
            )
        }
        Icon(
            painter = painterResource(com.kkday.design.R.drawable.ic_arrow_right_line),
            contentDescription = null,
            tint = StyleDictionary.kkColorTextMedium,
            modifier = Modifier.size(StyleDictionary.kkDimensionIconSm),
        )
    }
}

/**
 * 單筆歷史的海報檢視 + 分享（IG 限動 / 系統分享面板；素材缺漏時只能分享文字）。
 * 排版比照 ResultScreen 的 Ready 分支：滿版 Hero 置頂、其餘內容依序往下排列，右上角統一用圓形返回鈕，
 * 不再用 ScreenScaffold + ChatBubble 問答式排版與離屏合成好的扁平圖（會因縮放造成畫質變差）。
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun CompanionHistoryDetailContent(
    viewModel: AiCompanionViewModel,
    record: QuizHistoryRecord,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val searchResultRouter: SearchResultRouter = koinInject()
    val posterHistoryStorage: PosterHistoryStorage = koinInject()
    var showBottomSheet by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    var bottomBarHeight by remember { mutableStateOf(0.dp) }
    // 分享／分享到 IG 限時動態用：截「Hero + 疊加資訊卡」這一整塊畫面當下實際顯示的內容，比照 ResultScreen
    val shareableGraphicsLayer = rememberGraphicsLayer()

    // 進這筆紀錄時若海報是空的，補打一次 share-image（同一組 completionUuid）；
    // 只在切換到不同紀錄時觸發一次，成功回填後 record 會經由外層 quizHistory 更新
    LaunchedEffect(record.createdAt) {
        viewModel.retryFetchHistoryPoster(record)
    }

    // 從本機檔案即時重組 Hero 素材，比照 ResultScreen 用 PosterHeroWithBadge 即時渲染文字，不依賴扁平圖
    var assets by remember(record.createdAt) { mutableStateOf<ResolvedPosterAssets?>(null) }
    var assetsLoaded by remember(record.createdAt) { mutableStateOf(false) }
    LaunchedEffect(record.createdAt) {
        assets = record.loadResolvedPosterAssets(posterHistoryStorage)
        assetsLoaded = true
    }

    val view = LocalView.current
    SideEffect {
        val window = (view.context as Activity).window
        window.statusBarColor = Color.Transparent.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = assets == null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
        }
    }

    val caption = record.result.socialPost.ifBlank { record.result.recommendation.firstOrNull().orEmpty() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { testTagsAsResourceId = true }
            .testTag("companion_history_detail_screen")
            .background(StyleDictionary.kkColorWhite),
    ) {
        val currentAssets = assets
        if (currentAssets != null) {
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
                    PosterHeroWithBadge(assets = currentAssets, metrics = PosterOverlayMetrics.Live)
                }
                PosterBelowHeroContent(assets = currentAssets, showFooter = false, metrics = PosterOverlayMetrics.Live)
                CompanionResultHighlightContent(record.result, showDestinationAndTagline = false)
                CompanionResultDetailsContent(result = record.result, bottomSafeArea = bottomBarHeight)
            }
        } else if (assetsLoaded) {
            // 素材缺漏（例如較舊版本留下、尚未有這些欄位的紀錄）：退回基本資訊呈現，不顯示 Hero
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
                    .padding(top = StyleDictionary.kkSpacing600),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = StyleDictionary.kkSpacing300)
                        .height(200.dp)
                        .clip(RoundedCornerShape(StyleDictionary.kkRadiusLg))
                        .background(StyleDictionary.kkColorBackgroundPrimaryLighter)
                        .testTag("companion_history_poster_fallback"),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "${record.result.destinationCn} · ${record.result.destinationCountry}",
                        color = StyleDictionary.kkColorTextDark,
                        fontSize = StyleDictionary.kkFontSize2,
                    )
                }
                CompanionResultHighlightContent(record.result, showDestinationAndTagline = true)
                CompanionResultDetailsContent(result = record.result, bottomSafeArea = bottomBarHeight)
            }
        }

        // 底部固定「更多動作」按鈕列，比照 ResultScreen
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    bottomBarHeight = with(density) { coordinates.size.height.toDp() }
                }
                .background(StyleDictionary.kkColorWhite)
                .navigationBarsPadding()
                .padding(horizontal = StyleDictionary.kkSpacing300, vertical = StyleDictionary.kkSpacing200),
        ) {
            PrimaryButton(
                text = "更多動作", // TODO: replace with stringResource
                testTag = "companion_history_more_actions_btn",
                onClick = { showBottomSheet = true },
            )
        }
    }

    if (showBottomSheet) {
        val isImageReady = assets != null
        ResultActionsBottomSheet(
            result = record.result,
            isImageReady = isImageReady,
            onDismiss = { showBottomSheet = false },
            onShareDna = {
                showBottomSheet = false
                scope.launch {
                    val shared = isImageReady && CompanionShareActions.sharePosterBitmap(
                        context,
                        shareableGraphicsLayer.toImageBitmap().asAndroidBitmap(),
                        caption,
                    )
                    if (!shared) CompanionShareActions.shareText(context, caption)
                }
            },
            onShareToInstagramStories = {
                showBottomSheet = false
                // IG 限時動態不支援帶入文字文案，故點擊分享的同時把分享文案複製到剪貼簿，方便使用者貼上
                if (caption.isNotBlank()) context.copySocialPostToClipboard(caption)
                scope.launch {
                    val shared = isImageReady && CompanionShareActions.shareBitmapToInstagramStories(
                        context,
                        shareableGraphicsLayer.toImageBitmap().asAndroidBitmap(),
                        caption,
                    )
                    if (!shared) CompanionShareActions.shareText(context, caption)
                }
            },
            onExploreDestination = {
                showBottomSheet = false
                val keyword = record.result.destinationCn.ifBlank { record.result.destinationEn }
                searchResultRouter.launchSearchResult(context, keyword)
            },
            onDownloadImage = {
                showBottomSheet = false
                if (isImageReady) {
                    scope.launch {
                        CompanionShareActions.downloadBitmap(context, shareableGraphicsLayer.toImageBitmap().asAndroidBitmap())
                    }
                }
            },
            onViewDetail = {},
            onGoHome = {},
            showViewDetail = false,
            showGoHome = false,
            showExplore = true,
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
        KKDialog(
            headerType = DialogHeaderType.Text(title = "刪除這筆紀錄？", useScrollableContent = false), // TODO: replace with stringResource
            showHeaderCloseButton = false,
            showFooterShadow = false,
            onDismissRequest = { showDeleteConfirm = false },
            content = {
                Text(
                    "刪除後無法復原", // TODO: replace with stringResource
                    color = StyleDictionary.kkColorTextMedium,
                    fontSize = StyleDictionary.kkFontSize3,
                    modifier = Modifier.padding(
                        horizontal = StyleDictionary.kkSpacing300,
                        vertical = StyleDictionary.kkSpacing150,
                    ),
                )
            },
            onClickPrimaryButton = {
                KKButton(
                    buttonText = "刪除", // TODO: replace with stringResource
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
                KKButton(
                    buttonText = "取消", // TODO: replace with stringResource
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
 * 用「旅行 DNA 回顧」歷史紀錄的本機素材路徑重建 [ResolvedPosterAssets]，供詳情頁比照 ResultScreen
 * 即時渲染 Hero + 徽章文字。路徑缺漏或檔案讀取失敗（例如較舊版本留下、尚未有這些欄位的紀錄）回傳 null，
 * 呼叫端需自行 fallback 到基本資訊呈現。
 */
private suspend fun QuizHistoryRecord.loadResolvedPosterAssets(storage: PosterHistoryStorage): ResolvedPosterAssets? {
    if (heroLocalPath.isBlank() || stampLocalPath.isBlank() || tagIconLocalPaths.isEmpty()) return null
    val hero = storage.load(heroLocalPath) ?: return null
    val stamp = storage.load(stampLocalPath) ?: return null
    val tagIcons = tagIconLocalPaths.map { path -> storage.load(path) ?: return null }
    return ResolvedPosterAssets(
        hero = hero,
        stamp = stamp,
        tagIcons = tagIcons,
        content = ShareImageV2Content(
            travelIdentity = result.travelIdentity,
            travelIdentityEn = result.travelIdentityEn,
            destinationCn = result.destinationCn,
            destinationEn = result.destinationEn,
            tagline = result.tagline,
            highlightTags = result.highlightTags,
            companionQuote = result.companionQuote,
            companionName = companionSnapshot.name,
        ),
        qrCode = null,
    )
}

/**
 * 回顧列表縮圖來源：優先用 share-image-v2 本地合成好的完整海報檔案，缺漏時退回 hero 素材本身
 * （例如尚未合成好縮圖、但詳情頁用的個別素材已到位），兩者都轉成 file:// URI 讓 Coil 能載入。
 */
private fun QuizHistoryRecord.displayPosterUrl(): String =
    posterLocalPath.takeIf { it.isNotBlank() }?.let { "file://$it" }
        ?: heroLocalPath.takeIf { it.isNotBlank() }?.let { "file://$it" }
        ?: ""

private fun formatHistoryDate(epochMillis: Long): String {
    if (epochMillis <= 0L) return ""
    return java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale.getDefault())
        .format(java.util.Date(epochMillis))
}

/** 分享文案卡片的複製動作：寫入系統剪貼簿並跳 Toast 提示。 */
private fun Context.copySocialPostToClipboard(text: String) {
    copyToClipboard(label = "分享文案", text = text) {
        showToast("已複製到剪貼簿", Toast.LENGTH_SHORT) // TODO: replace with stringResource
    }
}
