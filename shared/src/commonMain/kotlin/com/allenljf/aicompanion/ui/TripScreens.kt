package com.allenljf.aicompanion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.allenljf.aicompanion.model.SavedTripRecord
import com.allenljf.aicompanion.model.TravelGuideDay
import com.allenljf.aicompanion.model.TravelGuideDayItem
import com.allenljf.aicompanion.theme.Tokens
import com.allenljf.aicompanion.ui.components.AppButton
import com.allenljf.aicompanion.ui.components.AppTag
import com.allenljf.aicompanion.ui.components.AppTagSolidColor
import com.allenljf.aicompanion.ui.components.ButtonSizeType
import com.allenljf.aicompanion.ui.components.ButtonState
import com.allenljf.aicompanion.ui.components.ButtonType
import com.allenljf.aicompanion.ui.components.DragHandle
import com.allenljf.aicompanion.viewmodel.TravelGuideState
import com.allenljf.aicompanion.viewmodel.TripProductState
import com.allenljf.aicompanion.viewmodel.TripReviseState
import org.jetbrains.compose.resources.painterResource
import aicompanion.shared.generated.resources.Res
import aicompanion.shared.generated.resources.ic_airplane_line
import aicompanion.shared.generated.resources.ic_arrow_right_line
import aicompanion.shared.generated.resources.ic_bus_line
import aicompanion.shared.generated.resources.ic_car_line
import aicompanion.shared.generated.resources.ic_cross_line
import aicompanion.shared.generated.resources.ic_heart_line
import aicompanion.shared.generated.resources.ic_location_arrow_line
import aicompanion.shared.generated.resources.ic_star_fill
import aicompanion.shared.generated.resources.ic_train_line
import aicompanion.shared.generated.resources.ic_walk_line
import kotlin.math.roundToInt

/**
 * Phase 2 行程成果頁（travel-guide 2026-08 狀態機式 schema：itinerary_patch.days[].items[]）。
 * 版面依 Claude design mockup（m-plan-finish.jsx）：hero 壓字＋天數分頁＋儲存出口；
 * Day 分頁直接呈現當日細節（時間軸：spot/meal 為卡片含導航、logistics 為輕量交通列），
 * 總覽點某天等同切換到該 Day 分頁，無獨立細節頁。
 * 「請{旅伴}幫我改」開 bottom sheet 對話（travel-revise）：每輪修改成功即 merge 本地生效，可連續修改。
 */

// ---------- ③ 行程成果頁 ----------

// 「請旅伴幫我改」FAB 尺寸：與測驗頁的旅伴頭像（QuizScreens.QuizBuddy）一致
private val TRIP_REVISE_FAB_SIZE = 72.dp

/**
 * hero：行程情境圖（travel-guide 的 hero_image_url）＋壓字（日期列＋大標）＋右上關閉鈕
 * （設計稿 TripHero，226dp）。無圖或載入失敗時退回品牌色深漸層占位。
 */
@Composable
private fun TripHero(trip: SavedTripRecord, onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(226.dp)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Tokens.colorBackgroundPrimaryMedium,
                        Tokens.colorBackgroundPrimaryDarker,
                    ),
                ),
            ),
    ) {
        CompanionAsyncImage(
            url = trip.heroImageUrl,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.34f),
                        0.34f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.82f),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = Tokens.spacing150, end = Tokens.spacing150)
                .size(34.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.4f))
                .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                .clickable(onClick = onClose)
                .testTag("companion_trip_close_btn"),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_cross_line),
                contentDescription = null,
                tint = Tokens.colorWhite,
                modifier = Modifier.size(Tokens.dimensionIconXs),
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = Tokens.spacing200, vertical = Tokens.spacing200),
        ) {
            Text(
                "行程排好了・${trip.city}・共 ${trip.totalDays} 天", // TODO: i18n
                fontSize = Tokens.fontSize2,
                fontWeight = FontWeight(Tokens.fontWeightMediumAndroid),
                color = Color.White.copy(alpha = 0.82f),
            )
            Spacer(Modifier.height(Tokens.spacing050))
            Text(
                trip.title,
                fontWeight = FontWeight(Tokens.fontWeightBold),
                fontSize = Tokens.fontSize7,
                color = Tokens.colorWhite,
            )
        }
    }
}

