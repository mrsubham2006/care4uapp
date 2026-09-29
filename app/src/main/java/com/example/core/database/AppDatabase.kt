package com.example.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [
    PatientProfileEntity::class,
    AppointmentEntity::class,
    MedicalRecordEntity::class,
    LabOrderEntity::class,
    PrescriptionEntity::class,
    PharmacyOrderEntity::class,
    BillEntity::class,
    NotificationEntity::class,
    DocumentEntity::class,
    FamilyMemberEntity::class,
    FamilyHealthRecordEntity::class
  ],
  version = 1,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun careDao(): CareDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "carenexus_patient_database"
        )
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
