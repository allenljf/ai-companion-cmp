package com.allenljf.aicompanion.data.mock

import com.allenljf.aicompanion.model.AiPartnerResult
import com.allenljf.aicompanion.model.CityChatMessage
import com.allenljf.aicompanion.model.CompanionAppearanceOption
import com.allenljf.aicompanion.model.CompanionTraitOption
import com.allenljf.aicompanion.model.QuizCompletionResult
import com.allenljf.aicompanion.model.QuizGalleryItem
import com.allenljf.aicompanion.model.QuizOption
import com.allenljf.aicompanion.model.QuizQuestion
import com.allenljf.aicompanion.model.QuizResult
import com.allenljf.aicompanion.model.RecommendCityResult
import com.allenljf.aicompanion.model.SelfIntroductionResult
import com.allenljf.aicompanion.model.TravelDestinationOption
import com.allenljf.aicompanion.model.TravelGuideDay
import com.allenljf.aicompanion.model.TravelGuideDayItem
import com.allenljf.aicompanion.model.TravelGuideResult
import com.allenljf.aicompanion.model.TravelReviseResult
import com.allenljf.aicompanion.model.TravelSummaryFromOrdersResult
import com.allenljf.aicompanion.model.TravelSummaryFromProductsResult
import com.allenljf.aicompanion.model.TravelSummaryResult
import com.allenljf.aicompanion.model.TripCityProducts
import com.allenljf.aicompanion.model.TripOrderMaterial
import com.allenljf.aicompanion.model.TripProductCard
import com.allenljf.aicompanion.model.TripProductMaterial
import com.allenljf.aicompanion.model.TripProductRef
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Demo 用軟失敗展示開關：打開後，套用此開關的 mock 方法會回傳「HTTP 200 + fail_reason 有值 + 兜底文案」，
 * 模擬 API_CONTRACT.md 強調的 LLM 軟失敗模式（見該檔「保留了什麼」一節）。
 * 目前為全域單一開關（demo 規模足夠）；真的要做成「單一 API 失敗」的細粒度控制可再拆分。
 */
object CompanionMockConfig {
    var forceFailReason: Boolean = false
}

/**
 * AI 旅伴 mock 資料來源。內容為寫實的繁體中文旅遊素材（東京／大阪／首爾等），
 * 供 Mock*Repository 組裝各支 API 的回應，形狀依 migration/API_CONTRACT.md。
 *
 * 檔案分工：這裡只放「資料」與純函式（挑選/組字），呼叫端的流程判斷（軟失敗開關、呼叫次數輪替）
 * 留在 Mock*Repository.kt。
 */
object MockData {

    /** 模擬網路延遲：300~800ms，讓 loading 狀態看起來真實（每支 mock API 呼叫一次）。 */
    suspend fun networkDelay() {
        // nextLong 上界為 exclusive，+1 讓 800ms 也在範圍內
        delay(Random.nextLong(300, 801))
    }

    // ============================================================
    // GET ai-partner
    // ============================================================

    val aiPartner = AiPartnerResult(
        personality = listOf(
            CompanionTraitOption("humorous", "幽默風趣", "說話總帶點笑點，氣氛絕不會冷場"),
            CompanionTraitOption("gentle", "溫柔體貼", "說話輕聲細語，總是先替你著想"),
            CompanionTraitOption("energetic", "元氣活潑", "精力充沛，走到哪都像在開派對"),
            CompanionTraitOption("calm", "沉穩可靠", "遇事不慌，是那種讓人安心的旅伴"),
        ),
        speechStyle = listOf(
            CompanionTraitOption("friendly", "好朋友", "像認識很久的朋友一樣自在"),
            CompanionTraitOption("formal", "有禮貌", "用詞得體，偶爾帶點小幽默"),
            CompanionTraitOption("playful", "愛耍寶", "三句不離玩笑，偶爾會裝可愛"),
        ),
        gender = listOf(
            CompanionAppearanceOption("male", "男生"),
            CompanionAppearanceOption("female", "女生"),
        ),
        outfit = listOf(
            CompanionAppearanceOption("casual", "休閒風"),
            CompanionAppearanceOption("formal", "正式風"),
            CompanionAppearanceOption("sporty", "運動風"),
        ),
        hairStyle = listOf(
            CompanionAppearanceOption("short", "短髮"),
            CompanionAppearanceOption("long", "長髮"),
            CompanionAppearanceOption("ponytail", "馬尾"),
        ),
        hairColor = listOf(
            CompanionAppearanceOption("black", "黑色"),
            CompanionAppearanceOption("brown", "棕色"),
            CompanionAppearanceOption("blonde", "金色"),
        ),
        // demo 沒有真的頭像合成素材可疊 key，avatars 留空；AiPartnerResult.avatarUrl() 遇缺失 key 會自動退回空字串
        avatars = emptyMap(),
    )

    // ============================================================
    // POST quiz
    // ============================================================

    val quizQuestions: List<QuizQuestion> = listOf(
        QuizQuestion(
            id = "1-0", dimensionId = 1, index = 0, type = "single", mode = "text",
            text = "如果現在馬上出發，你最想先做什麼？",
            options = listOf(
                QuizOption(id = "1-0-0", index = 0, text = "衝去吃在地小吃", tagId = "foodie", tagLabel = "美食控"),
                QuizOption(id = "1-0-1", index = 1, text = "找間有氛圍的咖啡廳坐下來", tagId = "aesthetic", tagLabel = "美圖控"),
                QuizOption(id = "1-0-2", index = 2, text = "直接殺去景點制高點看風景", tagId = "adventure", tagLabel = "冒險家"),
                QuizOption(id = "1-0-3", index = 3, text = "先回飯店躺平睡到自然醒", tagId = "relax", tagLabel = "悠閒派"),
            ),
        ),
        QuizQuestion(
            id = "1-1", dimensionId = 1, index = 1, type = "single", mode = "text",
            text = "逛街的時候你通常……",
            options = listOf(
                QuizOption(id = "1-1-0", index = 0, text = "路過每家藥妝店都要進去掃貨", tagId = "shopping", tagLabel = "購物狂"),
                QuizOption(id = "1-1-1", index = 1, text = "專挑巷弄裡的老店和文創小店", tagId = "culture", tagLabel = "文青魂"),
                QuizOption(id = "1-1-2", index = 2, text = "看到排隊名店一定要跟著排", tagId = "foodie", tagLabel = "美食控"),
                QuizOption(id = "1-1-3", index = 3, text = "其實我比較想找地方坐著休息", tagId = "relax", tagLabel = "悠閒派"),
            ),
        ),
        QuizQuestion(
            id = "2-0", dimensionId = 2, index = 0, type = "single", mode = "text",
            text = "規劃行程時，預算對你來說……",
            options = listOf(
                QuizOption(id = "2-0-0", index = 0, text = "能省則省，把錢留給真正想要的體驗", tagId = "budget", tagLabel = "精打細算"),
                QuizOption(id = "2-0-1", index = 1, text = "住宿吃飯都要有點質感", tagId = "comfort", tagLabel = "舒適系"),
                QuizOption(id = "2-0-2", index = 2, text = "難得出國，貴一點也值得", tagId = "luxury", tagLabel = "享受派"),
                QuizOption(id = "2-0-3", index = 3, text = "沒有特別設限，看當下心情", tagId = "flexible", tagLabel = "隨性派"),
            ),
        ),
        QuizQuestion(
            id = "2-1", dimensionId = 2, index = 1, type = "single", mode = "text",
            text = "理想中一天的行程節奏是？",
            options = listOf(
                QuizOption(id = "2-1-0", index = 0, text = "排滿滿，每分鐘都要有事做", tagId = "adventure", tagLabel = "冒險家"),
                QuizOption(id = "2-1-1", index = 1, text = "早出晚歸，但中午一定要休息", tagId = "balanced", tagLabel = "平衡型"),
                QuizOption(id = "2-1-2", index = 2, text = "一天一個重點景點就好", tagId = "relax", tagLabel = "悠閒派"),
                QuizOption(id = "2-1-3", index = 3, text = "隨遇而安，走到哪算哪", tagId = "flexible", tagLabel = "隨性派"),
            ),
        ),
        QuizQuestion(
            id = "3-0", dimensionId = 3, index = 0, type = "single", mode = "text",
            text = "比起熱門地標，你更容易被什麼吸引？",
            options = listOf(
                QuizOption(id = "3-0-0", index = 0, text = "在地市場和小吃攤", tagId = "foodie", tagLabel = "美食控"),
                QuizOption(id = "3-0-1", index = 1, text = "老街、神社，這種有歷史感的角落", tagId = "culture", tagLabel = "文青魂"),
                QuizOption(id = "3-0-2", index = 2, text = "海邊、山上這種能放空的自然景觀", tagId = "relax", tagLabel = "悠閒派"),
                QuizOption(id = "3-0-3", index = 3, text = "刺激的戶外活動或主題樂園", tagId = "adventure", tagLabel = "冒險家"),
            ),
        ),
        QuizQuestion(
            id = "3-1", dimensionId = 3, index = 1, type = "single", mode = "text",
            text = "如果旅伴臨時建議改變行程，你會？",
            options = listOf(
                QuizOption(id = "3-1-0", index = 0, text = "只要好玩都可以，走一步算一步", tagId = "flexible", tagLabel = "隨性派"),
                QuizOption(id = "3-1-1", index = 1, text = "先問清楚細節再決定要不要跟", tagId = "comfort", tagLabel = "舒適系"),
                QuizOption(id = "3-1-2", index = 2, text = "超興奮，馬上說走就走", tagId = "adventure", tagLabel = "冒險家"),
                QuizOption(id = "3-1-3", index = 3, text = "希望維持原計畫，不喜歡臨時變動", tagId = "budget", tagLabel = "精打細算"),
            ),
        ),
    )

