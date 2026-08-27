/*
 * Copyright (C) 2025-2026 aisleron.com
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

package com.aisleron.ui.menu

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.aisleron.R
import com.aisleron.domain.FilterType
import com.aisleron.domain.location.LocationType
import com.aisleron.ui.navigation.MainNavigatorTestImpl
import com.aisleron.ui.shopmenu.ShopMenuFragment
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.assertEquals

@RunWith(value = Parameterized::class)
class NavigationDrawerFragmentTest(
    private val testName: String,
    private val textViewId: Int,
    private val expectedDestination: MainNavigatorTestImpl.TestDestination
) {
    private lateinit var navigator: MainNavigatorTestImpl

    @Before
    fun setUp() {
        navigator = MainNavigatorTestImpl()
    }

    private fun getFragmentScenario(): FragmentScenario<NavigationDrawerFragment> {
        class StubShopMenuFragment : androidx.fragment.app.Fragment()

        val testFactory = object : androidx.fragment.app.FragmentFactory() {
            override fun instantiate(
                classLoader: ClassLoader, className: String
            ): androidx.fragment.app.Fragment {
                return when (className) {
                    NavigationDrawerFragment::class.java.name -> NavigationDrawerFragment(navigator)
                    ShopMenuFragment::class.java.name -> StubShopMenuFragment()
                    else -> super.instantiate(classLoader, className)
                }
            }
        }

        return launchFragmentInContainer<NavigationDrawerFragment>(
            themeResId = R.style.Theme_Aisleron,
            factory = testFactory,
            fragmentArgs = null
        )
    }

    @Test
    fun onClick_textViewClicked_NavigateToTargetView() {
        val scenario = getFragmentScenario()

        scenario.use {
            onView(withId(textViewId)).perform(ViewActions.click())
            assertEquals(expectedDestination, navigator.destination)
        }
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf(
                    "navInStock",
                    R.id.nav_in_stock,
                    MainNavigatorTestImpl.TestDestination.InStockDestination
                ),

                arrayOf(
                    "navNeeded",
                    R.id.nav_needed,
                    MainNavigatorTestImpl.TestDestination.NeededDestination
                ),

                arrayOf(
                    "navAllItems",
                    R.id.nav_all_items,
                    MainNavigatorTestImpl.TestDestination.AllItemsDestination
                ),

                arrayOf(
                    "navAllShops",
                    R.id.nav_all_shops,
                    MainNavigatorTestImpl.TestDestination.LocationGroupedProductListDestination(
                        LocationType.SHOP, FilterType.NEEDED
                    )
                ),

                arrayOf(
                    "navSettings",
                    R.id.nav_settings,
                    MainNavigatorTestImpl.TestDestination.SettingsDestination
                ),

                arrayOf(
                    "navAllLists",
                    R.id.nav_all_lists,
                    MainNavigatorTestImpl.TestDestination.AllListsDestination
                ),

                arrayOf(
                    "navAbout",
                    R.id.nav_about,
                    MainNavigatorTestImpl.TestDestination.AboutDestination
                )
            )
        }
    }
}