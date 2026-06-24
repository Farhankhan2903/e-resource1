package com.eresource.solution.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eresource.solution.data.models.AnalyticsDashboardResponse
import com.eresource.solution.data.repository.AppRepository
import com.eresource.solution.data.repository.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AnalyticsViewModel(private val repository: AppRepository) : ViewModel() {

    private val _analyticsState = MutableStateFlow<NetworkResult<AnalyticsDashboardResponse>>(NetworkResult.Loading)
    val analyticsState: StateFlow<NetworkResult<AnalyticsDashboardResponse>> = _analyticsState.asStateFlow()

    fun loadAnalytics(token: String) {
        _analyticsState.value = NetworkResult.Loading
        viewModelScope.launch {
            _analyticsState.value = repository.fetchAnalytics(token)
        }
    }
}
