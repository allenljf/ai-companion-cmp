package com.allenljf.aicompanion.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// ---------- Requests ----------

@Serializable
data class QuizRequest(
    @SerialName("shown_question_counts") val shownQuestionCounts: Map<String, Int> = emptyMap(),
    // API 規格：personality 為單一字串（App 端限定選 1 個）
    @SerialName("personality") val personality: String,
    @SerialName("speech_style") val speechStyle: String
)

@Serializable
data class QuizCompletionRequest(
    @SerialName("completion_uuid") val completionUuid: String,
    // API 規格：personality 為單一字串（App 端限定選 1 個）
    @SerialName("personality") val personality: String,
    @SerialName("speech_style") val speechStyle: String,
    @SerialName("selected_tags") val selectedTags: List<String>,
    @SerialName("shown_cities") val shownCities: List<String> = emptyList(),
    @SerialName("companion_name") val companionName: String? = null,
    // 使用者當時捏好的旅伴頭像 URL，供 quiz-gallery 清單顯示頭像；未捏頭像時不帶
    @SerialName("partner_avatar_url") val partnerAvatarUrl: String? = null
)

@Serializable
data class SelfIntroductionRequest(
    @SerialName("companion_name") val companionName: String,
    // API 規格：personality 為單一字串（App 端限定選 1 個）
    @SerialName("personality") val personality: String,
    @SerialName("speech_style") val speechStyle: String,
    @SerialName("gender") val gender: String
)

// ---------- GET /companion/ai-partner ----------
// 實際部署（2026-08）：選項欄位包在 data.variant 底下，data 另有 case_oid/version/expire_date/
// properties（都是版控/快取用途，這裡不需要）；partner_intro_prompt 是後端內部 LLM prompt 樣板，忽略即可

@Serializable
data class AiPartnerDataResponse(
    @SerialName("variant") val variant: AiPartnerVariantResponse = AiPartnerVariantResponse()
)

@Serializable
data class AiPartnerVariantResponse(
    @SerialName("personality") val personality: List<PersonalityOptionResponse> = emptyList(),
    @SerialName("speech_style") val speechStyle: List<SpeechStyleOptionResponse> = emptyList(),
    @SerialName("gender") val gender: List<AppearanceOptionResponse> = emptyList(),
    @SerialName("outfit") val outfit: List<AppearanceOptionResponse> = emptyList(),
    @SerialName("hair_style") val hairStyle: List<AppearanceOptionResponse> = emptyList(),
    @SerialName("hair_color") val hairColor: List<AppearanceOptionResponse> = emptyList(),
    // key = "{gender}_{outfit}_{hair_style}_{hair_color}" → 頭像圖片 URL
    @SerialName("avatars") val avatars: Map<String, String> = emptyMap()
)

@Serializable
data class PersonalityOptionResponse(
    @SerialName("tag") val tag: String = "",
    @SerialName("label") val label: String = "",
    @SerialName("description") val description: String? = null
)

@Serializable
data class SpeechStyleOptionResponse(
    @SerialName("tag") val tag: String = "",
    @SerialName("label") val label: String = "",
    @SerialName("description") val description: String? = null
)

@Serializable
data class AppearanceOptionResponse(
    @SerialName("tag") val tag: String = "",
    @SerialName("label") val label: String = ""
)

// ---------- POST /companion/quiz ----------

@Serializable
data class QuizDataResponse(
    @SerialName("count") val count: Int = 0,
    @SerialName("ai_model") val aiModel: String? = null,
    @SerialName("fail_reason") val failReason: String? = null,
    @SerialName("questions") val questions: List<QuizQuestionResponse> = emptyList()
)

@Serializable
data class QuizQuestionResponse(
    @SerialName("id") val id: String = "",
    @SerialName("dimension_id") val dimensionId: Int = 0,
    @SerialName("index") val index: Int = 0,
    @SerialName("type") val type: String = "",
    @SerialName("mode") val mode: String = "",
    @SerialName("text") val text: String = "",
    @SerialName("options") val options: List<QuizOptionResponse> = emptyList()
)

