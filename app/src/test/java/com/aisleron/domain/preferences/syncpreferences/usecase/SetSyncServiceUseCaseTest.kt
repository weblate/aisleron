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

import com.aisleron.domain.preferences.SyncServicePreference
import com.aisleron.testdata.data.preferences.syncpreferences.SyncPreferencesRepositoryTestImpl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SetSyncServiceUseCaseTest {
    private lateinit var setSyncServiceUseCase: SetSyncServiceUseCase
    private lateinit var syncPreferencesRepository: SyncPreferencesRepositoryTestImpl

    @BeforeEach
    fun setUp() {
        syncPreferencesRepository = SyncPreferencesRepositoryTestImpl()
        syncPreferencesRepository.resetSyncPreferences()
        setSyncServiceUseCase = SetSyncServiceUseCase(syncPreferencesRepository)
    }

    @Test
    fun invoke_CallsSetSyncService() {
        val syncServiceBefore = SyncServicePreference.NONE
        syncPreferencesRepository.setSyncService(syncServiceBefore)

        assertEquals(
            syncServiceBefore, syncPreferencesRepository.getSyncPreferences().syncServicePreference
        )

        val syncServiceNew = SyncServicePreference.CUSTOM_SERVICE
        setSyncServiceUseCase(syncServiceNew)

        val updatedPreferences = syncPreferencesRepository.getSyncPreferences()
        assertEquals(syncServiceNew, updatedPreferences.syncServicePreference)
    }

}