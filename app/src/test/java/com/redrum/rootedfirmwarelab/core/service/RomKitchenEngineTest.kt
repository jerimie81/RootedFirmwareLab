package com.redrum.rootedfirmwarelab.core.service

import com.redrum.rootedfirmwarelab.core.model.FirmwareFormat
import com.redrum.rootedfirmwarelab.core.model.ParseResult
import com.redrum.rootedfirmwarelab.core.model.PartitionRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class RomKitchenEngineTest {
    private val engine = RomKitchenEngine()

    @Test
    fun createManifestInfersPartitionRolesFromMetadata() {
        val source = File.createTempFile("super", ".img")
        val parseResult = ParseResult(
            format = FirmwareFormat.SUPER_IMG,
            summary = "dynamic",
            metadata = mapOf(
                "partition_0_name" to "system",
                "partition_0_size" to "1024",
                "partition_1_name" to "vendor",
                "partition_1_size" to "2048",
            ),
        )

        val manifest = engine.createManifest("project", source.parentFile, source, parseResult)

        assertEquals(2, manifest.partitions.size)
        assertEquals("system", manifest.partitions.first().name)
        assertEquals(PartitionRole.SYSTEM, manifest.partitions.first().role)
        assertEquals(PartitionRole.VENDOR, manifest.partitions[1].role)
    }

    @Test
    fun createPlanBuildsSparseAndBootActions() {
        val source = File.createTempFile("boot", ".img")
        val preflightOk = ParseResult(FirmwareFormat.SPARSE_IMG, "sparse", emptyMap())
        val bootParse = ParseResult(FirmwareFormat.ANDROID_BOOT_IMG, "boot", emptyMap())

        val sparsePlan = engine.createPlan("Sparse", source, preflightOk)
        val bootPlan = engine.createPlan("Boot", source, bootParse)

        assertEquals(listOf("sparse-to-raw", "hash-output"), sparsePlan.actions.map { it.id })
        assertEquals(listOf("unpack-boot", "repack-boot"), bootPlan.actions.map { it.id })
        assertTrue(bootPlan.preflight.canModify)
    }

    @Test
    fun recoveryScriptContainsPartitionRestores() {
        val manifest = engine.createManifest(
            "demo",
            File("/tmp"),
            File("/tmp/system.img"),
            ParseResult(
                FirmwareFormat.RAW_IMG,
                "raw",
                mapOf("partition_0_name" to "system", "partition_0_size" to "4096"),
            ),
        )

        val script = engine.recoveryScript(manifest)

        assertTrue(script.contains("RootedFirmwareLab recovery script: demo"))
        assertTrue(script.contains("dd if="))
        assertTrue(script.contains("system"))
    }
}
