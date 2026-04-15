package com.serhanensar.agentjee.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import com.serhanensar.agentjee.data.model.StatusData
import com.serhanensar.agentjee.data.network.ApiClient
import com.serhanensar.agentjee.ui.components.DashRow
import com.serhanensar.agentjee.ui.components.getTempColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject

@Composable
fun DashboardScreen(onBack: () -> Unit) {
    var status by remember { mutableStateOf<StatusData?>(null) }
    var error by remember { mutableStateOf(false) }

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

    Box(
        Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (error) {
            Text("Bağlantı yok", color = Color.Red, fontSize = 13.sp)
        } else if (status == null) {
            Text("Yükleniyor...", color = Color.Gray, fontSize = 13.sp)
        } else {
            val s = status!!
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Dashboard", color = Color(0xFF22C55E),
                    fontSize = 13.sp, fontWeight = FontWeight.Bold)
                DashRow("CPU",  "%.1f%%".format(s.cpu),  getTempColor(s.cpu))
                DashRow("RAM",  "%.1f%%".format(s.ram),  getTempColor(s.ram))
                DashRow("Disk", "%.1f%%".format(s.disk), getTempColor(s.disk))
                DashRow("Temp",
                    if (s.temp != null) "%.1f°C".format(s.temp) else "—",
                    if (s.temp != null && s.temp > 70) Color(0xFFEF4444)
                    else if (s.temp != null && s.temp > 55) Color(0xFFF59E0B)
                    else Color(0xFF22C55E)
                )
            }
        }
    }
}
