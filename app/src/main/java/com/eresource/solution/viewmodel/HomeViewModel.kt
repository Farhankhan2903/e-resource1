package com.eresource.solution.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eresource.solution.data.models.Worker
import com.eresource.solution.data.repository.AppRepository
import com.eresource.solution.data.repository.NetworkResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: AppRepository) : ViewModel() {

    private val _workersState = MutableStateFlow<NetworkResult<List<Worker>>>(NetworkResult.Loading)
    val workersState: StateFlow<NetworkResult<List<Worker>>> = _workersState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedType = MutableStateFlow<String?>("All") // 'All', 'Computer', 'Electrician'
    val selectedType: StateFlow<String?> = _selectedType.asStateFlow()

    private val _workersList = MutableStateFlow<List<Worker>>(emptyList())

    // Combined Flow for live UI search and type filter
    val filteredWorkers: StateFlow<List<Worker>> = combine(_workersList, _searchQuery, _selectedType) { list, query, type ->
        list.filter { worker ->
            val matchQuery = query.isEmpty() || worker.shop_name.contains(query, ignoreCase = true) || worker.shop_addr.contains(query, ignoreCase = true)
            val matchType = type == "All" || worker.type.equals(type, ignoreCase = true)
            matchQuery && matchType
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadWorkers()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onTypeSelected(type: String?) {
        _selectedType.value = type
    }

    fun loadWorkers() {
        _workersState.value = NetworkResult.Loading
        viewModelScope.launch {
            val result = repository.getWorkers()
            _workersState.value = result
            if (result is NetworkResult.Success) {
                _workersList.value = result.data
            }
        }
    }
}
