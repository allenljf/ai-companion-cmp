# AI 旅伴 CMP — 專案規範

## 這是什麼

把原本 KKday Android App 的「AI 旅伴」功能（Phase 1 測驗 + Phase 2 行程規劃）**獨立重做成 Compose Multiplatform demo app**。

**這是 demo，不是產品**——目標是驗證體驗與互動流程，不是重現 KKday 生態。

## 核心約束（很重要，會影響每個決定）

| 項目 | 決定 |
|---|---|
| **定位** | 輕量化 demo，**不追求功能完整或視覺 pixel-perfect** |
| **後端** | 換成自己的 API，**不接 KKday B2C** |
| **B2C 機制** | `x-auth-token`/`token`/`member-uuid`/`b2c-token1` 簽章/`{metadata,data}` envelope **全部不移植** |
| **命名** | 去 KKday/B2C 化（`B2CApiResponse`、`KKButton` 這類命名不要沿用） |
| **設計系統** | `reference/design-system/` 的 KK 元件**只是視覺參照，不要復刻**。用 Material3 + 少量 token 自己做輕量版 |
| **移植重點** | **API 路徑、input/output 形狀、UI 互動邏輯**。網路層實作細節不必復刻 |

## reference/ 導覽（唯讀，不要改）

| 路徑 | 內容 | 什麼時候看 |
|---|---|---|
| `reference/docs/ai-companion-rd-guide.md` | **最重要**：全功能指南，16 支 API、14 個畫面、所有流程與 mermaid 圖 | 先看這份 |
| `reference/docs/ai-companion-overview.md` | 概念與目的的精簡版 | 想快速理解定位 |
| `reference/docs/ios-handoff-*.md` | 各功能的細部串接規格 | 實作某功能時 |
| `reference/android-src/feature/` | **UI + ViewModel 原始碼**（17 檔，10,129 行） | 移植 UI 與邏輯時的主要來源 |
| `reference/android-src/model/` | DTO / Domain / Mappings | 看欄位形狀 |
| `reference/android-src/api-service/` | 16 支 API 的 Retrofit 定義 | 看路徑與型別 |
| `reference/android-src/domain-*/`、`data-repository/` | UseCase 與 Repository | 看業務邏輯 |
| `reference/design-system/USED_TOKENS.md` | 實際用到的 57 個 token | 建 token 層時 |
| `reference/design-system/USED_DRAWABLES.md` | 實際用到的 50 個 icon | 準備資源時 |
| `reference/design-system/ANDROID_ISMS.md` | 31 處 Android-only API 的分佈 | 規劃 expect/actual 時 |
| `migration/API_CONTRACT.md` | **去 B2C 化的 API 契約** | 實作後端或 mock 時 |

> 需要 reference/ 以外的原始檔案時，Android 專案還在 `~/Documents/kkday-android-member-2/`，可用 `--add-dir` 或直接讀絕對路徑。

## 移植原則

1. **ViewModel 邏輯直接搬**——`AiCompanionViewModel.kt`（1,610 行）只有 2 處 Android 依賴，狀態機邏輯是這個功能最有價值的資產，**不要重寫**
2. **Compose UI 搬程式碼，換葉節點**——不要照 HTML/設計稿重做（會丟掉累積的修正）
3. **編譯器優先於文件**——能編過就是對的，不需要為機械移植寫規格
4. **軟失敗模式保留**——`fail_reason` 有值 = LLM 失敗但仍回 200 + 兜底文案，UI 已照這個寫

## 開發慣例

- 回覆一律使用繁體中文
- 註解精簡，只寫「為什麼」不寫「做什麼」
- 硬編字串暫時可接受（demo），但標 `// TODO: i18n`
- 每完成一層就編譯驗證，不要累積到最後

## 工作流

跨對話的進度存在 `migration/` 底下：

| 檔案 | 用途 |
|---|---|
| `migration/00-kickoff.md` | 交接脈絡＋量化分析＋待決策項目 |
| `migration/01-decisions.md` | brainstorming 後的決策紀錄 |
| `migration/02-ledger.md` | 移植進度帳本（逐檔狀態） |

新對話開始時先讀 `00-kickoff.md` 與最新的 ledger。
