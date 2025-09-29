package com.takniatech.contec.domain.repository

import com.takniatech.contec.data.model.HistoryResultData
import com.takniatech.contec.data.model.TemperatureReading
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface TemperatureRepository {
    suspend fun insertReading(reading: TemperatureReading)
    suspend fun insertHistoryBatch(userId: Int, deviceAddress: String, deviceName: String?, history: List<HistoryResultData>)
    suspend fun getReadingsForUser(userId: Int): List<TemperatureReading>
    fun getReadingsFlow(userId: Int): StateFlow<List<TemperatureReading>>
    suspend fun getTotalCountForUser(userId: Int): Int
    suspend fun getReadingsForDevice(userId: Int, deviceAddress: String): List<TemperatureReading>
    suspend fun getLastRealtimeForDevice(userId: Int, deviceAddress: String): TemperatureReading?
    fun getRealtimeReadingsFlow(userId: Int): Flow<List<TemperatureReading>>
    fun getHistoryReadingsFlow(userId: Int): Flow<List<TemperatureReading>>
}