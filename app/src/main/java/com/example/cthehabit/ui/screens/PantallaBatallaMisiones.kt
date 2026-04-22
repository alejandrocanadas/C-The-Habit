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
import androidx.compose.ui.res.stringResource
import com.example.cthehabit.R

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

    // Usamos Column para que el juego y el botón estén en compartimentos separados
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0E14)) // Color oscuro para que coincida con el juego
    ) {
        // 1. EL JUEGO (Ocupa todo lo que puede arriba)
        Box(
            modifier = Modifier
                .weight(1f) // Esto empuja al botón hacia abajo
                .fillMaxWidth()
        ) {
            GameScreen(
                horas = horas,
                playerIndex = playerIndex,
                enemyIndex = enemyIndex,
                onBackToMain = onBack,
                onOpenCharacterSelect = onOpenCharacterSelect,
                onOpenTrophies = onOpenTrophies
            )
        }

        // 2. EL ÁREA DEL BOTÓN (Abajo del todo)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp), // Espaciado para que no toque el borde
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = onOpenMissions,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(58.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E3A5F)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.ver_misiones),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }
    }
}