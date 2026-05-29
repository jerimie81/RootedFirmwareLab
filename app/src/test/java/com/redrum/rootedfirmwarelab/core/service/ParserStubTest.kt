package com.redrum.rootedfirmwarelab.core.service

import com.redrum.rootedfirmwarelab.core.model.FirmwareFormat
import com.redrum.rootedfirmwarelab.core.model.ParseRequest
import org.junit.Assert.assertEquals
import org.junit.Test

class ParserStubTest {
    @Test
    fun binElfAndImgStubsReturnExpectedFormats() {
        assertEquals(FirmwareFormat.PAYLOAD_BIN, BinParserStub().inspect("/tmp/payload.bin").format)
        assertEquals(FirmwareFormat.ELF, ElfParserStub().inspect("/tmp/lib.so").format)
        assertEquals(FirmwareFormat.RAW_IMG, ImgParserStub().inspect("/tmp/system.img").format)
    }

    @Test
    fun firmwareParserStubProvidesNoopImplementation() {
        val parser = FirmwareParser.stub()

        assertEquals("stub-parser", parser.engineName())
        assertEquals(FirmwareFormat.UNKNOWN, parser.inspect(ParseRequest("/tmp/file")).format)
        assertEquals(false, parser.decompile("/tmp/file"))
        assertEquals(false, parser.disassemble("/tmp/file"))
        assertEquals(false, parser.recompile("/tmp/file"))
        assertEquals(false, parser.assemble("/tmp/file"))
    }
}
