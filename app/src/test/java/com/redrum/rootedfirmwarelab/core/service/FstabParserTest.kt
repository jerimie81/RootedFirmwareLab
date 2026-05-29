package com.redrum.rootedfirmwarelab.core.service

import com.redrum.rootedfirmwarelab.core.model.FstabEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class FstabParserTest {
    @Test
    fun parsesEntriesAndIgnoresComments() {
        val raw = """
            # comment
            /dev/block/by-name/system /system ext4 ro,barrier=1 wait,first_stage_mount
            /dev/block/by-name/vendor /vendor erofs ro wait
        """.trimIndent()

        val entries = FstabParser.parse(raw)

        assertEquals(2, entries.size)
        assertEquals(
            FstabEntry(
                blockDevice = "/dev/block/by-name/system",
                mountPoint = "/system",
                fsType = "ext4",
                mountFlags = listOf("ro", "barrier=1"),
                fsMgrFlags = listOf("wait", "first_stage_mount"),
            ),
            entries.first(),
        )
    }

    @Test
    fun parseFileReadsFromDisk() {
        val file = File.createTempFile("fstab", ".txt")
        file.writeText("/dev/block/by-name/product /product f2fs ro wait")

        val entries = FstabParser.parseFile(file)

        assertEquals(1, entries.size)
        assertEquals("/product", entries.single().mountPoint)
    }

    @Test
    fun validateFindsDuplicateMountsAndOddFileSystems() {
        val findings = FstabParser.validate(
            listOf(
                FstabEntry("/dev/block/by-name/system", "/system", "ext4", listOf("ro"), listOf("wait")),
                FstabEntry("/dev/block/by-name/system_ext", "/system", "weirdfs", listOf("ro"), listOf("avb", "wait")),
            ),
        )

        assertTrue(findings.any { it.contains("Duplicate fstab mount point") })
        assertTrue(findings.any { it.contains("Unsupported or unusual fs type") })
    }
}
