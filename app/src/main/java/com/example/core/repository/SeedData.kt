package com.example.core.repository

import com.example.core.model.*

object SeedData {
  val defaultPatient = PatientProfile(
    patientId = "PAT-2026-089",
    fullName = "Subham Pradhan",
    email = "pradhansubham024@gmail.com",
    phone = "+1 (555) 234-5678",
    dateOfBirth = "1994-08-15",
    gender = "Male",
    bloodGroup = "O+",
    address = "450 Medical Heights Blvd, Suite 300, Metro City, NY",
    emergencyContactName = "Anita Pradhan",
    emergencyContactPhone = "+1 (555) 987-6543",
    emergencyRelationship = "Spouse",
    allergies = "Penicillin, Peanuts",
    profilePhotoUri = null,
    role = "PATIENT"
  )

  val departments = listOf(
    Department("dept-cardio", "Cardiology", "favorite", "Heart, vascular health, echocardiograms and ECG monitoring", "Dr. Rahul Sharma", "3rd Floor", "Rooms 301-315"),
    Department("dept-genmed", "General Medicine", "medical_services", "Primary healthcare, chronic disease management, and wellness checks", "Dr. Priya Das", "2nd Floor", "Rooms 201-220"),
    Department("dept-neuro", "Neurology", "psychology", "Brain, spinal cord, neuropathy and central nervous system", "Dr. Arvind Mehta", "4th Floor", "Rooms 401-412"),
    Department("dept-ortho", "Orthopedics", "accessibility_new", "Bone fractures, joint replacements, sports injuries and physical rehab", "Dr. Elena Rostova", "1st Floor", "Rooms 110-125"),
    Department("dept-derma", "Dermatology", "spa", "Skin conditions, allergies, dermatopathology and cosmetic care", "Dr. Sarah Jenkins", "2nd Floor", "Rooms 230-240"),
    Department("dept-pedia", "Pediatrics", "child_care", "Child healthcare, immunization, infant development and growth", "Dr. Marcus Chen", "3rd Floor", "Rooms 320-335")
  )

  val doctors = listOf(
    Doctor(
      id = "doc-sharma",
      name = "Dr. Rahul Sharma",
      department = "Cardiology",
      specialization = "Senior Interventional Cardiologist",
      experienceYears = 16,
      rating = 4.9,
      reviewCount = 284,
      consultationFee = 120.0,
      availableSlots = listOf("09:00 AM", "10:30 AM", "11:45 AM", "02:15 PM", "03:45 PM", "05:00 PM"),
      hospitalName = "CareNexus Central Hospital",
      roomNumber = "Room 304, 3rd Floor",
      qualifications = "MD, DM (Cardiology), FACC (USA)",
      bio = "Dr. Rahul Sharma is a globally recognized cardiologist specializing in preventative heart health, coronary interventions, and cardiac arrhythmia management with over 16 years of clinical excellence."
    ),
    Doctor(
      id = "doc-priya",
      name = "Dr. Priya Das",
      department = "General Medicine",
      specialization = "Internal Medicine & Metabolic Specialist",
      experienceYears = 12,
      rating = 4.8,
      reviewCount = 312,
      consultationFee = 85.0,
      availableSlots = listOf("08:30 AM", "10:00 AM", "11:15 AM", "01:30 PM", "03:00 PM", "04:30 PM"),
      hospitalName = "CareNexus Central Hospital",
      roomNumber = "Room 205, 2nd Floor",
      qualifications = "MBBS, MD (Internal Medicine)",
      bio = "Dr. Priya Das brings compassionate and thorough patient-centered care for lifestyle disorders, hypertension, diabetes management, and preventive health screenings."
    ),
    Doctor(
      id = "doc-arvind",
      name = "Dr. Arvind Mehta",
      department = "Neurology",
      specialization = "Consultant Neurologist & Stroke Specialist",
      experienceYears = 18,
      rating = 4.9,
      reviewCount = 195,
      consultationFee = 150.0,
      availableSlots = listOf("10:00 AM", "11:30 AM", "02:00 PM", "04:00 PM"),
      hospitalName = "CareNexus Central Hospital",
      roomNumber = "Room 408, 4th Floor",
      qualifications = "MBBS, MD, DM (Neurology), FINR",
      bio = "Specialist in neurodegenerative conditions, migraine therapies, EEG interpretation, and neuromuscular rehabilitation."
    ),
    Doctor(
      id = "doc-elena",
      name = "Dr. Elena Rostova",
      department = "Orthopedics",
      specialization = "Orthopedic Surgeon & Joint Care",
      experienceYears = 14,
      rating = 4.7,
      reviewCount = 178,
      consultationFee = 110.0,
      availableSlots = listOf("09:15 AM", "11:00 AM", "02:30 PM", "04:15 PM"),
      hospitalName = "CareNexus Central Hospital",
      roomNumber = "Room 114, 1st Floor",
      qualifications = "MS (Ortho), Fellowship in Arthroscopy & Arthroplasty",
      bio = "Expert in sports medicine, robotic knee replacements, spine stabilization, and minimal invasive fracture fixation."
    ),
    Doctor(
      id = "doc-jenkins",
      name = "Dr. Sarah Jenkins",
      department = "Dermatology",
      specialization = "Consultant Dermatologist",
      experienceYears = 10,
      rating = 4.8,
      reviewCount = 142,
      consultationFee = 95.0,
      availableSlots = listOf("09:30 AM", "11:30 AM", "02:00 PM", "03:30 PM"),
      hospitalName = "CareNexus Central Hospital",
      roomNumber = "Room 234, 2nd Floor",
      qualifications = "MD (Dermatology, Venereology & Leprosy)",
      bio = "Specializing in clinical dermatology, contact allergies, eczema therapies, and preventative dermatological screenings."
    ),
    Doctor(
      id = "doc-marcus",
      name = "Dr. Marcus Chen",
      department = "Pediatrics",
      specialization = "Senior Pediatrician & Child Health",
      experienceYears = 15,
      rating = 4.9,
      reviewCount = 265,
      consultationFee = 90.0,
      availableSlots = listOf("08:45 AM", "10:15 AM", "01:45 PM", "03:15 PM"),
      hospitalName = "CareNexus Central Hospital",
      roomNumber = "Room 322, 3rd Floor",
      qualifications = "MD (Pediatrics), DCH, FAAP",
      bio = "Dedicated to child wellness, developmental milestones, pediatric allergies, and gentle pediatric emergency consultations."
    )
  )

