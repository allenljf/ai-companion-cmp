# AI 旅伴 API 契約（去 KKday/B2C 化版本）

> **這份文件的用途**：新專案要換成自己的後端，所以這裡把原本 KKday B2C 的包裝、簽章、驗證機制**全部剝掉**，只留下真正重要的東西：**路徑、輸入、輸出、以及 LLM 互動語意**。
>
> 拿這份就能直接實作自己的後端或 mock server，不需要理解 KKday 的 B2C envelope。

---

## 移除了什麼（原 Android 實作有、新專案不需要）

| 原機制 | 說明 | 新專案 |
|---|---|---|
| `{metadata: {status, desc}, data: {...}}` envelope | KKday 全站統一信封 | ❌ 直接回 payload 或自訂 |
| `metadata.status = "0000"` 判斷成功 | 業務狀態碼 | ❌ 用 HTTP status |
| `x-auth-token` / `token` / `member-uuid` | 環境 key、登入 session、會員識別 | ❌ 自己的簡易 auth 或無 auth |
| `timestamp` + `b2c-token1` HMAC 簽章 | 防篡改簽章（`APITokenFactory`） | ❌ 不需要 |
| `B2CInterceptor` / `BaseApiRepository` | 統一 header 注入與錯誤映射 | ❌ Ktor 直接寫 |
| Throttle（10~30/min per route） | 成本控管 | 🔸 自行決定 |

## 保留了什麼（這些是有價值的設計，建議沿用）

| 機制 | 為什麼要保留 |
|---|---|
| **`fail_reason` 軟失敗模式** | LLM 會失敗，回 200 + `fail_reason` + 兜底文案，比丟 5xx 好處理太多。App 端邏輯已經照這個寫 |
| **完全無狀態** | 對話歷史、行程都由 App 帶入。後端不用做 session 管理，實作成本極低 |
| **`prod_id`/`oid` 由後端依索引取回** | 不把編號送進 LLM，避免幻覺編號。這是個好防呆 |

---

## API 一覽（16 支）

Base path 建議：`/api/companion/`（原本是 `/api/v3/companion/`）

| # | Method | 路徑 | LLM | 用途 |
|---|---|---|---|---|
| **Phase 1** ||||
| 1 | GET | `ai-partner` | ❌ | 人格/風格/外觀選項 |
| 2 | POST | `self-introduction` | ✅ | 旅伴自我介紹 |
| 3 | POST | `quiz` | ✅ | 取測驗題 |
| 4 | POST | `quiz-completions` | ✅ | 人格分析＋命定城市 |
| 5 | POST | `share-image-v2` | ✅ | 海報素材（輪詢） |
| 6 | GET | `quiz-gallery` | ❌ | 社群牆 |
| **Phase 2 材料層** ||||
| 7 | GET | `orders` | ❌ | 即將出發訂單 |
| 8 | GET | `wish_list` | ❌ | 收藏商品 |
| 9 | GET | `history` | ❌ | 瀏覽紀錄商品 |
| **Phase 2 開場層** ||||
| 10 | POST | `travel-summary-from-orders` | ✅ | 訂單→城市選項 |
| 11 | POST | `travel-summary-from-wish` | ✅ | 收藏→城市選項 |
| 12 | POST | `travel-summary-from-history` | ✅ | 瀏覽→城市選項 |
| **Phase 2 規劃層** ||||
| 13 | POST | `travel-summary` | ✅ | 聊天室開場摘要 |
| 14 | POST | `recommend-city` | ✅ | 城市收斂多輪對話 |
| 15 | POST | `travel-guide` | ✅ | 生成完整行程 |
| 16 | POST | `travel-revise` | ✅ | 自然語言改行程 |

> Demo 精簡建議：**Phase 2 的 13~16 是核心**，7~12 可先用假資料，1~6 的 Phase 1 可視 demo 範圍決定要不要做（尤其 5 海報產圖成本高）。

---

## Phase 2 核心四支（優先實作）

### 13. POST `travel-summary` — 聊天室開場摘要

```jsonc
// Request
{
  "entry_type": "quiz_completion | from_orders | imported_itinerary | from_zero",
  "city": "大阪",              // quiz_completion / from_orders 必填；回傳原樣返回（不經 LLM）
  "order": {                   // from_orders 選填：開場白會呼應訂購商品
    "prod_name": "大阪環球影城門票", "package_name": "1 日券", "go_dt": "2026-09-02"
  },
  "intro_text": "你是私房鑑賞家…",   // quiz_completion：測驗分析文字
  "source_type": "text",             // imported_itinerary
  "content": "Day1 清水寺…",          // imported_itinerary（≤30000 字）
  "previous_summary": "…",           // 「資訊不夠想補充」時帶前次 summary
  "note": "我想多排自然景觀",          // 補充說明
  "companion_name": "小旅", "personality": "humorous", "speech_style": "friendly"
}

// Response
{
  "summary": "大阪根本是為你這種吃貨開的城市！要不要就從這裡開始排？",
  "city": "大阪",        // quiz/from_orders = 原樣返回；imported = LLM 推斷；from_zero = ""
  "fail_reason": null
}
```

