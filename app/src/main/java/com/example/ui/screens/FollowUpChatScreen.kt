package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.model.DiagnosisResult
import com.example.model.MessageSender
import com.example.ui.components.FrostedBackground
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.FrostedGlassSurface
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowUpChatScreen(
    diagnosis: DiagnosisResult,
    chatMessages: List<ChatMessage>,
    isSending: Boolean,
    onSendMessage: (String) -> Unit,
    onBack: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val suggestedQuestions = listOf(
        "Can I DIY repair this safely?",
        "Is it worth repairing vs buying new?",
        "How long will repair take?",
        "Are OEM spare parts required?"
    )

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    FrostedBackground {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("follow_up_chat_screen"),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "FixScan AI Consultant",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${diagnosis.brand} ${diagnosis.applianceType} Context Loaded",
                                style = MaterialTheme.typography.labelSmall.copy(color = SecondaryTeal, fontWeight = FontWeight.Bold)
                            )
                        }
                    },
                    navigationIcon = {
                        com.example.ui.components.GlassBackButton(onClick = onBack)
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = NeutralDark,
                        navigationIconContentColor = NeutralDark,
                        actionIconContentColor = NeutralDark
                    )
                )
            },
            bottomBar = {
                FrostedGlassSurface(
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    backgroundColor = Color.White.copy(alpha = 0.85f),
                    borderColor = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        // Suggested Question Chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            items(suggestedQuestions) { question ->
                                SuggestionChip(
                                    onClick = { onSendMessage(question) },
                                    label = { Text(question, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = PrimaryBlue.copy(alpha = 0.12f),
                                        labelColor = PrimaryBlue
                                    ),
                                    border = SuggestionChipDefaults.suggestionChipBorder(
                                        enabled = true,
                                        borderColor = PrimaryBlue.copy(alpha = 0.3f)
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Input Box
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                placeholder = { Text("Ask follow-up question...") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chat_input_text_field"),
                                shape = RoundedCornerShape(24.dp),
                                maxLines = 3,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White.copy(alpha = 0.7f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.5f)
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            FloatingActionButton(
                                onClick = {
                                    if (inputText.isNotBlank() && !isSending) {
                                        onSendMessage(inputText)
                                        inputText = ""
                                    }
                                },
                                modifier = Modifier.testTag("send_chat_button"),
                                containerColor = PrimaryBlue,
                                contentColor = Color.White
                            ) {
                                if (isSending) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Default.Send, contentDescription = "Send")
                                }
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                // Context Banner
                item {
                    FrostedGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color.White.copy(alpha = 0.70f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = PrimaryBlue
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "AI has loaded diagnosis: '${diagnosis.likelyFaults.firstOrNull()?.fault}'. Ask anything about parts or repair!",
                                style = MaterialTheme.typography.bodySmall.copy(color = NeutralMedium)
                            )
                        }
                    }
                }

                items(chatMessages) { msg ->
                    ChatMessageBubble(msg = msg)
                }
            }
        }
    }
}

@Composable
fun ChatMessageBubble(msg: ChatMessage) {
    val isUser = msg.sender == MessageSender.USER
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        if (isUser) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = 18.dp,
                    bottomEnd = 4.dp
                ),
                color = PrimaryBlue,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = msg.text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White,
                            lineHeight = 20.sp
                        )
                    )
                }
            }
        } else {
            FrostedGlassSurface(
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = 4.dp,
                    bottomEnd = 18.dp
                ),
                backgroundColor = Color.White.copy(alpha = 0.80f),
                borderColor = Color.White,
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = SecondaryTeal,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "FixScan AI Consultant",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SecondaryTeal,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = msg.text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = NeutralDark,
                            lineHeight = 20.sp
                        )
                    )
                }
            }
        }
    }
}
