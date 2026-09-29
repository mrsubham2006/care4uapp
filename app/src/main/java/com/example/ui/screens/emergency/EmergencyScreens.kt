package com.example.ui.screens.emergency

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.repository.CareNexusRepository
import com.example.ui.theme.*

@Composable
fun EmergencyScreen(
  repository: CareNexusRepository,
  onBackClick: () -> Unit
) {
  val context = LocalContext.current
  val patient by repository.currentPatient.collectAsState()
  var isSharingLocation by remember { mutableStateOf(false) }
  var locationSharedMessage by remember { mutableStateOf<String?>(null) }
  var isAmbulanceRequested by remember { mutableStateOf(false) }
  var showFirstAidDialog by remember { mutableStateOf<String?>(null) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .background(MaterialTheme.colorScheme.background)
      .padding(16.dp)
      .padding(bottom = 80.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // High-Priority Emergency Top Banner
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = EmergencyColor),
      elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.2f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.Emergency, null, tint = Color.White, modifier = Modifier.size(44.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "EMERGENCY RESPONSE CENTER",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = Color.White,
          letterSpacing = 1.sp
        )

        Text(
          text = "CareNexus 24/7 Rapid Triage • GPS Dispatch",
          style = MaterialTheme.typography.bodySmall,
          color = Color.White.copy(alpha = 0.9f)
        )
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // 1-TAP EMERGENCY CALLING ACTIONS
    Text(
      text = "Direct Emergency Helplines (1-Tap)",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.align(Alignment.Start)
    )

    Spacer(modifier = Modifier.height(10.dp))

    // 911 Call Button
    Button(
      onClick = {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:911"))
        context.startActivity(intent)
      },
      modifier = Modifier
        .fillMaxWidth()
        .height(56.dp)
        .testTag("emergency_call_911_button"),
      shape = RoundedCornerShape(16.dp),
      colors = ButtonDefaults.buttonColors(containerColor = EmergencyColor)
    ) {
      Icon(Icons.Default.Call, null, modifier = Modifier.size(22.dp))
      Spacer(modifier = Modifier.width(10.dp))
      Text("CALL EMERGENCY (911)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }

    Spacer(modifier = Modifier.height(10.dp))

    // CareNexus 24/7 ER Hotline
    Button(
      onClick = {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:18005559111"))
        context.startActivity(intent)
      },
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
        .testTag("emergency_call_hospital_btn"),
      shape = RoundedCornerShape(16.dp),
      colors = ButtonDefaults.buttonColors(containerColor = BlueDeep)
    ) {
      Icon(Icons.Default.LocalHospital, null, modifier = Modifier.size(20.dp))
      Spacer(modifier = Modifier.width(10.dp))
      Text("Call CareNexus ER Helpline (+1 800-555-9111)", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Call Emergency Contact
    OutlinedButton(
      onClick = {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${patient.emergencyContactPhone.filter { it.isDigit() }}"))
        context.startActivity(intent)
      },
      modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
        .testTag("emergency_call_contact_btn"),
      shape = RoundedCornerShape(16.dp),
      colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate900),
      border = ButtonDefaults.outlinedButtonBorder(enabled = true)
    ) {
      Icon(Icons.Default.ContactPhone, null, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(8.dp))
      Text("Call Contact: ${patient.emergencyContactName} (${patient.emergencyRelationship})", fontWeight = FontWeight.SemiBold)
    }

    Spacer(modifier = Modifier.height(20.dp))

    // GPS LOCATION TRIAGE & AMBULANCE DISPATCH
    Card(
      modifier = Modifier.fillMaxWidth(),
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
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.MyLocation, null, tint = TealPrimary, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("GPS Triage Location", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          }

          Surface(color = TealLight, shape = RoundedCornerShape(8.dp)) {
            Text("GPS Active", color = TealDark, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
          }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Coordinates: 40.7128° N, 74.0060° W", style = MaterialTheme.typography.bodySmall, color = Slate700)
        Text("Nearest CareNexus ER: 1.2 km away (~4 mins drive)", style = MaterialTheme.typography.bodySmall, color = Slate500)

        Spacer(modifier = Modifier.height(14.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Button(
            onClick = {
              isSharingLocation = true
              locationSharedMessage = "GPS coordinates broadcasted to CareNexus ER Dispatch (ID: DISPATCH-772)."
            },
            modifier = Modifier.weight(1f).testTag("share_emergency_location_btn"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
          ) {
            Icon(Icons.Default.ShareLocation, null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Share GPS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }

          Button(
            onClick = {
              isAmbulanceRequested = true
            },
            modifier = Modifier.weight(1.2f).testTag("request_ambulance_btn"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (isAmbulanceRequested) SuccessGreen else EmergencyColor)
          ) {
            Icon(Icons.Default.DirectionsCar, null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isAmbulanceRequested) "Ambulance En Route" else "Request Ambulance", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }

        if (locationSharedMessage != null) {
          Spacer(modifier = Modifier.height(10.dp))
          Surface(
            color = SuccessLight,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(locationSharedMessage ?: "", color = SuccessGreen, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
          }
        }

        if (isAmbulanceRequested) {
          Spacer(modifier = Modifier.height(10.dp))
          Surface(
            color = SuccessLight,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Ambulance #AMB-104 Dispatched • ETA: 6-8 Mins", color = SuccessGreen, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // CRITICAL USER HEALTH INFO CARD FOR PARAMEDICS
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Paramedic Critical Health Card",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
      Surface(color = ErrorLight, shape = RoundedCornerShape(8.dp)) {
        Text("First Responder Access", color = ErrorRed, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // High-Contrast Paramedic Health HUD
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("paramedic_health_card"),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        // Patient Header & Blood Type
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(patient.fullName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("DOB: ${patient.dateOfBirth} (Age 30) • Male", style = MaterialTheme.typography.bodySmall, color = Slate600)
            Text("Patient ID: ${patient.patientId}", style = MaterialTheme.typography.labelSmall, color = Slate400)
          }

          Surface(
            color = EmergencyColor.copy(alpha = 0.12f),
            shape = RoundedCornerShape(14.dp)
          ) {
            Column(
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text("BLOOD", style = MaterialTheme.typography.labelSmall, color = EmergencyColor, fontWeight = FontWeight.Bold)
              Text(patient.bloodGroup, style = MaterialTheme.typography.headlineMedium, color = EmergencyColor, fontWeight = FontWeight.Bold)
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = Slate100)
        Spacer(modifier = Modifier.height(12.dp))

        // Known Allergies (Severe Warning Highlight)
        Surface(
          color = ErrorLight,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Warning, null, tint = ErrorRed, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text("SEVERE ALLERGIES (ANAPHYLAXIS RISK)", style = MaterialTheme.typography.labelSmall, color = ErrorRed, fontWeight = FontWeight.Bold)
              Text(patient.allergies, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Slate900)
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Chronic Conditions & Recorded Meds
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Slate50)
            .padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Column {
            Text("CHRONIC CONDITIONS", style = MaterialTheme.typography.labelSmall, color = Slate500, fontWeight = FontWeight.Bold)
            Text("Mild Hyperlipidemia, Seasonal Airborne Allergen Sensitization", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = Slate800)
          }

          Column {
            Text("ACTIVE MEDICATIONS", style = MaterialTheme.typography.labelSmall, color = Slate500, fontWeight = FontWeight.Bold)
            Text("Atorvastatin 20mg (Daily Bedtime), CoQ10 100mg", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = Slate800)
          }

          Column {
            Text("ORGAN DONOR STATUS", style = MaterialTheme.typography.labelSmall, color = Slate500, fontWeight = FontWeight.Bold)
            Text("Registered Organ & Tissue Donor (YES)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = SuccessGreen)
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Emergency Contact Details
        Text("EMERGENCY NEXT OF KIN", style = MaterialTheme.typography.labelSmall, color = Slate500, fontWeight = FontWeight.Bold)
        Text("${patient.emergencyContactName} (${patient.emergencyRelationship})", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        Text(patient.emergencyContactPhone, style = MaterialTheme.typography.bodySmall, color = Slate600)
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // FIRST AID & EMERGENCY ACTION GUIDES
    Text(
      text = "Emergency First Aid Protocols",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.align(Alignment.Start)
    )

    Spacer(modifier = Modifier.height(8.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      FirstAidCard(
        title = "CPR Guide",
        subtitle = "Chest compressions",
        icon = Icons.Default.Favorite,
        modifier = Modifier.weight(1f),
        onClick = { showFirstAidDialog = "CPR" }
      )
      FirstAidCard(
        title = "Chest Pain",
        subtitle = "Cardiac triage",
        icon = Icons.Default.HealthAndSafety,
        modifier = Modifier.weight(1f),
        onClick = { showFirstAidDialog = "CHEST_PAIN" }
      )
    }
  }

  if (showFirstAidDialog != null) {
    val topic = showFirstAidDialog!!
    AlertDialog(
      onDismissRequest = { showFirstAidDialog = null },
      title = { Text(if (topic == "CPR") "Hands-Only CPR Protocol" else "Acute Chest Pain Protocol") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          if (topic == "CPR") {
            Text("1. Call 911 immediately.", fontWeight = FontWeight.Bold)
            Text("2. Place hands in the center of the chest.")
            Text("3. Push hard and fast at 100 to 120 beats per minute (to the beat of 'Stayin' Alive').")
            Text("4. Allow chest to rise completely between compressions.")
          } else {
            Text("1. Have the person sit down, rest, and try to stay calm.", fontWeight = FontWeight.Bold)
            Text("2. Loosen any tight clothing around the neck and chest.")
            Text("3. Ask if they take prescribed chest pain medicine (such as nitroglycerin).")
            Text("4. If pain lasts more than 3 minutes, dial 911 or proceed to the ER immediately.")
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showFirstAidDialog = null }) {
          Text("Understood")
        }
      }
    )
  }
}

@Composable
fun FirstAidCard(
  title: String,
  subtitle: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Card(
    modifier = modifier.clickable { onClick() },
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(EmergencyColor.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(icon, null, tint = EmergencyColor, modifier = Modifier.size(20.dp))
      }
      Spacer(modifier = Modifier.width(8.dp))
      Column {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Slate500)
      }
    }
  }
}
