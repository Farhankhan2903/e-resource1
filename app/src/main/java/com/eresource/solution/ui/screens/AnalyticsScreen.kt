@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.eresource.solution.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
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
import com.eresource.solution.data.repository.NetworkResult
import com.eresource.solution.ui.components.*
import com.eresource.solution.ui.theme.PrimaryBlue
import com.eresource.solution.ui.theme.AccentCyan
import com.eresource.solution.ui.theme.PrimaryBlueColor
import com.eresource.solution.ui.theme.SecondaryTealColor
import com.eresource.solution.ui.theme.SuccessGreenColor
import com.eresource.solution.viewmodel.AnalyticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    userToken: String,
    onNavigateBack: () -> Unit
) {
    val analyticsState by viewModel.analyticsState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAnalytics(userToken)
    }

    Scaffold(
        topBar = {
            EResourceHeader(
                title = "Analytics & Platform Trends",
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { viewModel.loadAnalytics(userToken) }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh stats", tint = Color.White)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (analyticsState) {
                is NetworkResult.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.testTag("analytics_loader"))
                    }
                }
                is NetworkResult.Error -> {
                    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        AnimatedErrorAlert(message = (analyticsState as NetworkResult.Error).message)
                    }
                }
                is NetworkResult.Success -> {
                    val data = (analyticsState as NetworkResult.Success).data
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Platform Metrics Summary",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        // KPI Cards Grid of 2 columns
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Total Bookings", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("${data.summary.totalBookings}", fontSize = 24.sp, fontWeight = FontWeight.Black, color = PrimaryBlueColor)
                                    Text("Active schedules", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Verified Workers", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("${data.summary.totalWorkers}", fontSize = 24.sp, fontWeight = FontWeight.Black, color = SuccessGreenColor)
                                    Text("Registered workshops", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Registered Users", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("${data.summary.totalUsers}", fontSize = 24.sp, fontWeight = FontWeight.Black, color = SecondaryTealColor)
                                    Text("Consumer accounts", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Total Revenue Earned", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(String.format("$%.2f", data.summary.totalRevenue), fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary)
                                    Text("Fulfillment billing", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }

                        // Appliance Popularity Chart Row Custom Displays
                        Divider()
                        Text(
                            text = "Repairs Distribution By Specialty",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        data.appliancePopularity.forEach { entry ->
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = entry.appliance.uppercase(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = "${entry.count} Orders",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryBlueColor
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))

                                // Visual bar representing portion
                                val maxCount = data.appliancePopularity.maxOfOrNull { it.count }?.toFloat() ?: 10f
                                val fraction = if (maxCount > 0) entry.count.toFloat() / maxCount else 0.1f

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(14.dp)
                                        .clip(RoundedCornerShape(7.dp))
                                        .background(Color.LightGray.copy(alpha = 0.2f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(fraction)
                                            .clip(RoundedCornerShape(7.dp))
                                            .background(PrimaryBlueColor)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
                else -> { /* Placeholder UI if Loading */ }
            }
        }
    }
}
