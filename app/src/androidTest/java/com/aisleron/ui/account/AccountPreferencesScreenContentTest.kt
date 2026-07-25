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

package com.aisleron.ui.account

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.test.platform.app.InstrumentationRegistry
import com.aisleron.R
import com.aisleron.domain.sync.SyncSessionStatus
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class AccountPreferencesScreenContentTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun accountPreferences_AuthenticatedState_DisplaysSignOutAndUserInfo() = runComposeUiTest {
        val user = "user@example.com"
        val signOutText = context.getString(R.string.sign_out)
        val statusText = context.getString(R.string.sync_status_signed_in_as, user)

        val state = AccountPreferencesUiState(
            serviceUrl = "https://sync.aisleron.com",
            sessionStatus = SyncSessionStatus.Authenticated(userId = user)
        )

        setContent {
            AccountPreferencesScreenContent(
                state = state,
                snackbarHostState = SnackbarHostState(),
                onSaveSyncService = { _, _ -> },
                onSignInPressed = {},
                onSignOutPressed = {},
                onSyncOnMobileDataChanged = {}
            )
        }

        onNodeWithText(signOutText).assertIsDisplayed()
        onNodeWithText(statusText, substring = true).assertIsDisplayed()
    }

    @Test
    fun accountPreferences_UnconfiguredState_DisablesSignInOption() = runComposeUiTest {
        val signInText = context.getString(R.string.sign_in)
        val statusText = context.getString(R.string.preference_none)

        val state = AccountPreferencesUiState(
            serviceUrl = "",
            sessionStatus = SyncSessionStatus.NotConfigured
        )

        setContent {
            AccountPreferencesScreenContent(
                state = state,
                snackbarHostState = SnackbarHostState(),
                onSaveSyncService = { _, _ -> },
                onSignInPressed = {},
                onSignOutPressed = {},
                onSyncOnMobileDataChanged = {}
            )
        }

        onNodeWithText(statusText).assertIsDisplayed()
        onNodeWithText(signInText).assertIsNotEnabled()
    }

    @Test
    fun accountPreferences_ClickSignIn_InvokesSignInCallback() = runComposeUiTest {
        val signInText = context.getString(R.string.sign_in)
        var signInClicked = false
        val state = AccountPreferencesUiState(
            serviceUrl = "https://sync.aisleron.com",
            sessionStatus = SyncSessionStatus.NotAuthenticated
        )

        setContent {
            AccountPreferencesScreenContent(
                state = state,
                snackbarHostState = SnackbarHostState(),
                onSaveSyncService = { _, _ -> },
                onSignInPressed = { signInClicked = true },
                onSignOutPressed = {},
                onSyncOnMobileDataChanged = {}
            )
        }

        onNodeWithText(signInText)
            .assertIsEnabled()
            .performClick()

        assertTrue(signInClicked)
    }

    @Test
    fun accountPreferences_ToggleMobileData_InvokesMobileDataCallback() = runComposeUiTest {
        val syncMobileDataText = context.getString(R.string.sync_on_mobile_data)
        var updatedValue: Boolean? = null
        val state = AccountPreferencesUiState(
            syncOnMobileData = false
        )

        setContent {
            AccountPreferencesScreenContent(
                state = state,
                snackbarHostState = SnackbarHostState(),
                onSaveSyncService = { _, _ -> },
                onSignInPressed = {},
                onSignOutPressed = {},
                onSyncOnMobileDataChanged = { newValue -> updatedValue = newValue }
            )
        }

        onNodeWithText(syncMobileDataText)
            .performClick()

        assertEquals(true, updatedValue)
    }

    @Test
    fun accountPreferences_ClickSyncService_DisplaysSyncServiceDialog() = runComposeUiTest {
        val syncServiceText = context.getString(R.string.sync_service)
        val dialogTitleText = context.getString(R.string.sync_service_title)
        val state = AccountPreferencesUiState()

        setContent {
            AccountPreferencesScreenContent(
                state = state,
                snackbarHostState = SnackbarHostState(),
                onSaveSyncService = { _, _ -> },
                onSignInPressed = {},
                onSignOutPressed = {},
                onSyncOnMobileDataChanged = {}
            )
        }

        onNodeWithText(syncServiceText).performClick()

        onNodeWithText(dialogTitleText).assertIsDisplayed()
    }

    @Test
    fun accountPreferences_ClickSignIn_TriggersSignInPressed() = runComposeUiTest {
        val signInText = context.getString(R.string.sign_in)
        var signInPressed = false
        val state = AccountPreferencesUiState(
            serviceUrl = "https://sync.aisleron.com",
            sessionStatus = SyncSessionStatus.NotAuthenticated
        )

        setContent {
            AccountPreferencesScreenContent(
                state = state,
                snackbarHostState = SnackbarHostState(),
                onSaveSyncService = { _, _ -> },
                onSignInPressed = { signInPressed = true },
                onSignOutPressed = {},
                onSyncOnMobileDataChanged = {}
            )
        }

        onNodeWithText(signInText)
            .assertIsEnabled()
            .performClick()

        assertTrue(signInPressed)
    }

    @Test
    fun accountPreferences_ConfirmSyncServiceDialog_InvokesSaveSyncServiceCallback() =
        runComposeUiTest {
            val syncServiceText = context.getString(R.string.sync_service)
            val confirmText = context.getString(R.string.save)
            val expectedUrl = "https://new-sync.aisleron.com"
            val expectedKey = "secret-key"

            var savedUrl: String? = null
            var savedKey: String? = null

            val state = AccountPreferencesUiState(
                serviceUrl = "https://old-sync.aisleron.com"
            )

            setContent {
                AccountPreferencesScreenContent(
                    state = state,
                    snackbarHostState = SnackbarHostState(),
                    onSaveSyncService = { url, key ->
                        savedUrl = url
                        savedKey = key
                    },
                    onSignInPressed = {},
                    onSignOutPressed = {},
                    onSyncOnMobileDataChanged = {}
                )
            }

            onNodeWithText(syncServiceText).performClick()

            onNodeWithText(context.getString(R.string.sync_service_address))
                .performTextReplacement(expectedUrl)

            onNodeWithText(context.getString(R.string.sync_service_public_key))
                .performTextReplacement(expectedKey)

            onNodeWithText(confirmText).performClick()

            assertEquals(expectedUrl, savedUrl)
            assertEquals(expectedKey, savedKey)
        }

    private fun validateStatusDescription(
        sessionStatus: SyncSessionStatus, expectedResId: Int
    ) = runComposeUiTest {
        val statusText = context.getString(expectedResId)

        val state = AccountPreferencesUiState(
            serviceUrl = "https://sync.aisleron.com",
            sessionStatus = sessionStatus
        )

        setContent {
            AccountPreferencesScreenContent(
                state = state,
                snackbarHostState = SnackbarHostState(),
                onSaveSyncService = { _, _ -> },
                onSignInPressed = {},
                onSignOutPressed = {},
                onSyncOnMobileDataChanged = {}
            )
        }

        onNodeWithText(statusText, substring = true).assertIsDisplayed()
    }

    @Test
    fun accountPreferences_SyncStatusIsLoading_ShowLoadingState() {
        validateStatusDescription(
            SyncSessionStatus.Loading,
            R.string.loading
        )
    }

    @Test
    fun accountPreferences_SyncStatusIsNotAuthenticated_ShowNotAuthenticatedState() {
        validateStatusDescription(
            SyncSessionStatus.NotAuthenticated,
            R.string.sync_status_not_authenticated
        )
    }

    @Test
    fun accountPreferences_SyncStatusIsRefreshFailure_ShowRefreshFailureState() {
        validateStatusDescription(
            SyncSessionStatus.RefreshFailure,
            R.string.sync_status_refresh_failure
        )
    }

    @Test
    fun accountPreferences_LoadingState_DisplaysLoadingIndicator() = runComposeUiTest {
        val state = AccountPreferencesUiState(
            isLoading = true
        )

        setContent {
            AccountPreferencesScreenContent(
                state = state,
                snackbarHostState = SnackbarHostState(),
                onSaveSyncService = { _, _ -> },
                onSignInPressed = {},
                onSignOutPressed = {},
                onSyncOnMobileDataChanged = {}
            )
        }

        // Match by ProgressBar role or content description / tag used for your loading indicator
        onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
    }
}