package com.kkday.design.textfield

import android.graphics.Rect
import android.view.View
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.kkday.design.R
import com.kkday.design.StyleDictionary
import com.kkday.design.common.LabelText
import com.kkday.design.form.message.KKFormMessage
import com.kkday.design.form.message.KKFormMessageSpacing
import com.kkday.design.form.message.KKFormMessageVariant
import com.kkday.design.model.StringType

@Composable
fun KKTextField(
    variant: KKTextFieldVariant = KKTextFieldVariant.TEXT,
    labelTextStringType: StringType = StringType.Text(""),
    placeholderTextStringType: StringType = StringType.Text(""),
    formMessageStringType: StringType = StringType.Text(""),
    text: MutableState<String> = mutableStateOf(""),
    @DrawableRes leadingIconRes: Int = -1,
    leadingIconColor: Color = StyleDictionary.kkColorTextLight,
    leadingIconSize: Dp? = null,
    @DrawableRes trailingIconRes: Int = -1,
    isRequires: Boolean = false,
    layoutWidth: Dp = (-1).dp,
    boxHeight: Dp = 48.dp,
    boxBackground: Color? = null,
    isNextAction: Boolean = true,
    imeAction: ImeAction? = null,
    enabled: Boolean = true,
    enabledDeleteIcon: MutableState<Boolean> = mutableStateOf(true),
    selfFocusRequester: FocusRequester? = null,
    nextFocusRequester: FocusRequester? = null,
    isError: Boolean = false,
    keyboardCapitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    textLengthLimit: Int = -1,
    textListener: (String) -> Unit = {},
    focusListener: (Boolean) -> Unit = {},
    onTapTrailingIconListener: () -> Unit = {},
    onSearchListener: () -> Unit = {},
    onDone: () -> Unit = {},
    onKeyboardScroll: (Int, Int) -> Unit = { _, _ -> }  //特殊处理recycleview中有关键盘的滚动
) {
    val labelText = when (labelTextStringType) {
        is StringType.Text -> labelTextStringType.text
        is StringType.Res -> stringResource(id = labelTextStringType.textRes)
    }

    val placeholderText = when (placeholderTextStringType) {
        is StringType.Text -> placeholderTextStringType.text
        is StringType.Res -> stringResource(id = placeholderTextStringType.textRes)
    }

    val formMessageText = when (formMessageStringType) {
        is StringType.Text -> formMessageStringType.text
        is StringType.Res -> stringResource(id = formMessageStringType.textRes)
    }

    val textFocus = remember { mutableStateOf(false) }
    val deleteIconVisible =
        remember { derivedStateOf { enabledDeleteIcon.value && text.value.isNotBlank() } }
    val passwordVisible = remember { mutableStateOf(variant != KKTextFieldVariant.PASSWORD) }

    val customTextSelectionColors by remember {
        mutableStateOf(
            TextSelectionColors(
                handleColor = StyleDictionary.kkColorTextDarker,
                backgroundColor = StyleDictionary.kkColorTextDark.copy(alpha = 0.3f)
            )
        )
    }

    // Holds the latest internal TextFieldValue state. We need to keep it to have the correct value
    // of the composition.
    var textFieldValueState by remember { mutableStateOf(TextFieldValue(text = text.value)) }
    // Holds the latest TextFieldValue that BasicTextField was recomposed with. We couldn't simply
    // pass `TextFieldValue(text = value)` to the CoreTextField because we need to preserve the
    // composition.
    val textFieldValue = textFieldValueState.copy(text = text.value)

    SideEffect {
        if (textFieldValue.selection != textFieldValueState.selection ||
            textFieldValue.composition != textFieldValueState.composition
        ) {
            textFieldValueState = textFieldValue
        }
    }

    CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
        BasicTextField(
            singleLine = true,
            enabled = enabled,
//            value = text.value,
            value = textFieldValue,
            onValueChange = { newValue ->
                val length =
                    if (textLengthLimit == -1) newValue.text.length else newValue.text.length.coerceAtMost(
                        textLengthLimit
                    )
                text.value = newValue.text.subSequence(0, length).toString()

                textFieldValueState = newValue.copy(text = text.value)

                textListener(text.value)
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = when (variant) {
                    KKTextFieldVariant.PASSWORD -> KeyboardType.Password
                    KKTextFieldVariant.EMAIL -> KeyboardType.Email
                    KKTextFieldVariant.PHONE -> KeyboardType.Phone
                    else -> KeyboardType.Text
                },
                capitalization = keyboardCapitalization,
                imeAction = imeAction ?: if (isNextAction) ImeAction.Next else ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onNext = { nextFocusRequester?.requestFocus() },
                onSearch = {
                    onSearchListener()
                },
                onDone = {
                    onDone()
                },
            ),
            visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier
                .run {
                    if (layoutWidth == (-1).dp) {
                        this.fillMaxWidth()
                    } else {
                        this.width(layoutWidth)
                    }
                }
                .run {
                    if (selfFocusRequester != null) this.focusRequester(selfFocusRequester)
                    else this
                }
                .onGloballyPositioned { coordinates ->
                    if (textFocus.value) {
                        val positionInWindow = coordinates.positionInWindow()
                        val textFieldTopInWindow = positionInWindow.y.toInt()
                        val textFieldBottomInWindow = (positionInWindow.y + coordinates.size.height).toInt()
                        onKeyboardScroll(textFieldTopInWindow, textFieldBottomInWindow)
                    }
                }
                .onFocusChanged {
                    if (textFocus.value != it.isFocused) {
                        textFocus.value = it.isFocused
                        focusListener(it.isFocused)
                    }
                },
            textStyle = TextStyle(
                fontSize = StyleDictionary.kkTextBodyMdFontSize, color = when {
                !enabled -> StyleDictionary.kkColorTextLight
                else -> StyleDictionary.kkColorTextDarker
            }
            ),
            decorationBox = { innerTextField ->
                decorationInnerTextField(
                    variant = variant,
                    labelText = labelText,
                    isRequires = isRequires,
                    isTextFocus = textFocus.value,
                    enabled = enabled,
                    isError = isError,
                    formMessageText = formMessageText,
                    layoutWidth = layoutWidth,
                    boxHeight = boxHeight,
                    boxBackground = boxBackground
                ) {
                    decoratedInnerTextField(
                        variant = variant,
                        leadingIconRes = leadingIconRes,
                        leadingIconColor = leadingIconColor,
                        leadingIconSize = leadingIconSize,
                        trailingIconRes = trailingIconRes,
                        text = text,
                        placeholder = placeholderText,
                        enabled = enabled,
                        innerTextField = innerTextField,
                        isTextFocus = textFocus.value,
                        deleteIconVisible = deleteIconVisible.value,
                        passwordVisible = passwordVisible,
                        onTapTrailingIconListener = onTapTrailingIconListener
                    )
                }
            })
    }
}

