# iOS 交接：AI 旅伴開場材料 API（訂單／收藏／瀏覽記錄）

> 三支 GET 端點，供「一起規劃旅遊行程」的三個材料入口（帶訂單／從心願清單／從瀏覽記錄）取得開場材料。
> 三支都是**目前回傳固定 mock/假資料**的端點（後端不串下游服務），之後接上真實資料源時呼叫方式不變、App 端不用改。

---

## 共同呼叫方式

| 項目 | 說明 |
|---|---|
| Host／環境 | 跟其他 `v3/companion/*` API 完全相同：**目前 build 環境**（SIT/Stage/Production 各打各的），不需要指定固定環境 |
| Method | `GET`，**不帶任何 query 參數、不帶 body** |
| Headers | App 既有的 v3 公版 headers（`x-auth-token`／`token`／`member-uuid`／`timestamp`／`b2c-token1` 簽章等），沿用一般登入態即可，**不需要任何覆寫或特殊處理** |
| 回應信封 | 標準 `{"metadata": {...}, "data": {...}}`；`metadata.status == "0000"` 為成功 |
| 錯誤處理 | 這三支不打 LLM、無軟失敗概念；網路/系統錯誤直接當一般 API 錯誤處理（開場畫面顯示錯誤泡泡＋重試） |

---

## 1. GET `/api/v3/companion/orders` — 即將出發的訂單

**用途**：「一起規劃旅遊行程(帶訂單)」入口的材料來源。回應形狀**跟既有 `v2.2/orders` 完全一樣**（`data.orders[]`），目前為固定 mock 內容。

### 回應（節錄，只列需要的欄位）

```json
{
  "metadata": { "status": "0000", "desc": "Success" },
  "data": {
    "orders": [
      {
        "id": "26KK216164788",
        "lst_dt_go": 1756771200,
        "prod_name": "大阪環球影城門票",
        "package_name": "1 日券",
        "destination": {
          "destinations": [ { "code": "D-JP-1289", "name": "大阪" } ]
        }
      }
    ],
    "exists_over_date_order": false
  }
}
```

### 萃取規則（Android 已實作，iOS 請對齊）

每筆訂單萃取 5 個欄位，組成後續 `travel-summary-from-orders` 與 `travel-guide orders[]` 要的材料：

| 回應欄位 | 萃取為 | 說明 |
|---|---|---|
| `orders[].id` | `oid` | 訂單編號，之後 `travel-guide` 排入行程的對映 key |
| `orders[].prod_name` | `prod_name` | 商品名稱 |
| `orders[].package_name` | `package_name` | 方案名稱（可能為 null → 空字串） |
| `orders[].destination.destinations[0].name` | `destination_name` | 目的地名稱，**取陣列第一個** |
| `orders[].lst_dt_go` | `go_dt` | 出發日 epoch timestamp → 轉 **`yyyy-MM-dd`** 字串（注意單位可能是秒或毫秒，Android 端以 `< 10^12` 判斷為秒）；`travel-guide` 用它決定排哪一天 |

**排序與截取**：
1. 過濾掉 `lst_dt_go` 為 null 的訂單。
2. 依 `lst_dt_go` 由小到大排序（離今天最近的在前）。
3. 取前 **3 筆**（`travel-summary-from-orders` 的上限）。

**空資料**：`orders[]` 為空 → 開場畫面顯示「沒找到即將出發的訂單」文案＋「改用一般規劃」按鈕。

---

## 2. GET `/api/v3/companion/wish_list` — 收藏（心願清單）商品

**用途**：「一起規劃旅遊行程(從心願清單)」入口的材料來源。

### 回應（節錄）

```json
{
  "metadata": { "status": "0000", "desc": "Success", "pagination": { "total_count": 48, "start": 0, "count": 48 } },
  "data": {
    "prods": [
      {
        "prod_mid": 157138,
        "prod_oid": 157138,
        "name": "從慕尼黑出發的新天鵝堡冬季之旅",
        "introduction": "與我們一起參觀由童話國王路德維希二世建造的新天鵝堡。…",
        "destinations": [ { "code": "D-DE-5421", "name": "新天鵝堡" } ]
      }
    ]
  }
}
```

### 萃取規則

每筆商品萃取 4 個欄位，組成後續 `travel-summary-from-wish` 與 `travel-guide products[]` 要的材料：

