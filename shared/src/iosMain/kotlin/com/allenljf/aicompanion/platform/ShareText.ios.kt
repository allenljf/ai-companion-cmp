package com.allenljf.aicompanion.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.popoverPresentationController

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberShareText(): (String) -> Unit {
    return remember {
        { text: String ->
            val activityController = UIActivityViewController(
                activityItems = listOf(text),
                applicationActivities = null,
            )
            topMostViewController()?.let { presenter ->
                // iPad 上 UIActivityViewController 用 popover 呈現；不設定 sourceView 會直接 crash
                // （demo 目標是 iPhone，這裡只是給 popover 一個 fallback 錨點，不精修箭頭指向位置）
                activityController.popoverPresentationController?.let { popover ->
                    popover.sourceView = presenter.view
                    popover.sourceRect = presenter.view.bounds
                }
                presenter.presentViewController(activityController, animated = true, completion = null)
            }
        }
    }
}

/** 從 keyWindow 往下找到目前最上層已 present 的 view controller，避免蓋在別的 sheet 底下。 */
private fun topMostViewController(): UIViewController? {
    var top = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (top?.presentedViewController != null) {
        top = top.presentedViewController
    }
    return top
}
