package com.example.cthehabit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.cthehabit.ui.game.GameView

@Composable
fun GameScreen(
    horas: Int,
    playerIndex: Int,
    enemyIndex: Int,
    onSiguienteClick: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // --- GameView ---
        AndroidView(
            factory = {
                GameView(context, horas, playerIndex, enemyIndex).apply {
                    resume() // inicia el thread
                }
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // --- Botón Siguiente ---
        Button(
            onClick = onSiguienteClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Siguiente pantalla")
        }
    }
}