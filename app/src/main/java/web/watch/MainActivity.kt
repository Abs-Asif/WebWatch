package web.watch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import web.watch.data.WatchItem
import web.watch.ui.screens.*
import web.watch.ui.theme.WebWatchTheme
import web.watch.viewmodel.MainViewModel

enum class Screen {
    HOME,
    DETAIL,
    DIFF,
    SETTINGS,
    ABOUT,
    CREDITS
}

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val packageInfo = try {
            packageManager.getPackageInfo(packageName, 0)
        } catch (e: Exception) {
            null
        }
        val versionName = packageInfo?.versionName ?: "2026.08.31.12.59"

        setContent {
            var isDarkMode by remember { mutableStateOf(false) }
            var isAmoledMode by remember { mutableStateOf(false) }
            val systemInDark = isSystemInDarkTheme()
            val useDark = isDarkMode || systemInDark

            var showStartupPermission by remember { mutableStateOf(true) }

            WebWatchTheme(
                darkTheme = useDark,
                amoledMode = isAmoledMode
            ) {
                val watchItems by viewModel.watchItems.collectAsState()
                val selectedWatchItem by viewModel.selectedWatchItem.collectAsState()
                val historyRecords by viewModel.historyRecords.collectAsState()
                val selectedHistoryRecord by viewModel.selectedHistoryRecord.collectAsState()

                var currentScreen by remember { mutableStateOf(Screen.HOME) }
                var showAddSheet by remember { mutableStateOf(false) }
                var editingWatchItem by remember { mutableStateOf<WatchItem?>(null) }

                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                if (showStartupPermission) {
                    StartupPermissionDialog(onDismiss = { showStartupPermission = false })
                }

                if (showAddSheet) {
                    WatchItemFormSheet(
                        existingItem = editingWatchItem,
                        onDismiss = {
                            showAddSheet = false
                            editingWatchItem = null
                        },
                        onSave = { title, url, freq, highPriority ->
                            if (editingWatchItem != null) {
                                val updated = editingWatchItem!!.copy(
                                    title = title,
                                    url = url,
                                    checkFrequencyMinutes = freq,
                                    notificationPriority = highPriority
                                )
                                viewModel.updateWatchItem(updated)
                            } else {
                                viewModel.addWatchItem(title, url, freq, highPriority)
                            }
                            showAddSheet = false
                            editingWatchItem = null
                        }
                    )
                }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    gesturesEnabled = currentScreen == Screen.HOME,
                    drawerContent = {
                        ModalDrawerSheet {
                            Text("WebWatch Menu", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
                            Divider()
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                label = { Text("Settings") },
                                selected = currentScreen == Screen.SETTINGS,
                                onClick = {
                                    currentScreen = Screen.SETTINGS
                                    scope.launch { drawerState.close() }
                                }
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Info, contentDescription = "About") },
                                label = { Text("About") },
                                selected = currentScreen == Screen.ABOUT,
                                onClick = {
                                    currentScreen = Screen.ABOUT
                                    scope.launch { drawerState.close() }
                                }
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.List, contentDescription = "Credits") },
                                label = { Text("Credits") },
                                selected = currentScreen == Screen.CREDITS,
                                onClick = {
                                    currentScreen = Screen.CREDITS
                                    scope.launch { drawerState.close() }
                                }
                            )
                        }
                    }
                ) {
                    when (currentScreen) {
                        Screen.HOME -> {
                            HomeScreen(
                                watchItems = watchItems,
                                onOpenDrawer = { scope.launch { drawerState.open() } },
                                onItemClick = { item ->
                                    viewModel.selectWatchItem(item)
                                    currentScreen = Screen.DETAIL
                                },
                                onAddClick = {
                                    editingWatchItem = null
                                    showAddSheet = true
                                }
                            )
                        }
                        Screen.DETAIL -> {
                            selectedWatchItem?.let { item ->
                                WatchDetailScreen(
                                    watchItem = item,
                                    historyList = historyRecords,
                                    onBack = {
                                        viewModel.selectWatchItem(null)
                                        currentScreen = Screen.HOME
                                    },
                                    onEdit = {
                                        editingWatchItem = item
                                        showAddSheet = true
                                    },
                                    onShowDiff = { record ->
                                        viewModel.selectHistoryRecord(record)
                                        currentScreen = Screen.DIFF
                                    }
                                )
                            } ?: run {
                                currentScreen = Screen.HOME
                            }
                        }
                        Screen.DIFF -> {
                            val record = selectedHistoryRecord
                            val item = selectedWatchItem
                            if (record != null && item != null) {
                                DiffViewerScreen(
                                    record = record,
                                    url = item.url,
                                    onBack = {
                                        viewModel.selectHistoryRecord(null)
                                        currentScreen = Screen.DETAIL
                                    }
                                )
                            } else {
                                currentScreen = Screen.HOME
                            }
                        }
                        Screen.SETTINGS -> {
                            SettingsScreen(
                                isDarkMode = isDarkMode,
                                onDarkModeChange = { isDarkMode = it },
                                isAmoledMode = isAmoledMode,
                                onAmoledModeChange = { isAmoledMode = it },
                                onBack = { currentScreen = Screen.HOME }
                            )
                        }
                        Screen.ABOUT -> {
                            AboutScreen(
                                versionName = versionName,
                                onBack = { currentScreen = Screen.HOME }
                            )
                        }
                        Screen.CREDITS -> {
                            CreditsScreen(
                                onBack = { currentScreen = Screen.HOME }
                            )
                        }
                    }
                }
            }
        }
    }
}
