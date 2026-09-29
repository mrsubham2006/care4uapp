package com.example.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.core.repository.CareNexusRepository
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
  repository: CareNexusRepository,
  onBackClick: () -> Unit
) {
  var queueNotifs by remember { mutableStateOf(true) }
  var labReportNotifs by remember { mutableStateOf(true) }
  var pharmacyNotifs by remember { mutableStateOf(true) }
  var emergencySharingConsent by remember { mutableStateOf(true) }
  var biometricAuth by remember { mutableStateOf(true) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .background(MaterialTheme.colorScheme.background)
      .padding(16.dp)
      .padding(bottom = 80.dp)
  ) {
    Text(
      text = "Hospital App Settings",
      style = MaterialTheme.typography.titleLarge,
      fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text("Notification Preferences", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))

    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SettingToggleItem(title = "Live Token & Queue Alerts", subtitle = "Receive alert when 5 and 2 patients remain", checked = queueNotifs, onCheckedChange = { queueNotifs = it })
        HorizontalDivider(color = Slate100)
        SettingToggleItem(title = "Diagnostic Lab Results", subtitle = "Notify immediately upon pathologist verification", checked = labReportNotifs, onCheckedChange = { labReportNotifs = it })
        HorizontalDivider(color = Slate100)
        SettingToggleItem(title = "Pharmacy Ready Alerts", subtitle = "Notify when medications are packed at counter", checked = pharmacyNotifs, onCheckedChange = { pharmacyNotifs = it })
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text("Security & Privacy Safeguards", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))

    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SettingToggleItem(title = "Biometric Passcode / Fingerprint", subtitle = "Require authentication to unlock medical records", checked = biometricAuth, onCheckedChange = { biometricAuth = it })
        HorizontalDivider(color = Slate100)
        SettingToggleItem(title = "Emergency Location Triage", subtitle = "Allow ER staff to receive GPS during SOS alert", checked = emergencySharingConsent, onCheckedChange = { emergencySharingConsent = it })
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = Slate100)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text("CareNexus Platform Compliance", style = MaterialTheme.typography.titleSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Version 2.4.0 (Build 2026.09) • End-to-End HIPAA & HL7 Compliant Data Encryption", style = MaterialTheme.typography.bodySmall, color = Slate600)
      }
    }
  }
}

@Composable
fun SettingToggleItem(
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
      Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Slate500)
    }
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(checkedThumbColor = TealPrimary, checkedTrackColor = TealLight)
    )
  }
}
