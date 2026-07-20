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

package com.aisleron.testdata.data.sync

import com.aisleron.domain.sync.SyncSessionManager
import com.aisleron.domain.sync.SyncSessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class SyncSessionManagerTestImpl(initialStatus: SyncSessionStatus) : SyncSessionManager {
    private var newStatus: SyncSessionStatus = SyncSessionStatus.NotConfigured
    private var _sessionStatus = MutableStateFlow(initialStatus)
    override val sessionStatus: Flow<SyncSessionStatus>
        get() = _sessionStatus

    private var _email: String = ""
    val email: String get() = _email

    private var _password: String = ""
    val password: String get() = _password

    private var _signedIn: Boolean = false
    val signedIn: Boolean get() = _signedIn

    private var _failWithException: Exception? = null

    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> {
        _failWithException?.let { return Result.failure(it) }

        _email = email
        _password = password
        _signedIn = true
        return Result.success(Unit)
    }

    override suspend fun signOut(): Result<Unit> {
        _failWithException?.let { return Result.failure(it) }

        _signedIn = false
        return Result.success(Unit)
    }

    override fun refreshStatus() {
        _sessionStatus.value = newStatus
    }

    fun setNewStatus(syncSessionStatus: SyncSessionStatus) {
        // Don't set the actual status in this method. This should be done with an explicit
        // call to refreshStatus
        newStatus = syncSessionStatus
    }

    fun failWith(e: Exception?) {
        _failWithException = e
    }

    fun setSignedIn(signedIn: Boolean) {
        _signedIn = signedIn
    }
}