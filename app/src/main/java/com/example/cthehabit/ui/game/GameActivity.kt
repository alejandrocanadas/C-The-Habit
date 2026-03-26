package com.example.cthehabit.ui.game

import android.app.Activity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GameActivity : Activity() {

    private var gameView: GameView? = null // Cambiamos a nullable para esperar a Firebase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val horas = intent.getIntExtra("horas_redes", 0)
        val playerIndex = intent.getIntExtra("playerIndex", 0)
        val enemyIndex = intent.getIntExtra("enemyIndex", 0)

        // 1. Obtener el ID del usuario actual (el código encriptado)
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            // 2. Buscar en Firestore el nivel guardado
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    // Si el campo no existe, por defecto es 1
                    val nivelGuardado = document.getLong("currentLevel")?.toInt() ?: 1

                    // 3. Crear el juego con ese nivel
                    gameView = GameView(this, horas, playerIndex, enemyIndex, nivelGuardado)
                    setContentView(gameView)
                    gameView?.resume()
                }
                .addOnFailureListener {
                    // Si falla el internet, empezamos en nivel 1 por seguridad
                    gameView = GameView(this, horas, playerIndex, enemyIndex, 1)
                    setContentView(gameView)
                    gameView?.resume()
                }
        }
    }

    override fun onResume() {
        super.onResume()
        gameView?.resume()
    }

    override fun onPause() {
        super.onPause()
        gameView?.pause()
    }
}