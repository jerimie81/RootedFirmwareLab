package com.redrum.rootedfirmwarelab.ui

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.redrum.rootedfirmwarelab.ui.state.RecentFirmwareEntry
import com.redrum.rootedfirmwarelab.ui.state.ThemeConfig
import com.redrum.rootedfirmwarelab.ui.state.ToolCommandHistoryEntry
import com.redrum.rootedfirmwarelab.ui.state.UiStateStore
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class UiStateStoreTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun clearState() {
        context.getSharedPreferences("rooted_firmware_lab_ui", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        File(context.filesDir, "datastore").deleteRecursively()
    }

    @Test
    fun themeConfigAndHistoryRoundTrip() = runTest {
        val store = UiStateStore(context, this)

        val defaultTheme = store.loadThemeConfig()
        assertFalse(defaultTheme.darkMode)
        assertFalse(defaultTheme.highContrast)

        store.saveThemeConfig(
            ThemeConfig(
                darkMode = true,
                highContrast = true,
                primaryArgb = 11,
                secondaryArgb = 22,
                tertiaryArgb = 33,
            ),
        )
        
        val savedTheme = store.loadThemeConfig()
        assertTrue(savedTheme.darkMode)
        assertTrue(savedTheme.highContrast)
        assertEquals(11, savedTheme.primaryArgb)
        assertEquals(22, savedTheme.secondaryArgb)
        assertEquals(33, savedTheme.tertiaryArgb)

        store.addRecentFirmware(RecentFirmwareEntry("uri://1", "img1", 1L))
        store.addRecentFirmware(RecentFirmwareEntry("uri://2", "img2", 2L))
        assertEquals(listOf("uri://2", "uri://1"), store.loadRecentFirmware().map { it.uri })

        store.saveFavoriteTools(setOf("lpunpack"))
        assertTrue("lpunpack" in store.loadFavoriteTools())

        store.addCommandHistory(
            ToolCommandHistoryEntry("lpunpack", listOf("a", "b"), "lpunpack 'a' 'b'", 3L),
        )
        assertEquals(1, store.loadCommandHistory().size)

        assertFalse(store.isFirstRunComplete())
        store.markFirstRunComplete()
        assertTrue(store.isFirstRunComplete())
        store.resetFirstRun()
        assertFalse(store.isFirstRunComplete())
    }
}
