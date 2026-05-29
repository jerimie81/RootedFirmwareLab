package com.redrum.rootedfirmwarelab.core.service

import com.redrum.rootedfirmwarelab.core.model.FirmwareFormat
import com.redrum.rootedfirmwarelab.core.model.ParseRequest
import com.redrum.rootedfirmwarelab.core.model.ParseResult

interface FirmwareParser {
    fun engineName(): String
    fun inspect(request: ParseRequest): ParseResult
    fun decompile(filePath: String): Boolean
    fun disassemble(filePath: String): Boolean
    fun recompile(filePath: String): Boolean
    fun assemble(filePath: String): Boolean

    companion object {
        fun stub(): FirmwareParser = object : FirmwareParser {
            override fun engineName(): String = "stub-parser"

            override fun inspect(request: ParseRequest): ParseResult {
                return ParseResult(
                    format = FirmwareFormat.UNKNOWN,
                    summary = "No native parser bound yet for ${request.filePath}",
                    metadata = mapOf("rootedMode" to request.rootedMode.toString()),
                )
            }
            override fun decompile(filePath: String): Boolean = false
            override fun disassemble(filePath: String): Boolean = false
            override fun recompile(filePath: String): Boolean = false
            override fun assemble(filePath: String): Boolean = false
        }
    }
}
