package com.example.cthehabit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.cthehabit.ui.game.GameView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun GameScreen(
    horas: Int,
    playerIndex: Int,
    enemyIndex: Int,
    onSiguienteClick: () -> Unit
) {
    val context = LocalContext.current
    var nivelCargado by remember { mutableStateOf<Int?>(null) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var gameViewInstance by remember { mutableStateOf<GameView?>(null) }

    LaunchedEffect(Unit) {
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("users")
                .document(userId).get()
                .addOnSuccessListener { doc ->
                    nivelCargado = doc.getLong("currentLevel")?.toInt() ?: 1
                }
                .addOnFailureListener { nivelCargado = 1 }
        } else {
            nivelCargado = 1
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray)
    ) {
        // ── 1. ÁREA DEL JUEGO (70%) ──────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.7f)
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

        // ── 2. PANEL DE BOTONES (30%) ────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.3f)
                .background(Color.White)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { gameViewInstance?.resetLevel() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F)
                )
            ) {
                Text("REINICIAR NIVEL", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(15.dp))

            Button(
                onClick = onSiguienteClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
            ) {
                Text("SIGUIENTE PANTALLA", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}