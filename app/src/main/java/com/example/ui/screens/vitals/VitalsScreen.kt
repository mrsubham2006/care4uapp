package com.example.ui.screens.vitals

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.core.model.*
import com.example.core.repository.CareNexusRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VitalsScreen(
  repository: CareNexusRepository,
  onNavigateBack: () -> Unit,
  onNavigateToCareAi: () -> Unit
) {
  val vitals by repository.vitals.collectAsState()
  var selectedTypeFilter by remember { mutableStateOf<VitalType?>(null) }
  var showLogDialog by remember { mutableStateOf(false) }
  var showAiAnalysisDialog by remember { mutableStateOf(false) }
  var aiAnalysisText by remember { mutableStateOf<String?>(null) }
  var isAiAnalyzing by remember { mutableStateOf(false) }

  val coroutineScope = rememberCoroutineScope()

  val filteredVitals = remember(vitals, selectedTypeFilter) {
    if (selectedTypeFilter == null) vitals else vitals.filter { it.type == selectedTypeFilter }
  }

  val latestBp = vitals.firstOrNull { it.type == VitalType.BLOOD_PRESSURE }
  val latestHr = vitals.firstOrNull { it.type == VitalType.HEART_RATE }
  val latestGlucose = vitals.firstOrNull { it.type == VitalType.BLOOD_GLUCOSE }
  val latestSpo2 = vitals.firstOrNull { it.type == VitalType.SPO2 }
  val latestTemp = vitals.firstOrNull { it.type == VitalType.TEMPERATURE }
  val latestWeight = vitals.firstOrNull { it.type == VitalType.WEIGHT }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Health Vitals & Biometrics",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Slate900
            )
            Text(
              text = "Continuous Health Tracking & AI Insights",
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
              showAiAnalysisDialog = true
              if (aiAnalysisText == null) {
                isAiAnalyzing = true
                coroutineScope.launch {
                  aiAnalysisText = repository.analyzeVitalsWithAi(vitals)
                  isAiAnalyzing = false
                }
              }
            }
          ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = "Analyze with CareAI", tint = TealPrimary)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
      )
    },
    floatingActionButton = {
      ExtendedFloatingActionButton(
        onClick = { showLogDialog = true },
        containerColor = TealPrimary,
        contentColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.testTag("add_vital_fab")
      ) {
        Icon(Icons.Default.Add, contentDescription = "Log Vital")
        Spacer(modifier = Modifier.width(8.dp))
        Text("Log Vital Reading", fontWeight = FontWeight.Bold)
      }
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .background(Slate50)
        .padding(paddingValues),
      contentPadding = PaddingValues(bottom = 90.dp)
    ) {
      // Header Banner with Quick AI Summary Trigger
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = NavyDark)
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(
                Brush.horizontalGradient(
                  colors = listOf(NavyDark, Color(0xFF1E293B), TealDark)
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
                      .size(10.dp)
                      .clip(CircleShape)
                      .background(TealAccent)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "LIVE HEALTH SYNC",
                    style = MaterialTheme.typography.labelSmall,
                    color = TealAccent,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                  )
                }

                Surface(
                  color = NavyCard,
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Text(
                    text = "Optimal Zone",
                    color = TealAccent,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(12.dp))

              Text(
                text = "Patient Biometrics Dashboard",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Track daily vital signs, spot clinical deviations early, and sync logs with your attending physician.",
                style = MaterialTheme.typography.bodySmall,
                color = Slate300
              )

              Spacer(modifier = Modifier.height(14.dp))

              Button(
                onClick = {
                  showAiAnalysisDialog = true
                  if (aiAnalysisText == null) {
                    isAiAnalyzing = true
                    coroutineScope.launch {
                      aiAnalysisText = repository.analyzeVitalsWithAi(vitals)
                      isAiAnalyzing = false
                    }
                  }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
              ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Analyze Vitals with CareAI", fontSize = 13.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      // Quick Vitals Metric Grid (2x3)
      item {
        Text(
          text = "Latest Key Indicators",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = Slate900,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
      }

      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            VitalMetricCard(
              title = "Blood Pressure",
              value = latestBp?.value ?: "120/80",
              unit = "mmHg",
              status = latestBp?.status ?: VitalStatus.NORMAL,
              icon = Icons.Default.Favorite,
              accentColor = Color(0xFFEF4444),
              modifier = Modifier.weight(1f)
            )
            VitalMetricCard(
              title = "Heart Rate",
              value = latestHr?.value ?: "72",
              unit = "BPM",
              status = latestHr?.status ?: VitalStatus.OPTIMAL,
              icon = Icons.Default.MonitorHeart,
              accentColor = Color(0xFFEC4899),
              modifier = Modifier.weight(1f)
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            VitalMetricCard(
              title = "Blood Glucose",
              value = latestGlucose?.value ?: "94",
              unit = "mg/dL",
              status = latestGlucose?.status ?: VitalStatus.NORMAL,
              icon = Icons.Default.WaterDrop,
              accentColor = Color(0xFF0284C7),
              modifier = Modifier.weight(1f)
            )
            VitalMetricCard(
              title = "Blood Oxygen",
              value = latestSpo2?.value ?: "99",
              unit = "% SpO2",
              status = latestSpo2?.status ?: VitalStatus.OPTIMAL,
              icon = Icons.Default.Air,
              accentColor = Color(0xFF0D9488),
              modifier = Modifier.weight(1f)
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            VitalMetricCard(
              title = "Body Temp",
              value = latestTemp?.value ?: "98.4",
              unit = "°F",
              status = latestTemp?.status ?: VitalStatus.NORMAL,
              icon = Icons.Default.DeviceThermostat,
              accentColor = Color(0xFFF59E0B),
              modifier = Modifier.weight(1f)
            )
            VitalMetricCard(
              title = "Weight & BMI",
              value = latestWeight?.value ?: "74.5",
              unit = "kg (BMI 23.4)",
              status = latestWeight?.status ?: VitalStatus.NORMAL,
              icon = Icons.Default.FitnessCenter,
              accentColor = Color(0xFF6366F1),
              modifier = Modifier.weight(1f)
            )
          }
        }
      }

      // Filter Chips
      item {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = "Vital Log History",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = Slate900,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        LazyRow(
          modifier = Modifier.fillMaxWidth(),
          contentPadding = PaddingValues(horizontal = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          item {
            FilterChip(
              selected = selectedTypeFilter == null,
              onClick = { selectedTypeFilter = null },
              label = { Text("All Vitals (${vitals.size})") },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = TealPrimary,
                selectedLabelColor = Color.White
              )
            )
          }

          items(VitalType.values()) { type ->
            FilterChip(
              selected = selectedTypeFilter == type,
              onClick = { selectedTypeFilter = type },
              label = { Text(type.displayName) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = TealPrimary,
                selectedLabelColor = Color.White
              )
            )
          }
        }
      }

      // Vitals List
      if (filteredVitals.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "No recorded readings for this category.",
              style = MaterialTheme.typography.bodyMedium,
              color = Slate500
            )
          }
        }
      } else {
        items(filteredVitals, key = { it.id }) { vital ->
          VitalRecordCard(
            record = vital,
            onDelete = { repository.deleteVitalReading(vital.id) }
          )
        }
      }
    }
  }

  // Add / Log Vital Dialog
  if (showLogDialog) {
    LogVitalDialog(
      onDismiss = { showLogDialog = false },
      onSave = { type, value, primary, secondary, status, notes ->
        repository.logVitalReading(
          type = type,
          value = value,
          primary = primary,
          secondary = secondary,
          status = status,
          notes = notes
        )
        showLogDialog = false
      }
    )
  }

  // AI Analysis Modal
  if (showAiAnalysisDialog) {
    Dialog(onDismissRequest = { showAiAnalysisDialog = false }) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
      ) {
        Column(
          modifier = Modifier.padding(20.dp)
        ) {
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
                  .background(TealLight),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = TealPrimary)
              }
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = "CareAI Vitals Assessment",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Slate900
              )
            }

            IconButton(onClick = { showAiAnalysisDialog = false }) {
              Icon(Icons.Default.Close, contentDescription = "Close")
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          if (isAiAnalyzing) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              CircularProgressIndicator(color = TealPrimary)
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "CareAI is synthesizing your vital trends...",
                style = MaterialTheme.typography.bodySmall,
                color = Slate600
              )
            }
          } else {
            Text(
              text = aiAnalysisText ?: "Vitals are within healthy cardiovascular baseline.",
              style = MaterialTheme.typography.bodySmall,
              color = Slate700,
              lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
              onClick = {
                showAiAnalysisDialog = false
                onNavigateToCareAi()
              },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
              Text("Ask Follow-up in CareAI Chat", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}

@Composable
fun VitalMetricCard(
  title: String,
  value: String,
  unit: String,
  status: VitalStatus,
  icon: ImageVector,
  accentColor: Color,
  modifier: Modifier = Modifier
) {
  val statusColor = when (status) {
    VitalStatus.OPTIMAL -> TealPrimary
    VitalStatus.NORMAL -> Color(0xFF10B981)
    VitalStatus.ELEVATED -> WarningAmber
    VitalStatus.CRITICAL -> ErrorRed
  }

  Card(
    modifier = modifier,
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(accentColor.copy(alpha = 0.12f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
        }

        Surface(
          color = statusColor.copy(alpha = 0.12f),
          shape = RoundedCornerShape(6.dp)
        ) {
          Text(
            text = status.name,
            color = statusColor,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = Slate500
      )

      Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Text(
          text = value,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = Slate900
        )
        Text(
          text = unit,
          style = MaterialTheme.typography.labelSmall,
          color = Slate500,
          modifier = Modifier.padding(bottom = 2.dp)
        )
      }
    }
  }
}

@Composable
fun VitalRecordCard(
  record: HealthVitalRecord,
  onDelete: () -> Unit
) {
  val statusColor = when (record.status) {
    VitalStatus.OPTIMAL -> TealPrimary
    VitalStatus.NORMAL -> Color(0xFF10B981)
    VitalStatus.ELEVATED -> WarningAmber
    VitalStatus.CRITICAL -> ErrorRed
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 5.dp),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(TealLight),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            when (record.type) {
              VitalType.BLOOD_PRESSURE -> Icons.Default.Favorite
              VitalType.HEART_RATE -> Icons.Default.MonitorHeart
              VitalType.BLOOD_GLUCOSE -> Icons.Default.WaterDrop
              VitalType.SPO2 -> Icons.Default.Air
              VitalType.TEMPERATURE -> Icons.Default.DeviceThermostat
              VitalType.WEIGHT -> Icons.Default.FitnessCenter
            },
            contentDescription = null,
            tint = TealPrimary,
            modifier = Modifier.size(22.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = record.type.displayName,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = Slate900
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              color = statusColor.copy(alpha = 0.12f),
              shape = RoundedCornerShape(4.dp)
            ) {
              Text(
                text = record.status.name,
                color = statusColor,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
              )
            }
          }

          Text(
            text = "${record.recordedAt} • ${record.recordedTime} • ${record.source}",
            style = MaterialTheme.typography.bodySmall,
            color = Slate500
          )

          if (record.notes.isNotBlank()) {
            Text(
              text = record.notes,
              style = MaterialTheme.typography.labelSmall,
              color = Slate600
            )
          }
        }
      }

      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = "${record.value} ${record.type.unit}",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = TealDark
        )
        Text(
          text = "Ref: ${record.type.normalRange}",
          style = MaterialTheme.typography.labelSmall,
          color = Slate400,
          fontSize = 10.sp
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogVitalDialog(
  onDismiss: () -> Unit,
  onSave: (VitalType, String, Float, Float?, VitalStatus, String) -> Unit
) {
  var selectedType by remember { mutableStateOf(VitalType.BLOOD_PRESSURE) }
  var primaryInput by remember { mutableStateOf("") }
  var secondaryInput by remember { mutableStateOf("") }
  var notesInput by remember { mutableStateOf("") }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
      Column(
        modifier = Modifier.padding(20.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Log Vital Reading",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Slate900
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Type selection dropdown / chips
        Text(
          text = "Select Biometric Type",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.SemiBold,
          color = Slate700
        )

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          items(VitalType.values()) { type ->
            FilterChip(
              selected = selectedType == type,
              onClick = {
                selectedType = type
                primaryInput = ""
                secondaryInput = ""
              },
              label = { Text(type.displayName, fontSize = 12.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = TealPrimary,
                selectedLabelColor = Color.White
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (selectedType == VitalType.BLOOD_PRESSURE) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedTextField(
              value = primaryInput,
              onValueChange = { primaryInput = it },
              label = { Text("Systolic (mmHg)") },
              placeholder = { Text("e.g. 120") },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
              value = secondaryInput,
              onValueChange = { secondaryInput = it },
              label = { Text("Diastolic (mmHg)") },
              placeholder = { Text("e.g. 80") },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(12.dp)
            )
          }
        } else {
          OutlinedTextField(
            value = primaryInput,
            onValueChange = { primaryInput = it },
            label = { Text("Value (${selectedType.unit})") },
            placeholder = { Text("Target: ${selectedType.normalRange}") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = notesInput,
          onValueChange = { notesInput = it },
          label = { Text("Notes / Context (Optional)") },
          placeholder = { Text("e.g. Fasting, after morning workout") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
          onClick = {
            val pVal = primaryInput.toFloatOrNull() ?: 0f
            val sVal = secondaryInput.toFloatOrNull()
            val finalVal = if (selectedType == VitalType.BLOOD_PRESSURE) {
              "${primaryInput.ifBlank { "120" }}/${secondaryInput.ifBlank { "80" }}"
            } else {
              primaryInput.ifBlank { "95" }
            }

            val status = when (selectedType) {
              VitalType.BLOOD_PRESSURE -> if (pVal > 140 || (sVal ?: 0f) > 90) VitalStatus.ELEVATED else VitalStatus.NORMAL
              VitalType.HEART_RATE -> if (pVal in 60f..85f) VitalStatus.OPTIMAL else VitalStatus.NORMAL
              VitalType.BLOOD_GLUCOSE -> if (pVal > 125) VitalStatus.ELEVATED else VitalStatus.NORMAL
              VitalType.SPO2 -> if (pVal < 94) VitalStatus.ELEVATED else VitalStatus.OPTIMAL
              VitalType.TEMPERATURE -> if (pVal > 99.5) VitalStatus.ELEVATED else VitalStatus.NORMAL
              VitalType.WEIGHT -> VitalStatus.NORMAL
            }

            onSave(selectedType, finalVal, pVal, sVal, status, notesInput)
          },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
        ) {
          Text("Save Biometric Entry", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
