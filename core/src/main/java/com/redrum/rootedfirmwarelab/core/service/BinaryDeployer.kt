package com.redrum.rootedfirmwarelab.core.service

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class BinaryDeployer(private val context: Context) {

    private val toolsDir: File by lazy {
        File(context.filesDir, "bin").apply {
            if (!exists()) mkdirs()
        }
    }

    // List of tools that should be deployed from assets
    private val toolsToDeploy = listOf(
        "lpunpack",
        "lpmake",
        "simg2img",
        "img2simg",
        "avbtool",
        "payload-dumper-go",
        "dtc",
        "brotli",
        "lz4",
        "e2fsck",
        "resize2fs",
        "cpio",
        "mkbootimg",
        "unmkbootimg",
    )

    fun deployBinaries() {
        toolsToDeploy.forEach { toolName ->
            val toolFile = File(toolsDir, toolName)
            if (!toolFile.exists() || toolFile.length() == 0L) { // Deploy if not exists or is empty
                try {
                    context.assets.open(toolName).use { input: InputStream ->
                        FileOutputStream(toolFile).use { output: FileOutputStream ->
                            input.copyTo(output)
                        }
                    }
                    toolFile.setExecutable(true, false) // Make the deployed binary executable
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Log or handle the error, e.g., tool not found in assets
                }
            }
        }
    }

    fun getToolPath(toolName: String): String {
        return File(toolsDir, toolName).absolutePath
    }
}
