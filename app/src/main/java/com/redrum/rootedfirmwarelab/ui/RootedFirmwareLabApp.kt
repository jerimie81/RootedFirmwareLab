@file:OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.animation.ExperimentalAnimationApi::class,
)

package com.redrum.rootedfirmwarelab.ui

import android.content.Context
import android.net.Uri
import android.view.DragEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.compose.runtime.livedata.observeAsState
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.redrum.rootedfirmwarelab.FirmwareInspectWorker
import com.redrum.rootedfirmwarelab.core.service.LogManager
import com.redrum.rootedfirmwarelab.core.service.TerminalSession
import com.redrum.rootedfirmwarelab.core.service.ToolLibrary
import com.redrum.rootedfirmwarelab.core.service.ToolLibrary.availableTools
import com.redrum.rootedfirmwarelab.core.service.ToolLibrary.placeholderCount
import com.redrum.rootedfirmwarelab.data.LogEntry
import com.redrum.rootedfirmwarelab.data.LogType
import com.redrum.rootedfirmwarelab.nativebridge.jni.NativeFirmwareBridge
import com.redrum.rootedfirmwarelab.nativebridge.service.RootMountService
import com.redrum.rootedfirmwarelab.ui.state.RecentFirmwareEntry
import com.redrum.rootedfirmwarelab.ui.state.ThemeConfig
import com.redrum.rootedfirmwarelab.ui.state.ThemePreset
import com.redrum.rootedfirmwarelab.ui.state.ToolCommandHistoryEntry
import com.redrum.rootedfirmwarelab.ui.state.UiStateStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

enum class LabScreen {
    Dashboard,
    Browser,
    Tools,
    Logs,
    Terminal,
    Settings,
    PartitionViewer,
}

data class FirmwareInspectionState(
    val requestId: UUID,
    val uriString: String,
    val displayName: String,
    val status: WorkInfo.State = WorkInfo.State.ENQUEUED,
    val format: String? = null,
    val summary: String? = null,
    val path: String? = null,
    val engine: String? = null,
    val metadata: Map<String, String> = emptyMap(),
)

data class ToolDraft(
    val toolName: String,
    val arguments: List<String>,
)

data class PartitionSlice(
    val name: String,
    val sizeBytes: Long,
)

private val themePresets = listOf(
    ThemePreset("Volt", Color(0xFF3DDC97), Color(0xFF7B9CFF), Color(0xFFFFB74D)),
    ThemePreset("Midnight", Color(0xFF8AB4F8), Color(0xFFA5D6A7), Color(0xFFFF8A80)),
    ThemePreset("Forge", Color(0xFFFFA000), Color(0xFFFFB74D), Color(0xFF90CAF9)),
    ThemePreset("Graphite", Color(0xFFB0BEC5), Color(0xFF80CBC4), Color(0xFFFFCC80)),
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun LabRootValidatorScreen(onResult: (Boolean) -> Unit) {
    LaunchedEffect(Unit) {
        val rooted = try {
            ProcessBuilder("su", "-c", "id").start().waitFor() == 0
        } catch (_: Exception) {
            false
        }
        onResult(rooted)
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            androidx.compose.material3.CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text("Checking for root access...")
        }
    }
}