/** 天數分頁列（設計稿：選中 teal 粗體＋2.5dp 底線貼齊文字寬）。 */
@Composable
private fun TripDayTabs(
    tabs: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Tokens.colorWhite)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = Tokens.spacing150),
    ) {
        tabs.forEachIndexed { index, tab ->
            val isSelected = index == selected
            Column(
                modifier = Modifier
                    .clickable { onSelect(index) }
                    .testTag("companion_trip_tab_$index"),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    tab,
                    fontWeight = FontWeight(
                        if (isSelected) Tokens.fontWeightBold else Tokens.fontWeightMediumAndroid,
                    ),
                    fontSize = Tokens.fontSize3,
                    color = if (isSelected) Tokens.colorTextPrimaryDark else Tokens.colorTextMedium,
                    modifier = Modifier.padding(
                        horizontal = Tokens.spacing150,
                        vertical = Tokens.spacing150,
                    ),
                    maxLines = 1,
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .clip(RoundedCornerShape(Tokens.radiusXs))
                        .background(
                            if (isSelected) Tokens.colorBackgroundPrimaryMedium else Color.Transparent,
                        ),
                )
            }
        }
    }
}

/** 天別標籤（抵達日／返程日／半天），kind/half_day 的視覺化。 */
@Composable
private fun DayKindTag(text: String) {
    Text(
        text,
        fontSize = Tokens.fontSize1,
        fontWeight = FontWeight(Tokens.fontWeightMediumAndroid),
        color = Tokens.colorTextHighlightDark,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Tokens.colorBackgroundHighlightLighter)
            .border(1.dp, Tokens.colorBorderHighlightLight, RoundedCornerShape(999.dp))
            .padding(horizontal = Tokens.spacing075),
    )
}

@Composable
private fun DayKindTags(day: TravelGuideDay) {
    if (day.isArrival) {
        Spacer(Modifier.width(Tokens.spacing075))
        DayKindTag("抵達日") // TODO: i18n
    }
    if (day.isDeparture) {
        Spacer(Modifier.width(Tokens.spacing075))
        DayKindTag("返程日") // TODO: i18n
    }
    if (day.halfDay) {
        Spacer(Modifier.width(Tokens.spacing075))
        DayKindTag("半天") // TODO: i18n
    }
}

/** 單日內容區塊（總覽列表用，設計稿 DayBlock）：Day 標題列＋逐項文字；點擊切到該 Day 分頁。 */
@Composable
private fun TripDayBlock(day: TravelGuideDay, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Tokens.spacing150),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Day ${day.day}",
                fontWeight = FontWeight(Tokens.fontWeightBold),
                fontSize = Tokens.fontSize2,
                color = Tokens.colorTextPrimaryDark,
            )
            DayKindTags(day)
            Spacer(Modifier.weight(1f))
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_right_line),
                contentDescription = null,
                tint = Tokens.colorTextMedium,
                modifier = Modifier.size(Tokens.dimensionIconXs),
            )
        }
        Spacer(Modifier.height(Tokens.spacing050))
        if (!day.isPlanned) {
            Text(
                "尚未安排…", // TODO: i18n
                fontSize = Tokens.fontSize2,
                color = Tokens.colorTextMedium,
                fontStyle = FontStyle.Italic,
            )
        } else {
            day.items.forEach { item ->
                Row(
                    modifier = Modifier.padding(vertical = Tokens.spacing025),
                    verticalAlignment = Alignment.Top,
                ) {
                    if (item.timeBand.isNotBlank()) {
                        Text(
                            item.timeBand,
                            fontSize = Tokens.fontSize1,
                            fontWeight = FontWeight(Tokens.fontWeightBold),
                            color = Tokens.colorTextPrimaryDark,
                            modifier = Modifier.width(40.dp),
                        )
                    } else {
                        Spacer(Modifier.width(40.dp))
                    }
                    Text(
                        item.text,
                        fontSize = Tokens.fontSize2,
                        color = if (item.isLogistics) Tokens.colorTextMedium else Tokens.colorTextDark,
                    )
                }
            }
        }
        Spacer(Modifier.height(Tokens.spacing100))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Tokens.colorBorderLighter),
        )
    }
}

/** 底部出口列：儲存到我的旅程（後端不儲存行程，儲存為 App 端本地行為；分享這期不做）。 */
@Composable
private fun TripBottomBar(onSave: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Tokens.colorWhite)
            .navigationBarsPadding()
            .padding(horizontal = Tokens.spacing200, vertical = Tokens.spacing150)
            .testTag("companion_trip_save_btn"),
    ) {
        AppButton(
            buttonText = "儲存到我的旅程", // TODO: i18n
            buttonType = ButtonType.PRIMARY,
            buttonState = ButtonState.ENABLED,
            buttonSizeType = ButtonSizeType.Lg,
            isFullWidth = true,
            leadingIcon = painterResource(Res.drawable.ic_heart_line),
            onClick = onSave,
        )
    }
}

