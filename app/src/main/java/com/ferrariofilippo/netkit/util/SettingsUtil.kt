/*
 * Copyright (c) 2026 Filippo Ferrario
 * Licensed under the MIT License. See the LICENSE.
 */

package com.ferrariofilippo.netkit.util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ferrariofilippo.netkit.NetKitApplication
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

object SettingsUtil {
    private val LAST_AGE_VERIFICATION = stringPreferencesKey("last_age_verification_time_stamp")
    private val AGE_VERIFICATION_ATTEMPTS = intPreferencesKey("age_verification_attempts")

    private var settingsStore: DataStore<Preferences>? = null

    fun setStore(application: NetKitApplication) {
        settingsStore = application.settingsStore
    }

    suspend fun setLastAgeVerificationTimeStamp(timestamp: String) {
        settingsStore!!.edit { pref ->
            pref[LAST_AGE_VERIFICATION] = timestamp
        }
    }

    fun getLastAgeVerificationTimeStamp(): Flow<String> {
        return settingsStore!!.data
            .catch {
                emit(emptyPreferences())
            }
            .map { preferences ->
                preferences[LAST_AGE_VERIFICATION] ?: ""
            }
    }

    suspend fun setAgeVerificationAttempts(count: Int) {
        settingsStore!!.edit { pref ->
            pref[AGE_VERIFICATION_ATTEMPTS] = count
        }
    }

    fun getAgeVerificationAttempts(): Flow<Int> {
        return settingsStore!!.data
            .catch {
                emit(emptyPreferences())
            }
            .map { preferences ->
                preferences[AGE_VERIFICATION_ATTEMPTS] ?: 0
            }
    }
}
