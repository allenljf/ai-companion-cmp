package com.allenljf.aicompanion.data

import com.allenljf.aicompanion.model.CompanionProfile
import com.allenljf.aicompanion.model.QuizHistoryRecord
import com.allenljf.aicompanion.model.SavedTripRecord
import com.russhwolf.settings.Settings
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 原始碼移植自 KKday `CompanionRepositoryImpl` 的 DataStore 存取邏輯（唯讀參照：
 * reference/android-src/data-repository/CompanionRepositoryImpl.kt），對應四把 key：
 * companion_profile / companion_shown_questions / companion_quiz_history / companion_saved_trips。
 *
 * 去 B2C 化調整：原本以 memberUuid 區隔多會員資料（`memberScopedKey`），demo 只有單一使用者，
 * 拿掉這層 scoping 直接用固定 key。Gson → kotlinx.serialization：手動 Json.encodeToString /
 * decodeFromString 存成單一字串（沒用 multiplatform-settings-serialization 的 encodeValue，
 * 行為完全自己掌握，出錯訊息直接看得到）。
 */
class LocalCompanionStore(private val settings: Settings) {

    private val json = Json { ignoreUnknownKeys = true }

    fun getLocalCompanion(): CompanionProfile? =
        settings.getStringOrNull(KEY_COMPANION_PROFILE)?.let { json.decodeFromString<CompanionProfile>(it) }

    fun saveLocalCompanion(profile: CompanionProfile) {
        settings.putString(KEY_COMPANION_PROFILE, json.encodeToString(profile))
    }

    fun clearLocalCompanion() {
        settings.remove(KEY_COMPANION_PROFILE)
    }

    fun getShownQuestionCounts(): Map<String, Int> =
        settings.getStringOrNull(KEY_SHOWN_QUESTIONS)
            ?.let { json.decodeFromString(MapSerializer(String.serializer(), Int.serializer()), it) }
            .orEmpty()

    fun saveShownQuestionCounts(counts: Map<String, Int>) {
        settings.putString(KEY_SHOWN_QUESTIONS, json.encodeToString(MapSerializer(String.serializer(), Int.serializer()), counts))
    }

    fun getQuizHistory(): List<QuizHistoryRecord> =
        settings.getStringOrNull(KEY_QUIZ_HISTORY)
            ?.let { json.decodeFromString(ListSerializer(QuizHistoryRecord.serializer()), it) }
            .orEmpty()

    fun saveQuizHistory(records: List<QuizHistoryRecord>) {
        settings.putString(KEY_QUIZ_HISTORY, json.encodeToString(ListSerializer(QuizHistoryRecord.serializer()), records))
    }

    fun getSavedTrips(): List<SavedTripRecord> =
        settings.getStringOrNull(KEY_SAVED_TRIPS)
            ?.let { json.decodeFromString(ListSerializer(SavedTripRecord.serializer()), it) }
            .orEmpty()

    fun saveSavedTrips(records: List<SavedTripRecord>) {
        settings.putString(KEY_SAVED_TRIPS, json.encodeToString(ListSerializer(SavedTripRecord.serializer()), records))
    }

    private companion object {
        const val KEY_COMPANION_PROFILE = "companion_profile"
        const val KEY_SHOWN_QUESTIONS = "companion_shown_questions"
        const val KEY_QUIZ_HISTORY = "companion_quiz_history"
        const val KEY_SAVED_TRIPS = "companion_saved_trips"
    }
}
