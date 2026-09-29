package com.example.core.repository

import android.content.Context
import com.example.core.ai.GeminiService
import com.example.core.database.AppDatabase
import com.example.core.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CareNexusRepository private constructor(context: Context) {
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  private val db = AppDatabase.getDatabase(context)
  private val dao = db.careDao()
  private val geminiService = GeminiService()

  // Authentication State
  private val _isAuthenticated = MutableStateFlow(true)
  val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

  private val _currentPatient = MutableStateFlow(SeedData.defaultPatient)
  val currentPatient: StateFlow<PatientProfile> = _currentPatient.asStateFlow()

  // In-Memory Reactive Stores initialized with Seed Data
  private val _appointments = MutableStateFlow<List<Appointment>>(SeedData.initialAppointments)
  val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()

  private val _labOrders = MutableStateFlow<List<LabOrder>>(SeedData.initialLabOrders)
  val labOrders: StateFlow<List<LabOrder>> = _labOrders.asStateFlow()

  private val _prescriptions = MutableStateFlow<List<Prescription>>(SeedData.initialPrescriptions)
  val prescriptions: StateFlow<List<Prescription>> = _prescriptions.asStateFlow()

  private val _medicationReminders = MutableStateFlow<List<MedicationReminder>>(SeedData.initialMedicationReminders)
  val medicationReminders: StateFlow<List<MedicationReminder>> = _medicationReminders.asStateFlow()

  private val _pharmacyOrders = MutableStateFlow<List<PharmacyOrder>>(SeedData.initialPharmacyOrders)
  val pharmacyOrders: StateFlow<List<PharmacyOrder>> = _pharmacyOrders.asStateFlow()

  private val _bills = MutableStateFlow<List<Bill>>(SeedData.initialBills)
  val bills: StateFlow<List<Bill>> = _bills.asStateFlow()

  private val _medicalRecords = MutableStateFlow<List<MedicalRecord>>(SeedData.initialMedicalRecords)
  val medicalRecords: StateFlow<List<MedicalRecord>> = _medicalRecords.asStateFlow()

  private val _notifications = MutableStateFlow<List<NotificationItem>>(SeedData.initialNotifications)
  val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

  private val _documents = MutableStateFlow<List<UploadedDocument>>(SeedData.initialDocuments)
  val documents: StateFlow<List<UploadedDocument>> = _documents.asStateFlow()

  private val _familyMembers = MutableStateFlow<List<FamilyMember>>(SeedData.initialFamilyMembers)
  val familyMembers: StateFlow<List<FamilyMember>> = _familyMembers.asStateFlow()

  private val _familyHealthRecords = MutableStateFlow<List<FamilyHealthRecord>>(SeedData.initialFamilyHealthRecords)
  val familyHealthRecords: StateFlow<List<FamilyHealthRecord>> = _familyHealthRecords.asStateFlow()

  private val _vitals = MutableStateFlow<List<HealthVitalRecord>>(SeedData.initialVitals)
  val vitals: StateFlow<List<HealthVitalRecord>> = _vitals.asStateFlow()

  private val _weeklyHealthInsights = MutableStateFlow<WeeklyHealthInsightReport?>(null)
  val weeklyHealthInsights: StateFlow<WeeklyHealthInsightReport?> = _weeklyHealthInsights.asStateFlow()

  private val _queueStatus = MutableStateFlow(
    QueueStatus(
      appointmentId = "APT-1024",
      tokenNumber = "A-27",
      currentToken = "A-22",
      patientsAhead = 5,
      estimatedWaitMinutes = 25,
      doctorStatus = "IN_CONSULTATION",
      roomNumber = "Room 304, 3rd Floor"
    )
  )
  val queueStatus: StateFlow<QueueStatus> = _queueStatus.asStateFlow()

  private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
    listOf(
      ChatMessage(
        id = "msg-welcome",
        sender = "AI",
        message = "Hello Subham! I am CareAI, your dedicated CareNexus hospital assistant. How can I assist you with your health, appointments, or medical reports today?",
        timestamp = "Just now"
      )
    )
  )
  val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

  val doctors: List<Doctor> = SeedData.doctors
  val departments: List<Department> = SeedData.departments
  val pharmacyLocations: List<PharmacyLocation> = SeedData.pharmacyLocations

  val unreadNotificationsCount: StateFlow<Int> = _notifications.map { list ->
    list.count { !it.isRead }
  }.stateIn(scope, SharingStarted.Eagerly, 2)

  // Auth Methods
  fun login(email: String, password: String): Boolean {
    if (email.isNotBlank() && password.length >= 6) {
      _currentPatient.value = _currentPatient.value.copy(email = email)
      _isAuthenticated.value = true
      addAuditLog("LOGIN", "Patient logged in successfully with $email")
      return true
    }
    return false
  }

  fun register(patientProfile: PatientProfile): Boolean {
    _currentPatient.value = patientProfile
    _isAuthenticated.value = true
    addAuditLog("REGISTRATION", "New patient profile registered: ${patientProfile.patientId}")
    return true
  }

  fun updateProfile(updated: PatientProfile) {
    _currentPatient.value = updated
    addAuditLog("PROFILE_UPDATE", "Patient profile details updated")
  }

  fun logout() {
    _isAuthenticated.value = false
    addAuditLog("LOGOUT", "Patient signed out securely")
  }

  // Appointment Methods
  fun bookAppointment(
    doctorId: String,
    date: String,
    timeSlot: String,
    consultationType: String,
    reason: String
  ): Result<Appointment> {
    val doctor = doctors.find { it.id == doctorId } ?: return Result.failure(Exception("Doctor not found"))
    
    // Double booking prevention check
    val isDoubleBooked = _appointments.value.any {
      it.doctorId == doctorId && it.date == date && it.timeSlot == timeSlot &&
      it.status != AppointmentStatus.CANCELLED
    }
    if (isDoubleBooked) {
      return Result.failure(Exception("This slot ($timeSlot on $date) is already booked. Please select another time slot."))
    }

    val aptId = "APT-${1000 + Random().nextInt(9000)}"
    val tokenNumber = "${doctor.department.first().uppercaseChar()}-${Random().nextInt(40) + 10}"
    val newAppointment = Appointment(
      appointmentId = aptId,
      patientId = _currentPatient.value.patientId,
      doctorId = doctor.id,
      doctorName = doctor.name,
      department = doctor.department,
      date = date,
      timeSlot = timeSlot,
      consultationType = consultationType,
      status = AppointmentStatus.CONFIRMED,
      tokenNumber = tokenNumber,
      qrCodeData = "CARENEXUS://APT/$aptId/${_currentPatient.value.patientId}/TOKEN-$tokenNumber",
      queuePosition = 3,
      estimatedWaitMinutes = 15,
      reasonForVisit = reason.ifBlank { "Consultation with ${doctor.name}" },
      notes = "Arrive 10 minutes prior to ${doctor.roomNumber}"
    )

    _appointments.value = listOf(newAppointment) + _appointments.value

    // Auto-create bill for consultation
    val newBill = Bill(
      id = "BILL-${Random().nextInt(9000) + 1000}",
      patientId = _currentPatient.value.patientId,
      invoiceNumber = "INV-${Random().nextInt(90000) + 10000}",
      date = date,
      dueDate = date,
      items = listOf(BillItem("Consultation Fee (${doctor.name})", "Consultation", doctor.consultationFee)),
      subtotal = doctor.consultationFee,
      discount = 0.0,
      tax = 0.0,
      total = doctor.consultationFee,
      status = BillStatus.PENDING
    )
    _bills.value = listOf(newBill) + _bills.value

    // Notify patient
    addNotification(
      title = "Appointment Confirmed",
      message = "Your appointment with ${doctor.name} on $date at $timeSlot is confirmed (Token $tokenNumber).",
      category = NotificationCategory.APPOINTMENTS,
      route = "appointments"
    )

    addAuditLog("APPOINTMENT_BOOK", "Booked appointment $aptId with ${doctor.name}")
    return Result.success(newAppointment)
  }

  fun cancelAppointment(appointmentId: String, reason: String) {
    _appointments.value = _appointments.value.map {
      if (it.appointmentId == appointmentId) it.copy(status = AppointmentStatus.CANCELLED) else it
    }
    addNotification(
      title = "Appointment Cancelled",
      message = "Appointment $appointmentId has been cancelled. Reason: $reason",
      category = NotificationCategory.APPOINTMENTS,
      route = "appointments"
    )
    addAuditLog("APPOINTMENT_CANCEL", "Cancelled appointment $appointmentId")
  }

  fun advanceDigitalQueue() {
    val current = _queueStatus.value
    val currentTokenNum = current.currentToken.filter { it.isDigit() }.toIntOrNull() ?: 20
    val myTokenNum = current.tokenNumber.filter { it.isDigit() }.toIntOrNull() ?: 27
    
    val nextTokenNum = currentTokenNum + 1
    val prefix = current.currentToken.filter { it.isLetter() }.ifEmpty { "A" }
    val newCurrentToken = "$prefix-$nextTokenNum"
    val ahead = maxOf(0, myTokenNum - nextTokenNum)
    val wait = ahead * 5

    _queueStatus.value = current.copy(
      currentToken = newCurrentToken,
      patientsAhead = ahead,
      estimatedWaitMinutes = wait
    )

    if (ahead == 2) {
      addNotification(
        title = "Queue Update: 2 Patients Ahead",
        message = "Please proceed to Room 304. Your token ${current.tokenNumber} will be called shortly.",
        category = NotificationCategory.APPOINTMENTS,
        route = "queue"
      )
    } else if (ahead == 0) {
      addNotification(
        title = "Your Token is Called!",
        message = "Token ${current.tokenNumber} is now entering Room 304 with Dr. Rahul Sharma.",
        category = NotificationCategory.APPOINTMENTS,
        route = "queue"
      )
    }
  }

  fun processQrCheckIn(qrData: String): Pair<Boolean, String> {
    return if (qrData.startsWith("CARENEXUS://") || qrData.contains("APT-") || qrData.contains("CHECKIN")) {
      // Update appointment status to checked in
      _appointments.value = _appointments.value.map {
        if (it.status == AppointmentStatus.CONFIRMED) it.copy(status = AppointmentStatus.CHECKED_IN) else it
      }
      addNotification(
        title = "Hospital Check-In Successful",
        message = "You have checked in at CareNexus Reception. Your token is now active in the OPD queue.",
        category = NotificationCategory.APPOINTMENTS,
        route = "queue"
      )
      Pair(true, "Check-in successful! Your token has been registered in the live OPD queue.")
    } else {
      Pair(false, "Invalid hospital QR code format. Please scan a valid CareNexus QR.")
    }
  }

  // Lab Methods
  fun requestLabAiExplanation(labOrderId: String) {
    scope.launch {
      val order = _labOrders.value.find { it.id == labOrderId } ?: return@launch
      val resultsText = order.results.joinToString("\n") { "${it.name}: ${it.value} ${it.unit} (Ref: ${it.referenceRange})" }
      
      val explanation = geminiService.explainLabReport(order.testName, resultsText)
      _labOrders.value = _labOrders.value.map {
        if (it.id == labOrderId) it.copy(aiExplanation = explanation) else it
      }
    }
  }

  // Prescription & Pharmacy Methods
  fun sendPrescriptionToPharmacy(prescriptionId: String) {
    val rx = _prescriptions.value.find { it.id == prescriptionId } ?: return
    _prescriptions.value = _prescriptions.value.map {
      if (it.id == prescriptionId) it.copy(isSentToPharmacy = true) else it
    }

    val orderItems = rx.items.map {
      MedicineOrderItem(
        name = it.medicineName,
        dosage = it.dosage,
        quantity = 1,
        unitPrice = 18.50
      )
    }
    val totalPrice = orderItems.size * 18.50

    val pharmacyOrder = PharmacyOrder(
      id = "PHARM-${Random().nextInt(900) + 100}",
      prescriptionId = rx.id,
      prescriptionPhotoUri = null,
      patientId = rx.patientId,
      patientName = _currentPatient.value.fullName,
      pharmacyLocationId = "pharm-main",
      pharmacyName = "Main Hospital Central Pharmacy",
      pickupCounter = "Counter B - Main Hospital Pharmacy (1st Floor)",
      medicines = orderItems,
      totalPrice = totalPrice,
      status = PharmacyOrderStatus.PREPARING,
      orderDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
      orderTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()),
      pickupTokenNumber = "PH-${Random().nextInt(80) + 10}",
      specialInstructions = "Prescription verified by hospital pharmacist.",
      prescriptionType = "DOCTOR_DIGITAL_RX"
    )

    _pharmacyOrders.value = listOf(pharmacyOrder) + _pharmacyOrders.value

    addNotification(
      title = "Prescription Sent to Pharmacy",
      message = "Order #${pharmacyOrder.id} is now being prepared by hospital pharmacists.",
      category = NotificationCategory.PHARMACY,
      route = "pharmacy"
    )
    addAuditLog("PRESCRIPTION_FORWARD", "Forwarded $prescriptionId to pharmacy")
  }

  fun createPharmacyOrderFromUpload(
    pharmacyLocationId: String,
    prescriptionPhotoUri: String,
    medicineNotes: String
  ): PharmacyOrder {
    val location = pharmacyLocations.find { it.id == pharmacyLocationId } ?: pharmacyLocations.first()
    val orderId = "PHARM-${Random().nextInt(900) + 100}"
    val tokenNum = "PH-${Random().nextInt(80) + 10}"
    val now = Date()
    val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now)
    val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(now)

    val simulatedItems = listOf(
      MedicineOrderItem("Prescribed Rx Medication (Verified by Pharmacist)", "Standard Dosage", 1, 22.00),
      MedicineOrderItem("Dispensing & Compounding Fee", "Clinical Unit", 1, 4.50)
    )

    val newOrder = PharmacyOrder(
      id = orderId,
      prescriptionId = "PHOTO-RX-${Random().nextInt(9000) + 1000}",
      prescriptionPhotoUri = prescriptionPhotoUri,
      patientId = _currentPatient.value.patientId,
      patientName = _currentPatient.value.fullName,
      pharmacyLocationId = location.id,
      pharmacyName = location.name,
      pickupCounter = "${location.counterName} (${location.floor})",
      medicines = simulatedItems,
      totalPrice = 26.50,
      status = PharmacyOrderStatus.PENDING,
      orderDate = dateStr,
      orderTime = timeStr,
      readyAt = null,
      pickupTokenNumber = tokenNum,
      specialInstructions = medicineNotes.ifBlank { "Patient photo prescription uploaded for dispensary verification." },
      prescriptionType = "PATIENT_UPLOADED_PHOTO"
    )

    _pharmacyOrders.value = listOf(newOrder) + _pharmacyOrders.value

    addNotification(
      title = "E-Prescription Order Submitted (#$orderId)",
      message = "Your prescription photo has been submitted to ${location.name}. Token: $tokenNum.",
      category = NotificationCategory.PHARMACY,
      route = "pharmacy"
    )
    addAuditLog("PHARMACY_UPLOAD_ORDER", "Created order $orderId at ${location.name}")
    return newOrder
  }

  fun advancePharmacyOrderStatus(orderId: String) {
    val currentOrder = _pharmacyOrders.value.find { it.id == orderId } ?: return
    val nextStatus = when (currentOrder.status) {
      PharmacyOrderStatus.PENDING -> PharmacyOrderStatus.VERIFIED
      PharmacyOrderStatus.VERIFIED -> PharmacyOrderStatus.PREPARING
      PharmacyOrderStatus.PREPARING -> PharmacyOrderStatus.READY
      PharmacyOrderStatus.READY -> PharmacyOrderStatus.COLLECTED
      PharmacyOrderStatus.COLLECTED -> PharmacyOrderStatus.COLLECTED
      PharmacyOrderStatus.CANCELLED -> PharmacyOrderStatus.CANCELLED
    }

    val readyTimeStr = if (nextStatus == PharmacyOrderStatus.READY) {
      SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()).format(Date())
    } else currentOrder.readyAt

    _pharmacyOrders.value = _pharmacyOrders.value.map {
      if (it.id == orderId) it.copy(status = nextStatus, readyAt = readyTimeStr) else it
    }

    if (nextStatus == PharmacyOrderStatus.READY) {
      addNotification(
        title = "Pharmacy Order #${currentOrder.id} is READY!",
        message = "Your medications are packed and ready for pickup at ${currentOrder.pickupCounter} (Token ${currentOrder.pickupTokenNumber}).",
        category = NotificationCategory.PHARMACY,
        route = "pharmacy"
      )
    } else if (nextStatus == PharmacyOrderStatus.COLLECTED) {
      addNotification(
        title = "Medication Pickup Completed",
        message = "Order #${currentOrder.id} has been marked as collected. Thank you for using CareNexus Pharmacy.",
        category = NotificationCategory.PHARMACY,
        route = "pharmacy"
      )
    }
  }

  // Medication Reminders Methods
  fun addMedicationReminder(
    prescriptionId: String?,
    medicineName: String,
    dosage: String,
    timeSlot: String,
    timePeriod: String,
    frequency: String,
    instructions: String
  ) {
    val newReminder = MedicationReminder(
      id = "REM-${Random().nextInt(9000) + 1000}",
      prescriptionId = prescriptionId,
      medicineName = medicineName,
      dosage = dosage,
      timeSlot = timeSlot,
      timePeriod = timePeriod,
      frequency = frequency,
      instructions = instructions,
      isActive = true,
      isTakenToday = false,
      takenAtTime = null,
      streakDays = 1
    )
    _medicationReminders.value = _medicationReminders.value + newReminder
    addNotification(
      title = "Medication Reminder Configured",
      message = "Daily alarm set for $medicineName ($dosage) at $timeSlot.",
      category = NotificationCategory.SYSTEM,
      route = "medication_reminders"
    )
    addAuditLog("REMINDER_CREATE", "Configured reminder for $medicineName")
  }

  fun toggleMedicationReminderActive(reminderId: String) {
    _medicationReminders.value = _medicationReminders.value.map {
      if (it.id == reminderId) it.copy(isActive = !it.isActive) else it
    }
  }

  fun markMedicationTaken(reminderId: String) {
    val timeNow = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
    val rem = _medicationReminders.value.find { it.id == reminderId } ?: return

    _medicationReminders.value = _medicationReminders.value.map {
      if (it.id == reminderId) {
        it.copy(
          isTakenToday = true,
          takenAtTime = "Today, $timeNow",
          streakDays = it.streakDays + 1
        )
      } else it
    }

    addNotification(
      title = "Dose Recorded: ${rem.medicineName}",
      message = "Great job! ${rem.dosage} logged at $timeNow. Current adherence streak: ${rem.streakDays + 1} days.",
      category = NotificationCategory.SYSTEM,
      route = "medication_reminders"
    )
    addAuditLog("MEDICATION_TAKEN", "Logged dose for ${rem.medicineName}")
  }

  fun triggerMedicationAlert(reminderId: String) {
    val rem = _medicationReminders.value.find { it.id == reminderId } ?: return
    addNotification(
      title = "⏰ Medication Alert: ${rem.medicineName}",
      message = "Time to take your scheduled dose: ${rem.dosage} (${rem.instructions}).",
      category = NotificationCategory.SYSTEM,
      route = "medication_reminders"
    )
  }

  fun importRemindersFromPrescriptions() {
    val existingNames = _medicationReminders.value.map { it.medicineName.lowercase() }.toSet()
    val newReminders = mutableListOf<MedicationReminder>()

    _prescriptions.value.forEach { rx ->
      rx.items.forEach { item ->
        if (!existingNames.contains(item.medicineName.lowercase())) {
          val timePeriod = when {
            item.frequency.contains("Bedtime", true) || item.frequency.contains("Night", true) -> "BEDTIME"
            item.frequency.contains("Evening", true) -> "EVENING"
            item.frequency.contains("Afternoon", true) || item.frequency.contains("Lunch", true) -> "AFTERNOON"
            else -> "MORNING"
          }
          val timeSlot = when (timePeriod) {
            "BEDTIME" -> "09:30 PM"
            "EVENING" -> "07:00 PM"
            "AFTERNOON" -> "01:00 PM"
            else -> "08:00 AM"
          }
          newReminders.add(
            MedicationReminder(
              id = "REM-${Random().nextInt(9000) + 1000}",
              prescriptionId = rx.id,
              medicineName = item.medicineName,
              dosage = item.dosage,
              timeSlot = timeSlot,
              timePeriod = timePeriod,
              frequency = item.frequency,
              instructions = item.instructions,
              isActive = true,
              isTakenToday = false,
              takenAtTime = null,
              streakDays = 1
            )
          )
        }
      }
    }

    if (newReminders.isNotEmpty()) {
      _medicationReminders.value = _medicationReminders.value + newReminders
      addNotification(
        title = "Prescriptions Imported to Daily Schedule",
        message = "Added ${newReminders.size} new medication reminders from your verified doctor e-prescriptions.",
        category = NotificationCategory.SYSTEM,
        route = "medication_reminders"
      )
    }
  }

  fun deleteMedicationReminder(reminderId: String) {
    _medicationReminders.value = _medicationReminders.value.filter { it.id != reminderId }
  }

  // Billing & Payment Methods
  fun payBill(billId: String, paymentMethod: String): Result<String> {
    val bill = _bills.value.find { it.id == billId } ?: return Result.failure(Exception("Bill not found"))
    val txnId = "TXN-${Random().nextInt(9000000) + 1000000}-${paymentMethod.take(4).uppercase()}"
    val now = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()).format(Date())

    _bills.value = _bills.value.map {
      if (it.id == billId) {
        it.copy(
          status = BillStatus.PAID,
          transactionId = txnId,
          paidAt = now,
          paymentMethod = paymentMethod
        )
      } else it
    }

    addNotification(
      title = "Payment Successful ($${String.format(Locale.US, "%.2f", bill.total)})",
      message = "Invoice ${bill.invoiceNumber} paid successfully. Receipt #$txnId generated.",
      category = NotificationCategory.PAYMENTS,
      route = "billing"
    )
    addAuditLog("PAYMENT_SUCCESS", "Paid bill $billId via $paymentMethod ($txnId)")
    return Result.success(txnId)
  }

  // Document Upload & AI Classification
  fun uploadDocument(
    name: String,
    uri: String,
    size: String,
    manualCategory: DocumentCategory? = null
  ) {
    scope.launch {
      val (detectedCategory, confidence) = if (manualCategory != null) {
        Pair(manualCategory.name, 1.0f)
      } else {
        geminiService.classifyDocument(name, "Medical record document uploaded by patient")
      }

      val catEnum = try {
        DocumentCategory.valueOf(detectedCategory)
      } catch (e: Exception) {
        DocumentCategory.MEDICAL_RECORD
      }

      val doc = UploadedDocument(
        id = "DOC-${Random().nextInt(900) + 100}",
        patientId = _currentPatient.value.patientId,
        fileName = name,
        category = catEnum,
        fileUri = uri,
        uploadDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
        fileSize = size,
        aiClassified = manualCategory == null,
        aiConfidence = confidence
      )

      _documents.value = listOf(doc) + _documents.value

      addNotification(
        title = "Document Uploaded & Categorized",
        message = "'$name' was filed under ${catEnum.name.replace("_", " ")}.",
        category = NotificationCategory.SYSTEM,
        route = "records"
      )
      addAuditLog("DOCUMENT_UPLOAD", "Uploaded document ${doc.id}")
    }
  }

  fun deleteDocument(id: String) {
    _documents.value = _documents.value.filter { it.id != id }
    addAuditLog("DOCUMENT_DELETE", "Deleted document $id")
  }

  // Family Members & Health Records (Room-synced)
  fun addFamilyMember(member: FamilyMember) {
    _familyMembers.value = _familyMembers.value + member
    scope.launch {
      dao.insertFamilyMember(
        com.example.core.database.FamilyMemberEntity(
          id = member.id,
          primaryPatientId = member.primaryPatientId,
          name = member.name,
          relationship = member.relationship,
          dateOfBirth = member.dateOfBirth,
          gender = member.gender,
          bloodGroup = member.bloodGroup,
          phone = member.phone,
          emergencyContact = member.emergencyContact,
          allergies = member.allergies,
          chronicConditions = member.chronicConditions,
          insuranceId = member.insuranceId
        )
      )
    }
    addNotification(
      title = "Family Member Added",
      message = "${member.name} (${member.relationship}) was linked to your shared family health profile.",
      category = NotificationCategory.SYSTEM,
      route = "family"
    )
    addAuditLog("FAMILY_ADD", "Added family member ${member.name}")
  }

  fun deleteFamilyMember(id: String) {
    _familyMembers.value = _familyMembers.value.filter { it.id != id }
    _familyHealthRecords.value = _familyHealthRecords.value.filter { it.familyMemberId != id }
    scope.launch {
      dao.deleteFamilyMember(id)
    }
    addAuditLog("FAMILY_DELETE", "Removed family member $id")
  }

  fun addFamilyHealthRecord(record: FamilyHealthRecord) {
    _familyHealthRecords.value = listOf(record) + _familyHealthRecords.value
    scope.launch {
      dao.insertFamilyHealthRecord(
        com.example.core.database.FamilyHealthRecordEntity(
          id = record.id,
          familyMemberId = record.familyMemberId,
          memberName = record.memberName,
          recordType = record.recordType,
          title = record.title,
          doctorName = record.doctorName,
          department = record.department,
          date = record.date,
          summary = record.summary,
          details = record.details,
          status = record.status,
          attachmentName = record.attachmentName
        )
      )
    }
    addNotification(
      title = "Family Health Record Added",
      message = "New ${record.recordType.lowercase()} recorded for ${record.memberName}: ${record.title}",
      category = NotificationCategory.SYSTEM,
      route = "family"
    )
    addAuditLog("FAMILY_RECORD_ADD", "Added ${record.recordType} for ${record.memberName}")
  }

  fun deleteFamilyHealthRecord(id: String) {
    _familyHealthRecords.value = _familyHealthRecords.value.filter { it.id != id }
    scope.launch {
      dao.deleteFamilyHealthRecord(id)
    }
    addAuditLog("FAMILY_RECORD_DELETE", "Removed family record $id")
  }

  suspend fun generateFamilyHealthSummaryWithAi(member: FamilyMember, records: List<FamilyHealthRecord>): String {
    val recSummary = if (records.isEmpty()) {
      "No historical hospital records logged yet."
    } else {
      records.joinToString("\n") {
        "- [${it.date}] ${it.recordType} (${it.doctorName}, ${it.department}): ${it.title} - ${it.summary}"
      }
    }
    val prompt = """
      Act as a compassionate family medical counselor for CareNexus Hospital. Provide an AI health summary for family member:
      Name: ${member.name}
      Relationship: ${member.relationship}
      DOB: ${member.dateOfBirth} (Blood Group: ${member.bloodGroup})
      Known Allergies: ${member.allergies}
      Chronic Conditions: ${member.chronicConditions}
      Insurance: ${member.insuranceId}
      
      Clinical Records:
      $recSummary
      
      Provide a structured 3-part overview:
      1. Health Status & Immunization/Care Overview
      2. Preventive Care & Upcoming Milestones (Age-appropriate for ${member.relationship})
      3. Practical Family Care Tips
      Include medical disclaimer.
    """.trimIndent()
    return geminiService.askCareAi(prompt, recSummary)
  }

  // CareAI Chat & Voice Command System
  fun sendChatMessage(userText: String) {
    val userMsg = ChatMessage(
      id = "msg-${System.currentTimeMillis()}",
      sender = "USER",
      message = userText,
      timestamp = "Just now"
    )
    val loadingMsg = ChatMessage(
      id = "msg-loading-${System.currentTimeMillis()}",
      sender = "AI",
      message = "",
      timestamp = "Thinking...",
      isLoading = true
    )

    _chatMessages.value = _chatMessages.value + userMsg + loadingMsg

    scope.launch {
      val patient = _currentPatient.value
      val nextApt = _appointments.value.firstOrNull { it.status == AppointmentStatus.CONFIRMED }
      val pendingBill = _bills.value.firstOrNull { it.status == BillStatus.PENDING }
      val latestLab = _labOrders.value.firstOrNull()

      val contextSummary = """
        Patient: ${patient.fullName} (ID: ${patient.patientId}, Blood: ${patient.bloodGroup})
        Next Appointment: ${nextApt?.let { "${it.doctorName} (${it.department}) on ${it.date} at ${it.timeSlot}, Token ${it.tokenNumber}" } ?: "None"}
        Pending Bill: ${pendingBill?.let { "$${it.total} for ${it.invoiceNumber}" } ?: "None"}
        Latest Lab: ${latestLab?.let { "${it.testName} (${it.status})" } ?: "None"}
        Live Queue: Token ${_queueStatus.value.tokenNumber} (Current Token ${_queueStatus.value.currentToken}, ${_queueStatus.value.patientsAhead} patients ahead)
      """.trimIndent()

      val aiResponse = geminiService.askCareAi(userText, contextSummary)

      val actionRequired = userText.contains("cancel", true) && userText.contains("appointment", true)
      val actionLabel = if (actionRequired) "Confirm Cancellation" else null

      val finalAiMsg = ChatMessage(
        id = "msg-ai-${System.currentTimeMillis()}",
        sender = "AI",
        message = aiResponse,
        timestamp = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()),
        actionRequired = actionRequired,
        actionLabel = actionLabel,
        actionPayload = nextApt?.appointmentId
      )

      _chatMessages.value = _chatMessages.value.filter { !it.isLoading } + finalAiMsg
    }
  }

  // Vitals Management
  fun logVitalReading(
    type: VitalType,
    value: String,
    primary: Float,
    secondary: Float? = null,
    status: VitalStatus = VitalStatus.NORMAL,
    notes: String = "",
    source: String = "Manual Log"
  ): HealthVitalRecord {
    val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
    val record = HealthVitalRecord(
      id = "VIT-${System.currentTimeMillis()}",
      patientId = _currentPatient.value.patientId,
      type = type,
      value = value,
      numericPrimary = primary,
      numericSecondary = secondary,
      recordedAt = dateStr,
      recordedTime = timeStr,
      status = status,
      notes = notes,
      source = source
    )
    _vitals.value = listOf(record) + _vitals.value
    addNotification(
      title = "New Vital Recorded",
      message = "${type.displayName}: $value ${type.unit} was recorded ($status).",
      category = NotificationCategory.SYSTEM,
      route = "vitals"
    )
    addAuditLog("LOG_VITAL", "Recorded ${type.displayName} reading: $value")
    return record
  }

  fun deleteVitalReading(id: String) {
    _vitals.value = _vitals.value.filter { it.id != id }
  }

  suspend fun analyzeVitalsWithAi(vitals: List<HealthVitalRecord>): String {
    val summary = vitals.joinToString("\n") { 
      "- ${it.type.displayName}: ${it.value} ${it.type.unit} (${it.recordedAt} ${it.recordedTime}) [${it.status}]" 
    }
    val prompt = """
      Act as a clinical health educator for CareNexus Hospital. Analyze the following vital signs history for patient Subham Pradhan:
      $summary
      
      Provide a concise 3-part medical assessment:
      1. Overall Cardiovascular & Metabolic Summary
      2. Positive Trends / Markers to Celebrate
      3. Actionable Lifestyle / Clinical Advice (diet, exercise, follow-up)
      Include medical educational disclaimers.
    """.trimIndent()
    return geminiService.askCareAi(prompt, summary)
  }

  // Weekly Health Insights (Gemini Multi-Source Aggregation)
  suspend fun generateWeeklyHealthInsights(weekPeriod: String = "Sep 23 - Sep 29, 2026"): WeeklyHealthInsightReport {
    val patient = _currentPatient.value
    val records = _medicalRecords.value
    val labs = _labOrders.value
    val rx = _prescriptions.value
    val reminders = _medicationReminders.value
    val vitalsList = _vitals.value

    val recordsText = records.take(3).joinToString("\n") {
      "- [${it.date}] ${it.doctorName} (${it.department}): ${it.title} | ${it.summary} | Vitals: ${it.vitals}"
    }

    val labsText = labs.take(3).joinToString("\n") {
      val abnormalCount = it.results.count { r -> r.isAbnormal }
      "- [${it.date}] ${it.testName} (${it.status}): ${it.reportSummary}. Abnormal flags: $abnormalCount"
    }

    val rxText = rx.joinToString("\n") { p ->
      val items = p.items.joinToString(", ") { "${it.medicineName} (${it.dosage}, ${it.frequency})" }
      "- Diagnosis: ${p.diagnosis} by ${p.doctorName}. Regimen: $items"
    }

    val vitalsText = vitalsList.take(6).joinToString("\n") {
      "- ${it.type.displayName}: ${it.value} ${it.type.unit} (${it.recordedAt}) [${it.status}]"
    }

    val longestStreak = reminders.maxOfOrNull { it.streakDays } ?: 12

    val aiSynthesis = geminiService.generateComprehensiveHealthInsights(
      patientName = patient.fullName,
      medicalRecordsText = recordsText,
      labReportsText = labsText,
      prescriptionsText = rxText,
      vitalsText = vitalsText,
      adherenceStreak = longestStreak
    )

    val report = WeeklyHealthInsightReport(
      id = "INSIGHT-${System.currentTimeMillis()}",
      weekPeriod = weekPeriod,
      healthScore = 88,
      scoreDelta = "+4% from last week",
      statusDescription = "Optimal Wellness & High Adherence",
      executiveSummary = aiSynthesis,
      cardiovascularTrend = "Resting BP at 120/78 mmHg and Heart Rate at 72 BPM indicate optimal autonomic control.",
      medicationAdherenceTrend = "Completed 12-day consecutive adherence streak (96% dosage regularity).",
      diagnosticLabTrend = "Lipid Panel and metabolic biomarkers stabilized under current therapy protocol.",
      recommendedNextSteps = listOf(
        "Maintain daily 30-minute aerobic routine and low sodium dietary intake.",
        "Ensure consistent 2.5L hydration daily for optimal metabolic clearance.",
        "Attend scheduled Cardiology routine review on Sep 30 at 10:30 AM (Room 304)."
      ),
      vitalStabilityScore = 94,
      adherencePercentage = 96,
      activeMedicationsCount = rx.sumOf { it.items.size },
      generatedAt = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date()),
      isAiGenerated = true
    )

    _weeklyHealthInsights.value = report
    addNotification(
      title = "Weekly Health Trend Report Ready",
      message = "CareAI has generated your holistic health insights report for $weekPeriod.",
      category = NotificationCategory.SYSTEM,
      route = "health_insights"
    )
    addAuditLog("HEALTH_INSIGHTS_GEN", "Generated weekly health insights for $weekPeriod")
    return report
  }

  // Symptom Checker AI Integration
  suspend fun checkSymptoms(
    userText: String,
    turnCount: Int,
    history: List<Pair<String, String>>
  ): Pair<String, SymptomTriageResult?> {
    val result = geminiService.checkSymptomsWithAi(userText, turnCount, history)
    addAuditLog("SYMPTOM_CHECK", "Checked symptoms: ${userText.take(50)}")
    return result
  }

  // Notifications
  fun markNotificationAsRead(id: String) {
    _notifications.value = _notifications.value.map {
      if (it.id == id) it.copy(isRead = true) else it
    }
  }

  fun addNotification(
    title: String,
    message: String,
    category: NotificationCategory,
    route: String? = null
  ) {
    val notif = NotificationItem(
      id = "NOTIF-${System.currentTimeMillis()}",
      patientId = _currentPatient.value.patientId,
      title = title,
      message = message,
      timestamp = "Just now",
      category = category,
      isRead = false,
      deepLinkRoute = route
    )
    _notifications.value = listOf(notif) + _notifications.value
  }

  private fun addAuditLog(action: String, details: String) {
    // In production, syncs to audit_logs collection
  }

  companion object {
    @Volatile
    private var INSTANCE: CareNexusRepository? = null

    fun getInstance(context: Context): CareNexusRepository {
      return INSTANCE ?: synchronized(this) {
        INSTANCE ?: CareNexusRepository(context.applicationContext).also { INSTANCE = it }
      }
    }
  }
}
