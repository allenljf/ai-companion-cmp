package com.allenljf.aicompanion.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextDecoration
import com.allenljf.aicompanion.theme.Tokens

// 原 KKButton 支援 11 種 ButtonType / 4 種 ButtonState / 5 種 ButtonSizeType，
// The app currently uses the following button variants.
// 依 YAGNI 只實作這些 variant。

enum class ButtonType {
    PRIMARY, SECONDARY, PRIMARY_SUBTLE, TEXT_SECONDARY
}

enum class ButtonState {
    ENABLED, DISABLED
}

enum class ButtonSizeType {
    Sm, Md, Lg
}

/**
 * 輕量版 KKButton——用 Material3 Button/OutlinedButton/TextButton 打底，Tokens 上色。
 * 不重刻原本的 ConstraintLayout + 自訂 press/loading 動畫，Material3 本身的互動狀態已足夠。
 *
 * leadingIcon 原為 @DrawableRes Int（Android 專屬資源系統），KMP 無法沿用，
 * 改用跨平台的 Painter；呼叫端自行用 painterResource 等方式提供。
 */
@Composable
fun AppButton(
    buttonText: String,
    buttonType: ButtonType,
    buttonState: ButtonState,
    buttonSizeType: ButtonSizeType,
    onClick: () -> Unit,
    isFullWidth: Boolean = false,
    leadingIcon: Painter? = null,
) {
    val enabled = buttonState == ButtonState.ENABLED
    val alphaScale = if (enabled) 1f else 0.5f
    val shape = RoundedCornerShape(Tokens.radiusXl)
    val widthModifier = if (isFullWidth) Modifier.fillMaxWidth() else Modifier
    val fontSize = if (buttonSizeType == ButtonSizeType.Sm) Tokens.fontSize2 else Tokens.fontSize3
    val iconSize = if (buttonSizeType == ButtonSizeType.Sm) Tokens.dimensionIconXs else Tokens.dimensionIconSm
    val contentPadding = when (buttonSizeType) {
        ButtonSizeType.Sm -> PaddingValues(horizontal = Tokens.spacing150, vertical = Tokens.spacing075)
        ButtonSizeType.Md -> PaddingValues(horizontal = Tokens.spacing200, vertical = Tokens.spacing100)
        ButtonSizeType.Lg -> PaddingValues(horizontal = Tokens.spacing300, vertical = Tokens.spacing150)
    }

    val content: @Composable () -> Unit = {
        if (leadingIcon != null) {
            Icon(painter = leadingIcon, contentDescription = null, modifier = Modifier.size(iconSize))
            Spacer(Modifier.width(Tokens.spacing050))
        }
        Text(
            text = buttonText,
            fontSize = fontSize,
            textDecoration = if (buttonType == ButtonType.TEXT_SECONDARY) TextDecoration.Underline else null,
        )
    }

    when (buttonType) {
        ButtonType.PRIMARY -> Button(
            onClick = onClick,
            enabled = enabled,
            shape = shape,
            modifier = widthModifier.glassSurface(
                shape = shape,
                fill = GlassStyle.fillTinted(Tokens.colorBackgroundPrimaryButton, alpha = 0.72f * alphaScale),
            ),
            contentPadding = contentPadding,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Tokens.colorWhite,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = Tokens.colorWhite.copy(alpha = 0.5f),
            ),
            content = { content() },
        )

        ButtonType.SECONDARY -> OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            shape = shape,
            modifier = widthModifier.glassSurface(shape = shape, borderAlpha = 0.6f * alphaScale),
            contentPadding = contentPadding,
            border = null,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Tokens.colorTextDarker,
                disabledContentColor = Tokens.colorTextDarker.copy(alpha = 0.4f),
            ),
            content = { content() },
        )

        ButtonType.PRIMARY_SUBTLE -> OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            shape = shape,
            modifier = widthModifier.glassSurface(
                shape = shape,
                fill = GlassStyle.fillTinted(Tokens.colorBackgroundPrimaryLight, alpha = 0.5f * alphaScale),
                borderAlpha = 0.6f * alphaScale,
            ),
            contentPadding = contentPadding,
            border = null,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Tokens.colorTextPrimaryDark,
                disabledContentColor = Tokens.colorTextPrimaryDark.copy(alpha = 0.4f),
            ),
            content = { content() },
        )

        ButtonType.TEXT_SECONDARY -> TextButton(
            onClick = onClick,
            enabled = enabled,
            modifier = widthModifier,
            contentPadding = contentPadding,
            colors = ButtonDefaults.textButtonColors(
                contentColor = Tokens.colorTextDarker,
                disabledContentColor = Tokens.colorTextDark.copy(alpha = 0.4f),
            ),
            content = { content() },
        )
    }
}
