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

import com.aisleron.data.sync.SyncDto
import com.aisleron.domain.preferences.TrackingMode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    @SerialName("id") override val id: String,
    @SerialName("name") val name: String,
    @SerialName("in_stock") val inStock: Boolean,
    @SerialName("qty_needed") val qtyNeeded: Double,
    @SerialName("note_id") val noteId: String?,
    @SerialName("qty_increment") val qtyIncrement: Double,
    @SerialName("unit_of_measure") val unitOfMeasure: String,
    @SerialName("tracking_mode") val trackingMode: TrackingMode?,
    @SerialName("is_deleted") override val isDeleted: Boolean,
    @SerialName("client_updated_at") override val clientUpdatedAt: String,
    @SerialName("server_updated_at") override val serverUpdatedAt: String? = null
) : SyncDto