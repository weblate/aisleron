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
import com.aisleron.data.preferences.SyncPreferencesImpl.Companion.CUSTOM_BACKEND_KEY
import com.aisleron.data.preferences.SyncPreferencesImpl.Companion.CUSTOM_BACKEND_URL
import com.aisleron.domain.preferences.SyncPreferences
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class SyncPreferencesImplTest {
    private lateinit var syncPreferences: SyncPreferences
    private lateinit var sharedPreferencesInitializer: SharedPreferencesInitializer

    @Before
    fun setUp() {
        sharedPreferencesInitializer = SharedPreferencesInitializer()
        syncPreferences = SyncPreferencesImpl(getInstrumentation().targetContext)
        sharedPreferencesInitializer.clearPreferences()
    }

    @Test
    fun useDefaultBackend_returnsCorrectBoolean_forGivenSetting() {
        // TODO: re-enable TRUE path once Aisleron Default sync is configured
        listOf(/*true, */ false).forEach { useDefaultBackend ->
            sharedPreferencesInitializer.setUseDefaultBackend(useDefaultBackend)
            val actual = syncPreferences.useDefaultBackEnd()
            assertEquals(
                useDefaultBackend, actual, "Failed for useDefaultBackend: $useDefaultBackend"
            )
        }
    }

    /* TODO: re-enable TRUE path once Aisleron Default sync is configured

    @Test
    fun getBackendUrl_UseDefaultBackEnd_ReturnsDefaultBackendUrl() {
        sharedPreferencesInitializer.setUseDefaultBackend(true)
        sharedPreferencesInitializer.setCustomBackendUrl("https://a.custom.url")

        val backendUrl = syncPreferences.getBackendUrl()

        assertEquals(BuildConfig.SUPABASE_URL, backendUrl)
    }

    @Test
    fun getBackendKey_UseDefaultBackEnd_ReturnsDefaultBackendKey() {
        sharedPreferencesInitializer.setUseDefaultBackend(true)
        sharedPreferencesInitializer.setCustomBackendKey("123abc")

        val backendKey = syncPreferences.getBackendKey()

        assertEquals(BuildConfig.SUPABASE_ANON_KEY, backendKey)
    }
    */

    @Test
    fun getBackendUrl_UseCustomBackEnd_ReturnsCustomBackendUrl() {
        val customBackendUrl = "https://a.custom.url"
        sharedPreferencesInitializer.setUseDefaultBackend(false)
        sharedPreferencesInitializer.setCustomBackendUrl(customBackendUrl)

        val backendUrl = syncPreferences.getBackendUrl()

        assertEquals(customBackendUrl, backendUrl)
    }

    @Test
    fun getBackendKey_UseCustomBackEnd_ReturnsCustomBackendKey() {
        val customBackendKey = "123abc"
        sharedPreferencesInitializer.setUseDefaultBackend(false)
        sharedPreferencesInitializer.setCustomBackendKey(customBackendKey)

        val backendKey = syncPreferences.getBackendKey()

        assertEquals(customBackendKey, backendKey)
    }

    private fun preferences() =
        PreferenceManager.getDefaultSharedPreferences(getInstrumentation().targetContext)

    @Test
    fun setBackendUrl_UrlProvided_SettingUpdated() {
        sharedPreferencesInitializer.setUseDefaultBackend(false)

        val customUrl = "https://a.custom.url"
        syncPreferences.setBackendUrl(customUrl)

        val updatedUrl = preferences().getString(CUSTOM_BACKEND_URL, "")
        assertEquals(customUrl, updatedUrl)
    }

    @Test
    fun setBackendKey_KeyProvided_SettingUpdated() {
        sharedPreferencesInitializer.setUseDefaultBackend(false)

        val customKey = "123abc"
        syncPreferences.setBackendKey(customKey)
        sharedPreferencesInitializer.setCustomBackendKey(customKey)

        val updatedKey = preferences().getString(CUSTOM_BACKEND_KEY, "")
        assertEquals(customKey, updatedKey)
    }
}