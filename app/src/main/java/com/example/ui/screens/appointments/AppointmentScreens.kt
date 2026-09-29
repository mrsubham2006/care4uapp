package com.example.ui.screens.appointments

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.Appointment
import com.example.core.model.AppointmentStatus
import com.example.core.repository.CareNexusRepository
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.QRCodeDisplayBox
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun MyAppointmentsScreen(
  repository: CareNexusRepository,
  onAppointmentClick: (String) -> Unit,
  onNavigateToQueue: () -> Unit,
  onJoinVideoCall: (String) -> Unit = {},
  onBookNewClick: () -> Unit
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  val tabs = listOf("Upcoming", "Today", "Completed", "Cancelled")
  val appointments by repository.appointments.collectAsState()

  val filteredList = remember(selectedTab, appointments) {
    when (selectedTab) {
      0 -> appointments.filter { it.status == AppointmentStatus.CONFIRMED || it.status == AppointmentStatus.REQUESTED }
      1 -> appointments.filter { it.status == AppointmentStatus.CONFIRMED || it.status == AppointmentStatus.CHECKED_IN || it.status == AppointmentStatus.IN_QUEUE }
      2 -> appointments.filter { it.status == AppointmentStatus.COMPLETED }
      3 -> appointments.filter { it.status == AppointmentStatus.CANCELLED || it.status == AppointmentStatus.NO_SHOW }
      else -> appointments
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(bottom = 80.dp)
  ) {
    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = MaterialTheme.colorScheme.surface,
      contentColor = TealPrimary
    ) {
      tabs.forEachIndexed { index, title ->
        Tab(
          selected = selectedTab == index,
          onClick = { selectedTab = index },
          text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
        )
      }
    }

    if (filteredList.isEmpty()) {
      EmptyStateCard(
        icon = Icons.Default.CalendarToday,
        title = "No ${tabs[selectedTab]} Appointments",
        subtitle = "Schedule a consultation with our hospital specialists anytime.",
        actionText = "Book Appointment",
        onActionClick = onBookNewClick
      )
    } else {
      LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        items(filteredList, key = { it.appointmentId }) { apt ->
          AppointmentCard(
            appointment = apt,
            onCardClick = { onAppointmentClick(apt.appointmentId) },
            onQueueClick = onNavigateToQueue,
            onVideoCallClick = { onJoinVideoCall(apt.appointmentId) }
          )
        }
      }
    }
  }
}

