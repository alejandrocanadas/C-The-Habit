package com.example.cthehabit.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cthehabit.data.model.GameCharacter
import com.example.cthehabit.data.model.HabitType
import com.example.cthehabit.ui.game.CharacterComponent
import com.example.cthehabit.ui.game.CharacterState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.res.stringResource
import com.example.cthehabit.data.model.GameBackground
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.cthehabit.R

private val BgDark    = Color(0xFF0D0D1A)
private val BgCard    = Color(0xFF16162A)
private val Gold      = Color(0xFFFFD700)
private val GoldDark  = Color(0xFFB8860B)
private val AccentRed = Color(0xFFCC2936)
private val AccentBlue = Color(0xFF4FC3F7)
private val TextMuted = Color(0xFF6B6B8A)

private fun colorForHabit(type: HabitType?) = when (type) {
    HabitType.TIEMPO_RED -> AccentRed
    HabitType.MISIONES   -> AccentBlue
    HabitType.RACHA      -> Gold
    else                 -> TextMuted
}

private fun iconForHabit(type: HabitType?) = when (type) {
    HabitType.TIEMPO_RED -> "📱"
    HabitType.MISIONES   -> "📋"
    HabitType.RACHA      -> "🔥"
    else                 -> "❓"
}

private fun labelForHabit(type: HabitType?) = when (type) {
    HabitType.TIEMPO_RED -> "Tiempo en pantalla"
    HabitType.MISIONES   -> "Misiones diarias"
    HabitType.RACHA      -> "Racha de días"
    else                 -> "Otros"
}

private fun condicionEnemigo(character: GameCharacter): String {
    val dif = character.config.dificultad
    return when (character.config.habitType) {
        HabitType.TIEMPO_RED -> when {
            dif <= 1 -> "Aparece con ≥ 1h de pantalla"
            dif <= 2 -> "Aparece con ≥ 3h de pantalla"
            dif <= 3 -> "Aparece con ≥ 5h de pantalla"
            else     -> "Aparece con ≥ 6h de pantalla"
        }
        HabitType.MISIONES -> when {
            dif <= 1 -> "Aparece si tienes misiones"
            dif <= 2 -> "Aparece si fallas 1+ misión"
            else     -> "Aparece si fallas 3+ misiones"
        }
        HabitType.RACHA -> when {
            dif <= 3 -> "Aparece con racha ≥ 1 día"
            dif <= 5 -> "Aparece con racha ≥ 3 días"
            else     -> "Aparece con racha ≥ 7 días"
        }
        else -> ""
    }
}

