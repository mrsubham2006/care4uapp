package com.example.ui.screens.scanner

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.DocumentCategory
import com.example.core.repository.CareNexusRepository
import com.example.ui.theme.*

@Composable
fun DocumentAndQrScannerScreen(
  repository: CareNexusRepository,
  onScanSuccess: () -> Unit,
  onBackClick: () -> Unit
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  val tabs = listOf("Document Scanner", "Hospital QR Code")

  // Scanner States
  var flashOn by remember { mutableStateOf(false) }
  var capturedDocName by remember { mutableStateOf("External_Blood_Test_Sep2026.pdf") }
  var capturedCategory by remember { mutableStateOf(DocumentCategory.LAB_REPORT) }
  var isCapturing by remember { mutableStateOf(false) }
  var hasCaptured by remember { mutableStateOf(false) }
  var qrScanResult by remember { mutableStateOf<String?>(null) }
  var qrSuccessMessage by remember { mutableStateOf<String?>(null) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(bottom = 80.dp)
  ) {
    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = MaterialTheme.colorScheme.surface,
      contentColor = TealPrimary
    ) {
      tabs.forEachIndexed { index, title ->
        Tab(
          selected = selectedTab == index,
          onClick = { selectedTab = index; hasCaptured = false; qrScanResult = null },
          text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
        )
      }
    }

    if (selectedTab == 0) {
      // Document Scanner Flow
      if (hasCaptured) {
        // Document Confirmation & AI Tagging Screen
        Column(
          modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text("Document Captured", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(6.dp))
          Text("CareAI has classified this document. You can adjust the category before saving.", style = MaterialTheme.typography.bodySmall, color = Slate600, textAlign = TextAlign.Center)

          Spacer(modifier = Modifier.height(16.dp))

          // Preview Canvas
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(240.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(Slate900),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(Icons.Default.Description, null, tint = TealAccent, modifier = Modifier.size(54.dp))
              Spacer(modifier = Modifier.height(8.dp))
              Text("Scanned Medical Document Preview", color = Color.White, style = MaterialTheme.typography.bodySmall)
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          OutlinedTextField(
            value = capturedDocName,
            onValueChange = { capturedDocName = it },
            label = { Text("Document Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(14.dp))

          Text("Document Classification", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            listOf(DocumentCategory.LAB_REPORT, DocumentCategory.PRESCRIPTION, DocumentCategory.INSURANCE).forEach { cat ->
              FilterChip(
                selected = capturedCategory == cat,
                onClick = { capturedCategory = cat },
                label = { Text(cat.name.replace("_", " "), fontSize = 11.sp) },
                shape = RoundedCornerShape(10.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(24.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedButton(
              onClick = { hasCaptured = false },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(12.dp)
            ) {
              Text("Retake")
            }

            Button(
              onClick = {
                repository.uploadDocument(
                  name = capturedDocName,
                  uri = "content://carenexus/docs/${System.currentTimeMillis()}.pdf",
                  size = "1.4 MB",
                  manualCategory = capturedCategory
                )
                onScanSuccess()
              },
              modifier = Modifier.weight(1.2f).testTag("confirm_upload_doc_btn"),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
              Text("Confirm & Upload", fontWeight = FontWeight.Bold)
            }
          }
        }
      } else {
        // Live Camera Scanner Viewfinder
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
        ) {
          // Viewfinder Background
          Box(
            modifier = Modifier
              .fillMaxSize()
              .clip(RoundedCornerShape(24.dp))
              .background(NavyDark),
            contentAlignment = Alignment.Center
          ) {
            // Scanner Guide Box
            Box(
              modifier = Modifier
                .fillMaxWidth(0.85f)
                .fillMaxHeight(0.65f)
                .border(2.dp, if (flashOn) TealAccent else Color.White.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                .padding(16.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "Align document within the frame",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodySmall
              )
            }

            // Top Flash and Camera controls
            Row(
              modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(16.dp),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              IconButton(
                onClick = { flashOn = !flashOn },
                modifier = Modifier.clip(CircleShape).background(Color.Black.copy(alpha = 0.4f))
              ) {
                Icon(
                  imageVector = if (flashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                  contentDescription = "Flash",
                  tint = if (flashOn) WarningAmber else Color.White
                )
              }
              Surface(color = Color.Black.copy(alpha = 0.4f), shape = RoundedCornerShape(12.dp)) {
                Text("AI Auto-Detect Active", color = TealAccent, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
              }
            }

            // Bottom Shutter Button
            Column(
              modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              IconButton(
                onClick = { hasCaptured = true },
                modifier = Modifier
                  .size(72.dp)
                  .clip(CircleShape)
                  .background(Color.White)
                  .padding(4.dp)
                  .border(3.dp, TealPrimary, CircleShape)
                  .testTag("camera_shutter_button")
              ) {
                Box(modifier = Modifier.size(52.dp).clip(CircleShape).background(TealPrimary))
              }
              Spacer(modifier = Modifier.height(8.dp))
              Text("Tap to Scan Document", color = Color.White, style = MaterialTheme.typography.labelMedium)
            }
          }
        }
      }
    } else {
      // QR Code Scanner Tab
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp)
      ) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(NavyDark),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(20.dp)
          ) {
            Text(
              text = "Hospital QR Scanner",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Point camera at Reception Kiosk or Appointment Slip",
              style = MaterialTheme.typography.bodySmall,
              color = Slate400,
              textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // QR Target Box
            Box(
              modifier = Modifier
                .size(240.dp)
                .border(2.5.dp, TealAccent, RoundedCornerShape(20.dp))
                .padding(20.dp),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.QrCodeScanner, null, tint = TealAccent.copy(alpha = 0.7f), modifier = Modifier.size(100.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (qrSuccessMessage != null) {
              Card(
                colors = CardDefaults.cardColors(containerColor = SuccessLight),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(qrSuccessMessage ?: "", color = SuccessGreen, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
              }
              Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
              onClick = {
                val (success, msg) = repository.processQrCheckIn("CARENEXUS://CHECKIN/ROOM304/PAT-2026-089")
                qrSuccessMessage = msg
              },
              modifier = Modifier.fillMaxWidth().height(48.dp).testTag("simulate_qr_scan_btn"),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
              Icon(Icons.Default.QrCode, null)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Simulate Hospital Check-In QR")
            }
          }
        }
      }
    }
  }
}
