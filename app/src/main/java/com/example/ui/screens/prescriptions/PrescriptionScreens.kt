package com.example.ui.screens.prescriptions

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.PharmacyLocation
import com.example.core.model.PharmacyOrder
import com.example.core.model.PharmacyOrderStatus
import com.example.core.model.Prescription
import com.example.core.repository.CareNexusRepository
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.QRCodeDisplayBox
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun PrescriptionsScreen(
  repository: CareNexusRepository,
  onNavigateToPharmacy: () -> Unit,
  onNavigateToReminders: () -> Unit = {}
) {
  val prescriptions by repository.prescriptions.collectAsState()
  val reminders by repository.medicationReminders.collectAsState()
  var selectedRxForAi by remember { mutableStateOf<Prescription?>(null) }
  var aiExplanationText by remember { mutableStateOf<String?>(null) }

  val takenCount = reminders.count { it.isTakenToday }
  val totalCount = reminders.size

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(bottom = 80.dp)
  ) {
    if (prescriptions.isEmpty()) {
      EmptyStateCard(
        icon = Icons.Default.Medication,
        title = "No Active Prescriptions",
        subtitle = "Doctor-issued medical prescriptions and dosages will be displayed here."
      )
    } else {
      LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Doctor E-Prescriptions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text("Verified physician electronic medication orders", style = MaterialTheme.typography.labelSmall, color = Slate500)
            }

            Button(
              onClick = onNavigateToPharmacy,
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
              Icon(Icons.Default.LocalPharmacy, null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Pharmacy Hub", fontSize = 12.sp)
            }
          }
        }

        // Daily Medication Reminders Quick Pill Card
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onNavigateToReminders() }
              .testTag("medication_reminders_banner"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = TealLight)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                  modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(TealPrimary),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.Alarm, null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text("Daily Medication Alarms", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Slate900)
                  Text("$takenCount of $totalCount pills logged today • Daily alerts active", style = MaterialTheme.typography.bodySmall, color = Slate700)
                }
              }

              Icon(Icons.Default.ChevronRight, null, tint = TealDark)
            }
          }
        }

        items(prescriptions, key = { it.id }) { rx ->
          PrescriptionCard(
            prescription = rx,
            onSendToPharmacy = { repository.sendPrescriptionToPharmacy(rx.id) },
            onExplainAi = {
              selectedRxForAi = rx
              val medList = rx.items.joinToString("\n") { "• ${it.medicineName} (${it.dosage}) - ${it.frequency} for ${it.duration}: ${it.instructions}" }
              aiExplanationText = """
                💊 **CareAI Prescription Guidance**
                
                • **Diagnosis Addressed:** ${rx.diagnosis}
                • **Attending Doctor:** ${rx.doctorName} (${rx.department})
                • **Physician Notes:** ${rx.notes}
                
                • **Medication Schedule & Instructions:**
                $medList
                
                • **Patient Safety & Hydration:**
                - Take prescribed dosages consistently with a full glass of water.
                - Complete the full course of therapy.
                - If you experience unexpected discomfort, contact ${rx.doctorName}.
                
                ⚕️ *Disclaimer: Always adhere strictly to your physician's written instructions. Do not alter dosage without consulting your doctor.*
              """.trimIndent()
            }
          )
        }
      }
    }
  }

  if (selectedRxForAi != null && aiExplanationText != null) {
    AlertDialog(
      onDismissRequest = { selectedRxForAi = null },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.AutoAwesome, null, tint = TealPrimary)
          Spacer(modifier = Modifier.width(8.dp))
          Text("CareAI Prescription Explainer")
        }
      },
      text = {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
          Text(aiExplanationText ?: "", style = MaterialTheme.typography.bodyMedium, color = Slate800, lineHeight = 22.sp)
        }
      },
      confirmButton = {
        TextButton(onClick = { selectedRxForAi = null }) {
          Text("Done")
        }
      }
    )
  }
}

