package com.kkday.design.button

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.kkday.design.R
import com.kkday.design.StyleDictionary
import com.kkday.design.font.fontSubtitleLg
import com.kkday.design.font.fontSubtitleMd

@Composable
fun KKButton(
    buttonText: String,
    buttonType: ButtonType,
    buttonState: ButtonState,
    buttonSizeType: ButtonSizeType,
    onClick: () -> Unit,
    onClickAtAnyState: () -> Unit = {},
    isFullWidth: Boolean = false,
    @DrawableRes leadingIcon: Int? = null,
    @DrawableRes trailingIcon: Int? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPress = interactionSource.collectIsPressedAsState()
    val state = if (isPress.value && buttonState != ButtonState.DISABLED && buttonState != ButtonState.LOADING) {
        ButtonState.PRESSED
    } else {
        buttonState
    }
    val buttonSize = when (buttonType) {
        ButtonType.TEXT_PRIMARY, ButtonType.TEXT_SECONDARY -> ButtonSizeType.NA.buttonSize
        else -> buttonSizeType.buttonSize
    }

    val btnTypeTwo = ButtonTypeWithState.convertToButtonTypeWithState(
        buttonType = buttonType,
        buttonState = state
    )
    val typeModifier = btnTypeTwo.modifier
    val textColor = btnTypeTwo.textColor
    val textStyle = when (buttonSizeType) {
        ButtonSizeType.Lg, ButtonSizeType.Md -> fontSubtitleLg
        else -> fontSubtitleMd
    }
    val decorateUnderLine = buttonType == ButtonType.TEXT_SECONDARY && leadingIcon == null && trailingIcon == null
    val iconSize = when (buttonSizeType) {
        ButtonSizeType.Lg, ButtonSizeType.Md -> StyleDictionary.kkDimensionIconSm
        else -> StyleDictionary.kkDimensionIconXs
    }
    val onClickListener: () -> Unit = if (buttonState == ButtonState.ENABLED || buttonState == ButtonState.PRESSED) {
        onClick
    } else {
        onClickAtAnyState
    }
    ConstraintLayout(
        modifier = typeModifier
            .clickable(
                interactionSource = interactionSource, indication = null, onClick = onClickListener,
            )
            .run {
                if (isFullWidth) fillMaxWidth() else wrapContentWidth()
            }
    ) {
        val (button, loading) = createRefs()
        Row(
            modifier = Modifier
                .constrainAs(button) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                    width = if (isFullWidth) Dimension.fillToConstraints else Dimension.wrapContent
                    height = Dimension.wrapContent
                }
                .padding(
                    vertical = buttonSize.verticalPadding,
                )
                .padding(
                    start = if (leadingIcon != null) getIconPadding(buttonSizeType = buttonSizeType) else buttonSize.horizontalPadding,
                    end = if (trailingIcon != null) getIconPadding(buttonSizeType = buttonSizeType) else buttonSize.horizontalPadding,
                )
                .alpha(alpha = if (buttonState == ButtonState.LOADING) 0F else 1F),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {

            if (leadingIcon != null) {
                Image(
                    modifier = Modifier
                        .padding(end = StyleDictionary.kkSpacing050)
                        .size(iconSize),
                    painter = painterResource(id = leadingIcon),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(textColor),
                    alignment = Alignment.Center,
                )
            }
            Text(
                modifier = Modifier
                    .weight(1F, false)
                    .wrapContentSize(align = Alignment.Center),
                text = buttonText,
                style = LocalTextStyle.current.merge(
                    textStyle.copy(
                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                    )
                ),
                color = textColor,
                textAlign = TextAlign.Center,
                textDecoration = if (decorateUnderLine) TextDecoration.Underline else null,
            )

            if (trailingIcon != null) {
                Image(
                    modifier = Modifier
                        .padding(start = StyleDictionary.kkSpacing050)
                        .size(iconSize),
                    painter = painterResource(id = trailingIcon),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(textColor),
                    alignment = Alignment.Center,
                )
            }
        }

        Box(modifier = Modifier.constrainAs(loading) {
            start.linkTo(button.start)
            top.linkTo(button.top)
            end.linkTo(button.end)
            bottom.linkTo(button.bottom)
        }) {
            if (buttonState == ButtonState.LOADING) {
                LoadingCircleAnimation(
                    tintColor = textColor,
                    buttonSizeType = buttonSizeType,
                )
            }
        }
    }
}

