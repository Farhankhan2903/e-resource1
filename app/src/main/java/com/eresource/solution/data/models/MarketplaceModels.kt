package com.eresource.solution.data.models

data class Shop(
    val shop_id: Int,
    val owner_id: Int,
    val name: String,
    val description: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val contact: String,
    val hours: String,
    val rating: Float
)

data class Tool(
    val tool_id: Int,
    val shop_id: Int,
    val name: String,
    val category: String,
    val description: String,
    val price_hr: Double,
    val price_day: Double,
    val qty: Int,
    val status: String
)

data class RentalRequest(
    val request_id: Int,
    val worker_id: Int,
    val tool_id: Int,
    val duration_hrs: Int,
    val status: String,
    val created_at: String
)
