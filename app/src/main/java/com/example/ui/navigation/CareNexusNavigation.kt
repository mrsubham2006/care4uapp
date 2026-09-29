package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.core.repository.CareNexusRepository
import com.example.ui.components.CareNexusTopBar
import com.example.ui.screens.ai.CareAiScreen
import com.example.ui.screens.appointments.AppointmentDetailScreen
import com.example.ui.screens.appointments.DigitalQueueScreen
import com.example.ui.screens.appointments.MyAppointmentsScreen
import com.example.ui.screens.appointments.VideoConsultationScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.OnboardingScreen
import com.example.ui.screens.auth.RegisterScreen
import com.example.ui.screens.billing.BillsScreen
import com.example.ui.screens.billing.PaymentScreen
import com.example.ui.screens.dashboard.HomeScreen
import com.example.ui.screens.doctors.AppointmentBookingScreen
import com.example.ui.screens.doctors.DoctorDetailScreen
import com.example.ui.screens.doctors.DoctorListScreen
import com.example.ui.screens.emergency.EmergencyScreen
import com.example.ui.screens.family.FamilyMembersScreen
import com.example.ui.screens.hospital.HospitalInfoScreen
import com.example.ui.screens.laboratory.LabReportDetailScreen
import com.example.ui.screens.laboratory.LaboratoryScreen
import com.example.ui.screens.medications.MedicationRemindersScreen
import com.example.ui.screens.notifications.NotificationsScreen
import com.example.ui.screens.prescriptions.PharmacyOrdersScreen
import com.example.ui.screens.prescriptions.PrescriptionsScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.profile.SettingsScreen
import com.example.ui.screens.insights.HealthInsightsScreen
import com.example.ui.screens.symptom.SymptomCheckerScreen
import com.example.ui.screens.records.MedicalTimelineScreen
import com.example.ui.screens.records.RecordDetailScreen
import com.example.ui.screens.scanner.DocumentAndQrScannerScreen
import com.example.ui.screens.vitals.VitalsScreen
import com.example.ui.theme.*

enum class AppScreen(val route: String, val title: String, val icon: ImageVector? = null) {
  ONBOARDING("onboarding", "Welcome"),
  LOGIN("login", "Sign In"),
  REGISTER("register", "Register"),
  HOME("home", "CareNexus", Icons.Default.Home),
  APPOINTMENTS("appointments", "Appointments", Icons.Default.CalendarMonth),
  RECORDS("records", "Medical Records", Icons.Default.FolderOpen),
  PHARMACY("pharmacy", "Pharmacy", Icons.Default.LocalPharmacy),
  PROFILE("profile", "Profile", Icons.Default.Person),
  DOCTORS("doctors", "Find Doctor"),
  DOCTOR_DETAIL("doctor_detail", "Doctor Details"),
  BOOK_APPOINTMENT("book_appointment", "Book Slot"),
  QUEUE("queue", "Live Queue"),
  APPOINTMENT_DETAIL("appointment_detail", "Appointment Pass"),
  LABORATORY("laboratory", "Diagnostic Lab"),
  LAB_DETAIL("lab_detail", "Lab Report"),
  PRESCRIPTIONS("prescriptions", "Prescriptions"),
  BILLING("billing", "Bills & Invoices"),
  PAYMENT("payment", "Payment Checkout"),
  SCANNER("scanner", "Scan & QR"),
  EMERGENCY("emergency", "Emergency SOS"),
  HOSPITAL("hospital", "Hospital Guide"),
  FAMILY("family", "Family Members"),
  MEDICATION_REMINDERS("medication_reminders", "Medication Reminders"),
  VITALS("vitals", "Health Vitals", Icons.Default.Favorite),
  HEALTH_INSIGHTS("health_insights", "Health Insights", Icons.Default.Insights),
  SYMPTOM_CHECKER("symptom_checker", "Symptom Checker", Icons.Default.HealthAndSafety),
  VIDEO_CONSULTATION("video_consultation", "Telehealth Video Call"),
  CARE_AI("care_ai", "CareAI Assistant"),
  NOTIFICATIONS("notifications", "Notifications"),
  SETTINGS("settings", "Settings")
}




