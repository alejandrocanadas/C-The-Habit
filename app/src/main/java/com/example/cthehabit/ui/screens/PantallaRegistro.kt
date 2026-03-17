package com.example.cthehabit.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.cthehabit.ui.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun PantallaRegistro(
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onLogin: () -> Unit,
    onRegistroExitoso: () -> Unit
) {
    var name by remember { mutableStateOf("") } // opcional
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Crear cuenta", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = name, onValueChange = { name = it },
            label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("Correo electrónico") }, modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(error ?: "", color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                isLoading = true
                error = null
                scope.launch {
                    authViewModel.register(
                        email,
                        password,
                        onSuccess = { isLoading = false; onRegistroExitoso() },
                        onError = { msg -> isLoading = false; error = msg }
                    )
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLoading) "Creando cuenta..." else "Crear cuenta")
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "¿Ya tienes cuenta? Inicia sesión",
            modifier = Modifier.clickable { onLogin() },
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(16.dp))

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Volver")
        }
    }
}