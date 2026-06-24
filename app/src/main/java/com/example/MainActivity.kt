package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.eresource.solution.data.DependencyProvider
import com.eresource.solution.data.models.UserSession
import com.eresource.solution.data.repository.AppRepository
import com.eresource.solution.ui.screens.*
import com.eresource.solution.ui.theme.EResourceSolutionTheme
import com.eresource.solution.viewmodel.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Dynamic initialization
        DependencyProvider.initialize(applicationContext)
        val repository = DependencyProvider.provideRepository()

        // Sync local base url from system preferences first
        lifecycleScopeLaunch {
            try {
                val savedUrl = repository.baseUrlFlow.first()
                repository.rebuildRetrofit(savedUrl)
                
                // Post fake FCM push token registration in background
                repository.userSessionFlow.first()?.let { session ->
                    repository.saveFcmToken("mock_fcm_token_3810294812_android", session.token)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        enableEdgeToEdge()
        setContent {
            val userSessionState by repository.userSessionFlow.collectAsState(initial = null)
            val darkModeState by repository.darkModeFlow.collectAsState(initial = false)

            var startDestState by remember { mutableStateOf<String?>(null) }
            var isSplashShowing by remember { mutableStateOf(true) }
            var animatedStart by remember { mutableStateOf(false) }

            // Spring bouncy anim for the E-Resource logo icon
            val scale by animateFloatAsState(
                targetValue = if (animatedStart) 1f else 0.4f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "scale"
            )

            // Smooth opacity transition fade
            val alpha by animateFloatAsState(
                targetValue = if (animatedStart) 1f else 0f,
                animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing),
                label = "alpha"
            )

            // Dynamic rotation for background gear wheel representation
            val infiniteTransition = rememberInfiniteTransition(label = "gearRotation")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(6000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "rotation"
            )

            LaunchedEffect(Unit) {
                animatedStart = true
                val session = repository.userSessionFlow.first()
                startDestState = when {
                    session == null -> "login"
                    session.role.lowercase() == "admin" -> "admin_home"
                    session.role.lowercase() == "worker" -> "worker_home"
                    session.role.lowercase() == "shop" -> "shop_home"
                    else -> "home"
                }
                // Allow the splash animation to show fully for 2200ms
                delay(2200)
                isSplashShowing = false
            }

            EResourceSolutionTheme(darkTheme = darkModeState) {
                Crossfade(targetState = isSplashShowing, label = "splashTransition") { showSplash ->
                    if (showSplash) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF0D1618),
                                            Color(0xFF1E88E5)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .padding(24.dp)
                                    .scale(scale)
                                    .alpha(alpha)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(160.dp)
                                ) {
                                    // Rotating Background Mechanic Gear
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = null,
                                        tint = Color(0xFF00BCD4).copy(alpha = 0.25f),
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .rotate(rotation)
                                    )
                                    // Primary central active hardware tool kit icon
                                    Icon(
                                        imageVector = Icons.Default.Build,
                                        contentDescription = "E-Resource Logo",
                                        tint = Color.White,
                                        modifier = Modifier.size(64.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                    text = "E-RESOURCE",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    letterSpacing = 4.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Appliance Diagnostics & Shared Tool Pool",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.75f),
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(modifier = Modifier.height(48.dp))

                                // Modern visual progressive loading status bar
                                LinearProgressIndicator(
                                    color = Color(0xFF00BCD4),
                                    trackColor = Color.White.copy(alpha = 0.2f),
                                    modifier = Modifier
                                        .width(180.dp)
                                        .height(4.dp)
                                )
                            }
                        }
                    } else {
                        val navController = rememberNavController()
                        val startDest = startDestState

                        if (startDest != null) {
                            NavHost(
                                navController = navController,
                                startDestination = startDest,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                composable("login") {
                                    val loginViewModel = remember { LoginViewModel(repository) }
                                    LoginScreen(
                                        viewModel = loginViewModel,
                                        onLoginSuccess = { auth ->
                                            val role = auth.role.lowercase()
                                            val target = when {
                                                role == "admin" -> "admin_home"
                                                role == "worker" && auth.worker_id == null -> "worker_signup"
                                                role == "worker" -> "worker_home"
                                                role == "shop" && auth.shop_id == null -> "shop_signup"
                                                role == "shop" -> "shop_home"
                                                else -> "home"
                                            }
                                            navController.navigate(target) {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        },
                                        onNavigateToRegister = {
                                            navController.navigate("register")
                                        },
                                        onNavigateToForgotPassword = {
                                            navController.navigate("forgot_password")
                                        }
                                    )
                                }

                                composable("forgot_password") {
                                    val loginViewModel = remember { LoginViewModel(repository) }
                                    ForgotPasswordScreen(
                                        viewModel = loginViewModel,
                                        onNavigateBack = { navController.popBackStack() }
                                    )
                                }

                                composable("register") {
                                    val registerViewModel = remember { RegisterViewModel(repository) }
                                    RegisterScreen(
                                        viewModel = registerViewModel,
                                        onRegisterSuccess = { auth ->
                                            val role = auth.role.lowercase()
                                            val target = when {
                                                role == "worker" -> "worker_signup"
                                                role == "shop" -> "shop_signup"
                                                else -> "home"
                                            }
                                            navController.navigate(target) {
                                                popUpTo("register") { inclusive = true }
                                            }
                                        },
                                        onNavigateToLogin = {
                                            navController.popBackStack()
                                        },
                                        onNavigateToLegal = {
                                            navController.navigate("legal")
                                        }
                                    )
                                }

                                composable("home") {
                                    val homeViewModel = remember { HomeViewModel(repository) }
                                    // Automatically reloads on focus
                                    LaunchedEffect(Unit) {
                                        homeViewModel.loadWorkers()
                                    }
                                    HomeScreen(
                                        viewModel = homeViewModel,
                                        onApplianceSelected = { appName, _ ->
                                            navController.navigate("diagnosis?appliance=$appName")
                                        },
                                        onBookWorkerSelected = { workerId, shopName ->
                                            navController.navigate("booking/$workerId/$shopName")
                                        },
                                        onNavigateToDiagnosis = {
                                            navController.navigate("diagnosis")
                                        },
                                        onNavigateToHistory = {
                                            navController.navigate("diagnosis_history")
                                        },
                                        onNavigateToMap = {
                                            navController.navigate("map")
                                        },
                                        onNavigateToChatbot = {
                                            navController.navigate("chatbot")
                                        },
                                        onNavigateToProfile = {
                                            navController.navigate("profile")
                                        }
                                    )
                                }

                                composable(
                                    route = "booking/{workerId}/{shopName}?appliance={appliance}&problem={problem}",
                                    arguments = listOf(
                                        navArgument("workerId") { type = NavType.IntType },
                                        navArgument("shopName") { type = NavType.StringType },
                                        navArgument("appliance") { type = NavType.StringType; nullable = true; defaultValue = null },
                                        navArgument("problem") { type = NavType.StringType; nullable = true; defaultValue = null }
                                    )
                                ) { backStackEntry ->
                                    val workerId = backStackEntry.arguments?.getInt("workerId") ?: 0
                                    val shopName = backStackEntry.arguments?.getString("shopName") ?: "Technician"
                                    val initApp = backStackEntry.arguments?.getString("appliance")
                                    val initProb = backStackEntry.arguments?.getString("problem")

                                    val bookingViewModel = remember { BookingViewModel(repository) }
                                    val session = userSessionState ?: return@composable

                                    BookingScreen(
                                        viewModel = bookingViewModel,
                                        userToken = session.token,
                                        workerId = workerId,
                                        workerShopName = shopName,
                                        initialAppliance = initApp,
                                        initialProblem = initProb,
                                        onNavigateBack = { navController.popBackStack() },
                                        onBookingSuccess = { bookingId, name, date ->
                                            navController.navigate("booking_success/$bookingId/$name/$date") {
                                                popUpTo("home")
                                            }
                                        }
                                    )
                                }

                                composable("diagnosis_history") {
                                    DiagnosisHistoryScreen(
                                        onNavigateBack = { navController.popBackStack() }
                                    )
                                }

                                composable("history") {
                                    val historyViewModel = remember { ServiceHistoryViewModel(repository) }
                                    val session = userSessionState ?: return@composable

                                    ServiceHistoryScreen(
                                        viewModel = historyViewModel,
                                        userToken = session.token,
                                        onNavigateBack = { navController.popBackStack() },
                                        onNavigateToChat = { bId, userLabel ->
                                            navController.navigate("chat/$bId/$userLabel")
                                        },
                                        onNavigateToReview = { bId, wId, techName ->
                                            navController.navigate("review/$bId/$wId/$techName")
                                        }
                                    )
                                }

                                composable(
                                    route = "chat/{bookingId}/{otherName}",
                                    arguments = listOf(
                                        navArgument("bookingId") { type = NavType.IntType },
                                        navArgument("otherName") { type = NavType.StringType }
                                    )
                                ) { backStackEntry ->
                                    val bookingId = backStackEntry.arguments?.getInt("bookingId") ?: 0
                                    val otherName = backStackEntry.arguments?.getString("otherName") ?: "Partner"

                                    val chatViewModel = remember { ChatViewModel(repository) }
                                    val session = userSessionState ?: return@composable

                                    ChatScreen(
                                        viewModel = chatViewModel,
                                        userToken = session.token,
                                        currentUserId = session.userId,
                                        bookingId = bookingId,
                                        otherPartyName = otherName,
                                        onNavigateBack = { navController.popBackStack() }
                                    )
                                }

                                composable(
                                    route = "review/{bookingId}/{workerId}/{techName}",
                                    arguments = listOf(
                                        navArgument("bookingId") { type = NavType.IntType },
                                        navArgument("workerId") { type = NavType.IntType },
                                        navArgument("techName") { type = NavType.StringType }
                                    )
                                ) { backStackEntry ->
                                    val bookingId = backStackEntry.arguments?.getInt("bookingId") ?: 0
                                    val workerId = backStackEntry.arguments?.getInt("workerId") ?: 0
                                    val techName = backStackEntry.arguments?.getString("techName") ?: "Technician"

                                    val reviewViewModel = remember { ReviewViewModel(repository) }
                                    val session = userSessionState ?: return@composable

                                    ReviewScreen(
                                        viewModel = reviewViewModel,
                                        userToken = session.token,
                                        bookingId = bookingId,
                                        workerId = workerId,
                                        technicianName = techName,
                                        onNavigateBack = { navController.popBackStack() },
                                        onReviewSuccess = {
                                            navController.popBackStack()
                                        }
                                    )
                                }

                                composable(
                                    route = "payment/{amount}",
                                    arguments = listOf(navArgument("amount") { type = NavType.FloatType })
                                ) { backStackEntry ->
                                    val amount = backStackEntry.arguments?.getFloat("amount") ?: 0f
                                    val paymentViewModel = remember { PaymentViewModel(repository) }
                                    PaymentScreen(
                                        viewModel = paymentViewModel,
                                        amount = amount.toDouble(),
                                        onNavigateBack = { navController.popBackStack() },
                                        onPaymentSuccess = { navController.popBackStack() }
                                    )
                                }

                                composable("diagnosis") {
                                    val diagnosisViewModel = remember { DiagnosisViewModel(repository) }
                                    
                                    val backStackEntry = navController.currentBackStackEntry
                                    val appParam = backStackEntry?.arguments?.getString("appliance")
                                    val probParam = backStackEntry?.arguments?.getString("problem")

                                    LaunchedEffect(appParam, probParam) {
                                        if (appParam != null && probParam != null) {
                                            diagnosisViewModel.onApplianceChanged(appParam)
                                            diagnosisViewModel.onProblemChanged(probParam)
                                            diagnosisViewModel.runDiagnosis()
                                        }
                                    }

                                    DiagnosisScreen(
                                        viewModel = diagnosisViewModel,
                                        onNavigateBack = { navController.popBackStack() },
                                        onViewTechnicianProfile = { workerId ->
                                            navController.navigate("tech_profile/$workerId")
                                        },
                                        onHireTechnician = { workerId, shopName, appliance, problem ->
                                            navController.navigate("booking/$workerId/$shopName?appliance=$appliance&problem=$problem")
                                        }
                                    )
                                }

                                composable(
                                    route = "tech_profile/{workerId}",
                                    arguments = listOf(navArgument("workerId") { type = NavType.IntType })
                                ) { backStackEntry ->
                                    val workerId = backStackEntry.arguments?.getInt("workerId") ?: 0
                                    val workerViewModel = remember { WorkerPanelViewModel(repository) }
                                    val session = userSessionState ?: return@composable

                                    TechnicianProfileScreen(
                                        viewModel = workerViewModel,
                                        workerId = workerId,
                                        userToken = session.token,
                                        onNavigateBack = { navController.popBackStack() },
                                        onBookNow = { id, name ->
                                            navController.navigate("booking/$id/$name")
                                        }
                                    )
                                }

                                composable(
                                    route = "booking_success/{bookingId}/{name}/{date}",
                                    arguments = listOf(
                                        navArgument("bookingId") { type = NavType.StringType },
                                        navArgument("name") { type = NavType.StringType },
                                        navArgument("date") { type = NavType.StringType }
                                    )
                                ) { backStackEntry ->
                                    val bId = backStackEntry.arguments?.getString("bookingId") ?: ""
                                    val name = backStackEntry.arguments?.getString("name") ?: ""
                                    val date = backStackEntry.arguments?.getString("date") ?: ""
                                    
                                    BookingSuccessScreen(
                                        bookingId = bId,
                                        technicianName = name,
                                        scheduledAt = date,
                                        onNavigateToHistory = {
                                            navController.navigate("history") {
                                                popUpTo("home") { inclusive = false }
                                            }
                                        }
                                    )
                                }

                                composable("chatbot") {
                                    val chatbotViewModel = remember { ChatbotViewModel() }
                                    ChatbotScreen(
                                        viewModel = chatbotViewModel,
                                        onNavigateBack = { navController.popBackStack() }
                                    )
                                }

                                composable("map") {
                                    val mapViewModel = remember { MapViewModel() }
                                    MapScreen(
                                        viewModel = mapViewModel,
                                        onNavigateBack = { navController.popBackStack() }
                                    )
                                }

                                composable("worker_home") {
                                    val workerViewModel = remember { WorkerPanelViewModel(repository) }
                                    val session = userSessionState ?: return@composable

                                    WorkerPanelScreen(
                                        viewModel = workerViewModel,
                                        userToken = session.token,
                                        onNavigateToSignup = {
                                            navController.navigate("worker_signup")
                                        },
                                        onNavigateToProfile = {
                                            navController.navigate("profile")
                                        },
                                        onNavigateToPayment = { amount ->
                                            navController.navigate("payment/$amount")
                                        }
                                    )
                                }

                                composable("shop_home") {
                                    val workerViewModel = remember { WorkerPanelViewModel(repository) }
                                    val session = userSessionState ?: return@composable
                                    ShopDashboardScreen(
                                        viewModel = workerViewModel,
                                        userToken = session.token,
                                        onNavigateToProfile = { navController.navigate("profile") },
                                        onNavigateToPayment = { amount ->
                                            navController.navigate("payment/$amount")
                                        }
                                    )
                                }

                                composable("worker_signup") {
                                    val signupViewModel = remember { WorkerSignupViewModel(repository) }
                                    val session = userSessionState ?: return@composable

                                    WorkerSignupScreen(
                                        viewModel = signupViewModel,
                                        userToken = session.token,
                                        onNavigateBack = { navController.popBackStack() },
                                        onSignupSuccess = {
                                            navController.navigate("worker_home") {
                                                popUpTo("worker_signup") { inclusive = true }
                                            }
                                        }
                                    )
                                }

                                composable("shop_signup") {
                                    val signupViewModel = remember { WorkerSignupViewModel(repository) }
                                    val session = userSessionState ?: return@composable

                                    ShopSignupScreen(
                                        viewModel = signupViewModel,
                                        userToken = session.token,
                                        onNavigateBack = { navController.popBackStack() },
                                        onSignupSuccess = {
                                            navController.navigate("shop_home") {
                                                popUpTo("shop_signup") { inclusive = true }
                                            }
                                        }
                                    )
                                }

                                composable("admin_home") {
                                    val adminViewModel = remember { AdminHomeViewModel(repository) }
                                    val session = userSessionState ?: return@composable

                                    AdminHomeScreen(
                                        viewModel = adminViewModel,
                                        userToken = session.token,
                                        onNavigateToAnalytics = {
                                            navController.navigate("analytics")
                                        },
                                        onNavigateToProfile = {
                                            navController.navigate("profile")
                                        }
                                    )
                                }

                                composable("analytics") {
                                    val analyticsViewModel = remember { AnalyticsViewModel(repository) }
                                    val session = userSessionState ?: return@composable

                                    AnalyticsScreen(
                                        viewModel = analyticsViewModel,
                                        userToken = session.token,
                                        onNavigateBack = { navController.popBackStack() }
                                    )
                                }

                                composable("profile") {
                                    val profileViewModel = remember { ProfileViewModel(repository) }
                                    val session = userSessionState ?: return@composable
                                    ProfileScreen(
                                        viewModel = profileViewModel,
                                        onNavigateBack = { navController.popBackStack() },
                                        onLogoutFinished = {
                                            navController.navigate("login") {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        },
                                        onNavigateToReferral = {
                                            navController.navigate("referral/${session.referralCode}")
                                        },
                                        onNavigateToLegal = {
                                            navController.navigate("legal")
                                        }
                                    )
                                }

                                composable("legal") {
                                    LegalScreen(onNavigateBack = { navController.popBackStack() })
                                }

                                composable(
                                    route = "referral/{code}",
                                    arguments = listOf(navArgument("code") { type = NavType.StringType })
                                ) { backStackEntry ->
                                    val code = backStackEntry.arguments?.getString("code") ?: "ER1234"
                                    ReferAndEarnScreen(
                                        referralCode = code,
                                        onNavigateBack = { navController.popBackStack() }
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.background),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun lifecycleScopeLaunch(block: suspend () -> Unit) {
        lifecycleScope.launch {
            block()
        }
    }
}