@Serializable
data class QuizOptionResponse(
    @SerialName("id") val id: String = "",
    @SerialName("index") val index: Int = 0,
    @SerialName("text") val text: String = "",
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("tag") val tag: QuizOptionTagResponse? = null
)

@Serializable
data class QuizOptionTagResponse(
    @SerialName("id") val id: String = "",
    @SerialName("label") val label: String = ""
)

// ---------- POST /companion/quiz-completions ----------

@Serializable
data class QuizCompletionDataResponse(
    @SerialName("quiz_completion_id") val quizCompletionId: Long = 0,
    @SerialName("travel_identity") val travelIdentity: String = "",
    @SerialName("travel_identity_en") val travelIdentityEn: String = "",
    @SerialName("destination_cn") val destinationCn: String = "",
    @SerialName("destination_en") val destinationEn: String = "",
    @SerialName("destination_country") val destinationCountry: String = "",
    @SerialName("destination_country_en") val destinationCountryEn: String = "",
    @SerialName("tagline") val tagline: String = "",
    @SerialName("tagline_en") val taglineEn: String = "",
    @SerialName("highlight_tags") val highlightTags: List<String> = emptyList(),
    @SerialName("highlight_tags_en") val highlightTagsEn: List<String> = emptyList(),
    @SerialName("companion_quote") val companionQuote: String = "",
    @SerialName("companion_quote_en") val companionQuoteEn: String = "",
    // 旅伴口吻推薦理由，固定 3 段、App 逐段分開顯示（後端相容舊版單字串：會自動包成單元素陣列）
    @SerialName("recommendation") val recommendation: List<String> = emptyList(),
    // 產圖等待畫面用的 AI 思考過程逐句陣列（≥10 句、每句 ≤20 字）；LLM 失敗時為空陣列
    @SerialName("reasoning") val reasoning: List<String> = emptyList(),
    @SerialName("social_post") val socialPost: String = "",
    @SerialName("share_image_status") val shareImageStatus: String = "",
    @SerialName("ai_model") val aiModel: String? = null,
    @SerialName("share_image_ai_model") val shareImageAiModel: String? = null,
    @SerialName("fail_reason") val failReason: String? = null
)

// ---------- POST /companion/self-introduction ----------

@Serializable
data class SelfIntroductionDataResponse(
    @SerialName("introduction") val introduction: String = "",
    @SerialName("ai_model") val aiModel: String? = null,
    @SerialName("fail_reason") val failReason: String? = null
)

// ---------- POST /companion/travel-summary（Phase 2：聊天室初始化摘要）----------

@Serializable
data class TravelSummaryRequest(
    // quiz_completion / from_orders / imported_itinerary / from_zero
    @SerialName("entry_type") val entryType: String,
    // 2026-08 改版：quiz_completion／from_orders 入口必填（缺少回 400），回傳 city 一律原樣用此值（權威、不經 LLM）
    @SerialName("city") val city: String? = null,
    // from_orders 入口選填：被點選那筆訂單的材料，開場白會呼應訂購商品
    @SerialName("order") val order: TravelSummaryOrderRequest? = null,
    @SerialName("city_image_url") val cityImageUrl: String? = null,
    @SerialName("intro_text") val introText: String? = null,
    // text / image
    @SerialName("source_type") val sourceType: String? = null,
    @SerialName("content") val content: String? = null,
    // 目前後端無圖片上傳端點可取得外部可讀 URL，image 路徑暫未開放（見 phase2 API 文件未定案事項 1）
    @SerialName("image_urls") val imageUrls: List<String>? = null,
    @SerialName("note") val note: String? = null,
    // 「資訊不夠再補充」重複呼叫時帶上前次回應的 summary
    @SerialName("previous_summary") val previousSummary: String? = null,
    @SerialName("companion_name") val companionName: String? = null,
    @SerialName("personality") val personality: String? = null,
    @SerialName("speech_style") val speechStyle: String? = null
)