@Composable
fun LabRootMissingScreen(onRetry: () -> Unit) {
    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier.padding(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        ) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                Spacer(Modifier.height(12.dp))
                Text(
                    "Root access required",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "RootedFirmwareLab needs su access to inspect, mount, and execute firmware workflows.",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = onRetry) {
                    Text("Recheck root")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun LabRootedFirmwareApp(
    rootMountService: RootMountService,
    firmwareBridge: NativeFirmwareBridge,
    logManager: LogManager,
    terminalSession: TerminalSession,
    uiStateStore: UiStateStore,
    themeConfig: ThemeConfig,
    onThemeConfigChange: (ThemeConfig) -> Unit,
) {
    val context = LocalContext.current
    val workManager = remember { WorkManager.getInstance(context) }
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val wideLayout = configuration.screenWidthDp >= 840
    val drawerState = rememberDrawerState(initialValue = androidx.compose.material3.DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }

    var currentScreen by rememberSaveable { mutableStateOf(LabScreen.Dashboard) }
    var showPalette by rememberSaveable { mutableStateOf(false) }
    var showOnboarding by remember { mutableStateOf(!uiStateStore.isFirstRunComplete()) }
    var currentInspection by remember { mutableStateOf<FirmwareInspectionState?>(null) }
    var currentWorkId by remember { mutableStateOf<UUID?>(null) }
    var toolDraft by remember { mutableStateOf(ToolDraft(availableTools.first().name, emptyList())) }
    val toolOutput = remember { mutableStateListOf<String>() }
    var toolRunInFlight by remember { mutableStateOf(false) }
    var toolFavorites by remember { mutableStateOf(uiStateStore.loadFavoriteTools()) }
    var commandHistory by remember { mutableStateOf(uiStateStore.loadCommandHistory()) }
    var recentFirmware by remember { mutableStateOf(uiStateStore.loadRecentFirmware()) }
    val allLogs by logManager.getAllLogs().collectAsState(initial = emptyList())
    val mountPoint = "/storage/emulated/0/firmware"

    val workInfo = currentWorkId?.let { workManager.getWorkInfoByIdLiveData(it).observeAsState().value }

    LaunchedEffect(workInfo?.state, workInfo?.outputData) {
        val inspection = currentInspection ?: return@LaunchedEffect
        val output = workInfo?.outputData ?: Data.EMPTY
        val metadata = parseMetadata(output.getString(FirmwareInspectWorker.KEY_METADATA))
        val updated = inspection.copy(
            status = workInfo?.state ?: inspection.status,
            format = output.getString(FirmwareInspectWorker.KEY_FORMAT) ?: inspection.format,
            summary = output.getString(FirmwareInspectWorker.KEY_SUMMARY) ?: inspection.summary,
            path = output.getString(FirmwareInspectWorker.KEY_PATH) ?: inspection.path,
            engine = output.getString(FirmwareInspectWorker.KEY_ENGINE) ?: inspection.engine,
            metadata = metadata.ifEmpty { inspection.metadata },
        )
        currentInspection = updated
        if (workInfo?.state == WorkInfo.State.SUCCEEDED && updated.path != null) {
            recentFirmware = upsertRecent(
                uiStateStore,
                recentFirmware,
                RecentFirmwareEntry(updated.uriString, updated.displayName, System.currentTimeMillis()),
            )
        }
    }

    val runCurrentTool: suspend () -> Unit = {
        val tool = availableTools.firstOrNull { it.name == toolDraft.toolName } ?: availableTools.first()
        val normalized = ensureArgumentCount(tool, toolDraft.arguments)
        toolDraft = ToolDraft(tool.name, normalized)
        toolRunInFlight = true
        try {
            toolOutput.clear()
            toolOutput.add("Dry-run: ${ToolLibrary.previewCommand(tool, normalized)}")
            val result = withContext(Dispatchers.IO) {
                ToolLibrary.executeTemplate(tool, normalized)
            }
            toolOutput.addAll(result.lineSequence().toList())
            val preview = ToolLibrary.previewCommand(tool, normalized)
            commandHistory = upsertCommandHistory(
                uiStateStore,
                commandHistory,
                ToolCommandHistoryEntry(tool.name, normalized, preview, System.currentTimeMillis()),
            )
            logManager.log("Executed tool ${tool.name}", LogType.INFO)
        } finally {
            toolRunInFlight = false
        }
    }

    val requestInspection: (Uri, String) -> Unit = { uri, displayName ->
        val request = OneTimeWorkRequestBuilder<FirmwareInspectWorker>()
            .setInputData(
                Data.Builder()
                    .putString(FirmwareInspectWorker.KEY_URI, uri.toString())
                    .build(),
            )
            .build()
        currentInspection = FirmwareInspectionState(
            requestId = request.id,
            uriString = uri.toString(),
            displayName = displayName,
            status = WorkInfo.State.ENQUEUED,
        )
        currentWorkId = request.id
        workManager.enqueue(request)
        logManager.log("Queued inspection for $displayName", LogType.INFO)
        scope.launch { snackbarHostState.showSnackbar("Inspection queued") }
    }

    val inspectCurrentSelection: () -> Unit = {
        currentInspection?.let { inspection ->
            requestInspection(Uri.parse(inspection.uriString), inspection.displayName)
        }
    }

    val mountCurrentInspection: () -> Boolean = mountCurrentInspection@{
        val path = currentInspection?.path ?: return@mountCurrentInspection false
        rootMountService.ensureMountPoint(mountPoint)
        val success = rootMountService.executeCommand(rootMountService.mountReadOnly(path, mountPoint))
        logManager.log(if (success) "Mounted $path at $mountPoint" else "Mount failed for $path", if (success) LogType.INFO else LogType.ERROR)
        success
    }

    val unmountCurrentInspection: () -> Boolean = {
        val success = rootMountService.executeCommand(rootMountService.unmount(mountPoint))
        logManager.log(if (success) "Unmounted $mountPoint" else "Unmount failed for $mountPoint", if (success) LogType.INFO else LogType.ERROR)
        success
    }

    val exportLogsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain"),
    ) { uri ->
        if (uri != null) {
            val payload = buildLogsExport(allLogs)
            writeText(context, uri, payload)
            scope.launch { snackbarHostState.showSnackbar("Logs exported") }
        }
    }

    val exportToolOutputLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain"),
    ) { uri ->
        if (uri != null) {
            writeText(context, uri, toolOutput.joinToString("\n"))
            scope.launch { snackbarHostState.showSnackbar("Output exported") }
        }
    }

    val exportScriptLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/x-shellscript"),
    ) { uri ->
        if (uri != null) {
            val tool = availableTools.firstOrNull { it.name == toolDraft.toolName } ?: availableTools.first()
            val script = buildShellScript(tool, toolDraft.arguments)
            writeText(context, uri, script)
            scope.launch { snackbarHostState.showSnackbar("Shell script exported") }
        }
    }

    val exportSnapshotLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip"),
    ) { uri ->
        if (uri != null) {
            currentInspection?.let { inspection ->
                writeSnapshotZip(context, uri, inspection, allLogs)
                scope.launch { snackbarHostState.showSnackbar("Project snapshot exported") }
            }
        }
    }

    val handleShortcut: (LabScreen) -> Unit = { screen ->
        currentScreen = screen
    }

    val keyboardModifier = Modifier.onPreviewKeyEvent { event ->
        if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
        when {
            event.isCtrlPressed && event.key == Key.K -> {
                showPalette = true
                true
            }
            event.isCtrlPressed && event.key == Key.B -> {
                handleShortcut(LabScreen.Browser)
                true
            }
            event.isCtrlPressed && event.key == Key.R -> {
                scope.launch {
                    when (currentScreen) {
                        LabScreen.Tools -> runCurrentTool()
                        LabScreen.Dashboard -> inspectCurrentSelection()
                        else -> Unit
                    }
                }
                true
            }
            else -> false
        }
    }

    RootedFirmwareLabTheme(themeConfig) {
        val isRootAvailable = remember { rootMountService.isRootAvailable() }
        val connectionStatus = buildConnectionStatus(
            rootMountService = rootMountService,
            inspection = currentInspection,
            isRootAvailable = isRootAvailable,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                        ),
                    ),
                )
                .then(keyboardModifier),
        ) {
            if (wideLayout) {
                Row(Modifier.fillMaxSize()) {
                    NavigationRail(
                        modifier = Modifier.fillMaxHeight().padding(vertical = 12.dp, horizontal = 8.dp),
                    ) {
                        LabNavigationItems(currentScreen) { currentScreen = it }
                    }
                    Scaffold(
                        modifier = Modifier.weight(1f),
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        topBar = {
                            LabTopBar(
                                title = "RootedFirmwareLab",
                                connectionStatus = connectionStatus,
                                onOpenPalette = { showPalette = true },
                                onMenuClick = {},
                            )
                        },
                    ) { padding ->
                        LabScreenContent(
                            modifier = Modifier.padding(padding),
                            currentScreen = currentScreen,
                            currentInspection = currentInspection,
                            themeConfig = themeConfig,
                            rootMountService = rootMountService,
                            firmwareBridge = firmwareBridge,
                            logManager = logManager,
                            allLogs = allLogs,
                            terminalSession = terminalSession,
                            toolDraft = toolDraft,
                            toolFavorites = toolFavorites,
                            toolOutput = toolOutput,
                            recentFirmware = recentFirmware,
                            commandHistory = commandHistory,
                            toolRunInFlight = toolRunInFlight,
                            workInfo = workInfo,
                            isRootAvailable = isRootAvailable,
                            connectionStatus = connectionStatus,
                            onRequestInspection = requestInspection,
                            onMountSnapshot = { exportSnapshotLauncher.launch("rooted-firmware-snapshot.zip") },
                            onMount = mountCurrentInspection,
                            onUnmount = unmountCurrentInspection,
                            onRunTool = { scope.launch { runCurrentTool() } },
                            onToolDraftChange = { toolDraft = it },
                            onToggleFavorite = {
                                toolFavorites = toggleFavoriteTool(uiStateStore, toolFavorites, it)
                            },
                            onClearToolOutput = { toolOutput.clear() },
                            onExportLogs = { exportLogsLauncher.launch("rooted-firmware-logs.txt") },
                            onExportToolOutput = { exportToolOutputLauncher.launch("tool-output.txt") },
                            onExportScript = { exportScriptLauncher.launch("${toolDraft.toolName}.sh") },
                            onAddHistory = {
                                commandHistory = upsertCommandHistory(uiStateStore, commandHistory, it)
                            },
                            onNavigate = { currentScreen = it },
                            onOpenPalette = { showPalette = true },
                            onThemeChange = onThemeConfigChange,
                            onThemePresetSelected = { preset ->
                                onThemeConfigChange(
                                    themeConfig.copy(
                                        primaryArgb = preset.primary.toArgb(),
                                        secondaryArgb = preset.secondary.toArgb(),
                                        tertiaryArgb = preset.tertiary.toArgb(),
                                    ),
                                )
                            },
                            onLaunchExportSnapshot = {
                                exportSnapshotLauncher.launch("project-snapshot.zip")
                            },
                            onDismissOnboarding = {
                                uiStateStore.markFirstRunComplete()
                                showOnboarding = false
                            },
                            onResetOnboarding = {
                                uiStateStore.resetFirstRun()
                                showOnboarding = true
                            },
                        )
                    }
                }
            } else {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet {
                            LabDrawerHeader(connectionStatus)
                            LabNavigationItems(currentScreen) {
                                currentScreen = it
                                scope.launch { drawerState.close() }
                            }
                        }
                    },
                ) {
                    Scaffold(
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        topBar = {
                            LabTopBar(
                                title = "RootedFirmwareLab",
                                connectionStatus = connectionStatus,
                                onOpenPalette = { showPalette = true },
                                onMenuClick = { scope.launch { drawerState.open() } },
                            )
                        },
                    ) { padding ->
                        LabScreenContent(
                            modifier = Modifier.padding(padding),
                            currentScreen = currentScreen,
                            currentInspection = currentInspection,
                            themeConfig = themeConfig,
                            rootMountService = rootMountService,
                            firmwareBridge = firmwareBridge,
                            logManager = logManager,
                            allLogs = allLogs,
                            terminalSession = terminalSession,
                            toolDraft = toolDraft,
                            toolFavorites = toolFavorites,
                            toolOutput = toolOutput,
                            recentFirmware = recentFirmware,
                            commandHistory = commandHistory,
                            toolRunInFlight = toolRunInFlight,
                            workInfo = workInfo,
                            isRootAvailable = isRootAvailable,
                            connectionStatus = connectionStatus,
                            onRequestInspection = requestInspection,
                            onMountSnapshot = { exportSnapshotLauncher.launch("rooted-firmware-snapshot.zip") },
                            onMount = mountCurrentInspection,
                            onUnmount = unmountCurrentInspection,
                            onRunTool = { scope.launch { runCurrentTool() } },
                            onToolDraftChange = { toolDraft = it },
                            onToggleFavorite = {
                                toolFavorites = toggleFavoriteTool(uiStateStore, toolFavorites, it)
                            },
                            onClearToolOutput = { toolOutput.clear() },
                            onExportLogs = { exportLogsLauncher.launch("rooted-firmware-logs.txt") },
                            onExportToolOutput = { exportToolOutputLauncher.launch("tool-output.txt") },
                            onExportScript = { exportScriptLauncher.launch("${toolDraft.toolName}.sh") },
                            onAddHistory = {
                                commandHistory = upsertCommandHistory(uiStateStore, commandHistory, it)
                            },
                            onNavigate = { currentScreen = it },
                            onOpenPalette = { showPalette = true },
                            onThemeChange = onThemeConfigChange,
                            onThemePresetSelected = { preset ->
                                onThemeConfigChange(
                                    themeConfig.copy(
                                        primaryArgb = preset.primary.toArgb(),
                                        secondaryArgb = preset.secondary.toArgb(),
                                        tertiaryArgb = preset.tertiary.toArgb(),
                                    ),
                                )
                            },
                            onLaunchExportSnapshot = {
                                exportSnapshotLauncher.launch("project-snapshot.zip")
                            },
                            onDismissOnboarding = {
                                uiStateStore.markFirstRunComplete()
                                showOnboarding = false
                            },
                            onResetOnboarding = {
                                uiStateStore.resetFirstRun()
                                showOnboarding = true
                            },
                        )
                    }
                }
            }

            AnimatedVisibility(visible = showPalette) {
                CommandPaletteDialog(
                    currentScreen = currentScreen,
                    toolDraft = toolDraft,
                    recentFirmware = recentFirmware,
                    commandHistory = commandHistory,
                    onDismiss = { showPalette = false },
                    onNavigate = { currentScreen = it },
                    onSelectTool = { toolName ->
                        val tool = availableTools.firstOrNull { it.name == toolName } ?: return@CommandPaletteDialog
                        toolDraft = ToolDraft(tool.name, ensureArgumentCount(tool, emptyList()))
                        currentScreen = LabScreen.Tools
                        showPalette = false
                    },
                    onSelectRecentFirmware = { entry ->
                        requestInspection(Uri.parse(entry.uri), entry.displayName)
                        currentScreen = LabScreen.Dashboard
                        showPalette = false
                    },
                    onSelectHistory = { entry ->
                        val tool = availableTools.firstOrNull { it.name == entry.toolName } ?: return@CommandPaletteDialog
                        toolDraft = ToolDraft(tool.name, entry.arguments)
                        currentScreen = LabScreen.Tools
                        showPalette = false
                    },
                )
            }

            if (showOnboarding) {
                OnboardingDialog(
                    onDismiss = {
                        uiStateStore.markFirstRunComplete()
                        showOnboarding = false
                    },
                )
            }
        }
    }
}

