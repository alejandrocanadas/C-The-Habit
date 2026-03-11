package com.example.cthehabit.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import com.example.cthehabit.ui.screens.PantallaInicio
import com.example.cthehabit.ui.screens.PantallaLogin
import com.example.cthehabit.ui.screens.PantallaPrincipal
import com.example.cthehabit.ui.screens.PantallaRegistro
import com.example.cthehabit.ui.screens.PantallaGraficas

@Composable
fun AppNavHost(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = "Inicio"
    ){

        composable("Inicio"){
            PantallaInicio(
                onEmpezarClick = { navController.navigate("registro")},
                onLoginClick = { navController.navigate("login")}
            )
        }

        composable("registro"){
            PantallaRegistro(
                onBack = { navController.popBackStack() },
                onLogin = { navController.navigate("login") },
                onRegistroExitoso = {
                    navController.navigate("main"){
                        popUpTo("Inicio") { inclusive = true }
                    }
                }
            )
        }

        composable("login"){
            PantallaLogin(
                onBack = { navController.popBackStack() },
                onRegistro = { navController.navigate("registro") },
                onLoginExitoso = {
                    navController.navigate("main"){
                        popUpTo("Inicio") { inclusive = true }
                    }
                }
            )
        }

        composable("main"){
            PantallaPrincipal(
                onGraficas24h = {
                    navController.navigate("graficas/24h")
                },
                onGraficas7d = {
                    navController.navigate("graficas/7d")
                }
            )
        }

        composable("graficas/{tipo}") { backStackEntry ->

            val tipo = backStackEntry.arguments?.getString("tipo") ?: "24h"

            PantallaGraficas(tipo = tipo)
        }
    }
}