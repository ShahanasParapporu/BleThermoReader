package com.takniatech.contec.presentation.auth.signup

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Patterns
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takniatech.contec.data.model.User
import com.takniatech.contec.domain.repository.UserRepository
import com.takniatech.contec.presentation.shared.utils.saveScaledImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val userRepository: UserRepository,private val app: Application
) : ViewModel() {
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    var name by mutableStateOf("")
    var dateOfBirth by mutableStateOf("")
    var gender by mutableStateOf("")
    var weight by mutableStateOf("")
    var height by mutableStateOf("")
    var profileImageUri by mutableStateOf<Uri?>(null)
        private set
    var profileImagePath by mutableStateOf<String?>(null)
        private set
    var isPasswordVisible by mutableStateOf(false)
    var isConfirmPasswordVisible by mutableStateOf(false)
    var errorMessage by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var dobError by mutableStateOf(false)
    var dobErrorMessage by mutableStateOf<String?>(null)

    val genderOptions = listOf("Male", "Female", "Other", "Prefer not to say")

    var emailError by mutableStateOf(false)
    var emailErrorMessage by mutableStateOf<String?>(null)
    private var emailCheckJob: Job? = null

    val passwordError: Boolean
        get() = password.isNotBlank() && !passwordPattern.matches(password)
    val passwordErrorMessage: String?
        get() = if (passwordError) "8-12 chars, upper, lower, number & special" else null

    val confirmPasswordError: Boolean
        get() = confirmPassword.isNotBlank() && confirmPassword != password
    val confirmPasswordErrorMessage: String?
        get() = if (confirmPasswordError) "Passwords don't match" else null
    private val passwordPattern =
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\$%^&+=!]).{8,12}\$".toRegex()
    private val dobPattern = "^([0-2][0-9]|(3)[0-1])/([0][1-9]|1[0-2])/([0-9]{4})\$".toRegex()

    fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
    }

    fun toggleConfirmPasswordVisibility() {
        isConfirmPasswordVisible = !isConfirmPasswordVisible
    }

    fun onProfileImageSelected(selectedUri: Uri) {
        //originalImageUri = selectedUri
        profileImageUri = selectedUri
        profileImagePath = app.saveScaledImage(selectedUri)
    }

    fun onGenderSelected(option: String) {
        gender = option
    }
    fun isFormValid(): Boolean {
        return email.isNotBlank() && !emailError &&
                password.isNotBlank() && !passwordError &&
                confirmPassword.isNotBlank() && !confirmPasswordError &&
                name.isNotBlank() &&
                dateOfBirth.isNotBlank() && !dobError &&
                gender.isNotBlank() &&
                weight.toFloatOrNull() != null &&
                height.toFloatOrNull() != null
    }

    fun onEmailChanged(newEmail: String) {
        email = newEmail
        emailError = false
        emailErrorMessage = null
        if (email.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = true
            emailErrorMessage = "Invalid email format"
            return
        }
        emailCheckJob?.cancel()
        emailCheckJob = viewModelScope.launch {
            delay(500)
            checkEmailExists()
        }
    }

    suspend fun checkEmailExists() {
        if (email.isBlank() || emailError) return

        val exists = userRepository.isEmailRegistered(email)
        if (exists) {
            emailError = true
            emailErrorMessage = "This email already exists. Use another email."
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun validateDob() {
        try {
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val dob = LocalDate.parse(dateOfBirth, formatter)
            val today = LocalDate.now()
            dobError = dob.isAfter(today) || dob.isEqual(today)
            dobErrorMessage = if (dobError) "Irrelevant Date of Birth" else null
        } catch (e: Exception) {
            dobError = true
            dobErrorMessage = "Invalid date format"
        }
    }


    // --- Sign Up Action ---
    fun onSignUpClicked(onSuccess: (Int) -> Unit) {
        if (!isFormValid()) {
            errorMessage = "Please correct the errors above"
            return
        }

        viewModelScope.launch {
            isLoading = true
            try {
                val user = User(
                    email = email,
                    password = password,
                    name = name,
                    dateOfBirth = dateOfBirth,
                    gender = gender,
                    weight = weight.toFloat(),
                    height = height.toFloat(),
                    profileImageUri = profileImagePath
                )
                val id = userRepository.insertUser(user)
                isLoading = false
                onSuccess(id.toInt())
            } catch (e: Exception) {
                isLoading = false
                errorMessage = e.message ?: "Sign up failed"
            }
        }
    }
}