/** 生成失敗（軟失敗/硬失敗共用）：可原樣重試；[canRetry]=false 為 400 驗證錯誤的通用提示。 */
@Composable
private fun TripFailedContent(
    canRetry: Boolean,
    onRetry: () -> Unit,
    onClose: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Tokens.spacing300)
            .testTag("companion_trip_failed"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            if (canRetry) "行程生成失敗，請重試" else "發生錯誤，請稍後再試", // TODO: i18n
            fontWeight = FontWeight(Tokens.fontWeightBold),
            fontSize = Tokens.fontSize4,
            color = Tokens.colorTextDarker,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Tokens.spacing300))
        if (canRetry) {
            Box(Modifier.fillMaxWidth().testTag("companion_trip_retry_btn")) {
                AppButton(
                    buttonText = "重試", // TODO: i18n
                    buttonType = ButtonType.PRIMARY,
                    buttonState = ButtonState.ENABLED,
                    buttonSizeType = ButtonSizeType.Md,
                    isFullWidth = true,
                    onClick = onRetry,
                )
            }
            Spacer(Modifier.height(Tokens.spacing150))
        }
        Box(Modifier.fillMaxWidth().testTag("companion_trip_failed_back_btn")) {
            AppButton(
                buttonText = "返回", // TODO: i18n
                buttonType = ButtonType.PRIMARY_SUBTLE,
                buttonState = ButtonState.ENABLED,
                buttonSizeType = ButtonSizeType.Md,
                isFullWidth = true,
                onClick = onClose,
            )
        }
    }
}

/**
 * ③ 行程成果頁：依 [TravelGuideState] 渲染成果／失敗重試。
 * 總覽/Day n 都在同一頁的分頁內：總覽點某天＝切到該 Day 分頁，Day 分頁直接是當日細節。
 */
@Composable
internal fun TripResultScreen(
    state: TravelGuideState,
    reviseState: TripReviseState,
    productStates: Map<String, TripProductState> = emptyMap(),
    avatarUrl: String,
    companionName: String,
    onClose: () -> Unit,
    onRetry: () -> Unit,
    onSave: () -> Unit,
    onStartRevise: (Int) -> Unit,
    onSendRevise: (String) -> Unit,
    onRetryRevise: () -> Unit,
    onDismissRevise: () -> Unit,
    onReorderItems: (dayNumber: Int, fromIndex: Int, toIndex: Int) -> Unit = { _, _, _ -> },
    onSearchProduct: (String) -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Tokens.colorBackgroundSurfaceLight)
            .testTag("companion_trip_result_screen"),
    ) {
        when (state) {
            is TravelGuideState.Loaded -> TripLoadedContent(
                trip = state.trip,
                reviseState = reviseState,
                productStates = productStates,
                avatarUrl = avatarUrl,
                companionName = companionName,
                firstMessage = state.messages.firstOrNull().orEmpty(),
                onClose = onClose,
                onSave = onSave,
                onStartRevise = onStartRevise,
                onSendRevise = onSendRevise,
                onRetryRevise = onRetryRevise,
                onDismissRevise = onDismissRevise,
                onReorderItems = onReorderItems,
                onSearchProduct = onSearchProduct,
            )

            TravelGuideState.SoftFailed, TravelGuideState.Error, TravelGuideState.Loading ->
                TripFailedContent(canRetry = true, onRetry = onRetry, onClose = onClose)

            TravelGuideState.InvalidRequest ->
                TripFailedContent(canRetry = false, onRetry = onRetry, onClose = onClose)

            TravelGuideState.Idle -> Unit
        }
    }
}

