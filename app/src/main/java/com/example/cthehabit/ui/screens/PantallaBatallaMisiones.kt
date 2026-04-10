package com.example.cthehabit.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PantallaBatallaMisiones(
    horas: Int,
    playerIndex: Int,
    enemyIndex: Int,
    onBack: () -> Unit,
    onOpenCharacterSelect: () -> Unit,
    onOpenTrophies: () -> Unit,
    onOpenMissions: () -> Unit
) {
    BackHandler { onBack() }

    Scaffold(containerColor = Color.DarkGray) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.DarkGray)
        ) {
            BattleSection(
                horas = horas,
                playerIndex = playerIndex,
                enemyIndex = enemyIndex,
                onOpenCharacterSelect = onOpenCharacterSelect,
                onOpenTrophies = onOpenTrophies
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onOpenMissions,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E3A5F)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Ver misiones",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

            }
        }
    }
}