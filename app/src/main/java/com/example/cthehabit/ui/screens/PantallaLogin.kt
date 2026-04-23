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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.cthehabit.R

@Composable
fun PantallaLogin(
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onRegistro: () -> Unit,
    onLoginExitoso: () -> Unit
) {
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
        Text(text = stringResource(R.string.inicia_sesion), style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text(stringResource(R.string.correo_electronico), style = MaterialTheme.typography.bodyLarge) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text(stringResource(R.string.contrasena), style = MaterialTheme.typography.bodyLarge)},
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
                    authViewModel.login(
                        email,
                        password,
                        onSuccess = { isLoading = false; onLoginExitoso() },
                        onError = { msg -> isLoading = false; error = msg }
                    )
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text =
                    if (isLoading)
                        stringResource(R.string.ingresando)
                    else
                        stringResource(R.string.iniciar_sesion),
                    style = MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.no_tienes_cuenta),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.clickable { onRegistro() },
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(16.dp))

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.volver), style = MaterialTheme.typography.bodyLarge)
        }
    }
}
