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

import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.sync.SyncSessionStatus
import com.aisleron.testdata.data.sync.SyncSessionManagerTestImpl
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.assertThrows
import java.lang.Exception
import kotlin.coroutines.cancellation.CancellationException

class SignInWithEmailUseCaseTest {
    private lateinit var sessionManager: SyncSessionManagerTestImpl
    private lateinit var signInWithEmailUseCase: SignInWithEmailUseCase

    @BeforeEach
    fun setUp() {
        sessionManager = SyncSessionManagerTestImpl(SyncSessionStatus.NotConfigured)
        signInWithEmailUseCase = SignInWithEmailUseCase(sessionManager)
    }

    @Test
    fun invoke_IsInvalidLogin_ReturnError() = runTest {
        val errorMessage = "Login Error"
        val email = "email"
        val password = "password"

        sessionManager.failWith(
            AisleronException.AuthException(
                AisleronException.ExceptionCode.AUTH_EXCEPTION,
                errorMessage
            )
        )

        val result = signInWithEmailUseCase(email, password)

        assertTrue(result.isFailure)
        assertEquals(errorMessage, result.exceptionOrNull()?.message)

        assertEquals("", sessionManager.email)
        assertEquals("", sessionManager.password)
    }

    @Test
    fun invoke_GenericError_ReturnError() = runTest {
        val email = "email"
        val password = "password"

        sessionManager.failWith(Exception("Error"))

        val result = signInWithEmailUseCase(email, password)

        assertTrue(result.isFailure)

        val resultException = result.exceptionOrNull()
        assertInstanceOf<AisleronException.SignInException> (resultException)

        assertEquals("", sessionManager.email)
        assertEquals("", sessionManager.password)
    }

    @Test
    fun invoke_IsValidLogin_ReturnSuccess() = runTest {
        val email = "email"
        val password = "password"

        val result = signInWithEmailUseCase(email, password)

        assertEquals(email, sessionManager.email)
        assertEquals(password, sessionManager.password)
        assertTrue(result.isSuccess)
        assertNull(result.exceptionOrNull())
    }

    private fun assertMissingCredentialException(result: Result<Unit>) {
        assertTrue(result.isFailure)

        val resultException = result.exceptionOrNull()
        assertInstanceOf<AisleronException.MissingCredentialException>(resultException)
        assertEquals(
            AisleronException.ExceptionCode.MISSING_CREDENTIAL_EXCEPTION,
            resultException.exceptionCode
        )
    }

    @Test
    fun invoke_emailIsBlank_ReturnError() = runTest {
        val email = ""
        val password = "password"

        val result = signInWithEmailUseCase(email, password)

        assertMissingCredentialException(result)

        assertEquals("", sessionManager.email)
        assertEquals("", sessionManager.password)
    }

    @Test
    fun invoke_passwordIsBlank_ReturnError() = runTest {
        val email = "email"
        val password = ""

        val result = signInWithEmailUseCase(email, password)

        assertMissingCredentialException(result)

        assertEquals("", sessionManager.email)
        assertEquals("", sessionManager.password)
    }

    @Test
    fun invoke_IsCancellationException_ThrowsCancellationException() = runTest {
        sessionManager.failWith(CancellationException())

        assertThrows<CancellationException> {
            signInWithEmailUseCase("email", "password")
        }
    }
}