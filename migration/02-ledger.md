# 移植計畫 + 進度帳本

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.
>
> **跨對話交接**：每次新對話先讀 `CLAUDE.md` → `00-kickoff.md` → `01-decisions.md` → 本檔。做完任何 task 立刻更新本檔的 checkbox 與帳本狀態欄，並 commit。

**Goal:** 把 AI 旅伴（Phase 1 測驗 + Phase 2 行程規劃）移植成可在 Android + iOS 跑的 CMP demo app，mock 資料驅動，不依賴後端部署進度。

**Architecture:** 單一 `shared` 模組承載全部邏輯與 UI（demo 規模不需要多模組）。分層由下往上移植：token → model → repository(mock) → usecase → ViewModel → DS 元件 → 畫面 → 平台特化。每層以「雙 target 編譯通過」為完成標準。

**Tech Stack:** Kotlin 2.4.10 / CMP 1.11.1 / kotlinx.serialization / Koin（手寫 module）/ multiplatform-settings（本地儲存）/ Ktor client（後端就緒後才加）

## Global Constraints（來自 01-decisions.md 與 CLAUDE.md，每個 task 都適用）

- 目標平台只有 **Android + iOS**；expect/actual 原則上只允許一組（文字分享），**T20 起解除**——結果頁海報完整版需要 IG 限動分享（圖片）與開外部瀏覽器兩組額外能力，經使用者本次需求明確同意新增，見下方決策補充
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

- [x] `AiCompanionStates.kt` 照搬（1 處 Android 依賴換掉；題庫若引用 DCS 常數改寫死）
- [x] `AiCompanionViewModel.kt` 照搬，只處理 2 處 Android 依賴與 import 換名——**狀態機邏輯一行都不改**
- [x] 海報/分享圖相關的 state 與 method：**保留兜底或整段移除，以「編譯最小改動」為準則現場判斷**，移除的話在本檔記錄清單
- [x] 編譯驗證 + Commit `feat: 移植 ViewModel 狀態機`

### T9：輕量 DS 元件

**Files:** Create `ui/components/AppButton.kt`、`AppTag.kt`、`AppDialog.kt`、`AppTextField.kt`、`DragHandle.kt`；來源 `reference/design-system/*.kt`（**僅對齊 API surface，不復刻內部**）

**Produces:** 與原 KK 元件**同參數簽章**的 5 個 composable（改名 App*），讓畫面層搬過來時只需改 import 與名稱

- [x] 每個元件用 Material3 對應件包一層 + Tokens 上色
- [x] 編譯驗證 + Commit `feat: 輕量 DS 元件`

### T10：TripList 畫面

**Files:** Create `ui/TripListScreen.kt`；來源 `AiCompanionTripListScreen.kt`（253行，0 Android 依賴）

- [x] 照搬，換 import（DS 元件→App*、R.drawable→Res.drawable）
- [x] 編譯驗證 + Commit `feat: TripList 畫面`

### T11：Plan 畫面（材料開場 + 規劃聊天室）

**Files:** Create `ui/PlanScreens.kt`；來源 `AiCompanionPlanScreens.kt`（903行，0 Android 依賴）

- [x] 照搬，換 import；拖曳 threshold、對話接續等行為邏輯**不動**
- [x] 編譯驗證 + Commit `feat: Plan 畫面`

### T12：Trip 成果畫面

**Files:** Create `ui/TripScreens.kt`；來源 `AiCompanionTripScreens.kt`（1,148行，3 處 Android 依賴＝Intent deeplink）

- [x] deeplink 3 處：demo 拿掉，按鈕改 no-op 或隱藏，標 `// TODO: deeplink`
- [x] 其餘照搬
- [x] 編譯驗證 + Commit `feat: Trip 成果畫面`

### T13：Root 導覽 + Phase 1 畫面（最大檔）

**Files:** Create `ui/CompanionRootScreen.kt`（Root 導覽＋共用 helper）、`ui/QuizScreens.kt`（建立旅伴/主頁/查看社群/測驗流程）、`ui/ResultScreens.kt`（結果頁/歷史回顧）；來源 `AiCompanionScreens.kt`（4,663行，4 處 Android 依賴）

- [x] Root `when(step)` 導覽照搬；BackHandler 未使用 CMP multiplatform BackHandler（原檔沒有 BackHandler 呼叫，Android 實體返回鍵接線留給 T14）
- [x] Android 依賴：全部 Activity/window 狀態列效果（10 處，遠多於原估 4 處）直接移除；`Build.VERSION` 分支（2 處，皆綁在海報揭曉狀態）隨海報鏈路移除一併消失
- [x] 海報產圖/分享畫面區塊整段跳過；測驗結果頁只到人格＋命定城市，加一顆純文字分享鈕（AppButton，onClick no-op 標 TODO: T15）
- [x] 社群牆（QuizGallery）照搬，資料來自 ViewModel 假資料，CompanionAsyncImage 佔位版下一律顯示 placeholder
- [x] `AiCompanionPhase2Previews.kt` 不搬（Android 專屬 tooling，本來就不在本次搬移範圍）
- [x] 編譯驗證（雙 target 皆過）+ Commit `feat: Root 導覽與 Phase 1 畫面`

