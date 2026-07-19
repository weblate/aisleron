/*
 * Copyright (C) 2026 aisleron.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.aisleron.data.preferences.syncpreferences

import android.content.SharedPreferences
import androidx.core.content.edit
import com.aisleron.domain.preferences.syncpreferences.SyncPreferences
import com.aisleron.domain.preferences.syncpreferences.SyncPreferencesRepository

class SyncPreferencesRepositoryImpl(
    private val sharedPreferences: SharedPreferences,
    private val defaultUrl: String,
    private val defaultKey: String
) : SyncPreferencesRepository {
    private fun useDefaultService(): Boolean = false
    //sharedPreferences.getBoolean(USE_DEFAULT_SERVICE, false)

    private fun getServiceUrl(): String {
        return if (useDefaultService())
            defaultUrl
        else
            sharedPreferences.getString(CUSTOM_SERVICE_URL, "").orEmpty()
    }

    private fun getServiceKey(): String {
        return if (useDefaultService())
            defaultKey
        else
            sharedPreferences.getString(CUSTOM_SERVICE_KEY, "").orEmpty()
    }

    private fun getSyncOnMobileData(): Boolean =
        sharedPreferences.getBoolean(SYNC_ON_MOBILE_DATA, false)

    override fun getSyncPreferences(): SyncPreferences =
        SyncPreferences(
            useDefaultService = useDefaultService(),
            serviceUrl = getServiceUrl(),
            serviceKey = getServiceKey(),
            syncOnMobileData = getSyncOnMobileData()
        )

    override fun setCustomServiceDetails(url: String, key: String) {
        sharedPreferences.edit {
            putString(CUSTOM_SERVICE_URL, url)
            putString(CUSTOM_SERVICE_KEY, key)
        }
    }

    override fun setSyncOnMobileData(value: Boolean) {
        sharedPreferences.edit {
            putBoolean(SYNC_ON_MOBILE_DATA, value)
        }
    }

    companion object {
        const val USE_DEFAULT_SERVICE = "use_default_service"
        const val CUSTOM_SERVICE_URL = "custom_service_url"
        const val CUSTOM_SERVICE_KEY = "custom_service_key"
        const val SYNC_ON_MOBILE_DATA = "sync_on_mobile_data"
    }
}