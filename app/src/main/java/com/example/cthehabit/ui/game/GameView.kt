package com.example.cthehabit.ui.game

import android.content.Context
import android.graphics.*
import android.view.SurfaceView
import android.view.MotionEvent
import com.example.cthehabit.R
import kotlin.math.abs

class GameView(context: Context, private val horas: Int) : SurfaceView(context), Runnable {

    private var thread: Thread? = null
    private var running = false
    private var canvas: Canvas? = null

    // Sprites Knight
    private val knightIdle = BitmapFactory.decodeResource(resources, R.drawable.knight_idle)
    private val knightWalk = BitmapFactory.decodeResource(resources, R.drawable.knight_walk)
    private val knightAttack = BitmapFactory.decodeResource(resources, R.drawable.knight_attack01)
    private val knightHurt = BitmapFactory.decodeResource(resources, R.drawable.knight_hurt)
    private val knightDeath = BitmapFactory.decodeResource(resources, R.drawable.knight_death)

    // Sprites Skeleton
    private val skeletonIdle = BitmapFactory.decodeResource(resources, R.drawable.skeleton_idle)
    private val skeletonWalk = BitmapFactory.decodeResource(resources, R.drawable.skeleton_walk)
    private val skeletonAttack = BitmapFactory.decodeResource(resources, R.drawable.skeleton_attack01)
    private val skeletonHurt = BitmapFactory.decodeResource(resources, R.drawable.skeleton_hurt)
    private val skeletonDeath = BitmapFactory.decodeResource(resources, R.drawable.skeleton_death)

    private val scale = 2f

    // Animaciones Knight
    private val idleFrames = sliceSpriteSheet(knightIdle, 6)
    private val walkFrames = sliceSpriteSheet(knightWalk, 8)
    private val attackFrames = sliceSpriteSheet(knightAttack, 7)
    private val hurtFrames = sliceSpriteSheet(knightHurt, 4)
    private val deathFrames = sliceSpriteSheet(knightDeath, 4)

    // Animaciones Skeleton (mirando a la izquierda)
    private val enemyIdleFrames = sliceSpriteSheet(skeletonIdle, 6).map { flipBitmap(it) }
    private val enemyWalkFrames = sliceSpriteSheet(skeletonWalk, 8).map { flipBitmap(it) }
    private val enemyAttackFrames = sliceSpriteSheet(skeletonAttack, 6).map { flipBitmap(it) }
    private val enemyHurtFrames = sliceSpriteSheet(skeletonHurt, 4).map { flipBitmap(it) }
    private val enemyDeathFrames = sliceSpriteSheet(skeletonDeath, 4).map { flipBitmap(it) }

    // Posiciones
    private var knightX = 100f
    private var knightY = 500f
    private var skeletonX = 600f
    private var skeletonY = 500f

    // Estados y frames
    private var knightState = "idle" // idle, walk, attack, hurt, death
    private var skeletonState = "walk" // idle, walk, attack, hurt, death
    private var knightFrame = 0
    private var skeletonFrame = 0

    // Movimiento
    private var movingLeft = false
    private var movingRight = false
    private var attacking = false
    private val moveSpeed = 15f
    private val skeletonSpeed = 5f

    // Control animaciones
    private val knightAnimationDelay = 150L
    private val skeletonAnimationDelay = 200L
    private var lastKnightFrameTime = System.currentTimeMillis()
    private var lastSkeletonFrameTime = System.currentTimeMillis()

    // Lógica de combate
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

            // --- Skeleton AI ---
            if (skeletonState != "death") {
                val direction = if (skeletonX + enemyWalkFrames[0].width*scale/2 > knightX + idleFrames[0].width*scale/2) -1 else 1
                skeletonX += direction * skeletonSpeed

                // Wrap-around pantalla
                if (skeletonX > width) skeletonX = 0f
                if (skeletonX < 0) skeletonX = width - enemyWalkFrames[0].width * scale

                // Mantener separación mínima
                if (abs(knightX - skeletonX) < 50f && skeletonState != "attack") {
                    skeletonX -= direction * skeletonSpeed
                }

                // Skeleton ataque
                if (abs(skeletonX - knightX) < attackDistance && now - lastSkeletonAttackTime >= skeletonAttackInterval) {
                    skeletonState = "attack"
                    skeletonFrame = 0
                    lastSkeletonAttackTime = now
                } else if (skeletonState != "attack" && skeletonState != "hurt") {
                    skeletonState = "walk"
                }
            }

