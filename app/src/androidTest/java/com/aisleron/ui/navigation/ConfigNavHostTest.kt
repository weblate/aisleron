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

package com.aisleron.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.aisleron.R
import com.aisleron.di.KoinTestRule
import com.aisleron.di.generalTestModule
import com.aisleron.di.preferenceModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelTestModule
import com.aisleron.domain.sync.SyncSessionManager
import com.aisleron.domain.sync.SyncSessionStatus
import com.aisleron.testdata.data.sync.SyncSessionManagerTestImpl
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get

@OptIn(ExperimentalTestApi::class)
class ConfigNavHostTest : KoinTest {
    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(
            viewModelTestModule,
            useCaseModule,
            generalTestModule,
            preferenceModule
        )
    )

    private fun navigateToScreen_ArrangeActAssert(
        destination: Destination, @StringRes titleResId: Int
    ) = runComposeUiTest {
        lateinit var titleText: String

        setContent {
            titleText = stringResource(titleResId)
            ConfigNavHost(startDestination = destination)
        }

        onNode(
            hasText(titleText) and isHeading()
        ).assertIsDisplayed()
    }

    @Test
    fun startDestination_About_DisplaysAboutScreen() {
        navigateToScreen_ArrangeActAssert(Destination.About, R.string.title_activity_about)
    }

    @Test
    fun startDestination_AccountPreferences_DisplaysAccountPreferencesScreen() {
        navigateToScreen_ArrangeActAssert(Destination.AccountPreferences, R.string.account_sync)
    }

    @Test
    fun startDestination_SignIn_DisplaysSignInScreen() {
        navigateToScreen_ArrangeActAssert(Destination.SignIn, R.string.sign_in_title)
    }

    @Test
    fun accountPreferences_clickSignIn_navigatesToSignInScreen() = runComposeUiTest {
        val sessionManager = get<SyncSessionManager>() as SyncSessionManagerTestImpl
        sessionManager.setFutureStatus(SyncSessionStatus.NotAuthenticated)
        sessionManager.refreshStatus()

        lateinit var signInText: String
        lateinit var signInTitle: String

        setContent {
            signInText = stringResource(R.string.sign_in)
            signInTitle = stringResource(R.string.sign_in_title)
            ConfigNavHost(startDestination = Destination.AccountPreferences)
        }

        onNodeWithText(signInText)
            .performClick()

        onNode(hasText(signInTitle) and isHeading())
            .assertIsDisplayed()
    }


}