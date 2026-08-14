package com.kkday.model.companion

fun AiPartnerDataResponse.toDomain() = AiPartnerResult(
    personality = personality.map { CompanionTraitOption(it.tag, it.label, it.description.orEmpty()) },
    speechStyle = speechStyle.map { CompanionTraitOption(it.tag, it.label, it.description.orEmpty()) },
    gender = gender.map { CompanionAppearanceOption(it.tag, it.label) },
    outfit = outfit.map { CompanionAppearanceOption(it.tag, it.label) },
    hairStyle = hairStyle.map { CompanionAppearanceOption(it.tag, it.label) },
    hairColor = hairColor.map { CompanionAppearanceOption(it.tag, it.label) },
    avatars = avatars
)

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
    status = ShareImageV2Status.fromValue(status),
    content = content.toDomain(),
    hero = AssetSlot(url = heroUrl, fallbackCategory = heroFallbackCategory),
    stamp = AssetSlot(url = decorations.stampUrl, fallbackCategory = decorations.stampFallbackCategory),
    tagIcons = decorations.tagIconUrls.mapIndexed { index, url ->
        AssetSlot(url = url, fallbackCategory = decorations.tagFallbackCategories.getOrNull(index).orEmpty())
    }
)

fun ShareImageV2ContentResponse.toDomain() = ShareImageV2Content(
    travelIdentity = travelIdentity,
    travelIdentityEn = travelIdentityEn,
    destinationCn = destinationCn,
    destinationEn = destinationEn,
    tagline = tagline,
    highlightTags = highlightTags,
    companionQuote = companionQuote,
    companionName = companionName.orEmpty()
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
    failReason = failReason
)

fun TravelReviseDataResponse.toDomain() = TravelReviseResult(
    reply = reply,
    days = itineraryPatch?.days?.map { it.toDomain() }.orEmpty(),
    changedDayNumbers = itineraryPatch?.changedDays.orEmpty(),
    // 文件規格為陣列、SIT 實測回單一字串：兩種形狀都容錯
    changedSummary = changedSummary.let { element ->
        when {
            element == null || element.isJsonNull -> emptyList()
            element.isJsonArray -> element.asJsonArray.mapNotNull { item ->
                item.takeIf { it.isJsonPrimitive }?.asString
            }
            element.isJsonPrimitive -> listOf(element.asString)
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
