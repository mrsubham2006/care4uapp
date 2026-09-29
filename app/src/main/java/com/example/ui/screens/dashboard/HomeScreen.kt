package com.example.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.AppointmentStatus
import com.example.core.repository.CareNexusRepository
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun HomeScreen(
  repository: CareNexusRepository,
  onNavigateToRoute: (String) -> Unit,
  onDoctorClick: (String) -> Unit,
  onAppointmentClick: (String) -> Unit,
  onLabOrderClick: (String) -> Unit
) {
  val patient by repository.currentPatient.collectAsState()
  val appointments by repository.appointments.collectAsState()
  val queueStatus by repository.queueStatus.collectAsState()
  val labOrders by repository.labOrders.collectAsState()
  val prescriptions by repository.prescriptions.collectAsState()
  val bills by repository.bills.collectAsState()
  val vitals by repository.vitals.collectAsState()

  val nextAppointment = appointments.firstOrNull {
    it.status == AppointmentStatus.CONFIRMED || it.status == AppointmentStatus.CHECKED_IN
  }

  val pendingBill = bills.firstOrNull { it.status == com.example.core.model.BillStatus.PENDING }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .background(MaterialTheme.colorScheme.background)
      .padding(bottom = 90.dp)
  ) {
    // Top Greeting & Patient Identity Header
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(52.dp)
              .clip(CircleShape)
              .background(
                Brush.linearGradient(listOf(TealPrimary, TealAccent))
              )
              .clickable { onNavigateToRoute("profile") },
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = patient.fullName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Text(
              text = "Hello, ${patient.fullName.split(" ").firstOrNull() ?: "Patient"}",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "Patient ID: ${patient.patientId}",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500
              )
              Spacer(modifier = Modifier.width(6.dp))
              Surface(
                color = TealLight,
                shape = RoundedCornerShape(8.dp)
              ) {
                Text(
                  text = patient.bloodGroup,
                  color = TealDark,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
          }
        }
      }
    }

    // NEXT APPOINTMENT HERO CARD
    if (nextAppointment != null) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp)
          .testTag("next_appointment_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = BlueDeep),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(TealAccent)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "NEXT APPOINTMENT",
                style = MaterialTheme.typography.labelMedium,
                color = TealAccent,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
              )
            }

            Surface(
              color = NavyCard,
              shape = RoundedCornerShape(12.dp)
            ) {
              Text(
                text = "Token ${nextAppointment.tokenNumber}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = nextAppointment.doctorName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )

          Text(
            text = "${nextAppointment.department} • Room 304, 3rd Floor",
            style = MaterialTheme.typography.bodyMedium,
            color = Slate300
          )

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(NavyCard)
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Event, null, tint = TealAccent, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(nextAppointment.date, color = Color.White, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Schedule, null, tint = TealAccent, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(nextAppointment.timeSlot, color = Color.White, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.HourglassBottom, null, tint = WarningAmber, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("${queueStatus.patientsAhead} ahead", color = WarningAmber, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedButton(
              onClick = { onAppointmentClick(nextAppointment.appointmentId) },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
              border = ButtonDefaults.outlinedButtonBorder(enabled = true)
            ) {
              Text("Details & QR", fontSize = 13.sp)
            }

            Button(
              onClick = { onNavigateToRoute("queue") },
              modifier = Modifier.weight(1f).testTag("track_queue_button"),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
              Text("Live Queue", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    // Pending Bill Quick Alert Banner (if exists)
    if (pendingBill != null) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp)
          .clickable { onNavigateToRoute("billing") },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WarningLight)
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
            Icon(Icons.AutoMirrored.Filled.ReceiptLong, null, tint = WarningAmber, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Pending Hospital Invoice",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Slate900
              )
              Text(
                text = "Invoice ${pendingBill.invoiceNumber} • $${pendingBill.total}",
                style = MaterialTheme.typography.bodySmall,
                color = Slate700
              )
            }
          }
          Button(
            onClick = { onNavigateToRoute("billing") },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = WarningAmber)
          ) {
            Text("Pay", fontWeight = FontWeight.Bold, color = Slate900)
          }
        }
      }
    }

    // Health Vitals Live Snapshot Card
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp)
        .clickable { onNavigateToRoute("vitals") },
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(TealLight),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Favorite, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Health Vitals & Biometrics",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = Slate900
            )
          }

          Text(
            text = "Track / Log >",
            style = MaterialTheme.typography.labelMedium,
            color = TealPrimary,
            fontWeight = FontWeight.Bold
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          val bp = vitals.firstOrNull { it.type == com.example.core.model.VitalType.BLOOD_PRESSURE }?.value ?: "120/78"
          val hr = vitals.firstOrNull { it.type == com.example.core.model.VitalType.HEART_RATE }?.value ?: "72"
          val glu = vitals.firstOrNull { it.type == com.example.core.model.VitalType.BLOOD_GLUCOSE }?.value ?: "94"
          val spo2 = vitals.firstOrNull { it.type == com.example.core.model.VitalType.SPO2 }?.value ?: "99%"

          VitalGlanceItem(label = "Blood Pressure", value = bp, unit = "mmHg")
          VitalGlanceItem(label = "Heart Rate", value = hr, unit = "BPM")
          VitalGlanceItem(label = "Blood Sugar", value = glu, unit = "mg/dL")
          VitalGlanceItem(label = "SpO2", value = spo2, unit = "%")
        }
      }
    }

    // CareAI Health Insights Weekly Trend Banner
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp)
        .clickable { onNavigateToRoute("health_insights") }
        .testTag("health_insights_banner"),
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = NavyDark)
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            Brush.horizontalGradient(
              listOf(NavyDark, Color(0xFF1E293B), TealDark)
            )
          )
          .padding(16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
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
                .clip(CircleShape)
                .background(TealPrimary),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Insights, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "Weekly Health Insights",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  color = TealAccent.copy(alpha = 0.2f),
                  shape = RoundedCornerShape(4.dp)
                ) {
                  Text(
                    text = "Score 88/100",
                    color = TealAccent,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                  )
                }
              }
              Text(
                text = "Gemini multi-source analysis: Records, Labs & Meds",
                style = MaterialTheme.typography.bodySmall,
                color = Slate300,
                maxLines = 1
              )
            }
          }

          Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TealAccent)
        }
      }
    }


    // QUICK ACTIONS GRID (All Core Features from CareNexus Spec)
    Text(
      text = "Hospital Quick Services",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 12.dp)
    )

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Row 1: Doctor Discovery & Appointments
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        QuickActionCard(
          title = "Find Doctor",
          subtitle = "Search specialists",
          icon = Icons.Default.PersonSearch,
          iconBg = TealPrimary,
          modifier = Modifier.weight(1f).testTag("action_find_doctor"),
          onClick = { onNavigateToRoute("doctors") }
        )
        QuickActionCard(
          title = "Book Slot",
          subtitle = "New appointment",
          icon = Icons.Default.AddCircleOutline,
          iconBg = TealDark,
          modifier = Modifier.weight(1f).testTag("action_book_appointment"),
          onClick = { onNavigateToRoute("doctors") }
        )
      }

      // Row 2: Queue & My Appointments
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        QuickActionCard(
          title = "My Bookings",
          subtitle = "Active & past",
          icon = Icons.Default.CalendarMonth,
          iconBg = CyanAccent,
          modifier = Modifier.weight(1f).testTag("action_my_appointments"),
          onClick = { onNavigateToRoute("appointments") }
        )
        QuickActionCard(
          title = "Live Queue",
          subtitle = "OPD token tracker",
          icon = Icons.Default.ConfirmationNumber,
          iconBg = BlueDeep,
          modifier = Modifier.weight(1f).testTag("action_live_queue"),
          onClick = { onNavigateToRoute("queue") }
        )
      }

      // Row 3: Diagnostics & Prescriptions
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        QuickActionCard(
          title = "Lab Reports",
          subtitle = "AI test analysis",
          icon = Icons.Default.Biotech,
          iconBg = TealDark,
          modifier = Modifier.weight(1f).testTag("action_lab_reports"),
          onClick = { onNavigateToRoute("laboratory") }
        )
        QuickActionCard(
          title = "Prescriptions",
          subtitle = "Doctor e-Rx list",
          icon = Icons.Default.Medication,
          iconBg = SuccessGreen,
          modifier = Modifier.weight(1f).testTag("action_prescriptions"),
          onClick = { onNavigateToRoute("prescriptions") }
        )
      }

      // Row 4: Pharmacy & Medical Timeline
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        QuickActionCard(
          title = "Pharmacy",
          subtitle = "Pickup counter B",
          icon = Icons.Default.LocalPharmacy,
          iconBg = SuccessGreen,
          modifier = Modifier.weight(1f).testTag("action_pharmacy"),
          onClick = { onNavigateToRoute("pharmacy") }
        )
        QuickActionCard(
          title = "Medical Records",
          subtitle = "Verified timeline",
          icon = Icons.Default.FolderOpen,
          iconBg = Slate700,
          modifier = Modifier.weight(1f).testTag("action_medical_records"),
          onClick = { onNavigateToRoute("records") }
        )
      }

      // Row 5: Vitals Tracker & Reminders
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        QuickActionCard(
          title = "Health Vitals",
          subtitle = "Log BP, Sugar, HR",
          icon = Icons.Default.Favorite,
          iconBg = Color(0xFFEF4444),
          modifier = Modifier.weight(1f).testTag("action_vitals"),
          onClick = { onNavigateToRoute("vitals") }
        )
        QuickActionCard(
          title = "Pill Reminders",
          subtitle = "Daily dose alerts",
          icon = Icons.Default.Alarm,
          iconBg = Color(0xFF8B5CF6),
          modifier = Modifier.weight(1f).testTag("action_med_reminders"),
          onClick = { onNavigateToRoute("medication_reminders") }
        )
      }

      // Row 6: Bills & Emergency
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        QuickActionCard(
          title = "Bills & Pay",
          subtitle = "Invoices & receipts",
          icon = Icons.AutoMirrored.Filled.ReceiptLong,
          iconBg = WarningAmber,
          modifier = Modifier.weight(1f).testTag("action_bills"),
          onClick = { onNavigateToRoute("billing") }
        )
        QuickActionCard(
          title = "Emergency SOS",
          subtitle = "24/7 ER hotline",
          icon = Icons.Default.Emergency,
          iconBg = EmergencyColor,
          modifier = Modifier.weight(1f).testTag("action_emergency"),
          onClick = { onNavigateToRoute("emergency") }
        )
      }

      // Row 7: Family Care & Scan Documents
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        QuickActionCard(
          title = "Family Health",
          subtitle = "Kids & elder records",
          icon = Icons.Default.FamilyRestroom,
          iconBg = Color(0xFF0D9488),
          modifier = Modifier.weight(1f).testTag("action_family"),
          onClick = { onNavigateToRoute("family") }
        )
        QuickActionCard(
          title = "Scan Documents",
          subtitle = "AI file scanner",
          icon = Icons.Default.QrCodeScanner,
          iconBg = Color(0xFF475569),
          modifier = Modifier.weight(1f).testTag("action_scanner"),
          onClick = { onNavigateToRoute("scanner") }
        )
      }

      // Row 8: AI Health Insights & Hospital Guide
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        QuickActionCard(
          title = "Health Insights",
          subtitle = "Gemini trend report",
          icon = Icons.Default.Insights,
          iconBg = Color(0xFF6366F1),
          modifier = Modifier.weight(1f).testTag("action_health_insights"),
          onClick = { onNavigateToRoute("health_insights") }
        )
        QuickActionCard(
          title = "Hospital Guide",
          subtitle = "Indoor wayfinding",
          icon = Icons.Default.Navigation,
          iconBg = Color(0xFF0284C7),
          modifier = Modifier.weight(1f).testTag("action_hospital_guide"),
          onClick = { onNavigateToRoute("hospital") }
        )
      }

      // Row 9: AI Symptom Checker & Telehealth
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        QuickActionCard(
          title = "Symptom Checker",
          subtitle = "Triage & dept guide",
          icon = Icons.Default.HealthAndSafety,
          iconBg = Color(0xFFE11D48),
          modifier = Modifier.weight(1f).testTag("action_symptom_checker"),
          onClick = { onNavigateToRoute("symptom_checker") }
        )
        QuickActionCard(
          title = "Video Consult",
          subtitle = "Telehealth call",
          icon = Icons.Default.VideoCall,
          iconBg = TealDark,
          modifier = Modifier.weight(1f).testTag("action_video_consult"),
          onClick = { onNavigateToRoute("appointments") }
        )
      }
    }





    // TOP SPECIALISTS CAROUSEL
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = 20.dp, end = 16.dp, top = 24.dp, bottom = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Top Specialists",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
      TextButton(onClick = { onNavigateToRoute("doctors") }) {
        Text("View All", color = TealPrimary, fontWeight = FontWeight.SemiBold)
      }
    }

    LazyRow(
      contentPadding = PaddingValues(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      items(repository.doctors) { doctor ->
        Card(
          modifier = Modifier
            .width(220.dp)
            .clickable { onDoctorClick(doctor.id) },
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .clip(CircleShape)
                  .background(TealLight),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.MedicalServices, null, tint = TealPrimary, modifier = Modifier.size(24.dp))
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = doctor.name,
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = doctor.department,
                  style = MaterialTheme.typography.bodySmall,
                  color = TealPrimary,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("⭐ ${doctor.rating} (${doctor.reviewCount})", style = MaterialTheme.typography.labelSmall, color = Slate600)
              Text("$${doctor.consultationFee}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Slate900)
            }
          }
        }
      }
    }

    // RECENT MEDICAL ACTIVITY
    Text(
      text = "Recent Activity",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 8.dp)
    )

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      labOrders.take(1).forEach { order ->
        ActivityItemCard(
          icon = Icons.Default.Science,
          title = order.testName,
          subtitle = "${order.doctorName} • Verified",
          date = order.date,
          status = order.status.name,
          onClick = { onLabOrderClick(order.id) }
        )
      }

      prescriptions.take(1).forEach { rx ->
        ActivityItemCard(
          icon = Icons.Default.Medication,
          title = "Prescription - ${rx.diagnosis}",
          subtitle = "${rx.doctorName} • ${rx.items.size} items",
          date = rx.date,
          status = "ACTIVE",
          onClick = { onNavigateToRoute("prescriptions") }
        )
      }
    }
  }
}

