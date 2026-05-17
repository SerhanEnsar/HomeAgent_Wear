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
import com.serhanensar.agentjee.data.model.StatusData
import com.serhanensar.agentjee.data.network.ApiClient
import com.serhanensar.agentjee.ui.components.getTempColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

@Composable
fun DashboardScreen(onBack: () -> Unit) {
    var status by remember { mutableStateOf<StatusData?>(null) }
    var error by remember { mutableStateOf(false) }
    
    val listState = rememberScalingLazyListState()
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()

    androidx.activity.compose.BackHandler(onBack = onBack)

    LaunchedEffect(Unit) {
        while (true) {
            val resp = withContext(Dispatchers.IO) { ApiClient.get("/api/status") }
            if (resp != null) {
                try {
                    val j = JSONObject(resp)
                    status = StatusData(
                        cpu = j.getDouble("cpu_percent"),
                        ram = j.getDouble("ram_percent"),
                        disk = j.getDouble("disk_percent"),
                        temp = if (j.isNull("cpu_temp")) null else j.getDouble("cpu_temp")
                    )
                    error = false
                } catch (e: Exception) { error = true }
            } else error = true
            delay(3000)
        }
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Scaffold(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) }
    ) {
        if (error) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Bağlantı yok", color = Color.Red, fontSize = 13.sp)
            }
        } else if (status == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val s = status!!
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
                    Text("Dashboard", color = Color(0xFF22C55E),
                        style = MaterialTheme.typography.caption1.copy(fontWeight = FontWeight.ExtraBold),
                        modifier = Modifier.padding(bottom = 8.dp))
                }
                
                item {
                    StatusCard("CPU", "${s.cpu}%", s.cpu.toFloat() / 100f, getTempColor(s.cpu))
                }
                item {
                    StatusCard("RAM", "${s.ram}%", s.ram.toFloat() / 100f, getTempColor(s.ram))
                }
                item {
                    StatusCard("Disk", "${s.disk}%", s.disk.toFloat() / 100f, getTempColor(s.disk))
                }
                if (s.temp != null) {
                    item {
                        val tColor = if (s.temp > 70) Color(0xFFEF4444) 
                                    else if (s.temp > 55) Color(0xFFF59E0B) 
                                    else Color(0xFF22C55E)
                        StatusCard("Sıcaklık", "${s.temp}°C", (s.temp.toFloat() / 100f).coerceIn(0f, 1f), tColor)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusCard(label: String, value: String, progress: Float, color: Color) {
    TitleCard(
        modifier = Modifier.fillMaxWidth(0.95f).padding(vertical = 2.dp),
        onClick = {},
        title = { Text(label, fontSize = 12.sp) },
        backgroundPainter = CardDefaults.cardBackgroundPainter(
            startBackgroundColor = Color(0xFF1C1F3A),
            endBackgroundColor = Color(0xFF1C1F3A)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(value, color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Box(Modifier.size(24.dp)) {
                CircularProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxSize(),
                    startAngle = 270f,
                    strokeWidth = 3.dp,
                    indicatorColor = color,
                    trackColor = color.copy(alpha = 0.2f)
                )
            }
        }
    }
}
