package com.example.core.model

data class PatientProfile(
  val patientId: String = "PAT-2026-089",
  val fullName: String = "Subham Pradhan",
  val email: String = "pradhansubham024@gmail.com",
  val phone: String = "+1 (555) 234-5678",
  val dateOfBirth: String = "1994-08-15",
  val gender: String = "Male",
  val bloodGroup: String = "O+",
  val address: String = "450 Medical Heights Blvd, Suite 300, Metro City",
  val emergencyContactName: String = "Anita Pradhan",
  val emergencyContactPhone: String = "+1 (555) 987-6543",
  val emergencyRelationship: String = "Spouse",
  val allergies: String = "Penicillin, Peanuts",
  val profilePhotoUri: String? = null,
  val role: String = "PATIENT"
)

data class Doctor(
  val id: String,
  val name: String,
  val department: String,
  val specialization: String,
  val experienceYears: Int,
  val rating: Double,
  val reviewCount: Int,
  val consultationFee: Double,
  val availableSlots: List<String>,
  val hospitalName: String = "CareNexus Central Hospital",
  val roomNumber: String,
  val qualifications: String,
  val bio: String
)

data class Department(
  val id: String,
  val name: String,
  val iconName: String,
  val description: String,
  val headDoctor: String,
  val floorNumber: String,
  val roomRange: String
)

enum class AppointmentStatus {
  REQUESTED,
  CONFIRMED,
  CHECKED_IN,
  IN_QUEUE,
  IN_CONSULTATION,
  COMPLETED,
  CANCELLED,
  NO_SHOW
}

data class Appointment(
  val appointmentId: String,
  val patientId: String,
  val doctorId: String,
  val doctorName: String,
  val department: String,
  val date: String,
  val timeSlot: String,
  val consultationType: String = "IN_PERSON", // IN_PERSON, VIDEO_CONSULTATION
  val status: AppointmentStatus = AppointmentStatus.CONFIRMED,
  val tokenNumber: String,
  val qrCodeData: String,
  val queuePosition: Int = 1,
  val estimatedWaitMinutes: Int = 15,
  val reasonForVisit: String = "General Routine Health Check",
  val notes: String = "Please arrive 10 minutes before your slot"
)

data class QueueStatus(
  val appointmentId: String,
  val tokenNumber: String,
  val currentToken: String,
  val patientsAhead: Int,
  val estimatedWaitMinutes: Int,
  val doctorStatus: String = "IN_CONSULTATION",
  val roomNumber: String = "Room 304, 3rd Floor"
)

enum class MedicalRecordType {
  CONSULTATION,
  DIAGNOSIS,
  LAB_REPORT,
  PRESCRIPTION,
  DISCHARGE_SUMMARY,
  UPLOADED_DOC
}

data class MedicalRecord(
  val id: String,
  val patientId: String,
  val doctorId: String,
  val doctorName: String,
  val department: String,
  val date: String,
  val type: MedicalRecordType,
  val title: String,
  val description: String,
  val summary: String,
  val vitals: String = "BP: 122/78 mmHg • HR: 68 bpm • SpO2: 99% • Temp: 98.4°F",
  val clinicalNotes: String = "Patient alert and oriented. Regular cardiac rhythm with no murmurs. Lungs clear to auscultation bilaterally.",
  val treatmentPlan: String = "1. Maintain low sodium diet\n2. Continue daily 30 min cardio\n3. Take prescribed cardioprotective therapy at bedtime\n4. Re-check lipid panel in 90 days",
  val doctorSignature: String = "Dr. Rahul Sharma, MD, FACC (USA) • DigCert#9942",
  val facility: String = "CareNexus Central Hospital - OPD Wing",
  val isDoctorVerified: Boolean = true,
  val attachments: List<String> = listOf("Consultation_Summary.pdf", "ECG_Tracing_Sep2026.pdf"),
  val fileSize: String = "2.4 MB"
)

data class LabParam(
  val name: String,
  val value: String,
  val unit: String,
  val referenceRange: String,
  val isAbnormal: Boolean = false
)

enum class LabOrderStatus {
  ORDERED,
  SAMPLE_PENDING,
  SAMPLE_COLLECTED,
  PROCESSING,
  COMPLETED,
  VERIFIED
}

data class LabOrder(
  val id: String,
  val patientId: String,
  val testName: String,
  val doctorName: String,
  val department: String,
  val date: String,
  val status: LabOrderStatus,
  val sampleType: String,
  val sampleCollectedAt: String? = null,
  val completedAt: String? = null,
  val results: List<LabParam> = emptyList(),
  val reportSummary: String = "",
  val aiExplanation: String? = null
)

