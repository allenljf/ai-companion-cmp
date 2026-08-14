package com.kkday.feature.ai_companion.presentation.poster

import androidx.annotation.DrawableRes
import com.kkday.member.resource.R

/**
 * fallback_category → 內建 bundled 素材，對應 spec「Per-Slot Asset Fallback」。
 * 類別字串直接來自後端 hero_fallback_category / stamp_fallback_category / tag_fallback_categories，
 * 任何未知類別一律退到各自的 generic 版本，避免 App 端因後端新增分類而崩潰。
 */
object PosterFallbackAssets {

    @DrawableRes
    fun heroDrawable(category: String): Int = when (category) {
        "urban" -> R.drawable.companion_hero_fallback_urban
        "coast" -> R.drawable.companion_hero_fallback_coast
        "mountain" -> R.drawable.companion_hero_fallback_mountain
        "nature" -> R.drawable.companion_hero_fallback_nature
        "culture" -> R.drawable.companion_hero_fallback_culture
        else -> R.drawable.companion_hero_fallback_generic
    }

    @DrawableRes
    fun stampDrawable(): Int = R.drawable.companion_stamp_fallback_generic

    @DrawableRes
    fun tagDrawable(category: String): Int = when (category) {
        "food" -> R.drawable.companion_tag_fallback_food
        "adventure" -> R.drawable.companion_tag_fallback_adventure
        "relaxation" -> R.drawable.companion_tag_fallback_relaxation
        "culture" -> R.drawable.companion_tag_fallback_culture
        "shopping" -> R.drawable.companion_tag_fallback_shopping
        "nightlife" -> R.drawable.companion_tag_fallback_nightlife
        else -> R.drawable.companion_tag_fallback_generic
    }
}
