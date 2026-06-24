@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.eresource.solution.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eresource.solution.data.models.DiagnosisResult
import com.eresource.solution.data.models.Worker
import com.eresource.solution.data.repository.NetworkResult
import com.eresource.solution.ui.components.*
import com.eresource.solution.ui.theme.*
import com.eresource.solution.viewmodel.DiagnosisViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosisScreen(
    viewModel: DiagnosisViewModel,
    onNavigateBack: () -> Unit,
    onViewTechnicianProfile: (workerId: Int) -> Unit,
    onHireTechnician: (workerId: Int, shopName: String, appliance: String, problem: String) -> Unit
) {
    val appliance by viewModel.appliance.collectAsState()
    val problem by viewModel.problem.collectAsState()
    val diagnosisState by viewModel.diagnosisState.collectAsState()
    val recommendedWorkers by viewModel.recommendedWorkers.collectAsState()

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(appliance) {
        if (appliance.isNotEmpty() && problem.isEmpty()) {
            focusRequester.requestFocus()
        }
    }

    Scaffold(
        topBar = {
            EResourceHeader(
                title = "AI Fault Diagnosis Hub",
                onBackClick = onNavigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Smart Trouble Analyzer",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                FormTextField(
                    value = appliance,
                    onValueChange = { viewModel.onApplianceChanged(it) },
                    label = "Appliance Name / Category",
                    placeholder = "e.g., Washing Machine",
                    leadingIcon = Icons.Default.Kitchen,
                    testTag = "diag_app_input"
                )

                FormTextField(
                    value = problem,
                    onValueChange = { viewModel.onProblemChanged(it) },
                    label = "Describe Problem Symptoms",
                    placeholder = "e.g., Water is not draining",
                    leadingIcon = Icons.Default.Warning,
                    testTag = "diag_prob_input",
                    modifier = Modifier.focusRequester(focusRequester)
                )

                if (diagnosisState is NetworkResult.Loading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    MainActionButton(
                        text = "Analyze Fault Now",
                        onClick = { viewModel.runDiagnosis() },
                        testTag = "run_diag_btn"
                    )
                }
            }

            AnimatedVisibility(
                visible = diagnosisState is NetworkResult.Success,
                enter = fadeIn() + expandVertically()
            ) {
                if (diagnosisState is NetworkResult.Success) {
                    val result = (diagnosisState as NetworkResult.Success).data
                    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        DiagnosisResultSection(result)
                        BookTechnicianSection(
                            result = result,
                            workersState = recommendedWorkers,
                            onViewProfile = onViewTechnicianProfile,
                            onHire = { wk -> onHireTechnician(wk.worker_id, wk.shop_name, result.appliance, result.cause ?: "General Fault") }
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
            
            if (diagnosisState is NetworkResult.Error) {
                Box(modifier = Modifier.padding(24.dp)) {
                    AnimatedErrorAlert(message = (diagnosisState as NetworkResult.Error).message)
                }
            }
        }
    }
}

@Composable
fun DiagnosisResultSection(result: DiagnosisResult) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(PrimaryBlueColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = PrimaryBlueColor)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("AI Diagnosis Results", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Analysis Confidence: ${result.confidence ?: 85}%", color = SuccessGreenColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
            
            HorizontalDivider()

            ResultItem(label = "Suspected Faulty Component", value = result.cause ?: "Unknown", icon = Icons.Default.Extension)
            ResultItem(label = "Estimated Repair Time", value = result.estRepairTime ?: "1-2 Hours", icon = Icons.Default.Timer)
            ResultItem(label = "Cost Range", value = result.estCostRange ?: "₹800 – ₹2,500", icon = Icons.Default.Payments)
            ResultItem(label = "Urgency Level", value = result.urgency ?: "Medium", icon = Icons.Default.Speed, color = if (result.urgency == "Critical") ErrorRedColor else WarningAmberColor)

            Text("Repair Tips & Safety:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            result.tips?.forEach { tip ->
                Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(start = 8.dp)) {
                    Text("•", fontWeight = FontWeight.Bold, color = PrimaryBlueColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(tip, fontSize = 13.sp, color = Color.Gray, lineHeight = 18.sp)
                }
            }
        }
    }
}

@Composable
fun ResultItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color = PrimaryBlueColor) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = color)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label, fontSize = 11.sp, color = Color.Gray)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun BookTechnicianSection(
    result: DiagnosisResult,
    workersState: NetworkResult<List<Worker>>,
    onViewProfile: (Int) -> Unit,
    onHire: (Worker) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Book a Verified Technician", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Text("Recommended", fontSize = 11.sp, color = PrimaryBlueColor, fontWeight = FontWeight.Bold)
        }
        
        Text(
            "Our AI recommends connecting with an '${result.recommendedWorkerType}' for this specific repair.",
            fontSize = 13.sp,
            color = Color.Gray
        )

        when (workersState) {
            is NetworkResult.Loading -> {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            is NetworkResult.Success -> {
                val workers = workersState.data
                if (workers.isEmpty()) {
                    Text("No matching technicians found in your area.", color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
                } else {
                    workers.forEach { worker ->
                        TechnicianHireCard(
                            worker = worker,
                            onViewProfile = { onViewProfile(worker.worker_id) },
                            onHire = { onHire(worker) }
                        )
                    }
                }
            }
            is NetworkResult.Error -> {
                Text("Could not load technicians: ${workersState.message}", color = ErrorRedColor, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun TechnicianHireCard(worker: Worker, onViewProfile: () -> Unit, onHire: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape).background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(worker.shop_name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        if (worker.verified == 1) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Verified, contentDescription = "Verified", tint = PrimaryBlueColor, modifier = Modifier.size(14.dp))
                        }
                    }
                    Text("${worker.type} • 4.8 ★ (120 reviews)", fontSize = 12.sp, color = Color.Gray)
                }
                Text("0.8 km", fontSize = 11.sp, color = PrimaryBlueColor, fontWeight = FontWeight.Bold)
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onViewProfile,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("View Profile", fontSize = 12.sp)
                }
                Button(
                    onClick = onHire,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueColor)
                ) {
                    Text("Hire Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
