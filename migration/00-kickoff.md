# 遷移 Kickoff — 交接脈絡與待決策

> 新對話從這裡開始。這份文件把「為什麼要做、目前有什麼、還沒決定什麼」交代清楚。
> 讀完這份 + `CLAUDE.md`，就可以開始 brainstorming。

---

## 一、要做什麼

把 KKday Android App 的「AI 旅伴」功能獨立重做成 **Compose Multiplatform demo app**。

- **Phase 1**：建立旅伴 → 旅行 DNA 測驗 → 人格分析＋命定城市 → 分享海報
- **Phase 2**：五種入口開場 → 收斂目的地 → 偏好問卷 → 生成逐日行程 → 自然語言修改

功能全貌請讀 `reference/docs/ai-companion-rd-guide.md`（789 行，含所有畫面、API、流程圖）。

## 二、已經定案的約束（不用再討論）

| 項目 | 決定 | 影響 |
|---|---|---|
| 定位 | **輕量化 demo**，非產品 | 不追求功能完整、不追求 pixel-perfect |
| 後端 | **換成自己的 API** | B2C envelope／簽章／header 機制全部不移植 |
| 命名 | **去 KKday/B2C 化** | `B2CApiResponse`、`KKButton` 這類命名不沿用 |
| 設計系統 | **不復刻**，只當視覺參照 | 用 Material3 + 輕量 token 自己做 |
| 移植重點 | **API 路徑 / input-output / UI 互動邏輯** | 網路層實作細節不必復刻 |

去 B2C 化的完整 API 契約已經寫好：`migration/API_CONTRACT.md`（可直接拿去實作自己的後端或 mock）。

---

## 三、量化分析（掃描原始碼得出，用來估工）

### 規模

```
ai_companion 模組總計 10,129 行 / 17 個 .kt
├── AiCompanionScreens.kt        4,663 行  （Root 導覽 + Phase 1 畫面）
├── AiCompanionViewModel.kt      1,610 行  （★ 所有業務邏輯與狀態機）
├── AiCompanionTripScreens.kt    1,148 行  （行程成果頁）
├── AiCompanionPlanScreens.kt      903 行  （材料開場 + 規劃聊天室）
├── SixZonePosterComposer.kt       319 行  （★ 海報 Bitmap 合成，最難移植）
├── AiCompanionStates.kt           299 行  （UI State + 問卷題庫）
├── AiCompanionTripListScreen.kt   253 行
└── 其他 10 個檔案                  934 行
```

### Android 依賴分佈（**只有 31 處**）

| 檔案 | android.* import 數 | 說明 |
|---|---|---|
| `CompanionShareActions.kt` | 8 | 分享（Toast/DownloadManager/MediaStore/Intent） |
| `AiCompanionScreens.kt` | 4 | Activity window、Build |
| `ShareImageV2AssetResolver.kt` | 3 | Bitmap 下載 |
| `PosterHistoryStorage.kt` | 3 | Bitmap 存檔 |
| `AiCompanionTripScreens.kt` | 3 | Intent deeplink |
| `BitmapExt.kt` | 2 | Bitmap 工具 |
| **`AiCompanionViewModel.kt`** | **2** | ← 核心邏輯幾乎無 Android 依賴 |
| `AiCompanionActivity.kt` | 2 | 進入點（CMP 會換掉） |
| `AiCompanionStates.kt` | 1 | |
| **`AiCompanionPlanScreens.kt`** | **0** | ← 完全可搬 |
| **`AiCompanionTripListScreen.kt`** | **0** | ← 完全可搬 |

詳細清單：`reference/design-system/ANDROID_ISMS.md`

### 其他移植項目

| 項目 | 數量 | 清單位置 |
|---|---|---|
| StyleDictionary token | 57 個 | `reference/design-system/USED_TOKENS.md` |
| drawable icon | 50 個 | `reference/design-system/USED_DRAWABLES.md` |
| DS 元件 | 5 個（KKButton/KKTag/KKDialog/KKTextField/DragHandle） | `reference/design-system/*.kt`（僅參照） |
| API | 16 支 | `migration/API_CONTRACT.md` |
| 畫面 | 14 個 Step + 2 個 BottomSheet | RD guide 第四章 |

---

## 四、移植難度分級

### 🟢 幾乎免費（直接搬，改幾行 import）

- `AiCompanionViewModel.kt`（1,610 行）— **這是最有價值的資產，絕對不要重寫**
- `AiCompanionPlanScreens.kt`（903 行）、`AiCompanionTripListScreen.kt`（253 行）— 0 個 Android 依賴
- `AiCompanionStates.kt`（299 行）— UI State 與問卷題庫
- domain 層（27 個 UseCase + 2 個 Repository 介面）— 純 Kotlin

### 🟡 機械但量大（可平行處理）

- model 層：Gson 註解 → kotlinx.serialization（4 個檔案）
- API service：Retrofit → Ktor（16 支）
- 57 個 token → 一個 Kotlin object
- 50 個 drawable → composeResources
- `AiCompanionTripScreens.kt`、`AiCompanionScreens.kt` 的 UI 部分

