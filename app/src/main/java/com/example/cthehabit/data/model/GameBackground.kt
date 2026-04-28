package com.example.cthehabit.data.model

import androidx.annotation.DrawableRes
import com.example.cthehabit.R

data class GameBackground(
    val name: String,
    @DrawableRes val drawableRes: Int,
    val unlockLevel: Int
) {
    companion object {
        val ALL = listOf(
            GameBackground("Monte Majin", R.drawable.zfall_night, unlockLevel = 1),
            GameBackground("Estatua",       R.drawable.statueback,          unlockLevel = 3),
            GameBackground("Pasillo del Castillo",       R.drawable.hallback,          unlockLevel = 6),
            GameBackground("Arbol Embrujado",       R.drawable.treeback,          unlockLevel = 9),
            GameBackground("Tierra Apocalipsis",       R.drawable.wastelandback,          unlockLevel = 12),
        )
    }
}