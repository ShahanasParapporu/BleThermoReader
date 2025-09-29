package com.takniatech.contec.presentation.shared.utils

import com.contec.htd.code.bean.ResultData as SdkResultData
import com.takniatech.contec.data.model.ResultData as AppResultData

fun SdkResultData.toAppModel(): AppResultData {
    return AppResultData(
        number = this.number,
        retValState = this.retValState,
        temp = this.temp,
        error = this.error
    )
}
