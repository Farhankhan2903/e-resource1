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
import com.eresource.solution.data.models.Booking
import com.eresource.solution.data.repository.NetworkResult
import com.eresource.solution.ui.components.*
import com.eresource.solution.ui.theme.PrimaryBlueColor
import com.eresource.solution.ui.theme.SuccessGreenColor
import com.eresource.solution.ui.theme.WarningAmberColor
import com.eresource.solution.viewmodel.ServiceHistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceHistoryScreen(
    viewModel: ServiceHistoryViewModel,
    userToken: String,
    onNavigateBack: () -> Unit,
    onNavigateToChat: (bookingId: Int, otherPartyName: String) -> Unit,
    onNavigateToReview: (bookingId: Int, workerId: Int, technicianName: String) -> Unit
) {
    val bookingsState by viewModel.bookingsState.collectAsState()
    val filteredBookings by viewModel.filteredBookings.collectAsState()
    val filterStatus by viewModel.filterStatus.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadBookings(userToken)
    }

    Scaffold(
        topBar = {
            EResourceHeader(
                title = "My Service History",
                onBackClick = onNavigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Filter Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "Pending", "Completed").forEach { status ->
                        val isSelected = filterStatus == status
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) PrimaryBlueColor else Color.LightGray.copy(alpha = 0.3f))
                                .clickable { viewModel.onFilterChanged(status) }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .testTag("history_filter_$status")
                        ) {
                            Text(
                                text = status,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            when (bookingsState) {
                is NetworkResult.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.testTag("bookings_list_loader"))
                    }
                }
                is NetworkResult.Error -> {
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        AnimatedErrorAlert(message = (bookingsState as NetworkResult.Error).message)
                    }
                }
                is NetworkResult.Success -> {
                    if (filteredBookings.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No matching service bookings found.", color = Color.Gray, fontSize = 14.sp)
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredBookings) { bk ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth().testTag("history_booking_card_${bk.booking_id}")
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
                                                fontSize = 12.sp,
                                                color = Color.Gray
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
                                            text = "Fault symptoms: ${bk.problem}",
                                            fontSize = 13.sp,
                                            color = Color.Gray
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(imageVector = Icons.Default.DateRange, contentDescription = "Time", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = bk.scheduled_at, fontSize = 12.sp, color = Color.Gray)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(imageVector = Icons.Default.Person, contentDescription = "Worker", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = "Technician: ${bk.shop_name ?: "Verified Specialist"}", fontSize = 12.sp, color = Color.Gray)
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        // Booking actions row (Chat, Review)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // Always allow chat to coordinate the repair
                                            Button(
                                                onClick = {
                                                    onNavigateToChat(bk.booking_id, bk.shop_name ?: "Technician")
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueColor),
                                                modifier = Modifier.weight(1f).testTag("chat_booking_btn_${bk.booking_id}")
                                            ) {
                                                Icon(imageVector = Icons.Default.Send, contentDescription = "Chat", modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Chat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }

                                            // Only completed repairs can be reviewed
                                            if (bk.status.lowercase() == "completed") {
                                                Button(
                                                    onClick = {
                                                        onNavigateToReview(bk.booking_id, bk.worker_id, bk.shop_name ?: "Technician")
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreenColor),
                                                    modifier = Modifier.weight(1f).testTag("rate_booking_btn_${bk.booking_id}")
                                                ) {
                                                    Icon(imageVector = Icons.Default.Star, contentDescription = "Rate", modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Rate Hub", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
}
