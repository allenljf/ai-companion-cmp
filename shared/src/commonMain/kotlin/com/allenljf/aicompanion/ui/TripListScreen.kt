package com.allenljf.aicompanion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.allenljf.aicompanion.model.SavedTripRecord
import com.allenljf.aicompanion.theme.Tokens
import com.allenljf.aicompanion.ui.components.AppButton
import com.allenljf.aicompanion.ui.components.AppDialog
import com.allenljf.aicompanion.ui.components.ButtonSizeType
import com.allenljf.aicompanion.ui.components.ButtonState
import com.allenljf.aicompanion.ui.components.ButtonType
import com.allenljf.aicompanion.ui.components.DialogHeaderType
import org.jetbrains.compose.resources.painterResource
import aicompanion.shared.generated.resources.Res
import aicompanion.shared.generated.resources.ic_arrow_left_line
import aicompanion.shared.generated.resources.ic_delete_line

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
            .background(Tokens.colorBackgroundSurfaceLight)
            .testTag("companion_trip_list_screen"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Tokens.colorWhite)
                .statusBarsPadding()
                .padding(horizontal = Tokens.spacing200, vertical = Tokens.spacing150),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_left_line),
                contentDescription = null,
                tint = Tokens.colorTextDarker,
                modifier = Modifier
                    .size(Tokens.dimensionIconMd)
                    .clickable(onClick = onBack)
                    .testTag("companion_trip_list_back_btn"),
            )
            Spacer(Modifier.width(Tokens.spacing100))
            Text(
                "回顧我的旅程", // TODO: i18n
                fontWeight = FontWeight(Tokens.fontWeightBold),
                fontSize = Tokens.fontSize4,
                color = Tokens.colorTextDarker,
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
                    "還沒有儲存過的行程", // TODO: i18n
                    fontWeight = FontWeight(Tokens.fontWeightBold),
                    fontSize = Tokens.fontSize4,
                    color = Tokens.colorTextDarker,
                )
                Spacer(Modifier.height(Tokens.spacing100))
                Text(
                    "跟旅伴一起排一趟，排完記得按「儲存到我的旅程」。", // TODO: i18n
                    fontSize = Tokens.fontSize2,
                    color = Tokens.colorTextMedium,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Tokens.spacing200),
                verticalArrangement = Arrangement.spacedBy(Tokens.spacing150),
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
        AppDialog(
            headerType = DialogHeaderType.Text(title = "刪除這筆行程？", useScrollableContent = false), // TODO: i18n
            showHeaderCloseButton = false,
            showFooterShadow = false,
            onDismissRequest = { pendingDelete = null },
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
                        onDelete(target)
                        pendingDelete = null
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
            .clip(RoundedCornerShape(Tokens.radiusLg))
            .border(1.dp, Tokens.colorBorderLight, RoundedCornerShape(Tokens.radiusLg))
            .background(Tokens.colorWhite)
            .clickable(onClick = onClick)
            .padding(Tokens.spacing150)
            .testTag("companion_trip_list_item"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 城市縮圖占位（場景圖 API 尚未提供）
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(Tokens.radiusMd))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Tokens.colorBackgroundPrimaryMedium,
                            Tokens.colorBackgroundPrimaryDarker,
                        ),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                trip.city.take(2),
                fontWeight = FontWeight(Tokens.fontWeightBold),
                fontSize = Tokens.fontSize4,
                color = Tokens.colorWhite,
            )
        }
        Spacer(Modifier.width(Tokens.spacing150))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                trip.title,
                fontWeight = FontWeight(Tokens.fontWeightBold),
                fontSize = Tokens.fontSize3,
                color = Tokens.colorTextDarker,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(Tokens.spacing050))
            Text(
                "${trip.city}・共 ${trip.totalDays} 天", // TODO: i18n
                fontSize = Tokens.fontSize2,
                color = Tokens.colorTextMedium,
            )
            if (trip.createdAt > 0) {
                Spacer(Modifier.height(Tokens.spacing025))
                Text(
                    formatSavedAtDate(trip.createdAt),
                    fontSize = Tokens.fontSize1,
                    color = Tokens.colorTextMedium,
                )
            }
        }
        Icon(
            painter = painterResource(Res.drawable.ic_delete_line),
            contentDescription = null,
            tint = Tokens.colorTextMedium,
            modifier = Modifier
                .size(Tokens.dimensionIconSm)
                .clickable(onClick = onDelete)
                .testTag("companion_trip_list_delete_btn"),
        )
    }
}

// 曆法換算抽到 CompanionRootScreen.formatEpochMillisAsDate 共用（見該處說明）。
private fun formatSavedAtDate(epochMillis: Long): String = "${formatEpochMillisAsDate(epochMillis)} 儲存"
