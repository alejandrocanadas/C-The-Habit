package com.example.cthehabit.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.cthehabit.ui.AuthViewModel
import com.example.cthehabit.ui.screens.*

@Composable
fun AppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val startRoute = remember {
        if (authViewModel.isLoggedIn.value) "main" else "Inicio"
    }

    NavHost(
        navController = navController,
        startDestination = startRoute
    ){

        composable("Inicio") {
            PantallaInicio(
                onEmpezarClick = { navController.navigate("registro")},
                onLoginClick = { navController.navigate("login")}
            )
        }

        composable("registro") {
            PantallaRegistro(
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onLogin = { navController.navigate("login") },
                onRegistroExitoso = { navController.navigate("encuesta") {
                    popUpTo("Inicio") { inclusive = true }
                }}
            )
        }

        composable("login") {
            PantallaLogin(
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onRegistro = { navController.navigate("registro") },
                onLoginExitoso = { navController.navigate("main") {
                    popUpTo("Inicio") { inclusive = true }
                }}
            )
        }

        composable("main") {
            PantallaPrincipal(
                navController = navController, // agregado
                authViewModel = authViewModel,
                onGraficas24h = { navController.navigate("graficas/24h") },
                onGraficas7d = { navController.navigate("graficas/7d") }
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
                            navController.navigate("main") { popUpTo("encuesta") { inclusive = true } }
                        },
                        onError = {
                            navController.navigate("main") { popUpTo("encuesta") { inclusive = true } }
                        }
                    )
                }
            )
        }

        // --- Ruta de GameScreen ---
        composable("game/{horas}") { backStackEntry ->
            val horas = backStackEntry.arguments?.getString("horas")?.toInt() ?: 0
            GameScreen(
                horas = horas,
                onSiguienteClick = {
                    navController.navigate("main") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }
    }
}