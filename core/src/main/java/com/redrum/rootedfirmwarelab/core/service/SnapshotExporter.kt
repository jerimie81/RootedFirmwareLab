package com.redrum.rootedfirmwarelab.core.service

import com.redrum.rootedfirmwarelab.core.model.RomKitchenManifest
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object SnapshotExporter {
    fun createSnapshot(manifest: RomKitchenManifest, workspace: File, outputZip: File) {
        ZipOutputStream(FileOutputStream(outputZip)).use { zip ->
            // Add manifest as metadata
            val manifestJson = JSONObject()
                .put("projectName", manifest.projectName)
                .put("createdAt", manifest.createdAtEpochMs)
            
            val entry = ZipEntry("manifest.json")
            zip.putNextEntry(entry)
            zip.write(manifestJson.toString().toByteArray())
            zip.closeEntry()
            
            // Add workspace files recursively
            workspace.walkTopDown().forEach { file ->
                if (file.isFile) {
                    val relativePath = file.relativeTo(workspace).path
                    val zipEntry = ZipEntry(relativePath)
                    zip.putNextEntry(zipEntry)
                    file.inputStream().use { input -> input.copyTo(zip) }
                    zip.closeEntry()
                }
            }
        }
    }
}
