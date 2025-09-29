package com.takniatech.contec.data.model

data class ResultData(
    val number: String?,
    val retValState: String?, // retValState as string in SDK
    val temp: String?,
    val error: String?
)
