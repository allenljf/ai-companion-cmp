package com.allenljf.aicompanion.platform

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val INSTAGRAM_PACKAGE = "com.instagram.android"
private const val INSTAGRAM_ADD_TO_STORY_ACTION = "com.instagram.share.ADD_TO_STORY"

@Composable
actual fun rememberShareImageToInstagramStory(): suspend (image: ImageBitmap, caption: String) -> Boolean {
    val context = LocalContext.current
    return remember(context) {
        { image: ImageBitmap, caption: String ->
            shareToInstagramStoryOrFallback(context, image.asAndroidBitmap(), caption)
        }
    }
}

// AndroidManifest 已加 <queries><package android:name="com.instagram.android"/></queries>（API 30+ 套件可見性）
private fun isInstagramInstalled(context: Context): Boolean = try {
    context.packageManager.getPackageInfo(INSTAGRAM_PACKAGE, 0)
    true
} catch (e: PackageManager.NameNotFoundException) {
    false
}

private suspend fun shareToInstagramStoryOrFallback(
    context: Context,
    bitmap: Bitmap,
    caption: String,
): Boolean {
    if (!isInstagramInstalled(context)) return fallbackShareImage(context, bitmap, caption)
    return withContext(Dispatchers.IO) {
        try {
            @Suppress("DEPRECATION")
            val path = MediaStore.Images.Media.insertImage(
                context.contentResolver, bitmap, "ai_companion_share_stories", null,
            ) ?: return@withContext fallbackShareImage(context, bitmap, caption)
            val imageUri = Uri.parse(path)
            val intent = Intent(INSTAGRAM_ADD_TO_STORY_ACTION).apply {
                setDataAndType(imageUri, "image/*")
                setPackage(INSTAGRAM_PACKAGE)
                // demo 沒有 Facebook app id 資源（原版讀 R.string.facebook_app_id），塞 applicationId 代替；
                // IG 可能因此忽略來源標示，這是 demo 限制，不影響「分享出去的圖」這個核心行為
                putExtra("source_application", context.packageName)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.grantUriPermission(INSTAGRAM_PACKAGE, imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            withContext(Dispatchers.Main) { context.startActivity(intent) }
            true
        } catch (e: Exception) {
            fallbackShareImage(context, bitmap, caption)
        }
    }
}

/** IG 未安裝或喚起失敗：退回系統分享這張圖（比照原版 CompanionShareActions.sharePosterBitmap）。 */
private suspend fun fallbackShareImage(context: Context, bitmap: Bitmap, caption: String): Boolean =
    withContext(Dispatchers.IO) {
        try {
            @Suppress("DEPRECATION")
            val path = MediaStore.Images.Media.insertImage(
                context.contentResolver, bitmap, "ai_companion_share", null,
            ) ?: return@withContext false
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, Uri.parse(path))
                if (caption.isNotBlank()) putExtra(Intent.EXTRA_TEXT, caption)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooserIntent = Intent.createChooser(intent, null)
            if (context !is Activity) chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            withContext(Dispatchers.Main) { context.startActivity(chooserIntent) }
            true
        } catch (e: Exception) {
            false
        }
    }
