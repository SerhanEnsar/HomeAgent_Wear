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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.*
import com.serhanensar.agentjee.data.model.FileItem
import com.serhanensar.agentjee.data.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun FilesScreen(
    currentMount: String,
    currentPath: String,
    onMountChange: (String) -> Unit,
    onPathChange: (String) -> Unit,
    onBack: () -> Unit
) {
    var items by remember { mutableStateOf<List<FileItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf(false) }

    val listState = rememberScalingLazyListState()
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()

    // Geri tuşu hiyerarşisi: Path -> Mount -> Menu
    androidx.activity.compose.BackHandler {
        when {
            currentPath.isNotEmpty() -> {
                val parent = currentPath.substringBeforeLast("/", "")
                onPathChange(parent)
            }
            currentMount.isNotEmpty() -> onMountChange("")
            else -> onBack()
        }
    }

    LaunchedEffect(currentMount, currentPath) {
        isLoading = true
        error = false
        val resp = withContext(Dispatchers.IO) {
            if (currentMount.isEmpty()) {
                ApiClient.get("/api/files/devices")
            } else {
                val enc = java.net.URLEncoder.encode(currentMount, "UTF-8")
                val pathEnc = java.net.URLEncoder.encode(currentPath, "UTF-8")
                ApiClient.get("/api/files/list?mount=$enc&path=$pathEnc")
            }
        }
        if (resp != null) {
            try {
                items = if (currentMount.isEmpty()) {
                    val arr = JSONArray(resp)
                    (0 until arr.length()).map {
                        val o = arr.getJSONObject(it)
                        FileItem(o.getString("mount"), true, 0)
                    }
                } else {
                    val obj = JSONObject(resp)
                    val arr = obj.getJSONArray("items")
                    (0 until arr.length()).map {
                        val o = arr.getJSONObject(it)
                        FileItem(
                            o.getString("name"),
                            o.getString("type") == "dir",
                            if (o.isNull("size")) 0L else o.getLong("size")
                        )
                    }
                }
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
                CircularProgressIndicator(indicatorColor = Color(0xFF22C55E))
            }
        } else if (error) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Pi'ye bağlanılamadı", color = Color.Red, fontSize = 12.sp, textAlign = TextAlign.Center)
            }
        } else {
            ScalingLazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .onRotaryScrollEvent { event ->
                        coroutineScope.launch { 
                            // animateScrollBy ile pürüzsüz kaydırma
                            listState.animateScrollBy(event.verticalScrollPixels) 
                        }
                        true
                    }
                    .focusRequester(focusRequester)
                    .focusable(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(top = 45.dp, bottom = 45.dp),
                autoCentering = AutoCenteringParams(itemIndex = 1)
            ) {
                item {
                    val title = if (currentMount.isEmpty()) "Aygıtlar"
                    else if (currentPath.isEmpty()) currentMount
                    else currentPath.substringAfterLast("/")
                    Text(title, color = Color(0xFF22C55E),
                        style = MaterialTheme.typography.caption1.copy(fontWeight = FontWeight.ExtraBold),
                        modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth(),
                        textAlign = TextAlign.Center)
                }
                items(items) { item ->
                    Chip(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        onClick = {
                            if (item.isDir) {
                                if (currentMount.isEmpty()) onMountChange(item.name)
                                else {
                                    val newPath = if (currentPath.isEmpty()) item.name
                                    else "$currentPath/${item.name}"
                                    onPathChange(newPath)
                                }
                            }
                        },
                        colors = ChipDefaults.chipColors(
                            backgroundColor = Color(0xFF1C1F3A),
                            contentColor = if (item.isDir) Color(0xFF60A5FA) else Color.White
                        ),
                        label = {
                            Text(
                                item.name,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        icon = {
                            Text(if (item.isDir) "📁" else "📄", fontSize = 14.sp)
                        }
                    )
                }
            }
        }
    }
}
