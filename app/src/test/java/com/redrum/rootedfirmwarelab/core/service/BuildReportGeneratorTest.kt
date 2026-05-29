package com.redrum.rootedfirmwarelab.core.service

import com.redrum.rootedfirmwarelab.core.model.FirmwareFormat
import com.redrum.rootedfirmwarelab.core.model.IntegrityFinding
import com.redrum.rootedfirmwarelab.core.model.IntegrityReport
import com.redrum.rootedfirmwarelab.core.model.KitchenAction
import com.redrum.rootedfirmwarelab.core.model.KitchenPlan
import com.redrum.rootedfirmwarelab.core.model.PartitionDescriptor
import com.redrum.rootedfirmwarelab.core.model.PartitionRole
import com.redrum.rootedfirmwarelab.core.model.PartitionTableType
import com.redrum.rootedfirmwarelab.core.model.RomKitchenManifest
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildReportGeneratorTest {
    @Test
    fun markdownIncludesManifestPlanAndFindings() {
        val manifest = RomKitchenManifest(
            projectName = "demo",
            workspacePath = "/tmp/workspace",
            sourcePath = "/tmp/system.img",
            createdAtEpochMs = 0L,
            partitions = listOf(
                PartitionDescriptor("system", PartitionRole.SYSTEM, 1024L, "ext4", "/system", sourcePath = "/tmp/system.img"),
            ),
        )
        val plan = KitchenPlan(
            id = "plan-1",
            label = "Inspect",
            sourcePath = "/tmp/system.img",
            actions = listOf(KitchenAction("hash-only", "Hash artifact", listOf("sha256sum", "/tmp/system.img"), requiresRoot = false)),
            preflight = IntegrityReport(
                targetPath = "/tmp/system.img",
                sha256 = "deadbeef",
                sizeBytes = 1024L,
                detectedFormat = FirmwareFormat.RAW_IMG,
                partitionTableType = PartitionTableType.UNKNOWN,
                findings = listOf(IntegrityFinding(IntegrityFinding.Severity.INFO, "ok", "No blockers")),
            ),
        )

        val markdown = BuildReportGenerator.markdown(manifest, plan)

        assertTrue(markdown.contains("# RootedFirmwareLab Build Report"))
        assertTrue(markdown.contains("Preflight Findings"))
        assertTrue(markdown.contains("Planned Actions"))
        assertTrue(markdown.contains("system"))
        assertTrue(markdown.contains("deadbeef"))
    }
}