  val initialAppointments = listOf(
    Appointment(
      appointmentId = "APT-1024",
      patientId = "PAT-2026-089",
      doctorId = "doc-sharma",
      doctorName = "Dr. Rahul Sharma",
      department = "Cardiology",
      date = "2026-09-30",
      timeSlot = "10:30 AM",
      consultationType = "IN_PERSON",
      status = AppointmentStatus.CONFIRMED,
      tokenNumber = "A-27",
      qrCodeData = "CARENEXUS://APT/APT-1024/PAT-2026-089/TOKEN-A27",
      queuePosition = 5,
      estimatedWaitMinutes = 25,
      reasonForVisit = "Quarterly cardiovascular checkup & ECG review",
      notes = "Arrive 10 minutes prior at Room 304"
    ),
    Appointment(
      appointmentId = "APT-1019",
      patientId = "PAT-2026-089",
      doctorId = "doc-priya",
      doctorName = "Dr. Priya Das",
      department = "General Medicine",
      date = "2026-09-15",
      timeSlot = "11:15 AM",
      consultationType = "IN_PERSON",
      status = AppointmentStatus.COMPLETED,
      tokenNumber = "G-14",
      qrCodeData = "CARENEXUS://APT/APT-1019/PAT-2026-089/TOKEN-G14",
      queuePosition = 0,
      estimatedWaitMinutes = 0,
      reasonForVisit = "Seasonal allergy and routine blood panel review",
      notes = "Completed. Follow up in 3 months."
    )
  )