@Composable
fun SalonDeTrofeos() {

    val players = remember { GameCharacter.PLAYERS }
    val enemiesByHabit = remember {
        GameCharacter.ENEMIES
            .groupBy { it.config.habitType ?: HabitType.TIEMPO_RED }
            .mapValues { (_, list) -> list.sortedBy { it.config.dificultad } }
    }

    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var nivelActual by remember { mutableStateOf(1) }
    var rachaActual by remember { mutableStateOf(0) }
    var selectedBg by remember { mutableStateOf(0) }

    DisposableEffect(userId) {
        if (userId == null) return@DisposableEffect onDispose {}
        val reg = FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    nivelActual = snapshot.getLong("currentLevel")?.toInt() ?: 1
                    rachaActual = snapshot.getLong("racha")?.toInt()        ?: 0
                    selectedBg = snapshot.getLong("selectedBg")?.toInt() ?: 0
                }
            }
        onDispose { reg.remove() }
    }

    val jugadoresDesbloqueados = minOf(1 + (nivelActual - 1) / 3, players.size)
    val escenasDesbloqueadas = GameBackground.ALL.count { nivelActual >= it.unlockLevel }

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


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        stringResource(R.string.salon_trofeos),
                        fontSize = 30.sp, fontWeight = FontWeight.Black,
                        color = Gold, letterSpacing = 2.sp,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        stringResource(R.string.nivel_alcanzado, nivelActual),
                        fontSize = 12.sp, color = TextMuted, letterSpacing = 1.sp,
                        style = MaterialTheme.typography.bodyLarge
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            stringResource(R.string.racha_actual), fontSize = 10.sp,
                            color = TextMuted, letterSpacing = 2.sp, fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            stringResource(R.string.racha_dias, rachaActual),
                            fontSize = 28.sp, fontWeight = FontWeight.Black,
                            color = if (rachaActual > 0) Gold else TextMuted, style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(stringResource(R.string.actualiza_cada_dia), fontSize = 10.sp, color = TextMuted, textAlign = TextAlign.End, style = MaterialTheme.typography.bodyLarge)
                        Text(stringResource(R.string.segun_tus_misiones), fontSize = 10.sp, color = TextMuted, textAlign = TextAlign.End, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(BgCard)
                    .padding(4.dp)
            ) {
                val tabs = listOf(
                    Triple("⚔\uFE0E", "Héroes", "$jugadoresDesbloqueados/${players.size}"),
                    Triple("💀\uFE0E", "Enemigos",   "${GameCharacter.ENEMIES.size}"),
                    Triple("🏔\uFE0E", "Escenarios", "$escenasDesbloqueadas/${GameBackground.ALL.size}")
                )
                tabs.forEachIndexed { index, (icon, name, counter) ->
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
                            .padding(vertical = 13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text     = icon,
                                fontSize = 18.sp,
                                color    = if (selectedTab == index) Color.Black else Color.White
                            )
                            Text(
                                text       = name,
                                fontSize   = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color      = if (selectedTab == index) Color.Black else Gold,
                                textAlign  = TextAlign.Center,
                                style      = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text      = counter,
                                fontSize  = 9.sp,
                                color     = if (selectedTab == index) Color.Black.copy(alpha = 0.6f) else TextMuted,
                                textAlign = TextAlign.Center,
                                style     = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }


            if (selectedTab == 0) {

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    val rows = players.chunked(2)
                    items(rows.size) { rowIndex ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rows[rowIndex].forEachIndexed { colIndex, character ->
                                val globalIndex = rowIndex * 2 + colIndex
                                val unlocked = globalIndex < jugadoresDesbloqueados
                                Box(modifier = Modifier.weight(1f)) {
                                    HeroeCard(
                                        character = character,
                                        unlocked  = unlocked,
                                        lockText  = stringResource(R.string.sube_nivel_trofeos, globalIndex * 3 + 1)
                                    )
                                }
                            }
                            if (rows[rowIndex].size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            } else if (selectedTab == 1){
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    val habitOrder = listOf(
                        HabitType.TIEMPO_RED,
                        HabitType.MISIONES,
                        HabitType.RACHA
                    )

                    habitOrder.forEach { habitType ->
                        val grupoEnemigos = enemiesByHabit[habitType] ?: return@forEach
                        val color = colorForHabit(habitType)

                        item {
                            Column(modifier = Modifier.fillMaxWidth()) {

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                                        .background(color.copy(alpha = 0.18f))
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(iconForHabit(habitType), fontSize = 20.sp)
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            labelForHabit(habitType),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black,
                                            color = color
                                        )
                                    }
                                    val minDif = grupoEnemigos.minOf { it.config.dificultad }
                                    val maxDif = grupoEnemigos.maxOf { it.config.dificultad }
                                    Text(
                                        stringResource(R.string.rango_dificultad, minDif, maxDif),
                                        fontSize = 12.sp,
                                        color = color.copy(alpha = 0.8f),
                                        fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge
                                    )
                                }

                                Spacer(Modifier.height(8.dp))

                                val rows = grupoEnemigos.chunked(2)
                                rows.forEach { fila ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        fila.forEach { character ->
                                            Box(modifier = Modifier.weight(1f)) {
                                                EnemyCard(character = character)
                                            }
                                        }
                                        if (fila.size == 1) Spacer(Modifier.weight(1f))
                                    }
                                    Spacer(Modifier.height(10.dp))
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val rows = GameBackground.ALL.chunked(2)
                    items(rows.size) { rowIndex ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rows[rowIndex].forEachIndexed { colIndex, bg ->
                                val globalIndex = rowIndex * 2 + colIndex
                                val unlocked   = nivelActual >= bg.unlockLevel
                                val isSelected = selectedBg == globalIndex
                                Box(modifier = Modifier.weight(1f)) {
                                    EscenarioCard(
                                        background = bg,
                                        unlocked   = unlocked,
                                        isSelected = isSelected,
                                        onClick    = {
                                            if (unlocked) {
                                                selectedBg = globalIndex
                                                userId?.let {
                                                    FirebaseFirestore.getInstance()
                                                        .collection("users").document(it)
                                                        .update("selectedBg", globalIndex)
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                            if (rows[rowIndex].size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroeCard(
    character : GameCharacter,
    unlocked  : Boolean,
    lockText  : String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .border(
                width = 1.5.dp,
                brush = if (unlocked)
                    Brush.linearGradient(listOf(GoldDark, Gold))
                else
                    Brush.linearGradient(listOf(TextMuted.copy(0.2f), TextMuted.copy(0.1f))),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                CharacterComponent(
                    modifier    = Modifier.fillMaxSize(),
                    character   = character,
                    spriteState = CharacterState.IDLE
                )
                if (!unlocked) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.55f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔒", fontSize = 28.sp)
                            Text(
                                lockText, fontSize = 10.sp, color = TextMuted,
                                textAlign = TextAlign.Center, lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Gold.copy(alpha = if (unlocked) 0.18f else 0.05f))
                    .padding(vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (unlocked) character.name else "???",
                    fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    color = if (unlocked) Gold else TextMuted,
                    textAlign = TextAlign.Center, maxLines = 1,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}


@Composable
private fun EnemyCard(character: GameCharacter) {
    val color = colorForHabit(character.config.habitType)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(listOf(color.copy(0.5f), color)),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                CharacterComponent(
                    modifier    = Modifier.fillMaxSize(),
                    character   = character,
                    spriteState = CharacterState.IDLE
                )
                // Insignia de dificultad (esquina superior derecha)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(bottomStart = 8.dp))
                        .background(color.copy(alpha = 0.85f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        stringResource(R.string.dificultad, character.config.dificultad),
                        fontSize = 9.sp, color = Color.Black, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color.copy(alpha = 0.18f))
                    .padding(vertical = 5.dp, horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    character.name,
                    fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    color = color, textAlign = TextAlign.Center, maxLines = 1,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color.copy(alpha = 0.06f))
                    .padding(vertical = 5.dp, horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    condicionEnemigo(character),
                    fontSize = 9.sp, color = TextMuted,
                    textAlign = TextAlign.Center, lineHeight = 13.sp,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun EscenarioCard(
    background: GameBackground,
    unlocked:   Boolean,
    isSelected: Boolean,
    onClick:    () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 2.5.dp else 1.5.dp,
                brush = if (isSelected)
                    Brush.linearGradient(listOf(GoldDark, Gold))
                else if (unlocked)
                    Brush.linearGradient(listOf(TextMuted.copy(0.3f), TextMuted.copy(0.15f)))
                else
                    Brush.linearGradient(listOf(TextMuted.copy(0.1f), TextMuted.copy(0.05f))),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
    ) {
        Image(
            painter            = painterResource(background.drawableRes),
            contentDescription = null,
            modifier           = Modifier.fillMaxSize(),
            contentScale       = ContentScale.Crop,
            alpha              = if (unlocked) 1f else 0.25f
        )
        if (!unlocked) {
            Box(
                modifier         = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔒", fontSize = 28.sp)
                    Text(
                        "Nivel ${background.unlockLevel}",
                        fontSize = 10.sp, color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(bottomStart = 8.dp))
                    .background(Gold)
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    "✓ Activo", fontSize = 9.sp,
                    color = Color.Black, fontWeight = FontWeight.Bold
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = if (unlocked) background.name else "???",
                fontSize   = 12.sp,
                fontWeight = FontWeight.Bold,
                color      = if (unlocked) Gold else TextMuted,
                textAlign  = TextAlign.Center,
                style      = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

