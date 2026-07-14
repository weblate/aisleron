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

import com.aisleron.domain.preferences.syncpreferences.SyncPreferences
import com.aisleron.domain.preferences.syncpreferences.SyncPreferencesRepository
import com.aisleron.domain.sync.SyncSessionStatus
import com.aisleron.testdata.data.log.LoggerTestImpl
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.status.RefreshFailureCause
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SupabaseSessionManagerImplTest {
    private val syncPreferencesRepository: SyncPreferencesRepository = mockk()
    private val clientFactory: SupabaseClientFactory = mockk()
    private val authDelegate: SupabaseAuthDelegate = mockk()
    private val mockSupabaseClient: SupabaseClient = mockk()
    private lateinit var sessionManager: SupabaseSessionManagerImpl

    @BeforeEach
    fun setUp() {
        sessionManager =
            SupabaseSessionManagerImpl(
                syncPreferencesRepository, clientFactory, authDelegate, LoggerTestImpl()
            )
    }

    private fun initPreferences(serviceUrl: String, serviceKey: String) {
        every { syncPreferencesRepository.getSyncPreferences() } returns SyncPreferences(
            useDefaultService = false,
            serviceUrl = serviceUrl,
            serviceKey = serviceKey
        )
    }

    private fun initMocks(serviceUrl: String, serviceKey: String) {
        initPreferences(serviceUrl, serviceKey)
        every {
            clientFactory.create(serviceUrl, serviceKey)
        } returns mockSupabaseClient
    }

    @Test
    fun getClientOrNull_ValidCredentialsForNewClient_ReturnsClient() = runTest {
        val serviceUrl = "https://example.supabase.co"
        val serviceKey = "some-valid-key"
        initMocks(serviceUrl, serviceKey)

        val client = sessionManager.getClientOrNull()

        assertNotNull(client)
        assertEquals(mockSupabaseClient, client)
        verify(exactly = 1) { clientFactory.create(serviceUrl, serviceKey) }
    }

    @Test
    fun getClientOrNull_BlankUrl_ReturnsNull() = runTest {
        val serviceUrl = ""
        val serviceKey = "some-valid-key"
        initPreferences(serviceUrl, serviceKey)

        val client = sessionManager.getClientOrNull()

        assertNull(client)
        verify(exactly = 0) { clientFactory.create(serviceUrl, serviceKey) }
    }

    @Test
    fun getClientOrNull_BlankKey_ReturnsNull() = runTest {
        val serviceUrl = "https://example.supabase.co"
        val serviceKey = ""
        initPreferences(serviceUrl, serviceKey)

        val client = sessionManager.getClientOrNull()

        assertNull(client)
        verify(exactly = 0) { clientFactory.create(serviceUrl, serviceKey) }
    }

    @Test
    fun getClientOrNull_ErrorWithClient_ReturnsNull() = runTest {
        val serviceUrl = "https://example.supabase.co"
        val serviceKey = "some-valid-key"
        initPreferences(serviceUrl, serviceKey)
        every {
            clientFactory.create(serviceUrl, serviceKey)
        } throws RuntimeException("Network Error")

        val client = sessionManager.getClientOrNull()

        assertNull(client)
        verify(exactly = 1) { clientFactory.create(serviceUrl, serviceKey) }
    }

    private fun getMockClient(
        url: String, key: String, errorOnClose: Boolean = false
    ): SupabaseClient {
        val client = mockk<SupabaseClient> {
            every { supabaseUrl } returns url
            every { supabaseKey } returns key
        }

        if (errorOnClose)
            coEvery { client.close() } throws RuntimeException("Network Error")
        else
            coEvery { client.close() } returns Unit
        return client
    }

    @Test
    fun getClientOrNull_ClientExists_ReturnsExistingClient() = runTest {
        val serviceUrl = "https://example.supabase.co"
        val serviceKey = "some-valid-key"
        initPreferences(serviceUrl, serviceKey)
        coEvery {
            clientFactory.create(serviceUrl, serviceKey)
        } returns getMockClient(serviceUrl, serviceKey)

        val client1 = sessionManager.getClientOrNull()
        val client2 = sessionManager.getClientOrNull()

        assertEquals(client1, client2)
        coVerify(exactly = 1) { clientFactory.create(serviceUrl, serviceKey) }
    }

    @Test
    fun getClientOrNull_HasDifferentAttributes_ReturnsNewClient() = runTest {
        val url1 = "https://one.supabase.co"
        val key1 = "some-valid-key-one"
        initPreferences(url1, key1)
        coEvery {
            clientFactory.create(url1, key1)
        } returns getMockClient(url1, key1)

        val client1 = sessionManager.getClientOrNull()

        val url2 = "https://two.supabase.co"
        val key2 = "some-valid-key-two"
        initPreferences(url2, key2)
        coEvery {
            clientFactory.create(url2, key2)
        } returns getMockClient(url2, key2)

        val client2 = sessionManager.getClientOrNull()

        assertNotEquals(client1, client2)
        coVerify(exactly = 1) { clientFactory.create(url1, key1) }
        coVerify(exactly = 1) { clientFactory.create(url2, key2) }
        coVerify(exactly = 1) { client1?.close() }
    }

    @Test
    fun signInWithEmail_SuccessfulAuth_ReturnsSuccess() = runTest {
        initMocks("https://example.supabase.co", "some-valid-key")
        coEvery {
            authDelegate.signInWithEmail(
                mockSupabaseClient, "test@example.com", "password123"
            )
        } returns Unit

        val result = sessionManager.signInWithEmail("test@example.com", "password123")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            authDelegate.signInWithEmail(
                mockSupabaseClient, "test@example.com", "password123"
            )
        }
    }

    @Test
    fun signInWithEmail_AuthThrowsException_ReturnsFailure() = runTest {
        initMocks("https://example.supabase.co", "some-valid-key")
        coEvery {
            authDelegate.signInWithEmail(
                mockSupabaseClient, "test@example.com", "password123"
            )
        } throws RuntimeException("Network Error")

        val result = sessionManager.signInWithEmail("test@example.com", "password123")

        assertTrue(result.isFailure)
        coVerify(exactly = 1) {
            authDelegate.signInWithEmail(
                mockSupabaseClient, "test@example.com", "password123"
            )
        }
    }

    @Test
    fun signInWithEmail_ClientAcquisitionError_ReturnsFailure() = runTest {
        val serviceUrl = "https://example.supabase.co"
        val serviceKey = "some-valid-key"
        initPreferences(serviceUrl, serviceKey)
        every {
            clientFactory.create(serviceUrl, serviceKey)
        } throws RuntimeException("Network Error")

        val result = sessionManager.signInWithEmail("test@example.com", "password123")

        assertTrue(result.isFailure)
    }

    @Test
    fun signOut_SuccessfulSignOut_ReturnsSuccess() = runTest {
        initMocks("https://example.supabase.co", "some-valid-key")

        coEvery {
            authDelegate.signOut(mockSupabaseClient)
        } returns Unit

        val result = sessionManager.signOut()

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { authDelegate.signOut(mockSupabaseClient) }
    }

    @Test
    fun signOut_ClientIsNull_SignOutNotCalled() = runTest {
        val serviceUrl = ""
        val serviceKey = ""
        initMocks(serviceUrl, serviceKey)

        val result = sessionManager.signOut()

        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { authDelegate.signOut(mockSupabaseClient) }
    }

    private suspend fun validateStatus_ArrangeActAssert(
        supabaseSessionStatus: SessionStatus, syncSessionStatus: SyncSessionStatus
    ) {
        initMocks("https://example.supabase.co", "some-valid-key")

        coEvery {
            authDelegate.getSessionStatusFlow(mockSupabaseClient)
        } returns flowOf(supabaseSessionStatus)

        sessionManager.refreshStatus()

        val resultSessionStatus: SyncSessionStatus = sessionManager.sessionStatus.first()
        assertEquals(syncSessionStatus, resultSessionStatus)
        coVerify(exactly = 1) {
            @Suppress("UnusedFlow")
            authDelegate.getSessionStatusFlow(mockSupabaseClient)
        }
    }

    @Test
    fun sessionStatus_AuthStatusIsAuthenticated_SyncSessionStatusIsAuthenticated() = runTest {
        val userEmail = "a@b.c"

        val userInfo: UserInfo = mockk {
            every { email } returns userEmail
        }

        val userSession: UserSession = mockk {
            every { user } returns userInfo
        }

        validateStatus_ArrangeActAssert(
            supabaseSessionStatus = SessionStatus.Authenticated(userSession),
            syncSessionStatus = SyncSessionStatus.Authenticated(userEmail)
        )
    }

    @Test
    fun sessionStatus_AuthStatusIsNotAuthenticated_SyncSessionStatusIsNotAuthenticated() = runTest {
        validateStatus_ArrangeActAssert(
            supabaseSessionStatus = SessionStatus.NotAuthenticated(),
            syncSessionStatus = SyncSessionStatus.NotAuthenticated
        )
    }

    @Test
    fun sessionStatus_AuthStatusIsInitializing_SyncSessionStatusIsLoading() = runTest {
        validateStatus_ArrangeActAssert(
            supabaseSessionStatus = SessionStatus.Initializing,
            syncSessionStatus = SyncSessionStatus.Loading
        )
    }

    @Test
    fun sessionStatus_AuthStatusIsRefreshFailure_SyncSessionStatusIsRefreshFailure() = runTest {
        val cause = RefreshFailureCause.NetworkError(Exception())

        validateStatus_ArrangeActAssert(
            supabaseSessionStatus = SessionStatus.RefreshFailure(cause),
            syncSessionStatus = SyncSessionStatus.RefreshFailure
        )
    }

    @Test
    fun sessionStatus_ClientIsNull_SyncSessionStatusIsNotConfigured() = runTest {
        initMocks("", "")

        sessionManager.refreshStatus()

        val resultSessionStatus: SyncSessionStatus = sessionManager.sessionStatus.first()
        assertEquals(SyncSessionStatus.NotConfigured, resultSessionStatus)
    }
}