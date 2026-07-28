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

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldLabelScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import com.aisleron.R

@Composable
fun PasswordTextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    label: (@Composable TextFieldLabelScope.() -> Unit)? = { Text(stringResource(R.string.password)) },
    enabled: Boolean = true
) {
    var passwordHidden by rememberSaveable { mutableStateOf(true) }
    OutlinedSecureTextField(
        modifier = modifier
            .semantics { contentType = ContentType.Password },
        state = state,
        label = label,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(
            autoCorrectEnabled = false,
            keyboardType = KeyboardType.Password
        ),
        textObfuscationMode =
            if (passwordHidden) TextObfuscationMode.RevealLastTyped
            else TextObfuscationMode.Visible,

        trailingIcon = {
            val visibilityIcon = if (passwordHidden)
                painterResource(R.drawable.baseline_visibility_24)
            else
                painterResource(R.drawable.baseline_visibility_off_24)

            val description = if (passwordHidden)
                stringResource(R.string.show_password)
            else
                stringResource(R.string.hide_password)

            IconButton(onClick = { passwordHidden = !passwordHidden }) {
                Icon(painter = visibilityIcon, contentDescription = description)
            }
        }
    )
}