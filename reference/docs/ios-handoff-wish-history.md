# iOS 交接執行手冊：一起規劃旅遊行程（從心願清單／從瀏覽記錄）

> 對應 Android 實作：`libs/feature/ai_companion/`。本文件涵蓋「一起規劃旅遊行程(從心願清單)」與「一起規劃旅遊行程(從瀏覽記錄)」兩顆按鈕的位置、點擊後的完整流程、呼叫的 API 與帶值方式。
> **2026-08 規格改版重點**：`prod_id` 會一路從清單傳到行程裡——回應的城市選項會附「是哪幾筆商品推導出這個城市」，選定城市後把這些商品帶進 `travel-guide`，**商品保證被排進行程**，行程 item 會帶 `prod_id` 供 UI 標示。
> 兩條流程**完全對稱**，只差資料來源與其中一支 API 路徑——照著其中一條做好，另一條換掉資料來源與 API 路徑即可完全複用。
> 後端完整規格見 `ai-companion-phase2-api.md` 與 `ai-companion-wish-history-integration.md`（b2c-api repo docs/）。

---

## 一、按鈕位置

旅伴首頁「想跟{旅伴名}一起做什麼呢？」意圖卡清單，由上而下：

1. 找到我的旅行 DNA 及命定旅程
2. 匯入你的 AI 行程
3. 社群旅伴貼文
4. 回顧我的旅行 DNA
5. 一起規劃旅遊行程
6. 一起規劃旅遊行程(帶訂單)
7. **一起規劃旅遊行程(從心願清單)** ← 本文件
8. **一起規劃旅遊行程(從瀏覽記錄)** ← 本文件

兩顆按鈕樣式與其他意圖卡完全相同（icon＋標題＋副標＋chevron 卡片列）：

| 按鈕 | 標題 | 副標 |
|---|---|---|
| 心願清單 | 一起規劃旅遊行程(從心願清單) | 從你收藏過的商品，找出你想去的地方 |
| 瀏覽記錄 | 一起規劃旅遊行程(從瀏覽記錄) | 從你最近看過的商品，找出你想去的地方 |

---

## 二、點擊後的完整流程

```
點按鈕
  → 進入「材料開場」聊天畫面（與帶訂單開場共用同一畫面，標題「一起規劃旅遊行程」）
  → 顯示打字中泡泡
  → [API 1] GET wish_list（或 GET history）取商品清單
  → 萃取每筆商品 4 個欄位（含 prod_id，見第三章），最多 20 筆
  → [API 2] POST travel-summary-from-wish（或 -from-history）
  → 回 greeting + 最多 3 個城市（每個城市附對應的商品 prod_id 清單，先存起來！）
  → 顯示 greeting 泡泡 + 城市 chips + 「我想直接開始規劃」chip
  → 使用者二選一：
      (a) 點某個城市 chip
          → [API 3] POST travel-summary（entry_type=from_orders，city=選的城市，不帶 order）
          → 聊天室接續顯示開場摘要（greeting 與使用者選擇保留在最上方，對話往下長）
          → 「開始規劃」→ 偏好問卷（純前端 Q1~Q6）
          → 答完 → [API 4] POST travel-guide（summary + city + preferences
                    + **該城市對應的 products[]**）
          → 行程成果頁：對應商品**一定**在行程裡，item 帶 prod_id，UI 標「感興趣」
      (b) 點「我想直接開始規劃」
          → POST travel-summary（entry_type=from_zero）＋直接啟動城市收斂對話（recommend-city）
          → 收斂出城市 → 問卷 → travel-guide（此路徑**不帶 products**——沒有選定城市的商品對應）
```

### 失敗與空資料分支

| 情況 | 判斷 | 畫面行為 |
|---|---|---|
| 商品清單為空 | API 1 的 `data.prods[]` 為空 | 固定文案「咦，我沒找到可以參考的資料耶…」＋「改用一般規劃」按鈕（走 `from_zero`） |
| LLM 判斷不出城市（軟失敗） | API 2 回 200 但 `cities` 為空（`fail_reason`=`llm_error`/`low_confidence`） | 渲染 `greeting`（兜底文案）＋「改用一般規劃」按鈕；不需區分兩種 fail_reason |
| 網路/硬失敗 | API 1 或 2 拋錯 | 錯誤泡泡＋「重試」（重跑**自己這條**流程）＋「改用一般規劃」 |

---

## 三、API 詳細規格與帶值

四支 API 都走一般 `v3/companion/*` 呼叫方式：目前 build 環境、正常登入態 header、標準 B2C 簽章，不需要任何特殊處理。

### API 1：取商品材料

```
GET /api/v3/companion/wish_list     （心願清單流程）
GET /api/v3/companion/history       （瀏覽記錄流程）
```

- 不帶參數、不帶 body。目前為**假資料端點**，之後接真實資料源 App 端不用改。
- 每筆商品萃取 **4 個欄位**（其餘 `img_url_list`/`official_price` 等都不需要）：