@Composable
private fun decorationInnerTextField(
    variant: KKTextFieldVariant,
    labelText: String = "",
    isRequires: Boolean = false,
    isTextFocus: Boolean = false,
    enabled: Boolean = true,
    isError: Boolean = false,
    formMessageText: String = "",
    layoutWidth: Dp = (-1).dp,
    boxHeight: Dp = 48.dp,
    boxBackground: Color? = null,
    decoratedInnerTextField: @Composable () -> Unit,
) {
    Column {
        if (labelText.isNotEmpty()) {
            LabelText(
                modifier = Modifier.padding(bottom = StyleDictionary.kkSpacing050),
                labelText = labelText,
                textColor = StyleDictionary.kkColorTextDarker,
                fontSize = StyleDictionary.kkTextBodyMdFontSize,
                isRequired = isRequires
            )
        }

        Box(
            contentAlignment = Alignment.CenterStart, modifier = Modifier
            .height(boxHeight)
            .background(
                color = when {
                    !enabled -> StyleDictionary.kkColorBackgroundSurfaceLight
                    else -> boxBackground ?: Color.White
                }, shape = RoundedCornerShape(
                size = when (variant) {
                    KKTextFieldVariant.SEARCH -> Int.MAX_VALUE.dp
                    else -> StyleDictionary.kkRadiusMd
                }
            )
            )
            .run {
                if (boxBackground == null) {
                    border(
                        width = 1.dp, color = when {
                        isError -> StyleDictionary.kkColorBorderCriticalDark
                        isTextFocus -> StyleDictionary.kkColorBorderDarker
                        enabled -> StyleDictionary.kkColorBorderLight
                        else -> StyleDictionary.kkColorBorderLighter
                    }, shape = RoundedCornerShape(
                        size = when (variant) {
                            KKTextFieldVariant.SEARCH -> Int.MAX_VALUE.dp
                            else -> StyleDictionary.kkRadiusMd
                        }
                    )
                    )
                } else {
                    this
                }
            }
            .padding(horizontal = StyleDictionary.kkSpacing150)
        ) {
            decoratedInnerTextField()
        }

        if (isError && formMessageText.isNotEmpty()) {
            KKFormMessage(
                messageStringType = StringType.Text(formMessageText),
                variant = KKFormMessageVariant.ERROR,
                spacing = KKFormMessageSpacing.TOP_4,
                layoutWidth = layoutWidth
            )
        } else if (formMessageText.isNotEmpty()) {
            KKFormMessage(
                hintStringType = StringType.Text(formMessageText),
                spacing = KKFormMessageSpacing.TOP_4,
                layoutWidth = layoutWidth
            )
        }
    }
}

