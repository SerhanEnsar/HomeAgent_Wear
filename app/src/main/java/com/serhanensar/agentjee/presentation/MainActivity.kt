// Copyright (c) 2026 Serhan Ensar. All rights reserved.
package com.serhanensar.agentjee.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.serhanensar.agentjee.data.model.Page
import com.serhanensar.agentjee.data.network.ApiClient
import com.serhanensar.agentjee.ui.screens.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AgentJeeApp() }
    }
}

@Composable
fun AgentJeeApp() {
    var page by remember { mutableStateOf(Page.MENU) }
    var currentMount by remember { mutableStateOf("") }
    var currentPath by remember { mutableStateOf("") }

    when (page) {
        Page.MENU -> MenuScreen(
            onNavigate = { page = it },
            onFiles = { page = Page.FILES; currentMount = ""; currentPath = "" }
        )
        Page.DASHBOARD -> DashboardScreen(onBack = { page = Page.MENU })
        Page.FILES -> FilesScreen(
            currentMount = currentMount,
            currentPath = currentPath,
            onMountChange = { currentMount = it },
            onPathChange = { currentPath = it },
            onBack = { page = Page.MENU }
        )
        Page.DOCKER -> DockerScreen(onBack = { page = Page.MENU })
        Page.SETTINGS -> SettingsScreen(onBack = { page = Page.MENU })
        Page.CONFIRM_REBOOT -> ConfirmScreen(
            title = "Reboot",
            message = "Sistemi yeniden başlat?",
            confirmColor = Color(0xFFF59E0B),
            onConfirm = {
                page = Page.MENU
                CoroutineScope(Dispatchers.IO).launch {
                    ApiClient.post("/api/system/reboot", "{}")
                }
            },
            onCancel = { page = Page.MENU }
        )
        Page.CONFIRM_SHUTDOWN -> ConfirmScreen(
            title = "Shutdown",
            message = "Sistemi kapat?",
            confirmColor = Color(0xFFEF4444),
            onConfirm = {
                page = Page.MENU
                CoroutineScope(Dispatchers.IO).launch {
                    ApiClient.post("/api/system/shutdown", "{}")
                }
            },
            onCancel = { page = Page.MENU }
        )
    }
}