| 回應欄位 | 萃取到 | 說明 |
|---|---|---|
| `prods[].prod_mid`（或 `prod_id`） | `products[].prod_id` | **必填**，轉成**字串**帶入。整條商品對映鏈路的 key |
| `prods[].name` | `products[].prod_name` | **必填** |
| `prods[].introduction` | `products[].introduction` | 選填，**截斷到 500 字以內**（後端上限，超過整包 400） |
| `prods[].destinations[].name` | `products[].destination_names` | 選填，字串陣列；常是**景點/地標**（如「新天鵝堡」）而非城市——預期行為，LLM 會自己推斷城市 |

**萃取規則（Android 已實作，iOS 請對齊）**：
1. 過濾 `prod_id` 或 `name` 為空白的商品（皆必填，空值會 400）。
2. 依原始陣列順序取前 **20 筆**（API 上限）。
3. ⚠️ `pagination.total_count` 與 `prods[]` 實際長度目前不一致（假資料落差），以 `prods[]` 實際長度為準；`history` 回應多的 `algo_version`/`session_id` 不需要萃取。

### API 2：LLM 聚合判斷城市（回應含商品對應）

```
POST /api/v3/companion/travel-summary-from-wish       （心願清單流程）
POST /api/v3/companion/travel-summary-from-history    （瀏覽記錄流程）
```

Request（兩支形狀完全相同）：

```json
{
  "products": [
    {
      "prod_id": "157138",
      "prod_name": "從慕尼黑出發的新天鵝堡冬季之旅",
      "introduction": "與我們一起參觀由童話國王路德維希二世建造的新天鵝堡。",
      "destination_names": ["新天鵝堡"]
    }
  ],
  "companion_name": "Kuma",
  "personality": "溫暖",
  "speech_style": "活潑"
}
```

Response——**每個城市附上「是哪幾筆商品推導出這個城市」**（多筆商品可歸到同一城市）：

```json
{
  "metadata": { "status": "0000" },
  "data": {
    "greeting": "Hi, 我是Kuma！看了你收藏的行程，要不要先選一個目的地，我們從這裡開始規劃？",
    "cities": [
      { "city": "慕尼黑", "products": [ { "prod_id": "157138", "prod_name": "從慕尼黑出發的新天鵝堡冬季之旅" } ] },
      { "city": "馬德里", "products": [ { "prod_id": "141270", "prod_name": "西班牙馬德里一日遊…" } ] }
    ],
    "ai_model": "gpt-5.6-terra",
    "fail_reason": null
  }
}
```

| 欄位 | 用途 |
|---|---|
| `greeting` | 旅伴開場泡泡文字 |
| `cities[].city` | 最多 3 個城市選項，渲染成 chips，**chips 最後追加一顆「我想直接開始規劃」** |
| `cities[].products` | **先存起來**（Step 4 travel-guide 要用），不一定要顯示；也可用長度顯示「包含你收藏的 N 個行程」。`prod_id` 保證是你送過去的值之一（後端依位置索引取回，不會有模型捏造的編號，`prod_name` 也不會被改寫） |
| `fail_reason` | 有值＝軟失敗（仍回 200），`cities` 為空、`greeting` 為兜底文案；顯示「改用一般規劃」逃生按鈕，可原樣重打 |

- Throttle：10 次/分鐘；建議 timeout ≥15s。
- HTTP 400（`metadata.status="110001"`，無 `data`）＝request 組法有問題（products 沒帶/超過 20 筆/缺 `prod_id` 或 `prod_name`），修呼叫方式，不是重試。

### API 3：選定城市 → travel-summary 拿開場摘要

```json
POST /api/v3/companion/travel-summary
{ "entry_type": "from_orders", "city": "慕尼黑", "companion_name": "Kuma" }
```

- 比照帶訂單開場選完城市的做法：`entry_type=from_orders` + **`city` 必填**（回傳 `city` 一律等於帶入值，權威、不經 LLM）；**不帶 `order`**。
- `summary` 渲染成開場摘要泡泡；`city` 保存，下一步原樣帶入。
- 「我想直接開始規劃」則改打 `{ "entry_type": "from_zero" }`（拿固定文案當之後 travel-guide 的 `summary`）＋直接啟動 recommend-city 對話。

### API 4：travel-guide 帶該城市的 products → 商品一定被排進行程

**這是整條鏈路的目的**。把 API 2 回應中**被點選那個城市**的 `products`，補上 API 1 萃取的同一份材料（`introduction`/`destination_names`，LLM 更好判斷排哪天哪個時段），放進 `travel-guide` 的 `products[]`：

```json
POST /api/v3/companion/travel-guide
{
  "summary": "（travel-summary 回傳的摘要）",
  "city": "慕尼黑",
  "preferences": { "duration": "4~5 天", "theme": "城市文化（歷史、建築、美食）" },
  "products": [
    {
      "prod_id": "157138",
      "prod_name": "從慕尼黑出發的新天鵝堡冬季之旅",
      "introduction": "參觀由路德維希二世建造的新天鵝堡",
      "destination_names": ["新天鵝堡"]
    }
  ],
  "companion_name": "Kuma"
}
```