    fun quizResult(): QuizResult =
        QuizResult(count = quizQuestions.size, failReason = null, questions = quizQuestions)

    // 軟失敗案例：LLM 改寫題目文案失敗，questions 用固定文案兜底（這裡示範直接沿用同一份題庫當兜底內容）
    fun quizResultSoftFailure(): QuizResult =
        QuizResult(count = quizQuestions.size, failReason = "llm_error", questions = quizQuestions)

    // ============================================================
    // POST quiz-completions
    // ============================================================

    private data class TravelProfile(
        val travelIdentity: String,
        val travelIdentityEn: String,
        val destinationCn: String,
        val destinationEn: String,
        val destinationCountry: String,
        val destinationCountryEn: String,
        val tagline: String,
        val taglineEn: String,
        val highlightTags: List<String>,
        val highlightTagsEn: List<String>,
        val companionQuote: String,
        val companionQuoteEn: String,
        val recommendation: List<String>,
        val socialPost: String,
    )

    private val travelProfiles: Map<String, TravelProfile> = mapOf(
        "foodie" to TravelProfile(
            travelIdentity = "深夜食堂放浪者", travelIdentityEn = "Midnight Diner Wanderer",
            destinationCn = "大阪", destinationEn = "Osaka",
            destinationCountry = "日本", destinationCountryEn = "Japan",
            tagline = "為了一口好吃的，可以走遍整座城市", taglineEn = "Will walk the whole city for one good bite",
            highlightTags = listOf("美食控", "夜生活", "在地小吃"), highlightTagsEn = listOf("Foodie", "Nightlife", "Street Food"),
            companionQuote = "跟你出門最安心，反正肚子餓了你一定知道要去哪吃！", companionQuoteEn = "Traveling with you is easy — you always know where to eat!",
            recommendation = listOf(
                "你的答案裡藏不住對食物的熱情，這種人只適合去一個「巷子裡都是美食」的城市。",
                "大阪剛好符合條件：黑門市場、道頓堀、章魚燒攤位密度全日本數一數二，怎麼吃都吃不完。",
                "而且大阪人講話直來直往又愛開玩笑，跟你的個性意外地合拍，一起去感受這座城市的活力吧！",
            ),
            socialPost = "測驗結果：我是深夜食堂放浪者，命定城市是大阪！看來這趟要練胃了 🍢",
        ),
        "culture" to TravelProfile(
            travelIdentity = "巷弄漫遊藝術家", travelIdentityEn = "Backstreet Wandering Artist",
            destinationCn = "京都", destinationEn = "Kyoto",
            destinationCountry = "日本", destinationCountryEn = "Japan",
            tagline = "喜歡在有故事的角落，走得比別人慢一點", taglineEn = "Lingers a little longer in every storied corner",
            highlightTags = listOf("文青魂", "古蹟巡禮", "慢步調"), highlightTagsEn = listOf("Culture", "Heritage", "Slow Travel"),
            companionQuote = "跟你走在老街上完全不用趕行程，隨便一條巷子都能拍上十分鐘。", companionQuoteEn = "Walking old streets with you, ten minutes per alley, easy.",
            recommendation = listOf(
                "你對有歷史感的角落特別有感覺，這種人適合一座「連巷子都有故事」的城市。",
                "京都的神社、老町屋、竹林小徑，每個轉角都值得你停下來看很久，完全對你的胃口。",
                "而且京都步調本來就慢，很適合你這種喜歡細細品味、不喜歡被行程追著跑的旅行方式。",
            ),
            socialPost = "測驗結果：我是巷弄漫遊藝術家，命定城市是京都！準備好慢慢逛了 🍁",
        ),
        "adventure" to TravelProfile(
            travelIdentity = "都市冒險特工", travelIdentityEn = "Urban Adventure Agent",
            destinationCn = "首爾", destinationEn = "Seoul",
            destinationCountry = "韓國", destinationCountryEn = "South Korea",
            tagline = "行程排滿才安心，休息是浪費時間", taglineEn = "A packed schedule is the only comfortable schedule",
            highlightTags = listOf("冒險家", "潮流敏感", "體力怪"), highlightTagsEn = listOf("Adventurous", "Trendsetter", "Energetic"),
            companionQuote = "跟你出去真的很累，但每次都玩得超值，行程排這麼滿也只有你受得了！", companionQuoteEn = "Exhausting but worth it — only you can handle this packed a schedule!",
            recommendation = listOf(
                "你答題的速度跟選項都透露出「閒不下來」的個性，這種人需要一座永遠有新事物的城市。",
                "首爾的節奏快、潮流換得也快，弘大、明洞、江南隨便排都能塞滿一整天，剛好餵飽你的行動力。",
                "而且首爾晚上也很熱鬧，就算白天走到鐵腿，晚上你應該還是想再衝一波夜市。",
            ),
            socialPost = "測驗結果：我是都市冒險特工，命定城市是首爾！行程已經自動排到爆滿 ⚡",
        ),
        "relax" to TravelProfile(
            travelIdentity = "海島耍廢大師", travelIdentityEn = "Island Relaxation Master",
            destinationCn = "沖繩", destinationEn = "Okinawa",
            destinationCountry = "日本", destinationCountryEn = "Japan",
            tagline = "旅行的意義就是找個地方好好放空", taglineEn = "The point of travel is to do absolutely nothing, beautifully",
            highlightTags = listOf("悠閒派", "海島控", "慢活"), highlightTagsEn = listOf("Relaxed", "Island Lover", "Slow Living"),
            companionQuote = "跟你旅行最舒服，行程隨便排都好，反正你最想做的事就是曬太陽。", companionQuoteEn = "Traveling with you is the easiest — you just want sunshine and quiet.",
            recommendation = listOf(
                "你在每一題都選了最放鬆的選項，這種人真的不適合排太滿的行程。",
                "沖繩海邊步調慢、景點之間車程也不趕，剛好讓你能一天只排一到兩件事就收工。",
                "美麗海水族館看完就去海邊發呆，晚上找家居酒屋小酌，這才是你要的旅行節奏。",
            ),
            socialPost = "測驗結果：我是海島耍廢大師，命定城市是沖繩！行程表只有「發呆」兩個字 🏖️",
        ),
    )

