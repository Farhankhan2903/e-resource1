package com.eresource.solution.viewmodel

import androidx.lifecycle.ViewModel
import com.eresource.solution.data.models.Worker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MapMarker(
    val name: String,
    val address: String,
    val type: String,
    val lat: Double,
    val lng: Double
)

class MapViewModel : ViewModel() {

    private val _markers = MutableStateFlow<List<MapMarker>>(emptyList())
    val markers: StateFlow<List<MapMarker>> = _markers.asStateFlow()

    init {
        loadMarkers()
    }

    private fun loadMarkers() {
        _markers.value = listOf(
            MapMarker("ElectroTech Systems", "123 Avenue, Tech District", "Electrician", 12.9716, 77.5946),
            MapMarker("CompRepair Hub", "456 Lane, Silicon Valley", "Computer", 12.9800, 77.6000),
            MapMarker("Quick Wire Masters", "78 Boulevard, Power Grid", "Electrician", 12.9650, 77.5850),
            MapMarker("LapTech Solutions", "19 Square, Cyber Tower", "Computer", 12.9900, 77.6100)
        )
    }
}
