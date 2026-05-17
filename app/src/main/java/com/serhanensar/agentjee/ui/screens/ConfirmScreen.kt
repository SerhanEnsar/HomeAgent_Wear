// Copyright (c) 2026 Serhan Ensar. All rights reserved.
package com.serhanensar.agentjee.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text

@Composable
fun ConfirmScreen(
    title: String,
    message: String,
    confirmColor: Color,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    androidx.activity.compose.BackHandler(onBack = onCancel)

    Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(title, color = confirmColor,
                fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(message, color = Color.White,
                fontSize = 12.sp, textAlign = TextAlign.Center)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1F2937), RoundedCornerShape(10.dp))
                        .clickable(onClick = onCancel)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text("İptal", color = Color.White, fontSize = 12.sp)
                }
                Box(
                    modifier = Modifier
                        .background(confirmColor.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                        .clickable(onClick = onConfirm)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text("Evet", color = confirmColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
