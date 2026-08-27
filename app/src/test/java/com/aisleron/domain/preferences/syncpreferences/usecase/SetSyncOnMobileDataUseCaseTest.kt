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

import com.aisleron.testdata.data.preferences.syncpreferences.SyncPreferencesRepositoryTestImpl
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SetSyncOnMobileDataUseCaseTest {
    private lateinit var setSyncOnMobileDataUseCase: SetSyncOnMobileDataUseCase
    private lateinit var syncPreferencesRepository: SyncPreferencesRepositoryTestImpl

    @BeforeEach
    fun setUp() {
        syncPreferencesRepository = SyncPreferencesRepositoryTestImpl()
        syncPreferencesRepository.resetSyncPreferences()
        setSyncOnMobileDataUseCase = SetSyncOnMobileDataUseCase(syncPreferencesRepository)
    }

    @Test
    fun invoke_CallsSetSyncOnMobileData() {
        val syncOnMobileDataBefore = syncPreferencesRepository.getSyncPreferences().syncOnMobileData

        setSyncOnMobileDataUseCase(!syncOnMobileDataBefore)

        val updatedPreferences = syncPreferencesRepository.getSyncPreferences()
        assertTrue(syncOnMobileDataBefore != updatedPreferences.syncOnMobileData)
    }
}