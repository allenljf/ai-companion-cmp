# iOS 交接：行程頁商品搜尋卡 + 帶訂單開場

> 對應 Android 實作：`libs/feature/ai_companion/`（商品搜尋、帶訂單開場）、`libs/networking/api/.../core/network/`（商品搜尋 API 的 HTTP client／header 覆寫）。
> 這份文件涵蓋兩支獨立功能：① 行程頁景點商品搜尋卡（**固定直接打正式環境**，跟平常 App 網路層行為不同，請特別注意）、② 帶訂單開場（走目前 build 環境正常登入態，跟平常 API 打法一樣，只是後端目前回 mock 資料）。

---

## 一、行程頁景點商品搜尋卡

### 1.1 功能說明

行程結果頁每個景點下方，背景用景點名稱打商品搜尋 API，找到商品就顯示第一項商品的縮圖卡片＋「還有 N 項相關商品」，點擊卡片用 deeplink 開啟搜尋結果頁。

### 1.2 API

```
POST /v2.1/search/product_list
```

**固定打正式環境**，不論目前 App 實際 build 環境是什麼（SIT/Stage/Production 皆一樣，一律打正式環境的 host），
理由：測試環境商品數太少，同一個關鍵字常常只查得到 0~1 項商品，不足以做 demo 展示。

- Base URL：`https://api-b2c.kkday.com/api/`

#### Request Body

```json
{
  "start": 0,
  "count": 10,
  "q": "東京 東京迪士尼樂園",
  "rewrite": "1",
  "translate_status": 1,
  "page_name": "product_list_mobile"
}
```

| 欄位 | 說明 |
|---|---|
| `q` | 搜尋關鍵字，**組成方式是 `"{目的地城市} {景點名稱}"`**（例如 `"東京 東京迪士尼樂園"`），不能只帶景點名稱——同名景點在不同城市會誤配（例如「中央市場」）。目的地城市取自目前這趟行程的 `city`。 |
| `count` | 10（比照既有搜尋頁預設值即可，這裡的 `count` 只影響縮圖來源筆數，**不要拿來當「還有 N 項」的依據**，見下方 Response 說明） |
| `page_name` | 固定 `"product_list_mobile"`，**必填**，少帶這欄後端會回 `110001 The page name field is required.` |
| `rewrite` | 固定 `"1"` |
| `translate_status` | 固定 `1` |

#### Response

沿用既有搜尋結果頁的 `SearchProductResult` 結構，重點欄位：

```json
{
  "metadata": {
    "status": "0000",
    "pagination": {
      "total_count": 37
    }
  },
  "data": {
    "prods": [ /* 商品卡陣列，取第一項顯示縮圖 */ ]
  }
}
```

- 「還有 N 項相關商品」= `metadata.pagination.total_count - 1`，**不是** `data.prods.size - 1`（`prods` 只是本次抓回的縮圖來源，不代表真正符合關鍵字的總數）。
- 找不到商品（`prods` 為空或 API 失敗）就不顯示卡片，不顯示錯誤訊息（純附加功能）。

### 1.3 Header（固定覆寫正式環境的值）

因為 baseUrl 已經是正式環境，其餘裝置類 header（`device-model`／`os-version`／`lang`／`locale`／`currency`／`user-latlong`／`timezone`／`ud1`~`ud5`／`ad-id`／`kk-ad-id`／`mixpanel-id`／`cid`／`x-req-source`／`x-req-version`）沿用目前 App 正常會帶的值即可，**不需要特殊處理**，只有以下這一個 header 需要強制覆寫成正式環境的值：

| Header | 值 | 說明 |
|---|---|---|
| `x-auth-token` | `FtUNyeEcjRmRD1OAG/OdaD5aBMTT5YfaiadIUe8SOc4=` | 正式環境的 app 端 key（對應 Android `AppConfigProduction.baseApiXAuthTokenValue`），**不可**用目前 build 環境（SIT/Stage）的值，否則正式環境會拒絕請求 |
| `token` | 沿用目前登入使用者的 token（不覆寫） | 這支 API 不需要固定測試帳號，一般登入態即可（跟下面「帶訂單開場」不同） |
| `member-uuid` | 沿用目前登入使用者（不覆寫） | 同上 |
| `timestamp` / `b2c-token1` | 沿用既有 B2C API 簽章演算法（iOS 應該已經有共用實作，跟平常打其他 B2C API 一樣，不需要另外處理） | 純簽章，跟環境／帳號都無關 |

> Android 端做法：獨立開一個 OkHttpClient/Retrofit instance，baseUrl 寫死正式環境，只在 interceptor 覆寫 `x-auth-token`，其餘 header 照常組裝。iOS 若也是共用同一套 header 組裝邏輯，只需要在打這支 API 時額外指定 baseUrl + 覆寫 `x-auth-token` 即可，不需要複製整套簽章邏輯。

