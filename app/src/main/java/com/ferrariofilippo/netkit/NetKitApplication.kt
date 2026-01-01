/*
 * Copyright (c) 2026 Filippo Ferrario
 * Licensed under the MIT License. See the LICENSE.
 */

package com.ferrariofilippo.netkit

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class NetKitApplication: Application() {
    companion object {
        const val SETTINGS_FILE_NAME = "settings"
    }

    val applicationScope = CoroutineScope(SupervisorJob())

    val settingsStore: DataStore<Preferences> by preferencesDataStore(SETTINGS_FILE_NAME)
}