@Composable
fun CareNexusMainNavigation(
  repository: CareNexusRepository
) {
  var currentRoute by remember { mutableStateOf("home") }
  var screenHistory by remember { mutableStateOf(listOf("home")) }
  var selectedDoctorId by remember { mutableStateOf<String?>(null) }
  var selectedAppointmentId by remember { mutableStateOf<String?>(null) }
  var selectedLabOrderId by remember { mutableStateOf<String?>(null) }
  var selectedRecordId by remember { mutableStateOf<String?>(null) }
  var selectedBillId by remember { mutableStateOf<String?>(null) }

  val isAuthenticated by repository.isAuthenticated.collectAsState()
  val unreadCount by repository.unreadNotificationsCount.collectAsState()

  fun navigateTo(route: String) {
    screenHistory = screenHistory + currentRoute
    currentRoute = route
  }

  fun navigateBack() {
    if (screenHistory.isNotEmpty()) {
      currentRoute = screenHistory.last()
      screenHistory = screenHistory.dropLast(1)
    } else {
      currentRoute = "home"
    }
  }

  BackHandler(enabled = currentRoute != "home" && currentRoute != "login" && currentRoute != "onboarding") {
    navigateBack()
  }

  if (!isAuthenticated && currentRoute != "login" && currentRoute != "register" && currentRoute != "onboarding") {
    currentRoute = "login"
  }

  val isBottomNavVisible = currentRoute in listOf("home", "appointments", "records", "pharmacy", "profile")

  Scaffold(
    topBar = {
      if (currentRoute != "login" && currentRoute != "register" && currentRoute != "onboarding") {
        val topTitle = when (currentRoute) {
          "home" -> "CareNexus Hospital"
          "appointments" -> "My Appointments"
          "records" -> "Medical Timeline"
          "pharmacy" -> "Hospital Pharmacy"
          "profile" -> "Patient Profile"
          "doctors" -> "Find Doctor"
          "doctor_detail" -> "Doctor Profile"
          "book_appointment" -> "Book Consultation"
          "queue" -> "OPD Live Queue"
          "appointment_detail" -> "Appointment Pass"
          "laboratory" -> "Laboratory Tests"
          "lab_detail" -> "Lab Report"
          "prescriptions" -> "Prescriptions"
          "billing" -> "Hospital Bills"
          "payment" -> "Make Payment"
          "scanner" -> "Document & QR Scanner"
          "emergency" -> "Emergency Care"
          "hospital" -> "Hospital Info"
          "family" -> "Family Members"
          "care_ai" -> "CareAI Assistant"
          "notifications" -> "Notifications"
          "settings" -> "Settings"
          else -> "CareNexus"
        }
        CareNexusTopBar(
          title = topTitle,
          showBackButton = !isBottomNavVisible,
          onBackClick = { navigateBack() },
          onNotificationClick = { navigateTo("notifications") },
          onEmergencyClick = { navigateTo("emergency") },
          onAiClick = { navigateTo("care_ai") },
          unreadNotifs = unreadCount
        )
      }
    },
    bottomBar = {
      if (isBottomNavVisible) {
        NavigationBar(
          containerColor = MaterialTheme.colorScheme.surface,
          contentColor = TealPrimary,
          tonalElevation = 8.dp
        ) {
          val navTabs = listOf(
            AppScreen.HOME,
            AppScreen.APPOINTMENTS,
            AppScreen.RECORDS,
            AppScreen.PHARMACY,
            AppScreen.PROFILE
          )
          navTabs.forEach { screen ->
            val isSelected = currentRoute == screen.route
            NavigationBarItem(
              icon = {
                Icon(
                  imageVector = screen.icon ?: Icons.Default.Home,
                  contentDescription = screen.title
                )
              },
              label = { Text(screen.title, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
              selected = isSelected,
              onClick = {
                if (currentRoute != screen.route) {
                  currentRoute = screen.route
                }
              },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TealPrimary,
                selectedTextColor = TealPrimary,
                indicatorColor = TealLight
              )
            )
          }
        }
      }
    },
    floatingActionButton = {
      if (isBottomNavVisible) {
        if (currentRoute == "home") {
          ExtendedFloatingActionButton(
            onClick = { navigateTo("care_ai") },
            containerColor = TealPrimary,
            contentColor = Color.White,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
              .testTag("floating_care_ai_btn")
              .testTag("home_fab_ask_ai"),
            icon = {
              Icon(Icons.Default.AutoAwesome, contentDescription = "CareAI", tint = TealAccent)
            },
            text = {
              Text(
                text = "Ask CareAI",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge
              )
            }
          )
        } else {
          FloatingActionButton(
            onClick = { navigateTo("care_ai") },
            containerColor = TealPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.testTag("floating_care_ai_btn")
          ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = "CareAI Assistant", tint = TealAccent)
          }
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (currentRoute) {
        "onboarding" -> OnboardingScreen(onFinishOnboarding = { currentRoute = "login" })
        "login" -> LoginScreen(
          repository = repository,
          onLoginSuccess = { currentRoute = "home" },
          onNavigateToRegister = { currentRoute = "register" }
        )
        "register" -> RegisterScreen(
          repository = repository,
          onRegisterSuccess = { currentRoute = "home" },
          onNavigateToLogin = { currentRoute = "login" }
        )
        "home" -> HomeScreen(
          repository = repository,
          onNavigateToRoute = { navigateTo(it) },
          onDoctorClick = { docId -> selectedDoctorId = docId; navigateTo("doctor_detail") },
          onAppointmentClick = { aptId -> selectedAppointmentId = aptId; navigateTo("appointment_detail") },
          onLabOrderClick = { labId -> selectedLabOrderId = labId; navigateTo("lab_detail") }
        )
        "appointments" -> MyAppointmentsScreen(
          repository = repository,
          onAppointmentClick = { aptId -> selectedAppointmentId = aptId; navigateTo("appointment_detail") },
          onNavigateToQueue = { navigateTo("queue") },
          onJoinVideoCall = { aptId -> selectedAppointmentId = aptId; navigateTo("video_consultation") },
          onBookNewClick = { navigateTo("doctors") }
        )
        "records" -> MedicalTimelineScreen(
          repository = repository,
          onRecordClick = { recId -> selectedRecordId = recId; navigateTo("record_detail") },
          onScanDocumentClick = { navigateTo("scanner") }
        )
        "pharmacy" -> PharmacyOrdersScreen(
          repository = repository,
          onBackClick = { navigateBack() }
        )
        "profile" -> ProfileScreen(
          repository = repository,
          onNavigateToRoute = { navigateTo(it) },
          onLogout = { currentRoute = "login" }
        )
        "doctors" -> DoctorListScreen(
          repository = repository,
          onDoctorClick = { docId -> selectedDoctorId = docId; navigateTo("doctor_detail") },
          onBookClick = { docId -> selectedDoctorId = docId; navigateTo("book_appointment") }
        )
        "doctor_detail" -> DoctorDetailScreen(
          doctorId = selectedDoctorId ?: "doc-sharma",
          repository = repository,
          onBookClick = { docId -> selectedDoctorId = docId; navigateTo("book_appointment") },
          onBackClick = { navigateBack() }
        )
        "book_appointment" -> AppointmentBookingScreen(
          doctorId = selectedDoctorId ?: "doc-sharma",
          repository = repository,
          onBookingSuccess = { aptId -> selectedAppointmentId = aptId; navigateTo("appointments") },
          onBackClick = { navigateBack() }
        )
        "queue" -> DigitalQueueScreen(
          repository = repository,
          onBackClick = { navigateBack() },
          onScanCheckInClick = { navigateTo("scanner") }
        )
        "appointment_detail" -> AppointmentDetailScreen(
          appointmentId = selectedAppointmentId ?: "APT-1024",
          repository = repository,
          onBackClick = { navigateBack() },
          onNavigateToQueue = { navigateTo("queue") },
          onJoinVideoCall = { navigateTo("video_consultation") }
        )
        "video_consultation" -> VideoConsultationScreen(
          appointmentId = selectedAppointmentId ?: "APT-1024",
          repository = repository,
          onConsultationEnd = { navigateTo("appointments") },
          onBackClick = { navigateBack() }
        )
        "laboratory" -> LaboratoryScreen(
          repository = repository,
          onLabOrderClick = { labId -> selectedLabOrderId = labId; navigateTo("lab_detail") }
        )
        "lab_detail" -> LabReportDetailScreen(
          orderId = selectedLabOrderId ?: "LAB-8841",
          repository = repository,
          onBackClick = { navigateBack() }
        )
        "prescriptions" -> PrescriptionsScreen(
          repository = repository,
          onNavigateToPharmacy = { navigateTo("pharmacy") },
          onNavigateToReminders = { navigateTo("medication_reminders") }
        )
        "billing" -> BillsScreen(
          repository = repository,
          onPayBillClick = { billId -> selectedBillId = billId; navigateTo("payment") }
        )
        "payment" -> PaymentScreen(
          billId = selectedBillId ?: "BILL-9012",
          repository = repository,
          onPaymentSuccess = { navigateTo("billing") },
          onBackClick = { navigateBack() }
        )
        "record_detail" -> RecordDetailScreen(
          recordId = selectedRecordId ?: "REC-301",
          repository = repository,
          onBackClick = { navigateBack() }
        )
        "scanner" -> DocumentAndQrScannerScreen(
          repository = repository,
          onScanSuccess = { navigateTo("records") },
          onBackClick = { navigateBack() }
        )
        "emergency" -> EmergencyScreen(
          repository = repository,
          onBackClick = { navigateBack() }
        )
        "hospital" -> HospitalInfoScreen(
          repository = repository,
          onNavigateToIndoorNav = { navigateTo("hospital") }
        )
        "family" -> FamilyMembersScreen(
          repository = repository,
          onBackClick = { navigateBack() }
        )
        "medication_reminders" -> MedicationRemindersScreen(
          repository = repository,
          onBackClick = { navigateBack() }
        )
        "vitals" -> VitalsScreen(
          repository = repository,
          onNavigateBack = { navigateBack() },
          onNavigateToCareAi = { navigateTo("care_ai") }
        )
        "health_insights" -> HealthInsightsScreen(
          repository = repository,
          onBackClick = { navigateBack() },
          onNavigateToCareAi = { navigateTo("care_ai") }
        )
        "symptom_checker" -> SymptomCheckerScreen(
          repository = repository,
          onNavigateBack = { navigateBack() },
          onBookAppointmentWithDoctor = { docId ->
            selectedDoctorId = docId
            navigateTo("book_appointment")
          },
          onExploreDepartmentDoctors = { deptId ->
            navigateTo("doctors")
          },
          onNavigateToEmergency = {
            navigateTo("emergency")
          }
        )
        "care_ai" -> CareAiScreen(
          repository = repository,
          onNavigateToRoute = { navigateTo(it) }
        )
        "notifications" -> NotificationsScreen(
          repository = repository,
          onNavigateToRoute = { navigateTo(it) },
          onBackClick = { navigateBack() }
        )
        "settings" -> SettingsScreen(
          repository = repository,
          onBackClick = { navigateBack() }
        )
        else -> HomeScreen(
          repository = repository,
          onNavigateToRoute = { navigateTo(it) },
          onDoctorClick = { docId -> selectedDoctorId = docId; navigateTo("doctor_detail") },
          onAppointmentClick = { aptId -> selectedAppointmentId = aptId; navigateTo("appointment_detail") },
          onLabOrderClick = { labId -> selectedLabOrderId = labId; navigateTo("lab_detail") }
        )
      }
    }
  }
}
