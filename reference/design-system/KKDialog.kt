package com.kkday.design.dialog

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.extensions.verticalColumnScrollbar
import com.kkday.design.R
import com.kkday.design.StyleDictionary
import com.kkday.design.StyleDictionary.kkColorBackgroundSurfaceDark
import com.kkday.design.button.ButtonSizeType
import com.kkday.design.button.ButtonState
import com.kkday.design.button.ButtonType
import com.kkday.design.button.KKButton
import com.kkday.design.font.fontH6
import com.kkday.design.utils.Device
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.kkday.design.font.medium
import com.kkday.design.utils.shadowFixed

const val NO_HEADER_ICON = 0

sealed class DialogHeaderType {
    data class Text(val title: String, val useScrollableContent: Boolean): DialogHeaderType()
    data class TextWithImage(val title: String, val imageContent: (@Composable () -> Unit)):
        DialogHeaderType()
}

enum class DialogButtonArrangement {
    HORIZONTAL, VERTICAL
}

/**
 * @param modifier 自訂對話框大小
 * @param headerType [DialogTitleType] Title：標題 , TitleWithImage：標題和圖片
 * @param buttonArrangement [DialogButtonArrangement] 按鈕排列方式 HORIZONTAL：水平排列 , VERTICAL：垂直排列
 * @param isDismissOnClickOutside 為 true 時，點擊外部區域會關閉對話框
 * @param showFooterShadow footer 顯示陰影
 * @param headerIconButton header 自定義圖示按鈕
 * @param showHeaderCloseButton header 顯示關閉按鈕
 * @param content 對話框內容
 * @param onClickHeaderIconButton 圖示按鈕點擊事件
 * @param onDismissRequest 關閉按鈕點擊事件
 * @param onClickPrimaryButton footer KKButton 主按鈕點擊事件
 * @param onClickSecondaryButton footer KKButton 次按鈕點擊事件
 * @param onClickCancelButton footer KKButton 取消按鈕點擊事件
 */
@Composable
fun KKDialog(
    modifier: Modifier = Modifier,
    headerType: DialogHeaderType,
    buttonArrangement: DialogButtonArrangement = DialogButtonArrangement.HORIZONTAL,
    isDismissOnClickOutside: Boolean = true,
    showFooterShadow: Boolean = true,
    @DrawableRes headerIconButton: Int = 0,
    showHeaderCloseButton: Boolean = true,
    content: @Composable () -> Unit,
    onClickHeaderIconButton: (() -> Unit)? = null,
    onDismissRequest: (() -> Unit)? = null,
    onClickPrimaryButton: (@Composable () -> Unit)? = null,
    onClickSecondaryButton: (@Composable () -> Unit)? = null,
    onClickCancelButton: (@Composable () -> Unit)? = null,
) {
    Dialog(
        onDismissRequest = { onDismissRequest?.invoke() },
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnClickOutside = isDismissOnClickOutside
            ),
    ) {
        DialogContent(
            modifier = modifier,
            headerType = headerType,
            showFooterShadow = showFooterShadow,
            content = content,
            buttonArrangement = buttonArrangement,
            headerIconButton = headerIconButton,
            showHeaderCloseButton = showHeaderCloseButton,
            onClickHeaderIconButton = { onClickHeaderIconButton?.invoke() },
            onClickCloseButton = { onDismissRequest?.invoke() },
            onClickPrimaryButton = onClickPrimaryButton,
            onClickSecondaryButton = onClickSecondaryButton,
            onClickCancelButton = onClickCancelButton
        )
    }
}