    private val defaultTravelProfile = TravelProfile(
        travelIdentity = "行程規劃控", travelIdentityEn = "The Itinerary Planner",
        destinationCn = "東京", destinationEn = "Tokyo",
        destinationCountry = "日本", destinationCountryEn = "Japan",
        tagline = "什麼都想安排，什麼都不想錯過", taglineEn = "Wants to plan everything, miss nothing",
        highlightTags = listOf("平衡型", "都會控", "行程控"), highlightTagsEn = listOf("Balanced", "City Lover", "Planner"),
        companionQuote = "跟你出門超放心，行程都排得妥妥的，我只要負責跟緊就好。", companionQuoteEn = "Traveling with you is easy — your itinerary is always ready.",
        recommendation = listOf(
            "你的答案分布很平均，屬於什麼都想試試看的類型，這種人適合一座「什麼都有」的城市。",
            "東京從歷史老街到最新潮流一應俱全，購物、美食、文化景點通通排得下，剛好符合你的胃口。",
            "行程可以排得很緊湊也可以隨時插入新發現，東京的彈性剛好配得上你這種「什麼都想要」的個性。",
        ),
        socialPost = "測驗結果：我是行程規劃控，命定城市是東京！Excel 行程表已經打開 📋",
    )

    private val quizCompletionReasoningLines = listOf(
        "正在翻開你的答案卡片……",
        "咦，這個組合有點意思",
        "美食分數偏高，記下來",
        "步調偏悠閒，先排除硬核行程",
        "翻了一下命定城市資料庫",
        "找到幾個很搭的候選城市",
        "比對你的預算傾向",
        "篩掉太貴或太趕的選項",
        "終於，鎖定一個城市了",
        "在想怎麼介紹這座城市給你",
        "順便想了一句適合你的標語",
        "整理完畢，準備公布結果！",
    )

    fun quizCompletion(selectedTags: List<String>): QuizCompletionResult {
        val dominantTag = selectedTags.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key
        val profile = travelProfiles[dominantTag] ?: defaultTravelProfile
        return QuizCompletionResult(
            quizCompletionId = Random.nextLong(100_000, 999_999),
            travelIdentity = profile.travelIdentity,
            travelIdentityEn = profile.travelIdentityEn,
            destinationCn = profile.destinationCn,
            destinationEn = profile.destinationEn,
            destinationCountry = profile.destinationCountry,
            destinationCountryEn = profile.destinationCountryEn,
            tagline = profile.tagline,
            taglineEn = profile.taglineEn,
            highlightTags = profile.highlightTags,
            highlightTagsEn = profile.highlightTagsEn,
            companionQuote = profile.companionQuote,
            companionQuoteEn = profile.companionQuoteEn,
            recommendation = profile.recommendation,
            reasoning = quizCompletionReasoningLines,
            socialPost = profile.socialPost,
            shareImageStatus = QuizCompletionResult.SHARE_IMAGE_STATUS_SKIPPED,
            failReason = null,
        )
    }

    // 軟失敗案例：分析快取過期/LLM 失敗，reasoning 為空陣列（依文件規則），其餘欄位用通用兜底文案
    fun quizCompletionSoftFailure(): QuizCompletionResult = QuizCompletionResult(
        quizCompletionId = 0,
        travelIdentity = "神秘旅人", travelIdentityEn = "Mystery Traveler",
        destinationCn = "", destinationEn = "",
        destinationCountry = "", destinationCountryEn = "",
        tagline = "這次分析有點卡住了，要不要再試一次？", taglineEn = "Something went wrong — want to try again?",
        highlightTags = emptyList(), highlightTagsEn = emptyList(),
        companionQuote = "抱歉，我剛剛分析到一半恍神了，可以再讓我看一次你的答案嗎？",
        companionQuoteEn = "Sorry, I got distracted mid-analysis — mind if I take another look?",
        recommendation = listOf("這次分析暫時沒有結果，重新測驗一次應該就可以了！"),
        reasoning = emptyList(),
        socialPost = "",
        shareImageStatus = QuizCompletionResult.SHARE_IMAGE_STATUS_SKIPPED,
        failReason = "llm_error",
    )

    // ============================================================
    // GET quiz-gallery
    // ============================================================

    val quizGalleryItems: List<QuizGalleryItem> = listOf(
        QuizGalleryItem(
            travelIdentity = "深夜食堂放浪者", travelIdentityEn = "Midnight Diner Wanderer",
            destinationCn = "大阪", destinationEn = "Osaka", destinationCountry = "日本",
            tagline = "為了一口好吃的，可以走遍整座城市",
            highlightTags = listOf("美食控", "夜生活", "在地小吃"),
            companionQuote = "跟你出門最安心，反正肚子餓了你一定知道要去哪吃！",
            shareImageUrl = null, companionName = "小旅", partnerAvatarUrl = null,
            createdAt = "2026-07-18T21:32:00Z",
        ),
        QuizGalleryItem(
            travelIdentity = "巷弄漫遊藝術家", travelIdentityEn = "Backstreet Wandering Artist",
            destinationCn = "京都", destinationEn = "Kyoto", destinationCountry = "日本",
            tagline = "喜歡在有故事的角落，走得比別人慢一點",
            highlightTags = listOf("文青魂", "古蹟巡禮", "慢步調"),
            companionQuote = "跟你走在老街上完全不用趕行程，隨便一條巷子都能拍上十分鐘。",
            shareImageUrl = null, companionName = "阿墨", partnerAvatarUrl = null,
            createdAt = "2026-07-19T10:05:00Z",
        ),
        QuizGalleryItem(
            travelIdentity = "都市冒險特工", travelIdentityEn = "Urban Adventure Agent",
            destinationCn = "首爾", destinationEn = "Seoul", destinationCountry = "韓國",
            tagline = "行程排滿才安心，休息是浪費時間",
            highlightTags = listOf("冒險家", "潮流敏感", "體力怪"),
            companionQuote = "跟你出去真的很累，但每次都玩得超值！",
            shareImageUrl = null, companionName = "Kuma", partnerAvatarUrl = null,
            createdAt = "2026-07-20T08:47:00Z",
        ),
        QuizGalleryItem(
            travelIdentity = "海島耍廢大師", travelIdentityEn = "Island Relaxation Master",
            destinationCn = "沖繩", destinationEn = "Okinawa", destinationCountry = "日本",
            tagline = "旅行的意義就是找個地方好好放空",
            highlightTags = listOf("悠閒派", "海島控", "慢活"),
            companionQuote = "跟你旅行最舒服，反正你最想做的事就是曬太陽。",
            shareImageUrl = null, companionName = "小魚", partnerAvatarUrl = null,
            createdAt = "2026-07-21T15:12:00Z",
        ),
        QuizGalleryItem(
            travelIdentity = "行程規劃控", travelIdentityEn = "The Itinerary Planner",
            destinationCn = "東京", destinationEn = "Tokyo", destinationCountry = "日本",
            tagline = "什麼都想安排，什麼都不想錯過",
            highlightTags = listOf("平衡型", "都會控", "行程控"),
            companionQuote = "跟你出門超放心，行程都排得妥妥的。",
            shareImageUrl = null, companionName = "小安", partnerAvatarUrl = null,
            createdAt = "2026-07-22T09:26:00Z",
        ),
        QuizGalleryItem(
            travelIdentity = "深夜食堂放浪者", travelIdentityEn = "Midnight Diner Wanderer",
            destinationCn = "大阪", destinationEn = "Osaka", destinationCountry = "日本",
            tagline = "為了一口好吃的，可以走遍整座城市",
            highlightTags = listOf("美食控", "夜生活", "在地小吃"),
            companionQuote = "肚子餓的時候找我就對了！",
            shareImageUrl = null, companionName = "旅旅", partnerAvatarUrl = null,
            createdAt = "2026-07-23T19:58:00Z",
        ),
    )

