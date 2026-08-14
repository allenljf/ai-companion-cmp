package com.allenljf.aicompanion.model

data class AiPartnerResult(
    val personality: List<CompanionTraitOption> = emptyList(),
    val speechStyle: List<CompanionTraitOption> = emptyList(),
    val gender: List<CompanionAppearanceOption> = emptyList(),
    val outfit: List<CompanionAppearanceOption> = emptyList(),
    val hairStyle: List<CompanionAppearanceOption> = emptyList(),
    val hairColor: List<CompanionAppearanceOption> = emptyList(),
    val avatars: Map<String, String> = emptyMap()
) {
    val isEmpty: Boolean
        get() = personality.isEmpty() && speechStyle.isEmpty() &&
            gender.isEmpty() && outfit.isEmpty() && hairStyle.isEmpty() && hairColor.isEmpty()

    /** 依四個外觀維度 tag 組 key（{gender}_{outfit}_{hair_style}_{hair_color}）取頭像 URL；任一維度未選回空字串。 */
    fun avatarUrl(gender: String?, outfit: String?, hairStyle: String?, hairColor: String?): String {
        if (gender.isNullOrEmpty() || outfit.isNullOrEmpty() ||
            hairStyle.isNullOrEmpty() || hairColor.isNullOrEmpty()
        ) return ""
        return avatars["${gender}_${outfit}_${hairStyle}_${hairColor}"].orEmpty()
    }
}

data class CompanionTraitOption(
    val tag: String,
    val label: String,
    val description: String = ""
)

data class CompanionAppearanceOption(
    val tag: String,
    val label: String
)

data class QuizResult(
    val count: Int = 0,
    val failReason: String? = null,
    val questions: List<QuizQuestion> = emptyList()
) {
    // fail_reason 有值 = LLM 改寫失敗、questions 為原始文案，仍照常顯示
    val isRewriteFallback: Boolean get() = !failReason.isNullOrEmpty()
}

data class QuizQuestion(
    val id: String,
    val dimensionId: Int,
    val index: Int,
    val type: String,
    val mode: String,
    val text: String,
    val options: List<QuizOption>
)

data class QuizOption(
    val id: String,
    val index: Int,
    val text: String,
    val imageUrl: String = "",
    val tagId: String = "",
    val tagLabel: String = ""
)

data class QuizCompletionResult(
    val quizCompletionId: Long = 0,
    val travelIdentity: String = "",
    val travelIdentityEn: String = "",
    val destinationCn: String = "",
    val destinationEn: String = "",
    val destinationCountry: String = "",
    val destinationCountryEn: String = "",
    val tagline: String = "",
    val taglineEn: String = "",
    val highlightTags: List<String> = emptyList(),
    val highlightTagsEn: List<String> = emptyList(),
    val companionQuote: String = "",
    val companionQuoteEn: String = "",
    // 旅伴口吻推薦理由：固定 3 段，UI 以對話框逐段顯示
    val recommendation: List<String> = emptyList(),
    // 產圖等待畫面用的 AI 思考過程逐句陣列；LLM 失敗時為空陣列，UI 需容忍
    val reasoning: List<String> = emptyList(),
    val socialPost: String = "",
    val shareImageStatus: String = "",
    val failReason: String? = null
) {
    // 成敗以 fail_reason 判斷（不是 HTTP / metadata.status）
    val isAnalysisSuccess: Boolean get() = failReason.isNullOrEmpty()

    // 靜態呈現（結果詳情/歷史回顧）用：分段以空行合併成單一文字
    val recommendationText: String get() = recommendation.joinToString("\n\n")

    // 分析成功且可接著呼叫 share-image 產圖
    val canGenerateShareImage: Boolean get() = isAnalysisSuccess && shareImageStatus == SHARE_IMAGE_STATUS_PENDING

    companion object {
        const val SHARE_IMAGE_STATUS_PENDING = "pending"
        const val SHARE_IMAGE_STATUS_SKIPPED = "skipped"
    }
}

data class QuizGalleryResult(
    val count: Int = 0,
    val items: List<QuizGalleryItem> = emptyList()
)

data class QuizGalleryItem(
    val travelIdentity: String = "",
    val travelIdentityEn: String = "",
    val destinationCn: String = "",
    val destinationEn: String = "",
    val destinationCountry: String = "",
    val tagline: String = "",
    val highlightTags: List<String> = emptyList(),
    val companionQuote: String = "",
    val shareImageUrl: String? = null,
    val companionName: String? = null,
    // 該次測驗當時的旅伴頭像 URL；null/空為正常狀態（舊資料或當時沒傳），UI 需以預設頭像 fallback
    val partnerAvatarUrl: String? = null,
    val createdAt: String = ""
) {
    // share_image_url 為 null 是正常狀態（尚未或未成功產過分享圖），不是錯誤
    val hasPoster: Boolean get() = !shareImageUrl.isNullOrBlank()
}

