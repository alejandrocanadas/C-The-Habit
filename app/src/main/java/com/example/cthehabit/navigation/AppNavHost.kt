package com.example.cthehabit.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.cthehabit.ui.PantallaInicio
import com.example.cthehabit.ui.PantallaRegistro
import com.example.cthehabit.ui.PantallaLogin
import com.example.cthehabit.ui.PantallaPrincipal

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "Inicio"
    ){
        composable("Inicio"){
            PantallaInicio(
                onEmpezarClick = { navController.navigate("registro")},
                onLoginClick = {navController.navigate("login")}
            )
        }

        composable("registro"){
            PantallaRegistro(
                onBack = { navController.popBackStack()},
                onLogin = {navController.navigate("login")},
                onRegistroExitoso = {
                    navController.navigate("main"){
                        popUpTo("Inicio") {inclusive = true}
                    }
                }
            )
        }

        composable("login"){
            PantallaLogin(
                onBack = { navController.popBackStack()},
                onRegistro = {navController.navigate("registro")},
                onLoginExitoso = {
                    navController.navigate("main"){
                        popUpTo("Inicio") {inclusive = true}
                    }
                }
            )
        }

        composable("main"){
            PantallaPrincipal()
        }
    }
}



