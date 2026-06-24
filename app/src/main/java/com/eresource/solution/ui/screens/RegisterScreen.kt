@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.eresource.solution.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eresource.solution.data.repository.NetworkResult
import com.eresource.solution.ui.components.*
import com.eresource.solution.ui.theme.PrimaryBlueColor
import com.eresource.solution.ui.theme.PrimaryDarkColor
import com.eresource.solution.viewmodel.RegisterViewModel

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onRegisterSuccess: (com.eresource.solution.data.models.AuthResponse) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToLegal: () -> Unit
) {
    val role by viewModel.role.collectAsState()
    val fullName by viewModel.fullName.collectAsState()
    val email by viewModel.email.collectAsState()
    val phone by viewModel.phone.collectAsState()
    val password by viewModel.password.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()
    val isTermsAccepted by viewModel.isTermsAccepted.collectAsState()
    val registerState by viewModel.registerState.collectAsState()

    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(registerState) {
        val state = registerState
        when (state) {
            is NetworkResult.Success -> {
                onRegisterSuccess(state.data)
                viewModel.clearState()
            }
            is NetworkResult.Error -> {
                errorMessage = state.message
            }
            else -> {
                errorMessage = null
            }
        }
    }

    Scaffold(
        topBar = {
            EResourceHeader(
                title = "Create Account",
                onBackClick = onNavigateToLogin
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Choose Account Type",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    onClick = { viewModel.onRoleChanged("user") },
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = if (role == "user") PrimaryBlueColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                    ),
                    border = if (role == "user") ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(PrimaryBlueColor)) else null
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = if (role == "user") PrimaryBlueColor else Color.Gray)
                        Text("Customer", fontWeight = FontWeight.Bold, color = if (role == "user") PrimaryBlueColor else Color.Gray)
                    }
                }
                Card(
                    onClick = { viewModel.onRoleChanged("worker") },
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = if (role == "worker") PrimaryBlueColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                    ),
                    border = if (role == "worker") ButtonDefaults.outlinedButtonBorder(enabled = true).copy(brush = androidx.compose.ui.graphics.SolidColor(PrimaryBlueColor)) else null
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Engineering, contentDescription = null, tint = if (role == "worker") PrimaryBlueColor else Color.Gray)
                        Text("Technician", fontWeight = FontWeight.Bold, color = if (role == "worker") PrimaryBlueColor else Color.Gray)
                    }
                }
                Card(
                    onClick = { viewModel.onRoleChanged("shop") },
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = if (role == "shop") PrimaryBlueColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                    ),
                    border = if (role == "shop") ButtonDefaults.outlinedButtonBorder(enabled = true).copy(brush = androidx.compose.ui.graphics.SolidColor(PrimaryBlueColor)) else null
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = if (role == "shop") PrimaryBlueColor else Color.Gray)
                        Text("Shop Owner", fontWeight = FontWeight.Bold, color = if (role == "shop") PrimaryBlueColor else Color.Gray, textAlign = TextAlign.Center)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            errorMessage?.let { AnimatedErrorAlert(message = it) }

            FormTextField(
                value = fullName,
                onValueChange = { viewModel.onFullNameChanged(it) },
                label = "Full Name",
                placeholder = "Enter your full name",
                leadingIcon = Icons.Default.Badge,
                testTag = "full_name_input"
            )

            FormTextField(
                value = email,
                onValueChange = { viewModel.onEmailChanged(it) },
                label = "Email Address",
                placeholder = "e.g., alex@example.com",
                leadingIcon = Icons.Default.Email,
                testTag = "email_input"
            )

            FormTextField(
                value = phone,
                onValueChange = { viewModel.onPhoneChanged(it) },
                label = "Phone Number",
                placeholder = "Enter 10-digit number",
                leadingIcon = Icons.Default.Phone,
                testTag = "phone_input"
            )

            FormTextField(
                value = password,
                onValueChange = { viewModel.onPasswordChanged(it) },
                label = "Password",
                placeholder = "Min 6 characters",
                leadingIcon = Icons.Default.Lock,
                testTag = "password_input"
            )

            FormTextField(
                value = confirmPassword,
                onValueChange = { viewModel.onConfirmPasswordChanged(it) },
                label = "Confirm Password",
                placeholder = "Repeat password",
                leadingIcon = Icons.Default.LockClock,
                testTag = "confirm_password_input"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isTermsAccepted,
                    onCheckedChange = { viewModel.onTermsAcceptedChanged(it) },
                    colors = CheckboxDefaults.colors(checkedColor = PrimaryBlueColor)
                )
                
                val annotatedString = androidx.compose.ui.text.buildAnnotatedString {
                    append("I agree to the ")
                    pushStringAnnotation(tag = "legal", annotation = "legal")
                    withStyle(style = androidx.compose.ui.text.SpanStyle(color = PrimaryBlueColor, fontWeight = FontWeight.Bold)) {
                        append("Terms and Conditions")
                    }
                    pop()
                    append(" and ")
                    pushStringAnnotation(tag = "legal", annotation = "legal")
                    withStyle(style = androidx.compose.ui.text.SpanStyle(color = PrimaryBlueColor, fontWeight = FontWeight.Bold)) {
                        append("Privacy Policy")
                    }
                    pop()
                }

                androidx.compose.foundation.text.ClickableText(
                    text = annotatedString,
                    style = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = Color.Gray),
                    onClick = { offset ->
                        annotatedString.getStringAnnotations(tag = "legal", start = offset, end = offset)
                            .firstOrNull()?.let {
                                onNavigateToLegal()
                            }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (registerState is NetworkResult.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                MainActionButton(
                    text = if (role == "worker") "Next: Verification" else "Complete Registration",
                    onClick = { viewModel.register() },
                    enabled = isTermsAccepted,
                    testTag = "submit_register_btn"
                )
            }
        }
    }
}
