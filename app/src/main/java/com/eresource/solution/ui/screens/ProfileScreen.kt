@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.eresource.solution.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eresource.solution.data.models.EarningsData
import com.eresource.solution.data.models.TechnicianStats
import com.eresource.solution.data.models.UserSession
import com.eresource.solution.ui.components.*
import com.eresource.solution.ui.theme.*
import com.eresource.solution.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit,
    onLogoutFinished: () -> Unit,
    onNavigateToReferral: () -> Unit,
    onNavigateToLegal: () -> Unit
) {
    val userSession by viewModel.userSession.collectAsState()
    val darkModeEnabled by viewModel.darkModeEnabled.collectAsState()
    val isAvailable by viewModel.isAvailable.collectAsState()
    val techStats by viewModel.technicianStats.collectAsState()
    val earnings by viewModel.earnings.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userSession) {
        if (userSession == null) {
            onLogoutFinished()
        }
    }

    Scaffold(
        topBar = {
            EResourceHeader(title = "Profile & Settings", onBackClick = onNavigateBack)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Decorative Element
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(PrimaryBlue, PrimaryBlue.copy(alpha = 0f))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                userSession?.let { session ->
                    ProfileHeader(session)
                    
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        
                        if (session.role.lowercase() == "worker") {
                            TechnicianAvailabilityToggle(isAvailable, onToggle = { viewModel.toggleAvailability(it) })
                            KycVerificationCard(session.kycStatus)
                            TechnicianStatsDashboard(techStats)
                            EarningsDashboard(earnings)
                        }

                        PersonalInformationCard(session)
                        
                        if (session.role.lowercase() == "shop") {
                            ShopInformationCard(session)
                        }

                        AccountSettingsSection(
                            snackbarHostState = snackbarHostState,
                            scope = scope,
                            darkModeEnabled = darkModeEnabled,
                            onDarkModeToggle = { viewModel.setDarkMode(it) },
                            onLogoutClick = { showLogoutDialog = true },
                            onReferralClick = onNavigateToReferral,
                            onLegalClick = onNavigateToLegal
                        )
                        
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                } ?: run {
                    // Empty or Error state
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                viewModel.logout()
            }
        )
    }
}

@Composable
fun ProfileHeader(session: UserSession) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Surface(
                modifier = Modifier.size(110.dp),
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 12.dp
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.padding(24.dp).fillMaxSize(),
                    tint = PrimaryBlue
                )
            }
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .offset(x = (-4).dp, y = (-4).dp)
                    .clickable { },
                shape = CircleShape,
                color = PrimaryBlue,
                shadowElevation = 4.dp
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt, 
                    contentDescription = null, 
                    modifier = Modifier.padding(8.dp),
                    tint = Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = session.username,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = session.email,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.size(4.dp).background(Color.LightGray, CircleShape))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = session.role.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
        }
    }
}

@Composable
fun TechnicianAvailabilityToggle(isAvailable: Boolean, onToggle: (Boolean) -> Unit) {
    PremiumCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = if (isAvailable) SuccessGreen.copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f)
                ) {
                    Icon(
                        imageVector = if (isAvailable) Icons.Default.WorkOutline else Icons.Default.WorkOff,
                        contentDescription = null,
                        modifier = Modifier.padding(10.dp),
                        tint = if (isAvailable) SuccessGreen else Color.Gray
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Available for Work", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (isAvailable) "You are visible to customers" else "You are currently hidden",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            Switch(
                checked = isAvailable, 
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = SuccessGreen,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.LightGray
                )
            )
        }
    }
}

