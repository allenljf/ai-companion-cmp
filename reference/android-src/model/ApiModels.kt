package com.kkday.model.companion

import com.google.gson.annotations.SerializedName

// ---------- Requests ----------

data class QuizRequest(
    @SerializedName("shown_question_counts") val shownQuestionCounts: Map<String, Int> = emptyMap(),
    // API 規格：personality 為單一字串（App 端限定選 1 個）
    @SerializedName("personality") val personality: String,
    @SerializedName("speech_style") val speechStyle: String
)

data class QuizCompletionRequest(
    @SerializedName("completion_uuid") val completionUuid: String,
    // API 規格：personality 為單一字串（App 端限定選 1 個）
    @SerializedName("personality") val personality: String,
    @SerializedName("speech_style") val speechStyle: String,
    @SerializedName("selected_tags") val selectedTags: List<String>,
    @SerializedName("shown_cities") val shownCities: List<String> = emptyList(),
    @SerializedName("companion_name") val companionName: String? = null,
    // 使用者當時捏好的旅伴頭像 URL，供 quiz-gallery 清單顯示頭像；未捏頭像時不帶
    @SerializedName("partner_avatar_url") val partnerAvatarUrl: String? = null
)

data class ShareImageV2Request(
    @SerializedName("completion_uuid") val completionUuid: String,
    // 使用者當時捏好的旅伴頭像 URL，供後端合成海報時使用；未捏頭像時可為 null
    @SerializedName("partner_image_url") val partnerImageUrl: String? = null
)

data class SelfIntroductionRequest(
    @SerializedName("companion_name") val companionName: String,
    // API 規格：personality 為單一字串（App 端限定選 1 個）
    @SerializedName("personality") val personality: String,
    @SerializedName("speech_style") val speechStyle: String,
    @SerializedName("gender") val gender: String
)

// ---------- GET /companion/ai-partner ----------

data class AiPartnerDataResponse(
    @SerializedName("personality") val personality: List<PersonalityOptionResponse> = emptyList(),
    @SerializedName("speech_style") val speechStyle: List<SpeechStyleOptionResponse> = emptyList(),
    @SerializedName("gender") val gender: List<AppearanceOptionResponse> = emptyList(),
    @SerializedName("outfit") val outfit: List<AppearanceOptionResponse> = emptyList(),
    @SerializedName("hair_style") val hairStyle: List<AppearanceOptionResponse> = emptyList(),
    @SerializedName("hair_color") val hairColor: List<AppearanceOptionResponse> = emptyList(),
    // key = "{gender}_{outfit}_{hair_style}_{hair_color}" → 頭像圖片 URL
    @SerializedName("avatars") val avatars: Map<String, String> = emptyMap()
)

data class PersonalityOptionResponse(
    @SerializedName("tag") val tag: String = "",
    @SerializedName("label") val label: String = "",
    @SerializedName("description") val description: String? = null
)

data class SpeechStyleOptionResponse(
    @SerializedName("tag") val tag: String = "",
    @SerializedName("label") val label: String = "",
    @SerializedName("description") val description: String? = null
)

data class AppearanceOptionResponse(
    @SerializedName("tag") val tag: String = "",
    @SerializedName("label") val label: String = ""
)

// ---------- POST /companion/quiz ----------

data class QuizDataResponse(
    @SerializedName("count") val count: Int = 0,
    @SerializedName("ai_model") val aiModel: String? = null,
    @SerializedName("fail_reason") val failReason: String? = null,
    @SerializedName("questions") val questions: List<QuizQuestionResponse> = emptyList()
)

data class QuizQuestionResponse(
    @SerializedName("id") val id: String = "",
    @SerializedName("dimension_id") val dimensionId: Int = 0,
    @SerializedName("index") val index: Int = 0,
    @SerializedName("type") val type: String = "",
    @SerializedName("mode") val mode: String = "",
    @SerializedName("text") val text: String = "",
    @SerializedName("options") val options: List<QuizOptionResponse> = emptyList()
)

data class QuizOptionResponse(
    @SerializedName("id") val id: String = "",
    @SerializedName("index") val index: Int = 0,
    @SerializedName("text") val text: String = "",
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("tag") val tag: QuizOptionTagResponse? = null
)

data class QuizOptionTagResponse(
    @SerializedName("id") val id: String = "",
    @SerializedName("label") val label: String = ""
)

// ---------- POST /companion/quiz-completions ----------

