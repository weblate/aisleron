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

package com.aisleron.testdata.data.base

import com.aisleron.data.base.BaseDao
import com.aisleron.data.base.SyncEntity
import com.aisleron.data.sync.SyncDao

abstract class BaseSyncTestDao<Entity : SyncEntity> : BaseDao<Entity>, SyncDao<Entity> {
    protected val entityList = mutableListOf<Entity>()

    protected val activeItems: List<Entity> get() = entityList.filter { !it.isRemoved }


    override suspend fun getModified(modifiedAfterDate: Long): List<Entity> {
        return entityList.filter { it.lastModifiedAt > modifiedAfterDate }
    }

    override suspend fun getBySyncId(syncId: String): Entity? {
        return entityList.find { it.syncId == syncId }
    }

    override suspend fun purgeRemoved(purgeToDate: Long) {
        entityList.removeIf { it.lastModifiedAt <= purgeToDate && it.isRemoved }
    }

    override suspend fun delete(vararg entity: Entity) {
        entityList.removeIf { it in entity }
    }
}