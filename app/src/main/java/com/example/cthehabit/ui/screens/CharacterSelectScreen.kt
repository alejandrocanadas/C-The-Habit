package com.example.cthehabit.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.cthehabit.data.model.Characters
import com.example.cthehabit.ui.game.GameActivity

// --- Helper para recortar un frame del sprite sheet ---
fun decodeFirstFrame(context: Context, resId: Int, frameCount: Int = 1): Bitmap {
    val sheet = BitmapFactory.decodeResource(context.resources, resId)
    val frameWidth = sheet.width / frameCount
    val frameHeight = sheet.height
    return Bitmap.createBitmap(sheet, 0, 0, frameWidth, frameHeight)
}

@Composable
fun CharacterSelectScreen(horas: Int, onStartGame: (Int, Int) -> Unit) {
    val context = LocalContext.current
    var selectedPlayer by remember { mutableStateOf(-1) }
    var selectedEnemy by remember { mutableStateOf(-1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Selecciona tu personaje", style = MaterialTheme.typography.titleLarge)

        LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.weight(1f)) {
            itemsIndexed(Characters.PLAYERS) { index, character ->
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .background(if (selectedPlayer == index) Color.Green.copy(alpha = 0.3f) else Color.Transparent)
                        .clickable { selectedPlayer = index }
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ✅ Aquí no usamos isNotEmpty ni [0], solo elegimos un drawable fijo
                    val bitmapPreview = decodeFirstFrame(context, character.hurtRes ?: character.idleRes, frameCount = 4)
                    Image(
                        bitmap = bitmapPreview.asImageBitmap(),
                        contentDescription = character.name,
                        modifier = Modifier.size(200.dp)
                    )
                    Text(character.name)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Selecciona enemigo", style = MaterialTheme.typography.titleLarge)

        LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.weight(1f)) {
            itemsIndexed(Characters.ENEMIES) { index, enemy ->
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .background(if (selectedEnemy == index) Color.Red.copy(alpha = 0.3f) else Color.Transparent)
                        .clickable { selectedEnemy = index }
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val bitmapPreview = decodeFirstFrame(context, enemy.hurtRes ?: enemy.idleRes, frameCount = 4)
                    Image(
                        bitmap = bitmapPreview.asImageBitmap(),
                        contentDescription = enemy.name,
                        modifier = Modifier
                            .size(200.dp)
                            .graphicsLayer { scaleX = -1f } // enemigo volteado
                    )
                    Text(enemy.name)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                if (selectedPlayer >= 0 && selectedEnemy >= 0) {
                    onStartGame(selectedPlayer, selectedEnemy)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("JUGAR")
        }
    }
}