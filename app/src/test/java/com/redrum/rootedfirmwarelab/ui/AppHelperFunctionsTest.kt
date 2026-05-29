package com.redrum.rootedfirmwarelab.ui

import com.redrum.rootedfirmwarelab.core.service.ToolLibrary
import com.redrum.rootedfirmwarelab.data.LogEntry
import com.redrum.rootedfirmwarelab.data.LogType
import com.redrum.rootedfirmwarelab.ui.state.RecentFirmwareEntry
import com.redrum.rootedfirmwarelab.ui.state.ToolCommandHistoryEntry
import com.redrum.rootedfirmwarelab.ui.state.UiStateStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Date

class AppHelperFunctionsTest {
    @Test
    fun parseMetadataHandlesJsonAndBadInput() {
        assertEquals(emptyMap<String, String>(), parseMetadata(null))
        assertEquals(emptyMap<String, String>(), parseMetadata("not-json"))
        assertEquals(mapOf("a" to "1", "b" to "two"), parseMetadata("""{"a":"1","b":"two"}"""))
    }

    @Test
    fun ensureArgumentCountPadsValuesForTemplate() {
        val tool = ToolLibrary.availableTools.first { it.name == "resize2fs" }
        val args = ensureArgumentCount(tool, listOf("/tmp/image"))

        assertEquals(listOf("/tmp/image", ""), args)
    }

    @Test
    fun buildLogsExportAndShellScriptIncludeExpectedMarkers() {
        val logs = listOf(
            LogEntry(timestamp = Date(0L), message = "Started", type = LogType.INFO),
            LogEntry(timestamp = Date(1000L), message = "Something failed", type = LogType.ERROR),
        )
        val tool = ToolLibrary.availableTools.first { it.name == "simg2img" }

        val export = buildLogsExport(logs)
        val script = buildShellScript(tool, listOf("/tmp/in", "/tmp/out"))

        assertTrue(export.contains("RootedFirmwareLab log export"))
        assertTrue(export.contains("Started"))
        assertTrue(script.startsWith("#!/bin/sh"))
        assertTrue(script.contains("su -c"))
    }

    @Test
    fun partitionSlicesAndFormattingAreStable() {
        val slices = buildPartitionSlices(
            mapOf(
                "partition_0_name" to "system",
                "partition_0_size" to "1024",
                "partition_1_name" to "vendor",
                "partition_1_size" to "2048",
            ),
        )

        assertEquals(2, slices.size)
        assertEquals("system", slices.first().name)
        assertEquals("1.0 KB", formatBytes(1024))
    }

    @Test
    fun renderTerminalLineAppliesErrorStyleAndBuildVisibleTreeTracksDepth() {
        val rendered = renderTerminalLine("error: missing block device")
        assertTrue(rendered.spanStyles.isNotEmpty())

        val root = File.createTempFile("tree", "").apply {
            delete()
            mkdirs()
        }
        File(root, "one.txt").writeText("1")
        val childDir = File(root, "nested").apply { mkdirs() }
        File(childDir, "two.txt").writeText("2")

        val nodes = buildTreeNodes(root, setOf(root.absolutePath))
        assertTrue(nodes.any { it.depth == 0 })
    }
}
