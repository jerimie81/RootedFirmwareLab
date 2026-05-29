package com.redrum.rootedfirmwarelab.core.service

import com.redrum.rootedfirmwarelab.core.model.FirmwareFormat
import com.redrum.rootedfirmwarelab.core.model.IntegrityFinding
import com.redrum.rootedfirmwarelab.core.model.IntegrityReport
import com.redrum.rootedfirmwarelab.core.model.PartitionTableType
import java.io.File
import java.security.MessageDigest

class IntegrityService {
    fun preflight(file: File, parseResult: com.redrum.rootedfirmwarelab.core.model.ParseResult): IntegrityReport {
        val findings = mutableListOf<IntegrityFinding>()
        if (!file.exists()) {
            findings += IntegrityFinding(
                IntegrityFinding.Severity.BLOCKER,
                "missing_target",
                "Target file does not exist.",
                "Re-select or restage the firmware artifact.",
            )
        }
        if (file.exists() && file.length() == 0L) {
            findings += IntegrityFinding(
                IntegrityFinding.Severity.BLOCKER,
                "empty_target",
                "Target file is empty.",
                "Use a complete firmware image.",
            )
        }
        val critical = criticalPartitionNames.any { file.nameWithoutExtension.equals(it, ignoreCase = true) }
        if (critical) {
            findings += IntegrityFinding(
                IntegrityFinding.Severity.WARNING,
                "critical_partition",
                "This artifact maps to a critical boot-chain partition.",
                "Create a full backup and recovery script before modification.",
            )
        }
        if (parseResult.format == FirmwareFormat.UNKNOWN) {
            findings += IntegrityFinding(
                IntegrityFinding.Severity.WARNING,
                "unknown_format",
                "The parser did not recognize this artifact format.",
                "Treat modification as unsafe until manually verified.",
            )
        }
        parseResult.metadata["fsHint"]?.let { fs ->
            if (fs !in setOf("ext4", "erofs", "f2fs")) {
                findings += IntegrityFinding(
                    IntegrityFinding.Severity.WARNING,
                    "unusual_filesystem",
                    "Detected unusual filesystem hint: $fs.",
                )
            }
        }

        return IntegrityReport(
            targetPath = file.absolutePath,
            sha256 = if (file.exists() && file.isFile) sha256(file) else null,
            sizeBytes = if (file.exists()) file.length() else 0L,
            detectedFormat = parseResult.format,
            partitionTableType = detectPartitionTable(parseResult),
            findings = findings.ifEmpty {
                listOf(IntegrityFinding(IntegrityFinding.Severity.INFO, "preflight_ok", "No blocking preflight findings."))
            },
        )
    }

    fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var read = input.read(buffer)
            while (read >= 0) {
                if (read > 0) digest.update(buffer, 0, read)
                read = input.read(buffer)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun detectPartitionTable(parseResult: com.redrum.rootedfirmwarelab.core.model.ParseResult): PartitionTableType {
        val layout = parseResult.metadata["dynamic_partition_layout"].orEmpty()
        if (layout.isNotBlank()) return PartitionTableType.ANDROID_DYNAMIC
        return when (parseResult.format) {
            FirmwareFormat.GPT -> PartitionTableType.GPT
            FirmwareFormat.MBR -> PartitionTableType.MBR
            FirmwareFormat.SUPER_IMG -> PartitionTableType.ANDROID_DYNAMIC
            else -> PartitionTableType.UNKNOWN
        }
    }

    private companion object {
        val criticalPartitionNames = setOf("boot", "vendor_boot", "init_boot", "recovery", "vbmeta", "vbmeta_system", "dtbo")
    }
}