@Composable
private fun RootedFirmwareLabTheme(
    themeConfig: ThemeConfig,
    content: @Composable () -> Unit,
) {
    val primary = Color(themeConfig.primaryArgb)
    val secondary = Color(themeConfig.secondaryArgb)
    val tertiary = Color(themeConfig.tertiaryArgb)
    val background = if (themeConfig.highContrast) {
        if (themeConfig.darkMode) Color(0xFF05070A) else Color(0xFFFFFFFF)
    } else if (themeConfig.darkMode) {
        Color(0xFF0F1117)
    } else {
        Color(0xFFF7F8FC)
    }
    val surface = if (themeConfig.highContrast) {
        if (themeConfig.darkMode) Color(0xFF000000) else Color(0xFFFFFFFF)
    } else if (themeConfig.darkMode) {
        Color(0xFF151923)
    } else {
        Color(0xFFFFFFFF)
    }
    val base = if (themeConfig.darkMode) {
        androidx.compose.material3.darkColorScheme(
            primary = primary,
            secondary = secondary,
            tertiary = tertiary,
            background = background,
            surface = surface,
            surfaceVariant = if (themeConfig.highContrast) Color(0xFF000000) else Color(0xFF1E2533),
            onSurface = if (themeConfig.highContrast) Color.White else Color(0xFFE7EDF7),
            onBackground = if (themeConfig.highContrast) Color.White else Color(0xFFE7EDF7),
            errorContainer = Color(0xFF5A1010),
            onErrorContainer = Color(0xFFFFD7D7),
        )
    } else {
        androidx.compose.material3.lightColorScheme(
            primary = primary,
            secondary = secondary,
            tertiary = tertiary,
            background = background,
            surface = surface,
            surfaceVariant = if (themeConfig.highContrast) Color(0xFFE0E0E0) else Color(0xFFE6EAF3),
            onSurface = if (themeConfig.highContrast) Color.Black else Color(0xFF18212F),
            onBackground = if (themeConfig.highContrast) Color.Black else Color(0xFF18212F),
            errorContainer = Color(0xFFFFDADB),
            onErrorContainer = Color(0xFF7A0000),
        )
    }
    MaterialTheme(colorScheme = base, content = content)
}

private fun buildConnectionStatus(
    rootMountService: RootMountService,
    inspection: FirmwareInspectionState?,
    isRootAvailable: Boolean,
): String {
    val rooted = if (isRootAvailable) "root ok" else "root missing"
    val mounted = if (inspection?.status == WorkInfo.State.RUNNING) "inspecting" else "ready"
    return "$rooted • $mounted • ${rootMountService::class.java.simpleName}"
}

