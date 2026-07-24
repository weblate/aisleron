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

package com.aisleron.domain.base.extension


import com.aisleron.domain.base.AisleronException
import kotlinx.coroutines.CancellationException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.assertThrows

class ResultExtTest {

    private fun getRecoverCatchingUnlessCancelledFailureResult(throwable: Throwable): Result<Unit> =
        runCatching {
            throw throwable
        }.recoverCatchingUnlessCancelled { throwable ->
            throw Exception(
                "Recovered Catching", throwable
            )
        }

    @Test
    fun recoverCatchingUnlessCancelled_IsCancellationException_RethrowCancellationException() {
        assertThrows<CancellationException> {
            getRecoverCatchingUnlessCancelledFailureResult(CancellationException())
        }
    }

    @Test
    fun recoverCatchingUnlessCancelled_NotCancellationException_ReturnResult() {
        val exception = Exception("Force Fail")

        val result = getRecoverCatchingUnlessCancelledFailureResult(exception)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull()?.cause)
    }

    @Test
    fun recoverCatchingUnlessCancelled_NoException_ReturnResult() {
        val expectedValue = "Success Data"
        val successResult = Result.success(expectedValue)

        val actualResult = successResult.recoverCatchingUnlessCancelled {
            throw Exception("This should not execute")
        }

        assertTrue(actualResult.isSuccess)
        assertEquals(expectedValue, actualResult.getOrNull())
    }


    private fun getRunCatchingUnlessCancelledFailureResult(throwable: Throwable): Result<Unit> =
        runCatchingUnlessCancelled {
            throw throwable
        }

    @Test
    fun runCatchingUnlessCancelled_IsCancellationException_RethrowCancellationException() {
        assertThrows<CancellationException> {
            getRunCatchingUnlessCancelledFailureResult(CancellationException())
        }
    }

    @Test
    fun runCatchingUnlessCancelled_NotCancellationException_ReturnResult() {
        val exception = Exception("Force Fail")

        val result = getRunCatchingUnlessCancelledFailureResult(exception)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun runCatchingUnlessCancelled_NoException_ReturnResult() {
        val expectedValue = "Success Data"

        val actualResult = runCatchingUnlessCancelled {
            expectedValue
        }

        assertTrue(actualResult.isSuccess)
        assertEquals(expectedValue, actualResult.getOrNull())
    }

    @Test
    fun recoverCatchingWithAisleronException_IsAisleronException_ReturnSameException() {
        val exception = AisleronException.CredentialException("Credential Failure")

        val result = getRunCatchingUnlessCancelledFailureResult(exception)
            .recoverCatchingWithAisleronException { cause ->
                AisleronException.SignOutException(
                    message = "Error on signing out",
                    cause = cause
                )
            }

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun recoverCatchingWithAisleronException_IsNotAisleronException_ReturnMappedException() {
        val exception = Exception("Force Fail")

        val result = getRunCatchingUnlessCancelledFailureResult(exception)
            .recoverCatchingWithAisleronException { cause ->
                AisleronException.SignOutException(
                    message = "Error on signing out",
                    cause = cause
                )
            }

        assertTrue(result.isFailure)

        val resultException = result.exceptionOrNull()
        assertInstanceOf<AisleronException.SignOutException>(resultException)
        assertEquals(exception, resultException.cause)
    }
}