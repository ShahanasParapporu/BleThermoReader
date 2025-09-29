package com.takniatech.contec.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AuthPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val USER_ID = intPreferencesKey("user_id")
        private val LAST_DEVICE_ADDRESS = stringPreferencesKey("last_device_address")
        private val LAST_DEVICE_NAME = stringPreferencesKey("last_device_name")
    }

    val userId: Flow<Int?> = dataStore.data.map { prefs ->
        prefs[USER_ID]
    }

    val isLoggedIn: Flow<Boolean> = userId.map { it != null }

    val lastDeviceAddress: Flow<String?> = dataStore.data.map { prefs ->
        prefs[LAST_DEVICE_ADDRESS]
    }

    val lastDeviceName: Flow<String?> = dataStore.data.map { prefs ->
        prefs[LAST_DEVICE_NAME]
    }

    suspend fun saveUserId(id: Int) {
        dataStore.edit { prefs ->
            prefs[USER_ID] = id
        }
    }

    suspend fun clearUserId() {
        dataStore.edit { prefs ->
            prefs.remove(USER_ID)
        }
    }

    suspend fun saveLastDevice(address: String, name: String) {
        dataStore.edit { prefs ->
            prefs[LAST_DEVICE_ADDRESS] = address
            prefs[LAST_DEVICE_NAME] = name
        }
    }

}