@Composable
fun AppointmentCard(
  appointment: Appointment,
  onCardClick: () -> Unit,
  onQueueClick: () -> Unit,
  onVideoCallClick: () -> Unit = {}
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onCardClick() },
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          color = TealLight,
          shape = RoundedCornerShape(10.dp)
        ) {
          Text(
            text = "Token ${appointment.tokenNumber}",
            color = TealDark,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }

        StatusBadge(status = appointment.status.name)
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = appointment.doctorName,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )

      Text(
        text = "${appointment.department} • ${appointment.consultationType.replace("_", " ")}",
        style = MaterialTheme.typography.bodySmall,
        color = TealPrimary,
        fontWeight = FontWeight.SemiBold
      )

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(Slate50)
          .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Event, null, tint = Slate600, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(appointment.date, style = MaterialTheme.typography.bodySmall, color = Slate700)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Schedule, null, tint = Slate600, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(appointment.timeSlot, style = MaterialTheme.typography.bodySmall, color = Slate700)
        }
      }

      if (appointment.status == AppointmentStatus.CONFIRMED || appointment.status == AppointmentStatus.CHECKED_IN) {
        Spacer(modifier = Modifier.height(14.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = onCardClick,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text("Pass", fontSize = 12.sp)
          }

          Button(
            onClick = onVideoCallClick,
            modifier = Modifier.weight(1.3f).testTag("join_video_consultation_btn_${appointment.appointmentId}"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NavyDark)
          ) {
            Icon(Icons.Default.Videocam, null, modifier = Modifier.size(16.dp), tint = TealAccent)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Video Call", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }

          Button(
            onClick = onQueueClick,
            modifier = Modifier.weight(1.1f),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
          ) {
            Text("Queue", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
fun DigitalQueueScreen(
  repository: CareNexusRepository,
  onBackClick: () -> Unit,
  onScanCheckInClick: () -> Unit
) {
  val queueStatus by repository.queueStatus.collectAsState()
  val appointments by repository.appointments.collectAsState()
  val currentApt = appointments.firstOrNull { it.status == AppointmentStatus.CONFIRMED || it.status == AppointmentStatus.CHECKED_IN }

  // Polling state
  var isAutoPollingEnabled by remember { mutableStateOf(true) }
  var pollIntervalSeconds by remember { mutableIntStateOf(5) }
  var lastUpdatedTimestamp by remember { mutableStateOf("Just now") }
  var syncCount by remember { mutableIntStateOf(0) }

  // Coroutine periodic polling loop for real-time OPD token updates
  LaunchedEffect(isAutoPollingEnabled, pollIntervalSeconds) {
    while (isAutoPollingEnabled) {
      kotlinx.coroutines.delay(pollIntervalSeconds * 1000L)
      syncCount++
      lastUpdatedTimestamp = "${pollIntervalSeconds}s ago"
    }
  }

  val myTokenNum = queueStatus.tokenNumber.filter { it.isDigit() }.toIntOrNull() ?: 27
  val currentTokenNum = queueStatus.currentToken.filter { it.isDigit() }.toIntOrNull() ?: 22
  val totalSpan = maxOf(1, myTokenNum - 20)
  val progress = ((currentTokenNum - 20).toFloat() / totalSpan.toFloat()).coerceIn(0f, 1f)

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .background(MaterialTheme.colorScheme.background)
      .padding(16.dp)
      .padding(bottom = 80.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Header & Live Polling Status Bar
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "OPD Live Digital Queue",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = Slate900
        )
        Text(
          text = "Room 304 • Cardiology Department",
          style = MaterialTheme.typography.bodySmall,
          color = Slate500
        )
      }

      // Live Polling Pulsing Indicator
      Surface(
        color = if (isAutoPollingEnabled) SuccessLight else Slate100,
        shape = RoundedCornerShape(12.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(7.dp)
              .clip(CircleShape)
              .background(if (isAutoPollingEnabled) SuccessGreen else Slate400)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (isAutoPollingEnabled) "LIVE SYNC ($lastUpdatedTimestamp)" else "PAUSED",
            color = if (isAutoPollingEnabled) SuccessGreen else Slate600,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Polling Interval Selector Chips
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text("Auto-Poll:", style = MaterialTheme.typography.labelSmall, color = Slate600)
      listOf(3 to "3s", 5 to "5s", 10 to "10s").forEach { (interval, label) ->
        FilterChip(
          selected = isAutoPollingEnabled && pollIntervalSeconds == interval,
          onClick = {
            isAutoPollingEnabled = true
            pollIntervalSeconds = interval
          },
          label = { Text(label, fontSize = 11.sp) },
          shape = RoundedCornerShape(8.dp)
        )
      }
      FilterChip(
        selected = !isAutoPollingEnabled,
        onClick = { isAutoPollingEnabled = !isAutoPollingEnabled },
        label = { Text(if (isAutoPollingEnabled) "Pause" else "Resume", fontSize = 11.sp) },
        shape = RoundedCornerShape(8.dp)
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Queue Stage Alert Banner
    when {
      queueStatus.patientsAhead == 0 -> {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = SuccessLight)
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.NotificationsActive, null, tint = SuccessGreen, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text("TOKEN CALLED — PLEASE ENTER NOW", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = SuccessGreen)
              Text("Please proceed inside Room 304. Dr. Sharma is waiting.", style = MaterialTheme.typography.bodySmall, color = Slate800)
            }
          }
        }
      }
      queueStatus.patientsAhead <= 2 -> {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = WarningLight)
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.HourglassTop, null, tint = WarningAmber, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text("GET READY — 2 PATIENTS REMAIN", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Slate900)
              Text("Please stand near the entrance of Room 304 (3rd Floor).", style = MaterialTheme.typography.bodySmall, color = Slate700)
            }
          }
        }
      }
      else -> {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = TealLight.copy(alpha = 0.6f))
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Info, null, tint = TealPrimary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text("QUEUE MOVING STEADILY", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TealDark)
              Text("You may relax in the 3rd Floor lounge. We'll alert you at 2 patients remaining.", style = MaterialTheme.typography.bodySmall, color = Slate700)
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Token Hero Card
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("digital_queue_hero_card"),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = BlueDeep),
      elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "YOUR CONSULTATION TOKEN",
          style = MaterialTheme.typography.labelMedium,
          color = TealAccent,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = queueStatus.tokenNumber,
          style = MaterialTheme.typography.displayLarge,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Progress Linear Indicator
        LinearProgressIndicator(
          progress = { progress },
          modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp)),
          color = TealAccent,
          trackColor = NavyCard
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = NavyCard)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceEvenly
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Current Token Called", style = MaterialTheme.typography.labelSmall, color = Slate400)
            Spacer(modifier = Modifier.height(4.dp))
            Text(queueStatus.currentToken, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = WarningAmber)
          }

          VerticalDivider(modifier = Modifier.height(40.dp), color = NavyCard)

          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Patients Ahead", style = MaterialTheme.typography.labelSmall, color = Slate400)
            Spacer(modifier = Modifier.height(4.dp))
            Text("${queueStatus.patientsAhead}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
          }

          VerticalDivider(modifier = Modifier.height(40.dp), color = NavyCard)

          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Est. Wait Time", style = MaterialTheme.typography.labelSmall, color = Slate400)
            Spacer(modifier = Modifier.height(4.dp))
            Text("${queueStatus.estimatedWaitMinutes} min", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TealAccent)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Doctor & Room Status Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(TealLight),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.MedicalInformation, null, tint = TealPrimary, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text("Dr. Rahul Sharma • Cardiology", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
          Text("Location: ${queueStatus.roomNumber}", style = MaterialTheme.typography.bodySmall, color = Slate600)
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(SuccessGreen))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Status: ${queueStatus.doctorStatus.replace("_", " ")}", style = MaterialTheme.typography.labelSmall, color = SuccessGreen, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Interactive Demo Simulation Button to advance queue
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = TealLight)
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Tune, null, tint = TealDark, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "OPD Queue Token Simulator",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TealDark
          )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Simulate doctor calling the next token to test live progression and automatic notifications.",
          style = MaterialTheme.typography.bodySmall,
          color = TealDark,
          textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
          onClick = { repository.advanceDigitalQueue() },
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
          modifier = Modifier.testTag("advance_queue_button")
        ) {
          Icon(Icons.Default.FastForward, null)
          Spacer(modifier = Modifier.width(6.dp))
          Text("Call Next Token (Simulate)", fontWeight = FontWeight.Bold)
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedButton(
      onClick = onScanCheckInClick,
      modifier = Modifier.fillMaxWidth().height(48.dp),
      shape = RoundedCornerShape(12.dp)
    ) {
      Icon(Icons.Default.QrCodeScanner, null)
      Spacer(modifier = Modifier.width(8.dp))
      Text("Scan Hospital Check-in QR")
    }
  }
}

