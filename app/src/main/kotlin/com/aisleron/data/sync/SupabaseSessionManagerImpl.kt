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

import com.aisleron.domain.log.Logger
import com.aisleron.domain.preferences.syncpreferences.SyncPreferencesRepository
import com.aisleron.domain.sync.SyncSessionStatus
import com.aisleron.domain.sync.SyncSessionManager
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class SupabaseSessionManagerImpl(
    private val syncPreferencesRepository: SyncPreferencesRepository,
    private val clientFactory: SupabaseClientFactory,
    private val authDelegate: SupabaseAuthDelegate,
    private val logger: Logger
) : SyncSessionManager, SupabaseClientProvider {
    private var activeClient: SupabaseClient? = null
    private val clientRefreshTrigger = MutableStateFlow(System.currentTimeMillis())

    override val sessionStatus: Flow<SyncSessionStatus> = clientRefreshTrigger.flatMapLatest {
        getClientOrNull()?.let {
            authDelegate.getSessionStatusFlow(it).map { status ->
                when (status) {
                    is SessionStatus.Authenticated -> SyncSessionStatus.Authenticated(status.session.user?.email.orEmpty())
                    is SessionStatus.NotAuthenticated -> SyncSessionStatus.NotAuthenticated
                    is SessionStatus.Initializing -> SyncSessionStatus.Loading
                    is SessionStatus.RefreshFailure -> SyncSessionStatus.RefreshFailure
                }
            }
        } ?: flowOf(SyncSessionStatus.NotConfigured)
    }

    override fun refreshStatus() {
        clientRefreshTrigger.value = System.currentTimeMillis()
    }

    override suspend fun getClientOrNull(): SupabaseClient? {
        val syncPreferences = syncPreferencesRepository.getSyncPreferences()
        val savedUrl = syncPreferences.serviceUrl
        val savedKey = syncPreferences.serviceKey

        if (
            activeClient != null
            && activeClient?.supabaseUrl == savedUrl
            && activeClient?.supabaseKey == savedKey
        ) return activeClient

        activeClient?.let {
            try {
                it.close()
            } catch (e: Exception) {
                logger.e("SupabaseSessionManager", "Error closing old client configuration", e)
            } finally {
                activeClient = null
            }
        }

        if (savedUrl.isNotBlank() && savedKey.isNotBlank()) {
            try {
                logger.d("SupabaseSessionManager", "Provisioning client on demand...")
                activeClient = clientFactory.create(savedUrl, savedKey)
            } catch (e: Exception) {
                logger.e("SupabaseSessionManager", "Failed to build Supabase client", e)
                activeClient = null
            }
        }

        return activeClient
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> =
        runCatching {
            val client = getClientOrNull()
                ?: throw IllegalStateException("Failed to acquire client.")

            authDelegate.signInWithEmail(client, email, password)
        }.also {
            refreshStatus()
        }

    override suspend fun signOut(): Result<Unit> =
        runCatching {
            getClientOrNull()?.let { authDelegate.signOut(it) } ?: Unit
        }.also {
            refreshStatus()
        }
}