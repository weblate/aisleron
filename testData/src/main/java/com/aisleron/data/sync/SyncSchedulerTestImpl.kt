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

import com.aisleron.domain.preferences.syncpreferences.SyncNetworkConstraint
import com.aisleron.domain.sync.SyncScheduler

class SyncSchedulerTestImpl : SyncScheduler {
    private var _scheduleType: ScheduleType = ScheduleType.NONE
    val scheduleType: ScheduleType get() = _scheduleType

    private lateinit var _networkConstraint: SyncNetworkConstraint
    val networkConstraint: SyncNetworkConstraint get() = _networkConstraint

    private var _intervalMinutes: Long = 0L
    val intervalMinutes: Long get() = _intervalMinutes

    override fun schedulePeriodicSync(
        networkConstraint: SyncNetworkConstraint, intervalMinutes: Long
    ) {
        _intervalMinutes = intervalMinutes
        _networkConstraint = networkConstraint
        _scheduleType = ScheduleType.PERIODIC
    }

    override fun scheduleForceSync(networkConstraint: SyncNetworkConstraint) {
        _networkConstraint = networkConstraint
        _scheduleType = ScheduleType.ONE_OFF_FORCE_SYNC
    }

    override fun scheduleAdhocSync(networkConstraint: SyncNetworkConstraint) {
        _networkConstraint = networkConstraint
        _scheduleType = ScheduleType.ONE_OFF_ADHOC
    }

    enum class ScheduleType {
        NONE,
        PERIODIC,
        ONE_OFF_ADHOC,
        ONE_OFF_FORCE_SYNC
    }
}