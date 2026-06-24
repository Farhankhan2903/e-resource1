package com.eresource.solution.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eresource.solution.data.models.*
import com.eresource.solution.data.repository.AppRepository
import com.eresource.solution.data.repository.NetworkResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MarketplaceViewModel(private val repository: AppRepository) : ViewModel() {

    private val _shops = MutableStateFlow<NetworkResult<List<Shop>>>(NetworkResult.Loading)
    val shops: StateFlow<NetworkResult<List<Shop>>> = _shops.asStateFlow()

    private val _myTools = MutableStateFlow<NetworkResult<List<Tool>>>(NetworkResult.Loading)
    val myTools: StateFlow<NetworkResult<List<Tool>>> = _myTools.asStateFlow()

    fun loadAllShops() {
        viewModelScope.launch {
            // repository.getShops() implementation needed in AppRepository
        }
    }

    fun loadShopTools(shopId: Int) {
        viewModelScope.launch {
            // repository.getTools(shopId) implementation needed
        }
    }

    fun addTool(shopId: Int, name: String, category: String, desc: String, hr: Double, day: Double, qty: Int) {
        viewModelScope.launch {
            // API call to add tool
        }
    }
}
