package com.example.cthehabit.data.model

import com.example.cthehabit.R

object Characters {

    val PLAYERS = listOf(
        CharacterConfig(
            name = "Knight",
            idleRes = R.drawable.knight_idle,       idleFrames = 6,
            walkRes = R.drawable.knight_walk,       walkFrames = 8,
            attackRes = R.drawable.knight_attack01, attackFrames = 7,
            hurtRes = R.drawable.knight_hurt,       hurtFrames = 4,
            deathRes = R.drawable.knight_death,     deathFrames = 4
        ),
        // Agregá tus otros 9 jugadores con el mismo patrón:
        // CharacterConfig(
        //     name = "Mage",
        //     idleRes = R.drawable.mage_idle,       idleFrames = 5,
        //     walkRes = R.drawable.mage_walk,       walkFrames = 6,
        //     attackRes = R.drawable.mage_attack01, attackFrames = 5,
        //     hurtRes = R.drawable.mage_hurt,       hurtFrames = 3,
        //     deathRes = R.drawable.mage_death,     deathFrames = 4
        // ),
    )

    val ENEMIES = listOf(
        CharacterConfig(
            name = "Skeleton",
            idleRes = R.drawable.skeleton_idle,       idleFrames = 6,
            walkRes = R.drawable.skeleton_walk,       walkFrames = 8,
            attackRes = R.drawable.skeleton_attack01, attackFrames = 6,
            hurtRes = R.drawable.skeleton_hurt,       hurtFrames = 4,
            deathRes = R.drawable.skeleton_death,     deathFrames = 4,
            isEnemy = true,
            flipped = true
        ),
        // Tus otros 9 enemigos acá...
    )
}