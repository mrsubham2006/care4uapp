package com.example.ui.screens.profile

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.PatientProfile
import com.example.core.repository.CareNexusRepository
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
  repository: CareNexusRepository,
  onNavigateToRoute: (String) -> Unit,
  onLogout: () -> Unit
) {
  val patient by repository.currentPatient.collectAsState()
  var showEditProfileDialog by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .background(MaterialTheme.colorScheme.background)
      .padding(16.dp)
      .padding(bottom = 80.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Profile Header Avatar Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(TealPrimary),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = patient.fullName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = patient.fullName,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )

        Text(
          text = "Patient ID: ${patient.patientId} • ${patient.role}",
          style = MaterialTheme.typography.bodySmall,
          color = Slate500
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Surface(color = TealLight, shape = RoundedCornerShape(10.dp)) {
            Text(
              text = "Blood: ${patient.bloodGroup}",
              color = TealDark,
              fontWeight = FontWeight.Bold,
              style = MaterialTheme.typography.labelMedium,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
          }

          Surface(color = Slate100, shape = RoundedCornerShape(10.dp)) {
            Text(
              text = "DOB: ${patient.dateOfBirth}",
              color = Slate700,
              fontWeight = FontWeight.SemiBold,
              style = MaterialTheme.typography.labelMedium,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Profile Actions List
    Text(
      text = "Patient Account",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.align(Alignment.Start)
    )

    Spacer(modifier = Modifier.height(8.dp))

    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column {
        ProfileMenuItem(icon = Icons.Default.Edit, title = "Edit Patient Information", subtitle = "Update contact, address, allergies", onClick = { showEditProfileDialog = true })
        HorizontalDivider(color = Slate100)
        ProfileMenuItem(icon = Icons.Default.People, title = "Family Members", subtitle = "Manage dependents and linked profiles", onClick = { onNavigateToRoute("family") })
        HorizontalDivider(color = Slate100)
        ProfileMenuItem(icon = Icons.Default.Emergency, title = "Emergency Contacts", subtitle = "${patient.emergencyContactName} (${patient.emergencyContactPhone})", onClick = { onNavigateToRoute("emergency") })
        HorizontalDivider(color = Slate100)
        ProfileMenuItem(icon = Icons.Default.Security, title = "Privacy & Security Audits", subtitle = "Role-based encryption active", onClick = { onNavigateToRoute("settings") })
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text(
      text = "App Settings",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.align(Alignment.Start)
    )

    Spacer(modifier = Modifier.height(8.dp))

    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column {
        ProfileMenuItem(icon = Icons.Default.Notifications, title = "Push Notification Preferences", subtitle = "OPD Queue, Lab alerts, Pharmacy pickup", onClick = { onNavigateToRoute("settings") })
        HorizontalDivider(color = Slate100)
        ProfileMenuItem(icon = Icons.Default.LocalHospital, title = "Hospital Information & Floor Map", subtitle = "Visiting hours, OPD schedules, contacts", onClick = { onNavigateToRoute("hospital") })
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Logout Button
    OutlinedButton(
      onClick = {
        repository.logout()
        onLogout()
      },
      modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
        .testTag("patient_logout_button"),
      shape = RoundedCornerShape(14.dp),
      colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
      border = ButtonDefaults.outlinedButtonBorder(enabled = true)
    ) {
      Icon(Icons.AutoMirrored.Filled.Logout, null, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(8.dp))
      Text("Sign Out Securely", fontWeight = FontWeight.Bold)
    }
  }

  if (showEditProfileDialog) {
    var editedName by remember { mutableStateOf(patient.fullName) }
    var editedPhone by remember { mutableStateOf(patient.phone) }
    var editedAddress by remember { mutableStateOf(patient.address) }
    var editedAllergies by remember { mutableStateOf(patient.allergies) }

    AlertDialog(
      onDismissRequest = { showEditProfileDialog = false },
      title = { Text("Edit Patient Profile") },
      text = {
        Column(
          modifier = Modifier.verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedTextField(value = editedName, onValueChange = { editedName = it }, label = { Text("Full Legal Name") }, singleLine = true)
          OutlinedTextField(value = editedPhone, onValueChange = { editedPhone = it }, label = { Text("Phone Number") }, singleLine = true)
          OutlinedTextField(value = editedAddress, onValueChange = { editedAddress = it }, label = { Text("Address") }, maxLines = 2)
          OutlinedTextField(value = editedAllergies, onValueChange = { editedAllergies = it }, label = { Text("Known Allergies") }, singleLine = true)
        }
      },
      confirmButton = {
        Button(
          onClick = {
            repository.updateProfile(
              patient.copy(
                fullName = editedName,
                phone = editedPhone,
                address = editedAddress,
                allergies = editedAllergies
              )
            )
            showEditProfileDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
        ) {
          Text("Save Changes")
        }
      },
      dismissButton = {
        TextButton(onClick = { showEditProfileDialog = false }) { Text("Cancel") }
      }
    )
  }
}

@Composable
fun ProfileMenuItem(
  icon: ImageVector,
  title: String,
  subtitle: String,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .padding(16.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(TealLight),
      contentAlignment = Alignment.Center
    ) {
      Icon(icon, null, tint = TealPrimary, modifier = Modifier.size(20.dp))
    }
    Spacer(modifier = Modifier.width(14.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
      Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Slate500)
    }
    Icon(Icons.Default.ChevronRight, null, tint = Slate400)
  }
}
