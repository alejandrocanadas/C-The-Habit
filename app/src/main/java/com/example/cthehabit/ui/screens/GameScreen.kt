package com.example.cthehabit.ui.screens

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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.cthehabit.ui.game.GameView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun GameScreen(
    horas: Int,
    playerIndex: Int,
    enemyIndex: Int,
    onSiguienteClick: () -> Unit,
    onOpenTrophies: () -> Unit
) {
    val context = LocalContext.current
    var nivelCargado by remember { mutableStateOf<Int?>(null) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var gameViewInstance by remember { mutableStateOf<GameView?>(null) }
    var currentPlayerIndex by remember { mutableStateOf(playerIndex) }
    var showCharacterSelect by remember { mutableStateOf(false) }
    var nivelActual by remember { mutableStateOf(1) }

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
                    .clickable { showCharacterSelect = true },
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
                    .clickable { onOpenTrophies() }, // ✅ Llama a la navegación
                contentAlignment = Alignment.Center
            ) { Text("🏆", fontSize = 22.sp) }
        }

        // JUEGO (Ocupa todo el peso)
        Box(modifier = Modifier.fillMaxWidth().weight(1f).background(Color.Black)) {
            if (nivelCargado == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                AndroidView(
                    factory = {
                        GameView(context, horas, currentPlayerIndex, enemyIndex, nivelCargado!!).apply {
                            resume()
                            gameViewInstance = this
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    // SELECTOR DE PERSONAJE
    if (showCharacterSelect) {
        Dialog(
            onDismissRequest = { showCharacterSelect = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            MaterialTheme(colorScheme = darkColorScheme(onSurface = Color.White)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.9f))
                        .clickable { showCharacterSelect = false },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .fillMaxHeight(0.85f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1A1C2C))
                            .clickable(enabled = false) { }
                    ) {
                        CharacterSelectScreen(
                            horas = horas,
                            onStartGame = { idx, _ ->
                                currentPlayerIndex = idx
                                showCharacterSelect = false
                            }
                        )
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { gameViewInstance?.pause() }
    }
}