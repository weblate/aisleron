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

package com.aisleron.testdata.data.loyaltycard

import com.aisleron.data.loyaltycard.LoyaltyCardDao
import com.aisleron.data.loyaltycard.LoyaltyCardEntity
import com.aisleron.domain.loyaltycard.LoyaltyCardProviderType
import com.aisleron.testdata.data.base.BaseSyncTestDao

class LoyaltyCardDaoTestImpl(
    private val locationLoyaltyCardDao: LocationLoyaltyCardDaoTestImpl
) : BaseSyncTestDao<LoyaltyCardEntity>(), LoyaltyCardDao {

    override suspend fun getLoyaltyCard(
        loyaltyCardId: Int, includeRemoved: Boolean
    ): LoyaltyCardEntity? {
        return entityList.find { it.id == loyaltyCardId && (!it.isRemoved || includeRemoved) }
    }

    override suspend fun getProviderCard(
        provider: LoyaltyCardProviderType, intent: String
    ): LoyaltyCardEntity? {
        return activeItems.find { it.provider == provider && it.intent == intent }
    }

    override suspend fun getLoyaltyCards(): List<LoyaltyCardEntity> = activeItems

    override suspend fun getLoyaltyCardForLocation(locationId: Int): LoyaltyCardEntity? {
        return activeItems.find {
            it.id == locationLoyaltyCardDao.getLocationLoyaltyCard(
                locationId, false
            )?.loyaltyCardId
        }
    }

    override suspend fun toggleLocationLoyaltyCardRemove(
        loyaltyCardId: Int, isRemoved: Boolean, lastModifiedAt: Long
    ) {
        val entity = locationLoyaltyCardDao.getLocationLoyaltyCard(loyaltyCardId, true)?.copy(
            isRemoved = isRemoved,
            lastModifiedAt = lastModifiedAt
        ) ?: return

        locationLoyaltyCardDao.upsert(entity)
    }

    override fun getByNaturalKey(
        provider: LoyaltyCardProviderType, intent: String
    ): List<LoyaltyCardEntity> {
        return entityList.filter { it.provider == provider && it.intent == intent }
    }

    override suspend fun upsert(vararg entity: LoyaltyCardEntity): List<Long> {
        val result = mutableListOf<Long>()
        entity.forEach {
            val existingEntity = getLoyaltyCard(it.id, true)
            val id = existingEntity?.let {
                entityList.removeAt(entityList.indexOf(existingEntity))
                existingEntity.id
            } ?: ((entityList.maxOfOrNull { e -> e.id } ?: 0) + 1)

            val newEntity = LoyaltyCardEntity(
                id = id,
                name = it.name,
                provider = it.provider,
                intent = it.intent,
                lastModifiedAt = it.lastModifiedAt,
                isRemoved = it.isRemoved,
                syncId = it.syncId,
                serverUpdatedAt = it.serverUpdatedAt
            )

            entityList.add(newEntity)
            result.add(existingEntity?.let { -1 } ?: newEntity.id.toLong())
        }

        return result
    }

    override suspend fun upsert(entities: List<LoyaltyCardEntity>) {
        upsert(*entities.toTypedArray())
    }
}