// from_orders 入口帶入的訂單材料（開場白呼應訂購商品用）
@Serializable
data class TravelSummaryOrderRequest(
    @SerialName("prod_name") val prodName: String,
    @SerialName("package_name") val packageName: String? = null,
    // yyyy-MM-dd 出發日
    @SerialName("go_dt") val goDt: String? = null,
)

@Serializable
data class TravelSummaryDataResponse(
    @SerialName("summary") val summary: String = "",
    @SerialName("city") val city: String = "",
    @SerialName("ai_model") val aiModel: String? = null,
    @SerialName("fail_reason") val failReason: String? = null
)

// ---------- POST /companion/travel-summary-from-orders（Phase 2：帶訂單開場，依訂單材料判斷目的地選項）----------
// 後端尚未實作，請見隨附的後端 handoff prompt

@Serializable
data class TripOrderMaterialRequest(
    @SerialName("prod_name") val prodName: String,
    @SerialName("package_name") val packageName: String,
    @SerialName("destination_name") val destinationName: String,
)

@Serializable
data class TravelSummaryFromOrdersRequest(
    // 最多 3 筆，依訂單出發日由近到遠排序
    @SerialName("orders") val orders: List<TripOrderMaterialRequest>,
    @SerialName("companion_name") val companionName: String? = null,
    @SerialName("personality") val personality: String? = null,
    @SerialName("speech_style") val speechStyle: String? = null,
)

@Serializable
data class TravelDestinationOptionResponse(
    // 對應送出的 orders 清單索引（0-based），用於選定後取回該筆訂單材料
    @SerialName("order_index") val orderIndex: Int,
    @SerialName("city") val city: String,
)

@Serializable
data class TravelSummaryFromOrdersDataResponse(
    @SerialName("greeting") val greeting: String = "",
    @SerialName("options") val options: List<TravelDestinationOptionResponse> = emptyList(),
    @SerialName("ai_model") val aiModel: String? = null,
    @SerialName("fail_reason") val failReason: String? = null,
)

// ---------- GET /companion/orders ----------
// 真實訂單形狀欄位極多（KKday 訂單系統原樣回傳）；這裡只取材料層用得到的 4 個欄位，其餘忽略

@Serializable
data class OrdersDataResponse(
    @SerialName("orders") val orders: List<OrderResponse> = emptyList()
)

@Serializable
data class OrderResponse(
    @SerialName("id") val id: String = "",
    @SerialName("prod_name") val prodName: String = "",
    @SerialName("package_name") val packageName: String = "",
    // yyyy-MM-dd，後端已格式化好，不需再自己轉換 epoch
    @SerialName("go_dt") val goDt: String? = null,
    @SerialName("destination") val destination: OrderDestinationResponse? = null
)

@Serializable
data class OrderDestinationResponse(
    @SerialName("destinations") val destinations: List<OrderDestinationItemResponse> = emptyList()
)

@Serializable
data class OrderDestinationItemResponse(
    @SerialName("name") val name: String = ""
)

// ---------- GET wish_list / GET history + POST travel-summary-from-wish / -from-history ----------
// 願望清單／瀏覽紀錄開場：兩條流程完全對稱（見 ai-companion-wish-history-integration.md），
// 後端刻意分兩支 API（判讀邏輯未來可能各自演進），App 端共用同一套 DTO

// GET v3/companion/wish_list／GET v3/companion/history 的回應（目前為假資料端點，同一套商品 schema）
@Serializable
data class CompanionProductListResponse(
    @SerialName("prods") val prods: List<CompanionProductResponse> = emptyList()
)

@Serializable
data class CompanionProductResponse(
    // 商品編號，整條「商品對映鏈路」的 key（回應會告訴你每個城市對應哪些 prod_id，
    // 再帶進 travel-guide 要求排入行程）
    @SerialName("prod_mid") val prodMid: Long? = null,
    @SerialName("name") val name: String = "",
    @SerialName("introduction") val introduction: String? = null,
    @SerialName("destinations") val destinations: List<CompanionProductDestinationResponse> = emptyList()
)

