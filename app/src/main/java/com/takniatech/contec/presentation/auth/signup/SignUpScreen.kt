package com.takniatech.contec.presentation.auth.signup

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.takniatech.contec.ui.theme.Montserrat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import coil.compose.rememberAsyncImagePainter
import com.takniatech.contec.ui.theme.Roboto
import com.takniatech.contec.R
import com.takniatech.contec.presentation.shared.components.ContecButton
import com.takniatech.contec.presentation.shared.components.ContecTextField
import com.takniatech.contec.ui.theme.ContecColors

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    viewModel: SignUpViewModel,
    onSignUpSuccess: (Int) -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.onProfileImageSelected(it) }
    }
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(horizontal = 24.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(36.dp))
                // Profile Image
                Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                    Image(
                        painter = if (viewModel.profileImageUri == null)
                            painterResource(R.drawable.welcomeimg)
                        else rememberAsyncImagePainter(viewModel.profileImageUri),
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-4).dp, y = (-8).dp)
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .clickable { launcher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Upload Image",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(5.dp))
            }

            item {
                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        lineHeight = 28.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ContecTextField(
                        value = viewModel.email,
                        onValueChange = { viewModel.onEmailChanged(it) },
                        label = "Email",
                        hint = "Enter your email",
                        isError = viewModel.emailError,
                        supportingText = viewModel.emailErrorMessage ,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    //var passwordFocused by remember { mutableStateOf(false) }
                    ContecTextField(
                        value = viewModel.password,
                        onValueChange = { viewModel.password = it },
                        label = "Password",
                        hint = "Enter password",
                        isPassword = !viewModel.isPasswordVisible,
                        trailingIcon = {
                            IconButton(onClick = { viewModel.togglePasswordVisibility() }) {
                                Icon(
                                    imageVector = if (viewModel.isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = ContecColors.AdaptiveText
                                )
                            }
                        },
                        isError = viewModel.passwordError,
                        supportingText = viewModel.passwordErrorMessage,
                        singleLine = true,
                    )

                    ContecTextField(
                        value = viewModel.confirmPassword,
                        onValueChange = { viewModel.confirmPassword = it },
                        label = "Confirm Password",
                        hint = "Re-enter password",
                        isPassword = !viewModel.isConfirmPasswordVisible,
                        trailingIcon = {
                            IconButton(onClick = { viewModel.toggleConfirmPasswordVisibility() }) {
                                Icon(
                                    imageVector = if (viewModel.isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = ContecColors.AdaptiveText

                                )
                            }
                        },
                        isError = viewModel.confirmPasswordError,
                        supportingText = viewModel.confirmPasswordErrorMessage,
                        singleLine = true
                    )

                    ContecTextField(
                        value = viewModel.name,
                        onValueChange = { viewModel.name = it },
                        label = "Full Name",
                        hint = "Enter your full name",
                        singleLine = true
                    )

                    var dobFocused by remember { mutableStateOf(false) }
                    ContecTextField(
                        value = viewModel.dateOfBirth,
                        onValueChange = { viewModel.dateOfBirth = it },
                        label = "Date of Birth",
                        hint = "DD-MM-YYYY",
                        isError = viewModel.dobError,
                        supportingText = if (!dobFocused) viewModel.dobErrorMessage else "",
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.onFocusChanged { focusState ->
                            if (dobFocused && !focusState.isFocused) {
                                viewModel.validateDob()
                            }
                            dobFocused = focusState.isFocused
                        }
                    )

                    val genderOptions = listOf("Male", "Female", "Other", "Prefer not to say")
                    var expanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = viewModel.gender ?: "",
                            onValueChange = {},
                            readOnly = true,
                            textStyle = TextStyle(
                                color = ContecColors.AdaptiveText
                            ),
                            label = { Text("Gender",color = ContecColors.AdaptiveText.copy(alpha = 0.7f)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            genderOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option,color = ContecColors.AdaptiveText.copy(alpha = 0.7f),) },
                                    onClick = {
                                        viewModel.gender = option
                                        expanded = false
                                    }
                                )
                            }
                        }

                    }


                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        ContecTextField(
                            value = viewModel.weight,
                            onValueChange = { input ->
                                viewModel.weight = input.filter { it.isDigit() } },
                            label = "Weight",
                            hint = "kg",
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        ContecTextField(
                            value = viewModel.height,
                            onValueChange = { input ->
                                viewModel.height = input.filter { it.isDigit() } },
                            label = "Height",
                            hint = "cm",
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
            }

            item {
                if (viewModel.errorMessage.isNotBlank()) {
                    Text(
                        text = viewModel.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            item {
                ContecButton(
                    onClick = { viewModel.onSignUpClicked(onSignUpSuccess) },
                    text = "Create Account",
                    enabled = viewModel.isFormValid(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    gradient = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    ),
                    isOutlined = false
                )
            }

            item {
                TextButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 30.dp)
                ) {
                    Text(
                        text = "Already have an account? Sign In",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = Roboto,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}