    // ============================================================
    // POST self-introduction
    // ============================================================

    // tag id → 中文 label 查表：personality/speechStyle 從 ViewModel 傳來的是 tag（如 "humorous"），
    // 直接塞進中文自介句子會混入英文，改用 aiPartner 既有的 label 資料查表還原成中文。
    private val personalityLabels: Map<String, String> = aiPartner.personality.associate { it.tag to it.label }
    private val speechStyleLabels: Map<String, String> = aiPartner.speechStyle.associate { it.tag to it.label }

    fun personalityLabel(tag: String): String = personalityLabels[tag] ?: tag

    fun speechStyleLabel(tag: String): String = speechStyleLabels[tag] ?: tag

    fun selfIntroduction(companionName: String, speechStyleLabel: String, personalityLabel: String): SelfIntroductionResult {
        val name = companionName.ifBlank { "旅伴" }
        return SelfIntroductionResult(
            introduction = "嗨，我是$name！個性$personalityLabel、講話走${speechStyleLabel}路線，" +
                "接下來不管是想找地方吃飯、還是想排一份完整的行程，都可以直接跟我說，我陪你一起搞定！",
            failReason = null,
        )
    }

    fun selfIntroductionSoftFailure(companionName: String): SelfIntroductionResult {
        val name = companionName.ifBlank { "旅伴" }
        return SelfIntroductionResult(
            introduction = "嗨，我是$name，很高興認識你！這趟旅程我會盡力幫你安排，有任何需求都可以告訴我。",
            failReason = "llm_error",
        )
    }

    // ============================================================
    // 城市判斷共用：把「日本」「關西」這類籠統地名收斂成具體城市
    // ============================================================

    private val vagueRegionToCity = mapOf(
        "日本" to "東京", "關東" to "東京", "关东" to "東京",
        "關西" to "大阪", "近畿" to "大阪", "关西" to "大阪",
        "韓國" to "首爾", "韩国" to "首爾",
        "泰國" to "曼谷", "泰国" to "曼谷",
        "台灣" to "台北", "台湾" to "台北",
    )

    private val knownConcreteCities = listOf(
        "東京", "大阪", "京都", "首爾", "曼谷", "沖繩", "台北", "濟州島", "河口湖", "富士山", "嵐山", "北海道",
    )

    /**
     * 城市判斷優先序（見 API_CONTRACT.md「只回具體城市不回國家」）：
     * primary（通常是 destination_name）先查；查不出來再退回 secondary（通常是 prod_name/package_name）；
     * 都判斷不出來回空字串。
     */
    fun resolveConcreteCity(primary: String, secondary: String = ""): String {
        resolveFromSingleText(primary)?.let { return it }
        resolveFromSingleText(secondary)?.let { return it }
        return ""
    }

    private fun resolveFromSingleText(text: String): String? {
        if (text.isBlank()) return null
        vagueRegionToCity[text]?.let { return it }
        knownConcreteCities.firstOrNull { text.contains(it) }?.let { return it }
        // 不在籠統地名清單裡、也對不到已知城市關鍵字——視為已經是具體地名（例如使用者自訂的小眾城市），直接沿用
        return text
    }

    // ============================================================
    // POST travel-summary
    // ============================================================

    fun travelSummaryFromZero(companionName: String?): TravelSummaryResult {
        val name = companionName?.takeIf { it.isNotBlank() } ?: "我"
        // from_zero 不打 LLM，回固定文案即可（見 API_CONTRACT.md）
        return TravelSummaryResult(
            summary = "哈囉，我是$name！還沒決定要去哪裡也沒關係，我們可以先聊聊你想要什麼樣的旅行感覺。",
            city = "",
            failReason = null,
        )
    }

    fun travelSummaryEcho(city: String, companionName: String?, order: TripOrderMaterial?): TravelSummaryResult {
        val name = companionName?.takeIf { it.isNotBlank() } ?: "我"
        val summary = if (order != null) {
            "看到你有${order.prodName}的訂單，$name 已經迫不及待想幫你把${city}的其他行程也排好了！"
        } else {
            "$city 根本是為你這種旅人開的城市！要不要就從這裡開始排行程？"
        }
        // quiz_completion / from_orders：city 為權威回傳值，原樣返回，不經 LLM 判斷
        return TravelSummaryResult(summary = summary, city = city, failReason = null)
    }

    fun travelSummaryFromImported(content: String?, companionName: String?): TravelSummaryResult {
        val name = companionName?.takeIf { it.isNotBlank() } ?: "我"
        val inferredCity = content
            ?.let { text -> knownConcreteCities.firstOrNull { text.contains(it) } }
            .orEmpty()
        val summary = if (inferredCity.isNotBlank()) {
            "看完你貼的行程，感覺得出來這趟是要去$inferredCity！$name 幫你把細節都補齊，一起把它排成完整版吧。"
        } else {
            "你貼的行程資訊有點少，$name 目前還看不出明確的目的地，要不要多補充一點細節？"
        }
        return TravelSummaryResult(summary = summary, city = inferredCity, failReason = null)
    }

    fun travelSummaryMerge(previousSummary: String, note: String, city: String?, companionName: String?): TravelSummaryResult {
        val name = companionName?.takeIf { it.isNotBlank() } ?: "我"
        // 帶 previous_summary + note 時要整合成新的一份取代前版（不是接在後面）
        val summary = if (note.isNotBlank()) {
            "補充了「$note」之後，$name 重新想了一下：${previousSummary}——這樣應該更符合你這次想要的感覺！"
        } else {
            previousSummary
        }
        return TravelSummaryResult(summary = summary, city = city.orEmpty(), failReason = null)
    }

    fun travelSummarySoftFailure(city: String?): TravelSummaryResult = TravelSummaryResult(
        summary = "剛剛想開場白的時候卡了一下，不過沒關係，我們還是可以繼續聊接下來想去哪裡玩！",
        city = city.orEmpty(),
        failReason = "llm_error",
    )

    // ============================================================
    // POST travel-summary-from-orders / -from-wish / -from-history
    // ============================================================

