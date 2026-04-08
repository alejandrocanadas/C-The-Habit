package com.example.cthehabit.ui.game

import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.SurfaceView
import android.view.MotionEvent
import com.example.cthehabit.R
import com.example.cthehabit.data.model.Character
import com.example.cthehabit.data.model.Characters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlin.math.abs

class GameView(
    context: Context,
    private val horas: Int,
    playerIndex: Int,
    private var enemyIndex: Int,
    private var nivelInicial: Int
) : SurfaceView(context), Runnable {

    private var thread: Thread? = null
    private var running = false
    private var canvas: Canvas? = null
    private val scale = 1.8f

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    private var nivel = nivelInicial
    private var xpActual = 0

    private val backgroundBitmap: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.zfall_night)
    private val player = Character(context, Characters.PLAYERS[playerIndex])
    private var enemy = Character(context, Characters.ENEMIES[enemyIndex])

    private var movingLeft = false
    private var movingRight = false
    private var attacking = false
    private var hasHit = false
    private var enemyHasHit = false

    private val moveSpeed = 15f
    private val skeletonSpeed = 5f

    private var knightHealth = 3
    private var enemyHealth = nivel
    private var vidas = 3

    private val attackDistance = 150f
    private var lastSkeletonAttackTime = 0L
    private val skeletonAttackInterval = 3000L

    private var respawnTimeKnight = 0L
    private val respawnDelay = 4000L
    private var respawnTimeEnemy = 0L
    private val respawnDelayEnemy = 3000L

    init {
        post { resetPositions() }
        fetchUserData()
    }

    private fun obtenerXpNecesariaParaSiguienteNivel(): Int {
        return 150 + ((nivel - 1) * 100)
    }

    private fun fetchUserData() {
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        nivel = document.getLong("currentLevel")?.toInt() ?: nivelInicial
                        xpActual = document.getLong("xp")?.toInt() ?: 0
                        enemyHealth = nivel
                    }
                }
        }
    }

    private fun saveProgressToFirebase() {
        if (userId != null) {
            val data = hashMapOf("currentLevel" to nivel, "xp" to xpActual)
            db.collection("users").document(userId).set(data, SetOptions.merge())
        }
    }

    override fun run() {
        while (running) {
            if (!holder.surface.isValid) continue
            val now = System.currentTimeMillis()

            actualizarIA(now)

            if (player.x > width) player.x = -50f
            if (player.x < -100f) player.x = width.toFloat()

            if (enemyHealth <= 0 && now >= respawnTimeEnemy) {
                val xpMeta = obtenerXpNecesariaParaSiguienteNivel()

                if (xpActual >= xpMeta) {
                    xpActual -= xpMeta // Se resta lo consumido, reiniciando la barra
                    nivel++
                    saveProgressToFirebase()
                }

                resetEnemigo()
            }

            if (knightHealth <= 0 && now >= respawnTimeKnight) {
                knightHealth = 3
                player.state = "idle"
                resetPositions()
            }

            canvas = holder.lockCanvas()
            if (canvas != null) {
                drawGame()
                holder.unlockCanvasAndPost(canvas)
            }
            Thread.sleep(20)
        }
    }

    private fun resetEnemigo() {
        enemyHealth = nivel
        val nuevoIndice = ((nivel - 1) / 3) % Characters.ENEMIES.size
        enemy = Character(context, Characters.ENEMIES[nuevoIndice])
        enemy.y = (height / 2f) * 0.62f
        enemy.x = if (player.x < width / 2) width - 100f else 100f
    }

    private fun drawGame() {
        canvas?.let { c ->
            c.drawColor(Color.WHITE) // Fondo blanco de la SurfaceView
            val mitadAltura = height / 2
            val destRect = Rect(0, 0, width, mitadAltura)
            // Fondo del juego (la imagen nocturna)
            c.drawBitmap(backgroundBitmap, null, destRect, null)

            player.update()
            enemy.update()
            actualizarMovimientoJugador()
            actualizarCombate()

            player.draw(c, scale)
            enemy.draw(c, scale)

            dibujarHUD(c)
        }
    }

    private fun dibujarHUD(c: Canvas) {
        val paint = Paint().apply {
            color = Color.WHITE
            textSize = 40f
            isFakeBoldText = true
            setShadowLayer(8f, 0f, 0f, Color.BLACK) // Sombra para legibilidad sobre el fondo
        }

        val xpMeta = obtenerXpNecesariaParaSiguienteNivel()

        c.drawText("Nivel Usuario: $nivel", 50f, 70f, paint)

        val barraAnchoTotal = 300f
        val xInicio = 50f
        val yInicio = 100f // Debajo del nivel
        val altoBarra = 15f

        paint.color = Color.parseColor("#333333")
        paint.clearShadowLayer()
        c.drawRect(xInicio, yInicio, xInicio + barraAnchoTotal, yInicio + altoBarra, paint)


        val porcentaje = (xpActual.toFloat() / xpMeta.toFloat()).coerceAtMost(1f)
        paint.color = Color.WHITE // Blanco puro
        c.drawRect(xInicio, yInicio, xInicio + (barraAnchoTotal * porcentaje), yInicio + altoBarra, paint)

        paint.color = Color.WHITE
        paint.textSize = 30f
        paint.setShadowLayer(8f, 0f, 0f, Color.BLACK) // Recuperamos sombra para el texto
        c.drawText("$xpActual / $xpMeta XP", xInicio, yInicio + altoBarra + 35f, paint)

        paint.textSize = 40f
        c.drawText("Salud: $knightHealth/3", 50f, yInicio + altoBarra + 90f, paint)

        if (enemyHealth > 0) {
            paint.color = Color.RED
            c.drawText("Jefe HP: $enemyHealth", width - 350f, 70f, paint)
        }
    }

    private fun actualizarIA(now: Long) {
        if (enemy.state != "death" && enemy.state != "hurt" && knightHealth > 0) {
            val direction = if (enemy.x > player.x) -1 else 1
            enemy.x += direction * skeletonSpeed
            if (abs(enemy.x - player.x) < attackDistance && now - lastSkeletonAttackTime >= skeletonAttackInterval) {
                enemy.state = "attack"; enemy.frame = 0; enemyHasHit = false
                lastSkeletonAttackTime = now
            } else if (enemy.state != "attack") enemy.state = "walk"
        }
    }

    private fun actualizarMovimientoJugador() {
        if (knightHealth > 0 && player.state != "hurt") {
            if (movingLeft) player.x -= moveSpeed
            if (movingRight) player.x += moveSpeed
            player.state = when {
                attacking -> "attack"
                movingLeft || movingRight -> "walk"
                else -> "idle"
            }
        }
    }

    private fun actualizarCombate() {
        if (attacking && !hasHit && player.frame == player.currentFrameCount() / 2) {
            if (abs(player.x - enemy.x) < attackDistance && enemyHealth > 0) {
                enemyHealth--; hasHit = true
                if (enemyHealth <= 0) {
                    enemy.state = "death"
                    respawnTimeEnemy = System.currentTimeMillis() + respawnDelayEnemy
                } else { enemy.state = "hurt"; enemy.frame = 0 }
            }
        }
        if (attacking && player.frame >= player.currentFrameCount() - 1) { attacking = false; hasHit = false }
        if (enemy.state == "attack" && !enemyHasHit && enemy.frame == enemy.currentFrameCount() / 2) {
            if (abs(player.x - enemy.x) < 100 && knightHealth > 0) {
                knightHealth--; enemyHasHit = true
                player.state = if (knightHealth <= 0) "death" else "hurt"
                if (knightHealth <= 0) respawnTimeKnight = System.currentTimeMillis() + respawnDelay
            }
        }
        if (player.state == "hurt" && player.frame >= player.currentFrameCount() - 1) player.state = "idle"
        if (enemy.state == "hurt" && enemy.frame >= enemy.currentFrameCount() - 1) enemy.state = "walk"
        if (enemy.state == "attack" && enemy.frame >= enemy.currentFrameCount() - 1) enemy.state = "walk"
    }

    private fun resetPositions() {
        val sueloY = (height / 2f) * 0.62f
        player.x = 100f; player.y = sueloY
        enemy.x = width - 400f; enemy.y = sueloY
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                when {
                    event.x < width / 3 -> movingLeft = true
                    event.x > 2 * width / 3 -> movingRight = true
                    else -> atacar()
                }
            }
            MotionEvent.ACTION_UP -> { movingLeft = false; movingRight = false }
        }
        return true
    }

    fun atacar() { if (!attacking && knightHealth > 0) { attacking = true; hasHit = false; player.frame = 0 } }
    fun resume() { running = true; thread = Thread(this); thread?.start() }
    fun pause() { running = false; try { thread?.join() } catch (e: Exception) {} }
}