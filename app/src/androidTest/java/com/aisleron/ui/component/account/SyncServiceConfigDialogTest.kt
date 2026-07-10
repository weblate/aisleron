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

package com.aisleron.ui.component.account

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.AnnotatedString
import androidx.test.platform.app.InstrumentationRegistry
import com.aisleron.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SyncServiceConfigDialogTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun targetContext() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun invoke_InitialUrlNotSet_UrlIsEmpty() {
        val urlLabel = targetContext().getString(R.string.sync_service_address)

        composeTestRule.setContent {
            SyncServiceConfigDialog(
                onDismissRequest = {},
                onConfirmPressed = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithText(urlLabel).assert(
            SemanticsMatcher.expectValue(
                SemanticsProperties.EditableText,
                AnnotatedString("")
            )
        )
    }

    @Test
    fun invoke_InitialUrlSet_UrlIsPrefilled() {
        val urlLabel = targetContext().getString(R.string.sync_service_address)
        val url = "https://test.url"

        composeTestRule.setContent {
            SyncServiceConfigDialog(
                onDismissRequest = {},
                initialUrl = url,
                onConfirmPressed = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithText(urlLabel).assert(
            SemanticsMatcher.expectValue(
                SemanticsProperties.EditableText,
                AnnotatedString(url)
            )
        )
    }

    @Test
    fun dismissButtonClicked_OnDismissRequestProvided_ONDismissRequestExecuted() {
        val buttonTitle = targetContext().getString(android.R.string.cancel)
        var dismissPressed = 0
        var confirmPressed = 0

        composeTestRule.setContent {
            SyncServiceConfigDialog(
                onDismissRequest = { dismissPressed++ },
                onConfirmPressed = { _, _ -> confirmPressed++ }
            )
        }

        composeTestRule.onNodeWithText(buttonTitle)
            .assertHasClickAction()
            .performClick()

        assertEquals(1, dismissPressed)
        assertEquals(0, confirmPressed)
    }

    @Test
    fun confirmButtonClicked_onConfirmPressedProvided_onConfirmPressedExecuted() {
        val context = targetContext()
        val buttonTitle = context.getString(R.string.save)
        val urlLabel = context.getString(R.string.sync_service_address)
        val keyLabel = context.getString(R.string.sync_service_public_key)

        val enteredUrl = "https://confirmtest.url"
        val enteredKey = "confirm-test-key"

        var dismissPressed = 0
        var callbackResult: Pair<String, String>? = null

        composeTestRule.setContent {
            SyncServiceConfigDialog(
                onDismissRequest = { dismissPressed++ },
                onConfirmPressed = { url, key -> callbackResult = url to key }
            )
        }

        composeTestRule.onNodeWithText(urlLabel).performTextInput(enteredUrl)
        composeTestRule.onNodeWithText(keyLabel).performTextInput(enteredKey)

        composeTestRule.onNodeWithText(buttonTitle)
            .assertHasClickAction()
            .performClick()

        assertEquals(0, dismissPressed)
        assertEquals(enteredUrl, callbackResult?.first)
        assertEquals(enteredKey, callbackResult?.second)
    }
}