@Composable
private fun LabTopBar(
    title: String,
    connectionStatus: String,
    onOpenPalette: () -> Unit,
    onMenuClick: () -> Unit,
) {
    TopAppBar(
        title = {
            Column {
                Text(title)
                Text(
                    connectionStatus,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Open navigation")
            }
        },
        actions = {
            IconButton(onClick = onOpenPalette) {
                Icon(Icons.Default.Search, contentDescription = "Open command palette")
            }
        },
    )
}

@Composable
private fun LabDrawerHeader(connectionStatus: String) {
    Column(Modifier.fillMaxWidth().padding(20.dp)) {
        Text("RootedFirmwareLab", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        StatusBadge(connectionStatus, success = true)
    }
    HorizontalDivider()
}

@Composable
private fun LabNavigationItems(
    currentScreen: LabScreen,
    onNavigate: (LabScreen) -> Unit,
) {
    val items = listOf(
        LabScreen.Dashboard to Triple("Dashboard", Icons.Default.Home, "Inspect firmware and mount partitions"),
        LabScreen.Browser to Triple("Browser", Icons.Default.Folder, "Browse extracted workspace files"),
        LabScreen.Tools to Triple("Tools", Icons.Default.Build, "Run firmware tools and export scripts"),
        LabScreen.Logs to Triple("Logs", Icons.Default.History, "Review categorized logs"),
        LabScreen.Terminal to Triple("Terminal", Icons.Default.Terminal, "Interact with a root shell"),
        LabScreen.PartitionViewer to Triple("Partitions", Icons.Default.Storage, "Visualize super.img layout"),
        LabScreen.Settings to Triple("Settings", Icons.Default.Settings, "Theme and accessibility"),
    )

    items.forEach { (screen, payload) ->
        NavigationRailItem(
            selected = currentScreen == screen,
            onClick = { onNavigate(screen) },
            icon = { Icon(payload.second, contentDescription = payload.first) },
            label = { Text(payload.first) },
        )
    }
}

@Composable
private fun LabScreenContent(
    modifier: Modifier,
    currentScreen: LabScreen,
    currentInspection: FirmwareInspectionState?,
    themeConfig: ThemeConfig,
    rootMountService: RootMountService,
    firmwareBridge: NativeFirmwareBridge,
    logManager: LogManager,
    allLogs: List<LogEntry>,
    terminalSession: TerminalSession,
    toolDraft: ToolDraft,
    toolFavorites: Set<String>,
    toolOutput: SnapshotStateList<String>,
    recentFirmware: List<RecentFirmwareEntry>,
    commandHistory: List<ToolCommandHistoryEntry>,
    toolRunInFlight: Boolean,
    workInfo: WorkInfo?,
    isRootAvailable: Boolean,
    connectionStatus: String,
    onRequestInspection: (Uri, String) -> Unit,
    onMountSnapshot: () -> Unit,
    onMount: () -> Boolean,
    onUnmount: () -> Boolean,
    onRunTool: () -> Unit,
    onToolDraftChange: (ToolDraft) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onClearToolOutput: () -> Unit,
    onExportLogs: () -> Unit,
    onExportToolOutput: () -> Unit,
    onExportScript: () -> Unit,
    onAddHistory: (ToolCommandHistoryEntry) -> Unit,
    onNavigate: (LabScreen) -> Unit,
    onOpenPalette: () -> Unit,
    onThemeChange: (ThemeConfig) -> Unit,
    onThemePresetSelected: (ThemePreset) -> Unit,
    onLaunchExportSnapshot: () -> Unit,
    onDismissOnboarding: () -> Unit,
    onResetOnboarding: () -> Unit,
) {
    val context = LocalContext.current
    AnimatedContent(
        targetState = currentScreen,
        modifier = modifier.fillMaxSize(),
        transitionSpec = {
            (slideInHorizontally { it / 5 } + fadeIn()).togetherWith(
                slideOutHorizontally { -it / 5 } + fadeOut(),
            )
        },
        label = "screen-transition",
    ) { screen ->
        when (screen) {
            LabScreen.Dashboard -> DashboardScreen(
                currentInspection = currentInspection,
                recentFirmware = recentFirmware,
                isRootAvailable = isRootAvailable,
                workInfo = workInfo,
                rootMountService = rootMountService,
                firmwareBridge = firmwareBridge,
                logManager = logManager,
                onPickFirmware = onRequestInspection,
                onMountSnapshot = onLaunchExportSnapshot,
                onMount = onMount,
                onUnmount = onUnmount,
                onQuickAction = { action ->
                    currentInspection?.path?.let { path ->
                        scopeAction(action, path, firmwareBridge, logManager)
                    }
                },
                onExportLogs = onExportLogs,
                onOpenPalette = onOpenPalette,
                onInspectCurrent = onLaunchExportSnapshot,
                connectionStatus = connectionStatus,
                onSelectRecent = onRequestInspection,
                onRequestRefresh = { currentInspection?.let { onRequestInspection(Uri.parse(it.uriString), it.displayName) } },
            )
            LabScreen.Browser -> BrowserScreen(
                recentFirmware = recentFirmware,
                rootPath = currentInspection?.path?.let { File(it).parentFile?.absolutePath } ?: "/storage/emulated/0",
                onOpenFile = { uri -> onRequestInspection(uri, displayNameFromUri(uri, context)) },
            )
            LabScreen.Tools -> ToolsScreen(
                toolDraft = toolDraft,
                toolFavorites = toolFavorites,
                toolOutput = toolOutput,
                commandHistory = commandHistory,
                toolRunInFlight = toolRunInFlight,
                onToolDraftChange = onToolDraftChange,
                onToggleFavorite = onToggleFavorite,
                onRunTool = onRunTool,
                onClearToolOutput = onClearToolOutput,
                onExportToolOutput = onExportToolOutput,
                onExportScript = onExportScript,
                onAddHistory = onAddHistory,
            )
            LabScreen.Logs -> LogsScreen(
                logManager = logManager,
                onExportLogs = onExportLogs,
            )
            LabScreen.Terminal -> TerminalScreen(
                logManager = logManager,
                terminalSession = terminalSession,
            )
            LabScreen.Settings -> SettingsScreen(
                themeConfig = themeConfig,
                onThemeChange = onThemeChange,
                onThemePresetSelected = onThemePresetSelected,
                onResetOnboarding = onResetOnboarding,
            )
            LabScreen.PartitionViewer -> PartitionViewerScreen(
                firmwareBridge = firmwareBridge,
                inspection = currentInspection,
            )
        }
    }
}

@Composable
private fun DashboardScreen(
    currentInspection: FirmwareInspectionState?,
    recentFirmware: List<RecentFirmwareEntry>,
    isRootAvailable: Boolean,
    workInfo: WorkInfo?,
    rootMountService: RootMountService,
    firmwareBridge: NativeFirmwareBridge,
    logManager: LogManager,
    onPickFirmware: (Uri, String) -> Unit,
    onMountSnapshot: () -> Unit,
    onMount: () -> Boolean,
    onUnmount: () -> Boolean,
    onQuickAction: (String) -> Unit,
    onExportLogs: () -> Unit,
    onOpenPalette: () -> Unit,
    onInspectCurrent: () -> Unit,
    connectionStatus: String,
    onSelectRecent: (Uri, String) -> Unit,
    onRequestRefresh: () -> Unit,
) {
    val context = LocalContext.current
    var mounted by remember { mutableStateOf(false) }
    var draggedUri by remember { mutableStateOf<Uri?>(null) }
    val listState = rememberLazyListState()
    val openDocument = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val name = displayNameFromUri(uri, context)
            onPickFirmware(uri, name)
        }
    }
    val view = LocalView.current
    DisposableEffect(view) {
        val listener = android.view.View.OnDragListener { _, event ->
            if (event.action == DragEvent.ACTION_DROP) {
                val item = event.clipData?.getItemAt(0)
                draggedUri = item?.uri
                if (draggedUri != null) {
                    onPickFirmware(draggedUri!!, displayNameFromUri(draggedUri!!, context))
                }
            }
            true
        }
        view.setOnDragListener(listener)
        onDispose { view.setOnDragListener(null) }
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatusCard(
                title = "Firmware",
                subtitle = currentInspection?.displayName ?: "No firmware selected",
                value = currentInspection?.format ?: "Idle",
                icon = Icons.Default.Storage,
                accent = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            StatusCard(
                title = "Mount",
                subtitle = if (mounted) "Mounted read-only" else "Not mounted",
                value = if (mounted) "Live" else "Offline",
                icon = Icons.Default.FolderOpen,
                accent = if (mounted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f),
            )
            StatusCard(
                title = "Tools",
                subtitle = "Available ${availableTools.size}",
                value = "${availableTools.size}",
                icon = Icons.Default.Build,
                accent = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f),
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    ActionButton(
                        label = "Pick firmware",
                        icon = Icons.Default.Upload,
                        tooltip = "Open a firmware image or workspace file",
                        onClick = { openDocument.launch(arrayOf("*/*")) },
                    )
                    ActionButton(
                        label = "Refresh",
                        icon = Icons.Default.Refresh,
                        tooltip = "Re-run the latest inspection request",
                        onClick = onRequestRefresh,
                    )
                    ActionButton(
                        label = "Palette",
                        icon = Icons.Default.Search,
                        tooltip = "Open the command palette with Ctrl+K",
                        onClick = onOpenPalette,
                    )
                    ActionButton(
                        label = "Snapshot",
                        icon = Icons.Default.Save,
                        tooltip = "Export a project snapshot zip",
                        onClick = onMountSnapshot,
                    )
                }

                AnimatedVisibility(visible = workInfo != null && !workInfo.state.isFinished) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Inspection in progress...",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        androidx.compose.material3.LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }

                if (currentInspection != null) {
                InspectionSummaryCard(
                    inspection = currentInspection,
                    mounted = mounted,
                    onMount = {
                            val success = onMount()
                            mounted = success
                            success
                    },
                    onUnmount = {
                            val success = onUnmount()
                            if (success) mounted = false
                            success
                    },
                    onQuickAction = onQuickAction,
                )
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(220.dp),
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                DashboardTile(
                    title = "Partition Share",
                    subtitle = "Visual layout of super.img",
                    accent = MaterialTheme.colorScheme.tertiary,
                    content = {
                        PartitionDonutChart(
                            slices = buildPartitionSlices(currentInspection?.metadata.orEmpty()),
                            modifier = Modifier.fillMaxWidth().height(180.dp),
                        )
                    },
                )
            }
            item {
                DashboardTile(
                    title = "Recent Files",
                    subtitle = "Tap to reopen",
                    accent = MaterialTheme.colorScheme.secondary,
                    content = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            recentFirmware.take(4).forEach { entry ->
                                AssistChip(
                                    onClick = { onSelectRecent(Uri.parse(entry.uri), entry.displayName) },
                                    label = { Text(entry.displayName) },
                                )
                            }
                        }
                    },
                )
            }
            item {
                DashboardTile(
                    title = "Connection",
                    subtitle = connectionStatus,
                    accent = if (isRootAvailable) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                    content = {
                        StatusBadge(
                            label = if (isRootAvailable) "Connected" else "Root unavailable",
                            success = isRootAvailable,
                        )
                    },
                )
            }
            item {
                DashboardTile(
                    title = "Export",
                    subtitle = "Logs, output, and snapshots",
                    accent = MaterialTheme.colorScheme.primary,
                    content = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = onExportLogs) { Text("Export logs") }
                            TextButton(onClick = onMountSnapshot) { Text("Export snapshot") }
                            TextButton(onClick = onRequestRefresh) { Text("Reuse current file") }
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun StatusCard(
    title: String,
    subtitle: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).background(accent.copy(alpha = 0.18f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = title, tint = accent)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    Text(subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(value, style = MaterialTheme.typography.headlineSmall, color = accent)
        }
    }
}

