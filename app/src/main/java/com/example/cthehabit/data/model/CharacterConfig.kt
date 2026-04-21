package com.example.cthehabit.data.model

import androidx.annotation.DrawableRes

// Configuración de un ataque: qué fila del spritesheet y cuántos frames
data class CharacterAttack(
    val framePos: Int,
    val frames: Int
)

enum class CharacterType { PLAYER, ENEMY }

/**
 * Configuración de un personaje usando UN SOLO spritesheet.
 * Cada fila = un estado. Cada columna = un frame.
 */
data class CharacterConfig(
    @DrawableRes val spriteRes: Int,
    val frameWidth: Int = 100,
    val frameHeight: Int = 100,
    val idlePos: Int, val idleFrames: Int,
    val walkPos: Int, val walkFrames: Int,
    val attacks: List<CharacterAttack>,
    val hurtPos: Int, val hurtFrames: Int,
    val deathPos: Int, val deathFrames: Int,
    val type: CharacterType = CharacterType.PLAYER,
    val habitType: HabitType? = null,
    val dificultad: Int = 1
)

enum class HabitType {
    TIEMPO_RED,
    MISIONES,
    RACHA
}