package com.redrum.rootedfirmwarelab.core.service

import android.content.Context
import com.redrum.rootedfirmwarelab.data.LogType
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Registry and executor for firmware manipulation tools.
 * Maps tools to their specific command structures and manages execution.
 */
object ToolLibrary {

    private lateinit var binaryDeployer: BinaryDeployer
    private lateinit var logManager: LogManager

    fun initialize(context: Context, logDao: com.redrum.rootedfirmwarelab.data.LogDao) {
        binaryDeployer = BinaryDeployer(context)
        logManager = LogManager(logDao)
    }

    data class ToolDefinition(
        val name: String,
        val description: String,
        val usage: String,
        val template: String // New field for command template
    )

    val availableTools = listOf(
        ToolDefinition("lpunpack", "Unpack dynamic super.img partitions", "lpunpack [img] [out]", "lpunpack %1 %2"),
        ToolDefinition("lpmake", "Create super.img dynamic partitions", "lpmake --metadata-size ...", "lpmake %1"),
        ToolDefinition("simg2img", "Convert Android sparse image to raw image", "simg2img [sparse] [raw]", "simg2img %1 %2"),
        ToolDefinition("img2simg", "Convert raw ext image to Android sparse image", "img2simg [raw] [sparse]", "img2simg %1 %2"),
        ToolDefinition("avbtool", "Inspect or patch Android Verified Boot metadata", "avbtool info_image --image [vbmeta]", "avbtool %1"),
        ToolDefinition("payload-dumper-go", "Extract partitions from OTA payload.bin", "payload-dumper-go -o [out] [payload]", "payload-dumper-go -o %1 %2"),
        ToolDefinition("dtc", "Decompile or compile DTB/DTBO device trees", "dtc -I dtb -O dts [dtb] -o [dts]", "dtc %1"),
        ToolDefinition("brotli", "Decompress Android Brotli-compressed images", "brotli -d [file]", "brotli %1"),
        ToolDefinition("lz4", "Compress or decompress LZ4 firmware images", "lz4 [args]", "lz4 %1"),
        ToolDefinition("e2fsck", "Read-only ext filesystem validation", "e2fsck -fn [img]", "e2fsck -fn %1"),
        ToolDefinition("resize2fs", "Resize ext filesystem images", "resize2fs [img] [size]", "resize2fs %1 %2"),
        ToolDefinition("cpio", "Extract/Repack ramdisk archives", "gunzip -c [gz] | cpio -idmv", "sh -c 'gunzip -c %1 | cpio -idmv'"),
        ToolDefinition("mkbootimg", "Combine into boot.img", "mkbootimg --kernel ... -o boot.img", "mkbootimg %1"),
        ToolDefinition("unmkbootimg", "Split boot image into components", "unmkbootimg -i [img] -o [dir]", "unmkbootimg -i %1 -o %2")
    )

    /**
     * Executes a tool via su shell.
     */
    fun execute(tool: String, args: List<String>): String {
        return try {
            val toolPath = if (::binaryDeployer.isInitialized) binaryDeployer.getToolPath(tool) else tool
            val commandString = buildShellCommand(toolPath, args)
            logManager.log("Executing: $commandString", LogType.TOOL_OUTPUT)

            val p = ProcessBuilder("su", "-c", commandString).redirectErrorStream(true).start()
            val output = p.inputStream.bufferedReader().use { it.readText() }
            logManager.log("Output for $tool: $output", LogType.TOOL_OUTPUT)
            output
        } catch (e: Exception) {
            logManager.log("Error executing $tool: ${e.message}", LogType.ERROR)
            "Error: ${e.message}"
        }
    }

    fun executeTemplate(tool: ToolDefinition, args: List<String>): String {
        val finalCommandStr = previewCommand(tool, args)
        return executeShellCommand(finalCommandStr)
    }

    fun previewCommand(tool: ToolDefinition, args: List<String>): String {
        return renderTemplate(tool.template, args)
    }

    fun renderTemplate(template: String, args: List<String>): String {
        val rendered = args.foldIndexed(template) { index, acc, value ->
            acc.replace("%${index + 1}", shellEscape(value))
        }
        return if (rendered.startsWith("sh -c '")) {
            val inner = rendered.removePrefix("sh -c '").removeSuffix("'")
            "sh -c ${shellEscape(resolveLeadingBinary(inner))}"
        } else {
            resolveLeadingBinary(rendered)
        }
    }

    fun executeShellCommand(command: String): String {
        return try {
            logManager.log("Executing: $command", LogType.TOOL_OUTPUT)
            val process = ProcessBuilder("su", "-c", command).redirectErrorStream(true).start()
            val output = process.inputStream.bufferedReader().use { it.readText() }
            logManager.log(output.ifBlank { "[no output]" }, LogType.TOOL_OUTPUT)
            output
        } catch (e: Exception) {
            val error = "Error: ${e.message}"
            logManager.log(error, LogType.ERROR)
            error
        }
    }

    fun shellEscape(value: String): String {
        return "'" + value.replace("'", "'\\''") + "'"
    }

    fun placeholderCount(template: String): Int {
        val matches = Regex("%(\\d+)").findAll(template)
        return matches.maxOfOrNull { it.groupValues[1].toInt() } ?: 0
    }

    private fun buildShellCommand(toolPath: String, args: List<String>): String {
        val quotedArgs = args.joinToString(" ") { shellEscape(it) }
        return if (quotedArgs.isBlank()) toolPath else "$toolPath $quotedArgs"
    }

    private fun resolveLeadingBinary(command: String): String {
        val trimmed = command.trim()
        if (trimmed.isBlank()) return trimmed
        val token = trimmed.substringBefore(" ")
        val resolved = if (::binaryDeployer.isInitialized) {
            binaryDeployer.getToolPath(token.trim('\''))
        } else {
            token
        }
        return trimmed.replaceFirst(token, resolved)
    }
}
