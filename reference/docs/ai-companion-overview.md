# AI 旅伴功能總覽 — Phase 1 & Phase 2

> 說明 AI 旅伴（AI Companion）兩個階段的核心概念、目的、API 互動邏輯、用戶操作流程與畫面清單。
> Android 實作位於 `libs/feature/ai_companion/`；後端 API 規格見 b2c-api repo 的 `ai-companion-api.md`（Phase 1）與 `ai-companion-phase2-api.md`（Phase 2）。

---

## 整體定位

AI 旅伴讓使用者「擁有一位專屬的 AI 旅遊夥伴」：

- **Phase 1（測驗與人格，已上線）**：建立有名字、有個性的旅伴角色 → 玩旅行 DNA 測驗 → 得到旅行人格分析與命定城市 → 生成個人化分享海報。核心是**建立關係、取得偏好、帶動傳播**。
- **Phase 2（行程規劃，試驗階段）**：從四種入口和旅伴展開對話 → 收斂目的地 → 回答偏好問卷 → AI 一次生成完整逐日行程 → 自然語言隨時修改 → 行程內直接看到可預訂的 KKday 商品。核心是**把行程決策過程留在站內，規劃即導購**。

兩個階段共用同一個旅伴角色（人格/說話風格/名字貫穿所有 AI 生成內容的語氣）。

### 共通設計前提（影響所有 API 互動）

- **完全無狀態**：後端不使用資料庫、不做 server 端持久化。所有「會員相關」資料（對話歷史、摘要、行程草稿）都由 App 端本地保存，每次 API 呼叫都要帶齊所需的全部輸入。沒有 conversation id / trip id。
- **軟失敗模式**：LLM 失敗一律回 HTTP 200 + `fail_reason` 有值，內容欄位為兜底文案。App 端以 `fail_reason` 判斷成敗，重試 = 原樣重打同一份 request（無副作用）。
- **HTTP 400（`metadata.status="110001"`）**= request 本身組錯（缺必填欄位等），要修呼叫方式而不是重試。
- **不驗證使用者身份**：companion API 不掛登入驗證，靠 throttle 防濫用。

---

## Phase 1：旅伴與測驗

### 核心概念與目的

用「角色化」取代「工具化」——使用者親手捏出旅伴（投入感），旅伴用一致的人設語氣出題、分析、給建議（關係感）。測驗過程等於使用者主動提供旅遊偏好（第一方資料），分享海報帶動社群自然傳播（獲客），命定城市直接接到 Phase 2 的行程規劃（轉換漏斗入口）。

### 畫面清單

| 畫面 | 說明 |
|---|---|
| 建立旅伴（CreateCompanion） | 選人格特質、說話風格、外觀（性別/髮型/穿著）、命名 |
| 旅伴誕生（Intro） | 顯示 AI 生成的自我介紹 |
| 旅伴主頁（Home） | 旅伴形象＋意圖卡清單（所有功能入口）＋我的旅程 |
| 測驗（Quiz） | 一次一題的情境選擇題，選項帶偏好標籤 |
| 測驗結果（Result / ResultDetail) | 旅行人格稱號、命定城市、亮點標籤、推薦文；等待產圖時逐句播放 AI 思考過程 |
| 分享海報 | AI 場景圖＋App 端合成裝飾與文字，可分享社群 |
| 回顧我的旅行 DNA（History） | 過去測驗結果清單，可重新分享 |
| 社群牆（QuizGallery / Detail） | 其他人的測驗結果與海報，逆向找靈感 |

### API 互動邏輯（7 支，皆在 `/api/v3/companion/`）

```
GET  ai-partner          → 人格/風格選項（建立選擇 UI，不打 LLM）
POST self-introduction   → 旅伴自我介紹（LLM，選填）
POST quiz                → 出題（LLM 改寫語氣；shown_question_counts 降低重複出題）
POST quiz-completions    → 提交答案 → 文字人格分析（LLM）；每輪測驗 App 生成一個 completion_uuid
POST share-image-v2      → 海報素材（按需產圖＋輪詢：首打觸發 → 等 30s → 每 10s 輪詢，
                            status=processing/ready；素材個別失敗用 fallback_category 降級）
GET  quiz-gallery        → 社群牆清單（Redis 最新 100 筆）
```

