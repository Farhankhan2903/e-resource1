package com.eresource.solution.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eresource.solution.data.repository.AppRepository
import com.eresource.solution.data.repository.NetworkResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PaymentViewModel(private val repository: AppRepository) : ViewModel() {

    private val _paymentState = MutableStateFlow<NetworkResult<String>?>(null)
    val paymentState: StateFlow<NetworkResult<String>?> = _paymentState.asStateFlow()

    fun processPayment(method: String, amount: Double) {
        _paymentState.value = NetworkResult.Loading
        viewModelScope.launch {
            // Simulate a smooth payment process
            delay(2000)
            if (method == "Cash") {
                _paymentState.value = NetworkResult.Success("Cash payment recorded. Please hand over ₹$amount at the counter.")
            } else {
                _paymentState.value = NetworkResult.Success("Payment of ₹$amount successful via $method!")
            }
        }
    }

    fun clearState() {
        _paymentState.value = null
    }
}