@Composable
private fun DialogContent(
    modifier: Modifier = Modifier,
    headerType: DialogHeaderType,
    showFooterShadow: Boolean = true,
    content: @Composable () -> Unit,
    buttonArrangement: DialogButtonArrangement = DialogButtonArrangement.HORIZONTAL,
    @DrawableRes headerIconButton: Int = 0,
    showHeaderCloseButton: Boolean,
    onClickHeaderIconButton: () -> Unit,
    onClickCloseButton: () -> Unit,
    onClickPrimaryButton: (@Composable () -> Unit)? = null,
    onClickSecondaryButton: (@Composable () -> Unit)? = null,
    onClickCancelButton: (@Composable () -> Unit)? = null,
) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val screenHeightDp = LocalConfiguration.current.screenHeightDp
    val dialogWith = screenWidthDp * 0.8f
    val dialogMaxHeight = (screenHeightDp - Device.dpToPx(40)).dp

    val scrollState = rememberScrollState()
    var isScrollAtBottom by remember { mutableStateOf(false) }

    val contentModifier =
        if (headerType is DialogHeaderType.Text && headerType.useScrollableContent) {
            Modifier
                .fillMaxWidth()
                .verticalColumnScrollbar(
                    scrollState = scrollState,
                    width = StyleDictionary.kkSpacing100,
                    scrollBarColor = kkColorBackgroundSurfaceDark,
                    scrollBarCornerRadius = 16f,
                    endPadding = 30f
                )
                .verticalScroll(scrollState)
        } else {
            Modifier
        }

    LaunchedEffect(scrollState.canScrollForward) {
        isScrollAtBottom = !scrollState.canScrollForward
    }

    Column(
        modifier = modifier
            .clip(shape = RoundedCornerShape(StyleDictionary.kkRadiusXl))
            .width(dialogWith)
            .heightIn(max = dialogMaxHeight)
            .background(StyleDictionary.kkColorBackgroundSurfaceLight)
    ) {
        when (headerType) {
            is DialogHeaderType.Text -> {
                HeaderWithText(
                    title = headerType.title,
                    headerIconButton = headerIconButton,
                    showHeaderCloseButton = showHeaderCloseButton,
                    onClickIconButton = onClickHeaderIconButton,
                    onClickCloseButton = onClickCloseButton
                )
                Column(
                    modifier = contentModifier
                        .background(StyleDictionary.kkColorWhite)
                        .weight(1f, false)
                ) {
                    content()
                }
            }

            is DialogHeaderType.TextWithImage ->
                Column(modifier = Modifier.weight(1f, false)) {
                    HeaderWithImageContent(
                        imageContent = headerType.imageContent,
                        title = headerType.title,
                        headerIconButton = headerIconButton,
                        showHeaderCloseButton = showHeaderCloseButton,
                        content = content,
                        onClickHeaderIconButton = onClickHeaderIconButton,
                        onClickCloseButton = onClickCloseButton,
                        scrollState = scrollState,
                    )
                }
        }

        Box(modifier = Modifier, contentAlignment = Alignment.BottomCenter) {
            when (buttonArrangement) {
                DialogButtonArrangement.HORIZONTAL -> HorizontalFooter(
                    btnRight = onClickPrimaryButton,
                    btnLeft = onClickCancelButton,
                    showFooterShadow = showFooterShadow,
                    isScrollAtBottom = isScrollAtBottom
                )

                DialogButtonArrangement.VERTICAL -> VerticalFooter(
                    btnPrimary = onClickPrimaryButton,
                    btnSecondary = onClickSecondaryButton,
                    btnCancel = onClickCancelButton,
                    showFooterShadow = showFooterShadow,
                    isScrollAtBottom = isScrollAtBottom
                )
            }
        }
    }
}

