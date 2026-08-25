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

import com.aisleron.data.base.SyncEntity
import com.aisleron.data.location.LocationDao
import com.aisleron.data.location.LocationEntity
import com.aisleron.data.loyaltycard.LocationLoyaltyCardDao
import com.aisleron.data.loyaltycard.LocationLoyaltyCardDto
import com.aisleron.data.loyaltycard.LocationLoyaltyCardDtoMapper
import com.aisleron.data.loyaltycard.LocationLoyaltyCardEntity
import com.aisleron.data.loyaltycard.LoyaltyCardDao
import com.aisleron.data.loyaltycard.LoyaltyCardEntity
import com.aisleron.domain.FilterType
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.loyaltycard.LoyaltyCardProviderType
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.koin.test.get
import kotlin.test.assertFailsWith

class LocationLoyaltyCardSyncTest : SyncTest<LocationLoyaltyCardEntity, LocationLoyaltyCardDto>() {
    private val locationLoyaltyCardDao: LocationLoyaltyCardDao get() = dao as LocationLoyaltyCardDao

    override fun initSyncApi(): SyncApiTestImpl<LocationLoyaltyCardDto> =
        SyncApiTestImpl("location_loyalty_cards")

    override fun initMapper(): DtoMapper<LocationLoyaltyCardEntity, LocationLoyaltyCardDto> =
        LocationLoyaltyCardDtoMapper(get<LocationDao>(), get<LoyaltyCardDao>())

    override fun initDao(): SyncDao<LocationLoyaltyCardEntity> =
        get<LocationLoyaltyCardDao>()

    override suspend fun addEntity(
        lastModifiedAt: Long,
        serverUpdatedAt: Long?,
        isRemoved: Boolean
    ): LocationLoyaltyCardEntity =
        addLocationLoyaltyCardEntity(lastModifiedAt, serverUpdatedAt, isRemoved)

    private suspend fun addLocationLoyaltyCardEntity(
        lastModifiedAt: Long,
        serverUpdatedAt: Long?,
        isRemoved: Boolean,
        syncId: String? = SyncEntity.generateSyncId()
    ): LocationLoyaltyCardEntity {
        val locationId = addLocationEntity().id
        val loyaltyCardId = addLoyaltyCardEntity().id

        val entity = LocationLoyaltyCardEntity(
            isRemoved = isRemoved,
            lastModifiedAt = lastModifiedAt,
            serverUpdatedAt = serverUpdatedAt,
            syncId = syncId,
            locationId = locationId,
            loyaltyCardId = loyaltyCardId
        )

        val id = locationLoyaltyCardDao.upsert(entity).first().toInt()

        return entity.copy(locationId = id)
    }

    override suspend fun addDto(
        id: String,
        serverUpdatedAt: String?,
        clientUpdatedAt: String,
        isDeleted: Boolean
    ): LocationLoyaltyCardDto =
        addLocationLoyaltyCardDto(id, serverUpdatedAt, clientUpdatedAt, isDeleted)

    private suspend fun addLocationLoyaltyCardDto(
        id: String,
        serverUpdatedAt: String?,
        clientUpdatedAt: String,
        isDeleted: Boolean
    ): LocationLoyaltyCardDto {
        val locationId = addLocationEntity().syncId!!
        val loyaltyCardId = addLoyaltyCardEntity().syncId!!

        val dto = LocationLoyaltyCardDto(
            id = id,
            isDeleted = isDeleted,
            clientUpdatedAt = clientUpdatedAt,
            serverUpdatedAt = serverUpdatedAt,
            locationId = locationId,
            loyaltyCardId = loyaltyCardId,
        )

        syncApi.push(listOf(dto))

        return dto
    }

    override suspend fun validateDtoToEntity(
        dto: LocationLoyaltyCardDto, compareEntity: LocationLoyaltyCardEntity
    ): Boolean {
        val expectedEntity = mapper.fromDto(dto, null).copy(
            locationId = compareEntity.locationId
        )

        return expectedEntity == compareEntity
    }

    private suspend fun addLocationEntity(syncId: String? = SyncEntity.generateSyncId()): LocationEntity {
        val entity = LocationEntity(
            id = 0,
            name = "Location for Sync Test",
            rank = 1,
            expanded = true,
            type = LocationType.SHOP,
            defaultFilter = FilterType.NEEDED,
            pinned = false,
            showDefaultAisle = true,
            noteId = null,
            syncId = syncId,
            isRemoved = false,
        )

        val id = get<LocationDao>().upsert(entity).first().toInt()

        return entity.copy(id = id)
    }

    private suspend fun addLoyaltyCardEntity(syncId: String? = SyncEntity.generateSyncId()): LoyaltyCardEntity {
        val entity = LoyaltyCardEntity(
            id = 0,
            name = "Loyalty Card for Sync Test",
            syncId = syncId,
            provider = LoyaltyCardProviderType.CATIMA,
            intent = "Intent for Sync Test",
            isRemoved = false
        )

        val id = get<LoyaltyCardDao>().upsert(entity).first().toInt()

        return entity.copy(id = id)
    }

    @Test
    fun toDto_LocationNotFound_ThrowsException() = runTest {
        val entity = addLocationLoyaltyCardEntity(0, 0, isRemoved = false)
            .copy(locationId = -1)

        assertFailsWith<IllegalStateException> {
            mapper.toDto(entity)
        }
    }

    @Test
    fun toDto_LoyaltyCardNotFound_ThrowsException() = runTest {
        val entity = addLocationLoyaltyCardEntity(0, 0, isRemoved = false)
            .copy(loyaltyCardId = -1)

        assertFailsWith<IllegalStateException> {
            mapper.toDto(entity)
        }
    }

    @Test
    fun fromDto_LocationNotFound_ThrowsException() = runTest {
        val dto = addLocationLoyaltyCardDto(
            SyncEntity.generateSyncId(), "", "",
            isDeleted = false
        ).copy(locationId = SyncEntity.generateSyncId())

        assertFailsWith<IllegalStateException> {
            mapper.fromDto(dto, null)
        }
    }

    @Test
    fun fromDto_LoyaltyCardNotFound_ThrowsException() = runTest {
        val dto = addLocationLoyaltyCardDto(
            SyncEntity.generateSyncId(), "", "",
            isDeleted = false
        ).copy(loyaltyCardId = SyncEntity.generateSyncId())

        assertFailsWith<IllegalStateException> {
            mapper.fromDto(dto, null)
        }
    }

    @Test
    fun toDto_SyncIdNull_throwsException() = runTest {
        val entity = addLocationLoyaltyCardEntity(0, 0, isRemoved = false, syncId = null)

        assertFailsWith<IllegalStateException> {
            mapper.toDto(entity)
        }
    }
}