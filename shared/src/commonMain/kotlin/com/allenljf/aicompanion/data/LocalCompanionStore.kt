package com.allenljf.aicompanion.data

import com.allenljf.aicompanion.model.CompanionProfile
import com.allenljf.aicompanion.model.QuizHistoryRecord
import com.allenljf.aicompanion.model.SavedTripRecord
import com.russhwolf.settings.Settings
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Cross-platform local storage for the companion profile, quiz history, and saved trips. */
class LocalCompanionStore(private val settings: Settings) {

    private val json = Json { ignoreUnknownKeys = true }

    fun getLocalCompanion(): CompanionProfile? =
        decodeOrNull(KEY_COMPANION_PROFILE) { json.decodeFromString<CompanionProfile>(it) }

    fun saveLocalCompanion(profile: CompanionProfile) {
        settings.putString(KEY_COMPANION_PROFILE, json.encodeToString(profile))
    }

    fun clearLocalCompanion() {
        settings.remove(KEY_COMPANION_PROFILE)
    }

    fun getShownQuestionCounts(): Map<String, Int> =
        decodeOrNull(KEY_SHOWN_QUESTIONS) {
            json.decodeFromString(MapSerializer(String.serializer(), Int.serializer()), it)
        }.orEmpty()

    fun saveShownQuestionCounts(counts: Map<String, Int>) {
        settings.putString(KEY_SHOWN_QUESTIONS, json.encodeToString(MapSerializer(String.serializer(), Int.serializer()), counts))
    }

    fun getQuizHistory(): List<QuizHistoryRecord> =
        decodeOrNull(KEY_QUIZ_HISTORY) {
            json.decodeFromString(ListSerializer(QuizHistoryRecord.serializer()), it)
        }.orEmpty()

    fun saveQuizHistory(records: List<QuizHistoryRecord>) {
        settings.putString(KEY_QUIZ_HISTORY, json.encodeToString(ListSerializer(QuizHistoryRecord.serializer()), records))
    }

    fun getSavedTrips(): List<SavedTripRecord> =
        decodeOrNull(KEY_SAVED_TRIPS) {
            json.decodeFromString(ListSerializer(SavedTripRecord.serializer()), it)
        }.orEmpty()

    fun saveSavedTrips(records: List<SavedTripRecord>) {
        settings.putString(KEY_SAVED_TRIPS, json.encodeToString(ListSerializer(SavedTripRecord.serializer()), records))
    }

    /**
     * 統一防護：壞資料（手動改過的 settings、跨版本欄位不相容等）解不出來就當作沒存過，
     * 順手把壞資料 remove 掉，避免每次讀都拋例外把 app 弄炸。
     */
    private inline fun <T> decodeOrNull(key: String, decode: (String) -> T): T? {
        val raw = settings.getStringOrNull(key) ?: return null
        return try {
            decode(raw)
        } catch (e: SerializationException) {
            settings.remove(key)
            null
        }
    }

    private companion object {
        const val KEY_COMPANION_PROFILE = "companion_profile"
        const val KEY_SHOWN_QUESTIONS = "companion_shown_questions"
        const val KEY_QUIZ_HISTORY = "companion_quiz_history"
        const val KEY_SAVED_TRIPS = "companion_saved_trips"
    }
}
