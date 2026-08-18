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
        { rawUrl: String ->
            val normalizedUrl = rawUrl.trim().takeIf { it.isNotEmpty() } ?: return@remember
            val candidate = when {
                normalizedUrl.startsWith("http://") || normalizedUrl.startsWith("https://") -> normalizedUrl
                normalizedUrl.startsWith("//") -> "https:$normalizedUrl"
                else -> "https://$normalizedUrl"
            }

            val preparedUrl = if (candidate.contains(" ")) {
                candidate.replace(" ", "%20")
            } else {
                candidate
            }

            val nsUrl = NSURL.URLWithString(preparedUrl)
                ?: NSURL.URLWithString(preparedUrl.replace("%20", " "))
                ?: return@remember

            UIApplication.sharedApplication.openURL(
                nsUrl,
                options = emptyMap<Any?, Any?>(),
                completionHandler = null,
            )
        }
    }
}