### T14：DI + App 進入點

**Files:** Create `di/AppModule.kt`、Modify `App.kt`；Delete `Greeting.kt`、`GreetingUtil.kt`、`Platform*.kt`（骨架樣板）；來源參照 `feature/di/AiCompanionAnnotationModule.kt`

- [x] 手寫 Koin module：3 個 mock repository（`single`）+ LocalCompanionStore + 全部 UseCase（`factory`）+ ViewModel
- [x] `App.kt` = AppTheme + KoinApplication + Root 畫面；Android `MainActivity` 與 iOS `MainViewController` 接上
- [x] 骨架樣板檔與其測試一併刪除
- [x] 編譯驗證 + Commit `feat: DI 與進入點串接`

### T15：文字分享 expect/actual（唯一平台特化）

**Files:** Create `platform/ShareText.kt`（expect fun shareText(text: String)）、`platform/ShareText.android.kt`（`Intent.ACTION_SEND`，context 由 Koin android context 取）、`platform/ShareText.ios.kt`（`UIActivityViewController`）

- [x] 三檔實作 + 結果頁分享鈕接上
- [x] 編譯驗證 + Commit `feat: 文字分享 expect/actual`

### T16：雙平台實跑驗證

- [x] Android：emulator 跑完整流程（建立旅伴→測驗→結果→Phase 2 開場→行程→自然語言修改）
- [x] iOS：Simulator 建置＋安裝＋啟動＋首屏渲染驗證通過；互動級測試已補（2026-08-15）：建立旅伴 → 測驗 → 解析動畫 → 結果頁（修復後內容）→ 系統分享面板 → BottomSheet → Phase 2 開場，本地持久化跨重裝存活
- [x] 發現的移植 bug 記入本檔「已知問題」節，修完再結
- [x] Commit + push，更新本檔全部狀態

### T17：真後端接入（等 API 部署好才做）

**Files:** Create `data/remote/CompanionApiClient.kt`（Ktor）、`data/remote/RemoteCompanionRepository.kt`、`data/remote/RemoteCompanionOrderRepository.kt`；Modify `libs.versions.toml`（ktor 3.5.2：client-core/content-negotiation/serialization + okhttp/darwin engine）、`shared/build.gradle.kts`、`di/AppModule.kt`、`model/ApiModels.kt`（ai-partner 信封巢狀化、新增 orders DTO）、`model/ModelMappings.kt`

- [x] Ktor client 依實際部署的 15 支端點實作（`share-image-v2` 不接、商品搜尋無端點續用 mock）
- [x] DI 綁定 mock→remote 用一個 flag 切換（`useRemoteApi`，保留 mock 供離線 demo）
- [x] 雙平台實跑 + Commit：Android emulator 真 API 全流程通過（ai-partner 11 個性選項、真題庫 1/8、quiz-completions「獨處療癒師×東京」、travel-summary 銜接測驗結果、travel-guide 4 天東京行程、travel-revise 語意移除 Nonbei Yokocho）；iOS Simulator（Darwin engine）建置＋啟動＋真題庫載入通過（2026-08-15）

### T18：Coil 3 圖片載入

**Files:** Modify `libs.versions.toml`（新增 coil=3.5.0 + coil-compose/coil-network-ktor3）、`shared/build.gradle.kts`（commonMain 依賴）、`App.kt`（`setSingletonImageLoaderFactory`）、`ui/CompanionRootScreen.kt`（`CompanionAsyncImage` 真實作）、`ui/PlanScreens.kt`（`PlanTopBar`/`PlanChatBubble` 恢復呼叫 `CompanionAsyncImage`，刪除 `PlanAvatarPlaceholder`）；Create `CompanionImageLoader.kt`

- [x] 依賴：coil-compose + coil-network-ktor3（commonMain，沿用專案既有 Ktor engine，不另加 engine）
- [x] 全域 ImageLoader：`companionImageLoader()` + `setSingletonImageLoaderFactory`，crossfade(true)，memory/disk cache 用 Coil 3 平台預設
- [x] `CompanionAsyncImage` 真實作：保留原簽章，內部改用 `rememberAsyncImagePainter` + `painter.state`（Coil 3 是 `StateFlow`，用 `collectAsState()`）判斷 Loading/Error/Success；retry 用 `memoryCacheKeyExtra` 換身分強制重載（Coil 3 拿掉 Coil 2 的 `setParameter` API）；`blurInOnLoad` 對齊原版 Animatable 模糊淡入語意
- [x] `PlanScreens.kt` 頭像恢復：對照 reference `AiCompanionPlanScreens.kt`，`PlanTopBar`/`PlanChatBubble` 改回呼叫 `CompanionAsyncImage(url = avatarUrl, ...)`，`PlanAvatarPlaceholder` 無人呼叫後刪除
- [x] 全域 grep 其餘畫面：ResultScreens/QuizScreens/TripScreens 皆已呼叫 `CompanionAsyncImage`（無其他被換成純佔位的呼叫點）
- [x] 雙平台編譯驗證 + Android emulator 冷啟截圖確認真頭像圖載出（建立旅伴外觀預覽步驟）+ Commit `feat: 接 Coil 3 圖片載入`

---

## 逐檔帳本（來源 → 目標）