data class CompanionSnapshot(
    val name: String = "",
    val introduction: String = "",
    val personalityTags: List<String> = emptyList(),
    val personalityLabels: List<String> = emptyList(),
    val speechStyleTag: String = "",
    val speechStyleLabel: String = "",
    val avatarUrl: String = ""
)

/** 測驗結果歷史紀錄：完整結果 + 海報素材本機路徑（產圖完成後回填）+ 當下旅伴快照 + 建立時間。 */
data class QuizHistoryRecord(
    val result: QuizCompletionResult = QuizCompletionResult(),
    // share-image-v2：本地離屏合成好的完整海報 PNG（僅供列表縮圖使用），存在裝置本機檔案系統的絕對路徑（非 URL）
    val posterLocalPath: String = "",
    // 「旅行 DNA 回顧」詳情頁用：個別素材（未疊字）本機路徑，供詳情頁比照 ResultScreen 即時組出 Hero + 徽章文字
    val heroLocalPath: String = "",
    val stampLocalPath: String = "",
    val tagIconLocalPaths: List<String> = emptyList(),
    val companionSnapshot: CompanionSnapshot = CompanionSnapshot(),
    // 保留產圖當時的 completion_uuid：本機素材缺漏時可用同一組 uuid 補打 share-image-v2 拿圖
    val completionUuid: String = "",
    val createdAt: Long = 0L
)

data class SelfIntroductionResult(
    val introduction: String = "",
    val failReason: String? = null
) {
    // fail_reason 有值 = LLM 生成失敗、introduction 為後端固定文案（軟失敗，仍可直接顯示）
    val isAiGenerated: Boolean get() = failReason.isNullOrEmpty()
}

// ---------- Phase 2：行程規劃 ----------

object TravelSummaryEntryType {
    const val QUIZ_COMPLETION = "quiz_completion"
    const val IMPORTED_ITINERARY = "imported_itinerary"
    const val FROM_ZERO = "from_zero"
    // 2026-08 改版：帶訂單開場選定城市後的入口（city 必填、order 選填帶該筆訂單材料）
    const val FROM_ORDERS = "from_orders"
}

object TravelSummarySourceType {
    const val TEXT = "text"
    const val IMAGE = "image"
}

data class TravelSummaryResult(
    val summary: String = "",
    // LLM 判斷出的目的地城市，之後打 travel-guide 原樣帶入；判斷不出或 from_zero 為空字串
    val city: String = "",
    val failReason: String? = null
) {
    // fail_reason 有值 = LLM 軟失敗，summary 為後端兜底文案（仍可顯示），可原樣重打重新生成
    val isGenerateSuccess: Boolean get() = failReason.isNullOrEmpty()
}

/**
 * 「帶訂單開場」的訂單材料：取即將出發（v2.2/orders?category=ON-GOING）中最近的最多 3 筆訂單，
 * 每筆訂單取商品名稱／方案名稱／目的地名稱（destinations 取第一個）餵給後端 LLM 判斷目的地選項。
 */
data class TripOrderMaterial(
    val prodName: String,
    val packageName: String,
    val destinationName: String,
    // 訂單編號（orders API 的 id），travel-guide 排入行程時的 oid 對映用
    val oid: String = "",
    // yyyy-MM-dd 出發日，travel-guide 決定排哪一天
    val goDt: String = "",
)

/** LLM 依單一筆訂單材料判斷出的目的地選項；orderIndex 對應送出的 orders 清單索引，供選定後回頭取用該筆訂單材料。 */
data class TravelDestinationOption(
    val orderIndex: Int,
    val city: String,
)

data class TravelSummaryFromOrdersResult(
    val greeting: String = "",
    val options: List<TravelDestinationOption> = emptyList(),
    val failReason: String? = null,
) {
    val isGenerateSuccess: Boolean get() = failReason.isNullOrEmpty()
}

/**
 * 願望清單／瀏覽紀錄開場的商品材料（GET wish_list／GET history 的 data.prods[] 萃取），
 * 丟給 travel-summary-from-wish／-from-history 聚合判斷城市。
 */
