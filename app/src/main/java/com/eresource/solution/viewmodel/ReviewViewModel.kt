package com.eresource.solution.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eresource.solution.data.models.MessageResponse
import com.eresource.solution.data.repository.AppRepository
import com.eresource.solution.data.repository.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReviewViewModel(private val repository: AppRepository) : ViewModel() {

    private val _rating = MutableStateFlow(5)
    val rating: StateFlow<Int> = _rating.asStateFlow()

    private val _comment = MutableStateFlow("")
    val comment: StateFlow<String> = _comment.asStateFlow()

    private val _submitState = MutableStateFlow<NetworkResult<MessageResponse>?>(null)
    val submitState: StateFlow<NetworkResult<MessageResponse>?> = _submitState.asStateFlow()

    fun onRatingChanged(value: Int) {
        _rating.value = value
    }

    fun onCommentChanged(value: String) {
        _comment.value = value
    }

    fun clearState() {
        _submitState.value = null
        _comment.value = ""
        _rating.value = 5
    }

    fun submitReview(token: String, workerId: Int, bookingId: Int) {
        val r = _rating.value
        val c = _comment.value.trim()

        _submitState.value = NetworkResult.Loading
        viewModelScope.launch {
            _submitState.value = repository.addReview(
                token = token,
                workerId = workerId,
                bookingId = bookingId,
                rating = r,
                comment = c
            )
        }
    }
}
