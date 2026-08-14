# 移植計畫 + 進度帳本

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.
>
> **跨對話交接**：每次新對話先讀 `CLAUDE.md` → `00-kickoff.md` → `01-decisions.md` → 本檔。做完任何 task 立刻更新本檔的 checkbox 與帳本狀態欄，並 commit。

**Goal:** 把 AI 旅伴（Phase 1 測驗 + Phase 2 行程規劃）移植成可在 Android + iOS 跑的 CMP demo app，mock 資料驅動，不依賴後端部署進度。

**Architecture:** 單一 `shared` 模組承載全部邏輯與 UI（demo 規模不需要多模組）。分層由下往上移植：token → model → repository(mock) → usecase → ViewModel → DS 元件 → 畫面 → 平台特化。每層以「雙 target 編譯通過」為完成標準。

**Tech Stack:** Kotlin 2.4.10 / CMP 1.11.1 / kotlinx.serialization / Koin（手寫 module）/ multiplatform-settings（本地儲存）/ Ktor client（後端就緒後才加）

## Global Constraints（來自 01-decisions.md 與 CLAUDE.md，每個 task 都適用）

- 目標平台只有 **Android + iOS**；expect/actual 只允許一組（文字分享）
- **海報全鏈路不做**：poster/ 目錄 7 檔、`FetchShareImageV2UseCase`、share-image API 一律跳過
- **不重寫 ViewModel 狀態機**；UI 搬程式碼、只換葉節點
- 命名去 KKday/B2C 化：`KKButton`→`AppButton` 等；package 一律 `com.allenljf.aicompanion.*`
- Gson → kotlinx.serialization（`@SerializedName`→`@SerialName`）；Retrofit 不移植，資料來源先走 mock
- 導航沿用 `AiCompanionStep` enum + `when`；DI 手寫 Koin module
- 硬編字串可接受但標 `// TODO: i18n`；註解只寫「為什麼」
- 每層完成即編譯驗證：`./gradlew :androidApp:assembleDebug :shared:compileKotlinIosSimulatorArm64`
- reference/ 唯讀；原專案在 `~/Documents/kkday-android-member-2/`

## 目標 package 佈局（shared/src/commonMain/kotlin/com/allenljf/aicompanion/）

```
theme/        Tokens.kt, AppTheme.kt
model/        ApiModels.kt, DomainModels.kt, ModelMappings.kt, TripProductSearchModels.kt
data/         CompanionRepository.kt(介面), CompanionOrderRepository.kt, TripProductSearchRepository.kt,
              CompanionApiException.kt, LocalCompanionStore.kt,
              mock/MockCompanionRepository.kt, mock/MockData.kt, …
domain/       27 個 UseCase（扣掉 FetchShareImageV2UseCase）
viewmodel/    AiCompanionStates.kt, AiCompanionViewModel.kt
ui/components/ AppButton.kt, AppTag.kt, AppDialog.kt, AppTextField.kt, DragHandle.kt
ui/            PlanScreens.kt, TripListScreen.kt, TripScreens.kt, CompanionScreens.kt（Root+Phase1）
di/           AppModule.kt（手寫 Koin）
platform/     ShareText.kt（expect）→ androidMain / iosMain 各一個 actual
```

---

## Tasks

### T1：建置設定（依賴與外掛）

**Files:** Modify `gradle/libs.versions.toml`、`shared/build.gradle.kts`

- [x] 加 kotlinx-serialization plugin（版本跟 kotlin 2.4.10）+ `kotlinx-serialization-json`
- [x] 加 `io.insert-koin:koin-core` 與 `koin-compose-viewmodel`（Koin 4.x）、`com.russhwolf:multiplatform-settings`（版本以當下最新 stable 為準，解析失敗就查 Maven Central 換版號）
- [x] Ktor **先不加**（後端就緒後的 T17 才加）
- [x] 編譯驗證雙 target
- [x] Commit `chore: 加入 serialization/koin/settings 依賴`

### T2：Design token 層

**Files:** Create `theme/Tokens.kt`、`theme/AppTheme.kt`；來源 `reference/design-system/USED_TOKENS.md`（57 個）

