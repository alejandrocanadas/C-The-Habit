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
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "C The Habit",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = negro
            )

            Spacer(modifier = Modifier.height(60.dp))

            Text(
                text = currentQuestion.text,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = blanco,
                textAlign = TextAlign.Center,
                lineHeight = 34.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(36.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(18.dp)
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

            Spacer(modifier = Modifier.weight(1f))

            if (currentQuestion.multipleSelection) {
                Text(
                    text = "Selecciona mínimo ${currentQuestion.minSelections} opciones",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    respuestas[currentQuestionIndex] = selectedOptions.toList()

                    if (currentQuestionIndex < questions.size - 1) {
                        currentQuestionIndex++
                    } else {
                        respuestas[currentQuestionIndex] = selectedOptions.toList()
                        onFinish(respuestas.toMap())  // ← envía todas las respuestas
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
                    text = if (currentQuestionIndex < questions.size - 1) "Siguiente" else "Finalizar",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
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
                modifier = Modifier.padding(12.dp)
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