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

package com.aisleron.testdata.data.aisle

import com.aisleron.data.aisle.AisleDao
import com.aisleron.data.aisle.AisleEntity
import com.aisleron.data.aisle.AisleWithProducts
import com.aisleron.testdata.data.aisleproduct.AisleProductDaoTestImpl
import com.aisleron.testdata.data.base.BaseSyncTestDao

class AisleDaoTestImpl(
    private val aisleProductDao: AisleProductDaoTestImpl
) : BaseSyncTestDao<AisleEntity>(), AisleDao {

    override suspend fun getAisle(aisleId: Int, includeRemoved: Boolean): AisleEntity? {
        return entityList.find { it.id == aisleId && (!it.isRemoved || includeRemoved) }
    }

    override suspend fun getAisles(): List<AisleEntity> = activeItems

    override suspend fun getAislesForLocation(locationId: Int): List<AisleEntity> {
        return activeItems.filter { it.locationId == locationId }
    }

    override suspend fun getDefaultAisles(): List<AisleEntity> {
        return activeItems.filter { it.isDefault }
    }

    override suspend fun getDefaultAisleFor(locationId: Int): AisleEntity? {
        return activeItems.find { it.locationId == locationId && it.isDefault }
    }

    override suspend fun getAisleWithProducts(aisleId: Int): AisleWithProducts {
        return AisleWithProducts(
            aisle = getAisle(aisleId, false)!!,
            products = aisleProductDao.getAisleProducts()
                .filter { ap -> ap.aisleProduct.aisleId == aisleId }
        )
    }

    override suspend fun getAislesWithProducts(): List<AisleWithProducts> {
        return activeItems.map {
            AisleWithProducts(
                aisle = it,
                products = aisleProductDao.getAisleProducts()
                    .filter { ap -> ap.aisleProduct.aisleId == it.id }
            )
        }
    }

    override fun getByNaturalKey(name: String, locationId: Int): List<AisleEntity> {
        return entityList.filter { it.name.equals(name, true) && it.locationId == locationId }
    }

    override suspend fun moveRanks(locationId: Int, fromRank: Int, lastModifiedAt: Long) {
        val locationAisles =
            entityList.filter { it.locationId == locationId && it.rank >= fromRank }

        locationAisles.forEach {
            val newAisle = it.copy(rank = it.rank + 1, lastModifiedAt = lastModifiedAt)
            entityList.removeAt(entityList.indexOf(it))
            entityList.add(newAisle)
        }
    }

    override suspend fun getMaxRank(locationId: Int): Int {
        return activeItems.filter { it.locationId == locationId && !it.isDefault }
            .maxOfOrNull { it.rank } ?: 0
    }

    override suspend fun upsert(vararg entity: AisleEntity): List<Long> {
        val result = mutableListOf<Long>()
        entity.forEach {
            val id: Int
            val existingEntity = getAisle(it.id, true)
            if (existingEntity == null) {
                id = (entityList.maxOfOrNull { a -> a.id } ?: 0) + 1
            } else {
                id = existingEntity.id
                delete(existingEntity)
            }

            val newEntity = AisleEntity(
                id = id,
                name = it.name,
                rank = it.rank,
                locationId = it.locationId,
                isDefault = it.isDefault,
                expanded = it.expanded,
                lastModifiedAt = it.lastModifiedAt,
                isRemoved = it.isRemoved,
                syncId = it.syncId,
                serverUpdatedAt = it.serverUpdatedAt
            )

            entityList.add(newEntity)
            result.add(newEntity.id.toLong())
        }

        return result
    }

    override suspend fun upsert(entities: List<AisleEntity>) {
        upsert(*entities.toTypedArray())
    }
}