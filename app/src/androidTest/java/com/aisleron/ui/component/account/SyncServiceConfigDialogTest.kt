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

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.text.AnnotatedString
import com.aisleron.R
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SyncServiceConfigDialogTest {
    @Test
    fun invoke_InitialUrlNotSet_UrlIsEmpty() = runComposeUiTest {
        lateinit var urlLabel: String

        setContent {
            urlLabel = stringResource(R.string.sync_service_address_title)

            SyncServiceConfigDialog(
                onDismissRequest = {},
                onConfirmPressed = { _, _ -> }
            )
        }

        onNodeWithText(urlLabel).assert(
            SemanticsMatcher.expectValue(
                SemanticsProperties.EditableText,
                AnnotatedString("")
            )
        )
    }

    @Test
    fun invoke_InitialUrlSet_UrlIsPrefilled() = runComposeUiTest {
        lateinit var urlLabel: String
        val url = "https://test.url"

        setContent {
            urlLabel = stringResource(R.string.sync_service_address_title)

            SyncServiceConfigDialog(
                onDismissRequest = {},
                initialUrl = url,
                onConfirmPressed = { _, _ -> }
            )
        }

        onNodeWithText(urlLabel).assert(
            SemanticsMatcher.expectValue(
                SemanticsProperties.EditableText,
                AnnotatedString(url)
            )
        )
    }

    @Test
    fun dismissButtonClicked_OnDismissRequestProvided_OnDismissRequestExecuted() =
        runComposeUiTest {
            lateinit var buttonTitle: String
            var dismissPressed = 0
            var confirmPressed = 0

            setContent {
                buttonTitle = stringResource(android.R.string.cancel)


                SyncServiceConfigDialog(
                    onDismissRequest = { dismissPressed++ },
                    onConfirmPressed = { _, _ -> confirmPressed++ }
                )
            }

            onNodeWithText(buttonTitle)
                .assertHasClickAction()
                .performClick()

            assertEquals(1, dismissPressed)
            assertEquals(0, confirmPressed)
        }

    @Test
    fun confirmButtonClicked_OnConfirmPressedProvided_OnConfirmPressedExecuted() =
        runComposeUiTest {
            lateinit var buttonTitle: String
            lateinit var urlLabel: String
            lateinit var keyLabel: String

            val enteredUrl = "https://confirmtest.url"
            val enteredKey = "confirm-test-key"

            var dismissPressed = 0
            var callbackResult: Pair<String, String>? = null

            setContent {
                buttonTitle = stringResource(R.string.save)
                urlLabel = stringResource(R.string.sync_service_address_title)
                keyLabel = stringResource(R.string.sync_service_public_key)

                SyncServiceConfigDialog(
                    onDismissRequest = { dismissPressed++ },
                    onConfirmPressed = { url, key -> callbackResult = url to key }
                )
            }

            onNodeWithText(urlLabel).performTextInput(enteredUrl)
            onNodeWithText(keyLabel).performTextInput(enteredKey)

            onNodeWithText(buttonTitle)
                .assertHasClickAction()
                .performClick()

            assertEquals(0, dismissPressed)
            assertEquals(enteredUrl, callbackResult?.first)
            assertEquals(enteredKey, callbackResult?.second)
        }
}