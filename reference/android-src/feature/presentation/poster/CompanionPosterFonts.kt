package com.kkday.feature.ai_companion.presentation.poster

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.kkday.member.resource.R

/**
 * 六區合成海報唯一主字體：辰宇落雁。Compose FontFamily 的 fallback fonts 機制會在缺字時
 * 自動改用清單中下一個字型（芫栎），不會落回系統預設字體。
 * TODO: replace with official font file — 目前 chenyuluoyan_2_0_thin.ttf / iansui_regular.ttf
 * 為暫代字型（複製自既有 noto_sans_medium.ttf），待 PM/設計提供正式檔案後直接替換同名檔案即可。
 */
val CompanionPosterFontFamily = FontFamily(
    Font(R.font.chenyuluoyan_2_0_thin),
    Font(R.font.iansui_regular),
)
