package com.redrum.rootedfirmwarelab.core.service

import com.redrum.rootedfirmwarelab.core.model.PluginDefinition
import org.json.JSONObject
import java.io.File

class PluginRegistry(private val pluginsDir: File) {
    fun listPlugins(): List<PluginDefinition> {
        if (!pluginsDir.exists()) return emptyList()
        return pluginsDir.walkTopDown()
            .filter { it.isFile && it.name == "plugin.json" }
            .mapNotNull { runCatching { parsePlugin(it.readText(), it.parentFile ?: pluginsDir) }.getOrNull() }
            .sortedBy { it.id }
            .toList()
    }

    fun validate(plugin: PluginDefinition): List<String> {
        val findings = mutableListOf<String>()
        if (!plugin.id.matches(Regex("[a-zA-Z0-9_.-]+"))) findings += "Plugin id contains unsupported characters."
        if (plugin.entrypoint.isBlank()) findings += "Plugin entrypoint is required."
        if ("flash" in plugin.permissions && "root" !in plugin.permissions) findings += "Flash permission requires root permission."
        return findings
    }

    private fun parsePlugin(raw: String, baseDir: File): PluginDefinition {
        val json = JSONObject(raw)
        val permissions = json.optJSONArray("permissions")?.let { array ->
            buildList {
                for (index in 0 until array.length()) add(array.optString(index))
            }
        }.orEmpty()
        val argsObject = json.optJSONObject("arguments")
        val arguments = buildMap {
            if (argsObject != null) {
                argsObject.keys().forEach { key -> put(key, argsObject.optString(key)) }
            }
        }
        val entrypoint = json.getString("entrypoint")
        return PluginDefinition(
            id = json.getString("id"),
            displayName = json.optString("displayName", json.getString("id")),
            version = json.optString("version", "0.1.0"),
            entrypoint = File(baseDir, entrypoint).absolutePath,
            description = json.optString("description"),
            permissions = permissions,
            arguments = arguments,
        )
    }
}