**LLM 語意**：依 entry_type 生成一句旅伴語氣的開場白。`from_zero` **不打 LLM**，回固定文案即可。帶 `previous_summary`+`note` 時要**整合成新的一份**取代前版（不是接在後面）。

---

### 14. POST `recommend-city` — 城市收斂多輪對話

```jsonc
// Request
{
  "messages": [                       // 必填 1~20 則，完整對話歷史全量帶入
    {"role": "user", "content": "京都大阪在猶豫"},
    {"role": "assistant", "content": "想要安靜慢步調還是熱鬧吃到飽？"},
    {"role": "user", "content": "安靜慢步調"}
  ],
  "shown_cities": ["布拉格"],          // 已推薦過、要排除的城市（≤30）
  "companion_name": "小旅", "personality": "…", "speech_style": "…"
}

// Response
{
  "reply": "那我推你去京都——要就定京都嗎？",
  "quick_replies": ["就去京都！", "換一個城市", "重新聊聊"],
  "recommended_city": "京都",   // 空字串 = 還在收斂中
  "city_reason": "古都慢步調很對你的味",
  "is_final": true,
  "off_topic": false,
  "round": 3, "max_rounds": 5, "remaining_rounds": 2,
  "swap_limit_reached": false,
  "fail_reason": null
}
```

**LLM 語意（實作重點）**：
- 輪次 = `messages` 中 `role=user` 的數量，**由後端算**
- 第 1~3 輪自由收斂（可提前給城市）；**第 4 輪結尾必提醒**「下次就選出城市」；**第 5 輪強制輸出城市**
- `recommended_city` 必須是**具體城市**（「京都」），不可是國家（「日本」）或行政區（「關西」）
- 禁止推薦 `shown_cities` 內的城市（跨語系辨識「京都」=「Kyoto」）；違規時後端硬檢查 → 自動帶回饋重試一次
- `is_final=true` 時 `quick_replies` **後端強制覆寫**為固定三顆
- 離題 → `off_topic=true`，用旅伴語氣導回，**該輪照樣計數**（防無限亂聊）

---

### 15. POST `travel-guide` — 生成完整行程

```jsonc
// Request
{
  "summary": "（travel-summary 或 recommend-city 的結果）",
  "city": "大阪",
  "preferences": {                    // key/value 不固定，後端不驗證固定 key
    "duration": "6~9 天（一般旅遊）", "budget": "舒適型（NT$30,000 ~ NT$80,000）",
    "pace": "平衡探索（每天 3~4 個景點）", "theme": "城市文化（歷史、建築、美食）",
    "priority": "美食與購物", "notes": "想帶長輩同行，不想自駕"
  },
  "orders": [                         // 選填 ≤3 筆：已預訂，必排入行程
    {"oid": "26KK216164788", "prod_name": "大阪環球影城門票",
     "package_name": "1 日券", "destination_name": "大阪", "go_dt": "2026-09-02"}
  ],
  "products": [                       // 選填 ≤10 筆：收藏/瀏覽過，必排入行程
    {"prod_id": "157138", "prod_name": "新天鵝堡冬季之旅",
     "introduction": "…", "destination_names": ["新天鵝堡"]}
  ],
  "companion_name": "小旅", "personality": "…", "speech_style": "…"
}

// Response
{
  "city": "大阪",
  "days": 3,
  "phase": "done",                    // done = 全排定；plan = 仍有 unplanned_days
  "unplanned_days": [],
  "pending_fields": ["航班"],
  "messages": [                       // 旅伴完成宣告，≤2 則、單則 ≤60 字
    {"type": "completion", "text": "行程排好了！這三天以美食為主軸…"}
  ],
  "itinerary_patch": {
    "mode": "full",
    "changed_days": [1, 2, 3],
    "days": [
      {
        "day": 1, "status": "planned", "kind": "arrival", "half_day": true,
        "booked_anchor": {"oids": ["26KK216164788"]},   // 該天含已預訂項目時才有值
        "items": [
          {"name": "道頓堀", "text": "晚間漫遊，燈牌配章魚燒", "type": "spot",
           "time": "19:00", "time_band": "晚上", "note": null,
           "lat": 34.6687, "lng": 135.5013,
           "oid": null, "prod_id": null}
        ]
      }
    ]
  },
  "progress_label": "已排 3/3 天",
  "main_action": {"type": "view_trip", "label": "看看完整行程"},
  "ai_model": "gemini-3.6-flash",
  "fail_reason": null,
  // 行程情境圖：成果頁 hero 與「我的旅程」縮圖用；產圖失敗為 null（App 退回漸層占位）
  "hero_image_url": "https://storage.googleapis.com/…/guide-hero/cda9a889e9b5-37e5dbab.png"
}
```

