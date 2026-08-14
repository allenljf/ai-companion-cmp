package com.kkday.feature.ai_companion.presentation.compose

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.kkday.design.StyleDictionary
import com.kkday.design.button.ButtonSizeType
import com.kkday.design.button.ButtonState
import com.kkday.design.button.ButtonType
import com.kkday.design.button.KKButton
import com.kkday.design.dialog.v3.KKModalBottomSheetDragHandle
import com.kkday.design.kkTag.KKTag
import com.kkday.design.kkTag.KKTagSolidColor
import com.kkday.feature.ai_companion.viewModel.TravelGuideState
import com.kkday.feature.ai_companion.viewModel.TripProductState
import com.kkday.feature.ai_companion.viewModel.TripReviseState
import com.kkday.model.companion.SavedTripRecord
import com.kkday.model.companion.TravelGuideDay
import com.kkday.model.companion.TravelGuideDayItem
import kotlin.math.roundToInt

/**
 * Phase 2 行程成果頁（travel-guide 2026-08 狀態機式 schema：itinerary_patch.days[].items[]）。
 * 版面依 Claude design mockup（m-plan-finish.jsx）：hero 壓字＋天數分頁＋儲存出口；
 * Day 分頁直接呈現當日細節（時間軸：spot/meal 為卡片含導航、logistics 為輕量交通列），
 * 總覽點某天等同切換到該 Day 分頁，無獨立細節頁。
 * 「請{旅伴}幫我改」開 bottom sheet 對話（travel-revise）：每輪修改成功即 merge 本地生效，可連續修改。
 */

// ---------- ③ 行程成果頁 ----------

/** hero：品牌色深漸層占位＋壓字（日期列＋大標）＋右上關閉鈕（設計稿 TripHero，226dp）。 */
@Composable
private fun TripHero(trip: SavedTripRecord, onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(226.dp)
            .background(
                Brush.verticalGradient(
                    listOf(
                        StyleDictionary.kkColorBackgroundPrimaryMedium,
                        StyleDictionary.kkColorBackgroundPrimaryDarker,
                    ),
                ),
            ),
    ) {
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
                .padding(top = StyleDictionary.kkSpacing150, end = StyleDictionary.kkSpacing150)
                .size(34.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.4f))
                .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                .clickable(onClick = onClose)
                .testTag("companion_trip_close_btn"),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(com.kkday.design.R.drawable.ic_cross_line),
                contentDescription = null,
                tint = StyleDictionary.kkColorWhite,
                modifier = Modifier.size(StyleDictionary.kkDimensionIconXs),
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = StyleDictionary.kkSpacing200, vertical = StyleDictionary.kkSpacing200),
        ) {
            Text(
                "行程排好了・${trip.city}・共 ${trip.totalDays} 天", // TODO: replace with stringResource
                fontSize = StyleDictionary.kkFontSize2,
                fontWeight = FontWeight(StyleDictionary.kkFontWeightMediumAndroid),
                color = Color.White.copy(alpha = 0.82f),
            )
            Spacer(Modifier.height(StyleDictionary.kkSpacing050))
            Text(
                trip.title,
                fontWeight = FontWeight(StyleDictionary.kkFontWeightBold),
                fontSize = StyleDictionary.kkFontSize7,
                color = StyleDictionary.kkColorWhite,
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
            .background(StyleDictionary.kkColorWhite)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = StyleDictionary.kkSpacing150),
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
                        if (isSelected) StyleDictionary.kkFontWeightBold else StyleDictionary.kkFontWeightMediumAndroid,
                    ),
                    fontSize = StyleDictionary.kkFontSize3,
                    color = if (isSelected) StyleDictionary.kkColorTextPrimaryDark else StyleDictionary.kkColorTextMedium,
                    modifier = Modifier.padding(
                        horizontal = StyleDictionary.kkSpacing150,
                        vertical = StyleDictionary.kkSpacing150,
                    ),
                    maxLines = 1,
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .clip(RoundedCornerShape(StyleDictionary.kkRadiusXs))
                        .background(
                            if (isSelected) StyleDictionary.kkColorBackgroundPrimaryMedium else Color.Transparent,
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
        fontSize = StyleDictionary.kkFontSize1,
        fontWeight = FontWeight(StyleDictionary.kkFontWeightMediumAndroid),
        color = StyleDictionary.kkColorTextHighlightDark,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(StyleDictionary.kkColorBackgroundHighlightLighter)
            .border(1.dp, StyleDictionary.kkColorBorderHighlightLight, RoundedCornerShape(999.dp))
            .padding(horizontal = StyleDictionary.kkSpacing075),
    )
}

