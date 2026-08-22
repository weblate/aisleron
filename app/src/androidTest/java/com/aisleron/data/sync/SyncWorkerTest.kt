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

import androidx.work.ListenableWorker
import com.aisleron.data.note.NoteDto
import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoTestModule
import com.aisleron.di.generalTestModule
import com.aisleron.di.preferenceTestModule
import com.aisleron.di.syncTestModule
import com.aisleron.domain.log.Logger
import com.aisleron.domain.preferences.SyncServicePreference
import com.aisleron.domain.preferences.syncpreferences.SyncPreferencesRepository
import com.aisleron.testdata.data.preferences.syncpreferences.SyncPreferencesRepositoryTestImpl
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.qualifier.named
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.test.assertEquals

class SyncWorkerTest : KoinTest {
    private lateinit var syncManager: SyncManager
    private lateinit var logger: Logger
    private lateinit var api: SyncApiTestImpl<NoteDto>

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(daoTestModule, syncTestModule, generalTestModule, preferenceTestModule)
    )

    @Before
    fun setUp() {
        syncManager = get()
        logger = get()
        api = get<SyncApi<NoteDto>>(named("noteSyncApi")) as SyncApiTestImpl<NoteDto>
        api.initSyncApi()

        val syncPreferencesRepository =
            get<SyncPreferencesRepository>() as SyncPreferencesRepositoryTestImpl

        syncPreferencesRepository.setSyncService(SyncServicePreference.CUSTOM_SERVICE)
    }

    @Test
    fun runSyncWork_SuccessfulSync_ReturnsSuccess() = runTest {
        val result = syncManager.runSyncWork(logger)

        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun runSyncWork_UnsuccessfulSync_ReturnRetry() = runTest {
        api.failWith(Exception("Sync API Failure"))

        val result = syncManager.runSyncWork(logger)

        assertEquals(ListenableWorker.Result.retry(), result)
    }

}