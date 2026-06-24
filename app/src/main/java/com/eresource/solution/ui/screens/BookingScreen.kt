@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.eresource.solution.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eresource.solution.data.repository.NetworkResult
import com.eresource.solution.ui.components.*
import com.eresource.solution.ui.theme.PrimaryBlueColor
import com.eresource.solution.ui.theme.SuccessGreenColor
import com.eresource.solution.viewmodel.BookingViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    viewModel: BookingViewModel,
    userToken: String,
    workerId: Int,
    workerShopName: String,
    initialAppliance: String? = null,
    initialProblem: String? = null,
    onNavigateBack: () -> Unit,
    onBookingSuccess: (String, String, String) -> Unit
) {
    val applianceType by viewModel.applianceType.collectAsState()
    val fullName by viewModel.fullName.collectAsState()
    val mobile by viewModel.mobileNumber.collectAsState()
    val houseNo by viewModel.houseNo.collectAsState()
    val street by viewModel.streetArea.collectAsState()
    val city by viewModel.city.collectAsState()
    val pinCode by viewModel.pinCode.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val timeSlot by viewModel.selectedTimeSlot.collectAsState()
    val payMethod by viewModel.paymentMethod.collectAsState()
    val bookingState by viewModel.bookingState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (initialAppliance != null && initialProblem != null) {
            viewModel.initFromDiagnosis(initialAppliance, initialProblem)
        }
    }

    LaunchedEffect(bookingState) {
        when (bookingState) {
            is NetworkResult.Success -> {
                val dateStr = selectedDate?.let { SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(it)) } ?: ""
                onBookingSuccess("BOOK-${System.currentTimeMillis() % 10000}", workerShopName, "$dateStr • $timeSlot")
                viewModel.clearState()
            }
            is NetworkResult.Error -> {
                snackbarHostState.showSnackbar((bookingState as NetworkResult.Error).message)
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            EResourceHeader(title = "Hire Technician", onBackClick = onNavigateBack)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            Text("Service with $workerShopName", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            
            // 1. SERVICE DETAILS
            CardWrapper {
                Text("Service Summary", fontWeight = FontWeight.Bold, color = PrimaryBlueColor)
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow(label = "Appliance", value = applianceType)
                DetailRow(label = "Inspection Fee", value = "₹199")
            }

            // 2. CUSTOMER INFO
            Text("Customer Details", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            FormTextField(value = fullName, onValueChange = { viewModel.onFullNameChanged(it) }, label = "Full Name", placeholder = "Enter name", leadingIcon = Icons.Default.Badge)
            FormTextField(value = mobile, onValueChange = { viewModel.onMobileChanged(it) }, label = "Mobile Number", placeholder = "+91...", leadingIcon = Icons.Default.Phone)

            // 3. INDIAN ADDRESS
            Text("Service Address (India 🇮🇳)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            FormTextField(value = houseNo, onValueChange = { viewModel.onHouseNoChanged(it) }, label = "House / Flat / Floor", placeholder = "e.g., 402, Block A", leadingIcon = Icons.Default.Home)
            FormTextField(value = street, onValueChange = { viewModel.onStreetAreaChanged(it) }, label = "Street / Area", placeholder = "e.g., Koramangala", leadingIcon = Icons.Default.LocationOn)
            FormTextField(value = city, onValueChange = { viewModel.onCityChanged(it) }, label = "City", placeholder = "e.g., Bengaluru", leadingIcon = Icons.Default.LocationCity)
            FormTextField(value = pinCode, onValueChange = { viewModel.onPinCodeChanged(it) }, label = "PIN Code", placeholder = "6-digit code", leadingIcon = Icons.Default.Pin)

            // 4. SCHEDULING
            Text("Schedule Visit", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            OutlinedCard(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                val label = selectedDate?.let { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it)) } ?: "Select Date"
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = PrimaryBlueColor)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(label, fontWeight = FontWeight.Medium)
                }
            }
            
            Text("Select Time Slot", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                viewModel.timeSlots.take(2).forEach { slot ->
                    TimeSlotChip(slot, timeSlot == slot) { viewModel.onTimeSlotSelected(slot) }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                viewModel.timeSlots.drop(2).forEach { slot ->
                    TimeSlotChip(slot, timeSlot == slot) { viewModel.onTimeSlotSelected(slot) }
                }
            }

            // 5. PAYMENT
            Text("Payment Method", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            val payments = listOf("Cash After Service", "UPI / Online")
            payments.forEach { method ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { viewModel.onPaymentMethodChanged(method) }) {
                    RadioButton(selected = payMethod == method, onClick = { viewModel.onPaymentMethodChanged(method) })
                    Text(method, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (bookingState is NetworkResult.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                MainActionButton(
                    text = "Confirm Booking",
                    onClick = { viewModel.createBooking(userToken, workerId) },
                    enabled = viewModel.isFormValid(),
                    testTag = "confirm_booking_btn"
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis >= System.currentTimeMillis() - 86400000
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onDateSelected(datePickerState.selectedDateMillis)
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) { DatePicker(state = datePickerState) }
    }
}

@Composable
fun TimeSlotChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) PrimaryBlueColor else Color.LightGray.copy(alpha = 0.2f))
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else Color.DarkGray)
    }
}