**Produces:** `object Tokens { val colorXxx: Color; val spacingXxx: Dp; … }`、`AppTheme { content }`（Material3 MaterialTheme 包一層，colorScheme 對到 token）

- [x] 57 個 token 翻成一個 Kotlin object（顏色 `Color(0xFF…)`、間距 `Dp`、字級 `TextStyle`）
- [x] `AppTheme` 掛 Material3，demo 只做 light theme
- [x] 編譯驗證 + Commit `feat: design token 層`

### T3：icon 資源

**Files:** Create `shared/src/commonMain/composeResources/drawable/*.xml`；來源 `reference/design-system/USED_DRAWABLES.md`（50 個）

- [x] 從原專案複製 vector xml；**android 專屬屬性（theme attr 引用等）要改成寫死值**（實際檢查後 36 個 icon 皆已是寫死 hex 色碼，無需改寫）
- [x] 海報/分享專用 icon 過濾掉不搬（對照 USED_DRAWABLES 標註的使用處）：過濾 14 個（6 個 hero + 1 個 stamp + 7 個 tag fallback），皆只被 `poster/PosterFallbackAssets.kt` 引用
- [x] 用 `Res.drawable.*` 在一個暫時 preview composable 裡點名驗證可解析，驗證後移除
- [x] 編譯驗證 + Commit `feat: icon 資源移植`

### T4：model 層

**Files:** Create `model/ApiModels.kt`(502行)、`model/DomainModels.kt`(465行)、`model/ModelMappings.kt`(255行)、`model/TripProductSearchModels.kt`(21行)；來源 `reference/android-src/model/`

**Produces:** 後續 repository/usecase/viewmodel 引用的全部 DTO 與 domain 型別，名稱與欄位**與原始碼一致**（去 B2C 前綴）

- [x] `@SerializedName("x")` → `@SerialName("x")` + class 加 `@Serializable`，import 換 kotlinx
- [x] share-image / poster 相關 DTO 跳過不搬（`ShareImageV2*` 等，對照 API_CONTRACT.md）
- [x] B2C envelope 型別（`{metadata,data}`）不搬；mapping 檔案裡引用到的話直接攤平
- [x] 編譯驗證 + Commit `feat: model 層（Gson→kotlinx.serialization）`

### T5：repository 契約 + mock 實作

**Files:** Create `data/CompanionRepository.kt`(167行)、`data/CompanionOrderRepository.kt`、`data/TripProductSearchRepository.kt`、`data/CompanionApiException.kt`(79行)、`data/mock/MockCompanionRepository.kt`、`data/mock/MockCompanionOrderRepository.kt`、`data/mock/MockTripProductSearchRepository.kt`、`data/mock/MockData.kt`；來源 `reference/android-src/domain-contract/`、`data-repository/`、`migration/API_CONTRACT.md`

**Interfaces:** Produces：與原 domain-contract 同名同簽章的三個介面（suspend fun，回傳 domain model）。**Retrofit 的 `ICompanionApiService` 不移植**，mock 直接實作 repository 介面。

- [x] 三個介面照搬（去 KKday 命名），`CompanionApiException` 照搬（瘦身：拿掉 B2C envelope 解析邏輯，保留錯誤碼常數）
- [x] Mock 實作：回應形狀完全依 `API_CONTRACT.md`；`travel-guide` 依城市選預錄範本（大阪/東京/首爾+通用 fallback）、`travel-revise` 依訊息關鍵字挑情境＋呼叫次數輪替 fallback，模擬自然語言修改的動態感；每支加 300–800ms delay 模擬網路
- [x] 軟失敗保留：`CompanionMockConfig.forceFailReason` 開關，覆蓋 9 支 LLM 端點（quiz/quiz-completions/self-introduction/travel-summary 系列/recommend-city/travel-guide/travel-revise）
- [x] share-image 相關方法不搬；`fetchShareImageV2` 與 `isMockEnabled`（無人呼叫的網路層開關）從介面移除
- [x] 編譯驗證 + Commit `feat: repository 契約與 mock 實作`