@Composable
private fun DayKindTags(day: TravelGuideDay) {
    if (day.isArrival) {
        Spacer(Modifier.width(StyleDictionary.kkSpacing075))
        DayKindTag("抵達日") // TODO: replace with stringResource
    }
    if (day.isDeparture) {
        Spacer(Modifier.width(StyleDictionary.kkSpacing075))
        DayKindTag("返程日") // TODO: replace with stringResource
    }
    if (day.halfDay) {
        Spacer(Modifier.width(StyleDictionary.kkSpacing075))
        DayKindTag("半天") // TODO: replace with stringResource
    }
}

/** 單日內容區塊（總覽列表用，設計稿 DayBlock）：Day 標題列＋逐項文字；點擊切到該 Day 分頁。 */
@Composable
private fun TripDayBlock(day: TravelGuideDay, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = StyleDictionary.kkSpacing150),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Day ${day.day}",
                fontWeight = FontWeight(StyleDictionary.kkFontWeightBold),
                fontSize = StyleDictionary.kkFontSize2,
                color = StyleDictionary.kkColorTextPrimaryDark,
            )
            DayKindTags(day)
            Spacer(Modifier.weight(1f))
            Icon(
                painter = painterResource(com.kkday.design.R.drawable.ic_arrow_right_line),
                contentDescription = null,
                tint = StyleDictionary.kkColorTextMedium,
                modifier = Modifier.size(StyleDictionary.kkDimensionIconXs),
            )
        }
        Spacer(Modifier.height(StyleDictionary.kkSpacing050))
        if (!day.isPlanned) {
            Text(
                "尚未安排…", // TODO: replace with stringResource
                fontSize = StyleDictionary.kkFontSize2,
                color = StyleDictionary.kkColorTextMedium,
                fontStyle = FontStyle.Italic,
            )
        } else {
            day.items.forEach { item ->
                Row(
                    modifier = Modifier.padding(vertical = StyleDictionary.kkSpacing025),
                    verticalAlignment = Alignment.Top,
                ) {
                    if (item.timeBand.isNotBlank()) {
                        Text(
                            item.timeBand,
                            fontSize = StyleDictionary.kkFontSize1,
                            fontWeight = FontWeight(StyleDictionary.kkFontWeightBold),
                            color = StyleDictionary.kkColorTextPrimaryDark,
                            modifier = Modifier.width(40.dp),
                        )
                    } else {
                        Spacer(Modifier.width(40.dp))
                    }
                    Text(
                        item.text,
                        fontSize = StyleDictionary.kkFontSize2,
                        color = if (item.isLogistics) StyleDictionary.kkColorTextMedium else StyleDictionary.kkColorTextDark,
                    )
                }
            }
        }
        Spacer(Modifier.height(StyleDictionary.kkSpacing100))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(StyleDictionary.kkColorBorderLighter),
        )
    }
}

