package com.allenljf.aicompanion.platform

import androidx.compose.runtime.Composable

/**
 * 開啟外部瀏覽器（T20 新增第二組 expect/actual——CLAUDE.md 原「全專案唯一一組」限制本次需求解除，
 * 見 migration/02-ledger.md 決策補充）。理由同 [rememberShareText]：兩端都要「目前畫面在哪」才能跳轉
 * （Android 需要 Activity context、iOS 走 UIApplication 單例即可，但一起包成 Composable 維持形式一致）。
 *
 * 用途：結果頁 BottomSheet「搜尋相關產品」深連結到 KKday 前台搜尋頁
 * （migration/research-ota-product-apis.md 定調的「真深連結」做法，不接 App 內搜尋結果頁）。
 */
@Composable
expect fun rememberOpenUrl(): (String) -> Unit
