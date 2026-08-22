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

package com.aisleron.data.note

import com.aisleron.data.sync.DtoMapper
import kotlin.time.Instant

class NoteDtoMapper : DtoMapper<NoteEntity, NoteDto> {
    override suspend fun toDto(entity: NoteEntity): NoteDto = NoteDto(
        id = checkNotNull(entity.syncId) { "syncId must be generated prior to push" },
        isDeleted = entity.isRemoved,
        clientUpdatedAt = Instant.fromEpochMilliseconds(entity.lastModifiedAt).toString(),
        noteText = entity.noteText
    )

    override suspend fun fromDto(dto: NoteDto, existing: NoteEntity?): NoteEntity = NoteEntity(
        id = existing?.id ?: 0,
        noteText = dto.noteText,
        syncId = dto.id,
        isRemoved = dto.isDeleted,
        lastModifiedAt = Instant.parse(dto.clientUpdatedAt).toEpochMilliseconds(),
        serverUpdatedAt = dto.serverUpdatedAt?.let { Instant.parse(it).toEpochMilliseconds() }
    )
}
