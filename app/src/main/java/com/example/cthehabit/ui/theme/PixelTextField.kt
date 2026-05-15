package com.example.cthehabit.ui.theme


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cthehabit.ui.theme.*

@Composable
fun PixelTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    var isFocused by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Label estático encima del campo
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isFocused) Cyan else SkyBlue,
            fontSize = 10.sp,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(6.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = PixelWhite,
                fontSize = 16.sp
            ),
            visualTransformation = visualTransformation,
            cursorBrush = SolidColor(Cyan),
            keyboardOptions = keyboardOptions,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused }
                .padding(bottom = 8.dp)
        )
        // Solo línea inferior — sin caja
        HorizontalDivider(
            color = if (isFocused) Cyan else NavyBlue,
            thickness = 1.dp
        )
    }
}