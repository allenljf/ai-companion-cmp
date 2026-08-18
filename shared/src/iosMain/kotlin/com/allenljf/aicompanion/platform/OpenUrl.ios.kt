package com.allenljf.aicompanion.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSCharacterSet
import platform.Foundation.NSURL
import platform.Foundation.stringByAddingPercentEncodingWithAllowedCharacters
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
            val preparedUrl = candidate
                .replace(" ", "%20")
                .let { url ->
                    url.stringByAddingPercentEncodingWithAllowedCharacters(
                        NSCharacterSet.URLQueryAllowedCharacterSet(),
                    ) ?: url
                }

            NSURL.URLWithString(preparedUrl)?.let { nsUrl ->
                if (UIApplication.sharedApplication.canOpenURL(nsUrl)) {
                    UIApplication.sharedApplication.openURL(nsUrl)
                }
            }
        }
    }
}