@Composable
fun LoadingCircleAnimation(tintColor: Color, buttonSizeType: ButtonSizeType) {
    val infiniteTransition = rememberInfiniteTransition(label = STRING_TRANSITION)
    val angle by infiniteTransition.animateFloat(
        initialValue = CIRCULAR_ANIMATION_START_DEGREE,
        targetValue = CIRCULAR_ANIMATION_END_DEGREE,
        animationSpec = infiniteRepeatable(
            animation = tween(CIRCULAR_ANIMATION_DURATION_IN_MILLIS, easing = LinearEasing),
        ),
        label = STRING_ROTATION,
    )
    val iconSize = if (buttonSizeType == ButtonSizeType.Lg || buttonSizeType == ButtonSizeType.Md) {
        StyleDictionary.kkDimensionIconMd
    } else {
        StyleDictionary.kkDimensionIconSm
    }
    Box(
        modifier = Modifier
            .wrapContentSize()
    ) {
        Image(
            modifier = Modifier
                .size(iconSize)
                .rotate(angle),
            painter = painterResource(id = R.drawable.ic_loading_line_semibold),
            contentDescription = "",
            colorFilter = ColorFilter.tint(color = tintColor)
        )
    }
}

@Preview
@Composable
fun PreviewKKButton() {
    Column(
        modifier = Modifier
            .background(Color.White)
            .padding(StyleDictionary.kkSpacing150),
        verticalArrangement = Arrangement.spacedBy(StyleDictionary.kkSpacing150)
    ) {
        val state = ButtonState.ENABLED

        KKButton(
            buttonText = "Button",
            buttonType = ButtonType.PRIMARY,
            buttonState = state,
            buttonSizeType = ButtonSizeType.Md,
            onClick = {},
            isFullWidth = false,
            leadingIcon = R.drawable.ic_download_line_semibold,
            trailingIcon = R.drawable.ic_arrow_right_line_semibold,
        )

        KKButton(
            buttonText = "Many Text Many Text Many Text Many Text Many Text Many Text Many Text Many Text Many Text Many Text Many Text Many Text",
            buttonType = ButtonType.ACCENT,
            buttonState = state,
            buttonSizeType = ButtonSizeType.Md,
            onClick = {},
            isFullWidth = false,
            leadingIcon = R.drawable.ic_download_line_semibold,
            trailingIcon = R.drawable.ic_arrow_right_line_semibold,
        )

        KKButton(
            buttonText = "Button",
            buttonType = ButtonType.PRIMARY_SUBTLE,
            buttonState = state,
            buttonSizeType = ButtonSizeType.Md,
            onClick = {},
            isFullWidth = false,
            leadingIcon = null,
            trailingIcon = null,
        )

        KKButton(
            buttonText = "Button",
            buttonType = ButtonType.CRITICAL,
            buttonState = ButtonState.LOADING,
            buttonSizeType = ButtonSizeType.Md,
            onClick = {},
            isFullWidth = false,
            leadingIcon = null,
            trailingIcon = null,
        )

        Text(text = "Horizontal Equal Weight")
        Row(
            modifier = Modifier
                .background(Color.LightGray.copy(alpha = StyleDictionary.kkOpacity2.toFloat()))
                .padding(StyleDictionary.kkSpacing150),
            horizontalArrangement = Arrangement.spacedBy(StyleDictionary.kkSpacing150)
        ) {
            Box(modifier = Modifier.weight(1F)) {
                KKButton(
                    buttonText = "Button",
                    buttonType = ButtonType.PRIMARY,
                    buttonState = ButtonState.ENABLED,
                    buttonSizeType = ButtonSizeType.Md,
                    onClick = {},
                    isFullWidth = true,
                    leadingIcon = null,
                    trailingIcon = null,
                )
            }

            Box(modifier = Modifier.weight(1F)) {
                KKButton(
                    buttonText = "Button",
                    buttonType = ButtonType.ACCENT,
                    buttonState = ButtonState.ENABLED,
                    buttonSizeType = ButtonSizeType.Md,
                    onClick = {},
                    isFullWidth = true,
                    leadingIcon = null,
                    trailingIcon = null,
                )
            }
        }

        Text(text = "Vertical Equal Weight")
        Column(
            modifier = Modifier
                .background(Color.LightGray.copy(alpha = StyleDictionary.kkOpacity2.toFloat()))
                .padding(StyleDictionary.kkSpacing150),
            verticalArrangement = Arrangement.spacedBy(StyleDictionary.kkSpacing150)
        ) {
            KKButton(
                buttonText = "Button",
                buttonType = ButtonType.PRIMARY,
                buttonState = ButtonState.ENABLED,
                buttonSizeType = ButtonSizeType.Md,
                onClick = {},
                isFullWidth = true,
                leadingIcon = null,
                trailingIcon = null,
            )

            KKButton(
                buttonText = "Button",
                buttonType = ButtonType.ACCENT,
                buttonState = ButtonState.ENABLED,
                buttonSizeType = ButtonSizeType.Md,
                onClick = {},
                isFullWidth = true,
                leadingIcon = null,
                trailingIcon = null,
            )
        }
    }
}

