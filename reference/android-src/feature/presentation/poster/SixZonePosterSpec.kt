package com.kkday.feature.ai_companion.presentation.poster

/**
 * 分享海報版面規格：Hero 圖完整顯示在最上方（不裁切、不疊加），下方依序接著文字資料（旅行人格、
 * 目的地、tagline、旅伴語錄）、右上角疊放目的地郵戳、再往下是三個 tag 圓圈與 hashtag。
 * 畫布寬度固定 1152px，高度依內容自然堆疊，不強制固定總高。
 */
object SixZonePosterSpec {
    const val CANVAS_WIDTH = 1152

    // Hero 完整顯示於頂部：後端 hero_url 實際回傳尺寸固定為 1152x2048，故沿用同一尺寸，
    // 避免用較小的目標高度 centerCrop 把圖片下半部裁掉
    const val HERO_WIDTH = CANVAS_WIDTH
    const val HERO_HEIGHT = 2048

    // 目的地郵戳：疊放於文字區塊右上角
    const val STAMP_SIZE = 168

    // 三個 tag 圓圈：直徑、白色外框寬度
    const val TAG_CIRCLE_SIZE = 168
    const val TAG_CIRCLE_BORDER_WIDTH = 6

    // 靜態分享圖 QR／品牌 footer
    const val QR_SIZE = 72
}
