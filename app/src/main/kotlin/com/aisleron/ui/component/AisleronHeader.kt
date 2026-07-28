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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import com.aisleron.R

@Composable
fun AisleronHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Row(
        modifier = modifier
    ) {
        val padding16 = dimensionResource(id = R.dimen.padding_16)
        val padding4 = dimensionResource(id = R.dimen.padding_4)

        Image(
            painter = painterResource(id = R.drawable.aisleron_24),
            contentDescription = stringResource(id = R.string.aisleron_logo),
            modifier = Modifier
                .size(dimensionResource(id = R.dimen.icon_size))
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .padding(padding16)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(padding4),
            modifier = Modifier
                .weight(1f)
                .padding(padding16)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall
            )

            subtitle?.let {
                Text(
                    text = it,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}