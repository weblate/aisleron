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

package com.aisleron

import com.aisleron.data.sync.SyncSchedulerTestImpl
import com.aisleron.di.KoinTestRule
import com.aisleron.di.preferenceTestModule
import com.aisleron.di.syncTestModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelTestModule
import com.aisleron.domain.preferences.SyncServicePreference
import com.aisleron.domain.preferences.syncpreferences.SyncPreferencesRepository
import com.aisleron.domain.sync.SyncScheduler
import com.aisleron.testdata.data.preferences.syncpreferences.SyncPreferencesRepositoryTestImpl
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MainViewModelTest : KoinTest {
    private lateinit var viewModel: MainViewModel
    private lateinit var syncScheduler: SyncSchedulerTestImpl
    private lateinit var syncPreferencesRepository: SyncPreferencesRepositoryTestImpl

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(
            viewModelTestModule, useCaseModule, preferenceTestModule, syncTestModule
        )
    )

    @Before
    fun setUp() {
        viewModel = get<MainViewModel>()
        syncScheduler = get<SyncScheduler>() as SyncSchedulerTestImpl
        syncPreferencesRepository =
            get<SyncPreferencesRepository>() as SyncPreferencesRepositoryTestImpl

        syncPreferencesRepository.setSyncService(SyncServicePreference.CUSTOM_SERVICE)
    }

    @Test
    fun constructor_NoCoroutineScopeProvided_MainViewModelReturned() {
        val vm = MainViewModel(
            scheduleAdhocSyncUseCase = get(),
            schedulePeriodicSyncUseCase = get(),
        )

        assertNotNull(vm)
    }

    @Test
    fun onAppStart_SchedulesPeriodicSync() {
        viewModel.onAppStart()

        assertEquals(SyncSchedulerTestImpl.ScheduleType.PERIODIC, syncScheduler.scheduleType)
    }

    @Test
    fun onAppResume_SchedulesAdhocSync() {
        viewModel.onAppResume()

        val syncScheduler = get<SyncScheduler>() as SyncSchedulerTestImpl
        assertEquals(SyncSchedulerTestImpl.ScheduleType.ONE_OFF_ADHOC, syncScheduler.scheduleType)
    }

}