@Composable
private fun TripLoadedContent(
    trip: SavedTripRecord,
    reviseState: TripReviseState,
    productStates: Map<String, TripProductState>,
    avatarUrl: String,
    companionName: String,
    firstMessage: String,
    onClose: () -> Unit,
    onSave: () -> Unit,
    onStartRevise: (Int) -> Unit,
    onSendRevise: (String) -> Unit,
    onRetryRevise: () -> Unit,
    onDismissRevise: () -> Unit,
    onReorderItems: (dayNumber: Int, fromIndex: Int, toIndex: Int) -> Unit,
    onSearchProduct: (String) -> Unit,
) {
    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf("總覽") + trip.days.map { "Day ${it.day}" } // TODO: i18n
    val currentDay = if (tab > 0) trip.days.getOrNull(tab - 1) else null

    // 拖拉排序狀態（依 Day 切換重置）：draggingIndex/dragOffset 驅動被拖曳項目的手動位移；
    // itemIds 是每個項目位置的穩定識別、隨交換一起移動，讓 LazyColumn 的 key 對得上同一個項目，
    // 未被拖曳的項目交給 Modifier.animateItem() 自動位移動畫（做出「讓位」效果）
    var draggingIndex by remember(currentDay?.day) { mutableIntStateOf(-1) }
    var dragOffset by remember(currentDay?.day) { mutableFloatStateOf(0f) }
    val itemHeights = remember(currentDay?.day) { mutableStateMapOf<Int, Int>() }
    val itemIds = remember(currentDay?.day, currentDay?.items?.size) {
        mutableStateListOf(*IntArray(currentDay?.items?.size ?: 0) { it }.toTypedArray())
    }

    // FAB 拖曳位置：跨天共用同一顆（不以 day 為 key），切換 Day 分頁位置保留；離開頁面才重置
    var fabDragOffset by remember { mutableStateOf(Offset.Zero) }
    var contentSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { contentSize = it },
    ) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            item(key = "hero") { TripHero(trip = trip, onClose = onClose) }
            item(key = "tabs") { TripDayTabs(tabs = tabs, selected = tab, onSelect = { tab = it }) }

            if (tab == 0) {
                // 總覽 tab：旅伴一句話＋逐日摘要區塊（點某天＝切到該 Day 分頁）
                if (firstMessage.isNotBlank()) {
                    item(key = "overview_message") {
                        Box(
                            Modifier.padding(
                                horizontal = Tokens.spacing200,
                                vertical = Tokens.spacing150,
                            ),
                        ) {
                            PlanChatBubble(fromMe = false, avatarUrl = avatarUrl, text = firstMessage)
                        }
                    }
                }
                itemsIndexed(trip.days, key = { _, day -> "day_block_${day.day}" }) { index, day ->
                    Box(modifier = Modifier.padding(horizontal = Tokens.spacing200)) {
                        TripDayBlock(day = day, onClick = { tab = index + 1 })
                    }
                }
            } else if (currentDay != null) {
                // Day n 分頁：直接呈現當日細節（無獨立頁面）
                item(key = "day_header_${currentDay.day}") {
                    DayDetailHeader(day = currentDay)
                }
                if (!currentDay.isPlanned) {
                    item(key = "day_unplanned_${currentDay.day}") {
                        Text(
                            "這天還沒排好，之後再補上。", // TODO: i18n
                            fontSize = Tokens.fontSize2,
                            color = Tokens.colorTextMedium,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.padding(horizontal = Tokens.spacing200),
                        )
                    }
                } else {
                    item(key = "day_stats_${currentDay.day}") { DayStatsLine(currentDay) }

                    // 綠色實心點固定給第一個卡片項目（設計稿起點視覺）
                    val firstCardIndex = currentDay.items.indexOfFirst { !it.isLogistics }
                    itemsIndexed(
                        items = currentDay.items,
                        key = { index, _ -> "day_item_${currentDay.day}_${itemIds.getOrElse(index) { index }}" },
                    ) { index, dayItem ->
                        val isDragging = index == draggingIndex
                        Box(
                            modifier = Modifier
                                .padding(horizontal = Tokens.spacing200)
                                .then(if (isDragging) Modifier else Modifier.animateItem())
                                .zIndex(if (isDragging) 1f else 0f)
                                .graphicsLayer {
                                    if (isDragging) {
                                        // 拖曳浮起效果：放大＋半透明＋陰影，跟著手指位移
                                        translationY = dragOffset
                                        scaleX = 1.03f
                                        scaleY = 1.03f
                                        alpha = 0.92f
                                        shadowElevation = 12.dp.toPx()
                                    }
                                }
                                .onGloballyPositioned { itemHeights[index] = it.size.height }
                                .pointerInput(currentDay.day, index) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = {
                                            draggingIndex = index
                                            dragOffset = 0f
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragOffset += dragAmount.y
                                            val current = draggingIndex
                                            if (current < 0) return@detectDragGesturesAfterLongPress
                                            val nextHeight = itemHeights[current + 1]
                                            val prevHeight = itemHeights[current - 1]
                                            when {
                                                // 往下拖過下一項的一半 → 與下一項交換（其他項目自動讓位）
                                                nextHeight != null && dragOffset > nextHeight / 2f -> {
                                                    onReorderItems(currentDay.day, current, current + 1)
                                                    itemIds.apply {
                                                        val moved = this[current]
                                                        this[current] = this[current + 1]
                                                        this[current + 1] = moved
                                                    }
                                                    itemHeights[current + 1] = itemHeights[current] ?: nextHeight
                                                    itemHeights[current] = nextHeight
                                                    draggingIndex = current + 1
                                                    dragOffset -= nextHeight
                                                }
                                                // 往上拖過上一項的一半 → 與上一項交換
                                                prevHeight != null && dragOffset < -prevHeight / 2f -> {
                                                    onReorderItems(currentDay.day, current, current - 1)
                                                    itemIds.apply {
                                                        val moved = this[current]
                                                        this[current] = this[current - 1]
                                                        this[current - 1] = moved
                                                    }
                                                    itemHeights[current - 1] = itemHeights[current] ?: prevHeight
                                                    itemHeights[current] = prevHeight
                                                    draggingIndex = current - 1
                                                    dragOffset += prevHeight
                                                }
                                            }
                                        },
                                        onDragEnd = {
                                            draggingIndex = -1
                                            dragOffset = 0f
                                        },
                                        onDragCancel = {
                                            draggingIndex = -1
                                            dragOffset = 0f
                                        },
                                    )
                                },
                        ) {
                            if (dayItem.isLogistics) {
                                LogisticsRow(dayItem)
                            } else {
                                TimelineStopCard(
                                    item = dayItem,
                                    isFirst = index == firstCardIndex,
                                    isLast = index == currentDay.items.lastIndex,
                                    productState = productStates[dayItem.displayTitle],
                                    onSearchProduct = onSearchProduct,
                                )
                            }
                        }
                    }
                }
            }
            item(key = "bottom_spacer") { Spacer(Modifier.height(Tokens.spacing300)) }
        }
        TripBottomBar(onSave = onSave)
    }

    // 「請旅伴幫我改」FAB：旅伴頭像圓鈕（72dp，與測驗頁 QuizBuddy 同尺寸），蓋在所有天數內容之上的**單一共用實例**
    // （不是每天各一顆），可拖曳移動且位置跨天保留；點擊開修改對話 bottom sheet，
    // target_day = 目前所在的 Day 分頁（總覽分頁不顯示——修改 API 要指定天，但拖曳位置仍保留）
    if (currentDay != null) {
        CompanionAsyncImage(
            url = avatarUrl,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = Tokens.spacing200, bottom = 96.dp)
                .offset { IntOffset(fabDragOffset.x.roundToInt(), fabDragOffset.y.roundToInt()) }
                .size(TRIP_REVISE_FAB_SIZE)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(Tokens.colorBackgroundPrimaryLighter)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        // 基準位置在右下角，offset 只允許往左/往上（負值），並夾在畫面範圍內
                        val fabPx = TRIP_REVISE_FAB_SIZE.toPx()
                        fabDragOffset = Offset(
                            (fabDragOffset.x + dragAmount.x)
                                .coerceIn(-(contentSize.width - fabPx).coerceAtLeast(0f), 0f),
                            (fabDragOffset.y + dragAmount.y)
                                .coerceIn(-(contentSize.height - fabPx).coerceAtLeast(0f), 0f),
                        )
                    }
                }
                .clickable { onStartRevise(currentDay.day) }
                .testTag("companion_trip_revise_fab"),
            placeholder = {
                Text(
                    companionName.take(1),
                    color = Tokens.colorTextPrimaryDark,
                    fontSize = Tokens.fontSize5,
                    fontWeight = FontWeight(Tokens.fontWeightBold),
                )
            },
        )
    }
    }

    if (reviseState.active) {
        TripReviseSheet(
            state = reviseState,
            avatarUrl = avatarUrl,
            companionName = companionName,
            onSend = onSendRevise,
            onRetry = onRetryRevise,
            onDismiss = onDismissRevise,
        )
    }
}

