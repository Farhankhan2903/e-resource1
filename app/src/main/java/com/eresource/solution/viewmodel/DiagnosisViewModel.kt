package com.eresource.solution.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eresource.solution.data.models.DiagnosisResult
import com.eresource.solution.data.models.Worker
import com.eresource.solution.data.repository.AppRepository
import com.eresource.solution.data.repository.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DiagnosisViewModel(private val repository: AppRepository) : ViewModel() {

    private val _appliance = MutableStateFlow("")
    val appliance: StateFlow<String> = _appliance.asStateFlow()

    private val _problem = MutableStateFlow("")
    val problem: StateFlow<String> = _problem.asStateFlow()

    private val _diagnosisState = MutableStateFlow<NetworkResult<DiagnosisResult>?>(null)
    val diagnosisState: StateFlow<NetworkResult<DiagnosisResult>?> = _diagnosisState.asStateFlow()

    // Recommended Technicians State
    private val _recommendedWorkers = MutableStateFlow<NetworkResult<List<Worker>>>(NetworkResult.Loading)
    val recommendedWorkers: StateFlow<NetworkResult<List<Worker>>> = _recommendedWorkers.asStateFlow()

    fun onApplianceChanged(value: String) {
        _appliance.value = value
    }

    fun onProblemChanged(value: String) {
        _problem.value = value
    }

    fun clearState() {
        _diagnosisState.value = null
    }

    fun runDiagnosis() {
        val app = _appliance.value.trim()
        val prob = _problem.value.trim()

        if (app.isEmpty() || prob.isEmpty()) {
            _diagnosisState.value = NetworkResult.Error("Please enter both the appliance name and fault symptoms")
            return
        }

        _diagnosisState.value = NetworkResult.Loading
        viewModelScope.launch {
            val result = repository.diagnoseAppliance(app, prob)
            _diagnosisState.value = result
            
            if (result is NetworkResult.Success) {
                loadRecommendedWorkers(result.data.recommendedWorkerType)
            }
        }
    }

    private fun loadRecommendedWorkers(type: String) {
        _recommendedWorkers.value = NetworkResult.Loading
        viewModelScope.launch {
            val allWorkers = repository.getWorkers()
            if (allWorkers is NetworkResult.Success) {
                // Smart Filter: Match worker type and prioritize verified
                val filtered = allWorkers.data.filter { it.type.contains(type, ignoreCase = true) || type.contains(it.type, ignoreCase = true) }
                    .sortedByDescending { it.verified == 1 }
                _recommendedWorkers.value = NetworkResult.Success(filtered)
            } else {
                _recommendedWorkers.value = allWorkers
            }
        }
    }
}