@Composable
private fun DashboardTile(
    title: String,
    subtitle: String,
    accent: Color,
    content: @Composable () -> Unit,
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box(
                modifier = Modifier.fillMaxWidth().border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(16.dp)).padding(12.dp),
            ) {
                content()
            }
        }
    }
}

@Composable
private fun InspectionSummaryCard(
    inspection: FirmwareInspectionState,
    mounted: Boolean,
    onMount: () -> Boolean,
    onUnmount: () -> Boolean,
    onQuickAction: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (inspection.status == WorkInfo.State.FAILED) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
            },
        ),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBadge(
                    label = inspection.status.name.lowercase().replaceFirstChar { it.uppercase() },
                    success = inspection.status == WorkInfo.State.SUCCEEDED,
                )
                Text(inspection.displayName, style = MaterialTheme.typography.titleMedium)
            }
            Text(inspection.summary ?: "Awaiting inspection output.", style = MaterialTheme.typography.bodyMedium)
            Text("Path: ${inspection.path ?: "pending"}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Decompile", "Disassemble", "Recompile", "Assemble").forEach { action ->
                    TooltipActionChip(
                        label = action,
                        tooltip = "Run $action on the selected firmware",
                        onClick = { onQuickAction(action) },
                    )
                }
                if (mounted) {
                    TextButton(onClick = { onUnmount() }) { Text("Unmount") }
                } else {
                    TextButton(onClick = { onMount() }) { Text("Mount") }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(label: String, success: Boolean) {
    val bg = if (success) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
    val fg = Color.White
    Row(
        modifier = Modifier.background(bg.copy(alpha = 0.14f), RoundedCornerShape(999.dp)).padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(8.dp).background(bg, CircleShape))
        Spacer(Modifier.width(8.dp))
        Text(label, color = bg, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun TooltipActionChip(
    label: String,
    tooltip: String,
    onClick: () -> Unit,
) {
    val state = rememberTooltipState()
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { PlainTooltip { Text(tooltip) } },
        state = state,
    ) {
        AssistChip(onClick = onClick, label = { Text(label) })
    }
}

@Composable
private fun ActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tooltip: String,
    onClick: () -> Unit,
) {
    val state = rememberTooltipState()
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { PlainTooltip { Text(tooltip) } },
        state = state,
    ) {
        TextButton(onClick = onClick) {
            Icon(icon, contentDescription = label)
            Spacer(Modifier.width(8.dp))
            Text(label)
        }
    }
}

@Composable
private fun BrowserScreen(
    recentFirmware: List<RecentFirmwareEntry>,
    rootPath: String,
    onOpenFile: (Uri) -> Unit,
) {
    val root = remember(rootPath) { File(rootPath).takeIf { it.exists() } ?: File("/storage/emulated/0") }
    var selectedFile by remember { mutableStateOf<File?>(null) }
    // Using a set for expanded folders for efficient lookup
    val expandedFolders = remember { mutableStateListOf<String>() }
    
    // Efficiently build the visible nodes
    val nodes = remember(root, expandedFolders.toList()) {
        buildTreeNodes(root, expandedFolders.toSet())
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Browser", style = MaterialTheme.typography.headlineSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            recentFirmware.take(6).forEach { entry ->
                AssistChip(onClick = { onOpenFile(Uri.parse(entry.uri)) }, label = { Text(entry.displayName) })
            }
        }
        
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(Modifier.weight(1f).fillMaxHeight()) {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    items(nodes) { node ->
                        FileTreeItem(
                            node = node,
                            isExpanded = expandedFolders.contains(node.file.absolutePath),
                            isSelected = selectedFile == node.file,
                            onToggleExpand = {
                                if (node.file.isDirectory) {
                                    if (expandedFolders.contains(node.file.absolutePath)) {
                                        expandedFolders.remove(node.file.absolutePath)
                                    } else {
                                        expandedFolders.add(node.file.absolutePath)
                                    }
                                }
                            },
                            onSelect = { selectedFile = node.file }
                        )
                    }
                }
            }

            // Preview pane remains largely the same
            Card(Modifier.weight(1f).fillMaxHeight()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Preview", style = MaterialTheme.typography.titleMedium)
                    if (selectedFile != null) {
                        Text(selectedFile!!.absolutePath, style = MaterialTheme.typography.labelSmall)
                        val preview = remember(selectedFile!!.absolutePath) {
                            if (selectedFile!!.isFile && selectedFile!!.length() < 128_000) {
                                runCatching { selectedFile!!.readText() }.getOrDefault("Unable to preview file.")
                            } else {
                                "Preview unavailable for large files or directories."
                            }
                        }
                        SelectionContainer {
                            Text(preview, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        }
                    } else {
                        Text("Select a file or folder to inspect its contents.")
                    }
                }
            }
        }
    }
}