data class QuizCompletionDataResponse(
    @SerializedName("quiz_completion_id") val quizCompletionId: Long = 0,
    @SerializedName("travel_identity") val travelIdentity: String = "",
    @SerializedName("travel_identity_en") val travelIdentityEn: String = "",
    @SerializedName("destination_cn") val destinationCn: String = "",
    @SerializedName("destination_en") val destinationEn: String = "",
    @SerializedName("destination_country") val destinationCountry: String = "",
    @SerializedName("destination_country_en") val destinationCountryEn: String = "",
    @SerializedName("tagline") val tagline: String = "",
    @SerializedName("tagline_en") val taglineEn: String = "",
    @SerializedName("highlight_tags") val highlightTags: List<String> = emptyList(),
    @SerializedName("highlight_tags_en") val highlightTagsEn: List<String> = emptyList(),
    @SerializedName("companion_quote") val companionQuote: String = "",
    @SerializedName("companion_quote_en") val companionQuoteEn: String = "",
    // 旅伴口吻推薦理由，固定 3 段、App 逐段分開顯示（後端相容舊版單字串：會自動包成單元素陣列）
    @SerializedName("recommendation") val recommendation: List<String> = emptyList(),
    // 產圖等待畫面用的 AI 思考過程逐句陣列（≥10 句、每句 ≤20 字）；LLM 失敗時為空陣列
    @SerializedName("reasoning") val reasoning: List<String> = emptyList(),
    @SerializedName("social_post") val socialPost: String = "",
    @SerializedName("share_image_status") val shareImageStatus: String = "",
    @SerializedName("ai_model") val aiModel: String? = null,
    @SerializedName("share_image_ai_model") val shareImageAiModel: String? = null,
    @SerializedName("fail_reason") val failReason: String? = null
)

// ---------- POST /companion/self-introduction ----------

data class SelfIntroductionDataResponse(
    @SerializedName("introduction") val introduction: String = "",
    @SerializedName("ai_model") val aiModel: String? = null,
    @SerializedName("fail_reason") val failReason: String? = null
)

// ---------- POST /companion/travel-summary（Phase 2：聊天室初始化摘要）----------

data class TravelSummaryRequest(
    // quiz_completion / from_orders / imported_itinerary / from_zero
    @SerializedName("entry_type") val entryType: String,
    // 2026-08 改版：quiz_completion／from_orders 入口必填（缺少回 400），回傳 city 一律原樣用此值（權威、不經 LLM）
    @SerializedName("city") val city: String? = null,
    // from_orders 入口選填：被點選那筆訂單的材料，開場白會呼應訂購商品
    @SerializedName("order") val order: TravelSummaryOrderRequest? = null,
    @SerializedName("city_image_url") val cityImageUrl: String? = null,
    @SerializedName("intro_text") val introText: String? = null,
    // text / image
    @SerializedName("source_type") val sourceType: String? = null,
    @SerializedName("content") val content: String? = null,
    // 目前後端無圖片上傳端點可取得外部可讀 URL，image 路徑暫未開放（見 phase2 API 文件未定案事項 1）
    @SerializedName("image_urls") val imageUrls: List<String>? = null,
    @SerializedName("note") val note: String? = null,
    // 「資訊不夠再補充」重複呼叫時帶上前次回應的 summary
    @SerializedName("previous_summary") val previousSummary: String? = null,
    @SerializedName("companion_name") val companionName: String? = null,
    @SerializedName("personality") val personality: String? = null,
    @SerializedName("speech_style") val speechStyle: String? = null
)

// from_orders 入口帶入的訂單材料（開場白呼應訂購商品用）
data class TravelSummaryOrderRequest(
    @SerializedName("prod_name") val prodName: String,
    @SerializedName("package_name") val packageName: String? = null,
    // yyyy-MM-dd 出發日
    @SerializedName("go_dt") val goDt: String? = null,
)

data class TravelSummaryDataResponse(
    @SerializedName("summary") val summary: String = "",
    @SerializedName("city") val city: String = "",
    @SerializedName("ai_model") val aiModel: String? = null,
    @SerializedName("fail_reason") val failReason: String? = null
)

// ---------- POST /companion/travel-summary-from-orders（Phase 2：帶訂單開場，依訂單材料判斷目的地選項）----------
// 後端尚未實作，請見隨附的後端 handoff prompt

data class TripOrderMaterialRequest(
    @SerializedName("prod_name") val prodName: String,
    @SerializedName("package_name") val packageName: String,
    @SerializedName("destination_name") val destinationName: String,
)

