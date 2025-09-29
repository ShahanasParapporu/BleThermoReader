package com.takniatech.contec.presentation.scan

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takniatech.contec.data.local.AuthPreferences
import com.takniatech.contec.data.model.ContecDevice
import com.takniatech.contec.data.sdk.ContecSdkManager
import com.takniatech.contec.data.sdk.ContecStatus
import com.takniatech.contec.presentation.shared.utils.isBluetoothOn
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class ScanViewModel @Inject constructor(
    val sdkManager: ContecSdkManager,
    @ApplicationContext val context: Context,
    private val authPreferences: AuthPreferences
) : ViewModel() {
    private val _connectedDeviceAddress = MutableStateFlow<String?>(null)
    val connectedDeviceAddress: StateFlow<String?> = _connectedDeviceAddress.asStateFlow()
    val foundDevices: StateFlow<List<ContecDevice>> = sdkManager.foundDevices
    private val _isSearching = MutableStateFlow(false) // start as false
    val isSearching: StateFlow<Boolean> = _isSearching

    val searchError: StateFlow<Int?> = sdkManager.searchError
    //val connectStatus: StateFlow<Int?> = sdkManager.connectStatus
    val connectStatus: SharedFlow<Int?> = sdkManager.connectStatus
    private var latestConnectStatus: Int? = null

    private val _statusMessage = MutableStateFlow<String>("Ready to scan.")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()
    private var scanTimeoutJob: Job? = null
    private val SCAN_DURATION_MS = 28000L
    private val _bluetoothEnabled = MutableStateFlow(true) // assume true initially
    val bluetoothEnabled: StateFlow<Boolean> = _bluetoothEnabled.asStateFlow()


    init {
        viewModelScope.launch {
            sdkManager.searchError.collect { errorCode ->
                errorCode?.let {
                    val message = when (it) {
                        0 -> "Search Error: Device doesn't support BLE."
                        1 -> "Search Error: Bluetooth is not open."
                        else -> "Search Error: Code $it"
                    }
                    _statusMessage.value = message
                    Log.e(TAG, message)
                    sdkManager.stopSearch() // Stop search on error
                }
            }
        }

        // Observe connection status changes
        viewModelScope.launch {
            sdkManager.connectStatus.collect { status ->
                status?.let {
                    latestConnectStatus = it
                    val message = ContecStatus.getMessage(it)

                    _statusMessage.value = message
                    val logLevel = ContecStatus.getLogLevel(it)

                    when (logLevel) {
                        Log.ERROR -> Log.e(TAG, message)
                        Log.WARN  -> Log.w(TAG, message)
                        Log.INFO  -> Log.i(TAG, message)
                        Log.DEBUG -> Log.d(TAG, message)
                    }

                    when (it) {
                        ContecStatus.CONNECTED_SUCCESS -> {
                            _connectedDeviceAddress.value = sdkManager.connectedDeviceAddress
                            saveLastConnectedDevice(sdkManager.connectedDeviceAddress)
                        }
                        ContecStatus.ACTIVE_DISCONNECTION,
                        ContecStatus.ABNORMAL_DISCONNECTION -> {
                            _connectedDeviceAddress.value = null
                        }
                    }
                }
            }
        }
        reconnectLastDevice()
    }

    private fun reconnectLastDevice() {
        viewModelScope.launch {
            authPreferences.lastDeviceAddress.collect { lastAddress ->
                lastAddress?.let { address ->
                    Log.d(TAG, "Attempting to reconnect to last device: $address")
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                        try {
                            connectToDeviceByAddress(address)
                        } catch (e: SecurityException) {
                            Log.e(TAG, "Permission denied for Bluetooth connection: ${e.localizedMessage}")
                            _statusMessage.value = "Cannot reconnect: Bluetooth permission denied."
                        }
                    } else {
                        Log.w(TAG, "BLUETOOTH_CONNECT permission not granted. Skipping reconnect.")
                        _statusMessage.value = "Bluetooth permission not granted. Enable in settings to reconnect."
                    }
                }
            }
        }
    }


    private fun saveLastConnectedDevice(address: String?) {
        if (address == null) return

        viewModelScope.launch {
            val deviceName = if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                foundDevices.value.find { it.bluetoothDevice.address == address }?.bluetoothDevice?.name
                    ?: "Unknown"
            } else {
                Log.w(TAG, "BLUETOOTH_CONNECT permission not granted. Using Unknown as device name.")
                "Unknown"
            }

            authPreferences.saveLastDevice(address, deviceName)
        }
    }



