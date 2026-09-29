package com.example.ui.screens.symptom

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.core.model.SymptomChatMessage
import com.example.core.model.SymptomTriageResult
import com.example.core.repository.CareNexusRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomCheckerScreen(
  repository: CareNexusRepository,
  onNavigateBack: () -> Unit,
  onBookAppointmentWithDoctor: (String) -> Unit,
  onExploreDepartmentDoctors: (String) -> Unit,
  onNavigateToEmergency: () -> Unit
) {
  var userInput by remember { mutableStateOf("") }
  var isAiThinking by remember { mutableStateOf(false) }
  var turnCount by remember { mutableStateOf(0) }

  val defaultWelcomeMsg = remember {
    SymptomChatMessage(
      id = "msg-welcome-triage",
      sender = "AI",
      message = "Hello Subham! I am **CareAI Triage Assistant**. Describe what symptoms you are feeling today, or select one of the common categories below. I will ask a few follow-up questions and guide you to the right hospital department for booking.",
      timestamp = "Just now",
      isFollowUpQuestion = true,
      suggestedQuickReplies = listOf(
        "Chest Discomfort / Palpitations",
        "Severe Migraine / Headache",
        "Joint & Knee Pain",
        "Fever & Chills",
        "Skin Rash & Itching",
        "Persistent Stomach Cramps"
      )
    )
  }

  var messages by remember { mutableStateOf(listOf(defaultWelcomeMsg)) }
  val listState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()

  val latestTriageResult = messages.mapNotNull { it.triageRecommendation }.lastOrNull()

  fun sendMessage(text: String) {
    if (text.isBlank()) return
    val userMsg = SymptomChatMessage(
      id = "msg-user-${System.currentTimeMillis()}",
      sender = "USER",
      message = text,
      timestamp = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
    )
    messages = messages + userMsg
    userInput = ""
    turnCount += 1
    isAiThinking = true

    coroutineScope.launch {
      listState.animateScrollToItem(messages.size - 1)
      val history = messages.map { Pair(it.sender, it.message) }
      val (aiText, triage) = repository.checkSymptoms(text, turnCount, history)

      val quickReplies = when {
        triage != null -> emptyList()
        text.contains("chest", true) || text.contains("heart", true) -> listOf("Started today", "Mild squeezing (3/10)", "Severe pain (8/10)", "No radiation to arm")
        text.contains("headache", true) || text.contains("migraine", true) -> listOf("Started 2 days ago", "Pulsating (6/10)", "Sensitive to light", "No fever")
        text.contains("joint", true) || text.contains("knee", true) -> listOf("After running / sports", "Morning stiffness", "Moderate ache (4/10)", "Gradual onset")
        text.contains("fever", true) || text.contains("cold", true) -> listOf("100.4°F (Mild)", "Over 101.5°F", "Dry cough & fatigue", "Started yesterday")
        else -> listOf("Mild (1-3)", "Moderate (4-6)", "Started today", "Over 3 days")
      }

      val aiMsg = SymptomChatMessage(
        id = "msg-ai-${System.currentTimeMillis()}",
        sender = "AI",
        message = aiText,
        timestamp = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()),
        isFollowUpQuestion = triage == null,
        suggestedQuickReplies = quickReplies,
        triageRecommendation = triage
      )

      messages = messages + aiMsg
      isAiThinking = false
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "AI Symptom Checker",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Slate900
            )
            Text(
              text = "Clinical Triage & Department Guidance",
              style = MaterialTheme.typography.labelSmall,
              color = Slate500
            )
          }
        },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Slate800)
          }
        },
        actions = {
          IconButton(
            onClick = {
              messages = listOf(defaultWelcomeMsg)
              turnCount = 0
            }
          ) {
            Icon(Icons.Default.Refresh, contentDescription = "Reset Triage", tint = TealPrimary)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
      )
    },
    bottomBar = {
      Surface(
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          // Suggested Quick Reply Chips
          val latestAiMsg = messages.lastOrNull { it.sender == "AI" }
          if (latestAiMsg != null && latestAiMsg.suggestedQuickReplies.isNotEmpty()) {
            LazyRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.padding(bottom = 8.dp)
            ) {
              items(latestAiMsg.suggestedQuickReplies) { reply ->
                SuggestionChip(
                  onClick = { sendMessage(reply) },
                  label = { Text(reply, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                  colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = TealLight,
                    labelColor = TealDark
                  ),
                  border = SuggestionChipDefaults.suggestionChipBorder(enabled = true, borderColor = TealPrimary.copy(alpha = 0.3f))
                )
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = userInput,
              onValueChange = { userInput = it },
              placeholder = { Text("Describe what you're experiencing...") },
              modifier = Modifier
                .weight(1f)
                .testTag("symptom_input_field"),
              shape = RoundedCornerShape(20.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TealPrimary,
                unfocusedBorderColor = Slate300
              ),
              maxLines = 3
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
              onClick = { sendMessage(userInput) },
              modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (userInput.isNotBlank()) TealPrimary else Slate300)
                .testTag("send_symptom_btn"),
              enabled = userInput.isNotBlank() && !isAiThinking
            ) {
              Icon(
                Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }
      }
    }
  ) { paddingValues ->
    LazyColumn(
      state = listState,
      modifier = Modifier
        .fillMaxSize()
        .background(Slate50)
        .padding(paddingValues),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // 1. Clinical Emergency Red-Flag Notice
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = WarningLight)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Emergency, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Emergency Warning",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Slate900
              )
              Text(
                text = "If experiencing crushing chest pain, severe shortness of breath, or sudden weakness, call 911 immediately.",
                style = MaterialTheme.typography.bodySmall,
                color = Slate700,
                fontSize = 11.sp
              )
            }
            TextButton(onClick = onNavigateToEmergency) {
              Text("SOS ER", color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }
        }
      }

      // 2. Chat Message Stream
      items(messages, key = { it.id }) { msg ->
        if (msg.sender == "USER") {
          UserMessageBubble(text = msg.message, timestamp = msg.timestamp)
        } else {
          AiMessageBubble(
            message = msg,
            onBookDoctor = onBookAppointmentWithDoctor,
            onExploreDept = onExploreDepartmentDoctors
          )
        }
      }

      // 3. Thinking Indicator
      if (isAiThinking) {
        item {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp)
          ) {
            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(TealLight),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = Color.White,
              shadowElevation = 1.dp
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = TealPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("CareAI is evaluating clinical indicators...", style = MaterialTheme.typography.bodySmall, color = Slate600)
              }
            }
          }
        }
      }

      // 4. Final Triage Highlight Card (if verdict reached)
      if (latestTriageResult != null) {
        item {
          TriageRecommendationCard(
            triage = latestTriageResult,
            onBookDoctor = onBookAppointmentWithDoctor,
            onExploreDept = onExploreDepartmentDoctors
          )
        }
      }
    }
  }
}