@Composable
fun AppointmentDetailScreen(
  appointmentId: String,
  repository: CareNexusRepository,
  onBackClick: () -> Unit,
  onNavigateToQueue: () -> Unit,
  onJoinVideoCall: () -> Unit = {}
) {
  val appointments by repository.appointments.collectAsState()
  val appointment = appointments.find { it.appointmentId == appointmentId } ?: appointments.first()
  var showCancelDialog by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .background(MaterialTheme.colorScheme.background)
      .padding(16.dp)
      .padding(bottom = 80.dp)
  ) {
    QRCodeDisplayBox(
      qrPayload = appointment.qrCodeData,
      title = "Appointment Pass",
      subtitle = "Appointment ID: ${appointment.appointmentId}"
    )

    Spacer(modifier = Modifier.height(16.dp))

    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Consultation Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          StatusBadge(status = appointment.status.name)
        }

        Spacer(modifier = Modifier.height(12.dp))

        DetailRow(label = "Doctor", value = appointment.doctorName)
        DetailRow(label = "Department", value = appointment.department)
        DetailRow(label = "Date & Slot", value = "${appointment.date} at ${appointment.timeSlot}")
        DetailRow(label = "Token Number", value = appointment.tokenNumber)
        DetailRow(label = "Mode", value = appointment.consultationType.replace("_", " "))
        DetailRow(label = "Reason", value = appointment.reasonForVisit)
        DetailRow(label = "Notes", value = appointment.notes)
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    if (appointment.status == AppointmentStatus.CONFIRMED || appointment.status == AppointmentStatus.CHECKED_IN) {
      Button(
        onClick = onJoinVideoCall,
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("appointment_detail_join_video_btn"),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = NavyDark)
      ) {
        Icon(Icons.Default.Videocam, null, tint = TealAccent)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Join Video Consultation Room", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
      }

      Spacer(modifier = Modifier.height(10.dp))

      Button(
        onClick = onNavigateToQueue,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
      ) {
        Text("Track Live Queue", fontWeight = FontWeight.Bold)
      }

      Spacer(modifier = Modifier.height(10.dp))

      OutlinedButton(
        onClick = { showCancelDialog = true },
        modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
        border = ButtonDefaults.outlinedButtonBorder(enabled = true)
      ) {
        Text("Cancel Appointment", fontWeight = FontWeight.Bold)
      }
    }
  }

  if (showCancelDialog) {
    AlertDialog(
      onDismissRequest = { showCancelDialog = false },
      title = { Text("Cancel Appointment?") },
      text = { Text("Are you sure you want to cancel appointment ${appointment.appointmentId} with ${appointment.doctorName}? This action cannot be undone.") },
      confirmButton = {
        Button(
          onClick = {
            repository.cancelAppointment(appointment.appointmentId, "Patient requested cancellation")
            showCancelDialog = false
            onBackClick()
          },
          colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
        ) {
          Text("Yes, Cancel")
        }
      },
      dismissButton = {
        TextButton(onClick = { showCancelDialog = false }) {
          Text("Keep Appointment")
        }
      }
    )
  }
}

@Composable
fun DetailRow(label: String, value: String) {
  Column(modifier = Modifier.padding(vertical = 4.dp)) {
    Text(text = label, style = MaterialTheme.typography.labelSmall, color = Slate500)
    Text(text = value, style = MaterialTheme.typography.bodyMedium, color = Slate900, fontWeight = FontWeight.Medium)
  }
}
