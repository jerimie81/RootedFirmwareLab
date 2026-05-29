package com.redrum.rootedfirmwarelab.ui.workspace

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class WorkspaceManagerTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun clearWorkspaces() {
        File(context.filesDir, "workspaces").deleteRecursively()
    }

    @Test
    fun createWorkspaceAddsDirectoryToListing() {
        val manager = WorkspaceManager(context)

        manager.createWorkspace("demo")

        assertTrue("demo" in manager.listWorkspaces())
    }
}
