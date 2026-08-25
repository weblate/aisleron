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

package com.aisleron.data.loyaltycard

import androidx.room.Dao
import androidx.room.Query
import com.aisleron.data.base.BaseDao
import com.aisleron.data.sync.SyncDao

@Dao
interface LocationLoyaltyCardDao :
    BaseDao<LocationLoyaltyCardEntity>,
    SyncDao<LocationLoyaltyCardEntity> {

    @Query(
        "SELECT * FROM LocationLoyaltyCard " +
                "WHERE locationId = :locationId AND (isRemoved = 0 OR :includeRemoved = 1)"
    )
    suspend fun getLocationLoyaltyCard(
        locationId: Int, includeRemoved: Boolean
    ): LocationLoyaltyCardEntity?

    /**
     * Sync Queries
     */
    @Query("SELECT * FROM LocationLoyaltyCard WHERE lastModifiedAt > :modifiedAfterDate")
    override suspend fun getModified(modifiedAfterDate: Long): List<LocationLoyaltyCardEntity>

    @Query("SELECT * FROM LocationLoyaltyCard WHERE syncId = :syncId")
    override suspend fun getBySyncId(syncId: String): LocationLoyaltyCardEntity?

    @Query("DELETE FROM LocationLoyaltyCard WHERE isRemoved = 1 AND lastModifiedAt <= :purgeToDate")
    override suspend fun purgeRemoved(purgeToDate: Long)
}