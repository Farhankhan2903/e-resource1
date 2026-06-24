package com.eresource.solution.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eresource.solution.data.models.*
import com.eresource.solution.data.repository.AppRepository
import com.eresource.solution.data.repository.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WorkerSignupViewModel(private val repository: AppRepository) : ViewModel() {

    // 1. Personal Information
    private val _fullName = MutableStateFlow("")
    val fullName: StateFlow<String> = _fullName.asStateFlow()

    private val _dob = MutableStateFlow("")
    val dob: StateFlow<String> = _dob.asStateFlow()

    private val _gender = MutableStateFlow("Male")
    val gender: StateFlow<String> = _gender.asStateFlow()

    private val _address = MutableStateFlow("")
    val address: StateFlow<String> = _address.asStateFlow()

    private val _city = MutableStateFlow("")
    val city: StateFlow<String> = _city.asStateFlow()

    private val _state = MutableStateFlow("")
    val state: StateFlow<String> = _state.asStateFlow()

    private val _zip = MutableStateFlow("")
    val zip: StateFlow<String> = _zip.asStateFlow()

    // 2. Professional Information
    private val _trade = MutableStateFlow("Electrician")
    val trade: StateFlow<String> = _trade.asStateFlow()

    private val _experience = MutableStateFlow("0–1 Years")
    val experience: StateFlow<String> = _experience.asStateFlow()

    private val _selectedSkills = MutableStateFlow<Set<String>>(emptySet())
    val selectedSkills: StateFlow<Set<String>> = _selectedSkills.asStateFlow()

    // 3. Banking Information
    private val _bankName = MutableStateFlow("")
    val bankName: StateFlow<String> = _bankName.asStateFlow()

    private val _accountNumber = MutableStateFlow("")
    val accountNumber: StateFlow<String> = _accountNumber.asStateFlow()

    private val _ifscCode = MutableStateFlow("")
    val ifscCode: StateFlow<String> = _ifscCode.asStateFlow()

    // 4. Emergency Contact
    private val _emergencyName = MutableStateFlow("")
    val emergencyName: StateFlow<String> = _emergencyName.asStateFlow()

    private val _emergencyRelation = MutableStateFlow("")
    val emergencyRelation: StateFlow<String> = _emergencyRelation.asStateFlow()

    private val _emergencyPhone = MutableStateFlow("")
    val emergencyPhone: StateFlow<String> = _emergencyPhone.asStateFlow()

    private val _idFrontImage = MutableStateFlow<ByteArray?>(null)
    val idFrontImage: StateFlow<ByteArray?> = _idFrontImage.asStateFlow()

    private val _idBackImage = MutableStateFlow<ByteArray?>(null)
    val idBackImage: StateFlow<ByteArray?> = _idBackImage.asStateFlow()

    private val _selfieImage = MutableStateFlow<ByteArray?>(null)
    val selfieImage: StateFlow<ByteArray?> = _selfieImage.asStateFlow()

    // Status Tracking
    private val _currentStep = MutableStateFlow(1) // 1 to 4
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _signupState = MutableStateFlow<NetworkResult<AuthResponse>?>(null)
    val signupState: StateFlow<NetworkResult<AuthResponse>?> = _signupState.asStateFlow()

    // Options
    val tradeOptions = listOf("Electrician", "Plumber", "Appliance Repair Technician", "HVAC Technician", "Carpenter", "Painter", "Handyman", "Other")
    val experienceOptions = listOf("0–1 Years", "1–3 Years", "3–5 Years", "5–10 Years", "10+ Years")
    val skillOptions = listOf("Refrigerator Repair", "Washing Machine Repair", "Microwave Repair", "AC Repair", "TV Repair", "Water Heater Repair", "Dishwasher Repair")

    fun onFullNameChanged(v: String) { _fullName.value = v }
    fun onDobChanged(v: String) { _dob.value = v }
    fun onGenderChanged(v: String) { _gender.value = v }
    fun onAddressChanged(v: String) { _address.value = v }
    fun onCityChanged(v: String) { _city.value = v }
    fun onStateChanged(v: String) { _state.value = v }
    fun onZipChanged(v: String) { _zip.value = v }
    fun onTradeChanged(v: String) { _trade.value = v }
    fun onExperienceChanged(v: String) { _experience.value = v }
    fun onSkillToggled(skill: String) {
        val current = _selectedSkills.value.toMutableSet()
        if (current.contains(skill)) current.remove(skill) else current.add(skill)
        _selectedSkills.value = current
    }
    fun onBankNameChanged(v: String) { _bankName.value = v }
    fun onAccountChanged(v: String) { _accountNumber.value = v }
    fun onIfscChanged(v: String) { _ifscCode.value = v }
    fun onEmergencyNameChanged(v: String) { _emergencyName.value = v }
    fun onEmergencyRelationChanged(v: String) { _emergencyRelation.value = v }
    fun onEmergencyPhoneChanged(v: String) { _emergencyPhone.value = v }

    fun onIdFrontImageChanged(data: ByteArray?) { _idFrontImage.value = data }
    fun onIdBackImageChanged(data: ByteArray?) { _idBackImage.value = data }
    fun onSelfieImageChanged(data: ByteArray?) { _selfieImage.value = data }

    fun nextStep() {
        if (_currentStep.value < 4) _currentStep.value += 1
    }

    fun prevStep() {
        if (_currentStep.value > 1) _currentStep.value -= 1
    }

    fun clearState() {
        _signupState.value = null
    }

    fun submitKYC(token: String) {
        if (_fullName.value.isBlank() || _address.value.isBlank() || _accountNumber.value.isBlank()) {
            _signupState.value = NetworkResult.Error("Please fill out all mandatory KYC fields")
            return
        }

        _signupState.value = NetworkResult.Loading
        viewModelScope.launch {
            // Mocking specialized KYC submission - leveraging existing 'applyAsWorker'
            // We pass shopName as trade + name for demo purposes
            val result = repository.applyAsWorker(
                token = token,
                shopName = "${_trade.value}: ${_fullName.value}",
                shopAddr = "${_address.value}, ${_city.value}",
                type = if (_trade.value == "Electrician") "Electrician" else "Computer",
                contactNo = _emergencyPhone.value,
                imageBytes = _idFrontImage.value ?: _selfieImage.value, // Pass first available image for now
                fileName = "kyc_doc.png"
            )
            _signupState.value = result
            if (result is NetworkResult.Success) {
                // Ensure session is saved to prevent stale local data
                repository.saveSession(result.data)
                
                // Extra layer of safety: Sync immediately
                repository.rebuildRetrofit(repository.baseUrlFlow.first())
            }
        }
    }
}
