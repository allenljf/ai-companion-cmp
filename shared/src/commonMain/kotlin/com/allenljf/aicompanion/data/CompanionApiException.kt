package com.allenljf.aicompanion.data

/**
 * Companion 業務例外。code 對齊後端錯誤碼（C005 題庫空、C007 分析快取過期、110001 全站共用參數錯誤）。
 *
 * 原始 KKday 版本另有 `fromApi`/`fromHttpException`/`parseBusinessError`/`checkCompanionMetadata`：
 * 那些是解析 `{metadata,data}` B2C envelope 與 Retrofit `HttpException` 的網路層細節，換自己的後端後
 * 信封形狀會不同，屬於 T17（Ktor 串接）該重新設計的部分——這裡先留錯誤碼常數與最小建構子供 domain 層使用。
 */
class CompanionApiException(val code: String, desc: String) : Exception(desc) {
    companion object {
        const val QUIZ_BANK_EMPTY = "C005"
        const val QUIZ_SESSION_NOT_FOUND = "C007"

        // Phase 2 驗證錯誤（request body 有誤，重試不會成功，屬 client bug）
        const val INVALID_PARAMS = "110001"
        const val VALIDATION_ERROR = "422"
    }
}