// ---------- Day 分頁的當日細節（時間軸） ----------

/** 當日摘要列：Day n＋標籤（「請旅伴幫我改」入口改為右下角頭像 FAB，見 TripLoadedContent）。 */
@Composable
private fun DayDetailHeader(day: TravelGuideDay) {
    Row(
        modifier = Modifier.padding(
            horizontal = Tokens.spacing200,
            vertical = Tokens.spacing150,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "Day ${day.day}",
            fontWeight = FontWeight(Tokens.fontWeightBold),
            fontSize = Tokens.fontSize2,
            color = Tokens.colorTextPrimaryDark,
        )
        DayKindTags(day)
    }
}

/** 當日統計列：N 個景點・N 個用餐。 */
@Composable
private fun DayStatsLine(day: TravelGuideDay) {
    val spotCount = day.items.count { it.isSpot }
    val mealCount = day.items.count { it.isMeal }
    Column {
        Text(
            buildString {
                append("$spotCount 個景點") // TODO: i18n
                if (mealCount > 0) append("・$mealCount 個用餐")
            },
            fontSize = Tokens.fontSize1,
            color = Tokens.colorTextMedium,
            modifier = Modifier.padding(horizontal = Tokens.spacing200),
        )
        Spacer(Modifier.height(Tokens.spacing150))
    }
}

