package com.takniatech.contec.data.model

import android.bluetooth.BluetoothDevice
data class ContecDevice(
    val bluetoothDevice: BluetoothDevice,
    val rssi: Int,
    val hasNewData: Boolean
)
