package com.allenljf.aicompanion.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.painter.Painter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.allenljf.aicompanion.model.AiPartnerResult
import com.allenljf.aicompanion.model.CompanionAppearanceOption
import com.allenljf.aicompanion.model.CompanionTraitOption
import com.allenljf.aicompanion.model.QuizGalleryItem
import com.allenljf.aicompanion.model.QuizOption
import com.allenljf.aicompanion.model.QuizQuestion
import com.allenljf.aicompanion.model.SavedTripRecord
import com.allenljf.aicompanion.theme.Tokens
import com.allenljf.aicompanion.ui.components.AppButton
import com.allenljf.aicompanion.ui.components.AppTag
import com.allenljf.aicompanion.ui.components.AppTagSolidColor
import com.allenljf.aicompanion.ui.components.AppTextField
import com.allenljf.aicompanion.ui.components.ButtonSizeType
import com.allenljf.aicompanion.ui.components.ButtonState
import com.allenljf.aicompanion.ui.components.ButtonType
import com.allenljf.aicompanion.ui.components.DragHandle
import com.allenljf.aicompanion.viewmodel.AiCompanionViewModel
import com.allenljf.aicompanion.viewmodel.CompanionCreationState
import com.allenljf.aicompanion.viewmodel.IntroductionState
import com.allenljf.aicompanion.viewmodel.PartnerState
import com.allenljf.aicompanion.viewmodel.QuizGalleryState
import com.allenljf.aicompanion.viewmodel.QuizState
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import aicompanion.shared.generated.resources.Res
import aicompanion.shared.generated.resources.ic_arrow_down_line
import aicompanion.shared.generated.resources.ic_arrow_left_line
import aicompanion.shared.generated.resources.ic_arrow_right_line
import aicompanion.shared.generated.resources.ic_check_circle_fill
import aicompanion.shared.generated.resources.ic_dots_three_line_semibold
import aicompanion.shared.generated.resources.ic_download_line
import aicompanion.shared.generated.resources.ic_gender_male_line
import aicompanion.shared.generated.resources.ic_id_card_line
import aicompanion.shared.generated.resources.ic_intersect_square_line
import aicompanion.shared.generated.resources.ic_lock_line
import aicompanion.shared.generated.resources.ic_message_line
import aicompanion.shared.generated.resources.ic_neutral_face_line
import aicompanion.shared.generated.resources.ic_note_line
import aicompanion.shared.generated.resources.ic_people_line
import aicompanion.shared.generated.resources.ic_reload_line_semibold
import aicompanion.shared.generated.resources.ic_road_map_line
import aicompanion.shared.generated.resources.ic_share_android_line
import aicompanion.shared.generated.resources.ic_sparkles_fill
import aicompanion.shared.generated.resources.ic_suitcase_line

// ---------- ①-a 建立旅伴（外觀＋個性）----------