| 回應欄位 | 萃取為 | 說明 |
|---|---|---|
| `prods[].prod_mid` | `prod_id` | **必填**，轉成**字串**帶入。整條商品對映鏈路的 key（後續回應會告訴你每個城市對應哪些 prod_id、行程 item 會回填 prod_id） |
| `prods[].name` | `prod_name` | **必填** |
| `prods[].introduction` | `introduction` | 選填，**截斷到 500 字以內**（後端欄位上限，超過整包 400） |
| `prods[].destinations[].name` | `destination_names` | 選填，取陣列裡**每個元素**的 name 組成字串陣列；常是**景點/地標**（如「新天鵝堡」）而非城市——預期行為，LLM 會自己從景點推斷城市 |

**過濾與截取**：
1. 過濾 `prod_mid` 或 `name` 為空的商品（後續 API 兩者皆必填，空值會 400）。
2. 依原始陣列順序取前 **20 筆**（`travel-summary-from-wish` 上限；假資料目前無「加入時間」可排序）。

⚠️ **注意**：`metadata.pagination.total_count` 目前固定回 48，但 `prods[]` 實際只有 3 筆（假資料落差）——**以 `prods[]` 實際長度為準**，不要用 total_count 當迴圈依據。

**不需要**萃取：`prod_oid`／`img_url_list`／`currency`／`official_price`／`rating_star` 等其他商品業務欄位。

---

## 3. GET `/api/v3/companion/history` — 瀏覽/購買紀錄商品

**用途**：「一起規劃旅遊行程(從瀏覽記錄)」入口的材料來源。**回應形狀與萃取規則跟 `wish_list` 完全相同**，照第 2 節做即可，唯二差異：

1. API 路徑不同（`history`），後續接的是 `travel-summary-from-history`（不是 `-from-wish`）。
2. 回應 `data` 多了推薦演算法的中繼欄位：

```json
{
  "data": {
    "prods": [ { "prod_mid": 282362, "name": "名古屋出發熱門上高地觀光健行巴士一日遊", "...": "同 wish_list 形狀" } ],
    "exp_version": 0,
    "algo_version": "history",
    "session_id": "987f5317-1dad-44d4-aa48-cb6d8fd96650"
  }
}
```

`exp_version`／`algo_version`／`session_id` 跟城市判斷無關，**不需要**萃取。

> 後端刻意把 wish/history 拆成兩支獨立 API（判讀邏輯未來可能各自演進、資料源之後可能各自變形），但對 App 端**兩條流程完全對稱**——同一套解析/萃取程式碼換個路徑就能複用。Android 端就是共用同一個 response model 與萃取函式。

---

## 材料取完之後接什麼

| 材料來源 | 下一支 API | 材料上限 | 完整流程文件 |
|---|---|---|---|
| `orders` | `POST travel-summary-from-orders` | 3 筆 | `ios-handoff-order-search.md` |
| `wish_list` | `POST travel-summary-from-wish` | 20 筆 | `ios-handoff-wish-history.md` |
| `history` | `POST travel-summary-from-history` | 20 筆 | `ios-handoff-wish-history.md` |

## curl 快速驗證

```bash
BASE=https://api-b2c.sit.kkday.com/api/v3/companion
# headers 沿用 App 抓包的一般 v3 公版 headers（含簽章），此處省略

curl -s $BASE/orders    | python3 -m json.tool
curl -s $BASE/wish_list | python3 -m json.tool
curl -s $BASE/history   | python3 -m json.tool
```

## 檢查清單

- [ ] 三支都是 GET、無參數、無 body、一般登入態 headers，不做任何環境/帳號覆寫
- [ ] orders：過濾無出發日 → 依 `lst_dt_go` 升冪 → 取前 3 筆；`lst_dt_go` 轉 `yyyy-MM-dd`（秒/毫秒相容）
- [ ] wish_list/history：過濾空 `prod_mid`/空 `name` → 取前 20 筆；`prod_mid` 轉字串當 `prod_id`；`introduction` 截 500 字
- [ ] 不使用 `pagination.total_count` 當筆數依據；不萃取 `algo_version`/`session_id` 等中繼欄位
- [ ] 空清單走「改用一般規劃」逃生門，不是錯誤畫面