### 🔴 需要決策或重寫

| 項目 | 問題 |
|---|---|
| **海報合成**（319 行） | 純 Android `Bitmap`/`Canvas`/`Paint`，CMP 要用 `ImageBitmap` 重寫，或 expect/actual |
| **分享功能**（8 處 Android API） | 存圖到相簿、分享 Intent → 需 expect/actual |
| **DS 元件** | 已決定不復刻，但要決定 Material3 對應方案 |
| **deeplink** | `Intent.ACTION_VIEW` → expect/actual（demo 可能直接拿掉） |

---

## 五、待決策項目（brainstorming 要處理這些）

### 1. Demo 範圍：Phase 1 要做到什麼程度？⭐ 最重要

Phase 2 是核心價值（行程規劃），Phase 1 有幾個高成本項目：

| 項目 | 成本 | 選項 |
|---|---|---|
| 測驗題庫 | 中 | 原本從 DCS 來 → demo 可寫死題目 |
| 人格分析（tag → 八種稱號 mapping） | 中 | 需要自己實作 mapping 邏輯 |
| **海報產圖** | **高** | 需 gpt-image + 儲存空間 + 輪詢機制 → **建議 demo 跳過或用固定圖** |
| 社群牆 | 低 | 假資料即可 |

**要問**：Phase 1 是「完整做」「只做測驗不做海報」還是「整個跳過，只做 Phase 2」？

### 2. 目標平台

CMP 可以出 Android / iOS / Desktop / Web。**要問**：demo 要 demo 在哪些平台上？（只有 iOS+Android 的話，很多 expect/actual 可以省）

### 3. 後端策略

- 真的寫一個後端，還是先做 mock server / 本地假資料？
- 用哪個 LLM API？（原本是 gpt-5.6 系列）
- 16 支 API 全做，還是先做 Phase 2 核心四支（`travel-summary`/`recommend-city`/`travel-guide`/`travel-revise`）？

建議實作順序見 `API_CONTRACT.md` 最後一節。

### 4. 導航模型

原本是 `AiCompanionStep` enum + `when` 分支（單 Activity）。這個模式**在 CMP 上完全可用而且很輕**，建議沿用；但如果要多平台 deeplink / 返回鍵處理，可能要換 Navigation Compose。**要問**：沿用還是換？

### 5. DI 方式

原本是 Koin annotation（`@Factory`/`@Single`/KSP）。**KSP 在 CMP 的設定較繁瑣**，建議改**手寫 Koin module**（這個專案只有 ~30 個 UseCase + 3 個 Repository，手寫成本很低）。**要問**：確認改手寫？

### 6. 海報與分享要不要做

跟第 1 點連動。如果 Phase 1 只做到測驗結果文字，這兩塊（319 行 Bitmap + 8 處 Android API）可以整個不做，省掉最麻煩的 expect/actual。

---

## 六、建議的執行順序

由下往上，每層編過就是驗證：

```
0. CMP 專案骨架        ← 用 KMP Wizard 或 IntelliJ 範本產生，不要手刻
1. token + 資源         ← 57 token → object、50 icon → composeResources（可平行）
2. model                ← Gson → kotlinx.serialization
3. networking           ← Retrofit → Ktor（依 API_CONTRACT.md）
4. repository + usecase ← 幾乎原樣搬
5. ViewModel            ← 1,610 行，只改 2 處
6. 輕量 DS 元件         ← Material3 包一層，對齊 API surface
7. UI 畫面              ← 前面到位後很快
8. 平台特化（如果要做） ← 分享/海報/deeplink 的 expect/actual
```

**為什麼由下往上**：每一層編譯成功就驗證了下一層的前提，不會累積一堆錯誤到最後才爆。

---

## 七、明確的反面建議（不要做這些）

| ❌ 不要 | 為什麼 |
|---|---|
| 照 Claude design HTML/設計稿重做 UI | 現有 Compose 已包含幾十輪累積的修正（拖曳 threshold、FAB 行為、標籤互斥規則、對話接續），mockup 沒有。重做等於全部重踩 |
| 重寫 ViewModel 邏輯 | 1,610 行的狀態機是這功能最有價值的資產，且只有 2 處 Android 依賴 |
| 跑完整 SDD／先寫規格再實作 | 遷移的「規格」已經存在（現有程式碼 + RD guide），寫 spec 是重複勞動。**只有第五章那 6 個決策值得寫規格** |
| 為機械移植寫測試/文件 | Kotlin 編譯器是更嚴格的 oracle |
| 復刻 KK 設計系統 | 已定案：輕量化，Material3 即可 |
| 移植 B2C 簽章/header/envelope | 已定案：換自己後端 |

---

## 八、下一步

1. **brainstorming**：處理第五章的 6 個決策 → 產出 `migration/01-decisions.md`
2. **writing-plans**：依決策產出移植計畫 + 進度帳本 → `migration/02-ledger.md`
3. **executing-plans**：分多次對話執行，每次讀 ledger 接續

> 三份文件都放在 `migration/`，是跨對話的「進度存檔」。每次開新對話先讀它們。
