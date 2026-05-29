package com.redrum.rootedfirmwarelab.core.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ToolLibraryTest {
    @Test
    fun shellEscapeQuotesSingleQuotes() {
        assertEquals("'can'\\''t'", ToolLibrary.shellEscape("can't"))
    }

    @Test
    fun placeholderCountFindsHighestPlaceholder() {
        assertEquals(3, ToolLibrary.placeholderCount("cmd %1 %3 %2"))
    }

    @Test
    fun previewCommandRendersArguments() {
        val tool = ToolLibrary.availableTools.first { it.name == "simg2img" }
        val preview = ToolLibrary.previewCommand(tool, listOf("/tmp/in.img", "/tmp/out.img"))

        assertEquals("simg2img '/tmp/in.img' '/tmp/out.img'", preview)
    }

    @Test
    fun availableToolsIncludesNewFirmwareUtilities() {
        val names = ToolLibrary.availableTools.map { it.name }.toSet()

        assertTrue("avbtool" in names)
        assertTrue("brotli" in names)
        assertTrue("resize2fs" in names)
    }
}
