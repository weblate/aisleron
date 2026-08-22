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

package com.aisleron.data.sync

import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.NetworkType
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.aisleron.data.sync.SyncSchedulerImpl.Companion.ADHOC_WORK_NAME
import com.aisleron.data.sync.SyncSchedulerImpl.Companion.FORCE_SYNC_WORK_NAME
import com.aisleron.data.sync.SyncSchedulerImpl.Companion.SCHEDULED_WORK_NAME
import com.aisleron.domain.preferences.syncpreferences.SyncNetworkConstraint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.koin.test.KoinTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SyncSchedulerImplTest : KoinTest {
    private lateinit var workManager: WorkManager
    private lateinit var syncScheduler: SyncSchedulerImpl

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        workManager = WorkManager.getInstance(context)
        syncScheduler = SyncSchedulerImpl(workManager)
    }

    @Test
    fun schedulePeriodicSync_SyncNetworkConstraintIsNotRequired_EnqueuesWorkWithNotRequiredNetwork() =
        runTest {
            syncScheduler.schedulePeriodicSync(SyncNetworkConstraint.NOT_REQUIRED, 15L)

            val workInfos = workManager.getWorkInfosForUniqueWorkFlow(SCHEDULED_WORK_NAME).first()
            val workInfo = workInfos.firstOrNull()

            assertNotNull(workInfo)
            assertEquals(WorkInfo.State.ENQUEUED, workInfo.state)
            assertEquals(
                NetworkType.NOT_REQUIRED,
                workInfo.constraints.requiredNetworkType
            )
        }

    @Test
    fun schedulePeriodicSync_SyncNetworkConstraintIsUnmetered_EnqueuesWorkWithUnmeteredNetwork() =
        runTest {
            syncScheduler.schedulePeriodicSync(SyncNetworkConstraint.UNMETERED, 15L)

            val workInfos = workManager.getWorkInfosForUniqueWorkFlow(SCHEDULED_WORK_NAME).first()
            val workInfo = workInfos.firstOrNull()

            assertEquals(
                NetworkType.UNMETERED,
                workInfo?.constraints?.requiredNetworkType
            )
        }

    @Test
    fun scheduleAdhocSync_Called_EnqueuesAdhocSync() = runTest {
        syncScheduler.scheduleAdhocSync(SyncNetworkConstraint.CONNECTED)

        val adhocWork =
            workManager.getWorkInfosForUniqueWorkFlow(ADHOC_WORK_NAME).first().firstOrNull()

        assertNotNull(adhocWork)
        assertEquals(WorkInfo.State.ENQUEUED, adhocWork.state)
        assertEquals(NetworkType.CONNECTED, adhocWork.constraints.requiredNetworkType)
    }

    @Test
    fun scheduleForceSync_Called_EnqueuesForceSync() = runTest {
        syncScheduler.scheduleForceSync(SyncNetworkConstraint.CONNECTED)

        val forceSyncWork =
            workManager.getWorkInfosForUniqueWorkFlow(FORCE_SYNC_WORK_NAME).first().firstOrNull()

        assertNotNull(forceSyncWork)
        assertEquals(WorkInfo.State.ENQUEUED, forceSyncWork.state)
        assertEquals(NetworkType.CONNECTED, forceSyncWork.constraints.requiredNetworkType)
    }

    @Test
    fun scheduleForceSync_Called_CancelsDataChangeWorkAndEnqueuesForceSync() = runTest {
        syncScheduler.scheduleAdhocSync(SyncNetworkConstraint.CONNECTED)

        syncScheduler.scheduleForceSync(SyncNetworkConstraint.CONNECTED)

        val adhocWork =
            workManager.getWorkInfosForUniqueWorkFlow(ADHOC_WORK_NAME).first().firstOrNull()

        val forceSyncWork =
            workManager.getWorkInfosForUniqueWorkFlow(FORCE_SYNC_WORK_NAME).first().firstOrNull()

        assertEquals(WorkInfo.State.CANCELLED, adhocWork?.state)
        assertEquals(WorkInfo.State.ENQUEUED, forceSyncWork?.state)
    }
}