### T6：本地儲存

**Files:** Create `data/LocalCompanionStore.kt`；來源：原 repository impl 裡的 SharedPreferences/DataStore 邏輯

**Produces:** `class LocalCompanionStore(settings: Settings)`，提供 local companion / quiz history / saved trips / shown question counts 的存取，供 8 個 local UseCase 使用

- [x] 用 multiplatform-settings 實作，JSON 序列化存字串
- [x] 編譯驗證 + Commit `feat: 本地儲存層`

### T7：UseCase 層

**Files:** Create `domain/*.kt`；來源 `reference/android-src/domain-usecase/`（27 檔）

- [x] 26 個照搬（多數 <45 行，純轉呼叫）；**跳過 `FetchShareImageV2UseCase`**
- [x] `IsChineseLanguageUseCase`：原本讀 Android locale → demo 直接回傳 true（寫死繁中），標 `// TODO: locale`
- [x] 編譯驗證 + Commit `feat: usecase 層`

### T8：UI State + ViewModel（★ 核心）

**Files:** Create `viewmodel/AiCompanionStates.kt`(299行)、`viewmodel/AiCompanionViewModel.kt`(1,610行)；來源 `reference/android-src/feature/viewModel/`

**Interfaces:** Produces：`AiCompanionViewModel`（繼承 jetbrains `androidx.lifecycle.ViewModel`）、`AiCompanionStep` enum、全部 UiState data class——**名稱簽章與原始碼一致**，畫面層照原樣引用。

- [ ] `AiCompanionStates.kt` 照搬（1 處 Android 依賴換掉；題庫若引用 DCS 常數改寫死）
- [ ] `AiCompanionViewModel.kt` 照搬，只處理 2 處 Android 依賴與 import 換名——**狀態機邏輯一行都不改**
- [ ] 海報/分享圖相關的 state 與 method：**保留兜底或整段移除，以「編譯最小改動」為準則現場判斷**，移除的話在本檔記錄清單
- [ ] 編譯驗證 + Commit `feat: 移植 ViewModel 狀態機`

### T9：輕量 DS 元件

**Files:** Create `ui/components/AppButton.kt`、`AppTag.kt`、`AppDialog.kt`、`AppTextField.kt`、`DragHandle.kt`；來源 `reference/design-system/*.kt`（**僅對齊 API surface，不復刻內部**）

**Produces:** 與原 KK 元件**同參數簽章**的 5 個 composable（改名 App*），讓畫面層搬過來時只需改 import 與名稱

- [ ] 每個元件用 Material3 對應件包一層 + Tokens 上色
- [ ] 編譯驗證 + Commit `feat: 輕量 DS 元件`

### T10：TripList 畫面

**Files:** Create `ui/TripListScreen.kt`；來源 `AiCompanionTripListScreen.kt`（253行，0 Android 依賴）

- [ ] 照搬，換 import（DS 元件→App*、R.drawable→Res.drawable）
- [ ] 編譯驗證 + Commit `feat: TripList 畫面`

### T11：Plan 畫面（材料開場 + 規劃聊天室）

**Files:** Create `ui/PlanScreens.kt`；來源 `AiCompanionPlanScreens.kt`（903行，0 Android 依賴）

- [ ] 照搬，換 import；拖曳 threshold、對話接續等行為邏輯**不動**
- [ ] 編譯驗證 + Commit `feat: Plan 畫面`

### T12：Trip 成果畫面

**Files:** Create `ui/TripScreens.kt`；來源 `AiCompanionTripScreens.kt`（1,148行，3 處 Android 依賴＝Intent deeplink）

- [ ] deeplink 3 處：demo 拿掉，按鈕改 no-op 或隱藏，標 `// TODO: deeplink`
- [ ] 其餘照搬
- [ ] 編譯驗證 + Commit `feat: Trip 成果畫面`

### T13：Root 導覽 + Phase 1 畫面（最大檔）

**Files:** Create `ui/CompanionScreens.kt`（可拆 2–3 檔：Root 導覽 / Quiz 流程 / 結果頁）；來源 `AiCompanionScreens.kt`（4,663行，4 處 Android 依賴）

