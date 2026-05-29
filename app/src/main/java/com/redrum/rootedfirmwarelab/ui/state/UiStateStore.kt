package com.redrum.rootedfirmwarelab.ui.state

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import com.redrum.rootedfirmwarelab.data.UserSettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

data class ThemeConfig(
    val darkMode: Boolean = false,
    val highContrast: Boolean = false,
    val primaryArgb: Int = 0xFF3DDC97.toInt(),
    val secondaryArgb: Int = 0xFF8AB4F8.toInt(),
    val tertiaryArgb: Int = 0xFFFFB74D.toInt(),
)

data class RecentFirmwareEntry(
    val uri: String,
    val displayName: String,
    val timestamp: Long,
)

data class ToolCommandHistoryEntry(
    val toolName: String,
    val arguments: List<String>,
    val preview: String,
    val timestamp: Long,
)

data class ThemePreset(
    val label: String,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
)

class UiStateStore(context: Context, private val scope: CoroutineScope) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val settingsRepository = UserSettingsRepository(context)

    suspend fun loadThemeConfig(): ThemeConfig = ThemeConfig(
        darkMode = settingsRepository.darkModeFlow.first(),
        highContrast = settingsRepository.highContrastFlow.first(),
        primaryArgb = prefs.getInt(KEY_PRIMARY, DEFAULT_PRIMARY),
        secondaryArgb = prefs.getInt(KEY_SECONDARY, DEFAULT_SECONDARY),
        tertiaryArgb = prefs.getInt(KEY_TERTIARY, DEFAULT_TERTIARY),
    )

    fun saveThemeConfig(config: ThemeConfig) {
        scope.launch {
            settingsRepository.setDarkMode(config.darkMode)
            settingsRepository.setHighContrast(config.highContrast)
            settingsRepository.setThemeColors(config.primaryArgb, config.secondaryArgb, config.tertiaryArgb)
        }
    }

    fun loadRecentFirmware(): List<RecentFirmwareEntry> = readJsonArray(KEY_RECENT_FIRMWARE) { json ->
        RecentFirmwareEntry(
            uri = json.optString("uri"),
            displayName = json.optString("displayName"),
            timestamp = json.optLong("timestamp"),
        )
    }

    fun addRecentFirmware(entry: RecentFirmwareEntry) {
        val current = loadRecentFirmware()
            .filterNot { it.uri == entry.uri }
            .toMutableList()
        current.add(0, entry)
        writeJsonArray(KEY_RECENT_FIRMWARE, current.take(MAX_RECENTS)) { item ->
            JSONObject()
                .put("uri", item.uri)
                .put("displayName", item.displayName)
                .put("timestamp", item.timestamp)
        }
    }

    fun loadFavoriteTools(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITE_TOOLS, emptySet())?.toSet() ?: emptySet()
    }

    fun saveFavoriteTools(toolNames: Set<String>) {
        prefs.edit().putStringSet(KEY_FAVORITE_TOOLS, toolNames.toSet()).apply()
    }

    fun loadCommandHistory(): List<ToolCommandHistoryEntry> = readJsonArray(KEY_COMMAND_HISTORY) { json ->
        ToolCommandHistoryEntry(
            toolName = json.optString("toolName"),
            arguments = json.optJSONArray("arguments")?.toStringList().orEmpty(),
            preview = json.optString("preview"),
            timestamp = json.optLong("timestamp"),
        )
    }

    fun addCommandHistory(entry: ToolCommandHistoryEntry) {
        val current = loadCommandHistory()
            .filterNot { it.preview == entry.preview && it.toolName == entry.toolName }
            .toMutableList()
        current.add(0, entry)
        writeJsonArray(KEY_COMMAND_HISTORY, current.take(MAX_HISTORY)) { item ->
            JSONObject()
                .put("toolName", item.toolName)
                .put("arguments", JSONArray(item.arguments))
                .put("preview", item.preview)
                .put("timestamp", item.timestamp)
        }
    }

    fun isFirstRunComplete(): Boolean = prefs.getBoolean(KEY_FIRST_RUN_COMPLETE, false)

    fun markFirstRunComplete() {
        prefs.edit().putBoolean(KEY_FIRST_RUN_COMPLETE, true).apply()
    }

    fun resetFirstRun() {
        prefs.edit().putBoolean(KEY_FIRST_RUN_COMPLETE, false).apply()
    }

    private fun <T> readJsonArray(key: String, mapper: (JSONObject) -> T): List<T> {
        val raw = prefs.getString(key, null) ?: return emptyList()
        val array = runCatching { JSONArray(raw) }.getOrNull() ?: return emptyList()
        return buildList {
            for (index in 0 until array.length()) {
                val obj = array.optJSONObject(index) ?: continue
                add(mapper(obj))
            }
        }
    }

    private fun <T> writeJsonArray(
        key: String,
        items: List<T>,
        mapper: (T) -> JSONObject,
    ) {
        val array = JSONArray()
        items.forEach { array.put(mapper(it)) }
        prefs.edit().putString(key, array.toString()).apply()
    }

    private fun JSONArray.toStringList(): List<String> {
        return buildList {
            for (index in 0 until length()) {
                add(optString(index))
            }
        }
    }

    companion object {
        private const val PREFS_NAME = "rooted_firmware_lab_ui"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_HIGH_CONTRAST = "high_contrast"
        private const val KEY_PRIMARY = "primary_argb"
        private const val KEY_SECONDARY = "secondary_argb"
        private const val KEY_TERTIARY = "tertiary_argb"
        private const val KEY_RECENT_FIRMWARE = "recent_firmware"
        private const val KEY_FAVORITE_TOOLS = "favorite_tools"
        private const val KEY_COMMAND_HISTORY = "command_history"
        private const val KEY_FIRST_RUN_COMPLETE = "first_run_complete"
        private const val MAX_RECENTS = 6
        private const val MAX_HISTORY = 8

        const val DEFAULT_PRIMARY = 0xFF3DDC97.toInt()
        const val DEFAULT_SECONDARY = 0xFF8AB4F8.toInt()
        const val DEFAULT_TERTIARY = 0xFFFFB74D.toInt()
    }
}

fun ThemeConfig.toPreservedColorSchemeValues(): List<Int> = listOf(primaryArgb, secondaryArgb, tertiaryArgb)
