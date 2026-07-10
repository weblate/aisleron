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

package com.aisleron.data.preferences

import androidx.preference.PreferenceManager
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.aisleron.SharedPreferencesInitializer
import com.aisleron.data.preferences.syncpreferences.SyncPreferencesRepositoryImpl
import com.aisleron.data.preferences.syncpreferences.SyncPreferencesRepositoryImpl.Companion.CUSTOM_SERVICE_KEY
import com.aisleron.data.preferences.syncpreferences.SyncPreferencesRepositoryImpl.Companion.CUSTOM_SERVICE_URL
import com.aisleron.domain.preferences.syncpreferences.SyncPreferencesRepository
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class SyncPreferencesRepositoryImplTest {
    private lateinit var syncPreferencesRepository: SyncPreferencesRepository
    private lateinit var sharedPreferencesInitializer: SharedPreferencesInitializer

    private val defaultUrl = "https://SyncPreferencesRepositoryImplTest.com"
    private val defaultKey = "SyncPreferencesRepositoryImplTest"

    @Before
    fun setUp() {
        sharedPreferencesInitializer = SharedPreferencesInitializer()
        sharedPreferencesInitializer.clearPreferences()
        syncPreferencesRepository = SyncPreferencesRepositoryImpl(
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getInstrumentation().targetContext),
            defaultUrl = defaultUrl,
            defaultKey = defaultKey
        )
    }

    @Test
    fun useDefaultService_returnsCorrectBoolean_forGivenSetting() {
        // TODO: re-enable TRUE path once Aisleron Default sync is configured
        listOf(/*true, */ false).forEach { useDefaultService ->
            sharedPreferencesInitializer.setUseDefaultSyncService(useDefaultService)

            val syncPreferences = syncPreferencesRepository.getSyncPreferences()

            assertEquals(
                useDefaultService,
                syncPreferences.useDefaultService,
                "Failed for useDefaultService: $useDefaultService"
            )
        }
    }

    /* TODO: re-enable TRUE path once Aisleron Default sync is configured
    @Test
    fun getSyncPreferences_UseDefaultService_ReturnsDefaultServiceUrl() {
        val customServiceUrl = "https://a.custom.url"
        val customServiceKey = "123abc"

        sharedPreferencesInitializer.setUseDefaultSyncService(true)
        sharedPreferencesInitializer.setCustomSyncServiceUrl(customServiceUrl)
        sharedPreferencesInitializer.setCustomSyncServiceKey(customServiceKey)

        val syncPreferences = syncPreferencesRepository.getSyncPreferences()

        assertEquals(defaultUrl, syncPreferences.serviceUrl)
        assertEquals(defaultKey, syncPreferences.serviceKey)
    }
    */

    @Test
    fun getSyncPreferences_UseCustomService_ReturnsCustomServiceUrl() {
        val customServiceUrl = "https://a.custom.url"
        val customServiceKey = "123abc"

        sharedPreferencesInitializer.setUseDefaultSyncService(false)
        sharedPreferencesInitializer.setCustomSyncServiceUrl(customServiceUrl)
        sharedPreferencesInitializer.setCustomSyncServiceKey(customServiceKey)

        val syncPreferences = syncPreferencesRepository.getSyncPreferences()

        assertEquals(customServiceUrl, syncPreferences.serviceUrl)
        assertEquals(customServiceKey, syncPreferences.serviceKey)
    }

    private fun preferences() =
        PreferenceManager.getDefaultSharedPreferences(getInstrumentation().targetContext)

    @Test
    fun setCustomServiceDetails_DetailsProvided_SettingsUpdated() {
        sharedPreferencesInitializer.setUseDefaultSyncService(false)
        val customUrl = "https://a.custom.url"
        val customKey = "123abc"

        syncPreferencesRepository.setCustomServiceDetails(customUrl, customKey)

        val updatedUrl = preferences().getString(CUSTOM_SERVICE_URL, "")
        assertEquals(customUrl, updatedUrl)

        val updatedKey = preferences().getString(CUSTOM_SERVICE_KEY, "")
        assertEquals(customKey, updatedKey)
    }
}