  val initialLabOrders = listOf(
    LabOrder(
      id = "LAB-8841",
      patientId = "PAT-2026-089",
      testName = "Comprehensive Lipid & Metabolic Panel",
      doctorName = "Dr. Rahul Sharma",
      department = "Cardiology",
      date = "2026-09-28",
      status = LabOrderStatus.VERIFIED,
      sampleType = "Serum Blood Sample",
      sampleCollectedAt = "2026-09-28 08:30 AM",
      completedAt = "2026-09-28 02:45 PM",
      results = listOf(
        LabParam("Total Cholesterol", "184", "mg/dL", "125 - 200 mg/dL", false),
        LabParam("HDL (Good Cholesterol)", "56", "mg/dL", "> 40 mg/dL", false),
        LabParam("LDL (Bad Cholesterol)", "108", "mg/dL", "< 100 mg/dL", true),
        LabParam("Triglycerides", "142", "mg/dL", "< 150 mg/dL", false),
        LabParam("Fasting Blood Glucose", "92", "mg/dL", "70 - 99 mg/dL", false),
        LabParam("HbA1c", "5.4", "%", "< 5.7 %", false),
        LabParam("Serum Creatinine", "0.95", "mg/dL", "0.70 - 1.30 mg/dL", false)
      ),
      reportSummary = "Lipid panel shows optimal HDL and triglycerides. LDL slightly elevated at 108 mg/dL. Renal and glycemic markers within normal reference range.",
      aiExplanation = """
        🧪 **CareAI Report Breakdown**
        
        • **What Was Evaluated:**
        This panel checks cardiovascular risk factors (cholesterol ratios) and kidney/glycemic stability.
        
        • **Key Findings:**
        - Your **HDL (Good Cholesterol)** is protective at 56 mg/dL.
        - Your **Fasting Blood Sugar (92 mg/dL)** and **HbA1c (5.4%)** indicate excellent metabolic control.
        - Your **LDL** is slightly above the ideal 100 mg/dL mark (108 mg/dL), which Dr. Sharma can address through simple dietary fiber adjustments.
        
        • **Suggested Questions for Dr. Sharma:**
        1. "Should I modify my dietary fats or increase aerobic workouts?"
        2. "Is any medication adjustment needed for my LDL level?"
        
        ⚕️ *Disclaimer: This AI explanation is for educational purposes and does not replace advice from your healthcare professional.*
      """.trimIndent()
    ),
    LabOrder(
      id = "LAB-8830",
      patientId = "PAT-2026-089",
      testName = "Complete Blood Count (CBC) with Differential",
      doctorName = "Dr. Priya Das",
      department = "General Medicine",
      date = "2026-09-14",
      status = LabOrderStatus.VERIFIED,
      sampleType = "Whole Blood EDTA",
      sampleCollectedAt = "2026-09-14 09:00 AM",
      completedAt = "2026-09-14 01:15 PM",
      results = listOf(
        LabParam("Hemoglobin", "14.8", "g/dL", "13.8 - 17.2 g/dL", false),
        LabParam("RBC Count", "4.92", "M/uL", "4.50 - 5.90 M/uL", false),
        LabParam("WBC (White Blood Cells)", "6.8", "k/uL", "4.0 - 11.0 k/uL", false),
        LabParam("Platelet Count", "245", "k/uL", "150 - 450 k/uL", false),
        LabParam("Hematocrit", "44.2", "%", "40.7 - 50.3 %", false)
      ),
      reportSummary = "Normal cellular morphology. No evidence of anemia or acute systemic infection.",
      aiExplanation = null
    )
  )

  val initialPrescriptions = listOf(
    Prescription(
      id = "RX-4402",
      patientId = "PAT-2026-089",
      doctorName = "Dr. Rahul Sharma",
      department = "Cardiology",
      date = "2026-09-28",
      diagnosis = "Mild Dyslipidemia & Cardioprotective Therapy",
      items = listOf(
        PrescriptionItem("Atorvastatin", "20 mg", "Once Daily (Night)", "90 Days", "Take with water after dinner", true),
        PrescriptionItem("CoQ10 Supplement", "100 mg", "Once Daily (Morning)", "90 Days", "Take with breakfast", true),
        PrescriptionItem("Omega-3 EPA/DHA", "1000 mg", "Twice Daily", "60 Days", "Take after meals", false)
      ),
      isSentToPharmacy = true,
      notes = "Maintain balanced low-sodium diet. Re-check lipid panel in 90 days."
    ),
    Prescription(
      id = "RX-4389",
      patientId = "PAT-2026-089",
      doctorName = "Dr. Priya Das",
      department = "General Medicine",
      date = "2026-09-15",
      diagnosis = "Seasonal Allergic Rhinitis",
      items = listOf(
        PrescriptionItem("Levocetirizine", "5 mg", "Once Daily (Bedtime)", "10 Days", "Non-drowsy formula, take before sleep", true),
        PrescriptionItem("Fluticasone Nasal Spray", "50 mcg", "1 Spray Each Nostril Daily", "14 Days", "Gently blow nose before administering", true)
      ),
      isSentToPharmacy = true,
      notes = "Completed course. Use nasal spray only as needed."
    )
  )

  val initialMedicationReminders = listOf(
    MedicationReminder(
      id = "REM-1",
      prescriptionId = "RX-4402",
      medicineName = "CoQ10 Ubiquinol",
      dosage = "100 mg",
      timeSlot = "08:30 AM",
      timePeriod = "MORNING",
      frequency = "Once Daily (With Breakfast)",
      instructions = "Take with breakfast for optimal absorption.",
      isActive = true,
      isTakenToday = true,
      takenAtTime = "Today, 08:35 AM",
      streakDays = 14
    ),
    MedicationReminder(
      id = "REM-2",
      prescriptionId = "RX-4402",
      medicineName = "Atorvastatin Calcium",
      dosage = "20 mg",
      timeSlot = "09:00 PM",
      timePeriod = "BEDTIME",
      frequency = "Once Daily (Bedtime)",
      instructions = "Take at bedtime with water. Avoid grapefruit juice.",
      isActive = true,
      isTakenToday = false,
      takenAtTime = null,
      streakDays = 14
    ),
    MedicationReminder(
      id = "REM-3",
      prescriptionId = "RX-4389",
      medicineName = "Levocetirizine",
      dosage = "5 mg",
      timeSlot = "10:00 PM",
      timePeriod = "BEDTIME",
      frequency = "As Needed (Night)",
      instructions = "Take only if experiencing seasonal rhinitis symptoms.",
      isActive = true,
      isTakenToday = false,
      takenAtTime = null,
      streakDays = 5
    )
  )

