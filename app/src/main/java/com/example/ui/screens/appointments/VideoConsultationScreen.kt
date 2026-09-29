package com.example.ui.screens.appointments

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.core.model.Appointment
import com.example.core.repository.CareNexusRepository
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun VideoConsultationScreen(
  appointmentId: String,
  repository: CareNexusRepository,
  onConsultationEnd: () -> Unit,
  onBackClick: () -> Unit
) {
  val context = LocalContext.current
  val appointments by repository.appointments.collectAsState()
  val appointment = appointments.find { it.appointmentId == appointmentId } ?: appointments.first()

  // Permission State
  var hasCameraPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    )
  }
  var hasMicPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    )
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { perms ->
    hasCameraPermission = perms[Manifest.permission.CAMERA] == true
    hasMicPermission = perms[Manifest.permission.RECORD_AUDIO] == true
  }

  // Auto request permissions upon entering
  LaunchedEffect(Unit) {
    if (!hasCameraPermission || !hasMicPermission) {
      permissionLauncher.launch(
        arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
      )
    }
  }

  // Call Controls State
  var isMicMuted by remember { mutableStateOf(false) }
  var isVideoDisabled by remember { mutableStateOf(false) }
  var isFrontCamera by remember { mutableStateOf(true) }
  var isChatDrawerOpen by remember { mutableStateOf(false) }
  var callDurationSeconds by remember { mutableIntStateOf(0) }
  var isConnected by remember { mutableStateOf(false) }
  var showPostCallSummary by remember { mutableStateOf(false) }

  // In-Call Chat messages
  var inCallMessage by remember { mutableStateOf("") }
  var inCallChatList by remember {
    mutableStateOf(
      listOf(
        "Dr. Sharma: Hello Subham, I can see your resting ECG is stable. How have you been feeling after exercise?",
        "Subham: Hello Dr. Sharma! I feel good, no chest tightness during my daily morning walks."
      )
    )
  }

  // Timer & Connection simulation
  LaunchedEffect(Unit) {
    delay(1500L) // Handshake simulation
    isConnected = true
    while (true) {
      delay(1000L)
      callDurationSeconds++
    }
  }

  val minutes = callDurationSeconds / 60
  val seconds = callDurationSeconds % 60
  val durationFormatted = String.format("%02d:%02d", minutes, seconds)

  if (!hasCameraPermission || !hasMicPermission) {
    // Permission Required Screen
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier
          .size(80.dp)
          .clip(CircleShape)
          .background(TealLight),
        contentAlignment = Alignment.Center
      ) {
        Icon(Icons.Default.Videocam, null, tint = TealPrimary, modifier = Modifier.size(44.dp))
      }

      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = "Camera & Microphone Access Required",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "CareNexus requires Camera and Audio permissions to connect you to your secure telehealth consultation with ${appointment.doctorName}.",
        style = MaterialTheme.typography.bodyMedium,
        color = Slate600,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(24.dp))

      Button(
        onClick = {
          permissionLauncher.launch(
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
          )
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("grant_telehealth_permissions_btn"),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
      ) {
        Text("Grant Telehealth Permissions", fontWeight = FontWeight.Bold)
      }

      Spacer(modifier = Modifier.height(12.dp))

      TextButton(onClick = onBackClick) {
        Text("Cancel & Go Back")
      }
    }
  } else {
    // Active Telehealth Video Consultation Screen
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(NavyDark)
    ) {
      // MAIN DOCTOR VIDEO FEED CANVAS
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F172A))
            )
          )
      ) {
        // Doctor Video Avatar / Live Feed Simulation
        Column(
          modifier = Modifier.align(Alignment.Center),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(130.dp)
              .clip(CircleShape)
              .background(TealPrimary.copy(alpha = 0.2f))
              .border(3.dp, if (isConnected) TealAccent else WarningAmber, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.MedicalServices,
              contentDescription = "Doctor Video",
              tint = Color.White,
              modifier = Modifier.size(64.dp)
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = appointment.doctorName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )

          Text(
            text = "${appointment.department} • Telehealth Specialist",
            style = MaterialTheme.typography.bodyMedium,
            color = TealAccent
          )

          Spacer(modifier = Modifier.height(8.dp))

          Surface(
            color = if (isConnected) SuccessGreen.copy(alpha = 0.2f) else WarningAmber.copy(alpha = 0.2f),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isConnected) SuccessGreen else WarningAmber)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(if (isConnected) SuccessGreen else WarningAmber)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (isConnected) "Connected ($durationFormatted)" else "Connecting to Telehealth Server...",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        // TOP CALL OVERLAY BAR
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, start = 16.dp, end = 16.dp)
            .align(Alignment.TopCenter),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            color = Color.Black.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.Security, null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "256-bit WebRTC Encrypted",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold
              )
            }
          }

          IconButton(
            onClick = { isChatDrawerOpen = !isChatDrawerOpen },
            modifier = Modifier
              .clip(CircleShape)
              .background(Color.Black.copy(alpha = 0.5f))
              .testTag("toggle_in_call_chat_btn")
          ) {
            Icon(
              imageVector = if (isChatDrawerOpen) Icons.Default.Close else Icons.Default.ChatBubbleOutline,
              contentDescription = "In-Call Chat",
              tint = Color.White
            )
          }
        }

        // PATIENT SELF-VIEW (PICTURE-IN-PICTURE TILE)
        Box(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(top = 90.dp, end = 16.dp)
            .size(width = 110.dp, height = 150.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.8f))
            .border(2.dp, TealAccent.copy(alpha = 0.8f), RoundedCornerShape(16.dp)),
          contentAlignment = Alignment.Center
        ) {
          if (isVideoDisabled) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(Icons.Default.VideocamOff, null, tint = Slate400, modifier = Modifier.size(28.dp))
              Text("Video Off", color = Slate400, fontSize = 10.sp)
            }
          } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(36.dp))
              Text("Self View", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
              Text(if (isFrontCamera) "Front Camera" else "Rear Camera", color = TealAccent, fontSize = 8.sp)
            }
          }
        }

        // IN-CALL CHAT DRAWER (Slide in overlay)
        AnimatedVisibility(
          visible = isChatDrawerOpen,
          enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
          exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(),
          modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .align(Alignment.BottomCenter)
            .padding(bottom = 100.dp, start = 16.dp, end = 16.dp)
        ) {
          Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text("In-Consultation Chat & Notes", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
              Spacer(modifier = Modifier.height(8.dp))

              LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                items(inCallChatList) { msg ->
                  Text(msg, style = MaterialTheme.typography.bodySmall, color = Slate800)
                }
              }

              Spacer(modifier = Modifier.height(8.dp))

              Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                  value = inCallMessage,
                  onValueChange = { inCallMessage = it },
                  placeholder = { Text("Type message for doctor...", fontSize = 12.sp) },
                  modifier = Modifier.weight(1f),
                  shape = RoundedCornerShape(12.dp),
                  singleLine = true
                )
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                  onClick = {
                    if (inCallMessage.isNotBlank()) {
                      inCallChatList = inCallChatList + "Subham: ${inCallMessage.trim()}"
                      inCallMessage = ""
                    }
                  },
                  modifier = Modifier.clip(CircleShape).background(TealPrimary)
                ) {
                  Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
              }
            }
          }
        }

        // FLOATING TELEHEALTH CONTROL BAR
        Surface(
          color = Color.Black.copy(alpha = 0.85f),
          shape = RoundedCornerShape(24.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .align(Alignment.BottomCenter)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Mute / Unmute Mic
            IconButton(
              onClick = { isMicMuted = !isMicMuted },
              modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(if (isMicMuted) ErrorRed else Color.White.copy(alpha = 0.15f))
                .testTag("toggle_telehealth_mic_btn")
            ) {
              Icon(
                imageVector = if (isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = "Mute",
                tint = Color.White
              )
            }

            // Video Camera Toggle
            IconButton(
              onClick = { isVideoDisabled = !isVideoDisabled },
              modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(if (isVideoDisabled) ErrorRed else Color.White.copy(alpha = 0.15f))
                .testTag("toggle_telehealth_camera_btn")
            ) {
              Icon(
                imageVector = if (isVideoDisabled) Icons.Default.VideocamOff else Icons.Default.Videocam,
                contentDescription = "Camera",
                tint = Color.White
              )
            }

            // Flip Camera Front/Rear
            IconButton(
              onClick = { isFrontCamera = !isFrontCamera },
              modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f))
                .testTag("flip_telehealth_camera_btn")
            ) {
              Icon(
                imageVector = Icons.Default.FlipCameraAndroid,
                contentDescription = "Flip Camera",
                tint = Color.White
              )
            }

            // END CALL BUTTON
            IconButton(
              onClick = { showPostCallSummary = true },
              modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(EmergencyColor)
                .testTag("end_telehealth_call_btn")
            ) {
              Icon(
                imageVector = Icons.Default.CallEnd,
                contentDescription = "End Call",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
              )
            }
          }
        }
      }
    }
  }

  // POST-CONSULTATION SUMMARY DIALOG
  if (showPostCallSummary) {
    AlertDialog(
      onDismissRequest = {
        showPostCallSummary = false
        onConsultationEnd()
      },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(24.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Consultation Completed")
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Doctor: ${appointment.doctorName} (${appointment.department})", fontWeight = FontWeight.Bold)
          Text("Call Duration: $durationFormatted", style = MaterialTheme.typography.bodySmall, color = Slate600)
          Spacer(modifier = Modifier.height(4.dp))
          Text("Clinical Encounter Summary:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
          Text(
            "Dr. Sharma reviewed resting 12-lead ECG and metabolic lipid results. Stable hemodynamics confirmed. E-prescription generated for Atorvastatin 20mg and CoQ10. Re-evaluation scheduled in 90 days.",
            style = MaterialTheme.typography.bodySmall,
            color = Slate800,
            lineHeight = 20.sp
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            showPostCallSummary = false
            onConsultationEnd()
          },
          colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
        ) {
          Text("View Summary & Close")
        }
      }
    )
  }
}