    fun travelSummaryFromOrders(orders: List<TripOrderMaterial>, companionName: String?): TravelSummaryFromOrdersResult {
        val name = companionName?.takeIf { it.isNotBlank() } ?: "我"
        if (orders.isEmpty()) {
            return TravelSummaryFromOrdersResult(
                greeting = "看了一下，你最近好像沒有即將出發的訂單，要不要直接告訴$name 你想去哪裡玩？",
                options = emptyList(),
                failReason = null,
            )
        }
        // 編號防呆：後端依位置索引（order_index）取回原始訂單，不會有 LLM 幻覺編號
        val options = orders.mapIndexed { index, order ->
            TravelDestinationOption(
                orderIndex = index,
                city = resolveConcreteCity(order.destinationName, order.prodName),
            )
        }
        return TravelSummaryFromOrdersResult(
            greeting = "Hi, 我是$name！我發現你的訂單中有即將出發的旅程，想從哪個目的地開始？",
            options = options,
            failReason = null,
        )
    }

    fun travelSummaryFromProducts(products: List<TripProductMaterial>): TravelSummaryFromProductsResult {
        if (products.isEmpty()) {
            return TravelSummaryFromProductsResult(
                greeting = "目前還看不出明確的目的地，要不要直接告訴我你想去哪裡玩？",
                cities = emptyList(),
                failReason = "low_confidence",
            )
        }
        val grouped = LinkedHashMap<String, MutableList<TripProductRef>>()
        products.forEach { product ->
            val city = resolveConcreteCity(product.destinationNames.firstOrNull().orEmpty(), product.prodName)
            if (city.isBlank()) return@forEach
            grouped.getOrPut(city) { mutableListOf() }
                .add(TripProductRef(prodId = product.prodId, prodName = product.prodName))
        }
        val cities = grouped.entries.take(3).map { (city, refs) -> TripCityProducts(city = city, products = refs) }
        return TravelSummaryFromProductsResult(
            greeting = "看了你收藏的行程，要不要先選一個目的地？",
            cities = cities,
            failReason = null,
        )
    }

    // ============================================================
    // POST recommend-city
    // ============================================================

    private const val RECOMMEND_MAX_ROUNDS = 5
    private const val RECOMMEND_SWAP_LIMIT = 5

    // (城市, 推薦理由)
    private val recommendableCities = listOf(
        "京都" to "古都慢步調很對你的味",
        "大阪" to "美食密度全日本第一",
        "東京" to "什麼都有，逛到腿軟",
        "首爾" to "潮流跟美食一次滿足",
        "曼谷" to "物價親民又好拍",
        "沖繩" to "海島步調最放鬆",
    )

    private val citySynonyms = mapOf(
        "kyoto" to "京都", "osaka" to "大阪", "tokyo" to "東京",
        "seoul" to "首爾", "bangkok" to "曼谷", "okinawa" to "沖繩",
    )

    private val offTopicKeywords = listOf("天氣", "笑話", "講個笑話", "你幾歲", "你是誰", "無聊", "AI是什麼")
    private val decisiveKeywords = listOf("直接", "就去", "幫我定", "不用想了", "你決定", "都可以，你推薦")

    private fun normalizeCityName(raw: String): String = citySynonyms[raw.trim().lowercase()] ?: raw.trim()

    private fun containsAny(text: String, keywords: List<String>): Boolean = keywords.any { text.contains(it) }

    fun recommendCity(messages: List<CityChatMessage>, shownCities: List<String>): RecommendCityResult {
        val round = messages.count { it.role == CityChatMessage.ROLE_USER }
        val lastUserMessage = messages.lastOrNull { it.role == CityChatMessage.ROLE_USER }?.content.orEmpty()
        val remainingRounds = (RECOMMEND_MAX_ROUNDS - round).coerceAtLeast(0)

        // 換城超限保底：未打 LLM，reply 為固定文案
        if (shownCities.size > RECOMMEND_SWAP_LIMIT) {
            return RecommendCityResult(
                reply = "已經換了好幾個城市囉，要不要我們重新聊聊你想要的旅遊感覺？",
                quickReplies = listOf("重新聊聊"),
                recommendedCity = "", cityReason = "", isFinal = false, offTopic = false,
                round = round, maxRounds = RECOMMEND_MAX_ROUNDS, remainingRounds = remainingRounds,
                swapLimitReached = true, failReason = null,
            )
        }

        // 離題 → 用旅伴語氣導回，該輪照樣計數
        if (containsAny(lastUserMessage, offTopicKeywords)) {
            return RecommendCityResult(
                reply = "哈哈這個我們晚點聊，先想想你想去哪個城市吧！安靜一點還是熱鬧一點？",
                quickReplies = listOf("安靜慢步調", "熱鬧吃到飽", "都可以，你推薦"),
                recommendedCity = "", cityReason = "", isFinal = false, offTopic = true,
                round = round, maxRounds = RECOMMEND_MAX_ROUNDS, remainingRounds = remainingRounds,
                swapLimitReached = false, failReason = null,
            )
        }

        val excluded = shownCities.map { normalizeCityName(it) }.toSet()
        val candidates = recommendableCities.filter { normalizeCityName(it.first) !in excluded }
        val candidate = candidates.firstOrNull() ?: recommendableCities.first()

        // 第 5 輪強制輸出城市；使用者明確表態（decisiveKeywords）時可提前收斂
        val shouldFinalize = round >= RECOMMEND_MAX_ROUNDS || containsAny(lastUserMessage, decisiveKeywords)
        if (shouldFinalize) {
            return RecommendCityResult(
                reply = "那我推你去${candidate.first}——${candidate.second}，要就定${candidate.first}嗎？",
                // is_final=true 時 quick_replies 後端強制覆寫為固定三顆
                quickReplies = listOf("就去${candidate.first}！", "換一個城市", "重新聊聊"),
                recommendedCity = candidate.first,
                cityReason = candidate.second,
                isFinal = true, offTopic = false,
                round = round, maxRounds = RECOMMEND_MAX_ROUNDS, remainingRounds = remainingRounds,
                swapLimitReached = false, failReason = null,
            )
        }

        // 自由收斂中：問一個收斂問題；第 4 輪結尾附加提醒
        val followUps = listOf(
            "想要安靜慢步調還是熱鬧吃到飽？",
            "比較想吃美食還是逛景點呢？",
            "預算抓寬鬆一點還是精打細算？",
        )
        val question = followUps[(round - 1).coerceIn(0, followUps.lastIndex)]
        val reminder = if (round == RECOMMEND_MAX_ROUNDS - 1) "（下一輪我就直接幫你定城市囉！）" else ""
        return RecommendCityResult(
            reply = "$question$reminder",
            quickReplies = listOf("安靜慢步調", "熱鬧吃到飽", "都可以，你推薦"),
            recommendedCity = "", cityReason = "", isFinal = false, offTopic = false,
            round = round, maxRounds = RECOMMEND_MAX_ROUNDS, remainingRounds = remainingRounds,
            swapLimitReached = false, failReason = null,
        )
    }

    fun recommendCitySoftFailure(round: Int): RecommendCityResult = RecommendCityResult(
        reply = "剛剛想推薦城市的時候卡了一下，要不要換個說法再聊聊？",
        quickReplies = listOf("安靜慢步調", "熱鬧吃到飽", "都可以，你推薦"),
        recommendedCity = "", cityReason = "", isFinal = false, offTopic = false,
        round = round, maxRounds = RECOMMEND_MAX_ROUNDS, remainingRounds = (RECOMMEND_MAX_ROUNDS - round).coerceAtLeast(0),
        swapLimitReached = false, failReason = "llm_error",
    )

    // ============================================================
    // POST travel-guide —— 依城市準備多組預錄完整行程，輪替呈現
    // ============================================================