            // Lock canvas
            canvas = holder.lockCanvas()
            drawGame()
            holder.unlockCanvasAndPost(canvas)
            Thread.sleep(16)
        }
    }

    private fun drawGame() {
        val now = System.currentTimeMillis()
        canvas?.drawColor(Color.WHITE)

        // --- Knight Animación ---
        if (now - lastKnightFrameTime >= knightAnimationDelay) {
            knightFrame++
            lastKnightFrameTime = now
        }

        if (movingLeft) knightX -= moveSpeed
        if (movingRight) knightX += moveSpeed

        val knightBitmap = when (knightState) {
            "attack" -> attackFrames[knightFrame % attackFrames.size]
            "hurt" -> hurtFrames[knightFrame % hurtFrames.size]
            "death" -> deathFrames[knightFrame % deathFrames.size]
            "walk" -> walkFrames[knightFrame % walkFrames.size]
            else -> idleFrames[knightFrame % idleFrames.size]
        }

        val kW = knightBitmap.width * scale
        val kH = knightBitmap.height * scale
        if (knightX > width) knightX = -kW
        if (knightX + kW < 0) knightX = width.toFloat()
        val knightRect = RectF(knightX, knightY, knightX + kW, knightY + kH)
        canvas?.drawBitmap(knightBitmap, null, knightRect, null)

        // --- Skeleton Animación ---
        if (now - lastSkeletonFrameTime >= skeletonAnimationDelay) {
            skeletonFrame++
            lastSkeletonFrameTime = now
        }

        val skeletonBitmap = when(skeletonState) {
            "attack" -> enemyAttackFrames[skeletonFrame % enemyAttackFrames.size]
            "hurt" -> enemyHurtFrames[skeletonFrame % enemyHurtFrames.size]
            "death" -> enemyDeathFrames[skeletonFrame % enemyDeathFrames.size]
            "walk" -> enemyWalkFrames[skeletonFrame % enemyWalkFrames.size]
            else -> enemyIdleFrames[skeletonFrame % enemyIdleFrames.size]
        }

        val sW = skeletonBitmap.width * scale
        val sH = skeletonBitmap.height * scale
        val skeletonRect = RectF(skeletonX, skeletonY, skeletonX + sW, skeletonY + sH)
        canvas?.drawBitmap(skeletonBitmap, null, skeletonRect, null)

        // --- Knight ataque ---
        if (attacking && knightHealth>0 && abs(knightX - skeletonX) < attackDistance && skeletonState != "death") {
            skeletonState = "hurt"
            golpesActuales++
            skeletonFrame = 0
            if (golpesActuales >= golpesNecesarios) enemigoMuere()
        }
        if (attacking && knightFrame % attackFrames.size == attackFrames.size-1) attacking=false
        knightState = when {
            knightHealth <=0 -> "death"
            attacking -> "attack"
            movingLeft || movingRight -> "walk"
            else -> "idle"
        }

        // --- Skeleton golpea Knight solo de frente ---
        val skeletonFacingRight = skeletonX < knightX
        val canHit = (skeletonFacingRight && knightX > skeletonX) || (!skeletonFacingRight && knightX < skeletonX)
        if (RectF.intersects(knightRect, skeletonRect) && skeletonState=="attack" && canHit && knightHealth>0) {
            knightHealth--
            knightState = "hurt"
            knightFrame = 0
            skeletonState = "walk"
            if (knightHealth <=0) {
                vidas--
                muertesTotales++
                respawnTimeKnight = System.currentTimeMillis() + respawnDelay
            }
        }

        // Respawn Knight
        if (knightHealth<=0 && System.currentTimeMillis()>=respawnTimeKnight) {
            knightHealth = 2
            knightState = "idle"
            knightX = 100f
            knightY = 500f
        }

        // Reset Skeleton ataque
        if (skeletonState=="attack" && skeletonFrame % enemyAttackFrames.size == enemyAttackFrames.size-1) {
            skeletonState="walk"
        }

        // --- HUD ---
        val paint = Paint()
        paint.color = Color.BLACK
        paint.textSize = 80f
        paint.isFakeBoldText = true
        canvas?.drawText("Nivel: $nivel", 50f, 100f, paint)
        canvas?.drawText("Vidas: $vidas", 50f, 200f, paint)
        canvas?.drawText("Muertes: $muertesTotales", 50f, 300f, paint)
    }

    fun startMoveLeft() { movingLeft = true }
    fun stopMoveLeft() { movingLeft = false }
    fun startMoveRight() { movingRight = true }
    fun stopMoveRight() { movingRight = false }
    fun atacar() { if (!attacking) attacking=true }

    private fun enemigoMuere() {
        nivel++
        golpesActuales=0
        golpesNecesarios=nivel
        skeletonState="death"
        skeletonFrame=0
        skeletonX = width - 200f
    }

    fun resume() {
        running=true
        thread=Thread(this)
        thread!!.start()
    }

    fun pause() {
        running=false
        thread?.join()
    }

    private fun sliceSpriteSheet(sheet: Bitmap, frameCount: Int): List<Bitmap> {
        val frames = mutableListOf<Bitmap>()
        val frameWidth = sheet.width / frameCount
        val frameHeight = sheet.height
        for (i in 0 until frameCount) frames.add(Bitmap.createBitmap(sheet, i*frameWidth,0,frameWidth,frameHeight))
        return frames
    }

    private fun flipBitmap(src: Bitmap): Bitmap {
        val matrix = Matrix()
        matrix.preScale(-1f,1f)
        return Bitmap.createBitmap(src,0,0,src.width,src.height,matrix,false)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        when(event.action){
            MotionEvent.ACTION_DOWN -> {
                when {
                    x < width/3 -> startMoveLeft()
                    x > 2*width/3 -> startMoveRight()
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