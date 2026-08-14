package com.allenljf.aicompanion.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import com.allenljf.aicompanion.theme.Tokens

// 原 DialogHeaderType 是 sealed class（Text / TextWithImage 兩種 header），
// 畫面只用到 Text 這個分支（見呼叫點），依 YAGNI 只實作這個 variant，保留 sealed 外殼方便日後擴充。
sealed class DialogHeaderType {
    data class Text(val title: String, val useScrollableContent: Boolean = false) : DialogHeaderType()
}

/**
 * 輕量版 KKDialog——只做畫面實際用到的形狀：純文字 header + 內容 + 左右兩顆按鈕的水平 footer。
 * 不搬 buttonArrangement（VERTICAL 分支）、headerIconButton、onClickSecondaryButton、
 * isDismissOnClickOutside 等沒被呼叫的參數；也不做 footer 陰影/捲動偵測等內部視覺細節。
 */
@Composable
fun AppDialog(
    headerType: DialogHeaderType,
    showFooterShadow: Boolean = true,
    showHeaderCloseButton: Boolean = true,
    onDismissRequest: (() -> Unit)? = null,
    content: @Composable () -> Unit,
    onClickPrimaryButton: (@Composable () -> Unit)? = null,
    onClickCancelButton: (@Composable () -> Unit)? = null,
) {
    Dialog(onDismissRequest = { onDismissRequest?.invoke() }) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .clip(RoundedCornerShape(Tokens.radiusXl))
                .background(Tokens.colorWhite),
        ) {
            val title = when (headerType) {
                is DialogHeaderType.Text -> headerType.title
            }
            if (title.isNotBlank() || showHeaderCloseButton) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = Tokens.spacing200,
                            top = Tokens.spacing200,
                            end = Tokens.spacing200,
                            bottom = Tokens.spacing150,
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = title,
                        fontSize = Tokens.fontSize4,
                        fontWeight = FontWeight.Medium,
                        color = Tokens.colorTextDarker,
                        modifier = Modifier.weight(1f),
                    )
                    if (showHeaderCloseButton) {
                        Box(
                            modifier = Modifier
                                .size(Tokens.dimensionIconMd)
                                .clip(RoundedCornerShape(50))
                                .background(Tokens.colorBackgroundSurfaceLight)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                ) { onDismissRequest?.invoke() },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "×",
                                fontSize = Tokens.fontSize4,
                                color = Tokens.colorTextDarker,
                            )
                        }
                    }
                }
            }

            Box(modifier = Modifier.padding(bottom = Tokens.spacing100)) {
                content()
            }

            if (onClickPrimaryButton != null || onClickCancelButton != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Tokens.colorWhite)
                        .padding(Tokens.spacing300),
                    horizontalArrangement = Arrangement.spacedBy(Tokens.spacing200),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (onClickCancelButton != null) {
                        Box(modifier = Modifier.weight(1f)) {
                            onClickCancelButton()
                        }
                    }
                    if (onClickPrimaryButton != null) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            onClickPrimaryButton()
                        }
                    }
                }
            }
        }
    }
}
