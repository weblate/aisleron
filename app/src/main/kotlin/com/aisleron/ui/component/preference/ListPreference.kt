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

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.aisleron.R
import com.aisleron.domain.preferences.PreferenceEnum

@Composable
fun <T> ListPreference(
    title: String,
    selectedValue: T,
    entries: List<T>,
    onValueSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes iconResId: Int? = null,
) where T : Enum<T>, T : PreferenceEnum {
    var showDialog by remember { mutableStateOf(false) }

    val padding16 = dimensionResource(id = R.dimen.padding_16)

    Preference(
        title = title,
        summary = stringResource(selectedValue.labelRes),
        modifier = modifier
            .clickable { showDialog = true },
        iconResId = iconResId,
        control = {
            Icon(
                painter = painterResource(R.drawable.baseline_arrow_drop_down_24),
                contentDescription = null
            )
        }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = title) },
            text = {
                Column(
                    modifier = Modifier.selectableGroup()
                ) {
                    entries.forEach { entry ->
                        val isSelected = (entry == selectedValue)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(padding16),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .selectable(
                                    selected = isSelected,
                                    onClick = {
                                        onValueSelected(entry)
                                        showDialog = false
                                    },
                                    role = Role.RadioButton
                                )
                        ) {
                            RadioButton(selected = isSelected, onClick = null)
                            Text(text = stringResource(entry.labelRes))
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false }
                ) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }
}