data class TravelSummaryFromOrdersRequest(
    // 最多 3 筆，依訂單出發日由近到遠排序
    @SerializedName("orders") val orders: List<TripOrderMaterialRequest>,
    @SerializedName("companion_name") val companionName: String? = null,
    @SerializedName("personality") val personality: String? = null,
    @SerializedName("speech_style") val speechStyle: String? = null,
)

data class TravelDestinationOptionResponse(
    // 對應送出的 orders 清單索引（0-based），用於選定後取回該筆訂單材料
    @SerializedName("order_index") val orderIndex: Int,
    @SerializedName("city") val city: String,
)

data class TravelSummaryFromOrdersDataResponse(
    @SerializedName("greeting") val greeting: String = "",
    @SerializedName("options") val options: List<TravelDestinationOptionResponse> = emptyList(),
    @SerializedName("ai_model") val aiModel: String? = null,
    @SerializedName("fail_reason") val failReason: String? = null,
)

// ---------- GET wish_list / GET history + POST travel-summary-from-wish / -from-history ----------
// 願望清單／瀏覽紀錄開場：兩條流程完全對稱（見 ai-companion-wish-history-integration.md），
// 後端刻意分兩支 API（判讀邏輯未來可能各自演進），App 端共用同一套 DTO

// GET v3/companion/wish_list／GET v3/companion/history 的回應（目前為假資料端點，同一套商品 schema）
data class CompanionProductListResponse(
    @SerializedName("prods") val prods: List<CompanionProductResponse> = emptyList()
)

data class CompanionProductResponse(
    // 商品編號，整條「商品對映鏈路」的 key（回應會告訴你每個城市對應哪些 prod_id，
    // 再帶進 travel-guide 要求排入行程）
    @SerializedName("prod_mid") val prodMid: Long? = null,
    @SerializedName("name") val name: String = "",
    @SerializedName("introduction") val introduction: String? = null,
    @SerializedName("destinations") val destinations: List<CompanionProductDestinationResponse> = emptyList()
)

data class CompanionProductDestinationResponse(
    @SerializedName("name") val name: String = ""
)

// travel-summary-from-wish／-from-history 與 travel-guide 的 products[] 共用材料（每筆萃取 4 個欄位）
data class TripPlanProductRequest(
    // 必填，商品編號（缺少會被 400），字串型別
    @SerializedName("prod_id") val prodId: String,
    @SerializedName("prod_name") val prodName: String,
    // 建議截斷 ≤500 字（後端欄位上限，超過會被 400 擋掉）
    @SerializedName("introduction") val introduction: String? = null,
    // destinations[].name，常是景點/地標而非城市（預期行為，LLM 會自己從景點推斷城市）
    @SerializedName("destination_names") val destinationNames: List<String>? = null,
)

data class TravelSummaryFromProductsRequest(
    // 1~20 筆（超過會被 400 擋掉）
    @SerializedName("products") val products: List<TripPlanProductRequest>,
    @SerializedName("companion_name") val companionName: String? = null,
    @SerializedName("personality") val personality: String? = null,
    @SerializedName("speech_style") val speechStyle: String? = null,
)

data class TravelSummaryFromProductsDataResponse(
    @SerializedName("greeting") val greeting: String = "",
    // 2026-08 改版：每個城市附上「是哪幾筆商品推導出這個城市」（prod_id 由後端依位置索引取回，
    // 不會出現模型捏造的編號）；軟失敗時為空陣列
    @SerializedName("cities") val cities: List<CityProductsResponse> = emptyList(),
    @SerializedName("ai_model") val aiModel: String? = null,
    @SerializedName("fail_reason") val failReason: String? = null,
)

data class CityProductsResponse(
    @SerializedName("city") val city: String = "",
    @SerializedName("products") val products: List<CityProductRefResponse> = emptyList(),
)

data class CityProductRefResponse(
    @SerializedName("prod_id") val prodId: String = "",
    @SerializedName("prod_name") val prodName: String = "",
)

// ---------- POST /companion/recommend-city（Phase 2：城市推薦多輪對話）----------

data class RecommendCityRequest(
    // 完整對話歷史（1~20 則，無狀態設計每輪全量帶入），最後一則須為本次使用者輸入
    @SerializedName("messages") val messages: List<RecommendCityMessageRequest>,
    // 需排除的城市（≤30 個）：本次對話中已被推薦過的城市，「換一個城市」時帶入避免重複推薦
    @SerializedName("shown_cities") val shownCities: List<String>? = null,
    @SerializedName("companion_name") val companionName: String? = null,
    @SerializedName("personality") val personality: String? = null,
    @SerializedName("speech_style") val speechStyle: String? = null
)