  val pharmacyLocations = listOf(
    PharmacyLocation(
      id = "pharm-main",
      name = "Main Hospital Central Pharmacy",
      building = "Main Clinical Tower",
      floor = "1st Floor (Opposite Cafeteria)",
      counterName = "Counter B (Dispensing & Patient Pickups)",
      operatingHours = "Open 24/7 (365 Days)",
      contactPhone = "+1 (800) 555-PHARM",
      distanceMeters = 50,
      currentWaitMinutes = 8,
      isOpen24Hours = true
    ),
    PharmacyLocation(
      id = "pharm-opd",
      name = "OPD West Pavilion Pharmacy",
      building = "Outpatient Specialty Wing",
      floor = "Ground Floor (Near Room 102)",
      counterName = "Counter 1 & 2 (Express Refill)",
      operatingHours = "8:00 AM - 9:00 PM (Mon-Sat)",
      contactPhone = "+1 (800) 555-8821",
      distanceMeters = 180,
      currentWaitMinutes = 12,
      isOpen24Hours = false
    ),
    PharmacyLocation(
      id = "pharm-er",
      name = "CareNexus Emergency & Trauma Dispensary",
      building = "Emergency Wing",
      floor = "Ground Floor Triage Concourse",
      counterName = "Emergency Counter ER-1",
      operatingHours = "Open 24/7 (Priority Emergencies)",
      contactPhone = "+1 (800) 555-9111",
      distanceMeters = 95,
      currentWaitMinutes = 5,
      isOpen24Hours = true
    )
  )

  val initialPharmacyOrders = listOf(
    PharmacyOrder(
      id = "PHARM-721",
      prescriptionId = "RX-4402",
      prescriptionPhotoUri = null,
      patientId = "PAT-2026-089",
      patientName = "Subham Pradhan",
      pharmacyLocationId = "pharm-main",
      pharmacyName = "Main Hospital Central Pharmacy",
      pickupCounter = "Counter B - Main Hospital Pharmacy (1st Floor)",
      medicines = listOf(
        MedicineOrderItem("Atorvastatin 20mg (90 tabs)", "20mg", 1, 24.50),
        MedicineOrderItem("CoQ10 100mg (90 caps)", "100mg", 1, 18.00)
      ),
      totalPrice = 42.50,
      status = PharmacyOrderStatus.READY,
      orderDate = "2026-09-28",
      orderTime = "02:30 PM",
      readyAt = "2026-09-28 04:30 PM",
      pickupTokenNumber = "PH-48",
      specialInstructions = "Prescription verified by Pharmacist David Miller. Ready for collection at Counter B.",
      prescriptionType = "DOCTOR_DIGITAL_RX"
    ),
    PharmacyOrder(
      id = "PHARM-690",
      prescriptionId = "RX-4389",
      prescriptionPhotoUri = "content://carenexus/prescriptions/photo_rx_allergy.jpg",
      patientId = "PAT-2026-089",
      patientName = "Subham Pradhan",
      pharmacyLocationId = "pharm-opd",
      pharmacyName = "OPD West Pavilion Pharmacy",
      pickupCounter = "Counter 1 - Express Dispensary (Ground Floor)",
      medicines = listOf(
        MedicineOrderItem("Levocetirizine 5mg (10 tabs)", "5mg", 1, 8.50),
        MedicineOrderItem("Fluticasone Nasal Spray (50mcg)", "50mcg", 1, 12.00)
      ),
      totalPrice = 20.50,
      status = PharmacyOrderStatus.COLLECTED,
      orderDate = "2026-09-15",
      orderTime = "11:45 AM",
      readyAt = "2026-09-15 12:15 PM",
      pickupTokenNumber = "PH-12",
      specialInstructions = "Patient collected on Sep 15.",
      prescriptionType = "PATIENT_UPLOADED_PHOTO"
    )
  )

