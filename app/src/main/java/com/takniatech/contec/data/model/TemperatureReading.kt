package com.takniatech.contec.data.model

data class TemperatureReading(
    val id: Int? = null,
    val userId: Int,
    val deviceAddress: String,
    val deviceName: String?,
    val temp: Float,
    val unit: String,
    val timestamp: Long,
    val isRealtime: Boolean,
    val deviceError: String? = null,
    val rawState: Int? = null
)

