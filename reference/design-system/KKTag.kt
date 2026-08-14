package com.kkday.design.kkTag

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kkday.design.R
import com.kkday.design.StyleDictionary
import com.kkday.design.StyleDictionary.kkDimensionIcon2xs
import com.kkday.design.StyleDictionary.kkDimensionIconXs
import com.kkday.design.StyleDictionary.kkRadiusSm
import com.kkday.design.StyleDictionary.kkSpacing050
import com.kkday.design.font.color
import com.kkday.design.font.fontBodySm
import com.kkday.design.kkTag.KKTagColor.Companion.resolveTintOrText
import com.kkday.design.utils.shadowFixed
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

/**
 * - 依據 [kkTagColor] 套用背景與文字色（支援 Solid / Subtle / Media / Custom）。
 * - 可選擇性加入左右圖示（[kkTagIcons]）；除 Custom 外，icon tint 預設強制使用文字色。
 * - 可設定 [maxWidth]，超出寬度時會以省略號截斷文字。
 * - 支援點擊事件（[onTagClick]）。
 * - 搭配 [hazeState] 可實現「背後模糊 + 半透明」的材質效果
 *
 * @param maxWidth 標籤最大寬度；`null` 表示不限制寬度
 * @param text 標籤顯示的文字內容
 * @param kkTagColor 標籤的色票設定（決定背景色/文字色/透明度/邊框）
 * @param kkTagIcons 可選的左右圖示設定；除 Custom 外，tint 會預設為文字色
 * @param hazeState 若 [kkTagColor.shouldAddBlur] 為 `true` 時須提供，
 *        且其背景元件要設定 `.hazeSource(hazeState)` 否則不會有 Blur 效果。可參考 KKTagPreviewSection 作法
 * @param onTagClick 標籤點擊事件回呼；預設為空實作
 * @param cornerShape 标签自定义圆角
 */
@Composable
fun KKTag(
    maxWidth: Dp? = null,
    text: String,
    kkTagColor: KKTagColor,
    kkTagIcons: KKTagIcons? = null,
    hazeState: HazeState? = null,
    onTagClick: () -> Unit = {},
    cornerShape: RoundedCornerShape? = null
) {
    val leadingTint = kkTagIcons?.leadingIconTintRes
        .let { kkTagColor.resolveTintOrText(it) }
    val interactiveTint = kkTagIcons?.interactiveIconTintRes
        .let { kkTagColor.resolveTintOrText(it) }

    KKBaseTag(
        maxWidth = maxWidth,
        text = text,
        textColor = colorResource(kkTagColor.textColorRes),
        backgroundColor = colorResource(kkTagColor.backgroundColorRes)
            .copy(alpha = kkTagColor.backgroundAlpha),
        leftIcon = kkTagIcons?.leadingIconRes,
        leftIconTint = leadingTint?.let { colorResource(it) },
        rightIcon = kkTagIcons?.interactiveIconRes,
        rightIconTint = interactiveTint?.let { colorResource(it) },
        shouldAddBlur = kkTagColor.shouldAddBlur,
        hazeState = hazeState,
        onTagClick = onTagClick,
        cornerShape = cornerShape,
        borderColor = kkTagColor.borderColorRes?.let {
            colorResource(it).copy(alpha = kkTagColor.borderAlpha)
        },
        borderWidth = kkTagColor.borderWidthDp.dp,
        shouldAddShadow = kkTagColor.shouldAddShadow
    )
}

@Preview
@Composable
private fun PreviewKKTagList() {
    Column(
        verticalArrangement = spacedBy(8.dp)
    ) {
        KKTag(
            maxWidth = 100.dp,
            text = "ABCCCFFF",
            kkTagColor = KKTagSolidColor.RED,
            kkTagIcons = KKTagIcons(
                leadingIconRes = R.drawable.ic_deals_fill,
            ),
            onTagClick = {}
        )

        KKTag(
            maxWidth = 100.dp,
            text = "限時優惠",
            kkTagColor = KKTagMediaColor.DARK,
            onTagClick = {}
        )

        KKTag(
            maxWidth = null,
            text = "限時優惠",
            kkTagColor = KKTagSubtleColor.CYAN,
            kkTagIcons = KKTagIcons(
                leadingIconRes = R.drawable.ic_deals_fill,
                interactiveIconRes = R.drawable.ic_info_line
            ),
            onTagClick = {}
        )

        KKTag(
            maxWidth = 100.dp,
            text = "限時優惠限時優惠限時優惠",
            kkTagColor = KKTagColor.KKTagCustomColor(
                backgroundColorRes = R.color.kk_color_background_info_medium,
                textColorRes = R.color.kk_color_white
            ),
            kkTagIcons = KKTagIcons(
                leadingIconRes = R.drawable.ic_deals_fill,
                interactiveIconRes = R.drawable.ic_airport_transfer_line,
                interactiveIconTintRes = R.color.kk_color_white
            ),
            onTagClick = {}
        )

        KKTag(
            maxWidth = 100.dp,
            text = "限時優惠限時優惠限時優惠",
            kkTagColor = KKTagMediaLightColor.LIGHT,
            kkTagIcons = KKTagIcons(
                leadingIconRes = R.drawable.ic_deals_fill,
                interactiveIconRes = R.drawable.ic_airport_transfer_line,
                interactiveIconTintRes = R.color.kk_color_white
            ),
            onTagClick = {}
        )
    }
}