  val initialBills = listOf(
    Bill(
      id = "BILL-9012",
      patientId = "PAT-2026-089",
      invoiceNumber = "INV-2026-09012",
      date = "2026-09-28",
      dueDate = "2026-10-15",
      items = listOf(
        BillItem("Specialist Consultation (Dr. Sharma)", "Consultation", 120.00),
        BillItem("Comprehensive Lipid & Metabolic Panel", "Laboratory", 65.00)
      ),
      subtotal = 185.00,
      discount = 0.0,
      tax = 0.0,
      total = 185.00,
      status = BillStatus.PENDING,
      transactionId = null,
      paidAt = null,
      paymentMethod = null
    ),
    Bill(
      id = "BILL-8874",
      patientId = "PAT-2026-089",
      invoiceNumber = "INV-2026-08874",
      date = "2026-09-15",
      dueDate = "2026-09-30",
      items = listOf(
        BillItem("General Medicine Consultation (Dr. Das)", "Consultation", 85.00),
        BillItem("Complete Blood Count (CBC)", "Laboratory", 35.00),
        BillItem("Hospital Pharmacy Order #PHARM-690", "Pharmacy", 16.50)
      ),
      subtotal = 136.50,
      discount = 10.00,
      tax = 0.0,
      total = 126.50,
      status = BillStatus.PAID,
      transactionId = "TXN-9948201-VISA",
      paidAt = "2026-09-15 12:30 PM",
      paymentMethod = "Visa ending in 4242"
    )
  )

  val initialNotifications = listOf(
    NotificationItem(
      id = "NOTIF-1",
      patientId = "PAT-2026-089",
      title = "Appointment Confirmed",
      message = "Your appointment with Dr. Rahul Sharma is confirmed for Sep 30 at 10:30 AM (Token A-27).",
      timestamp = "Today, 08:00 AM",
      category = NotificationCategory.APPOINTMENTS,
      isRead = false,
      deepLinkRoute = "appointments"
    ),
    NotificationItem(
      id = "NOTIF-2",
      patientId = "PAT-2026-089",
      title = "Lab Report Ready & Verified",
      message = "Your Comprehensive Lipid & Metabolic Panel is ready with AI explanation.",
      timestamp = "Yesterday, 02:45 PM",
      category = NotificationCategory.LABORATORY,
      isRead = false,
      deepLinkRoute = "laboratory"
    ),
    NotificationItem(
      id = "NOTIF-3",
      patientId = "PAT-2026-089",
      title = "Prescription Ready at Pharmacy",
      message = "Order #PHARM-721 is packed and waiting at Counter B (1st Floor).",
      timestamp = "Yesterday, 04:35 PM",
      category = NotificationCategory.PHARMACY,
      isRead = true,
      deepLinkRoute = "pharmacy"
    ),
    NotificationItem(
      id = "NOTIF-4",
      patientId = "PAT-2026-089",
      title = "Hospital Announcement",
      message = "CareNexus Rapid Heart Screening Camp starts this Saturday on 3rd Floor.",
      timestamp = "2 days ago",
      category = NotificationCategory.HOSPITAL,
      isRead = true,
      deepLinkRoute = "hospital"
    )
  )

