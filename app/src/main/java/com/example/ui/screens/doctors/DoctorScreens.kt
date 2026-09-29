package com.example.ui.screens.doctors

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.Appointment
import com.example.core.model.Doctor
import com.example.core.repository.CareNexusRepository
import com.example.ui.components.QRCodeDisplayBox
import com.example.ui.theme.*

@Composable
fun DoctorListScreen(
  repository: CareNexusRepository,
  onDoctorClick: (String) -> Unit,
  onBookClick: (String) -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedDepartment by remember { mutableStateOf<String?>(null) }

  val filteredDoctors = remember(searchQuery, selectedDepartment) {
    repository.doctors.filter { doc ->
      val matchesSearch = doc.name.contains(searchQuery, ignoreCase = true) ||
          doc.specialization.contains(searchQuery, ignoreCase = true) ||
          doc.department.contains(searchQuery, ignoreCase = true)
      val matchesDept = selectedDepartment == null || doc.department.equals(selectedDepartment, ignoreCase = true)
      matchesSearch && matchesDept
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(bottom = 80.dp)
  ) {
    // Search Bar
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
        .testTag("doctor_search_bar"),
      placeholder = { Text("Search doctors, specializations, departments...") },
      leadingIcon = { Icon(Icons.Default.Search, null, tint = Slate500) },
      trailingIcon = {
        if (searchQuery.isNotEmpty()) {
          IconButton(onClick = { searchQuery = "" }) {
            Icon(Icons.Default.Clear, null)
          }
        }
      },
      shape = RoundedCornerShape(16.dp),
      singleLine = true
    )

    // Department Filter Chips
    LazyRow(
      contentPadding = PaddingValues(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.padding(vertical = 6.dp)
    ) {
      item {
        FilterChip(
          selected = selectedDepartment == null,
          onClick = { selectedDepartment = null },
          label = { Text("All Departments") },
          shape = RoundedCornerShape(12.dp)
        )
      }
      items(repository.departments) { dept ->
        FilterChip(
          selected = selectedDepartment == dept.name,
          onClick = { selectedDepartment = if (selectedDepartment == dept.name) null else dept.name },
          label = { Text(dept.name) },
          shape = RoundedCornerShape(12.dp)
        )
      }
    }

    // Doctors List
    LazyColumn(
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      items(filteredDoctors, key = { it.id }) { doctor ->
        DoctorListItemCard(
          doctor = doctor,
          onCardClick = { onDoctorClick(doctor.id) },
          onBookClick = { onBookClick(doctor.id) }
        )
      }
    }
  }
}

@Composable
fun DoctorListItemCard(
  doctor: Doctor,
  onCardClick: () -> Unit,
  onBookClick: () -> Unit
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
        verticalAlignment = Alignment.Top
      ) {
        Box(
          modifier = Modifier
            .size(60.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(TealLight),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.Person, null, tint = TealPrimary, modifier = Modifier.size(36.dp))
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = doctor.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = doctor.specialization,
            style = MaterialTheme.typography.bodySmall,
            color = TealPrimary,
            fontWeight = FontWeight.SemiBold
          )
          Text(
            text = "${doctor.department} • ${doctor.experienceYears} yrs exp",
            style = MaterialTheme.typography.bodySmall,
            color = Slate500
          )

          Spacer(modifier = Modifier.height(4.dp))

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, null, tint = WarningAmber, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${doctor.rating} (${doctor.reviewCount} reviews)",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = Slate700
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))
      HorizontalDivider(color = Slate100)
      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text("Consultation Fee", style = MaterialTheme.typography.labelSmall, color = Slate500)
          Text(
            text = "$${doctor.consultationFee}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Slate900
          )
        }

        Button(
          onClick = onBookClick,
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
          modifier = Modifier.testTag("book_doctor_${doctor.id}")
        ) {
          Text("Book Slot", fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.width(6.dp))
          Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(16.dp))
        }
      }
    }
  }
}