enum class ButtonState {
    ENABLED, PRESSED, DISABLED, LOADING
}

enum class ButtonSizeType(val buttonSize: ButtonSize) {
    Xs(
        ButtonSize(
            horizontalPadding = StyleDictionary.kkSpacing150,
            verticalPadding = StyleDictionary.kkSpacing050
        )
    ),
    Sm(
        ButtonSize(
            horizontalPadding = StyleDictionary.kkSpacing150,
            verticalPadding = 6.dp
        )
    ),
    Md(
        ButtonSize(
            horizontalPadding = StyleDictionary.kkSpacing250,
            verticalPadding = StyleDictionary.kkSpacing100
        )
    ),
    Lg(
        ButtonSize(
            horizontalPadding = StyleDictionary.kkSpacing300,
            verticalPadding = StyleDictionary.kkSpacing150
        )
    ),
    NA(ButtonSize(0.dp, 0.dp))
}

private fun getIconPadding(buttonSizeType: ButtonSizeType): Dp {
    return when (buttonSizeType) {
        ButtonSizeType.Lg -> {
            StyleDictionary.kkSpacing200
        }

        ButtonSizeType.Md -> {
            StyleDictionary.kkSpacing150
        }

        else -> {
            StyleDictionary.kkSpacing100
        }
    }
}

data class ButtonSize(
    val horizontalPadding: Dp, val verticalPadding: Dp
)

enum class ButtonType {
    PRIMARY,
    ACCENT,
    CRITICAL,
    PRIMARY_SUBTLE,
    CRITICAL_SUBTLE,
    SECONDARY,
    TEXT_PRIMARY,
    TEXT_SECONDARY,
    WHITE,
    SEMI_TRANSPARENT,
    SURFACE // 新增 Surface 类型
}

private sealed class ButtonTypeWithState(protected open val buttonState: ButtonState) {
    abstract val modifier: Modifier
    abstract val textColor: Color

    data class Primary(override val buttonState: ButtonState): ButtonTypeWithState(buttonState) {
        override val modifier: Modifier
            get() {
                return when (buttonState) {
                    ButtonState.ENABLED -> {
                        Modifier
                            .background(
                                color = StyleDictionary.kkColorBackgroundPrimaryButton,
                                shape = RoundedCornerShape(StyleDictionary.kkRadiusMd)
                            )
                    }

                    ButtonState.PRESSED -> {
                        Modifier.background(
                            color = StyleDictionary.kkColorBackgroundPrimaryDark,
                            shape = RoundedCornerShape(StyleDictionary.kkRadiusMd)
                        )
                    }

                    else -> {
                        Modifier.background(
                            color = StyleDictionary.kkColorBackgroundPrimaryButton.copy(alpha = StyleDictionary.kkOpacity5.toFloat()),
                            shape = RoundedCornerShape(StyleDictionary.kkRadiusMd)
                        )
                    }
                }
            }

        override val textColor: Color
            get() {
                return Color.White.copy(alpha = if (buttonState == ButtonState.DISABLED || buttonState == ButtonState.LOADING) StyleDictionary.kkOpacity9.toFloat() else 1F)
            }
    }