    private fun spot(name: String, text: String, time: String, timeBand: String, lat: Double? = null, lng: Double? = null) =
        TravelGuideDayItem(name = name, text = text, type = TravelGuideDayItem.TYPE_SPOT, time = time, timeBand = timeBand, lat = lat, lng = lng)

    private fun meal(name: String, text: String, time: String, timeBand: String) =
        TravelGuideDayItem(name = name, text = text, type = TravelGuideDayItem.TYPE_MEAL, time = time, timeBand = timeBand)

    private fun logistics(name: String, text: String, time: String, timeBand: String, transportMode: String) =
        TravelGuideDayItem(name = name, text = text, type = TravelGuideDayItem.TYPE_LOGISTICS, time = time, timeBand = timeBand, transportMode = transportMode)

    private val osakaGuide = TravelGuideResult(
        city = "大阪", totalDays = 3, phase = "done", unplannedDays = emptyList(), pendingFields = emptyList(),
        messages = listOf("行程排好了！這三天以美食與大阪代表景點為主軸，晚上也留了時間讓你逛街。"),
        days = listOf(
            TravelGuideDay(
                day = 1, status = "planned", kind = TravelGuideDay.KIND_ARRIVAL, halfDay = true,
                items = listOf(
                    logistics("關西國際機場", "抵達大阪，轉搭機場特急前往市區", "14:00", "下午", "train"),
                    spot("道頓堀", "晚間漫遊，燈牌配章魚燒才道地", "19:00", "晚上", 34.6687, 135.5013),
                    meal("大阪燒本舖", "來一份道地大阪燒收尾第一天", "20:30", "晚上"),
                ),
            ),
            TravelGuideDay(
                day = 2, status = "planned", kind = "normal", halfDay = false,
                items = listOf(
                    spot("大阪城公園", "晨間散步，順便拍幾張天守閣", "09:00", "上午", 34.6873, 135.5262),
                    meal("黑門市場", "邊走邊吃在地海鮮和當季水果", "12:00", "中午"),
                    spot("環球影城", "哈利波特魔法世界不能錯過", "14:00", "下午", 34.6654, 135.4323),
                    spot("心齋橋", "逛街買藥妝到打烊", "19:30", "晚上", 34.6717, 135.5011),
                ),
            ),
            TravelGuideDay(
                day = 3, status = "planned", kind = TravelGuideDay.KIND_DEPARTURE, halfDay = true,
                items = listOf(
                    spot("梅田空中庭園展望台", "離開前上去看一次城市全景", "10:00", "上午", 34.7055, 135.4959),
                    logistics("關西國際機場", "搭車返回機場準備搭機", "15:00", "下午", "train"),
                ),
            ),
        ),
        progressLabel = "已排 3/3 天",
        mainActionLabel = "看看完整行程",
        failReason = null,
    )

    private val tokyoGuide = TravelGuideResult(
        city = "東京", totalDays = 3, phase = "done", unplannedDays = emptyList(), pendingFields = emptyList(),
        messages = listOf("行程排好了！從老街文化到潮流街區都幫你排進去了，走起來會很有東京的感覺。"),
        days = listOf(
            TravelGuideDay(
                day = 1, status = "planned", kind = TravelGuideDay.KIND_ARRIVAL, halfDay = true,
                items = listOf(
                    logistics("成田機場", "抵達東京，搭利木津巴士前往市區", "15:00", "下午", "bus"),
                    spot("淺草寺", "雷門拍照，順便逛仲見世通商店街", "18:00", "晚上", 35.7148, 139.7967),
                    meal("晴空塔周邊拉麵店", "吃碗拉麵當作第一天的收尾", "20:00", "晚上"),
                ),
            ),
            TravelGuideDay(
                day = 2, status = "planned", kind = "normal", halfDay = false,
                items = listOf(
                    spot("明治神宮", "晨間漫步在森林參道裡", "09:00", "上午", 35.6764, 139.6993),
                    meal("表參道小店", "邊逛設計小店邊找午餐", "12:30", "中午"),
                    spot("澀谷十字路口", "體驗全世界最有名的斑馬線", "15:00", "下午", 35.6595, 139.7005),
                    spot("新宿歌舞伎町", "夜晚燈牌下感受東京的活力", "20:00", "晚上", 35.6938, 139.7034),
                ),
            ),
            TravelGuideDay(
                day = 3, status = "planned", kind = TravelGuideDay.KIND_DEPARTURE, halfDay = true,
                items = listOf(
                    spot("東京晴空塔", "登高俯瞰整個關東平原", "10:00", "上午", 35.7101, 139.8107),
                    logistics("成田機場", "搭車返回機場準備搭機", "15:00", "下午", "bus"),
                ),
            ),
        ),
        progressLabel = "已排 3/3 天",
        mainActionLabel = "看看完整行程",
        failReason = null,
    )

    private val seoulGuide = TravelGuideResult(
        city = "首爾", totalDays = 2, phase = "done", unplannedDays = emptyList(), pendingFields = emptyList(),
        messages = listOf("兩天的首爾行程排好了！時間比較緊湊，但重點景點跟美食都排進去了。"),
        days = listOf(
            TravelGuideDay(
                day = 1, status = "planned", kind = TravelGuideDay.KIND_ARRIVAL, halfDay = true,
                items = listOf(
                    logistics("仁川機場", "抵達首爾，搭機場快線進市區", "14:00", "下午", "train"),
                    spot("明洞", "逛街試吃街邊小吃，順便換錢", "17:00", "下午", 37.5636, 126.9834),
                    meal("炭火烤肉店", "韓式烤肉配燒酒收尾第一天", "19:30", "晚上"),
                ),
            ),
            TravelGuideDay(
                day = 2, status = "planned", kind = TravelGuideDay.KIND_DEPARTURE, halfDay = true,
                items = listOf(
                    spot("北村韓屋村", "晨間穿韓服拍傳統建築照", "09:30", "上午", 37.5826, 126.9830),
                    spot("景福宮", "順路參觀朝鮮王朝正宮", "11:30", "上午", 37.5796, 126.9770),
                    logistics("仁川機場", "搭車返回機場準備搭機", "16:00", "下午", "train"),
                ),
            ),
        ),
        progressLabel = "已排 2/2 天",
        mainActionLabel = "看看完整行程",
        failReason = null,
    )

    private val cityGuideTemplates = listOf(
        listOf("大阪", "osaka") to osakaGuide,
        listOf("東京", "tokyo") to tokyoGuide,
        listOf("首爾", "seoul") to seoulGuide,
    )

    /** 找不到預錄城市時的通用兩天行程，把城市名稱直接帶入文案，確保任何輸入都能得到看起來合理的結果。 */
    private fun genericGuide(city: String): TravelGuideResult {
        val displayCity = city.ifBlank { "這座城市" }
        return TravelGuideResult(
            city = city,
            totalDays = 2,
            phase = "done",
            unplannedDays = emptyList(),
            pendingFields = emptyList(),
            messages = listOf("行程排好了！先幫你排了兩天的$displayCity 精華路線，之後想調整都可以直接跟我說。"),
            days = listOf(
                TravelGuideDay(
                    day = 1, status = "planned", kind = TravelGuideDay.KIND_ARRIVAL, halfDay = true,
                    items = listOf(
                        logistics("${displayCity}機場", "抵達$displayCity，前往市區飯店放行李", "14:00", "下午", "car"),
                        spot("${displayCity}市中心", "晚間隨意走走，先熟悉一下環境", "18:00", "晚上"),
                        meal("在地餐廳", "吃一頓在地口味的晚餐", "19:30", "晚上"),
                    ),
                ),
                TravelGuideDay(
                    day = 2, status = "planned", kind = TravelGuideDay.KIND_DEPARTURE, halfDay = true,
                    items = listOf(
                        spot("${displayCity}代表景點", "把最經典的景點打卡朝聖一次", "10:00", "上午"),
                        logistics("${displayCity}機場", "搭車返回機場準備搭機", "15:00", "下午", "car"),
                    ),
                ),
            ),
            progressLabel = "已排 2/2 天",
            mainActionLabel = "看看完整行程",
            failReason = null,
        )
    }

