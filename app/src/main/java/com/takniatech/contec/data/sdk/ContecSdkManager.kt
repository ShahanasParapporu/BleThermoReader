package com.takniatech.contec.data.sdk

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import com.contec.htd.code.callback.BluetoothSearchCallback
import com.contec.htd.code.callback.ConnectCallback
import com.contec.htd.code.callback.OnOperateListener
import com.contec.htd.code.connect.ContecSdk
import com.takniatech.contec.data.model.ContecDevice
import com.takniatech.contec.data.model.HistoryResultData
import com.takniatech.contec.data.model.ResultData
import com.takniatech.contec.data.model.TemperatureReading
import com.takniatech.contec.domain.repository.TemperatureRepository
import com.takniatech.contec.presentation.shared.utils.toAppModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class ContecSdkManager @Inject constructor(
    @ApplicationContext private val context: Context,  private val temperatureRepository: TemperatureRepository

) {
    companion object {
        const val TAG = "ContecSDKManager"
    }

    private val sdk: ContecSdk = ContecSdk(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _timeSynced = MutableStateFlow(false)
    val timeSynced: StateFlow<Boolean> = _timeSynced.asStateFlow()
    private val _storageTotal = MutableStateFlow(0)
    val storageTotal: StateFlow<Int> = _storageTotal.asStateFlow()

    @Volatile
    var currentUserId: Int = -1
    private val _foundDevices = MutableStateFlow<List<ContecDevice>>(emptyList())
    val foundDevices: StateFlow<List<ContecDevice>> = _foundDevices.asStateFlow()
    private val _searchError = MutableStateFlow<Int?>(null)
    val searchError: StateFlow<Int?> = _searchError.asStateFlow()
    private val _searchComplete = MutableStateFlow(false)
    val searchComplete: StateFlow<Boolean> = _searchComplete.asStateFlow()
//    private val _connectStatus = MutableStateFlow<Int?>(null)
//    val connectStatus: StateFlow<Int?> = _connectStatus

    private val _connectStatus = MutableSharedFlow<Int>(replay = 1)
    val connectStatus: SharedFlow<Int> = _connectStatus.asSharedFlow()


    private var connectingDeviceAddress: String? = null
    private val _historyData = MutableStateFlow<List<HistoryResultData>>(emptyList())
    val historyData: StateFlow<List<HistoryResultData>> = _historyData.asStateFlow()

    private val _realtimeData = MutableStateFlow<ResultData?>(null)
    val realtimeData: StateFlow<ResultData?> = _realtimeData.asStateFlow()

    private val _operationError = MutableStateFlow<Pair<Int, Int>?>(null)
    val operationError: StateFlow<Pair<Int, Int>?> = _operationError.asStateFlow()

//    val connectedDeviceAddress: String?
//        get() = if (connectStatus.value == 3) connectingDeviceAddress else null

    @Volatile
    private var latestConnectStatus: Int? = null

    val connectedDeviceAddress: String?
        get() = if (latestConnectStatus == 3) connectingDeviceAddress else null


    private var pollingJob: Job? = null
    fun initSdk(isDelete: Boolean, connectTimeoutMillis: Int = 20000) {
        try {
            sdk.init(isDelete)
            sdk.setConnectTimeout(connectTimeoutMillis)
        }catch (t: Throwable) {
            Log.e(TAG, "Error initializing SDK: ${t.message}", t)
        }

    }

    fun setCurrentUser(userId: Int) {
        currentUserId = userId
        Log.d(TAG, "Current user set: $userId")
    }

    fun startSearch(timeoutMillis: Int = 20000) {
        try {
            _foundDevices.value = emptyList()
            _searchError.value = null
            _searchComplete.value = false

            sdk.startBluetoothSearch(bluetoothSearchCallback, timeoutMillis)
            Log.i(TAG, "Started Bluetooth search with timeout=$timeoutMillis ms")
        }catch (t: Throwable) {
            Log.e(TAG, "Error starting Bluetooth search: ${t.message}", t)
        }

    }

    fun stopSearch() {
        try {
            sdk.stopBluetoothSearch()
            Log.i(TAG, "Stopped Bluetooth search")
        } catch (t: Throwable) {
            Log.e(TAG, "Error stopping Bluetooth search: ${t.message}", t)
        }
    }


    private val bluetoothSearchCallback = object : BluetoothSearchCallback {
        override fun onSearchError(errorCode: Int) { //0 or 1 | 0:The device does not support BLE search | 1: Bluetooth is not open
            _searchError.value = errorCode
            Log.e(TAG, "Bluetooth search error: code=$errorCode")
        }

        override fun onContecDeviceFound(device: BluetoothDevice, rssi: Int, record: ByteArray) {
            try {
                val hasNewData = String(record).contains("DATA")
                val contecDevice = ContecDevice(device, rssi, hasNewData)
                _foundDevices.update { currentList ->
                    val filtered =
                        currentList.filterNot { it.bluetoothDevice.address == contecDevice.bluetoothDevice.address }
                    filtered + contecDevice
                }
                Log.d(TAG, "Device found: ${device.address}, rssi=$rssi, hasNewData=$hasNewData")
            }catch (t: Throwable) {
                Log.e(TAG, "Error processing found device: ${t.message}", t)
            }

        }

        override fun onSearchComplete() {
            _searchComplete.value = true
            Log.i(TAG, "Bluetooth search complete. Devices found=${_foundDevices.value.size}")
        }
    }

    //** --- Connect using BluetoothDevice --- *//*
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connectToDevice(device: BluetoothDevice) {
        connectingDeviceAddress = device.address
        val deviceName = device.name ?: "Unknown"
        try {
            Log.i(TAG, "Connecting to $deviceName (${device.address})")
            sdk.connect(device, connectCallback, operateListener)
        } catch (t: Throwable) {
            Log.e(TAG, "Error connecting to $deviceName (${device.address}): ${t.message}", t)
        }
    }

    //** --- Connect using address --- *//*
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connectToDevice(address: String) {
        connectingDeviceAddress = address
        val deviceName = _foundDevices.value.find { it.bluetoothDevice.address == address }?.bluetoothDevice?.name ?: "Unknown"
        try {
            sdk.connect(address, connectCallback, operateListener)
            Log.i(TAG, "Connecting to $deviceName ($address)")
        } catch (t: Throwable) {
            Log.e(TAG, "Error connecting to $deviceName ($address): ${t.message}", t)
        }
    }

    //** --- ConnectCallback Implementation --- *//*
    private val connectCallback = object : ConnectCallback {
        override fun onConnectStatus(status: Int) {
            //_connectStatus.value = status
            _connectStatus.tryEmit(status)
            latestConnectStatus = status
            val logLevel = ContecStatus.getLogLevel(status)
            val message = "Connection Status $status: ${ContecStatus.getMessage(status)}"

            Log.println(logLevel, TAG, message)

            when (status) {
                ContecStatus.CONNECTED_SUCCESS -> {
                    try {
                        sdk.setTime()
                    } catch (t: Throwable) {
                        Log.e(TAG, "Error setting time: ${t.message}", t)
                    }
                }

                ContecStatus.ACTIVE_DISCONNECTION, ContecStatus.ABNORMAL_DISCONNECTION -> {
                    resetConnectionState()
                }
            }
        }
    }

    private val operateListener = object : OnOperateListener {
        override fun onFail(currentOperate: Int, errorCode: Int) {
            _operationError.value = currentOperate to errorCode
            Log.e(TAG, "Operation failed. CurrentOperate=$currentOperate, ErrorCode=$errorCode")
            //currentOperate 0:Synchronization time | 1:Get space information | 2:Get historical data
            //errorCode 0:No connection operation | 1:fail | 830000:Synchronization timecommand timeout | 900000:Get space informationtimeout | 910001:Get historical data timeout
        }

        override fun onSetTimeSuccess() {
            Log.d(TAG, "Time synchronized successfully. Getting storage info...")
            _timeSynced.value = true
            try {
                sdk.getStorageInfo()
            } catch (t: Throwable) {
                Log.e(TAG, "Error requesting storage info: ${t.message}", t)
            }
        }

        override fun onStorageInfoSuccess(totalNumber: Int) {
            Log.d(TAG, "Storage info received: totalNumber=$totalNumber")
            _storageTotal.value = totalNumber
            try {
                if (totalNumber > 0) {
                    Log.d(TAG, "Requesting historical data...")
                    sdk.getHistoryData()
                } else {
                    Log.d(TAG, "No history. Requesting realtime data...")
                    sdk.getRealtimeData()
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Error handling storage info: ${t.message}", t)
            }
        }

        override fun onHistoryDataEmpty() {
            _historyData.value = emptyList()
            Log.d(TAG, "History data is empty. Requesting real-time data...")
            try {
                startRealtimePolling()
            } catch (t: Throwable) {
                Log.e(TAG, "Error requesting realtime data: ${t.message}", t)
            }
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onHistoryDataSuccess(historyResultDataArrayList: ArrayList<com.contec.htd.code.bean.HistoryResultData>) {
            try {
                val appHistoryList = historyResultDataArrayList.map { it.toAppModel(connectingDeviceAddress) }
                _historyData.value = appHistoryList
                Log.d(
                    TAG,
                    "History data received: ${appHistoryList.size} records. Requesting real-time data..."
                )

                val userId = currentUserId.takeIf { it >= 0 } ?: return
                val deviceAddr = connectingDeviceAddress ?: "unknown"
                val deviceName = _foundDevices.value
                    .find { it.bluetoothDevice.address == deviceAddr }
                    ?.bluetoothDevice?.name ?: "Unknown"

                scope.launch {
                    temperatureRepository.insertHistoryBatch(
                        userId,
                        deviceAddr,
                        deviceName,
                        appHistoryList
                    )
                    startRealtimePolling()
                }
            }catch (t: Throwable) {
                Log.e(TAG, "Error handling history data: ${t.message}", t)
            }
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onRealtimeDataSuccess(resultData: com.contec.htd.code.bean.ResultData) {
            try {
                // map SDK ResultData -> app ResultData
                val appResultData = resultData.toAppModel()

                _realtimeData.value = appResultData
                Log.d(TAG, "Realtime data received: $appResultData")

                val userId = currentUserId.takeIf { it >= 0 } ?: return
                val deviceAddr = connectingDeviceAddress ?: "unknown"
                val deviceName = _foundDevices.value
                    .find { it.bluetoothDevice.address == deviceAddr }
                    ?.bluetoothDevice?.name ?: "Unknown"

                // map to TemperatureReading
                val parsedTemp = appResultData.temp?.toFloatOrNull() ?: Float.NaN
                val unit = when (appResultData.temp?.contains("F", ignoreCase = true)) {
                    true -> "F"
                    else -> "C"
                }
                val rawState = appResultData.retValState?.toIntOrNull()
                val deviceError = appResultData.error

                scope.launch {
                    try {
                        val last = temperatureRepository.getLastRealtimeForDevice(userId, deviceAddr)
                        val shouldInsert = last == null ||
                                last.deviceError != deviceError ||
                                last.rawState != rawState ||
                                abs(last.temp - (parsedTemp.takeUnless { it.isNaN() } ?: last.temp)) > 0.0001f

                        if (shouldInsert) {
                            val reading = TemperatureReading(
                                id = null,
                                userId = userId,
                                deviceAddress = deviceAddr,
                                deviceName = deviceName,
                                temp = parsedTemp.takeIf { !it.isNaN() } ?: 0f,
                                unit = unit,
                                timestamp = System.currentTimeMillis(),
                                isRealtime = true,
                                deviceError = deviceError,
                                rawState = rawState
                            )
                            temperatureRepository.insertReading(reading)
                            Log.d(TAG, "Inserted realtime reading into DB: $reading")
                        } else {
                            Log.d(TAG, "Realtime data same as last, skipping DB insert")
                        }
                    }catch (t: Throwable) {
                        Log.e(TAG, "Error inserting realtime reading: ${t.message}", t)
                    }

                }

                if (pollingJob == null || pollingJob?.isActive == false) {
                    startRealtimePolling()
                }
            }catch (t: Throwable) {
                Log.e(TAG, "Error handling realtime data: ${t.message}", t)
            }
        }

    }

    private fun startRealtimePolling() {
        pollingJob?.cancel()
        pollingJob = scope.launch {
            Log.i(TAG, "Started realtime polling every 2s")
            while (isActive) {
                delay(2000L)
                try {
                    sdk.getRealtimeData()
                } catch (t: Throwable) {
                    Log.e(TAG, "Polling error: ${t.localizedMessage}")
                }
            }
        }
    }

    fun stopRealtimePolling() {
        pollingJob?.cancel()
        pollingJob = null
        Log.i(TAG, "Stopped realtime polling")
    }


    //disconnect
    //** --- Disconnect the device --- *//*
    fun disconnectDevice() {
        try {
            stopRealtimePolling()
            //scope.cancel()
            sdk.disconnect()
            Log.d(TAG, "Device disconnected.")
            resetConnectionState()
        }catch (t: Throwable) {
            Log.e(TAG, "Error disconnecting device: ${t.message}", t)
        }

    }

    private fun resetConnectionState() {
        Log.w(TAG, "Resetting connection state")
        //_connectStatus.value = null
        latestConnectStatus = null
        _operationError.value = null
        _timeSynced.value = false
        _storageTotal.value = 0
        //_realtimeData.value = null
        //_historyData.value = emptyList()

    }

}
