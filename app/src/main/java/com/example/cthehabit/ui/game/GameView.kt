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
    enemyIndex: Int
) : SurfaceView(context), Runnable {

    private var thread: Thread? = null
    private var running = false
    private var canvas: Canvas? = null
    private val scale = 1.8f

    private val backgroundBitmap: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.zfall_night)

    private val player = Character(context, Characters.PLAYERS[playerIndex])
    private val enemy = Character(context, Characters.ENEMIES[enemyIndex])

    private var movingLeft = false
    private var movingRight = false
    private var attacking = false
    private var hasHit = false // Nueva variable para controlar que el golpe sea único
    private var enemyHasHit = false // Para que el enemigo no te quite toda la vida de un toque

    private val moveSpeed = 15f
    private val skeletonSpeed = 5f

    private var knightHealth = 3 // Siempre empiezas con 3 de vida
    private var nivel = 1
    private var enemyHealth = 1 // Nivel 1 = 1 HP
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

            // IA Enemigo
            if (enemy.state != "death" && enemy.state != "hurt" && knightHealth > 0) {
                val direction = if (enemy.x > player.x) -1 else 1
                enemy.x += direction * skeletonSpeed

                if (abs(enemy.x - player.x) < attackDistance && now - lastSkeletonAttackTime >= skeletonAttackInterval) {
                    enemy.state = "attack"
                    enemy.frame = 0
                    enemyHasHit = false // Reset de daño enemigo
                    lastSkeletonAttackTime = now
                } else if (enemy.state != "attack") {
                    enemy.state = "walk"
                }
            }

            // Infinito
            if (player.x > width) player.x = -50f
            if (player.x < -100f) player.x = width.toFloat()
            if (enemy.x > width) enemy.x = -50f
            if (enemy.x < -100f) enemy.x = width.toFloat()

            // Respawn Enemigo
            if (enemyHealth <= 0 && now >= respawnTimeEnemy) {
                nivel++
                enemyHealth = nivel // El HP escala con el nivel
                enemy.state = "idle"
                enemy.frame = 0
                enemy.x = if (player.x < width / 2) width - 100f else 100f
            }

            // Respawn Jugador
            if (knightHealth <= 0 && now >= respawnTimeKnight) {
                knightHealth = 3 // Reinicia a 3 toques
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

        // Animaciones
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

        // Movimiento
        if (knightHealth > 0 && player.state != "hurt") {
            if (movingLeft) player.x -= moveSpeed
            if (movingRight) player.x += moveSpeed
            if (attacking) player.state = "attack"
            else if (movingLeft || movingRight) player.state = "walk"
            else player.state = "idle"
        }

        // --- LÓGICA DE ATAQUE PLAYER (1 golpe por animación) ---
        if (attacking && !hasHit && player.frame == player.currentFrameCount() / 2) {
            if (abs(player.x - enemy.x) < attackDistance && enemyHealth > 0 && enemy.state != "death") {
                enemyHealth--
                hasHit = true // Bloqueamos más daño hasta el siguiente ataque
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
            hasHit = false // Permitir nuevo golpe en el siguiente clic
        }

        // --- LÓGICA DE ATAQUE ENEMIGO (1 golpe por animación) ---
        if (enemy.state == "attack" && !enemyHasHit && enemy.frame == enemy.currentFrameCount() / 2) {
            if (abs(player.x - enemy.x) < 100 && knightHealth > 0 && player.state != "death") {
                knightHealth--
                enemyHasHit = true // Bloqueamos para que no te quite 2 vidas en un solo ataque
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

        // Reset Hurt
        if (player.state == "hurt" && player.frame >= player.currentFrameCount() - 1) player.state = "idle"
        if (enemy.state == "hurt" && enemy.frame >= enemy.currentFrameCount() - 1) enemy.state = "walk"
        if (enemy.state == "attack" && enemy.frame >= enemy.currentFrameCount() - 1) enemy.state = "walk"

        player.draw(canvas!!, scale)
        enemy.draw(canvas!!, scale)

        // HUD
        val paint = Paint().apply {
            color = Color.WHITE
            textSize = 45f
            isFakeBoldText = true
            setShadowLayer(10f, 0f, 0f, Color.BLACK)
        }
        canvas?.drawText("Nivel: $nivel", 40f, 70f, paint)
        canvas?.drawText("Tus Toques: $knightHealth", 40f, 130f, paint)

        if (enemyHealth > 0 && enemy.state != "death") {
            paint.color = Color.RED
            canvas?.drawText("HP Enemigo: $enemyHealth", width - 350f, 70f, paint)
        }
    }

    fun startMoveLeft() { movingLeft = true }
    fun stopMoveLeft() { movingLeft = false }
    fun startMoveRight() { movingRight = true }
    fun stopMoveRight() { movingRight = false }
    fun atacar() {
        if (!attacking && knightHealth > 0) {
            attacking = true
            hasHit = false // Iniciamos el ataque listo para golpear una vez
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