data class TripProductMaterial(
    // 商品編號：整條商品對映鏈路的 key（from-wish/-history 必填，travel-guide 排入行程時對映 item.prod_id）
    val prodId: String,
    val prodName: String,
    val introduction: String = "",
    // 常是景點/地標而非城市，LLM 會自己推斷城市
    val destinationNames: List<String> = emptyList(),
)

/**
 * travel-summary-from-wish／-from-history 的聚合結果：開場白＋最多 3 個城市，
 * 每個城市附上「是哪幾筆商品推導出這個城市」（選定城市後帶進 travel-guide 的 products[]）。
 */
data class TravelSummaryFromProductsResult(
    val greeting: String = "",
    val cities: List<TripCityProducts> = emptyList(),
    val failReason: String? = null,
) {
    val isGenerateSuccess: Boolean get() = failReason.isNullOrEmpty()
}

data class TripCityProducts(
    val city: String = "",
    // prod_id 保證是送出去的值之一（後端依位置索引取回，不會有模型捏造的編號）
    val products: List<TripProductRef> = emptyList(),
)

data class TripProductRef(
    val prodId: String = "",
    val prodName: String = "",
)

/** recommend-city 的一則對話（無狀態設計：App 端保存完整歷史，每輪全量帶回）。 */
data class CityChatMessage(
    val role: String,
    val content: String
) {
    companion object {
        const val ROLE_USER = "user"
        const val ROLE_ASSISTANT = "assistant"

        fun user(content: String) = CityChatMessage(ROLE_USER, content)
        fun assistant(content: String) = CityChatMessage(ROLE_ASSISTANT, content)
    }
}

data class RecommendCityResult(
    val reply: String = "",
    val quickReplies: List<String> = emptyList(),
    val recommendedCity: String = "",
    val cityReason: String = "",
    // true = 已收斂，App 不應再呼叫；判斷收斂只看 isFinal，不要自己數輪次（LLM 可能提前收斂）
    val isFinal: Boolean = false,
    val offTopic: Boolean = false,
    val round: Int = 0,
    val maxRounds: Int = 0,
    val remainingRounds: Int = 0,
    // 換城超限保底（shown_cities 超過 5 個）：未打 LLM，reply 為固定文案，App 跳 dialog 引導重新聊聊
    val swapLimitReached: Boolean = false,
    val failReason: String? = null
) {
    // fail_reason 有值 = 兜底文案：照常渲染泡泡，但不要 append 進 messages 帶回（會污染上下文）
    val isGenerateSuccess: Boolean get() = failReason.isNullOrEmpty()
}

/** travel-guide（2026-08 狀態機式 schema）逐日行程的單一項目。 */
data class TravelGuideDayItem(
    // 標題：地點/活動簡短名稱；後端尚未提供時為空字串，displayTitle 會退回用 text
    val name: String = "",
    // 副標：一句話描述/行為
    val text: String = "",
    // spot（景點/活動）／logistics（交通/後勤）／meal（用餐）
    val type: String = "",
    // 精確時間 "HH:MM"；後端尚未提供時為空字串，displayTime 會退回用 timeBand
    val time: String = "",
    val timeBand: String = "",
    // 僅 type=logistics 可能有值：walk/bus/train/car/subway/ferry/flight
    val transportMode: String = "",
    val note: String = "",
    // 僅 type=spot 才有值；LLM 推算的近似座標，僅供地圖大致標點
    val lat: Double? = null,
    val lng: Double? = null,
    // 該 item 對應的已預訂訂單編號；有值 = 已預訂項目（LLM 不可刪除、不可改），UI 標「已預訂」
    val oid: String = "",
    // 該 item 對應的願望清單/瀏覽紀錄商品編號；有值 = 想去但還沒買（可刪），
    // UI 標「感興趣」之類，**絕對不可標成「已預訂」**（與 oid 是兩條不同的軸）
    val prodId: String = ""
) {
    val isSpot: Boolean get() = type == TYPE_SPOT
    val isLogistics: Boolean get() = type == TYPE_LOGISTICS
    val isMeal: Boolean get() = type == TYPE_MEAL
    val isBooked: Boolean get() = oid.isNotBlank()
    val isFromInterest: Boolean get() = prodId.isNotBlank()

    // 標題／副標分離：後端補上 name 前，標題退回用 text（維持現有畫面不破版）
    val displayTitle: String get() = name.ifBlank { text }
    val displaySubtitle: String get() = if (name.isBlank()) "" else text
    val displayTime: String get() = time.ifBlank { timeBand }

    companion object {
        const val TYPE_SPOT = "spot"
        const val TYPE_LOGISTICS = "logistics"
        const val TYPE_MEAL = "meal"
    }
}

