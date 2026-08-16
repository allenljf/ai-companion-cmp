package com.allenljf.aicompanion.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberOpenUrl(): (String) -> Unit {
    return remember {
        { url: String ->
            NSURL.URLWithString(url)?.let { nsUrl ->
                UIApplication.sharedApplication.openURL(nsUrl)
            }
        }
    }
}
