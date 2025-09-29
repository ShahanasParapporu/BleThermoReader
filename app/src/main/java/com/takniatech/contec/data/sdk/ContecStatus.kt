package com.takniatech.contec.data.sdk

import android.util.Log

object ContecStatus {
    const val DEVICE_CREATION_FAILED = 0
    const val COMMUNICATION_MODE_FAILED = 1
    const val CONNECTING = 2
    const val CONNECTED_SUCCESS = 3
    const val ACTIVE_DISCONNECTION = 4
    const val NO_SERVICE_FOUND = 5
    const val LISTENING_FAILED = 6
    const val ABNORMAL_DISCONNECTION = 7

    fun getMessage(status: Int): String {
        return when (status) {
            DEVICE_CREATION_FAILED -> "Device creation failed, unsupported Bluetooth device."
            COMMUNICATION_MODE_FAILED -> "Failed to create communication mode, unsupported Bluetooth type."
            CONNECTING -> "Device is connecting..."
            CONNECTED_SUCCESS -> "Device connected successfully. Synchronizing time..."
            ACTIVE_DISCONNECTION -> "Active disconnection. Cleaning up..."
            NO_SERVICE_FOUND -> "No service found."
            LISTENING_FAILED -> "Listening failed."
            ABNORMAL_DISCONNECTION -> "Abnormal disconnection. Cleaning up..."
            else -> "Unknown or unhandled status code."
        }
    }

    fun getLogLevel(status: Int): Int {
        return when (status) {
            CONNECTED_SUCCESS -> Log.INFO
            CONNECTING -> Log.DEBUG
            ACTIVE_DISCONNECTION -> Log.WARN
            else -> Log.ERROR // Treat all failures/unhandled codes as errors for visibility
        }
    }
}