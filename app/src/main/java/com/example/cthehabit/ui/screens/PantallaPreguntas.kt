package com.example.cthehabit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cthehabit.utils.questions
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.stringResource
import com.example.cthehabit.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PantallaPreguntas(onFinish: (Map<Int, List<String>>) -> Unit) {
    var currentQuestionIndex by remember { mutableStateOf(0) }
    val respuestas = remember { mutableStateMapOf<Int, List<String>>() }

    val currentQuestion = questions[currentQuestionIndex]

    val selectedOptions = remember(currentQuestionIndex) {
        mutableStateListOf<String>().apply {
            addAll(respuestas[currentQuestionIndex] ?: emptyList())
        }
    }

    val puedeContinuar = if (currentQuestion.multipleSelection) {
        selectedOptions.size >= currentQuestion.minSelections
    } else {
        selectedOptions.isNotEmpty()
    }

    val scrollState = rememberScrollState()

    val fondoAzul = Color(0xFF3F8EFC)
    val colorTarjeta = Color(0xFFACD7F6)
    val colorTarjetaSeleccionada = Color(0xFF7BBCEB)
    val negro = Color.Black
    val blanco = Color.White
    val colorBoton = Color(0xFF2567FF)

    Scaffold { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(fondoAzul)
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = stringResource(R.string.app_name),
                fontSize = 50.sp,
                fontWeight = FontWeight.ExtraBold,
                color = negro,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = currentQuestion.text,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = blanco,
                textAlign = TextAlign.Center,
                lineHeight = 34.sp,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(30.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                maxItemsInEachRow = 2
            ) {
                currentQuestion.options.forEach { option ->
                    OpcionCard(
                        text = option,
                        onClick = {
                            if (currentQuestion.multipleSelection) {
                                if (selectedOptions.contains(option)) {
                                    selectedOptions.remove(option)
                                } else {
                                    selectedOptions.add(option)
                                }
                            } else {
                                selectedOptions.clear()
                                selectedOptions.add(option)
                            }
                        },
                        colorTarjeta = colorTarjeta,
                        colorSeleccionada = colorTarjetaSeleccionada,
                        seleccionada = selectedOptions.contains(option)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (currentQuestion.multipleSelection) {
                Text(
                    text = stringResource(R.string.selecciona_minimo, currentQuestion.minSelections),
                    color = Color.White,
                    fontSize = 14.sp,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    respuestas[currentQuestionIndex] = selectedOptions.toList()

                    if (currentQuestionIndex < questions.size - 1) {
                        currentQuestionIndex++
                    } else {
                        onFinish(respuestas.toMap())
                    }
                },
                enabled = puedeContinuar,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorBoton,
                    disabledContainerColor = Color.Gray
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text =
                        if (currentQuestionIndex < questions.size - 1)
                            stringResource(R.string.siguiente)
                        else
                            stringResource(R.string.finalizar),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun OpcionCard(
    text: String,
    onClick: () -> Unit,
    colorTarjeta: Color,
    colorSeleccionada: Color,
    seleccionada: Boolean
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(140.dp)
            .padding(8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (seleccionada) colorSeleccionada else colorTarjeta
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                lineHeight = 22.sp,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewPantallaPreguntas() {
    PantallaPreguntas(
        onFinish = { _ ->}
    )
}