data class TravelGuideDay(
    val day: Int = 0,
    // planned / unplanned（discussing 為多輪對話狀態，單次生成不會出現）
    val status: String = "",
    // arrival / normal / departure
    val kind: String = "",
    val halfDay: Boolean = false,
    // 該天包含的已預訂訂單編號（後端依合法 oid 推導），空 = 該天無已預訂項目
    val bookedAnchorOids: List<String> = emptyList(),
    val items: List<TravelGuideDayItem> = emptyList()
) {
    val isPlanned: Boolean get() = status != STATUS_UNPLANNED
    val isArrival: Boolean get() = kind == KIND_ARRIVAL
    val isDeparture: Boolean get() = kind == KIND_DEPARTURE

    companion object {
        const val STATUS_UNPLANNED = "unplanned"
        const val KIND_ARRIVAL = "arrival"
        const val KIND_DEPARTURE = "departure"
    }
}

data class TravelGuideResult(
    // LLM 失敗時為空字串
    val city: String = "",
    val totalDays: Int = 0,
    // done = 所有天皆已排定；plan = 仍有 unplannedDays
    val phase: String = "",
    val unplannedDays: List<Int> = emptyList(),
    val pendingFields: List<String> = emptyList(),
    // 旅伴語氣的完成宣告（≤2 則），渲染成對話泡泡
    val messages: List<String> = emptyList(),
    val days: List<TravelGuideDay> = emptyList(),
    // 「已排 N/M 天」
    val progressLabel: String = "",
    // phase=done 時的收尾主行動文字（如「看看完整行程」）；未完成為 null
    val mainActionLabel: String? = null,
    val failReason: String? = null
) {
    // 失敗仍回 HTTP 200：fail_reason 有值時 days=[]，UI 顯示重試
    val isGenerateSuccess: Boolean get() = failReason.isNullOrEmpty() && days.isNotEmpty()
}

/**
 * travel-revise 修改既有行程的結果。
 * 文件宣稱 [days] 恆為修改後的完整行程，但 SIT 實測後端目前只回有變動的天——
 * App 端一律以「天為單位 upsert merge」套用（回應有的天覆蓋、沒回的天保留、新天號附加），
 * 不管這次實際回幾天都不會遺失既有內容。離題/失敗時後端回原樣輸入天，merge 後內容不變。
 */
data class TravelReviseResult(
    val reply: String = "",
    // 本次回應包含的天（可能只有變動的天，也可能是完整行程；App 端一律走 merge，見上方類別註解）
    val days: List<TravelGuideDay> = emptyList(),
    // 真的有內容變動的天（後端逐位置比對算出），純 UI 高亮提示用，正確性不依賴它
    val changedDayNumbers: List<Int> = emptyList(),
    val changedSummary: List<String> = emptyList(),
    // 非行程修改的輸入（閒聊/離題）：reply 為導回主題文案、行程原樣返回
    val offTopic: Boolean = false,
    val failReason: String? = null
) {
    // fail_reason 有值 = 兜底文案：照常渲染泡泡，但不 append 進 messages（可原樣重打）
    val isGenerateSuccess: Boolean get() = failReason.isNullOrEmpty()
}

/** 422 驗證錯誤：request body 有誤（client bug），原樣重試不會成功，UI 應顯示通用錯誤而非重試。 */
class TravelPlanValidationException(message: String) : Exception(message)

/**
 * 「我的旅程」本地紀錄：後端不儲存行程，travel-guide 結果由 App 端存 DataStore。
 * [preferences] 一併保存：回訪後開啟行程仍能繼續請旅伴修改（travel-revise 帶原偏好維持風格）。
 */
data class SavedTripRecord(
    // 成果頁標題（「{旅伴名} × 你的{城市}」，儲存當下組好）
    val title: String = "",
    val city: String = "",
    val totalDays: Int = 0,
    val days: List<TravelGuideDay> = emptyList(),
    val preferences: Map<String, String> = emptyMap(),
    val createdAt: Long = 0L
)

data class CompanionProfile(
    val name: String = "",
    val personality: List<String> = emptyList(),
    val personalityLabels: List<String> = emptyList(),
    val speechStyle: String = "",
    val speechStyleLabel: String = "",
    val gender: String = "",
    val outfit: String = "",
    val hairStyle: String = "",
    val hairColor: String = "",
    val avatarUrl: String = "",
    // 旅伴自我介紹（self-introduction API 結果），存下來後首頁不用每次都重打 API 才有內容可顯示
    val introduction: String = ""
)
