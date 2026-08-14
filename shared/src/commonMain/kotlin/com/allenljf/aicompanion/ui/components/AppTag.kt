package com.allenljf.aicompanion.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import com.allenljf.aicompanion.theme.Tokens

// 原 KKTagColor 是 sealed interface，底下有 Solid/Subtle/Media/Custom 四種色票族；
// 畫面只用到 KKTagSolidColor 的 RED / CYAN / AMBER 三色（見呼叫點），依 YAGNI 只做這個子集。
enum class AppTagSolidColor(val backgroundColor: Color, val textColor: Color) {
    RED(Tokens.colorBackgroundCriticalMedium, Tokens.colorWhite),
    CYAN(Tokens.colorBackgroundPrimaryMedium, Tokens.colorWhite),
    AMBER(Tokens.colorBackgroundHighlightDarker, Tokens.colorWhite),
}

/**
 * 輕量版 KKTag——只支援純文字 + 純色背景（不含 icon slot / haze 模糊 / 自訂圓角，畫面沒用到）。
 */
@Composable
fun AppTag(
    text: String,
    tagColor: AppTagSolidColor,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(Tokens.radiusSm))
            .background(color = tagColor.backgroundColor)
            .padding(horizontal = Tokens.spacing050, vertical = Tokens.spacing025),
    ) {
        Text(
            text = text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = Tokens.fontSize1,
            color = tagColor.textColor,
        )
    }
}
