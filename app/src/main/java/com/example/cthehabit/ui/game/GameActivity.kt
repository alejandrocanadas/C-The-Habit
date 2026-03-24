package com.example.cthehabit.ui.game

import android.app.Activity
import android.os.Bundle

class GameActivity : Activity() {

    private lateinit var gameView: GameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Obtenemos los parámetros que vienen del CharacterSelect
        val horas = intent.getIntExtra("horas_redes", 0)  // quemamos horas aquí mismo
        val playerIndex = intent.getIntExtra("playerIndex", 0)
        val enemyIndex = intent.getIntExtra("enemyIndex", 0)

        // Creamos el GameView con los personajes seleccionados
        gameView = GameView(this, horas, playerIndex, enemyIndex)

        // Asignamos el GameView como contenido de la actividad
        setContentView(gameView)
    }

    override fun onResume() {
        super.onResume()
        gameView.resume()
    }

    override fun onPause() {
        super.onPause()
        gameView.pause()
    }
}