package com.example.cthehabit

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.cthehabit.data.local.SessionManager
import com.example.cthehabit.navigation.AppNavHost
import com.example.cthehabit.services.NotificationHelper
import com.example.cthehabit.services.SyncAppsUsageWorker
import com.example.cthehabit.services.UsageMonitorService
import com.example.cthehabit.ui.AuthViewModel
import com.example.cthehabit.ui.AuthViewModelFactory
import com.example.cthehabit.ui.theme.CTheHabitTheme
import com.example.cthehabit.viewmodels.AppUsageViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }


        NotificationHelper.createChannel(this)

        UsageMonitorService.start(this)

        SyncAppsUsageWorker.schedule(this)


        enableEdgeToEdge()

        val sessionManager = SessionManager(this)
        val authFactory = AuthViewModelFactory(sessionManager)

        setContent {
            CTheHabitTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel(factory = authFactory)
                val usageViewModel: AppUsageViewModel = viewModel()
                val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        if (isLoggedIn == null) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        } else {
                            AppNavHost(
                                navController = navController,
                                authViewModel = authViewModel,
                                usageViewModel = usageViewModel
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}