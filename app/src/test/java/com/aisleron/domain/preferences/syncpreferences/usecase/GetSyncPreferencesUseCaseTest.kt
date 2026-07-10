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

package com.aisleron.domain.preferences.syncpreferences.usecase

import com.aisleron.domain.preferences.syncpreferences.SyncPreferences
import com.aisleron.domain.preferences.syncpreferences.SyncPreferencesRepository
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetSyncPreferencesUseCaseTest {
    private lateinit var getSyncPreferencesUseCaseTest: GetSyncPreferencesUseCase
    private lateinit var syncPreferencesRepository: SyncPreferencesRepository

    @BeforeEach
    fun setUp() {
        syncPreferencesRepository = mockk()
        getSyncPreferencesUseCaseTest = GetSyncPreferencesUseCase(syncPreferencesRepository)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun invoke_ReturnsSyncPreferences() {
        val syncPreferences = SyncPreferences(
            useDefaultService = false,
            serviceUrl = "https://example.com",
            serviceKey = "abc143293293248329048"
        )

        every { syncPreferencesRepository.getSyncPreferences() } returns syncPreferences

        val resultPreferences = getSyncPreferencesUseCaseTest()

        assertEquals(syncPreferences, resultPreferences)
        verify(exactly = 1) { syncPreferencesRepository.getSyncPreferences() }
    }
}