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

package com.aisleron.testdata.data.preferences.syncpreferences

import com.aisleron.domain.preferences.syncpreferences.SyncPreferences
import com.aisleron.domain.preferences.syncpreferences.SyncPreferencesRepository

class SyncPreferencesRepositoryTestImpl : SyncPreferencesRepository {
    private var _syncPreferences = getDefaultSyncPreferences()

    override fun getSyncPreferences(): SyncPreferences = _syncPreferences

    override fun setCustomServiceDetails(url: String, key: String) {
        _syncPreferences = _syncPreferences.copy(
            serviceUrl = url,
            serviceKey = key
        )
    }

    override fun setSyncOnMobileData(value: Boolean) {
        _syncPreferences = _syncPreferences.copy(
            syncOnMobileData = value
        )
    }

    fun setSyncPreferences(syncPreferences: SyncPreferences) {
        _syncPreferences = syncPreferences
    }

    fun getDefaultSyncPreferences() = SyncPreferences(
        useDefaultService = false,
        serviceUrl = "",
        serviceKey = "",
        syncOnMobileData = false
    )

    fun resetSyncPreferences() {
        _syncPreferences = getDefaultSyncPreferences()
    }
}