- `products[].prod_id`/`prod_name` **必填**（直接用 API 2 回應的值）；`introduction`/`destination_names` 選填建議帶。
- 上限 **10 筆**（比 API 2 的 20 筆少——「必須排入行程」的商品放太多會塞爆行程）。
- 該城市 `products` 為空陣列時**不帶** `products` 欄位。
- Android 實作供對照：以 `prod_id` 回查 API 1 萃取的原始材料補齊欄位，存成「選定城市的商品清單」，問卷答完打 travel-guide 時帶入。

回應中被排入的 item 會帶 `prod_id`：

```json
{ "name": "新天鵝堡", "text": "沿山徑走上城堡…", "type": "spot", "time": "10:00", "prod_id": "157138", "lat": 47.5576, "lng": 10.7498 }
```

### ⚠️ `prod_id` 與 `oid` 是兩條不同的軸，UI 不可混用

| | `items[].oid` | `items[].prod_id` |
|---|---|---|
| 來源 | `travel-guide` 的 `orders[]`（帶訂單入口） | `travel-guide` 的 `products[]`（心願清單/瀏覽記錄入口） |
| 語意 | **已預訂**（已付錢） | **想去、但還沒買** |
| 該天 `booked_anchor` | 有值（`{"oids":[...]}`） | **不受影響、維持 null** |
| UI 標示 | 「已預訂」 | 「感興趣」之類；**絕對不可標成「已預訂」** |
| revise 能否刪除 | 不可刪（LLM 會拒絕並建議先處理訂單） | 使用者明確要求時可以刪 |

Android 端 UI：行程項目標題旁，`oid` 有值標「已預訂」（CYAN）、否則 `prod_id` 有值標「感興趣」（AMBER）。

### travel-revise 的 prod_id 保護（不用額外做事）

- 行程內既有 item 的 `prod_id` 會**自動併入白名單保護**，每輪 revise **不需要**重帶 `products`，也不會弄丟。
- LLM 被要求「非必要不主動刪除」帶 `prod_id` 的項目，但使用者明確要求刪除時可以刪（與 `oid` 的一律不可刪不同）。
- 只有「要把新商品排進行程」時才需要在 revise 帶 `products`（形狀同 travel-guide）——目前 App 端沒有這個入口，不用實作。

---

## 四、對話接續（UX 重點，容易漏做）

使用者點了城市（或「我想直接開始規劃」）之後，**不要把畫面清掉重來**：

- 開場的 `greeting` 泡泡＋使用者的選擇（城市名／「我想直接開始規劃」）以**前導訊息**保留在聊天室最上方，後續內容往下長。
- 這兩句也要 append 進「整個聊天室的完整對話紀錄」（travel-revise 全量帶入的 messages）。

Android 端做法：ViewModel 維護 `planChatPrelude`（兩則），聊天畫面渲染在最上方，同時 append 進統一對話紀錄 `companionTranscript`；開新一輪規劃時清空。

---

## 五、檢查清單

- [ ] 兩顆按鈕在「一起規劃旅遊行程(帶訂單)」下方，樣式同其他意圖卡
- [ ] `GET wish_list`／`GET history` → 萃取 **prod_id（轉字串）**/name/introduction（≤500 字）/destinations[].name，過濾空 id/空名稱、取前 20 筆
- [ ] `POST travel-summary-from-wish`／`-from-history`：`products`（含 prod_id）＋旅伴人設
- [ ] 渲染 `greeting`＋`cities[].city` chips＋「我想直接開始規劃」chip；**每個城市的 `products` 先存起來**
- [ ] `cities` 為空（軟失敗）或商品清單為空：「改用一般規劃」；硬失敗另給「重試」（重跑自己這條流程）
- [ ] 選城市 → `travel-summary`（`from_orders`＋`city` 必填、不帶 order）
- [ ] 問卷答完 → `travel-guide` 帶**該城市的 `products[]`**（≤10 筆，prod_id/prod_name 必填，建議補 introduction/destination_names）
- [ ] 行程 item 的 `prod_id` 標「感興趣」，**不可**標「已預訂」；該天 `booked_anchor` 應為 null
- [ ] revise 不重帶 products，確認 `prod_id` 不會弄丟（後端自動白名單保護）
- [ ] 「我想直接開始規劃」→ `from_zero`＋直接啟動 recommend-city；此路徑不帶 products
- [ ] 開場對話（greeting＋使用者選擇）保留在聊天室最上方接續顯示，並進完整對話紀錄
- [ ] 測試：多筆同城商品應歸在同一個 `cities[]` 元素下（不會拆成重複城市）；後端這批 API 標註「尚未部署實測」，sit 打不通先確認部署狀態
