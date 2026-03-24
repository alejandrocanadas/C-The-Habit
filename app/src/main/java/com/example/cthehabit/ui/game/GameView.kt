package com.example.cthehabit.ui.game

import android.content.Context
import android.graphics.*
import android.view.SurfaceView
import android.view.MotionEvent
import com.example.cthehabit.R
import com.example.cthehabit.data.model.Character
import com.example.cthehabit.data.model.Characters
import kotlin.math.abs

class GameView(
    context: Context,
    private val horas: Int,
    playerIndex: Int,
    private var enemyIndex: Int // Índice inicial
) : SurfaceView(context), Runnable {

    private var thread: Thread? = null
    private var running = false
    private var canvas: Canvas? = null
    private val scale = 1.8f

    // --- Imagen de Fondo ---
    private val backgroundBitmap: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.zfall_night)

    // --- Personajes ---
    private val player = Character(context, Characters.PLAYERS[playerIndex])
    private var enemy = Character(context, Characters.ENEMIES[enemyIndex])

    // --- Estados de Control ---
    private var movingLeft = false
    private var movingRight = false
    private var attacking = false
    private var hasHit = false      // Controla que el jugador pegue 1 vez por clic
    private var enemyHasHit = false // Controla que el enemigo pegue 1 vez por animación

    private val moveSpeed = 15f
    private val skeletonSpeed = 5f

    // --- Estadísticas ---
    private var knightHealth = 3    // Siempre 3 toques para el jugador
    private var nivel = 1
    private var enemyHealth = 1    // HP inicial = Nivel 1
    private var vidas = 3

    private val attackDistance = 150f
    private var lastSkeletonAttackTime = 0L
    private val skeletonAttackInterval = 3000L

    private var respawnTimeKnight = 0L
    private val respawnDelay = 4000L
    private var respawnTimeEnemy = 0L
    private val respawnDelayEnemy = 3000L

    init {
        post {
            resetPositions()
        }
    }

    private fun resetPositions() {
        val sueloY = (height / 2f) * 0.62f
        player.x = 100f
        player.y = sueloY
        enemy.x = width - 400f
        enemy.y = sueloY
    }

    override fun run() {
        while (running) {
            if (!holder.surface.isValid) continue
            val now = System.currentTimeMillis()

            // --- Lógica IA Enemigo ---
            if (enemy.state != "death" && enemy.state != "hurt" && knightHealth > 0) {
                val direction = if (enemy.x > player.x) -1 else 1
                enemy.x += direction * skeletonSpeed

                // Intentar atacar si está cerca
                if (abs(enemy.x - player.x) < attackDistance && now - lastSkeletonAttackTime >= skeletonAttackInterval) {
                    enemy.state = "attack"
                    enemy.frame = 0
                    enemyHasHit = false
                    lastSkeletonAttackTime = now
                } else if (enemy.state != "attack") {
                    enemy.state = "walk"
                }
            }

            // --- Lógica de Pantalla Infinita (Loop) ---
            if (player.x > width) player.x = -50f
            if (player.x < -100f) player.x = width.toFloat()
            if (enemy.x > width) enemy.x = -50f
            if (enemy.x < -100f) enemy.x = width.toFloat()

            // --- Respawn Enemigo y Cambio cada 3 niveles ---
            if (enemyHealth <= 0 && now >= respawnTimeEnemy) {
                nivel++
                enemyHealth = nivel // Sube HP según nivel

                // CAMBIO DE ENEMIGO CADA 3 NIVELES
                val nuevoIndice = ((nivel - 1) / 3) % Characters.ENEMIES.size
                enemy = Character(context, Characters.ENEMIES[nuevoIndice])

                enemy.state = "idle"
                enemy.frame = 0
                enemy.y = (height / 2f) * 0.62f
                enemy.x = if (player.x < width / 2) width - 100f else 100f
            }

            // --- Respawn Jugador ---
            if (knightHealth <= 0 && now >= respawnTimeKnight) {
                knightHealth = 3
                player.state = "idle"
                player.frame = 0
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

    private fun drawGame() {
        canvas?.drawColor(Color.WHITE)
        val mitadAltura = height / 2
        val destRect = Rect(0, 0, width, mitadAltura)
        canvas?.drawBitmap(backgroundBitmap, null, destRect, null)

        // --- Actualizar Animaciones (Muerte lenta sin repetir) ---
        if (player.state == "death") {
            if (player.frame < player.currentFrameCount() - 1) player.update()
        } else {
            player.update()
        }

        if (enemy.state == "death") {
            if (enemy.frame < enemy.currentFrameCount() - 1) enemy.update()
        } else {
            enemy.update()
        }

        // --- Movimiento y Estados Player ---
        if (knightHealth > 0 && player.state != "hurt") {
            if (movingLeft) player.x -= moveSpeed
            if (movingRight) player.x += moveSpeed

            player.state = when {
                attacking -> "attack"
                movingLeft || movingRight -> "walk"
                else -> "idle"
            }
        }

        // --- Daño al Enemigo (Solo 1 golpe por clic) ---
        if (attacking && !hasHit && player.frame == player.currentFrameCount() / 2) {
            if (abs(player.x - enemy.x) < attackDistance && enemyHealth > 0 && enemy.state != "death") {
                enemyHealth--
                hasHit = true
                if (enemyHealth <= 0) {
                    enemy.state = "death"
                    enemy.frame = 0
                    respawnTimeEnemy = System.currentTimeMillis() + respawnDelayEnemy
                } else {
                    enemy.state = "hurt"
                    enemy.frame = 0
                }
            }
        }
        if (attacking && player.frame >= player.currentFrameCount() - 1) {
            attacking = false
            hasHit = false
        }

        // --- Daño al Jugador (Solo 1 golpe por animación enemiga) ---
        if (enemy.state == "attack" && !enemyHasHit && enemy.frame == enemy.currentFrameCount() / 2) {
            if (abs(player.x - enemy.x) < 100 && knightHealth > 0 && player.state != "death") {
                knightHealth--
                enemyHasHit = true
                if (knightHealth <= 0) {
                    player.state = "death"
                    player.frame = 0
                    vidas--
                    respawnTimeKnight = System.currentTimeMillis() + respawnDelay
                } else {
                    player.state = "hurt"
                    player.frame = 0
                }
            }
        }

        // --- Reset de estados temporales ---
        if (player.state == "hurt" && player.frame >= player.currentFrameCount() - 1) player.state = "idle"
        if (enemy.state == "hurt" && enemy.frame >= enemy.currentFrameCount() - 1) enemy.state = "walk"
        if (enemy.state == "attack" && enemy.frame >= enemy.currentFrameCount() - 1) enemy.state = "walk"

        // --- Dibujar ---
        player.draw(canvas!!, scale)
        enemy.draw(canvas!!, scale)

        // --- HUD ---
        val paint = Paint().apply {
            color = Color.WHITE
            textSize = 45f
            isFakeBoldText = true
            setShadowLayer(10f, 0f, 0f, Color.BLACK)
        }
        canvas?.drawText("Nivel: $nivel", 40f, 70f, paint)
        canvas?.drawText("Tus Toques: $knightHealth/3", 40f, 130f, paint)

        if (enemyHealth > 0 && enemy.state != "death") {
            paint.color = Color.RED
            canvas?.drawText("HP Enemigo: $enemyHealth", width - 400f, 70f, paint)
        }
    }

    // --- Funciones de Control ---
    fun startMoveLeft() { movingLeft = true }
    fun stopMoveLeft() { movingLeft = false }
    fun startMoveRight() { movingRight = true }
    fun stopMoveRight() { movingRight = false }
    fun atacar() {
        if (!attacking && knightHealth > 0) {
            attacking = true
            hasHit = false
            player.frame = 0
        }
    }

    fun resume() {
        running = true
        thread = Thread(this)
        thread!!.start()
    }

    fun pause() {
        running = false
        thread?.join()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                when {
                    x < width / 3 -> startMoveLeft()
                    x > 2 * width / 3 -> startMoveRight()
                    else -> atacar()
                }
            }
            MotionEvent.ACTION_UP -> {
                stopMoveLeft()
                stopMoveRight()
            }
        }
        return true
    }
}