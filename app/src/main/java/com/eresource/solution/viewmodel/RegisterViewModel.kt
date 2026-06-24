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

class RegisterViewModel(private val repository: AppRepository) : ViewModel() {

    private val _role = MutableStateFlow("user") // 'user' (Customer) or 'worker'
    val role: StateFlow<String> = _role.asStateFlow()

    private val _fullName = MutableStateFlow("")
    val fullName: StateFlow<String> = _fullName.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _isTermsAccepted = MutableStateFlow(false)
    val isTermsAccepted: StateFlow<Boolean> = _isTermsAccepted.asStateFlow()

    private val _registerState = MutableStateFlow<NetworkResult<AuthResponse>?>(null)
    val registerState: StateFlow<NetworkResult<AuthResponse>?> = _registerState.asStateFlow()

    fun onRoleChanged(value: String) {
        _role.value = value
    }

    fun onFullNameChanged(value: String) {
        _fullName.value = value
    }

    fun onEmailChanged(value: String) {
        _email.value = value
    }

    fun onPhoneChanged(value: String) {
        _phone.value = value
    }

    fun onPasswordChanged(value: String) {
        _password.value = value
    }

    fun onConfirmPasswordChanged(value: String) {
        _confirmPassword.value = value
    }

    fun onTermsAcceptedChanged(value: Boolean) {
        _isTermsAccepted.value = value
    }

    fun clearState() {
        _registerState.value = null
    }

    fun register() {
        val name = _fullName.value.trim()
        val mail = _email.value.trim()
        val phoneNum = _phone.value.trim()
        val pass = _password.value.trim()
        val confirmPass = _confirmPassword.value.trim()

        if (name.isEmpty()) {
            _registerState.value = NetworkResult.Error("Full Name is required")
            return
        }
        if (mail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
            _registerState.value = NetworkResult.Error("Valid email is required")
            return
        }
        if (phoneNum.length < 10) {
            _registerState.value = NetworkResult.Error("Valid phone number is required")
            return
        }
        if (pass.length < 6) {
            _registerState.value = NetworkResult.Error("Password must be at least 6 characters")
            return
        }
        if (pass != confirmPass) {
            _registerState.value = NetworkResult.Error("Passwords do not match")
            return
        }
        if (!_isTermsAccepted.value) {
            _registerState.value = NetworkResult.Error("You must accept the Terms and Conditions")
            return
        }

        _registerState.value = NetworkResult.Loading
        viewModelScope.launch {
            // Note: Backend 'register' takes (username, email_id, password)
            // We use fullName as username for this demo
            val result = repository.register(RegisterRequest(name, mail, pass))
            if (result is NetworkResult.Success) {
                repository.saveSession(result.data)
                _registerState.value = result
            } else if (result is NetworkResult.Error) {
                _registerState.value = result
            }
        }
    }
}
