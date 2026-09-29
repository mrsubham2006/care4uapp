package com.example.ui.screens.family

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.core.model.FamilyHealthRecord
import com.example.core.model.FamilyMember
import com.example.core.repository.CareNexusRepository
import com.example.ui.components.EmptyStateCard
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyMembersScreen(
  repository: CareNexusRepository,
  onBackClick: () -> Unit
) {
  val familyMembers by repository.familyMembers.collectAsState()
  val familyHealthRecords by repository.familyHealthRecords.collectAsState()
  val primaryPatient by repository.currentPatient.collectAsState()

  var selectedMemberId by remember {
    mutableStateOf(familyMembers.firstOrNull()?.id ?: "")
  }

  // Auto-select first member if current selection is invalid
  LaunchedEffect(familyMembers) {
    if (selectedMemberId.isEmpty() || familyMembers.none { it.id == selectedMemberId }) {
      selectedMemberId = familyMembers.firstOrNull()?.id ?: ""
    }
  }

  val selectedMember = familyMembers.find { it.id == selectedMemberId }
  val memberRecords = remember(familyHealthRecords, selectedMemberId) {
    familyHealthRecords.filter { it.familyMemberId == selectedMemberId }
  }

  var selectedTab by remember { mutableStateOf(0) } // 0: Health Records, 1: Immunization, 2: CareAI Summary
  var showAddMemberDialog by remember { mutableStateOf(false) }
  var showAddRecordDialog by remember { mutableStateOf(false) }
  var selectedRecordForDetail by remember { mutableStateOf<FamilyHealthRecord?>(null) }
  var aiHealthSummary by remember { mutableStateOf<String?>(null) }
  var isAiGenerating by remember { mutableStateOf(false) }

  val coroutineScope = rememberCoroutineScope()
  val context = LocalContext.current

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Shared Family Health Hub",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Slate900
            )
            Text(
              text = "Multi-Profile Care & Room Database Sync",
              style = MaterialTheme.typography.labelSmall,
              color = Slate500
            )
          }
        },
        navigationIcon = {
          IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Slate800)
          }
        },
        actions = {
          IconButton(
            onClick = { showAddMemberDialog = true },
            modifier = Modifier.testTag("add_family_member_top_btn")
          ) {
            Icon(Icons.Default.PersonAdd, contentDescription = "Add Family Member", tint = TealPrimary)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
      )
    },
    floatingActionButton = {
      if (selectedMember != null) {
        ExtendedFloatingActionButton(
          onClick = { showAddRecordDialog = true },
          containerColor = TealPrimary,
          contentColor = Color.White,
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier.testTag("add_family_record_fab")
        ) {
          Icon(Icons.Default.AddCircleOutline, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Add Record for ${selectedMember.name.split(" ").firstOrNull() ?: "Member"}", fontWeight = FontWeight.Bold)
        }
      }
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .background(Slate50)
        .padding(paddingValues),
      contentPadding = PaddingValues(bottom = 90.dp)
    ) {
      // 1. Family Members Horizontal Carousel
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(TealLight),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.Groups, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Family Profiles (${familyMembers.size + 1})",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = Slate900
                )
              }

              TextButton(
                onClick = { showAddMemberDialog = true }
              ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = TealPrimary)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Member", color = TealPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              // Primary User Pill
              item {
                FamilyMemberPill(
                  name = "${primaryPatient.fullName.split(" ").firstOrNull() ?: "You"} (Primary)",
                  relationship = "Account Holder",
                  isSelected = selectedMemberId == "PRIMARY",
                  bloodGroup = primaryPatient.bloodGroup,
                  onClick = {
                    selectedMemberId = "PRIMARY"
                    aiHealthSummary = null
                  }
                )
              }

              // Family Members
              items(familyMembers, key = { it.id }) { member ->
                FamilyMemberPill(
                  name = member.name,
                  relationship = member.relationship,
                  isSelected = selectedMemberId == member.id,
                  bloodGroup = member.bloodGroup,
                  onClick = {
                    selectedMemberId = member.id
                    aiHealthSummary = null
                  }
                )
              }
            }
          }
        }
      }

      // 2. Active Member Health Passport Banner
      if (selectedMember != null) {
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = NavyDark)
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(
                  Brush.horizontalGradient(
                    colors = listOf(NavyDark, Color(0xFF1E293B), TealDark)
                  )
                )
                .padding(18.dp)
            ) {
              Column {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                      modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(TealPrimary),
                      contentAlignment = Alignment.Center
                    ) {
                      Text(
                        text = selectedMember.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                      )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                          text = selectedMember.name,
                          style = MaterialTheme.typography.titleMedium,
                          fontWeight = FontWeight.Bold,
                          color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                          color = TealLight.copy(alpha = 0.25f),
                          shape = RoundedCornerShape(6.dp)
                        ) {
                          Text(
                            text = selectedMember.relationship,
                            color = TealAccent,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                          )
                        }
                      }
                      Text(
                        text = "DOB: ${selectedMember.dateOfBirth} • Insurance: ${selectedMember.insuranceId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate300
                      )
                    }
                  }

                  IconButton(
                    onClick = { repository.deleteFamilyMember(selectedMember.id) }
                  ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Member", tint = Slate400)
                  }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Medical Safety Highlights Strip
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NavyCard)
                    .padding(10.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column {
                    Text("Blood Group", style = MaterialTheme.typography.labelSmall, color = Slate400, fontSize = 10.sp)
                    Text(selectedMember.bloodGroup, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                  }
                  Box(modifier = Modifier.width(1.dp).height(24.dp).background(Slate700))
                  Column {
                    Text("Allergies", style = MaterialTheme.typography.labelSmall, color = Slate400, fontSize = 10.sp)
                    Text(selectedMember.allergies, color = WarningAmber, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                  }
                  Box(modifier = Modifier.width(1.dp).height(24.dp).background(Slate700))
                  Column {
                    Text("Conditions", style = MaterialTheme.typography.labelSmall, color = Slate400, fontSize = 10.sp)
                    Text(selectedMember.chronicConditions, color = Slate200, style = MaterialTheme.typography.bodySmall)
                  }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Emergency Dial Shortcut
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Call, contentDescription = null, tint = TealAccent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "Contact: ${selectedMember.emergencyContact}",
                      color = Slate300,
                      style = MaterialTheme.typography.labelSmall
                    )
                  }

                  TextButton(
                    onClick = {
                      try {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${selectedMember.phone.replace(" ", "")}"))
                        context.startActivity(intent)
                      } catch (e: Exception) {
                        // ignore
                      }
                    }
                  ) {
                    Text("Call Member", color = TealAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                  }
                }
              }
            }
          }
        }

        // 3. Navigation Tabs
        item {
          TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = TealPrimary,
            modifier = Modifier
              .padding(horizontal = 16.dp, vertical = 6.dp)
              .clip(RoundedCornerShape(14.dp))
          ) {
            Tab(
              selected = selectedTab == 0,
              onClick = { selectedTab = 0 },
              text = { Text("Clinical Records (${memberRecords.size})", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
            )
            Tab(
              selected = selectedTab == 1,
              onClick = { selectedTab = 1 },
              text = { Text("Vaccines & Growth", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
            )
            Tab(
              selected = selectedTab == 2,
              onClick = {
                selectedTab = 2
                if (aiHealthSummary == null) {
                  isAiGenerating = true
                  coroutineScope.launch {
                    aiHealthSummary = repository.generateFamilyHealthSummaryWithAi(selectedMember, memberRecords)
                    isAiGenerating = false
                  }
                }
              },
              text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("CareAI Brief", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
              }
            )
          }
        }

        // 4. Tab Contents
        when (selectedTab) {
          0 -> {
            // Clinical Records List
            if (memberRecords.isEmpty()) {
              item {
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Slate400, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No records found for ${selectedMember.name}", fontWeight = FontWeight.Bold, color = Slate700)
                    Text("Tap the '+' button below to log a consultation or medical document.", style = MaterialTheme.typography.bodySmall, color = Slate500)
                  }
                }
              }
            } else {
              items(memberRecords, key = { it.id }) { record ->
                FamilyHealthRecordCard(
                  record = record,
                  onClick = { selectedRecordForDetail = record },
                  onDelete = { repository.deleteFamilyHealthRecord(record.id) }
                )
              }
            }
          }

          1 -> {
            // Vaccines & Pediatric Growth Panel
            val vaxRecords = memberRecords.filter { it.recordType == "VACCINATION" || it.recordType == "GROWTH_RECORD" }
            if (vaxRecords.isEmpty()) {
              item {
                Card(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                  shape = RoundedCornerShape(16.dp),
                  colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                  Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                  ) {
                    Icon(Icons.Default.Vaccines, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No Immunizations Logged Yet", fontWeight = FontWeight.Bold, color = Slate800)
                    Text("Log pediatric vaccines, boosters, or growth percentiles to keep health data synced in Room.", style = MaterialTheme.typography.bodySmall, color = Slate600)
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                      onClick = { showAddRecordDialog = true },
                      colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                      shape = RoundedCornerShape(10.dp)
                    ) {
                      Text("Log Vaccine / Growth Record")
                    }
                  }
                }
              }
            } else {
              items(vaxRecords, key = { it.id }) { record ->
                FamilyHealthRecordCard(
                  record = record,
                  onClick = { selectedRecordForDetail = record },
                  onDelete = { repository.deleteFamilyHealthRecord(record.id) }
                )
              }
            }
          }

          2 -> {
            // CareAI Health Synthesis
            item {
              Card(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
              ) {
                Column(modifier = Modifier.padding(18.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Box(
                        modifier = Modifier
                          .size(36.dp)
                          .clip(CircleShape)
                          .background(TealLight),
                        contentAlignment = Alignment.Center
                      ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = TealPrimary)
                      }
                      Spacer(modifier = Modifier.width(10.dp))
                      Column {
                        Text(
                          text = "CareAI Family Health Brief",
                          style = MaterialTheme.typography.titleMedium,
                          fontWeight = FontWeight.Bold,
                          color = Slate900
                        )
                        Text(
                          text = "Personalized for ${selectedMember.name} (${selectedMember.relationship})",
                          style = MaterialTheme.typography.labelSmall,
                          color = Slate500
                        )
                      }
                    }

                    IconButton(
                      onClick = {
                        isAiGenerating = true
                        coroutineScope.launch {
                          aiHealthSummary = repository.generateFamilyHealthSummaryWithAi(selectedMember, memberRecords)
                          isAiGenerating = false
                        }
                      }
                    ) {
                      Icon(Icons.Default.Refresh, contentDescription = "Regenerate", tint = TealPrimary)
                    }
                  }

                  Spacer(modifier = Modifier.height(14.dp))

                  if (isAiGenerating) {
                    Column(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                      horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                      CircularProgressIndicator(color = TealPrimary)
                      Spacer(modifier = Modifier.height(12.dp))
                      Text("Synthesizing family health milestones and risk factors...", style = MaterialTheme.typography.bodySmall, color = Slate600)
                    }
                  } else {
                    Text(
                      text = aiHealthSummary ?: "CareAI has synthesized records: Routine immunizations are up-to-date. Keep tracking growth percentiles and schedule pediatric dental review at 3.5 years.",
                      style = MaterialTheme.typography.bodyMedium,
                      color = Slate800,
                      lineHeight = 22.sp
                    )
                  }
                }
              }
            }
          }
        }
      } else {
        // Fallback if no family members
        item {
          EmptyStateCard(
            icon = Icons.Default.FamilyRestroom,
            title = "No Family Profiles Added",
            subtitle = "Link family members to securely manage pediatric checkups, senior vitals, and immunization records locally in Room.",
            actionText = "Add First Family Member",
            onActionClick = { showAddMemberDialog = true }
          )
        }
      }
    }
  }

  // Add Family Member Dialog
  if (showAddMemberDialog) {
    AddFamilyMemberDialog(
      onDismiss = { showAddMemberDialog = false },
      onSave = { name, relationship, dob, gender, bloodGroup, phone, allergies, chronicConditions, insuranceId ->
        repository.addFamilyMember(
          FamilyMember(
            id = "FAM-${System.currentTimeMillis() % 10000}",
            primaryPatientId = "PAT-2026-089",
            name = name,
            relationship = relationship,
            dateOfBirth = dob,
            gender = gender,
            bloodGroup = bloodGroup,
            phone = phone,
            emergencyContact = "Subham Pradhan",
            allergies = allergies.ifBlank { "None reported" },
            chronicConditions = chronicConditions.ifBlank { "None" },
            insuranceId = insuranceId.ifBlank { "INS-CARE-9021" }
          )
        )
        showAddMemberDialog = false
      }
    )
  }

  // Add Health Record Dialog
  if (showAddRecordDialog && selectedMember != null) {
    AddFamilyRecordDialog(
      memberName = selectedMember.name,
      onDismiss = { showAddRecordDialog = false },
      onSave = { recordType, title, doctorName, department, summary, details, attachment ->
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        repository.addFamilyHealthRecord(
          FamilyHealthRecord(
            id = "FAM-REC-${System.currentTimeMillis() % 10000}",
            familyMemberId = selectedMember.id,
            memberName = selectedMember.name,
            recordType = recordType,
            title = title,
            doctorName = doctorName,
            department = department,
            date = dateStr,
            summary = summary,
            details = details,
            status = "VERIFIED",
            attachmentName = attachment.ifBlank { "${recordType.lowercase()}_doc.pdf" }
          )
        )
        showAddRecordDialog = false
      }
    )
  }

  // Record Detail Modal
  if (selectedRecordForDetail != null) {
    val rec = selectedRecordForDetail!!
    Dialog(onDismissRequest = { selectedRecordForDetail = null }) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = rec.recordType.replace("_", " "),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = TealDark
            )
            IconButton(onClick = { selectedRecordForDetail = null }) {
              Icon(Icons.Default.Close, contentDescription = "Close")
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = rec.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Slate900
          )
          Text(
            text = "${rec.doctorName} • ${rec.department} • ${rec.date}",
            style = MaterialTheme.typography.bodySmall,
            color = Slate600
          )

          Spacer(modifier = Modifier.height(14.dp))

          Text("Clinical Summary", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, color = Slate700)
          Text(rec.summary, style = MaterialTheme.typography.bodyMedium, color = Slate800)

          Spacer(modifier = Modifier.height(10.dp))

          Text("Physician Notes & Directives", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, color = Slate700)
          Text(rec.details, style = MaterialTheme.typography.bodySmall, color = Slate700)

          Spacer(modifier = Modifier.height(14.dp))

          if (rec.attachmentName != null) {
            Surface(
              color = Slate100,
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFEF4444))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(rec.attachmentName, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                  Text("Stored securely in local Room DB", style = MaterialTheme.typography.labelSmall, color = Slate500)
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Button(
            onClick = { selectedRecordForDetail = null },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
          ) {
            Text("Done", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
fun FamilyMemberPill(
  name: String,
  relationship: String,
  isSelected: Boolean,
  bloodGroup: String,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = if (isSelected) TealPrimary else Slate100,
    modifier = Modifier.clickable { onClick() }
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(28.dp)
          .clip(CircleShape)
          .background(if (isSelected) Color.White.copy(alpha = 0.25f) else TealLight),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = name.take(1),
          fontWeight = FontWeight.Bold,
          color = if (isSelected) Color.White else TealPrimary,
          fontSize = 13.sp
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      Column {
        Text(
          text = name,
          fontWeight = FontWeight.Bold,
          color = if (isSelected) Color.White else Slate900,
          style = MaterialTheme.typography.bodySmall
        )
        Text(
          text = "$relationship • $bloodGroup",
          style = MaterialTheme.typography.labelSmall,
          color = if (isSelected) TealAccent else Slate500,
          fontSize = 10.sp
        )
      }
    }
  }
}

@Composable
fun FamilyHealthRecordCard(
  record: FamilyHealthRecord,
  onClick: () -> Unit,
  onDelete: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 5.dp)
      .clickable { onClick() },
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
              when (record.recordType) {
                "VACCINATION" -> Color(0xFFE0F2FE)
                "GROWTH_RECORD" -> Color(0xFFFEF3C7)
                "LAB_REPORT" -> Color(0xFFF3E8FF)
                else -> TealLight
              }
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            when (record.recordType) {
              "VACCINATION" -> Icons.Default.Vaccines
              "GROWTH_RECORD" -> Icons.Default.Height
              "LAB_REPORT" -> Icons.Default.Biotech
              "PRESCRIPTION" -> Icons.Default.Medication
              else -> Icons.Default.MedicalServices
            },
            contentDescription = null,
            tint = when (record.recordType) {
              "VACCINATION" -> Color(0xFF0284C7)
              "GROWTH_RECORD" -> Color(0xFFD97706)
              "LAB_REPORT" -> Color(0xFF9333EA)
              else -> TealPrimary
            },
            modifier = Modifier.size(22.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = record.title,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = Slate900,
              maxLines = 1
            )
          }

          Text(
            text = "${record.doctorName} • ${record.department} • ${record.date}",
            style = MaterialTheme.typography.bodySmall,
            color = Slate500
          )

          Text(
            text = record.summary,
            style = MaterialTheme.typography.labelSmall,
            color = Slate700,
            maxLines = 1
          )
        }
      }

      IconButton(onClick = onDelete) {
        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Slate400, modifier = Modifier.size(20.dp))
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFamilyMemberDialog(
  onDismiss: () -> Unit,
  onSave: (String, String, String, String, String, String, String, String, String) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var relationship by remember { mutableStateOf("Child") }
  var dob by remember { mutableStateOf("2023-01-10") }
  var gender by remember { mutableStateOf("Male") }
  var bloodGroup by remember { mutableStateOf("O+") }
  var phone by remember { mutableStateOf("+1 (555) 234-5678") }
  var allergies by remember { mutableStateOf("None known") }
  var chronicConditions by remember { mutableStateOf("None") }
  var insuranceId by remember { mutableStateOf("INS-CARE-9021-05") }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Add Family Member",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Slate900
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Full Name *") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = relationship,
            onValueChange = { relationship = it },
            label = { Text("Relationship") },
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

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = dob,
            onValueChange = { dob = it },
            label = { Text("Date of Birth") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
          )
          OutlinedTextField(
            value = gender,
            onValueChange = { gender = it },
            label = { Text("Gender") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = allergies,
          onValueChange = { allergies = it },
          label = { Text("Allergies (e.g. Penicillin, Peanuts)") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = chronicConditions,
          onValueChange = { chronicConditions = it },
          label = { Text("Chronic Conditions (e.g. Asthma)") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
          onClick = {
            if (name.isNotBlank()) {
              onSave(name, relationship, dob, gender, bloodGroup, phone, allergies, chronicConditions, insuranceId)
            }
          },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
        ) {
          Text("Save & Sync to Room DB", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFamilyRecordDialog(
  memberName: String,
  onDismiss: () -> Unit,
  onSave: (String, String, String, String, String, String, String) -> Unit
) {
  var recordType by remember { mutableStateOf("CONSULTATION") }
  var title by remember { mutableStateOf("") }
  var doctorName by remember { mutableStateOf("Dr. Priya Das") }
  var department by remember { mutableStateOf("Pediatrics") }
  var summary by remember { mutableStateOf("") }
  var details by remember { mutableStateOf("") }
  var attachment by remember { mutableStateOf("") }

  val recordTypes = listOf("CONSULTATION", "VACCINATION", "GROWTH_RECORD", "LAB_REPORT", "PRESCRIPTION")

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Add Record for $memberName",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Slate900
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Record type selector
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          items(recordTypes) { type ->
            FilterChip(
              selected = recordType == type,
              onClick = { recordType = type },
              label = { Text(type.replace("_", " "), fontSize = 11.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = TealPrimary,
                selectedLabelColor = Color.White
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Record Title *") },
          placeholder = { Text("e.g. Annual Growth Checkup, DTaP Booster") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = doctorName,
            onValueChange = { doctorName = it },
            label = { Text("Attending Doctor") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
          )
          OutlinedTextField(
            value = department,
            onValueChange = { department = it },
            label = { Text("Department") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = summary,
          onValueChange = { summary = it },
          label = { Text("Clinical Summary") },
          placeholder = { Text("e.g. Normal weight/height, booster completed") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = details,
          onValueChange = { details = it },
          label = { Text("Detailed Directives / Notes") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
          onClick = {
            if (title.isNotBlank()) {
              onSave(recordType, title, doctorName, department, summary, details, attachment)
            }
          },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
        ) {
          Text("Save to Family Record", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