    data class Accent(override val buttonState: ButtonState):
        ButtonTypeWithState(buttonState) {
        override val modifier: Modifier
            get() {
                return when (buttonState) {
                    ButtonState.ENABLED -> {
                        Modifier.background(
                            color = StyleDictionary.kkColorBackgroundInfoMedium,
                            shape = RoundedCornerShape(StyleDictionary.kkRadiusMd),
                        )
                    }

                    ButtonState.PRESSED -> {
                        Modifier.background(
                            color = StyleDictionary.kkColorBackgroundInfoDark,
                            shape = RoundedCornerShape(StyleDictionary.kkRadiusMd)
                        )
                    }

                    else -> {
                        Modifier.background(
                            color = StyleDictionary.kkColorBackgroundInfoMedium.copy(alpha = StyleDictionary.kkOpacity5.toFloat()),
                            shape = RoundedCornerShape(StyleDictionary.kkRadiusMd)
                        )
                    }
                }
            }

        override val textColor: Color
            get() = Color.White.copy(alpha = if (buttonState == ButtonState.DISABLED || buttonState == ButtonState.LOADING) StyleDictionary.kkOpacity9.toFloat() else 1F)
    }

    data class Critical(override val buttonState: ButtonState): ButtonTypeWithState(buttonState) {
        override val modifier: Modifier
            get() {
                return when (buttonState) {
                    ButtonState.ENABLED -> {
                        Modifier.background(
                            color = StyleDictionary.kkColorBackgroundCriticalMedium,
                            shape = RoundedCornerShape(StyleDictionary.kkRadiusMd),
                        )
                    }

                    ButtonState.PRESSED -> {
                        Modifier.background(
                            color = StyleDictionary.kkColorBackgroundCriticalDark,
                            shape = RoundedCornerShape(StyleDictionary.kkRadiusMd)
                        )
                    }

                    else -> {
                        Modifier.background(
                            color = StyleDictionary.kkColorBackgroundCriticalMedium.copy(alpha = StyleDictionary.kkOpacity5.toFloat()),
                            shape = RoundedCornerShape(StyleDictionary.kkRadiusMd)
                        )
                    }
                }
            }

        override val textColor: Color
            get() = Color.White.copy(alpha = if (buttonState == ButtonState.DISABLED || buttonState == ButtonState.LOADING) StyleDictionary.kkOpacity9.toFloat() else 1F)
    }

    data class PrimarySubtle(override val buttonState: ButtonState):
        ButtonTypeWithState(buttonState) {
        override val modifier: Modifier
            get() {
                return when (buttonState) {
                    ButtonState.ENABLED -> {
                        Modifier.border(
                            width = 1.dp,
                            color = StyleDictionary.kkColorBorderPrimaryDark,
                            shape = RoundedCornerShape(StyleDictionary.kkRadiusMd),
                        )
                    }

                    ButtonState.PRESSED -> {
                        Modifier
                            .border(
                                width = 1.dp,
                                color = StyleDictionary.kkColorBorderPrimaryDark,
                                shape = RoundedCornerShape(StyleDictionary.kkRadiusMd),
                            )
                            .background(
                                color = StyleDictionary.kkColorBackgroundPrimaryLighter,
                                shape = RoundedCornerShape(StyleDictionary.kkRadiusMd),
                            )
                    }

                    else -> {
                        Modifier.border(
                            width = 1.dp,
                            color = StyleDictionary.kkColorBorderPrimaryDark.copy(alpha = StyleDictionary.kkOpacity4.toFloat()),
                            shape = RoundedCornerShape(StyleDictionary.kkRadiusMd)
                        )
                    }
                }
            }

        override val textColor: Color
            get() = StyleDictionary.kkColorTextPrimaryDark.copy(alpha = if (buttonState == ButtonState.DISABLED || buttonState == ButtonState.LOADING) StyleDictionary.kkOpacity4.toFloat() else 1F)
    }

