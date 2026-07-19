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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aisleron.R

@Composable
fun SyncServiceConfigDialog(
    onDismissRequest: () -> Unit,
    onConfirmPressed: (url: String, key: String) -> Unit,
    initialUrl: String = ""
) {
    val servicedUrlState = rememberTextFieldState(initialText = initialUrl)
    val serviceKeyState = rememberTextFieldState(initialText = "")

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.sync_service_title)) },
        text = {
            Column {
                Text(stringResource(R.string.sync_service_dialog_info))

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    state = servicedUrlState,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    label = { Text(stringResource(R.string.sync_service_address)) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    state = serviceKeyState,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    label = { Text(stringResource(R.string.sync_service_public_key)) }
                )
            }
        },

        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmPressed(servicedUrlState.text.toString(), serviceKeyState.text.toString())
                }
            ) {
                Text(stringResource(R.string.save))
            }
        },

        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}