package com.serhanensar.agentjee.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text

@Composable
fun DashRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.width(150.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color(0xFF9CA3AF), fontSize = 12.sp)
        Text(value, color = valueColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SettingRow(label: String, value: String) {
    Row(
        modifier = Modifier.width(160.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color(0xFF9CA3AF), fontSize = 12.sp)
        Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CustomDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(Color(0xFF1F2937))
    )
}

fun getTempColor(pct: Double) = when {
    pct > 85 -> Color(0xFFEF4444)
    pct > 65 -> Color(0xFFF59E0B)
    else -> Color(0xFF22C55E)
}
