package com.serhanensar.agentjee.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.scrollBy
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
import com.serhanensar.agentjee.data.model.DockerContainer
import com.serhanensar.agentjee.data.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

@Composable
fun DockerScreen(onBack: () -> Unit) {
    var containers by remember { mutableStateOf<List<DockerContainer>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf(false) }
    
    val listState = rememberScalingLazyListState()
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

    Scaffold(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
    ) {
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (error) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Bağlantı hatası", color = Color.Red, fontSize = 12.sp)
            }
        } else {
            ScalingLazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .onRotaryScrollEvent { event ->
                        coroutineScope.launch { listState.scrollBy(event.verticalScrollPixels) }
                        true
                    }
                    .focusRequester(focusRequester)
                    .focusable(),
                contentPadding = PaddingValues(top = 40.dp, bottom = 40.dp),
                autoCentering = AutoCenteringParams(itemIndex = 1)
            ) {
                item {
                    Text("Docker", color = Color(0xFF22C55E),
                        fontSize = 13.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth(),
                        textAlign = TextAlign.Center)
                }
                items(containers) { container ->
                    val isRunning = container.state == "running"
                    
                    // AppCard yerine Chip kullanıyoruz, bu ekranlar için daha optimize.
                    // İleride detaylı bilgi eklemek istersek AppCard'a geçebiliriz.
                    Chip(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        onClick = { /* Detay ekranı eklenebilir */ },
                        colors = ChipDefaults.chipColors(
                            backgroundColor = Color(0xFF1C1F3A),
                            contentColor = Color.White
                        ),
                        label = {
                            Text(
                                container.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        secondaryLabel = {
                            Text(
                                container.status,
                                fontSize = 10.sp,
                                color = if (isRunning) Color(0xFF22C55E) else Color(0xFF9CA3AF)
                            )
                        },
                        icon = {
                            Box(
                                Modifier
                                    .size(10.dp)
                                    .background(
                                        if (isRunning) Color.Green else Color.Red,
                                        shape = androidx.compose.foundation.shape.CircleShape
                                    )
                            )
                        }
                    )
                }
            }
        }
    }
}
