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

package com.aisleron.ui.component

import android.content.Context
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.StateRestorationTester
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.test.platform.app.InstrumentationRegistry
import com.aisleron.R
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class PasswordTextFieldTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun passwordTextField_DefaultState_DisplaysCorrectLabelAndHiddenIcon() = runComposeUiTest {
        val passwordString = context.getString(R.string.password)
        val showPasswordString = context.getString(R.string.show_password)
        val state = TextFieldState()

        setContent {
            PasswordTextField(state = state)
        }

        onNodeWithText(passwordString).assertIsDisplayed()
        onNodeWithContentDescription(showPasswordString).assertIsDisplayed()
    }

    @Test
    fun passwordTextField_ToggleVisibility_SwapsContentDescription() = runComposeUiTest {
        val showPasswordString = context.getString(R.string.show_password)
        val hidePasswordString = context.getString(R.string.hide_password)
        val state = TextFieldState()
        setContent {
            PasswordTextField(state = state)
        }

        onNodeWithContentDescription(showPasswordString).performClick()
        onNodeWithContentDescription(hidePasswordString).assertIsDisplayed()

        onNodeWithContentDescription(hidePasswordString).performClick()
        onNodeWithContentDescription(showPasswordString).assertIsDisplayed()
    }

    @Test
    fun passwordTextField_TextInput_UpdatesTextFieldState() = runComposeUiTest {
        val passwordString = context.getString(R.string.password)
        val password = "Secret123!"
        val state = TextFieldState()
        setContent {
            PasswordTextField(state = state)
        }

        onNodeWithText(passwordString).performTextClearance()
        onNodeWithText(passwordString).performTextInput(password)

        assertEquals(password, state.text.toString())
    }

    @Test
    fun passwordTextField_DisabledState_DisablesField() = runComposeUiTest {
        val passwordString = context.getString(R.string.password)
        val state = TextFieldState()

        setContent {
            PasswordTextField(
                state = state,
                enabled = false
            )
        }

        onNodeWithText(passwordString).assertIsNotEnabled()
    }

    @Test
    fun passwordTextField_CustomLabel_ShowCustomLabel() = runComposeUiTest {
        val state = TextFieldState()
        val label = "Custom Security Code"

        setContent {
            PasswordTextField(
                state = state,
                label = { Text(label) }
            )
        }

        onNodeWithText(label).assertIsDisplayed()
    }

    @Test
    fun passwordTextField_VisibilityState_PersistsAcrossConfigurationChange() = runComposeUiTest {
        val showPasswordString = context.getString(R.string.show_password)
        val hidePasswordString = context.getString(R.string.hide_password)
        val restorationTester = StateRestorationTester(this)
        val state = TextFieldState()

        restorationTester.setContent {
            PasswordTextField(state = state)
        }

        onNodeWithContentDescription(showPasswordString).performClick()
        onNodeWithContentDescription(hidePasswordString).assertIsDisplayed()

        restorationTester.emulateSaveAndRestore()

        onNodeWithContentDescription(hidePasswordString).assertIsDisplayed()
    }
}