@Composable
fun PrescriptionCard(
  prescription: Prescription,
  onSendToPharmacy: () -> Unit,
  onExplainAi: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth().testTag("prescription_card_${prescription.id}"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(color = TealLight, shape = RoundedCornerShape(10.dp)) {
          Text(
            text = prescription.id,
            color = TealDark,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
        Text(prescription.date, style = MaterialTheme.typography.bodySmall, color = Slate500, fontWeight = FontWeight.SemiBold)
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = prescription.diagnosis,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = Slate900
      )

      Text(
        text = "Prescribed by ${prescription.doctorName} • ${prescription.department}",
        style = MaterialTheme.typography.bodySmall,
        color = TealDark,
        fontWeight = FontWeight.SemiBold
      )

      Spacer(modifier = Modifier.height(14.dp))

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(Slate50)
          .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        prescription.items.forEach { item ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(item.medicineName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
              Text("${item.dosage} • ${item.frequency} • ${item.duration}", style = MaterialTheme.typography.bodySmall, color = Slate600)
              Text(item.instructions, style = MaterialTheme.typography.labelSmall, color = Slate500)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedButton(
          onClick = onExplainAi,
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(10.dp)
        ) {
          Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("AI Explainer", fontSize = 12.sp)
        }

        Button(
          onClick = onSendToPharmacy,
          modifier = Modifier.weight(1.2f).testTag("send_pharmacy_btn_${prescription.id}"),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = if (prescription.isSentToPharmacy) SuccessGreen else TealPrimary)
        ) {
          Icon(Icons.Default.LocalPharmacy, null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(if (prescription.isSentToPharmacy) "Order Active" else "Send to Pharmacy", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
fun PharmacyOrdersScreen(
  repository: CareNexusRepository,
  onBackClick: () -> Unit
) {
  val orders by repository.pharmacyOrders.collectAsState()
  val locations = repository.pharmacyLocations

  var selectedTab by remember { mutableIntStateOf(0) }
  val tabs = listOf("Order Tracker", "Snap / Upload Rx", "Nearby Pharmacies")

  // Upload Prescriptions State
  var selectedLocationId by remember { mutableStateOf(locations.first().id) }
  var hasCapturedPhoto by remember { mutableStateOf(false) }
  var prescriptionNotes by remember { mutableStateOf("") }
  var isFlashOn by remember { mutableStateOf(false) }
  var orderSubmittedSuccess by remember { mutableStateOf<PharmacyOrder?>(null) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(bottom = 80.dp)
  ) {
    // Top Tab Selector
    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = MaterialTheme.colorScheme.surface,
      contentColor = TealPrimary
    ) {
      tabs.forEachIndexed { index, title ->
        Tab(
          selected = selectedTab == index,
          onClick = { selectedTab = index },
          text = { Text(title, fontSize = 12.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
        )
      }
    }

    when (selectedTab) {
      0 -> {
        // TAB 1: Live Order Pickup Tracker
        if (orders.isEmpty()) {
          EmptyStateCard(
            icon = Icons.Default.LocalPharmacy,
            title = "No Active Pharmacy Orders",
            subtitle = "Upload an e-prescription or forward a doctor prescription to initiate pickup packaging.",
            actionText = "Upload Prescription Photo",
            onActionClick = { selectedTab = 1 }
          )
        } else {
          LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            item {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Active Pharmacy Pickup Orders (${orders.size})",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
                Surface(color = TealLight, shape = RoundedCornerShape(8.dp)) {
                  Text("Live Tracking", color = TealDark, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
              }
            }

            items(orders, key = { it.id }) { order ->
              LivePharmacyOrderTrackerCard(
                order = order,
                onAdvanceStep = { repository.advancePharmacyOrderStatus(order.id) }
              )
            }
          }
        }
      }

      1 -> {
        // TAB 2: Upload / Snap E-Prescription Flow
        if (orderSubmittedSuccess != null) {
          val submitted = orderSubmittedSuccess!!
          Column(
            modifier = Modifier
              .fillMaxSize()
              .verticalScroll(rememberScrollState())
              .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(SuccessLight),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(42.dp))
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text("Prescription Submitted!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = SuccessGreen)
            Text("Order #${submitted.id} has been dispatched to ${submitted.pharmacyName}.", style = MaterialTheme.typography.bodySmall, color = Slate600, textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(20.dp))

            QRCodeDisplayBox(
              qrPayload = "CARENEXUS://PHARM/${submitted.id}/TOKEN-${submitted.pickupTokenNumber}",
              title = "Pickup Token: ${submitted.pickupTokenNumber}",
              subtitle = "Present at ${submitted.pickupCounter}"
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
              onClick = {
                orderSubmittedSuccess = null
                selectedTab = 0 // Go to live tracker
              },
              modifier = Modifier.fillMaxWidth().height(50.dp),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
              Text("Track Pickup Status", fontWeight = FontWeight.Bold)
            }
          }
        } else {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .verticalScroll(rememberScrollState())
              .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text("Snap or Upload E-Prescription", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Take a clear photo of your prescription or select an image file", style = MaterialTheme.typography.bodySmall, color = Slate500)

            Spacer(modifier = Modifier.height(16.dp))

            if (!hasCapturedPhoto) {
              // Camera Capture Viewfinder
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(220.dp)
                  .clip(RoundedCornerShape(20.dp))
                  .background(NavyDark),
                contentAlignment = Alignment.Center
              ) {
                // Framing Box
                Box(
                  modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .fillMaxHeight(0.75f)
                    .border(2.dp, if (isFlashOn) TealAccent else Color.White.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .padding(12.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CameraAlt, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Align Doctor Prescription Within Frame", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall)
                  }
                }

                // Flash toggle
                IconButton(
                  onClick = { isFlashOn = !isFlashOn },
                  modifier = Modifier.align(Alignment.TopEnd).padding(12.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.4f))
                ) {
                  Icon(if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff, "Flash", tint = if (isFlashOn) WarningAmber else Color.White)
                }

                // Shutter Button
                Button(
                  onClick = { hasCapturedPhoto = true },
                  modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp).testTag("snap_prescription_btn"),
                  shape = RoundedCornerShape(12.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                  Icon(Icons.Default.PhotoCamera, null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Snap Photo", fontWeight = FontWeight.Bold)
                }
              }
            } else {
              // Captured Preview Card
              Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
              ) {
                Column(modifier = Modifier.padding(16.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                      Spacer(modifier = Modifier.width(6.dp))
                      Text("Prescription Photo Captured", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = { hasCapturedPhoto = false }) {
                      Text("Retake", color = ErrorRed)
                    }
                  }

                  Spacer(modifier = Modifier.height(8.dp))

                  Box(
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(110.dp)
                      .clip(RoundedCornerShape(12.dp))
                      .background(Slate100),
                    contentAlignment = Alignment.Center
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(Icons.Default.Description, null, tint = TealPrimary, modifier = Modifier.size(32.dp))
                      Spacer(modifier = Modifier.width(10.dp))
                      Column {
                        Text("Scanned_Prescription_Photo.jpg", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text("Size: 1.8 MB • Resolution: 1080p OK", style = MaterialTheme.typography.labelSmall, color = Slate500)
                      }
                    }
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Select Nearby Hospital Pharmacy
            Text(
              text = "Select Nearby Hospital Pharmacy",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            locations.forEach { location ->
              val isSelected = location.id == selectedLocationId
              Card(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp)
                  .clickable { selectedLocationId = location.id }
                  .border(1.5.dp, if (isSelected) TealPrimary else Slate200, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (isSelected) TealLight else MaterialTheme.colorScheme.surface)
              ) {
                Row(
                  modifier = Modifier.padding(14.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  RadioButton(
                    selected = isSelected,
                    onClick = { selectedLocationId = location.id },
                    colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(location.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("${location.floor} • ${location.counterName}", style = MaterialTheme.typography.bodySmall, color = Slate600)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(Icons.Default.HourglassBottom, null, tint = WarningAmber, modifier = Modifier.size(14.dp))
                      Spacer(modifier = Modifier.width(4.dp))
                      Text("Est. Wait: ${location.currentWaitMinutes} mins • ${location.distanceMeters}m away", style = MaterialTheme.typography.labelSmall, color = Slate600)
                    }
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
              value = prescriptionNotes,
              onValueChange = { prescriptionNotes = it },
              label = { Text("Special Patient Notes / Generic Preference") },
              modifier = Modifier.fillMaxWidth().testTag("prescription_notes_input"),
              shape = RoundedCornerShape(12.dp),
              minLines = 2
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
              onClick = {
                val newOrder = repository.createPharmacyOrderFromUpload(
                  pharmacyLocationId = selectedLocationId,
                  prescriptionPhotoUri = "content://carenexus/rx/photo_${System.currentTimeMillis()}.jpg",
                  medicineNotes = prescriptionNotes
                )
                orderSubmittedSuccess = newOrder
              },
              modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("submit_prescription_order_btn"),
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
              Icon(Icons.AutoMirrored.Filled.Send, null)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Submit Order to Pharmacy", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
          }
        }
      }

      2 -> {
        // TAB 3: Nearby Hospital Pharmacies Directory
        LazyColumn(
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          item {
            Text(
              text = "Hospital Dispensaries & Pickup Counters",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
          }

          items(locations, key = { it.id }) { loc ->
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(18.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(loc.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                  if (loc.isOpen24Hours) {
                    Surface(color = TealLight, shape = RoundedCornerShape(8.dp)) {
                      Text("24/7", color = TealDark, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                  }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Building: ${loc.building} (${loc.floor})", style = MaterialTheme.typography.bodySmall, color = Slate700)
                Text("Dispensing Counter: ${loc.counterName}", style = MaterialTheme.typography.bodySmall, color = TealDark, fontWeight = FontWeight.SemiBold)
                Text("Hours: ${loc.operatingHours}", style = MaterialTheme.typography.bodySmall, color = Slate500)
                Text("Direct Contact: ${loc.contactPhone}", style = MaterialTheme.typography.bodySmall, color = Slate500)

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Slate100)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.HourglassBottom, null, tint = WarningAmber, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${loc.currentWaitMinutes} mins wait", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Slate800)
                  }

                  Button(
                    onClick = {
                      selectedLocationId = loc.id
                      selectedTab = 1 // Go to upload prescription tab
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                  ) {
                    Text("Select for Pickup", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun LivePharmacyOrderTrackerCard(
  order: PharmacyOrder,
  onAdvanceStep: () -> Unit
) {
  val stepIndex = when (order.status) {
    PharmacyOrderStatus.PENDING -> 1
    PharmacyOrderStatus.VERIFIED -> 2
    PharmacyOrderStatus.PREPARING -> 3
    PharmacyOrderStatus.READY -> 4
    PharmacyOrderStatus.COLLECTED -> 5
    PharmacyOrderStatus.CANCELLED -> 0
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("pharmacy_order_tracker_${order.id}"),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      // Header with Token and Status
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
            text = "Pickup Token ${order.pickupTokenNumber}",
            color = TealDark,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
          )
        }

        StatusBadge(status = order.status.name)
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "Order #${order.id}",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = Slate900
      )

      Text(
        text = "${order.pharmacyName} • ${order.orderDate} at ${order.orderTime}",
        style = MaterialTheme.typography.bodySmall,
        color = Slate500
      )

      Spacer(modifier = Modifier.height(16.dp))

      // 5-Step Visual Progress Stepper
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(Slate50)
          .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text("Live Pharmacy Fulfillment Pipeline", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TealDark)

        val steps = listOf(
          Pair("Order Received & Logged", stepIndex >= 1),
          Pair("Pharmacist Clinical Verification", stepIndex >= 2),
          Pair("Medication Packaging & Compounding", stepIndex >= 3),
          Pair("Ready for Counter Pickup", stepIndex >= 4),
          Pair("Collected by Patient", stepIndex >= 5)
        )

        steps.forEachIndexed { idx, (label, isCompleted) ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (isCompleted) SuccessGreen else Slate300),
              contentAlignment = Alignment.Center
            ) {
              if (isCompleted) {
                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
              } else {
                Text("${idx + 1}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = label,
              style = MaterialTheme.typography.bodySmall,
              fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Normal,
              color = if (isCompleted) Slate900 else Slate400
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Pickup Counter Details
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(TealLight)
          .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(Icons.Default.Place, null, tint = TealPrimary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text("Pickup Location", style = MaterialTheme.typography.labelSmall, color = TealDark, fontWeight = FontWeight.Bold)
          Text(order.pickupCounter, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Slate900)
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Medicine items list
      order.medicines.forEach { med ->
        Row(
          modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(med.name, style = MaterialTheme.typography.bodySmall, color = Slate700)
          Text("$${med.unitPrice}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
      HorizontalDivider(color = Slate100)
      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text("Total Bill", style = MaterialTheme.typography.labelSmall, color = Slate500)
          Text("$${order.totalPrice}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TealDark)
        }

        // Live Dispensing Simulation Action
        if (order.status != PharmacyOrderStatus.COLLECTED) {
          Button(
            onClick = onAdvanceStep,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
            modifier = Modifier.testTag("advance_order_status_btn_${order.id}")
          ) {
            Icon(Icons.Default.FastForward, null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              when (order.status) {
                PharmacyOrderStatus.PENDING -> "Verify Order"
                PharmacyOrderStatus.VERIFIED -> "Start Packaging"
                PharmacyOrderStatus.PREPARING -> "Mark Ready"
                PharmacyOrderStatus.READY -> "Mark Collected"
                else -> "Next Step"
              },
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}
