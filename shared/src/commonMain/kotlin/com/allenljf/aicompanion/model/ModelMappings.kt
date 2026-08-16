package com.allenljf.aicompanion.model

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

fun AiPartnerDataResponse.toDomain() = variant.let {
    AiPartnerResult(
        personality = it.personality.map { opt -> CompanionTraitOption(opt.tag, opt.label, opt.description.orEmpty()) },
        speechStyle = it.speechStyle.map { opt -> CompanionTraitOption(opt.tag, opt.label, opt.description.orEmpty()) },
        gender = it.gender.map { opt -> CompanionAppearanceOption(opt.tag, opt.label) },
        outfit = it.outfit.map { opt -> CompanionAppearanceOption(opt.tag, opt.label) },
        hairStyle = it.hairStyle.map { opt -> CompanionAppearanceOption(opt.tag, opt.label) },
        hairColor = it.hairColor.map { opt -> CompanionAppearanceOption(opt.tag, opt.label) },
        avatars = it.avatars
    )
}

fun QuizDataResponse.toDomain() = QuizResult(
    count = count,
    failReason = failReason,
    questions = questions.map { it.toDomain() }
)

fun QuizQuestionResponse.toDomain() = QuizQuestion(
    id = id,
    dimensionId = dimensionId,
    index = index,
    type = type,
    mode = mode,
    text = text,
    options = options.map { it.toDomain() }
)

fun QuizOptionResponse.toDomain() = QuizOption(
    id = id,
    index = index,
    text = text,
    imageUrl = imageUrl.orEmpty(),
    tagId = tag?.id.orEmpty(),
    tagLabel = tag?.label.orEmpty()
)

fun QuizCompletionDataResponse.toDomain() = QuizCompletionResult(
    quizCompletionId = quizCompletionId,
    travelIdentity = travelIdentity,
    travelIdentityEn = travelIdentityEn,
    destinationCn = destinationCn,
    destinationEn = destinationEn,
    destinationCountry = destinationCountry,
    destinationCountryEn = destinationCountryEn,
    tagline = tagline,
    taglineEn = taglineEn,
    highlightTags = highlightTags,
    highlightTagsEn = highlightTagsEn,
    companionQuote = companionQuote,
    companionQuoteEn = companionQuoteEn,
    recommendation = recommendation,
    reasoning = reasoning,
    socialPost = socialPost,
    shareImageStatus = shareImageStatus,
    failReason = failReason
)

fun ShareImageV2DataResponse.toDomain() = ShareImageV2Result(
    heroUrl = heroUrl,
    stampUrl = decorations.stampUrl,
    // 保留 null 佔位（轉空字串）：list 與 highlight_tags 逐位對應，filterNotNull 會讓 icon 和標籤錯位；
    // 空字串在 UI 端落到 CompanionAsyncImage 的 placeholder 圓（見 ShareImageV2TagIconsRow）
    tagIconUrls = decorations.tagIconUrls.map { it.orEmpty() },
    content = ShareImageV2Content(
        travelIdentity = content.travelIdentity,
        travelIdentityEn = content.travelIdentityEn,
        destinationCn = content.destinationCn,
        destinationEn = content.destinationEn,
        tagline = content.tagline,
        highlightTags = content.highlightTags,
        companionQuote = content.companionQuote,
        companionName = content.companionName.orEmpty()
    ),
    failReason = failReason
)

fun SelfIntroductionDataResponse.toDomain() = SelfIntroductionResult(
    introduction = introduction,
    failReason = failReason
)

fun TravelSummaryDataResponse.toDomain() = TravelSummaryResult(
    summary = summary,
    city = city,
    failReason = failReason
)

fun TravelSummaryFromOrdersDataResponse.toDomain() = TravelSummaryFromOrdersResult(
    greeting = greeting,
    options = options.map { TravelDestinationOption(orderIndex = it.orderIndex, city = it.city) },
    failReason = failReason,
)

// GET orders 的訂單 → 開場材料（比照 reference CompanionOrderRepositoryImpl：destinations 取第一筆當代表城市）
fun OrderResponse.toMaterial() = TripOrderMaterial(
    prodName = prodName,
    packageName = packageName,
    destinationName = destination?.destinations?.firstOrNull()?.name.orEmpty(),
    oid = id,
    goDt = goDt.orEmpty(),
)

fun TripOrderMaterial.toRequestModel() = TripOrderMaterialRequest(
    prodName = prodName,
    packageName = packageName,
    destinationName = destinationName,
)

private const val PRODUCT_INTRODUCTION_MAX_LENGTH = 500

// GET wish_list／GET history 的商品 → 開場材料（每筆萃取 4 個欄位，簡介截斷到後端上限內）
fun CompanionProductResponse.toMaterial() = TripProductMaterial(
    prodId = prodMid?.toString().orEmpty(),
    prodName = name,
    introduction = introduction.orEmpty().take(PRODUCT_INTRODUCTION_MAX_LENGTH),
    destinationNames = destinations.map { it.name }.filter { it.isNotBlank() },
)

fun TripProductMaterial.toRequestModel() = TripPlanProductRequest(
    prodId = prodId,
    prodName = prodName,
    introduction = introduction.takeIf { it.isNotBlank() },
    destinationNames = destinationNames.takeIf { it.isNotEmpty() },
)

