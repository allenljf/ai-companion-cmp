package com.kkday.feature.ai_companion.presentation.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kkday.design.StyleDictionary
import com.kkday.design.button.ButtonSizeType
import com.kkday.design.button.ButtonState
import com.kkday.design.button.ButtonType
import com.kkday.design.button.KKButton
import com.kkday.design.dialog.DialogHeaderType
import com.kkday.design.dialog.KKDialog
import com.kkday.model.companion.SavedTripRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 「回顧我的旅程」列表頁：瀏覽本地儲存過的行程（後端不儲存，純 DataStore 本地資料）。
 * 點卡片回到行程成果頁（總覽/每日分頁、拖拉排序、請旅伴續改都能繼續用）；長按刪除鈕可移除。
 */
@Composable
internal fun SavedTripListScreen(
    trips: List<SavedTripRecord>,
    onBack: () -> Unit,
    onOpen: (SavedTripRecord) -> Unit,
    onDelete: (SavedTripRecord) -> Unit,
) {
    var pendingDelete by remember { mutableStateOf<SavedTripRecord?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StyleDictionary.kkColorBackgroundSurfaceLight)
            .testTag("companion_trip_list_screen"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StyleDictionary.kkColorWhite)
                .statusBarsPadding()
                .padding(horizontal = StyleDictionary.kkSpacing200, vertical = StyleDictionary.kkSpacing150),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(com.kkday.design.R.drawable.ic_arrow_left_line),
                contentDescription = null,
                tint = StyleDictionary.kkColorTextDarker,
                modifier = Modifier
                    .size(StyleDictionary.kkDimensionIconMd)
                    .clickable(onClick = onBack)
                    .testTag("companion_trip_list_back_btn"),
            )
            Spacer(Modifier.width(StyleDictionary.kkSpacing100))
            Text(
                "回顧我的旅程", // TODO: replace with stringResource
                fontWeight = FontWeight(StyleDictionary.kkFontWeightBold),
                fontSize = StyleDictionary.kkFontSize4,
                color = StyleDictionary.kkColorTextDarker,
            )
        }

        if (trips.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("companion_trip_list_empty"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    "還沒有儲存過的行程", // TODO: replace with stringResource
                    fontWeight = FontWeight(StyleDictionary.kkFontWeightBold),
                    fontSize = StyleDictionary.kkFontSize4,
                    color = StyleDictionary.kkColorTextDarker,
                )
                Spacer(Modifier.height(StyleDictionary.kkSpacing100))
                Text(
                    "跟旅伴一起排一趟，排完記得按「儲存到我的旅程」。", // TODO: replace with stringResource
                    fontSize = StyleDictionary.kkFontSize2,
                    color = StyleDictionary.kkColorTextMedium,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(StyleDictionary.kkSpacing200),
                verticalArrangement = Arrangement.spacedBy(StyleDictionary.kkSpacing150),
            ) {
                items(trips, key = { it.createdAt }) { trip ->
                    SavedTripListItem(
                        trip = trip,
                        onClick = { onOpen(trip) },
                        onDelete = { pendingDelete = trip },
                    )
                }
            }
        }
    }

    pendingDelete?.let { target ->
        KKDialog(
            headerType = DialogHeaderType.Text(title = "刪除這筆行程？", useScrollableContent = false), // TODO: replace with stringResource
            showHeaderCloseButton = false,
            showFooterShadow = false,
            onDismissRequest = { pendingDelete = null },
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
                        onDelete(target)
                        pendingDelete = null
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
                    onClick = { pendingDelete = null },
                )
            },
        )
    }
}

/** 列表卡片：城市縮圖占位＋行程名＋城市/天數/儲存日期＋刪除鈕。 */
@Composable
private fun SavedTripListItem(
    trip: SavedTripRecord,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(StyleDictionary.kkRadiusLg))
            .border(1.dp, StyleDictionary.kkColorBorderLight, RoundedCornerShape(StyleDictionary.kkRadiusLg))
            .background(StyleDictionary.kkColorWhite)
            .clickable(onClick = onClick)
            .padding(StyleDictionary.kkSpacing150)
            .testTag("companion_trip_list_item"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 城市縮圖占位（場景圖 API 尚未提供）
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(StyleDictionary.kkRadiusMd))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            StyleDictionary.kkColorBackgroundPrimaryMedium,
                            StyleDictionary.kkColorBackgroundPrimaryDarker,
                        ),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                trip.city.take(2),
                fontWeight = FontWeight(StyleDictionary.kkFontWeightBold),
                fontSize = StyleDictionary.kkFontSize4,
                color = StyleDictionary.kkColorWhite,
            )
        }
        Spacer(Modifier.width(StyleDictionary.kkSpacing150))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                trip.title,
                fontWeight = FontWeight(StyleDictionary.kkFontWeightBold),
                fontSize = StyleDictionary.kkFontSize3,
                color = StyleDictionary.kkColorTextDarker,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(StyleDictionary.kkSpacing050))
            Text(
                "${trip.city}・共 ${trip.totalDays} 天", // TODO: replace with stringResource
                fontSize = StyleDictionary.kkFontSize2,
                color = StyleDictionary.kkColorTextMedium,
            )
            if (trip.createdAt > 0) {
                Spacer(Modifier.height(StyleDictionary.kkSpacing025))
                Text(
                    SAVED_TRIP_DATE_FORMAT.format(Date(trip.createdAt)),
                    fontSize = StyleDictionary.kkFontSize1,
                    color = StyleDictionary.kkColorTextMedium,
                )
            }
        }
        Icon(
            painter = painterResource(com.kkday.design.R.drawable.ic_delete_line),
            contentDescription = null,
            tint = StyleDictionary.kkColorTextMedium,
            modifier = Modifier
                .size(StyleDictionary.kkDimensionIconSm)
                .clickable(onClick = onDelete)
                .testTag("companion_trip_list_delete_btn"),
        )
    }
}

private val SAVED_TRIP_DATE_FORMAT = SimpleDateFormat("yyyy/MM/dd 儲存", Locale.getDefault())