/** 底部出口列：儲存到我的旅程（後端不儲存行程，儲存為 App 端本地行為；分享這期不做）。 */
@Composable
private fun TripBottomBar(onSave: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(StyleDictionary.kkColorWhite)
            .navigationBarsPadding()
            .padding(horizontal = StyleDictionary.kkSpacing200, vertical = StyleDictionary.kkSpacing150)
            .testTag("companion_trip_save_btn"),
    ) {
        KKButton(
            buttonText = "儲存到我的旅程", // TODO: replace with stringResource
            buttonType = ButtonType.PRIMARY,
            buttonState = ButtonState.ENABLED,
            buttonSizeType = ButtonSizeType.Lg,
            isFullWidth = true,
            leadingIcon = com.kkday.design.R.drawable.ic_heart_line,
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
            .padding(horizontal = StyleDictionary.kkSpacing300)
            .testTag("companion_trip_failed"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            if (canRetry) "行程生成失敗，請重試" else "發生錯誤，請稍後再試", // TODO: replace with stringResource
            fontWeight = FontWeight(StyleDictionary.kkFontWeightBold),
            fontSize = StyleDictionary.kkFontSize4,
            color = StyleDictionary.kkColorTextDarker,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(StyleDictionary.kkSpacing300))
        if (canRetry) {
            Box(Modifier.fillMaxWidth().testTag("companion_trip_retry_btn")) {
                KKButton(
                    buttonText = "重試", // TODO: replace with stringResource
                    buttonType = ButtonType.PRIMARY,
                    buttonState = ButtonState.ENABLED,
                    buttonSizeType = ButtonSizeType.Md,
                    isFullWidth = true,
                    onClick = onRetry,
                )
            }
            Spacer(Modifier.height(StyleDictionary.kkSpacing150))
        }
        Box(Modifier.fillMaxWidth().testTag("companion_trip_failed_back_btn")) {
            KKButton(
                buttonText = "返回", // TODO: replace with stringResource
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
            .background(StyleDictionary.kkColorBackgroundSurfaceLight)
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
    val tabs = listOf("總覽") + trip.days.map { "Day ${it.day}" } // TODO: replace with stringResource
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
                                horizontal = StyleDictionary.kkSpacing200,
                                vertical = StyleDictionary.kkSpacing150,
                            ),
                        ) {
                            PlanChatBubble(fromMe = false, avatarUrl = avatarUrl, text = firstMessage)
                        }
                    }
                }
                itemsIndexed(trip.days, key = { _, day -> "day_block_${day.day}" }) { index, day ->
                    Box(modifier = Modifier.padding(horizontal = StyleDictionary.kkSpacing200)) {
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
                            "這天還沒排好，之後再補上。", // TODO: replace with stringResource
                            fontSize = StyleDictionary.kkFontSize2,
                            color = StyleDictionary.kkColorTextMedium,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.padding(horizontal = StyleDictionary.kkSpacing200),
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
                                .padding(horizontal = StyleDictionary.kkSpacing200)
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
            item(key = "bottom_spacer") { Spacer(Modifier.height(StyleDictionary.kkSpacing300)) }
        }
        TripBottomBar(onSave = onSave)
    }

    // 「請旅伴幫我改」FAB：旅伴頭像圓鈕（100dp），蓋在所有天數內容之上的**單一共用實例**
    // （不是每天各一顆），可拖曳移動且位置跨天保留；點擊開修改對話 bottom sheet，
    // target_day = 目前所在的 Day 分頁（總覽分頁不顯示——修改 API 要指定天，但拖曳位置仍保留）
    if (currentDay != null) {
        CompanionAsyncImage(
            url = avatarUrl,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = StyleDictionary.kkSpacing200, bottom = 96.dp)
                .offset { IntOffset(fabDragOffset.x.roundToInt(), fabDragOffset.y.roundToInt()) }
                .size(100.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(StyleDictionary.kkColorBackgroundPrimaryLighter)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        // 基準位置在右下角，offset 只允許往左/往上（負值），並夾在畫面範圍內
                        val fabPx = 100.dp.toPx()
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
                    color = StyleDictionary.kkColorTextPrimaryDark,
                    fontSize = StyleDictionary.kkFontSize5,
                    fontWeight = FontWeight(StyleDictionary.kkFontWeightBold),
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
            horizontal = StyleDictionary.kkSpacing200,
            vertical = StyleDictionary.kkSpacing150,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "Day ${day.day}",
            fontWeight = FontWeight(StyleDictionary.kkFontWeightBold),
            fontSize = StyleDictionary.kkFontSize2,
            color = StyleDictionary.kkColorTextPrimaryDark,
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
                append("$spotCount 個景點") // TODO: replace with stringResource
                if (mealCount > 0) append("・$mealCount 個用餐")
            },
            fontSize = StyleDictionary.kkFontSize1,
            color = StyleDictionary.kkColorTextMedium,
            modifier = Modifier.padding(horizontal = StyleDictionary.kkSpacing200),
        )
        Spacer(Modifier.height(StyleDictionary.kkSpacing150))
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
    val context = LocalContext.current
    Row {
        Column(
            modifier = Modifier.width(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(StyleDictionary.kkSpacing150))
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (isFirst) StyleDictionary.kkColorBackgroundPrimaryMedium else StyleDictionary.kkColorWhite)
                    .border(2.5.dp, StyleDictionary.kkColorBackgroundPrimaryMedium, CircleShape),
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(StyleDictionary.kkColorBorderLight),
                )
            }
        }
        Spacer(Modifier.width(StyleDictionary.kkSpacing100))
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = StyleDictionary.kkSpacing150)
                .clip(RoundedCornerShape(StyleDictionary.kkRadiusLg))
                .border(1.dp, StyleDictionary.kkColorBorderLight, RoundedCornerShape(StyleDictionary.kkRadiusLg))
                .background(StyleDictionary.kkColorWhite)
                .padding(StyleDictionary.kkSpacing150),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (item.displayTime.isNotBlank()) {
                            // 有精確時間（time）就顯示 HH:MM；後端尚未補上前退回 time_band 時段字樣
                            Text(
                                item.displayTime,
                                fontWeight = FontWeight(StyleDictionary.kkFontWeightBold),
                                fontSize = StyleDictionary.kkFontSize2,
                                color = StyleDictionary.kkColorTextPrimaryDark,
                            )
                            Spacer(Modifier.width(StyleDictionary.kkSpacing100))
                        }
                        // 標題＝地點/活動名稱；後端尚未補 name 前退回顯示原本的描述句
                        Text(
                            item.displayTitle,
                            fontWeight = FontWeight(StyleDictionary.kkFontWeightBold),
                            fontSize = StyleDictionary.kkFontSize3,
                            color = StyleDictionary.kkColorTextDarker,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        // 已預訂項目（有 oid）：後端保護不可刪除，UI 標示供使用者辨識
                        if (item.isBooked) {
                            Spacer(Modifier.width(StyleDictionary.kkSpacing075))
                            KKTag(
                                text = "已預訂", // TODO: replace with stringResource
                                kkTagColor = KKTagSolidColor.CYAN,
                            )
                        } else if (item.isFromInterest) {
                            // 願望清單/瀏覽紀錄排入的商品（有 prod_id）：想去但還沒買，
                            // 與 oid「已預訂」是兩條不同的軸，不可標成「已預訂」
                            Spacer(Modifier.width(StyleDictionary.kkSpacing075))
                            KKTag(
                                text = "感興趣", // TODO: replace with stringResource
                                kkTagColor = KKTagSolidColor.AMBER,
                            )
                        }
                    }
                    // 副標＝一句話描述/行為（僅在後端已回 name 時才顯示，避免與標題重複同一句話）
                    if (item.displaySubtitle.isNotBlank()) {
                        Spacer(Modifier.height(StyleDictionary.kkSpacing050))
                        Text(
                            item.displaySubtitle,
                            fontSize = StyleDictionary.kkFontSize2,
                            color = StyleDictionary.kkColorTextMedium,
                        )
                    }
                    if (item.note.isNotBlank()) {
                        Spacer(Modifier.height(StyleDictionary.kkSpacing050))
                        Text(
                            item.note,
                            fontSize = StyleDictionary.kkFontSize2,
                            color = StyleDictionary.kkColorTextMedium,
                        )
                    }
                }
                // 導航鈕：依 LLM 推算座標開地圖（近似值，僅供大致標點；meal/logistics 無座標不顯示）
                if (item.lat != null && item.lng != null) {
                    Icon(
                        painter = painterResource(com.kkday.design.R.drawable.ic_location_arrow_line),
                        contentDescription = null,
                        tint = StyleDictionary.kkColorTextPrimaryDark,
                        modifier = Modifier
                            .size(StyleDictionary.kkDimensionIconSm)
                            .clickable {
                                val uri = Uri.parse(
                                    "geo:${item.lat},${item.lng}?q=${item.lat},${item.lng}(${Uri.encode(item.displayTitle)})",
                                )
                                try {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                } catch (_: ActivityNotFoundException) {
                                    // 裝置無地圖 App：退回瀏覽器開 Google Maps
                                    context.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("https://maps.google.com/?q=${item.lat},${item.lng}"),
                                        ),
                                    )
                                }
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

// 對應 AppConfigProduction.kkdayWebUrl，寫死避免吃到目前 build 環境的 web host
private const val PRODUCT_DEEPLINK_HOST = "www.kkday.com"

/**
 * 景點商品搜尋卡：用景點名稱背景打既有搜尋 API（見 SearchTripProductsUseCase），
 * 找到 ≥1 個商品才顯示——第一項商品的縮圖＋名稱＋評分＋價格，其餘顯示「還有 N 項相關商品」；
 * 整張卡可點擊，用真正的 App Link deeplink（非站內 KRouter）導去搜尋結果頁：
 * `https://{正式環境 web host}/zh-tw/product/productlist?keyword={關鍵字}`。
 * host 比照商品搜尋 API 寫死正式環境（`www.kkday.com`）——商品本身就是打正式環境查出來的，
 * 且正式環境 app 的 App Link 驗證（assetlinks.json）只登記在正式環境 host 底下，
 * 若沿用目前 build 環境的 host（如 SIT），已安裝的正式環境 App 不會被判定為已驗證的 handler，
 * 系統會改用瀏覽器開啟而非導回 App。
 * Loading／NotFound／null 皆不顯示任何東西，避免行程頁被大量 loading 佔位塞滿。
 */
@Composable
private fun TripProductCard(spotName: String, state: TripProductState?) {
    if (state !is TripProductState.Found || state.products.isEmpty()) return
    val context = LocalContext.current
    val firstProduct = state.products.first()
    // 用 API 回的實際總數（totalCount）算「還有 N 項」，不可用 products.size 代替——
    // 那只是本次抓回的筆數上限，跟真正符合關鍵字的商品總數無關
    val moreCount = (state.totalCount - 1).coerceAtLeast(0)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = StyleDictionary.kkSpacing100)
            .clip(RoundedCornerShape(StyleDictionary.kkRadiusMd))
            .border(1.dp, StyleDictionary.kkColorBorderLight, RoundedCornerShape(StyleDictionary.kkRadiusMd))
            .clickable {
                val deeplink = "https://$PRODUCT_DEEPLINK_HOST/zh-tw/product/productlist" +
                    "?keyword=${Uri.encode(spotName)}"
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(deeplink)))
            }
            .testTag("companion_trip_product_card"),
    ) {
        Row(
            modifier = Modifier.padding(StyleDictionary.kkSpacing100),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompanionAsyncImage(
                url = firstProduct.imgUrl,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(StyleDictionary.kkRadiusSm)),
                placeholder = {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(StyleDictionary.kkColorBackgroundSurfaceMedium),
                    )
                },
            )
            Spacer(Modifier.width(StyleDictionary.kkSpacing100))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    firstProduct.name,
                    fontWeight = FontWeight(StyleDictionary.kkFontWeightMediumAndroid),
                    fontSize = StyleDictionary.kkFontSize2,
                    color = StyleDictionary.kkColorTextDarker,
                    maxLines = 2,
                )
                Spacer(Modifier.height(StyleDictionary.kkSpacing025))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (firstProduct.ratingCount > 0) {
                        Icon(
                            painter = painterResource(com.kkday.design.R.drawable.ic_star_fill),
                            contentDescription = null,
                            tint = StyleDictionary.kkColorBackgroundHighlightMedium,
                            modifier = Modifier.size(StyleDictionary.kkDimensionIcon2xs),
                        )
                        Text(
                            "${firstProduct.ratingStar}(${firstProduct.ratingCount})",
                            fontSize = StyleDictionary.kkFontSize1,
                            color = StyleDictionary.kkColorTextMedium,
                            modifier = Modifier.padding(start = StyleDictionary.kkSpacing025),
                        )
                        Spacer(Modifier.width(StyleDictionary.kkSpacing075))
                    }
                    Text(
                        "${firstProduct.displaySymbolCurrency}${firstProduct.priceDouble.toInt()} 起", // TODO: replace with stringResource
                        fontWeight = FontWeight(StyleDictionary.kkFontWeightBold),
                        fontSize = StyleDictionary.kkFontSize2,
                        color = StyleDictionary.kkColorTextPrimaryDark,
                    )
                }
            }
        }
        if (moreCount > 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(StyleDictionary.kkColorBorderLighter),
            )
            Text(
                "還有 $moreCount 項相關商品", // TODO: replace with stringResource
                fontSize = StyleDictionary.kkFontSize2,
                fontWeight = FontWeight(StyleDictionary.kkFontWeightMediumAndroid),
                color = StyleDictionary.kkColorTextPrimaryDark,
                modifier = Modifier.padding(
                    horizontal = StyleDictionary.kkSpacing100,
                    vertical = StyleDictionary.kkSpacing075,
                ),
            )
        }
    }
}

