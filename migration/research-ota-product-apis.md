# Research：三家 OTA 是否提供「對外部合作夥伴開放的商品搜尋 API」

**日期**：2026-08-16
**用途**：AI 旅遊行程規劃 demo app，行程生成後想依「城市＋活動關鍵字」查詢可訂購商品做導購連結。對照 `migration/API_CONTRACT.md` 第 327 行的現況：「無獨立商品搜尋端點，行程頁『用景點名稱找可訂商品』在後端未部署，`TripProductSearchRepository` 續用 mock」——本研究是為了回答：能不能用 KKday/Klook/Trip.com 官方 API 把這塊做成真的。

**結論先講**：三家都**沒有**自助申請、免審核、回傳完整商品資料（含深連結）的公開「商品搜尋 API」。三家的「開放 API」其實都是**供應商上架用**（把你的商品同步進他們的系統），跟「查詢他們的商品庫」是相反方向。真正能導購用的路徑是各家的**聯盟行銷（affiliate）計畫**，但那是自助註冊 + 深連結/搜尋 widget，不是給你 JSON 回應去接自己 UI 的搜尋 API；要拿到「可程式化查詢＋結構化回應」等級的商品 feed，三家都需要走**商務洽談／指定合作夥伴**這條路。

---

## TL;DR 對照表

| 平台 | 公開 API 名稱 | 申請門檻 | 商品搜尋能力（依城市/關鍵字） | 分潤機制 |
|---|---|---|---|---|
| **KKday** | 無公開「商品搜尋」API。有 KKpartners 聯盟行銷平台（自助註冊）；另有「B2D」白牌訂購平台與 B2B API v3（Apiary）文件公開可見，但屬**供應商/通路商**上架與訂購用途 | 聯盟行銷：**免費自助註冊**，通過審核即可。API／完整商品列表：官方明講**僅「指定夥伴」可申請**（企業/媒體/品牌/航空/點數生態），非自助 | 聯盟後台可用**動態商品廣告**（挑商品產生素材）與白牌推廣頁，但**無**公開「輸入城市+關鍵字回傳 JSON 商品清單」的 API 文件 | 階梯制佣金，公開文件外的三方部落格估計約 **2%–5.5%**（依月銷售額分級），達 $200 美金可提領 |
| **Klook** | **Klook Open API**（klook.gitbook.io/openapi）——明確標註「intended for merchants, reservation systems & channel managers」，即**供應商把商品同步進 Klook**，非查詢 Klook 商品庫。另有獨立的 **Affiliate Partner Program**（affiliate.klook.com） | Affiliate：**自助 Sign Up** 即可申請，無最低流量門檻公開揭露；Open API（供應商向）需走 Partner With Us / 業務接洽 | Affiliate 後台提供 **Search Boxes（搜尋 widget，非開放 API）、Text Links（深連結）、Static/Activity Banners、Promo Codes**——是嵌入式 widget/深連結產生器，**不是**回傳結構化 JSON 給你自建 UI 的搜尋端點 | 三方資料估計 **5%–8%（affiliate 層級）**、Partner API 層級 5%–12%（未見官方公開費率頁） |
| **Trip.com** | **Trip.com Partner API - Tours & Tickets**（open.trip.com/apiplatform）——文件開頭即寫明「helps suppliers provide services to users…helps suppliers synchronize their products in Trip.com's product system」，即**供應商上架用**，非查詢用。另有獨立 **Trip.com Affiliate Program**（trip.com/partners） | Affiliate：**免費加入，無明說流量門檻**；Partner API（供應商向）需聯繫 `DMJSYFMP_gys@trip.com` 走商務對接 | Affiliate 提供 **Search Box widget**（可嵌入自家網站搜尋機票/飯店/火車票）與**深連結**，官方文案未特別強調 tours & tickets 的關鍵字搜尋 widget；同樣**沒有**公開的「依城市/關鍵字回傳結構化商品資料」REST API 給一般開發者 | 最高 **7%** 基本佣金，30 天 cookie window |

---

## 各家細節

### 1. KKday

- **KKpartners 聯盟行銷平台**（<https://kkpartners.kkday.com/>）：免費自助註冊，新戶審核通過後 3 個月內享 $10 美元旅遊金獎勵。頁面原文（繁中站）：
  > 「歡迎媒體網站、品牌、航空、點數生態等企業加入聯盟行銷合作，**指定夥伴可申請 API 與產品列表**。」
  這句話是全站唯一提到「API」的地方——明確把「API + 完整產品列表」列為**指定夥伴（非自助）**才能申請的進階功能，一般聯盟夥伴用的是後台的「動態商品廣告」（挑商品、一鍵產生推廣素材）與「白牌推廣頁」，不是開放 API。
  來源：<https://kkpartners.kkday.com/>
- **佣金**：官方頁面本身未列出公開費率表；第三方教學文章（非官方，僅供參考）指出階梯制、依月銷售額約 2%–5.5%，滿 $200 美金可電匯提領，CID 為專屬追蹤碼。
  來源（部落格，非一手）：搜尋彙整自多篇 KKday 聯盟行銷教學文章。
- **KKday B2D 平台**（<https://b2d.kkday.com/>）：白牌訂購網站（免費），支援搜尋/商品/組合選擇/訂單流程（不含金流），但這是給**通路商做完整訂購站台**用的商業合作，需另外「Sign up to be a partner」洽談，非公開自助 API。
- **KKday B2B API v3**（Apiary：<https://kkdayb2bapiv3.docs.apiary.io/>）：文件公開可瀏覽，但屬於 B2B/B2D 合作夥伴的訂購串接 API（給已簽約的通路夥伴用），不是對外開放註冊即可取得 key 的商品搜尋 API。

