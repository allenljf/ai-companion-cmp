package com.kkday.data.repository.companion

import com.google.gson.JsonObject
import com.kkday.library.networking.core.api.B2CApiResponse
import com.kkday.util.gson.GsonInstance
import retrofit2.HttpException

/**
 * Companion 業務錯誤。code 對齊後端錯誤碼（C005 題庫空、C007 分析快取過期）。
 */
class CompanionApiException(val code: String, desc: String) : Exception(desc) {
    companion object {
        const val QUIZ_BANK_EMPTY = "C005"
        const val QUIZ_SESSION_NOT_FOUND = "C007"

        // Phase 2 驗證錯誤（request body 有誤，重試不會成功，屬 client bug）：
        // 實測為 HTTP 400 + metadata.status=110001（全站共用 invalid_params，parseBusinessError 可直接解析）；
        // 422 為舊版文件的 Laravel 預設格式推測值，保留兼容
        const val INVALID_PARAMS = "110001"
        const val VALIDATION_ERROR = "422"
        private const val HTTP_UNPROCESSABLE_ENTITY = 422

        fun fromApi(status: String, desc: String) = CompanionApiException(status, desc)

        fun fromHttpException(e: HttpException): CompanionApiException? {
            val body = e.response()?.errorBody()?.string() ?: return null
            parseBusinessError(body)?.let { return it }
            // Phase 2 FormRequest 走 Laravel 預設驗證錯誤格式 {"message":"...","errors":{...}}，
            // 非 {metadata,data} 信封；代表呼叫方 request 本身有問題，不應原樣重試
            if (e.code() == HTTP_UNPROCESSABLE_ENTITY) {
                val message = try {
                    GsonInstance.fromJson(body, JsonObject::class.java)?.get("message")?.asString
                } catch (_: Exception) {
                    null
                }
                return CompanionApiException(VALIDATION_ERROR, message ?: "Validation failed")
            }
            return null
        }

        /**
         * 同時處理兩種錯誤信封：
         * 1. 巢狀：{"metadata":{"status":"Cxxx",...},"data":null}
         * 2. 扁平：{"status":"Cxxx","desc":"...","data":null}（無 metadata 外層）
         * TODO: 後端確認 C005/C007 實際回傳的信封形狀與 HTTP status 後，收斂此處。
         */
        fun parseBusinessError(body: String): CompanionApiException? {
            return try {
                val response = GsonInstance.fromJson(body, B2CApiResponse::class.java)
                val meta = response.metadata
                if (meta != null) {
                    CompanionApiException(meta.status, meta.getDesc())
                } else {
                    val obj = GsonInstance.fromJson(body, JsonObject::class.java)
                    val status = obj?.get("status")?.asString ?: return null
                    val desc = obj.get("desc")?.asString ?: status
                    CompanionApiException(status, desc)
                }
            } catch (_: Exception) {
                null
            }
        }
    }
}

/** metadata 非成功即拋 CompanionApiException（巢狀情形）。 */
fun <T> B2CApiResponse<T>.checkCompanionMetadata() {
    val meta = metadata ?: return
    if (!meta.isResultSuccess()) {
        throw CompanionApiException.fromApi(meta.status, meta.getDesc())
    }
}

fun Exception.toCompanionApiException(): Exception {
    if (this is HttpException) {
        return CompanionApiException.fromHttpException(this) ?: this
    }
    return this
}
