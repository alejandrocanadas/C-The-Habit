package com.example.cthehabit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.example.cthehabit.R

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

        Text(stringResource(R.string.c), fontSize = 130.sp)

        Text(stringResource(R.string.the_habit), fontSize = 50.sp)
        Spacer(Modifier.height(16.dp))

        Spacer(Modifier.height(40.dp))

        Button(onClick = onEmpezarClick, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.empezar))
        }

        Spacer(Modifier.height(16.dp))

        Button(onClick = onLoginClick, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.inicia_sesion))
        }
    }
}