data class RecommendCityMessageRequest(
    // user / assistant
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String
)

data class RecommendCityDataResponse(
    @SerializedName("reply") val reply: String = "",
    @SerializedName("quick_replies") val quickReplies: List<String> = emptyList(),
    // 空字串 = 還在收斂中；有值時搭配 is_final=true
    @SerializedName("recommended_city") val recommendedCity: String = "",
    @SerializedName("city_reason") val cityReason: String = "",
    @SerializedName("is_final") val isFinal: Boolean = false,
    @SerializedName("off_topic") val offTopic: Boolean = false,
    @SerializedName("round") val round: Int = 0,
    @SerializedName("max_rounds") val maxRounds: Int = 0,
    @SerializedName("remaining_rounds") val remainingRounds: Int = 0,
    // true = 換城超限（shown_cities 超過 5 個的保底）：未打 LLM，reply 為固定文案、chips 只剩「重新聊聊」
    @SerializedName("swap_limit_reached") val swapLimitReached: Boolean = false,
    @SerializedName("ai_model") val aiModel: String? = null,
    @SerializedName("fail_reason") val failReason: String? = null
)

// ---------- POST /companion/travel-guide（Phase 2：產生結構化行程）----------

data class TravelGuideRequest(
    @SerializedName("summary") val summary: String,
    @SerializedName("city") val city: String = "",
    // key/value 不固定的偏好 map（Q1~Q6 選項顯示文字），後端不驗證固定 key
    @SerializedName("preferences") val preferences: Map<String, String> = emptyMap(),
    // 2026-08 改版：選填 ≤3 筆已預訂訂單，帶了就必須排入行程（item 回填 oid、該天 booked_anchor 有值）
    @SerializedName("orders") val orders: List<TripPlanOrderRequest>? = null,
    // 2026-08 改版（wish/history 鏈路）：選填 ≤10 筆願望清單/瀏覽紀錄商品，帶了就必須排入行程
    // （item 回填 prod_id；不影響 booked_anchor——那是 orders/oid 專屬）
    @SerializedName("products") val products: List<TripPlanProductRequest>? = null,
    @SerializedName("companion_name") val companionName: String? = null,
    @SerializedName("personality") val personality: String? = null,
    @SerializedName("speech_style") val speechStyle: String? = null
)

// travel-guide／travel-revise 共用：要排入行程的已預訂訂單（oid 取 orders API 的訂單 id）
data class TripPlanOrderRequest(
    @SerializedName("oid") val oid: String,
    @SerializedName("prod_name") val prodName: String,
    @SerializedName("package_name") val packageName: String? = null,
    @SerializedName("destination_name") val destinationName: String? = null,
    // yyyy-MM-dd 出發日，決定排哪一天
    @SerializedName("go_dt") val goDt: String? = null,
)

// 2026-08 改版：狀態機式 schema（city/phase/itinerary_patch），取代舊版 overview/days[].spots[]
data class TravelGuideDataResponse(
    // LLM 失敗（fail_reason 有值）時 city 為 null、itinerary_patch.days 為空
    @SerializedName("city") val city: String? = null,
    @SerializedName("days") val days: Int = 0,
    @SerializedName("date_range") val dateRange: TravelGuideDateRangeResponse? = null,
    @SerializedName("days_provisional") val daysProvisional: Boolean = true,
    // done = 所有天皆已排定；plan = 仍有 unplanned_days
    @SerializedName("phase") val phase: String = "",
    @SerializedName("unplanned_days") val unplannedDays: List<Int> = emptyList(),
    // 已停泊、之後需回收的欄位（目前只會出現「航班」）
    @SerializedName("pending_fields") val pendingFields: List<String> = emptyList(),
    // 旅伴語氣的完成宣告（≤2 則），渲染成對話泡泡，不需 append 回任何歷史
    @SerializedName("messages") val messages: List<TravelGuideMessageResponse> = emptyList(),
    @SerializedName("itinerary_patch") val itineraryPatch: TravelGuideItineraryPatchResponse? = null,
    @SerializedName("progress_label") val progressLabel: String = "",
    // 本設計固定 {mode:"hidden", items:[]}（一次性生成沒有下一輪），照收但不使用
    @SerializedName("chips") val chips: TravelGuideChipsResponse? = null,
    // phase=done 時為 {type:"view_trip", label:"看看完整行程"}；仍有 unplanned_days 時為 null
    @SerializedName("main_action") val mainAction: TravelGuideMainActionResponse? = null,
    @SerializedName("ai_model") val aiModel: String? = null,
    @SerializedName("fail_reason") val failReason: String? = null
)

