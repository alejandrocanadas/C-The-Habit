package com.example.cthehabit.data.model

import androidx.annotation.DrawableRes

data class CharacterConfig(
    val name: String,
    @DrawableRes val idleRes: Int,    val idleFrames: Int,
    @DrawableRes val walkRes: Int,    val walkFrames: Int,
    @DrawableRes val attackRes: Int,  val attackFrames: Int,
    @DrawableRes val hurtRes: Int,    val hurtFrames: Int,
    @DrawableRes val deathRes: Int,   val deathFrames: Int,
    val isEnemy: Boolean = false,
    val flipped: Boolean = false
)

