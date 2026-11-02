package me.zhanghai.android.files.filelist

import android.content.Context
import android.content.SharedPreferences
import me.zhanghai.android.files.app.application
import org.json.JSONObject

// Простая структура для хранения наших данных
data class FolderCustomization(val color: Int, val emoji: String)

// Объект-одиночка, который будет управлять сохранением и чтением
object FolderCustomizationStore {
    private const val PREFS_NAME = "folder_customizations"
    private const val KEY_COLOR = "color"
    private const val KEY_EMOJI = "emoji"

    // Ленивая инициализация хранилища
    private val prefs: SharedPreferences by lazy {
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Метод для сохранения цвета и эмодзи по абсолютному пути папки
    fun saveCustomization(path: String, color: Int, emoji: String) {
        val json = JSONObject().apply {
            put(KEY_COLOR, color)
            put(KEY_EMOJI, emoji)
        }
        prefs.edit().putString(path, json.toString()).apply()
    }

    // Метод для получения цвета и эмодзи
    fun getCustomization(path: String): FolderCustomization? {
        val jsonString = prefs.getString(path, null) ?: return null
        return try {
            val json = JSONObject(jsonString)
            val color = json.getInt(KEY_COLOR)
            val emoji = json.optString(KEY_EMOJI, "")
            FolderCustomization(color, emoji)
        } catch (e: Exception) {
            null // Если что-то пошло не так при чтении, возвращаем null (стандартный вид папки)
        }
    }

    // Метод для сброса настроек папки
    fun removeCustomization(path: String) {
        prefs.edit().remove(path).apply()
    }
}