// Phase 1 mockup 採用版順序：先定個性（靈魂）→ 打造外觀 → 產圖預覽（含命名）→ 召喚
private const val STEP_PERSONALITY = 0
private const val STEP_APPEARANCE = 1
private const val STEP_PREVIEW = 2

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun CreateCompanionScreen(
    viewModel: AiCompanionViewModel,
    onCreated: () -> Unit,
) {
    val partner by viewModel.partnerState.collectAsStateWithLifecycle()
    val creation by viewModel.creationState.collectAsStateWithLifecycle()
    var currentStep by remember { mutableIntStateOf(STEP_PERSONALITY) }

    val p = partner
    if (p is PartnerState.Loaded) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .testTag("companion_create_screen"),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Tokens.colorWhite)
                    .statusBarsPadding(),
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = "打造你的專屬旅伴", // TODO: i18n - 建立旅伴標題
                            style = ScreenTitleStyle,
                        )
                    },
                    navigationIcon = {
                        // 第一步沒有上一頁可回（獨立 app 的進入點），改留等寬空位讓標題位置不隨步驟跳動
                        if (currentStep == STEP_PERSONALITY) {
                            Spacer(Modifier.size(48.dp))
                        } else {
                            IconButton(
                                onClick = {
                                    if (currentStep == STEP_PREVIEW) {
                                        currentStep = STEP_APPEARANCE
                                    } else {
                                        currentStep = STEP_PERSONALITY
                                    }
                                },
                                modifier = Modifier.testTag("companion_back_btn"),
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_arrow_left_line),
                                    contentDescription = null,
                                    tint = Tokens.colorTextDarker,
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(Tokens.colorWhite),
                )
                Box(Modifier.padding(horizontal = Tokens.spacing200)) {
                    StepIndicator(currentStep)
                }
                Spacer(Modifier.height(Tokens.spacing150))
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Tokens.colorBackgroundSurfaceLight)
                    .verticalScroll(rememberScrollState())
                    .padding(Tokens.spacing300),
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
                    .background(Tokens.colorWhite)
                    .navigationBarsPadding()
                    .padding(Tokens.spacing200),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                when (currentStep) {
                    STEP_PERSONALITY -> {
                        val isPersonalityComplete =
                            creation.selectedPersonality.isNotEmpty() && creation.selectedSpeechStyle != null
                        Box(Modifier.fillMaxWidth().testTag("companion_personality_next_btn")) {
                            AppButton(
                                buttonText = "下一步：打造外觀", // TODO: i18n
                                buttonType = ButtonType.PRIMARY,
                                buttonState = if (isPersonalityComplete) ButtonState.ENABLED else ButtonState.DISABLED,
                                buttonSizeType = ButtonSizeType.Md,
                                onClick = { currentStep = STEP_APPEARANCE },
                                isFullWidth = true,
                            )
                        }
                        Spacer(Modifier.height(Tokens.spacing050))
                        Text(
                            "先定個性，再幫它畫一張臉", // TODO: i18n
                            color = Tokens.colorTextMedium,
                            fontSize = Tokens.fontSize2,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                    }
                    STEP_APPEARANCE -> {
                        Box(Modifier.fillMaxWidth().testTag("companion_next_btn")) {
                            AppButton(
                                buttonText = "看看它的樣子", // TODO: i18n
                                buttonType = ButtonType.PRIMARY,
                                buttonState = if (creation.isAppearanceComplete) ButtonState.ENABLED else ButtonState.DISABLED,
                                buttonSizeType = ButtonSizeType.Md,
                                onClick = { currentStep = STEP_PREVIEW },
                                isFullWidth = true,
                            )
                        }
                        Spacer(Modifier.height(Tokens.spacing200))
                        Text(
                            "不知道怎麼選？", // TODO: i18n
                            color = Tokens.colorTextMedium,
                            fontSize = Tokens.fontSize3,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(Tokens.spacing100))
                        Box(Modifier.testTag("companion_random_btn")) {
                            AppButton(
                                buttonText = "隨機配一個", // TODO: i18n
                                buttonType = ButtonType.PRIMARY_SUBTLE,
                                buttonState = ButtonState.ENABLED,
                                buttonSizeType = ButtonSizeType.Sm,
                                onClick = {
                                    // 選項已由 ai-partner API 取回，本地亂數選好四個外觀維度後直接進外觀預覽
                                    viewModel.randomizeAppearance()
                                    currentStep = STEP_PREVIEW
                                },
                                leadingIcon = painterResource(Res.drawable.ic_intersect_square_line),
                            )
                        }
                    }
                    STEP_PREVIEW -> {
                        Box(Modifier.fillMaxWidth().testTag("companion_create_btn")) {
                            AppButton(
                                buttonText = "召喚我的旅伴", // TODO: i18n
                                buttonType = ButtonType.PRIMARY,
                                buttonState = if (creation.isAllDimensionsSelected) ButtonState.ENABLED else ButtonState.DISABLED,
                                buttonSizeType = ButtonSizeType.Md,
                                onClick = onCreated,
                                isFullWidth = true,
                            )
                        }
                        Spacer(Modifier.height(Tokens.spacing050))
                        Text(
                            "✦ 結合外觀＋個性，召喚會說話的旅伴", // TODO: i18n
                            color = Tokens.colorTextMedium,
                            fontSize = Tokens.fontSize2,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    } else {
        ScreenScaffold(title = "打造你的專屬旅伴", screenTag = "companion_create_screen") { // TODO: i18n - 建立旅伴標題
            // ScreenScaffold 的外層 Column 套用 verticalScroll，會把高度限制變成無限，Modifier.fillMaxSize()
            // 在此情境下對高度沒有效果（無額外空間可置中）。改用 heightIn(min = ...) 強制保留至少一個螢幕高度
            // （扣除標題列估計高度）的空間，讓 contentAlignment = Center 能真正把內容置中，而非貼齊左上角。
            // 原用 LocalConfiguration.current.screenHeightDp（Android-only，KMP commonMain 未提供），
            // 改用跨平台可用的 LocalWindowInfo.containerSize 換算 dp 高度
            val density = LocalDensity.current
            val windowHeightDp = with(density) { LocalWindowInfo.current.containerSize.height.toDp() }
            val minContentHeight = windowHeightDp - COMPANION_CREATE_HEADER_HEIGHT
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
                            "目前無法載入旅伴設定，請稍後再試", // TODO: i18n - 空狀態
                            modifier = Modifier.testTag("companion_create_empty"),
                        )
                    is PartnerState.Error ->
                        Text(
                            "載入失敗，請稍後再試", // TODO: i18n - 錯誤狀態
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
    val steps = listOf("1. 個性", "2. 外觀") // TODO: i18n
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
                        .height(Tokens.spacing050)
                        .clip(RoundedCornerShape(Tokens.radiusXs)),
                    color = Tokens.colorBackgroundPrimaryMedium,
                    trackColor = Tokens.colorBorderLight,
                    strokeCap = StrokeCap.Round,
                )
                Spacer(Modifier.height(Tokens.spacing050))
                Text(
                    text = label,
                    fontSize = Tokens.fontSize2,
                    fontWeight = if (index == visualStep) FontWeight(Tokens.fontWeightMediumAndroid) else FontWeight.Normal,
                    color = if (isActive) Tokens.colorTextPrimaryDark else Tokens.colorTextMedium,
                )
            }
            if (index < steps.lastIndex) Spacer(Modifier.width(Tokens.spacing100))
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

    Spacer(Modifier.height(Tokens.spacing200))

    Text(
        "選好以下四項外觀，就能看看它的樣子", // TODO: i18n
        fontSize = Tokens.fontSize3,
        color = Tokens.colorTextMedium,
    )
    Spacer(Modifier.height(Tokens.spacing200))
    Text(
        "打造外觀", // TODO: i18n
        fontWeight = FontWeight(Tokens.fontWeightBold),
        fontSize = Tokens.fontSize4,
        color = Tokens.colorTextDarker,
    )
    Spacer(Modifier.height(Tokens.spacing200))

    DimensionCard(title = "性別", testTag = "companion_dim_gender", icon = painterResource(Res.drawable.ic_gender_male_line)) { // TODO: i18n
        AppearancePills(partner.gender, creation.selectedGender, viewModel::selectGender, "gender")
    }
    DimensionCard(title = "髮型", testTag = "companion_dim_hair_style", icon = painterResource(Res.drawable.ic_neutral_face_line)) { // TODO: i18n
        AppearancePills(partner.hairStyle, creation.selectedHairStyle, viewModel::selectHairStyle, "hair_style")
    }
    DimensionCard(title = "髮色", testTag = "companion_dim_hair_color", icon = painterResource(Res.drawable.ic_people_line)) { // TODO: i18n
        AppearancePills(partner.hairColor, creation.selectedHairColor, viewModel::selectHairColor, "hair_color")
    }
    DimensionCard(title = "服裝", testTag = "companion_dim_outfit", icon = painterResource(Res.drawable.ic_suitcase_line)) { // TODO: i18n
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
            .clip(RoundedCornerShape(Tokens.radiusLg))
            .border(2.dp, Tokens.colorBackgroundPrimaryMedium, RoundedCornerShape(Tokens.radiusLg))
            .background(Tokens.colorBackgroundSurfaceLight)
            .testTag("companion_preview_image"),
        placeholder = {
            Text(
                "旅伴頭像預覽", // TODO: i18n
                color = Tokens.colorTextMedium,
                fontSize = Tokens.fontSize6,
            )
        },
    )

    Spacer(Modifier.height(Tokens.spacing300))

    Text(
        "喜歡這個樣子嗎？", // TODO: i18n
        fontWeight = FontWeight(Tokens.fontWeightBold),
        fontSize = Tokens.fontSize5,
        color = Tokens.colorTextDarker,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
    )

    Spacer(Modifier.height(Tokens.spacing150))

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Tokens.spacing100, Alignment.CenterHorizontally),
    ) {
        selectedTags.forEach { tag ->
            SelectablePill(label = tag, isSelected = true, enabled = false, testTag = "preview_tag_$tag", onClick = {})
        }
    }

    Spacer(Modifier.height(Tokens.spacing300))

    // 產完外觀圖才命名（mockup 採用版：命名放在產圖之後）
    DimensionCard(title = "幫它取個名字", badge = "必填", testTag = "companion_dim_name", icon = painterResource(Res.drawable.ic_id_card_line)) { // TODO: i18n
        val nameState = remember { mutableStateOf(creation.companionName) }
        LaunchedEffect(creation.companionName) {
            if (nameState.value != creation.companionName) {
                nameState.value = creation.companionName
            }
        }
        Box(modifier = Modifier.testTag("companion_name_input")) {
            AppTextField(
                text = nameState,
                placeholder = "例：迷路也不怕", // TODO: i18n
                textLengthLimit = CompanionCreationState.NAME_MAX_LENGTH,
                isNextAction = false,
                textListener = viewModel::updateCompanionName,
            )
        }
        Spacer(Modifier.height(Tokens.spacing050))
        Text(
            "${creation.companionName.length} / ${CompanionCreationState.NAME_MAX_LENGTH}",
            fontSize = Tokens.fontSize2,
            color = if (creation.isNameValid) Tokens.colorTextMedium
            else Tokens.colorTextCriticalDark,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End,
        )
    }

    Box(modifier = Modifier.fillMaxWidth().testTag("companion_reselect_btn")) {
        AppButton(
            buttonText = "換個樣子？回上一步重選外觀", // TODO: i18n
            buttonType = ButtonType.PRIMARY_SUBTLE,
            buttonState = ButtonState.ENABLED,
            buttonSizeType = ButtonSizeType.Md,
            onClick = onReselect,
            leadingIcon = painterResource(Res.drawable.ic_reload_line_semibold),
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
        "先決定它的靈魂", // TODO: i18n
        fontWeight = FontWeight(Tokens.fontWeightBold),
        fontSize = Tokens.fontSize5,
        color = Tokens.colorTextDarker,
    )
    Spacer(Modifier.height(Tokens.spacing050))
    Text(
        "選說話風格與個性，等等再幫它打造外觀", // TODO: i18n
        fontSize = Tokens.fontSize3,
        color = Tokens.colorTextMedium,
    )
    Spacer(Modifier.height(Tokens.spacing200))

    DimensionCard(title = "個性", badge = "必選", testTag = "companion_dim_personality", icon = painterResource(Res.drawable.ic_sparkles_fill)) { // TODO: i18n
        PersonalityPills(partner.personality, creation.selectedPersonality, viewModel::togglePersonality)
    }

    DimensionCard(title = "說話風格", badge = "必選", testTag = "companion_dim_speech_style", icon = painterResource(Res.drawable.ic_sparkles_fill)) { // TODO: i18n
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
            .clip(RoundedCornerShape(Tokens.radiusLg))
            .border(1.dp, Tokens.colorBorderLight, RoundedCornerShape(Tokens.radiusLg))
            .background(Tokens.colorWhite)
            .padding(horizontal = Tokens.spacing150, vertical = Tokens.spacing100),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Tokens.colorBackgroundPrimaryLighter),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_sparkles_fill),
                contentDescription = null,
                tint = Tokens.colorTextPrimaryDark,
                modifier = Modifier.size(Tokens.dimensionIconSm),
            )
        }
        Spacer(Modifier.width(Tokens.spacing150))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(Res.drawable.ic_check_circle_fill),
                    contentDescription = null,
                    tint = Tokens.colorTextPrimaryDark,
                    modifier = Modifier.size(Tokens.dimensionIcon2xs),
                )
                Spacer(Modifier.width(Tokens.spacing050))
                Text(
                    "個性完成", // TODO: i18n
                    fontWeight = FontWeight(Tokens.fontWeightMediumAndroid),
                    color = Tokens.colorTextPrimaryDark,
                    fontSize = Tokens.fontSize2,
                )
            }
            if (summary.isNotBlank()) {
                Spacer(Modifier.height(Tokens.spacing025))
                Text(
                    summary,
                    color = Tokens.colorTextMedium,
                    fontSize = Tokens.fontSize2,
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
    icon: Painter? = null,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .padding(bottom = Tokens.spacing150)
            .border(1.dp, Tokens.colorBorderLight, RoundedCornerShape(Tokens.radiusLg))
            .clip(RoundedCornerShape(Tokens.radiusLg))
            .background(Tokens.colorWhite)
            .padding(Tokens.spacing200),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(
                        painter = icon,
                        contentDescription = null,
                        modifier = Modifier.size(Tokens.dimensionIconSm),
                        tint = Tokens.colorTextMedium,
                    )
                    Spacer(Modifier.width(Tokens.spacing075))
                }
                Text(
                    text = title,
                    fontWeight = FontWeight(Tokens.fontWeightMediumAndroid),
                    color = Tokens.colorTextDarker,
                )
                if (badge != null) {
                    Spacer(Modifier.width(Tokens.spacing075))
                    Text(
                        text = badge,
                        fontSize = Tokens.fontSize1,
                        fontWeight = FontWeight(Tokens.fontWeightMediumAndroid),
                        color = Tokens.colorTextCriticalDark, // TODO: 確認 badge 顏色 token
                    )
                }
            }
            Spacer(Modifier.height(Tokens.spacing100))
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
        horizontalArrangement = Arrangement.spacedBy(Tokens.spacing100),
        verticalArrangement = Arrangement.spacedBy(Tokens.spacing100),
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
        horizontalArrangement = Arrangement.spacedBy(Tokens.spacing100),
        verticalArrangement = Arrangement.spacedBy(Tokens.spacing100),
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
        horizontalArrangement = Arrangement.spacedBy(Tokens.spacing100),
        verticalArrangement = Arrangement.spacedBy(Tokens.spacing100),
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
internal fun SelectablePill(
    label: String,
    isSelected: Boolean,
    enabled: Boolean,
    testTag: String,
    onClick: () -> Unit,
    fontSize: TextUnit = TextUnit.Unspecified,
) {
    val bgColor = when {
        isSelected -> Tokens.colorBackgroundPrimaryLighter
        !enabled -> Tokens.colorBackgroundSurfaceLight
        else -> Tokens.colorWhite
    }
    val textColor = when {
        isSelected -> Tokens.colorTextPrimaryDark
        !enabled -> Tokens.colorTextMedium
        else -> Tokens.colorTextDark
    }
    val borderColor = if (isSelected) Tokens.colorBackgroundPrimaryMedium
    else Tokens.colorBorderLight

    Text(
        text = label,
        color = textColor,
        fontSize = fontSize,
        modifier = Modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(Tokens.radiusXl))
            .then(if (enabled) Modifier.clickable { onClick() } else Modifier)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(Tokens.radiusXl),
            )
            .background(bgColor, RoundedCornerShape(Tokens.radiusXl))
            .padding(
                horizontal = Tokens.spacing200,
                vertical = Tokens.spacing100,
            ),
    )
}

