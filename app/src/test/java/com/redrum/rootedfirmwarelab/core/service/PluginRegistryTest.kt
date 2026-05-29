package com.redrum.rootedfirmwarelab.core.service

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PluginRegistryTest {
    @Test
    fun listPluginsParsesPluginJson() {
        val root = File.createTempFile("plugins", "").apply {
            delete()
            mkdirs()
        }
        val pluginDir = File(root, "example.plugin").apply { mkdirs() }
        File(pluginDir, "plugin.json").writeText(
            """
            {
              "id": "example.plugin",
              "displayName": "Example Plugin",
              "version": "1.0.0",
              "entrypoint": "run.sh",
              "description": "demo",
              "permissions": ["root"],
              "arguments": {"mode": "safe"}
            }
            """.trimIndent(),
        )
        File(pluginDir, "run.sh").writeText("#!/bin/sh\n")

        val registry = PluginRegistry(root)
        val plugins = registry.listPlugins()

        assertTrue(plugins.any { it.id == "example.plugin" && it.entrypoint.endsWith("run.sh") })
    }

    @Test
    fun validateFlagsInvalidIdsAndMissingRootPermissionForFlash() {
        val registry = PluginRegistry(File("/tmp/does-not-matter"))
        val findings = registry.validate(
            com.redrum.rootedfirmwarelab.core.model.PluginDefinition(
                id = "bad id!",
                displayName = "Bad",
                version = "1.0.0",
                entrypoint = "run.sh",
                permissions = listOf("flash"),
            ),
        )

        assertTrue(findings.any { it.contains("Plugin id contains unsupported characters.") })
        assertTrue(findings.any { it.contains("Flash permission requires root permission.") })
    }
}
