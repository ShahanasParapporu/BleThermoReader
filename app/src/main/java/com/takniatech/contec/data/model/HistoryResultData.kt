package com.takniatech.contec.data.model

data class HistoryResultData(
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val min: Int,
    val sec: Int,
    val tempUnit: Int, // 0=c 1=f
    val temp: String,
    val deviceName: String? = null,
    val deviceAddress: String? = null
)
