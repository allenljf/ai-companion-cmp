package com.allenljf.aicompanion.platform

import android.app.Activity
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberShareText(): (String) -> Unit {
    val context = LocalContext.current
    return remember(context) {
        { text: String ->
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            val chooserIntent = Intent.createChooser(sendIntent, null)
            // LocalContext 理論上是 Activity，但保底一下：非 Activity context（例如 Application）
            // 呼叫 startActivity 沒有 NEW_TASK flag 會直接 crash
            if (context !is Activity) {
                chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooserIntent)
        }
    }
}
