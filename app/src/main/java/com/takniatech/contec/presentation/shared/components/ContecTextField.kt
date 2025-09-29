package com.takniatech.contec.presentation.shared.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.takniatech.contec.ui.theme.ContecColors
import com.takniatech.contec.ui.theme.Roboto


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContecTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    isError: Boolean = false,
    supportingText: String? = null,
    singleLine: Boolean = true,
    hint: String? = null,
    leadingIcon: (@Composable (() -> Unit))? = null,
    trailingIcon: (@Composable (() -> Unit))? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                label,
                color = if (isError) MaterialTheme.colorScheme.error
                else ContecColors.AdaptiveText.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = Roboto)
            )
        },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = singleLine,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = ContecColors.AdaptiveText,
            unfocusedTextColor = ContecColors.AdaptiveText.copy(alpha = 0.7f),
            focusedBorderColor = ContecColors.AdaptiveText,
            errorTextColor = MaterialTheme.colorScheme.error,
            unfocusedBorderColor = ContecColors.AdaptiveText.copy(alpha = 0.2f),
            focusedLabelColor = ContecColors.AdaptiveText,
            cursorColor = ContecColors.AdaptiveText,
            errorCursorColor = MaterialTheme.colorScheme.error,
            focusedPlaceholderColor = ContecColors.AdaptiveText.copy(alpha = 0.5f),
            unfocusedPlaceholderColor = ContecColors.AdaptiveText.copy(alpha = 0.5f)
        ),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        isError = isError,
        supportingText = supportingText?.let {
            { Text(it, color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), fontFamily = Roboto) }
        },
        placeholder = hint?.let {
            { Text(it, color = ContecColors.AdaptiveText.copy(alpha = 0.7f), fontFamily = Roboto) }
        },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        keyboardOptions = keyboardOptions,
    )
}
