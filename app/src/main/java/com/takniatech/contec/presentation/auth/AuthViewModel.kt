package com.takniatech.contec.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takniatech.contec.data.local.AuthPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authPrefs: AuthPreferences
) : ViewModel() {

    val userId: StateFlow<Int?> = authPrefs.userId.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val isLoggedIn: StateFlow<Boolean> = authPrefs.isLoggedIn.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun saveUserId(userId: Int) = viewModelScope.launch {
        authPrefs.saveUserId(userId)
    }

    fun clearUserId() = viewModelScope.launch {
        authPrefs.clearUserId()
    }
}
