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

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.aisleron.domain.preferences.SyncServicePreference
import com.aisleron.domain.preferences.syncpreferences.SyncPreferencesRepository
import com.aisleron.domain.sync.SyncScheduler
import java.util.concurrent.TimeUnit

class SyncSchedulerImpl(
    private val workManager: WorkManager,
    private val syncPreferencesRepository: SyncPreferencesRepository
) : SyncScheduler {
    private fun getNetworkType(forceSync: Boolean): NetworkType {
        val syncPreferences = syncPreferencesRepository.getSyncPreferences()
        if (syncPreferences.syncServicePreference == SyncServicePreference.NONE)
            return NetworkType.NOT_REQUIRED

        if (forceSync || syncPreferences.syncOnMobileData)
            return NetworkType.CONNECTED

        return NetworkType.UNMETERED
    }

    override fun schedulePeriodicSync(intervalMinutes: Long) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(getNetworkType(false))
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            repeatInterval = intervalMinutes.coerceAtLeast(MIN_PERIODIC_INTERVAL_MINUTES),
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .applyDefaultBackoff()
            .build()

        workManager.enqueueUniquePeriodicWork(
            SCHEDULED_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            syncRequest
        )
    }

    private fun scheduleOneOffSync(
        constraints: Constraints, workName: String, policy: ExistingWorkPolicy
    ) {
        val immediateRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .applyDefaultBackoff()
            .build()

        workManager.enqueueUniqueWork(
            workName,
            policy,
            immediateRequest
        )
    }


    override fun scheduleForceSync() {
        // Cancel any background data-change syncs waiting on constraints (e.g. Wi-Fi)
        workManager.cancelUniqueWork(ADHOC_WORK_NAME)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(getNetworkType(forceSync = true))
            .build()

        scheduleOneOffSync(constraints, FORCE_SYNC_WORK_NAME, ExistingWorkPolicy.KEEP)
    }

    override fun scheduleAdhocSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(getNetworkType(forceSync = false))
            .build()

        scheduleOneOffSync(
            constraints, ADHOC_WORK_NAME, ExistingWorkPolicy.APPEND_OR_REPLACE
        )
    }

    private fun <B : WorkRequest.Builder<B, *>> B.applyDefaultBackoff(): B {
        return setBackoffCriteria(
            backoffPolicy = BackoffPolicy.EXPONENTIAL,
            backoffDelay = INITIAL_BACKOFF_SECONDS,
            timeUnit = TimeUnit.SECONDS
        )
    }

    companion object {
        const val SCHEDULED_WORK_NAME = "aisleron_periodic_sync"
        const val ADHOC_WORK_NAME = "aisleron_adhoc_sync"
        const val FORCE_SYNC_WORK_NAME = "aisleron_force_sync"

        private const val MIN_PERIODIC_INTERVAL_MINUTES = 15L
        private const val INITIAL_BACKOFF_SECONDS = 30L
    }
}