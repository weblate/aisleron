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

package com.aisleron.ui.about

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runComposeUiTest
import com.aisleron.R
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@OptIn(ExperimentalTestApi::class)
@RunWith(value = Parameterized::class)
class AboutScreenContentUrlTest(private val labelResId: Int, private val expectedUri: String) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: URL={1}")
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf(
                    R.string.about_support_version_title,
                    "https://aisleron.com/docs/version-history"
                ),
                arrayOf(
                    R.string.about_support_report_issue_title,
                    "https://aisleron.com/docs/reporting-issues"
                ),
                arrayOf(
                    R.string.about_support_sourcecode_title,
                    "https://github.com/aisleron/aisleron"
                ),
                arrayOf(
                    R.string.about_legal_license_title,
                    "https://aisleron.com/docs/licenses-policies/aisleron-license"
                ),
                arrayOf(
                    R.string.about_legal_privacy_title,
                    "https://aisleron.com/docs/licenses-policies/aisleron-privacy-policy"
                ),
                arrayOf(
                    R.string.about_support_documentation_title,
                    "https://aisleron.com/docs/documentation/"
                ),
                arrayOf(
                    R.string.about_legal_3rdparty_title,
                    "https://aisleron.com/docs/licenses-policies/3rd-party-licenses"
                ),
                arrayOf(
                    R.string.about_contribute_translate_title,
                    "https://aisleron.com/docs/contribute/translate"
                ),
                arrayOf(
                    R.string.about_contribute_financial_title,
                    "https://aisleron.com/docs/contribute/financial_contributions"
                )
            )
        }
    }

    @Test
    fun onAboutEntryClick_InvokesCallbackWithCorrectUrl() = runComposeUiTest {
        lateinit var expectedLabelString: String
        var capturedUrl: String? = null

        val fakeUriHandler = object : UriHandler {
            override fun openUri(uri: String) {
                capturedUrl = uri
            }
        }

        setContent {
            expectedLabelString = stringResource(labelResId)
            CompositionLocalProvider(LocalUriHandler provides fakeUriHandler) {
                AboutScreenContent(
                    versionName = "2.4.1"
                )
            }
        }

        onNodeWithText(expectedLabelString)
            .performScrollTo()
            .performClick()

        assertEquals(expectedUri, capturedUrl)
    }
}