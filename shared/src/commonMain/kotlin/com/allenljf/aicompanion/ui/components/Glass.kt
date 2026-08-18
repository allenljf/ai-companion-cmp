package com.allenljf.aicompanion.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.allenljf.aicompanion.theme.Tokens

/**
 * 液態玻璃視覺層：半透明疊層＋左上高光模擬玻璃質感，不做真的 backdrop blur
 * （效能與跨平台一致性優先，demo 不追求 pixel-perfect）。顏色只用 Color.White／既有品牌色
 * 疊 alpha，不新增 Tokens.kt 以外的色相——Tokens.kt 是機械轉錄合約，玻璃樣式不進去那份清單。
 */
object GlassStyle {
    val fillClear = Color.White.copy(alpha = 0.55f)

    // 文字密集內容（列表、bottom sheet 內文）用的高不透明玻璃——半透明太低會影響閱讀
    val fillReadable = Color.White.copy(alpha = 0.88f)
    val borderColor = Color.White.copy(alpha = 0.6f)

    fun fillTinted(base: Color, alpha: Float = 0.72f) = base.copy(alpha = alpha)

    val highlightBrush = Brush.linearGradient(
        colors = listOf(Color.White.copy(alpha = 0.35f), Color.White.copy(alpha = 0f)),
        start = Offset.Zero,
        end = Offset.Infinite,
    )
}

/** 卡片/按鈕/bottom sheet 共用的玻璃底：clip → 玻璃色底 → 高光疊層 → 半透明白邊框。 */
fun Modifier.glassSurface(
    shape: Shape = RoundedCornerShape(Tokens.radiusXl),
    fill: Color = GlassStyle.fillClear,
    borderAlpha: Float = 0.6f,
): Modifier = this
    .clip(shape)
    .background(fill)
    .background(GlassStyle.highlightBrush)
    .border(1.dp, Color.White.copy(alpha = borderAlpha), shape)

/**
 * 全頁襯底斜向漸層，取代純白/純灰底——玻璃層要有色彩襯底才顯得出半透明效果，
 * 純白底上疊半透明白色等於看不到玻璃感。只用既有品牌淺色 token，不發明新色相。
 */
fun Modifier.appGradientBackdrop(): Modifier = this.background(
    Brush.linearGradient(
        colors = listOf(
            Tokens.colorBackgroundPrimaryLighter,
            Tokens.colorBackgroundPrimaryLight,
            Tokens.colorBackgroundHighlightLighter,
        ),
        start = Offset.Zero,
        end = Offset.Infinite,
    ),
)