### 1.4 點擊卡片 → Deeplink

點擊整張卡片，用**真正的 App Link**（不是站內 router）開啟搜尋結果頁：

```
https://www.kkday.com/zh-tw/product/productlist?keyword={景點名稱，需 URL encode}
```

- Host **固定寫死正式環境 `www.kkday.com`**，不要跟著目前 build 環境走。原因：正式環境 App 的 App Link 驗證（`assetlinks.json` / iOS 的 `apple-app-site-association`）只登記在正式環境網域下；若用 SIT/Stage 的網域，已安裝的正式環境 App 不會被系統判定為已驗證的 handler，會被導去一般瀏覽器而不是導回 App。
- `keyword` 帶純景點名稱即可（不需要帶城市），對應既有搜尋結果頁吃的 query 參數就是 `keyword`。

---

## 二、帶訂單開場（demo）

### 2.1 功能說明

旅伴首頁「一起規劃旅遊行程」下方多一顆「一起規劃旅遊行程(帶訂單)」按鈕。點擊後：

1. 背景抓該會員即將出發的訂單（最多 3 筆，依出發日由近到遠排序），整理成訂單材料。
2. 把材料丟給新 API `travel-summary-from-orders`（**後端尚未實作**，見下方章節三），LLM 判斷出每筆訂單對應的目的地城市。
3. 畫面顯示：「Hi, 我是 {旅伴名}！我發現你的訂單中有即將出發的旅程，你想要從哪個目的地開始？」+ 最多 3 個城市選項 chip。
4. 使用者點選其中一個城市後，**不會**再打一次 `travel-summary`（後端 `entry_type` 只認 `quiz_completion`/`imported_itinerary`/`from_zero`，沒有帶訂單專用值，硬塞會被判 `110001 The selected entry type is invalid.`）。改成直接把「城市已定案」的狀態設好，等同「從零開始」入口走完 5 輪城市收斂對話後、使用者按下「就去{城市}！」的那個狀態——接著進入聊天式偏好問卷，答完才打 `travel-guide` 產生行程。

> **這支功能現在走的是 mock 資料**：`v3/companion/orders` 目前後端回傳固定的 mock 內容（形狀跟既有 `v2.2/orders` 一樣），不是真的查該會員的訂單。等後端把這支換成真正查詢邏輯後，App/iOS 端完全不用改，因為呼叫方式、header、response 解析都跟平常的登入態 API 一樣。

### 2.2 訂單列表 API

```
GET /v3/companion/orders
```

- **走目前 App build 當下的環境**（SIT/Stage/Production 都一樣，跟其他 `v3/companion/*` API 完全相同的打法），不需要指定固定環境。
- **不需要帶任何 body**（GET 且無 query params）。
- Header **完全比照其他 `v3/companion/*` API**（例如 `travel-summary`）：沿用目前登入態的 `token`／`member-uuid`，`x-auth-token` 用目前 build 環境對應的值，不需要任何覆寫或指定測試帳號。

#### 從 Response 取材料

Response 資料形狀跟既有 `v2.2/orders` 完全一樣（`data.orders[]`），`OrderInfo`（每一項）取這三個欄位當材料：

| Response 欄位 | 用途 |
|---|---|
| `prod_name` | 商品名稱 |
| `package_name` | 方案名稱 |
| `destination.destinations[0].name` | 目的地名稱（`destinations` 陣列取第一個） |

**排序與截取規則**：

1. 過濾掉沒有出發日（`lst_dt_go` 為 null）的訂單。
2. 依 `lst_dt_go`（出發日 timestamp）由小到大排序（離今天最近的排最前面）。
3. 取前 **3 筆**。

每筆訂單組成一組材料（`prod_name` + `package_name` + `destination_name`），最多 3 組，依上面排序後的順序（第 0 筆最快出發）送進下一支 API。

---

## 三、新 API `travel-summary-from-orders`（後端尚未實作）

這支是給 LLM 判斷「每筆訂單材料對應的目的地城市」用的，**目前後端還沒做**，App 端已經把 request/response 型別都定義好可以直接測，打下去現在會是 404。已經另外給後端一份 handoff prompt 說明要怎麼實作（含 LLM 輸出規範），這裡只講 App/iOS 端要怎麼呼叫、怎麼處理回應。

### 3.1 Request

```
POST /v3/companion/travel-summary-from-orders
```

