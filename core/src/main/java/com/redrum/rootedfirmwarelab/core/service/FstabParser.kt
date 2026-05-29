package com.redrum.rootedfirmwarelab.core.service

import com.redrum.rootedfirmwarelab.core.model.FstabEntry
import java.io.File

object FstabParser {
    fun parseFile(file: File): List<FstabEntry> {
        if (!file.exists() || !file.isFile) return emptyList()
        return parse(file.readText())
    }

    fun parse(raw: String): List<FstabEntry> {
        return raw.lineSequence()
            .map { it.substringBefore("#").trim() }
            .filter { it.isNotBlank() }
            .mapNotNull(::parseLine)
            .toList()
    }

    private fun parseLine(line: String): FstabEntry? {
        val parts = line.split(Regex("\\s+"))
        if (parts.size < 4) return null
        val mountFlags = parts.getOrNull(3)
            ?.split(",")
            ?.filter { it.isNotBlank() }
            .orEmpty()
        val fsMgrFlags = parts.getOrNull(4)
            ?.split(",")
            ?.filter { it.isNotBlank() }
            .orEmpty()
        return FstabEntry(
            blockDevice = parts[0],
            mountPoint = parts[1],
            fsType = parts[2],
            mountFlags = mountFlags,
            fsMgrFlags = fsMgrFlags,
        )
    }

    fun validate(entries: List<FstabEntry>): List<String> {
        val findings = mutableListOf<String>()
        val duplicateMounts = entries.groupBy { it.mountPoint }.filterValues { it.size > 1 }.keys
        duplicateMounts.forEach { findings += "Duplicate fstab mount point: $it" }
        entries.filter { it.fsType !in supportedFileSystems }.forEach {
            findings += "Unsupported or unusual fs type '${it.fsType}' for ${it.mountPoint}"
        }
        entries.filter { "avb" in it.fsMgrFlags.joinToString(",") && "verify" !in it.fsMgrFlags.joinToString(",") }.forEach {
            findings += "AVB-related entry lacks explicit verify flag: ${it.mountPoint}"
        }
        return findings
    }

    private val supportedFileSystems = setOf("ext4", "f2fs", "erofs", "vfat", "emmc", "swap", "auto")
}
