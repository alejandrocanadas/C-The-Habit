package com.example.cthehabit.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PantallaInicio(
    onEmpezarClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("THE HABIT")
        Spacer(Modifier.height(16.dp))
        Text("CRUSH THE HABIT")

        Spacer(Modifier.height(40.dp))

        Button(onClick = onEmpezarClick, modifier = Modifier.fillMaxWidth()) {
            Text("Empezar")
        }

        Spacer(Modifier.height(16.dp))

        Button(onClick = onLoginClick, modifier = Modifier.fillMaxWidth()) {
            Text("Inicia Sesión")
        }
    }
}