package com.takniatech.contec.presentation.shared.utils

import com.contec.htd.code.bean.HistoryResultData as SdkHistoryData
import com.takniatech.contec.data.model.HistoryResultData as AppHistoryData

fun SdkHistoryData.toAppModel(deviceAddress: String? = null, deviceName: String? = null): AppHistoryData {
    return AppHistoryData(
        year = this.year,
        month = this.month,
        day = this.day,
        hour = this.hour,
        min = this.min,
        sec = this.sec,
        tempUnit = this.tempUnit,   // 0 = C, 1 = F
        temp = this.temp,
        deviceName = deviceName,
        deviceAddress = deviceAddress
    )
}