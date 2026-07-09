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

import com.aisleron.R
import com.aisleron.domain.FilterType
import com.aisleron.domain.location.LocationType

class MainNavigatorTestImpl : MainNavigator {
    private var _destination: TestDestination? = null
    val destination: TestDestination? get() = _destination

    override fun navigateToAddShop() {
        _destination = TestDestination.AddShopDestination
    }

    override fun navigateToEditShop(locationId: Int) {
        _destination = TestDestination.EditShopDestination(locationId)
    }

    override fun navigateToAddProduct(
        filterType: FilterType, name: String, aisleId: Int?
    ) {
        _destination = TestDestination.AddProductDestination(filterType, name, aisleId)
    }

    override fun navigateToEditProduct(productId: Int) {
        _destination = TestDestination.EditProductDestination(productId)
    }

    override fun navigateToAisleGroupedProductList(locationId: Int, productFilter: FilterType) {
        _destination =
            TestDestination.AisleGroupedProductListDestination(locationId, productFilter)
    }

    override fun navigateToLocationGroupedProductList(
        locationType: LocationType, productFilter: FilterType
    ) {
        _destination =
            TestDestination.LocationGroupedProductListDestination(locationType, productFilter)
    }

    override fun navigateToDefaultRoute(destinationId: Int) {
        _destination = when (destinationId) {
            R.id.nav_in_stock -> TestDestination.InStockDestination
            R.id.nav_needed -> TestDestination.NeededDestination
            R.id.nav_all_items -> TestDestination.AllItemsDestination
            R.id.nav_settings -> TestDestination.SettingsDestination
            R.id.nav_all_lists -> TestDestination.AllListsDestination
            else -> TestDestination.UnknownDestination
        }
    }

    override fun navigateToWelcome() {
        _destination = TestDestination.WelcomeDestination
    }

    override fun navigateToAbout() {
        _destination = TestDestination.AboutDestination
    }

    override fun navigateToAccountPreferences() {
        _destination = TestDestination.AccountPreferencesDestination
    }

    sealed class TestDestination {
        data object UnknownDestination : TestDestination()
        data object InStockDestination : TestDestination()
        data object AllItemsDestination : TestDestination()
        data object NeededDestination : TestDestination()
        data object SettingsDestination : TestDestination()
        data object AllListsDestination : TestDestination()
        data object AccountPreferencesDestination : TestDestination()
        data object AboutDestination : TestDestination()
        data object WelcomeDestination : TestDestination()

        data class LocationGroupedProductListDestination(
            val locationType: LocationType,
            val productFilter: FilterType
        ) : TestDestination()

        data class AisleGroupedProductListDestination(
            val locationId: Int,
            val productFilter: FilterType
        ) : TestDestination()

        data object AddShopDestination : TestDestination()

        data class EditShopDestination(
            val locationId: Int
        ) : TestDestination()

        data class EditProductDestination(val productId: Int) : TestDestination()
        data class AddProductDestination(
            val filterType: FilterType,
            val name: String,
            val aisleId: Int?
        ) : TestDestination()


    }
}