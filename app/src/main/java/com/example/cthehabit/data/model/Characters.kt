package com.example.cthehabit.data.model

import com.example.cthehabit.R

object Characters {

    val PLAYERS = listOf(
        CharacterConfig(
            name = "Archer",
            idleRes = R.drawable.archer_idle,       idleFrames = 6,
            walkRes = R.drawable.archer_walk,       walkFrames = 8,
            attackRes = R.drawable.archer_attack01, attackFrames = 9,
            hurtRes = R.drawable.archer_hurt,       hurtFrames = 4,
            deathRes = R.drawable.archer_death,     deathFrames = 4
        ),
        CharacterConfig(
            name = "Armored Axeman",
            idleRes = R.drawable.armored_axeman_idle,       idleFrames = 6,
            walkRes = R.drawable.armored_axeman_walk,       walkFrames = 8,
            attackRes = R.drawable.armored_axeman_attack01, attackFrames = 9,
            hurtRes = R.drawable.armored_axeman_hurt,       hurtFrames = 4,
            deathRes = R.drawable.armored_axeman_death,     deathFrames = 4
        ),
        CharacterConfig(
            name = "Knight",
            idleRes = R.drawable.knight_idle,       idleFrames = 6,
            walkRes = R.drawable.knight_walk,       walkFrames = 8,
            attackRes = R.drawable.knight_attack01, attackFrames = 7,
            hurtRes = R.drawable.knight_hurt,       hurtFrames = 4,
            deathRes = R.drawable.knight_death,     deathFrames = 4
        ),
        CharacterConfig(
            name = "Knight Templar",
            idleRes = R.drawable.knight_templar_idle,       idleFrames = 6,
            walkRes = R.drawable.knight_templar_walk01,       walkFrames = 8,
            attackRes = R.drawable.knight_templar_attack01, attackFrames = 7,
            hurtRes = R.drawable.knight_templar_hurt,       hurtFrames = 4,
            deathRes = R.drawable.knight_templar_death,     deathFrames = 4
        ),
        CharacterConfig(
            name = "Lancer",
            idleRes = R.drawable.lancer_idle,       idleFrames = 6,
            walkRes = R.drawable.lancer_walk01,       walkFrames = 8,
            attackRes = R.drawable.lancer_attack01, attackFrames = 6,
            hurtRes = R.drawable.lancer_hurt,       hurtFrames = 4,
            deathRes = R.drawable.lancer_death,     deathFrames = 4
        ),
        CharacterConfig(
            name = "Priest",
            idleRes = R.drawable.priest_idle,       idleFrames = 6,
            walkRes = R.drawable.priest_walk,       walkFrames = 8,
            attackRes = R.drawable.priest_attack, attackFrames = 9,
            hurtRes = R.drawable.priest_hurt,       hurtFrames = 4,
            deathRes = R.drawable.priest_death,     deathFrames = 4
        ),
        CharacterConfig(
            name = "Slime",
            idleRes = R.drawable.slime_idle,       idleFrames = 6,
            walkRes = R.drawable.slime_walk,       walkFrames = 6,
            attackRes = R.drawable.slime_attack01, attackFrames = 6,
            hurtRes = R.drawable.slime_hurt,       hurtFrames = 4,
            deathRes = R.drawable.slime_death,     deathFrames = 4
        ),
        CharacterConfig(
            name = "Soldier",
            idleRes = R.drawable.soldier_idle,       idleFrames = 6,
            walkRes = R.drawable.soldier_walk,       walkFrames = 8,
            attackRes = R.drawable.soldier_attack01, attackFrames = 6,
            hurtRes = R.drawable.soldier_hurt,       hurtFrames = 4,
            deathRes = R.drawable.soldier_death,     deathFrames = 4
        ),
        CharacterConfig(
            name = "Swordsman",
            idleRes = R.drawable.swordsman_idle,       idleFrames = 6,
            walkRes = R.drawable.swordsman_walk,       walkFrames = 8,
            attackRes = R.drawable.swordsman_attack01, attackFrames = 7,
            hurtRes = R.drawable.swordsman_hurt,       hurtFrames = 4,
            deathRes = R.drawable.swordsman_death,     deathFrames = 4
        ),
        CharacterConfig(
            name = "Wizard",
            idleRes = R.drawable.wizard_idle,       idleFrames = 6,
            walkRes = R.drawable.wizard_walk,       walkFrames = 8,
            attackRes = R.drawable.wizard_attack01, attackFrames = 6,
            hurtRes = R.drawable.wizard_hurt,       hurtFrames = 4,
            deathRes = R.drawable.wizard_death,     deathFrames = 4
        ),
    )

