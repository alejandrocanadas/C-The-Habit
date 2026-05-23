package com.example.cthehabit.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cthehabit.R
import com.example.cthehabit.ui.theme.*

@Composable
fun PantallaInicio(
    onEmpezarClick: () -> Unit,
    onLoginClick: () -> Unit
) {
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


            Text(
                text = stringResource(R.string.c),
                fontSize = 120.sp,
                style = MaterialTheme.typography.titleLarge,
                color = PixelWhite,
                textAlign = TextAlign.Center,
                lineHeight = 110.sp
            )


            HorizontalDivider(
                color = Cyan,
                thickness = 2.dp,
                modifier = Modifier.padding(horizontal = 48.dp)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.the_habit),
                fontSize = 32.sp,
                style = MaterialTheme.typography.titleLarge,
                color = PixelWhite,
                textAlign = TextAlign.Center,
                letterSpacing = 6.sp
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "BUILD YOUR LEGEND",
                style = MaterialTheme.typography.labelSmall,
                color = Cyan,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(48.dp))


            Spacer(Modifier.height(48.dp))


            Button(
                onClick = onEmpezarClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PixelSurface,
                    contentColor   = PixelWhite
                ),
                border = BorderStroke(1.dp, NavyBlue),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = PixelWhite
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.empezar),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(Modifier.height(12.dp))


            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PixelBackground,
                    contentColor   = SkyBlue
                ),
                border = BorderStroke(1.dp, NavyBlue),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = SkyBlue
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.inicia_sesion),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}