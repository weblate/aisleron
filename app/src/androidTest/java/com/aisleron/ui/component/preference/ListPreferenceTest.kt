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

package com.aisleron.ui.component.preference

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.aisleron.domain.preferences.SyncServicePreference
import com.aisleron.ui.ComposeScreenTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.koin.core.module.Module

@OptIn(ExperimentalTestApi::class)
class ListPreferenceTest : ComposeScreenTest() {

    override val koinModules: List<Module> = emptyList()

    @Test
    fun listPreference_InitialState_DisplaysTitleAndSummary() = runComposeUiTest {
        val title = "Test Preference Title"
        val expectedSummary = getString(SyncServicePreference.NONE.labelRes)

        setContent {
            ListPreference(
                title = title,
                selectedValue = SyncServicePreference.NONE,
                entries = SyncServicePreference.entries,
                onValueSelected = {}
            )
        }

        onNodeWithText(title).assertIsDisplayed()
        onNodeWithText(expectedSummary).assertIsDisplayed()
    }

    @Test
    fun listPreference_OnClick_DisplaysDialogWithTitleAndEntries() = runComposeUiTest {
        val title = "Test Preference Title"
        val optionOneText = getString(SyncServicePreference.NONE.labelRes)
        val optionTwoText = getString(SyncServicePreference.CUSTOM_SERVICE.labelRes)

        setContent {
            ListPreference(
                title = title,
                selectedValue = SyncServicePreference.NONE,
                entries = SyncServicePreference.entries,
                onValueSelected = {}
            )
        }

        onNodeWithText(title).performClick()

        // Verify Dialog Title and Listed Entries
        onAllNodesWithText(title).onFirst().assertIsDisplayed()
        onAllNodesWithText(optionOneText).onFirst().assertIsDisplayed()
        onNodeWithText(optionTwoText).assertIsDisplayed()

        // Verify Option One is selected
        onNode(hasText(optionOneText) and isSelectable()).assertIsSelected()
    }

    @Test
    fun listPreference_SelectEntry_InvokesCallbackAndDismissesDialog() = runComposeUiTest {
        val title = "Test Preference Title"
        val optionTwoText = getString(SyncServicePreference.CUSTOM_SERVICE.labelRes)
        var selectedValue: SyncServicePreference? = null

        setContent {
            ListPreference(
                title = title,
                selectedValue = SyncServicePreference.NONE,
                entries = SyncServicePreference.entries,
                onValueSelected = { selectedValue = it }
            )
        }

        onNodeWithText(title).performClick()
        onNodeWithText(optionTwoText).performClick()

        assertEquals(SyncServicePreference.CUSTOM_SERVICE, selectedValue)

        // Verify Dialog is closed by checking that the secondary entry is no longer displayed
        // Might error due to summary update?
        onNodeWithText(optionTwoText).assertDoesNotExist()
    }

    @Test
    fun listPreference_ClickCancel_DismissesDialogWithoutInvokingCallback() = runComposeUiTest {
        val title = "Test Preference Title"
        val optionTwoText = getString(SyncServicePreference.CUSTOM_SERVICE.labelRes)
        val cancelText = getString(android.R.string.cancel)
        var callbackInvoked = false

        setContent {
            ListPreference(
                title = title,
                selectedValue = SyncServicePreference.NONE,
                entries = SyncServicePreference.entries,
                onValueSelected = { callbackInvoked = true }
            )
        }

        onNodeWithText(title).performClick()
        onNodeWithText(cancelText).performClick()

        assertEquals(false, callbackInvoked)
        // Might error due to summary update?
        onNodeWithText(optionTwoText).assertDoesNotExist()
    }
}