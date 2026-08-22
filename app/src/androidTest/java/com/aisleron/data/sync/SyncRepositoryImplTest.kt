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
import com.aisleron.data.note.NoteDtoMapper
import com.aisleron.data.note.NoteEntity
import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoTestModule
import com.aisleron.di.syncTestModule
import com.aisleron.testdata.data.note.NoteDaoTestImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SyncRepositoryImplTest : KoinTest {
    private lateinit var repository: SyncRepositoryImpl<NoteEntity, NoteDto>
    private lateinit var dao: NoteDaoTestImpl
    private lateinit var syncApi: SyncApiTestImpl<NoteDto>
    private lateinit var mapper: NoteDtoMapper

    @get:Rule
    val koinTestRule = KoinTestRule(modules = listOf(daoTestModule, syncTestModule))

    @Before
    fun setUp() {
        mapper = get()
        dao = get<NoteDao>() as NoteDaoTestImpl
        syncApi = (get<SyncApi<NoteDto>>() as SyncApiTestImpl<NoteDto>)
            .apply { initSyncApi() }

        repository = SyncRepositoryImpl(
            syncOrder = 1,
            dao = dao,
            syncApi = syncApi,
            dtoMapper = mapper
        )
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
    fun remoteEntityName_Called_ReturnsApiEntityName() = runTest {
        assertEquals(syncApi.entityName, repository.remoteEntityName)
    }

    /**
     * Push Tests
     * */

    @Test
    fun push_LocalNewOrModifiedEntitiesExist_PushesMappedDtoToApi() = runTest {
        val lastSyncTimestamp = 1000L
        val localEntity = getNoteEntity(lastModifiedAt = 1500L)
        val expectedDto = mapper.toDto(localEntity)

        repository.push(lastSyncTimestamp)

        assertEquals(listOf(expectedDto), syncApi.remoteDtoList)
    }

    @Test
    fun push_NoLocalModifiedEntities_DoesNotCallApiPush() = runTest {
        val lastSyncTimestamp = 1000L
        getNoteEntity(lastModifiedAt = 500L)

        repository.push(lastSyncTimestamp)

        assertTrue(syncApi.remoteDtoList.isEmpty())
    }

    @Test
    fun push_LocalTombstoneEntitiesExist_PushesDeleteDtoToApi() = runTest {
        val lastSyncTimestamp = 1000L
        val localEntity = getNoteEntity(lastModifiedAt = 1500L, isRemoved = true)
        val expectedDto = mapper.toDto(localEntity)

        repository.push(lastSyncTimestamp)

        assertEquals(listOf(expectedDto), syncApi.remoteDtoList)
    }

    /**
     * Pull Tests
     */

    @Test
    fun pull_RemoteNewOrUpdatedEntitiesExist_UpsertsEntitiesAndReturnsLatestTimestamp() = runTest {
        val lastSyncIso = "2026-08-18T00:00:00Z"
        val remoteDto1 = NoteDto(
            id = "remote-1",
            serverUpdatedAt = "2026-08-18T05:00:00Z",
            noteText = "Remote Note 1 - to be returned",
            isDeleted = false,
            clientUpdatedAt = "2026-08-17T05:00:00Z"
        )

        val remoteDto2 = NoteDto(
            id = "remote-2",
            serverUpdatedAt = "2026-08-18T10:00:00Z",
            noteText = "Remote Note 2 - to be returned",
            isDeleted = false,
            clientUpdatedAt = "2026-08-17T10:00:00Z"
        )

        val remoteDto3 = NoteDto(
            id = "remote-3",
            serverUpdatedAt = "2026-08-17T10:00:00Z",
            noteText = "Remote Note 3 - not to be returned",
            isDeleted = false,
            clientUpdatedAt = "2026-08-16T10:00:00Z"
        )

        syncApi.push(listOf(remoteDto1, remoteDto2, remoteDto3))

        val resultTimestamp = repository.pull(lastSyncIso)

        assertEquals(lastSyncIso, syncApi.fetchSinceArg)
        assertEquals("2026-08-18T10:00:00Z", resultTimestamp)

        assertEquals(2, dao.getNotes().size)
        assertNotNull(dao.getBySyncId("remote-1"))
        assertNotNull(dao.getBySyncId("remote-2"))
        assertNull(dao.getBySyncId("remote-3"))
    }

    @Test
    fun pull_NoRemoteEntitiesFound_ReturnsOriginalServerLastUpdatedDateIso() = runTest {
        val lastSyncIso = "2026-08-18T00:00:00Z"

        val remoteDto1 = NoteDto(
            id = "remote-1",
            serverUpdatedAt = "2026-08-17T05:00:00Z",
            noteText = "Remote Note 1 - not to be returned",
            isDeleted = false,
            clientUpdatedAt = "2026-08-17T05:00:00Z"
        )

        val remoteDto2 = NoteDto(
            id = "remote-2",
            serverUpdatedAt = "2026-08-17T10:00:00Z",
            noteText = "Remote Note 2 - not to be returned",
            isDeleted = false,
            clientUpdatedAt = "2026-08-17T10:00:00Z"
        )

        val remoteDto3 = NoteDto(
            id = "remote-3",
            serverUpdatedAt = "2026-08-17T10:00:00Z",
            noteText = "Remote Note 3 - not to be returned",
            isDeleted = false,
            clientUpdatedAt = "2026-08-16T10:00:00Z"
        )

        syncApi.push(listOf(remoteDto1, remoteDto2, remoteDto3))


        val resultTimestamp = repository.pull(lastSyncIso)

        assertEquals(lastSyncIso, syncApi.fetchSinceArg)
        assertEquals(lastSyncIso, resultTimestamp)
        assertTrue(dao.getNotes().isEmpty())
    }

    @Test
    fun pull_RemoteDtoHasNullServerUpdatedAt_ReturnsOriginalServerLastUpdatedDateIso() = runTest {
        val lastSyncIso = "2026-08-18T00:00:00Z"
        val remoteDto = NoteDto(
            id = "null-date-1",
            serverUpdatedAt = null,
            noteText = "Remote Note 1 - not to be returned",
            isDeleted = false,
            clientUpdatedAt = "2026-08-17T05:00:00Z"
        )

        syncApi.allowNullDates(true)
        syncApi.push(listOf(remoteDto))

        val resultTimestamp = repository.pull(lastSyncIso)

        assertEquals(lastSyncIso, resultTimestamp)
        assertEquals(1, dao.getNotes().size)
        assertNotNull(dao.getBySyncId("null-date-1"))
    }

    /**
     * Purge Removed Tests
     */

    @Test
    fun purgeRemoved_PurgeToDateProvided_CallsDaoPurgeRemoved() = runTest {
        val purgeToDate = 5000L
        val note = getNoteEntity(lastModifiedAt = 5000L, isRemoved = true)

        assertNotNull(dao.getNote(note.id, true))

        repository.purgeRemoved(purgeToDate)

        assertNull(dao.getNote(note.id, true))

    }
}