- [ ] Root `when(step)` 導覽照搬；BackHandler 用 CMP 的 `androidx.compose.ui.backhandler.BackHandler` 對接 step 回退
- [ ] 4 處 Android 依賴（Activity window / Build）：window 效果直接移除、`Build.VERSION` 分支取新行為
- [ ] 海報產圖/分享畫面區塊整段跳過；測驗結果頁只到人格＋命定城市，加一顆純文字分享鈕（呼叫 T15 的 `shareText`）
- [ ] 社群牆若畫面需要 → MockData 假資料
- [ ] `AiCompanionPhase2Previews.kt` 不搬（Android 專屬 tooling）
- [ ] 編譯驗證 + Commit `feat: Root 導覽與 Phase 1 畫面`

### T14：DI + App 進入點

**Files:** Create `di/AppModule.kt`、Modify `App.kt`；Delete `Greeting.kt`、`GreetingUtil.kt`、`Platform*.kt`（骨架樣板）；來源參照 `feature/di/AiCompanionAnnotationModule.kt`

- [ ] 手寫 Koin module：3 個 mock repository（`single`）+ LocalCompanionStore + 全部 UseCase（`factory`）+ ViewModel
- [ ] `App.kt` = AppTheme + KoinApplication + Root 畫面；Android `MainActivity` 與 iOS `MainViewController` 接上
- [ ] 骨架樣板檔與其測試一併刪除
- [ ] 編譯驗證 + Commit `feat: DI 與進入點串接`

### T15：文字分享 expect/actual（唯一平台特化）

**Files:** Create `platform/ShareText.kt`（expect fun shareText(text: String)）、`platform/ShareText.android.kt`（`Intent.ACTION_SEND`，context 由 Koin android context 取）、`platform/ShareText.ios.kt`（`UIActivityViewController`）

- [ ] 三檔實作 + 結果頁分享鈕接上
- [ ] 編譯驗證 + Commit `feat: 文字分享 expect/actual`

### T16：雙平台實跑驗證

- [ ] Android：emulator 跑完整流程（建立旅伴→測驗→結果→Phase 2 開場→行程→自然語言修改）
- [ ] iOS：Simulator 跑同一流程
- [ ] 發現的移植 bug 記入本檔「已知問題」節，修完再結
- [ ] Commit + push，更新本檔全部狀態

### T17：真後端接入（等 API 部署好才做）

**Files:** Create `data/remote/CompanionApiClient.kt`（Ktor）、`data/remote/RemoteCompanionRepository.kt`；Modify `libs.versions.toml`（ktor-client-core/content-negotiation/serialization + okhttp/darwin engine）、`di/AppModule.kt`

- [ ] Ktor client 依 `API_CONTRACT.md` 16→實作範圍內的路徑實作
- [ ] DI 綁定 mock→remote 用一個 flag 切換（保留 mock 供離線 demo）
- [ ] 雙平台實跑 + Commit

---

## 逐檔帳本（來源 → 目標）

狀態：⬜ 未動工 / 🔄 進行中 / ✅ 完成 / ⛔ 不移植

