package com.redrum.rootedfirmwarelab.core.service

import com.redrum.rootedfirmwarelab.core.model.FirmwareFormat
import com.redrum.rootedfirmwarelab.core.model.ParseResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class IntegrityServiceTest {
    private val service = IntegrityService()

    @Test
    fun sha256HashesKnownContent() {
        val file = File.createTempFile("sha", ".bin")
        file.writeText("abc")

        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", service.sha256(file))
    }

    @Test
    fun preflightBlocksMissingFiles() {
        val report = service.preflight(
            File("/tmp/does-not-exist.img"),
            ParseResult(FirmwareFormat.UNKNOWN, "unknown", emptyMap()),
        )

        assertFalse(report.canModify)
        assertTrue(report.findings.any { it.severity == com.redrum.rootedfirmwarelab.core.model.IntegrityFinding.Severity.BLOCKER })
    }

    @Test
    fun preflightDetectsDynamicPartitionLayoutAndCriticalPartitions() {
        val file = File.createTempFile("boot", ".img")
        file.writeBytes(byteArrayOf(1, 2, 3))

        val report = service.preflight(
            file,
            ParseResult(
                format = FirmwareFormat.SUPER_IMG,
                summary = "dynamic",
                metadata = mapOf("fsHint" to "ext4"),
            ),
        )

        assertTrue(report.canModify)
        assertEquals(com.redrum.rootedfirmwarelab.core.model.PartitionTableType.ANDROID_DYNAMIC, report.partitionTableType)
        assertNotNull(report.sha256)
    }
}