/**
 * 時間軸站點卡（spot/meal，設計稿 TimelineStop）：圓點（第一站實心）＋卡片
 * （時段＋標題＋副標描述＋右上導航鈕）。導航僅 spot 有座標（API 僅 type=spot 回 lat/lng）。
 */
@Composable
private fun TimelineStopCard(
    item: TravelGuideDayItem,
    isFirst: Boolean,
    isLast: Boolean,
    productState: TripProductState?,
    onSearchProduct: (String) -> Unit,
) {
    Row {
        Column(
            modifier = Modifier.width(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(Tokens.spacing150))
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (isFirst) Tokens.colorBackgroundPrimaryMedium else Tokens.colorWhite)
                    .border(2.5.dp, Tokens.colorBackgroundPrimaryMedium, CircleShape),
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(Tokens.colorBorderLight),
                )
            }
        }
        Spacer(Modifier.width(Tokens.spacing100))
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = Tokens.spacing150)
                .clip(RoundedCornerShape(Tokens.radiusLg))
                .border(1.dp, Tokens.colorBorderLight, RoundedCornerShape(Tokens.radiusLg))
                .background(Tokens.colorWhite)
                .padding(Tokens.spacing150),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (item.displayTime.isNotBlank()) {
                            // 有精確時間（time）就顯示 HH:MM；後端尚未補上前退回 time_band 時段字樣
                            Text(
                                item.displayTime,
                                fontWeight = FontWeight(Tokens.fontWeightBold),
                                fontSize = Tokens.fontSize2,
                                color = Tokens.colorTextPrimaryDark,
                            )
                            Spacer(Modifier.width(Tokens.spacing100))
                        }
                        // 標題＝地點/活動名稱；後端尚未補 name 前退回顯示原本的描述句
                        Text(
                            item.displayTitle,
                            fontWeight = FontWeight(Tokens.fontWeightBold),
                            fontSize = Tokens.fontSize3,
                            color = Tokens.colorTextDarker,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        // 已預訂項目（有 oid）：後端保護不可刪除，UI 標示供使用者辨識
                        if (item.isBooked) {
                            Spacer(Modifier.width(Tokens.spacing075))
                            AppTag(
                                text = "已預訂", // TODO: i18n
                                tagColor = AppTagSolidColor.CYAN,
                            )
                        } else if (item.isFromInterest) {
                            // 願望清單/瀏覽紀錄排入的商品（有 prod_id）：想去但還沒買，
                            // 與 oid「已預訂」是兩條不同的軸，不可標成「已預訂」
                            Spacer(Modifier.width(Tokens.spacing075))
                            AppTag(
                                text = "感興趣", // TODO: i18n
                                tagColor = AppTagSolidColor.AMBER,
                            )
                        }
                    }
                    // 副標＝一句話描述/行為（僅在後端已回 name 時才顯示，避免與標題重複同一句話）
                    if (item.displaySubtitle.isNotBlank()) {
                        Spacer(Modifier.height(Tokens.spacing050))
                        Text(
                            item.displaySubtitle,
                            fontSize = Tokens.fontSize2,
                            color = Tokens.colorTextMedium,
                        )
                    }
                    if (item.note.isNotBlank()) {
                        Spacer(Modifier.height(Tokens.spacing050))
                        Text(
                            item.note,
                            fontSize = Tokens.fontSize2,
                            color = Tokens.colorTextMedium,
                        )
                    }
                }
                // 導航鈕：依 LLM 推算座標開地圖（近似值，僅供大致標點；meal/logistics 無座標不顯示）
                if (item.lat != null && item.lng != null) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_location_arrow_line),
                        contentDescription = null,
                        tint = Tokens.colorTextPrimaryDark,
                        modifier = Modifier
                            .size(Tokens.dimensionIconSm)
                            .clickable {
                                // TODO: deeplink（原用 geo: intent 開地圖 app，找不到再退回瀏覽器開 Google Maps；demo 拿掉）
                            }
                            .testTag("companion_trip_nav_btn"),
                    )
                }
            }
            // 商品搜尋卡：以景點名稱為關鍵字背景搜尋，找到商品才顯示（見 TripProductCard）
            if (item.isSpot && item.displayTitle.isNotBlank()) {
                LaunchedEffect(item.displayTitle) { onSearchProduct(item.displayTitle) }
                TripProductCard(spotName = item.displayTitle, state = productState)
            }
        }
    }
}

/**
 * 景點商品搜尋卡：用景點名稱背景打既有搜尋 API（見 SearchTripProductsUseCase），
 * 找到 ≥1 個商品才顯示——第一項商品的縮圖＋名稱＋評分＋價格，其餘顯示「還有 N 項相關商品」；
 * 整張卡原可點擊導去商品搜尋結果頁（App Link deeplink），demo 拿掉，改 no-op。
 * Loading／NotFound／null 皆不顯示任何東西，避免行程頁被大量 loading 佔位塞滿。
 */
