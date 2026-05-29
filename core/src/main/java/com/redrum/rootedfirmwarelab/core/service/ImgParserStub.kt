package com.redrum.rootedfirmwarelab.core.service

import com.redrum.rootedfirmwarelab.core.model.FirmwareFormat
import com.redrum.rootedfirmwarelab.core.model.ParseResult

class ImgParserStub {
    fun inspect(filePath: String): ParseResult {
        return ParseResult(
            format = FirmwareFormat.RAW_IMG,
            summary = "IMG parser stub: header probe and sparse/raw decision pending.",
            metadata = mapOf(
                "path" to filePath,
                "next" to "implement sparse header + ext4/erofs probing",
            ),
        )
    }
}