@Composable
private fun decoratedInnerTextField(
    variant: KKTextFieldVariant,
    text: MutableState<String>,
    @DrawableRes leadingIconRes: Int = -1,
    leadingIconColor: Color = StyleDictionary.kkColorTextLight,
    leadingIconSize: Dp? = null,
    @DrawableRes trailingIconRes: Int = -1,
    innerTextField: @Composable () -> Unit,
    placeholder: String = "",
    enabled: Boolean = true,
    isTextFocus: Boolean = false,
    deleteIconVisible: Boolean = false,
    passwordVisible: MutableState<Boolean>,
    textListener: (String) -> Unit = {},
    onTapTrailingIconListener: () -> Unit = {},
) {
    val leadingIcon = if (leadingIconRes == -1) {
        leadingIconRes
    } else {
        when (variant) {
            KKTextFieldVariant.EMAIL -> R.drawable.ic_mail_line
            KKTextFieldVariant.PASSWORD -> R.drawable.ic_lock_line
            KKTextFieldVariant.SEARCH -> R.drawable.ic_search_line
            else -> leadingIconRes
        }
    }

    val trailingIcon = when (variant) {
        KKTextFieldVariant.PASSWORD -> if (passwordVisible.value) R.drawable.ic_eye_slash_line else R.drawable.ic_eye_line
        else -> trailingIconRes
    }

    val isShowDeleteIcon = isTextFocus && deleteIconVisible

    ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
        val (leadingIconRef, innerTextFieldRef, deleteIconRef, trailingIconRef, kkFormMessageRef) = createRefs()

        if (leadingIcon != -1) {
            Image(
                painter = painterResource(id = leadingIcon),
                contentDescription = null,
                colorFilter = ColorFilter.tint(leadingIconColor),
                modifier = Modifier
                    .size(leadingIconSize ?: StyleDictionary.kkDimensionIconSm)
                    .constrainAs(leadingIconRef) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                    })
        }

        Box(modifier = Modifier.constrainAs(innerTextFieldRef) {
            top.linkTo(parent.top)
            bottom.linkTo(parent.bottom, StyleDictionary.kkSpacing025)

            if (leadingIcon != -1) {
                start.linkTo(leadingIconRef.end, StyleDictionary.kkSpacing100)
            } else {
                start.linkTo(parent.start)
            }

            if (isShowDeleteIcon) {
                end.linkTo(deleteIconRef.start, StyleDictionary.kkSpacing100)
            } else if (trailingIcon != -1) {
                end.linkTo(trailingIconRef.start, StyleDictionary.kkSpacing100)
            } else {
                end.linkTo(parent.end)
            }

            width = Dimension.fillToConstraints
        }) {
            if (text.value.isEmpty()) {
                Text(
                    maxLines = 1,
                    text = placeholder,
                    fontSize = StyleDictionary.kkTextBodyMdFontSize,
                    color = StyleDictionary.kkColorTextLight,
                    overflow = TextOverflow.Ellipsis
                )
            }

            innerTextField()
        }

        if (isShowDeleteIcon) {
            Image(
                painter = painterResource(id = R.drawable.ic_cross_circle_fill),
                contentDescription = null,
                colorFilter = ColorFilter.tint(StyleDictionary.kkColorTextMedium),
                modifier = Modifier
                    .padding(StyleDictionary.kkSpacing025)
                    .size(StyleDictionary.kkDimensionIconXs)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            text.value = ""
                            textListener("")
                        })
                    }
                    .constrainAs(deleteIconRef) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        if (trailingIcon != -1) {
                            end.linkTo(trailingIconRef.start, margin = StyleDictionary.kkSpacing150)
                        } else {
                            end.linkTo(parent.end)
                        }
                    })
        }

        if (trailingIcon != -1) {
            Image(
                painter = painterResource(id = trailingIcon),
                contentDescription = null,
                colorFilter = ColorFilter.tint(
                    when {
                        !enabled -> StyleDictionary.kkColorTextLight
                        else -> StyleDictionary.kkColorTextDarker // // If we have KKTextSelect, we need to change color back to KKColorTextMedium.
                    }
                ),
                modifier = Modifier
                    .size(StyleDictionary.kkDimensionIconSm)
                    .pointerInput(Unit) {
                        if (variant == KKTextFieldVariant.PASSWORD) detectTapGestures(onTap = {
                            passwordVisible.value = !passwordVisible.value
                        })
                        else detectTapGestures(onTap = { onTapTrailingIconListener.invoke() })
                    }
                    .constrainAs(trailingIconRef) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end)
                    })
        }
    }
}