// transport_mode → icon；後端尚未提供或未知值時退回飛機 icon（沿用既有的機場後勤視覺）
private fun transportModeIcon(transportMode: String): Int = when (transportMode) {
    "walk" -> com.kkday.design.R.drawable.ic_walk_line
    "bus" -> com.kkday.design.R.drawable.ic_bus_line
    "train", "subway" -> com.kkday.design.R.drawable.ic_train_line
    "car" -> com.kkday.design.R.drawable.ic_car_line
    else -> com.kkday.design.R.drawable.ic_airplane_line
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
                    .background(StyleDictionary.kkColorBorderLight),
            )
        }
        Spacer(Modifier.width(StyleDictionary.kkSpacing100))
        Icon(
            painter = painterResource(transportModeIcon(item.transportMode)),
            contentDescription = null,
            tint = StyleDictionary.kkColorTextMedium,
            modifier = Modifier.size(StyleDictionary.kkDimensionIcon2xs),
        )
        Spacer(Modifier.width(StyleDictionary.kkSpacing050))
        Text(
            buildString {
                if (item.displayTime.isNotBlank()) append("${item.displayTime}・")
                append(item.displayTitle)
                if (item.note.isNotBlank()) append("（${item.note}）")
            },
            fontSize = StyleDictionary.kkFontSize1,
            color = StyleDictionary.kkColorTextMedium,
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
        shape = RoundedCornerShape(topStart = StyleDictionary.kkRadiusXl, topEnd = StyleDictionary.kkRadiusXl),
        dragHandle = { KKModalBottomSheetDragHandle() },
        scrimColor = Color.Black.copy(alpha = 0.5f),
    ) {
        // 展開到全螢幕但距離頂部 100dp
        val sheetHeight = LocalConfiguration.current.screenHeightDp.dp - 100.dp
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
                    .padding(horizontal = StyleDictionary.kkSpacing200)
                    .padding(bottom = StyleDictionary.kkSpacing100),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CompanionAsyncImage(
                    url = avatarUrl,
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(StyleDictionary.kkColorBackgroundPrimaryLighter),
                    placeholder = { Text("?", color = StyleDictionary.kkColorTextPrimaryDark) },
                )
                Spacer(Modifier.width(StyleDictionary.kkSpacing100))
                Text(
                    "${companionName}・改 Day ${state.dayNumber}", // TODO: replace with stringResource
                    fontWeight = FontWeight(StyleDictionary.kkFontWeightBold),
                    fontSize = StyleDictionary.kkFontSize3,
                    color = StyleDictionary.kkColorTextDarker,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "收小", // TODO: replace with stringResource
                    fontSize = StyleDictionary.kkFontSize2,
                    fontWeight = FontWeight(StyleDictionary.kkFontWeightMediumAndroid),
                    color = StyleDictionary.kkColorTextMedium,
                    modifier = Modifier
                        .clickable(onClick = onDismiss)
                        .testTag("companion_trip_revise_close"),
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(StyleDictionary.kkColorBorderLight),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = StyleDictionary.kkSpacing200, vertical = StyleDictionary.kkSpacing150),
            ) {
                if (state.messages.isEmpty()) {
                    PlanChatBubble(
                        fromMe = false,
                        avatarUrl = avatarUrl,
                        text = "想調整 Day ${state.dayNumber} 的哪裡？例如「下午想加個抹茶體驗」。", // TODO: replace with stringResource
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
                        Box(Modifier.padding(start = 36.dp, top = StyleDictionary.kkSpacing050)) {
                            PlanChoiceChip(
                                label = "重試", // TODO: replace with stringResource
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
                            text = "連線好像出了點問題，再試一次好嗎？", // TODO: replace with stringResource
                        )
                        Box(Modifier.padding(start = 36.dp, top = StyleDictionary.kkSpacing050)) {
                            PlanChoiceChip(
                                label = "重試", // TODO: replace with stringResource
                                solid = true,
                                testTag = "companion_trip_revise_retry_chip",
                                onClick = onRetry,
                            )
                        }
                    }

                    // 已至少成功修改一次（本地已生效）：給收尾主行動回成果頁
                    state.hasRevised -> Box(Modifier.padding(start = 36.dp, top = StyleDictionary.kkSpacing050)) {
                        PlanChoiceChip(
                            label = "回去看修改結果", // TODO: replace with stringResource
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
                placeholder = "或直接告訴${companionName}你想怎麼改…", // TODO: replace with stringResource
            )
        }
    }
}