// ---------- ①-b 旅伴誕生 / 主頁 ----------

@Composable
internal fun CompanionBornScreen(
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Tokens.colorWhite)
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
                    .background(Tokens.colorBackgroundPrimaryLighter),
                contentAlignment = Alignment.Center,
            ) {
                CompanionAsyncImage(
                    url = creation.avatarUrl,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("companion_born_avatar"),
                    placeholder = {
                        Text(
                            "旅伴頭像", // TODO: i18n
                            color = Tokens.colorTextPrimaryDark,
                            fontSize = Tokens.fontSize6,
                        )
                    },
                )
                // 分享 icon：目前無點擊行為（原始碼即為裝飾性，未接分享流程）
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(Tokens.spacing200)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_share_android_line),
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
                    .clip(RoundedCornerShape(topStart = Tokens.radiusXl, topEnd = Tokens.radiusXl))
                    .background(Tokens.colorWhite)
                    .padding(horizontal = Tokens.spacing300),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                DragHandle()
                Spacer(Modifier.height(Tokens.spacing150))
                Text(
                    "你的專屬旅伴誕生了", // TODO: i18n
                    color = Tokens.colorTextMedium,
                    fontSize = Tokens.fontSize3,
                )
                Spacer(Modifier.height(Tokens.spacing100))
                Text(
                    creation.companionName.ifBlank { "旅伴" }, // TODO: i18n fallback
                    fontWeight = FontWeight(Tokens.fontWeightBold),
                    fontSize = Tokens.fontSize8,
                    color = Tokens.colorTextDarker,
                    modifier = Modifier.testTag("companion_born_name"),
                )
                Spacer(Modifier.height(Tokens.spacing150))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Tokens.spacing100, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(Tokens.spacing100),
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
                Spacer(Modifier.height(Tokens.spacing200))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Tokens.radiusLg))
                        .background(Tokens.colorBackgroundSurfaceLight)
                        .padding(Tokens.spacing200),
                ) {
                    Column {
                        Text(
                            "“",
                            color = Tokens.colorTextPrimaryDark,
                            fontSize = Tokens.fontSize9,
                            lineHeight = Tokens.fontSize9,
                            fontWeight = FontWeight(Tokens.fontWeightBold),
                        )
                        Spacer(Modifier.height(Tokens.spacing050))
                        val companionName = creation.companionName.ifBlank { "旅伴" } // TODO: i18n fallback
                        val introductionText = when (introduction) {
                            is IntroductionState.Loaded -> introduction.introduction
                            is IntroductionState.Loading -> "${companionName}正在想怎麼介紹自己⋯" // TODO: i18n
                            // Idle / Error（硬失敗）→ 本地固定文案 fallback，不擋流程
                            else -> "嗨！我是$companionName，一個喜歡隨性探索、享受旅途驚喜的旅伴。我們會一起發現那些地圖上找不到的角落，用你自己的步調感受每一個城市的溫度。準備好了嗎？" // TODO: i18n
                        }
                        Text(
                            introductionText,
                            color = Tokens.colorTextDark,
                            fontSize = Tokens.fontSize3,
                            modifier = Modifier.testTag("companion_born_quote"),
                        )
                    }
                }
                Spacer(Modifier.height(Tokens.spacing300))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Tokens.colorWhite)
                .navigationBarsPadding()
                .padding(horizontal = Tokens.spacing300, vertical = Tokens.spacing200),
            horizontalArrangement = Arrangement.spacedBy(Tokens.spacing150),
        ) {
            Box(Modifier.weight(3f).testTag("companion_born_home_btn")) {
                AppButton(
                    buttonText = "進入主頁", // TODO: i18n
                    buttonType = ButtonType.PRIMARY,
                    buttonState = ButtonState.ENABLED,
                    buttonSizeType = ButtonSizeType.Md,
                    onClick = onGoHome,
                    isFullWidth = true,
                )
            }
            Box(Modifier.weight(2f).testTag("companion_born_recreate_btn")) {
                AppButton(
                    buttonText = "重新打造", // TODO: i18n
                    buttonType = ButtonType.PRIMARY_SUBTLE,
                    buttonState = ButtonState.ENABLED,
                    buttonSizeType = ButtonSizeType.Md,
                    onClick = onRecreate,
                    isFullWidth = true,
                    leadingIcon = painterResource(Res.drawable.ic_reload_line_semibold),
                )
            }
        }
    }
}

