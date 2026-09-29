package com.example.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.PatientProfile
import com.example.core.repository.CareNexusRepository
import com.example.ui.theme.*

@Composable
fun OnboardingScreen(
  onFinishOnboarding: () -> Unit
) {
  var currentStep by remember { mutableIntStateOf(0) }

  val pages = listOf(
    Triple(
      "Seamless Hospital Appointments",
      "Discover verified specialists, book OPD consultation slots, and avoid waiting in long reception lines with digital tokens.",
      Icons.Default.CalendarMonth
    ),
    Triple(
      "Live Digital Queue & Lab Reports",
      "Track your token progress in real-time, get notified when your turn is near, and access certified lab reports instantly.",
      Icons.Default.Science
    ),
    Triple(
      "CareAI & Complete Records",
      "Understand diagnostic parameters with AI lab report explainer, manage pharmacy pickups, and reach emergency care 24/7.",
      Icons.Default.AutoAwesome
    )
  )

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(TealPrimary),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.LocalHospital, null, tint = Color.White, modifier = Modifier.size(22.dp))
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "CareNexus",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TealPrimary
          )
        }

        TextButton(
          onClick = onFinishOnboarding,
          modifier = Modifier.testTag("onboarding_skip_button")
        ) {
          Text("Skip", color = Slate500, fontWeight = FontWeight.SemiBold)
        }
      }

      // Main Illustration & Content
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 24.dp)
      ) {
        Box(
          modifier = Modifier
            .size(160.dp)
            .clip(CircleShape)
            .background(
              Brush.radialGradient(
                listOf(TealLight, Slate50)
              )
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = pages[currentStep].third,
            contentDescription = null,
            tint = TealPrimary,
            modifier = Modifier.size(80.dp)
          )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
          text = pages[currentStep].first,
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Center,
          color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = pages[currentStep].second,
          style = MaterialTheme.typography.bodyLarge,
          textAlign = TextAlign.Center,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          lineHeight = 22.sp
        )
      }

      // Indicator & Buttons
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Step Dots
        Row(
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier.padding(bottom = 24.dp)
        ) {
          repeat(3) { index ->
            Box(
              modifier = Modifier
                .padding(horizontal = 4.dp)
                .height(8.dp)
                .width(if (index == currentStep) 24.dp else 8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(if (index == currentStep) TealPrimary else Slate300)
            )
          }
        }

        Button(
          onClick = {
            if (currentStep < 2) currentStep++ else onFinishOnboarding()
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("onboarding_next_button"),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
        ) {
          Text(
            text = if (currentStep == 2) "Get Started" else "Continue",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.width(8.dp))
          Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
      }
    }
  }
}