@Composable
private fun FileTreeItem(
    node: BrowserNode,
    isExpanded: Boolean,
    isSelected: Boolean,
    onToggleExpand: () -> Unit,
    onSelect: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent, RoundedCornerShape(4.dp))
            .clickable {
                onToggleExpand()
                onSelect()
            }
            .padding(start = (node.depth * 16).dp, top = 8.dp, bottom = 8.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (node.file.isDirectory) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.primary
            )
        } else {
            Icon(Icons.Default.Code, contentDescription = "File")
        }
        Spacer(Modifier.width(8.dp))
        Text(node.file.name.ifBlank { node.file.absolutePath }, style = MaterialTheme.typography.bodyMedium)
    }
}

internal data class BrowserNode(val file: File, val depth: Int)

// More robust tree builder
internal fun buildTreeNodes(root: File, expanded: Set<String>): List<BrowserNode> {
    val nodes = mutableListOf<BrowserNode>()
    
    fun walk(file: File, depth: Int) {
        nodes.add(BrowserNode(file, depth))
        if (file.isDirectory && expanded.contains(file.absolutePath)) {
            val children = file.listFiles()?.sortedWith(compareBy<File> { !it.isDirectory }.thenBy { it.name.lowercase(Locale.getDefault()) })
            children?.forEach { walk(it, depth + 1) }
        }
    }
    
    walk(root, 0)
    return nodes
}

