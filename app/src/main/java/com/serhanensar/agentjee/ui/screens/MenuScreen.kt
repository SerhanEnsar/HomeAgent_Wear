package com.serhanensar.agentjee.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.*
import com.serhanensar.agentjee.data.model.Page
import kotlinx.coroutines.launch

@Composable
fun MenuScreen(onNavigate: (Page) -> Unit, onFiles: () -> Unit) {
    val items = listOf(
        Triple("📊", "Dashboard", Page.DASHBOARD),
        Triple("📁", "Dosyalar", Page.FILES),
        Triple("🐳", "Docker", Page.DOCKER),
        Triple("⚙️", "Ayarlar", Page.SETTINGS),
        Triple("🔄", "Reboot", Page.CONFIRM_REBOOT),
        Triple("⏻", "Shutdown", Page.CONFIRM_SHUTDOWN)
    )

    val listState = rememberScalingLazyListState()
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) }
    ) {
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .onRotaryScrollEvent { event ->
                    coroutineScope.launch {
                        // animateScrollBy kullanarak bezel hareketini yumuşatıyoruz.
                        // Bu yöntem kare atlamalarını (stutter) engeller.
                        listState.animateScrollBy(event.verticalScrollPixels)
                    }
                    true
                }
                .focusRequester(focusRequester)
                .focusable(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(top = 50.dp, bottom = 50.dp),
            autoCentering = AutoCenteringParams(itemIndex = 1)
        ) {
            item {
                Text(
                    "AgentJee",
                    color = Color(0xFF22C55E),
                    style = MaterialTheme.typography.title2.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            items(items) { (icon, label, target) ->
                val isRed = target == Page.CONFIRM_SHUTDOWN
                val isAmber = target == Page.CONFIRM_REBOOT
                
                Chip(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    onClick = {
                        if (target == Page.FILES) onFiles()
                        else onNavigate(target)
                    },
                    colors = ChipDefaults.chipColors(
                        backgroundColor = when {
                            isRed -> Color(0xFF3F0000)
                            isAmber -> Color(0xFF3F2A00)
                            else -> Color(0xFF1C1F3A)
                        },
                        contentColor = when {
                            isRed -> Color(0xFFEF4444)
                            isAmber -> Color(0xFFF59E0B)
                            else -> Color.White
                        }
                    ),
                    label = {
                        Text(label, fontWeight = FontWeight.Bold)
                    },
                    icon = {
                        Text(icon, fontSize = 18.sp)
                    }
                )
            }
        }
    }
    
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
