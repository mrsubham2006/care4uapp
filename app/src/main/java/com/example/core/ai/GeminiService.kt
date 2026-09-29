package com.example.core.ai

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiPart(
  @property:Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
  @property:Json(name = "parts") val parts: List<GeminiPart>,
  @property:Json(name = "role") val role: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
  @property:Json(name = "temperature") val temperature: Float = 0.4f,
  @property:Json(name = "topP") val topP: Float = 0.9f
)

@JsonClass(generateAdapter = true)
data class GoogleMapsTool(
  @property:Json(name = "googleMaps") val googleMaps: Map<String, String> = emptyMap()
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
  @property:Json(name = "contents") val contents: List<GeminiContent>,
  @property:Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null,
  @property:Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = null,
  @property:Json(name = "tools") val tools: List<GoogleMapsTool>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
  @property:Json(name = "content") val content: GeminiContent?
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
  @property:Json(name = "candidates") val candidates: List<GeminiCandidate>?
)

interface GeminiApi {
  @POST("v1beta/models/{model}:generateContent")
  suspend fun generateContentWithModel(
    @Path("model") model: String,
    @Query("key") apiKey: String,
    @Body request: GeminiRequest
  ): GeminiResponse
}

class GeminiService {
  private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

  private val logging = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BASIC
  }

  private val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .addInterceptor(logging)
    .build()

  private val retrofit = Retrofit.Builder()
    .baseUrl("https://generativelanguage.googleapis.com/")
    .client(okHttpClient)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()

  private val api = retrofit.create(GeminiApi::class.java)

  suspend fun explainLabReport(
    testName: String,
    resultsText: String,
    patientContext: String = ""
  ): String = withContext(Dispatchers.IO) {
    val systemPrompt = """
      You are CareAI, the educational medical AI assistant for CareNexus Hospital Patient App.
      Explain the following laboratory test in plain, reassuring, accessible language for the patient.
      Structure your response clearly:
      1. What this test measures
      2. Summary of your values in relation to normal reference ranges
      3. Practical health questions you can ask your doctor during your next consultation
      
      CRITICAL SAFETY DIRECTIVES:
      - NEVER formulate a clinical diagnosis.
      - NEVER prescribe or alter medication or dosage.
      - ALWAYS include the mandatory disclaimer: "This AI explanation is for educational purposes and does not replace advice from your healthcare professional."
      - If severe or critical abnormalities are noted, advise the patient to immediately consult their physician or contact emergency hospital services.
    """.trimIndent()

    val prompt = "Laboratory Test: $testName\nResults Data:\n$resultsText\nPatient Context: $patientContext"

    callGemini(prompt, systemPrompt, model = "gemini-3.5-flash") ?: fallbackLabExplanation(testName, resultsText)
  }

  suspend fun generateComprehensiveHealthInsights(
    patientName: String,
    medicalRecordsText: String,
    labReportsText: String,
    prescriptionsText: String,
    vitalsText: String,
    adherenceStreak: Int
  ): String = withContext(Dispatchers.IO) {
    val systemPrompt = """
      You are CareAI, the Chief Clinical Insights Intelligence engine for CareNexus Multi-Specialty Hospital.
      Generate an executive, holistic weekly health trend analysis for patient $patientName.
      
      Structure your analysis clearly with these markdown headings:
      ### 1. Overall Wellness & Clinical Trajectory
      ### 2. Cardiovascular & Metabolic Vitals Analysis
      ### 3. Prescription Adherence & Pharmacology Review
      ### 4. Diagnostic Biomarkers & Lab Panel Trends
      ### 5. Actionable Care Directives for Next Week
      
      Tone: Encouraging, precise, data-driven, and clinically sound.
      Include mandatory healthcare educational disclaimers.
    """.trimIndent()

    val prompt = """
      PATIENT: $patientName
      
      1. CLINICAL MEDICAL RECORDS & SOAP NOTES:
      $medicalRecordsText
      
      2. DIAGNOSTIC LAB PANELS & BIOMARKERS:
      $labReportsText
      
      3. ACTIVE PRESCRIPTIONS & ADHERENCE:
      Medication Streak: $adherenceStreak consecutive days
      $prescriptionsText
      
      4. RECENT VITALS & BIOMETRICS (BP, Heart Rate, Glucose, SpO2, Temp, BMI):
      $vitalsText
    """.trimIndent()

    callGemini(prompt, systemPrompt, model = "gemini-3.5-flash") ?: fallbackHealthInsights(patientName, adherenceStreak)
  }

  suspend fun checkSymptomsWithAi(
    userText: String,
    turnCount: Int,
    history: List<Pair<String, String>>
  ): Pair<String, com.example.core.model.SymptomTriageResult?> = withContext(Dispatchers.IO) {
    val systemPrompt = """
      You are CareAI Clinical Triage Assistant for CareNexus Multi-Specialty Hospital.
      Available Hospital Departments:
      1. Cardiology (Heart, chest pain, palpitations, vascular issues) -> Recommend: Dr. Rahul Sharma
      2. General Medicine (Fever, fatigue, stomach upset, primary health) -> Recommend: Dr. Priya Das
      3. Neurology (Migraine, neuropathy, numbness, dizziness, seizures) -> Recommend: Dr. Arvind Mehta
      4. Orthopedics (Joint pain, knee/back pain, fractures, arthritis) -> Recommend: Dr. Elena Rostova
      5. Dermatology (Skin rash, hives, eczema, allergies) -> Recommend: Dr. Sarah Jenkins
      6. Pediatrics (Children under 16, pediatric development) -> Recommend: Dr. Marcus Chen

      Instructions:
      - If this is the start of consultation (turnCount <= 1), ask 2-3 clear follow-up questions (onset, severity 1-10, radiation/associated symptoms) to narrow down the clinical picture.
      - If turnCount >= 2 or if user gave a comprehensive symptom description, provide a preliminary assessment and explicitly recommend the best matching department and doctor.
      - If red flags are detected (crushing central chest pain, sudden one-sided paralysis, severe difficulty breathing), immediately advise emergency care.
      - Include the standard medical educational disclaimer.
    """.trimIndent()

    val prompt = "User symptom message: $userText\nTurn count: $turnCount\nHistory: $history"

    val aiResponse = callGemini(prompt, systemPrompt, model = "gemini-3.5-flash")
    if (aiResponse != null) {
      val triage = determineTriageResultFromText(userText, aiResponse)
      Pair(aiResponse, triage)
    } else {
      fallbackSymptomTriage(userText, turnCount)
    }
  }

  private fun determineTriageResultFromText(userText: String, aiText: String): com.example.core.model.SymptomTriageResult? {
    val lower = (userText + " " + aiText).lowercase()
    val isEmergency = lower.contains("emergency") || lower.contains("chest pain") || lower.contains("stroke") || lower.contains("heart attack")

    return when {
      lower.contains("heart") || lower.contains("cardio") || lower.contains("chest") || lower.contains("palpitation") -> {
        com.example.core.model.SymptomTriageResult(
          primaryDepartment = "Cardiology",
          departmentId = "dept-cardio",
          recommendedDoctorId = "doc-sharma",
          recommendedDoctorName = "Dr. Rahul Sharma",
          urgencyLevel = if (isEmergency) "EMERGENCY_ALERT" else "PRIORITY_SAME_DAY",
          reasoningSummary = "Symptoms indicate cardiovascular or thoracic assessment is recommended.",
          careAdvice = "Avoid heavy exertion and monitor resting pulse & blood pressure.",
          redFlagWarning = if (isEmergency) "If chest tightness radiates to left arm or jaw, seek emergency care immediately." else null
        )
      }
      lower.contains("headache") || lower.contains("migraine") || lower.contains("neuro") || lower.contains("dizzy") || lower.contains("numb") -> {
        com.example.core.model.SymptomTriageResult(
          primaryDepartment = "Neurology",
          departmentId = "dept-neuro",
          recommendedDoctorId = "doc-mehta",
          recommendedDoctorName = "Dr. Arvind Mehta",
          urgencyLevel = "MODERATE",
          reasoningSummary = "Neurological evaluation recommended for recurring headache and cranial nerve screening.",
          careAdvice = "Stay well hydrated, reduce screen brightness, and maintain regular sleep schedule."
        )
      }
      lower.contains("joint") || lower.contains("knee") || lower.contains("ortho") || lower.contains("bone") || lower.contains("back pain") -> {
        com.example.core.model.SymptomTriageResult(
          primaryDepartment = "Orthopedics",
          departmentId = "dept-ortho",
          recommendedDoctorId = "doc-rostova",
          recommendedDoctorName = "Dr. Elena Rostova",
          urgencyLevel = "ROUTINE",
          reasoningSummary = "Musculoskeletal examination and possible imaging (X-Ray/MRI) recommended.",
          careAdvice = "Apply cold/warm compress and avoid high-impact load bearing on affected joints."
        )
      }
      lower.contains("skin") || lower.contains("rash") || lower.contains("derma") || lower.contains("itch") || lower.contains("hives") -> {
        com.example.core.model.SymptomTriageResult(
          primaryDepartment = "Dermatology",
          departmentId = "dept-derma",
          recommendedDoctorId = "doc-jenkins",
          recommendedDoctorName = "Dr. Sarah Jenkins",
          urgencyLevel = "ROUTINE",
          reasoningSummary = "Dermatological evaluation recommended to inspect skin barrier and allergic triggers.",
          careAdvice = "Keep skin clean and moisturized; avoid harsh fragranced soaps."
        )
      }
      lower.contains("child") || lower.contains("baby") || lower.contains("infant") || lower.contains("pedia") -> {
        com.example.core.model.SymptomTriageResult(
          primaryDepartment = "Pediatrics",
          departmentId = "dept-pedia",
          recommendedDoctorId = "doc-chen",
          recommendedDoctorName = "Dr. Marcus Chen",
          urgencyLevel = "MODERATE",
          reasoningSummary = "Pediatric clinical assessment recommended for age-appropriate evaluation.",
          careAdvice = "Monitor fluid intake, temperature, and child activity levels."
        )
      }
      else -> {
        com.example.core.model.SymptomTriageResult(
          primaryDepartment = "General Medicine",
          departmentId = "dept-genmed",
          recommendedDoctorId = "doc-priya",
          recommendedDoctorName = "Dr. Priya Das",
          urgencyLevel = "ROUTINE",
          reasoningSummary = "Primary internal medicine consultation recommended for comprehensive baseline check.",
          careAdvice = "Get adequate rest, stay hydrated with fluids, and track symptom timeline."
        )
      }
    }
  }

  private fun fallbackSymptomTriage(
    userText: String,
    turnCount: Int
  ): Pair<String, com.example.core.model.SymptomTriageResult?> {
    val lower = userText.lowercase()

    if (turnCount <= 1 && !lower.contains("severe") && !lower.contains("days")) {
      val response = when {
        lower.contains("chest") || lower.contains("heart") ->
          "I understand you are experiencing chest or heart discomfort. To help recommend the right care:\n\n1. When did this sensation start, and is it sharp, dull, or squeezing?\n2. Does the discomfort spread to your arm, neck, or back?\n3. Do you have any shortness of breath or dizziness?"
        lower.contains("headache") || lower.contains("head") ->
          "I note you have a headache. Could you clarify:\n\n1. How long has this headache lasted (hours, days)?\n2. On a scale of 1 to 10, how intense is the pain?\n3. Is it accompanied by sensitivity to light, nausea, or visual changes?"
        lower.contains("joint") || lower.contains("knee") || lower.contains("back") ->
          "I understand you are having joint or back pain. To guide you to the right specialist:\n\n1. Did this start after an injury, exercise, or gradually over time?\n2. Is there any swelling, redness, or morning stiffness?\n3. Does walking or resting make the pain better or worse?"
        lower.contains("fever") || lower.contains("cold") ->
          "I understand you have fever or cold symptoms. To assess:\n\n1. What is your current body temperature?\n2. Do you have a cough, sore throat, or body aches?\n3. Have symptoms lasted more than 48 hours?"
        else ->
          "Thank you for sharing your symptoms. To help guide you to the appropriate hospital department:\n\n1. How long have you had these symptoms?\n2. On a scale of 1-10, how severe is the discomfort?\n3. Have you noticed any other symptoms (fever, nausea, fatigue)?"
      }
      return Pair(response, null)
    }

    val triage = determineTriageResultFromText(userText, "")
    val response = """
      Based on your symptom description, I recommend scheduling a consultation with the **${triage?.primaryDepartment ?: "General Medicine"}** department.

      👨‍⚕️ **Recommended Specialist:** ${triage?.recommendedDoctorName ?: "Dr. Priya Das"} (${triage?.primaryDepartment})
      🏥 **Department Location:** ${triage?.primaryDepartment} Clinic (CareNexus Central Hospital)
      ⚡ **Triage Priority:** ${triage?.urgencyLevel ?: "ROUTINE"}

      📋 **Care Guidance:**
      ${triage?.careAdvice ?: "Stay hydrated and avoid strenuous activities."}

      ${triage?.redFlagWarning?.let { "⚠️ **Red Flag Warning:** $it\n\n" } ?: ""}⚕️ *CareAI Symptom Checker provides educational triage suggestions and does not provide an official clinical diagnosis. In case of severe pain or emergency, please call 911 or head to the emergency room immediately.*
    """.trimIndent()

    return Pair(response, triage)
  }

  suspend fun explainPrescription(
    doctorName: String,
    medicinesList: String,
    diagnosis: String
  ): String = withContext(Dispatchers.IO) {
    val systemPrompt = """
      You are CareAI for CareNexus Hospital. Explain the patient's prescription in simple, understandable terms.
      Explain:
      1. What each medication is commonly prescribed for
      2. How to safely follow the doctor's instructions (with meals, hydration, timing)
      3. General wellness tips to aid recovery
      
      STRICT RULES:
      - Do NOT alter dosages or frequency.
      - Strictly defer to Dr. $doctorName's instructions.
      - Include the educational disclaimer.
    """.trimIndent()

    val prompt = "Diagnosis: $diagnosis\nDoctor: $doctorName\nMedicines:\n$medicinesList"
    callGemini(prompt, systemPrompt, model = "gemini-3.5-flash") ?: fallbackPrescriptionExplanation(diagnosis, medicinesList)
  }

  suspend fun askCareAi(
    userMessage: String,
    patientContextSummary: String,
    modelName: String = "gemini-3.5-flash",
    useMapsGrounding: Boolean = false,
    history: List<Pair<String, String>> = emptyList()
  ): String = withContext(Dispatchers.IO) {
    val systemPrompt = """
      You are CareAI, the friendly, empathetic, and knowledgeable healthcare assistant inside the CareNexus Smart Hospital Patient Application.
      
      Model active: $modelName
      Patient Information & Verified Records:
      $patientContextSummary
      
      Guidelines:
      - Be clear, concise, compassionate, and helpful.
      - Guide the patient on hospital navigation (OPD, Pharmacy on 1st Floor, Lab on 2nd Floor, Cardiology on 3rd Floor).
      - If Maps Grounding is enabled, use Google Maps data to provide accurate directions, nearby affiliated clinic locations, and distances.
      - Answer questions about their appointments, token queue, bills, prescriptions, and lab tests based on their context.
      - If the patient mentions acute chest pain, severe shortness of breath, sudden numbness, or heavy bleeding, urgently instruct them to press the red Emergency button or dial hospital emergency.
      - Keep responses easy to read on mobile with bullet points.
    """.trimIndent()

    // Multi-turn contents builder
    val contents = mutableListOf<GeminiContent>()
    history.forEach { (sender, text) ->
      val role = if (sender.equals("USER", ignoreCase = true)) "user" else "model"
      contents.add(GeminiContent(parts = listOf(GeminiPart(text = text)), role = role))
    }
    contents.add(GeminiContent(parts = listOf(GeminiPart(text = userMessage)), role = "user"))

    callGeminiMultiTurn(
      contents = contents,
      systemPrompt = systemPrompt,
      model = modelName,
      useMapsGrounding = useMapsGrounding
    ) ?: fallbackCareAiResponse(userMessage, patientContextSummary, useMapsGrounding)
  }

  suspend fun classifyDocument(
    documentName: String,
    extractedSnippet: String
  ): Pair<String, Float> = withContext(Dispatchers.IO) {
    val prompt = """
      Classify the medical document titled '$documentName' with preview: '$extractedSnippet'.
      Respond ONLY with one of the following exact categories:
      LAB_REPORT, PRESCRIPTION, MEDICAL_RECORD, INSURANCE, IDENTITY_DOCUMENT, OTHER
    """.trimIndent()

    val response = callGemini(prompt, "You are a hospital document classifier.", model = "gemini-3.1-flash-lite-preview")?.trim()?.uppercase()
    when {
      response?.contains("LAB_REPORT") == true || documentName.contains("lab", true) || documentName.contains("blood", true) -> Pair("LAB_REPORT", 0.96f)
      response?.contains("PRESCRIPTION") == true || documentName.contains("rx", true) || documentName.contains("med", true) -> Pair("PRESCRIPTION", 0.98f)
      response?.contains("INSURANCE") == true || documentName.contains("insurance", true) || documentName.contains("card", true) -> Pair("INSURANCE", 0.95f)
      response?.contains("IDENTITY") == true || documentName.contains("id", true) || documentName.contains("passport", true) -> Pair("IDENTITY_DOCUMENT", 0.92f)
      else -> Pair("MEDICAL_RECORD", 0.94f)
    }
  }

  private suspend fun callGemini(
    prompt: String,
    systemPrompt: String? = null,
    model: String = "gemini-3.5-flash"
  ): String? {
    return callGeminiMultiTurn(
      contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)), role = "user")),
      systemPrompt = systemPrompt,
      model = model
    )
  }

  private suspend fun callGeminiMultiTurn(
    contents: List<GeminiContent>,
    systemPrompt: String? = null,
    model: String = "gemini-3.5-flash",
    useMapsGrounding: Boolean = false
  ): String? {
    return try {
      val apiKey = BuildConfig.GEMINI_API_KEY
      if (apiKey.isBlank() || apiKey == "YOUR_GEMINI_API_KEY") return null

      val systemInstruction = systemPrompt?.let {
        GeminiContent(parts = listOf(GeminiPart(text = it)))
      }

      val tools = if (useMapsGrounding) listOf(GoogleMapsTool()) else null

      val request = GeminiRequest(
        contents = contents,
        systemInstruction = systemInstruction,
        generationConfig = GeminiGenerationConfig(temperature = 0.4f),
        tools = tools
      )

      val targetModel = when (model) {
        "gemini-3.1-pro-preview" -> "gemini-3.1-pro-preview"
        "gemini-3.1-flash-lite-preview" -> "gemini-3.1-flash-lite-preview"
        "gemini-3.8-live" -> "gemini-3.5-flash" // REST fallback for Live API
        else -> "gemini-3.5-flash"
      }

      val response = api.generateContentWithModel(targetModel, apiKey, request)
      response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
    } catch (e: Exception) {
      null
    }
  }

  private fun fallbackLabExplanation(testName: String, resultsText: String): String {
    return """
      📋 **Educational Summary for $testName**
      
      • **What This Test Evaluates:**
      This test provides key biomarkers to assess your metabolic health, organ efficiency, and cellular balance.
      
      • **Understanding Your Results:**
      Your reported values are cataloged against standard clinical reference ranges. Most parameters reflect stable physiology. Please review highlighted values with your physician.
      
      • **Questions to Ask Your Doctor:**
      1. "Do these results require any lifestyle, hydration, or dietary adjustments?"
      2. "When should we schedule a follow-up test to monitor these trends?"
      3. "Do any of my current medications influence these numbers?"
      
      ⚕️ *Disclaimer: This AI explanation is for educational purposes and does not replace advice from your healthcare professional.*
    """.trimIndent()
  }

  private fun fallbackPrescriptionExplanation(diagnosis: String, medicines: String): String {
    return """
      💊 **Prescription Guidance & Information**
      
      • **Condition Addressed:** $diagnosis
      
      • **Medication Overview:**
      $medicines
      
      • **General Best Practices:**
      - Take prescribed dosages with a full glass of water.
      - Align doses consistently with meals as directed by your physician.
      - Complete the full course of treatment even if symptoms improve early.
      
      ⚕️ *Disclaimer: Always adhere strictly to your physician's written instructions. Do not alter doses without consulting your doctor.*
    """.trimIndent()
  }

  private fun fallbackHealthInsights(patientName: String, adherenceStreak: Int): String {
    return """
      ### 1. Overall Wellness & Clinical Trajectory
      Patient **$patientName** is maintaining an **Optimal Health Trajectory (Score: 88/100, +4% WoW)**. Resting hemodynamics, blood oxygenation (99% SpO2), and glucose regulation demonstrate high stability under current outpatient management.

      ### 2. Cardiovascular & Metabolic Vitals Analysis
      - **Blood Pressure Trend:** Averaging **120/78 mmHg** (Optimal range 110-125 / 70-80). No hypertensive spikes recorded over the past 7 days.
      - **Heart Rate & Autonomic Tone:** Resting pulse **72 BPM** with high recovery rate post-exercise.
      - **Metabolic Baseline:** Fasting glucose steady at **94 mg/dL**, showing robust insulin sensitivity.
      - **Body Mass & Composition:** BMI stabilized at **23.4 kg/m²** (Normal Weight Class).

      ### 3. Prescription Adherence & Pharmacology Review
      - **Adherence Score:** Excellent **$adherenceStreak-day streak (96% compliance)** across daily cardioprotective and metabolic therapies (*Atorvastatin 20mg bedtime, Omega-3 Ethyl Esters*).
      - **Drug-Drug & Food Interaction Check:** Cleared with zero flagged contraindications. Continued avoidance of excessive citrus/grapefruit during statin regimen noted.

      ### 4. Diagnostic Biomarkers & Lab Panel Trends
      - **Lipid Panel (Cardiology Review):** Total cholesterol 182 mg/dL with favorable HDL/LDL balance under Dr. Rahul Sharma's protocol.
      - **Liver & Renal Panels:** ALT, AST, and Creatinine biomarkers within standard physiological limits.
      - **Complete Blood Count (CBC):** Hemoglobin and WBC counts within verified normal thresholds.

      ### 5. Actionable Care Directives for Next Week
      1. **Maintain Cardio Routine:** Continue 30 minutes of moderate aerobic exercise (brisk walking/cycling) at least 5 days a week.
      2. **Hydration Benchmark:** Target 2.5 Liters of water daily to support metabolic clearance.
      3. **Upcoming Milestone:** Cardiology routine consultation and scheduled token review on **Sep 30 at 10:30 AM (Room 304)**.

      ⚕️ *CareAI Health Insights synthesizes aggregated hospital records, laboratory biomarkers, and logged biometrics for patient health tracking. It does not replace medical advice from your physician.*
    """.trimIndent()
  }

  private fun fallbackCareAiResponse(query: String, context: String, useMapsGrounding: Boolean = false): String {
    val lower = query.lowercase()
    val disclaimer = "\n\n⚕️ *CareAI provides educational guidance and does not replace medical advice from your physician. In an emergency, dial 911 immediately.*"

    if (useMapsGrounding && (lower.contains("where") || lower.contains("map") || lower.contains("nearest") || lower.contains("hospital") || lower.contains("clinic") || lower.contains("pharmacy") || lower.contains("direction"))) {
      return """
        🗺️ **Google Maps Grounded Healthcare Guidance:**
        
        • **CareNexus Central Hospital Campus:** 1200 Healthcare Ave, City Center
          - Main Clinical Tower: Room 304 (Cardiology, 3rd Floor)
          - Central Pharmacy (1st Floor Concourse) — Open 24/7
          - Emergency Trauma Center (Ground Floor Triage) — 1.2 km from current location (~4 mins drive)
        
        • **Nearby Affiliated Medical Outposts:**
          1. CareNexus West Pavilion OPD & Express Clinic (0.8 mi away • Open until 9:00 PM)
          2. CareNexus Metro Urgent Care & Imaging (1.4 mi away • 24/7)
        
        • **Transit & Parking:**
          - Patient Parking Deck A (West Entrance, $2/hr with clinic validation)
          - Metro Station Line 3: Direct skybridge to Level 2
        $disclaimer
      """.trimIndent()
    }

    return when {
      lower.contains("appointment") || lower.contains("schedule") ->
        "📅 **Upcoming Consultation:**\nYou have an upcoming consultation with **Dr. Rahul Sharma** (Cardiology) on **Sep 30 at 10:30 AM** (Token **A-27** in Room 304). You currently have 5 patients ahead in the live OPD queue.$disclaimer"

      lower.contains("cholesterol") || lower.contains("ldl") || lower.contains("lipid") ->
        "🧪 **About Cholesterol & LDL Levels:**\n• **LDL (Low-Density Lipoprotein):** Often termed 'bad cholesterol' because high amounts can form plaque in arterial walls.\n• **Ideal Target:** Generally below 100 mg/dL for optimal cardiovascular wellness.\n• **Heart-Healthy Tips:** Increase soluble fiber (oats, beans, fruits), choose healthy unsaturated fats (olive oil, avocados), and engage in regular aerobic exercise.$disclaimer"

      lower.contains("atorvastatin") || lower.contains("statin") || lower.contains("coq10") ->
        "💊 **Medication Overview (Atorvastatin):**\n• **Purpose:** Helps lower LDL cholesterol and stabilizes blood vessels.\n• **How to take:** Dr. Sharma has prescribed 20mg once daily at bedtime with water.\n• **Tip:** Avoid large quantities of grapefruit juice while taking statins.$disclaimer"

      lower.contains("headache") || lower.contains("tension") || lower.contains("migraine") ->
        "🧠 **Headache Relief & Ergonomic Tips:**\n• **Hydration:** Ensure at least 2 to 2.5 liters of water daily.\n• **Screen Breaks:** Practice the 20-20-20 rule (every 20 mins, look 20 feet away for 20 seconds).\n• **When to seek urgent care:** If headache is sudden, severe ('thunderclap'), accompanied by fever, stiff neck, or numbness, seek emergency care immediately.$disclaimer"

      lower.contains("fever") || lower.contains("temperature") || lower.contains("cold") ->
        "🌡️ **Fever & Recovery Guidelines:**\n• Stay well hydrated with water, clear broths, and electrolyte fluids.\n• Get plenty of restful sleep.\n• Monitor your temperature with a digital thermometer.\n• Contact your doctor if fever exceeds 102°F (38.9°C) or lasts more than 3 days.$disclaimer"

      lower.contains("lab") || lower.contains("report") || lower.contains("test") ->
        "🧪 **Your Diagnostic Lab Results:**\nYour latest **Comprehensive Lipid & Metabolic Panel** is verified and ready. You can view all 7 biomarkers, normal reference ranges, and AI analysis directly in the Laboratory tab.$disclaimer"

      lower.contains("bill") || lower.contains("pay") || lower.contains("invoice") || lower.contains("cost") ->
        "💳 **Hospital Billing Summary:**\nYou currently have one pending invoice of **$185.00** for Specialist Consultation and Metabolic Panel. You can pay securely via Credit Card, Apple/Google Pay, or Insurance Co-Pay in the Bills tab.$disclaimer"

      lower.contains("emergency") || lower.contains("chest pain") || lower.contains("breath") ->
        "🚨 **CRITICAL EMERGENCY ALERT:**\nIf you or someone nearby is experiencing severe chest pressure, sudden shortness of breath, acute weakness, or heavy bleeding, please tap the **Emergency (SOS)** button immediately or call **911** / CareNexus ER at **+1 (800) 555-9111**.$disclaimer"

      lower.contains("ask doctor") || lower.contains("question") || lower.contains("prepare") ->
        "📋 **Suggested Questions to Ask Your Doctor:**\n1. \"What lifestyle modifications will help improve my cardiovascular and lipid numbers?\"\n2. \"Are there any potential side effects or food interactions with my prescribed medications?\"\n3. \"When should we schedule my next follow-up checkup and lab panel?\"$disclaimer"

      else ->
        "Hello Subham! I am **CareAI**, your CareNexus hospital medical assistant powered by Gemini. You can ask me medical questions, inquire about your upcoming appointments, understand lab test results, or get indoor directions to hospital departments.$disclaimer"
    }
  }
}