  val initialMedicalRecords = listOf(
    MedicalRecord(
      id = "REC-301",
      patientId = "PAT-2026-089",
      doctorId = "doc-sharma",
      doctorName = "Dr. Rahul Sharma",
      department = "Cardiology",
      date = "2026-09-28",
      type = MedicalRecordType.CONSULTATION,
      title = "Cardiovascular Evaluation & Resting ECG Review",
      description = "Patient presents for annual routine cardiovascular risk stratification. Resting 12-lead ECG demonstrates normal sinus rhythm with heart rate 68 bpm. Blood pressure sitting is 122/78 mmHg. S1/S2 distinct with no third heart sound or gallop. Ordered comprehensive lipid profile and prescribed cardioprotective therapy.",
      summary = "Normal sinus rhythm, stable hemodynamics. Prescribed Atorvastatin & CoQ10.",
      vitals = "BP: 122/78 mmHg • HR: 68 bpm • SpO2: 99% • Temp: 98.4°F",
      clinicalNotes = "Coronary risk evaluation: Non-smoker, BMI 23.4. No exertional angina, dyspnea on exertion, or peripheral edema. Heart sounds normal. Carotid upstrokes brisk without bruits.",
      treatmentPlan = "1. Maintain Mediterranean cardio-protective dietary plan\n2. Continue 150 min/week moderate aerobic exercise\n3. Nightly Atorvastatin 20mg\n4. Re-evaluate fasting lipids in 90 days",
      doctorSignature = "Dr. Rahul Sharma, MD, FACC (USA) • DigCert#9942",
      facility = "CareNexus Central Hospital - Suite 304",
      isDoctorVerified = true,
      attachments = listOf("Consultation_Summary_REC301.pdf", "Resting_12Lead_ECG_Sep2026.pdf"),
      fileSize = "2.8 MB"
    ),
    MedicalRecord(
      id = "REC-298",
      patientId = "PAT-2026-089",
      doctorId = "doc-priya",
      doctorName = "Dr. Priya Das",
      department = "General Medicine",
      date = "2026-09-15",
      type = MedicalRecordType.DIAGNOSIS,
      title = "Seasonal Allergic Rhinitis & Primary Assessment",
      description = "Patient presented with a 5-day history of clear rhinorrhea, sneezing paroxysms, and itchy conjunctivae coinciding with seasonal pollen spike. Examination of HEENT reveals boggy, pale nasal turbinates. Posterior oropharynx without cobblestoning. Complete blood count unrevealing for acute bacterial illness.",
      summary = "Seasonal allergies resolved with 2nd gen antihistamine therapy.",
      vitals = "BP: 118/76 mmHg • HR: 72 bpm • SpO2: 98% • Temp: 98.6°F",
      clinicalNotes = "No fever, productive cough, or wheezing. Lungs clear to percussion and auscultation. Symptoms consistent with seasonal airborne allergen sensitization.",
      treatmentPlan = "1. Levocetirizine 5mg at bedtime for 10 days\n2. Fluticasone nasal spray 1 spray each nostril daily\n3. Saline nasal irrigation as needed",
      doctorSignature = "Dr. Priya Das, MBBS, MD (Internal Medicine) • DigCert#7819",
      facility = "CareNexus Central Hospital - Room 205",
      isDoctorVerified = true,
      attachments = listOf("Allergy_Assessment_REC298.pdf"),
      fileSize = "1.4 MB"
    ),
    MedicalRecord(
      id = "REC-280",
      patientId = "PAT-2026-089",
      doctorId = "doc-elena",
      doctorName = "Dr. Elena Rostova",
      department = "Orthopedics",
      date = "2026-05-10",
      type = MedicalRecordType.DISCHARGE_SUMMARY,
      title = "Post-Sprain Ankle Recovery & Rehab Discharge",
      description = "Grade 1 lateral ankle ligament strain (ATFL) confirmed by ultrasound in April. Patient completed 4 weeks of targeted physical therapy and proprioceptive rehabilitation with full weight-bearing range of motion restored and zero residual instability.",
      summary = "Full recovery and discharged from active orthopedic physical therapy.",
      vitals = "BP: 120/80 mmHg • HR: 70 bpm • SpO2: 100% • Temp: 98.2°F",
      clinicalNotes = "Anterior drawer and talar tilt tests negative bilaterally. Dorsiflexion, plantarflexion, inversion, and eversion 5/5 strength without pain. Patient cleared for running and sports activities.",
      treatmentPlan = "1. Continue home stretching exercises\n2. Supportive footwear during high-impact sports\n3. Discharge from active clinical PT program",
      doctorSignature = "Dr. Elena Rostova, MS (Ortho) • DigCert#6143",
      facility = "CareNexus Orthopedic & Sports Institute",
      isDoctorVerified = true,
      attachments = listOf("Discharge_Summary_REC280.pdf", "PT_Discharge_Chart.pdf"),
      fileSize = "3.1 MB"
    ),
    MedicalRecord(
      id = "REC-265",
      patientId = "PAT-2026-089",
      doctorId = "doc-arvind",
      doctorName = "Dr. Arvind Mehta",
      department = "Neurology",
      date = "2026-02-18",
      type = MedicalRecordType.CONSULTATION,
      title = "Tension Headache & Ergonomic Screening",
      description = "Evaluation for episodic occipital and temporal tension-type headaches triggered by screen exposure. Cranial nerve exam (CN II-XII) intact with normal fundoscopy and symmetrical reflexes.",
      summary = "Benign tension headaches associated with occupational posture. Non-pharmacological ergonomic measures advised.",
      vitals = "BP: 124/80 mmHg • HR: 66 bpm • SpO2: 99% • Temp: 98.4°F",
      clinicalNotes = "No red flags (no aura, morning nausea, visual disturbances, or sudden thunderclap onset). Paraspinal cervical muscles mildly tender without spasm.",
      treatmentPlan = "1. 20-20-20 screen rest rule\n2. Ergonomic monitor height adjustment\n3. Hydration minimum 2.5L daily\n4. Magnesium supplement 200mg daily",
      doctorSignature = "Dr. Arvind Mehta, DM (Neurology) • DigCert#5501",
      facility = "CareNexus Neurosciences Pavilion - Floor 4",
      isDoctorVerified = true,
      attachments = listOf("Neurology_Consult_REC265.pdf"),
      fileSize = "1.9 MB"
    )
  )

