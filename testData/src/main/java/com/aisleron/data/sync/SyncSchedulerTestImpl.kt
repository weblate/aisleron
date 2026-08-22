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

import com.aisleron.domain.sync.SyncScheduler

class SyncSchedulerTestImpl : SyncScheduler {
    private var _scheduleType: ScheduleType = ScheduleType.NONE
    val scheduleType: ScheduleType get() = _scheduleType

    override fun schedulePeriodicSync(intervalMinutes: Long) {
        _scheduleType = ScheduleType.PERIODIC
    }

    override fun scheduleForceSync() {
        _scheduleType = ScheduleType.ONE_OFF_FORCE_SYNC
    }

    override fun scheduleAdhocSync() {
        _scheduleType = ScheduleType.ONE_OFF_ADHOC
    }

    enum class ScheduleType {
        NONE,
        PERIODIC,
        ONE_OFF_ADHOC,
        ONE_OFF_FORCE_SYNC
    }
}