/**
 * KKBaseTag
 *
 * 通用標籤元件，用於顯示提示或標示資訊，例如「限時優惠」、「新上架」等。
 *
 * 支援文字、左右圖示、自訂顏色、邊框與點擊事件，適合用於列表標籤、功能標籤等場景。
 *
 * @param maxWidth 標籤最大寬度，預設為 null 表示不限制
 * @param text 標籤顯示的文字內容
 * @param textColor 文字顏色
 * @param backgroundColor 標籤背景顏色
 * @param leftIcon 可選的起始圖示資源 ID（顯示在文字左側）
 * @param leftIconTint 起始圖示的顏色，預設為 null 則使用原始圖示色
 * @param rightIcon 可選的結尾圖示資源 ID（顯示在文字右側）
 * @param rightIconTint 結尾圖示的顏色，預設為 null 則使用原始圖示色
 * @param shouldAddBlur 是否啟用背景模糊效果
 * @param hazeState 背景模糊狀態，需搭配 shouldAddBlur 使用
 * @param onTagClick Tag 的點擊事件，預設為空操作
 * @param cornerShape 標籤自定義圓角
 * @param borderColor 邊框顏色，預設為 null 表示無邊框
 * @param borderWidth 邊框寬度，預設為 0.dp
 */
@Composable
fun KKBaseTag(
    maxWidth: Dp? = null,
    text: String,
    textColor: Color,
    backgroundColor: Color,
    leftIcon: Int? = null,
    leftIconTint: Color? = null,
    rightIcon: Int? = null,
    rightIconTint: Color? = null,
    shouldAddBlur: Boolean = false,
    hazeState: HazeState? = null,
    onTagClick: () -> Unit = {},
    cornerShape: RoundedCornerShape? = null,
    borderColor: Color? = null,
    borderWidth: Dp = 0.dp,
    shouldAddShadow: Boolean = false,
) {
    val shape = cornerShape ?: RoundedCornerShape(kkRadiusSm)

    Row(
        modifier = Modifier
            .then(if (maxWidth != null) Modifier.widthIn(max = maxWidth) else Modifier)
            .then(if (shouldAddShadow) Modifier.shadowFixed() else Modifier)
            .clip(shape)
            .then(
                if (shouldAddBlur && hazeState != null) {
                    Modifier.hazeEffect(hazeState) {
                        blurRadius = 10.dp
                        tints = listOf(HazeTint(color = backgroundColor))
                    }
                } else {
                    Modifier.background(color = backgroundColor, shape = shape)
                }
            )
            .then(
                if (borderColor != null && borderWidth > 0.dp) {
                    Modifier.border(width = borderWidth, color = borderColor, shape = shape)
                } else {
                    Modifier
                }
            )
            .padding(horizontal = kkSpacing050, vertical = StyleDictionary.kkSpacing025)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTagClick
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leftIcon != null) {
            BulletIcon(
                modifier = Modifier.padding(end = kkSpacing050),
                iconRes = leftIcon,
                iconSize = kkDimensionIconXs,
                iconColor = leftIconTint,
                textStyle = fontBodySm
            )
        }

        val annotatedString = buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    fontSize = fontBodySm.fontSize,
                    textGeometricTransform = TextGeometricTransform(scaleX = 0.01f)
                )
            ) {
                append("　")
            }
        }

        Text(
            modifier = Modifier
                .weight(weight = 1f, fill = false)
                .offset(y = (-1).dp),
            text = annotatedString + AnnotatedString(text),
            maxLines = 1,
            style = fontBodySm.color(textColor),
            overflow = TextOverflow.Ellipsis,
        )

        if (rightIcon != null) {
            BulletIcon(
                modifier = Modifier.padding(start = kkSpacing050),
                iconRes = rightIcon,
                iconSize = kkDimensionIcon2xs,
                iconColor = rightIconTint,
                textStyle = fontBodySm
            )
        }
    }
}

@Composable
private fun BulletIcon(
    modifier: Modifier = Modifier,
    @DrawableRes iconRes: Int,
    iconSize: Dp,
    iconColor: Color?,
    textStyle: TextStyle,
    alignLanguage: Boolean = true   //如果有這個的話, 會加一個全形空白, 這樣英文中文高度都一樣
) {
    val fontSizeSp = textStyle.fontSize
    val annotatedString = buildAnnotatedString {
        if (alignLanguage) {
            //這邊要加一個跟右邊文字一樣大的空白, icon 才能跟文字對齊
            withStyle(
                style = SpanStyle(
                    fontSize = fontSizeSp,
                    textGeometricTransform = TextGeometricTransform(scaleX = 0.01f)
                )
            ) {
                append("　")
            }
        }
        appendInlineContent(id = "bullet")
    }

    val iconSizeInSp = with(LocalDensity.current) { iconSize.toSp() }

    val inlineContentMap = mapOf(
        "bullet" to InlineTextContent(
            Placeholder(iconSizeInSp, iconSizeInSp, PlaceholderVerticalAlign.TextCenter)
        ) {
            val colorFilter = if (iconColor == null) null else ColorFilter.tint(iconColor)

            Image(
                painter = painterResource(id = iconRes),
                modifier = Modifier.fillMaxSize(),
                contentDescription = "",
                colorFilter = colorFilter
            )
        })

    Text(
        text = annotatedString,
        inlineContent = inlineContentMap,
        style = textStyle,
        modifier = modifier
            .wrapContentWidth()
            .offset(y = (-1).dp),
        fontSize = fontSizeSp
    )
}