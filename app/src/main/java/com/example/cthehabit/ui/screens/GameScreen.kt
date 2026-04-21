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
import com.example.cthehabit.ui.game.CharacterComponent
import com.example.cthehabit.ui.game.CharacterState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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

    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val scope = rememberCoroutineScope()

    var nivel by remember { mutableStateOf(1) }
    var xp by remember { mutableStateOf(0) }
    var dataLoaded by remember { mutableStateOf(false) }
    fun xpMeta(lvl: Int) = 150 + ((lvl - 1) * 100)

    suspend fun fetchProgress() {
        userId ?: return
        try {
            val doc = db.collection("users").document(userId).get().await()
            nivel = (doc.getLong("currentLevel") ?: 1L).toInt()
            xp = (doc.getLong("xp") ?: 0L).toInt()
        } catch (_: Exception) {}
    }

    suspend fun subirNivel() {
        userId ?: return
        val meta = xpMeta(nivel)
        if (xp >= meta) {
            val nuevoNivel = nivel + 1
            val nuevaXp = xp - meta
            try {
                db.collection("users").document(userId)
                    .update(mapOf("currentLevel" to nuevoNivel, "xp" to nuevaXp))
                    .await()
                nivel = nuevoNivel
                xp = nuevaXp
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(Unit) {
        fetchProgress()
        dataLoaded = true
    }

    val playerChar = GameCharacter.PLAYERS[playerIndex]
    var enemyChar by remember(nivel) {
        mutableStateOf(GameCharacter.ENEMIES[((nivel - 1) / 3) % GameCharacter.ENEMIES.size])
    }

    var playerHp by remember { mutableStateOf(3) }
    var enemyHp by remember(nivel) { mutableStateOf(nivel) }
    var playerState by remember { mutableStateOf(CharacterState.IDLE) }
    var enemyState by remember { mutableStateOf(CharacterState.IDLE) }

    var playerX by remember { mutableStateOf(-150f) }
    var enemyX by remember { mutableStateOf(150f) }

    var isAttacking by remember { mutableStateOf(false) }
    var movingLeft by remember { mutableStateOf(false) }
    var movingRight by remember { mutableStateOf(false) }

    fun estaEnRangoDeGolpe() = abs(playerX - enemyX) < 80f

    val enemyRotation = if (playerX > enemyX) 180f else 0f

    LaunchedEffect(enemyHp, playerHp) {
        while (enemyHp > 0 && playerHp > 0) {
            delay(16)
            if (enemyState != CharacterState.ATTACKING && enemyState != CharacterState.HURT) {
                val speed = 2.8f + (nivel * 0.15f)
                if (!estaEnRangoDeGolpe()) {
                    if (enemyX > playerX) enemyX -= speed else enemyX += speed
                    enemyState = CharacterState.WALKING
                } else {
                    enemyState = CharacterState.IDLE
                }
            }
        }
    }

    LaunchedEffect(enemyHp, playerHp) {
        while (enemyHp > 0 && playerHp > 0) {
            delay(2000)
            if (estaEnRangoDeGolpe() && enemyState != CharacterState.HURT && enemyHp > 0) {
                enemyState = CharacterState.ATTACKING
                delay(500) // Tiempo de "viento" antes del golpe

                val mirandoAlJugador = (enemyRotation == 180f && playerX > enemyX) || (enemyRotation == 0f && playerX < enemyX)

                if (estaEnRangoDeGolpe() && mirandoAlJugador && playerHp > 0) {
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

            val enemigoEstaDeFrente = enemyX > playerX

            if (estaEnRangoDeGolpe() && enemigoEstaDeFrente) {

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

            if (playerState == CharacterState.ATTACKING) {
                playerState = CharacterState.IDLE
            }
        }
    }

    // --- MUERTE Y RESPAWN ---
    LaunchedEffect(enemyHp) {
        if (enemyHp <= 0 && dataLoaded) {
            delay(2500)
            fetchProgress()
            if (xp >= xpMeta(nivel)) subirNivel()
            enemyHp = nivel
            enemyX = 250f
            enemyState = CharacterState.IDLE
            enemyChar = GameCharacter.ENEMIES[((nivel - 1) / 3) % GameCharacter.ENEMIES.size]
        }
    }

    LaunchedEffect(playerHp) {
        if (playerHp <= 0) {
            delay(3000)
            playerHp = 3
            playerX = -150f
            playerState = CharacterState.IDLE
        }
    }

    LaunchedEffect(movingLeft, movingRight) {
        while (movingLeft || movingRight) {
            if (movingLeft) playerX -= 10f
            if (movingRight) playerX += 10f

            if (playerX > 320f) playerX = -320f
            if (playerX < -320f) playerX = 320f

            if (!isAttacking && playerState != CharacterState.HURT && playerHp > 0) {
                playerState = CharacterState.WALKING
            }
            delay(16)
        }
        if (!isAttacking && playerState != CharacterState.HURT && playerHp > 0) {
            playerState = CharacterState.IDLE
        }
    }

    Column(Modifier.fillMaxSize().background(Color(0xFF0D0E14))) {
        if (!dataLoaded) {
            Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            return@Column
        }

        val meta = xpMeta(nivel)

        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF1A1C2C)).padding(16.dp, 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onOpenCharacterSelect, Modifier.background(Color(0xFF2A2D42), RoundedCornerShape(8.dp))) {
                Text("⚔️")
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Text("NIVEL $nivel", color = Color.Cyan, fontWeight = FontWeight.Bold)
                Box(Modifier.width(140.dp).height(8.dp).clip(RoundedCornerShape(4.dp)).background(Color.Black)) {
                    val progress = (xp.toFloat() / meta.toFloat()).coerceIn(0f, 1f)
                    Box(Modifier.fillMaxWidth(progress).fillMaxHeight().background(Color(0xFF4CAF50)))
                }
                Text("$xp / $meta XP", color = Color.White, fontSize = 10.sp)
            }
            IconButton(onClick = onOpenTrophies, Modifier.background(Color(0xFF2A2D42), RoundedCornerShape(8.dp))) {
                Text("🏆")
            }
        }

        Row(Modifier.fillMaxWidth().padding(10.dp), Arrangement.SpaceBetween) {
            Text("TU: ${"❤️".repeat(playerHp.coerceAtLeast(0))}", color = Color.White, fontWeight = FontWeight.Bold)
            Text("BOSS HP: $enemyHp", color = Color.Red, fontWeight = FontWeight.Bold)
        }

        Box(
            Modifier.weight(1f).fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(onPress = { offset ->
                        val w = size.width.toFloat()
                        if (offset.x < w / 3.5f) movingLeft = true
                        else if (offset.x > w - (w / 3.5f)) movingRight = true
                        else { scope.launch { ejecutarAtaque() } }
                        tryAwaitRelease()
                        movingLeft = false
                        movingRight = false
                    })
                }
        ) {
            Image(painter = painterResource(R.drawable.zfall_night), contentDescription = null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)

            CharacterComponent(
                modifier = Modifier.size(200.dp).offset(x = playerX.dp).align(Alignment.BottomCenter)
                    .graphicsLayer { rotationY = 0f },
                character = playerChar,
                spriteState = playerState
            )

            CharacterComponent(
                modifier = Modifier.size(200.dp).offset(x = enemyX.dp).align(Alignment.BottomCenter)
                    .graphicsLayer { rotationY = enemyRotation },
                character = enemyChar,
                spriteState = enemyState
            )
        }
    }
}
