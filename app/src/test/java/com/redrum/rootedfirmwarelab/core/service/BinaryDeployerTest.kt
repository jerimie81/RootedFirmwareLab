package com.redrum.rootedfirmwarelab.core.service

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class BinaryDeployerTest {
    @Test
    fun getToolPathResolvesIntoAppFilesDir() {
        val context = mockk<Context>()
        val filesDir = File("/mock/files")
        every { context.filesDir } returns filesDir
        val deployer = BinaryDeployer(context)

        assertTrue(deployer.getToolPath("lpunpack").contains(filesDir.absolutePath))
    }
}
