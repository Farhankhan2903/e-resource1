@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.eresource.solution.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eresource.solution.data.models.Worker
import com.eresource.solution.data.repository.NetworkResult
import com.eresource.solution.ui.components.*
import com.eresource.solution.ui.theme.*
import com.eresource.solution.viewmodel.HomeViewModel

data class ApplianceItem(
    val name: String,
    val icon: ImageVector,
    val description: String
)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onApplianceSelected: (applianceName: String, initialProblem: String) -> Unit,
    onBookWorkerSelected: (workerId: Int, workerShopName: String) -> Unit,
    onNavigateToDiagnosis: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToChatbot: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val workersState by viewModel.workersState.collectAsState()
    val filteredWorkers by viewModel.filteredWorkers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val appliances = listOf(
        ApplianceItem("Washing Machine", Icons.Default.Refresh, ""),
        ApplianceItem("Refrigerator", Icons.Default.AcUnit, ""),
        ApplianceItem("Computer / PC", Icons.Default.Computer, ""),
        ApplianceItem("Air Conditioner", Icons.Default.Kitchen, ""),
        ApplianceItem("Microwave Oven", Icons.Default.PlayArrow, ""),
        ApplianceItem("Ceiling Fan", Icons.Default.FlashOn, "")
    )

    Scaffold(
        topBar = {
            EResourceHeader(
                title = "E-Resource",
                actions = {
                    IconButton(onClick = onNavigateToChatbot, modifier = Modifier.testTag("support_chatbot_btn")) {
                        Icon(imageVector = Icons.Default.Face, contentDescription = "FAQ chatbot", tint = PrimaryBlue)
                    }
                    IconButton(onClick = onNavigateToMap, modifier = Modifier.testTag("worker_map_btn")) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = "OpenMap view", tint = PrimaryBlue)
                    }
                    IconButton(onClick = onNavigateToHistory, modifier = Modifier.testTag("booking_history_btn")) {
                        Icon(imageVector = Icons.Default.DateRange, contentDescription = "History", tint = PrimaryBlue)
                    }
                    IconButton(onClick = onNavigateToProfile, modifier = Modifier.testTag("profile_section_btn")) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "Profile", tint = PrimaryBlue)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quick Diagnosis Banner Action Promo
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PrimaryDarkColor),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToDiagnosis() }
                        .testTag("ai_diagnosis_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Unsure what's broken?",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Run immediate AI Diagnosis recommendations!",
                                color = SecondaryTealColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Diagnose",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            // 1. Appliance Grid
            item {
                Text(
                    text = "Select Appliance to Diagnose & Book",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Appliance list represented vertically/horizontally or in nested boxes (we avoid nested LazyColumn grid scrolling conflicts!)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Chunk in pairs of 2
                    val chunks = appliances.chunked(2)
                    for (row in chunks) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (item in row) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            onApplianceSelected(item.name, "My ${item.name} has a problem: ")
                                        }
                                        .testTag("appliance_${item.name.replace(" ", "_").lowercase()}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(PrimaryBlueColor.copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = item.name,
                                                tint = PrimaryBlueColor,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = item.name,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                            Text(
                                                text = item.description,
                                                fontSize = 11.sp,
                                                color = Color.Gray,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Worker Listings Title & Filters
            item {
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Verified Service Technicians",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Search Box
                FormTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    label = "Search by workshop name",
                    placeholder = "e.g., ElectroTech",
                    leadingIcon = Icons.Default.Search,
                    testTag = "search_worker_bar"
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Filter Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "Electrician", "Computer").forEach { type ->
                        val isSelected = selectedType == type
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) PrimaryBlueColor else Color.LightGray.copy(alpha = 0.3f))
                                .clickable { viewModel.onTypeSelected(type) }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .testTag("filter_chip_$type")
                        ) {
                            Text(
                                text = type,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Worker List Renderer
            when (workersState) {
                is NetworkResult.Loading -> {
                    item {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                                .wrapContentWidth(Alignment.CenterHorizontally)
                        )
                    }
                }
                is NetworkResult.Success -> {
                    if (filteredWorkers.isEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "No verified technicians match the chosen criteria.",
                                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        // Show "AI Recommended" on top worker (which is first due to rating sort from backend API!)
                        val sortedWorkers = filteredWorkers.sortedWith(compareByDescending<Worker> { it.avg_rating }.thenByDescending { it.total_reviews })
                        
                        items(sortedWorkers) { worker ->
                            val isTopRated = sortedWorkers.indexOf(worker) == 0 && worker.avg_rating >= 4.0f
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onBookWorkerSelected(worker.worker_id, worker.shop_name) }
                                    .testTag("worker_card_${worker.worker_id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = worker.shop_name,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                            if (isTopRated) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(SecondaryTealColor)
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "AI RECOMMENDED",
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = worker.shop_addr,
                                            fontSize = 13.sp,
                                            color = Color.Gray
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = "Avg Rating",
                                                    tint = WarningAmberColor,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(
                                                    text = String.format("%.1f", worker.avg_rating),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onBackground
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "(${worker.total_reviews} reviews)",
                                                    fontSize = 11.sp,
                                                    color = Color.Gray
                                                )
                                            }

                                            // Specialty badge
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(PrimaryBlueColor.copy(alpha = 0.12f))
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = worker.type.uppercase(),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PrimaryBlueColor
                                                )
                                            }
                                        }
                                    }

                                    Button(
                                        onClick = { onBookWorkerSelected(worker.worker_id, worker.shop_name) },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.testTag("book_direct_btn_${worker.worker_id}"),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueColor)
                                    ) {
                                        Text("Book", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
                is NetworkResult.Error -> {
                    item {
                        AnimatedErrorAlert(message = (workersState as NetworkResult.Error).message)
                    }
                }
            }
        }
    }
}
