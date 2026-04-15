package com.serhanensar.agentjee.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import com.serhanensar.agentjee.data.model.FileItem
import com.serhanensar.agentjee.data.network.ApiClient
import com.serhanensar.agentjee.ui.components.CustomDivider
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
    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()

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

    Scaffold(Modifier.fillMaxSize().background(Color.Black)) {
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Yükleniyor...", color = Color.Gray, fontSize = 12.sp)
            }
        } else if (error) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Bağlantı hatası", color = Color.Red, fontSize = 12.sp)
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .onRotaryScrollEvent { event ->
                        coroutineScope.launch { listState.scrollBy(event.verticalScrollPixels * 2f) }
                        true
                    }
                    .focusRequester(focusRequester)
                    .focusable(),
                contentPadding = PaddingValues(vertical = 20.dp, horizontal = 8.dp)
            ) {
                item {
                    val title = if (currentMount.isEmpty()) "Aygıtlar"
                    else if (currentPath.isEmpty()) currentMount
                    else currentPath.substringAfterLast("/")
                    Text(title, color = Color(0xFF22C55E),
                        fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp).fillMaxWidth(),
                        textAlign = TextAlign.Center)
                }
                itemsIndexed(items) { _, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (item.isDir) {
                                    if (currentMount.isEmpty()) onMountChange(item.name)
                                    else {
                                        val newPath = if (currentPath.isEmpty()) item.name
                                        else "$currentPath/${item.name}"
                                        onPathChange(newPath)
                                    }
                                }
                            }
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (item.isDir) "📁" else "📄", fontSize = 14.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            item.name,
                            color = if (item.isDir) Color(0xFF60A5FA) else Color.White,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    CustomDivider()
                }
            }
        }
    }
}
