package com.kkday.feature.ai_companion.presentation.poster

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.annotation.DrawableRes
import androidx.compose.ui.geometry.Size
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.kkday.design.utils.getBitmapFromImage
import com.kkday.library.common.helper.IBitmapHelper
import com.kkday.model.companion.AssetSlot
import com.kkday.model.companion.ShareImageV2Content
import com.kkday.model.companion.ShareImageV2Result
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.koin.core.annotation.Factory
import timber.log.Timber

/** Hero + 目的地郵戳 + 三個 tag 圓圈合成所需、每個素材槽位都已 settled（Remote 或 Fallback）的最終 Bitmap。 */
data class ResolvedPosterAssets(
    val hero: Bitmap,
    val stamp: Bitmap,
    val tagIcons: List<Bitmap>,
    val content: ShareImageV2Content,
    val qrCode: Bitmap?,
)

/**
 * 將 [ShareImageV2Result] 的素材槽位（hero/stamp/tag×3）各自獨立解析成最終 Bitmap：
 * - remote 下載/解碼成功 → CenterCrop 至對應槽位尺寸（stamp/tag 另做白底去背）
 * - remote 為 null、下載失敗或解碼失敗 → 依 fallbackCategory 使用內建 bundled 素材
 * 對應需求「Per-Slot Asset Fallback」：任何一槽失敗都不影響其他槽位，且最終一定產出完整素材集合。
 */
@Factory
class ShareImageV2AssetResolver(
    private val context: Context,
    private val bitmapHelper: IBitmapHelper,
) {

    suspend fun resolve(result: ShareImageV2Result): ResolvedPosterAssets = coroutineScope {
        val heroDeferred = async {
            resolveSlot(
                slot = result.hero,
                targetWidth = SixZonePosterSpec.HERO_WIDTH,
                targetHeight = SixZonePosterSpec.HERO_HEIGHT,
                whiten = false,
                fallbackDrawable = PosterFallbackAssets.heroDrawable(result.hero.fallbackCategory),
            )
        }
        val stampDeferred = async {
            resolveSlot(
                slot = result.stamp,
                targetWidth = SixZonePosterSpec.STAMP_SIZE,
                targetHeight = SixZonePosterSpec.STAMP_SIZE,
                whiten = true,
                fallbackDrawable = PosterFallbackAssets.stampDrawable(),
            )
        }
        val tagDeferreds = result.tagIcons.map { slot ->
            async {
                resolveSlot(
                    slot = slot,
                    targetWidth = SixZonePosterSpec.TAG_CIRCLE_SIZE,
                    targetHeight = SixZonePosterSpec.TAG_CIRCLE_SIZE,
                    whiten = true,
                    fallbackDrawable = PosterFallbackAssets.tagDrawable(slot.fallbackCategory),
                )
            }
        }
        val qrDeferred = async {
            runCatching { bitmapHelper.convertUrlToQRCode(KKDAY_QR_TARGET_URL, SixZonePosterSpec.QR_SIZE) }
                .getOrNull()
        }

        ResolvedPosterAssets(
            hero = heroDeferred.await(),
            stamp = stampDeferred.await(),
            tagIcons = tagDeferreds.map { it.await() },
            content = result.content,
            qrCode = qrDeferred.await(),
        )
    }

    private suspend fun resolveSlot(
        slot: AssetSlot,
        targetWidth: Int,
        targetHeight: Int,
        whiten: Boolean,
        @DrawableRes fallbackDrawable: Int,
    ): Bitmap {
        val remote = slot.url?.takeIf { it.isNotBlank() }?.let { downloadBitmap(it) }
        val resolved = remote?.let { if (whiten) it.removeWhiteBackground() else it }
            ?: getBitmapFromImage(context, fallbackDrawable, Size(targetWidth.toFloat(), targetHeight.toFloat()))
            ?: return Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        return resolved.centerCrop(targetWidth, targetHeight)
    }

    /**
     * 這些素材網址常是「目的地素材包」共用（同一目的地被推薦多次時網址不變），若 CDN 剛好還在傳播、
     * 第一次下載遇到 404/例外，Coil 預設會把失敗結果快取住，之後即使 CDN 已經有圖也會一直讀到舊的失敗快取。
     * 素材本身只會在 ready 時下載這一次，快取沒有實質好處、只有讀到 stale 失敗結果的風險，故關閉記憶體/硬碟快取。
     */
    private suspend fun downloadBitmap(url: String): Bitmap? = try {
        val request = ImageRequest.Builder(context)
            .data(url)
            .allowHardware(false)
            .memoryCachePolicy(CachePolicy.DISABLED)
            .diskCachePolicy(CachePolicy.DISABLED)
            .build()
        when (val result = context.imageLoader.execute(request)) {
            is SuccessResult -> (result.drawable as? BitmapDrawable)?.bitmap
            is ErrorResult -> {
                Timber.e(result.throwable, "ShareImageV2AssetResolver: 素材下載失敗，將退回 fallback 圖 url=$url")
                null
            }
        }
    } catch (e: Exception) {
        Timber.e(e, "ShareImageV2AssetResolver: 素材下載發生例外，將退回 fallback 圖 url=$url")
        null
    }

    companion object {
        const val KKDAY_QR_TARGET_URL = "https://www.kkday.com/zh-tw"
    }
}
