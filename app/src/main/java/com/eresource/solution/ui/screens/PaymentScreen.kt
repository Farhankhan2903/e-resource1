@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.eresource.solution.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eresource.solution.data.repository.NetworkResult
import com.eresource.solution.ui.components.*
import com.eresource.solution.ui.theme.*
import com.eresource.solution.viewmodel.PaymentViewModel

@Composable
fun PaymentScreen(
    viewModel: PaymentViewModel,
    amount: Double,
    onNavigateBack: () -> Unit,
    onPaymentSuccess: () -> Unit
) {
    val paymentState by viewModel.paymentState.collectAsState()
    var selectedMethod by remember { mutableStateOf("UPI") }

    LaunchedEffect(paymentState) {
        if (paymentState is NetworkResult.Success) {
            // Wait a bit to show success message then navigate back
            kotlinx.coroutines.delay(1500)
            onPaymentSuccess()
            viewModel.clearState()
        }
    }

    Scaffold(
        topBar = {
            EResourceHeader(title = "Secure Payment", onBackClick = onNavigateBack)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Amount Summary
            PremiumCard {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Total Amount Due", color = Color.Gray, fontSize = 14.sp)
                    Text("₹$amount", fontSize = 36.sp, fontWeight = FontWeight.Black, color = PrimaryBlue)
                    Spacer(modifier = Modifier.height(8.dp))
                    StatusChip(status = "UNPAID")
                }
            }

            Text("Select Payment Method", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PaymentMethodItem(
                    icon = Icons.Default.QrCode,
                    label = "UPI (PhonePe, Google Pay)",
                    isSelected = selectedMethod == "UPI",
                    onClick = { selectedMethod = "UPI" }
                )
                PaymentMethodItem(
                    icon = Icons.Default.CreditCard,
                    label = "Credit / Debit Card",
                    isSelected = selectedMethod == "Card",
                    onClick = { selectedMethod = "Card" }
                )
                PaymentMethodItem(
                    icon = Icons.Default.Payments,
                    label = "Cash Payment (At Center)",
                    isSelected = selectedMethod == "Cash",
                    onClick = { selectedMethod = "Cash" }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (paymentState is NetworkResult.Loading) {
                CircularProgressIndicator(color = PrimaryBlue)
                Text("Processing your payment...", color = Color.Gray, fontSize = 14.sp)
            } else if (paymentState is NetworkResult.Success) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(64.dp))
                Text((paymentState as NetworkResult.Success).data, textAlign = TextAlign.Center, color = SuccessGreen, fontWeight = FontWeight.Bold)
            } else {
                MainActionButton(
                    text = if (selectedMethod == "Cash") "Confirm Cash Payment" else "Pay Now",
                    onClick = { viewModel.processPayment(selectedMethod, amount) },
                    backgroundColor = if (selectedMethod == "Cash") WarningAmber else PrimaryBlue
                )
            }

            if (paymentState is NetworkResult.Error) {
                Text((paymentState as NetworkResult.Error).message, color = ErrorRed, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun PaymentMethodItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    PremiumCard(
        onClick = onClick,
        modifier = Modifier.border(
            width = if (isSelected) 2.dp else 0.dp,
            color = if (isSelected) PrimaryBlue else Color.Transparent,
            shape = RoundedCornerShape(24.dp)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = PrimaryBlue.copy(alpha = 0.05f)
                ) {
                    Icon(icon, contentDescription = null, modifier = Modifier.padding(10.dp), tint = PrimaryBlue)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
            }
            RadioButton(selected = isSelected, onClick = onClick, colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue))
        }
    }
}