關鍵邏輯：
- `completion_uuid` 由 App 端每輪測驗生成（UUID v4），`quiz-completions` 與 `share-image-v2` 共用——它是分析快取 key 也是產圖冪等 key。
- 海報**版面由 App 端本地合成**（hero 全版背景＋疊放郵戳/標籤插畫/文字），後端只回素材 URL；個別素材失敗依 fallback 用內建素材補位，不可留空白破圖。
- 測驗結果（含命定城市）由 App 本地保存（依 memberUuid 區分），後端不回查。

---

## Phase 2：行程規劃

### 核心概念與目的

把「去哪玩、怎麼排」的決策過程搬進站內：後端只負責三件事——**開場白、一份行程 JSON、用自然語言局部修改這份行程**；儲存（我的旅程）、商品搜尋卡、分享皆為 App 端行為。行程中的每個項目都可以對應 KKday 商品（搜尋卡、已預訂訂單、收藏/瀏覽過的商品），規劃即導購。

### 五種進入方式（入口）

| 入口 | 首頁按鈕 | 開場資料來源 | entry_type |
|---|---|---|---|
| 從測驗結果 | 測驗結果頁「繼續規劃」 | 測驗判定城市＋分析文字 | `quiz_completion`（city 必填） |
| 帶訂單 | 一起規劃旅遊行程(帶訂單) | 即將出發訂單（≤3 筆） | 選城市後 `from_orders`（city 必填） |
| 從心願清單 | 一起規劃旅遊行程(從心願清單) | 收藏商品（≤20 筆） | 選城市後 `from_orders`（city 必填） |
| 從瀏覽記錄 | 一起規劃旅遊行程(從瀏覽記錄) | 最近看過的商品（≤20 筆） | 選城市後 `from_orders`（city 必填） |
| 匯入行程／從零開始 | 匯入你的 AI 行程／一起規劃旅遊行程 | 貼上的外部行程文字／無 | `imported_itinerary`／`from_zero` |

### 畫面清單

| 畫面 | 說明 |
|---|---|
| 材料開場（OrderOpening） | 帶訂單/心願清單/瀏覽記錄共用：greeting 泡泡＋最多 3 個城市 chips＋「我想直接開始規劃」chip；空資料/軟失敗出「改用一般規劃」逃生門 |
| 匯入行程（ImportItinerary） | 貼上外部 AI 行程文字（截圖上傳待後端端點定案） |
| 規劃聊天室（PlanChat） | 開場摘要 → 補充重生成 →（從零）城市收斂多輪對話 → 聊天式偏好問卷（Q1~Q6 一次一題）→ 完成宣告＋「看看完整行程」；材料開場的 greeting＋使用者選擇以前導訊息保留在最上方 |
| 行程成果頁（TripResult） | hero 壓字＋總覽/Day n 分頁；Day 分頁為時間軸（精確時間、交通 icon、導航鈕）；長按拖曳排序；每個景點自動搜尋 KKday 商品卡；「已預訂」（oid）/「感興趣」（prod_id）標籤；右下角旅伴頭像 FAB（可拖曳、跨天共用）開修改對話；「儲存到我的旅程」存本地不關頁 |
| 修改對話 bottom sheet（TripReviseSheet） | 距頂 100dp 全螢幕 sheet；顯示整個聊天室的完整對話（跨天、跨開關保留）；每句需求打一次 travel-revise，成功即 merge 生效 |
| 我的旅程列表（TripList）／首頁「我的旅程」區塊 | 本地儲存的行程卡（後端不儲存），點開回成果頁可繼續編輯/修改 |

### API 互動邏輯（Phase 2 共 8 支）

#### 開場階段

```
（帶訂單）    GET v3/companion/orders            → 訂單清單（目前 mock）→ 萃取 oid/prod_name/package_name/
                                                   destinations[0].name/go_dt，依出發日取前 3 筆
              POST travel-summary-from-orders     → greeting + options[]（order_index+city，逐筆對應）

（心願清單）  GET v3/companion/wish_list          → 商品清單 → 萃取 prod_id/name/introduction(≤500字)/
                                                   destinations[].name，取前 20 筆
              POST travel-summary-from-wish       → greeting + cities[]（city + 對應的 products[prod_id]，聚合）

（瀏覽記錄）  GET v3/companion/history            → 同上
              POST travel-summary-from-history    → 同 wish（兩支後端刻意分開實作，App 端可共用串接）
```

#### 開場摘要（travel-summary，四種 entry_type 同一支 API）

