package com.redrum.rootedfirmwarelab.core.service

import com.redrum.rootedfirmwarelab.core.model.FirmwareFormat
import com.redrum.rootedfirmwarelab.core.model.IntegrityReport
import com.redrum.rootedfirmwarelab.core.model.KitchenAction
import com.redrum.rootedfirmwarelab.core.model.KitchenPlan
import com.redrum.rootedfirmwarelab.core.model.PartitionDescriptor
import com.redrum.rootedfirmwarelab.core.model.PartitionRole
import com.redrum.rootedfirmwarelab.core.model.ParseResult
import com.redrum.rootedfirmwarelab.core.model.RomKitchenManifest
import java.io.File
import java.util.UUID

class RomKitchenEngine(
    private val integrityService: IntegrityService = IntegrityService(),
) {
    fun createManifest(projectName: String, workspace: File, source: File, parseResult: ParseResult): RomKitchenManifest {
        return RomKitchenManifest(
            projectName = projectName,
            workspacePath = workspace.absolutePath,
            sourcePath = source.absolutePath,
            createdAtEpochMs = System.currentTimeMillis(),
            partitions = inferPartitions(source, parseResult),
            properties = parseResult.metadata,
        )
    }

    fun createPlan(label: String, source: File, parseResult: ParseResult): KitchenPlan {
        val preflight = integrityService.preflight(source, parseResult)
        return KitchenPlan(
            id = UUID.randomUUID().toString(),
            label = label,
            sourcePath = source.absolutePath,
            actions = buildActions(source, parseResult, preflight),
            preflight = preflight,
        )
    }

    fun recoveryScript(manifest: RomKitchenManifest): String {
        val lines = mutableListOf<String>()
        lines += "#!/system/bin/sh"
        lines += "set -eu"
        lines += "echo 'RootedFirmwareLab recovery script: ${manifest.projectName}'"
        manifest.partitions.forEach { partition ->
            val source = partition.sourcePath ?: return@forEach
            val block = "/dev/block/by-name/${partition.name}"
            lines += "if [ -f '$source' ] && [ -e '$block' ]; then"
            lines += "  echo 'Restoring ${partition.name}'"
            lines += "  dd if='$source' of='$block' bs=4096 conv=fsync"
            lines += "fi"
        }
        return lines.joinToString("\n") + "\n"
    }

    private fun buildActions(source: File, parseResult: ParseResult, preflight: IntegrityReport): List<KitchenAction> {
        if (!preflight.canModify) {
            return listOf(
                KitchenAction(
                    id = "blocked-preflight",
                    label = "Resolve preflight blockers before modification",
                    command = emptyList(),
                    requiresRoot = false,
                ),
            )
        }
        return when (parseResult.format) {
            FirmwareFormat.SPARSE_IMG -> listOf(
                KitchenAction("sparse-to-raw", "Convert sparse image to raw", listOf("simg2img", source.absolutePath, "${source.absolutePath}.raw")),
                KitchenAction("hash-output", "Hash converted image", listOf("sha256sum", "${source.absolutePath}.raw"), requiresRoot = false),
            )
            FirmwareFormat.ANDROID_BOOT_IMG -> listOf(
                KitchenAction("unpack-boot", "Unpack boot image", listOf("unmkbootimg", "-i", source.absolutePath, "-o", "${source.absolutePath}.unpacked")),
                KitchenAction("repack-boot", "Repack boot image after edits", listOf("mkbootimg", "--kernel", "kernel", "--ramdisk", "ramdisk.img", "-o", "${source.absolutePath}.patched.img")),
            )
            FirmwareFormat.PAYLOAD_BIN -> listOf(
                KitchenAction("payload-extract", "Extract OTA payload partitions", listOf("payload-dumper-go", "-o", "${source.absolutePath}.out", source.absolutePath)),
            )
            FirmwareFormat.SUPER_IMG, FirmwareFormat.RAW_IMG -> listOf(
                KitchenAction("lpunpack", "Unpack dynamic partitions if present", listOf("lpunpack", source.absolutePath, "${source.absolutePath}.parts")),
                KitchenAction("filesystem-check", "Run filesystem validation", listOf("e2fsck", "-fn", source.absolutePath)),
            )
            FirmwareFormat.VB_META -> listOf(
                KitchenAction("patch-vbmeta", "Patch vbmeta verification flags", listOf("avbtool", "make_vbmeta_image", "--flags", "3", "--output", "${source.absolutePath}.patched")),
            )
            else -> listOf(
                KitchenAction("hash-only", "Hash artifact for audit trail", listOf("sha256sum", source.absolutePath), requiresRoot = false),
            )
        }
    }

    private fun inferPartitions(source: File, parseResult: ParseResult): List<PartitionDescriptor> {
        val fromMetadata = parseResult.metadata.keys
            .mapNotNull { key ->
                val prefix = "partition_"
                if (!key.startsWith(prefix) || !key.endsWith("_name")) return@mapNotNull null
                val index = key.removePrefix(prefix).removeSuffix("_name")
                val name = parseResult.metadata[key].orEmpty()
                val size = parseResult.metadata["partition_${index}_size"]?.toLongOrNull() ?: 0L
                PartitionDescriptor(
                    name = name,
                    role = roleFor(name),
                    sizeBytes = size,
                    sourcePath = source.absolutePath,
                )
            }
        if (fromMetadata.isNotEmpty()) return fromMetadata
        val name = source.nameWithoutExtension.substringBefore(".")
        return listOf(
            PartitionDescriptor(
                name = name,
                role = roleFor(name),
                sizeBytes = source.length(),
                fsType = parseResult.metadata["fsHint"],
                sourcePath = source.absolutePath,
            ),
        )
    }

    private fun roleFor(name: String): PartitionRole {
        return when {
            name.equals("boot", true) || name.equals("vendor_boot", true) || name.equals("init_boot", true) -> PartitionRole.BOOT
            name.equals("recovery", true) -> PartitionRole.RECOVERY
            name.equals("system", true) || name.startsWith("system_", true) -> PartitionRole.SYSTEM
            name.equals("vendor", true) || name.startsWith("vendor_", true) -> PartitionRole.VENDOR
            name.equals("product", true) -> PartitionRole.PRODUCT
            name.equals("odm", true) -> PartitionRole.ODM
            name.startsWith("vbmeta", true) -> PartitionRole.VBMETA
            name.equals("super", true) -> PartitionRole.SUPER
            name.equals("userdata", true) -> PartitionRole.USERDATA
            else -> PartitionRole.UNKNOWN
        }
    }
}
