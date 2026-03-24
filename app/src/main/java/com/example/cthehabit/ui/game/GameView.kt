package com.example.cthehabit.ui.game

import android.content.Context
import android.graphics.*
import android.view.SurfaceView
import android.view.MotionEvent
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
    private val scale = 2f

    // --- Personajes ---
    private val player = Character(context, Characters.PLAYERS[playerIndex])
    private val enemy = Character(context, Characters.ENEMIES[enemyIndex])

    init {
        player.x = 100f
        player.y = 500f
        enemy.x = 600f
        enemy.y = 500f
    }

    // Movimiento
    private var movingLeft = false
    private var movingRight = false
    private var attacking = false
    private val moveSpeed = 15f
    private val skeletonSpeed = 5f

    // Lógica combate
    private var knightHealth = 2
    private var vidas = 3
    private var muertesTotales = 0
    private var nivel = 1
    private var golpesNecesarios = 2
    private var golpesActuales = 0
    private val attackDistance = 150f
    private var respawnTimeKnight = 0L
    private val respawnDelay = 4000L
    private var lastSkeletonAttackTime = 0L
    private val skeletonAttackInterval = 3000L

    override fun run() {
        while (running) {
            if (!holder.surface.isValid) continue
            val now = System.currentTimeMillis()

            // --- IA Enemigo ---
            if (enemy.state != "death") {
                val direction = if (enemy.x > player.x) -1 else 1
                enemy.x += direction * skeletonSpeed

                if (enemy.x > width) enemy.x = 0f
                if (enemy.x < 0) enemy.x = width.toFloat()

                if (abs(player.x - enemy.x) < 50f && enemy.state != "attack") {
                    enemy.x -= direction * skeletonSpeed
                }

                if (abs(enemy.x - player.x) < attackDistance &&
                    now - lastSkeletonAttackTime >= skeletonAttackInterval
                ) {
                    enemy.state = "attack"
                    lastSkeletonAttackTime = now
                } else if (enemy.state != "attack" && enemy.state != "hurt") {
                    enemy.state = "walk"
                }
            }

            canvas = holder.lockCanvas()
            drawGame()
            holder.unlockCanvasAndPost(canvas)
            Thread.sleep(16)
        }
    }

    private fun drawGame() {
        canvas?.drawColor(Color.WHITE)

        // --- Animaciones ---
        player.update()
        enemy.update()

        // --- Movimiento player ---
        if (movingLeft) player.x -= moveSpeed
        if (movingRight) player.x += moveSpeed

        // --- Estado player ---
        player.state = when {
            knightHealth <= 0 -> "death"
            attacking -> "attack"
            movingLeft || movingRight -> "walk"
            else -> "idle"
        }

        // --- Dibujar personajes ---
        player.draw(canvas!!, scale)
        enemy.draw(canvas!!, scale)

        // --- Ataque ---
        if (attacking && knightHealth > 0 && abs(player.x - enemy.x) < attackDistance && enemy.state != "death") {
            enemy.state = "hurt"
            golpesActuales++
            if (golpesActuales >= golpesNecesarios) enemigoMuere()
        }

        if (attacking && player.currentFrameCount() > 0 &&
            player.frame % player.currentFrameCount() == player.currentFrameCount() - 1
        ) attacking = false

        // --- Enemigo golpea ---
        val canHit = (enemy.x < player.x && player.x > enemy.x) || (enemy.x > player.x && player.x < enemy.x)
        if (abs(player.x - enemy.x) < 100 && enemy.state == "attack" && canHit && knightHealth > 0) {
            knightHealth--
            player.state = "hurt"
            enemy.state = "walk"
            if (knightHealth <= 0) {
                vidas--
                muertesTotales++
                respawnTimeKnight = System.currentTimeMillis() + respawnDelay
            }
        }

        // --- Respawn ---
        if (knightHealth <= 0 && System.currentTimeMillis() >= respawnTimeKnight) {
            knightHealth = 2
            player.state = "idle"
            player.x = 100f
            player.y = 500f
        }

        // --- Reset ataque enemigo ---
        if (enemy.state == "attack" && enemy.frame % enemy.currentFrameCount() == enemy.currentFrameCount() - 1) {
            enemy.state = "walk"
        }

        // --- HUD ---
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 80f
            isFakeBoldText = true
        }
        canvas?.drawText("Nivel: $nivel", 50f, 100f, paint)
        canvas?.drawText("Vidas: $vidas", 50f, 200f, paint)
        canvas?.drawText("Muertes: $muertesTotales", 50f, 300f, paint)
    }

    fun startMoveLeft() { movingLeft = true }
    fun stopMoveLeft() { movingLeft = false }
    fun startMoveRight() { movingRight = true }
    fun stopMoveRight() { movingRight = false }
    fun atacar() { if (!attacking) attacking = true }

    private fun enemigoMuere() {
        nivel++
        golpesActuales = 0
        golpesNecesarios = nivel
        enemy.state = "death"
        enemy.x = width - 200f
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