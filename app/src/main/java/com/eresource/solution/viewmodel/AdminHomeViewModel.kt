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

class AdminHomeViewModel(private val repository: AppRepository) : ViewModel() {

    private val _workersState = MutableStateFlow<NetworkResult<List<Worker>>>(NetworkResult.Loading)
    val workersState: StateFlow<NetworkResult<List<Worker>>> = _workersState.asStateFlow()

    private val _allocationLogsState = MutableStateFlow<NetworkResult<List<AllocationLog>>>(NetworkResult.Loading)
    val allocationLogsState: StateFlow<NetworkResult<List<AllocationLog>>> = _allocationLogsState.asStateFlow()

    private val _reviewsState = MutableStateFlow<NetworkResult<List<Review>>>(NetworkResult.Loading)
    val reviewsState: StateFlow<NetworkResult<List<Review>>> = _reviewsState.asStateFlow()

    private val _actionState = MutableStateFlow<NetworkResult<MessageResponse>?>(null)
    val actionState: StateFlow<NetworkResult<MessageResponse>?> = _actionState.asStateFlow()

    fun clearActionState() {
        _actionState.value = null
    }

    fun loadAllWorkers(token: String) {
        _workersState.value = NetworkResult.Loading
        viewModelScope.launch {
            _workersState.value = repository.getWorkersAdmin(token)
        }
    }

    fun loadAllocationLogs(token: String) {
        _allocationLogsState.value = NetworkResult.Loading
        viewModelScope.launch {
            _allocationLogsState.value = repository.getAllocationLogs(token)
        }
    }

    fun loadAllReviews() {
        _reviewsState.value = NetworkResult.Loading
        viewModelScope.launch {
            _reviewsState.value = repository.getReviews(null)
        }
    }

    fun verifyWorker(token: String, workerId: Int) {
        _actionState.value = NetworkResult.Loading
        viewModelScope.launch {
            val res = repository.verifyWorker(token, workerId)
            _actionState.value = res
            if (res is NetworkResult.Success) {
                loadAllWorkers(token)
            }
        }
    }

    fun rejectWorker(token: String, workerId: Int) {
        _actionState.value = NetworkResult.Loading
        viewModelScope.launch {
            val res = repository.rejectWorker(token, workerId)
            _actionState.value = res
            if (res is NetworkResult.Success) {
                loadAllWorkers(token)
            }
        }
    }

    fun removeWorker(token: String, workerId: Int) {
        _actionState.value = NetworkResult.Loading
        viewModelScope.launch {
            val res = repository.removeWorker(token, workerId)
            // convert to MessageResponse if success
            if (res is NetworkResult.Success) {
                _actionState.value = NetworkResult.Success(MessageResponse(res.data.message))
                loadAllWorkers(token)
            } else if (res is NetworkResult.Error) {
                _actionState.value = NetworkResult.Error(res.message)
            }
        }
    }
}
