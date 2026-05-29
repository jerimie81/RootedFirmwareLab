package com.redrum.rootedfirmwarelab

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.redrum.rootedfirmwarelab.core.model.ParseRequest
import com.redrum.rootedfirmwarelab.core.service.BuildReportGenerator
import com.redrum.rootedfirmwarelab.core.service.FstabParser
import com.redrum.rootedfirmwarelab.core.service.RomKitchenEngine
import com.redrum.rootedfirmwarelab.nativebridge.jni.NativeFirmwareBridge
import java.io.File
import java.io.FileOutputStream
import org.json.JSONObject
import java.util.zip.ZipInputStream

class FirmwareInspectWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val uriString = inputData.getString(KEY_URI) ?: return Result.failure(
            Data.Builder().putString(KEY_ERROR, "Missing file uri").build()
        )

        return try {
            val staged = stageUriToCache(applicationContext, Uri.parse(uriString))
            val isZip = staged.name.endsWith(".zip", ignoreCase = true)
            val processingPath = if (isZip) extractZip(staged) else staged.absolutePath
            
            val parser = NativeFirmwareBridge()
            val parsed = parser.inspect(ParseRequest(filePath = processingPath, rootedMode = true))
            val engine = RomKitchenEngine()
            val sourceFile = File(processingPath)
            val workspace = sourceFile.parentFile ?: applicationContext.cacheDir
            val manifest = engine.createManifest(sourceFile.nameWithoutExtension, workspace, sourceFile, parsed)
            val plan = engine.createPlan("Inspect and prepare ${sourceFile.name}", sourceFile, parsed)
            val reportFile = File(workspace, "${sourceFile.nameWithoutExtension}-build-report.md")
            reportFile.writeText(BuildReportGenerator.markdown(manifest, plan))
            val recoveryFile = File(workspace, "${sourceFile.nameWithoutExtension}-recovery.sh")
            recoveryFile.writeText(engine.recoveryScript(manifest))
            recoveryFile.setExecutable(true, false)
            val fstabFindings = discoverFstabFindings(sourceFile)
            val enrichedMetadata = parsed.metadata.toMutableMap().apply {
                put("sha256", plan.preflight.sha256.orEmpty())
                put("preflightCanModify", plan.preflight.canModify.toString())
                put("preflightFindingCount", plan.preflight.findings.size.toString())
                put("partitionTableType", plan.preflight.partitionTableType.name)
                put("buildReportPath", reportFile.absolutePath)
                put("recoveryScriptPath", recoveryFile.absolutePath)
                put("plannedActionCount", plan.actions.size.toString())
                put("plannedActions", plan.actions.joinToString(",") { it.id })
                put("partitionCount", manifest.partitions.size.toString())
                manifest.partitions.forEachIndexed { index, partition ->
                    put("partition_${index}_name", partition.name)
                    put("partition_${index}_role", partition.role.name)
                    put("partition_${index}_size", partition.sizeBytes.toString())
                    partition.fsType?.let { put("partition_${index}_fs", it) }
                }
                plan.preflight.findings.forEachIndexed { index, finding ->
                    put("finding_${index}_severity", finding.severity.name)
                    put("finding_${index}_code", finding.code)
                    put("finding_${index}_message", finding.message)
                }
                if (fstabFindings.isNotEmpty()) {
                    put("fstabFindings", fstabFindings.joinToString(" | "))
                }
            }
            Result.success(
                Data.Builder()
                    .putString(KEY_FORMAT, parsed.format.name)
                    .putString(KEY_SUMMARY, parsed.summary)
                    .putString(KEY_PATH, processingPath)
                    .putBoolean(KEY_IS_ZIP, isZip)
                    .putString(KEY_ENGINE, parser.engineName())
                    .putString(KEY_METADATA, JSONObject(enrichedMetadata.toMap()).toString())
                    .build()
            )
        } catch (e: Exception) {
            Result.failure(
                Data.Builder()
                    .putString(KEY_ERROR, e.message ?: "inspection failed")
                    .build()
            )
        }
    }

    internal fun extractZip(zipFile: File): String {
        val destDir = File(zipFile.parent, zipFile.nameWithoutExtension)
        destDir.mkdirs()
        ZipInputStream(zipFile.inputStream()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val newFile = safeZipTarget(destDir, entry.name)
                if (entry.isDirectory) newFile.mkdirs()
                else {
                    newFile.parentFile?.mkdirs()
                    FileOutputStream(newFile).use { fos -> zis.copyTo(fos) }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        return destDir.absolutePath
    }

    internal fun safeZipTarget(destDir: File, entryName: String): File {
        val target = File(destDir, entryName)
        val canonicalDest = destDir.canonicalPath + File.separator
        val canonicalTarget = target.canonicalPath
        require(canonicalTarget.startsWith(canonicalDest)) { "Unsafe zip entry path: $entryName" }
        return target
    }

    internal fun discoverFstabFindings(source: File): List<String> {
        val candidates = when {
            source.isDirectory -> source.walkTopDown().filter { it.isFile && it.name.startsWith("fstab") }.take(8).toList()
            source.name.startsWith("fstab") -> listOf(source)
            else -> emptyList()
        }
        return candidates.flatMap { fstab ->
            FstabParser.validate(FstabParser.parseFile(fstab)).map { "${fstab.name}: $it" }
        }
    }

    private fun stageUriToCache(context: Context, uri: Uri): File {
        val flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
        runCatching { context.contentResolver.takePersistableUriPermission(uri, flags) }
        val name = lastStagedFileName(context, uri)
        val out = File(context.cacheDir, name)
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Unable to read selected file." }
            out.outputStream().use { output -> input.copyTo(output) }
        }
        return out
    }

    private fun lastStagedFileName(context: Context, uri: Uri): String {
        val fallback = "firmware-${System.currentTimeMillis()}.bin"
        val projection = arrayOf(OpenableColumns.DISPLAY_NAME)
        context.contentResolver.query(uri, projection, null, null, null).use { cursor ->
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    val raw = cursor.getString(index) ?: fallback
                    return raw.replace("/", "_")
                }
            }
        }
        return fallback
    }

    companion object {
        const val KEY_URI = "uri"
        const val KEY_FORMAT = "format"
        const val KEY_SUMMARY = "summary"
        const val KEY_PATH = "path"
        const val KEY_IS_ZIP = "is_zip"
        const val KEY_ENGINE = "engine"
        const val KEY_METADATA = "metadata"
        const val KEY_ERROR = "error"
    }
}
