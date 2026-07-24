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

/**
 * A safe alternative to [recoverCatching] that automatically propagates
 * coroutine cancellation instead of wrapping it.
 */
inline fun <T> Result<T>.recoverCatchingUnlessCancelled(
    transform: (exception: Throwable) -> T
): Result<T> {
    return when (val throwable = exceptionOrNull()) {
        null -> this
        is CancellationException -> throw throwable
        else -> runCatching { transform(throwable) }
    }
}

/**
 * A safe alternative to [runCatching] that automatically propagates
 * coroutine cancellation instead of wrapping it.
 */
inline fun <T, R> T.runCatchingUnlessCancelled(block: T.() -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }
}

/**
 * Ensures any failure in the [Result] is mapped to an [AisleronException].
 *
 * If the existing error is already an [AisleronException], it is preserved untouched.
 * Otherwise, [exceptionFactory] is invoked to construct a domain-specific fallback exception.
 *
 * Propagates [kotlinx.coroutines.CancellationException] automatically.
 */
inline fun <T> Result<T>.recoverCatchingWithAisleronException(
    crossinline exceptionFactory: (cause: Throwable) -> AisleronException
): Result<T> = recoverCatchingUnlessCancelled { throwable ->
    if (throwable is AisleronException) {
        throw throwable
    } else {
        throw exceptionFactory(throwable)
    }
}