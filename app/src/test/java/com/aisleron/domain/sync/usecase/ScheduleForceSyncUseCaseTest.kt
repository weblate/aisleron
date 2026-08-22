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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ScheduleForceSyncUseCaseTest {
    private lateinit var syncScheduler: SyncSchedulerTestImpl
    private lateinit var scheduleForceSyncUseCase: ScheduleForceSyncUseCase

    @BeforeEach
    fun setUp() {
        syncScheduler = SyncSchedulerTestImpl()
        scheduleForceSyncUseCase = ScheduleForceSyncUseCase(syncScheduler)
    }

    @Test
    fun invoke_ScheduleForceSync() {
        scheduleForceSyncUseCase()

        assertEquals(
            SyncSchedulerTestImpl.ScheduleType.ONE_OFF_FORCE_SYNC,
            syncScheduler.scheduleType
        )
    }
}