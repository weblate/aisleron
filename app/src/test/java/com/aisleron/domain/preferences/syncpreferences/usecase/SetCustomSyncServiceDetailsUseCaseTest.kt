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

import com.aisleron.domain.preferences.syncpreferences.SyncPreferencesRepository
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SetCustomSyncServiceDetailsUseCaseTest {
    private lateinit var setCustomSyncServiceDetailsUseCase: SetCustomSyncServiceDetailsUseCase
    private lateinit var syncPreferencesRepository: SyncPreferencesRepository

    @BeforeEach
    fun setUp() {
        syncPreferencesRepository = mockk()
        setCustomSyncServiceDetailsUseCase =
            SetCustomSyncServiceDetailsUseCase(syncPreferencesRepository)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun invoke_CallsSetCustomServiceDetails() {
        val url = "https://SetCustomSyncServiceDetailsUseCaseTest.com"
        val key = "SetCustomSyncServiceDetailsUseCaseTest"
        every { syncPreferencesRepository.setCustomServiceDetails(url, key) } returns Unit

        setCustomSyncServiceDetailsUseCase(url, key)

        verify(exactly = 1) { syncPreferencesRepository.setCustomServiceDetails(url, key) }
    }

}