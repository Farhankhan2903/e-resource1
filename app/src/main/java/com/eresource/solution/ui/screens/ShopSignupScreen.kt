@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.eresource.solution.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eresource.solution.data.repository.NetworkResult
import com.eresource.solution.ui.components.*
import com.eresource.solution.ui.theme.PrimaryBlue
import com.eresource.solution.viewmodel.WorkerSignupViewModel

@Composable
fun ShopSignupScreen(
    viewModel: WorkerSignupViewModel,
    userToken: String,
    onNavigateBack: () -> Unit,
    onSignupSuccess: () -> Unit
) {
    // Reusing WorkerSignupViewModel for simplicity in demo, but usually separate
    val currentStep by viewModel.currentStep.collectAsState()
    val signupState by viewModel.signupState.collectAsState()
    val shopName by viewModel.fullName.collectAsState()
    val address by viewModel.address.collectAsState()

    val context = LocalContext.current
    val logoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        // Logic to save logo
    }

    LaunchedEffect(signupState) {
        if (signupState is NetworkResult.Success) {
            onSignupSuccess()
            viewModel.clearState()
        }
    }

    Scaffold(
        topBar = {
            EResourceHeader(title = "Shop Registration", onBackClick = onNavigateBack)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            VerificationProgressIndicator(currentStep)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                when (currentStep) {
                    1 -> {
                        Text("Basic Shop Details", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        FormTextField(value = shopName, onValueChange = { viewModel.onFullNameChanged(it) }, label = "Business Name", placeholder = "e.g., Star Electronics")
                        FormTextField(value = address, onValueChange = { viewModel.onAddressChanged(it) }, label = "Shop Address", placeholder = "Full locality details...")
                    }
                    2 -> {
                        Text("Business Documents", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        UploadCard("Shop Trade License", Icons.Default.Description, false) { }
                        UploadCard("Shop Front Photo", Icons.Default.Storefront, false) { }
                    }
                    3 -> {
                        Text("Verification", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        PremiumCard {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(64.dp), tint = PrimaryBlue)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Final Review", fontWeight = FontWeight.Bold)
                                Text("Your business profile will be live after admin audit.", color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            }
                        }
                    }
                }
            }

            // Navigation
            Row(modifier = Modifier.padding(24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (currentStep > 1) {
                    OutlinedButton(onClick = { viewModel.prevStep() }, modifier = Modifier.weight(1f)) { Text("Back") }
                }
                MainActionButton(
                    text = if (currentStep < 3) "Continue" else "Submit KYC",
                    onClick = { 
                        if (currentStep < 3) viewModel.nextStep() 
                        else viewModel.submitKYC(userToken)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