    fun travelGuideFor(city: String): TravelGuideResult {
        val matched = cityGuideTemplates.firstOrNull { (keywords, _) ->
            keywords.any { city.contains(it, ignoreCase = true) }
        }
        return matched?.second ?: genericGuide(city)
    }

    fun travelGuideSoftFailure(): TravelGuideResult = TravelGuideResult(
        city = "", totalDays = 0, phase = "plan",
        unplannedDays = emptyList(), pendingFields = emptyList(),
        messages = listOf("行程生成時遇到一點小狀況，要不要再試一次？"),
        days = emptyList(),
        progressLabel = "已排 0/0 天",
        mainActionLabel = null,
        failReason = "llm_error",
    )

    // ============================================================
    // POST travel-revise —— 依訊息關鍵字挑選情境，讓連續自然語言修改看起來是活的
    // ============================================================

    private fun reviseSeafoodScenario(days: List<TravelGuideDay>): TravelReviseResult {
        var matched = false
        val newDays = days.map { day ->
            val newItems = day.items.map { item ->
                val hitsSeafood = item.name.contains("海鮮") || item.text.contains("海鮮") ||
                    item.name.contains("生魚片") || item.text.contains("生魚片")
                if (item.type == TravelGuideDayItem.TYPE_MEAL && hitsSeafood) {
                    matched = true
                    item.copy(name = "在地家庭料理", text = "換成不含海鮮的家常菜，吃得安心")
                } else {
                    item
                }
            }
            day.copy(items = newItems)
        }
        return if (matched) {
            TravelReviseResult(
                reply = "了解，我已經把跟海鮮有關的行程都換掉了！",
                days = newDays,
                changedDayNumbers = days.zip(newDays).filter { (old, new) -> old.items != new.items }.map { it.first.day },
                changedSummary = listOf("- 用餐行程改為不含海鮮的選項"),
                offTopic = false, failReason = null,
            )
        } else {
            TravelReviseResult(
                reply = "我看了一下目前行程，沒有海鮮相關的餐點，幫你保留原本的安排！",
                days = days, changedDayNumbers = emptyList(), changedSummary = emptyList(),
                offTopic = false, failReason = null,
            )
        }
    }

    private fun reviseAddTreatScenario(days: List<TravelGuideDay>, targetDay: Int?): TravelReviseResult {
        if (days.isEmpty()) {
            return TravelReviseResult(reply = "目前還沒有行程可以修改喔，先請我幫你排一份行程吧！", days = days, failReason = null)
        }
        val dayIndex = days.indexOfFirst { it.day == targetDay }.takeIf { it >= 0 } ?: 0
        val newItem = spot("抹茶體驗", "來一份道地抹茶甜點，午後充電一下", "15:30", "下午")
        val newDays = days.mapIndexed { idx, day -> if (idx == dayIndex) day.copy(items = day.items + newItem) else day }
        val touchedDay = newDays[dayIndex].day
        return TravelReviseResult(
            reply = "幫你在 Day $touchedDay 下午加了抹茶體驗！",
            days = newDays,
            changedDayNumbers = listOf(touchedDay),
            changedSummary = listOf("- Day $touchedDay 下午加了抹茶體驗"),
            offTopic = false, failReason = null,
        )
    }

    private fun reviseSlowDownScenario(days: List<TravelGuideDay>): TravelReviseResult {
        val busiestIndex = days.indices.filter { days[it].items.isNotEmpty() }.maxByOrNull { days[it].items.size }
        if (busiestIndex == null) {
            return TravelReviseResult(reply = "目前行程還很空，應該不需要再減量囉！", days = days, failReason = null)
        }
        val busiestDay = days[busiestIndex]
        val removed = busiestDay.items.last()
        val newDays = days.mapIndexed { idx, day -> if (idx == busiestIndex) day.copy(items = day.items.dropLast(1)) else day }
        return TravelReviseResult(
            reply = "幫你把 Day ${busiestDay.day} 的節奏放慢了，先拿掉「${removed.displayTitle}」，多留點休息時間！",
            days = newDays,
            changedDayNumbers = listOf(busiestDay.day),
            changedSummary = listOf("- Day ${busiestDay.day} 移除「${removed.displayTitle}」，行程更輕鬆"),
            offTopic = false, failReason = null,
        )
    }

    /** 找不到關鍵字命中時的通用小修改，依呼叫次數輪替兩種變化，避免每次都回一模一樣的內容。 */
    private fun reviseFallbackScenario(days: List<TravelGuideDay>, targetDay: Int?, callCount: Int): TravelReviseResult {
        if (days.isEmpty()) {
            return TravelReviseResult(reply = "目前還沒有行程可以修改喔，先請我幫你排一份行程吧！", days = days, failReason = null)
        }
        val dayIndex = days.indexOfFirst { it.day == targetDay }.takeIf { it >= 0 } ?: 0
        val day = days[dayIndex]
        if (day.items.isEmpty()) {
            return TravelReviseResult(reply = "Day ${day.day} 目前還沒有安排，先幫你保留空白讓你自由運用！", days = days, failReason = null)
        }
        return if (callCount % 2 == 0 || day.items.size < 2) {
            val newItems = day.items.toMutableList()
            newItems[0] = newItems[0].copy(note = "依你的最新需求微調過囉")
            val newDays = days.mapIndexed { idx, d -> if (idx == dayIndex) d.copy(items = newItems) else d }
            TravelReviseResult(
                reply = "幫你在 Day ${day.day} 做了點小調整！",
                days = newDays,
                changedDayNumbers = listOf(day.day),
                changedSummary = listOf("- Day ${day.day} 依需求微調細節"),
                offTopic = false, failReason = null,
            )
        } else {
            val newItems = day.items.toMutableList()
            val tmp = newItems[0]
            newItems[0] = newItems[1]
            newItems[1] = tmp
            val newDays = days.mapIndexed { idx, d -> if (idx == dayIndex) d.copy(items = newItems) else d }
            TravelReviseResult(
                reply = "幫你把 Day ${day.day} 的順序調整了一下，動線更順！",
                days = newDays,
                changedDayNumbers = listOf(day.day),
                changedSummary = listOf("- Day ${day.day} 調整行程順序"),
                offTopic = false, failReason = null,
            )
        }
    }

    private val reviseSeafoodKeywords = listOf("海鮮", "過敏", "生魚片", "甲殼類")
    private val reviseTreatKeywords = listOf("抹茶", "甜點", "下午茶", "咖啡")
    private val reviseSlowDownKeywords = listOf("太累", "步調", "悠閒", "慢一點", "太趕")

