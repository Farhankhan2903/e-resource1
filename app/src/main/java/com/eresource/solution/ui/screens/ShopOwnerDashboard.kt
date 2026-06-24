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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eresource.solution.data.models.Tool
import com.eresource.solution.data.repository.NetworkResult
import com.eresource.solution.ui.components.*
import com.eresource.solution.ui.theme.*
import com.eresource.solution.viewmodel.MarketplaceViewModel

@Composable
fun ShopOwnerDashboard(
    viewModel: MarketplaceViewModel,
    onNavigateToProfile: () -> Unit
) {
    val myToolsState by viewModel.myTools.collectAsState()
    var showAddToolDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            EResourceHeader(
                title = "Shop Owner Hub",
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.White)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddToolDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Tool") },
                containerColor = PrimaryBlueColor,
                contentColor = Color.White
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Text("Your Listed Tools", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            when (myToolsState) {
                is NetworkResult.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                is NetworkResult.Success -> {
                    val tools = (myToolsState as NetworkResult.Success).data
                    if (tools.isEmpty()) {
                        Text("No tools listed. Tap '+' to add one.", color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(tools) { tool ->
                                ToolManagementCard(tool)
                            }
                        }
                    }
                }
                is NetworkResult.Error -> AnimatedErrorAlert((myToolsState as NetworkResult.Error).message)
            }
        }
    }
}

@Composable
fun ToolManagementCard(tool: Tool) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(tool.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                StatusChip(status = tool.status)
            }
            Text(tool.category, fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Price: ₹${tool.price_hr}/hr", color = PrimaryBlueColor, fontWeight = FontWeight.Bold)
                Text("Qty: ${tool.qty}", fontWeight = FontWeight.Medium)
            }
        }
    }
}
