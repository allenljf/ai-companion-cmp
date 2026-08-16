package com.allenljf.aicompanion.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.NSMutableData
import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIPasteboard
import platform.UIKit.UIViewController
import platform.UIKit.popoverPresentationController
import platform.posix.memcpy

// IG 限動的圖片交接管道：不支援直接帶 URL/文字，靠這組固定的剪貼簿 pasteboard type 傳圖給 IG
// （原版 Android 走 ADD_TO_STORY intent + MediaStore；iOS 走 UIPasteboard + URL scheme，見任務 brief）
private const val INSTAGRAM_STICKER_BACKGROUND_PASTEBOARD_TYPE = "com.instagram.sharedSticker.backgroundImage"

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberShareImageToInstagramStory(): suspend (image: ImageBitmap, caption: String) -> Boolean {
    return remember {
        { image: ImageBitmap, caption: String ->
            shareToInstagramStoryOrFallback(image, caption)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun shareToInstagramStoryOrFallback(image: ImageBitmap, caption: String): Boolean {
    val pngBytes = Image.makeFromBitmap(image.asSkiaBitmap()).encodeToData(EncodedImageFormat.PNG)?.bytes
        ?: return false
    val nsData = pngBytes.toNSData()

    // Info.plist 已加 LSApplicationQueriesSchemes: [instagram-stories]；沒宣告的話 canOpenURL 恆回 false
    val bundleId = NSBundle.mainBundle.bundleIdentifier.orEmpty()
    val storyUrl = NSURL.URLWithString("instagram-stories://share?source_application=$bundleId")
    val canOpenInstagram = storyUrl != null && UIApplication.sharedApplication.canOpenURL(storyUrl)

    return if (canOpenInstagram) {
        // IG 限動不支援帶入文字文案，只能透過 pasteboard 把圖片交給 IG（source_application 只作標示用）
        UIPasteboard.generalPasteboard.setData(
            nsData,
            forPasteboardType = INSTAGRAM_STICKER_BACKGROUND_PASTEBOARD_TYPE,
        )
        UIApplication.sharedApplication.openURL(storyUrl)
        true
    } else {
        fallbackShareImage(nsData, caption)
    }
}

/** IG 未安裝（或 URL scheme 打不開）：退回系統分享這張圖（同 Android fallback 語意）。 */
@OptIn(ExperimentalForeignApi::class)
private fun fallbackShareImage(nsData: NSData, caption: String): Boolean {
    val uiImage = UIImage(data = nsData)
    val items = listOfNotNull(uiImage, caption.takeIf { it.isNotBlank() })
    val activityController = UIActivityViewController(activityItems = items, applicationActivities = null)
    val presenter = topMostViewController() ?: return false
    // iPad popover 錨點（同 ShareText.ios.kt 的處理，避免不設定 sourceView 直接 crash）
    activityController.popoverPresentationController?.let { popover ->
        popover.sourceView = presenter.view
        popover.sourceRect = presenter.view.bounds
    }
    presenter.presentViewController(activityController, animated = true, completion = null)
    return true
}

private fun topMostViewController(): UIViewController? {
    var top = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (top?.presentedViewController != null) {
        top = top.presentedViewController
    }
    return top
}

// 這個 cinterop 版本的 platform.Foundation.NSData 沒有暴露 dataWithBytes:length:／initWithBytes:length:
// （BLOCKED 排查過程見任務報告），改用 NSMutableData 的 setLength + mutableBytes + memcpy 手動搬 bytes，
// 這兩個 API 是任何 Foundation cinterop 版本都會有的最小集合，不依賴那組被裁掉的工廠方法。
@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData {
    val data = NSMutableData()
    data.setLength(size.toULong())
    val destination = data.mutableBytes ?: return data
    if (isNotEmpty()) {
        usePinned { pinned -> memcpy(destination, pinned.addressOf(0), size.toULong()) }
    }
    return data
}
