package com.eresource.solution.data.api

import com.eresource.solution.data.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @GET("api/fetchWorkers")
    suspend fun fetchWorkers(): Response<List<Worker>>

    @GET("api/fetchWorkersAdmin")
    suspend fun fetchWorkersAdmin(
        @Header("Authorization") token: String
    ): Response<List<Worker>>

    @GET("api/workerStatus")
    suspend fun checkWorkerStatus(
        @Header("Authorization") token: String
    ): Response<WorkerStatusResponse>

    @GET("api/fetchWorkerSelfDetails")
    suspend fun fetchWorkerSelfDetails(
        @Header("Authorization") token: String
    ): Response<Worker>

    @Multipart
    @POST("api/applyForWorker")
    suspend fun applyForWorker(
        @Header("Authorization") token: String,
        @Part("shop_name") shopName: RequestBody,
        @Part("shop_addr") shopAddr: RequestBody,
        @Part("type") type: RequestBody,
        @Part("contact_no") contactNo: RequestBody,
        @Part passport: MultipartBody.Part?
    ): Response<AuthResponse>

    @POST("api/verifyWorkerById")
    suspend fun verifyWorkerById(
        @Header("Authorization") token: String,
        @Body request: Map<String, Int>
    ): Response<MessageResponse>

    @POST("api/rejectWorkerById")
    suspend fun rejectWorkerById(
        @Header("Authorization") token: String,
        @Body request: Map<String, Int>
    ): Response<MessageResponse>

    @POST("api/removeWorkerById")
    suspend fun removeWorkerById(
        @Header("Authorization") token: String,
        @Body request: Map<String, Int>
    ): Response<MessageResponse>

    @GET("api/checkAdminRights")
    suspend fun checkAdminRights(
        @Header("Authorization") token: String
    ): Response<AdminCheckResponse>

    // Shared Equipment / Resource Pool
    @GET("api/resource-api/fetchAvailResources")
    suspend fun fetchAvailableResources(): Response<List<Resource>>

    @GET("api/resource-api/fetchRentedResources")
    suspend fun fetchRentedResources(
        @Header("Authorization") token: String
    ): Response<List<AllocationLog>>

    @POST("api/resource-api/rentResource")
    suspend fun rentResource(
        @Header("Authorization") token: String,
        @Body request: RentRequest
    ): Response<MessageResponse>

    @POST("api/resource-api/returnResource")
    suspend fun returnResource(
        @Header("Authorization") token: String,
        @Body request: ReturnRequest
    ): Response<ReturnResponse>

    @GET("api/resource-api/fetchAllocationLogs")
    suspend fun fetchAllocationLogs(
        @Header("Authorization") token: String
    ): Response<List<AllocationLog>>

    // Booking System
    @POST("api/booking/createBooking")
    suspend fun createBooking(
        @Header("Authorization") token: String,
        @Body request: CreateBookingRequest
    ): Response<MessageResponse>

    @GET("api/booking/getBookings")
    suspend fun getBookings(
        @Header("Authorization") token: String
    ): Response<List<Booking>>

    @POST("api/booking/updateStatus")
    suspend fun updateBookingStatus(
        @Header("Authorization") token: String,
        @Body request: UpdateBookingStatusRequest
    ): Response<MessageResponse>

    // Ratings & Reviews
    @POST("api/reviews/addReview")
    suspend fun addReview(
        @Header("Authorization") token: String,
        @Body request: ReviewRequest
    ): Response<MessageResponse>

    @GET("api/reviews/getWorkerReviews")
    suspend fun getWorkerReviews(
        @Query("worker_id") workerId: Int?
    ): Response<List<Review>>

    // Chat System
    @POST("api/chat/sendMessage")
    suspend fun sendMessage(
        @Header("Authorization") token: String,
        @Body request: SendMessageRequest
    ): Response<MessageResponse>

    @GET("api/chat/getMessages")
    suspend fun getMessages(
        @Header("Authorization") token: String,
        @Query("booking_id") bookingId: Int
    ): Response<List<ChatMessage>>

    // AI Fault Diagnosis
    @POST("api/ai/diagnose")
    suspend fun diagnoseFault(
        @Body request: DiagnosisRequest
    ): Response<DiagnosisResult>

    // Admin Dashboard Analytics
    @GET("api/analytics/dashboard")
    suspend fun fetchAdminDashboard(
        @Header("Authorization") token: String
    ): Response<AnalyticsDashboardResponse>

    // Save FCM Token
    @POST("api/notifications/saveFcmToken")
    suspend fun saveFcmToken(
        @Header("Authorization") token: String,
        @Body request: FcmTokenRequest
    ): Response<MessageResponse>
    @GET("api/marketplace/getNearbyShops")
    suspend fun fetchShops(): Response<List<Shop>>

    @GET("api/marketplace/listTools")
    suspend fun fetchTools(@Query("shop_id") shopId: Int): Response<List<Tool>>

    @POST("api/marketplace/addTool")
    suspend fun addToolToShop(@Query("shop_id") shopId: Int, @Body request: ToolRequest): Response<MessageResponse>
}
