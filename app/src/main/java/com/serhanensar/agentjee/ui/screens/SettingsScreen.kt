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
import com.serhanensar.agentjee.data.network.ApiClient
import com.serhanensar.agentjee.ui.components.SettingRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    var ip by remember { mutableStateOf("—") }
    var power by remember { mutableStateOf("—") }
    var fan by remember { mutableStateOf("—") }

    androidx.activity.compose.BackHandler(onBack = onBack)

    LaunchedEffect(Unit) {
        val resp = withContext(Dispatchers.IO) { ApiClient.get("/api/settings/info") }
        if (resp != null) {
            try {
                val j = JSONObject(resp)
                ip = j.optString("ip", "—")
                power = if (j.isNull("power_w")) "—" else "${j.getDouble("power_w")} W"
                fan = if (j.isNull("fan_rpm")) "—" else "${j.getInt("fan_rpm")} RPM"
            } catch (e: Exception) {}
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Ayarlar", color = Color(0xFF22C55E),
                fontSize = 14.sp, fontWeight = FontWeight.Bold)
            SettingRow("IP", ip)
            SettingRow("Güç", power)
            SettingRow("Fan", fan)
        }
    }
}