@Composable
private fun HeaderWithText(
    title: String,
    @DrawableRes headerIconButton: Int = 0,
    showHeaderCloseButton: Boolean,
    onClickIconButton: () -> Unit,
    onClickCloseButton: () -> Unit,
) {
    if (title.isBlank() && headerIconButton == 0 && !showHeaderCloseButton) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(StyleDictionary.kkColorWhite)
            .padding(
                start = StyleDictionary.kkSpacing250,
                top = StyleDictionary.kkSpacing250,
                end = StyleDictionary.kkSpacing250,
                bottom = StyleDictionary.kkSpacing150
            ),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(StyleDictionary.kkSpacing050),
            contentAlignment = Alignment.TopStart
        ) {
            Text(text = title, style = fontH6.medium())
        }

        Row {
            if (headerIconButton != 0) {
                Spacer(modifier = Modifier.width(StyleDictionary.kkSpacing100))
                Box(
                    modifier = Modifier
                        .size(StyleDictionary.kkSpacing400)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                onClickIconButton.invoke()
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        modifier = Modifier.size(StyleDictionary.kkDimensionIconSm),
                        painter = painterResource(id = headerIconButton),
                        tint = StyleDictionary.kkColorTextDarker,
                        contentDescription = null
                    )
                }
                Spacer(modifier = Modifier.width(StyleDictionary.kkSpacing050))
            }

            if (showHeaderCloseButton) {
                Spacer(modifier = Modifier.width(StyleDictionary.kkSpacing100))
                Box(
                    modifier = Modifier
                        .size(StyleDictionary.kkSpacing400)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                onClickCloseButton.invoke()
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        modifier = Modifier.size(StyleDictionary.kkDimensionIconSm),
                        painter = painterResource(id = R.drawable.ic_cross_line_semibold_lg),
                        tint = StyleDictionary.kkColorTextDarker,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderWithImageContent(
    imageContent: @Composable () -> Unit, title: String,
    @DrawableRes headerIconButton: Int = 0,
    showHeaderCloseButton: Boolean,
    scrollState: ScrollState,
    content: @Composable () -> Unit,
    onClickHeaderIconButton: () -> Unit,
    onClickCloseButton: () -> Unit,
) {
    val alphaStickyHeader by remember {
        derivedStateOf {
            val startFade = 0f
            val endFade = 130f
            val scrollY = scrollState.value.toFloat()
            (scrollY - startFade) / endFade
        }
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .verticalColumnScrollbar(
                    scrollState = scrollState,
                    width = StyleDictionary.kkSpacing100,
                    scrollBarColor = kkColorBackgroundSurfaceDark,
                    scrollBarCornerRadius = 16f,
                    endPadding = 30f
                )
                .verticalScroll(scrollState)
                .background(StyleDictionary.kkColorWhite),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                imageContent()
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = StyleDictionary.kkSpacing300,
                            top = StyleDictionary.kkSpacing300,
                            end = StyleDictionary.kkSpacing300,
                            bottom = StyleDictionary.kkSpacing100
                        ),
                    text = title,
                    style = fontH6.medium(),
                    textAlign = TextAlign.Center
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = StyleDictionary.kkSpacing050),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    content()
                }
            }
        }

        Box(
            modifier = Modifier
                .graphicsLayer { this.alpha = alphaStickyHeader }
                .fillMaxWidth()
                .height(64.dp)
                .background(StyleDictionary.kkColorWhite)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = StyleDictionary.kkSpacing250,
                    end = StyleDictionary.kkSpacing250,
                    bottom = StyleDictionary.kkSpacing150
                ),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(
                modifier = Modifier,
                horizontalArrangement = Arrangement.spacedBy(StyleDictionary.kkSpacing150)
            ) {
                headerIconButton.takeIf { it != 0 }?.let {
                    BackgroundHeaderImage(it, true) {
                        onClickHeaderIconButton.invoke()
                    }
                }
                showHeaderCloseButton.takeIf { it }?.let {
                    BackgroundHeaderImage(R.drawable.ic_cross_line_semibold_lg, true) {
                        onClickCloseButton.invoke()
                    }
                }
            }
        }
    }
}

@Composable
private fun HorizontalFooter(
    btnLeft: @Composable (() -> Unit)? = null,
    btnRight: @Composable (() -> Unit)? = null,
    showFooterShadow: Boolean = true,
    isScrollAtBottom: Boolean,
) {
    if (btnLeft == null && btnRight == null) return
    val modifier = if (isScrollAtBottom || !showFooterShadow) Modifier else Modifier.shadowFixed()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(StyleDictionary.kkColorWhite)
            .padding(StyleDictionary.kkSpacing300),
        horizontalArrangement = Arrangement.spacedBy(StyleDictionary.kkSpacing200),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (btnLeft != null) {
            Box(
                modifier = Modifier.weight(1f)
            ) {
                btnLeft.invoke()
            }
        }

        if (btnRight != null) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                btnRight.invoke()
            }
        }
    }
}

@Composable
private fun VerticalFooter(
    btnPrimary: (@Composable () -> Unit?)? = null,
    btnSecondary: (@Composable () -> Unit)? = null,
    btnCancel: (@Composable () -> Unit)? = null,
    showFooterShadow: Boolean = true,
    isScrollAtBottom: Boolean = false,
) {
    if (btnPrimary == null && btnSecondary == null && btnCancel == null) return
    val modifier = if (isScrollAtBottom || !showFooterShadow) Modifier else Modifier.shadowFixed()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(StyleDictionary.kkColorWhite)
            .padding(StyleDictionary.kkSpacing300),
        verticalArrangement = Arrangement.spacedBy(StyleDictionary.kkSpacing200),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (btnPrimary != null) {
            Box(
                modifier = Modifier
            ) {
                btnPrimary.invoke()
            }
        }

        if (btnSecondary != null) {
            Box(
                modifier = Modifier,
                contentAlignment = Alignment.Center
            ) {
                btnSecondary.invoke()
            }
        }

        if (btnCancel != null) {
            Box(
                modifier = Modifier.padding(horizontal = StyleDictionary.kkSpacing250),
                contentAlignment = Alignment.Center
            ) {
                btnCancel.invoke()
            }
        }
    }
}

