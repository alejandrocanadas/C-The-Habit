package com.example.cthehabit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.cthehabit.data.entity.AppUsage
import com.example.cthehabit.data.repositories.getUsageStats
import com.example.cthehabit.utils.hasUsageStatsPermission
import com.example.cthehabit.utils.requestUsagePermission

@Composable
fun PantallaPrincipal() {

    val context = LocalContext.current
    var apps by remember { mutableStateOf(listOf<AppUsage>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        if (!hasUsageStatsPermission(context)) {

            Text(
                text = "Debes conceder permiso de uso para obtener métricas"
            )

            Spacer(Modifier.height(12.dp))

            Button(onClick = {
                requestUsagePermission(context)
            }) {
                Text("Conceder permiso")
            }

        } else {

            Button(
                onClick = {
                    apps = getUsageStats(context)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Obtener métricas")
            }

            Spacer(Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                items(apps) { app ->

                    val minutes = app.timeInForeground / 1000 / 60
                    val pm = context.packageManager

                    val appName = try {
                        val appInfo = pm.getApplicationInfo(app.packageName, 0)
                        appInfo.loadLabel(pm).toString()
                    } catch (e: Exception) {
                        app.packageName
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {

                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            Text(text = appName)

                            Text(text = "$minutes min")
                        }
                    }
                }
            }
        }
    }
}