@Serializable
data class CompanionProductDestinationResponse(
    @SerialName("name") val name: String = ""
)

// travel-summary-from-wish／-from-history 與 travel-guide 的 products[] 共用材料（每筆萃取 4 個欄位）
@Serializable
data class TripPlanProductRequest(
    // 必填，商品編號（缺少會被 400），字串型別
    @SerialName("prod_id") val prodId: String,
    @SerialName("prod_name") val prodName: String,
    // 建議截斷 ≤500 字（後端欄位上限，超過會被 400 擋掉）
    @SerialName("introduction") val introduction: String? = null,
    // destinations[].name，常是景點/地標而非城市（預期行為，LLM 會自己從景點推斷城市）
    @SerialName("destination_names") val destinationNames: List<String>? = null,
)

@Serializable
data class TravelSummaryFromProductsRequest(
    // 1~20 筆（超過會被 400 擋掉）
    @SerialName("products") val products: List<TripPlanProductRequest>,
    @SerialName("companion_name") val companionName: String? = null,
    @SerialName("personality") val personality: String? = null,
    @SerialName("speech_style") val speechStyle: String? = null,
)

@Serializable
data class TravelSummaryFromProductsDataResponse(
    @SerialName("greeting") val greeting: String = "",
    // 2026-08 改版：每個城市附上「是哪幾筆商品推導出這個城市」（prod_id 由後端依位置索引取回，
    // 不會出現模型捏造的編號）；軟失敗時為空陣列
    @SerialName("cities") val cities: List<CityProductsResponse> = emptyList(),
    @SerialName("ai_model") val aiModel: String? = null,
    @SerialName("fail_reason") val failReason: String? = null,
)

@Serializable
data class CityProductsResponse(
    @SerialName("city") val city: String = "",
    @SerialName("products") val products: List<CityProductRefResponse> = emptyList(),
)

@Serializable
data class CityProductRefResponse(
    @SerialName("prod_id") val prodId: String = "",
    @SerialName("prod_name") val prodName: String = "",
)

// ---------- POST /companion/recommend-city（Phase 2：城市推薦多輪對話）----------

@Serializable
data class RecommendCityRequest(
    // 完整對話歷史（1~20 則，無狀態設計每輪全量帶入），最後一則須為本次使用者輸入
    @SerialName("messages") val messages: List<RecommendCityMessageRequest>,
    // 需排除的城市（≤30 個）：本次對話中已被推薦過的城市，「換一個城市」時帶入避免重複推薦
    @SerialName("shown_cities") val shownCities: List<String>? = null,
    @SerialName("companion_name") val companionName: String? = null,
    @SerialName("personality") val personality: String? = null,
    @SerialName("speech_style") val speechStyle: String? = null
)

@Serializable
data class RecommendCityMessageRequest(
    // user / assistant
    @SerialName("role") val role: String,
    @SerialName("content") val content: String
)

@Serializable
data class RecommendCityDataResponse(
    @SerialName("reply") val reply: String = "",
    @SerialName("quick_replies") val quickReplies: List<String> = emptyList(),
    // 空字串 = 還在收斂中；有值時搭配 is_final=true
    @SerialName("recommended_city") val recommendedCity: String = "",
    @SerialName("city_reason") val cityReason: String = "",
    @SerialName("is_final") val isFinal: Boolean = false,
    @SerialName("off_topic") val offTopic: Boolean = false,
    @SerialName("round") val round: Int = 0,
    @SerialName("max_rounds") val maxRounds: Int = 0,
    @SerialName("remaining_rounds") val remainingRounds: Int = 0,
    // true = 換城超限（shown_cities 超過 5 個的保底）：未打 LLM，reply 為固定文案、chips 只剩「重新聊聊」
    @SerialName("swap_limit_reached") val swapLimitReached: Boolean = false,
    @SerialName("ai_model") val aiModel: String? = null,
    @SerialName("fail_reason") val failReason: String? = null
)