    data class CriticalSubtle(override val buttonState: ButtonState):
        ButtonTypeWithState(buttonState) {
        override val modifier: Modifier
            get() {
                return when (buttonState) {
                    ButtonState.ENABLED -> {
                        Modifier.border(
                            width = 1.dp,
                            color = StyleDictionary.kkColorBorderCriticalDark,
                            shape = RoundedCornerShape(StyleDictionary.kkRadiusMd),
                        )
                    }

                    ButtonState.PRESSED -> {
                        Modifier
                            .background(
                                color = StyleDictionary.kkColorBackgroundCriticalLighter,
                                shape = RoundedCornerShape(StyleDictionary.kkRadiusMd)
                            )
                            .border(
                                width = 1.dp,
                                color = StyleDictionary.kkColorBorderCriticalDark,
                                shape = RoundedCornerShape(StyleDictionary.kkRadiusMd)
                            )
                    }

                    else -> {
                        Modifier.border(
                            width = 1.dp,
                            color = StyleDictionary.kkColorBorderCriticalDark.copy(alpha = StyleDictionary.kkOpacity4.toFloat()),
                            shape = RoundedCornerShape(StyleDictionary.kkRadiusMd)
                        )
                    }
                }
            }

        override val textColor: Color
            get() = StyleDictionary.kkColorTextCriticalDark.copy(alpha = if (buttonState == ButtonState.DISABLED || buttonState == ButtonState.LOADING) StyleDictionary.kkOpacity4.toFloat() else 1F)
    }

    data class Secondary(override val buttonState: ButtonState): ButtonTypeWithState(buttonState) {
        override val modifier: Modifier
            get() {
                return when (buttonState) {
                    ButtonState.ENABLED -> {
                        Modifier.border(
                            width = 1.dp,
                            color = StyleDictionary.kkColorBorderDark,
                            shape = RoundedCornerShape(StyleDictionary.kkRadiusMd)
                        )
                    }

                    ButtonState.PRESSED -> {
                        Modifier
                            .background(
                                color = StyleDictionary.kkColorBackgroundSurfaceLighter,
                                shape = RoundedCornerShape(StyleDictionary.kkRadiusMd)
                            )
                            .border(
                                width = 1.dp,
                                color = StyleDictionary.kkColorBorderDark,
                                shape = RoundedCornerShape(StyleDictionary.kkRadiusMd)
                            )
                    }

                    else -> {
                        Modifier.border(
                            width = 1.dp,
                            color = StyleDictionary.kkColorBorderDark.copy(alpha = StyleDictionary.kkOpacity4.toFloat()),
                            shape = RoundedCornerShape(StyleDictionary.kkRadiusMd)
                        )
                    }
                }
            }

        override val textColor: Color
            get() = StyleDictionary.kkColorTextDarker.copy(alpha = if (buttonState == ButtonState.DISABLED || buttonState == ButtonState.LOADING) StyleDictionary.kkOpacity4.toFloat() else 1F)
    }

    data class TextPrimary(override val buttonState: ButtonState):
        ButtonTypeWithState(buttonState) {
        override val modifier: Modifier = Modifier
        override val textColor: Color
            get() {
                return when (buttonState) {
                    ButtonState.ENABLED -> StyleDictionary.kkColorTextPrimaryDark
                    ButtonState.PRESSED -> StyleDictionary.kkColorTextPrimaryDarker
                    else -> StyleDictionary.kkColorTextPrimaryDark.copy(alpha = 0.4F)
                }
            }
    }

    data class TextSecondary(override val buttonState: ButtonState):
        ButtonTypeWithState(buttonState) {
        override val modifier: Modifier = Modifier
        override val textColor: Color
            get() = if (buttonState == ButtonState.ENABLED || buttonState == ButtonState.PRESSED) {
                StyleDictionary.kkColorTextDarker
            } else {
                StyleDictionary.kkColorTextDark.copy(alpha = StyleDictionary.kkOpacity4.toFloat())
            }
    }

