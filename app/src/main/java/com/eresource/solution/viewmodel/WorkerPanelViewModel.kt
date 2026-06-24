package com.eresource.solution.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eresource.solution.data.models.*
import com.eresource.solution.data.repository.AppRepository
import com.eresource.solution.data.repository.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorkerPanelViewModel(private val repository: AppRepository) : ViewModel() {

    private val _workerSelfState = MutableStateFlow<NetworkResult<Worker>>(NetworkResult.Loading)
    val workerSelfState: StateFlow<NetworkResult<Worker>> = _workerSelfState.asStateFlow()

    private val _statusState = MutableStateFlow<NetworkResult<WorkerStatusResponse>>(NetworkResult.Loading)
    val statusState: StateFlow<NetworkResult<WorkerStatusResponse>> = _statusState.asStateFlow()

    // Tool Rental States
    private val _toolsState = MutableStateFlow<NetworkResult<List<Resource>>>(NetworkResult.Loading)
    val toolsState: StateFlow<NetworkResult<List<Resource>>> = _toolsState.asStateFlow()

    private val _rentedToolsState = MutableStateFlow<NetworkResult<List<AllocationLog>>>(NetworkResult.Loading)
    val rentedToolsState: StateFlow<NetworkResult<List<AllocationLog>>> = _rentedToolsState.asStateFlow()

    private val _rentActionState = MutableStateFlow<NetworkResult<MessageResponse>?>(null)
    val rentActionState: StateFlow<NetworkResult<MessageResponse>?> = _rentActionState.asStateFlow()

    private val _returnActionState = MutableStateFlow<NetworkResult<ReturnResponse>?>(null)
    val returnActionState: StateFlow<NetworkResult<ReturnResponse>?> = _returnActionState.asStateFlow()

    // Worker Bookings
    private val _bookingsState = MutableStateFlow<NetworkResult<List<Booking>>>(NetworkResult.Loading)
    val bookingsState: StateFlow<NetworkResult<List<Booking>>> = _bookingsState.asStateFlow()

    private val _updateBookingState = MutableStateFlow<NetworkResult<MessageResponse>?>(null)
    val updateBookingState: StateFlow<NetworkResult<MessageResponse>?> = _updateBookingState.asStateFlow()

    fun clearActionStates() {
        _rentActionState.value = null
        _returnActionState.value = null
        _updateBookingState.value = null
    }

    fun loadStatus(token: String) {
        _statusState.value = NetworkResult.Loading
        viewModelScope.launch {
            _statusState.value = repository.checkWorkerStatus(token)
        }
    }

    fun loadSelfDetails(token: String) {
        _workerSelfState.value = NetworkResult.Loading
        viewModelScope.launch {
            _workerSelfState.value = repository.fetchWorkerSelf(token)
        }
    }

    fun loadRentableTools() {
        _toolsState.value = NetworkResult.Loading
        viewModelScope.launch {
            _toolsState.value = repository.getAvailableResources()
        }
    }

    fun loadRentedTools(token: String) {
        _rentedToolsState.value = NetworkResult.Loading
        viewModelScope.launch {
            _rentedToolsState.value = repository.getRentedResources(token)
        }
    }

    fun rentTool(token: String, resourceId: Int, hours: Int) {
        _rentActionState.value = NetworkResult.Loading
        viewModelScope.launch {
            val res = repository.rentResource(token, resourceId, hours)
            _rentActionState.value = res
            if (res is NetworkResult.Success) {
                loadRentableTools()
                loadRentedTools(token)
            }
        }
    }

    fun returnTool(token: String, allocationId: Int) {
        _returnActionState.value = NetworkResult.Loading
        viewModelScope.launch {
            val res = repository.returnResource(token, allocationId)
            _returnActionState.value = res
            if (res is NetworkResult.Success) {
                loadRentableTools()
                loadRentedTools(token)
            }
        }
    }

    fun loadBookings(token: String) {
        _bookingsState.value = NetworkResult.Loading
        viewModelScope.launch {
            _bookingsState.value = repository.getBookings(token)
        }
    }

    fun updateBooking(token: String, bookingId: Int, status: String) {
        _updateBookingState.value = NetworkResult.Loading
        viewModelScope.launch {
            val res = repository.updateBookingStatus(token, bookingId, status)
            _updateBookingState.value = res
            if (res is NetworkResult.Success) {
                loadBookings(token)
            }
        }
    }
}