// ---------- POST /companion/travel-guide（Phase 2：產生結構化行程）----------

@Serializable
data class TravelGuideRequest(
    @SerialName("summary") val summary: String,
    @SerialName("city") val city: String = "",
    // key/value 不固定的偏好 map（Q1~Q6 選項顯示文字），後端不驗證固定 key
    @SerialName("preferences") val preferences: Map<String, String> = emptyMap(),
    // 2026-08 改版：選填 ≤3 筆已預訂訂單，帶了就必須排入行程（item 回填 oid、該天 booked_anchor 有值）
    @SerialName("orders") val orders: List<TripPlanOrderRequest>? = null,
    // 2026-08 改版（wish/history 鏈路）：選填 ≤10 筆願望清單/瀏覽紀錄商品，帶了就必須排入行程
    // （item 回填 prod_id；不影響 booked_anchor——那是 orders/oid 專屬）
    @SerialName("products") val products: List<TripPlanProductRequest>? = null,
    @SerialName("companion_name") val companionName: String? = null,
    @SerialName("personality") val personality: String? = null,
    @SerialName("speech_style") val speechStyle: String? = null
)

// travel-guide／travel-revise 共用：要排入行程的已預訂訂單（oid 取 orders API 的訂單 id）
@Serializable
data class TripPlanOrderRequest(
    @SerialName("oid") val oid: String,
    @SerialName("prod_name") val prodName: String,
    @SerialName("package_name") val packageName: String? = null,
    @SerialName("destination_name") val destinationName: String? = null,
    // yyyy-MM-dd 出發日，決定排哪一天
    @SerialName("go_dt") val goDt: String? = null,
)

// 2026-08 改版：狀態機式 schema（city/phase/itinerary_patch），取代舊版 overview/days[].spots[]
@Serializable
data class TravelGuideDataResponse(
    // LLM 失敗（fail_reason 有值）時 city 為 null、itinerary_patch.days 為空
    @SerialName("city") val city: String? = null,
    @SerialName("days") val days: Int = 0,
    @SerialName("date_range") val dateRange: TravelGuideDateRangeResponse? = null,
    @SerialName("days_provisional") val daysProvisional: Boolean = true,
    // done = 所有天皆已排定；plan = 仍有 unplanned_days
    @SerialName("phase") val phase: String = "",
    @SerialName("unplanned_days") val unplannedDays: List<Int> = emptyList(),
    // 已停泊、之後需回收的欄位（目前只會出現「航班」）
    @SerialName("pending_fields") val pendingFields: List<String> = emptyList(),
    // 旅伴語氣的完成宣告（≤2 則），渲染成對話泡泡，不需 append 回任何歷史
    @SerialName("messages") val messages: List<TravelGuideMessageResponse> = emptyList(),
    @SerialName("itinerary_patch") val itineraryPatch: TravelGuideItineraryPatchResponse? = null,
    @SerialName("progress_label") val progressLabel: String = "",
    // 本設計固定 {mode:"hidden", items:[]}（一次性生成沒有下一輪），照收但不使用
    @SerialName("chips") val chips: TravelGuideChipsResponse? = null,
    // phase=done 時為 {type:"view_trip", label:"看看完整行程"}；仍有 unplanned_days 時為 null
    @SerialName("main_action") val mainAction: TravelGuideMainActionResponse? = null,
    @SerialName("ai_model") val aiModel: String? = null,
    @SerialName("fail_reason") val failReason: String? = null
)

@Serializable
data class TravelGuideDateRangeResponse(
    @SerialName("start") val start: String = "",
    @SerialName("end") val end: String = ""
)

@Serializable
data class TravelGuideMessageResponse(
    @SerialName("type") val type: String = "",
    @SerialName("text") val text: String = ""
)

