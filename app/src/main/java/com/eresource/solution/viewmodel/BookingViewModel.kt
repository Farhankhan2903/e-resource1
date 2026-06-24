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
import java.text.SimpleDateFormat
import java.util.*

class BookingViewModel(private val repository: AppRepository) : ViewModel() {

    // 1. APPLIANCE INFO
    private val _applianceType = MutableStateFlow("")
    val applianceType: StateFlow<String> = _applianceType.asStateFlow()
    private val _diagnosisSummary = MutableStateFlow("")
    val diagnosisSummary: StateFlow<String> = _diagnosisSummary.asStateFlow()

    // 2. CUSTOMER INFO
    private val _fullName = MutableStateFlow("")
    val fullName: StateFlow<String> = _fullName.asStateFlow()
    private val _mobileNumber = MutableStateFlow("")
    val mobileNumber: StateFlow<String> = _mobileNumber.asStateFlow()
    private val _altMobileNumber = MutableStateFlow("")
    val altMobileNumber: StateFlow<String> = _altMobileNumber.asStateFlow()

    // 3. INDIAN ADDRESS FIELDS
    private val _houseNo = MutableStateFlow("")
    val houseNo: StateFlow<String> = _houseNo.asStateFlow()
    private val _streetArea = MutableStateFlow("")
    val streetArea: StateFlow<String> = _streetArea.asStateFlow()
    private val _landmark = MutableStateFlow("")
    val landmark: StateFlow<String> = _landmark.asStateFlow()
    private val _city = MutableStateFlow("")
    val city: StateFlow<String> = _city.asStateFlow()
    private val _state = MutableStateFlow("")
    val state: StateFlow<String> = _state.asStateFlow()
    private val _pinCode = MutableStateFlow("")
    val pinCode: StateFlow<String> = _pinCode.asStateFlow()

    // 4. SCHEDULING
    private val _selectedDate = MutableStateFlow<Long?>(null)
    val selectedDate: StateFlow<Long?> = _selectedDate.asStateFlow()
    private val _selectedTimeSlot = MutableStateFlow("")
    val selectedTimeSlot: StateFlow<String> = _selectedTimeSlot.asStateFlow()
    val timeSlots = listOf("9:00 AM – 11:00 AM", "11:00 AM – 1:00 PM", "2:00 PM – 4:00 PM", "4:00 PM – 6:00 PM")

    // 5. PAYMENT & NOTES
    private val _paymentMethod = MutableStateFlow("Cash After Service")
    val paymentMethod: StateFlow<String> = _paymentMethod.asStateFlow()
    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    private val _bookingState = MutableStateFlow<NetworkResult<MessageResponse>?>(null)
    val bookingState: StateFlow<NetworkResult<MessageResponse>?> = _bookingState.asStateFlow()

    fun onApplianceTypeChanged(v: String) { _applianceType.value = v }
    fun onDiagnosisChanged(v: String) { _diagnosisSummary.value = v }
    fun onFullNameChanged(v: String) { _fullName.value = v }
    fun onMobileChanged(v: String) { _mobileNumber.value = v }
    fun onAltMobileChanged(v: String) { _altMobileNumber.value = v }
    fun onHouseNoChanged(v: String) { _houseNo.value = v }
    fun onStreetAreaChanged(v: String) { _streetArea.value = v }
    fun onLandmarkChanged(v: String) { _landmark.value = v }
    fun onCityChanged(v: String) { _city.value = v }
    fun onStateChanged(v: String) { _state.value = v }
    fun onPinCodeChanged(v: String) { _pinCode.value = v }
    fun onDateSelected(v: Long?) { _selectedDate.value = v }
    fun onTimeSlotSelected(v: String) { _selectedTimeSlot.value = v }
    fun onPaymentMethodChanged(v: String) { _paymentMethod.value = v }
    fun onNotesChanged(v: String) { _notes.value = v }

    fun initFromDiagnosis(app: String, prob: String) {
        _applianceType.value = app
        _diagnosisSummary.value = prob
    }

    fun isFormValid(): Boolean {
        return _fullName.value.isNotBlank() && 
               _mobileNumber.value.length >= 10 && 
               _houseNo.value.isNotBlank() &&
               _city.value.isNotBlank() &&
               _pinCode.value.length == 6 &&
               _selectedDate.value != null &&
               _selectedTimeSlot.value.isNotBlank()
    }

    fun createBooking(token: String, workerId: Int) {
        if (!isFormValid()) return

        val fullAddress = "${_houseNo.value}, ${_streetArea.value}, Landmark: ${_landmark.value}, ${_city.value}, ${_state.value} - ${_pinCode.value}"
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val dateStr = _selectedDate.value?.let { sdf.format(Date(it)) } ?: ""
        val schedule = "$dateStr • ${_selectedTimeSlot.value}"

        _bookingState.value = NetworkResult.Loading
        viewModelScope.launch {
            _bookingState.value = repository.createBooking(
                token = token,
                workerId = workerId,
                appliance = _applianceType.value,
                problem = _diagnosisSummary.value,
                scheduledAt = schedule,
                notes = "Address: $fullAddress | Pay: ${_paymentMethod.value} | Note: ${_notes.value}"
            )
        }
    }

    fun clearState() {
        _bookingState.value = null
    }
}