data class TravelGuideDateRangeResponse(
    @SerializedName("start") val start: String = "",
    @SerializedName("end") val end: String = ""
)

data class TravelGuideMessageResponse(
    @SerializedName("type") val type: String = "",
    @SerializedName("text") val text: String = ""
)

data class TravelGuideChipsResponse(
    @SerializedName("mode") val mode: String = "",
    @SerializedName("items") val items: List<String> = emptyList()
)

data class TravelGuideMainActionResponse(
    @SerializedName("type") val type: String = "",
    @SerializedName("label") val label: String = ""
)

data class TravelGuideItineraryPatchResponse(
    // 固定 "full"（一次性生成，沒有前次狀態可 diff）
    @SerializedName("mode") val mode: String = "",
    @SerializedName("changed_days") val changedDays: List<Int> = emptyList(),
    @SerializedName("days") val days: List<TravelGuideDayResponse> = emptyList()
)

data class TravelGuideDayResponse(
    @SerializedName("day") val day: Int = 0,
    // planned / discussing / unplanned（一次性生成只會出現 planned / unplanned）
    @SerializedName("status") val status: String = "",
    // arrival / normal / departure
    @SerializedName("kind") val kind: String = "",
    @SerializedName("half_day") val halfDay: Boolean = false,
    // 2026-08 改版：該天含已預訂項目時為 {"oids":[...]}，由後端依合法 oid 推導；沒帶 orders 時恆為 null
    @SerializedName("booked_anchor") val bookedAnchor: BookedAnchorResponse? = null,
    @SerializedName("items") val items: List<TravelGuideItemResponse> = emptyList()
)

data class BookedAnchorResponse(
    @SerializedName("oids") val oids: List<String> = emptyList()
)

data class TravelGuideItemResponse(
    // 標題：地點/活動的簡短名稱（spot/meal 必填，logistics 選填）；後端尚未補這支欄位前為 null，
    // App 端退回用 text 當標題顯示（見 TravelGuideDayItem.displayTitle）
    @SerializedName("name") val name: String? = null,
    // 副標：一句話描述/行為（原本常混雜地點名稱，待後端依 name/text 分離後才是純描述）
    @SerializedName("text") val text: String = "",
    // spot（景點/活動）／logistics（交通/後勤）／meal（用餐）
    @SerializedName("type") val type: String = "",
    // 精確時間 "HH:MM"（24 小時制）；後端尚未補這支欄位前為 null，App 端退回用 time_band
    @SerializedName("time") val time: String? = null,
    @SerializedName("time_band") val timeBand: String? = null,
    // 僅 type=logistics 可能有值：walk/bus/train/car/subway/ferry/flight，App 端依此選交通 icon
    @SerializedName("transport_mode") val transportMode: String? = null,
    @SerializedName("note") val note: String? = null,
    // 僅 type=spot 才有值；LLM 推算的近似座標，僅供地圖大致標點，不可用於導航
    @SerializedName("lat") val lat: Double? = null,
    @SerializedName("lng") val lng: Double? = null,
    // 2026-08 改版：該 item 對應的已預訂訂單編號，僅請求有帶 orders 且 oid 通過後端白名單時有值
    @SerializedName("oid") val oid: String? = null,
    // 2026-08 改版（wish/history 鏈路）：該 item 對應的願望清單/瀏覽紀錄商品編號（想去但還沒買，
    // 與 oid「已預訂」是兩條不同的軸，UI 不可混用）
    @SerializedName("prod_id") val prodId: String? = null
)

// ---------- POST /companion/travel-revise（Phase 2：修改既有行程）----------