@Preview(showBackground = true)
@Composable
fun previewAllVariant() {
    Column(
        modifier = Modifier.background(Color.Yellow),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        KKTextField(
            variant = KKTextFieldVariant.TEXT,
            labelTextStringType = StringType.Text("TEXT"),
            placeholderTextStringType = StringType.Text("placeholder"),
            text = remember { mutableStateOf("text123123123123123123123123") },
            formMessageStringType = StringType.Text("HintHintHint"),
            layoutWidth = 200.dp,
            isRequires = true,
            enabled = true,
            isError = false,
        )
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(Color.Black)
        )
        KKTextField(
            variant = KKTextFieldVariant.EMAIL,
            leadingIconRes = R.drawable.ic_mail_line,
            layoutWidth = 200.dp,
            labelTextStringType = StringType.Text("EMAIL"),
            placeholderTextStringType = StringType.Text("placeholder"),
            formMessageStringType = StringType.Text("Error Message"),
            text = remember { mutableStateOf("text123123123123123123123123") },
            enabled = true,
            isError = true
        )
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(Color.Black)
        )
        KKTextField(
            variant = KKTextFieldVariant.PASSWORD,
            leadingIconRes = R.drawable.ic_mail_line,
            layoutWidth = 200.dp,
            labelTextStringType = StringType.Text("PASSWORD"),
            placeholderTextStringType = StringType.Text("placeholder"),
            text = remember { mutableStateOf("text123123123123123123123123") },
            enabled = true,
            isError = false
        )
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(Color.Black)
        )
        KKTextField(
            variant = KKTextFieldVariant.PHONE,
            leadingIconRes = R.drawable.ic_mail_line,
            layoutWidth = 200.dp,
            labelTextStringType = StringType.Text("PHONE"),
            placeholderTextStringType = StringType.Text("placeholder"),
            text = remember { mutableStateOf("text123123123123123123123123") },
            enabled = true,
            isError = false
        )
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(Color.Black)
        )
        KKTextField(
            variant = KKTextFieldVariant.SEARCH,
            leadingIconRes = R.drawable.ic_mail_line,
            layoutWidth = 200.dp,
            labelTextStringType = StringType.Text("SEARCH"),
            placeholderTextStringType = StringType.Text("placeholder"),
            text = remember { mutableStateOf("text123123123123123123123123") },
            enabled = true,
            isError = false
        )
    }
}

enum class KKTextFieldVariant {
    TEXT, EMAIL, PASSWORD, PHONE, SEARCH
}
