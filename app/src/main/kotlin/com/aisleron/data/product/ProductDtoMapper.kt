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

package com.aisleron.data.product

import com.aisleron.data.note.NoteDao
import com.aisleron.data.sync.DtoMapper
import kotlin.time.Instant

class ProductDtoMapper(private val noteDao: NoteDao) : DtoMapper<ProductEntity, ProductDto> {
    override suspend fun toDto(entity: ProductEntity): ProductDto {
        val noteSyncId = entity.noteId?.let { localId -> noteDao.getNote(localId, false)?.syncId }

        return ProductDto(
            id = checkNotNull(entity.syncId) { "syncId must be generated prior to push" },
            name = entity.name,
            inStock = entity.inStock,
            qtyNeeded = entity.qtyNeeded,
            noteId = noteSyncId,
            qtyIncrement = entity.qtyIncrement,
            unitOfMeasure = entity.unitOfMeasure,
            trackingMode = entity.trackingMode,
            isDeleted = entity.isRemoved,
            clientUpdatedAt = Instant.fromEpochMilliseconds(entity.lastModifiedAt).toString()
        )
    }

    override suspend fun fromDto(dto: ProductDto, existing: ProductEntity?): ProductEntity {
        val localNoteId = dto.noteId?.let { remoteSyncId -> noteDao.getBySyncId(remoteSyncId)?.id }

        return ProductEntity(
            id = existing?.id ?: 0,
            name = dto.name,
            inStock = dto.inStock,
            qtyNeeded = dto.qtyNeeded,
            noteId = localNoteId,
            qtyIncrement = dto.qtyIncrement,
            unitOfMeasure = dto.unitOfMeasure,
            trackingMode = dto.trackingMode,
            syncId = dto.id,
            isRemoved = dto.isDeleted,
            lastModifiedAt = Instant.parse(dto.clientUpdatedAt).toEpochMilliseconds(),
            serverUpdatedAt = dto.serverUpdatedAt?.let { Instant.parse(it).toEpochMilliseconds() }
        )
    }
}