package com.takniatech.contec.di

import android.content.Context
import com.takniatech.contec.data.local.ContecSQLiteHelper
import com.takniatech.contec.data.repository.TemperatureRepositoryImpl
import com.takniatech.contec.data.repository.UserRepositoryImpl
import com.takniatech.contec.domain.repository.TemperatureRepository
import com.takniatech.contec.domain.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSQLiteHelper(@ApplicationContext context: Context): ContecSQLiteHelper {
        return ContecSQLiteHelper(context)
    }

    @Provides
    @Singleton
    fun provideUserRepository(dbHelper: ContecSQLiteHelper): UserRepository {
        return UserRepositoryImpl(dbHelper)
    }

    @Provides
    @Singleton
    fun provideTemperatureRepository(dbHelper: ContecSQLiteHelper): TemperatureRepository {
        return TemperatureRepositoryImpl(dbHelper)
    }
}