/** 旅伴主頁意圖卡：圓角方塊 icon + 標題/副標 + chevron；comingSoon 時右側鎖頭、標題旁「即將推出」chip、不可點擊。 */
@Composable
private fun HomeIntentCard(
    iconRes: Painter,
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
            .clip(RoundedCornerShape(Tokens.radiusXl))
            .border(1.dp, Tokens.colorBorderLight, RoundedCornerShape(Tokens.radiusXl))
            .background(Tokens.colorWhite)
            .then(if (onClick != null && !comingSoon) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(Tokens.spacing200),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(Tokens.radiusLg))
                    .background(iconBackground)
                    .alpha(if (comingSoon) 0.55f else 1f),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = iconRes,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(Tokens.dimensionIconMd),
                )
            }
            Spacer(Modifier.width(Tokens.spacing150))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .alpha(if (comingSoon) 0.72f else 1f),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        title,
                        fontWeight = FontWeight(Tokens.fontWeightMediumAndroid),
                        color = Tokens.colorTextDarker,
                        fontSize = Tokens.fontSize3,
                    )
                    if (comingSoon) {
                        Spacer(Modifier.width(Tokens.spacing075))
                        Text(
                            "即將推出", // TODO: i18n
                            color = Tokens.colorWhite,
                            fontSize = Tokens.fontSize1,
                            fontWeight = FontWeight(Tokens.fontWeightMediumAndroid),
                            modifier = Modifier
                                .clip(RoundedCornerShape(Tokens.radiusSm))
                                .background(Tokens.colorBackgroundCriticalMedium)
                                .padding(horizontal = Tokens.spacing075, vertical = Tokens.spacing025),
                        )
                    }
                }
                Spacer(Modifier.height(Tokens.spacing050))
                Text(
                    subtitle,
                    color = Tokens.colorTextMedium,
                    fontSize = Tokens.fontSize2,
                )
            }
            Icon(
                painter = painterResource(
                    if (comingSoon) Res.drawable.ic_lock_line
                    else Res.drawable.ic_arrow_right_line,
                ),
                contentDescription = null,
                tint = Tokens.colorTextMedium,
                modifier = Modifier.size(Tokens.dimensionIconSm),
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
            .clip(RoundedCornerShape(Tokens.radiusLg))
            .border(1.dp, Tokens.colorBorderLight, RoundedCornerShape(Tokens.radiusLg))
            .background(Tokens.colorWhite)
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
                            Tokens.colorBackgroundPrimaryMedium,
                            Tokens.colorBackgroundPrimaryDarker,
                        ),
                    ),
                ),
        ) {
            Text(
                "共 ${trip.totalDays} 天", // TODO: i18n
                fontSize = Tokens.fontSize1,
                fontWeight = FontWeight(Tokens.fontWeightBold),
                color = Tokens.colorWhite,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(Tokens.spacing075)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(horizontal = Tokens.spacing075, vertical = Tokens.spacing025),
            )
        }
        Column(modifier = Modifier.padding(Tokens.spacing150)) {
            Text(
                trip.title,
                fontWeight = FontWeight(Tokens.fontWeightBold),
                fontSize = Tokens.fontSize2,
                color = Tokens.colorTextDarker,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(Tokens.spacing025))
            Text(
                "${trip.city}・一起排的", // TODO: i18n
                fontSize = Tokens.fontSize1,
                color = Tokens.colorTextMedium,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun CompanionHomeScreen(
    creation: CompanionCreationState,
    introduction: String,
    onTravelDna: () -> Unit,
    onHistory: () -> Unit,
    onQuizGallery: () -> Unit,
    onRecreate: () -> Unit,
    onImportItinerary: () -> Unit = {},
    onPlanTrip: () -> Unit = {},
    onTripList: () -> Unit = {},
    savedTrips: List<SavedTripRecord> = emptyList(),
    onOpenSavedTrip: (SavedTripRecord) -> Unit = {},
) {
    val companionName = creation.companionName.ifBlank { "旅伴" } // TODO: i18n fallback
    var showProfileSheet by remember { mutableStateOf(false) }
    val personaTags = creation.selectedPersonality.map { it.label.ifBlank { it.tag } } +
        listOfNotNull(creation.selectedSpeechStyle?.label?.ifBlank { creation.selectedSpeechStyle?.tag }?.takeIf { it.isNotBlank() })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("companion_home_screen")
            .background(Tokens.colorBackgroundSurfaceLight)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding(),
    ) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = Tokens.spacing200, vertical = Tokens.spacing150),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 獨立 app 的首頁沒有上一頁可回，留等寬空位讓標題維持置中
            Spacer(Modifier.size(48.dp))
            Spacer(Modifier.weight(1f))
            Text(
                "我的 AI 旅伴", // TODO: i18n
                fontWeight = FontWeight(Tokens.fontWeightMediumAndroid),
                fontSize = Tokens.fontSize4,
                color = Tokens.colorTextDarker,
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
                        painter = painterResource(Res.drawable.ic_dots_three_line_semibold),
                        contentDescription = null,
                        tint = Tokens.colorTextDarker,
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
                                "重新打造旅伴", // TODO: i18n
                                fontSize = Tokens.fontSize3,
                                color = Tokens.colorTextPrimaryDark,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(Res.drawable.ic_reload_line_semibold),
                                contentDescription = null,
                                tint = Tokens.colorTextPrimaryDark,
                                modifier = Modifier.size(Tokens.dimensionIconSm),
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
                    .border(3.dp, Tokens.colorWhite, CircleShape)
                    .background(Tokens.colorBackgroundPrimaryLighter)
                    .clickable { showProfileSheet = true }
                    .testTag("companion_home_avatar"),
                placeholder = {
                    Text("?", color = Tokens.colorTextPrimaryDark, fontSize = Tokens.fontSize9)
                },
            )
            Spacer(Modifier.height(Tokens.spacing150))
            Text(
                companionName,
                fontWeight = FontWeight(Tokens.fontWeightBold),
                fontSize = Tokens.fontSize5,
                color = Tokens.colorTextDarker,
                modifier = Modifier.testTag("companion_home_name"),
            )
            if (personaTags.isNotEmpty()) {
                Spacer(Modifier.height(Tokens.spacing100))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Tokens.spacing100, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(Tokens.spacing100),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Tokens.spacing300)
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

        Spacer(Modifier.height(Tokens.spacing300))

        Column(modifier = Modifier.padding(horizontal = Tokens.spacing300)) {
            // 理解度卡片（demo 暫時隱藏，先不刪程式碼，之後要恢復顯示把這個 if(false) 拿掉即可）
            if (false) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Tokens.colorBorderLight, RoundedCornerShape(Tokens.radiusLg))
                    .clip(RoundedCornerShape(Tokens.radiusLg))
                    .background(Tokens.colorWhite)
                    .padding(Tokens.spacing200),
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "${companionName}對你的理解度", // TODO: i18n
                                fontWeight = FontWeight(Tokens.fontWeightMediumAndroid),
                                color = Tokens.colorTextDarker,
                                fontSize = Tokens.fontSize3,
                            )
                            Spacer(Modifier.width(Tokens.spacing075))
                            AppTag(
                                text = "即將推出", // TODO: i18n
                                tagColor = AppTagSolidColor.RED,
                            )
                        }
                        Text(
                            "-%", // TODO: replace with API data
                            color = Tokens.colorTextPrimaryDark,
                            fontWeight = FontWeight(Tokens.fontWeightMediumAndroid),
                            fontSize = Tokens.fontSize3,
                        )
                    }
                    Spacer(Modifier.height(Tokens.spacing100))
                    LinearProgressIndicator(
                        progress = { 0f }, // TODO: replace with API data
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Tokens.colorBackgroundPrimaryMedium,
                        trackColor = Tokens.colorBackgroundSurfaceMedium,
                        strokeCap = StrokeCap.Round,
                    )
                    Spacer(Modifier.height(Tokens.spacing100))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_sparkles_fill),
                            contentDescription = null,
                            tint = Tokens.colorTextPrimaryDark,
                            modifier = Modifier.size(Tokens.dimensionIconXs),
                        )
                        Spacer(Modifier.width(Tokens.spacing050))
                        Text(
                            "玩測驗、日後多聊天，${companionName}更懂你", // TODO: i18n
                            color = Tokens.colorTextMedium,
                            fontSize = Tokens.fontSize1,
                        )
                    }
                }
            }
            } // if (false) 理解度卡片

            Spacer(Modifier.height(Tokens.spacing300))

            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Tokens.colorTextDarker)) {
                        append("想跟") // TODO: i18n
                    }
                    withStyle(SpanStyle(color = Tokens.colorTextPrimaryDark)) {
                        append(companionName)
                    }
                    withStyle(SpanStyle(color = Tokens.colorTextDarker)) {
                        append("一起做什麼呢？")
                    }
                },
                fontWeight = FontWeight(Tokens.fontWeightBold),
                fontSize = Tokens.fontSize5,
            )

            // 我的旅程：本地儲存的行程清單（後端不儲存，最新在前、橫向捲動），放標題正下方
            if (savedTrips.isNotEmpty()) {
                Spacer(Modifier.height(Tokens.spacing150))
                Text(
                    "我的旅程", // TODO: i18n
                    fontWeight = FontWeight(Tokens.fontWeightBold),
                    fontSize = Tokens.fontSize3,
                    color = Tokens.colorTextDarker,
                )
                Spacer(Modifier.height(Tokens.spacing100))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Tokens.spacing150),
                ) {
                    savedTrips.forEach { trip ->
                        SavedTripCard(trip = trip, onClick = { onOpenSavedTrip(trip) })
                    }
                }
            }

            Spacer(Modifier.height(Tokens.spacing200))

            // 五張意圖卡（依 Phase 1 mockup 旅伴主頁定案順序）
            HomeIntentCard(
                iconRes = painterResource(Res.drawable.ic_sparkles_fill),
                iconTint = Tokens.colorTextPrimaryDark,
                iconBackground = Tokens.colorBackgroundPrimaryLighter,
                title = "找到我的旅行 DNA 及命定旅程", // TODO: i18n
                subtitle = "玩個測驗，發現你的旅行性格與最適合的目的地", // TODO: i18n
                testTag = "companion_home_quiz_btn",
                onClick = onTravelDna,
            )

            Spacer(Modifier.height(Tokens.spacing150))

            HomeIntentCard(
                iconRes = painterResource(Res.drawable.ic_download_line),
                iconTint = Tokens.colorTextPrimaryDark,
                iconBackground = Tokens.colorBackgroundPrimaryLighter,
                title = "匯入你的 AI 行程", // TODO: i18n
                subtitle = "把 ChatGPT／其他 AI 排好的貼給我，或傳截圖，我幫你對上可訂體驗", // TODO: i18n
                testTag = "companion_home_import_btn",
                onClick = onImportItinerary,
            )

            Spacer(Modifier.height(Tokens.spacing150))

            HomeIntentCard(
                iconRes = painterResource(Res.drawable.ic_message_line),
                iconTint = Tokens.colorBackgroundHighlightDarker,
                iconBackground = Tokens.colorBackgroundHighlightLighter,
                title = "社群旅伴貼文", // TODO: i18n
                subtitle = "看別人的旅伴與命定城市，逆向找旅行靈感", // TODO: i18n
                testTag = "companion_home_quiz_gallery_btn",
                onClick = onQuizGallery,
            )

            Spacer(Modifier.height(Tokens.spacing150))

            HomeIntentCard(
                iconRes = painterResource(Res.drawable.ic_note_line),
                iconTint = Tokens.colorTextPrimaryDark,
                iconBackground = Tokens.colorBackgroundPrimaryLighter,
                title = "回顧我的旅行 DNA", // TODO: i18n
                subtitle = "看過去的測驗結果，隨時再分享", // TODO: i18n
                testTag = "companion_home_history_btn",
                onClick = onHistory,
            )

            Spacer(Modifier.height(Tokens.spacing150))

            HomeIntentCard(
                iconRes = painterResource(Res.drawable.ic_road_map_line),
                iconTint = Tokens.colorTextPrimaryDark,
                iconBackground = Tokens.colorBackgroundPrimaryLighter,
                title = "一起規劃旅遊行程", // TODO: i18n
                subtitle = "還沒有想法？沒關係，從頭聊，一步步排出來", // TODO: i18n
                testTag = "companion_home_plan_btn",
                onClick = onPlanTrip,
            )
        }

        Spacer(Modifier.weight(1f))

        // 底部聊天 bar（即將推出，demo 暫時隱藏，先不刪程式碼，之後要恢復顯示把這個 if(false) 拿掉即可）
        if (false) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Tokens.spacing200),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Tokens.radiusXl))
                    .border(1.dp, Tokens.colorBorderLight, RoundedCornerShape(Tokens.radiusXl))
                    .background(Tokens.colorWhite)
                    .padding(horizontal = Tokens.spacing200, vertical = Tokens.spacing150),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_message_line),
                    contentDescription = null,
                    tint = Tokens.colorTextMedium,
                    modifier = Modifier.size(Tokens.dimensionIconMd),
                )
                Spacer(Modifier.width(Tokens.spacing100))
                Text(
                    "直接和${companionName}聊聊", // TODO: i18n
                    color = Tokens.colorTextMedium,
                    fontSize = Tokens.fontSize3,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "即將推出", // TODO: i18n
                    color = Tokens.colorWhite,
                    fontSize = Tokens.fontSize1,
                    fontWeight = FontWeight(Tokens.fontWeightMediumAndroid),
                    modifier = Modifier
                        .clip(RoundedCornerShape(Tokens.radiusSm))
                        .background(Tokens.colorBackgroundCriticalMedium)
                        .padding(horizontal = Tokens.spacing075, vertical = Tokens.spacing025),
                )
            }
        }
        } // if (false) 底部聊天 bar

        // 首頁底部留白，避免最後一個區塊貼齊畫面底緣
        Spacer(Modifier.height(Tokens.spacing200))
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
            topStart = Tokens.radiusXl,
            topEnd = Tokens.radiusXl,
        ),
        dragHandle = { DragHandle() },
        scrimColor = Color.Black.copy(alpha = 0.5f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Tokens.spacing400)
                .testTag("companion_profile_sheet"),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 滿版方形大圖：左右各留 16dp、1:1 正方形圓角，寬度撐滿後高度等比放大
            CompanionAsyncImage(
                url = avatarUrl,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Tokens.spacing200)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(Tokens.radiusLg))
                    .testTag("companion_profile_sheet_avatar"),
                placeholder = {
                    Text("?", color = Tokens.colorTextPrimaryDark, fontSize = Tokens.fontSize9)
                },
            )
            Spacer(Modifier.height(Tokens.spacing150))
            Text(
                companionName,
                fontWeight = FontWeight(Tokens.fontWeightBold),
                fontSize = Tokens.fontSize5,
                color = Tokens.colorTextDarker,
            )
            if (personaTags.isNotEmpty()) {
                Spacer(Modifier.height(Tokens.spacing150))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Tokens.spacing100, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(Tokens.spacing100),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Tokens.spacing300),
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
                Spacer(Modifier.height(Tokens.spacing200))
                Text(
                    introduction,
                    color = Tokens.colorTextDarker,
                    fontSize = Tokens.fontSize3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Tokens.spacing300)
                        .clip(RoundedCornerShape(Tokens.radiusLg))
                        .border(1.dp, Tokens.colorBorderLight, RoundedCornerShape(Tokens.radiusLg))
                        .background(Tokens.colorBackgroundSurfaceLight)
                        .padding(Tokens.spacing200)
                        .testTag("companion_profile_sheet_introduction"),
                )
            }
        }
    }
}

