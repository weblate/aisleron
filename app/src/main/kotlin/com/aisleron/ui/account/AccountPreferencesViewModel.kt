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

package com.aisleron.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.base.exceptionCode
import com.aisleron.domain.log.Logger
import com.aisleron.domain.preferences.syncpreferences.SyncPreferences
import com.aisleron.domain.preferences.syncpreferences.usecase.GetSyncPreferencesUseCase
import com.aisleron.domain.preferences.syncpreferences.usecase.SetCustomSyncServiceDetailsUseCase
import com.aisleron.domain.preferences.syncpreferences.usecase.SetSyncOnMobileDataUseCase
import com.aisleron.domain.sync.SyncSessionStatus
import com.aisleron.domain.sync.usecase.GetSessionStatusUseCase
import com.aisleron.domain.sync.usecase.RefreshSessionStatusUseCase
import com.aisleron.domain.sync.usecase.SignOutUseCase
import com.aisleron.ui.base.UiEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class AccountPreferencesViewModel(
    private val signOutUseCase: SignOutUseCase,
    private val getSyncPreferencesUseCase: GetSyncPreferencesUseCase,
    private val setCustomSyncServiceDetailsUseCase: SetCustomSyncServiceDetailsUseCase,
    getSessionStatusUseCase: GetSessionStatusUseCase,
    private val refreshSessionStatusUseCase: RefreshSessionStatusUseCase,
    private val setSyncOnMobileDataUseCase: SetSyncOnMobileDataUseCase,
    private val logger: Logger,
    debounceTime: Long = 300,
    coroutineScopeProvider: CoroutineScope? = null
) : ViewModel() {
    private val coroutineScope = coroutineScopeProvider ?: this.viewModelScope
    private var syncPreferences: SyncPreferences = getSyncPreferencesUseCase()

    private val _syncServiceUrl = MutableStateFlow(syncPreferences.serviceUrl)
    private val _syncOnMobileData = MutableStateFlow(syncPreferences.syncOnMobileData)
    private val _isLoading = MutableStateFlow(false)
    private val _sessionStatusFlow = getSessionStatusUseCase()
        .transformLatest { status ->
            if (status is SyncSessionStatus.Loading) {
                delay(debounceTime.milliseconds)
            }
            emit(status)
        }

    private val _signOutError = MutableStateFlow<UiEvent<AisleronException.ExceptionCode>?>(null)
    val signOutError: StateFlow<UiEvent<AisleronException.ExceptionCode>?> =
        _signOutError.asStateFlow()

    val uiState: StateFlow<AccountPreferencesUiState> = combine(
        _syncServiceUrl,
        _syncOnMobileData,
        _isLoading,
        _sessionStatusFlow
    ) { url, mobileData, loading, status ->
        AccountPreferencesUiState(
            serviceUrl = url,
            syncOnMobileData = mobileData,
            isLoading = loading,
            sessionStatus = status
        )
    }.stateIn(
        scope = coroutineScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AccountPreferencesUiState(
            serviceUrl = syncPreferences.serviceUrl,
            syncOnMobileData = syncPreferences.syncOnMobileData
        )
    )

    fun signOut() {
        coroutineScope.launch {
            try {
                _isLoading.value = true
                signOutUseCase().onFailure { throwable ->
                    logger.e(TAG, throwable.message.orEmpty(), throwable)
                    _signOutError.value = UiEvent(throwable.exceptionCode)
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveSyncServiceDetails(url: String, key: String) {
        setCustomSyncServiceDetailsUseCase(url, key)
        refreshSessionStatusUseCase()
        refreshSyncPreferences()
    }

    fun setSyncOnMobileData(syncOnMobileData: Boolean) {
        setSyncOnMobileDataUseCase(syncOnMobileData)
        refreshSyncPreferences()
    }

    private fun refreshSyncPreferences() {
        // TODO: Remove this method when moving to DataStore preferences
        syncPreferences = getSyncPreferencesUseCase()
        _syncServiceUrl.value = syncPreferences.serviceUrl
        _syncOnMobileData.value = syncPreferences.syncOnMobileData
    }

    companion object {
        const val TAG = "AccountPreferencesViewModel"
    }
}

data class AccountPreferencesUiState(
    val serviceUrl: String = "",
    val syncOnMobileData: Boolean = false,
    val isLoading: Boolean = false,
    val sessionStatus: SyncSessionStatus = SyncSessionStatus.NotConfigured
)