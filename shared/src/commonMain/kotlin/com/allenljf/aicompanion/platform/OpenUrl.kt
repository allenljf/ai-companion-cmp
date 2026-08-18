package com.allenljf.aicompanion.platform

import androidx.compose.runtime.Composable

/**
 * Opens an external URL through the current platform's browser or URL handler.
 * Android needs an Activity context; iOS uses UIApplication, so the API remains Composable on both platforms.
 */
@Composable
expect fun rememberOpenUrl(): (String) -> Unit
