package com.eresource.solution.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eresource.solution.data.models.EarningsData
import com.eresource.solution.data.models.TechnicianStats
import com.eresource.solution.data.models.UserSession
import com.eresource.solution.data.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: AppRepository) : ViewModel() {

    val userSession: StateFlow<UserSession?> = repository.userSessionFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val darkModeEnabled: StateFlow<Boolean> = repository.darkModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _isAvailable = MutableStateFlow(true)
    val isAvailable: StateFlow<Boolean> = _isAvailable.asStateFlow()

    private val _technicianStats = MutableStateFlow(TechnicianStats())
    val technicianStats: StateFlow<TechnicianStats> = _technicianStats.asStateFlow()

    private val _earnings = MutableStateFlow(EarningsData())
    val earnings: StateFlow<EarningsData> = _earnings.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.setDarkMode(enabled)
        }
    }

    fun toggleAvailability(available: Boolean) {
        _isAvailable.value = available
    }

    fun refreshProfile() {
        viewModelScope.launch {
            _isRefreshing.value = true
            kotlinx.coroutines.delay(1000) // Simulation
            _isRefreshing.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.clearSession()
        }
    }
}
