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
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SwitchPreference(
    title: String,
    checked: Boolean,
    onCheckedChanged: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    summary: String? = null,
    @DrawableRes iconResId: Int? = null
) {
    Preference(
        title = title,
        summary = summary,
        modifier = modifier,
        iconResId = iconResId,
        control = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChanged
            )
        }
    )
}