@Serializable
data class TravelGuideChipsResponse(
    @SerialName("mode") val mode: String = "",
    @SerialName("items") val items: List<String> = emptyList()
)

@Serializable
data class TravelGuideMainActionResponse(
    @SerialName("type") val type: String = "",
    @SerialName("label") val label: String = ""
)

@Serializable
data class TravelGuideItineraryPatchResponse(
    // 固定 "full"（一次性生成，沒有前次狀態可 diff）
    @SerialName("mode") val mode: String = "",
    @SerialName("changed_days") val changedDays: List<Int> = emptyList(),
    @SerialName("days") val days: List<TravelGuideDayResponse> = emptyList()
)

@Serializable
data class TravelGuideDayResponse(
    @SerialName("day") val day: Int = 0,
    // planned / discussing / unplanned（一次性生成只會出現 planned / unplanned）
    @SerialName("status") val status: String = "",
    // arrival / normal / departure
    @SerialName("kind") val kind: String = "",
    @SerialName("half_day") val halfDay: Boolean = false,
    // 2026-08 改版：該天含已預訂項目時為 {"oids":[...]}，由後端依合法 oid 推導；沒帶 orders 時恆為 null
    @SerialName("booked_anchor") val bookedAnchor: BookedAnchorResponse? = null,
    @SerialName("items") val items: List<TravelGuideItemResponse> = emptyList()
)

@Serializable
data class BookedAnchorResponse(
    @SerialName("oids") val oids: List<String> = emptyList()
)

@Serializable
data class TravelGuideItemResponse(
    // 標題：地點/活動的簡短名稱（spot/meal 必填，logistics 選填）；後端尚未補這支欄位前為 null，
    // App 端退回用 text 當標題顯示（見 TravelGuideDayItem.displayTitle）
    @SerialName("name") val name: String? = null,
    // 副標：一句話描述/行為（原本常混雜地點名稱，待後端依 name/text 分離後才是純描述）
    @SerialName("text") val text: String = "",
    // spot（景點/活動）／logistics（交通/後勤）／meal（用餐）
    @SerialName("type") val type: String = "",
    // 精確時間 "HH:MM"（24 小時制）；後端尚未補這支欄位前為 null，App 端退回用 time_band
    @SerialName("time") val time: String? = null,
    @SerialName("time_band") val timeBand: String? = null,
    // 僅 type=logistics 可能有值：walk/bus/train/car/subway/ferry/flight，App 端依此選交通 icon
    @SerialName("transport_mode") val transportMode: String? = null,
    @SerialName("note") val note: String? = null,
    // 僅 type=spot 才有值；LLM 推算的近似座標，僅供地圖大致標點，不可用於導航
    @SerialName("lat") val lat: Double? = null,
    @SerialName("lng") val lng: Double? = null,
    // 2026-08 改版：該 item 對應的已預訂訂單編號，僅請求有帶 orders 且 oid 通過後端白名單時有值
    @SerialName("oid") val oid: String? = null,
    // 2026-08 改版（wish/history 鏈路）：該 item 對應的願望清單/瀏覽紀錄商品編號（想去但還沒買，
    // 與 oid「已預訂」是兩條不同的軸，UI 不可混用）
    @SerialName("prod_id") val prodId: String? = null
)

// ---------- POST /companion/travel-revise（Phase 2：修改既有行程）----------

