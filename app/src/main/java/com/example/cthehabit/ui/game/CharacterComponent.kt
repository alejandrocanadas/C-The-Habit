package com.example.cthehabit.ui.game

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.example.cthehabit.data.model.CharacterAttack
import com.example.cthehabit.data.model.CharacterConfig
import com.example.cthehabit.data.model.CharacterType
import com.example.cthehabit.data.model.GameCharacter
import kotlinx.coroutines.delay

const val DEFAULT_FPS = 8
const val DEFAULT_FRAME_WIDTH = 100
const val DEFAULT_FRAME_HEIGHT = 100

enum class CharacterState { IDLE, WALKING, ATTACKING, HURT, DEATH }


@Composable
fun CharacterComponent(
    modifier: Modifier = Modifier,
    character: GameCharacter,
    spriteState: CharacterState = CharacterState.IDLE,
    currentAttack: CharacterAttack = character.config.attacks.first(),
    onAnimationComplete: () -> Unit = {}
) {
    val cfg = character.config
    val isEnemy = cfg.type == CharacterType.ENEMY

    when (spriteState) {
        CharacterState.IDLE -> SpriteAnimation(
            modifier = modifier,
            spriteSheetId = cfg.spriteRes,
            frameWidth = cfg.frameWidth,
            frameHeight = cfg.frameHeight,
            spriteRow = cfg.idlePos,
            totalFrames = cfg.idleFrames,
            flipHorizontal = isEnemy
        )
        CharacterState.WALKING -> SpriteAnimation(
            modifier = modifier,
            spriteSheetId = cfg.spriteRes,
            frameWidth = cfg.frameWidth,
            frameHeight = cfg.frameHeight,
            spriteRow = cfg.walkPos,
            totalFrames = cfg.walkFrames,
            flipHorizontal = isEnemy
        )
        CharacterState.ATTACKING -> SpriteAnimation(
            modifier = modifier,
            spriteSheetId = cfg.spriteRes,
            frameWidth = cfg.frameWidth,
            frameHeight = cfg.frameHeight,
            spriteRow = currentAttack.framePos,
            totalFrames = currentAttack.frames,
            flipHorizontal = isEnemy,
            loop = false,
            onAnimationComplete = onAnimationComplete
        )
        CharacterState.HURT -> SpriteAnimation(
            modifier = modifier,
            spriteSheetId = cfg.spriteRes,
            frameWidth = cfg.frameWidth,
            frameHeight = cfg.frameHeight,
            spriteRow = cfg.hurtPos,
            totalFrames = cfg.hurtFrames,
            flipHorizontal = isEnemy,
            loop = false,
            onAnimationComplete = onAnimationComplete
        )
        CharacterState.DEATH -> SpriteAnimation(
            modifier = modifier,
            spriteSheetId = cfg.spriteRes,
            frameWidth = cfg.frameWidth,
            frameHeight = cfg.frameHeight,
            spriteRow = cfg.deathPos,
            totalFrames = cfg.deathFrames,
            flipHorizontal = isEnemy,
            loop = false,
            holdLastFrameMs = 1200L,
            onAnimationComplete = onAnimationComplete
        )
    }
}


@Composable
fun SpriteAnimation(
    modifier: Modifier = Modifier,
    @DrawableRes spriteSheetId: Int,
    frameWidth: Int = DEFAULT_FRAME_WIDTH,
    frameHeight: Int = DEFAULT_FRAME_HEIGHT,
    spriteRow: Int = 0,
    totalFrames: Int = 2,
    fps: Int = DEFAULT_FPS,
    loop: Boolean = true,
    holdLastFrameMs: Long = 0L,
    flipHorizontal: Boolean = false,
    onAnimationComplete: () -> Unit = {}
) {
    val spriteSheet = ImageBitmap.imageResource(id = spriteSheetId)

    val frameAnim = remember { Animatable(0, Int.VectorConverter) }
    val latestOnComplete by rememberUpdatedState(onAnimationComplete)

    LaunchedEffect(spriteSheetId, spriteRow, totalFrames, fps, loop) {
        frameAnim.snapTo(0)
        val durationMs = totalFrames * 1000 / fps
        if (loop) {
            while (true) {
                frameAnim.animateTo(
                    targetValue = totalFrames,
                    animationSpec = tween(durationMillis = durationMs, easing = LinearEasing)
                )
                frameAnim.snapTo(0)
            }
        } else {
            frameAnim.animateTo(
                targetValue = totalFrames,
                animationSpec = tween(durationMillis = durationMs, easing = LinearEasing)
            )
            if (holdLastFrameMs > 0L) delay(holdLastFrameMs)
            latestOnComplete()
        }
    }

    val currentFrame = frameAnim.value.coerceIn(0, (totalFrames - 1).coerceAtLeast(0))

    val srcOffset = IntOffset(
        x = currentFrame * frameWidth,
        y = spriteRow * frameHeight
    )
//lo que decia el profe
    Canvas(
        modifier = modifier
            .aspectRatio(frameWidth.toFloat() / frameHeight.toFloat())
            .clipToBounds()
    ) {
        scale(
            scaleX = if (flipHorizontal) -1f else 1f,
            scaleY = 1f,
            pivot = Offset(size.width / 2f, size.height / 2f)
        ) {
            drawImage(
                image = spriteSheet,
                srcOffset = srcOffset,
                srcSize = IntSize(frameWidth, frameHeight),
                dstOffset = IntOffset.Zero,
                dstSize = IntSize(size.width.toInt(), size.height.toInt()),
                filterQuality = FilterQuality.None   // ← pixel art nítido
            )
        }
    }
}