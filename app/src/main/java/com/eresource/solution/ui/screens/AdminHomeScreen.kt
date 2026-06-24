@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.eresource.solution.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eresource.solution.data.models.AllocationLog
import com.eresource.solution.data.models.Review
import com.eresource.solution.data.models.Worker
import com.eresource.solution.data.repository.NetworkResult
import com.eresource.solution.ui.components.*
import com.eresource.solution.ui.theme.*
import com.eresource.solution.viewmodel.AdminHomeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    viewModel: AdminHomeViewModel,
    userToken: String,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val workersState by viewModel.workersState.collectAsState()
    val allocationLogsState by viewModel.allocationLogsState.collectAsState()
    val reviewsState by viewModel.reviewsState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var activeTab by remember { mutableStateOf(0) } // 0 = APPLICATIONS, 1 = ALLOCATIONS LOG, 2 = REVIEWS

    LaunchedEffect(Unit) {
        viewModel.loadAllWorkers(userToken)
        viewModel.loadAllocationLogs(userToken)
        viewModel.loadAllReviews()
    }

    LaunchedEffect(actionState) {
        when (actionState) {
            is NetworkResult.Success -> {
                snackbarHostState.showSnackbar("Platform action completed successfully!")
                viewModel.clearActionState()
            }
            is NetworkResult.Error -> {
                snackbarHostState.showSnackbar((actionState as NetworkResult.Error).message)
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            EResourceHeader(
                title = "Platform Hub",
                actions = {
                    IconButton(onClick = onNavigateToAnalytics, modifier = Modifier.testTag("admin_analytics_btn")) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Analytics dashboard", tint = PrimaryBlue) // Analytics
                    }
                    IconButton(onClick = onNavigateToProfile, modifier = Modifier.testTag("admin_profile_btn")) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "Profile Settings", tint = PrimaryBlue)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Main tabs row
            TabRow(
                selectedTabIndex = activeTab,
                contentColor = PrimaryBlueColor,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("KYC Profiles", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("kyc_profiles_tab")
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Tool Rentals", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tool_rentals_tab")
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = { Text("Feedbacks", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("feedbacks_tab")
                )
            }

            // Action Status Bar
            if (actionState is NetworkResult.Loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().testTag("admin_action_loader"))
            }

            when (activeTab) {
                0 -> {
                    // Applications Core List
                    when (workersState) {
                        is NetworkResult.Loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        is NetworkResult.Error -> {
                            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                AnimatedErrorAlert(message = (workersState as NetworkResult.Error).message)
                            }
                        }
                        is NetworkResult.Success -> {
                            val list = (workersState as NetworkResult.Success).data
                            if (list.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("No worker profiles registered yet.", color = Color.Gray)
                                }
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(list) { wk ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column {
                                                        Text(
                                                            text = wk.shop_name,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 16.sp
                                                        )
                                                        Text(text = "License ID: #${wk.worker_id}", fontSize = 11.sp, color = Color.Gray)
                                                    }
                                                    StatusChip(
                                                        status = if (wk.verified == 1) "verified" else if (wk.verified == 0) "rejected" else "pending"
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(text = "Type: ${wk.type.uppercase()}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = PrimaryBlue)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(text = "Specialty Shop: ${wk.shop_addr}", fontSize = 13.sp, color = Color.Gray)
                                                Text(text = "Contact Phone: ${wk.contact_no}", fontSize = 13.sp, color = Color.Gray)

                                                Spacer(modifier = Modifier.height(12.dp))

                                                // KYC Detailed info for Admin review
                                                if (wk.verified == null) {
                                                    Card(
                                                        colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.1f)),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                            Text("KYC Verification Documents", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryBlueColor)
                                                            AdminDetailRow(label = "Aadhaar No", value = wk.aadhaar_no ?: "N/A")
                                                            AdminDetailRow(label = "PAN Number", value = wk.pan_no ?: "N/A")
                                                            AdminDetailRow(label = "Birth Date", value = wk.dob ?: "N/A")
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(12.dp))
                                                }

                                                // Actions control
                                                if (wk.verified == null) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Button(
                                                            onClick = { viewModel.verifyWorker(userToken, wk.worker_id) },
                                                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreenColor),
                                                            modifier = Modifier.weight(1f).testTag("verify_action_btn_${wk.worker_id}")
                                                        ) {
                                                            Text("Verify KYC", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                        Button(
                                                            onClick = { viewModel.rejectWorker(userToken, wk.worker_id) },
                                                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRedColor),
                                                            modifier = Modifier.weight(1f).testTag("reject_action_btn_${wk.worker_id}")
                                                        ) {
                                                            Text("Reject KYC", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                } else {
                                                    IconButton(onClick = { 
                                                        viewModel.removeWorker(userToken, wk.worker_id)
                                                        scope.launch { snackbarHostState.showSnackbar("Deleting worker profile...") }
                                                    }) {
                                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Allocations Log Block
                    when (allocationLogsState) {
                        is NetworkResult.Loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        is NetworkResult.Error -> {
                            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                AnimatedErrorAlert(message = (allocationLogsState as NetworkResult.Error).message)
                            }
                        }
                        is NetworkResult.Success -> {
                            val logs = (allocationLogsState as NetworkResult.Success).data
                            if (logs.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("No shared tool rental logs recorded.", color = Color.Gray)
                                }
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(logs) { log ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(14.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = log.resource_name ?: "Unknown Tool",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = "Rented by Technician ID #${log.worker_id}",
                                                        fontSize = 12.sp,
                                                        color = Color.Gray
                                                    )
                                                    Text(
                                                        text = "Hours Allocated: ${log.alloc_hour} hrs | Rate: ₹${log.price_per_hour}/hr",
                                                        fontSize = 12.sp,
                                                        color = Color.Gray
                                                    )
                                                }
                                                Column(horizontalAlignment = Alignment.End) {
                                                    if (log.returned == 1) {
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(SuccessGreenColor.copy(alpha = 0.15f))
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text("Returned", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SuccessGreenColor)
                                                        }
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "₹${log.alloc_hour * (log.price_per_hour ?: 0.0)} Charged",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = ErrorRed
                                                    )
                                                    } else {
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(WarningAmberColor.copy(alpha = 0.15f))
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text("In-Use", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = WarningAmberColor)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Reviews Platform Auditing
                    when (reviewsState) {
                        is NetworkResult.Loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        is NetworkResult.Error -> {
                            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                AnimatedErrorAlert(message = (reviewsState as NetworkResult.Error).message)
                            }
                        }
                        is NetworkResult.Success -> {
                            val list = (reviewsState as NetworkResult.Success).data
                            if (list.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("No review entries posted on the platform.", color = Color.Gray)
                                }
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(list) { rev ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "Booking #${rev.booking_id} Review",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        color = Color.Gray
                                                    )
                                                    StarRatingBar(rating = rev.rating, starSize = 16)
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "\"${rev.comment}\"",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onBackground
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Reviewed Technician: ${rev.shop_name ?: "ID #${rev.worker_id}"}",
                                                    fontSize = 11.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminDetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray, fontSize = 12.sp)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}
