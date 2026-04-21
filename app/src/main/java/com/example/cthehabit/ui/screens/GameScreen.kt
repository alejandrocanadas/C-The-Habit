package com.example.cthehabit.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.example.cthehabit.R
import com.example.cthehabit.data.model.GameCharacter
import com.example.cthehabit.data.model.HabitType
import com.example.cthehabit.ui.game.CharacterComponent
import com.example.cthehabit.ui.game.CharacterState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun GameScreen(
    horas: Int,
    playerIndex: Int,
    enemyIndex: Int,
    onOpenCharacterSelect: () -> Unit = {},
    onOpenTrophies: () -> Unit = {},
    onBackToMain: () -> Unit = {}
) {
    BackHandler { onBackToMain() }

    val db     = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val scope  = rememberCoroutineScope()
    val today  = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    var nivel      by remember { mutableStateOf(1) }
    var xp         by remember { mutableStateOf(0) }
    var dataLoaded by remember { mutableStateOf(false) }
    fun xpMeta(lvl: Int) = 150 + ((lvl - 1) * 100)

    var misionesTotalesHoy     by remember { mutableStateOf(0) }
    var misionesCompletadasHoy by remember { mutableStateOf(0) }
    var rachaActual            by remember { mutableStateOf(0) }

    suspend fun fetchProgress() {
        userId ?: return
        try {
            val doc = db.collection("users").document(userId).get().await()
            nivel = (doc.getLong("currentLevel") ?: 1L).toInt()
            xp    = (doc.getLong("xp")           ?: 0L).toInt()
        } catch (_: Exception) {}
    }

    suspend fun fetchMisionesHoy() {
        userId ?: return
        try {
            val snapshot = db.collection("users").document(userId)
                .collection("missions")
                .whereEqualTo("dateAssigned", today)
                .whereEqualTo("cancelled", false)
                .get().await()
            val missions = snapshot.documents
            misionesTotalesHoy     = missions.size
            misionesCompletadasHoy = missions.count { it.getBoolean("completed") == true }
        } catch (_: Exception) {}
    }

    suspend fun fetchRacha() {
        userId ?: return
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            var racha = 0
            var checkDate = Date()
            repeat(30) {
                val dateStr = sdf.format(checkDate)
                val snap = db.collection("users").document(userId)
                    .collection("missions")
                    .whereEqualTo("dateAssigned", dateStr)
                    .whereEqualTo("completed", true)
                    .get().await()
                if (snap.isEmpty) {
                    if (dateStr != today) return@repeat
                } else { racha++ }
                checkDate = Date(checkDate.time - 86_400_000L)
            }
            rachaActual = racha
        } catch (_: Exception) {}
    }


    suspend fun intentarSubirNivel(): Boolean {
        userId ?: return false
        if (xp < xpMeta(nivel)) return false
        val nuevoNivel = nivel + 1
        val nuevaXp    = xp - xpMeta(nivel)
        return try {
            db.collection("users").document(userId)
                .update(mapOf("currentLevel" to nuevoNivel, "xp" to nuevaXp))
                .await()
            nivel = nuevoNivel
            xp    = nuevaXp
            true
        } catch (_: Exception) { false }
    }

    LaunchedEffect(Unit) {
        fetchProgress()
        fetchMisionesHoy()
        fetchRacha()
        dataLoaded = true
    }

    fun elegirEnemigo(): GameCharacter {
        val scoreTiempo = when {
            horas >= 6 -> 10; horas >= 4 -> 7; horas >= 2 -> 4; else -> 1
        }
        val scoreMisiones = if (misionesTotalesHoy == 0) 0 else {
            val pct = misionesCompletadasHoy.toFloat() / misionesTotalesHoy.toFloat()
            when { pct >= 1f -> 0; pct >= 0.5f -> 4; pct >= 0.1f -> 7; else -> 10 }
        }
        val scoreRacha = when {
            rachaActual >= 7 -> 0; rachaActual >= 3 -> 3; rachaActual >= 1 -> 6; else -> 10
        }
        val habitType = when {
            scoreRacha  >= scoreMisiones && scoreRacha  >= scoreTiempo -> HabitType.RACHA
            scoreMisiones >= scoreTiempo                                -> HabitType.MISIONES
            else                                                        -> HabitType.TIEMPO_RED
        }
        val score = when (habitType) {
            HabitType.RACHA    -> scoreRacha
            HabitType.MISIONES -> scoreMisiones
            else               -> scoreTiempo
        }
        return GameCharacter.ENEMIES
            .filter { it.config.habitType == habitType }
            .minByOrNull { abs(it.config.dificultad - score) }
            ?: GameCharacter.ENEMIES.first()
    }

    val playerChar  = GameCharacter.PLAYERS[playerIndex]
    var enemyChar   by remember { mutableStateOf(GameCharacter.ENEMIES.first()) }
    var playerHp    by remember { mutableStateOf(3) }
    var enemyHp     by remember { mutableStateOf(1) }
    var playerState by remember { mutableStateOf(CharacterState.IDLE) }
    var enemyState  by remember { mutableStateOf(CharacterState.IDLE) }


    LaunchedEffect(dataLoaded, misionesCompletadasHoy, rachaActual) {
        if (dataLoaded) {
            enemyChar = elegirEnemigo()
            enemyHp   = enemyChar.config.dificultad
        }
    }

    var playerX     by remember { mutableStateOf(-150f) }
    var enemyX      by remember { mutableStateOf( 150f) }
    var isAttacking by remember { mutableStateOf(false) }
    var movingLeft  by remember { mutableStateOf(false) }
    var movingRight by remember { mutableStateOf(false) }

    fun estaEnRango() = abs(playerX - enemyX) < 80f
    val enemyRotation = if (playerX > enemyX) 180f else 0f

    LaunchedEffect(enemyHp, playerHp) {
        while (enemyHp > 0 && playerHp > 0) {
            delay(16)
            if (enemyState != CharacterState.ATTACKING && enemyState != CharacterState.HURT) {
                if (!estaEnRango()) {
                    val speed = 2.8f + (nivel * 0.15f)
                    enemyX += if (enemyX > playerX) -speed else speed
                    enemyState = CharacterState.WALKING
                } else {
                    if (enemyState == CharacterState.WALKING) enemyState = CharacterState.IDLE
                }
            }
        }
    }


    LaunchedEffect(enemyHp, playerHp) {
        while (enemyHp > 0 && playerHp > 0) {
            delay(2000)
            if (estaEnRango() && enemyState != CharacterState.HURT && enemyHp > 0) {
                enemyState = CharacterState.ATTACKING
                delay(500)
                val mirandoAlJugador = (enemyRotation == 180f && playerX > enemyX) ||
                        (enemyRotation == 0f && playerX < enemyX)
                if (estaEnRango() && mirandoAlJugador && playerHp > 0) {
                    playerHp--
                    if (playerHp <= 0) {
                        playerState = CharacterState.DEATH
                    } else {
                        playerState = CharacterState.HURT
                        delay(400)
                        if (playerHp > 0) playerState = CharacterState.IDLE
                    }
                }
                delay(300)
                if (enemyHp > 0) enemyState = CharacterState.IDLE
            }
        }
    }

    suspend fun ejecutarAtaque() {
        if (!isAttacking && enemyHp > 0) {
            isAttacking = true
            playerState = CharacterState.ATTACKING
            if (estaEnRango() && enemyX > playerX) {
                scope.launch {
                    enemyHp--
                    if (enemyHp <= 0) {
                        enemyState = CharacterState.DEATH
                    } else {
                        enemyState = CharacterState.HURT
                        delay(500)
                        if (enemyHp > 0) enemyState = CharacterState.IDLE
                    }
                }
            }
            delay(400)
            isAttacking = false
            if (playerState == CharacterState.ATTACKING) playerState = CharacterState.IDLE
        }
    }

    LaunchedEffect(enemyHp) {
        if (enemyHp <= 0 && dataLoaded) {
            delay(2500)
            fetchProgress()
            fetchMisionesHoy()
            intentarSubirNivel()

            enemyChar  = elegirEnemigo()
            enemyHp    = enemyChar.config.dificultad   // CAMBIO 3
            enemyX     = 250f
            enemyState = CharacterState.IDLE
        }
    }

    LaunchedEffect(playerHp) {
        if (playerHp <= 0) {
            delay(3000)
            playerHp    = 3
            playerX     = -150f
            playerState = CharacterState.IDLE
        }
    }

    LaunchedEffect(movingLeft, movingRight) {
        while (movingLeft || movingRight) {
            if (movingLeft)  playerX -= 10f
            if (movingRight) playerX += 10f
            if (playerX >  320f) playerX = -320f
            if (playerX < -320f) playerX =  320f
            if (!isAttacking && playerState != CharacterState.HURT && playerHp > 0)
                playerState = CharacterState.WALKING
            delay(16)
        }
        if (!isAttacking && playerState != CharacterState.HURT && playerHp > 0)
            playerState = CharacterState.IDLE
    }

    Column(Modifier.fillMaxSize().background(Color(0xFF0D0E14))) {

        if (!dataLoaded) {
            Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            return@Column
        }

        val meta    = xpMeta(nivel)
        val xpListo = xp >= meta   // ya tiene XP para subir, solo falta matar al enemigo


        Row(
            modifier = Modifier.fillMaxWidth()
                .background(Color(0xFF1A1C2C))
                .padding(16.dp, 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick  = onOpenCharacterSelect,
                modifier = Modifier.background(Color(0xFF2A2D42), RoundedCornerShape(8.dp))
            ) { Text("⚔️") }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                // Aviso cuando el XP ya alcanzó para subir
                if (xpListo) {
                    Text(
                        "⚡ ¡Mata al enemigo para subir!",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                }
                Text("NIVEL $nivel", color = Color.Cyan, fontWeight = FontWeight.Bold)
                Box(
                    Modifier.width(140.dp).height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black)
                ) {
                    val progress = (xp.toFloat() / meta.toFloat()).coerceIn(0f, 1f)
                    Box(
                        Modifier.fillMaxWidth(progress).fillMaxHeight()
                            .background(if (xpListo) Color(0xFFFFD700) else Color(0xFF4CAF50))
                    )
                }
                Text("$xp / $meta XP", color = Color.White, fontSize = 10.sp)
            }

            IconButton(
                onClick  = onOpenTrophies,
                modifier = Modifier.background(Color(0xFF2A2D42), RoundedCornerShape(8.dp))
            ) { Text("🏆") }
        }

        // Indicador de hábito + HP con total
        val habitLabel = when (enemyChar.config.habitType) {
            HabitType.TIEMPO_RED -> "📱 ${horas}h de pantalla hoy"
            HabitType.MISIONES   -> "📋 Misiones: $misionesCompletadasHoy/$misionesTotalesHoy"
            HabitType.RACHA      -> "🔥 Racha: $rachaActual días"
            else                 -> ""
        }

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
            Arrangement.SpaceBetween,
            Alignment.CenterVertically
        ) {
            Text(
                "TU: ${"❤️".repeat(playerHp.coerceAtLeast(0))}",
                color = Color.White, fontWeight = FontWeight.Bold
            )
            Text(habitLabel, color = Color.Gray, fontSize = 11.sp)
            // CAMBIO 3: muestra golpes restantes / total (= dificultad)
            Text(
                "HP: $enemyHp/${enemyChar.config.dificultad}",
                color = Color.Red, fontWeight = FontWeight.Bold
            )
        }

        // ARENA
        Box(
            Modifier.weight(1f).fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(onPress = { offset ->
                        val w = size.width.toFloat()
                        when {
                            offset.x < w / 3.5f         -> movingLeft  = true
                            offset.x > w - w / 3.5f     -> movingRight = true
                            else                         -> scope.launch { ejecutarAtaque() }
                        }
                        tryAwaitRelease()
                        movingLeft  = false
                        movingRight = false
                    })
                }
        ) {
            Image(
                painter      = painterResource(R.drawable.zfall_night),
                contentDescription = null,
                modifier     = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            CharacterComponent(
                modifier = Modifier
                    .size(200.dp)
                    .offset(x = playerX.dp)
                    .align(Alignment.BottomCenter)
                    .graphicsLayer { rotationY = 0f },
                character   = playerChar,
                spriteState = playerState
            )

            CharacterComponent(
                modifier = Modifier
                    .size(200.dp)
                    .offset(x = enemyX.dp)
                    .align(Alignment.BottomCenter)
                    .graphicsLayer { rotationY = enemyRotation },
                character   = enemyChar,
                spriteState = enemyState
            )
        }
    }
}