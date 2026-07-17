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

package com.aisleron.domain.sync.usecase

import com.aisleron.domain.sync.SyncSessionStatus
import com.aisleron.testdata.data.sync.SyncSessionManagerTestImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetSessionStatusUseCaseTest {
    private lateinit var sessionManager: SyncSessionManagerTestImpl
    private lateinit var getSessionStatusUseCase: GetSessionStatusUseCase

    @BeforeEach
    fun setUp() {
        sessionManager = SyncSessionManagerTestImpl(SyncSessionStatus.NotConfigured)
        getSessionStatusUseCase = GetSessionStatusUseCase(sessionManager)
    }

    @Test
    fun invoke_SessionStatusSet_ReturnsSessionStatus() = runTest {
        val result = getSessionStatusUseCase()
        assertEquals(SyncSessionStatus.NotConfigured, result.first())
    }

    @Test
    fun invoke_SessionStatusChanged_ReturnsUpdatedStatus() = runTest {
        sessionManager.setNewStatus(SyncSessionStatus.NotAuthenticated)
        sessionManager.refreshStatus()

        val result = getSessionStatusUseCase()

        assertEquals(SyncSessionStatus.NotAuthenticated, result.first())
    }
}