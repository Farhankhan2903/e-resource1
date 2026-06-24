@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.eresource.solution.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eresource.solution.ui.components.EResourceHeader
import com.eresource.solution.ui.components.PremiumCard

@Composable
fun LegalScreen(
    onNavigateBack: () -> Unit
) {
    var activeTab by remember { mutableStateOf(0) } // 0 = T&C, 1 = Privacy Policy

    Scaffold(
        topBar = {
            EResourceHeader(title = "Legal & Policies", onBackClick = onNavigateBack)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Terms of Use", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Privacy Policy", fontWeight = FontWeight.Bold) }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                PremiumCard {
                    if (activeTab == 0) {
                        TermsContent()
                    } else {
                        PrivacyContent()
                    }
                }
            }
        }
    }
}

@Composable
fun TermsContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        LegalSection("1. Nature of Platform", "E-Resource is a marketplace connecting Customers, Technicians, and Shop Owners. We act as intermediaries only.")
        LegalSection("2. User Responsibility", "Users are responsible for their own interactions. Ensure you provide accurate KYC data.")
        LegalSection("3. Indian Laws", "All operations are subject to the Information Technology Act, 2000 and Bangalore jurisdiction.")
        LegalSection("4. Payments", "All transactions are in ₹ INR. Security deposits are mandatory for tool rentals.")
    }
}

@Composable
fun PrivacyContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        LegalSection("1. Data Collection", "We collect your Name, Email, Phone, and Identity Documents for verification.")
        LegalSection("2. Purpose", "Data is used for KYC, proximity matching, and generating legal invoices.")
        LegalSection("3. Security", "We use enterprise-grade encryption and bcrypt hashing for sensitive data.")
        LegalSection("4. Sharing", "Your contact is only shared with a technician after a booking is accepted.")
    }
}

@Composable
fun LegalSection(title: String, content: String) {
    Column {
        Text(title, fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(content, fontSize = 14.sp, color = Color.Gray)
    }
}