// ---------- ② 查看社群（QuizGallery，資料來自 GetQuizGalleryUseCase 假資料）----------

/**
 * 查看社群：無資料庫下「其他人做過的測驗結果」清單，兩欄瀑布流呈現，不重用 [ScreenScaffold]
 * ——其 verticalScroll 會讓 LazyVerticalStaggeredGrid 撞上無限高度約束，故自建不含 verticalScroll 的頂層容器。
 */
@Composable
internal fun QuizGalleryScreen(
    state: QuizGalleryState,
    onBack: () -> Unit,
    onItemClick: (QuizGalleryItem) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("companion_quiz_gallery_screen")
            .background(Tokens.colorBackgroundSurfaceLight),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(Tokens.spacing300),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("companion_quiz_gallery_back_btn")) {
                Icon(
                    painter = painterResource(Res.drawable.ic_arrow_left_line),
                    contentDescription = null,
                    tint = Tokens.colorTextDarker,
                )
            }
            Spacer(Modifier.width(Tokens.spacing100))
            Text("查看社群", style = ScreenTitleStyle) // TODO: i18n - 查看社群標題
        }

        when (state) {
            is QuizGalleryState.Idle, QuizGalleryState.Loading ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.testTag("companion_quiz_gallery_loading"))
                }

            is QuizGalleryState.Error ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "載入失敗，請稍後再試", // TODO: i18n
                        color = Tokens.colorTextMedium,
                        modifier = Modifier.testTag("companion_quiz_gallery_error"),
                    )
                }

            is QuizGalleryState.Loaded -> {
                val items = state.result.items
                if (items.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "目前還沒有人分享測驗結果，成為第一個吧！", // TODO: i18n
                            color = Tokens.colorTextMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(horizontal = Tokens.spacing300)
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
                            start = Tokens.spacing300,
                            end = Tokens.spacing300,
                            bottom = Tokens.spacing300 +
                                WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
                        ),
                        horizontalArrangement = Arrangement.spacedBy(Tokens.spacing150),
                        verticalItemSpacing = Tokens.spacing150,
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
            .clip(RoundedCornerShape(Tokens.radiusLg))
            .border(1.dp, Tokens.colorBorderLight, RoundedCornerShape(Tokens.radiusLg))
            .background(Tokens.colorWhite)
            .clickable(onClick = onClick),
    ) {
        // share_image_url 為 null 是正常狀態（尚未或未成功產過分享圖），以無圖漸層卡呈現，不當作載入失敗
        if (item.hasPoster) {
            CompanionAsyncImage(
                url = item.shareImageUrl.orEmpty(),
                placeholderAspectRatio = 9f / 16f,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = Tokens.radiusLg, topEnd = Tokens.radiusLg))
                    .testTag("companion_quiz_gallery_card_poster"),
                placeholder = {
                    Text(
                        item.travelIdentity,
                        fontWeight = FontWeight(Tokens.fontWeightBold),
                        color = Tokens.colorBackgroundPrimaryDarker,
                        fontSize = Tokens.fontSize4,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(Tokens.spacing200),
                    )
                },
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Tokens.colorBackgroundPrimaryLighter,
                                Tokens.colorBackgroundPrimaryLight,
                            ),
                        ),
                    )
                    .testTag("companion_quiz_gallery_card_no_poster"),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    item.travelIdentity,
                    fontWeight = FontWeight(Tokens.fontWeightBold),
                    color = Tokens.colorBackgroundPrimaryDarker,
                    fontSize = Tokens.fontSize4,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(Tokens.spacing200),
                )
            }
        }
        Column(modifier = Modifier.padding(Tokens.spacing150)) {
            Text(
                item.travelIdentity,
                fontWeight = FontWeight(Tokens.fontWeightMediumAndroid),
                color = Tokens.colorTextDarker,
                fontSize = Tokens.fontSize3,
            )
            if (destination.isNotBlank()) {
                Spacer(Modifier.height(Tokens.spacing050))
                Text(destination, color = Tokens.colorTextMedium, fontSize = Tokens.fontSize2)
            }
            if (item.tagline.isNotBlank()) {
                Spacer(Modifier.height(Tokens.spacing050))
                Text(item.tagline, color = Tokens.colorTextMedium, fontSize = Tokens.fontSize2)
            }
            if (!item.companionName.isNullOrBlank() || !item.partnerAvatarUrl.isNullOrBlank()) {
                Spacer(Modifier.height(Tokens.spacing050))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // partner_avatar_url 可能為 null（舊資料、或當時沒傳），以 "?" 圓形佔位圖 fallback
                    CompanionAsyncImage(
                        url = item.partnerAvatarUrl.orEmpty(),
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Tokens.colorBackgroundPrimaryLighter)
                            .testTag("companion_quiz_gallery_card_avatar"),
                        placeholder = {
                            Text("?", color = Tokens.colorTextPrimaryDark, fontSize = Tokens.fontSize1)
                        },
                    )
                    if (!item.companionName.isNullOrBlank()) {
                        Spacer(Modifier.width(Tokens.spacing050))
                        Text(
                            "by ${item.companionName}",
                            color = Tokens.colorTextMedium,
                            fontSize = Tokens.fontSize1,
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
internal fun QuizGalleryDetailScreen(item: QuizGalleryItem, onBack: () -> Unit) {
    val destination = item.destinationCn.ifBlank { item.destinationEn }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("companion_quiz_gallery_detail_screen")
            .background(Tokens.colorWhite),
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
                    .padding(horizontal = Tokens.spacing300)
                    .padding(top = Tokens.spacing300, bottom = Tokens.spacing400)
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
                        .background(Tokens.colorBackgroundPrimaryLighter)
                        .testTag("companion_quiz_gallery_detail_avatar"),
                    placeholder = {
                        Text("?", color = Tokens.colorTextPrimaryDark, fontSize = Tokens.fontSize4)
                    },
                )
                Spacer(Modifier.height(Tokens.spacing100))
                if (!item.companionName.isNullOrBlank()) {
                    Text(
                        item.companionName.orEmpty(),
                        fontWeight = FontWeight(Tokens.fontWeightBold),
                        fontSize = Tokens.fontSize5,
                        color = Tokens.colorTextDarker,
                    )
                    Spacer(Modifier.height(Tokens.spacing100))
                }
                Text(
                    item.travelIdentity,
                    fontWeight = FontWeight(Tokens.fontWeightMediumAndroid),
                    fontSize = Tokens.fontSize4,
                    color = Tokens.colorTextPrimaryDark,
                )
                if (destination.isNotBlank()) {
                    Spacer(Modifier.height(Tokens.spacing050))
                    Text(destination, color = Tokens.colorTextMedium, fontSize = Tokens.fontSize3)
                }
                if (item.tagline.isNotBlank()) {
                    Spacer(Modifier.height(Tokens.spacing100))
                    Text(item.tagline, color = Tokens.colorTextDarker, fontSize = Tokens.fontSize3)
                }
                if (item.highlightTags.isNotEmpty()) {
                    Spacer(Modifier.height(Tokens.spacing150))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(Tokens.spacing100, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(Tokens.spacing100),
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
                    Spacer(Modifier.height(Tokens.spacing200))
                    Text(
                        item.companionQuote,
                        color = Tokens.colorTextDarker,
                        fontSize = Tokens.fontSize3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Tokens.radiusLg))
                            .border(1.dp, Tokens.colorBorderLight, RoundedCornerShape(Tokens.radiusLg))
                            .background(Tokens.colorBackgroundSurfaceLight)
                            .padding(Tokens.spacing200)
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
                .padding(Tokens.spacing200)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(onClick = onBack)
                .testTag("companion_quiz_gallery_detail_back_btn"),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_left_line),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

// ---------- ③ 測驗作答 ----------

@Composable
internal fun QuizScreen(
    viewModel: AiCompanionViewModel,
    creation: CompanionCreationState,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
) {
    val quiz by viewModel.quizState.collectAsStateWithLifecycle()
    val quizAnswers by viewModel.quizAnswers.collectAsStateWithLifecycle()
    val companionName = creation.companionName.ifBlank { "旅伴" } // TODO: i18n fallback

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("companion_quiz_screen")
            .background(Tokens.colorBackgroundSurfaceLight),
    ) {
        when (val q = quiz) {
            is QuizState.Loading, QuizState.Idle ->
                CompanionLoadingContent(
                    avatarUrl = creation.avatarUrl,
                    companionName = companionName,
                    title = "正在準備你的旅行問卷中", // TODO: i18n
                    subtitle = "挑選題目 ▸ 客製化語氣 ▸ 準備選項", // TODO: i18n
                    testTag = "companion_quiz_loading",
                )
            is QuizState.Error ->
                Box(Modifier.fillMaxSize().padding(Tokens.spacing300), contentAlignment = Alignment.Center) {
                    Text(
                        "題庫載入失敗，請稍後再試", // TODO: i18n
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
        "這題沒有標準答案，選你最想去的就好", // TODO: i18n
        "憑直覺～我在旁邊陪你", // TODO: i18n
        "偷偷說，我也喜歡第二張", // TODO: i18n
        "再幾題，我就更懂你了！", // TODO: i18n
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
                            topStart = Tokens.radiusLg,
                            topEnd = Tokens.radiusLg,
                            bottomStart = Tokens.radiusLg,
                            bottomEnd = Tokens.radiusSm,
                        ),
                    )
                    .background(Tokens.colorTextDarker)
                    .padding(horizontal = Tokens.spacing150, vertical = Tokens.spacing100),
            ) {
                Text(
                    bubbles[bubbleIndex],
                    color = Tokens.colorWhite,
                    fontSize = Tokens.fontSize2,
                )
            }
        }
        Spacer(Modifier.height(Tokens.spacing100))
        CompanionAsyncImage(
            url = avatarUrl,
            modifier = Modifier
                .offset(y = (-6 * bobProgress).dp)
                .graphicsLayer { rotationZ = -2f + 4f * bobProgress }
                .size(72.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .border(3.dp, Tokens.colorWhite, CircleShape)
                .background(Tokens.colorBackgroundPrimaryLighter),
            placeholder = {
                Text("?", color = Tokens.colorTextPrimaryDark)
            },
        )
    }
}

@Composable
private fun QuizQuestionContent(
    question: QuizQuestion,
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
                .background(Tokens.colorWhite)
                .statusBarsPadding()
                .padding(horizontal = Tokens.spacing200, vertical = Tokens.spacing150),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("companion_quiz_back_btn"),
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_arrow_left_line),
                    contentDescription = null,
                    tint = Tokens.colorTextDarker,
                )
            }
            Spacer(Modifier.width(Tokens.spacing150))
            LinearProgressIndicator(
                progress = { (questionIndex + 1).toFloat() / totalCount },
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Tokens.colorBackgroundPrimaryMedium,
                trackColor = Tokens.colorBackgroundSurfaceMedium,
                strokeCap = StrokeCap.Round,
                drawStopIndicator = {},
            )
            Spacer(Modifier.width(Tokens.spacing150))
            Text(
                "${questionIndex + 1}/$totalCount",
                color = Tokens.colorTextPrimaryDark,
                fontWeight = FontWeight(Tokens.fontWeightMediumAndroid),
                fontSize = Tokens.fontSize3,
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
                    .padding(horizontal = Tokens.spacing300),
            ) {
                Spacer(Modifier.height(Tokens.spacing200))
                Text(
                    question.text,
                    fontWeight = FontWeight(Tokens.fontWeightBold),
                    fontSize = Tokens.fontSize5,
                    color = Tokens.colorTextDarker,
                    modifier = Modifier.testTag("companion_quiz_question_text"),
                )
                Spacer(Modifier.height(Tokens.spacing200))

                // 2 欄圖卡 grid
                val rows = options.chunked(2)
                rows.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Tokens.spacing150),
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
                    Spacer(Modifier.height(Tokens.spacing150))
                }
            }

            if (showScrollHint && remainingOptions > 0) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = Tokens.spacing100)
                        .clip(RoundedCornerShape(Tokens.radiusMd))
                        .background(Tokens.colorWhite.copy(alpha = 0.9f))
                        .padding(horizontal = Tokens.spacing150, vertical = Tokens.spacing075),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "下面還有 $remainingOptions 個選項", // TODO: i18n
                        color = Tokens.colorTextPrimaryDark,
                        fontSize = Tokens.fontSize2,
                        fontWeight = FontWeight(Tokens.fontWeightMediumAndroid),
                    )
                    Spacer(Modifier.width(Tokens.spacing050))
                    Icon(
                        painter = painterResource(Res.drawable.ic_arrow_down_line),
                        contentDescription = null,
                        tint = Tokens.colorTextPrimaryDark,
                        modifier = Modifier.size(Tokens.dimensionIconXs),
                    )
                }
            }

            if (avatarUrl.isNotBlank()) {
                QuizBuddy(
                    avatarUrl = avatarUrl,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = Tokens.spacing150, bottom = Tokens.spacing150),
                )
            }
        }

        // 底部按鈕
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Tokens.colorWhite)
                .navigationBarsPadding()
                .padding(horizontal = Tokens.spacing300, vertical = Tokens.spacing200),
        ) {
            PrimaryButton(
                text = if (isLast) "提交答案" else "下一題", // TODO: i18n
                enabled = selectedOptionId != null,
                testTag = if (isLast) "companion_quiz_submit_btn" else "companion_quiz_next_btn",
                onClick = onNext,
            )
        }
    }
}

