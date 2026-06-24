@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.eresource.solution.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eresource.solution.data.repository.NetworkResult
import com.eresource.solution.ui.components.AnimatedErrorAlert
import com.eresource.solution.ui.components.EResourceHeader
import com.eresource.solution.ui.components.FormTextField
import com.eresource.solution.ui.components.MainActionButton
import com.eresource.solution.viewmodel.LoginViewModel

@Composable
fun ForgotPasswordScreen(
    viewModel: LoginViewModel,
    onNavigateBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    val forgotPasswordState by viewModel.forgotPasswordState.collectAsState()

    Scaffold(
        topBar = {
            EResourceHeader(
                title = "Reset Password",
                onBackClick = onNavigateBack
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
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(80.dp)
            )

            Text(
                text = "Forgot your password?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Enter your registered email address and we'll send you a link to reset your password.",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            if (forgotPasswordState is NetworkResult.Success) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = (forgotPasswordState as NetworkResult.Success).data.message,
                        modifier = Modifier.padding(16.dp),
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                if (forgotPasswordState is NetworkResult.Error) {
                    AnimatedErrorAlert(message = (forgotPasswordState as NetworkResult.Error).message)
                }

                FormTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email Address",
                    placeholder = "e.g., alex@example.com",
                    leadingIcon = Icons.Default.Email
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (forgotPasswordState is NetworkResult.Loading) {
                    CircularProgressIndicator()
                } else {
                    MainActionButton(
                        text = "Send Reset Link",
                        onClick = { viewModel.resetPassword(email) }
                    )
                }
            }

            TextButton(onClick = onNavigateBack) {
                Text("Back to Login", fontWeight = FontWeight.Bold)
            }
        }
    }
}