@Composable
private fun TripProductCard(spotName: String, state: TripProductState?) {
    if (state !is TripProductState.Found || state.products.isEmpty()) return
    val firstProduct = state.products.first()
    // 用 API 回的實際總數（totalCount）算「還有 N 項」，不可用 products.size 代替——
    // 那只是本次抓回的筆數上限，跟真正符合關鍵字的商品總數無關
    val moreCount = (state.totalCount - 1).coerceAtLeast(0)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Tokens.spacing100)
            .clip(RoundedCornerShape(Tokens.radiusMd))
            .border(1.dp, Tokens.colorBorderLight, RoundedCornerShape(Tokens.radiusMd))
            .clickable {
                // TODO: deeplink（原用 App Link 開商品搜尋結果頁；demo 拿掉）
            }
            .testTag("companion_trip_product_card"),
    ) {
        Row(
            modifier = Modifier.padding(Tokens.spacing100),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompanionAsyncImage(
                url = firstProduct.imageUrl,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(Tokens.radiusSm)),
                placeholder = {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Tokens.colorBackgroundSurfaceMedium),
                    )
                },
            )
            Spacer(Modifier.width(Tokens.spacing100))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    firstProduct.name,
                    fontWeight = FontWeight(Tokens.fontWeightMediumAndroid),
                    fontSize = Tokens.fontSize2,
                    color = Tokens.colorTextDarker,
                    maxLines = 2,
                )
                Spacer(Modifier.height(Tokens.spacing025))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (firstProduct.ratingCount > 0) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_star_fill),
                            contentDescription = null,
                            tint = Tokens.colorBackgroundHighlightMedium,
                            modifier = Modifier.size(Tokens.dimensionIcon2xs),
                        )
                        Text(
                            "${firstProduct.ratingStar}(${firstProduct.ratingCount})",
                            fontSize = Tokens.fontSize1,
                            color = Tokens.colorTextMedium,
                            modifier = Modifier.padding(start = Tokens.spacing025),
                        )
                        Spacer(Modifier.width(Tokens.spacing075))
                    }
                    Text(
                        "${firstProduct.currencySymbol}${firstProduct.price.toInt()} 起", // TODO: i18n
                        fontWeight = FontWeight(Tokens.fontWeightBold),
                        fontSize = Tokens.fontSize2,
                        color = Tokens.colorTextPrimaryDark,
                    )
                }
            }
        }
        if (moreCount > 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Tokens.colorBorderLighter),
            )
            Text(
                "還有 $moreCount 項相關商品", // TODO: i18n
                fontSize = Tokens.fontSize2,
                fontWeight = FontWeight(Tokens.fontWeightMediumAndroid),
                color = Tokens.colorTextPrimaryDark,
                modifier = Modifier.padding(
                    horizontal = Tokens.spacing100,
                    vertical = Tokens.spacing075,
                ),
            )
        }
    }
}

// transport_mode → icon；後端尚未提供或未知值時退回飛機 icon（沿用既有的機場後勤視覺）
private fun transportModeIcon(transportMode: String) = when (transportMode) {
    "walk" -> Res.drawable.ic_walk_line
    "bus" -> Res.drawable.ic_bus_line
    "train", "subway" -> Res.drawable.ic_train_line
    "car" -> Res.drawable.ic_car_line
    else -> Res.drawable.ic_airplane_line
}

/** 交通/後勤列（logistics，設計稿 TransitHint）：直線軸＋icon（依 transport_mode）＋文字，不包卡片。 */
@Composable
private fun LogisticsRow(item: TravelGuideDayItem) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.width(20.dp), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(28.dp)
                    .background(Tokens.colorBorderLight),
            )
        }
        Spacer(Modifier.width(Tokens.spacing100))
        Icon(
            painter = painterResource(transportModeIcon(item.transportMode)),
            contentDescription = null,
            tint = Tokens.colorTextMedium,
            modifier = Modifier.size(Tokens.dimensionIcon2xs),
        )
        Spacer(Modifier.width(Tokens.spacing050))
        Text(
            buildString {
                if (item.displayTime.isNotBlank()) append("${item.displayTime}・")
                append(item.displayTitle)
                if (item.note.isNotBlank()) append("（${item.note}）")
            },
            fontSize = Tokens.fontSize1,
            color = Tokens.colorTextMedium,
        )
    }
}

// ---------- 「請{旅伴}幫我改」修改對話 bottom sheet（travel-revise） ----------

