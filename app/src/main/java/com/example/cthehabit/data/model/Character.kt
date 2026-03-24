package com.example.cthehabit.data.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF

class Character(context: Context, val config: CharacterConfig) {

    var x = 0f; var y = 0f
    var state = "idle"
    var health = 3
    var frame = 0
    private var lastFrameTime = 0L
    private val animationDelay = 150L

    private val frames: Map<String, List<Bitmap>>

    init {
        val res = context.resources
        fun load(resId: Int, count: Int): List<Bitmap> {
            val sheet = BitmapFactory.decodeResource(res, resId)
            return sliceSpriteSheet(sheet, count).let {
                if (config.flipped) it.map { bmp -> flipBitmap(bmp) } else it
            }
        }
        frames = mapOf(
            "idle"   to load(config.idleRes,   config.idleFrames),
            "walk"   to load(config.walkRes,   config.walkFrames),
            "attack" to load(config.attackRes, config.attackFrames),
            "hurt"   to load(config.hurtRes,   config.hurtFrames),
            "death"  to load(config.deathRes,  config.deathFrames),
        )
    }

    fun update() {
        val now = System.currentTimeMillis()
        if (now - lastFrameTime >= animationDelay) {
            frame++
            lastFrameTime = now
        }
    }

    fun draw(canvas: Canvas, scale: Float) {
        val list = frames[state] ?: frames["idle"]!!
        val bmp = list[frame % list.size]
        val w = bmp.width * scale
        val h = bmp.height * scale
        canvas.drawBitmap(bmp, null, RectF(x, y, x + w, y + h), null)
    }

    fun currentFrameCount() = frames[state]?.size ?: 1

    private fun sliceSpriteSheet(sheet: Bitmap, count: Int): List<Bitmap> {
        val fw = sheet.width / count
        return (0 until count).map { Bitmap.createBitmap(sheet, it * fw, 0, fw, sheet.height) }
    }

    private fun flipBitmap(src: Bitmap): Bitmap {
        val m = Matrix().apply { preScale(-1f, 1f) }
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, m, false)
    }
}