data class TravelReviseRequest(
    @SerializedName("city") val city: String? = null,
    // 選填：優先只動這一天；未帶 = 整份可動
    @SerializedName("target_day") val targetDay: Int? = null,
    // 必填：App 本地保存的「當前最新」完整行程（扁平陣列，不包 {"days":[...]}；
    // 連續修改帶「上一次回應的 itinerary_patch.days」原封不動送回）
    @SerializedName("itinerary") val itinerary: List<TravelGuideDayResponse>,
    // 2026-08 改版（二）：request 欄位整個移除，改成 messages 必填，
    // 這次的修改需求＝messages 最後一則 role=user 的 content（同 recommend-city 模式，
    // 但這裡要放「整個聊天室從頭到尾」的完整對話紀錄，不是只有這次 bottom sheet 的小段落，上限 100 則）
    @SerializedName("messages") val messages: List<RecommendCityMessageRequest>,
    // 選填：原問卷偏好，讓修改維持整體風格
    @SerializedName("preferences") val preferences: Map<String, String>? = null,
    @SerializedName("companion_name") val companionName: String? = null,
    @SerializedName("personality") val personality: String? = null,
    @SerializedName("speech_style") val speechStyle: String? = null
)

data class TravelReviseDataResponse(
    // 旅伴語氣的修改結果宣告（≤60 字）
    @SerializedName("reply") val reply: String = "",
    // 2026-08 改版：mode 恆為 "full"，days 永遠是「這次修改後的完整行程」（整包替換即可）；
    // 離題/失敗時 days 不會是空的——後端原樣回傳輸入行程（正規化過，內容不變）
    @SerializedName("itinerary_patch") val itineraryPatch: TravelGuideItineraryPatchResponse? = null,
    // 文件規格為陣列，SIT 實測後端回單一字串——用 JsonElement 收，mapping 時兩種形狀都容錯
    @SerializedName("changed_summary") val changedSummary: com.google.gson.JsonElement? = null,
    @SerializedName("off_topic") val offTopic: Boolean = false,
    @SerializedName("ai_model") val aiModel: String? = null,
    @SerializedName("fail_reason") val failReason: String? = null
)

// ---------- GET /companion/quiz-gallery ----------

data class QuizGalleryDataResponse(
    @SerializedName("count") val count: Int = 0,
    @SerializedName("items") val items: List<QuizGalleryItemResponse> = emptyList()
)

data class QuizGalleryItemResponse(
    @SerializedName("travel_identity") val travelIdentity: String = "",
    @SerializedName("travel_identity_en") val travelIdentityEn: String = "",
    @SerializedName("destination_cn") val destinationCn: String = "",
    @SerializedName("destination_en") val destinationEn: String = "",
    @SerializedName("destination_country") val destinationCountry: String = "",
    @SerializedName("tagline") val tagline: String = "",
    @SerializedName("highlight_tags") val highlightTags: List<String> = emptyList(),
    @SerializedName("companion_quote") val companionQuote: String = "",
    @SerializedName("share_image_url") val shareImageUrl: String? = null,
    @SerializedName("companion_name") val companionName: String? = null,
    // 該次測驗當時捏好的旅伴頭像 URL；可能為 null（舊資料、或當時沒傳），App 端需準備預設頭像 fallback
    @SerializedName("partner_avatar_url") val partnerAvatarUrl: String? = null,
    @SerializedName("created_at") val createdAt: String = ""
)

// ---------- POST /companion/share-image-v2 ----------

data class ShareImageV2DataResponse(
    @SerializedName("status") val status: String = "",
    @SerializedName("hero_url") val heroUrl: String? = null,
    @SerializedName("hero_fallback_category") val heroFallbackCategory: String = "",
    @SerializedName("content") val content: ShareImageV2ContentResponse = ShareImageV2ContentResponse(),
    @SerializedName("decorations") val decorations: ShareImageV2DecorationsResponse = ShareImageV2DecorationsResponse(),
    @SerializedName("fail_reason") val failReason: String? = null
)

data class ShareImageV2ContentResponse(
    @SerializedName("travel_identity") val travelIdentity: String = "",
    @SerializedName("travel_identity_en") val travelIdentityEn: String = "",
    @SerializedName("destination_cn") val destinationCn: String = "",
    @SerializedName("destination_en") val destinationEn: String = "",
    @SerializedName("tagline") val tagline: String = "",
    @SerializedName("highlight_tags") val highlightTags: List<String> = emptyList(),
    @SerializedName("companion_quote") val companionQuote: String = "",
    @SerializedName("companion_name") val companionName: String? = null
)

data class ShareImageV2DecorationsResponse(
    @SerializedName("stamp_url") val stampUrl: String? = null,
    @SerializedName("stamp_fallback_category") val stampFallbackCategory: String = "",
    @SerializedName("tag_icon_urls") val tagIconUrls: List<String?> = emptyList(),
    @SerializedName("tag_fallback_categories") val tagFallbackCategories: List<String> = emptyList()
)