/**
 * 修改行程對話（設計稿 chat bottom sheet）：每次開啟都是新對話（由 ViewModel startTripRevise 重置）。
 * 每送出一句需求即打一次 travel-revise，成功時變動的天已由 ViewModel merge 進本地行程（立即生效），
 * 連續修改會自動帶「當前最新行程」再打。軟失敗（fail_reason）渲染兜底泡泡＋重試，不 merge。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TripReviseSheet(
    state: TripReviseState,
    avatarUrl: String,
    companionName: String,
    onSend: (String) -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    val scrollState = rememberScrollState()
    LaunchedEffect(state.messages.size, state.isWaiting, state.fallbackReply) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = Tokens.radiusXl, topEnd = Tokens.radiusXl),
        dragHandle = { DragHandle() },
        scrimColor = Color.Black.copy(alpha = 0.5f),
    ) {
        // 展開到全螢幕但距離頂部 100dp
        // 原用 LocalConfiguration.current.screenHeightDp（Android-only，KMP commonMain 未提供），
        // 改用跨平台可用的 LocalWindowInfo.containerSize 換算 dp 高度
        val density = LocalDensity.current
        val windowHeightDp = with(density) { LocalWindowInfo.current.containerSize.height.toDp() }
        val sheetHeight = windowHeightDp - 100.dp
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(sheetHeight)
                .testTag("companion_trip_revise_sheet"),
        ) {
            // 標頭：頭像＋「{旅伴}・改 Day n」＋收小
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Tokens.spacing200)
                    .padding(bottom = Tokens.spacing100),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CompanionAsyncImage(
                    url = avatarUrl,
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Tokens.colorBackgroundPrimaryLighter),
                    placeholder = { Text("?", color = Tokens.colorTextPrimaryDark) },
                )
                Spacer(Modifier.width(Tokens.spacing100))
                Text(
                    "${companionName}・改 Day ${state.dayNumber}", // TODO: i18n
                    fontWeight = FontWeight(Tokens.fontWeightBold),
                    fontSize = Tokens.fontSize3,
                    color = Tokens.colorTextDarker,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "收小", // TODO: i18n
                    fontSize = Tokens.fontSize2,
                    fontWeight = FontWeight(Tokens.fontWeightMediumAndroid),
                    color = Tokens.colorTextMedium,
                    modifier = Modifier
                        .clickable(onClick = onDismiss)
                        .testTag("companion_trip_revise_close"),
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Tokens.colorBorderLight),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = Tokens.spacing200, vertical = Tokens.spacing150),
            ) {
                if (state.messages.isEmpty()) {
                    PlanChatBubble(
                        fromMe = false,
                        avatarUrl = avatarUrl,
                        text = "想調整 Day ${state.dayNumber} 的哪裡？例如「下午想加個抹茶體驗」。", // TODO: i18n
                    )
                }
                state.messages.forEach { message ->
                    PlanChatBubble(fromMe = message.fromMe, avatarUrl = avatarUrl, text = message.text)
                }
                when {
                    state.isWaiting -> PlanTypingBubble(avatarUrl = avatarUrl)

                    // 軟失敗兜底文案：照常渲染泡泡（不進歷史、不 merge），可原樣重打
                    state.fallbackReply.isNotBlank() -> {
                        PlanChatBubble(fromMe = false, avatarUrl = avatarUrl, text = state.fallbackReply)
                        Box(Modifier.padding(start = 36.dp, top = Tokens.spacing050)) {
                            PlanChoiceChip(
                                label = "重試", // TODO: i18n
                                solid = true,
                                testTag = "companion_trip_revise_retry_chip",
                                onClick = onRetry,
                            )
                        }
                    }

                    state.isError -> {
                        PlanChatBubble(
                            fromMe = false,
                            avatarUrl = avatarUrl,
                            text = "連線好像出了點問題，再試一次好嗎？", // TODO: i18n
                        )
                        Box(Modifier.padding(start = 36.dp, top = Tokens.spacing050)) {
                            PlanChoiceChip(
                                label = "重試", // TODO: i18n
                                solid = true,
                                testTag = "companion_trip_revise_retry_chip",
                                onClick = onRetry,
                            )
                        }
                    }

                    // 已至少成功修改一次（本地已生效）：給收尾主行動回成果頁
                    state.hasRevised -> Box(Modifier.padding(start = 36.dp, top = Tokens.spacing050)) {
                        PlanChoiceChip(
                            label = "回去看修改結果", // TODO: i18n
                            solid = true,
                            testTag = "companion_trip_revise_done_chip",
                            onClick = onDismiss,
                        )
                    }
                }
            }

            PlanInputBar(
                enabled = !state.isWaiting,
                onSend = onSend,
                placeholder = "或直接告訴${companionName}你想怎麼改…", // TODO: i18n
            )
        }
    }
}
