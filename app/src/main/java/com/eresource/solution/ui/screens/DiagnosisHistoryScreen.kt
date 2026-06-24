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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eresource.solution.data.models.DiagnosisHistory
import com.eresource.solution.ui.components.EResourceHeader
import com.eresource.solution.ui.components.PremiumCard
import com.eresource.solution.ui.theme.PrimaryBlue
import com.eresource.solution.ui.theme.SuccessGreen

@Composable
fun DiagnosisHistoryScreen(
    onNavigateBack: () -> Unit
) {
    // Mock History
    val history = listOf(
        DiagnosisHistory(1, "Washing Machine", "Drainage failure", "12 May 2024", "98%", "₹1,200"),
        DiagnosisHistory(2, "Air Conditioner", "Cooling issue", "05 May 2024", "92%", "₹3,500"),
        DiagnosisHistory(3, "Microwave Oven", "Sparking", "28 April 2024", "95%", "₹850")
    )

    Scaffold(
        topBar = {
            EResourceHeader(title = "Health Reports", onBackClick = onNavigateBack)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(history) { report ->
                    DiagnosisHistoryCard(report)
                }
            }
        }
    }
}

@Composable
fun DiagnosisHistoryCard(report: DiagnosisHistory) {
    PremiumCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(report.appliance, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                Text(report.date, fontSize = 12.sp, color = Color.Gray)
            }
            
            Column {
                Text("Detected Problem", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Text(report.problem, style = MaterialTheme.typography.bodyMedium)
            }

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(16.dp), tint = SuccessGreen)
                    Text(" Confidence: ${report.confidence}", fontSize = 12.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                }
                Text("Est. Cost: ${report.estimatedCost}", fontWeight = FontWeight.Black, color = PrimaryBlue)
            }
        }
    }
}
