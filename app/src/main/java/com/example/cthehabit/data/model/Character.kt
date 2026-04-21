package com.example.cthehabit.data.model

import com.example.cthehabit.R

enum class GameCharacter(val config: CharacterConfig) {


    ARCHER(CharacterConfig(
        spriteRes = R.drawable.archer,
        idlePos = 0, idleFrames = 6,
        walkPos = 1, walkFrames = 8,
        attacks = listOf(CharacterAttack(2, 8), CharacterAttack(3, 12)),
        hurtPos = 4, hurtFrames = 4,
        deathPos = 5, deathFrames = 4,
        type = CharacterType.PLAYER
    )),

    ARMORED_AXEMAN(CharacterConfig(
        spriteRes = R.drawable.armored_axeman,
        idlePos = 0, idleFrames = 6,
        walkPos = 1, walkFrames = 8,
        attacks = listOf(CharacterAttack(2, 8), CharacterAttack(3, 8), CharacterAttack(4, 12)),
        hurtPos = 5, hurtFrames = 4,
        deathPos = 6, deathFrames = 4,
        type = CharacterType.PLAYER
    )),

    KNIGHT(CharacterConfig(
        spriteRes = R.drawable.knight,
        idlePos = 0, idleFrames = 6,
        walkPos = 1, walkFrames = 8,
        attacks = listOf(CharacterAttack(2, 6), CharacterAttack(3, 10), CharacterAttack(4, 12)),
        hurtPos = 6, hurtFrames = 4,
        deathPos = 7, deathFrames = 4,
        type = CharacterType.PLAYER
    )),

    KNIGHT_TEMPLAR(CharacterConfig(
        spriteRes = R.drawable.knight_templar,
        idlePos = 0, idleFrames = 6,
        walkPos = 1, walkFrames = 8,
        attacks = listOf(CharacterAttack(2, 8), CharacterAttack(3, 6), CharacterAttack(4, 8), CharacterAttack(5, 12)),
        hurtPos = 7, hurtFrames = 4,
        deathPos = 8, deathFrames = 4,
        type = CharacterType.PLAYER
    )),

    LANCER(CharacterConfig(
        spriteRes = R.drawable.lancer,
        idlePos = 0, idleFrames = 6,
        walkPos = 1, walkFrames = 8,
        attacks = listOf(CharacterAttack(2, 8), CharacterAttack(3, 6), CharacterAttack(4, 8), CharacterAttack(5, 8)),
        hurtPos = 6, hurtFrames = 4,
        deathPos = 7, deathFrames = 4,
        type = CharacterType.PLAYER
    )),

    PRIEST(CharacterConfig(
        spriteRes = R.drawable.priest,
        idlePos = 0, idleFrames = 4,
        walkPos = 1, walkFrames = 8,
        attacks = listOf(CharacterAttack(2, 8), CharacterAttack(3, 8), CharacterAttack(5, 6), CharacterAttack(6, 6)),
        hurtPos = 8, hurtFrames = 4,
        deathPos = 9, deathFrames = 4,
        type = CharacterType.PLAYER
    )),

    SLIME(CharacterConfig(
        spriteRes = R.drawable.slime,
        idlePos = 0, idleFrames = 6,
        walkPos = 1, walkFrames = 6,
        attacks = listOf(CharacterAttack(2, 6), CharacterAttack(3, 12)),
        hurtPos = 4, hurtFrames = 4,
        deathPos = 5, deathFrames = 4,
        type = CharacterType.PLAYER
    )),

    SOLDIER(CharacterConfig(
        spriteRes = R.drawable.soldier,
        idlePos = 0, idleFrames = 6,
        walkPos = 1, walkFrames = 8,
        attacks = listOf(CharacterAttack(2, 6), CharacterAttack(3, 6), CharacterAttack(4, 8)),
        hurtPos = 5, hurtFrames = 4,
        deathPos = 6, deathFrames = 4,
        type = CharacterType.PLAYER
    )),

    SWORDSMAN(CharacterConfig(
        spriteRes = R.drawable.swordsman,
        idlePos = 0, idleFrames = 6,
        walkPos = 1, walkFrames = 8,
        attacks = listOf(CharacterAttack(2, 6), CharacterAttack(3, 14), CharacterAttack(4, 12)),
        hurtPos = 5, hurtFrames = 4,
        deathPos = 6, deathFrames = 4,
        type = CharacterType.PLAYER
    )),

    WIZARD(CharacterConfig(
        spriteRes = R.drawable.wizard,
        idlePos = 0, idleFrames = 4,
        walkPos = 1, walkFrames = 8,
        attacks = listOf(CharacterAttack(2, 14), CharacterAttack(3, 6), CharacterAttack(5, 12), CharacterAttack(6, 6)),
        hurtPos = 8, hurtFrames = 4,
        deathPos = 9, deathFrames = 4,
        type = CharacterType.PLAYER
    )),


