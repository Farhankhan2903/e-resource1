package com.eresource.solution.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eresource.solution.data.models.ChatMessage
import com.eresource.solution.data.models.MessageResponse
import com.eresource.solution.data.repository.AppRepository
import com.eresource.solution.data.repository.NetworkResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(private val repository: AppRepository) : ViewModel() {

    private val _messagesState = MutableStateFlow<NetworkResult<List<ChatMessage>>>(NetworkResult.Loading)
    val messagesState: StateFlow<NetworkResult<List<ChatMessage>>> = _messagesState.asStateFlow()

    private val _sendState = MutableStateFlow<NetworkResult<MessageResponse>?>(null)
    val sendState: StateFlow<NetworkResult<MessageResponse>?> = _sendState.asStateFlow()

    private var pollJob: Job? = null

    fun startPolling(token: String, bookingId: Int) {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (true) {
                // Fetch messages
                val res = repository.getChatMessages(token, bookingId)
                _messagesState.value = res
                delay(3000) // Poll every 3 seconds
            }
        }
    }

    fun stopPolling() {
        pollJob?.cancel()
    }

    fun clearSendState() {
        _sendState.value = null
    }

    fun sendMessage(token: String, bookingId: Int, messageText: String) {
        val msg = messageText.trim()
        if (msg.isEmpty()) return

        _sendState.value = NetworkResult.Loading
        viewModelScope.launch {
            val result = repository.sendChatMessage(token, bookingId, msg)
            _sendState.value = result
            if (result is NetworkResult.Success) {
                // Immediately refresh in background
                val refresh = repository.getChatMessages(token, bookingId)
                if (refresh is NetworkResult.Success) {
                    _messagesState.value = refresh
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }
}