```json
{
  "orders": [
    { "prod_name": "東京迪士尼樂園 & 東京迪士尼海洋門票", "package_name": "東京迪士尼度假區：單園一日護照", "destination_name": "東京" },
    { "prod_name": "首爾自由行接送", "package_name": "機場-市區單程", "destination_name": "首爾" }
  ],
  "companion_name": "kk",
  "personality": "tsundere",
  "speech_style": "chuunibyou"
}
```

- `orders`：上一步整理好的最多 3 筆材料，**保持排序**（第 0 筆是最快出發的訂單）。
- `companion_name` / `personality` / `speech_style`：目前使用者旅伴的人設，用於生成開場白語氣，可為 null。
- 這支 API 走一般登入態即可（不是 demo 的那個固定測試帳號），跟平常打其他 `v3/companion/*` API 一樣。

### 3.2 Response

```json
{
  "metadata": { "status": "0000" },
  "data": {
    "greeting": "Hi, 我是kk！我發現你的訂單中有即將出發的旅程，你想要從哪個目的地開始？",
    "options": [
      { "order_index": 0, "city": "東京" },
      { "order_index": 1, "city": "首爾" }
    ],
    "fail_reason": null
  }
}
```

| 欄位 | 說明 |
|---|---|
| `greeting` | 開場白，直接顯示在聊天室第一則訊息（旅伴訊息泡泡） |
| `options` | 目的地選項，`order_index` 對應請求 `orders` 陣列的索引（0-based）；畫面把每個 `city` 顯示成一顆可點的 chip |
| `fail_reason` | 有值＝LLM 軟失敗，`greeting` 為後端兜底文案，`options` 可能是空陣列；此時畫面顯示兜底文案＋「改用一般規劃」按鈕（不顯示任何城市 chip） |

### 3.3 選定城市後的處理（重要：不要再打 `travel-summary`）

使用者點選某個城市 chip 後：

1. **不要**呼叫 `POST /v3/companion/travel-summary`（後端 `entry_type` 目前只驗證 `quiz_completion`/`imported_itinerary`/`from_zero` 三種固定值，沒有「帶訂單」專用值，硬塞任何自訂值都會被判 `110001 The selected entry type is invalid.`，這是實測過的真實錯誤）。
2. 用 `order_index` 反查回第 2 章整理好的那筆訂單材料，組一句摘要文字，例如：

   ```
   我想安排「東京」的行程。這趟旅程來自我的訂單：東京迪士尼樂園 & 東京迪士尼海洋門票（東京迪士尼度假區：單園一日護照）。
   ```

3. 把這句摘要文字＋選定的城市，**直接當成「城市已定案」的狀態**，等同「從零開始」入口走完 recommend-city 5 輪收斂對話、使用者按下「就去{城市}！」之後的狀態——聊天室顯示這句摘要文字（當作旅伴的開場摘要泡泡）＋「開始規劃」按鈕。
4. 使用者按「開始規劃」→ 進入既有的聊天式偏好問卷（純前端，不打 API，一次一題）。
5. 問卷答完 → 打 `POST /v3/companion/travel-guide`（既有 API，不需要新增），帶：
   - `summary` = 上面第 2 步組的摘要文字
   - `city` = 選定的城市名稱
   - `preferences` = 問卷收集到的答案

也就是說，**`travel-summary-from-orders` 只負責「判斷目的地選項」這一步**，選定之後直接接上既有「城市已知，進問卷，問卷完打 travel-guide」的流程，不需要為這條路徑另外設計新的 UI 狀態或串新的 API。

---

## 四、快速檢查清單（iOS 對照用）

- [ ] 商品搜尋卡：打 `v2.1/search/product_list`，baseUrl 寫死正式環境，關鍵字＝`"{城市} {景點名}"`，`page_name` 必帶
- [ ] 商品搜尋卡：「還有 N 項」用 `metadata.pagination.total_count`，不是 `prods.size`
- [ ] 商品搜尋卡：header 只需覆寫 `x-auth-token`（正式環境值），其餘照常
- [ ] 商品搜尋卡：點擊用 deeplink `https://www.kkday.com/zh-tw/product/productlist?keyword=...`（host 寫死正式環境）
- [ ] 帶訂單開場：訂單 API 打 `v3/companion/orders`（GET，不帶 body），走目前 build 環境正常登入態，header 不用覆寫任何東西
- [ ] 帶訂單開場：訂單依 `lst_dt_go` 排序取前 3 筆，材料＝`prod_name`＋`package_name`＋`destinations[0].name`
- [ ] 帶訂單開場：呼叫 `travel-summary-from-orders`（後端尚未實作，先擋著等後端上線）
- [ ] 帶訂單開場：選定城市後**不要**再打 `travel-summary`，直接接上「城市已知」狀態進問卷