/*    fun toggleSearch(enable: Boolean) {
        if (enable) {
            _isSearching.value = true
            startScan()
            return
        }else {
            stopScan()
        }
    }*/
fun toggleSearch(enable: Boolean) {
    if (!isBluetoothOn(context)) {
        if (_isSearching.value) stopScan()
        _statusMessage.value = "Bluetooth is off. Please turn it on."
        _bluetoothEnabled.value = false
        return
    } else {
        _bluetoothEnabled.value = true
    }

    if (enable) {
        _isSearching.value = true
        startScan()
    } else {
        stopScan()
    }
}

    private fun startScan() {
        Log.d(TAG, "Starting Bluetooth scan...")
        _statusMessage.value = "Scanning for devices..."
        try {
            sdkManager.startSearch()
            scanTimeoutJob?.cancel() // Cancel any previous job
            scanTimeoutJob = viewModelScope.launch {
                delay(SCAN_DURATION_MS)
                if (_isSearching.value) {
                    Log.w(TAG, "BLE Scan timeout reached (28s). Stopping scan.")
                    stopScan()
                    _statusMessage.value = "Found ${foundDevices.value.size} devices. Tap 'Start Scan' to refresh."
                }
            }
        } catch (e: Exception) {
            val errorMessage = "Failed to start scan: ${e.localizedMessage}"
            _statusMessage.value = errorMessage
            Log.e(TAG, errorMessage, e)
            sdkManager.stopSearch() // Ensure cleanup
        }
    }

    fun stopScan() {
        Log.d(TAG, "Stopping Bluetooth scan.")
        sdkManager.stopSearch()
        scanTimeoutJob?.cancel() // **NEW: Cancel the timeout**
        _isSearching.value = false // Ensure the UI state is updated
        _statusMessage.value = "Scan stopped. Found ${foundDevices.value.size} devices."
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connectToDevice(device: ContecDevice) {
//        if (connectStatus.value == 3) {
//            _statusMessage.value = "Already connected to a device. Please disconnect first."
//            Log.w(TAG, "Attempted to connect while already connected.")
//            return
//        }
        if (latestConnectStatus == ContecStatus.CONNECTED_SUCCESS) {
            _statusMessage.value = "Already connected to a device. Please disconnect first."
            Log.w(TAG, "Attempted to connect while already connected.")
            return
        }

        Log.d(TAG, "Attempting to connect to device: ${device.bluetoothDevice.address}")
        _statusMessage.value = "Connecting to ${device.bluetoothDevice.address}..."

        if (_isSearching.value) {
            Log.d(TAG, "Scan is active. Stopping search for connection.")
            stopScan()
        }

        sdkManager.connectToDevice(device.bluetoothDevice)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connectToDeviceByAddress(address: String) {
        if (latestConnectStatus == ContecStatus.CONNECTED_SUCCESS) {
            _statusMessage.value = "Already connected to a device. Please disconnect first."
            Log.w(TAG, "Attempted to connect while already connected.")
            return
        }

        Log.d(TAG, "Attempting to connect to device by address: $address")
        _statusMessage.value = "Connecting to $address..."

        if (_isSearching.value) {
            Log.d(TAG, "Scan is active. Stopping search for connection.")
            stopScan()
        }

        sdkManager.connectToDevice(address)
    }


    fun disconnectDevice() {
        sdkManager.disconnectDevice()
        _connectedDeviceAddress.value = null
        viewModelScope.launch {
            authPreferences.saveLastDevice("", "")
        }
    }

    override fun onCleared() {
        super.onCleared()
        sdkManager.stopSearch()
        //sdkManager.disconnectDevice()
        Log.d(TAG, "ViewModel cleared and resources released.")
    }

    companion object {
        private const val TAG = "ScanViewModel"
    }
}
