package com.example.ui.screens.ai

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.ChatMessage
import com.example.core.repository.CareNexusRepository
import com.example.ui.theme.*

@Composable
fun CareAiScreen(
  repository: CareNexusRepository,
  onNavigateToRoute: (String) -> Unit
) {
  val messages by repository.chatMessages.collectAsState()
  var inputQuery by remember { mutableStateOf("") }
  var isListeningVoice by remember { mutableStateOf(false) }
  var showConfirmCancelDialog by remember { mutableStateOf<String?>(null) }
  var showSafetyInfoDialog by remember { mutableStateOf(false) }
  val listState = rememberLazyListState()

  val suggestedPrompts = listOf(
    "🩺 What should I ask Dr. Sharma about my heart health?",
    "🧪 Explain high LDL cholesterol & dietary tips",
    "💊 How does Atorvastatin work and when should I take it?",
    "📅 When is my next hospital appointment?",
    "🏥 Where is Cardiology & Room 304?",
    "💳 How do I pay my pending hospital bill?",
    "🌡️ How should I manage a fever at home?"
  )

  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.lastIndex)
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(bottom = 80.dp)
  ) {
    // Top Sub-Header with Gemini Model Indicator & Safety Info
    Surface(
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 1.dp
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(
                Brush.linearGradient(listOf(TealPrimary, TealAccent))
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(22.dp))
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text("CareAI Assistant", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
              Spacer(modifier = Modifier.width(6.dp))
              Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(SuccessGreen))
            }
            Text("Powered by Gemini 3.5 Flash • Patient Context Active", style = MaterialTheme.typography.labelSmall, color = Slate500)
          }
        }

        IconButton(onClick = { showSafetyInfoDialog = true }) {
          Icon(Icons.Default.Info, contentDescription = "Safety Guidelines", tint = Slate500, modifier = Modifier.size(20.dp))
        }
      }
    }

    // Safety Banner Notice
    Surface(
      color = TealLight.copy(alpha = 0.5f),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(Icons.Default.HealthAndSafety, null, tint = TealPrimary, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Ask any medical question, lab result explanation, or appointment question.",
          style = MaterialTheme.typography.labelSmall,
          color = TealDark
        )
      }
    }

    // Messages List
    LazyColumn(
      state = listState,
      modifier = Modifier
        .weight(1f)
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      items(messages, key = { it.id }) { msg ->
        ChatBubbleItem(
          message = msg,
          onActionClick = { payload ->
            if (msg.actionRequired) {
              showConfirmCancelDialog = payload
            }
          },
          onNavigateShortcut = { route ->
            onNavigateToRoute(route)
          }
        )
      }
    }

    // Quick Prompt Suggestions Carousel
    LazyRow(
      contentPadding = PaddingValues(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.padding(vertical = 6.dp)
    ) {
      items(suggestedPrompts) { prompt ->
        SuggestionChip(
          onClick = {
            repository.sendChatMessage(prompt)
          },
          label = { Text(prompt, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
          shape = RoundedCornerShape(12.dp),
          colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.surface
          )
        )
      }
    }

    // Input Bar with Animated Voice & Send
    Surface(
      color = MaterialTheme.colorScheme.surface,
      shadowElevation = 4.dp
    ) {
      Column {
        if (isListeningVoice) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(ErrorLight)
              .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ErrorRed))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Listening to your medical question...", color = ErrorRed, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = { isListeningVoice = false }) {
              Text("Stop", color = ErrorRed, style = MaterialTheme.typography.labelSmall)
            }
          }
        }

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Voice Input Mic Button
          IconButton(
            onClick = {
              isListeningVoice = !isListeningVoice
              if (isListeningVoice) {
                inputQuery = "What should I ask Dr. Sharma about my heart health?"
              }
            },
            modifier = Modifier.testTag("careai_voice_mic_button")
          ) {
            Icon(
              imageVector = if (isListeningVoice) Icons.Default.Mic else Icons.Default.MicNone,
              contentDescription = "Voice Input",
              tint = if (isListeningVoice) ErrorRed else TealPrimary
            )
          }

          OutlinedTextField(
            value = inputQuery,
            onValueChange = { inputQuery = it },
            placeholder = { Text(if (isListeningVoice) "Listening..." else "Ask a medical or hospital question...") },
            modifier = Modifier
              .weight(1f)
              .testTag("careai_text_input"),
            shape = RoundedCornerShape(20.dp),
            maxLines = 3
          )

          Spacer(modifier = Modifier.width(6.dp))

          IconButton(
            onClick = {
              if (inputQuery.isNotBlank()) {
                repository.sendChatMessage(inputQuery.trim())
                inputQuery = ""
                isListeningVoice = false
              }
            },
            modifier = Modifier
              .clip(CircleShape)
              .background(TealPrimary)
              .testTag("careai_send_button")
          ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
          }
        }
      }
    }
  }

  // Safety Guidelines Modal
  if (showSafetyInfoDialog) {
    AlertDialog(
      onDismissRequest = { showSafetyInfoDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.HealthAndSafety, null, tint = TealPrimary)
          Spacer(modifier = Modifier.width(8.dp))
          Text("CareAI Clinical Guidelines")
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("• CareAI is designed to provide educational assistance and clarify hospital workflows.", style = MaterialTheme.typography.bodySmall, color = Slate700)
          Text("• CareAI does not diagnose conditions or prescribe treatments.", style = MaterialTheme.typography.bodySmall, color = Slate700)
          Text("• Always follow your attending physician's direct clinical advice.", style = MaterialTheme.typography.bodySmall, color = Slate700)
          Text("• For urgent emergencies (chest pain, severe shortness of breath), immediately tap Emergency (SOS) or dial 911.", style = MaterialTheme.typography.bodySmall, color = ErrorRed, fontWeight = FontWeight.Bold)
        }
      },
      confirmButton = {
        TextButton(onClick = { showSafetyInfoDialog = false }) {
          Text("I Understand")
        }
      }
    )
  }

  // Sensitive Voice Confirmation Modal
  if (showConfirmCancelDialog != null) {
    AlertDialog(
      onDismissRequest = { showConfirmCancelDialog = null },
      title = { Text("Sensitive Action Confirmation") },
      text = { Text("Voice AI detected request: You are about to cancel your scheduled appointment. Do you wish to proceed?") },
      confirmButton = {
        Button(
          onClick = {
            showConfirmCancelDialog?.let { repository.cancelAppointment(it, "Cancelled via CareAI Assistant") }
            showConfirmCancelDialog = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
        ) {
          Text("Yes, Cancel Appointment")
        }
      },
      dismissButton = {
        TextButton(onClick = { showConfirmCancelDialog = null }) {
          Text("Dismiss")
        }
      }
    )
  }
}