**item 欄位規則（實作重點）**：

| 欄位 | 規則 |
|---|---|
| `name` | 純地點/店家名稱（不含描述）。`spot`/`meal` 必填，`logistics` 通常 null |
| `text` | 一句話描述（**不含地點名稱本身**） |
| `type` | `spot` / `logistics` / `meal` |
| `time` | `HH:MM` 24 小時制，LLM 依節奏推算 |
| `transport_mode` | **僅 logistics**：`walk`/`bus`/`train`/`car`/null（非移動性質為 null） |
| `lat`/`lng` | **僅 spot**：近似座標（LLM 推算，非地理編碼），僅供大致標點 |
| `oid` | 有帶 `orders` 且通過白名單才有值。**LLM 幻覺的 oid 一律濾成 null** |
| `prod_id` | 有帶 `products` 且通過白名單才有值。同上 |

> **商業資訊禁令**：`name`/`text`/`note` **不可**出現價格、庫存、營業時間、供應商名稱——LLM 不真正知道這些，prompt 要明文禁止捏造。

---

### 16. POST `travel-revise` — 自然語言修改行程

```jsonc
// Request
{
  "itinerary": [ /* 當前最新完整行程，扁平陣列，形狀 = travel-guide 的 itinerary_patch.days */ ],
  "messages": [                       // 必填 1~100 則：整個聊天室從頭到尾的完整對話
    {"role": "assistant", "content": "行程排好了！這三天以美食為主軸…"},
    {"role": "user", "content": "我對海鮮過敏"},
    {"role": "assistant", "content": "了解，我避開海鮮"},
    {"role": "user", "content": "下午想加個抹茶體驗"}   // ← 最後一則 = 本次需求
  ],
  "city": "大阪",
  "target_day": 2,                    // 從哪個 Day 進入，優先只動這天
  "preferences": { /* 同 travel-guide */ },
  "companion_name": "小旅"
}

// Response（形狀與 travel-guide 幾乎相同，多了三個欄位）
{
  "reply": "幫你在 Day 2 下午加了抹茶體驗！",     // ≤60 字
  "changed_summary": "- Day 2 下午加了抹茶體驗",  // 可為 null
  "off_topic": false,
  "itinerary_patch": {"mode": "full", "changed_days": [2], "days": [ /* 完整行程 */ ]},
  "city": "大阪", "days": 3, "phase": "done",
  "fail_reason": null
}
```

**實作重點（這支最複雜）**：

1. **沒有 `request` 欄位**——本次需求 = `messages` 最後一則 `role=user`
2. `messages` 是**整個聊天室**的完整對話（開場摘要、城市對話、完成宣告、每輪修改），不是只有這次 bottom sheet。LLM 讀完整段脈絡再改，但**只處理最後一則需求**（之前已反映在 itinerary 的舊需求不重複套用）
3. **day 編號由後端重新指定**：LLM 輸出的陣列順序 = 天數順序，後端依序編號 1..N。新增一天 = 插入元素、刪除一天 = 移除元素，**不讓 LLM 自己算數字**
4. `changed_days` 由**後端 diff 算出**（比對輸入與輸出），純 UI 高亮用
5. **離題/失敗一律原樣返回輸入行程**（不會是空的），App 端可用同一套渲染邏輯
6. **白名單保護**：行程內既有 `oid` **不可刪除**（LLM 要拒絕並建議先處理訂單）；`prod_id` **非必要不主動刪**，但使用者明確要求可刪
7. 無次數上限

---

## Phase 2 材料層與開場層（7~12）

### 7~9：材料 API（GET，無參數）

```jsonc
// GET orders → 形狀同一般訂單列表
{"orders": [{"id": "26KK…", "lst_dt_go": 1756771200, "prod_name": "…",
             "package_name": "…", "destination": {"destinations": [{"name": "大阪"}]}}]}

// GET wish_list / history → 同一套商品 schema
{"prods": [{"prod_mid": 157138, "name": "…", "introduction": "…",
            "destinations": [{"name": "新天鵝堡"}]}]}
```

**App 端萃取規則**（新後端可直接照這個形狀設計，或更精簡）：

| 來源 | 萃取 | 排序/截取 |
|---|---|---|
| orders | `id`→oid、prod_name、package_name、`destinations[0].name`、`lst_dt_go`→`go_dt`(yyyy-MM-dd) | 過濾無出發日 → 依出發日升冪 → 前 **3** 筆 |
| wish_list/history | `prod_mid`→`prod_id`(字串)、name、introduction(**截 500 字**)、`destinations[].name` | 過濾空值 → 前 **20** 筆 |

