package com.takniatech.contec

import android.app.Application
import com.takniatech.contec.data.sdk.ContecSdkManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ContecMedicalApplication : Application() {
    @Inject
    lateinit var sdkManager: ContecSdkManager

    override fun onCreate() {
        super.onCreate()
        sdkManager.initSdk(isDelete = false)
    }
}