@Composable
fun KycVerificationCard(status: String) {
    val bgColor: Color
    val icon: ImageVector
    val title: String
    val message: String

    when (status.uppercase()) {
        "APPROVED" -> {
            bgColor = SuccessGreen
            icon = Icons.Default.Verified
            title = "VERIFIED PRO"
            message = "Your KYC verification is complete. High trust badge enabled."
        }
        "PENDING" -> {
            bgColor = WarningAmber
            icon = Icons.Default.Schedule
            title = "UNDER REVIEW"
            message = "Documents are under review. Access will be granted shortly."
        }
        else -> {
            bgColor = ErrorRed
            icon = Icons.Default.GppBad
            title = "VERIFICATION FAILED"
            message = "Your submission was rejected. Review feedback and resubmit."
        }
    }

    PremiumCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = bgColor.copy(alpha = 0.1f)
                ) {
                    Icon(icon, contentDescription = null, tint = bgColor, modifier = Modifier.padding(10.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = bgColor)
                    Text(message, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
            if (status.uppercase() == "REJECTED") {
                Button(
                    onClick = { /* Resubmit */ },
                    colors = ButtonDefaults.buttonColors(containerColor = bgColor),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Update Documents", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TechnicianStatsDashboard(stats: TechnicianStats) {
    PremiumCard {
        Text("Performance Dashboard", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            StatItem("Jobs", "${stats.completedJobs}", SuccessGreen)
            StatItem("Rating", "4.8 ★", WarningAmber)
            StatItem("Success", stats.successRate, PrimaryBlue)
            StatItem("Reviews", "${stats.reviewsCount}", SecondaryBlue)
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Black, fontSize = 18.sp, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

@Composable
fun EarningsDashboard(data: EarningsData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = PrimaryBlue.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryBlue)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color.White, modifier = Modifier.padding(10.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text("Business Earnings", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("TODAY", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall, letterSpacing = 1.sp)
                    Text(data.today, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("THIS MONTH", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall, letterSpacing = 1.sp)
                    Text(data.month, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Lifetime Revenue", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium)
                Text(data.lifetime, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PersonalInformationCard(session: UserSession) {
    PremiumCard {
        Text("Personal Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            InfoRow(Icons.Default.Badge, "Full Name", session.username)
            InfoRow(Icons.Default.Phone, "Mobile Number", session.mobile)
            InfoRow(Icons.Default.Email, "Email Address", session.email)
            InfoRow(Icons.Default.LocationCity, "Service City", session.city)
            InfoRow(Icons.Default.PinDrop, "PIN Code", session.pinCode)
        }
    }
}

@Composable
fun ShopInformationCard(session: UserSession) {
    PremiumCard {
        Text("Shop Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            InfoRow(Icons.Default.Storefront, "Shop ID", "#${session.shopId ?: 0}")
            InfoRow(Icons.Default.VerifiedUser, "Shop Status", "Active Pro")
            InfoRow(Icons.Default.SettingsSuggest, "Marketplace", "Full Catalog Access")
        }
    }
}

@Composable
fun AccountSettingsSection(
    snackbarHostState: SnackbarHostState,
    scope: kotlinx.coroutines.CoroutineScope,
    darkModeEnabled: Boolean,
    onDarkModeToggle: (Boolean) -> Unit,
    onLogoutClick: () -> Unit,
    onReferralClick: () -> Unit,
    onLegalClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Account Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        
        PremiumCard {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SettingsItem(Icons.Default.Edit, "Edit Profile") {
                    scope.launch { snackbarHostState.showSnackbar("Edit Profile coming soon!") }
                }
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 4.dp))
                SettingsItem(Icons.Default.History, "Booking History") {
                    scope.launch { snackbarHostState.showSnackbar("Loading service records...") }
                }
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 4.dp))
                SettingsItem(Icons.Default.Notifications, "Notifications") {
                    scope.launch { snackbarHostState.showSnackbar("No new alerts.") }
                }
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 4.dp))
                SettingsItem(Icons.Default.CardGiftcard, "Refer & Earn") {
                    onReferralClick()
                }
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 4.dp))
                SettingsItem(Icons.Default.Gavel, "Legal & Policies") {
                    onLegalClick()
                }
            }
        }
        
        PremiumCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = PrimaryBlue.copy(alpha = 0.05f)
                    ) {
                        Icon(Icons.Default.DarkMode, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.padding(10.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Dark Mode", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                }
                Switch(checked = darkModeEnabled, onCheckedChange = onDarkModeToggle)
            }
        }

        PremiumCard(onClick = onLogoutClick) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = ErrorRed.copy(alpha = 0.1f)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = ErrorRed, modifier = Modifier.padding(10.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text("Logout Account", color = ErrorRed, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SettingsItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Black, fontSize = 16.sp, color = PrimaryBlueColor)
        Text(label, fontSize = 10.sp, color = Color.Gray)
    }
}

@Composable
fun LogoutConfirmationDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = { 
            Text("Logout", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) 
        },
        text = { 
            Text("Are you sure you want to sign out? You will need to login again to access your dashboard.", style = MaterialTheme.typography.bodyMedium) 
        },
        confirmButton = {
            Button(
                onClick = onConfirm, 
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Logout", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = PrimaryBlue, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}
