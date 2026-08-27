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

import com.aisleron.domain.preferences.SyncServicePreference
import com.aisleron.domain.preferences.syncpreferences.SyncPreferencesRepository
import com.aisleron.domain.sync.SyncScheduler

class ScheduleAdhocSyncUseCase(
    private val syncPreferencesRepository: SyncPreferencesRepository,
    private val syncScheduler: SyncScheduler
) {
    operator fun invoke() {
        val syncPreferences = syncPreferencesRepository.getSyncPreferences()
        if (syncPreferences.syncServicePreference == SyncServicePreference.NONE) return

        val networkConstraint = syncPreferences.getRequiredNetworkConstraint(false)
        syncScheduler.scheduleAdhocSync(networkConstraint)
    }
}