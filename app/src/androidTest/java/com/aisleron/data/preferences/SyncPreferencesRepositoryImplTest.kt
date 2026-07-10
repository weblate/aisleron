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
import com.aisleron.data.preferences.SyncPreferencesRepositoryImpl.Companion.CUSTOM_SERVICE_KEY
import com.aisleron.data.preferences.SyncPreferencesRepositoryImpl.Companion.CUSTOM_SERVICE_URL
import com.aisleron.domain.preferences.SyncPreferencesRepository
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class SyncPreferencesRepositoryImplTest {
    private lateinit var syncPreferencesRepository: SyncPreferencesRepository
    private lateinit var sharedPreferencesInitializer: SharedPreferencesInitializer

    @Before
    fun setUp() {
        sharedPreferencesInitializer = SharedPreferencesInitializer()
        sharedPreferencesInitializer.clearPreferences()
        syncPreferencesRepository =
            SyncPreferencesRepositoryImpl(getInstrumentation().targetContext)
    }

    @Test
    fun useDefaultService_returnsCorrectBoolean_forGivenSetting() {
        // TODO: re-enable TRUE path once Aisleron Default sync is configured
        listOf(/*true, */ false).forEach { useDefaultService ->
            sharedPreferencesInitializer.setUseDefaultSyncService(useDefaultService)
            val actual = syncPreferencesRepository.useDefaultService()
            assertEquals(
                useDefaultService, actual, "Failed for useDefaultService: $useDefaultService"
            )
        }
    }

    /* TODO: re-enable TRUE path once Aisleron Default sync is configured

    @Test
    fun getServiceUrl_UseDefaultService_ReturnsDefaultServiceUrl() {
        sharedPreferencesInitializer.setUseDefaultService(true)
        sharedPreferencesInitializer.setCustomServiceUrl("https://a.custom.url")

        val serviceUrl = syncPreferences.getServiceUrl()

        assertEquals(BuildConfig.SUPABASE_URL, serviceUrl)
    }

    @Test
    fun getServiceKey_UseDefaultService_ReturnsDefaultServiceKey() {
        sharedPreferencesInitializer.setUseDefaultService(true)
        sharedPreferencesInitializer.setCustomServiceKey("123abc")

        val serviceKey = syncPreferences.getServiceKey()

        assertEquals(BuildConfig.SUPABASE_ANON_KEY, serviceKey)
    }
    */

    @Test
    fun getServiceUrl_UseCustomService_ReturnsCustomServiceUrl() {
        val customServiceUrl = "https://a.custom.url"
        sharedPreferencesInitializer.setUseDefaultSyncService(false)
        sharedPreferencesInitializer.setCustomSyncServiceUrl(customServiceUrl)

        val serviceUrl = syncPreferencesRepository.getServiceUrl()

        assertEquals(customServiceUrl, serviceUrl)
    }

    @Test
    fun getServiceKey_UseCustomService_ReturnsCustomServiceKey() {
        val customServiceKey = "123abc"
        sharedPreferencesInitializer.setUseDefaultSyncService(false)
        sharedPreferencesInitializer.setCustomSyncServiceKey(customServiceKey)

        val serviceKey = syncPreferencesRepository.getServiceKey()

        assertEquals(customServiceKey, serviceKey)
    }

    private fun preferences() =
        PreferenceManager.getDefaultSharedPreferences(getInstrumentation().targetContext)

    @Test
    fun setServiceUrl_UrlProvided_SettingUpdated() {
        sharedPreferencesInitializer.setUseDefaultSyncService(false)

        val customUrl = "https://a.custom.url"
        syncPreferencesRepository.setServiceUrl(customUrl)

        val updatedUrl = preferences().getString(CUSTOM_SERVICE_URL, "")
        assertEquals(customUrl, updatedUrl)
    }

    @Test
    fun setServiceKey_KeyProvided_SettingUpdated() {
        sharedPreferencesInitializer.setUseDefaultSyncService(false)

        val customKey = "123abc"
        syncPreferencesRepository.setServiceKey(customKey)
        sharedPreferencesInitializer.setCustomSyncServiceKey(customKey)

        val updatedKey = preferences().getString(CUSTOM_SERVICE_KEY, "")
        assertEquals(customKey, updatedKey)
    }
}