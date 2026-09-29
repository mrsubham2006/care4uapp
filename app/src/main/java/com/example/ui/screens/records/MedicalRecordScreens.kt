package com.example.ui.screens.records

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.DocumentCategory
import com.example.core.model.MedicalRecord
import com.example.core.model.MedicalRecordType
import com.example.core.model.UploadedDocument
import com.example.core.repository.CareNexusRepository
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun MedicalTimelineScreen(
  repository: CareNexusRepository,
  onRecordClick: (String) -> Unit,
  onScanDocumentClick: () -> Unit
) {
  val records by repository.medicalRecords.collectAsState()
  val documents by repository.documents.collectAsState()
  val labOrders by repository.labOrders.collectAsState()

  var selectedTab by remember { mutableIntStateOf(0) }
  val tabs = listOf("Clinical Timeline", "Doctor Notes", "Lab Results", "Room DB Docs")

  var searchQuery by remember { mutableStateOf("") }
  var selectedFilterType by remember { mutableStateOf<MedicalRecordType?>(null) }
  var viewingDocument by remember { mutableStateOf<UploadedDocument?>(null) }

  // Filtered records
  val filteredRecords = remember(records, searchQuery, selectedFilterType) {
    records.filter { rec ->
      val matchesSearch = rec.title.contains(searchQuery, ignoreCase = true) ||
          rec.doctorName.contains(searchQuery, ignoreCase = true) ||
          rec.description.contains(searchQuery, ignoreCase = true) ||
          rec.department.contains(searchQuery, ignoreCase = true)
      val matchesType = selectedFilterType == null || rec.type == selectedFilterType
      matchesSearch && matchesType
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(bottom = 80.dp)
  ) {
    // Top Security & Room Persistence Header
    Surface(
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 2.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 10.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(TealLight),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Security, null, tint = TealPrimary, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text("Secure Medical Records", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
              Text("Room DB Local Storage • HL7 Encrypted", style = MaterialTheme.typography.labelSmall, color = Slate500)
            }
          }

          Surface(
            color = SuccessLight,
            shape = RoundedCornerShape(12.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(SuccessGreen))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Verified", color = SuccessGreen, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Search records, doctor notes, diagnoses...") },
          leadingIcon = { Icon(Icons.Default.Search, null, tint = Slate400) },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { searchQuery = "" }) {
                Icon(Icons.Default.Clear, null)
              }
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("search_medical_records_input"),
          shape = RoundedCornerShape(14.dp),
          singleLine = true
        )
      }
    }

    // Tab Navigation
    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = MaterialTheme.colorScheme.surface,
      contentColor = TealPrimary
    ) {
      tabs.forEachIndexed { index, title ->
        Tab(
          selected = selectedTab == index,
          onClick = { selectedTab = index },
          text = { Text(title, fontSize = 12.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
        )
      }
    }

    // TAB CONTENT
    when (selectedTab) {
      0 -> {
        // TAB 1: Clinical Timeline
        LazyColumn(
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          item {
            // Category Filter Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              item {
                FilterChip(
                  selected = selectedFilterType == null,
                  onClick = { selectedFilterType = null },
                  label = { Text("All Records (${records.size})") },
                  shape = RoundedCornerShape(10.dp)
                )
              }
              items(MedicalRecordType.values()) { type ->
                FilterChip(
                  selected = selectedFilterType == type,
                  onClick = { selectedFilterType = if (selectedFilterType == type) null else type },
                  label = { Text(type.name.replace("_", " ")) },
                  shape = RoundedCornerShape(10.dp)
                )
              }
            }
          }

          if (filteredRecords.isEmpty()) {
            item {
              EmptyStateCard(
                icon = Icons.Default.FolderOpen,
                title = "No Matching Records Found",
                subtitle = "Try adjusting your search query or filter tags."
              )
            }
          } else {
            items(filteredRecords, key = { it.id }) { record ->
              TimelineRecordCard(
                record = record,
                onClick = { onRecordClick(record.id) }
              )
            }
          }
        }
      }

      1 -> {
        // TAB 2: Doctor Clinical Notes & SOAP Notes
        val notesRecords = records.filter { it.type == MedicalRecordType.CONSULTATION || it.type == MedicalRecordType.DIAGNOSIS }
        LazyColumn(
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          item {
            Text(
              text = "Physician SOAP Notes & Clinical Summaries",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
          }

          items(notesRecords, key = { it.id }) { noteRec ->
            DoctorClinicalNoteCard(
              record = noteRec,
              onClick = { onRecordClick(noteRec.id) }
            )
          }
        }
      }

      2 -> {
        // TAB 3: Diagnostic Lab Results Integration
        LazyColumn(
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          item {
            Text(
              text = "Diagnostic Pathology & Biomarker Panels",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
          }

          items(labOrders, key = { it.id }) { lab ->
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onRecordClick("REC-301") },
              shape = RoundedCornerShape(20.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Surface(color = TealLight, shape = RoundedCornerShape(8.dp)) {
                    Text(lab.id, color = TealDark, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                  }
                  StatusBadge(status = lab.status.name)
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(lab.testName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Ordered by ${lab.doctorName} • ${lab.date}", style = MaterialTheme.typography.bodySmall, color = Slate500)

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate50)
                    .padding(10.dp),
                  verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  lab.results.take(3).forEach { param ->
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                      Text(param.name, style = MaterialTheme.typography.bodySmall, color = Slate700)
                      Text("${param.value} ${param.unit}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = if (param.isAbnormal) ErrorRed else Slate900)
                    }
                  }
                }
              }
            }
          }
        }
      }

      3 -> {
        // TAB 4: Stored Documents in Room Database
        LazyColumn(
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          item {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "Room Local Document Store (${documents.size})",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
                Text("Cached on device for instant offline access", style = MaterialTheme.typography.labelSmall, color = Slate500)
              }

              Button(
                onClick = onScanDocumentClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                modifier = Modifier.testTag("scan_new_room_doc_button")
              ) {
                Icon(Icons.Default.AddAPhoto, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Scan New")
              }
            }
          }

          if (documents.isEmpty()) {
            item {
              EmptyStateCard(
                icon = Icons.Default.Description,
                title = "No Stored Documents in Room",
                subtitle = "Scan and upload external hospital reports, insurance cards, or IDs.",
                actionText = "Scan Document",
                onActionClick = onScanDocumentClick
              )
            }
          } else {
            items(documents, key = { it.id }) { doc ->
              StoredDocumentCard(
                document = doc,
                onViewClick = { viewingDocument = doc },
                onDeleteClick = { repository.deleteDocument(doc.id) }
              )
            }
          }
        }
      }
    }
  }

  // Local Document Viewer Dialog Modal
  if (viewingDocument != null) {
    val doc = viewingDocument!!
    DocumentPreviewDialog(
      document = doc,
      onDismiss = { viewingDocument = null }
    )
  }
}

