package com.eresource.solution.data.models

import com.google.gson.annotations.SerializedName

data class EnterpriseUser(
    @SerializedName("user_id") val id: Int,
    val username: String,
    val email: String,
    val role: String,
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String
)

data class ToolListing(
    @SerializedName("tool_id") val id: Int,
    @SerializedName("shop_id") val shopId: Int,
    val name: String,
    val category: String,
    @SerializedName("price_hr") val hourlyPrice: Double,
    @SerializedName("price_day") val dailyPrice: Double,
    @SerializedName("security_deposit") val securityDeposit: Double,
    @SerializedName("available_qty") val quantity: Int,
    @SerializedName("condition_status") val condition: String
)

data class BookingRequest(
    @SerializedName("tool_id") val toolId: Int,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String,
    @SerializedName("duration_type") val type: String // 'hourly' | 'daily'
)