    val ENEMIES = listOf(
        CharacterConfig(
            name = "Armored Orc",
            idleRes = R.drawable.armored_orc_idle,       idleFrames = 6,
            walkRes = R.drawable.armored_orc_walk,       walkFrames = 8,
            attackRes = R.drawable.armored_orc_attack01, attackFrames = 7,
            hurtRes = R.drawable.armored_orc_hurt,       hurtFrames = 4,
            deathRes = R.drawable.armored_orc_death,     deathFrames = 4,
            isEnemy = true, flipped = true
        ),
        CharacterConfig(
            name = "Armored Skeleton",
            idleRes = R.drawable.armored_skeleton_idle,       idleFrames = 6,
            walkRes = R.drawable.armored_skeleton_walk,       walkFrames = 8,
            attackRes = R.drawable.armored_skeleton_attack01, attackFrames = 8,
            hurtRes = R.drawable.armored_skeleton_hurt,       hurtFrames = 4,
            deathRes = R.drawable.armored_skeleton_death,     deathFrames = 4,
            isEnemy = true, flipped = true
        ),
        CharacterConfig(
            name = "Elite Orc",
            idleRes = R.drawable.elite_orc_idle,       idleFrames = 6,
            walkRes = R.drawable.elite_orc_walk,       walkFrames = 8,
            attackRes = R.drawable.elite_orc_attack01, attackFrames = 7,
            hurtRes = R.drawable.elite_orc_hurt,       hurtFrames = 4,
            deathRes = R.drawable.elite_orc_death,     deathFrames = 4,
            isEnemy = true, flipped = true
        ),
        CharacterConfig(
            name = "Greatsword Skeleton",
            idleRes = R.drawable.greatsword_skeleton_idle,       idleFrames = 6,
            walkRes = R.drawable.greatsword_skeleton_walk,       walkFrames = 8,
            attackRes = R.drawable.greatsword_skeleton_attack01, attackFrames = 9,
            hurtRes = R.drawable.greatsword_skeleton_hurt,       hurtFrames = 4,
            deathRes = R.drawable.greatsword_skeleton_death,     deathFrames = 4,
            isEnemy = true, flipped = true
        ),
        CharacterConfig(
            name = "Orc",
            idleRes = R.drawable.orc_idle,       idleFrames = 6,
            walkRes = R.drawable.orc_walk,       walkFrames = 8,
            attackRes = R.drawable.orc_attack01, attackFrames = 6,
            hurtRes = R.drawable.orc_hurt,       hurtFrames = 4,
            deathRes = R.drawable.orc_death,     deathFrames = 4,
            isEnemy = true, flipped = true
        ),
        CharacterConfig(
            name = "Orc Rider",
            idleRes = R.drawable.orc_rider_idle,       idleFrames = 6,
            walkRes = R.drawable.orc_rider_walk,       walkFrames = 8,
            attackRes = R.drawable.orc_rider_attack01, attackFrames = 8,
            hurtRes = R.drawable.orc_rider_hurt,       hurtFrames = 4,
            deathRes = R.drawable.orc_rider_death,     deathFrames = 4,
            isEnemy = true, flipped = true
        ),
        CharacterConfig(
            name = "Skeleton",
            idleRes = R.drawable.skeleton_idle,       idleFrames = 6,
            walkRes = R.drawable.skeleton_walk,       walkFrames = 8,
            attackRes = R.drawable.skeleton_attack01, attackFrames = 6,
            hurtRes = R.drawable.skeleton_hurt,       hurtFrames = 4,
            deathRes = R.drawable.skeleton_death,     deathFrames = 4,
            isEnemy = true, flipped = true
        ),
        CharacterConfig(
            name = "Skeleton Archer",
            idleRes = R.drawable.skeleton_archer_idle,       idleFrames = 6,
            walkRes = R.drawable.skeleton_archer_walk,       walkFrames = 8,
            attackRes = R.drawable.skeleton_archer_attack, attackFrames = 9,
            hurtRes = R.drawable.skeleton_archer_hurt,       hurtFrames = 4,
            deathRes = R.drawable.skeleton_archer_death,     deathFrames = 4,
            isEnemy = true, flipped = true
        ),
        CharacterConfig(
            name = "Werewolf",
            idleRes = R.drawable.werewolf_idle,       idleFrames = 6,
            walkRes = R.drawable.werewolf_walk,       walkFrames = 8,
            attackRes = R.drawable.werewolf_attack01, attackFrames = 9,
            hurtRes = R.drawable.werewolf_hurt,       hurtFrames = 4,
            deathRes = R.drawable.werewolf_death,     deathFrames = 4,
            isEnemy = true, flipped = true
        ),
        CharacterConfig(
            name = "Werebear",
            idleRes = R.drawable.werebear_idle,       idleFrames = 6,
            walkRes = R.drawable.werebear_walk,       walkFrames = 8,
            attackRes = R.drawable.werebear_attack01, attackFrames = 9,
            hurtRes = R.drawable.werebear_hurt,       hurtFrames = 4,
            deathRes = R.drawable.werebear_death,     deathFrames = 4,
            isEnemy = true, flipped = true
        ),
    )
}