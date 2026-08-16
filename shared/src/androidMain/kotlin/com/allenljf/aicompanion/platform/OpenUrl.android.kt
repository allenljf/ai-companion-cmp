package com.allenljf.aicompanion.platform

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberOpenUrl(): (String) -> Unit {
    val context = LocalContext.current
    return remember(context) {
        { url: String ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            // 同 ShareText.android.kt：LocalContext 理論上是 Activity，保底非 Activity context 的情況
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}