| 來源（reference/android-src/） | 行數 | 目標 | Task | 狀態 |
|---|---|---|---|---|
| model/ApiModels.kt | 502 | model/ApiModels.kt | T4 | ✅ |
| model/DomainModels.kt | 465 | model/DomainModels.kt | T4 | ✅ |
| model/ModelMappings.kt | 255 | model/ModelMappings.kt | T4 | ✅ |
| model/TripProductSearchModels.kt | 21 | model/TripProductSearchModels.kt | T4 | ✅ |
| api-service/ICompanionApiService.kt | 93 | —（mock 直接實作 repository；T17 才有 Ktor client） | T5/T17 | ⛔ |
| api-service/ITripProductSearchApiService.kt | 19 | — 同上 | T5/T17 | ⛔ |
| domain-contract/CompanionRepository.kt | 167 | data/CompanionRepository.kt | T5 | ✅ |
| domain-contract/CompanionOrderRepository.kt | 18 | data/CompanionOrderRepository.kt | T5 | ✅ |
| domain-contract/TripProductSearchRepository.kt | 20 | data/TripProductSearchRepository.kt | T5 | ✅ |
| data-repository/CompanionRepositoryImpl.kt | 555 | data/mock/MockCompanionRepository.kt + data/mock/MockData.kt（重寫為 mock） | T5 | ✅ |
| data-repository/CompanionOrderRepositoryImpl.kt | 83 | data/mock/MockCompanionOrderRepository.kt | T5 | ✅ |
| data-repository/TripProductSearchRepositoryImpl.kt | 28 | data/mock/MockTripProductSearchRepository.kt | T5 | ✅ |
| data-repository/CompanionApiException.kt | 79 | data/CompanionApiException.kt（瘦身，拿掉 B2C envelope 解析） | T5 | ✅ |
| domain-usecase/（26 檔，扣 ShareImageV2） | ~600 | domain/*.kt | T7 | ✅ |
| domain-usecase/FetchShareImageV2UseCase.kt | 21 | — | — | ⛔ |
| feature/viewModel/AiCompanionStates.kt | 299 | viewmodel/AiCompanionStates.kt | T8 | ⬜ |
| feature/viewModel/AiCompanionViewModel.kt | 1,610 | viewmodel/AiCompanionViewModel.kt | T8 | ⬜ |
| feature/presentation/compose/AiCompanionTripListScreen.kt | 253 | ui/TripListScreen.kt | T10 | ⬜ |
| feature/presentation/compose/AiCompanionPlanScreens.kt | 903 | ui/PlanScreens.kt | T11 | ⬜ |
| feature/presentation/compose/AiCompanionTripScreens.kt | 1,148 | ui/TripScreens.kt | T12 | ⬜ |
| feature/presentation/compose/AiCompanionScreens.kt | 4,663 | ui/CompanionScreens.kt（可拆檔） | T13 | ⬜ |
| feature/presentation/compose/AiCompanionPhase2Previews.kt | 311 | — | — | ⛔ |
| feature/presentation/compose/CompanionShareActions.kt | 175 | platform/ShareText.kt（重寫為純文字分享） | T15 | ⬜ |
| feature/presentation/poster/（7 檔） | ~880 | — 海報全鏈路不做 | — | ⛔ |
| feature/presentation/AiCompanionActivity.kt | 82 | —（CMP 進入點取代） | T14 | ⛔ |
| feature/di/AiCompanionAnnotationModule.kt | 76 | di/AppModule.kt（手寫重寫） | T14 | ⬜ |
| design-system/USED_TOKENS.md（57 token） | — | theme/Tokens.kt | T2 | ✅ |
| design-system/USED_DRAWABLES.md（50 icon） | — | composeResources/drawable/（36 個，過濾 14 個海報專用） | T3 | ✅ |

## 已知問題

（實跑驗證發現的問題記在這裡，修完劃掉）

## 決策補充紀錄

（執行中臨場決定的事記在這裡，例如 ViewModel 海報分支的處理方式、被過濾掉的 icon 清單）

- T3：50 個 icon 過濾掉 14 個海報專用（6 hero + 1 stamp + 7 tag fallback，僅被 PosterFallbackAssets.kt 引用），實搬 36 個
- T4：`TravelReviseDataResponse.changedSummary` 由 Gson JsonElement 改為 kotlinx JsonElement（JVM-only 型別 iOS 編不過），mapping 三分支語意經審查確認不變
- T4：QuizGallery 型別保留（社群牆走假資料仍需要），share-image 鏈路共跳過 4 DTO + 4 domain 型別 + 2 mapping
- T7：`IsChineseLanguageUseCase` 依 brief 指示改為無參數建構、直接回傳 `true`（不再持有 `CompanionRepository`），取代原本「委派給 `repository.isChineseLanguage()`」的寫法；`CompanionRepository.isChineseLanguage()` 介面方法保留（T5 mock 已回傳 true），但目前無人呼叫，屬預期的孤兒方法
