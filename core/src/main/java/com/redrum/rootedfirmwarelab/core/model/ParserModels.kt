package com.redrum.rootedfirmwarelab.core.model

data class ParseRequest(
    val filePath: String,
    val rootedMode: Boolean = true,
)

data class ParseResult(
    val format: FirmwareFormat,
    val summary: String,
    val metadata: Map<String, String>,
)

enum class PartitionRole {
    BOOT,
    RECOVERY,
    SYSTEM,
    VENDOR,
    PRODUCT,
    ODM,
    VBMETA,
    SUPER,
    USERDATA,
    UNKNOWN,
}

enum class PartitionTableType {
    GPT,
    MBR,
    ANDROID_DYNAMIC,
    UNKNOWN,
}

data class PartitionDescriptor(
    val name: String,
    val role: PartitionRole = PartitionRole.UNKNOWN,
    val sizeBytes: Long = 0L,
    val fsType: String? = null,
    val mountPoint: String? = null,
    val flags: List<String> = emptyList(),
    val sourcePath: String? = null,
)

data class FstabEntry(
    val blockDevice: String,
    val mountPoint: String,
    val fsType: String,
    val mountFlags: List<String>,
    val fsMgrFlags: List<String>,
)

data class IntegrityFinding(
    val severity: Severity,
    val code: String,
    val message: String,
    val remediation: String? = null,
) {
    enum class Severity {
        INFO,
        WARNING,
        ERROR,
        BLOCKER,
    }
}

data class IntegrityReport(
    val targetPath: String,
    val sha256: String?,
    val sizeBytes: Long,
    val detectedFormat: FirmwareFormat,
    val partitionTableType: PartitionTableType,
    val findings: List<IntegrityFinding>,
) {
    val canModify: Boolean
        get() = findings.none { it.severity == IntegrityFinding.Severity.BLOCKER }
}

data class RomKitchenManifest(
    val projectName: String,
    val workspacePath: String,
    val sourcePath: String,
    val createdAtEpochMs: Long,
    val partitions: List<PartitionDescriptor>,
    val properties: Map<String, String> = emptyMap(),
    val plugins: List<String> = emptyList(),
)

data class KitchenAction(
    val id: String,
    val label: String,
    val command: List<String>,
    val requiresRoot: Boolean = true,
    val modifiesDevice: Boolean = false,
    val expectedOutputs: List<String> = emptyList(),
)

data class KitchenPlan(
    val id: String,
    val label: String,
    val sourcePath: String,
    val actions: List<KitchenAction>,
    val preflight: IntegrityReport,
)

data class PluginDefinition(
    val id: String,
    val displayName: String,
    val version: String,
    val entrypoint: String,
    val description: String = "",
    val permissions: List<String> = emptyList(),
    val arguments: Map<String, String> = emptyMap(),
)
