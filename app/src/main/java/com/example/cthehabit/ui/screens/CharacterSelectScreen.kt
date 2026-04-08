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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cthehabit.data.model.Characters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private val BgDark   = Color(0xFF0D0D1A)
private val BgCard   = Color(0xFF16162A)
private val Gold     = Color(0xFFFFD700)
private val GoldDark = Color(0xFFB8860B)
private val TextMuted = Color(0xFF6B6B8A)

fun decodeAndScaleFrame(context: Context, resId: Int, targetHeight: Int, frameCount: Int): Bitmap {
    val sheet = BitmapFactory.decodeResource(context.resources, resId)
    val frameWidth = sheet.width / frameCount
    val frameBitmap = Bitmap.createBitmap(sheet, 0, 0, frameWidth, sheet.height)
    val scaleFactor = targetHeight.toFloat() / sheet.height
    val newWidth = (frameWidth * scaleFactor).toInt()
    return Bitmap.createScaledBitmap(frameBitmap, newWidth, targetHeight, false)
}

@Composable
fun CharacterSelectScreen(horas: Int, onStartGame: (Int, Int) -> Unit) {
    val context = LocalContext.current
    var selectedPlayer by remember { mutableStateOf(0) }
    var nivelActual by remember { mutableStateOf(1) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(Unit) {
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("users")
                .document(userId).get()
                .addOnSuccessListener { doc ->
                    nivelActual = doc.getLong("currentLevel")?.toInt() ?: 1
                }
        }
    }

    val jugadoresDesbloqueados = minOf(1 + (nivelActual - 1) / 3, Characters.PLAYERS.size)

    // Filtro escala de grises para personajes bloqueados
    val grayscaleMatrix = ColorMatrix().apply { setToSaturation(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Selecciona tu Héroe",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = Gold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(Characters.PLAYERS) { index, character ->
                val unlocked = index < jugadoresDesbloqueados
                val isSelected = selectedPlayer == index && unlocked

                val bitmapPreview = remember(character.idleRes) {
                    decodeAndScaleFrame(context, character.idleRes, 400, character.idleFrames)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.75f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BgCard)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Gold
                            else if (unlocked) GoldDark.copy(alpha = 0.3f)
                            else Color.Gray.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable(enabled = unlocked) { selectedPlayer = index }
                        .padding(8.dp)
                ) {
                    Image(
                        bitmap = bitmapPreview.asImageBitmap(),
                        contentDescription = character.name,
                        filterQuality = FilterQuality.None,
                        contentScale = ContentScale.Fit,
                        colorFilter = if (!unlocked) ColorFilter.colorMatrix(grayscaleMatrix) else null,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(if (unlocked) 1f else 0.4f)
                    )

                    if (!unlocked) {
                        Text(
                            text = "🔒",
                            fontSize = 16.sp,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.5f))
                                .padding(3.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                            .background(Color.Black.copy(alpha = 0.45f))
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = character.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (unlocked) Gold else TextMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { onStartGame(selectedPlayer, 0) },
            enabled = selectedPlayer < jugadoresDesbloqueados,
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 16.dp)
        ) {
            Text("¡A LUCHAR!", fontWeight = FontWeight.Black, fontSize = 16.sp)
        }
    }
}