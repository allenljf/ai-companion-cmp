package com.kkday.feature.ai_companion.presentation.poster

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kkday.design.StyleDictionary

/**
 * 分享海報的離屏 Compose 版面：Hero 圖完整顯示於頂部（不裁切、不疊加），下方依序接著文字資料、
 * 目的地郵戳（疊放於文字區塊右上角）、三個 tag 圓圈，由外部透過 [GraphicsLayer] 擷取成最終
 * [android.graphics.Bitmap]（見 [OffscreenSixZonePoster]）。畫布寬度固定 1152px，高度依內容
 * 自然堆疊（不強制固定總高）；App 畫面即時顯示則改用 [PosterOverlayMetrics.Live]（見 ResultScreen），
 * 在正常裝置密度下以手機螢幕合理的 dp/sp 數值呈現，兩者共用同一份版面結構，只是尺寸參數不同。
 */
@Composable
fun OffscreenSixZonePoster(
    assets: ResolvedPosterAssets,
    graphicsLayer: GraphicsLayer,
    metrics: PosterOverlayMetrics = PosterOverlayMetrics.Export,
    showFooter: Boolean = true,
    captureWidth: Int = SixZonePosterSpec.CANVAS_WIDTH,
) {
    // 外層鎖 1dp 並裁切：避免離屏內容影響 Result 頁真正的版面尺寸，同時完全不可視
    Box(Modifier.size(1.dp).clipToBounds()) {
        Box(
            modifier = Modifier
                .layout { measurable, _ ->
                    // 無視父層 1dp 限制，寬度固定為 captureWidth、高度依內容自然量測，才能正確合成
                    val placeable = measurable.measure(
                        Constraints(
                            minWidth = captureWidth,
                            maxWidth = captureWidth,
                            minHeight = 0,
                            maxHeight = Constraints.Infinity,
                        )
                    )
                    layout(1, 1) { placeable.place(0, 0) }
                }
                .drawWithContent {
                    graphicsLayer.record { this@drawWithContent.drawContent() }
                    drawLayer(graphicsLayer)
                }
        ) {
            CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 1f)) {
                PosterOverlayContent(assets, showFooter = showFooter, metrics = metrics)
            }
        }
    }
}

/** 版面尺寸參數：[Export] 對應 1152px 寬虛擬像素畫布（density 覆寫為 1f）；[Live] 對應手機螢幕即時顯示（正常裝置密度）。 */
data class PosterOverlayMetrics(
    val horizontalPadding: Dp,
    val verticalSpacing: Dp,
    val stampSize: Dp,
    val circleSize: Dp,
    val badgeCornerRadius: Dp,
    val titleFontSize: TextUnit,
    val subtitleFontSize: TextUnit,
    val destinationFontSize: TextUnit,
    val taglineFontSize: TextUnit,
    val quoteFontSize: TextUnit,
    val hashtagFontSize: TextUnit,
    val qrSize: Dp,
    val footerFontSize: TextUnit,
) {
    companion object {
        val Export = PosterOverlayMetrics(
            horizontalPadding = 48.dp,
            verticalSpacing = 24.dp,
            stampSize = 168.dp,
            circleSize = 168.dp,
            badgeCornerRadius = 32.dp,
            titleFontSize = 36.sp,
            subtitleFontSize = 18.sp,
            destinationFontSize = 30.sp,
            taglineFontSize = 20.sp,
            quoteFontSize = 18.sp,
            hashtagFontSize = 16.sp,
            qrSize = 72.dp,
            footerFontSize = 20.sp,
        )

        // 手機螢幕寬度通常僅 ~360~430dp，沿用 Export 的絕對數值會過大、擠壓變形，故另外準備一組較小尺寸
        val Live = PosterOverlayMetrics(
            horizontalPadding = 20.dp,
            verticalSpacing = 12.dp,
            stampSize = 96.dp, // 64dp × 1.5
            circleSize = 120.dp,
            badgeCornerRadius = 20.dp,
            // 字級改用 StyleDictionary token：放大一級後若無剛好對應的 token，往上一級靠攏
            titleFontSize = StyleDictionary.kkFontSize5, // 22sp（18→20 無對應 token，靠攏 22）
            subtitleFontSize = StyleDictionary.kkFontSize3, // 16sp（副標題再放大一級：14→16）
            destinationFontSize = StyleDictionary.kkFontSize4, // 18sp（剛好對應）
            taglineFontSize = StyleDictionary.kkFontSize2, // 14sp（剛好對應）
            quoteFontSize = StyleDictionary.kkFontSize2, // 14sp（12→13 無對應 token，靠攏 14）
            hashtagFontSize = StyleDictionary.kkFontSize2, // 14sp（tag 文字再放大一級：12→14）
            qrSize = 0.dp, // Live 模式不顯示 footer
            footerFontSize = 0.sp,
        )
    }
}

/**
 * Hero 完整顯示於頂部 + 下方依序排列內容的版面本體。[showFooter] 控制是否顯示 QR/KKday 品牌 footer：
 * 靜態分享圖需要（見 [OffscreenSixZonePoster]）；App 畫面上這塊改由真正的「探索這趟旅程」按鈕
 * 取代（見 ResultScreen），故顯示時關閉 footer 避免重複。[metrics] 決定實際尺寸，見 [PosterOverlayMetrics]。
 * 由 [PosterHeroWithBadge]（Hero + 疊加資訊卡，分享截圖範圍）與 [PosterBelowHeroContent]（其餘內容）組成。
 */
