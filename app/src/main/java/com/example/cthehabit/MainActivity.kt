package com.example.cthehabit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.cthehabit.data.local.SessionManager
import com.example.cthehabit.navigation.AppNavHost
import com.example.cthehabit.ui.AuthViewModel
import com.example.cthehabit.ui.AuthViewModelFactory
import com.example.cthehabit.ui.theme.CTheHabitTheme
import com.example.cthehabit.services.SyncAppsUsageWorker
import com.example.cthehabit.viewmodels.AppUsageViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Programar el worker que sincroniza el uso de apps
        SyncAppsUsageWorker.schedule(this)

        // Activar modo edge-to-edge
        enableEdgeToEdge()

        // Instanciar SessionManager y AuthViewModelFactory
        val sessionManager = SessionManager(this)
        val authFactory = AuthViewModelFactory(sessionManager)

        setContent {
            CTheHabitTheme {
                val navController = rememberNavController()

                // Instanciar viewmodels
                val authViewModel: AuthViewModel = viewModel(factory = authFactory)
                val usageViewModel: AppUsageViewModel = viewModel() // ✅ agregado

                val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(Modifier.padding(innerPadding)) {
                        if (isLoggedIn == null) {
                            // Loading inicial mientras revisamos token
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        } else {
                            // Pasamos ambos viewmodels al NavHost
                            AppNavHost(
                                navController = navController,
                                authViewModel = authViewModel,
                                usageViewModel = usageViewModel // ✅ FIX
                            )
                        }
                    }
                }
            }
        }
    }
}