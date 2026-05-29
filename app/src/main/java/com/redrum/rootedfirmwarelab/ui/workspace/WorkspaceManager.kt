package com.redrum.rootedfirmwarelab.ui.workspace

import android.content.Context
import java.io.File

class WorkspaceManager(context: Context) {
    private val workspaceDir = File(context.filesDir, "workspaces")

    init {
        if (!workspaceDir.exists()) workspaceDir.mkdirs()
    }

    fun listWorkspaces(): List<String> {
        return workspaceDir.listFiles { file -> file.isDirectory }?.map { it.name } ?: emptyList()
    }

    fun createWorkspace(name: String) {
        File(workspaceDir, name).mkdirs()
    }
}
