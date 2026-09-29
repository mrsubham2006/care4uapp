package com.example.ui.screens.medications

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.MedicationReminder
import com.example.core.repository.CareNexusRepository
import com.example.ui.components.EmptyStateCard
import com.example.ui.theme.*

@Composable
fun MedicationRemindersScreen(
  repository: CareNexusRepository,
  onBackClick: () -> Unit
) {
  val reminders by repository.medicationReminders.collectAsState()
  var showAddDialog by remember { mutableStateOf(false) }

  val takenCount = reminders.count { it.isTakenToday }
  val totalCount = reminders.size
  val adherencePercent = if (totalCount > 0) ((takenCount.toFloat() / totalCount.toFloat()) * 100).toInt() else 0

  val morningList = reminders.filter { it.timePeriod == "MORNING" }
  val afternoonList = reminders.filter { it.timePeriod == "AFTERNOON" }
  val eveningList = reminders.filter { it.timePeriod == "EVENING" }
  val bedtimeList = reminders.filter { it.timePeriod == "BEDTIME" }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(bottom = 80.dp)
  ) {
    // Top Summary Banner
    Surface(
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 2.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text("Medication Reminders", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Daily alarms synchronized with doctor e-prescriptions", style = MaterialTheme.typography.labelSmall, color = Slate500)
          }

          Surface(
            color = if (adherencePercent == 100) SuccessLight else WarningLight,
            shape = RoundedCornerShape(12.dp)
          ) {
            Text(
              text = "$takenCount of $totalCount Taken Today",
              color = if (adherencePercent == 100) SuccessGreen else Slate900,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Adherence Progress Card
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = NavyDark)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocalFireDepartment, null, tint = WarningAmber, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("14 Day Adherence Streak", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
              }
              Text("$adherencePercent%", color = TealAccent, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
              progress = { adherencePercent / 100f },
              modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
              color = if (adherencePercent == 100) SuccessGreen else TealAccent,
              trackColor = NavyCard
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Actions Row: Import Rx & Add Custom
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = { repository.importRemindersFromPrescriptions() },
            modifier = Modifier.weight(1f).testTag("import_rx_reminders_btn"),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.Sync, null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Sync e-Prescriptions", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
          }

          Button(
            onClick = { showAddDialog = true },
            modifier = Modifier.weight(1f).testTag("add_custom_reminder_btn"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
          ) {
            Icon(Icons.Default.AddAlarm, null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Add Reminder", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // Reminders List by Time Period
    if (reminders.isEmpty()) {
      EmptyStateCard(
        icon = Icons.Default.Alarm,
        title = "No Medication Reminders Scheduled",
        subtitle = "Sync with your doctor e-prescriptions or add a custom daily pill alert.",
        actionText = "Sync Doctor Prescriptions",
        onActionClick = { repository.importRemindersFromPrescriptions() }
      )
    } else {
      LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        if (morningList.isNotEmpty()) {
          item {
            PeriodHeader("🌅 Morning Doses", "Breakfast & Start of Day")
          }
          items(morningList, key = { it.id }) { reminder ->
            ReminderCard(
              reminder = reminder,
              onToggleActive = { repository.toggleMedicationReminderActive(reminder.id) },
              onMarkTaken = { repository.markMedicationTaken(reminder.id) },
              onTestAlert = { repository.triggerMedicationAlert(reminder.id) },
              onDelete = { repository.deleteMedicationReminder(reminder.id) }
            )
          }
        }

        if (afternoonList.isNotEmpty()) {
          item {
            PeriodHeader("☀️ Afternoon Doses", "Lunch & Midday")
          }
          items(afternoonList, key = { it.id }) { reminder ->
            ReminderCard(
              reminder = reminder,
              onToggleActive = { repository.toggleMedicationReminderActive(reminder.id) },
              onMarkTaken = { repository.markMedicationTaken(reminder.id) },
              onTestAlert = { repository.triggerMedicationAlert(reminder.id) },
              onDelete = { repository.deleteMedicationReminder(reminder.id) }
            )
          }
        }

        if (eveningList.isNotEmpty()) {
          item {
            PeriodHeader("🌆 Evening Doses", "Dinner & Sunset")
          }
          items(eveningList, key = { it.id }) { reminder ->
            ReminderCard(
              reminder = reminder,
              onToggleActive = { repository.toggleMedicationReminderActive(reminder.id) },
              onMarkTaken = { repository.markMedicationTaken(reminder.id) },
              onTestAlert = { repository.triggerMedicationAlert(reminder.id) },
              onDelete = { repository.deleteMedicationReminder(reminder.id) }
            )
          }
        }

        if (bedtimeList.isNotEmpty()) {
          item {
            PeriodHeader("🌙 Bedtime Doses", "Night & Sleep Schedule")
          }
          items(bedtimeList, key = { it.id }) { reminder ->
            ReminderCard(
              reminder = reminder,
              onToggleActive = { repository.toggleMedicationReminderActive(reminder.id) },
              onMarkTaken = { repository.markMedicationTaken(reminder.id) },
              onTestAlert = { repository.triggerMedicationAlert(reminder.id) },
              onDelete = { repository.deleteMedicationReminder(reminder.id) }
            )
          }
        }
      }
    }
  }

  // Add Custom Medication Reminder Modal Dialog
  if (showAddDialog) {
    AddReminderDialog(
      onDismiss = { showAddDialog = false },
      onSave = { name, dose, timeSlot, period, freq, instructions ->
        repository.addMedicationReminder(
          prescriptionId = null,
          medicineName = name,
          dosage = dose,
          timeSlot = timeSlot,
          timePeriod = period,
          frequency = freq,
          instructions = instructions
        )
        showAddDialog = false
      }
    )
  }
}

@Composable
fun PeriodHeader(title: String, subtitle: String) {
  Column(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Slate900)
    Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Slate500)
  }
}

@Composable
fun ReminderCard(
  reminder: MedicationReminder,
  onToggleActive: () -> Unit,
  onMarkTaken: () -> Unit,
  onTestAlert: () -> Unit,
  onDelete: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("medication_reminder_${reminder.id}")
      .border(
        width = if (reminder.isTakenToday) 1.5.dp else 0.dp,
        color = if (reminder.isTakenToday) SuccessGreen else Color.Transparent,
        shape = RoundedCornerShape(20.dp)
      ),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (reminder.isTakenToday) SuccessLight.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(if (reminder.isTakenToday) SuccessLight else TealLight),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (reminder.isTakenToday) Icons.Default.CheckCircle else Icons.Default.Medication,
              contentDescription = null,
              tint = if (reminder.isTakenToday) SuccessGreen else TealPrimary,
              modifier = Modifier.size(24.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Text(
              text = reminder.medicineName,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = "${reminder.dosage} • ${reminder.frequency}",
              style = MaterialTheme.typography.bodySmall,
              color = Slate600
            )
          }
        }

        Switch(
          checked = reminder.isActive,
          onCheckedChange = { onToggleActive() },
          colors = SwitchDefaults.colors(checkedThumbColor = TealPrimary, checkedTrackColor = TealLight)
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Time & Instructions Box
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(Slate50)
          .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Schedule, null, tint = TealPrimary, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(reminder.timeSlot, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = Slate900)
        }

        if (reminder.prescriptionId != null) {
          Surface(color = TealLight, shape = RoundedCornerShape(6.dp)) {
            Text("Doctor Rx", color = TealDark, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
      Text(reminder.instructions, style = MaterialTheme.typography.labelSmall, color = Slate600)

      Spacer(modifier = Modifier.height(14.dp))
      HorizontalDivider(color = Slate100)
      Spacer(modifier = Modifier.height(10.dp))

      // Card Bottom Controls
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(onClick = onTestAlert) {
            Icon(Icons.Default.NotificationsNone, "Test Notification", tint = Slate500, modifier = Modifier.size(20.dp))
          }
          IconButton(onClick = onDelete) {
            Icon(Icons.Default.DeleteOutline, "Delete", tint = Slate400, modifier = Modifier.size(20.dp))
          }
        }

        if (reminder.isTakenToday) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Check, null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(reminder.takenAtTime ?: "Logged", color = SuccessGreen, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
          }
        } else {
          Button(
            onClick = onMarkTaken,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
            modifier = Modifier.testTag("mark_taken_btn_${reminder.id}")
          ) {
            Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Mark as Taken", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
fun AddReminderDialog(
  onDismiss: () -> Unit,
  onSave: (String, String, String, String, String, String) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var dose by remember { mutableStateOf("") }
  var timeSlot by remember { mutableStateOf("08:00 AM") }
  var selectedPeriod by remember { mutableStateOf("MORNING") }
  var frequency by remember { mutableStateOf("Once Daily") }
  var instructions by remember { mutableStateOf("Take with water after food") }

  val periods = listOf("MORNING" to "🌅 Morning", "AFTERNOON" to "☀️ Afternoon", "EVENING" to "🌆 Evening", "BEDTIME" to "🌙 Bedtime")

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.AlarmAdd, null, tint = TealPrimary)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Add Medication Reminder")
      }
    },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Medicine Name") },
          modifier = Modifier.fillMaxWidth().testTag("add_reminder_name_input"),
          singleLine = true
        )

        OutlinedTextField(
          value = dose,
          onValueChange = { dose = it },
          label = { Text("Dosage (e.g. 20 mg, 1 tablet)") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        Text("Time of Day", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          periods.forEach { (periodKey, label) ->
            FilterChip(
              selected = selectedPeriod == periodKey,
              onClick = {
                selectedPeriod = periodKey
                timeSlot = when (periodKey) {
                  "MORNING" -> "08:00 AM"
                  "AFTERNOON" -> "01:00 PM"
                  "EVENING" -> "07:00 PM"
                  else -> "09:30 PM"
                }
              },
              label = { Text(label, fontSize = 10.sp) },
              modifier = Modifier.weight(1f)
            )
          }
        }

        OutlinedTextField(
          value = timeSlot,
          onValueChange = { timeSlot = it },
          label = { Text("Alarm Time (e.g. 08:00 AM)") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        OutlinedTextField(
          value = frequency,
          onValueChange = { frequency = it },
          label = { Text("Frequency") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        OutlinedTextField(
          value = instructions,
          onValueChange = { instructions = it },
          label = { Text("Food / Intake Instructions") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank()) {
            onSave(name, dose.ifBlank { "1 dose" }, timeSlot, selectedPeriod, frequency, instructions)
          }
        },
        enabled = name.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
      ) {
        Text("Save Reminder")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
