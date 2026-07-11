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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.aisleron.ui.about.AboutScreen
import com.aisleron.ui.account.AccountPreferencesScreen
import com.aisleron.ui.account.SignInScreen
import kotlinx.serialization.serializer

@Composable
private fun rememberConfigNavBackStack(vararg elements: Destination): NavBackStack<Destination> {
    return rememberSerializable(serializer = serializer()) {
        NavBackStack(*elements)
    }
}

@Composable
fun ConfigNavHost(
    startDestination: Destination
) {
    val backStack = rememberConfigNavBackStack(startDestination)

    val entryProvider = entryProvider {
        entry<Destination.About> {
            AboutScreen()
        }

        entry<Destination.AccountPreferences> {
            AccountPreferencesScreen(
                onSignInPressed = { backStack.add(Destination.SignIn) }
            )
        }

        entry<Destination.SignIn> {
            SignInScreen()
        }
    }

    NavDisplay(
        backStack = backStack,
        entryProvider = entryProvider,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        )
    )
}