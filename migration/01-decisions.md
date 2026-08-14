# 決策紀錄 — brainstorming 結論

> 2026-08-14 與 Allen 逐題確認 `00-kickoff.md` 第五章的 6 個待決策項目。
> 本文件是後續移植計畫（`02-ledger.md`）的依據。

---

## 決策總表

| # | 項目 | 決定 |
|---|---|---|
| 1 | Demo 範圍 | **Phase 1 測驗做、海報產圖跳過**；Phase 2 完整做 |
| 2 | 目標平台 | **Android + iOS**（不做 Desktop / Web） |
| 3a | 後端策略 | **先 mock、留接口換真後端**（Repository 介面不變，之後只換實作） |
| 3b | API 範圍 | **Phase 2 核心四支優先**，再補 Phase 1 測驗所需；海報／社群 API 不做 |
| 4 | 導航模型 | **沿用 `Step` enum + `when` 分支**，不引入 Navigation Compose |
| 5 | DI 方式 | **手寫 Koin module**，不用 Koin annotation + KSP |
| 6 | 海報與分享 | **海報合成不做；保留純文字分享**（一組 expect/actual 呼叫系統分享面板） |

---

## 各決策細節與影響

### 1. Demo 範圍：Phase 1 測驗做、海報跳過

- 保留完整產品敘事：建立旅伴 → DNA 測驗 → 人格分析＋命定城市 → 進 Phase 2。
- 測驗題庫**寫死在 app 內**（原本從 DCS 來），人格 mapping（tag → 八種稱號）自己實作。
- 海報產圖整段跳過：不接 gpt-image、不做輪詢、不做儲存空間。
- 社群牆如果畫面需要，用假資料。
- **省掉的移植項**：`SixZonePosterComposer.kt`（319 行）、`PosterHistoryStorage.kt`、`ShareImageV2AssetResolver.kt`、`BitmapExt.kt`。

### 2. 目標平台：Android + iOS

- KMP Wizard 產骨架時只勾 Android + iOS。
- 開發驗證：Android 用模擬器/實機，iOS 用 Mac 上的 Simulator。
- 影響：expect/actual 面積縮到最小（目前只剩決策 6 的分享一組）。

### 3. 後端策略：mock 優先、留接口

- 第一階段用**本地假資料 / 內建 mock**，回應形狀完全依 `migration/API_CONTRACT.md`。
- Repository 介面照原樣移植；mock 與真實 client 都實作同一介面，之後換真後端只換 DI 綁定。
- API 實作順序：
  1. Phase 2 核心四支：`travel-summary` / `recommend-city` / `travel-guide` / `travel-revise`
  2. Phase 1 測驗流程所需的最小集合
  3. 其餘（海報、社群相關）**不做**
- mock 要能模擬「自然語言修改行程」的動態互動（至少多組預錄回應），避免 demo 效果太假。
- 軟失敗模式照原樣保留：`fail_reason` 有值 = 回 200 + 兜底文案。

### 4. 導航模型：沿用 Step enum

- `AiCompanionStep` enum + `when` 分支在 CMP 完全可用，且 step 轉換邏輯已內建在 ViewModel 狀態機——換導航框架等於重寫這塊，違反「不重寫 ViewModel」原則。
- 返回鍵：BackHandler 對接 step 回退。
- deeplink 不做（決策 1 已省掉分享回流場景）。

### 5. DI：手寫 Koin module

- 移除 `@Factory` / `@Single` 註解與 KSP 設定，集中手寫 Koin module。
- 規模：~30 個 UseCase + 3 個 Repository + ViewModel，手寫成本低、依賴關係一目了然。

### 6. 海報不做、保留純文字分享

- 測驗結果頁保留一個分享按鈕，分享**純文字**（人格稱號＋命定城市）。
- 實作：一組 expect/actual —— Android 用 `Intent.ACTION_SEND`、iOS 用 `UIActivityViewController`。
- 這是全專案**唯一**一組平台特化程式碼。

---

## 專案骨架的建立方式（附帶決議）

- 由 Allen 用 **KMP Wizard（kmp.jetbrains.com）或 Android Studio 範本**產生 Android + iOS 骨架，不由 AI 手刻 Gradle 設定（版本組合官方驗證過，避免相容性地雷）。
- 骨架建好、能編譯後才開始移植實作。

## 對執行順序的影響（對照 kickoff 第六章）

```
0. CMP 專案骨架（Android + iOS）   ← Allen 用 Wizard 產生
1. token + 資源                    ← 不變
2. model                           ← 不變（海報相關 DTO 可略）
3. networking                      ← 只做 mock repository（依 API_CONTRACT.md 形狀）
4. repository + usecase            ← 海報／分享圖相關 UseCase 不搬
5. ViewModel                       ← 不變（海報相關分支保留兜底或移除，實作時判斷）
6. 輕量 DS 元件                    ← 不變
7. UI 畫面                         ← 海報畫面不做；結果頁加純文字分享鈕
8. 平台特化                        ← 只剩一組：文字分享 expect/actual
```

## 下一步

1. Allen 用 KMP Wizard 建立 Android + iOS 骨架並確認可編譯。
2. 依本文件跑 `writing-plans` 產出移植計畫與 `02-ledger.md`。
3. 分多次對話執行，每次讀 ledger 接續。
