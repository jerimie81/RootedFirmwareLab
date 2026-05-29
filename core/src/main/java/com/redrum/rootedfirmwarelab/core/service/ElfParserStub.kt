package com.redrum.rootedfirmwarelab.core.service

import com.redrum.rootedfirmwarelab.core.model.FirmwareFormat
import com.redrum.rootedfirmwarelab.core.model.ParseResult

class ElfParserStub {
    fun inspect(filePath: String): ParseResult {
        return ParseResult(
            format = FirmwareFormat.ELF,
            summary = "ELF parser stub: section/symbol parsing pending.",
            metadata = mapOf(
                "path" to filePath,
                "next" to "implement ELF class, machine, section table, symbols",
            ),
        )
    }
}
