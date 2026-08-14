package com.kkday.feature.ai_companion.presentation.poster

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.max

/**
 * gpt-image-2 產出的 stamp／tag 插畫皆為純白底 PNG，App 端需自行去背才能疊圖。
 * 對每個 pixel 依與純白的距離做 alpha keying：完全純白 → 全透明，離純白越遠 → 越不透明，
 * [threshold] 到 255 之間做線性羽化，避免邊緣出現生硬鋸齒。
 */
fun Bitmap.removeWhiteBackground(threshold: Int = 250): Bitmap {
    val result = copy(Bitmap.Config.ARGB_8888, true) ?: return this
    val width = result.width
    val height = result.height
    val pixels = IntArray(width * height)
    result.getPixels(pixels, 0, width, 0, 0, width, height)

    for (i in pixels.indices) {
        val pixel = pixels[i]
        val r = Color.red(pixel)
        val g = Color.green(pixel)
        val b = Color.blue(pixel)
        val minChannel = minOf(r, g, b)
        val alpha = featheredAlpha(minChannel, Color.alpha(pixel), threshold)
        if (alpha != Color.alpha(pixel)) {
            pixels[i] = Color.argb(alpha, r, g, b)
        }
    }

    result.setPixels(pixels, 0, width, 0, 0, width, height)
    return result
}

/**
 * 依 pixel 最暗色版（[minChannel]）與純白 [threshold] 的距離計算羽化後的 alpha 值：
 * [minChannel] < [threshold]（非白色）→ 保留原始 [originalAlpha]；
 * [minChannel] 在 [threshold]..255 之間 → 依線性比例往 0 遞減（越接近 255 越透明）。
 * 抽成純 Kotlin function（不依賴 android.graphics.*）方便在無 Android runtime 的 JVM 單元測試中驗證。
 */
internal fun featheredAlpha(minChannel: Int, originalAlpha: Int, threshold: Int): Int {
    if (minChannel < threshold) return originalAlpha
    val featherRange = (255 - threshold).coerceAtLeast(1)
    val whiteness = ((minChannel - threshold).toFloat() / featherRange).coerceIn(0f, 1f)
    return (originalAlpha * (1f - whiteness)).toInt().coerceIn(0, 255)
}

/** 依目標寬高做「等比縮放覆蓋、再置中裁切」的 CenterCrop，不拉伸變形。 */
fun Bitmap.centerCrop(targetWidth: Int, targetHeight: Int): Bitmap {
    if (width == targetWidth && height == targetHeight) return this
    val scale = max(targetWidth.toFloat() / width, targetHeight.toFloat() / height)
    val scaledWidth = (width * scale).toInt().coerceAtLeast(1)
    val scaledHeight = (height * scale).toInt().coerceAtLeast(1)
    val scaled = Bitmap.createScaledBitmap(this, scaledWidth, scaledHeight, true)
    val x = ((scaledWidth - targetWidth) / 2).coerceAtLeast(0)
    val y = ((scaledHeight - targetHeight) / 2).coerceAtLeast(0)
    return Bitmap.createBitmap(
        scaled,
        x.coerceAtMost(scaledWidth - targetWidth).coerceAtLeast(0),
        y.coerceAtMost(scaledHeight - targetHeight).coerceAtLeast(0),
        targetWidth.coerceAtMost(scaledWidth),
        targetHeight.coerceAtMost(scaledHeight),
    )
}
