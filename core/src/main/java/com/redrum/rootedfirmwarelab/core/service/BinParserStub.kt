package com.redrum.rootedfirmwarelab.core.service

import com.redrum.rootedfirmwarelab.core.model.FirmwareFormat
import com.redrum.rootedfirmwarelab.core.model.ParseResult

class BinParserStub {
    fun inspect(filePath: String): ParseResult {
        return ParseResult(
            format = FirmwareFormat.PAYLOAD_BIN,
            summary = "BIN parser stub: payload manifest parsing pending.",
            metadata = mapOf(
                "path" to filePath,
                "next" to "implement payload header + partition stream extraction",
            ),
        )
    }
}
