@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.eresource.solution.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
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
import com.eresource.solution.data.models.ChatMessage
import com.eresource.solution.data.repository.NetworkResult
import com.eresource.solution.ui.components.*
import com.eresource.solution.ui.theme.PrimaryBlueColor
import com.eresource.solution.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    userToken: String,
    currentUserId: Int,
    bookingId: Int,
    otherPartyName: String,
    onNavigateBack: () -> Unit
) {
    val messagesState by viewModel.messagesState.collectAsState()
    val sendState by viewModel.sendState.collectAsState()

    var inputMessageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.startPolling(userToken, bookingId)
    }

    LaunchedEffect(sendState) {
        if (sendState is NetworkResult.Success) {
            inputMessageText = ""
            viewModel.clearSendState()
        }
    }

    // Auto scroll to bottom upon message loads
    LaunchedEffect(messagesState) {
        if (messagesState is NetworkResult.Success) {
            val list = (messagesState as NetworkResult.Success).data
            if (list.isNotEmpty()) {
                listState.animateScrollToItem(list.size - 1)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopPolling()
        }
    }

    Scaffold(
        topBar = {
            EResourceHeader(
                title = "Chat: $otherPartyName",
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
            // Main Messages Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (messagesState) {
                    is NetworkResult.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.testTag("chat_messages_loader"))
                        }
                    }
                    is NetworkResult.Error -> {
                        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            AnimatedErrorAlert(message = (messagesState as NetworkResult.Error).message)
                        }
                    }
                    is NetworkResult.Success -> {
                        val messages = (messagesState as NetworkResult.Success).data
                        if (messages.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Introduce yourself, and coordinate the repair slot!", color = Color.Gray, fontSize = 13.sp)
                            }
                        } else {
                            LazyColumn(
                                state = listState,
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(messages) { msg ->
                                    val isMe = msg.sender_id == currentUserId

                                    val bubbleAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                                    val bubbleBg = if (isMe) PrimaryBlueColor else Color.LightGray.copy(alpha = 0.4f)
                                    val textWeight = if (isMe) FontWeight.Bold else FontWeight.Medium
                                    val textColor = if (isMe) Color.White else MaterialTheme.colorScheme.onBackground

                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = bubbleAlignment
                                    ) {
                                        Column(
                                            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
                                            modifier = Modifier.widthIn(max = 280.dp)
                                        ) {
                                            // Sender meta label
                                            Text(
                                                text = if (isMe) "Me" else otherPartyName,
                                                fontSize = 10.sp,
                                                color = Color.Gray,
                                                modifier = Modifier.padding(bottom = 2.dp)
                                            )

                                            Box(
                                                modifier = Modifier
                                                    .clip(
                                                        RoundedCornerShape(
                                                            topStart = 12.dp,
                                                            topEnd = 12.dp,
                                                            bottomStart = if (isMe) 12.dp else 0.dp,
                                                            bottomEnd = if (isMe) 0.dp else 12.dp
                                                        )
                                                    )
                                                    .background(bubbleBg)
                                                    .padding(12.dp)
                                            ) {
                                                Text(
                                                    text = msg.message,
                                                    fontSize = 14.sp,
                                                    fontWeight = textWeight,
                                                    color = textColor,
                                                    modifier = Modifier.testTag("chat_msg_bubble")
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Input Send Row
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputMessageText,
                        onValueChange = { inputMessageText = it },
                        placeholder = { Text("Tape message...") },
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlueColor,
                            focusedLabelColor = PrimaryBlueColor
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_text_field")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            viewModel.sendMessage(userToken, bookingId, inputMessageText)
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(PrimaryBlueColor)
                            .testTag("chat_send_message_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}
