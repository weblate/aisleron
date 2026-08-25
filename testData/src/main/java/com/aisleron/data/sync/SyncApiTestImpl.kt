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

import kotlin.time.Instant

class SyncApiTestImpl<Dto : SyncDto>(override val entityName: String) : SyncApi<Dto> {
    private var _remoteDtoList = mutableListOf<Dto>()
    val remoteDtoList: List<Dto> get() = _remoteDtoList

    private var _fetchSinceArg: String = ""
    val fetchSinceArg: String get() = _fetchSinceArg

    private var _allowNullDates: Boolean = false

    private var _failWithException: Exception? = null

    private var _pushCallCount: Int = 0
    val pushCallCount: Int get() = _pushCallCount

    private var _fetchSinceCallCount: Int = 0
    val fetchSinceCallCount: Int get() = _fetchSinceCallCount

    override suspend fun push(dto: List<Dto>) {
        _pushCallCount += 1
        _failWithException?.let { throw it }

        _remoteDtoList.addAll(dto)
    }

    private fun getInstant(input: CharSequence) =
        Instant.parseOrNull(input) ?: Instant.fromEpochMilliseconds(0)

    override suspend fun fetchSince(lastUpdatedDateIso: String): List<Dto> {
        _fetchSinceCallCount += 1
        _fetchSinceArg = lastUpdatedDateIso
        _failWithException?.let { throw it }

        val thresholdInstant = getInstant(lastUpdatedDateIso)

        return _remoteDtoList.filter { dto ->
            dto.serverUpdatedAt?.let { isoString ->
                getInstant(isoString) > thresholdInstant
            } ?: _allowNullDates
        }
    }

    fun initSyncApi() {
        _remoteDtoList.clear()
        _allowNullDates = false
        _failWithException = null
        _fetchSinceArg = ""
        _pushCallCount = 0
        _fetchSinceCallCount = 0
    }

    fun allowNullDates(value: Boolean) {
        _allowNullDates = value
    }

    fun failWith(e: Exception?) {
        _failWithException = e
    }
}