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

package com.aisleron.domain.sync.usecase

import com.aisleron.data.sync.SyncSchedulerTestImpl
import com.aisleron.domain.preferences.SyncServicePreference
import com.aisleron.testdata.data.preferences.syncpreferences.SyncPreferencesRepositoryTestImpl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ScheduleAdhocSyncUseCaseTest {
    private lateinit var syncScheduler: SyncSchedulerTestImpl
    private lateinit var syncPreferencesRepository: SyncPreferencesRepositoryTestImpl
    private lateinit var scheduleAdhocSyncUseCase: ScheduleAdhocSyncUseCase

    @BeforeEach
    fun setUp() {
        syncScheduler = SyncSchedulerTestImpl()
        syncPreferencesRepository = SyncPreferencesRepositoryTestImpl()
        scheduleAdhocSyncUseCase = ScheduleAdhocSyncUseCase(
            syncPreferencesRepository = syncPreferencesRepository,
            syncScheduler = syncScheduler
        )
    }

    @Test
    fun invoke_SyncServicePreferenceNotNone_ScheduleAdhocSync() {
        syncPreferencesRepository.setSyncService(SyncServicePreference.CUSTOM_SERVICE)

        scheduleAdhocSyncUseCase()

        assertEquals(
            SyncSchedulerTestImpl.ScheduleType.ONE_OFF_ADHOC,
            syncScheduler.scheduleType
        )
    }

    @Test
    fun invoke_SyncServicePreferenceNone_DoNotScheduleAdhocSync() {
        syncPreferencesRepository.setSyncService(SyncServicePreference.NONE)

        scheduleAdhocSyncUseCase()

        assertEquals(
            SyncSchedulerTestImpl.ScheduleType.NONE,
            syncScheduler.scheduleType
        )
    }
}