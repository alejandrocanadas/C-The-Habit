package com.example.cthehabit.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.cthehabit.data.model.Characters

fun decodeAndScaleFrame(context: Context, resId: Int, targetHeight: Int, frameCount: Int): Bitmap {
    val sheet = BitmapFactory.decodeResource(context.resources, resId)
    val frameWidth = sheet.width / frameCount
    val frameHeight = sheet.height
    val frameBitmap = Bitmap.createBitmap(sheet, 0, 0, frameWidth, frameHeight)
    val scaleFactor = targetHeight.toFloat() / frameHeight
    val newWidth = (frameWidth * scaleFactor).toInt()
    return Bitmap.createScaledBitmap(frameBitmap, newWidth, targetHeight, false)
}

@Composable
fun CharacterSelectScreen(horas: Int, onStartGame: (Int, Int) -> Unit) {
    val context = LocalContext.current
    var selectedPlayer by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Selecciona tu Héroe",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(8.dp)
        ) {
            itemsIndexed(Characters.PLAYERS) { index, character ->
                val isSelected = selectedPlayer == index
                val bitmapPreview = remember(character.idleRes) {
                    decodeAndScaleFrame(context, character.idleRes, 400, character.idleFrames)
                }

                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .aspectRatio(0.75f)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) Color(0xFF4CAF50) else Color.Gray.copy(alpha = 0.3f),
                            shape = MaterialTheme.shapes.medium
                        )
                        .background(
                            color = if (isSelected) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color.Transparent,
                            shape = MaterialTheme.shapes.medium
                        )
                        .clickable { selectedPlayer = index }
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        bitmap = bitmapPreview.asImageBitmap(),
                        contentDescription = character.name,
                        filterQuality = FilterQuality.None,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    )
                    Text(text = character.name, style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        Button(
            onClick = { onStartGame(selectedPlayer, 0) },
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 16.dp)
        ) {
            Text("¡A LUCHAR!")
        }
    }
}