  val initialDocuments = listOf(
    UploadedDocument(
      id = "DOC-101",
      patientId = "PAT-2026-089",
      fileName = "Health_Insurance_Card_2026.pdf",
      category = DocumentCategory.INSURANCE,
      fileUri = "content://carenexus/docs/insurance.pdf",
      uploadDate = "2026-09-10",
      fileSize = "1.2 MB",
      aiClassified = true,
      aiConfidence = 0.98f
    ),
    UploadedDocument(
      id = "DOC-102",
      patientId = "PAT-2026-089",
      fileName = "External_Immunization_Record.pdf",
      category = DocumentCategory.MEDICAL_RECORD,
      fileUri = "content://carenexus/docs/vaccines.pdf",
      uploadDate = "2026-08-20",
      fileSize = "840 KB",
      aiClassified = true,
      aiConfidence = 0.94f
    ),
    UploadedDocument(
      id = "DOC-103",
      patientId = "PAT-2026-089",
      fileName = "MRI_Lumbar_Spine_External_Scan.pdf",
      category = DocumentCategory.MEDICAL_RECORD,
      fileUri = "content://carenexus/docs/mri_spine.pdf",
      uploadDate = "2026-07-14",
      fileSize = "4.6 MB",
      aiClassified = true,
      aiConfidence = 0.96f
    ),
    UploadedDocument(
      id = "DOC-104",
      patientId = "PAT-2026-089",
      fileName = "Previous_Hospital_Discharge_Card.pdf",
      category = DocumentCategory.MEDICAL_RECORD,
      fileUri = "content://carenexus/docs/prev_discharge.pdf",
      uploadDate = "2026-04-02",
      fileSize = "2.1 MB",
      aiClassified = true,
      aiConfidence = 0.95f
    )
  )

  val initialFamilyMembers = listOf(
    FamilyMember(
      id = "FAM-1",
      primaryPatientId = "PAT-2026-089",
      name = "Anita Pradhan",
      relationship = "Spouse",
      dateOfBirth = "1996-03-22",
      gender = "Female",
      bloodGroup = "B+",
      phone = "+1 (555) 987-6543",
      emergencyContact = "Subham Pradhan (+1 555 234-5678)",
      allergies = "Sulfa Drugs",
      chronicConditions = "Mild Asthma",
      insuranceId = "INS-CARE-9021-02"
    ),
    FamilyMember(
      id = "FAM-2",
      primaryPatientId = "PAT-2026-089",
      name = "Aarav Pradhan",
      relationship = "Child",
      dateOfBirth = "2022-11-05",
      gender = "Male",
      bloodGroup = "O+",
      phone = "+1 (555) 234-5678",
      emergencyContact = "Subham Pradhan (Father)",
      allergies = "None known",
      chronicConditions = "None",
      insuranceId = "INS-CARE-9021-03"
    ),
    FamilyMember(
      id = "FAM-3",
      primaryPatientId = "PAT-2026-089",
      name = "Kavita Pradhan",
      relationship = "Mother",
      dateOfBirth = "1968-07-14",
      gender = "Female",
      bloodGroup = "O+",
      phone = "+1 (555) 345-6789",
      emergencyContact = "Subham Pradhan (Son)",
      allergies = "Aspirin",
      chronicConditions = "Hypertension, Osteoarthritis",
      insuranceId = "INS-CARE-9021-04"
    )
  )

  val initialFamilyHealthRecords = listOf(
    FamilyHealthRecord(
      id = "FAM-REC-201",
      familyMemberId = "FAM-2",
      memberName = "Aarav Pradhan",
      recordType = "VACCINATION",
      title = "MMR & DTaP Booster #4",
      doctorName = "Dr. Priya Das",
      department = "Pediatrics",
      date = "2026-08-15",
      summary = "4th scheduled pediatric booster administered with zero acute reaction.",
      details = "Batch #VAX-2026-994. Patient tolerated well. Next vaccination due at age 5.",
      status = "COMPLETED",
      attachmentName = "Pediatric_Immunization_Aarav.pdf"
    ),
    FamilyHealthRecord(
      id = "FAM-REC-202",
      familyMemberId = "FAM-2",
      memberName = "Aarav Pradhan",
      recordType = "GROWTH_RECORD",
      title = "3-Year Pediatric Wellness & Milestone Check",
      doctorName = "Dr. Priya Das",
      department = "Pediatrics",
      date = "2026-08-15",
      summary = "Growth percentile: Height 78th percentile (96cm), Weight 70th percentile (14.8kg).",
      details = "Developmental milestones fully achieved. Vision and hearing screening normal. Recommended fluoride varnish application.",
      status = "VERIFIED",
      attachmentName = "Growth_Chart_Aarav.pdf"
    ),
    FamilyHealthRecord(
      id = "FAM-REC-203",
      familyMemberId = "FAM-1",
      memberName = "Anita Pradhan",
      recordType = "CONSULTATION",
      title = "Annual Comprehensive Health & Asthma Review",
      doctorName = "Dr. Rahul Sharma",
      department = "General Medicine",
      date = "2026-07-20",
      summary = "Asthma well controlled with maintenance inhaler. Normal peak flow rate.",
      details = "Spirometry within normal limits. Continued budesonide inhaler 100mcg PRN. Refill issued for 6 months.",
      status = "VERIFIED",
      attachmentName = "Pulmonary_Review_Anita.pdf"
    ),
    FamilyHealthRecord(
      id = "FAM-REC-204",
      familyMemberId = "FAM-1",
      memberName = "Anita Pradhan",
      recordType = "LAB_REPORT",
      title = "Preventive Thyroid & Lipid Panel",
      doctorName = "Dr. Arvind Patel",
      department = "Pathology",
      date = "2026-07-21",
      summary = "TSH: 2.1 mIU/L (Normal), Total Cholesterol: 172 mg/dL (Normal).",
      details = "All thyroid hormones (FT3, FT4, TSH) and lipid markers within normal reference ranges.",
      status = "VERIFIED",
      attachmentName = "Lab_Panel_Anita.pdf"
    ),
    FamilyHealthRecord(
      id = "FAM-REC-205",
      familyMemberId = "FAM-3",
      memberName = "Kavita Pradhan",
      recordType = "CONSULTATION",
      title = "Geriatric Hypertension & Bone Health Assessment",
      doctorName = "Dr. Rahul Sharma",
      department = "Cardiology & Geriatrics",
      date = "2026-06-10",
      summary = "Blood Pressure controlled at 128/82 mmHg. Calcium + Vitamin D3 prescribed.",
      details = "DEXA scan shows mild osteopenia in L-spine (T-score -1.4). Prescribed daily Calcium carbonate 500mg and weekly Vit D3 60,000 IU.",
      status = "VERIFIED",
      attachmentName = "Geriatric_Assessment_Kavita.pdf"
    )
  )