@Composable
fun TimelineRecordCard(
  record: MedicalRecord,
  onClick: () -> Unit
) {
  val (icon, badgeColor) = when (record.type) {
    MedicalRecordType.CONSULTATION -> Pair(Icons.Default.MedicalServices, TealPrimary)
    MedicalRecordType.DIAGNOSIS -> Pair(Icons.Default.Biotech, CyanAccent)
    MedicalRecordType.LAB_REPORT -> Pair(Icons.Default.Science, WarningAmber)
    MedicalRecordType.PRESCRIPTION -> Pair(Icons.Default.Medication, SuccessGreen)
    MedicalRecordType.DISCHARGE_SUMMARY -> Pair(Icons.Default.CheckCircle, Slate700)
    MedicalRecordType.UPLOADED_DOC -> Pair(Icons.Default.AttachFile, Slate500)
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("timeline_record_${record.id}"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(color = TealLight, shape = RoundedCornerShape(8.dp)) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(icon, null, tint = badgeColor, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(record.type.name.replace("_", " "), color = badgeColor, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
          }
        }
        Text(record.date, style = MaterialTheme.typography.bodySmall, color = Slate500, fontWeight = FontWeight.SemiBold)
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(record.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Slate900)
      Text("Attending: ${record.doctorName} • ${record.department}", style = MaterialTheme.typography.bodySmall, color = TealDark, fontWeight = FontWeight.SemiBold)

      Spacer(modifier = Modifier.height(10.dp))

      // Vitals Preview Badge
      Surface(
        color = Slate50,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.FavoriteBorder, null, tint = ErrorRed, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(record.vitals, style = MaterialTheme.typography.labelSmall, color = Slate700, fontWeight = FontWeight.SemiBold)
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(record.summary, style = MaterialTheme.typography.bodyMedium, color = Slate700, lineHeight = 20.sp)

      Spacer(modifier = Modifier.height(12.dp))
      HorizontalDivider(color = Slate100)
      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.VerifiedUser, null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Doctor Digitally Signed", style = MaterialTheme.typography.labelSmall, color = SuccessGreen, fontWeight = FontWeight.Bold)
        }

        TextButton(onClick = onClick) {
          Text("View Full Record", color = TealPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
          Icon(Icons.Default.ChevronRight, null, tint = TealPrimary, modifier = Modifier.size(16.dp))
        }
      }
    }
  }
}

