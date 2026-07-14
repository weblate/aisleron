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

import com.aisleron.domain.sync.SyncSessionManager
import com.aisleron.domain.sync.SyncSessionStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetSessionStatusUseCaseTest {
    private val sessionManager: SyncSessionManager = mockk()
    private lateinit var getSessionStatusUseCase: GetSessionStatusUseCase

    @BeforeEach
    fun setUp() {
        getSessionStatusUseCase = GetSessionStatusUseCase(sessionManager)
    }

    @Test
    fun invoke_SessionStatusSet_ReturnsSessionStatus() = runTest {
        coEvery {
            sessionManager.sessionStatus
        } returns flowOf(SyncSessionStatus.NotConfigured)

        val result = getSessionStatusUseCase()

        assertEquals(SyncSessionStatus.NotConfigured, result.single())
        coVerify(exactly = 1) {
            @Suppress("UnusedFlow")
            sessionManager.sessionStatus
        }
    }
}