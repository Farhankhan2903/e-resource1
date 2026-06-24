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
import com.eresource.solution.ui.theme.PrimaryBlueColor
import com.eresource.solution.viewmodel.WorkerSignupViewModel

@Composable
fun WorkerSignupScreen(
    viewModel: WorkerSignupViewModel,
    userToken: String,
    onNavigateBack: () -> Unit,
    onSignupSuccess: () -> Unit
) {
    val currentStep by viewModel.currentStep.collectAsState()
    val signupState by viewModel.signupState.collectAsState()

    LaunchedEffect(signupState) {
        if (signupState is NetworkResult.Success) {
            onSignupSuccess()
            viewModel.clearState()
        }
    }

    Scaffold(
        topBar = {
            EResourceHeader(
                title = "Worker Verification",
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
            // Progress Indicator
            VerificationProgressIndicator(currentStep)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Crossfade(targetState = currentStep, label = "stepTransition") { step ->
                    when (step) {
                        1 -> PersonalInfoStep(viewModel)
                        2 -> ProfessionalInfoStep(viewModel)
                        3 -> IdVerificationStep(viewModel)
                        4 -> FinalReviewStep(viewModel, userToken)
                    }
                }
            }

            // Bottom Navigation
            Surface(
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = { viewModel.prevStep() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Previous")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                    
                    if (currentStep < 4) {
                        Button(
                            onClick = { viewModel.nextStep() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Next Step")
                        }
                    } else {
                        // Submit logic handled inside FinalReviewStep
                    }
                }
            }
        }
    }
}

@Composable
fun VerificationProgressIndicator(step: Int) {
    val labels = listOf("Account", "KYC", "Admin", "Start")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        labels.forEachIndexed { index, label ->
            val active = step > index
            val current = step == index + 1
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(if (active) PrimaryBlueColor else Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    if (active && !current) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Text("${index + 1}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text(label, fontSize = 10.sp, color = if (current) PrimaryBlueColor else Color.Gray)
            }
            
            if (index < labels.size - 1) {
                HorizontalDivider(modifier = Modifier.width(40.dp).padding(top = 12.dp), color = if (step > index + 1) PrimaryBlueColor else Color.LightGray)
            }
        }
    }
}

@Composable
fun PersonalInfoStep(viewModel: WorkerSignupViewModel) {
    val name by viewModel.fullName.collectAsState()
    val dob by viewModel.dob.collectAsState()
    val address by viewModel.address.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Personal Information", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        FormTextField(value = name, onValueChange = { viewModel.onFullNameChanged(it) }, label = "Full Name", placeholder = "As per ID card", leadingIcon = Icons.Default.Badge)
        FormTextField(value = dob, onValueChange = { viewModel.onDobChanged(it) }, label = "Date of Birth", placeholder = "DD/MM/YYYY", leadingIcon = Icons.Default.CalendarToday)
        FormTextField(value = address, onValueChange = { viewModel.onAddressChanged(it) }, label = "Permanent Address", placeholder = "House no, Street...", leadingIcon = Icons.Default.Home)
    }
}

@Composable
fun ProfessionalInfoStep(viewModel: WorkerSignupViewModel) {
    val trade by viewModel.trade.collectAsState()
    val exp by viewModel.experience.collectAsState()
    val skills by viewModel.selectedSkills.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Professional Details", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        
        Text("Primary Trade", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        viewModel.tradeOptions.forEach { option ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = trade == option, onClick = { viewModel.onTradeChanged(option) })
                Text(option)
            }
        }

        Text("Select Skills", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        FlowRow(mainAxisSpacing = 8.dp, crossAxisSpacing = 8.dp) {
            viewModel.skillOptions.forEach { skill ->
                FilterChip(
                    selected = skills.contains(skill),
                    onClick = { viewModel.onSkillToggled(skill) },
                    label = { Text(skill) }
                )
            }
        }
    }
}

@Composable
fun IdVerificationStep(viewModel: WorkerSignupViewModel) {
    val context = LocalContext.current
    val idFront by viewModel.idFrontImage.collectAsState()
    val idBack by viewModel.idBackImage.collectAsState()
    val selfie by viewModel.selfieImage.collectAsState()

    val frontLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.onIdFrontImageChanged(context.contentResolver.openInputStream(it)?.readBytes()) }
    }
    val backLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.onIdBackImageChanged(context.contentResolver.openInputStream(it)?.readBytes()) }
    }
    val selfieLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.onSelfieImageChanged(context.contentResolver.openInputStream(it)?.readBytes()) }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Identity Verification", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        UploadCard("Government ID Front", Icons.Default.Upload, idFront != null) { frontLauncher.launch("image/*") }
        UploadCard("Government ID Back", Icons.Default.Upload, idBack != null) { backLauncher.launch("image/*") }
        UploadCard("Selfie Verification", Icons.Default.CameraAlt, selfie != null) { selfieLauncher.launch("image/*") }
    }
}

@Composable
fun UploadCard(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isUploaded: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(100.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = if (isUploaded) PrimaryBlueColor.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(if (isUploaded) Icons.Default.CheckCircle else icon, contentDescription = null, tint = if (isUploaded) PrimaryBlueColor else Color.Gray)
            Text(if (isUploaded) "Document Uploaded" else label, fontSize = 12.sp, color = if (isUploaded) PrimaryBlueColor else Color.Gray)
        }
    }
}

@Composable
fun FinalReviewStep(viewModel: WorkerSignupViewModel, token: String) {
    val signupState by viewModel.signupState.collectAsState()
    val bank by viewModel.bankName.collectAsState()
    val account by viewModel.accountNumber.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Banking & Review", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        FormTextField(value = bank, onValueChange = { viewModel.onBankNameChanged(it) }, label = "Bank Name", placeholder = "e.g., HDFC Bank", leadingIcon = Icons.Default.AccountBalance)
        FormTextField(value = account, onValueChange = { viewModel.onAccountChanged(it) }, label = "Account Number", placeholder = "Enter 12-16 digits", leadingIcon = Icons.Default.Payments)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (signupState is NetworkResult.Loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            MainActionButton(
                text = "Submit for Review",
                onClick = { viewModel.submitKYC(token) }
            )
        }
        
        if (signupState is NetworkResult.Error) {
            AnimatedErrorAlert(message = (signupState as NetworkResult.Error).message)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    mainAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    crossAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(mainAxisSpacing),
        verticalArrangement = Arrangement.spacedBy(crossAxisSpacing)
    ) {
        content()
    }
}
