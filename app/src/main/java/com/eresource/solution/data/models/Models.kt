package com.eresource.solution.data.models

// 1. Auth & Identity Models
data class LoginRequest(
    val email_id: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val email_id: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val user_id: Int,
    val username: String,
    val email_id: String,
    val role: String, // 'user' | 'worker' | 'admin' | 'shop'
    val worker_id: Int?,
    val admin_id: Int?,
    val shop_id: Int? = null,
    val message: String
)

data class UserSession(
    val token: String,
    val userId: Int,
    val username: String,
    val email: String,
    val role: String, // 'user' | 'worker' | 'admin' | 'shop'
    val workerId: Int?,
    val adminId: Int?,
    val shopId: Int? = null,
    val mobile: String = "+91 9876543210",
    val kycStatus: String = "PENDING",
    val city: String = "Bengaluru",
    val state: String = "Karnataka",
    val pinCode: String = "560001",
    val referralCode: String = "ER${(1000..9999).random()}",
    val totalEarnings: String = "₹0"
)

data class DiagnosisHistory(
    val id: Int,
    val appliance: String,
    val problem: String,
    val date: String,
    val confidence: String,
    val estimatedCost: String
)

data class TechnicianStats(
    val completedJobs: Int = 278,
    val ongoingJobs: Int = 3,
    val successRate: String = "98%",
    val reviewsCount: Int = 163,
    val responseRate: String = "95%"
)

data class EarningsData(
    val today: String = "₹1,850",
    val week: String = "₹9,500",
    val month: String = "₹34,200",
    val lifetime: String = "₹4,87,000"
)

data class AdminCheckResponse(
    val is_admin: Boolean,
    val message: String?
)

data class FcmTokenRequest(
    val token: String
)

// 2. Technician Profile Models
data class Worker(
    val worker_id: Int,
    val shop_name: String,
    val shop_addr: String,
    val type: String, // 'Computer' | 'Electrician'
    val contact_no: String,
    val verified: Int?, // null = pending, 1 = verified, 0 = rejected
    val username: String? = null,
    val email_id: String? = null,
    val avg_rating: Float = 0f,
    val total_reviews: Int = 0,
    // KYC Details
    val aadhaar_no: String? = "XXXX XXXX 5678",
    val pan_no: String? = "ABCDE****F",
    val dob: String? = "12 Jan 1994"
)

data class WorkerStatusResponse(
    val has_applied: Boolean,
    val worker_id: Int?,
    val shop_name: String?,
    val type: String?,
    val verified: Int?
)

// 3. Resource & Rental Models
data class Resource(
    val resource_id: Int,
    val name: String,
    val price_per_hour: Double,
    val avail: Int,
    val total: Int,
    val description: String?
)

data class RentRequest(
    val resource_id: Int,
    val alloc_hour: Int
)

data class ReturnRequest(
    val allocation_id: Int
)

data class ReturnResponse(
    val message: String,
    val billing_hours: Int,
    val rate_per_hour: Double,
    val total_amount: Double
)

data class AllocationLog(
    val allocation_id: Int,
    val resource_id: Int,
    val worker_id: Int,
    val alloc_hour: Int,
    val alloc_time: String,
    val returned: Int, // 0 = false, 1 = true
    val resource_name: String?,
    val price_per_hour: Double?,
    val shop_name: String?
)

// 4. Booking System Models
data class Booking(
    val booking_id: Int,
    val user_id: Int,
    val worker_id: Int,
    val appliance: String,
    val problem: String,
    val scheduled_at: String,
    val status: String, // 'pending' | 'accepted' | 'rejected' | 'completed'
    val notes: String?,
    val created_at: String,
    // Joined details
    val customer_name: String? = null,
    val customer_email: String? = null,
    val shop_name: String? = null,
    val worker_type: String? = null,
    val worker_contact: String? = null
)

data class CreateBookingRequest(
    val worker_id: Int,
    val appliance: String,
    val problem: String,
    val scheduled_at: String,
    val notes: String
)

data class UpdateBookingStatusRequest(
    val booking_id: Int,
    val status: String
)

// 5. Review & Rating Models
data class Review(
    val review_id: Int,
    val user_id: Int,
    val worker_id: Int,
    val booking_id: Int,
    val rating: Int,
    val comment: String?,
    val created_at: String,
    val customer_name: String? = null,
    val shop_name: String? = null
)

data class ReviewRequest(
    val worker_id: Int,
    val booking_id: Int,
    val rating: Int,
    val comment: String
)

// 6. Chat System Models
data class ChatMessage(
    val message_id: Int,
    val booking_id: Int,
    val sender_id: Int,
    val message: String,
    val sent_at: String
)

data class SendMessageRequest(
    val booking_id: Int,
    val message: String
)

// 7. Rule-Based AI Diagnosis Models
data class DiagnosisRequest(
    val appliance: String,
    val problem: String
)

data class DiagnosisResult(
    val appliance: String,
    val problem: String,
    val likelyCategory: String,
    val cause: String?,
    val urgency: String?, // 'Low' | 'Medium' | 'High' | 'Critical'
    val recommendedWorkerType: String,
    val confidence: Int?,
    val estRepairTime: String?,
    val estCostRange: String?,
    val tips: List<String>?
)

// 8. Admin Dashboard Analytics Models
data class AnalyticsDashboardResponse(
    val summary: DashboardSummary,
    val appliancePopularity: List<ApplianceCount>,
    val monthlyTrend: List<MonthlyTrendCount>,
    val topWorkers: List<Worker>
)

data class DashboardSummary(
    val totalUsers: Int,
    val totalWorkers: Int,
    val totalBookings: Int,
    val totalRevenue: Double
)

data class ApplianceCount(
    val appliance: String,
    val count: Int
)

data class MonthlyTrendCount(
    val month: String,
    val count: Int
)

data class ToolRequest(
    val name: String,
    val category: String,
    val description: String,
    val price_hr: Double,
    val price_day: Double,
    val qty: Int
)

// General Response Wrapper
data class MessageResponse(
    val message: String
)
