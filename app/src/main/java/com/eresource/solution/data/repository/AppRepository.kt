package com.eresource.solution.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.eresource.solution.data.api.ApiService
import com.eresource.solution.data.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

// Datastore context delegate
val Context.dataStore by preferencesDataStore(name = "eresource_preferences")

sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String, val code: Int? = null) : NetworkResult<Nothing>()
    object Loading : NetworkResult<Nothing>()
}

class AppRepository(private val context: Context) {

    companion object {
        // Preference Keys
        private val JWT_TOKEN = stringPreferencesKey("jwt_token")
        private val USER_ID = intPreferencesKey("user_id")
        private val USERNAME = stringPreferencesKey("username")
        private val EMAIL = stringPreferencesKey("email")
        private val ROLE = stringPreferencesKey("role")
        private val WORKER_ID = intPreferencesKey("worker_id")
        private val ADMIN_ID = intPreferencesKey("admin_id")
        private val SHOP_ID = intPreferencesKey("shop_id")
        private val DARK_MODE = booleanPreferencesKey("dark_mode")
        private val BASE_URL_KEY = stringPreferencesKey("base_url")

        // Live dev/shared endpoint default fallbacks
        const val DEFAULT_DEVELOPMENT_URL = "http://10.0.2.2:4000/"
        // Emulator localhost fallback
        const val EMULATOR_LOCALHOST_URL = "http://10.0.2.2:4000/"
    }

    private var activeApiService: ApiService? = null
    private var activeBaseUrl: String = DEFAULT_DEVELOPMENT_URL

    init {
        // Default init
        rebuildRetrofit(DEFAULT_DEVELOPMENT_URL)
    }

    // Dynamic retrofit instantiation to support base URL overrides in Emulator sandbox
    fun rebuildRetrofit(baseUrl: String) {
        val targetUrl = if (baseUrl.isBlank()) DEFAULT_DEVELOPMENT_URL else baseUrl
        val sanitized = if (targetUrl.endsWith("/")) targetUrl else "$targetUrl/"
        activeBaseUrl = sanitized
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()

        try {
            val retrofit = Retrofit.Builder()
                .baseUrl(sanitized)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()

            activeApiService = retrofit.create(ApiService::class.java)
        } catch (e: Throwable) {
            val fallbackUrl = if (DEFAULT_DEVELOPMENT_URL.endsWith("/")) DEFAULT_DEVELOPMENT_URL else "$DEFAULT_DEVELOPMENT_URL/"
            activeBaseUrl = fallbackUrl
            try {
                val retrofit = Retrofit.Builder()
                    .baseUrl(fallbackUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build()
                activeApiService = retrofit.create(ApiService::class.java)
            } catch (fatal: Throwable) {
                fatal.printStackTrace()
            }
        }
    }

    fun getApiService(): ApiService {
        return activeApiService ?: throw IllegalStateException("ApiService is uninitialized")
    }

    // DATASTORE OPERATORS

    val userSessionFlow: Flow<UserSession?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { prefs ->
            val token = prefs[JWT_TOKEN]
            val userId = prefs[USER_ID]
            val username = prefs[USERNAME]
            val email = prefs[EMAIL]
            val role = prefs[ROLE]
            val workerId = prefs[WORKER_ID]
            val adminId = prefs[ADMIN_ID]
            val shopId = prefs[SHOP_ID]

            if (token != null && userId != null && username != null && email != null && role != null) {
                UserSession(token, userId, username, email, role, workerId, adminId, shopId)
            } else {
                null
            }
        }

    val darkModeFlow: Flow<Boolean> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs -> prefs[DARK_MODE] ?: false }