@Composable
fun UserMessageBubble(text: String, timestamp: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.End
  ) {
    Surface(
      shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp),
      color = TealPrimary,
      shadowElevation = 1.dp,
      modifier = Modifier.widthIn(max = 300.dp)
    ) {
      Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
        Text(
          text = text,
          color = Color.White,
          style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = timestamp,
          color = Color.White.copy(alpha = 0.7f),
          style = MaterialTheme.typography.labelSmall,
          fontSize = 9.sp,
          modifier = Modifier.align(Alignment.End)
        )
      }
    }
  }
}

@Composable
fun AiMessageBubble(
  message: SymptomChatMessage,
  onBookDoctor: (String) -> Unit,
  onExploreDept: (String) -> Unit
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.Start
  ) {
    Box(
      modifier = Modifier
        .size(34.dp)
        .clip(CircleShape)
        .background(TealLight),
      contentAlignment = Alignment.Center
    ) {
      Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
    }

    Spacer(modifier = Modifier.width(8.dp))

    Column(modifier = Modifier.widthIn(max = 310.dp)) {
      Surface(
        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp),
        color = Color.White,
        shadowElevation = 1.dp
      ) {

        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = message.message,
            style = MaterialTheme.typography.bodyMedium,
            color = Slate800,
            lineHeight = 20.sp
          )

          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = message.timestamp,
            style = MaterialTheme.typography.labelSmall,
            color = Slate400,
            fontSize = 9.sp
          )
        }
      }
    }
  }
}

@Composable
fun TriageRecommendationCard(
  triage: SymptomTriageResult,
  onBookDoctor: (String) -> Unit,
  onExploreDept: (String) -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = NavyDark)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.horizontalGradient(
            listOf(NavyDark, Color(0xFF1E293B), TealDark)
          )
        )
        .padding(18.dp)
    ) {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(TealAccent),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Verified, contentDescription = null, tint = NavyDark, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "CLINICAL TRIAGE VERDICT",
                style = MaterialTheme.typography.labelSmall,
                color = TealAccent,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
              )
              Text(
                text = "Suggested Department: ${triage.primaryDepartment}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
          }

          Surface(
            color = if (triage.urgencyLevel == "EMERGENCY_ALERT") ErrorRed else TealLight.copy(alpha = 0.2f),
            shape = RoundedCornerShape(6.dp)
          ) {
            Text(
              text = triage.urgencyLevel.replace("_", " "),
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Doctor Card recommendation inside
        Surface(
          color = NavyCard,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .clip(CircleShape)
                  .background(TealPrimary),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.MedicalServices, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = triage.recommendedDoctorName,
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  style = MaterialTheme.typography.titleSmall
                )
                Text(
                  text = "${triage.primaryDepartment} Specialist • CareNexus Central",
                  color = Slate300,
                  style = MaterialTheme.typography.bodySmall,
                  fontSize = 11.sp
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = triage.reasoningSummary,
          color = Slate200,
          style = MaterialTheme.typography.bodySmall,
          lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 1-Tap Booking CTA
        Button(
          onClick = { onBookDoctor(triage.recommendedDoctorId) },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("book_recommended_appointment_btn"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
        ) {
          Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Book Appointment with ${triage.recommendedDoctorName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedButton(
          onClick = { onExploreDept(triage.departmentId) },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
          border = ButtonDefaults.outlinedButtonBorder(enabled = true)
        ) {
          Text("View All Specialists in ${triage.primaryDepartment}", fontSize = 12.sp)
        }
      }
    }
  }
}
