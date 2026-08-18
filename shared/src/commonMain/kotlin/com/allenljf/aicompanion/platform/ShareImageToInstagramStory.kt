package com.allenljf.aicompanion.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

/**
 * Shares an image to Instagram Stories through platform-specific implementations.
 *
 * 圖片來源刻意是 [ImageBitmap]（呼叫端用 `rememberGraphicsLayer()` 持續錄製「Hero + 黑色資訊卡」容器的
 * 當下畫面截圖，見 ui/ResultScreens.kt），不是重新下載 hero URL——確保分享出去的圖片跟使用者在畫面上
 * 看到的完全一致（比照原始碼 CompanionShareActions.shareBitmapToInstagramStories 的素材選擇）。
 *
 * 回傳 false = IG 未安裝或喚起失敗，呼叫端要自行 fallback 成系統分享（比照原版 sharePosterBitmap）。
 * [caption] 只用於 fallback 的系統分享文字——IG 限動 deep link 不支援帶入文字，呼叫端會在點擊當下
 * 另外把文案複製到剪貼簿（見 AiCompanionScreens.kt:3185-3186 的原版行為）。
 */
@Composable
expect fun rememberShareImageToInstagramStory(): suspend (image: ImageBitmap, caption: String) -> Boolean
