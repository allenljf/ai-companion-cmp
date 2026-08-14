package com.kkday.feature.ai_companion.presentation.compose

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.provider.MediaStore
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.kkday.library.common.extension.createDownloadManagerImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 分享動作。
 * - legacy（v1，`*Image`/`downloadImage` 系列）：ready 海報為 CDN PNG URL，Coil 現場下載後走 MediaStore
 * - share-image-v2（`*Bitmap` 系列）：海報已是本地離屏合成好的 1152×2048 Bitmap，直接存 MediaStore，
 *   不重新下載、也不分享個別素材，確保輸出永遠是完整合成結果
 * TODO: failed 狀態的 share_fallback 文字卡轉 bitmap 分享（仿 VoucherScreenShotGeneratorImpl）。
 */
object CompanionShareActions {

    private const val INSTAGRAM_PACKAGE = "com.instagram.android"
    private const val INSTAGRAM_ADD_TO_STORY_ACTION = "com.instagram.share.ADD_TO_STORY"

    fun isInstagramInstalled(context: Context): Boolean = try {
        context.packageManager.getPackageInfo(INSTAGRAM_PACKAGE, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }

    fun downloadImage(context: Context, url: String) {
        if (url.isBlank()) return
        val request = Uri.parse(url).createDownloadManagerImageRequest()
        (context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager)?.enqueue(request)
    }

    fun shareText(context: Context, text: String) {
        if (text.isBlank()) return
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, null))
    }

    suspend fun sharePosterImage(context: Context, url: String, caption: String): Boolean {
        if (url.isBlank()) return false
        return try {
            val request = ImageRequest.Builder(context).data(url).allowHardware(false).build()
            val bitmap = (context.imageLoader.execute(request) as? SuccessResult)
                ?.drawable?.let { it as? BitmapDrawable }?.bitmap ?: return false
            @Suppress("DEPRECATION")
            val path = MediaStore.Images.Media.insertImage(
                context.contentResolver, bitmap, "kkday_companion", null,
            ) ?: return false
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, Uri.parse(path))
                if (caption.isNotBlank()) putExtra(Intent.EXTRA_TEXT, caption)
                // 接收端 App 需要讀取權限才拿得到 MediaStore content URI
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, null))
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun shareToInstagramStories(
        context: Context,
        url: String,
        caption: String,
        fallback: suspend (Context, String, String) -> Boolean = ::sharePosterImage,
    ): Boolean {
        if (!isInstagramInstalled(context)) return fallback(context, url, caption)
        if (url.isBlank()) return false
        return try {
            val request = ImageRequest.Builder(context).data(url).allowHardware(false).build()
            val bitmap = (context.imageLoader.execute(request) as? SuccessResult)
                ?.drawable?.let { it as? BitmapDrawable }?.bitmap
                ?: return fallback(context, url, caption)
            @Suppress("DEPRECATION")
            val path = MediaStore.Images.Media.insertImage(
                context.contentResolver, bitmap, "kkday_companion_stories", null,
            ) ?: return fallback(context, url, caption)
            val imageUri = Uri.parse(path)
            val intent = Intent(INSTAGRAM_ADD_TO_STORY_ACTION).apply {
                setDataAndType(imageUri, "image/*")
                setPackage(INSTAGRAM_PACKAGE)
                putExtra("source_application", context.getString(com.kkday.member.resource.R.string.facebook_app_id))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.grantUriPermission(INSTAGRAM_PACKAGE, imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            fallback(context, url, caption)
        }
    }

    /**
     * share-image-v2 專用：海報已是本地離屏合成好的完整 1152×2048 Bitmap，不需要（也不應該）
     * 重新下載，直接存 MediaStore 分享，確保分享輸出永遠是完整合成結果（對應需求
     * 「Share and Export Output Integrity」），而非個別素材（如 hero 原圖）。
     */
    suspend fun sharePosterBitmap(context: Context, bitmap: Bitmap, caption: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                @Suppress("DEPRECATION")
                val path = MediaStore.Images.Media.insertImage(
                    context.contentResolver, bitmap, "kkday_companion", null,
                ) ?: return@withContext false
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_STREAM, Uri.parse(path))
                    if (caption.isNotBlank()) putExtra(Intent.EXTRA_TEXT, caption)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                withContext(Dispatchers.Main) {
                    context.startActivity(Intent.createChooser(intent, null))
                }
                true
            } catch (e: Exception) {
                false
            }
        }

    suspend fun shareBitmapToInstagramStories(
        context: Context,
        bitmap: Bitmap,
        caption: String,
        fallback: suspend (Context, Bitmap, String) -> Boolean = ::sharePosterBitmap,
    ): Boolean {
        if (!isInstagramInstalled(context)) return fallback(context, bitmap, caption)
        return withContext(Dispatchers.IO) {
            try {
                @Suppress("DEPRECATION")
                val path = MediaStore.Images.Media.insertImage(
                    context.contentResolver, bitmap, "kkday_companion_stories", null,
                ) ?: return@withContext fallback(context, bitmap, caption)
                val imageUri = Uri.parse(path)
                val intent = Intent(INSTAGRAM_ADD_TO_STORY_ACTION).apply {
                    setDataAndType(imageUri, "image/*")
                    setPackage(INSTAGRAM_PACKAGE)
                    putExtra("source_application", context.getString(com.kkday.member.resource.R.string.facebook_app_id))
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.grantUriPermission(INSTAGRAM_PACKAGE, imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                withContext(Dispatchers.Main) {
                    context.startActivity(intent)
                }
                true
            } catch (e: Exception) {
                fallback(context, bitmap, caption)
            }
        }
    }

    /** 直接把本地合成好的海報 Bitmap 存進裝置相簿（無 remote URL 可用 DownloadManager，改走 MediaStore）。 */
    suspend fun downloadBitmap(context: Context, bitmap: Bitmap): Boolean = withContext(Dispatchers.IO) {
        try {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "kkday_companion", null) != null
        } catch (e: Exception) {
            false
        }
    }
}
