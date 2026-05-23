package com.example.cthehabit.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cthehabit.R
import com.example.cthehabit.ui.AuthViewModel
import com.example.cthehabit.ui.theme.PixelTextField
import com.example.cthehabit.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun PantallaRegistro(
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onLogin: () -> Unit,
    onRegistroExitoso: () -> Unit
) {
    var name      by remember { mutableStateOf("") }
    var email     by remember { mutableStateOf("") }
    var password  by remember { mutableStateOf("") }
    var error     by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val camposLlenos = listOf(name, email, password).count { it.isNotBlank() }
    val progressFraction = camposLlenos / 3f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PixelBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.c),
                    fontSize = 26.sp,
                    style = MaterialTheme.typography.titleLarge,
                    color = PixelWhite
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.the_habit),
                    style = MaterialTheme.typography.labelSmall,
                    color = SkyBlue,
                    letterSpacing = 3.sp
                )
            }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = NavyBlue, thickness = 1.dp)
            Spacer(Modifier.height(24.dp))


            Text(
                text = stringResource(R.string.crear_cuenta),
                style = MaterialTheme.typography.titleLarge,
                fontSize = 28.sp,
                color = Cyan,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(32.dp))

            PixelTextField(
                value = name,
                onValueChange = { name = it },
                label = stringResource(R.string.nombre)
            )

            Spacer(Modifier.height(20.dp))

            PixelTextField(
                value = email,
                onValueChange = { email = it },
                label = stringResource(R.string.correo_electronico),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(Modifier.height(20.dp))

            PixelTextField(
                value = password,
                onValueChange = { password = it },
                label = stringResource(R.string.contrasena),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(24.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "PROGRESO DE REGISTRO",
                    style = MaterialTheme.typography.labelSmall,
                    color = SkyBlue,
                    fontSize = 9.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "$camposLlenos/3",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (camposLlenos == 3) Cyan else SkyBlue,
                    fontSize = 9.sp
                )
            }
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier.fillMaxWidth().height(3.dp),
                color = Cyan,
                trackColor = NavyBlue.copy(alpha = 0.3f),
                strokeCap = StrokeCap.Square
            )

            Spacer(Modifier.height(24.dp))


            Button(
                onClick = {
                    isLoading = true
                    error = null
                    scope.launch {
                        authViewModel.register(
                            email, password,
                            onSuccess = { isLoading = false; onRegistroExitoso() },
                            onError   = { msg -> isLoading = false; error = msg }
                        )
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor         = PixelSurface,
                    contentColor           = PixelWhite,
                    disabledContainerColor = PixelSurface.copy(alpha = 0.5f),
                    disabledContentColor   = PixelWhite.copy(alpha = 0.4f)
                ),
                border = BorderStroke(1.dp, NavyBlue),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Cyan,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.crear_cuenta),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.ya_tienes_cuenta),
                style = MaterialTheme.typography.labelSmall,
                color = Cyan,
                fontSize = 10.sp,
                modifier = Modifier.clickable { onLogin() }
            )
        }
    }
}