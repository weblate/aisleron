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
import com.aisleron.domain.preferences.syncpreferences.SyncNetworkConstraint
import com.aisleron.domain.sync.SyncScheduler
import java.util.concurrent.TimeUnit

class SyncSchedulerImpl(
    private val workManager: WorkManager
) : SyncScheduler {
    override fun schedulePeriodicSync(
        networkConstraint: SyncNetworkConstraint, intervalMinutes: Long,
    ) {
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            repeatInterval = intervalMinutes.coerceAtLeast(MIN_PERIODIC_INTERVAL_MINUTES),
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .applyDefaultConstraints(networkConstraint)
            .applyDefaultBackoff()
            .build()

        workManager.enqueueUniquePeriodicWork(
            SCHEDULED_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            syncRequest
        )
    }

    private fun scheduleOneOffSync(
        workName: String, policy: ExistingWorkPolicy, networkConstraint: SyncNetworkConstraint
    ) {
        val immediateRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .applyDefaultConstraints(networkConstraint)
            .applyDefaultBackoff()
            .build()

        workManager.enqueueUniqueWork(
            workName,
            policy,
            immediateRequest
        )
    }

    override fun scheduleForceSync(networkConstraint: SyncNetworkConstraint) {
        // Cancel any background data-change syncs waiting on constraints (e.g. Wi-Fi)
        workManager.cancelUniqueWork(ADHOC_WORK_NAME)
        scheduleOneOffSync(FORCE_SYNC_WORK_NAME, ExistingWorkPolicy.KEEP, networkConstraint)
    }

    override fun scheduleAdhocSync(networkConstraint: SyncNetworkConstraint) {
        scheduleOneOffSync(
            ADHOC_WORK_NAME, ExistingWorkPolicy.APPEND_OR_REPLACE, networkConstraint
        )
    }

    private fun <B : WorkRequest.Builder<B, *>> B.applyDefaultBackoff(): B {
        return setBackoffCriteria(
            backoffPolicy = BackoffPolicy.EXPONENTIAL,
            backoffDelay = INITIAL_BACKOFF_SECONDS,
            timeUnit = TimeUnit.SECONDS
        )
    }

    private fun <B : WorkRequest.Builder<B, *>> B.applyDefaultConstraints(networkConstraint: SyncNetworkConstraint): B {
        val networkType = when (networkConstraint) {
            SyncNetworkConstraint.NOT_REQUIRED -> NetworkType.NOT_REQUIRED
            SyncNetworkConstraint.CONNECTED -> NetworkType.CONNECTED
            SyncNetworkConstraint.UNMETERED -> NetworkType.UNMETERED
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(networkType)
            .build()

        return setConstraints(constraints)
    }

    companion object {
        const val SCHEDULED_WORK_NAME = "aisleron_periodic_sync"
        const val ADHOC_WORK_NAME = "aisleron_adhoc_sync"
        const val FORCE_SYNC_WORK_NAME = "aisleron_force_sync"

        private const val MIN_PERIODIC_INTERVAL_MINUTES = 15L
        private const val INITIAL_BACKOFF_SECONDS = 30L
    }
}