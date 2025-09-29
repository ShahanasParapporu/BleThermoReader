package com.takniatech.contec.presentation.profile

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takniatech.contec.ContecMedicalApplication
import com.takniatech.contec.data.model.TemperatureReading
import com.takniatech.contec.data.model.User
import com.takniatech.contec.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.runtime.*
import com.takniatech.contec.data.sdk.ContecSdkManager
import com.takniatech.contec.domain.repository.TemperatureRepository
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val temperatureRepository: TemperatureRepository,
    private val sdkManager: ContecSdkManager
) : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _realtimeReadings = MutableStateFlow<List<TemperatureReading>>(emptyList())
    val realtimeReadings: StateFlow<List<TemperatureReading>> = _realtimeReadings.asStateFlow()

    private val _historyReadings = MutableStateFlow<List<TemperatureReading>>(emptyList())
    val historyReadings: StateFlow<List<TemperatureReading>> = _historyReadings.asStateFlow()

    // flags from SDK
    val timeSynced: StateFlow<Boolean> = sdkManager.timeSynced
    val storageTotal: StateFlow<Int> = sdkManager.storageTotal

    fun refreshData(userId: Int) {
        sdkManager.setCurrentUser(userId)

        viewModelScope.launch {
            _user.value = userRepository.getUserById(userId)
        }

        // Realtime readings
        viewModelScope.launch {
            temperatureRepository.getRealtimeReadingsFlow(userId).collectLatest {
                _realtimeReadings.value = it
            }
        }

        // History readings
        viewModelScope.launch {
            temperatureRepository.getHistoryReadingsFlow(userId).collectLatest {
                _historyReadings.value = it
            }
        }
    }

}



