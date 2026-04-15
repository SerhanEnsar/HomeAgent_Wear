package com.serhanensar.agentjee.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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

    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .onRotaryScrollEvent { event ->
                    coroutineScope.launch {
                        listState.scrollBy(event.verticalScrollPixels * 2f)
                    }
                    true
                }
                .focusRequester(focusRequester)
                .focusable(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {
            item {
                Text(
                    "AgentJee",
                    color = Color(0xFF22C55E),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            items(items) { (icon, label, target) ->
                val isRed = target == Page.CONFIRM_SHUTDOWN
                val isAmber = target == Page.CONFIRM_REBOOT
                val cardColor = when {
                    isRed -> Color(0xFF3F0000)
                    isAmber -> Color(0xFF3F2A00)
                    else -> Color(0xFF1C1F3A)
                }
                val textColor = when {
                    isRed -> Color(0xFFEF4444)
                    isAmber -> Color(0xFFF59E0B)
                    else -> Color.White
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(vertical = 4.dp)
                        .background(cardColor, shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                        .clickable {
                            if (target == Page.FILES) onFiles()
                            else onNavigate(target)
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(icon, fontSize = 18.sp)
                        Spacer(Modifier.width(10.dp))
                        Text(label, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
