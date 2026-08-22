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

import com.aisleron.data.note.NoteDao
import com.aisleron.data.note.NoteDto
import com.aisleron.data.note.NoteEntity
import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoTestModule
import com.aisleron.di.generalTestModule
import com.aisleron.di.preferenceTestModule
import com.aisleron.di.syncTestModule
import com.aisleron.domain.preferences.SyncServicePreference
import com.aisleron.domain.preferences.SyncStatusPreference
import com.aisleron.domain.preferences.syncpreferences.SyncPreferencesRepository
import com.aisleron.testdata.data.preferences.syncpreferences.SyncPreferencesRepositoryTestImpl
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.qualifier.named
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SyncManagerTest : KoinTest {
    private lateinit var syncManager: SyncManager
    private lateinit var api: SyncApiTestImpl<NoteDto>
    private lateinit var syncPreferencesRepository: SyncPreferencesRepositoryTestImpl
    private lateinit var dao: NoteDao

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(daoTestModule, syncTestModule, generalTestModule, preferenceTestModule)
    )

    @Before
    fun setUp() {
        syncManager = get()
        dao = get<NoteDao>()

        api = get<SyncApi<NoteDto>>(named("noteSyncApi")) as SyncApiTestImpl<NoteDto>
        api.initSyncApi()

        syncPreferencesRepository =
            get<SyncPreferencesRepository>() as SyncPreferencesRepositoryTestImpl

        syncPreferencesRepository.setSyncService(SyncServicePreference.CUSTOM_SERVICE)
        syncPreferencesRepository.setSyncStatus(SyncStatusPreference.NONE)
    }

    private suspend fun getNoteEntity(
        lastModifiedAt: Long = 0, serverUpdatedAt: Long? = null, isRemoved: Boolean = false
    ): NoteEntity {
        val noteText = "Note to test Sync"
        val noteEntity = NoteEntity(
            id = 0,
            noteText = noteText,
            lastModifiedAt = lastModifiedAt,
            serverUpdatedAt = serverUpdatedAt,
            isRemoved = isRemoved
        )

        val noteId = dao.upsert(noteEntity).first().toInt()

        return noteEntity.copy(id = noteId)
    }

    @Test
    fun syncAll_WhenServicePreferenceIsNotNone_ExecutesPushPullAndPurge() = runTest {
        val removedId = getNoteEntity(lastModifiedAt = 1000L, isRemoved = true).id
        assertNotNull(dao.getNote(removedId, true))

        val result = syncManager.syncAll()

        assertTrue(result.isSuccess)
        assertEquals(1, api.pushCallCount)
        assertEquals(1, api.fetchSinceCallCount)
        assertNull(dao.getNote(removedId, true))

        val prefs = syncPreferencesRepository.getSyncPreferences()
        assertEquals(SyncStatusPreference.SUCCESS, prefs.lastSyncStatus)
    }

    @Test
    fun syncAll_WhenServicePreferenceIsNone_ExecutesPurgeOnly() = runTest {
        syncPreferencesRepository.setSyncService(SyncServicePreference.NONE)
        val removedId = getNoteEntity(lastModifiedAt = 1000L, isRemoved = true).id
        assertNotNull(dao.getNote(removedId, true))

        val result = syncManager.syncAll()

        assertTrue(result.isSuccess)
        assertEquals(0, api.pushCallCount)
        assertEquals(0, api.fetchSinceCallCount)
        assertNull(dao.getNote(removedId, true))

        val prefs = syncPreferencesRepository.getSyncPreferences()
        assertEquals(SyncStatusPreference.SUCCESS, prefs.lastSyncStatus)
    }

    @Test
    fun syncAll_OnSuccess_ReturnsSuccessAndUpdatesStatusToSuccess() = runTest {
        val initialLastSyncedAt = 0L
        syncPreferencesRepository.setSyncStatus(initialLastSyncedAt, SyncStatusPreference.NONE)

        val result = syncManager.syncAll()

        assertTrue(result.isSuccess)

        val prefs = syncPreferencesRepository.getSyncPreferences()
        assertEquals(SyncStatusPreference.SUCCESS, prefs.lastSyncStatus)
        assertTrue(initialLastSyncedAt < prefs.lastSyncedAt)
    }

    @Test
    fun syncAll_OnFailure_ReturnsFailureAndUpdatesStatusToFailure() = runTest {
        val expectedLastSyncedAt = 1700000000L
        syncPreferencesRepository.setSyncStatus(expectedLastSyncedAt, SyncStatusPreference.NONE)
        val exceptionMessage = "Test Sync Manager Fail state"
        api.failWith(Exception(exceptionMessage))

        val result = syncManager.syncAll()

        assertTrue(result.isFailure)
        assertEquals(exceptionMessage, result.exceptionOrNull()?.message)

        val prefs = syncPreferencesRepository.getSyncPreferences()
        assertEquals(SyncStatusPreference.FAILURE, prefs.lastSyncStatus)
        assertEquals(expectedLastSyncedAt, prefs.lastSyncedAt)
    }

    @Test
    fun syncAll_OnPull_UpdatesRemoteEntityLastUpdatedIso() = runTest {
        val initialIso = syncPreferencesRepository.getRemoteEntityLastUpdatedIso("notes")

        val remoteDto1 = NoteDto(
            id = "remote-1",
            serverUpdatedAt = "2026-08-18T05:00:00Z",
            noteText = "Remote Note 1 - to be returned",
            isDeleted = false,
            clientUpdatedAt = "2026-08-17T05:00:00Z"
        )

        api.push(listOf(remoteDto1))

        syncManager.syncAll()

        val updatedIso = syncPreferencesRepository.getRemoteEntityLastUpdatedIso("notes")
        assertNotEquals(initialIso, updatedIso)
        assertEquals(remoteDto1.serverUpdatedAt, updatedIso)
    }

    @Test
    fun syncAll_WithGivenLastSyncedAt_PushesFromLastSyncedAt() = runTest {
        syncPreferencesRepository.setSyncStatus(1700L, SyncStatusPreference.NONE)

        getNoteEntity(lastModifiedAt = 1600L)
        getNoteEntity(lastModifiedAt = 1800L)
        assertEquals(2, dao.getNotes().size)

        val result = syncManager.syncAll()

        assertTrue(result.isSuccess)
        assertEquals(1, api.remoteDtoList.size)
    }

    @Test
    fun syncAll_WithGivenLastServerUpdatedDate_PullsFromLastServerUpdatedDate() = runTest {
        val expectedIso = "2026-01-01T00:00:00Z"
        syncPreferencesRepository.setRemoteEntityLastUpdatedIso("notes", expectedIso)

        val result = syncManager.syncAll()

        assertTrue(result.isSuccess)
        assertEquals(1, api.fetchSinceCallCount)
        assertEquals(expectedIso, api.fetchSinceArg)
    }
}