- `quiz_completion`/`from_orders`：**city 必填**，回傳 city 權威等於帶入值（不經 LLM）；`from_orders` 可帶 `order`（訂單材料，開場白呼應訂購商品）。
- `imported_itinerary`：`source_type=text`+`content`，LLM 從內容推斷城市。
- `from_zero`：不打 LLM 回固定文案，city 為空字串。
- 「資訊不夠再補充」：帶 `previous_summary`+`note` 重打，新摘要**取代**前一版（App 只保存最新一份）。

#### 城市收斂（recommend-city，從零開始/直接開始規劃時）

- 無狀態多輪：App 每輪把**完整對話歷史** `messages` 全量帶入，上限 5 輪（第 4 輪預告、第 5 輪強制收斂）；判斷收斂只看 `is_final`，不要自己數輪次。
- 收斂後固定三顆 chips：`就去{城市}！`（接受）／`換一個城市`（被推薦城市累加進 `shown_cities` 重打，上限 5 次）／`重新聊聊`（純 App 端清歷史重來，不打 API）。

#### 行程生成（travel-guide）

- 帶 `summary`＋`city`＋`preferences`（問卷 Q1~Q6 的顯示文字 map，題目前端寫死）一次生成完整行程。
- 選填 `orders[]`（≤3 筆，帶訂單入口）：**必排入行程**，item 回填 `oid`、該天 `booked_anchor` 有值。
- 選填 `products[]`（≤10 筆，心願清單/瀏覽記錄入口選定城市對應的商品）：**必排入行程**，item 回填 `prod_id`（不影響 booked_anchor）。
- 回應為狀態機式 schema：`itinerary_patch.days[].items[]`，item 含 `name`（標題）/`text`（描述）/`type`（spot/logistics/meal）/`time`（HH:MM）/`transport_mode`/`lat`/`lng`（僅 spot，近似座標）。
- `fail_reason` 有值＝生成失敗，`days=[]`，原樣重打即可。

#### 行程修改（travel-revise）

- 每句修改需求打一次；`itinerary` 帶當前最新完整行程（扁平陣列）、`target_day` 帶從哪個 Day 的 FAB 進入（優先只動該天）。
- **`messages` 必填**＝整個聊天室從頭到尾的完整對話紀錄（開場摘要、城市收斂、完成宣告、每輪修改都要 append），最後一則是本次需求；上限 100 則，App 端超過時保留最近的。
- 回應 `itinerary_patch.days` 為完整行程（`mode:"full"`）；App 端以天為單位 upsert merge 保底（實測後端曾只回變動天）。
- `off_topic=true`＝離題導回（不是失敗）；行程內既有 `oid`（不可刪）/`prod_id`（非必要不刪）自動受白名單保護，不需重帶。

#### 商品搜尋卡（App 端行為，非 companion API）

- 行程頁每個景點以「{城市} {景點名}」打 `v2.1/search/product_list`（固定打正式環境，測試環境商品太少），找到就顯示第一項商品卡＋「還有 N 項」（用 `pagination.total_count`），點擊以 App Link deeplink（`https://www.kkday.com/zh-tw/product/productlist?keyword=…`）開搜尋結果頁。

### 用戶操作流程（以心願清單入口為例，最完整的一條）

1. 旅伴首頁點「一起規劃旅遊行程(從心願清單)」
2. 旅伴打字中 → 打招呼：「看了你收藏的行程，要不要先選一個目的地？」＋城市 chips＋「我想直接開始規劃」
3. 點「慕尼黑」→ 進聊天室（greeting 與選擇保留在最上方）→ 旅伴生成開場摘要
4. 點「開始規劃」→ 聊天式問卷：天數/預算/節奏/主題/最在意/特殊需求（一次一題，chips 或打字）
5. 答完 → 旅伴生成完整行程（收藏的慕尼黑商品保證排在裡面）→「看看完整行程」
6. 行程成果頁：總覽/逐日時間軸；收藏商品標「感興趣」；景點下方出現 KKday 商品卡
7. 長按拖曳調整順序；點旅伴 FAB 說「Day 2 下午想加個景點」→ AI 修改即時生效
8. 點「儲存到我的旅程」→ 首頁「我的旅程」隨時回顧、繼續編輯

---

## 兩個 Phase 的銜接

- 測驗結果頁「繼續規劃」＝Phase 1 → Phase 2 的主要轉換點：命定城市與人格分析直接成為行程規劃的起點（`quiz_completion` 入口）。
- 旅伴人設（`companion_name`/`personality`/`speech_style`）作為參數貫穿 Phase 2 所有 LLM API，維持同一個角色的語氣。
- Phase 1 的測驗偏好標籤，未來可作為 Phase 2 `preferences` 的自動預填來源（延伸方向）。