    data class White(override val buttonState: ButtonState): ButtonTypeWithState(buttonState) {
        override val modifier: Modifier
            get() {
                return when (buttonState) {
                    ButtonState.ENABLED -> {
                        Modifier
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(StyleDictionary.kkRadiusMd)
                            )
                            .border(
                                width = 1.dp,
                                color = StyleDictionary.kkColorBorderLighter,
                                shape = RoundedCornerShape(StyleDictionary.kkRadiusMd)
                            )
                    }

                    ButtonState.PRESSED -> {
                        Modifier
                            .background(
                                color = StyleDictionary.kkColorBackgroundSurfaceLighter,
                                shape = RoundedCornerShape(StyleDictionary.kkRadiusMd)
                            )
                            .border(
                                width = 1.dp,
                                color = StyleDictionary.kkColorBorderLighter,
                                shape = RoundedCornerShape(StyleDictionary.kkRadiusMd)
                            )
                    }

                    else -> {
                        Modifier
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(StyleDictionary.kkRadiusMd)
                            )
                            .border(
                                width = 1.dp,
                                color = StyleDictionary.kkColorBorderLighter.copy(alpha = StyleDictionary.kkOpacity4.toFloat()),
                                shape = RoundedCornerShape(StyleDictionary.kkRadiusMd)
                            )
                    }
                }
            }

        override val textColor: Color
            get() = if (buttonState == ButtonState.ENABLED || buttonState == ButtonState.PRESSED) {
                StyleDictionary.kkColorTextDarker
            } else {
                StyleDictionary.kkColorTextDark.copy(alpha = StyleDictionary.kkOpacity4.toFloat())
            }
    }

    data class SemiTransparent(override val buttonState: ButtonState):
        ButtonTypeWithState(buttonState) {
        override val modifier: Modifier
            get() {
                return when (buttonState) {
                    ButtonState.ENABLED -> {
                        Modifier.background(
                            color = Color.Black.copy(alpha = 0.6F),
                            shape = RoundedCornerShape(StyleDictionary.kkRadiusMd)
                        )
                    }

                    ButtonState.PRESSED -> {
                        Modifier.background(
                            color = Color.Black.copy(alpha = StyleDictionary.kkOpacity7.toFloat()),
                            shape = RoundedCornerShape(StyleDictionary.kkRadiusMd)
                        )
                    }

                    else -> {
                        Modifier.background(
                            color = Color.Black.copy(alpha = StyleDictionary.kkOpacity4.toFloat()),
                            shape = RoundedCornerShape(StyleDictionary.kkRadiusMd)
                        )
                    }
                }
            }

        override val textColor: Color
            get() = Color.White.copy(alpha = if (buttonState == ButtonState.DISABLED || buttonState == ButtonState.LOADING) StyleDictionary.kkOpacity6.toFloat() else 1F)
    }

    data class Surface(override val buttonState: ButtonState): ButtonTypeWithState(buttonState) {
        override val modifier: Modifier
            get() {
                return when (buttonState) {
                    ButtonState.ENABLED, ButtonState.PRESSED -> {
                        Modifier.background(
                            color = StyleDictionary.kkColorBackgroundSurfaceLight,
                            shape = RoundedCornerShape(StyleDictionary.kkRadiusMd)
                        )
                    }
                    else -> {
                        Modifier.background(
                            color = StyleDictionary.kkColorBackgroundSurfaceLight.copy(alpha = StyleDictionary.kkOpacity4.toFloat()),
                            shape = RoundedCornerShape(StyleDictionary.kkRadiusMd)
                        )
                    }
                }
            }

        override val textColor: Color
            get() = StyleDictionary.kkColorTextDarker.copy(alpha = if (buttonState == ButtonState.DISABLED || buttonState == ButtonState.LOADING) StyleDictionary.kkOpacity4.toFloat() else 1F)
    }

    companion object {
        fun convertToButtonTypeWithState(
            buttonType: ButtonType,
            buttonState: ButtonState
        ): ButtonTypeWithState =
            when (buttonType) {
                ButtonType.PRIMARY -> Primary(buttonState)
                ButtonType.ACCENT -> Accent(buttonState)
                ButtonType.CRITICAL -> Critical(buttonState)
                ButtonType.PRIMARY_SUBTLE -> PrimarySubtle(buttonState)
                ButtonType.CRITICAL_SUBTLE -> CriticalSubtle(buttonState)
                ButtonType.SECONDARY -> Secondary(buttonState)
                ButtonType.TEXT_PRIMARY -> TextPrimary(buttonState)
                ButtonType.TEXT_SECONDARY -> TextSecondary(buttonState)
                ButtonType.WHITE -> White(buttonState)
                ButtonType.SEMI_TRANSPARENT -> SemiTransparent(buttonState)
                ButtonType.SURFACE -> Surface(buttonState) // 新增 Surface 类型
            }
    }
}

private const val CIRCULAR_ANIMATION_START_DEGREE = 0F
private const val CIRCULAR_ANIMATION_END_DEGREE = 360F
private const val CIRCULAR_ANIMATION_DURATION_IN_MILLIS = 1000
private const val STRING_TRANSITION = "Transition"
private const val STRING_ROTATION = "Rotation"