    ARMORED_ORC(CharacterConfig(
        spriteRes = R.drawable.armored_orc,
        idlePos = 0, idleFrames = 6,
        walkPos = 1, walkFrames = 8,
        attacks = listOf(CharacterAttack(2, 6), CharacterAttack(3, 8), CharacterAttack(4, 8)),
        hurtPos = 6, hurtFrames = 4,
        deathPos = 7, deathFrames = 4,
        type = CharacterType.ENEMY
    )),

    ARMORED_SKELETON(CharacterConfig(
        spriteRes = R.drawable.armored_skeleton,
        idlePos = 0, idleFrames = 6,
        walkPos = 1, walkFrames = 8,
        attacks = listOf(CharacterAttack(2, 8), CharacterAttack(3, 8)),
        hurtPos = 4, hurtFrames = 4,
        deathPos = 5, deathFrames = 4,
        type = CharacterType.ENEMY
    )),

    ELITE_ORC(CharacterConfig(
        spriteRes = R.drawable.elite_orc,
        idlePos = 0, idleFrames = 6,
        walkPos = 1, walkFrames = 8,
        attacks = listOf(CharacterAttack(2, 6), CharacterAttack(3, 10), CharacterAttack(4, 8)),
        hurtPos = 5, hurtFrames = 4,
        deathPos = 6, deathFrames = 4,
        type = CharacterType.ENEMY
    )),

    GREATSWORD_SKELETON(CharacterConfig(
        spriteRes = R.drawable.greatsword_skeleton,
        idlePos = 0, idleFrames = 6,
        walkPos = 1, walkFrames = 8,
        attacks = listOf(CharacterAttack(2, 8), CharacterAttack(3, 12), CharacterAttack(4, 8)),
        hurtPos = 5, hurtFrames = 4,
        deathPos = 6, deathFrames = 4,
        type = CharacterType.ENEMY
    )),

    ORC(CharacterConfig(
        spriteRes = R.drawable.orc,
        idlePos = 0, idleFrames = 6,
        walkPos = 1, walkFrames = 8,
        attacks = listOf(CharacterAttack(2, 6), CharacterAttack(3, 6)),
        hurtPos = 4, hurtFrames = 4,
        deathPos = 5, deathFrames = 4,
        type = CharacterType.ENEMY
    )),

    ORC_RIDER(CharacterConfig(
        spriteRes = R.drawable.orc_rider,
        idlePos = 0, idleFrames = 6,
        walkPos = 1, walkFrames = 8,
        attacks = listOf(CharacterAttack(2, 8), CharacterAttack(3, 8), CharacterAttack(4, 10)),
        hurtPos = 6, hurtFrames = 4,
        deathPos = 7, deathFrames = 4,
        type = CharacterType.ENEMY
    )),

    SKELETON(CharacterConfig(
        spriteRes = R.drawable.skeleton,
        idlePos = 0, idleFrames = 6,
        walkPos = 1, walkFrames = 8,
        attacks = listOf(CharacterAttack(2, 6), CharacterAttack(3, 6)),
        hurtPos = 5, hurtFrames = 4,
        deathPos = 6, deathFrames = 4,
        type = CharacterType.ENEMY
    )),

    SKELETON_ARCHER(CharacterConfig(
        spriteRes = R.drawable.skeleton_archer,
        idlePos = 0, idleFrames = 6,
        walkPos = 1, walkFrames = 8,
        attacks = listOf(CharacterAttack(2, 8)),
        hurtPos = 3, hurtFrames = 4,
        deathPos = 4, deathFrames = 4,
        type = CharacterType.ENEMY
    )),

    WEREWOLF(CharacterConfig(
        spriteRes = R.drawable.werewolf,
        idlePos = 0, idleFrames = 6,
        walkPos = 1, walkFrames = 8,
        attacks = listOf(CharacterAttack(2, 8), CharacterAttack(3, 12)),
        hurtPos = 4, hurtFrames = 4,
        deathPos = 5, deathFrames = 4,
        type = CharacterType.ENEMY
    )),

    WEREBEAR(CharacterConfig(
        spriteRes = R.drawable.werebear,
        idlePos = 0, idleFrames = 6,
        walkPos = 1, walkFrames = 8,
        attacks = listOf(CharacterAttack(2, 8), CharacterAttack(3, 12), CharacterAttack(4, 8)),
        hurtPos = 5, hurtFrames = 4,
        deathPos = 6, deathFrames = 4,
        type = CharacterType.ENEMY
    ));

    companion object {
        val PLAYERS = entries.filter { it.config.type == CharacterType.PLAYER }
        val ENEMIES = entries.filter { it.config.type == CharacterType.ENEMY }
    }
}