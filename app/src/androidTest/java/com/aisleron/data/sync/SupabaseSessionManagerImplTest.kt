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

import com.aisleron.domain.preferences.SyncPreferencesRepository
import io.github.jan.supabase.SupabaseClient
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SupabaseSessionManagerImplTest {
    private val syncPreferencesRepository: SyncPreferencesRepository = mockk()
    private val clientFactory: SupabaseClientFactory = mockk()
    private val authDelegate: SupabaseAuthDelegate = mockk()
    private val mockSupabaseClient: SupabaseClient = mockk(relaxed = true)

    private lateinit var sessionManager: SupabaseSessionManagerImpl

    @Before
    fun setUp() {
        sessionManager =
            SupabaseSessionManagerImpl(syncPreferencesRepository, clientFactory, authDelegate)
    }

    private fun initPreferences(serviceUrl: String, serviceKey: String) {
        every { syncPreferencesRepository.getServiceUrl() } returns serviceUrl
        every { syncPreferencesRepository.getServiceKey() } returns serviceKey
    }

    private fun initMocks(serviceUrl: String, serviceKey: String) {
        initPreferences(serviceUrl, serviceKey)
        every {
            clientFactory.create(serviceUrl, serviceKey)
        } returns mockSupabaseClient
    }

    @Test
    fun getClientOrNull_ValidCredentialsForNewClient_ReturnsClient() {
        val serviceUrl = "https://example.supabase.co"
        val serviceKey = "some-valid-key"
        initMocks(serviceUrl, serviceKey)

        val client = sessionManager.getClientOrNull()

        assertNotNull(client)
        assertEquals(mockSupabaseClient, client)
        verify(exactly = 1) { clientFactory.create(serviceUrl, serviceKey) }
    }

    @Test
    fun getClientOrNull_BlankCredentials_ReturnsNull() {
        val serviceUrl = ""
        val serviceKey = ""
        initPreferences(serviceUrl, serviceKey)

        val client = sessionManager.getClientOrNull()

        assertNull(client)
        verify(exactly = 0) { clientFactory.create(serviceUrl, serviceKey) }
    }

    @Test
    fun getClientOrNull_ErrorWithClient_ReturnsNull() {
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

    @Test
    fun getClientOrNull_ClientExists_ReturnsExistingClient() {
        val serviceUrl = "https://example.supabase.co"
        val serviceKey = "some-valid-key"
        initPreferences(serviceUrl, serviceKey)
        every {
            clientFactory.create(serviceUrl, serviceKey)
        } returns mockk<SupabaseClient>(relaxed = true)

        val client1 = sessionManager.getClientOrNull()
        val client2 = sessionManager.getClientOrNull()

        assertEquals(client1, client2)
        verify(exactly = 1) { clientFactory.create(serviceUrl, serviceKey) }
    }

    @Test
    fun signInWithEmail_SuccessfulAuth_ReturnsSuccess() = runTest {
        val serviceUrl = "https://example.supabase.co"
        val serviceKey = "some-valid-key"
        initMocks(serviceUrl, serviceKey)
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
    fun signInWithEmail_AuthThrowsException_ReturnsFailureAndClosesClient() = runTest {
        val serviceUrl = "https://example.supabase.co"
        val serviceKey = "some-valid-key"
        initMocks(serviceUrl, serviceKey)
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

        coVerify(exactly = 1) { mockSupabaseClient.close() }
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
        val serviceUrl = "https://example.supabase.co"
        val serviceKey = "some-valid-key"
        initMocks(serviceUrl, serviceKey)

        coEvery {
            authDelegate.signOut(mockSupabaseClient)
        } returns Unit

        val result = sessionManager.signOut()

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { authDelegate.signOut(mockSupabaseClient) }
        coVerify(exactly = 1) { mockSupabaseClient.close() }
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
}