@Composable
fun QuickActionCard(
  title: String,
  subtitle: String,
  icon: ImageVector,
  iconBg: Color,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Card(
    modifier = modifier
      .height(84.dp)
      .clickable { onClick() },
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(iconBg),
        contentAlignment = Alignment.Center
      ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
      }
      Spacer(modifier = Modifier.width(10.dp))
      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = Slate500,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
  }
}

@Composable
fun ActivityItemCard(
  icon: ImageVector,
  title: String,
  subtitle: String,
  date: String,
  status: String,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() },
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
            .size(40.dp)
            .clip(CircleShape)
            .background(TealLight),
          contentAlignment = Alignment.Center
        ) {
          Icon(icon, null, tint = TealPrimary, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = "$subtitle • $date",
            style = MaterialTheme.typography.bodySmall,
            color = Slate500,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }
      StatusBadge(status = status)
    }
  }
}

@Composable
fun VitalGlanceItem(
  label: String,
  value: String,
  unit: String
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .clip(RoundedCornerShape(12.dp))
      .background(Slate50)
      .padding(horizontal = 8.dp, vertical = 6.dp)
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = Slate500,
      fontSize = 10.sp
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = value,
      style = MaterialTheme.typography.titleSmall,
      fontWeight = FontWeight.Bold,
      color = Slate900
    )
    Text(
      text = unit,
      style = MaterialTheme.typography.labelSmall,
      color = Slate400,
      fontSize = 9.sp
    )
  }
}

