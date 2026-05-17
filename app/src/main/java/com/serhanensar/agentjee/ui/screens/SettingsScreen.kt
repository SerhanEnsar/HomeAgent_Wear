// Copyright (c) 2026 Serhan Ensar. All rights reserved.
package com.serhanensar.agentjee.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.*
import com.serhanensar.agentjee.data.network.ApiClient
import com.serhanensar.agentjee.ui.components.SettingRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    var ip by remember { mutableStateOf("—") }
    var power by remember { mutableStateOf("—") }
    var fan by remember { mutableStateOf("—") }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf(false) }

    val listState = rememberScalingLazyListState()
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()

    androidx.activity.compose.BackHandler(onBack = onBack)

    LaunchedEffect(Unit) {
        isLoading = true
        val resp = withContext(Dispatchers.IO) { ApiClient.get("/api/settings/info") }
        if (resp != null) {
            try {
                val j = JSONObject(resp)
                ip = j.optString("ip", "—")
                power = j.optString("power", "—")
                fan = j.optString("fan", "—")
                error = false
            } catch (e: Exception) { error = true }
        } else error = true
        isLoading = false
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Scaffold(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) }
    ) {
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (error) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Hata oluştu", color = Color.Red, fontSize = 12.sp)
            }
        } else {
            ScalingLazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .onRotaryScrollEvent { event ->
                        coroutineScope.launch { listState.animateScrollBy(event.verticalScrollPixels) }
                        true
                    }
                    .focusRequester(focusRequester)
                    .focusable(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(top = 45.dp, bottom = 45.dp),
                autoCentering = AutoCenteringParams(itemIndex = 1)
            ) {
                item {
                    Text("Ayarlar", color = Color(0xFF22C55E),
                        style = MaterialTheme.typography.caption1.copy(fontWeight = FontWeight.ExtraBold),
                        modifier = Modifier.padding(bottom = 12.dp))
                }
                item { SettingCard("IP Adresi", ip, "🌐") }
                item { SettingCard("Güç Tüketimi", power, "⚡") }
                item { SettingCard("Fan Hızı", fan, "🌀") }
                item {
                    Text("v1.0.0", color = Color.Gray, fontSize = 10.sp, 
                        modifier = Modifier.padding(top = 10.dp))
                }
            }
        }
    }
}

@Composable
fun SettingCard(label: String, value: String, icon: String) {
    AppCard(
        modifier = Modifier.fillMaxWidth(0.95f).padding(vertical = 2.dp),
        onClick = {},
        appName = { Text(icon) },
        time = { },
        title = { Text(label, fontSize = 11.sp, color = Color(0xFF9CA3AF)) },
        backgroundPainter = CardDefaults.cardBackgroundPainter(
            startBackgroundColor = Color(0xFF1C1F3A),
            endBackgroundColor = Color(0xFF1C1F3A)
        )
    ) {
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
