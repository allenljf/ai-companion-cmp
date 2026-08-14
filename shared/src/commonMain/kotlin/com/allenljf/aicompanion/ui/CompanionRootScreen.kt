package com.allenljf.aicompanion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.allenljf.aicompanion.model.QuizGalleryItem
import com.allenljf.aicompanion.theme.Tokens
import com.allenljf.aicompanion.ui.components.ButtonSizeType
import com.allenljf.aicompanion.ui.components.ButtonState
import com.allenljf.aicompanion.ui.components.ButtonType
import com.allenljf.aicompanion.ui.components.AppButton
import com.allenljf.aicompanion.viewmodel.AiCompanionViewModel
import com.allenljf.aicompanion.viewmodel.IntroductionState
import org.jetbrains.compose.resources.painterResource
import aicompanion.shared.generated.resources.Res
import aicompanion.shared.generated.resources.ic_arrow_left_line

/**
 * 測驗流程 step 列舉（沿用原始碼設計，去 KKday 化，命名/順序不變）。
 * Phase 2 行程規劃：開場摘要（travel-summary）→ 聊天式偏好問卷（純前端，在 PlanChat 內）
 * → 行程成果（travel-guide；總覽/每日細節都在 TripResult 的分頁內，無獨立細節頁）
 * TripList = 回顧我的旅程列表（純本地資料）
 * OrderOpening = demo「帶訂單開場」：抓即將出發訂單材料 → LLM 判斷目的地選項 → 選定後進 PlanChat
 */
enum class AiCompanionStep {
    CreateCompanion, Intro, Home, Quiz, Result, ResultDetail, History, QuizGallery, QuizGalleryDetail,
    ImportItinerary, PlanChat, TripResult, TripList, OrderOpening,
}

/**
 * AI 旅伴測驗流程根組件。
 * 注意：此為功能性 fallback UI —— 使用 material3 primitives + Tokens + 硬編字串（標 TODO），
 * 待設計素材與 PM 字串到位再替換。E2E 選擇器一律用 testTag（不用會被 PM 替換的中文字串）。
 *
 * 原始碼用 BackHandler 對接 step 回退；CMP 1.11.1 尚未確認有可用的 multiplatform BackHandler，
 * 故本檔只保留 step 回退邏輯本身（各 step 分支內的 onBack callback），Android 端實體返回鍵
 * 接線留給 T14（不自建 expect/actual）。
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
                companionName = creation.companionName.ifBlank { "旅伴" }, // TODO: i18n fallback
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
@Composable
internal fun ScreenScaffold(
    title: String,
    screenTag: String,
    onBack: (() -> Unit)? = null,
    action: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    // 原用 Activity.window 設定沈浸式狀態列（透明+深色圖示），KMP commonMain 無此 API，
    // demo 不做這層視覺效果，直接移除（不影響版面邏輯）。
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag(screenTag)
            .background(Tokens.colorBackgroundSurfaceLight)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(Tokens.spacing300),
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
                        painter = painterResource(Res.drawable.ic_arrow_left_line),
                        contentDescription = null,
                        tint = Tokens.colorTextDarker,
                    )
                }
                Spacer(Modifier.width(Tokens.spacing100))
            }
            Text(
                text = title,
                style = ScreenTitleStyle,
                modifier = Modifier.weight(1f),
            )
            action?.invoke()
        }
        Spacer(Modifier.height(Tokens.spacing300))
        content()
    }
}

// 原 fontH6（com.kkday.design.font）為 DS 標題字級，未移植；用 Tokens 手動組一個語意相近的樣式。
internal val ScreenTitleStyle = androidx.compose.ui.text.TextStyle(
    fontSize = Tokens.fontSize4,
    fontWeight = androidx.compose.ui.text.font.FontWeight(Tokens.fontWeightBold),
    color = Tokens.colorTextDarker,
)

@Composable
internal fun PrimaryButton(
    text: String,
    enabled: Boolean = true,
    testTag: String? = null,
    onClick: () -> Unit,
) {
    Box(modifier = if (testTag != null) Modifier.testTag(testTag) else Modifier) {
        AppButton(
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
 * 統一的旅伴/題目圖片載入元件：無 Coil 移植，一律顯示 [placeholder]（不判斷載入中/失敗狀態）。
 * 保留原簽章（url/blurInOnLoad/loadingContent/errorContent 等）讓所有呼叫點原樣搬移不用改，
 * 未使用的參數留著純粹是相容性佔位。
 * TODO: image loading — 之後要接真圖再實作 loadingContent/errorContent 分支。
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
    val sizedModifier = placeholderAspectRatio?.let { modifier.aspectRatio(it) } ?: modifier
    Box(modifier = sizedModifier, contentAlignment = Alignment.Center) {
        placeholder()
    }
}

// 原用 java.text.SimpleDateFormat（JVM-only，iOS target 編不過）。
// 改用純 Kotlin 的曆法換算（Howard Hinnant civil_from_days）避免額外引入 kotlinx-datetime 依賴，
// TripListScreen.formatSavedAtDate 與 ResultScreens.formatHistoryDate 共用。
// TODO: timezone —— 用台北時區（UTC+8）近似值換算日界，非精確依裝置時區換算。
internal fun formatEpochMillisAsDate(epochMillis: Long): String {
    val localMillis = epochMillis + 8 * 3_600_000L
    val days = localMillis.floorDiv(86_400_000L)
    val z = days + 719468L
    val era = (if (z >= 0) z else z - 146096L).floorDiv(146097L)
    val doe = z - era * 146097L
    val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
    val y = yoe + era * 400L
    val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
    val mp = (5 * doy + 2) / 153
    val d = doy - (153 * mp + 2) / 5 + 1
    val m = if (mp < 10) mp + 3 else mp - 9
    val year = if (m <= 2) y + 1 else y
    return "$year/${m.toString().padStart(2, '0')}/${d.toString().padStart(2, '0')}"
}
