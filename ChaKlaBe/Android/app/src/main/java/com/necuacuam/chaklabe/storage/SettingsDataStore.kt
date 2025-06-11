package com.necuacuam.chaklabe.storage

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "settings"

val Context.settingsDataStore by preferencesDataStore(name = DATASTORE_NAME)

object SettingsKeys {
    val CAMERA_IP = stringPreferencesKey("camera_ip")
}

class SettingsDataStore(private val context: Context) {

    val cameraIpAddress: Flow<String> = context.settingsDataStore.data
        .map { preferences ->
            preferences[SettingsKeys.CAMERA_IP] ?: ""
        }

    suspend fun saveCameraIpAddress(ip: String) {
        context.settingsDataStore.edit { settings ->
            settings[SettingsKeys.CAMERA_IP] = ip
        }
    }
}
