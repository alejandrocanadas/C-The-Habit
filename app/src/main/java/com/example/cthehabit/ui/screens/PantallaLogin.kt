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
fun PantallaLogin(
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onRegistro: () -> Unit,
    onLoginExitoso: () -> Unit
) {
    var email     by remember { mutableStateOf("") }
    var password  by remember { mutableStateOf("") }
    var error     by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

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
                text = stringResource(R.string.inicia_sesion),
                style = MaterialTheme.typography.titleLarge,
                fontSize = 28.sp,
                color = Cyan,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(32.dp))


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



            Spacer(Modifier.height(32.dp))


            Button(
                onClick = {
                    isLoading = true
                    error = null
                    scope.launch {
                        authViewModel.login(
                            email, password,
                            onSuccess = { isLoading = false; onLoginExitoso() },
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
                        text = stringResource(R.string.iniciar_sesion),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            HorizontalDivider(color = NavyBlue, thickness = 1.dp)
            Spacer(Modifier.height(16.dp))


            Text(
                text = "RACHA ANTERIOR",
                style = MaterialTheme.typography.labelSmall,
                color = NavyBlue,
                fontSize = 9.sp,
                letterSpacing = 2.sp
            )
            Text(
                text = "---",
                style = MaterialTheme.typography.titleLarge,
                fontSize = 22.sp,
                color = MidPurple
            )
            Text(
                text = "DÍAS CONSECUTIVOS",
                style = MaterialTheme.typography.labelSmall,
                color = NavyBlue,
                fontSize = 9.sp,
                letterSpacing = 1.sp
            )

            Spacer(Modifier.height(20.dp))


            Text(
                text = stringResource(R.string.no_tienes_cuenta),
                style = MaterialTheme.typography.labelSmall,
                color = Cyan,
                fontSize = 10.sp,
                modifier = Modifier.clickable { onRegistro() }
            )
        }
    }
}