### 2. Klook

- **Klook Open API**（<https://klook.gitbook.io/openapi>）文件原文：
  > 「This specification is intended for **merchants, reservation systems & channel managers** who are looking to integrate with Klook.」
  > 「The API supports the retrieval of product content, availability, and pricing... helps suppliers synchronize their products in Klook's product system.」
  這是**供應商把自己的門票/行程商品同步進 Klook 賣**的 API（Product Integration + Order Integration），方向與我們要的（查詢 Klook 商品庫）相反。
  來源：<https://klook.gitbook.io/openapi>
- **Klook Affiliate Partner Program**（<https://affiliate.klook.com/>）：自助 Sign Up，портfolio 100,000+ 在地體驗。Tools 頁面（<https://affiliate.klook.com/tools/>，需登入後台完整版）列出的推廣工具：
  - Text Links（深連結）
  - Static Banners
  - **Search Boxes**（「Let users search activities within your website」——是嵌入式搜尋 widget，使用者在你網站上搜尋，實際查詢與導頁邏輯在 Klook 端，不是開放給你打的 JSON API）
  - Activity Banners
  - Promo Codes
  來源：<https://affiliate.klook.com/tools/>、<https://affiliate.klook.com/help/>
- 佣金：官方 FAQ 頁面本身未在可爬取內容中列出具體費率；第三方彙整資料估計 affiliate 層級 5%–8%，走 Partner API／大量對接層級 5%–12%（未經官方一手文件驗證）。

### 3. Trip.com

- **Trip.com Partner API - Tours & Tickets**（<https://open.trip.com/apiplatform/order_en.jsp>）文件原文：
  > 「Trip.com Partner API - Tours & Tickets is a platform that helps suppliers provide services to users... aimed at providing system integration services to suppliers... to help suppliers synchronize their products in Trip.com's product system via the API interface, this set of standard interfaces is developed, which includes creating and updating product or package info and synchronize prices and stocks.」
  同樣是**供應商上架 API**（Product Integration + Order Integration），不是查詢 Trip.com 商品庫的搜尋 API。有提供 OpenAPI spec 下載與加密簽章機制，但用途是供應商同步庫存/價格，需聯繫 `DMJSYFMP_gys@trip.com` 走商務對接。
  來源：<https://open.trip.com/apiplatform/order_en.jsp>
- **Trip.com Affiliate Program**（<https://www.trip.com/partners>）：免費加入，最高 7% 基本佣金，30 天 cookie。工具包含深連結與 **Search Box widget**（可嵌入自家網站搜尋機票/飯店/火車票），官方文案未特別強調 tours & tickets 品類的搜尋 widget，也未見公開的活動類商品 JSON API。
  來源：<https://www.trip.com/partners>（頁面載入需 JS 渲染，實際內容以搜尋引擎摘要與商業慣例交叉確認）。

---

## 對這個 demo 的建議接法

1. **不要期待任何一家有「自助申請、免審核、回傳 JSON（名稱/圖/價格/評分/深連結）」的商品搜尋 API。** 三家的「開放 API」文件都是給供應商用的反方向 API（push 商品進去），跟「pull 商品出來做導購」是兩回事。這點建議明確寫進 `migration/02-ledger.md`，避免之後又花時間找「有沒有漏看的商品搜尋端點」。

2. **符合這個 demo 定位（輕量化、不接 KKday B2C、換自己後端）的務實選項，依優先序：**
   - **(a) 繼續 mock `TripProductSearchRepository`**（目前做法）——demo 目的是驗證體驗流程，不是真的要導購賺分潤，這是成本最低、最貼合 CLAUDE.md「不追求功能完整」的路徑。可以把 mock 資料做得更像真實 KKday 商品（用 `reference/` 裡看得到的欄位形狀），維持 UI 邏輯不變。
   - **(b) 若真的想接一條「看起來像真的可以點進去買」的路徑**：三家都提供**深連結（deep link）格式**，可以做「假搜尋、真深連結」——即後端仍用你自己的邏輯/LLM 判斷城市+關鍵字該連去哪個商品類別，但實際跳轉用 KKday/Klook/Trip.com 的**站內搜尋結果頁深連結**（例如 `https://www.kkday.com/zh-tw/search?keyword=...&city=...` 這類前台搜尋頁面 URL，不需要 API key），使用者點下去看到的是真實商品，只是沒有先在自家 UI 呈現價格/評分等結構化資料。這條路不需要商務洽談、不需要審核，缺點是無法在行程頁先渲染商品卡片（名稱/圖/價格），只能做「查看更多商品」按鈕。
   - **(c) 若之後這個 demo 要升級成有實際導購分潤的產品**：需要走商務接洽——KKday 是「指定夥伴可申請 API 與產品列表」，Klook/Trip.com 則是先加入自助 affiliate 拿到深連結權限，若要拿到結構化 feed／更高階 API 存取，一樣要走 Partner With Us／業務窗口。這已經超出 demo 範疇，需要 PM/BD 介入，不是工程可以自助解決的。

3. **不建議**接第三方聯盟網路（Impact/CJ/Awin 等）做 KKday/Klook/Trip.com 的 deeplink——本次研究沒有找到三家官方公開在這些網路上架的一手證據（KKday 有自建 KKpartners，Klook/Trip.com 也都是自建 affiliate 站台），貿然假設走 CJ/Impact 反而可能查不到正確 deeplink 格式。若要驗證，需要在 CJ/Impact 後台實際搜尋這三個商家帳號是否存在（需要帳號登入，本次研究工具無法驗證）。
