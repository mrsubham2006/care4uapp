package com.example.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patient_profiles")
data class PatientProfileEntity(
  @PrimaryKey val patientId: String,
  val fullName: String,
  val email: String,
  val phone: String,
  val dateOfBirth: String,
  val gender: String,
  val bloodGroup: String,
  val address: String,
  val emergencyContactName: String,
  val emergencyContactPhone: String,
  val emergencyRelationship: String,
  val allergies: String,
  val profilePhotoUri: String?,
  val role: String
)

@Entity(tableName = "appointments")
data class AppointmentEntity(
  @PrimaryKey val appointmentId: String,
  val patientId: String,
  val doctorId: String,
  val doctorName: String,
  val department: String,
  val date: String,
  val timeSlot: String,
  val consultationType: String,
  val status: String,
  val tokenNumber: String,
  val qrCodeData: String,
  val queuePosition: Int,
  val estimatedWaitMinutes: Int,
  val reasonForVisit: String,
  val notes: String
)

@Entity(tableName = "medical_records")
data class MedicalRecordEntity(
  @PrimaryKey val id: String,
  val patientId: String,
  val doctorId: String,
  val doctorName: String,
  val department: String,
  val date: String,
  val type: String,
  val title: String,
  val description: String,
  val summary: String,
  val isDoctorVerified: Boolean
)

@Entity(tableName = "lab_orders")
data class LabOrderEntity(
  @PrimaryKey val id: String,
  val patientId: String,
  val testName: String,
  val doctorName: String,
  val department: String,
  val date: String,
  val status: String,
  val sampleType: String,
  val sampleCollectedAt: String?,
  val completedAt: String?,
  val resultsJson: String,
  val reportSummary: String,
  val aiExplanation: String?
)

@Entity(tableName = "prescriptions")
data class PrescriptionEntity(
  @PrimaryKey val id: String,
  val patientId: String,
  val doctorName: String,
  val department: String,
  val date: String,
  val diagnosis: String,
  val itemsJson: String,
  val isSentToPharmacy: Boolean,
  val notes: String
)

@Entity(tableName = "pharmacy_orders")
data class PharmacyOrderEntity(
  @PrimaryKey val id: String,
  val prescriptionId: String,
  val patientId: String,
  val medicinesJson: String,
  val totalPrice: Double,
  val status: String,
  val pickupCounter: String,
  val orderDate: String,
  val readyAt: String?
)

@Entity(tableName = "bills")
data class BillEntity(
  @PrimaryKey val id: String,
  val patientId: String,
  val invoiceNumber: String,
  val date: String,
  val dueDate: String,
  val itemsJson: String,
  val subtotal: Double,
  val discount: Double,
  val tax: Double,
  val total: Double,
  val status: String,
  val transactionId: String?,
  val paidAt: String?,
  val paymentMethod: String?
)

@Entity(tableName = "notifications")
data class NotificationEntity(
  @PrimaryKey val id: String,
  val patientId: String,
  val title: String,
  val message: String,
  val timestamp: String,
  val category: String,
  val isRead: Boolean,
  val deepLinkRoute: String?
)

@Entity(tableName = "documents")
data class DocumentEntity(
  @PrimaryKey val id: String,
  val patientId: String,
  val fileName: String,
  val category: String,
  val fileUri: String,
  val uploadDate: String,
  val fileSize: String,
  val aiClassified: Boolean,
  val aiConfidence: Float
)

@Entity(tableName = "family_members")
data class FamilyMemberEntity(
  @PrimaryKey val id: String,
  val primaryPatientId: String,
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

@Entity(tableName = "family_health_records")
data class FamilyHealthRecordEntity(
  @PrimaryKey val id: String,
  val familyMemberId: String,
  val memberName: String,
  val recordType: String,
  val title: String,
  val doctorName: String,
  val department: String,
  val date: String,
  val summary: String,
  val details: String,
  val status: String,
  val attachmentName: String?
)

