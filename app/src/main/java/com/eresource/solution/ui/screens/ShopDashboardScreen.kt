@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.eresource.solution.ui.screens

import androidx.compose.foundation.background
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
import com.eresource.solution.data.models.Resource
import com.eresource.solution.data.repository.NetworkResult
import com.eresource.solution.ui.components.*
import com.eresource.solution.ui.theme.*
import com.eresource.solution.viewmodel.WorkerPanelViewModel

@Composable
fun ShopDashboardScreen(
    viewModel: WorkerPanelViewModel,
    userToken: String,
    onNavigateToProfile: () -> Unit,
    onNavigateToPayment: (Double) -> Unit
) {
    val toolsState by viewModel.toolsState.collectAsState()
    val rentedToolsState by viewModel.rentedToolsState.collectAsState()
    val rentActionState by viewModel.rentActionState.collectAsState()
    val returnActionState by viewModel.returnActionState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showRentDialog by remember { mutableStateOf<Resource?>(null) }
    var rentHoursText by remember { mutableStateOf("2") }
    var billingInvoice by remember { mutableStateOf<com.eresource.solution.data.models.ReturnResponse?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadRentableTools()
        viewModel.loadRentedTools(userToken)
    }

    LaunchedEffect(rentActionState) {
        val currentAction = rentActionState
        when (currentAction) {
            is NetworkResult.Success -> {
                snackbarHostState.showSnackbar("Rental confirmed by shop!")
                showRentDialog = null
                rentHoursText = "2"
                viewModel.clearActionStates()
                viewModel.loadRentableTools()
                viewModel.loadRentedTools(userToken)
            }
            is NetworkResult.Error -> {
                snackbarHostState.showSnackbar(currentAction.message)
            }
            else -> {}
        }
    }

    LaunchedEffect(returnActionState) {
        if (returnActionState is NetworkResult.Success) {
            snackbarHostState.showSnackbar("Return processed successfully.")
            billingInvoice = (returnActionState as NetworkResult.Success).data
            viewModel.clearActionStates()
            viewModel.loadRentableTools()
            viewModel.loadRentedTools(userToken)
        }
    }

    Scaffold(
        topBar = {
            EResourceHeader(
                title = "Shop Hub",
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", tint = PrimaryBlue)
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
                            Text("The tool has been returned. Please settle the following rental charges:")
                            HorizontalDivider()
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Rental Period:", color = Color.Gray)
                                Text("${inv.billing_hours} Hours", fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Hourly Rate:", color = Color.Gray)
                                Text(String.format("₹%.2f / hr", inv.rate_per_hour), fontWeight = FontWeight.Bold)
                            }
                            HorizontalDivider()
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Amount Due:", color = PrimaryDarkColor, fontWeight = FontWeight.Bold)
                                Text(String.format("₹%.2f", inv.total_amount), color = ErrorRedColor, fontWeight = FontWeight.Black, fontSize = 18.sp)
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp)
                )
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Text("My Active Tool Rentals", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                when (rentedToolsState) {
                    is NetworkResult.Loading -> item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
                    is NetworkResult.Success -> {
                        val list = (rentedToolsState as NetworkResult.Success).data
                        if (list.isEmpty()) {
                            item { Text("No active rentals.", color = Color.Gray, fontSize = 14.sp) }
                        } else {
                            items(list) { rented ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(rented.resource_name ?: "Tool", fontWeight = FontWeight.Bold)
                                            Text("₹${rented.price_per_hour}/hr • ${rented.alloc_hour} hrs", fontSize = 12.sp, color = Color.Gray)
                                        }
                                        Button(onClick = { viewModel.returnTool(userToken, rented.allocation_id) }, colors = ButtonDefaults.buttonColors(containerColor = ErrorRedColor)) {
                                            Text("Return")
                                        }
                                    }
                                }
                            }
                        }
                    }
                    is NetworkResult.Error -> item { AnimatedErrorAlert(message = (rentedToolsState as NetworkResult.Error).message) }
                }

                item {
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Available Tools for Rent", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }

                when (toolsState) {
                    is NetworkResult.Loading -> item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
                    is NetworkResult.Success -> {
                        val list = (toolsState as NetworkResult.Success).data
                        items(list) { tool ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(tool.name, fontWeight = FontWeight.Bold)
                                        Text("₹${tool.price_per_hour}/hr", color = PrimaryBlueColor, fontWeight = FontWeight.Bold)
                                    }
                                    Text(tool.description ?: "", fontSize = 12.sp, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { showRentDialog = tool },
                                        modifier = Modifier.align(Alignment.End),
                                        enabled = tool.avail > 0
                                    ) {
                                        Text("Rent Now")
                                    }
                                }
                            }
                        }
                    }
                    is NetworkResult.Error -> item { AnimatedErrorAlert(message = (toolsState as NetworkResult.Error).message) }
                }
            }
        }
    }

    showRentDialog?.let { tool ->
        AlertDialog(
            onDismissRequest = { showRentDialog = null },
            confirmButton = {
                Button(onClick = {
                    val h = rentHoursText.toIntOrNull() ?: 2
                    viewModel.rentTool(userToken, tool.resource_id, h)
                }) { Text("Confirm Rent") }
            },
            dismissButton = { TextButton(onClick = { showRentDialog = null }) { Text("Cancel") } },
            title = { Text("Rent ${tool.name}") },
            text = {
                Column {
                    Text("Duration (Hours):")
                    OutlinedTextField(value = rentHoursText, onValueChange = { rentHoursText = it }, singleLine = true)
                }
            }
        )
    }
}
