package com.redrum.rootedfirmwarelab.core.service

import com.redrum.rootedfirmwarelab.core.model.IntegrityFinding
import com.redrum.rootedfirmwarelab.core.model.KitchenPlan
import com.redrum.rootedfirmwarelab.core.model.RomKitchenManifest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BuildReportGenerator {
    fun markdown(manifest: RomKitchenManifest, plan: KitchenPlan): String {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.US).format(Date())
        return buildString {
            appendLine("# RootedFirmwareLab Build Report")
            appendLine()
            appendLine("- Project: ${manifest.projectName}")
            appendLine("- Generated: $timestamp")
            appendLine("- Source: ${manifest.sourcePath}")
            appendLine("- Workspace: ${manifest.workspacePath}")
            appendLine("- SHA-256: ${plan.preflight.sha256 ?: "unavailable"}")
            appendLine("- Format: ${plan.preflight.detectedFormat}")
            appendLine("- Partition table: ${plan.preflight.partitionTableType}")
            appendLine("- Modification allowed: ${plan.preflight.canModify}")
            appendLine()
            appendLine("## Partitions")
            manifest.partitions.forEach {
                appendLine("- ${it.name}: role=${it.role}, size=${it.sizeBytes}, fs=${it.fsType ?: "unknown"}")
            }
            appendLine()
            appendLine("## Preflight Findings")
            plan.preflight.findings.forEach {
                val marker = when (it.severity) {
                    IntegrityFinding.Severity.INFO -> "INFO"
                    IntegrityFinding.Severity.WARNING -> "WARN"
                    IntegrityFinding.Severity.ERROR -> "ERROR"
                    IntegrityFinding.Severity.BLOCKER -> "BLOCKER"
                }
                appendLine("- [$marker] ${it.code}: ${it.message}")
                it.remediation?.let { remediation -> appendLine("  Remediation: $remediation") }
            }
            appendLine()
            appendLine("## Planned Actions")
            plan.actions.forEach {
                appendLine("- ${it.id}: ${it.label}")
                if (it.command.isNotEmpty()) appendLine("  Command: `${it.command.joinToString(" ")}`")
                appendLine("  Requires root: ${it.requiresRoot}; modifies device: ${it.modifiesDevice}")
            }
        }
    }
}
