package com.kkday.feature.ai_companion.presentation.poster

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory
import java.io.File
import java.io.FileOutputStream

/** 「旅行 DNA 回顧」詳情頁重建 [com.kkday.feature.ai_companion.presentation.poster.ResolvedPosterAssets] 用的個別素材本機路徑。 */
data class HistoryAssetPaths(
    val heroPath: String,
    val stampPath: String,
    val tagIconPaths: List<String>,
)

/**
 * 把 share-image-v2 本地合成好的完整海報 Bitmap（僅供列表縮圖）與個別素材 Bitmap（供詳情頁即時重組排版）
 * 存成裝置本機檔案（歷史紀錄只存路徑字串，Bitmap 本身無法直接序列化進 DataStore）。
 */
@Factory
class PosterHistoryStorage(private val context: Context) {

    suspend fun save(bitmap: Bitmap, completionUuid: String): String? = withContext(Dispatchers.IO) {
        try {
            savePng(bitmap, File(posterDir(), "$completionUuid.png")).absolutePath
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 存下未疊字的原始素材（hero/stamp/tag 圖示），讓「旅行 DNA 回顧」詳情頁之後能比照 ResultScreen
     * 用 [com.kkday.feature.ai_companion.presentation.poster.PosterHeroWithBadge] 即時渲染文字，
     * 不必依賴離屏合成好的扁平圖（避免縮放造成畫質變差）。
     */
    suspend fun saveAssets(
        hero: Bitmap,
        stamp: Bitmap,
        tagIcons: List<Bitmap>,
        completionUuid: String,
    ): HistoryAssetPaths? = withContext(Dispatchers.IO) {
        try {
            val dir = posterDir()
            val heroPath = savePng(hero, File(dir, "$completionUuid-hero.png")).absolutePath
            val stampPath = savePng(stamp, File(dir, "$completionUuid-stamp.png")).absolutePath
            val tagPaths = tagIcons.mapIndexed { index, tagBitmap ->
                savePng(tagBitmap, File(dir, "$completionUuid-tag$index.png")).absolutePath
            }
            HistoryAssetPaths(heroPath = heroPath, stampPath = stampPath, tagIconPaths = tagPaths)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun load(path: String): Bitmap? {
        if (path.isBlank()) return null
        return withContext(Dispatchers.IO) {
            try {
                BitmapFactory.decodeFile(path)
            } catch (e: Exception) {
                null
            }
        }
    }

    /** 刪除歷史紀錄時一併清掉對應本機檔案，避免累積佔用空間。 */
    suspend fun deleteFiles(paths: List<String>) = withContext(Dispatchers.IO) {
        paths.forEach { path ->
            if (path.isNotBlank()) runCatching { File(path).delete() }
        }
    }

    private fun posterDir(): File = File(context.filesDir, POSTER_DIR_NAME).apply { mkdirs() }

    private fun savePng(bitmap: Bitmap, file: File): File {
        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
        return file
    }

    companion object {
        private const val POSTER_DIR_NAME = "companion_posters"
    }
}
