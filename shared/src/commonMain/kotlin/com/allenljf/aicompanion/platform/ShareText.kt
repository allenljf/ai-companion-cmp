package com.allenljf.aicompanion.platform

import androidx.compose.runtime.Composable

/**
 * 全專案唯一一組 expect/actual（CLAUDE.md 全域限制）。
 *
 * 用 `@Composable expect fun rememberShareText(): (String) -> Unit` 而非裸的
 * `expect fun shareText(text: String)`：兩端都需要「目前畫面在哪」才能發出分享——
 * Android 要用 `LocalContext.current` 取得 Activity context 才能 startActivity；
 * iOS 要拿到目前的 rootViewController 才能 present UIActivityViewController。
 * 這兩者都是 Compose 才知道的資訊，裸 expect fun 沒有管道拿到，勢必得另外用
 * Koin 塞一個 context/ViewController 單例——多繞一層還要處理生命週期換頁問題。
 * 包成 Composable 回傳穩定 lambda，呼叫端（結果頁 AppButton.onClick）拿到的
 * 是純 `(String) -> Unit`，不用知道底下平台細節。
 */
@Composable
expect fun rememberShareText(): (String) -> Unit
