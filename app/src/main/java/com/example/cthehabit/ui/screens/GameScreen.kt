package com.example.cthehabit.ui.screens

import androidx.activity.compose.BackHandler // Importante
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.cthehabit.ui.game.GameView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun GameScreen(
    horas: Int,
    playerIndex: Int,
    enemyIndex: Int,
    onSiguienteClick: () -> Unit,
    onOpenCharacterSelect: () -> Unit,
    onOpenTrophies: () -> Unit,
    onBackToMain: () -> Unit // Nuevo parámetro para controlar el regreso
) {
    val context = LocalContext.current
    var nivelCargado by remember { mutableStateOf<Int?>(null) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var gameViewInstance by remember { mutableStateOf<GameView?>(null) }
    var nivelActual by remember { mutableStateOf(1) }

    // Intercepta el botón atrás del sistema (flecha del cel)
    BackHandler {
        onBackToMain()
    }

    LaunchedEffect(Unit) {
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("users")
                .document(userId).get()
                .addOnSuccessListener { doc ->
                    val lvl = doc.getLong("currentLevel")?.toInt() ?: 1
                    nivelCargado = lvl
                    nivelActual = lvl
                }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.DarkGray)) {
        // BARRA SUPERIOR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1C2C))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF2A2D42))
                    .border(2.dp, Color(0xFF4FC3F7), RoundedCornerShape(8.dp))
                    .clickable { onOpenCharacterSelect() },
                contentAlignment = Alignment.Center
            ) { Text("⚔️", fontSize = 22.sp) }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("NIVEL", fontSize = 9.sp, color = Color(0xFF9A9EC4), fontWeight = FontWeight.Bold)
                Text("$nivelActual", fontSize = 22.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.Black)
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF2A2D42))
                    .border(2.dp, Color(0xFFB8860B), RoundedCornerShape(8.dp))
                    .clickable { onOpenTrophies() },
                contentAlignment = Alignment.Center
            ) { Text("🏆", fontSize = 22.sp) }
        }

        // ÁREA DEL JUEGO
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black)
        ) {
            if (nivelCargado == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                AndroidView(
                    factory = {
                        GameView(context, horas, playerIndex, enemyIndex, nivelCargado!!).apply {
                            resume()
                            gameViewInstance = this
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { gameViewInstance?.pause() }
    }
}