@Serializable
data class TravelReviseRequest(
    @SerialName("city") val city: String? = null,
    // 選填：優先只動這一天；未帶 = 整份可動
    @SerialName("target_day") val targetDay: Int? = null,
    // 必填：App 本地保存的「當前最新」完整行程（扁平陣列，不包 {"days":[...]}；
    // 連續修改帶「上一次回應的 itinerary_patch.days」原封不動送回）
    @SerialName("itinerary") val itinerary: List<TravelGuideDayResponse>,
    // 2026-08 改版（二）：request 欄位整個移除，改成 messages 必填，
    // 這次的修改需求＝messages 最後一則 role=user 的 content（同 recommend-city 模式，
    // 但這裡要放「整個聊天室從頭到尾」的完整對話紀錄，不是只有這次 bottom sheet 的小段落，上限 100 則）
    @SerialName("messages") val messages: List<RecommendCityMessageRequest>,
    // 選填：原問卷偏好，讓修改維持整體風格
    @SerialName("preferences") val preferences: Map<String, String>? = null,
    @SerialName("companion_name") val companionName: String? = null,
    @SerialName("personality") val personality: String? = null,
    @SerialName("speech_style") val speechStyle: String? = null
)

@Serializable
data class TravelReviseDataResponse(
    // 旅伴語氣的修改結果宣告（≤60 字）
    @SerialName("reply") val reply: String = "",
    // 2026-08 改版：mode 恆為 "full"，days 永遠是「這次修改後的完整行程」（整包替換即可）；
    // 離題/失敗時 days 不會是空的——後端原樣回傳輸入行程（正規化過，內容不變）
    @SerialName("itinerary_patch") val itineraryPatch: TravelGuideItineraryPatchResponse? = null,
    // 文件規格為陣列，SIT 實測後端回單一字串——用 JsonElement 收，mapping 時兩種形狀都容錯
    // （Gson JsonElement → kotlinx.serialization.json.JsonElement，僅 JVM 的 Gson 型別在 iOS target 無法編譯）
    @SerialName("changed_summary") val changedSummary: JsonElement? = null,
    @SerialName("off_topic") val offTopic: Boolean = false,
    @SerialName("ai_model") val aiModel: String? = null,
    @SerialName("fail_reason") val failReason: String? = null
)

// ---------- GET /companion/quiz-gallery ----------

@Serializable
data class QuizGalleryDataResponse(
    @SerialName("count") val count: Int = 0,
    @SerialName("items") val items: List<QuizGalleryItemResponse> = emptyList()
)

@Serializable
data class QuizGalleryItemResponse(
    @SerialName("travel_identity") val travelIdentity: String = "",
    @SerialName("travel_identity_en") val travelIdentityEn: String = "",
    @SerialName("destination_cn") val destinationCn: String = "",
    @SerialName("destination_en") val destinationEn: String = "",
    @SerialName("destination_country") val destinationCountry: String = "",
    @SerialName("tagline") val tagline: String = "",
    @SerialName("highlight_tags") val highlightTags: List<String> = emptyList(),
    @SerialName("companion_quote") val companionQuote: String = "",
    @SerialName("share_image_url") val shareImageUrl: String? = null,
    @SerialName("companion_name") val companionName: String? = null,
    // 該次測驗當時捏好的旅伴頭像 URL；可能為 null（舊資料、或當時沒傳），App 端需準備預設頭像 fallback
    @SerialName("partner_avatar_url") val partnerAvatarUrl: String? = null,
    @SerialName("created_at") val createdAt: String = ""
)

// ---------- POST /companion/share-image-v2 ----------
// T19：後端已產好合成圖，App 只顯示 hero_url，不做 Bitmap 疊字/輪詢（見 migration/01-decisions.md #6 後續變更）。
// content/decorations 對應欄位刻意不宣告——ignoreUnknownKeys=true 會自動忽略，這裡只取顯示 hero 需要的最小集合。

@Serializable
data class ShareImageV2Request(
    @SerialName("completion_uuid") val completionUuid: String,
    // 使用者當時捏的旅伴頭像 URL，供後端合成海報參考；demo 目前一律不傳
    @SerialName("partner_image_url") val partnerImageUrl: String? = null
)

@Serializable
data class ShareImageV2DataResponse(
    // 實測恆為同步回 "ready"（見 scratchpad/sdd/share-image-v2-response.json），不需輪詢
    @SerialName("status") val status: String = "",
    @SerialName("hero_url") val heroUrl: String? = null,
    @SerialName("fail_reason") val failReason: String? = null
)
