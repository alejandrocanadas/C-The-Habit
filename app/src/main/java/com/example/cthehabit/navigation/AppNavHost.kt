package com.example.cthehabit.navigation

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.cthehabit.ui.AuthViewModel
import com.example.cthehabit.ui.screens.*
import com.example.cthehabit.ui.game.GameActivity

@Composable
fun AppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val startRoute = remember {
        if (authViewModel.isLoggedIn.value) "main" else "Inicio"
    }

    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = startRoute
    ) {

        composable("Inicio") {
            PantallaInicio(
                onEmpezarClick = { navController.navigate("registro") },
                onLoginClick = { navController.navigate("login") }
            )
        }

        composable("registro") {
            PantallaRegistro(
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onLogin = { navController.navigate("login") },
                onRegistroExitoso = {
                    navController.navigate("encuesta") {
                        popUpTo("Inicio") { inclusive = true }
                    }
                }
            )
        }

        composable("login") {
            PantallaLogin(
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onRegistro = { navController.navigate("registro") },
                onLoginExitoso = {
                    navController.navigate("main") {
                        popUpTo("Inicio") { inclusive = true }
                    }
                }
            )
        }

        composable("main") {
            PantallaPrincipal(
                navController = navController,
                authViewModel = authViewModel,
                onGraficas24h = { navController.navigate("graficas/24h") },
                onGraficas7d = { navController.navigate("graficas/7d") },
                // NUEVO: ir a seleccionar personaje
                onJugarClick = { horas ->
                    navController.navigate("characterSelect/$horas")
                }
            )
        }

        composable("graficas/{tipo}") { backStackEntry ->
            val tipo = backStackEntry.arguments?.getString("tipo") ?: "24h"
            PantallaGraficas(tipo = tipo)
        }

        composable("encuesta") {
            PantallaInicialEncuesta(onContinuar = { navController.navigate("preguntas") })
        }

        composable("preguntas") {
            PantallaPreguntas(
                onFinish = { respuestas ->
                    authViewModel.saveQuestionnaire(
                        answers = respuestas,
                        onSuccess = {
                            navController.navigate("main") {
                                popUpTo("encuesta") {
                                    inclusive = true
                                }
                            }
                        },
                        onError = {
                            navController.navigate("main") {
                                popUpTo("encuesta") {
                                    inclusive = true
                                }
                            }
                        }
                    )
                }
            )
        }

        // --- Selección de personaje ---
        composable("characterSelect/{horas}") { backStackEntry ->
            val horas = backStackEntry.arguments?.getString("horas")?.toInt() ?: 0
            CharacterSelectScreen(
                horas = horas,
                onStartGame = { playerIndex, enemyIndex ->
                    // Abrir GameActivity pasando player y enemy seleccionados
                    val intent = Intent(context, GameActivity::class.java).apply {
                        putExtra("horas_redes", horas)
                        putExtra("playerIndex", playerIndex)
                        putExtra("enemyIndex", enemyIndex)
                    }
                    context.startActivity(intent)
                }
            )
        }

        // --- Ruta de GameScreen (opcional si quieres mantener algo de Compose) ---
        composable(
            route = "game/{horas}/{playerIndex}/{enemyIndex}"
        ) { backStackEntry ->
            val horas = backStackEntry.arguments?.getString("horas")?.toInt() ?: 0
            val playerIndex = backStackEntry.arguments?.getString("playerIndex")?.toInt() ?: 0
            val enemyIndex = backStackEntry.arguments?.getString("enemyIndex")?.toInt() ?: 0

            GameScreen(
                horas = horas,
                playerIndex = playerIndex,
                enemyIndex = enemyIndex,
                onSiguienteClick = {
                    navController.navigate("main") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }
    }
}