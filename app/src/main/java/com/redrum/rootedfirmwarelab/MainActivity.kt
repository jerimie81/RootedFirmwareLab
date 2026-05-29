package com.redrum.rootedfirmwarelab

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.redrum.rootedfirmwarelab.core.service.BinaryDeployer
import com.redrum.rootedfirmwarelab.core.service.LogManager
import com.redrum.rootedfirmwarelab.core.service.TerminalSession
import com.redrum.rootedfirmwarelab.core.service.ToolLibrary
import com.redrum.rootedfirmwarelab.data.LogEntry
import com.redrum.rootedfirmwarelab.data.LogType
import com.redrum.rootedfirmwarelab.nativebridge.jni.NativeFirmwareBridge
import com.redrum.rootedfirmwarelab.nativebridge.service.RootMountService
import com.redrum.rootedfirmwarelab.ui.LabRootMissingScreen
import com.redrum.rootedfirmwarelab.ui.LabRootValidatorScreen
import com.redrum.rootedfirmwarelab.ui.LabRootedFirmwareApp
import com.redrum.rootedfirmwarelab.ui.onboarding.OnboardingWalkthrough
import com.redrum.rootedfirmwarelab.ui.state.ThemeConfig
import com.redrum.rootedfirmwarelab.ui.state.UiStateStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

enum class Screen { Dashboard, Logs, Settings, Terminal, Tools, Browser, RootCheck, PartitionViewer }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
class MainActivity : ComponentActivity() {
    private val rootMountService = RootMountService()
    private val firmwareBridge = NativeFirmwareBridge()
    private lateinit var binaryDeployer: BinaryDeployer
    private lateinit var logManager: LogManager // Initialize LogManager
    private lateinit var terminalSession: TerminalSession

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            // Handle permissions result
            if (permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true &&
                permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true &&
                permissions[Manifest.permission.MANAGE_EXTERNAL_STORAGE] == true) {
                // Permissions granted
            } else {
                // Permissions denied, inform user
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binaryDeployer = BinaryDeployer(applicationContext)
        binaryDeployer.deployBinaries() // Deploy binaries on app startup
        val logDao = com.redrum.rootedfirmwarelab.data.LogDatabase.getDatabase(applicationContext).logDao()
        ToolLibrary.initialize(applicationContext, logDao)
        logManager = LogManager(logDao) // Initialize LogManager
        terminalSession = TerminalSession(lifecycleScope)
        terminalSession.start()
        logManager.log("Application started.")
        val uiStateStore = UiStateStore(applicationContext, lifecycleScope)

        // Request permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.MANAGE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.MANAGE_EXTERNAL_STORAGE
                )
            )
        }

        setContent {
            var themeConfig by remember { mutableStateOf(ThemeConfig()) }
            var showOnboarding by remember { mutableStateOf(!uiStateStore.isFirstRunComplete()) }
            
            LaunchedEffect(Unit) {
                themeConfig = uiStateStore.loadThemeConfig()
            }
            var isRooted by remember { mutableStateOf<Boolean?>(null) }

            if (showOnboarding) {
                OnboardingWalkthrough(
                    steps = listOf(
                        "Welcome to RootedFirmwareLab" to "A professional Android ROM kitchen.",
                        "Safety First" to "Mount guards protect sensitive partitions.",
                        "Get Started" to "Select a firmware image to begin."
                    ),
                    onDismiss = {
                        uiStateStore.markFirstRunComplete()
                        showOnboarding = false
                    }
                )
            } else {
                when (isRooted) {
                    null -> LabRootValidatorScreen(onResult = { rooted -> isRooted = rooted })
                    true -> LabRootedFirmwareApp(
                        rootMountService = rootMountService,
                        firmwareBridge = firmwareBridge,
                        logManager = logManager,
                        terminalSession = terminalSession,
                        uiStateStore = uiStateStore,
                        themeConfig = themeConfig,
                        onThemeConfigChange = { updated ->
                            themeConfig = updated
                            uiStateStore.saveThemeConfig(updated)
                        },
                    )
                    false -> LabRootMissingScreen(onRetry = { isRooted = null })
                }
            }
        }
    }
}
