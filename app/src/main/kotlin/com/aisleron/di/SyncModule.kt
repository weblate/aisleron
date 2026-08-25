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

import com.aisleron.data.aisle.AisleDao
import com.aisleron.data.aisle.AisleDto
import com.aisleron.data.aisle.AisleDtoMapper
import com.aisleron.data.aisleproduct.AisleProductDao
import com.aisleron.data.aisleproduct.AisleProductDto
import com.aisleron.data.aisleproduct.AisleProductDtoMapper
import com.aisleron.data.location.LocationDao
import com.aisleron.data.location.LocationDto
import com.aisleron.data.location.LocationDtoMapper
import com.aisleron.data.loyaltycard.LocationLoyaltyCardDao
import com.aisleron.data.loyaltycard.LocationLoyaltyCardDto
import com.aisleron.data.loyaltycard.LocationLoyaltyCardDtoMapper
import com.aisleron.data.loyaltycard.LoyaltyCardDao
import com.aisleron.data.loyaltycard.LoyaltyCardDto
import com.aisleron.data.loyaltycard.LoyaltyCardDtoMapper
import com.aisleron.data.note.NoteDao
import com.aisleron.data.note.NoteDto
import com.aisleron.data.note.NoteDtoMapper
import com.aisleron.data.product.ProductDao
import com.aisleron.data.product.ProductDto
import com.aisleron.data.product.ProductDtoMapper
import com.aisleron.data.productvariant.ProductVariantDao
import com.aisleron.data.productvariant.ProductVariantDto
import com.aisleron.data.productvariant.ProductVariantDtoMapper
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

private val locationSyncModule = module {
    single<LocationDtoMapper>()

    single<SyncApi<LocationDto>>(named("locationSyncApi")) {
        SupabaseSyncApi(
            clientProvider = get(),
            serializer = LocationDto.serializer(),
            entityName = "locations"
        )
    }

    single(named("locationSync")) {
        SyncRepositoryImpl(
            syncOrder = 200,
            dao = get<LocationDao>(),
            syncApi = get(named("locationSyncApi")),
            dtoMapper = get<LocationDtoMapper>()
        )
    } bind SyncRepository::class
}

private val aisleSyncModule = module {
    single<AisleDtoMapper> { AisleDtoMapper(get()) }

    single<SyncApi<AisleDto>>(named("aisleSyncApi")) {
        SupabaseSyncApi(
            clientProvider = get(),
            serializer = AisleDto.serializer(),
            entityName = "aisles"
        )
    }

    single(named("aisleSync")) {
        SyncRepositoryImpl(
            syncOrder = 300,
            dao = get<AisleDao>(),
            syncApi = get(named("aisleSyncApi")),
            dtoMapper = get<AisleDtoMapper>()
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
            syncOrder = 400,
            dao = get<ProductDao>(),
            syncApi = get(named("productSyncApi")),
            dtoMapper = get<ProductDtoMapper>()
        )
    } bind SyncRepository::class
}

private val aisleProductSyncModule = module {
    single<AisleProductDtoMapper> { AisleProductDtoMapper(get(), get()) }

    single<SyncApi<AisleProductDto>>(named("aisleProductSyncApi")) {
        SupabaseSyncApi(
            clientProvider = get(),
            serializer = AisleProductDto.serializer(),
            entityName = "aisle_products"
        )
    }

    single(named("aisleProductSync")) {
        SyncRepositoryImpl(
            syncOrder = 500,
            dao = get<AisleProductDao>(),
            syncApi = get(named("aisleProductSyncApi")),
            dtoMapper = get<AisleProductDtoMapper>()
        )
    } bind SyncRepository::class
}

private val productVariantSyncModule = module {
    single<ProductVariantDtoMapper> { ProductVariantDtoMapper(get()) }

    single<SyncApi<ProductVariantDto>>(named("productVariantSyncApi")) {
        SupabaseSyncApi(
            clientProvider = get(),
            serializer = ProductVariantDto.serializer(),
            entityName = "product_variants"
        )
    }

    single(named("productVariantSync")) {
        SyncRepositoryImpl(
            syncOrder = 600,
            dao = get<ProductVariantDao>(),
            syncApi = get(named("productVariantSyncApi")),
            dtoMapper = get<ProductVariantDtoMapper>()
        )
    } bind SyncRepository::class
}

private val loyaltyCardSyncModule = module {
    single<LoyaltyCardDtoMapper>()

    single<SyncApi<LoyaltyCardDto>>(named("loyaltyCardSyncApi")) {
        SupabaseSyncApi(
            clientProvider = get(),
            serializer = LoyaltyCardDto.serializer(),
            entityName = "loyalty_cards"
        )
    }

    single(named("loyaltyCardSync")) {
        SyncRepositoryImpl(
            syncOrder = 700,
            dao = get<LoyaltyCardDao>(),
            syncApi = get(named("loyaltyCardSyncApi")),
            dtoMapper = get<LoyaltyCardDtoMapper>()
        )
    } bind SyncRepository::class
}

private val locationLoyaltyCardSyncModule = module {
    single<LocationLoyaltyCardDtoMapper> { LocationLoyaltyCardDtoMapper(get(), get()) }

    single<SyncApi<LocationLoyaltyCardDto>>(named("locationLoyaltyCardSyncApi")) {
        SupabaseSyncApi(
            clientProvider = get(),
            serializer = LocationLoyaltyCardDto.serializer(),
            entityName = "location_loyalty_cards"
        )
    }

    single(named("locationLoyaltyCardSync")) {
        SyncRepositoryImpl(
            syncOrder = 800,
            dao = get<LocationLoyaltyCardDao>(),
            syncApi = get(named("locationLoyaltyCardSyncApi")),
            dtoMapper = get<LocationLoyaltyCardDtoMapper>()
        )
    } bind SyncRepository::class
}

val syncModule = module {
    includes(
        noteSyncModule,
        locationSyncModule,
        aisleSyncModule,
        productSyncModule,
        aisleProductSyncModule,
        productVariantSyncModule,
        loyaltyCardSyncModule,
        locationLoyaltyCardSyncModule
    )

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