    val baseUrlFlow: Flow<String> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs -> prefs[BASE_URL_KEY] ?: DEFAULT_DEVELOPMENT_URL }

    suspend fun saveSession(auth: AuthResponse) {
        context.dataStore.edit { prefs ->
            prefs[JWT_TOKEN] = auth.token
            prefs[USER_ID] = auth.user_id
            prefs[USERNAME] = auth.username
            prefs[EMAIL] = auth.email_id
            prefs[ROLE] = auth.role
            auth.worker_id?.let { prefs[WORKER_ID] = it } ?: prefs.remove(WORKER_ID)
            auth.admin_id?.let { prefs[ADMIN_ID] = it } ?: prefs.remove(ADMIN_ID)
            auth.shop_id?.let { prefs[SHOP_ID] = it } ?: prefs.remove(SHOP_ID)
        }
    }

    suspend fun saveBaseUrl(url: String) {
        context.dataStore.edit { prefs ->
            prefs[BASE_URL_KEY] = url
        }
        rebuildRetrofit(url)
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DARK_MODE] = enabled
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(JWT_TOKEN)
            prefs.remove(USER_ID)
            prefs.remove(USERNAME)
            prefs.remove(EMAIL)
            prefs.remove(ROLE)
            prefs.remove(WORKER_ID)
            prefs.remove(ADMIN_ID)
        }
    }

    // AUTH SERVICE ACTIONS

    suspend fun login(req: LoginRequest): NetworkResult<AuthResponse> = safeApiCall {
        getApiService().login(req)
    }

    suspend fun register(req: RegisterRequest): NetworkResult<AuthResponse> = safeApiCall {
        getApiService().register(req)
    }

    // WORKER PROFILE ACTIONS

    suspend fun getWorkers(): NetworkResult<List<Worker>> = safeApiCall {
        getApiService().fetchWorkers()
    }

    suspend fun getWorkersAdmin(token: String): NetworkResult<List<Worker>> = safeApiCall {
        getApiService().fetchWorkersAdmin("Bearer $token")
    }

    suspend fun checkWorkerStatus(token: String): NetworkResult<WorkerStatusResponse> = safeApiCall {
        getApiService().checkWorkerStatus("Bearer $token")
    }

    suspend fun fetchWorkerSelf(token: String): NetworkResult<Worker> = safeApiCall {
        getApiService().fetchWorkerSelfDetails("Bearer $token")
    }

    suspend fun applyAsWorker(
        token: String,
        shopName: String,
        shopAddr: String,
        type: String,
        contactNo: String,
        imageBytes: ByteArray?,
        fileName: String?
    ): NetworkResult<AuthResponse> {
        val sName = shopName.toRequestBody("text/plain".toMediaTypeOrNull())
        val sAddr = shopAddr.toRequestBody("text/plain".toMediaTypeOrNull())
        val sType = type.toRequestBody("text/plain".toMediaTypeOrNull())
        val sContact = contactNo.toRequestBody("text/plain".toMediaTypeOrNull())

        var imagePart: MultipartBody.Part? = null
        if (imageBytes != null && fileName != null) {
            val reqBody = imageBytes.toRequestBody("image/*".toMediaTypeOrNull())
            imagePart = MultipartBody.Part.createFormData("passport", fileName, reqBody)
        }

        return safeApiCall {
            getApiService().applyForWorker(
                "Bearer $token",
                sName,
                sAddr,
                sType,
                sContact,
                imagePart
            )
        }
    }

    suspend fun checkIsAdmin(token: String): NetworkResult<AdminCheckResponse> = safeApiCall {
        getApiService().checkAdminRights("Bearer $token")
    }

    suspend fun verifyWorker(token: String, workerId: Int): NetworkResult<MessageResponse> = safeApiCall {
        getApiService().verifyWorkerById("Bearer $token", mapOf("worker_id" to workerId))
    }

    suspend fun rejectWorker(token: String, workerId: Int): NetworkResult<MessageResponse> = safeApiCall {
        getApiService().rejectWorkerById("Bearer $token", mapOf("worker_id" to workerId))
    }

    suspend fun removeWorker(token: String, workerId: Int): NetworkResult<MessageResponse> = safeApiCall {
        getApiService().removeWorkerById("Bearer $token", mapOf("worker_id" to workerId))
    }

    // SHARED POOL ACTIONS

    suspend fun getAvailableResources(): NetworkResult<List<Resource>> = safeApiCall {
        getApiService().fetchAvailableResources()
    }

    suspend fun getRentedResources(token: String): NetworkResult<List<AllocationLog>> = safeApiCall {
        getApiService().fetchRentedResources("Bearer $token")
    }

    suspend fun rentResource(token: String, resourceId: Int, hours: Int): NetworkResult<MessageResponse> = safeApiCall {
        getApiService().rentResource("Bearer $token", RentRequest(resourceId, hours))
    }

    suspend fun returnResource(token: String, allocationId: Int): NetworkResult<ReturnResponse> = safeApiCall {
        getApiService().returnResource("Bearer $token", ReturnRequest(allocationId))
    }

    suspend fun getAllocationLogs(token: String): NetworkResult<List<AllocationLog>> = safeApiCall {
        getApiService().fetchAllocationLogs("Bearer $token")
    }

    // BOOKING SERVICE ACTIONS

    suspend fun createBooking(
        token: String,
        workerId: Int,
        appliance: String,
        problem: String,
        scheduledAt: String,
        notes: String
    ): NetworkResult<MessageResponse> = safeApiCall {
        getApiService().createBooking(
            "Bearer $token",
            CreateBookingRequest(workerId, appliance, problem, scheduledAt, notes)
        )
    }

    suspend fun getBookings(token: String): NetworkResult<List<Booking>> = safeApiCall {
        getApiService().getBookings("Bearer $token")
    }

    suspend fun updateBookingStatus(token: String, bookingId: Int, status: String): NetworkResult<MessageResponse> = safeApiCall {
        getApiService().updateBookingStatus(
            "Bearer $token",
            UpdateBookingStatusRequest(bookingId, status)
        )
    }

    // REVIEWS ACTIONS

    suspend fun addReview(token: String, workerId: Int, bookingId: Int, rating: Int, comment: String): NetworkResult<MessageResponse> = safeApiCall {
        getApiService().addReview("Bearer $token", ReviewRequest(workerId, bookingId, rating, comment))
    }

    suspend fun getReviews(workerId: Int?): NetworkResult<List<Review>> = safeApiCall {
        getApiService().getWorkerReviews(workerId)
    }

    // CHAT ACTIONS

    suspend fun sendChatMessage(token: String, bookingId: Int, msg: String): NetworkResult<MessageResponse> = safeApiCall {
        getApiService().sendMessage("Bearer $token", SendMessageRequest(bookingId, msg))
    }

    suspend fun getChatMessages(token: String, bookingId: Int): NetworkResult<List<ChatMessage>> = safeApiCall {
        getApiService().getMessages("Bearer $token", bookingId)
    }

    // DIAGNOSTIC DIAGNOSIS

    suspend fun diagnoseAppliance(appliance: String, problem: String): NetworkResult<DiagnosisResult> = safeApiCall {
        getApiService().diagnoseFault(DiagnosisRequest(appliance, problem))
    }

    // ANALYTICS DASHBOARD

    suspend fun fetchAnalytics(token: String): NetworkResult<AnalyticsDashboardResponse> = safeApiCall {
        getApiService().fetchAdminDashboard("Bearer $token")
    }

    // FCM TOKEN SAVING

    suspend fun saveFcmToken(tokenStr: String, userToken: String): NetworkResult<MessageResponse> = safeApiCall {
        getApiService().saveFcmToken("Bearer $userToken", FcmTokenRequest(tokenStr))
    }

    // MARKETPLACE ACTIONS

    suspend fun getShops(): NetworkResult<List<Shop>> = safeApiCall {
        getApiService().fetchShops()
    }

    suspend fun getTools(shopId: Int): NetworkResult<List<Tool>> = safeApiCall {
        getApiService().fetchTools(shopId)
    }

    suspend fun addTool(shopId: Int, req: ToolRequest): NetworkResult<MessageResponse> = safeApiCall {
        getApiService().addToolToShop(shopId, req)
    }

    // GENERIC SAFE API EXECUTION
    private suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): NetworkResult<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    NetworkResult.Success(body)
                } else {
                    NetworkResult.Error("Empty server response content")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = if (!errorBody.isNullOrBlank() && errorBody.startsWith("{")) {
                    // Try to extract "message" from JSON error body if possible
                    try {
                        val json = com.google.gson.JsonParser.parseString(errorBody).asJsonObject
                        json.get("message")?.asString ?: "Unknown Error"
                    } catch (e: Exception) {
                        "API Error ${response.code()}: $errorBody"
                    }
                } else {
                    response.message().ifBlank { "API Error ${response.code()}" }
                }
                NetworkResult.Error(errorMsg, response.code())
            }
        } catch (e: java.net.ConnectException) {
            NetworkResult.Error("Cannot connect to server. Ensure backend is running and IP is correct.")
        } catch (e: java.net.SocketTimeoutException) {
            NetworkResult.Error("Server connection timed out. Try again.")
        } catch (e: Throwable) {
            NetworkResult.Error("Network Error: ${e.localizedMessage ?: "Unknown connection failure"}")
        }
    }
}
