package com.redrum.rootedfirmwarelab.ui

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.Data
import androidx.work.testing.TestListenableWorkerBuilder
import com.redrum.rootedfirmwarelab.FirmwareInspectWorker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class FirmwareInspectWorkerHelpersTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun safeZipTargetRejectsTraversal() {
        val worker = buildWorker()
        val root = File.createTempFile("zip", "").apply {
            delete()
            mkdirs()
        }

        assertThrows(IllegalArgumentException::class.java) {
            worker.safeZipTarget(root, "../escape.txt")
        }
    }

    @Test
    fun extractZipCreatesDirectoryTree() {
        val worker = buildWorker()
        val zip = File.createTempFile("firmware", ".zip")
        ZipOutputStream(zip.outputStream()).use { zos ->
            zos.putNextEntry(ZipEntry("dir/file.txt"))
            zos.write("hello".toByteArray())
            zos.closeEntry()
        }

        val extractedPath = worker.extractZip(zip)
        val extracted = File(extractedPath, "dir/file.txt")

        assertEquals("hello", extracted.readText())
    }

    @Test
    fun discoverFstabFindingsReturnsValidationMessages() {
        val worker = buildWorker()
        val root = File.createTempFile("fstab", "").apply {
            delete()
            mkdirs()
        }
        File(root, "fstab.vendor").writeText(
            """
            /dev/block/by-name/vendor /vendor ext4 ro wait
            /dev/block/by-name/vendor /vendor weirdfs ro avb
            """.trimIndent(),
        )

        val findings = worker.discoverFstabFindings(root)

        assertTrue(findings.size >= 2)
        assertTrue(findings.any { it.contains("Duplicate fstab mount point") })
    }

    private fun buildWorker(): FirmwareInspectWorker {
        return TestListenableWorkerBuilder<FirmwareInspectWorker>(context)
            .setInputData(Data.EMPTY)
            .build()
    }
}
