package com.redrum.rootedfirmwarelab.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
class UserSettingsRepositoryTest {
    private lateinit var context: Context

    @Before
    fun setup() {
        context = org.robolectric.RuntimeEnvironment.getApplication()
    }

    @Before
    fun clearState() {
        context.getSharedPreferences("rooted_firmware_lab_ui", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        File(context.filesDir, "datastore").deleteRecursively()
    }

    @Test
    fun flowsReflectPersistedSettings() = runTest {
        val repository = UserSettingsRepository(context)

        assertFalse(repository.darkModeFlow.first())
        assertFalse(repository.highContrastFlow.first())

        repository.setDarkMode(true)
        repository.setHighContrast(true)
        repository.setThemeColors(1, 2, 3)

        assertTrue(repository.darkModeFlow.first())
        assertTrue(repository.highContrastFlow.first())
    }
}
