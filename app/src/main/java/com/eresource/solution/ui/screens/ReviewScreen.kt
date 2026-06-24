@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.eresource.solution.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eresource.solution.data.repository.NetworkResult
import com.eresource.solution.ui.components.EResourceHeader
import com.eresource.solution.ui.components.FormTextField
import com.eresource.solution.ui.components.MainActionButton
import com.eresource.solution.ui.theme.PrimaryBlue
import com.eresource.solution.ui.theme.WarningAmber
import com.eresource.solution.viewmodel.ReviewViewModel

@Composable
fun ReviewScreen(
    viewModel: ReviewViewModel,
    userToken: String,
    bookingId: Int,
    workerId: Int,
    technicianName: String,
    onNavigateBack: () -> Unit,
    onReviewSuccess: () -> Unit
) {
    val comment by viewModel.comment.collectAsState()
    val rating by viewModel.rating.collectAsState()
    val actionState by viewModel.submitState.collectAsState()

    LaunchedEffect(actionState) {
        if (actionState is NetworkResult.Success) {
            onReviewSuccess()
        }
    }

    Scaffold(
        topBar = {
            EResourceHeader(title = "Post Review", onBackClick = onNavigateBack)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = PrimaryBlue.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.padding(20.dp),
                    tint = PrimaryBlue
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Rate your service with $technicianName",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Booking ID #$bookingId",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text("How was your experience?", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                repeat(5) { index ->
                    IconButton(onClick = { viewModel.onRatingChanged(index + 1) }) {
                        Icon(
                            imageVector = if (index < rating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = if (index < rating) WarningAmber else Color.LightGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            FormTextField(
                value = comment,
                onValueChange = { viewModel.onCommentChanged(it) },
                label = "Repair Review / Comment",
                placeholder = "Describe the technician's speed, behavior, and repair quality...",
                singleLine = false,
                modifier = Modifier.height(150.dp).testTag("review_input")
            )

            Spacer(modifier = Modifier.height(32.dp))

            MainActionButton(
                text = "Submit Review",
                onClick = { viewModel.submitReview(userToken, workerId, bookingId) },
                isLoading = actionState is NetworkResult.Loading,
                backgroundColor = PrimaryBlue
            )

            if (actionState is NetworkResult.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (actionState as NetworkResult.Error).message,
                    color = Color.Red,
                    fontSize = 13.sp
                )
            }
        }
    }
}