@Composable
private fun QuizOptionCard(
    option: QuizOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (isSelected) Tokens.colorBackgroundPrimaryMedium else Tokens.colorBorderLight
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Column(
        modifier = modifier
            .testTag("quiz_option_${option.id}")
            .clip(RoundedCornerShape(Tokens.radiusLg))
            .border(borderWidth, borderColor, RoundedCornerShape(Tokens.radiusLg))
            .background(Tokens.colorWhite)
            .clickable { onClick() },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f) // 選項圖片本身是直式 4:3（高:寬），對齊 iOS 呈現，容器比例需一致才不會裁切
                .background(Tokens.colorBackgroundSurfaceMedium),
        ) {
            CompanionAsyncImage(
                url = option.imageUrl,
                contentDescription = option.text,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = Tokens.radiusLg, topEnd = Tokens.radiusLg)),
            )
            if (isSelected) {
                Icon(
                    painter = painterResource(Res.drawable.ic_check_circle_fill),
                    contentDescription = null,
                    tint = Tokens.colorBackgroundPrimaryMedium,
                    modifier = Modifier
                        .padding(Tokens.spacing075)
                        .size(28.dp),
                )
            }
        }
        Text(
            text = option.text,
            color = Tokens.colorBlack10,
            fontWeight = FontWeight(Tokens.fontWeightBold),
            fontSize = Tokens.fontSize2,
            modifier = Modifier.padding(Tokens.spacing150),
        )
    }
}
