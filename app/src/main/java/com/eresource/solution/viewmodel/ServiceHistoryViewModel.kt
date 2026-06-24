package com.eresource.solution.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eresource.solution.data.models.Booking
import com.eresource.solution.data.repository.AppRepository
import com.eresource.solution.data.repository.NetworkResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ServiceHistoryViewModel(private val repository: AppRepository) : ViewModel() {

    private val _bookingsState = MutableStateFlow<NetworkResult<List<Booking>>>(NetworkResult.Loading)
    val bookingsState: StateFlow<NetworkResult<List<Booking>>> = _bookingsState.asStateFlow()

    private val _filterStatus = MutableStateFlow("All") // 'All', 'Pending', 'Completed'
    val filterStatus: StateFlow<String> = _filterStatus.asStateFlow()

    private val _rawList = MutableStateFlow<List<Booking>>(emptyList())

    val filteredBookings: StateFlow<List<Booking>> = combine(_rawList, _filterStatus) { list, status ->
        if (status == "All") {
            list
        } else {
            list.filter { it.status.equals(status, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onFilterChanged(status: String) {
        _filterStatus.value = status
    }

    fun loadBookings(token: String) {
        _bookingsState.value = NetworkResult.Loading
        viewModelScope.launch {
            val res = repository.getBookings(token)
            _bookingsState.value = res
            if (res is NetworkResult.Success) {
                _rawList.value = res.data
            }
        }
    }
}
