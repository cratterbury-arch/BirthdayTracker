package com.chris.birthdaytracker

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun BirthdayDateField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Date of birth"
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            val digitsOnly = newValue.filter { it.isDigit() }.take(8)
            onValueChange(digitsOnly)
        },
        label = { Text(label) },
        visualTransformation = DateVisualTransformation(),
        singleLine = true
    )
}

/**
 * Converts ######## -> ##/##/####
 */
class DateVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val input = text.text

        val formatted = buildString {
            for (i in input.indices) {
                append(input[i])
                if (i == 1 || i == 3) append('/')
            }
        }

        val offsetMapping = object : OffsetMapping {

            override fun originalToTransformed(offset: Int): Int =
                when {
                    offset <= 1 -> offset
                    offset <= 3 -> offset + 1
                    offset <= 8 -> offset + 2
                    else -> formatted.length
                }

            override fun transformedToOriginal(offset: Int): Int =
                when {
                    offset <= 2 -> offset
                    offset <= 5 -> offset - 1
                    offset <= 10 -> offset - 2
                    else -> input.length
                }
        }

        return TransformedText(
            AnnotatedString(formatted),
            offsetMapping
        )
    }
}
