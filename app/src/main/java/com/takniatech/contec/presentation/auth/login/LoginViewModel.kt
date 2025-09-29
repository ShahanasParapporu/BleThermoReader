package com.takniatech.contec.presentation.auth.login

import android.util.Patterns
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.takniatech.contec.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.compose.runtime.*
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isPasswordVisible by mutableStateOf(false)
    var errorMessage by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var emailError by mutableStateOf(false)
    var emailErrorMessage by mutableStateOf<String?>(null)

    fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
    }

    fun isFormValid(): Boolean {
        return email.isNotBlank() && !emailError &&
                password.isNotBlank()
    }

    fun validateEmailFormat() {
        emailError = email.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()
        emailErrorMessage = if (emailError) "Invalid email address" else null
    }

    fun onLoginClicked(
        onSuccess: (Int) -> Unit
    ) {
        if (!isFormValid()) {
            errorMessage = "Please enter valid email and password"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = ""

            try {
                val emailExists = userRepository.isEmailRegistered(email)

                if (!emailExists) {
                    errorMessage = "User not registered, create account first"
                    isLoading = false
                    return@launch
                }

                val user = userRepository.getUser(email, password)
                isLoading = false

                if (user != null) {
                    onSuccess(user.id)
                } else {
                    errorMessage = "Invalid email or password"
                }
            } catch (e: Exception) {
                isLoading = false
                errorMessage = e.message ?: "Login failed due to a system error"
            }
        }
    }
}