狀態：⬜ 未動工 / 🔄 進行中 / ✅ 完成 / ⛔ 不移植

| 來源（reference/android-src/） | 行數 | 目標 | Task | 狀態 |
|---|---|---|---|---|
| model/ApiModels.kt | 502 | model/ApiModels.kt | T4 | ✅ |
| model/DomainModels.kt | 465 | model/DomainModels.kt | T4 | ✅ |
| model/ModelMappings.kt | 255 | model/ModelMappings.kt | T4 | ✅ |
| model/TripProductSearchModels.kt | 21 | model/TripProductSearchModels.kt | T4 | ✅ |
| api-service/ICompanionApiService.kt | 93 | data/remote/CompanionApiClient.kt（Ktor，去 Retrofit/B2C 化） | T17 | ✅ |
| api-service/ITripProductSearchApiService.kt | 19 | — 同上 | T5/T17 | ⛔ |
| domain-contract/CompanionRepository.kt | 167 | data/CompanionRepository.kt | T5 | ✅ |
| domain-contract/CompanionOrderRepository.kt | 18 | data/CompanionOrderRepository.kt | T5 | ✅ |
| domain-contract/TripProductSearchRepository.kt | 20 | data/TripProductSearchRepository.kt | T5 | ✅ |
| data-repository/CompanionRepositoryImpl.kt | 555 | data/mock/MockCompanionRepository.kt + data/mock/MockData.kt（重寫為 mock） | T5 | ✅ |
| data-repository/CompanionOrderRepositoryImpl.kt | 83 | data/mock/MockCompanionOrderRepository.kt | T5 | ✅ |
| data-repository/TripProductSearchRepositoryImpl.kt | 28 | data/mock/MockTripProductSearchRepository.kt | T5 | ✅ |
| （新增，無對應原始檔） | — | data/remote/RemoteCompanionRepository.kt + data/remote/RemoteCompanionOrderRepository.kt | T17 | ✅ |
| data-repository/CompanionApiException.kt | 79 | data/CompanionApiException.kt（瘦身，拿掉 B2C envelope 解析） | T5 | ✅ |
| domain-usecase/（26 檔，扣 ShareImageV2） | ~600 | domain/*.kt | T7 | ✅ |
| domain-usecase/FetchShareImageV2UseCase.kt | 21 | — | — | ⛔ |
| feature/viewModel/AiCompanionStates.kt | 299 | viewmodel/AiCompanionStates.kt | T8 | ✅ |
| feature/viewModel/AiCompanionViewModel.kt | 1,610 | viewmodel/AiCompanionViewModel.kt | T8 | ✅ |
| feature/presentation/compose/AiCompanionTripListScreen.kt | 253 | ui/TripListScreen.kt | T10 | ✅ |
| feature/presentation/compose/AiCompanionPlanScreens.kt | 903 | ui/PlanScreens.kt | T11 | ✅ |
| feature/presentation/compose/AiCompanionTripScreens.kt | 1,148 | ui/TripScreens.kt | T12 | ✅ |
| feature/presentation/compose/AiCompanionScreens.kt | 4,663 | ui/CompanionRootScreen.kt(452) + ui/QuizScreens.kt(2,297) + ui/ResultScreens.kt(1,118) | T13 | ✅ |
| feature/presentation/compose/AiCompanionPhase2Previews.kt | 311 | — | — | ⛔ |
| feature/presentation/compose/CompanionShareActions.kt | 175 | platform/ShareText.kt（重寫為純文字分享） | T15 | ✅ |
| feature/presentation/poster/（7 檔） | ~880 | — 海報全鏈路不做 | — | ⛔ |
| feature/presentation/AiCompanionActivity.kt | 82 | —（CMP 進入點取代） | T14 | ⛔ |
| feature/di/AiCompanionAnnotationModule.kt | 76 | di/AppModule.kt（手寫重寫） | T14 | ✅ |
| design-system/USED_TOKENS.md（57 token） | — | theme/Tokens.kt | T2 | ✅ |
| design-system/USED_DRAWABLES.md（50 icon） | — | composeResources/drawable/（36 個，過濾 14 個海報專用） | T3 | ✅ |

## 已知問題

（實跑驗證發現的問題記在這裡，修完劃掉）

- ~~[Minor/T10] TripListScreen.formatSavedAtDate 以 UTC 日界切分日期（原版用裝置時區），Taipei 使用者每日 00:00–08:00 存的行程日期會少一天。~~ 已修：final review 時抽成共用 helper `CompanionRootScreen.formatEpochMillisAsDate`，epoch 先加 8 小時（台北時區近似值），標 `// TODO: timezone` 待日後接精確時區換算
- 全專案無自動化測試，品質依賴手動雙平台實跑（`:androidApp:assembleDebug` + `:shared:compileKotlinIosSimulatorArm64`）
- ~~[Minor/iOS] 結果頁頂部標題會被 Dynamic Island 遮住~~ 已修：ResultScreen 根 Box 補 `statusBarsPadding()`
- [Minor/後端] LLM 回應偶爾簡繁混雜（travel-summary 開場訊息出現簡體）——後端 prompt 的 locale 約束問題，app 端無關
- [Minor/預期] 商品推薦卡固定顯示 mock 目錄（大阪環球影城等）與真實行程城市可能不匹配——商品搜尋無後端端點，待後端補端點後接真資料
- [備註] 後端 LLM 偶發 429（Groq rate limit）→ 走軟失敗兜底文案，屬預期行為
- ~~[Image loading] `CompanionAsyncImage` 為佔位版，一律顯示 placeholder，不載入真圖~~ 已修（T18）：接 Coil 3，真頭像/題目圖可正常載入，Android emulator 截圖確認

## 決策補充紀錄

- 行程成果頁儲存／刪除操作（2026-08-16）：儲存成功後底部按鈕的 `ic_heart_line` 改為新增的 `ic_heart_fill`，並顯示 2.5 秒「已儲存到我的旅程」commonMain toast；提示與狀態都只在 `saveSavedTrips` 成功後更新。hero 右上關閉鈕下新增同樣半透明樣式的 `ic_delete_line` 刪除鈕，刪除本地記錄成功後依 `tripReturnStep` 返回來源頁面。
- T21 打字機揭曉頁（2026-08-16）：恢復原版 `PosterGeneratingContent`/`TypewriterText`/`SequentialTypewriterItems`/
  `ReasoningBubble`/`BreathingLoadingText`/`PosterReadyBanner` 六個 composable（逐行照搬換葉節點），加回
  ViewModel 的 `posterRevealed`/`revealPosterResult()`。分析成功後先進打字機等待頁（不自動跳結果頁），
  `Ready`/`Failed` 都讓「一起去看看」按鈕出現（軟失敗不卡死使用者）。
- T21 tag icon 定案：後端實際只回 hero 與 stamp，**不回** tag_icon_urls——T20 一度做的三顆圓圖列拿掉，
  highlight_tags 維持純文字 pill（不是 fallback，是後端本來就沒有這個資料）。`tagFallbackCategories`
  留在 DTO/domain model 不刪（鏡射真實 API 形狀，YAGNI 成本低，之後後端補上就能直接接）。
- T21 補回 3 個 fallback 欄位：`heroFallbackCategory`/`stampFallbackCategory`/`tagFallbackCategories`；
  只有 `stampFallbackCategory` 有 UI 用途（`PosterFallbackAssets.stampDrawable()`，`stamp_url` 缺圖時退本地 icon）。
  Fallback icon 8 顆（1 stamp + 7 tag，tag 系列雖不用但保留供未來 tag icon 恢復時使用）從原 Android 專案
  複製進 composeResources；hero fallback 6 顆先不搬（hero 缺圖走無海報 fallback 版面，不需要類別 icon）。
- T21 非範圍 UI 調整確認（2026-08-16）：使用者確認保留兩項既有變更：
  1. `ui/TripScreens.kt`：拖曳 FAB 尺寸維持 72dp（與測驗頁旅伴頭像同尺寸）
  2. `ui/CompanionRootScreen.kt`：從 `PlanChat` 開啟的行程成果頁，關閉鈕返回 `Home`，不回聊天室

- T19 海報接回（2026-08-16，**部分推翻決策 1／6**）：後端 `share-image-v2` 已能產圖並回 `hero_url`，因此接回「顯示海報」。與原版關鍵差異：**後端同步一次回完（實測首次約 80 秒、同 uuid 重打約 35 秒，未快取），不是原版的 pending＋輪詢**，因此 app 端不做輪詢、不做 Bitmap 合成、不下載存檔（`poster/` 7 檔仍不移植）。ViewModel 只加簡化版 `ShareImageV2State`（Idle/Loading/Ready/Failed）與一個背景 coroutine：分析成功當下即觸發，結果頁文字先顯示、圖 ready 再補上；失敗只影響海報區塊（軟失敗契約）。Ktor 這支需**同時**放寬 `requestTimeoutMillis` 與 `socketTimeoutMillis`——產圖那 80 秒 socket 上無資料往來，只放寬前者會被預設 socket 逾時打斷。產圖成功回填 `QuizHistoryRecord.heroImageUrl`，回顧頁不必重打。`decorations`（stamp/tag icon 疊圖素材）未使用

- 行程情境圖 `hero_image_url`（2026-08-16）：`travel-guide` 回應新增這支欄位（後端一併產好的遠端圖 URL，非另一支 API），沿 `TravelGuideDataResponse` → `TravelGuideResult` → `SavedTripRecord` 一路帶到本地持久化，回訪不必重打。三處占位換成 `CompanionAsyncImage`：成果頁 `TripHero`（226dp，圖在漸層 scrim 之下）、首頁「我的旅程」小卡（92dp）、`TripListScreen` 列表縮圖（72dp，無圖時 placeholder 仍是城市字）。`travel-revise` 不回這支欄位，靠 `trip.copy(...)` 保留原值；產圖失敗為 `null` → 空字串 → `CompanionAsyncImage` 自然退回漸層占位（軟失敗契約）

- UI 調整（2026-08-16，獨立 app 定位）：首頁與建立旅伴第一步的返回鍵移除（獨立 app 沒有上一頁，原本點了沒反應），改留等寬 Spacer 讓標題不位移；建立旅伴第二/三步的返回鍵**保留**（那是精靈步驟回退，實際有作用）。首頁移除「帶訂單／從心願清單／從瀏覽記錄」三個開場入口（獨立 app 不會有這些紀錄），對應的 `onPlanTripWith*` 參數與呼叫點一併移除；ViewModel 的 `startPlanFromOrders/Wish/History` 與 `OrderOpening` step 保留不動（不重寫狀態機），成為暫時無入口的死路徑

（執行中臨場決定的事記在這裡，例如 ViewModel 海報分支的處理方式、被過濾掉的 icon 清單）

- Final review 修正（結果頁內容、mock 文案、時區 helper、清理）：`ResultScreen` 成功分支補回 `CompanionResultHighlightContent`/`CompanionResultDetailsContent` 呼叫（原本結果頁只有 hero 文字，其餘內容區塊漏接，畫面等於空白）；`MockData.selfIntroduction` 呼叫端改用 tag→中文 label 查表（原本直接把 personality/speechStyle 的 tag id 塞進中文自介句子，混入英文字），ViewModel 不動；`TripListScreen`/`ResultScreens` 重複的曆法換算抽成 `CompanionRootScreen.formatEpochMillisAsDate` 共用；`TripScreens.TripAsyncImagePlaceholder` 收斂成共用 `CompanionAsyncImage`（`PlanScreens.PlanAvatarPlaceholder` 維持不動）；刪殘留的海報鏈路 icon（`compose-multiplatform.xml`/`ic_eye_line.xml`/`ic_map_line.xml`，確認無引用）與 3 份骨架樣板測試檔；`ResultDetailScreen` 補註解說明目前執行期不可達
- T15：`platform/ShareText.kt` 用 `@Composable expect fun rememberShareText(): (String) -> Unit`（非裸 `expect fun shareText(text: String)`）——兩端都要「目前畫面在哪」才能發分享：Android 用 `LocalContext.current`、iOS 用目前最上層 `rootViewController`，兩者都是 Compose 才知道的資訊，裸 expect fun 得另外用 Koin 塞 context/ViewController 單例反而更繞。Android actual：`Intent.ACTION_SEND` + `Intent.createChooser`，context 非 `Activity` 時補 `FLAG_ACTIVITY_NEW_TASK` 保底。iOS actual：`UIActivityViewController` 從 keyWindow 往下找最上層已 present 的 VC present；popover（iPad）沒設 `sourceView`/`sourceRect` 會直接 crash，接上 `presenter.view`/`presenter.view.bounds` 當 fallback 錨點（demo 目標 iPhone，不精修箭頭位置）。編譯debug 花絮：`UIViewController.popoverPresentationController` 在這個 Kotlin/Native cinterop 版本是**擴充屬性**而非成員屬性，光 import `UIActivityViewController`/`UIViewController` 編不過（`Unresolved reference`），要多 `import platform.UIKit.popoverPresentationController` 才解得到。結果頁分享鈕文案：「我的旅行人格是＜travelIdentity＞，命定城市是＜destinationCn＞！」（`// TODO: i18n`）
- T14：`AiCompanionViewModel` 建構子 26 參數超過 koin-core-viewmodel `viewModelOf` 的 reified 上限（22），改用具名參數 `viewModel { AiCompanionViewModel(getAiPartnerUseCase = get(), ...) }`，避免同型別（多個 UseCase 共用 `CompanionRepository`）位置性 `get()` 對錯位
- T14：App 進入點起始頁邏輯——`loadLocalCompanion()` 完成前 `hasLocalCompanion` 為 null，顯示簡易 loading（CircularProgressIndicator），避免尚未判定就先閃一次錯誤起始頁；`AiCompanionRoot.onFinish` 因 demo 只有這一個 feature、沒有外層畫面可退，訂為 no-op（原始碼此處會 finish Activity）
- T14：原始碼 `AiCompanionScreens.kt` 全檔無 `BackHandler`，Android 實體返回鍵沿用系統預設行為，本任務未額外接線（T13 已確認過此點，見上）
- T10：SimpleDateFormat→手寫曆法換算（Hinnant civil_from_days，reviewer 交叉驗算通過）；UTC 日界差異記入已知問題
- T17：真後端接入，15 支端點逐支真連線驗證（詳見 task-T17-report.md）。DTO 修正 2 處：(1) `AiPartnerDataResponse` 實際回應把 personality/speech_style/gender/outfit/hair_style/hair_color/avatars 包在 `data.variant` 底下（原 DTO 是攤平），新增 `AiPartnerVariantResponse` 巢狀層，domain model/ViewModel 不動；(2) `GET orders` 原本沒有對應 DTO（mock 版材料層直接回固定清單），新增 `OrdersDataResponse`/`OrderResponse`/`OrderDestinationResponse`，`destinationName` 取 `destinations.firstOrNull()`（比照 reference `CompanionOrderRepositoryImpl` 邏輯，`go_dt` 後端已格式化成 yyyy-MM-dd 不必再轉 epoch）。Ktor Json 設定 2 個關鍵 flag：`encodeDefaults = true`（後端把 `shown_question_counts`/`shown_cities` 等欄位標必填，即使空 map/list 也要序列化，否則 kotlinx.serialization 預設省略等於預設值的欄位會被 400 擋掉）、`coerceInputValues = true`（travel-guide 軟失敗時 `days: Int` 欄位會回 `null` 而非省略，非 nullable 欄位遇 null 要退回預設值而非直接炸 decode）；另外 `quiz-gallery` 曾在錯誤情況下遇過 `text/plain` content-type 但 body 仍是 JSON，ContentNegotiation 註冊 `contentType = ContentType.Any` 放寬比對。信封拆殼：`ApiEnvelope<T>{metadata,data}` + `unwrap()`，`metadata.status != "0000"` 丟 `CompanionApiException`。無商品搜尋端點，`TripProductSearchRepository` 續綁 mock。
- ~~T11/T12/T13：CompanionAsyncImage（Coil）以佔位版實作——T13 定義原名共用版，T11/T12 各有私有佔位，最終 review 收斂；`// TODO: image loading`~~ 已於 T18 接 Coil 3 補實作
- T18：Coil 3（3.5.0）取代佔位版。`companionImageLoader()`（`CompanionImageLoader.kt`）用 `KtorNetworkFetcherFactory()`，不帶自訂 `HttpClient`/engine——沿用 androidMain=okhttp/iosMain=darwin 既有 engine，跟 `CompanionApiClient` 的 `HttpClient()` 同一套自動解析機制；`App.kt` 用 `setSingletonImageLoaderFactory` 掛全域單例。`CompanionAsyncImage` 對照 reference 原版行為：無 URL/Error/Loading 都退回 `placeholder()`（`loadingContent`/`errorContent` 可覆寫），Success 顯示圖並依 `placeholderAspectRatio` 用圖片實際比例撐高度。已知行為差異：(1) 原版 loading 預設是 `companionShimmer()` 效果，本專案未移植 shimmer 元件，loading 態直接退回 `placeholder()`（非 shimmer）；(2) retry 語意用 `memoryCacheKeyExtra("companion_retry", n)` 換 `ImageRequest` 身分強制重新發起請求，因為 Coil 3 拿掉了 Coil 2 的 `ImageRequest.Builder.setParameter` API；(3) `blurInOnLoad` 模糊淡入邏輯（Animatable + `Modifier.blur`）照搬原版，`blurAnimatedUrls` 全域 Set 記錄同張圖不重播動畫。`painter.state` 在 Coil 3 是 `StateFlow`（Coil 2 是可直接讀的欄位），改用 `collectAsState()`。`PlanScreens.kt` 的 `PlanTopBar`/`PlanChatBubble` 恢復呼叫 `CompanionAsyncImage(url = avatarUrl, ...)`（對照 reference `AiCompanionPlanScreens.kt` 逐字搬），`PlanAvatarPlaceholder` 刪除。驗證：Android emulator 冷啟→建立旅伴→個性擇一→外觀「隨機配一個」→外觀預覽區截圖確認真頭像圖（GCS）成功載出，取代原本「旅伴頭像預覽」文字佔位。
- T12：deeplink 3 處 onClick 改 no-op 保留外觀；LocalConfiguration.screenHeightDp→LocalWindowInfo.containerSize 換算（審查確認語意等價）
- T13：拆 3 檔（CompanionRootScreen/QuizScreens/ResultScreens，沿原檔章節斷面）；跳過 6 個海報鏈路 @Composable；結果頁底部欄取原檔 no-poster fallback 分支，BottomSheet 剪 4 個海報項留 6 個 nav 項；分享鈕 no-op 待 T15；原檔無 BackHandler，返回鍵接線歸 T14
- T8：海報鏈路移除清單見 task-T8 報告（8 個 method + ShareImageV2State + 建構子 3 參數 + onCleared 空殼）；java.util.UUID→kotlin.uuid.Uuid、System.currentTimeMillis→kotlin.time.Clock（審查確認語意等價）
- T9：DS 元件參數改名對照（T10–13 搬畫面時要一起改）：`kkTagColor`→`tagColor`（去 KK 化優先於同簽章）、`KKButton.leadingIcon: @DrawableRes Int`→`Painter?`（呼叫點改 painterResource）、`KKTextField.placeholderTextStringType: StringType`→`placeholder: String`；enum 只保留畫面用到的 variant
- T5：介面移除 `fetchShareImageV2`（海報鏈路）與 `isMockEnabled`（reference 中無呼叫端）；CompanionApiException 79→19 行（去 B2C envelope 解析）；補中性型別 TripProductCard/TripProductSearchResult；軟失敗開關 = CompanionMockConfig.forceFailReason
- T6：LocalCompanionStore 用 multiplatform-settings + JSON；壞資料防護 decodeOrNull（壞 JSON → 移除 + 回 null/空）
- T7：IsChineseLanguageUseCase 寫死 true，`CompanionRepository.isChineseLanguage()` 成孤兒方法（T17 接真後端時再清）
- T3：50 個 icon 過濾掉 14 個海報專用（6 hero + 1 stamp + 7 tag fallback，僅被 PosterFallbackAssets.kt 引用），實搬 36 個
- T4：`TravelReviseDataResponse.changedSummary` 由 Gson JsonElement 改為 kotlinx JsonElement（JVM-only 型別 iOS 編不過），mapping 三分支語意經審查確認不變
- T4：QuizGallery 型別保留（社群牆走假資料仍需要），share-image 鏈路共跳過 4 DTO + 4 domain 型別 + 2 mapping
- T7：`IsChineseLanguageUseCase` 依 brief 指示改為無參數建構、直接回傳 `true`（不再持有 `CompanionRepository`），取代原本「委派給 `repository.isChineseLanguage()`」的寫法；`CompanionRepository.isChineseLanguage()` 介面方法保留（T5 mock 已回傳 true），但目前無人呼叫，屬預期的孤兒方法
- T8：`androidx.lifecycle.ViewModel`/`viewModelScope` import 不用改——JetBrains 版 lifecycle-viewmodel 的 Maven 座標雖是 `org.jetbrains.androidx.lifecycle`，但 Kotlin package 仍是 `androidx.lifecycle`（原始碼直接沿用，零改動）
- T8：`java.util.UUID.randomUUID()` / `System.currentTimeMillis()` 為 JVM-only，commonMain 編不過 iOS：改用 `kotlin.uuid.Uuid.random()`（`newCompletionUuid`）與 `kotlin.time.Clock.System.now().toEpochMilliseconds()`（新增私有頂層 `currentTimeMillis()`），呼叫端邏輯不變，僅 API 替換（屬 (d) 編譯最小修正）
- T8：`org.koin.android.annotation.KoinViewModel` 移除（annotation import + `@KoinViewModel`）——koin-annotations/koin-android 未接入此專案（DI 改手寫 Koin module，見 T14），annotation 留著會直接編不過
- T8：`android.content.Context` import 為原始碼未使用的殘留 import，直接刪除，無任何邏輯或呼叫點受影響
- T8：海報/分享圖鏈路移除清單（全部因「海報全鏈路不做」的 CLAUDE.md 核心約束）——
  - state：`AiCompanionStates.kt` 的整個 `ShareImageV2State` sealed interface（Idle/Polling/Composing/Ready/SessionExpired/Error）
  - ViewModel state 欄位：`_shareImageV2State`/`shareImageV2State`、`_posterRevealed`/`posterRevealed`、`shareImageV2Job`、`shareImageV2PollFailureCount`、`activeHistoryCreatedAt`（唯一用途是海報回填 key）
  - ViewModel method：`startShareImageV2Generation`、`callShareImageV2Once`、`onPosterComposed`、`cancelShareImageV2Polling`、`retryFetchHistoryPoster`、`backfillHistoryPosterLocalPath`、`backfillHistoryAssetPaths`
  - 建構子參數：`fetchShareImageV2UseCase`（reference 依賴不存在，T7 已跳過）、`shareImageV2AssetResolver`、`posterHistoryStorage`
  - 呼叫點最小切除：`submitQuiz(selectedTags, shownCities)` 拿掉 `_posterRevealed.value = false` 與 `if (result.canGenerateShareImage) startShareImageV2Generation()`；`startNewQuizRound()` 拿掉 `cancelShareImageV2Polling()`／`_shareImageV2State` 重置／`_posterRevealed` 重置；`deleteQuizHistoryRecord` 拿掉 `posterHistoryStorage.deleteFiles(...)` 整個 if 區塊；`appendQuizHistory` 拿掉 `activeHistoryCreatedAt = record.createdAt`；`override fun onCleared()` 因唯一內容是 `cancelShareImageV2Polling()`，波及後整個 override 一併移除（無其他邏輯）
  - `AiCompanionStates.kt` 的 `TripProductState.Found.products` 型別由 `com.kkday.library.networking.resource.product.B2CProductCardData` 改為 T5 已定義的中性型別 `com.allenljf.aicompanion.model.TripProductCard`（沿用既有去 B2C 化決策，非本次新引入）
  - 測驗結果頁需要的人格＋命定城市 state（`AnalysisState.Success(result: QuizCompletionResult)`）完整保留，未受影響
- T13：拆檔以原檔章節註解為自然斷面——`ui/CompanionRootScreen.kt`＝`AiCompanionRoot`＋共用 helper（`ScreenScaffold`/`PrimaryButton`/`CompanionAsyncImage`/`ScreenTitleStyle`）；`ui/QuizScreens.kt`＝原檔「各步驟畫面」整段（建立旅伴 3 步驟＋旅伴誕生/主頁＋查看社群＋測驗作答）；`ui/ResultScreens.kt`＝原檔「結果頁（C-1 統一畫面）」整段（結果頁/結果詳情/旅行 DNA 回顧）
- T13：`CompanionAsyncImage` 佔位版收斂於 `CompanionRootScreen.kt`（保留原簽章 url/contentScale/blurInOnLoad/placeholderAspectRatio/placeholder/loadingContent/errorContent，內部一律顯示 placeholder）；T11/T12 的私有佔位（`PlanAsyncImagePlaceholder`/`TripAsyncImagePlaceholder`）本次未動，留待最終 review 決定是否收斂成單一實作
- T13：BackHandler——原始碼 `AiCompanionScreens.kt` 全檔沒有任何 `BackHandler` 呼叫（Android 實體返回鍵原本就交給系統預設行為，未特別攔截），故本次不需要引入 CMP 的 multiplatform BackHandler，也沒有自建 expect/actual；Root 的 step 回退邏輯已透過各分支的 `onBack` callback 完整保留
- T13：Android 依賴實際盤點——`(view.context as Activity).window` 狀態列/edge-to-edge 效果共 10 處（`ScreenScaffold`、`CreateCompanionScreen`、`CompanionBornScreen`、`CompanionHomeScreen`、`QuizGalleryScreen`、`QuizGalleryDetailScreen`、`QuizScreen`、原 `ResultScreen`、原 `CompanionHistoryDetailContent` 各 1 處），全部直接移除（純視覺效果，不影響邏輯）；`Build.VERSION.SDK_INT >= Q` 判斷共 2 處，皆綁在海報揭曉時的狀態列圖示色（`isStatusBarContrastEnforced`），隨海報鏈路整段移除一併消失，未特別处理「取新版行為」
- T13：`ResultScreen` 重新設計——原始碼以 `ShareImageV2State`（Idle/Polling/Composing/Ready/…）驅動三段式流程（分析中→海報產圖等待動畫`PosterGeneratingContent`→海報 Hero 結果）；T8 已整個移除 `ShareImageV2State`，故本檔只保留原本「無海報」fallback 分支（`travelIdentity`＋`destinationCn/destinationCountry`＋`companionQuote`）作為唯一成功態內容，即「人格＋命定城市」。跳過的 @Composable：`PosterGeneratingContent`、`PosterReadyBanner`、`ReasoningBubble`、`BreathingLoadingText`、`TypewriterText`、`SequentialTypewriterItems`（連同 `TAG_STAGGER_DELAY_MS`/`SEQUENTIAL_ITEM_GAP_MS`/`TYPEWRITER_CHAR_DELAY_MS` 常數）
- T13：`ResultActionsBottomSheet`/`ActionRow` 保留但瘦身——移除 `isImageReady`/`onShareDna`/`onShareToInstagramStories`/`onDownloadImage`/`showExplore`/`onExploreDestination`（皆依附海報 bitmap 或未搬的 `SearchResultRouter`），保留純導覽列（繼續規劃／看其他人／查看完整結果／回到旅伴／回到列表／刪除紀錄）；`ResultScreen` 底部改成雙按鈕列（`AppButton` 純文字「分享我的旅行 DNA」onClick no-op TODO T15 + 「更多動作」開瘦身後的選單），是本次唯一額外新增的 UI 元素（非原檔葉節點置換），理由：忠實移植「無海報」fallback 分支即已符合「人格＋命定城市」，但原分享動作全部依附海報 bitmap 無法直接搬，改用一顆獨立純文字分享鈕滿足 brief 第 2 點要求，同時保留選單另外三個非海報導覽項目的可達性
- T13：`ResultDetailScreen`/`CompanionHistoryDetailContent` 同樣移除海報 bitmap 顯示分支，改用原本就存在的「素材缺漏」fallback（命定城市文字佔位 Box）作為唯一內容；`CompanionHistoryDetailContent` 額外移除了 `retryFetchHistoryPoster`（T8 已從 ViewModel 移除）、`PosterHistoryStorage`/`SearchResultRouter` 的 koinInject、`loadResolvedPosterAssets` 擴充函式、`shareableGraphicsLayer`／IG 限動／下載圖片分享動作
- T13：`copySocialPostToClipboard`（`Context` 擴充函式 + `Toast`）非 KMP commonMain API，改用 `androidx.compose.ui.platform.LocalClipboardManager`（commonMain 既有 API，非新增 expect/actual）在呼叫端組出 `{ clipboardManager.setText(AnnotatedString(text)) }`；複製成功的 Toast 提示先省略，標 TODO
- T13：`formatHistoryDate`（原用 `java.text.SimpleDateFormat`/`java.util.Locale`/`java.util.Date`，JVM-only 編不過 iOS）比照 T10 `TripListScreen.formatSavedAtDate` 改用純 Kotlin civil-days 曆法換算
- T13：`fontH6`（`com.kkday.design.font`，DS 標題字級）未移植，用 `Tokens.fontSize4` + `FontWeight(Tokens.fontWeightBold)` + `Tokens.colorTextDarker` 組一個語意相近的 `ScreenTitleStyle`（定義於 `CompanionRootScreen.kt`）
- T13：`LocalConfiguration.current.screenHeightDp`（`CreateCompanionScreen` 的 loading/empty/error 置中用）比照 T12 `TripScreens.kt` 已用過的模式，改用 `LocalWindowInfo.current.containerSize` + `LocalDensity` 換算
- T13：`DimensionCard`/`HomeIntentCard`/`ActionRow` 的 `@DrawableRes Int` icon 參數改為 `Painter?`（比照 T9 `AppButton.leadingIcon` 的既有慣例），呼叫端改用 `painterResource(Res.drawable.xxx)`
- T13：`QuizGalleryItem`（社群牆項目）與其 `shareImageUrl`/`hasPoster` 欄位照搬不動——這是「其他使用者」的假資料展示內容，資料來自 `GetQuizGalleryUseCase` mock，不屬於本次要跳過的「自己的海報生成鏈路」；`CompanionAsyncImage` 佔位版下一律顯示 placeholder，不影響邏輯