@Composable
fun LoginScreen(
  repository: CareNexusRepository,
  onLoginSuccess: () -> Unit,
  onNavigateToRegister: () -> Unit
) {
  var email by remember { mutableStateOf("pradhansubham024@gmail.com") }
  var password by remember { mutableStateOf("CareNexus2026!") }
  var passwordVisible by remember { mutableStateOf(false) }
  var isOtpMode by remember { mutableStateOf(false) }
  var phoneOrOtp by remember { mutableStateOf("+1 (555) 234-5678") }
  var otpCode by remember { mutableStateOf("5829") }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var showForgotPasswordDialog by remember { mutableStateOf(false) }

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      // App Branding
      Box(
        modifier = Modifier
          .size(72.dp)
          .clip(RoundedCornerShape(20.dp))
          .background(TealPrimary),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.LocalHospital,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(42.dp)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "CareNexus",
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        color = TealPrimary
      )

      Text(
        text = "Smart Hospital Patient Portal",
        style = MaterialTheme.typography.bodyMedium,
        color = Slate500
      )

      Spacer(modifier = Modifier.height(32.dp))

      // Card Form
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Text(
            text = if (isOtpMode) "Sign In with Phone OTP" else "Patient Sign In",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )

          Spacer(modifier = Modifier.height(16.dp))

          if (!isOtpMode) {
            OutlinedTextField(
              value = email,
              onValueChange = { email = it; errorMessage = null },
              label = { Text("Email Address") },
              leadingIcon = { Icon(Icons.Default.Email, null) },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("login_email_input"),
              shape = RoundedCornerShape(12.dp),
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
              value = password,
              onValueChange = { password = it; errorMessage = null },
              label = { Text("Password") },
              leadingIcon = { Icon(Icons.Default.Lock, null) },
              trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                  Icon(
                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = null
                  )
                }
              },
              visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("login_password_input"),
              shape = RoundedCornerShape(12.dp),
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
          } else {
            OutlinedTextField(
              value = phoneOrOtp,
              onValueChange = { phoneOrOtp = it },
              label = { Text("Phone Number") },
              leadingIcon = { Icon(Icons.Default.Phone, null) },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
              value = otpCode,
              onValueChange = { otpCode = it },
              label = { Text("4-Digit OTP") },
              leadingIcon = { Icon(Icons.Default.Key, null) },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp)
            )
          }

          if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = errorMessage ?: "",
              color = ErrorRed,
              style = MaterialTheme.typography.bodySmall
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            TextButton(onClick = { isOtpMode = !isOtpMode }) {
              Text(
                text = if (isOtpMode) "Use Email / Password" else "Use OTP Login",
                style = MaterialTheme.typography.labelMedium,
                color = TealPrimary
              )
            }

            TextButton(onClick = { showForgotPasswordDialog = true }) {
              Text(
                text = "Forgot?",
                style = MaterialTheme.typography.labelMedium,
                color = Slate500
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Button(
            onClick = {
              val success = repository.login(email, password)
              if (success) {
                onLoginSuccess()
              } else {
                errorMessage = "Invalid credentials. Password must be at least 6 characters."
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("login_submit_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
          ) {
            Text(
              text = "Sign In",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Text("New patient to CareNexus?", color = Slate600)
        TextButton(
          onClick = onNavigateToRegister,
          modifier = Modifier.testTag("login_goto_register_button")
        ) {
          Text("Create Account", color = TealPrimary, fontWeight = FontWeight.Bold)
        }
      }
    }
  }

  if (showForgotPasswordDialog) {
    AlertDialog(
      onDismissRequest = { showForgotPasswordDialog = false },
      title = { Text("Password Reset") },
      text = { Text("A secure verification reset link has been dispatched to $email.") },
      confirmButton = {
        TextButton(onClick = { showForgotPasswordDialog = false }) {
          Text("Done")
        }
      }
    )
  }
}

@Composable
fun RegisterScreen(
  repository: CareNexusRepository,
  onRegisterSuccess: () -> Unit,
  onNavigateToLogin: () -> Unit
) {
  var fullName by remember { mutableStateOf("Subham Pradhan") }
  var dob by remember { mutableStateOf("1994-08-15") }
  var gender by remember { mutableStateOf("Male") }
  var phone by remember { mutableStateOf("+1 (555) 234-5678") }
  var email by remember { mutableStateOf("pradhansubham024@gmail.com") }
  var bloodGroup by remember { mutableStateOf("O+") }
  var address by remember { mutableStateOf("450 Medical Heights Blvd, Suite 300") }
  var emergencyName by remember { mutableStateOf("Anita Pradhan") }
  var emergencyPhone by remember { mutableStateOf("+1 (555) 987-6543") }
  var allergies by remember { mutableStateOf("Penicillin, Peanuts") }
  var password by remember { mutableStateOf("CareNexus2026!") }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "Patient Registration",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = TealPrimary
      )

      Text(
        text = "Register for hospital services and medical records",
        style = MaterialTheme.typography.bodyMedium,
        color = Slate500
      )

      Spacer(modifier = Modifier.height(20.dp))

      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Legal Name *") },
            leadingIcon = { Icon(Icons.Default.Person, null) },
            modifier = Modifier.fillMaxWidth().testTag("register_fullname_input"),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(12.dp))

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
              value = dob,
              onValueChange = { dob = it },
              label = { Text("DOB (YYYY-MM-DD)") },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
              value = bloodGroup,
              onValueChange = { bloodGroup = it },
              label = { Text("Blood Group") },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(12.dp)
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = gender,
            onValueChange = { gender = it },
            label = { Text("Gender") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number *") },
            leadingIcon = { Icon(Icons.Default.Phone, null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address *") },
            leadingIcon = { Icon(Icons.Default.Email, null) },
            modifier = Modifier.fillMaxWidth().testTag("register_email_input"),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Residential Address") },
            leadingIcon = { Icon(Icons.Default.Home, null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = "Emergency Contact Information",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Slate700
          )

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = emergencyName,
            onValueChange = { emergencyName = it },
            label = { Text("Emergency Contact Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = emergencyPhone,
            onValueChange = { emergencyPhone = it },
            label = { Text("Emergency Contact Phone") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = allergies,
            onValueChange = { allergies = it },
            label = { Text("Known Allergies") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Create Account Password *") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )

          if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(errorMessage ?: "", color = ErrorRed, style = MaterialTheme.typography.bodySmall)
          }

          Spacer(modifier = Modifier.height(20.dp))

          Button(
            onClick = {
              if (fullName.isBlank() || email.isBlank() || phone.isBlank() || password.length < 6) {
                errorMessage = "Please complete required fields (password min 6 chars)."
              } else {
                val profile = PatientProfile(
                  patientId = "PAT-2026-${(100..999).random()}",
                  fullName = fullName,
                  email = email,
                  phone = phone,
                  dateOfBirth = dob,
                  gender = gender,
                  bloodGroup = bloodGroup,
                  address = address,
                  emergencyContactName = emergencyName,
                  emergencyContactPhone = emergencyPhone,
                  emergencyRelationship = "Relative",
                  allergies = allergies,
                  profilePhotoUri = null,
                  role = "PATIENT"
                )
                repository.register(profile)
                onRegisterSuccess()
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("register_submit_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
          ) {
            Text("Complete Registration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Text("Already registered?", color = Slate600)
        TextButton(onClick = onNavigateToLogin) {
          Text("Sign In", color = TealPrimary, fontWeight = FontWeight.Bold)
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}