fun TravelSummaryFromProductsDataResponse.toDomain() = TravelSummaryFromProductsResult(
    greeting = greeting,
    cities = cities
        .filter { it.city.isNotBlank() }
        .map { city ->
            TripCityProducts(
                city = city.city,
                products = city.products
                    .filter { it.prodId.isNotBlank() }
                    .map { TripProductRef(prodId = it.prodId, prodName = it.prodName) },
            )
        },
    failReason = failReason,
)

fun RecommendCityDataResponse.toDomain() = RecommendCityResult(
    reply = reply,
    quickReplies = quickReplies,
    recommendedCity = recommendedCity,
    cityReason = cityReason,
    isFinal = isFinal,
    offTopic = offTopic,
    round = round,
    maxRounds = maxRounds,
    remainingRounds = remainingRounds,
    swapLimitReached = swapLimitReached,
    failReason = failReason
)

fun TravelGuideDataResponse.toDomain() = TravelGuideResult(
    city = city.orEmpty(),
    totalDays = days,
    phase = phase,
    unplannedDays = unplannedDays,
    pendingFields = pendingFields,
    messages = messages.map { it.text }.filter { it.isNotBlank() },
    days = itineraryPatch?.days?.map { it.toDomain() }.orEmpty(),
    progressLabel = progressLabel,
    mainActionLabel = mainAction?.label?.takeIf { it.isNotBlank() },
    heroImageUrl = heroImageUrl.orEmpty(),
    failReason = failReason
)

fun TravelReviseDataResponse.toDomain() = TravelReviseResult(
    reply = reply,
    days = itineraryPatch?.days?.map { it.toDomain() }.orEmpty(),
    changedDayNumbers = itineraryPatch?.changedDays.orEmpty(),
    // 文件規格為陣列、SIT 實測回單一字串：兩種形狀都容錯
    changedSummary = changedSummary.let { element ->
        when (element) {
            null, is JsonNull -> emptyList()
            is JsonArray -> element.mapNotNull { item -> (item as? JsonPrimitive)?.contentOrNull }
            is JsonPrimitive -> listOfNotNull(element.contentOrNull)
            else -> emptyList()
        }
    },
    offTopic = offTopic,
    failReason = failReason
)

/** domain → request DTO：travel-revise 需把 App 本地保存的行程原樣帶回給後端。 */
fun TravelGuideDay.toRequestModel() = TravelGuideDayResponse(
    day = day,
    status = status,
    kind = kind,
    halfDay = halfDay,
    items = items.map {
        TravelGuideItemResponse(
            name = it.name.takeIf { name -> name.isNotBlank() },
            text = it.text,
            type = it.type,
            time = it.time.takeIf { time -> time.isNotBlank() },
            timeBand = it.timeBand.takeIf { band -> band.isNotBlank() },
            transportMode = it.transportMode.takeIf { mode -> mode.isNotBlank() },
            note = it.note.takeIf { note -> note.isNotBlank() },
            lat = it.lat,
            lng = it.lng,
            // oid 必須原樣送回：travel-revise 依此保護已預訂項目（LLM 不可刪除、不可改）
            oid = it.oid.takeIf { oid -> oid.isNotBlank() },
            // prod_id 必須原樣送回：travel-revise 自動併入白名單保護（非必要不主動刪除）
            prodId = it.prodId.takeIf { id -> id.isNotBlank() }
        )
    }
)

fun TravelGuideDayResponse.toDomain() = TravelGuideDay(
    day = day,
    status = status,
    kind = kind,
    halfDay = halfDay,
    bookedAnchorOids = bookedAnchor?.oids.orEmpty(),
    items = items.map {
        TravelGuideDayItem(
            name = it.name.orEmpty(),
            text = it.text,
            type = it.type,
            time = it.time.orEmpty(),
            timeBand = it.timeBand.orEmpty(),
            transportMode = it.transportMode.orEmpty(),
            note = it.note.orEmpty(),
            lat = it.lat,
            lng = it.lng,
            oid = it.oid.orEmpty(),
            prodId = it.prodId.orEmpty()
        )
    }
)

fun TripOrderMaterial.toTripPlanOrderRequest() = TripPlanOrderRequest(
    oid = oid,
    prodName = prodName,
    packageName = packageName.takeIf { it.isNotBlank() },
    destinationName = destinationName.takeIf { it.isNotBlank() },
    goDt = goDt.takeIf { it.isNotBlank() },
)

fun QuizGalleryDataResponse.toDomain() = QuizGalleryResult(
    count = count,
    items = items.map { it.toDomain() }
)

fun QuizGalleryItemResponse.toDomain() = QuizGalleryItem(
    travelIdentity = travelIdentity,
    travelIdentityEn = travelIdentityEn,
    destinationCn = destinationCn,
    destinationEn = destinationEn,
    destinationCountry = destinationCountry,
    tagline = tagline,
    highlightTags = highlightTags,
    companionQuote = companionQuote,
    shareImageUrl = shareImageUrl,
    companionName = companionName,
    partnerAvatarUrl = partnerAvatarUrl,
    createdAt = createdAt
)
