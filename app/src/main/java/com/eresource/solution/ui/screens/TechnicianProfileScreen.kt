@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.eresource.solution.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.eresource.solution.data.models.Worker
import com.eresource.solution.data.repository.NetworkResult
import com.eresource.solution.ui.components.EResourceHeader
import com.eresource.solution.ui.components.MainActionButton
import com.eresource.solution.ui.components.StarRatingBar
import com.eresource.solution.ui.theme.PrimaryBlueColor
import com.eresource.solution.ui.theme.SuccessGreenColor
import com.eresource.solution.viewmodel.WorkerPanelViewModel

@Composable
fun TechnicianProfileScreen(
    viewModel: WorkerPanelViewModel,
    workerId: Int,
    userToken: String,
    onNavigateBack: () -> Unit,
    onBookNow: (Int, String) -> Unit
) {
    val workerSelfState by viewModel.workerSelfState.collectAsState()

    LaunchedEffect(workerId) {
        viewModel.loadSelfDetails(userToken) // Note: In a real app we'd load by workerId, but our backend currently has 'fetchWorkerSelf'
    }

    Scaffold(
        topBar = {
            EResourceHeader(
                title = "Technician Profile",
                onBackClick = onNavigateBack
            )
        }
    ) { innerPadding ->
        when (workerSelfState) {
            is NetworkResult.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is NetworkResult.Success -> {
                val worker = (workerSelfState as NetworkResult.Success).data
                TechnicianProfileContent(worker, innerPadding, onBookNow)
            }
            is NetworkResult.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${(workerSelfState as NetworkResult.Error).message}")
                }
            }
        }
    }
}

@Composable
fun TechnicianProfileContent(worker: Worker, padding: PaddingValues, onBookNow: (Int, String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Hero Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryBlueColor)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(100.dp).clip(CircleShape).background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(60.dp), tint = PrimaryBlueColor)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(worker.shop_name, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    if (worker.verified == 1) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.Verified, contentDescription = "Verified", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
                Text(worker.type, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
            }
        }

        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            // Stats Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                ProfileStat(label = "Rating", value = "${worker.avg_rating} ★")
                ProfileStat(label = "Completed", value = "250+")
                ProfileStat(label = "Success", value = "98%")
            }

            HorizontalDivider()

            // Info Sections
            Text("Professional Details", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            InfoRow(icon = Icons.Default.WorkHistory, label = "Experience", value = "5+ Years")
            InfoRow(icon = Icons.Default.LocationOn, label = "Address", value = worker.shop_addr)
            InfoRow(icon = Icons.Default.Phone, label = "Contact", value = worker.contact_no)

            HorizontalDivider()

            Text("Skills & Specializations", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SkillChip("AC Repair")
                SkillChip("Fridge")
                SkillChip("Washing Machine")
            }

            Spacer(modifier = Modifier.height(24.dp))

            MainActionButton(
                text = "Book Appointment Now",
                onClick = { onBookNow(worker.worker_id, worker.shop_name) }
            )
            
            OutlinedButton(
                onClick = { /* Chat */ },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Chat, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Message Technician")
            }
        }
    }
}

@Composable
fun ProfileStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Black, fontSize = 18.sp, color = PrimaryBlueColor)
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.Gray)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 11.sp, color = Color.Gray)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun SkillChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(PrimaryBlueColor.copy(alpha = 0.1f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(label, fontSize = 12.sp, color = PrimaryBlueColor, fontWeight = FontWeight.Bold)
    }
}