### 10~12：開場層（材料 → 城市選項）

```jsonc
// POST travel-summary-from-orders
// Request: {"orders": [{"prod_name","package_name","destination_name"}], "companion_name"…}
// Response:
{"greeting": "Hi, 我是Kuma！我發現你的訂單中有即將出發的旅程，想從哪個目的地開始？",
 "options": [{"order_index": 0, "city": "東京"}],   // 逐筆對應，0-based
 "fail_reason": null}

// POST travel-summary-from-wish / -from-history
// Request: {"products": [{"prod_id","prod_name","introduction","destination_names"}], …}
// Response:
{"greeting": "看了你收藏的行程，要不要先選一個目的地？",
 "cities": [                                        // 聚合，多商品可歸同城市
   {"city": "慕尼黑", "products": [{"prod_id": "157138", "prod_name": "…"}]}
 ],
 "fail_reason": null}
```

**LLM 語意**：
- 只回**具體城市**，不回國家/行政區（`destination_name` 是「日本」「關西」這類籠統值時，要從 `prod_name` 收斂到城市）
- 城市判斷優先序：`destination_name` → 不精確時用 `prod_name`/`package_name` 推斷 → 完全無法判斷給空字串
- **編號防呆**：送進 LLM 的材料**刻意不含 `oid`/`prod_id`**，LLM 只回位置索引，編號由後端依索引從原始輸入取回 → 保證不會有幻覺編號
- 軟失敗（`llm_error` / `low_confidence`）→ `cities`/`options` 空陣列 + 兜底 greeting

---

## Phase 1（1~6）— 視 demo 範圍決定要不要做

| # | 路徑 | 精簡建議 |
|---|---|---|
| 1 | GET `ai-partner` | 選項可直接寫死在前端，**可省** |
| 2 | POST `self-introduction` | 單純 LLM 生成一段文案，**容易做** |
| 3 | POST `quiz` | 需要題庫；可寫死題目省掉 LLM 改寫，**可簡化** |
| 4 | POST `quiz-completions` | 核心：tag → 人格稱號 mapping + LLM 分析。**Phase 1 的重點** |
| 5 | POST `share-image-v2` | 需 gpt-image 產圖 + S3/CDN + 輪詢機制，**成本最高，demo 建議先跳過或用固定圖** |
| 6 | GET `quiz-gallery` | Redis 清單，**可用假資料** |

詳細欄位見 `reference/docs/ai-companion-rd-guide.md` 第五章。

---

## 建議的實作順序（後端）

```
1. travel-summary（from_zero 分支）      ← 不打 LLM，先讓 App 跑起來
2. travel-guide                          ← 核心價值，先做這個
3. travel-revise                         ← 第二核心
4. recommend-city                        ← 多輪對話
5. 材料層 7~9（假資料即可）
6. 開場層 10~12
7. Phase 1（視 demo 範圍）
```

---

## 實際部署差異（2026-08-15，T17）

- **回應信封**：所有端點實際都包 `{metadata:{status,desc}, data:{...}}`（`status=="0000"` 成功，失敗 `desc` 常是字串陣列）；`GET orders` 另帶 `pagination`/`dynamic`/`queue_it` 等雜訊欄位，一律只取 `data`。
- **`entry_type` 不是 `source`**：travel-summary 的欄位名稱以本文件的 request 定義為準，實測與文件一致。
- **`GET ai-partner`** 的選項欄位包在 `data.variant` 底下（非本文件早前草稿的攤平形狀），另外多了 `case_oid`/`version`/`expire_date`/`properties`/`partner_intro_prompt`（版控與內部 prompt 樣板，前端不需要）。
- **無獨立商品搜尋端點**：行程頁「用景點名稱找可訂商品」原規劃打後端搜尋 API，但沒有真後端可用
  （KKday 內部端點不接、三家主要 OTA 皆無公開商品搜尋 API，見 `research-ota-product-apis.md`）。
  T22 起整段商品搜尋 repository/usecase/mock 已移除，改成純開網頁的 5 個平台按鈕（見 02-ledger.md T22）。
- **`share-image-v2`**：端點存在，T19 起已接（顯示後端產好的海報圖，見 02-ledger.md T19/T20/T21）。
  app 端仍不做 Bitmap 合成/本機存檔/輪詢。
- **Ktor 序列化眉角**：後端把部分「有預設值」的欄位當必填（例如 `shown_question_counts`/`shown_cities` 即使空也要出現在 body），呼叫端需 `encodeDefaults = true`；LLM 軟失敗時部分非 nullable 欄位（如 travel-guide 的 `days`）會回 `null` 而非省略，需 `coerceInputValues = true` 才不會直接 decode 炸掉。