@Composable
fun PosterOverlayContent(
    assets: ResolvedPosterAssets,
    showFooter: Boolean,
    metrics: PosterOverlayMetrics = PosterOverlayMetrics.Live,
) {
    Column(Modifier.fillMaxWidth()) {
        PosterHeroWithBadge(assets = assets, metrics = metrics)
        PosterBelowHeroContent(assets = assets, showFooter = showFooter, metrics = metrics)
    }
}

/**
 * Hero 圖完整顯示，底部疊放一張黑色半透明圓角資訊卡（旅伴推薦文案／目的地／tagline／目的地郵戳）。
 * 對應「旅伴結果分享／分享到 IG 限時動態」需求：分享素材就是這一整塊（Hero + 資訊卡）的畫面截圖。
 */
@Composable
fun PosterHeroWithBadge(
    assets: ResolvedPosterAssets,
    metrics: PosterOverlayMetrics,
) {
    val content = assets.content
    Box(Modifier.fillMaxWidth()) {
        // Hero 完整顯示（不裁切成固定比例，維持圖片原始寬高比）
        Image(
            bitmap = assets.hero.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth(),
        )

        // 疊放於 Hero 底部的黑色半透明圓角資訊卡：左側文字、右側目的地郵戳
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = metrics.horizontalPadding, vertical = metrics.verticalSpacing)
                .clip(RoundedCornerShape(metrics.badgeCornerRadius))
                .background(PosterColors.badgeBackground)
                .padding(metrics.horizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    // TODO: replace with stringResource
                    text = "${content.companionName.ifBlank { "旅伴" }}推薦我的旅遊城市",
                    fontFamily = CompanionPosterFontFamily,
                    fontSize = metrics.subtitleFontSize,
                    color = PosterColors.badgeTextSecondary,
                )
                Spacer(Modifier.height(metrics.verticalSpacing / 3))
                Text(
                    text = listOf(content.destinationCn, content.destinationEn).filter { it.isNotBlank() }.joinToString(" · "),
                    fontFamily = CompanionPosterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = metrics.destinationFontSize,
                    color = PosterColors.badgeTextPrimary,
                )
                if (content.tagline.isNotBlank()) {
                    Spacer(Modifier.height(metrics.verticalSpacing / 3))
                    Text(
                        text = content.tagline,
                        fontFamily = CompanionPosterFontFamily,
                        fontSize = metrics.taglineFontSize,
                        color = PosterColors.badgeTextSecondary,
                    )
                }
            }
            Spacer(Modifier.width(metrics.verticalSpacing))
            Image(
                bitmap = assets.stamp.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(metrics.stampSize)
                    .clip(CircleShape),
            )
        }
    }
}

/**
 * Hero 下方維持原樣往下排列的內容：旅伴語錄、三個 tag 圖示與 hashtag、（僅靜態分享圖）QR／品牌 footer。
 * 這一段不在分享截圖範圍內。
 */
@Composable
fun PosterBelowHeroContent(
    assets: ResolvedPosterAssets,
    showFooter: Boolean,
    metrics: PosterOverlayMetrics,
) {
    val content = assets.content
    Column(Modifier.fillMaxWidth()) {
        if (content.companionQuote.isNotBlank()) {
            Text(
                text = listOfNotNull(
                    content.companionQuote,
                    content.companionName.takeIf { it.isNotBlank() }?.let { "— $it" },
                ).joinToString(" "),
                fontFamily = CompanionPosterFontFamily,
                fontSize = metrics.quoteFontSize,
                color = PosterColors.textSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = metrics.horizontalPadding, vertical = metrics.verticalSpacing),
            )
        }

        // 三個 tag 圖示：素材本身為白底透明去背圖，直接融入背景，不再套圓形裁切/外框，下方放 hashtag 文字
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = metrics.horizontalPadding),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            assets.tagIcons.forEachIndexed { index, bitmap ->
                val tag = content.highlightTags.getOrNull(index).orEmpty()
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.wrapContentWidth()) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(metrics.circleSize),
                    )
                    if (tag.isNotBlank()) {
                        Spacer(Modifier.height(metrics.verticalSpacing / 3))
                        Text(
                            text = "#$tag",
                            fontFamily = CompanionPosterFontFamily,
                            fontSize = metrics.hashtagFontSize,
                            color = PosterColors.textPrimary,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                        )
                    }
                }
            }
        }

        if (showFooter) {
            Spacer(Modifier.height(metrics.verticalSpacing))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = metrics.verticalSpacing),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                assets.qrCode?.let { qr ->
                    Image(
                        bitmap = qr.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(metrics.qrSize),
                    )
                    Spacer(Modifier.width(metrics.verticalSpacing / 2))
                }
                Text(
                    text = "KKday",
                    fontFamily = CompanionPosterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = metrics.footerFontSize,
                    color = PosterColors.textPrimary,
                )
            }
        }
    }
}

private object PosterColors {
    val textPrimary = Color(0xFF222222)
    val textSecondary = Color(0xFF666666)
    val badgeBackground = Color.Black.copy(alpha = 0.45f)
    val badgeTextPrimary = Color.White
    val badgeTextSecondary = Color(0xFFE0E0E0)
}
