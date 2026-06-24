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

class LoginViewModel(private val repository: AppRepository) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _rememberMe = MutableStateFlow(false)
    val rememberMe: StateFlow<Boolean> = _rememberMe.asStateFlow()

    private val _loginState = MutableStateFlow<NetworkResult<AuthResponse>?>(null)
    val loginState: StateFlow<NetworkResult<AuthResponse>?> = _loginState.asStateFlow()

    // Forgot Password Flow
    private val _forgotPasswordState = MutableStateFlow<NetworkResult<MessageResponse>?>(null)
    val forgotPasswordState: StateFlow<NetworkResult<MessageResponse>?> = _forgotPasswordState.asStateFlow()

    fun onEmailChanged(value: String) {
        _email.value = value
    }

    fun onPasswordChanged(value: String) {
        _password.value = value
    }

    fun onRememberMeChanged(value: Boolean) {
        _rememberMe.value = value
    }

    fun clearState() {
        _loginState.value = null
        _forgotPasswordState.value = null
    }

    fun login() {
        val mail = _email.value.trim()
        val pass = _password.value.trim()

        if (mail.isEmpty()) {
            _loginState.value = NetworkResult.Error("Email is required")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
            _loginState.value = NetworkResult.Error("Please enter a valid email address")
            return
        }
        if (pass.isEmpty()) {
            _loginState.value = NetworkResult.Error("Password is required")
            return
        }

        _loginState.value = NetworkResult.Loading
        viewModelScope.launch {
            val result = repository.login(LoginRequest(mail, pass))
            if (result is NetworkResult.Success) {
                repository.saveSession(result.data)
                _loginState.value = result
            } else if (result is NetworkResult.Error) {
                _loginState.value = result
            }
        }
    }

    fun resetPassword(email: String) {
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _forgotPasswordState.value = NetworkResult.Error("Valid email required")
            return
        }

        _forgotPasswordState.value = NetworkResult.Loading
        viewModelScope.launch {
            // Mocking API call for reset
            kotlinx.coroutines.delay(1500)
            _forgotPasswordState.value = NetworkResult.Success(MessageResponse("Password reset link has been sent to your email."))
        }
    }
}