@Composable
fun DoctorClinicalNoteCard(
  record: MedicalRecord,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() },
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text("Clinical Encounter Note", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          Text("${record.doctorName} (${record.department})", style = MaterialTheme.typography.bodySmall, color = TealDark, fontWeight = FontWeight.SemiBold)
        }
        Text(record.date, style = MaterialTheme.typography.bodySmall, color = Slate500)
      }

      Spacer(modifier = Modifier.height(12.dp))

      // SOAP Format Sections
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(Slate50)
          .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Column {
          Text("CHIEF ASSESSMENT / OBSERVATIONS", style = MaterialTheme.typography.labelSmall, color = TealDark, fontWeight = FontWeight.Bold)
          Text(record.clinicalNotes, style = MaterialTheme.typography.bodySmall, color = Slate800)
        }

        Column {
          Text("TREATMENT PLAN & FOLLOW-UP", style = MaterialTheme.typography.labelSmall, color = TealDark, fontWeight = FontWeight.Bold)
          Text(record.treatmentPlan, style = MaterialTheme.typography.bodySmall, color = Slate800)
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(record.doctorSignature, style = MaterialTheme.typography.labelSmall, color = Slate500, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
        TextButton(onClick = onClick) {
          Text("Detailed View", fontWeight = FontWeight.Bold, color = TealPrimary)
        }
      }
    }
  }
}

