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

package com.aisleron.di

import com.aisleron.data.note.NoteDao
import com.aisleron.data.note.NoteDto
import com.aisleron.data.note.NoteDtoMapper
import com.aisleron.data.sync.SyncApi
import com.aisleron.data.sync.SyncApiTestImpl
import com.aisleron.data.sync.SyncManager
import com.aisleron.data.sync.SyncRepository
import com.aisleron.data.sync.SyncRepositoryImpl
import com.aisleron.data.sync.SyncSchedulerTestImpl
import com.aisleron.domain.sync.SyncScheduler
import kotlinx.coroutines.sync.Mutex
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

val syncTestModule = module {
    single<NoteDtoMapper>()
    single<SyncApi<NoteDto>>(named("noteSyncApi")) {
        SyncApiTestImpl("notes")
    }

    single(named("noteSync")) {
        SyncRepositoryImpl(
            syncOrder = 100,
            dao = get<NoteDao>(),
            syncApi = get(named("noteSyncApi")),
            dtoMapper = get<NoteDtoMapper>()
        )
    } bind SyncRepository::class

    single<SyncSchedulerTestImpl>() bind SyncScheduler::class

    single(named("SyncMutex")) { Mutex() }
    single<SyncManager> {
        SyncManager(
            repositories = getAll(),
            syncPreferencesRepository = get(),
            mutex = get(named("SyncMutex")),
            logger = get()
        )
    }
}