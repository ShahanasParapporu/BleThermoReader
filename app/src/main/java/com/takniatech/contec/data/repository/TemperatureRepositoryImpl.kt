package com.takniatech.contec.data.repository

import com.takniatech.contec.data.local.ContecSQLiteHelper
import com.takniatech.contec.data.model.HistoryResultData
import com.takniatech.contec.data.model.TemperatureReading
import com.takniatech.contec.domain.repository.TemperatureRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TemperatureRepositoryImpl @Inject constructor(
    private val dbHelper: ContecSQLiteHelper
) : TemperatureRepository {
    private val userFlows = mutableMapOf<Int, MutableStateFlow<List<TemperatureReading>>>()

    private fun ensureFlow(userId: Int): MutableStateFlow<List<TemperatureReading>> {
        return userFlows.getOrPut(userId) {
            MutableStateFlow(dbHelper.getTemperatureReadingsForUserSync(userId))
        }
    }

    override suspend fun insertReading(reading: TemperatureReading) = withContext(Dispatchers.IO) {
        dbHelper.insertTemperatureReadingSync(reading)

        // refresh flow
        val flow = ensureFlow(reading.userId)
        flow.value = dbHelper.getTemperatureReadingsForUserSync(reading.userId)
    }

    override suspend fun insertHistoryBatch(
        userId: Int,
        deviceAddress: String,
        deviceName: String?,
        history: List<HistoryResultData>
    ) = withContext(Dispatchers.IO) {
        dbHelper.insertHistoryBatch(userId, deviceAddress, deviceName, history)
        val flow = ensureFlow(userId)
        flow.value = dbHelper.getTemperatureReadingsForUserSync(userId)
    }

    override suspend fun getReadingsForUser(userId: Int): List<TemperatureReading> =
        withContext(Dispatchers.IO) {
            dbHelper.getTemperatureReadingsForUserSync(userId)
        }

    override fun getReadingsFlow(userId: Int): StateFlow<List<TemperatureReading>> {
        return ensureFlow(userId).asStateFlow()
    }

    override suspend fun getTotalCountForUser(userId: Int): Int = withContext(Dispatchers.IO) {
        dbHelper.getTotalReadingsForUserSync(userId)
    }

    override suspend fun getReadingsForDevice(userId: Int, deviceAddress: String): List<TemperatureReading> = withContext(Dispatchers.IO) {
            dbHelper.getTemperatureReadingsForUserSync(userId)
                .filter { it.deviceAddress == deviceAddress }
        }
    override suspend fun getLastRealtimeForDevice(userId: Int, deviceAddress: String): TemperatureReading? =
        withContext(Dispatchers.IO) {
            dbHelper.getLastRealtimeForDevice(userId, deviceAddress)
        }
    override fun getRealtimeReadingsFlow(userId: Int): Flow<List<TemperatureReading>> {
        return getReadingsFlow(userId).map { list -> list.filter { it.isRealtime } }
    }

    override fun getHistoryReadingsFlow(userId: Int): Flow<List<TemperatureReading>> {
        return getReadingsFlow(userId).map { list -> list.filter { !it.isRealtime } }
    }

}