  val initialVitals = listOf(
    HealthVitalRecord(
      id = "VIT-101",
      type = VitalType.BLOOD_PRESSURE,
      value = "120/78",
      numericPrimary = 120f,
      numericSecondary = 78f,
      recordedAt = "2026-09-29",
      recordedTime = "07:30 AM",
      status = VitalStatus.NORMAL,
      notes = "Resting morning check, normal systolic/diastolic",
      source = "CareNexus HealthSync"
    ),
    HealthVitalRecord(
      id = "VIT-102",
      type = VitalType.HEART_RATE,
      value = "72",
      numericPrimary = 72f,
      recordedAt = "2026-09-29",
      recordedTime = "07:32 AM",
      status = VitalStatus.OPTIMAL,
      notes = "Resting heart rate in optimal athletic zone",
      source = "Wearable Watch"
    ),
    HealthVitalRecord(
      id = "VIT-103",
      type = VitalType.BLOOD_GLUCOSE,
      value = "94",
      numericPrimary = 94f,
      recordedAt = "2026-09-29",
      recordedTime = "07:15 AM",
      status = VitalStatus.NORMAL,
      notes = "Fasting glucose, within 70-99 target range",
      source = "Glucometer Log"
    ),
    HealthVitalRecord(
      id = "VIT-104",
      type = VitalType.SPO2,
      value = "99",
      numericPrimary = 99f,
      recordedAt = "2026-09-29",
      recordedTime = "07:31 AM",
      status = VitalStatus.OPTIMAL,
      notes = "Blood oxygenation excellent",
      source = "Pulse Oximeter"
    ),
    HealthVitalRecord(
      id = "VIT-105",
      type = VitalType.TEMPERATURE,
      value = "98.4",
      numericPrimary = 98.4f,
      recordedAt = "2026-09-29",
      recordedTime = "07:33 AM",
      status = VitalStatus.NORMAL,
      notes = "Afebrile, normal core body temp",
      source = "Digital Thermometer"
    ),
    HealthVitalRecord(
      id = "VIT-106",
      type = VitalType.WEIGHT,
      value = "74.5",
      numericPrimary = 74.5f,
      numericSecondary = 23.4f,
      recordedAt = "2026-09-28",
      recordedTime = "08:00 AM",
      status = VitalStatus.NORMAL,
      notes = "BMI 23.4 kg/m² (Normal Weight Class)",
      source = "Smart Scale"
    ),
    HealthVitalRecord(
      id = "VIT-107",
      type = VitalType.BLOOD_PRESSURE,
      value = "124/82",
      numericPrimary = 124f,
      numericSecondary = 82f,
      recordedAt = "2026-09-27",
      recordedTime = "06:45 PM",
      status = VitalStatus.NORMAL,
      notes = "Post-evening walk reading",
      source = "Manual Log"
    ),
    HealthVitalRecord(
      id = "VIT-108",
      type = VitalType.BLOOD_GLUCOSE,
      value = "98",
      numericPrimary = 98f,
      recordedAt = "2026-09-27",
      recordedTime = "07:00 AM",
      status = VitalStatus.NORMAL,
      notes = "Fasting glucose test",
      source = "Glucometer Log"
    )
  )
}

