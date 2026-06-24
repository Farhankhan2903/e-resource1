@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.eresource.solution.ui.screens.marketplace

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eresource.solution.data.models.ToolListing
import com.eresource.solution.ui.components.*
import com.eresource.solution.ui.theme.*

@Composable
fun MarketplaceHubScreen(
    onNavigateToDetails: (Int) -> Unit,
    onNavigateToMap: () -> Unit
) {
    // Enterprise Mock State
    val categories = listOf("Power Tools", "Construction", "Cleaning", "Heavy Duty")
    var selectedCategory by remember { mutableStateOf("Power Tools") }
    
    Scaffold(
        topBar = {
            EResourceHeader(
                title = "Tool Marketplace",
                actions = {
                    IconButton(onClick = onNavigateToMap) {
                        Icon(Icons.Default.Map, contentDescription = "Map View", tint = PrimaryBlue)
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
            CategoryScroller(
                categories = categories,
                selected = selectedCategory,
                onSelect = { selectedCategory = it }
            )
            
            ToolGrid(onNavigateToDetails)
        }
    }
}

@Composable
fun CategoryScroller(categories: List<String>, selected: String, onSelect: (String) -> Unit) {
    ScrollableTabRow(
        selectedTabIndex = categories.indexOf(selected),
        edgePadding = 16.dp,
        containerColor = Color.Transparent,
        divider = {},
        indicator = {}
    ) {
        categories.forEach { category ->
            val isSelected = selected == category
            Tab(
                selected = isSelected,
                onClick = { onSelect(category) },
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                StatusChip(
                    status = category,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}

@Composable
fun ToolGrid(onNavigate: (Int) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mock Tools
        val tools = listOf(
            ToolListing(1, 1, "Jack Hammer", "Heavy Duty", 150.0, 1200.0, 2000.0, 3, "New"),
            ToolListing(2, 1, "Pressure Washer", "Cleaning", 80.0, 600.0, 1000.0, 5, "Excellent")
        )
        
        items(tools) { tool ->
            PremiumToolCard(tool, onNavigate)
        }
    }
}

@Composable
fun PremiumToolCard(tool: ToolListing, onClick: (Int) -> Unit) {
    PremiumCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onClick(tool.id) }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(PrimaryBlue.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Build, 
                    contentDescription = null, 
                    modifier = Modifier.size(48.dp), 
                    tint = PrimaryBlue
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(tool.name, fontWeight = FontWeight.Black, fontSize = 16.sp)
            Text(tool.category, color = Color.Gray, fontSize = 12.sp)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("₹${tool.hourlyPrice}/hr", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                Spacer(modifier = Modifier.weight(1f))
                StatusChip(tool.condition)
            }
        }
    }
}