@Composable
fun ChatBubbleItem(
  message: ChatMessage,
  onActionClick: (String?) -> Unit,
  onNavigateShortcut: (String) -> Unit
) {
  val isUser = message.sender == "USER"

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
  ) {
    if (!isUser) {
      Box(
        modifier = Modifier
          .size(34.dp)
          .clip(CircleShape)
          .background(TealLight),
        contentAlignment = Alignment.Center
      ) {
        Icon(Icons.Default.AutoAwesome, null, tint = TealPrimary, modifier = Modifier.size(18.dp))
      }
      Spacer(modifier = Modifier.width(8.dp))
    }

    Card(
      shape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (isUser) 16.dp else 4.dp,
        bottomEnd = if (isUser) 4.dp else 16.dp
      ),
      colors = CardDefaults.cardColors(
        containerColor = if (isUser) TealPrimary else MaterialTheme.colorScheme.surface
      ),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
      modifier = Modifier.widthIn(max = 310.dp)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        if (message.isLoading) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(
              modifier = Modifier.size(16.dp),
              color = TealPrimary,
              strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("CareAI is consulting verified records...", style = MaterialTheme.typography.bodySmall, color = Slate500)
          }
        } else {
          Text(
            text = message.message,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isUser) Color.White else Slate900,
            lineHeight = 21.sp
          )

          // Contextual Action Shortcuts embedded in AI message
          if (!isUser) {
            val lower = message.message.lowercase()
            if (lower.contains("appointment") || lower.contains("dr. rahul sharma")) {
              Spacer(modifier = Modifier.height(10.dp))
              OutlinedButton(
                onClick = { onNavigateShortcut("appointments") },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("View Appointments Pass", fontSize = 11.sp)
              }
            } else if (lower.contains("lab") || lower.contains("lipid")) {
              Spacer(modifier = Modifier.height(10.dp))
              OutlinedButton(
                onClick = { onNavigateShortcut("laboratory") },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Icon(Icons.Default.Science, null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Open Laboratory Report", fontSize = 11.sp)
              }
            } else if (lower.contains("emergency") || lower.contains("critical")) {
              Spacer(modifier = Modifier.height(10.dp))
              Button(
                onClick = { onNavigateShortcut("emergency") },
                colors = ButtonDefaults.buttonColors(containerColor = EmergencyColor),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Icon(Icons.Default.Emergency, null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Open Emergency (SOS) Center", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }

          if (message.actionRequired && message.actionLabel != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Button(
              onClick = { onActionClick(message.actionPayload) },
              colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(message.actionLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = message.timestamp,
            style = MaterialTheme.typography.labelSmall,
            color = if (isUser) Color.White.copy(alpha = 0.7f) else Slate400,
            modifier = Modifier.align(Alignment.End)
          )
        }
      }
    }
  }
}
