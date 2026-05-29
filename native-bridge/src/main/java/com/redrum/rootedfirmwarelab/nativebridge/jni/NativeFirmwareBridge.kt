package com.redrum.rootedfirmwarelab.nativebridge.jni

import com.redrum.rootedfirmwarelab.core.model.FirmwareFormat
import com.redrum.rootedfirmwarelab.core.model.ParseRequest
import com.redrum.rootedfirmwarelab.core.model.ParseResult
import com.redrum.rootedfirmwarelab.core.service.FirmwareParser

class NativeFirmwareBridge : FirmwareParser {
    override fun engineName(): String = nativeEngineName()

    override fun inspect(request: ParseRequest): ParseResult {
        val native = nativeInspect(request.filePath, request.rootedMode)
        val format = runCatching { FirmwareFormat.valueOf(native.format) }.getOrDefault(FirmwareFormat.UNKNOWN)
        return ParseResult(
            format = format,
            summary = native.summary,
            metadata = native.metadata,
        )
    }

    override fun decompile(filePath: String): Boolean = nativeDecompile(filePath)
    override fun disassemble(filePath: String): Boolean = nativeDisassemble(filePath)
    override fun recompile(filePath: String): Boolean = nativeRecompile(filePath)
    override fun assemble(filePath: String): Boolean = nativeAssemble(filePath)

    // Advanced ROM Kitchen Actions
    fun convertSparseToRaw(src: String, dest: String): Boolean = nativeConvertSparseToRaw(src, dest)
    fun convertRawToSparse(src: String, dest: String): Boolean = nativeConvertRawToSparse(src, dest)
    fun patchVbmeta(filePath: String): Boolean = nativePatchVbmeta(filePath)
    fun unpackBootImage(filePath: String, destDir: String): Boolean = nativeUnpackBootImage(filePath, destDir)
    fun repackBootImage(srcDir: String, destFile: String): Boolean = nativeRepackBootImage(srcDir, destFile)
    fun extractPayload(filePath: String, destDir: String): Boolean = nativeExtractPayload(filePath, destDir)
    fun parseSuperImage(filePath: String): Map<String, String> = nativeParseSuperImage(filePath)
    fun decompileDtb(filePath: String): String = nativeDecompileDtb(filePath)

    private external fun nativeEngineName(): String
    private external fun nativeInspect(filePath: String, rootedMode: Boolean): NativeInspectResult
    private external fun nativeDecompile(filePath: String): Boolean
    private external fun nativeDisassemble(filePath: String): Boolean
    private external fun nativeRecompile(filePath: String): Boolean
    private external fun nativeAssemble(filePath: String): Boolean

    // ROM Kitchen External Functions
    private external fun nativeConvertSparseToRaw(src: String, dest: String): Boolean
    private external fun nativeConvertRawToSparse(src: String, dest: String): Boolean
    private external fun nativePatchVbmeta(filePath: String): Boolean
    private external fun nativeUnpackBootImage(filePath: String, destDir: String): Boolean
    private external fun nativeRepackBootImage(srcDir: String, destFile: String): Boolean
    private external fun nativeExtractPayload(filePath: String, destDir: String): Boolean
    private external fun nativeParseSuperImage(filePath: String): Map<String, String>
    private external fun nativeDecompileDtb(filePath: String): String
    
    // Partition Analysis
    private external fun nativeListPartitionFiles(imagePath: String, path: String): Any?
    private external fun nativeExtractFile(imagePath: String, path: String, dest: String): Boolean

    companion object {
        init {
            System.loadLibrary("native_engine")
        }
    }
}

data class NativeInspectResult(
    val format: String,
    val summary: String,
    val metadata: Map<String, String>,
)