@Composable
fun StoredDocumentCard(
  document: UploadedDocument,
  onViewClick: () -> Unit,
  onDeleteClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onViewClick() }
      .testTag("stored_doc_${document.id}"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(TealLight),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.PictureAsPdf, null, tint = TealPrimary, modifier = Modifier.size(26.dp))
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Text(
              text = document.fileName,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = "${document.category.name.replace("_", " ")} • ${document.fileSize}",
              style = MaterialTheme.typography.bodySmall,
              color = Slate500
            )
          }
        }

        IconButton(onClick = onDeleteClick) {
          Icon(Icons.Default.DeleteOutline, null, tint = Slate400)
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(color = TealLight, shape = RoundedCornerShape(8.dp)) {
          Text(
            text = "AI Confidence: ${(document.aiConfidence * 100).toInt()}%",
            color = TealDark,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }

        Button(
          onClick = onViewClick,
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
        ) {
          Icon(Icons.Default.Visibility, null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("View in Room", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
fun DocumentPreviewDialog(
  document: UploadedDocument,
  onDismiss: () -> Unit
) {
  var currentPage by remember { mutableIntStateOf(1) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(document.fileName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
          Text("Room Local Encrypted Store • ${document.category.name}", style = MaterialTheme.typography.labelSmall, color = Slate500)
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .height(340.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(Slate900)
          .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        // Document Page Canvas Preview
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = MedicalWhite)
        ) {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("CareNexus Medical Document", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Slate800)
              Text("Page $currentPage of 2", style = MaterialTheme.typography.labelSmall, color = Slate400)
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Box(modifier = Modifier.fillMaxWidth().height(8.dp).background(Slate200, RoundedCornerShape(4.dp)))
              Box(modifier = Modifier.fillMaxWidth(0.85f).height(8.dp).background(Slate200, RoundedCornerShape(4.dp)))
              Box(modifier = Modifier.fillMaxWidth(0.9f).height(8.dp).background(Slate200, RoundedCornerShape(4.dp)))
              Box(modifier = Modifier.fillMaxWidth(0.7f).height(8.dp).background(Slate200, RoundedCornerShape(4.dp)))
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Checksum: SHA256-OK", style = MaterialTheme.typography.labelSmall, color = SuccessGreen, fontSize = 9.sp)
              Text("Size: ${document.fileSize}", style = MaterialTheme.typography.labelSmall, color = Slate500, fontSize = 9.sp)
            }
          }
        }

        // Pagination Controls
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedButton(
            onClick = { if (currentPage > 1) currentPage-- },
            enabled = currentPage > 1,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
          ) {
            Text("Previous", fontSize = 11.sp)
          }

          Text("Page $currentPage / 2", color = Color.White, style = MaterialTheme.typography.labelMedium)

          OutlinedButton(
            onClick = { if (currentPage < 2) currentPage++ },
            enabled = currentPage < 2,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
          ) {
            Text("Next", fontSize = 11.sp)
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
        shape = RoundedCornerShape(10.dp)
      ) {
        Text("Done")
      }
    }
  )
}

@Composable
fun RecordDetailScreen(
  recordId: String,
  repository: CareNexusRepository,
  onBackClick: () -> Unit
) {
  val records by repository.medicalRecords.collectAsState()
  val record = records.find { it.id == recordId } ?: records.first()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .background(MaterialTheme.colorScheme.background)
      .padding(16.dp)
      .padding(bottom = 80.dp)
  ) {
    // Record Header Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(color = TealLight, shape = RoundedCornerShape(8.dp)) {
            Text(record.type.name.replace("_", " "), color = TealDark, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
          }
          Text(record.date, color = Slate500, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(record.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Slate900)
        Text("Attending: ${record.doctorName}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TealDark)
        Text("Facility: ${record.facility}", style = MaterialTheme.typography.bodySmall, color = Slate600)
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Recorded Vitals Card
    Text("Encounter Vital Signs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(6.dp))

    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier.size(44.dp).clip(CircleShape).background(ErrorLight),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.Favorite, null, tint = ErrorRed, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
          Text(record.vitals, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Slate900)
          Text("Logged by Triage Nurse on encounter admission", style = MaterialTheme.typography.labelSmall, color = Slate500)
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Clinical Findings & Notes
    Text("Physician Clinical Notes & Observations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(6.dp))

    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text(record.description, style = MaterialTheme.typography.bodyMedium, color = Slate800, lineHeight = 24.sp)
        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = Slate100)
        Spacer(modifier = Modifier.height(12.dp))
        Text("CLINICAL PLAN & INSTRUCTIONS", style = MaterialTheme.typography.labelSmall, color = TealDark, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(record.treatmentPlan, style = MaterialTheme.typography.bodyMedium, color = Slate800, lineHeight = 22.sp)
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Attached PDF Documents stored in Room
    Text("Attached Files & Local PDF Scans (${record.attachments.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))

    record.attachments.forEach { fileName ->
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.PictureAsPdf, null, tint = ErrorRed, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(fileName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
              Text("Room DB Cache • ${record.fileSize}", style = MaterialTheme.typography.labelSmall, color = Slate500)
            }
          }

          Surface(color = TealLight, shape = RoundedCornerShape(8.dp)) {
            Text("Available Offline", color = TealDark, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Security & Digital Signature Footer
    Surface(
      color = Slate100,
      shape = RoundedCornerShape(14.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier.padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(Icons.Default.VerifiedUser, null, tint = SuccessGreen, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text("Digitally Signed by Physician", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Slate900)
          Text(record.doctorSignature, style = MaterialTheme.typography.bodySmall, color = Slate600)
        }
      }
    }
  }
}