data class PrescriptionItem(
  val medicineName: String,
  val dosage: String,
  val frequency: String,
  val duration: String,
  val instructions: String,
  val isSentToPharmacy: Boolean = false
)

data class Prescription(
  val id: String,
  val patientId: String,
  val doctorName: String,
  val department: String,
  val date: String,
  val diagnosis: String,
  val items: List<PrescriptionItem>,
  val isSentToPharmacy: Boolean = false,
  val notes: String = "Take with meals. Stay hydrated."
)

data class MedicationReminder(
  val id: String,
  val prescriptionId: String? = null,
  val medicineName: String,
  val dosage: String,
  val timeSlot: String,
  val timePeriod: String = "MORNING", // MORNING, AFTERNOON, EVENING, BEDTIME
  val frequency: String = "Once Daily",
  val instructions: String = "Take with water after food",
  val isActive: Boolean = true,
  val isTakenToday: Boolean = false,
  val takenAtTime: String? = null,
  val streakDays: Int = 12
)

enum class PharmacyOrderStatus {
  PENDING,
  VERIFIED,
  PREPARING,
  READY,
  COLLECTED,
  CANCELLED
}

data class MedicineOrderItem(
  val name: String,
  val dosage: String,
  val quantity: Int,
  val unitPrice: Double
)

data class PharmacyLocation(
  val id: String,
  val name: String,
  val building: String,
  val floor: String,
  val counterName: String,
  val operatingHours: String,
  val contactPhone: String,
  val distanceMeters: Int,
  val currentWaitMinutes: Int = 10,
  val isOpen24Hours: Boolean = false
)

data class PharmacyOrder(
  val id: String,
  val prescriptionId: String,
  val prescriptionPhotoUri: String? = null,
  val patientId: String,
  val patientName: String = "Subham Pradhan",
  val pharmacyLocationId: String = "pharm-main",
  val pharmacyName: String = "Main Hospital Central Pharmacy",
  val pickupCounter: String = "Counter B - Main Pharmacy (1st Floor)",
  val medicines: List<MedicineOrderItem>,
  val totalPrice: Double,
  val status: PharmacyOrderStatus,
  val orderDate: String,
  val orderTime: String = "02:30 PM",
  val readyAt: String? = null,
  val pickupTokenNumber: String = "PH-48",
  val specialInstructions: String = "Please review dosage with pharmacist upon collection.",
  val prescriptionType: String = "DOCTOR_DIGITAL_RX" // DOCTOR_DIGITAL_RX or PATIENT_UPLOADED_PHOTO
)

enum class BillStatus {
  PENDING,
  PAID,
  FAILED,
  REFUNDED
}

data class BillItem(
  val name: String,
  val category: String,
  val amount: Double
)

data class Bill(
  val id: String,
  val patientId: String,
  val invoiceNumber: String,
  val date: String,
  val dueDate: String,
  val items: List<BillItem>,
  val subtotal: Double,
  val discount: Double = 0.0,
  val tax: Double = 0.0,
  val total: Double,
  val status: BillStatus,
  val transactionId: String? = null,
  val paidAt: String? = null,
  val paymentMethod: String? = null
)

enum class NotificationCategory {
  APPOINTMENTS,
  LABORATORY,
  PHARMACY,
  PAYMENTS,
  HOSPITAL,
  EMERGENCY,
  SYSTEM
}

data class NotificationItem(
  val id: String,
  val patientId: String,
  val title: String,
  val message: String,
  val timestamp: String,
  val category: NotificationCategory,
  val isRead: Boolean = false,
  val deepLinkRoute: String? = null
)

enum class DocumentCategory {
  LAB_REPORT,
  PRESCRIPTION,
  MEDICAL_RECORD,
  INSURANCE,
  IDENTITY_DOCUMENT,
  OTHER
}

data class UploadedDocument(
  val id: String,
  val patientId: String,
  val fileName: String,
  val category: DocumentCategory,
  val fileUri: String,
  val uploadDate: String,
  val fileSize: String,
  val aiClassified: Boolean = true,
  val aiConfidence: Float = 0.94f
)

data class FamilyMember(
  val id: String,
  val primaryPatientId: String = "PAT-2026-089",
  val name: String,
  val relationship: String,
  val dateOfBirth: String,
  val gender: String,
  val bloodGroup: String,
  val phone: String,
  val emergencyContact: String,
  val allergies: String = "None reported",
  val chronicConditions: String = "None",
  val insuranceId: String = "INS-CARE-9021"
)