@Composable
fun DoctorDetailScreen(
  doctorId: String,
  repository: CareNexusRepository,
  onBookClick: (String) -> Unit,
  onBackClick: () -> Unit
) {
  val doctor = repository.doctors.find { it.id == doctorId } ?: repository.doctors.first()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .background(MaterialTheme.colorScheme.background)
      .padding(16.dp)
      .padding(bottom = 80.dp)
  ) {
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
            .size(88.dp)
            .clip(CircleShape)
            .background(TealLight),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.Person, null, tint = TealPrimary, modifier = Modifier.size(54.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = doctor.name,
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold
        )

        Text(
          text = doctor.specialization,
          style = MaterialTheme.typography.bodyMedium,
          color = TealPrimary,
          fontWeight = FontWeight.SemiBold
        )

        Text(
          text = "${doctor.department} • ${doctor.roomNumber}",
          style = MaterialTheme.typography.bodySmall,
          color = Slate500
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Slate50)
            .padding(12.dp),
          horizontalArrangement = Arrangement.SpaceEvenly
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Experience", style = MaterialTheme.typography.labelSmall, color = Slate500)
            Text("${doctor.experienceYears}+ Years", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
          }
          VerticalDivider(modifier = Modifier.height(30.dp), color = Slate200)
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Rating", style = MaterialTheme.typography.labelSmall, color = Slate500)
            Text("⭐ ${doctor.rating}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
          }
          VerticalDivider(modifier = Modifier.height(30.dp), color = Slate200)
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Fee", style = MaterialTheme.typography.labelSmall, color = Slate500)
            Text("$${doctor.consultationFee}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = "About Doctor",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = doctor.bio,
      style = MaterialTheme.typography.bodyMedium,
      color = Slate700,
      lineHeight = 22.sp
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = "Qualifications",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = doctor.qualifications,
      style = MaterialTheme.typography.bodyMedium,
      color = Slate700
    )

    Spacer(modifier = Modifier.height(24.dp))

    Button(
      onClick = { onBookClick(doctor.id) },
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
        .testTag("doctor_detail_book_button"),
      shape = RoundedCornerShape(14.dp),
      colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
    ) {
      Text("Book Appointment with ${doctor.name.split(" ").takeLast(1).joinToString("")}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
  }
}

@Composable
fun AppointmentBookingScreen(
  doctorId: String,
  repository: CareNexusRepository,
  onBookingSuccess: (String) -> Unit,
  onBackClick: () -> Unit
) {
  val doctor = repository.doctors.find { it.id == doctorId } ?: repository.doctors.first()

  val availableDates = listOf("2026-09-30", "2026-10-01", "2026-10-02", "2026-10-03")
  var selectedDate by remember { mutableStateOf(availableDates.first()) }
  var selectedSlot by remember { mutableStateOf(doctor.availableSlots.first()) }
  var consultationType by remember { mutableStateOf("IN_PERSON") }
  var reasonForVisit by remember { mutableStateOf("Routine Consultation") }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var bookedAppointment by remember { mutableStateOf<Appointment?>(null) }

  if (bookedAppointment != null) {
    // Booking Confirmation Success View
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .background(MaterialTheme.colorScheme.background)
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

      Text(
        text = "Appointment Confirmed!",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = SuccessGreen
      )

      Text(
        text = "Your digital OPD token has been generated.",
        style = MaterialTheme.typography.bodyMedium,
        color = Slate600
      )

      Spacer(modifier = Modifier.height(20.dp))

      QRCodeDisplayBox(
        qrPayload = bookedAppointment?.qrCodeData ?: "",
        title = "Token: ${bookedAppointment?.tokenNumber}",
        subtitle = "Scan at Reception or Room 304 Kiosk"
      )

      Spacer(modifier = Modifier.height(16.dp))

      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("Doctor: ${bookedAppointment?.doctorName}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
          Text("Department: ${bookedAppointment?.department}", style = MaterialTheme.typography.bodySmall, color = Slate600)
          Text("Date & Time: ${bookedAppointment?.date} at ${bookedAppointment?.timeSlot}", style = MaterialTheme.typography.bodySmall, color = Slate600)
          Text("Consultation: ${bookedAppointment?.consultationType}", style = MaterialTheme.typography.bodySmall, color = Slate600)
          Text("Appointment ID: ${bookedAppointment?.appointmentId}", style = MaterialTheme.typography.labelSmall, color = Slate400)
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      Button(
        onClick = { onBookingSuccess(bookedAppointment?.appointmentId ?: "") },
        modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
      ) {
        Text("Done & View Appointments", fontWeight = FontWeight.Bold)
      }
    }
  } else {
    // Step by Step Form
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .background(MaterialTheme.colorScheme.background)
        .padding(16.dp)
        .padding(bottom = 80.dp)
    ) {
      Text(
        text = "Select Appointment Date",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )

      Spacer(modifier = Modifier.height(8.dp))

      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(availableDates) { date ->
          val isSelected = date == selectedDate
          Surface(
            modifier = Modifier
              .clickable { selectedDate = date; errorMessage = null }
              .clip(RoundedCornerShape(14.dp))
              .border(
                1.5.dp,
                if (isSelected) TealPrimary else Slate200,
                RoundedCornerShape(14.dp)
              ),
            color = if (isSelected) TealLight else MaterialTheme.colorScheme.surface
          ) {
            Column(
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = date.takeLast(5),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) TealPrimary else Slate800
              )
              Text(
                text = "Available",
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) TealDark else Slate500
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = "Available Time Slots",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )

      Spacer(modifier = Modifier.height(8.dp))

      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(doctor.availableSlots) { slot ->
          val isSelected = slot == selectedSlot
          FilterChip(
            selected = isSelected,
            onClick = { selectedSlot = slot; errorMessage = null },
            label = { Text(slot, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
            shape = RoundedCornerShape(12.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = "Consultation Mode",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Surface(
          modifier = Modifier
            .weight(1f)
            .clickable { consultationType = "IN_PERSON" }
            .clip(RoundedCornerShape(14.dp))
            .border(
              1.5.dp,
              if (consultationType == "IN_PERSON") TealPrimary else Slate200,
              RoundedCornerShape(14.dp)
            ),
          color = if (consultationType == "IN_PERSON") TealLight else MaterialTheme.colorScheme.surface
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.LocationOn, null, tint = TealPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("In-Person OPD", fontWeight = FontWeight.Bold, fontSize = 13.sp)
          }
        }

        Surface(
          modifier = Modifier
            .weight(1f)
            .clickable { consultationType = "VIDEO_CONSULTATION" }
            .clip(RoundedCornerShape(14.dp))
            .border(
              1.5.dp,
              if (consultationType == "VIDEO_CONSULTATION") TealPrimary else Slate200,
              RoundedCornerShape(14.dp)
            ),
          color = if (consultationType == "VIDEO_CONSULTATION") TealLight else MaterialTheme.colorScheme.surface
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Videocam, null, tint = TealPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Tele-Consult", fontWeight = FontWeight.Bold, fontSize = 13.sp)
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      OutlinedTextField(
        value = reasonForVisit,
        onValueChange = { reasonForVisit = it },
        label = { Text("Reason for Visit / Symptoms") },
        modifier = Modifier.fillMaxWidth().testTag("booking_reason_input"),
        shape = RoundedCornerShape(12.dp),
        minLines = 2
      )

      if (errorMessage != null) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(errorMessage ?: "", color = ErrorRed, style = MaterialTheme.typography.bodySmall)
      }

      Spacer(modifier = Modifier.height(24.dp))

      Button(
        onClick = {
          val result = repository.bookAppointment(
            doctorId = doctor.id,
            date = selectedDate,
            timeSlot = selectedSlot,
            consultationType = consultationType,
            reason = reasonForVisit
          )
          result.onSuccess { apt ->
            bookedAppointment = apt
          }.onFailure { err ->
            errorMessage = err.message ?: "Booking failed. Please try another slot."
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("confirm_booking_button"),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
      ) {
        Text("Confirm & Book Slot ($${doctor.consultationFee})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
      }
    }
  }
}