    /**
     * @param callCount 本次聊天室內第幾次呼叫 travel-revise（由呼叫端遞增），只用來讓 fallback 情境輪替，
     * 不影響關鍵字命中的情境選擇。
     */
    fun travelRevise(days: List<TravelGuideDay>, targetDay: Int?, lastUserMessage: String, callCount: Int): TravelReviseResult {
        return when {
            containsAny(lastUserMessage, reviseSeafoodKeywords) -> reviseSeafoodScenario(days)
            containsAny(lastUserMessage, reviseTreatKeywords) -> reviseAddTreatScenario(days, targetDay)
            containsAny(lastUserMessage, reviseSlowDownKeywords) -> reviseSlowDownScenario(days)
            containsAny(lastUserMessage, offTopicKeywords) -> TravelReviseResult(
                reply = "哈哈這個我們晚點再聊，先專心把行程喬好吧！要不要看看目前排的內容？",
                days = days, offTopic = true, failReason = null,
            )
            else -> reviseFallbackScenario(days, targetDay, callCount)
        }
    }

    fun travelReviseSoftFailure(days: List<TravelGuideDay>): TravelReviseResult = TravelReviseResult(
        reply = "剛剛修改行程的時候卡了一下，行程先幫你保留原樣，要不要再說一次你想改的地方？",
        days = days, changedDayNumbers = emptyList(), changedSummary = emptyList(),
        offTopic = false, failReason = "llm_error",
    )

    // ============================================================
    // GET orders / wish_list / history（CompanionOrderRepository 材料）
    // ============================================================

    val upcomingOrders: List<TripOrderMaterial> = listOf(
        TripOrderMaterial(
            prodName = "大阪環球影城門票", packageName = "1 日券", destinationName = "大阪",
            oid = "26KK216164788", goDt = "2026-09-02",
        ),
        TripOrderMaterial(
            prodName = "東京晴空塔展望台門票", packageName = "標準展望台套票", destinationName = "東京",
            oid = "26KK889012345", goDt = "2026-10-15",
        ),
        TripOrderMaterial(
            prodName = "首爾南山首爾塔套票", packageName = "N首爾塔＋纜車來回", destinationName = "首爾",
            oid = "26KK773344556", goDt = "2026-11-01",
        ),
    )

    val wishProducts: List<TripProductMaterial> = listOf(
        TripProductMaterial(
            prodId = "157138", prodName = "新天鵝堡冬季一日遊",
            introduction = "冬季限定的夢幻城堡外觀，搭配周邊小鎮的聖誕市集氛圍，是德國南部最受歡迎的一日行程之一。",
            destinationNames = listOf("新天鵝堡"),
        ),
        TripProductMaterial(
            prodId = "200456", prodName = "京都嵐山竹林包車一日遊",
            introduction = "包車走訪嵐山竹林小徑、渡月橋與周邊神社，適合想避開人潮又想深度體驗京都風情的旅人。",
            destinationNames = listOf("嵐山", "京都"),
        ),
        TripProductMaterial(
            prodId = "200789", prodName = "沖繩美麗海水族館門票",
            introduction = "近距離欣賞鯨鯊悠游的黑潮之海大水槽，是沖繩親子旅遊必訪景點之一。",
            destinationNames = listOf("沖繩"),
        ),
        TripProductMaterial(
            prodId = "201122", prodName = "曼谷大皇宮半日導覽",
            introduction = "由中文導遊帶隊參觀大皇宮與玉佛寺，深入了解泰國王室歷史與建築工藝。",
            destinationNames = listOf("曼谷"),
        ),
        TripProductMaterial(
            prodId = "201567", prodName = "首爾北村韓服體驗",
            introduction = "換上傳統韓服在北村韓屋村取景拍照，體驗古代韓國的日常生活氛圍。",
            destinationNames = listOf("首爾"),
        ),
    )

    val historyProducts: List<TripProductMaterial> = listOf(
        TripProductMaterial(
            prodId = "300111", prodName = "大阪心齋橋購物街半日自由行",
            introduction = "自由穿梭於心齋橋與道頓堀之間的購物與美食路線，適合喜歡邊走邊逛的旅人。",
            destinationNames = listOf("大阪"),
        ),
        TripProductMaterial(
            prodId = "300222", prodName = "東京迪士尼樂園門票",
            introduction = "亞洲最經典的迪士尼樂園之一，適合喜歡主題樂園與卡通角色的旅人。",
            destinationNames = listOf("東京"),
        ),
        TripProductMaterial(
            prodId = "300333", prodName = "富士山河口湖一日遊",
            introduction = "從東京出發的經典一日遊，天氣好時能拍到富士山與河口湖倒影的經典畫面。",
            destinationNames = listOf("河口湖", "富士山"),
        ),
        TripProductMaterial(
            prodId = "300444", prodName = "清水寺周邊人力車體驗",
            introduction = "由專業車夫拉著人力車穿梭清水寺周邊石坂路，用不同視角感受古都風情。",
            destinationNames = listOf("清水寺", "京都"),
        ),
        TripProductMaterial(
            prodId = "300555", prodName = "濟州島海女文化體驗",
            introduction = "近距離認識濟州島特有的海女文化，並品嚐現撈海產。",
            destinationNames = listOf("濟州島"),
        ),
    )

    // ============================================================
    // 行程頁景點卡「用景點名稱找可訂商品」搜尋 catalog
    // ============================================================

    val searchCatalog: List<TripProductCard> = listOf(
        TripProductCard(id = "prod-001", name = "大阪環球影城門票", price = 2680.0, currencySymbol = "NT$", ratingStar = 4.8, ratingCount = 15234),
        TripProductCard(id = "prod-002", name = "心齋橋觀光乘車券", price = 350.0, currencySymbol = "NT$", ratingStar = 4.5, ratingCount = 892),
        TripProductCard(id = "prod-003", name = "道頓堀觀光船", price = 480.0, currencySymbol = "NT$", ratingStar = 4.6, ratingCount = 1203),
        TripProductCard(id = "prod-004", name = "清水寺周邊人力車體驗", price = 1200.0, currencySymbol = "NT$", ratingStar = 4.9, ratingCount = 456),
        TripProductCard(id = "prod-005", name = "嵐山竹林包車一日遊", price = 3200.0, currencySymbol = "NT$", ratingStar = 4.7, ratingCount = 678),
        TripProductCard(id = "prod-006", name = "伏見稻荷大社參拜體驗", price = 0.0, currencySymbol = "NT$", ratingStar = 4.8, ratingCount = 2341),
        TripProductCard(id = "prod-007", name = "東京晴空塔展望台門票", price = 990.0, currencySymbol = "NT$", ratingStar = 4.6, ratingCount = 8921),
        TripProductCard(id = "prod-008", name = "東京迪士尼樂園門票", price = 2450.0, currencySymbol = "NT$", ratingStar = 4.9, ratingCount = 23456),
        TripProductCard(id = "prod-009", name = "淺草人力車體驗", price = 1500.0, currencySymbol = "NT$", ratingStar = 4.7, ratingCount = 534),
        TripProductCard(id = "prod-010", name = "明治神宮參拜導覽", price = 600.0, currencySymbol = "NT$", ratingStar = 4.5, ratingCount = 342),
        TripProductCard(id = "prod-011", name = "首爾南山首爾塔套票", price = 780.0, currencySymbol = "NT$", ratingStar = 4.6, ratingCount = 3456),
        TripProductCard(id = "prod-012", name = "北村韓服體驗", price = 950.0, currencySymbol = "NT$", ratingStar = 4.7, ratingCount = 1234),
        TripProductCard(id = "prod-013", name = "濟州島海女文化體驗", price = 1600.0, currencySymbol = "NT$", ratingStar = 4.4, ratingCount = 187),
        TripProductCard(id = "prod-014", name = "沖繩美麗海水族館門票", price = 1050.0, currencySymbol = "NT$", ratingStar = 4.8, ratingCount = 5678),
    )
}