data class FamilyHealthRecord(
  val id: String,
  val familyMemberId: String,
  val memberName: String,
  val recordType: String, // CONSULTATION, VACCINATION, LAB_REPORT, PRESCRIPTION, GROWTH_RECORD
  val title: String,
  val doctorName: String,
  val department: String,
  val date: String,
  val summary: String,
  val details: String,
  val status: String = "VERIFIED",
  val attachmentName: String? = null
)


data class HospitalInfo(
  val name: String = "CareNexus Multi-Specialty Hospital & Medical Research Center",
  val address: String = "450 Medical Heights Blvd, Metro City, NY 10001",
  val emergencyPhone: String = "+1 (800) 555-9111",
  val generalPhone: String = "+1 (800) 555-CARE",
  val visitingHours: String = "General Ward: 10:00 AM - 12:00 PM, 4:00 PM - 7:00 PM\nICU: 11:00 AM - 12:00 PM, 5:00 PM - 6:00 PM",
  val opdTimings: String = "Monday to Saturday: 8:00 AM - 8:00 PM\nSunday: 9:00 AM - 1:00 PM (Emergency 24/7)"
)

data class IndoorWaypoint(
  val floor: String,
  val department: String,
  val room: String,
  val counter: String,
  val stepInstruction: String
)

data class ChatMessage(
  val id: String,
  val sender: String, // "USER" or "AI"
  val message: String,
  val timestamp: String,
  val intentType: String? = null,
  val actionRequired: Boolean = false,
  val actionLabel: String? = null,
  val actionPayload: String? = null,
  val isLoading: Boolean = false
)

enum class VitalType(val displayName: String, val unit: String, val normalRange: String) {
  BLOOD_PRESSURE("Blood Pressure", "mmHg", "110-125 / 70-80"),
  HEART_RATE("Heart Rate", "BPM", "60-100"),
  BLOOD_GLUCOSE("Blood Glucose (Fasting)", "mg/dL", "70-99"),
  SPO2("Blood Oxygen", "%", "95-100"),
  TEMPERATURE("Body Temperature", "°F", "97.8-99.1"),
  WEIGHT("Weight & BMI", "kg", "BMI 18.5-24.9")
}

enum class VitalStatus {
  NORMAL,
  ELEVATED,
  CRITICAL,
  OPTIMAL
}

data class HealthVitalRecord(
  val id: String,
  val patientId: String = "PAT-2026-089",
  val type: VitalType,
  val value: String,
  val numericPrimary: Float,
  val numericSecondary: Float? = null,
  val recordedAt: String,
  val recordedTime: String,
  val status: VitalStatus = VitalStatus.NORMAL,
  val notes: String = "",
  val source: String = "Manual Log" // Manual Log, CareNexus HealthSync, Wearable
)

data class WeeklyHealthInsightReport(
  val id: String,
  val weekPeriod: String, // e.g. "Sep 23 - Sep 29, 2026"
  val healthScore: Int = 88,
  val scoreDelta: String = "+4% from last week",
  val statusDescription: String = "Optimal Wellness & Medication Adherence",
  val executiveSummary: String,
  val cardiovascularTrend: String,
  val medicationAdherenceTrend: String,
  val diagnosticLabTrend: String,
  val recommendedNextSteps: List<String>,
  val vitalStabilityScore: Int = 94,
  val adherencePercentage: Int = 96,
  val activeMedicationsCount: Int = 3,
  val generatedAt: String,
  val isAiGenerated: Boolean = true
)

data class SymptomChatMessage(
  val id: String,
  val sender: String, // "USER" or "AI"
  val message: String,
  val timestamp: String,
  val isFollowUpQuestion: Boolean = false,
  val suggestedQuickReplies: List<String> = emptyList(),
  val triageRecommendation: SymptomTriageResult? = null
)

data class SymptomTriageResult(
  val primaryDepartment: String, // e.g. Cardiology, Neurology, Orthopedics, General Medicine, Dermatology, Pediatrics
  val departmentId: String,
  val recommendedDoctorId: String,
  val recommendedDoctorName: String,
  val urgencyLevel: String = "MODERATE", // ROUTINE, MODERATE, PRIORITY_SAME_DAY, EMERGENCY_ALERT
  val reasoningSummary: String,
  val careAdvice: String,
  val redFlagWarning: String? = null
)



