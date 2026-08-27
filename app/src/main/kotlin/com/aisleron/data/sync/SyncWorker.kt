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

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.aisleron.data.sync.SyncWorker.Companion.TAG
import com.aisleron.domain.log.Logger

class SyncWorker(
    context: Context,
    params: WorkerParameters,
    private val logger: Logger,
    private val syncManager: SyncManager
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result =
        syncManager.runSyncWork(logger)

    companion object {
        const val TAG = "SyncWorker"
    }
}

/**
 * This function is used to bridge [SyncManager] result with the Android specific
 * [SyncWorker] [ListenableWorker.Result]. This keeps any Android dependencies out of SyncManager,
 * and allows the logic to be tested without needing setup for WorkManager in tests.
 */
internal suspend fun SyncManager.runSyncWork(logger: Logger): ListenableWorker.Result {
    logger.d(TAG, "Sync worker started.")
    return syncAll().fold(
        onSuccess = {
            logger.d(TAG, "Sync worker completed successfully.")
            ListenableWorker.Result.success()
        },
        onFailure = { throwable ->
            logger.e(TAG, "Sync worker error, retry scheduled.", throwable)
            ListenableWorker.Result.retry()
        }
    )
}