@Composable
private fun ToolsScreen(
    toolDraft: ToolDraft,
    toolFavorites: Set<String>,
    toolOutput: SnapshotStateList<String>,
    commandHistory: List<ToolCommandHistoryEntry>,
    toolRunInFlight: Boolean,
    onToolDraftChange: (ToolDraft) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onRunTool: () -> Unit,
    onClearToolOutput: () -> Unit,
    onExportToolOutput: () -> Unit,
    onExportScript: () -> Unit,
    onAddHistory: (ToolCommandHistoryEntry) -> Unit,
) {
    val selectedTool = availableTools.firstOrNull { it.name == toolDraft.toolName } ?: availableTools.first()
    val argumentCount = max(placeholderCount(selectedTool.template), 1)
    val normalizedArguments = remember(toolDraft.toolName, toolDraft.arguments) {
        ensureArgumentCount(selectedTool, toolDraft.arguments)
    }
    val preview = remember(normalizedArguments, selectedTool.template) {
        ToolLibrary.previewCommand(selectedTool, normalizedArguments)
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Column {
                Text("Tools", style = MaterialTheme.typography.headlineSmall)
                Text("Build commands, execute them, and export standalone scripts.")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionButton(
                    label = "Export",
                    icon = Icons.Default.Share,
                    tooltip = "Export the current output or command as a file",
                    onClick = onExportToolOutput,
                )
                ActionButton(
                    label = "Script",
                    icon = Icons.Default.Code,
                    tooltip = "Generate a standalone shell script",
                    onClick = onExportScript,
                )
            }
        }

        ScrollableTabRow(
            selectedTabIndex = availableTools.indexOf(selectedTool).coerceAtLeast(0),
        ) {
            availableTools.forEach { tool ->
                Tab(
                    selected = selectedTool == tool,
                    onClick = { onToolDraftChange(ToolDraft(tool.name, ensureArgumentCount(tool, emptyList()))) },
                    text = { Text(tool.name) },
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedCard(Modifier.weight(1f)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Preset", style = MaterialTheme.typography.titleMedium)
                    Text(selectedTool.description)
                    Text(selectedTool.usage, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = { onToggleFavorite(selectedTool.name) }) {
                            Icon(
                                if (toolFavorites.contains(selectedTool.name)) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Favorite tool",
                            )
                        }
                        TextButton(onClick = onRunTool, enabled = !toolRunInFlight) {
                            Text(if (toolRunInFlight) "Running..." else "Run ${selectedTool.name}")
                        }
                    }
                }
            }
            OutlinedCard(Modifier.weight(1f)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Dry-run preview", style = MaterialTheme.typography.titleMedium)
                    SelectionContainer {
                        Text(preview, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    }
                }
            }
        }

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(argumentCount) { index ->
                OutlinedTextField(
                    value = normalizedArguments.getOrNull(index).orEmpty(),
                    onValueChange = { value ->
                        val next = normalizedArguments.toMutableList()
                        while (next.size <= index) next += ""
                        next[index] = value
                        onToolDraftChange(ToolDraft(selectedTool.name, next))
                    },
                    label = { Text("Arg ${index + 1}") },
                    modifier = Modifier.width(240.dp),
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onClearToolOutput) { Text("Clear output") }
            TextButton(onClick = onExportToolOutput) { Text("Export output") }
            TextButton(onClick = onExportScript) { Text("Export script") }
        }

        Column(Modifier.fillMaxWidth().weight(1f)) {
            Text("History", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                commandHistory.take(6).forEach { history ->
                    AssistChip(
                        onClick = {
                            val tool = availableTools.firstOrNull { it.name == history.toolName } ?: return@AssistChip
                            onToolDraftChange(ToolDraft(tool.name, history.arguments))
                        },
                        label = { Text(history.preview.take(36)) },
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedCard(Modifier.fillMaxSize()) {
                LazyColumn(Modifier.fillMaxSize().padding(12.dp)) {
                    items(toolOutput) { line ->
                        Text(line, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun LogsScreen(
    logManager: LogManager,
    onExportLogs: () -> Unit,
) {
    val logs by logManager.getAllLogs().collectAsState(initial = emptyList())
    val dateFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    var filter by rememberSaveable { mutableStateOf<LogType?>(null) }

    val filtered = remember(logs, filter) {
        if (filter == null) logs else logs.filter { it.type == filter }
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Column {
                Text("Logs", style = MaterialTheme.typography.headlineSmall)
                Text("Severity-coded output with export support.")
            }
            TextButton(onClick = onExportLogs) { Text("Export logs") }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(null, LogType.INFO, LogType.WARNING, LogType.ERROR, LogType.TOOL_OUTPUT, LogType.TERMINAL_COMMAND).forEach { type ->
                FilterChip(
                    selected = filter == type,
                    onClick = { filter = type },
                    label = { Text(type?.name ?: "All") },
                )
            }
        }
        OutlinedCard(Modifier.fillMaxSize()) {
            LazyColumn(Modifier.fillMaxSize().padding(12.dp)) {
                items(filtered) { log ->
                    val color = when (log.type) {
                        LogType.ERROR -> MaterialTheme.colorScheme.error
                        LogType.WARNING -> Color(0xFFF4B400)
                        LogType.INFO -> MaterialTheme.colorScheme.onSurface
                        LogType.TOOL_OUTPUT -> MaterialTheme.colorScheme.secondary
                        LogType.TERMINAL_COMMAND -> MaterialTheme.colorScheme.primary
                    }
                    Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Box(Modifier.size(10.dp).background(color, CircleShape))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                "[${dateFormatter.format(log.timestamp)}] ${log.type.name}",
                                style = MaterialTheme.typography.labelSmall,
                                color = color,
                            )
                            Text(log.message, color = color)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TerminalScreen(
    logManager: LogManager,
    terminalSession: TerminalSession,
) {
    val outputLines = remember { mutableStateListOf<String>() }
    var input by rememberSaveable { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        terminalSession.outputFlow.collect { chunk ->
            chunk.lineSequence().filter { it.isNotBlank() }.forEach { line ->
                outputLines += line
                logManager.log(line, LogType.TOOL_OUTPUT)
            }
            if (outputLines.isNotEmpty()) {
                listState.animateScrollToItem(outputLines.lastIndex)
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Terminal", style = MaterialTheme.typography.headlineSmall)
        OutlinedCard(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF0B1020)),
        ) {
            LazyColumn(Modifier.fillMaxSize().padding(12.dp), state = listState) {
                items(outputLines) { line ->
                    Text(renderTerminalLine(line), color = Color(0xFFCEFFB3), fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                label = { Text("Root command") },
                singleLine = true,
            )
            TextButton(onClick = {
                if (input.isNotBlank()) {
                    terminalSession.sendCommand(input)
                    logManager.log(input, LogType.TERMINAL_COMMAND)
                    input = ""
                }
            }) {
                Text("Send")
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    themeConfig: ThemeConfig,
    onThemeChange: (ThemeConfig) -> Unit,
    onThemePresetSelected: (ThemePreset) -> Unit,
    onResetOnboarding: () -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Dark theme")
            Switch(
                checked = themeConfig.darkMode,
                onCheckedChange = { onThemeChange(themeConfig.copy(darkMode = it)) },
            )
            Text("High contrast")
            Switch(
                checked = themeConfig.highContrast,
                onCheckedChange = { onThemeChange(themeConfig.copy(highContrast = it)) },
            )
        }
        Text("Theme presets", style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            themePresets.forEach { preset ->
                AssistChip(
                    onClick = { onThemePresetSelected(preset) },
                    label = { Text(preset.label) },
                )
            }
        }
        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Current colors")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ColorSwatch(Color(themeConfig.primaryArgb), "Primary")
                    ColorSwatch(Color(themeConfig.secondaryArgb), "Secondary")
                    ColorSwatch(Color(themeConfig.tertiaryArgb), "Tertiary")
                }
            }
        }
        TextButton(onClick = onResetOnboarding) {
            Text("Show walkthrough again")
        }
        Text("Window width: ${LocalConfiguration.current.screenWidthDp} dp", style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun ColorSwatch(color: Color, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(48.dp).background(color, RoundedCornerShape(14.dp)))
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun PartitionViewerScreen(
    firmwareBridge: NativeFirmwareBridge,
    inspection: FirmwareInspectionState?,
) {
    val metadata = remember(inspection?.path, inspection?.metadata) {
        if (inspection?.path == null) emptyMap() else runCatching { firmwareBridge.parseSuperImage(inspection.path) }.getOrDefault(emptyMap())
    }
    val slices = remember(metadata) { buildPartitionSlices(metadata) }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Partition Viewer", style = MaterialTheme.typography.headlineSmall)
        Text("Interactive donut chart and partition metadata.")
        if (inspection == null) {
            Text("Inspect a super image first to populate this view.")
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedCard(Modifier.weight(1f)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(inspection.displayName, style = MaterialTheme.typography.titleMedium)
                        Text("Format: ${inspection.format ?: "unknown"}")
                        Text("Engine: ${inspection.engine ?: "native-engine"}")
                    }
                }
                OutlinedCard(Modifier.weight(1f)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Geometry", style = MaterialTheme.typography.titleMedium)
                        metadata["geometry_magic"]?.let { Text(it) }
                        metadata["block_size"]?.let { Text("Block size: $it") }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                PartitionDonutChart(slices, Modifier.weight(1f).height(260.dp))
                OutlinedCard(Modifier.weight(1f)) {
                    LazyColumn(Modifier.fillMaxSize().padding(12.dp)) {
                        items(slices) { slice ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(slice.name)
                                Text(formatBytes(slice.sizeBytes))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PartitionDonutChart(
    slices: List<PartitionSlice>,
    modifier: Modifier = Modifier,
) {
    val colors = listOf(Color(0xFF3DDC97), Color(0xFF8AB4F8), Color(0xFFFFB74D), Color(0xFFEF9A9A), Color(0xFFCE93D8))
    val total = slices.sumOf { max(it.sizeBytes, 0L) }.coerceAtLeast(1L)
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val diameter = min(size.width, size.height)
            val stroke = Stroke(width = diameter * 0.18f, cap = StrokeCap.Butt)
            var start = -90f
            slices.forEachIndexed { index, slice ->
                val sweep = (slice.sizeBytes.toFloat() / total.toFloat()) * 360f
                drawArc(
                    color = colors[index % colors.size],
                    startAngle = start,
                    sweepAngle = sweep.coerceAtLeast(2f),
                    useCenter = false,
                    style = stroke,
                )
                start += sweep
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("super.img", style = MaterialTheme.typography.titleMedium)
            Text("${slices.size} partitions", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun CommandPaletteDialog(
    currentScreen: LabScreen,
    toolDraft: ToolDraft,
    recentFirmware: List<RecentFirmwareEntry>,
    commandHistory: List<ToolCommandHistoryEntry>,
    onDismiss: () -> Unit,
    onNavigate: (LabScreen) -> Unit,
    onSelectTool: (String) -> Unit,
    onSelectRecentFirmware: (RecentFirmwareEntry) -> Unit,
    onSelectHistory: (ToolCommandHistoryEntry) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val lower = query.lowercase(Locale.getDefault())
    val screenMatches = LabScreen.entries.filter { it.name.lowercase(Locale.getDefault()).contains(lower) }
    val toolMatches = availableTools.filter { it.name.contains(lower, true) || it.description.contains(lower, true) }
    val recentMatches = recentFirmware.filter { it.displayName.contains(lower, true) }
    val historyMatches = commandHistory.filter { it.preview.contains(lower, true) || it.toolName.contains(lower, true) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        title = { Text("Command palette") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Search screens, tools, files, and commands") },
                    singleLine = true,
                )
                LazyColumn(Modifier.height(360.dp)) {
                    if (screenMatches.isNotEmpty()) {
                        item { Text("Screens", style = MaterialTheme.typography.titleSmall) }
                        items(screenMatches) { screen ->
                            DropdownMenuItem(
                                text = { Text(screen.name) },
                                onClick = {
                                    onNavigate(screen)
                                    onDismiss()
                                },
                            )
                        }
                    }
                    if (toolMatches.isNotEmpty()) {
                        item { Text("Tools", style = MaterialTheme.typography.titleSmall) }
                        items(toolMatches) { tool ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(tool.name)
                                        Text(tool.description, style = MaterialTheme.typography.labelSmall)
                                    }
                                },
                                onClick = {
                                    onSelectTool(tool.name)
                                    onDismiss()
                                },
                            )
                        }
                    }
                    if (recentMatches.isNotEmpty()) {
                        item { Text("Recent files", style = MaterialTheme.typography.titleSmall) }
                        items(recentMatches) { entry ->
                            DropdownMenuItem(
                                text = { Text(entry.displayName) },
                                onClick = {
                                    onSelectRecentFirmware(entry)
                                    onDismiss()
                                },
                            )
                        }
                    }
                    if (historyMatches.isNotEmpty()) {
                        item { Text("Command history", style = MaterialTheme.typography.titleSmall) }
                        items(historyMatches) { entry ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(entry.toolName)
                                        Text(entry.preview, style = MaterialTheme.typography.labelSmall)
                                    }
                                },
                                onClick = {
                                    onSelectHistory(entry)
                                    onDismiss()
                                },
                            )
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun OnboardingDialog(onDismiss: () -> Unit) {
    var step by rememberSaveable { mutableStateOf(0) }
    val cards = listOf(
        "Pick a firmware image from Dashboard or drop one from a file manager.",
        "Use Tools to preview commands, star favorites, and export scripts.",
        "Review logs, terminal output, and partition maps before exporting a snapshot.",
    )
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                if (step >= cards.lastIndex) onDismiss() else step += 1
            }) {
                Text(if (step >= cards.lastIndex) "Done" else "Next")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Skip") }
        },
        title = { Text("Welcome to RootedFirmwareLab") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Step ${step + 1} of ${cards.size}", style = MaterialTheme.typography.labelMedium)
                Text(cards[step])
            }
        },
    )
}

internal fun parseMetadata(raw: String?): Map<String, String> {
    if (raw.isNullOrBlank()) return emptyMap()
    val json = runCatching { JSONObject(raw) }.getOrNull() ?: return emptyMap()
    return json.keys().asSequence().associateWith { key -> json.optString(key) }
}

internal fun upsertRecent(
    store: UiStateStore,
    current: List<RecentFirmwareEntry>,
    entry: RecentFirmwareEntry,
): List<RecentFirmwareEntry> {
    val next = current.filterNot { it.uri == entry.uri }.toMutableList()
    next.add(0, entry)
    store.addRecentFirmware(entry)
    return next.take(6)
}

internal fun toggleFavoriteTool(
    store: UiStateStore,
    favorites: Set<String>,
    toolName: String,
): Set<String> {
    val next = favorites.toMutableSet()
    if (!next.add(toolName)) next.remove(toolName)
    store.saveFavoriteTools(next)
    return next
}

internal fun upsertCommandHistory(
    store: UiStateStore,
    current: List<ToolCommandHistoryEntry>,
    entry: ToolCommandHistoryEntry,
): List<ToolCommandHistoryEntry> {
    val next = current.filterNot { it.preview == entry.preview && it.toolName == entry.toolName }.toMutableList()
    next.add(0, entry)
    store.addCommandHistory(entry)
    return next.take(8)
}

internal fun ensureArgumentCount(tool: com.redrum.rootedfirmwarelab.core.service.ToolLibrary.ToolDefinition, args: List<String>): List<String> {
    val needed = max(placeholderCount(tool.template), 1)
    val next = args.toMutableList()
    while (next.size < needed) next += ""
    return next.take(needed)
}

private fun scopeAction(action: String, path: String, firmwareBridge: NativeFirmwareBridge, logManager: LogManager) {
    val result = runCatching {
        when (action) {
            "Decompile" -> firmwareBridge.decompile(path)
            "Disassemble" -> firmwareBridge.disassemble(path)
            "Recompile" -> firmwareBridge.recompile(path)
            "Assemble" -> firmwareBridge.assemble(path)
            else -> false
        }
    }.getOrDefault(false)
    logManager.log("$action ${if (result) "succeeded" else "failed"} for $path", if (result) LogType.INFO else LogType.ERROR)
}

internal fun buildLogsExport(logs: List<LogEntry>): String {
    if (logs.isEmpty()) {
        return "No logs captured yet."
    }
    return buildString {
        appendLine("RootedFirmwareLab log export")
        appendLine()
        logs.forEach { log ->
            appendLine("[${log.timestamp.time}] ${log.type.name}: ${log.message}")
        }
    }
}

internal fun buildShellScript(tool: com.redrum.rootedfirmwarelab.core.service.ToolLibrary.ToolDefinition, args: List<String>): String {
    val preview = ToolLibrary.previewCommand(tool, ensureArgumentCount(tool, args))
    return buildString {
        appendLine("#!/bin/sh")
        appendLine("set -eu")
        appendLine()
        appendLine("# Generated by RootedFirmwareLab")
        appendLine("su -c ${shellQuote(preview)}")
    }
}

private fun shellQuote(value: String): String = "'" + value.replace("'", "'\\''") + "'"

private fun writeText(context: Context, uri: Uri, text: String) {
    context.contentResolver.openOutputStream(uri)?.use { stream ->
        stream.write(text.toByteArray())
        stream.flush()
    }
}

private fun writeSnapshotZip(
    context: Context,
    uri: Uri,
    inspection: FirmwareInspectionState,
    logs: List<LogEntry>,
) {
    context.contentResolver.openOutputStream(uri)?.use { output ->
        ZipOutputStream(output).use { zip ->
            zip.putNextEntry(ZipEntry("manifest.txt"))
            zip.write(
                buildString {
                    appendLine("RootedFirmwareLab snapshot")
                    appendLine("Display: ${inspection.displayName}")
                    appendLine("Path: ${inspection.path ?: inspection.uriString}")
                    appendLine("Format: ${inspection.format ?: "unknown"}")
                    appendLine("Summary: ${inspection.summary ?: "n/a"}")
                }.toByteArray(),
            )
            zip.closeEntry()

            zip.putNextEntry(ZipEntry("logs.txt"))
            zip.write(buildLogsExport(logs).toByteArray())
            zip.closeEntry()

            val source = inspection.path?.let { File(it) }
            if (source?.exists() == true) {
                if (source.isDirectory) {
                    source.walkTopDown().forEach { file ->
                        if (file.isDirectory) return@forEach
                        val relative = file.relativeTo(source.parentFile ?: source).path
                        zip.putNextEntry(ZipEntry("files/$relative"))
                        FileInputStream(file).use { input -> input.copyTo(zip) }
                        zip.closeEntry()
                    }
                } else {
                    zip.putNextEntry(ZipEntry("files/${source.name}"))
                    FileInputStream(source).use { input -> input.copyTo(zip) }
                    zip.closeEntry()
                }
            }
        }
    }
}

private fun displayNameFromUri(uri: Uri, context: Context): String {
    val projection = arrayOf(android.provider.OpenableColumns.DISPLAY_NAME)
    context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (index >= 0) {
                return cursor.getString(index) ?: uri.lastPathSegment.orEmpty()
            }
        }
    }
    return uri.lastPathSegment?.substringAfterLast('/')?.ifBlank { "selected-firmware" } ?: "selected-firmware"
}

internal fun buildPartitionSlices(metadata: Map<String, String>): List<PartitionSlice> {
    val names = mutableMapOf<Int, String>()
    val sizes = mutableMapOf<Int, Long>()
    metadata.forEach { (key, value) ->
        val match = Regex("partition_(\\d+)_(name|size)").matchEntire(key) ?: return@forEach
        val index = match.groupValues[1].toInt()
        when (match.groupValues[2]) {
            "name" -> names[index] = value
            "size" -> sizes[index] = value.toLongOrNull() ?: 0L
        }
    }
    val slices = sizes.keys.sorted().map { index ->
        PartitionSlice(names[index] ?: "partition_$index", sizes[index] ?: 0L)
    }
    return if (slices.isEmpty()) {
        listOf(PartitionSlice("system", 1L), PartitionSlice("vendor", 1L), PartitionSlice("product", 1L))
    } else {
        slices
    }
}

internal fun formatBytes(bytes: Long): String {
    val units = listOf("B", "KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var unitIndex = 0
    while (value >= 1024 && unitIndex < units.lastIndex) {
        value /= 1024
        unitIndex += 1
    }
    return String.format(Locale.getDefault(), "%.1f %s", value, units[unitIndex])
}

internal fun renderTerminalLine(line: String): AnnotatedString {
    return buildAnnotatedString {
        if (line.contains("$")) {
            withStyle(SpanStyle(color = Color(0xFFFFD54F))) {
                append(line)
            }
        } else if (line.contains("error", true)) {
            withStyle(SpanStyle(color = Color(0xFFFF8A80))) {
                append(line)
            }
        } else if (line.startsWith("su") || line.startsWith("mount") || line.startsWith("dd")) {
            withStyle(SpanStyle(color = Color(0xFF90CAF9))) {
                append(line)
            }
        } else {
            append(line)
        }
    }
}
