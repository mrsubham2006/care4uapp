package com.example.ui.screens.hospital

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.core.model.Department
import com.example.core.model.HospitalInfo
import com.example.core.repository.CareNexusRepository
import com.example.ui.theme.*

@Composable
fun HospitalInfoScreen(
  repository: CareNexusRepository,
  onNavigateToIndoorNav: () -> Unit
) {
  val hospital = HospitalInfo()
  var selectedTab by remember { mutableIntStateOf(0) }
  val tabs = listOf("Hospital Overview", "Indoor Navigator")

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

    if (selectedTab == 0) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(16.dp)
      ) {
        // Hospital Hero Card
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(TealLight),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.LocalHospital, null, tint = TealPrimary, modifier = Modifier.size(28.dp))
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(hospital.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("NABH Accredited Tertiary Medical Center", style = MaterialTheme.typography.labelSmall, color = TealDark, fontWeight = FontWeight.Bold)
              }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Slate100)
            Spacer(modifier = Modifier.height(14.dp))

            InfoItem(icon = Icons.Default.LocationOn, label = "Address", value = hospital.address)
            InfoItem(icon = Icons.Default.Emergency, label = "24/7 ER Hotline", value = hospital.emergencyPhone, isHighlight = true)
            InfoItem(icon = Icons.Default.Phone, label = "General Inquiries", value = hospital.generalPhone)
            InfoItem(icon = Icons.Default.Schedule, label = "OPD Operating Hours", value = hospital.opdTimings)
            InfoItem(icon = Icons.Default.Group, label = "Visiting Hours", value = hospital.visitingHours)
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("Clinical Departments (${repository.departments.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        repository.departments.forEach { dept ->
          DepartmentCard(department = dept)
          Spacer(modifier = Modifier.height(8.dp))
        }
      }
    } else {
      // Indoor Navigator Prototype
      IndoorNavigationScreen(repository)
    }
  }
}

@Composable
fun IndoorNavigationScreen(repository: CareNexusRepository) {
  var selectedDept by remember { mutableStateOf("Cardiology") }

  val waypoints = listOf(
    Pair("Step 1: Main Hospital Entrance", "Proceed past the Triage Kiosk to the Main Lobby."),
    Pair("Step 2: Central Elevators", "Take Elevator Bank B to 3rd Floor (Cardiology & Inpatient)."),
    Pair("Step 3: 3rd Floor Concourse", "Exit elevator, turn right past the Central Nurse Station."),
    Pair("Step 4: Room 304 (Dr. Rahul Sharma)", "Arrive at Consultation Room 304. Scan Token A-27 at Door Kiosk.")
  )

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(16.dp)
  ) {
    Text(
      text = "Indoor Wayfinding Guide",
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.Bold
    )
    Text(
      text = "Step-by-step navigation inside CareNexus Central Hospital",
      style = MaterialTheme.typography.bodySmall,
      color = Slate500
    )

    Spacer(modifier = Modifier.height(16.dp))

    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = BlueDeep)
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Text("Destination Selected", style = MaterialTheme.typography.labelSmall, color = TealAccent)
        Text("Cardiology Department • Room 304", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
        Text("3rd Floor • Est. Walk: 2 minutes", style = MaterialTheme.typography.bodySmall, color = Slate300)
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text("Turn-by-Turn Directions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(12.dp))

    waypoints.forEachIndexed { index, waypoint ->
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
      ) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(TealPrimary),
          contentAlignment = Alignment.Center
        ) {
          Text("${index + 1}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(waypoint.first, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = Slate900)
          Text(waypoint.second, style = MaterialTheme.typography.bodySmall, color = Slate600)
        }
      }
      if (index < waypoints.lastIndex) {
        Box(
          modifier = Modifier
            .padding(start = 15.dp)
            .height(20.dp)
            .width(2.dp)
            .background(TealLight)
        )
      }
    }
  }
}

@Composable
fun InfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, isHighlight: Boolean = false) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp),
    verticalAlignment = Alignment.Top
  ) {
    Icon(icon, null, tint = if (isHighlight) EmergencyColor else TealPrimary, modifier = Modifier.size(20.dp))
    Spacer(modifier = Modifier.width(10.dp))
    Column {
      Text(label, style = MaterialTheme.typography.labelSmall, color = Slate500)
      Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal, color = if (isHighlight) EmergencyColor else Slate900)
    }
  }
}

@Composable
fun DepartmentCard(department: Department) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).background(TealLight),
        contentAlignment = Alignment.Center
      ) {
        Icon(Icons.Default.MedicalServices, null, tint = TealPrimary)
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(department.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text("${department.floorNumber} • ${department.roomRange}", style = MaterialTheme.typography.bodySmall, color = Slate500)
        Text(department.description, style = MaterialTheme.typography.labelSmall, color = Slate600, maxLines = 1)
      }
    }
  }
}
