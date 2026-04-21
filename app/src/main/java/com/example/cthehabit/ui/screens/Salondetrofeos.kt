package com.example.cthehabit.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cthehabit.data.model.GameCharacter
import com.example.cthehabit.ui.game.CharacterComponent
import com.example.cthehabit.ui.game.CharacterState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private val BgDark       = Color(0xFF0D0D1A)
private val BgCard       = Color(0xFF16162A)
private val BgCardLocked = Color(0xFF0F0F1C)
private val Gold         = Color(0xFFFFD700)
private val GoldDark     = Color(0xFFB8860B)
private val AccentRed    = Color(0xFFCC2936)
private val TextMuted    = Color(0xFF6B6B8A)

@Composable
fun SalonDeTrofeos() {

    // Listas desde GameCharacter — sin Characters.PLAYERS / ENEMIES
    val players = remember { GameCharacter.PLAYERS }
    val enemies = remember { GameCharacter.ENEMIES }

    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var nivelActual by remember { mutableStateOf(1) }
    var rachaActual by remember { mutableStateOf(0) }

    DisposableEffect(userId) {
        if (userId == null) return@DisposableEffect onDispose {}
        val reg = FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    nivelActual = snapshot.getLong("currentLevel")?.toInt() ?: 1
                    rachaActual = snapshot.getLong("racha")?.toInt()        ?: 0
                }
            }
        onDispose { reg.remove() }
    }

    val jugadoresDesbloqueados = minOf(1 + (nivelActual - 1) / 3, players.size)
    val enemigosDesbloqueados  = minOf((nivelActual - 1) / 3 + 1, enemies.size)

    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by pulseAnim.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = "glow"
    )

    var selectedTab by remember { mutableStateOf(0) }

    Box(modifier = Modifier.fillMaxSize().background(BgDark)) {

        Canvas(modifier = Modifier.fillMaxSize()) {
            listOf(
                Offset(50f, 120f), Offset(300f, 80f),  Offset(500f, 200f),
                Offset(150f, 400f),Offset(700f, 350f), Offset(80f,  600f),
                Offset(400f, 500f),Offset(600f, 650f), Offset(250f, 750f),
                Offset(750f, 150f),Offset(30f,  850f), Offset(680f, 800f)
            ).forEach { pos ->
                drawCircle(color = Color.White.copy(alpha = 0.15f), radius = 2f, center = pos)
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {

            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text          = "⚔  SALÓN DE TROFEOS  ⚔",
                        fontSize      = 18.sp,
                        fontWeight    = FontWeight.Black,
                        color         = Gold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text          = "Nivel alcanzado: $nivelActual",
                        fontSize      = 12.sp,
                        color         = TextMuted,
                        letterSpacing = 1.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFF1A1A2E))
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            listOf(
                                GoldDark.copy(alpha = glowAlpha),
                                Gold.copy(alpha = glowAlpha),
                                GoldDark.copy(alpha = glowAlpha)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "RACHA ACTUAL", fontSize = 10.sp,
                            color = TextMuted, letterSpacing = 2.sp, fontWeight = FontWeight.Bold
                        )
                        Text(
                            text       = "🔥  $rachaActual días",
                            fontSize   = 28.sp,
                            fontWeight = FontWeight.Black,
                            color      = if (rachaActual > 0) Gold else TextMuted
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Actualiza cada día", fontSize = 10.sp, color = TextMuted, textAlign = TextAlign.End)
                        Text(text = "según tus misiones",  fontSize = 10.sp, color = TextMuted, textAlign = TextAlign.End)
                    }
                }
            }

            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BgCard)
            ) {
                listOf(
                    "⚔ Héroes ($jugadoresDesbloqueados/${players.size})",
                    "💀 Enemigos ($enemigosDesbloqueados/${enemies.size})"
                ).forEachIndexed { index, label ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (selectedTab == index)
                                    Brush.horizontalGradient(listOf(GoldDark, Gold.copy(alpha = 0.8f)))
                                else
                                    Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                            )
                            .clickable { selectedTab = index }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = label,
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color      = if (selectedTab == index) Color.Black else TextMuted,
                            textAlign  = TextAlign.Center
                        )
                    }
                }
            }

            val list          = if (selectedTab == 0) players else enemies
            val unlockedCount = if (selectedTab == 0) jugadoresDesbloqueados else enemigosDesbloqueados

            LazyVerticalGrid(
                columns               = GridCells.Fixed(2),
                modifier              = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                contentPadding        = PaddingValues(bottom = 24.dp, top = 4.dp),
                verticalArrangement   = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(list) { index, character ->
                    TrofeoCard(
                        character = character,
                        unlocked  = index < unlockedCount,
                        isEnemy   = selectedTab == 1,
                        index     = index
                    )
                }
            }
        }
    }
}

@Composable
private fun TrofeoCard(
    character : GameCharacter,
    unlocked  : Boolean,
    isEnemy   : Boolean,
    index     : Int
) {
    val borderBrush = if (unlocked) {
        if (isEnemy) Brush.linearGradient(listOf(AccentRed, Color(0xFF8B0000)))
        else         Brush.linearGradient(listOf(GoldDark, Gold))
    } else {
        Brush.linearGradient(listOf(TextMuted.copy(alpha = 0.2f), TextMuted.copy(alpha = 0.1f)))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (unlocked) BgCard else BgCardLocked)
            .border(width = 1.5.dp, brush = borderBrush, shape = RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier            = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Zona del sprite
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 12.dp, start = 12.dp, end = 12.dp),
                contentAlignment = Alignment.Center
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (!unlocked) Modifier.alpha(0.15f).blur(2.dp) else Modifier
                        )
                ) {
                    CharacterComponent(
                        modifier    = Modifier.fillMaxSize(),
                        character   = character,
                        spriteState = CharacterState.IDLE
                    )
                }

                if (!unlocked) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "🔒", fontSize = 32.sp)
                        Text(
                            text       = if (isEnemy) "Derrota más\nenemigos"
                            else         "Sube al\nnivel ${index * 3 + 1}",
                            fontSize   = 10.sp,
                            color      = TextMuted,
                            textAlign  = TextAlign.Center,
                            lineHeight = 14.sp
                        )
                    }
                }

                if (unlocked) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .clip(RoundedCornerShape(bottomStart = 8.dp))
                            .background(if (isEnemy) AccentRed else Gold)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = if (isEnemy) "💀" else "⚔", fontSize = 10.sp)
                    }
                }
            }

            // Nombre
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (unlocked)
                            if (isEnemy) Color(0x33CC2936) else Color(0x33FFD700)
                        else Color(0x11FFFFFF)
                    )
                    .padding(vertical = 8.dp, horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = if (unlocked) character.name else "???",
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color      = if (unlocked) (if (isEnemy) AccentRed else Gold) else TextMuted,
                    textAlign  = TextAlign.Center,
                    maxLines   = 1
                )
            }
        }
    }
}