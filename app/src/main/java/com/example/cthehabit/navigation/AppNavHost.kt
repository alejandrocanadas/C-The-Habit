package com.example.cthehabit.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) "main" else "Inicio"
    ) {

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
                onFinish = {
                    navController.navigate("main") {
                        popUpTo("encuesta") { inclusive = true }
                    }
                }
            )
        }
    }
}