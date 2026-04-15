package com.serhanensar.agentjee.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.serhanensar.agentjee.data.model.DockerContainer
import com.serhanensar.agentjee.data.network.ApiClient
import com.serhanensar.agentjee.ui.components.CustomDivider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

@Composable
fun DockerScreen(onBack: () -> Unit) {
    var containers by remember { mutableStateOf<List<DockerContainer>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()

    androidx.activity.compose.BackHandler(onBack = onBack)

    LaunchedEffect(Unit) {
        isLoading = true
        val resp = withContext(Dispatchers.IO) { ApiClient.get("/api/docker/containers") }
        if (resp != null) {
            try {
                val arr = JSONArray(resp)
                containers = (0 until arr.length()).map {
                    val o = arr.getJSONObject(it)
                    DockerContainer(
                        name = o.getString("name"),
                        image = o.getString("image"),
                        state = o.getString("state"),
                        status = o.getString("status")
                    )
                }
                error = false
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
                contentPadding = PaddingValues(vertical = 24.dp, horizontal = 8.dp)
            ) {
                item {
                    Text("Docker", color = Color(0xFF22C55E),
                        fontSize = 14.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth(),
                        textAlign = TextAlign.Center)
                }
                items(containers) { container ->
                    val isRunning = container.state == "running"
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(8.dp)
                                    .background(if (isRunning) Color.Green else Color.Red,
                                        shape = androidx.compose.foundation.shape.CircleShape)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                container.name,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            container.image,
                            color = Color.Gray,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            container.status,
                            color = if (isRunning) Color(0xFF22C55E) else Color(0xFF9CA3AF),
                            fontSize = 10.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        CustomDivider()
                    }
                }
            }
        }
    }
}
