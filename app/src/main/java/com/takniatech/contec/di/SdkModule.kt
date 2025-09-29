package com.takniatech.contec.di

import android.content.Context
import com.takniatech.contec.data.sdk.ContecSdkManager
import com.takniatech.contec.domain.repository.TemperatureRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SdkModule {
    @Provides
    @Singleton
    fun provideContecSdkManager(
        @ApplicationContext context: Context,
        temperatureRepository: TemperatureRepository
    ): ContecSdkManager {
        return ContecSdkManager(context, temperatureRepository)
    }
}
