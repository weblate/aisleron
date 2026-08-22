/*
 * Copyright (C) 2025-2026 aisleron.com
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
import com.aisleron.data.product.ProductDao
import com.aisleron.data.product.ProductDto
import com.aisleron.data.product.ProductDtoMapper
import com.aisleron.data.sync.SupabaseClientProvider
import com.aisleron.data.sync.SupabaseSessionManagerImpl
import com.aisleron.data.sync.SupabaseSyncApi
import com.aisleron.data.sync.SyncApi
import com.aisleron.data.sync.SyncManager
import com.aisleron.data.sync.SyncRepository
import com.aisleron.data.sync.SyncRepositoryImpl
import com.aisleron.data.sync.SyncSchedulerImpl
import com.aisleron.data.sync.SyncWorker
import com.aisleron.domain.sync.SyncScheduler
import com.aisleron.domain.sync.SyncSessionManager
import kotlinx.coroutines.sync.Mutex
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.binds
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single
import org.koin.plugin.module.dsl.worker

private val noteSyncModule = module {
    single<NoteDtoMapper>()

    single<SyncApi<NoteDto>>(named("noteSyncApi")) {
        SupabaseSyncApi(
            clientProvider = get(),
            serializer = NoteDto.serializer(),
            entityName = "notes"
        )
    }

    single(named("noteSync")) {
        SyncRepositoryImpl(
            syncOrder = 100,
            dao = get<NoteDao>(),
            syncApi = get(named("noteSyncApi")),
            dtoMapper = get<NoteDtoMapper>()
        )
    } bind SyncRepository::class
}

private val productSyncModule = module {
    single<ProductDtoMapper>()

    single<SyncApi<ProductDto>>(named("productSyncApi")) {
        SupabaseSyncApi(
            clientProvider = get(),
            serializer = ProductDto.serializer(),
            entityName = "products"
        )
    }

    single(named("productSync")) {
        SyncRepositoryImpl(
            syncOrder = 200,
            dao = get<ProductDao>(),
            syncApi = get(named("productSyncApi")),
            dtoMapper = get<ProductDtoMapper>()
        )
    } bind SyncRepository::class

}

val syncModule = module {
    includes(noteSyncModule, productSyncModule)

    single<SupabaseSessionManagerImpl>() binds arrayOf(
        SyncSessionManager::class,
        SupabaseClientProvider::class
    )

    worker<SyncWorker>()
    single<SyncSchedulerImpl>() bind SyncScheduler::class

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