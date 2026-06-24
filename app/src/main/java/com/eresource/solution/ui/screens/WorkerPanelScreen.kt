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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eresource.solution.data.models.AllocationLog
import com.eresource.solution.data.models.Booking
import com.eresource.solution.data.models.Resource
import com.eresource.solution.data.repository.NetworkResult
import com.eresource.solution.ui.components.*
import com.eresource.solution.ui.theme.*
import com.eresource.solution.viewmodel.WorkerPanelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerPanelScreen(
    viewModel: WorkerPanelViewModel,
    userToken: String,
    onNavigateToSignup: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToPayment: (Double) -> Unit
) {
    val statusState by viewModel.statusState.collectAsState()
    val toolsState by viewModel.toolsState.collectAsState()
    val rentedToolsState by viewModel.rentedToolsState.collectAsState()
    val rentActionState by viewModel.rentActionState.collectAsState()
    val returnActionState by viewModel.returnActionState.collectAsState()
    val bookingsState by viewModel.bookingsState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var activeTab by remember { mutableStateOf(0) } // 0 = Bookings, 1 = Shared Tools Pool

    // Rent Trigger variables
    var showRentDialog by remember { mutableStateOf<Resource?>(null) }
    var rentHoursText by remember { mutableStateOf("2") }

    // Bill Invoice overlay
    var billingInvoice by remember { mutableStateOf<com.eresource.solution.data.models.ReturnResponse?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadStatus(userToken)
    }

    LaunchedEffect(statusState) {
        if (statusState is NetworkResult.Success) {
            val status = (statusState as NetworkResult.Success).data
            if (status.verified == 1) {
                viewModel.loadBookings(userToken)
                viewModel.loadRentableTools()
                viewModel.loadRentedTools(userToken)
                viewModel.loadSelfDetails(userToken)
            }
        }
    }

    LaunchedEffect(rentActionState) {
        val currentRent = rentActionState
        when (currentRent) {
            is NetworkResult.Success -> {
                snackbarHostState.showSnackbar("Tool rented successfully! Pickup from center.")
                showRentDialog = null
                rentHoursText = "2"
                viewModel.clearActionStates()
            }
            is NetworkResult.Error -> {
                snackbarHostState.showSnackbar(currentRent.message)
            }
            else -> {}
        }
    }

    LaunchedEffect(returnActionState) {
        if (returnActionState is NetworkResult.Success) {
            snackbarHostState.showSnackbar("Tool return verified.")
            billingInvoice = (returnActionState as NetworkResult.Success).data
            viewModel.clearActionStates()
        }
    }

    Scaffold(
        topBar = {
            EResourceHeader(
                title = "Worker Panel",
                actions = {
                    IconButton(onClick = onNavigateToProfile, modifier = Modifier.testTag("worker_profile_btn")) {
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
            when (statusState) {
                is NetworkResult.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.testTag("status_loader"))
                    }
                }
                is NetworkResult.Error -> {
                    val err = (statusState as NetworkResult.Error).message
                    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        AnimatedErrorAlert(message = err)
                    }
                }
                is NetworkResult.Success -> {
                    val status = (statusState as NetworkResult.Success).data

                    if (!status.has_applied) {
                        // Display credentials apply prompt page
                        Column(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = "KYC locked", modifier = Modifier.size(64.dp), tint = ErrorRedColor)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No Active Worker Profile",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Apply for a Verified Worker license to receive repair bookings and rent specialized tools by the hour.",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            MainActionButton(
                                text = "Complete KYC Application",
                                onClick = onNavigateToSignup,
                                testTag = "complete_kyc_direct_btn"
                            )
                        }
                    } else if (status.verified == null) {
                        // Pending admin review
                        Column(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = "Pending Approval", modifier = Modifier.size(64.dp), tint = WarningAmberColor)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Application Pending Review",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Our administrators are verifying your uploaded passport and shop address details. This usually takes under 24 hours.",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else if (status.verified == 0) {
                        // Rejected
                        Column(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Application Rejected", modifier = Modifier.size(64.dp), tint = ErrorRedColor)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "KYC Verifications Rejected",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Your verification application has been denied due to incomplete or blurred passport photo credentials.",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            MainActionButton(
                                text = "Apply Again",
                                onClick = onNavigateToSignup,
                                testTag = "reapply_kyc_button"
                            )
                        }
                    } else {
                        // Fully verified state! Tab layouts
                        TabRow(
                            selectedTabIndex = activeTab,
                            contentColor = PrimaryBlueColor,
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            Tab(
                                selected = activeTab == 0,
                                onClick = { activeTab = 0 },
                                text = { Text("Service Bookings", fontWeight = FontWeight.Bold) },
                                modifier = Modifier.testTag("bookings_tab")
                            )
                            Tab(
                                selected = activeTab == 1,
                                onClick = { activeTab = 1 },
                                text = { Text("Specialized Tool Pool", fontWeight = FontWeight.Bold) },
                                modifier = Modifier.testTag("tools_tab")
                            )
                        }

                        // INVOICE SUMMARY DIALOG OVERLAY
                        billingInvoice?.let { inv ->
                            AlertDialog(
                                onDismissRequest = { billingInvoice = null },
                                confirmButton = {
                                    Button(
                                        onClick = { 
                                            val amount = inv.total_amount
                                            billingInvoice = null
                                            onNavigateToPayment(amount)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueColor)
                                    ) {
                                         Text("Proceed to Pay", fontWeight = FontWeight.Bold)
                                    }
                                },
                                title = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Success", tint = SuccessGreenColor)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Return Successful 🎉", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    }
                                },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text("The tool has been returned to the pool. Please settle the following rental charges:")
                                        HorizontalDivider()
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Rental Period:", color = Color.Gray)
                                            Text("${inv.billing_hours} Hours", fontWeight = FontWeight.Bold)
                                        }
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Hourly Stock Rate:", color = Color.Gray)
                                            Text(String.format("₹%.2f / hr", inv.rate_per_hour), fontWeight = FontWeight.Bold)
                                        }
                                        HorizontalDivider()
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Total Amount Due:", color = PrimaryDarkColor, fontWeight = FontWeight.Bold)
                                            Text(String.format("₹%.2f", inv.total_amount), color = ErrorRedColor, fontWeight = FontWeight.Black, fontSize = 18.sp)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("You can pay via UPI or Cash at the center.", fontSize = 12.sp, color = PrimaryBlueColor, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.testTag("invoice_dialog")
                            )
                        }

                        if (activeTab == 0) {
                            // RENDER bookings
                            when (bookingsState) {
                                is NetworkResult.Loading -> {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator()
                                    }
                                }
                                is NetworkResult.Error -> {
                                    AnimatedErrorAlert(message = (bookingsState as NetworkResult.Error).message)
                                }
                                is NetworkResult.Success -> {
                                    val list = (bookingsState as NetworkResult.Success).data
                                    if (list.isEmpty()) {
                                        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                                            Text("No incoming repair appointments booked yet.", color = Color.Gray)
                                        }
                                    } else {
                                        LazyColumn(
                                            contentPadding = PaddingValues(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp),
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            items(list) { bk ->
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
                                                            Text(
                                                                text = "Booking #${bk.booking_id}",
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color.Gray,
                                                                fontSize = 12.sp
                                                            )
                                                            StatusChip(status = bk.status)
                                                        }
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        Text(
                                                            text = bk.appliance,
                                                            fontSize = 16.sp,
                                                            fontWeight = FontWeight.Black,
                                                            color = MaterialTheme.colorScheme.onBackground
                                                        )
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(
                                                            text = "Problem: ${bk.problem}",
                                                            fontSize = 13.sp,
                                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                                                        )
                                                        Spacer(modifier = Modifier.height(6.dp))
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(imageVector = Icons.Default.DateRange, contentDescription = "Time", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text(text = "Scheduled: ${bk.scheduled_at}", fontSize = 12.sp, color = Color.Gray)
                                                        }
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(imageVector = Icons.Default.Person, contentDescription = "User", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text(text = "Client: ${bk.customer_name ?: "N/A"}", fontSize = 12.sp, color = Color.Gray)
                                                        }

                                                        if (!bk.notes.isNullOrEmpty()) {
                                                            Spacer(modifier = Modifier.height(8.dp))
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(8.dp))
                                                                    .background(Color.LightGray.copy(alpha = 0.2f))
                                                                    .padding(8.dp)
                                                                    .fillMaxWidth()
                                                            ) {
                                                                Text("Note: ${bk.notes}", fontSize = 11.sp, color = Color.DarkGray)
                                                            }
                                                        }

                                                        Spacer(modifier = Modifier.height(12.dp))

                                                        // Action controls depending on status
                                                        when (bk.status.lowercase()) {
                                                            "pending" -> {
                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                                ) {
                                                                    Button(
                                                                        onClick = { viewModel.updateBooking(userToken, bk.booking_id, "accepted") },
                                                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreenColor),
                                                                        modifier = Modifier.weight(1f).testTag("accept_booking_${bk.booking_id}")
                                                                    ) {
                                                                        Text("Accept", fontWeight = FontWeight.Bold)
                                                                    }
                                                                    Button(
                                                                        onClick = { viewModel.updateBooking(userToken, bk.booking_id, "rejected") },
                                                                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRedColor),
                                                                        modifier = Modifier.weight(1f).testTag("reject_booking_${bk.booking_id}")
                                                                    ) {
                                                                        Text("Reject", fontWeight = FontWeight.Bold)
                                                                    }
                                                                }
                                                            }
                                                            "accepted" -> {
                                                                MainActionButton(
                                                                   text = "Mark Completed",
                                                                   onClick = { viewModel.updateBooking(userToken, bk.booking_id, "completed") },
                                                                   testTag = "complete_booking_${bk.booking_id}",
                                                                   backgroundColor = PrimaryBlueColor
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
                        } else {
                            // RENDER Specialized Tool Pool
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                // Currently rented tools section
                                item {
                                    Text(
                                        "My Active Tool Rentals",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                when (rentedToolsState) {
                                    is NetworkResult.Loading -> {
                                        item { CircularProgressIndicator() }
                                    }
                                    is NetworkResult.Success -> {
                                        val list = (rentedToolsState as NetworkResult.Success).data
                                        if (list.isEmpty()) {
                                            item {
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        "You are not renting any shared equipment tools.",
                                                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                                        textAlign = TextAlign.Center,
                                                        fontSize = 12.sp,
                                                        color = Color.Gray
                                                    )
                                                }
                                            }
                                        } else {
                                            items(list) { rented ->
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(16.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                text = rented.resource_name ?: "Unknown Tool",
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 14.sp
                                                            )
                                                            Spacer(modifier = Modifier.height(2.dp))
                                                            Text(
                                                                text = "Period: ${rented.alloc_hour} hrs | Rate: ₹${rented.price_per_hour}/hr",
                                                                fontSize = 12.sp,
                                                                color = Color.Gray
                                                            )
                                                        }
                                                        Button(
                                                            onClick = { viewModel.returnTool(userToken, rented.allocation_id) },
                                                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRedColor),
                                                            modifier = Modifier.testTag("return_tool_btn_${rented.allocation_id}")
                                                        ) {
                                                            Text("Return", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    is NetworkResult.Error -> {
                                        item { AnimatedErrorAlert(message = (rentedToolsState as NetworkResult.Error).message) }
                                    }
                                }

                                // Available tool catalog
                                item {
                                    Divider()
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Shared Equipment Pool catalog",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                when (toolsState) {
                                    is NetworkResult.Loading -> {
                                        item { CircularProgressIndicator() }
                                    }
                                    is NetworkResult.Success -> {
                                        val list = (toolsState as NetworkResult.Success).data
                                        items(list) { tool ->
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
                                                        Text(
                                                            text = tool.name,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 15.sp
                                                        )
                                                        Text(
                                                            text = String.format("₹%.2f / hr", tool.price_per_hour),
                                                            color = PrimaryBlueColor,
                                                            fontWeight = FontWeight.Black,
                                                            fontSize = 14.sp
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = tool.description ?: "No description provided",
                                                        fontSize = 12.sp,
                                                        color = Color.Gray
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = "Stock Available: ${tool.avail} / ${tool.total}",
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = if (tool.avail > 0) SuccessGreenColor else ErrorRedColor
                                                        )
                                                        Button(
                                                            onClick = { showRentDialog = tool },
                                                            enabled = tool.avail > 0,
                                                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueColor),
                                                            modifier = Modifier.testTag("rent_tool_btn_${tool.resource_id}")
                                                        ) {
                                                            Text("Rent Tool", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    is NetworkResult.Error -> {
                                        item { AnimatedErrorAlert(message = (toolsState as NetworkResult.Error).message) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Rent hours dialog helper
    showRentDialog?.let { tool ->
        AlertDialog(
            onDismissRequest = { showRentDialog = null },
            confirmButton = {
                Button(
                    onClick = {
                        val hours = rentHoursText.toIntOrNull() ?: 2
                        viewModel.rentTool(userToken, tool.resource_id, hours)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueColor),
                    modifier = Modifier.testTag("confirm_rent_btn")
                ) {
                    Text("Rent Now", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRentDialog = null }) {
                    Text("Cancel")
                }
            },
            title = { Text("Rent Specialized Tool", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tool Name: ${tool.name}")
                                                    Text("Rental price: ₹${tool.price_per_hour}/hr")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Specify rent duration (hours):", fontSize = 12.sp, color = Color.Gray)
                    OutlinedTextField(
                        value = rentHoursText,
                        onValueChange = { rentHoursText = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("rent_hours_input")
                    )
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}
