package com.example.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CareDao {
  // Patient Profile
  @Query("SELECT * FROM patient_profiles WHERE patientId = :patientId LIMIT 1")
  fun getPatientProfile(patientId: String): Flow<PatientProfileEntity?>

  @Query("SELECT * FROM patient_profiles LIMIT 1")
  fun getCurrentProfile(): Flow<PatientProfileEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProfile(profile: PatientProfileEntity)

  // Appointments
  @Query("SELECT * FROM appointments ORDER BY date DESC, timeSlot ASC")
  fun getAllAppointments(): Flow<List<AppointmentEntity>>

  @Query("SELECT * FROM appointments WHERE appointmentId = :id LIMIT 1")
  fun getAppointmentById(id: String): Flow<AppointmentEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAppointments(appointments: List<AppointmentEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAppointment(appointment: AppointmentEntity)

  @Update
  suspend fun updateAppointment(appointment: AppointmentEntity)

  @Query("UPDATE appointments SET status = :status WHERE appointmentId = :id")
  suspend fun updateAppointmentStatus(id: String, status: String)

  // Medical Records
  @Query("SELECT * FROM medical_records ORDER BY date DESC")
  fun getAllMedicalRecords(): Flow<List<MedicalRecordEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMedicalRecords(records: List<MedicalRecordEntity>)

  // Lab Orders
  @Query("SELECT * FROM lab_orders ORDER BY date DESC")
  fun getAllLabOrders(): Flow<List<LabOrderEntity>>

  @Query("SELECT * FROM lab_orders WHERE id = :id LIMIT 1")
  fun getLabOrderById(id: String): Flow<LabOrderEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLabOrders(orders: List<LabOrderEntity>)

  @Update
  suspend fun updateLabOrder(order: LabOrderEntity)

  // Prescriptions
  @Query("SELECT * FROM prescriptions ORDER BY date DESC")
  fun getAllPrescriptions(): Flow<List<PrescriptionEntity>>

  @Query("SELECT * FROM prescriptions WHERE id = :id LIMIT 1")
  fun getPrescriptionById(id: String): Flow<PrescriptionEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPrescriptions(prescriptions: List<PrescriptionEntity>)

  @Update
  suspend fun updatePrescription(prescription: PrescriptionEntity)

  // Pharmacy Orders
  @Query("SELECT * FROM pharmacy_orders ORDER BY orderDate DESC")
  fun getAllPharmacyOrders(): Flow<List<PharmacyOrderEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPharmacyOrders(orders: List<PharmacyOrderEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPharmacyOrder(order: PharmacyOrderEntity)

  @Update
  suspend fun updatePharmacyOrder(order: PharmacyOrderEntity)

  // Bills
  @Query("SELECT * FROM bills ORDER BY date DESC")
  fun getAllBills(): Flow<List<BillEntity>>

  @Query("SELECT * FROM bills WHERE id = :id LIMIT 1")
  fun getBillById(id: String): Flow<BillEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBills(bills: List<BillEntity>)

  @Update
  suspend fun updateBill(bill: BillEntity)

  // Notifications
  @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
  fun getAllNotifications(): Flow<List<NotificationEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNotifications(notifications: List<NotificationEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNotification(notification: NotificationEntity)

  @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
  suspend fun markNotificationAsRead(id: String)

  // Documents
  @Query("SELECT * FROM documents ORDER BY uploadDate DESC")
  fun getAllDocuments(): Flow<List<DocumentEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDocument(document: DocumentEntity)

  @Query("DELETE FROM documents WHERE id = :id")
  suspend fun deleteDocument(id: String)

  // Family Members
  @Query("SELECT * FROM family_members")
  fun getAllFamilyMembers(): Flow<List<FamilyMemberEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertFamilyMembers(members: List<FamilyMemberEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertFamilyMember(member: FamilyMemberEntity)

  @Query("DELETE FROM family_members WHERE id = :id")
  suspend fun deleteFamilyMember(id: String)

  // Family Health Records
  @Query("SELECT * FROM family_health_records ORDER BY date DESC")
  fun getAllFamilyHealthRecords(): Flow<List<FamilyHealthRecordEntity>>

  @Query("SELECT * FROM family_health_records WHERE familyMemberId = :memberId ORDER BY date DESC")
  fun getFamilyRecordsByMember(memberId: String): Flow<List<FamilyHealthRecordEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertFamilyHealthRecords(records: List<FamilyHealthRecordEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertFamilyHealthRecord(record: FamilyHealthRecordEntity)

  @Query("DELETE FROM family_health_records WHERE id = :id")
  suspend fun deleteFamilyHealthRecord(id: String)
}

