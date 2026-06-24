@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.eresource.solution.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.eresource.solution.ui.components.EResourceHeader
import com.eresource.solution.ui.theme.PrimaryBlueColor
import com.eresource.solution.viewmodel.ChatbotViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatbotScreen(
    viewModel: ChatbotViewModel,
    onNavigateBack: () -> Unit
) {
    val messages by viewModel.messages.collectAsState()
    val suggestedQuestions by viewModel.suggestedQuestions.collectAsState()
    val isTyping by viewModel.isTyping.collectAsState()

    var inputCustomText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto scrolls on new message additions
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            EResourceHeader(
                title = "IQ Appliance SmartBot",
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
            // Main Dialog thread
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(messages) { msg ->
                        val align = if (msg.isUser) Alignment.CenterEnd else Alignment.CenterStart
                        val bg = if (msg.isUser) PrimaryBlueColor else Color.LightGray.copy(alpha = 0.4f)
                        val txtColor = if (msg.isUser) Color.White else MaterialTheme.colorScheme.onBackground

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = align
                        ) {
                            Column(
                                horizontalAlignment = if (msg.isUser) Alignment.End else Alignment.Start,
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
                                Text(
                                    text = if (msg.isUser) "Me" else "IQ Bot Assistant",
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
                                                bottomStart = if (msg.isUser) 12.dp else 0.dp,
                                                bottomEnd = if (msg.isUser) 0.dp else 12.dp
                                            )
                                        )
                                        .background(bg)
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = msg.text,
                                        fontSize = 14.sp,
                                        lineHeight = 18.sp,
                                        color = txtColor,
                                        modifier = Modifier.testTag("bot_message_content")
                                    )
                                }
                            }
                        }
                    }

                    if (isTyping) {
                        item {
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                LinearProgressIndicator(
                                    modifier = Modifier.width(40.dp).height(2.dp),
                                    color = PrimaryBlueColor
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("IQ Bot is typing...", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            // Quick Suggestions Chips
            if (suggestedQuestions.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LazyRow(
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(suggestedQuestions) { sug ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(PrimaryBlueColor.copy(alpha = 0.12f))
                                    .clickable { viewModel.selectQuestion(sug) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                                    .testTag("faq_suggestion_chip")
                            ) {
                                Text(
                                    text = sug,
                                    fontSize = 12.sp,
                                    color = PrimaryBlueColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Standard message inputs
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
                        value = inputCustomText,
                        onValueChange = { inputCustomText = it },
                        placeholder = { Text("Ask Bot technical questions...") },
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlueColor,
                            focusedLabelColor = PrimaryBlueColor
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("bot_custom_input_field")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (inputCustomText.isNotBlank()) {
                                viewModel.submitCustomQuestion(inputCustomText)
                                inputCustomText = ""
                            }
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(PrimaryBlueColor)
                            .testTag("submit_bot_custom_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send to Bot",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}