@Composable
private fun BackgroundHeaderImage(imageRes: Int, circleShape: Boolean, onClick: () -> Unit) {
    val modifier = if (circleShape) Modifier.clip(shape = CircleShape) else Modifier
    Box(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick.invoke() }
            .size(StyleDictionary.kkDimensionIconLg)
            .background(StyleDictionary.kkColorWhite),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size(StyleDictionary.kkDimensionIconSm),
            painter = painterResource(imageRes),
            tint = StyleDictionary.kkColorTextDarker,
            contentDescription = "image"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewKKDialog(
    @PreviewParameter(PreviewDialogDataProvider::class) previewDialogData: PreviewDialogData,
) {
    DialogContent(
        headerType = previewDialogData.headerType,
        buttonArrangement = previewDialogData.buttonArrangement,
        content = previewDialogData.content,
        headerIconButton = previewDialogData.headerIconButton,
        showHeaderCloseButton = previewDialogData.showHeaderCloseButton,
        onClickHeaderIconButton = { previewDialogData.onClickHeaderIconButton?.invoke() },
        onClickCloseButton = { previewDialogData.onDismissRequest?.invoke() },
        onClickPrimaryButton = previewDialogData.onClickPrimaryButton,
        onClickSecondaryButton = previewDialogData.onClickSecondaryButton,
        onClickCancelButton = previewDialogData.onClickCancelButton,
    )
}

class PreviewDialogDataProvider: PreviewParameterProvider<PreviewDialogData> {
    override val values: Sequence<PreviewDialogData>
        get() = sequenceOf(
            PreviewDialogData(
                headerType = DialogHeaderType.Text(title = "標題", useScrollableContent = false),
                buttonArrangement = DialogButtonArrangement.HORIZONTAL,
                isDismissOnClickOutside = true,
                showFooterShadow = true,
                content = {
                    Column {
                        Text(text = "內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容")
                    }
                },
                headerIconButton = R.drawable.ic_share_android_line_semibold,
                showHeaderCloseButton = true,
                onClickHeaderIconButton = { },
                onDismissRequest = { },
                onClickPrimaryButton = {
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
                },
                onClickSecondaryButton = { },
                onClickCancelButton = {
                    KKButton(
                        buttonText = "Button",
                        buttonType = ButtonType.TEXT_SECONDARY,
                        buttonState = ButtonState.ENABLED,
                        buttonSizeType = ButtonSizeType.Md,
                        onClick = {},
                        isFullWidth = true,
                        leadingIcon = null,
                        trailingIcon = null,
                    )
                }
            ),
            PreviewDialogData(
                headerType = DialogHeaderType.TextWithImage(
                    title = "標題",
                    imageContent = {
                        Image(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .background(StyleDictionary.kkColorBackgroundSurfaceMedium),
                            painter = painterResource(R.drawable.ic_image_line),
                            contentDescription = "HeaderImage",
                            contentScale = ContentScale.Inside
                        )
                    }
                ),
                buttonArrangement = DialogButtonArrangement.VERTICAL,
                isDismissOnClickOutside = true,
                content = {
                    Text(text = "內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容內容")
                },
                headerIconButton = R.drawable.ic_share_android_line_semibold,
                showHeaderCloseButton = true,
                onClickHeaderIconButton = { },
                onDismissRequest = { },
                showFooterShadow = true,
                onClickPrimaryButton = {
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
                },
                onClickSecondaryButton = {
                    KKButton(
                        buttonText = "Button",
                        buttonType = ButtonType.SECONDARY,
                        buttonState = ButtonState.ENABLED,
                        buttonSizeType = ButtonSizeType.Md,
                        onClick = {},
                        isFullWidth = true,
                        leadingIcon = null,
                        trailingIcon = null,
                    )
                },
                onClickCancelButton = {
                    KKButton(
                        buttonText = "Button",
                        buttonType = ButtonType.TEXT_SECONDARY,
                        buttonState = ButtonState.ENABLED,
                        buttonSizeType = ButtonSizeType.Md,
                        onClick = {},
                        isFullWidth = true,
                        leadingIcon = null,
                        trailingIcon = null,
                    )
                }
            ),
        )
}

data class PreviewDialogData(
    val headerType: DialogHeaderType,
    val buttonArrangement: DialogButtonArrangement,
    val isDismissOnClickOutside: Boolean,
    val showFooterShadow: Boolean,
    val headerIconButton: Int,
    val showHeaderCloseButton: Boolean,
    val content: @Composable () -> Unit,
    val onClickHeaderIconButton: (() -> Unit)?,
    val onDismissRequest: (() -> Unit)?,
    val onClickPrimaryButton: (@Composable () -> Unit)? = null,
    val onClickSecondaryButton: (@Composable () -> Unit)? = null,
    val onClickCancelButton: (@Composable () -> Unit)? = null,
)