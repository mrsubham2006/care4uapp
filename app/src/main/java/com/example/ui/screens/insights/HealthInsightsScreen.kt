package com.example.ui.screens.insights

import android.content.Intent
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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.WeeklyHealthInsightReport
import com.example.core.repository.CareNexusRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthInsightsScreen(
  repository: CareNexusRepository,
  onBackClick: () -> Unit,
  onNavigateToCareAi: () -> Unit
) {
  val currentReport by repository.weeklyHealthInsights.collectAsState()
  val vitals by repository.vitals.collectAsState()
  val labOrders by repository.labOrders.collectAsState()
  val prescriptions by repository.prescriptions.collectAsState()
  val records by repository.medicalRecords.collectAsState()
  val reminders by repository.medicationReminders.collectAsState()

  var selectedWeek by remember { mutableStateOf("Sep 23 - Sep 29, 2026") }
  val weekOptions = listOf(
    "Sep 23 - Sep 29, 2026",
    "Sep 16 - Sep 22, 2026",
    "Sep 09 - Sep 15, 2026"
  )

  var isGenerating by remember { mutableStateOf(false) }
  val coroutineScope = rememberCoroutineScope()
  val context = LocalContext.current

  // Generate initial insights if null
  LaunchedEffect(selectedWeek) {
    if (currentReport == null || currentReport?.weekPeriod != selectedWeek) {
      isGenerating = true
      repository.generateWeeklyHealthInsights(selectedWeek)
      isGenerating = false
    }
  }

  val report = currentReport

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "CareAI Health Insights",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Slate900
            )
            Text(
              text = "Aggregated Multi-Source Health Trends",
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
            onClick = {
              isGenerating = true
              coroutineScope.launch {
                repository.generateWeeklyHealthInsights(selectedWeek)
                isGenerating = false
              }
            },
            modifier = Modifier.testTag("refresh_insights_btn")
          ) {
            Icon(Icons.Default.Refresh, contentDescription = "Re-analyze with Gemini", tint = TealPrimary)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
      )
    },
    bottomBar = {
      Surface(
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = {
              val shareText = buildString {
                appendLine("CareNexus Health Insights Weekly Brief ($selectedWeek)")
                appendLine("Overall Health Score: ${report?.healthScore ?: 88}/100 (${report?.scoreDelta ?: "+4%"})")
                appendLine("Status: ${report?.statusDescription ?: "Optimal Trajectory"}")
                appendLine()
                appendLine("Cardiovascular: ${report?.cardiovascularTrend}")
                appendLine("Medication Adherence: ${report?.medicationAdherenceTrend}")
                appendLine("Diagnostic Labs: ${report?.diagnosticLabTrend}")
              }
              val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "CareNexus Weekly Health Brief")
                putExtra(Intent.EXTRA_TEXT, shareText)
              }
              context.startActivity(Intent.createChooser(intent, "Share Weekly Health Insights"))
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Share Brief", fontSize = 13.sp)
          }

          Button(
            onClick = onNavigateToCareAi,
            modifier = Modifier.weight(1.3f).testTag("ask_careai_insights_btn"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
          ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Discuss in CareAI", fontWeight = FontWeight.Bold, fontSize = 13.sp)
          }
        }
      }
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .background(Slate50)
        .padding(paddingValues),
      contentPadding = PaddingValues(bottom = 24.dp)
    ) {
      // 1. Week Selector Carousel
      item {
        LazyRow(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(weekOptions) { week ->
            FilterChip(
              selected = selectedWeek == week,
              onClick = { selectedWeek = week },
              label = {
                Text(
                  text = if (week == weekOptions.first()) "$week (Latest)" else week,
                  fontWeight = if (selectedWeek == week) FontWeight.Bold else FontWeight.Normal,
                  fontSize = 12.sp
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = TealPrimary,
                selectedLabelColor = Color.White
              )
            )
          }
        }
      }

      // 2. Weekly Health Score Hero Card
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          shape = RoundedCornerShape(22.dp),
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
              .padding(20.dp)
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
                      .size(10.dp)
                      .clip(CircleShape)
                      .background(TealAccent)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "GEMINI HEALTH SYNTHESIS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TealAccent,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                  )
                }

                Surface(
                  color = NavyCard,
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Text(
                    text = report?.weekPeriod ?: selectedWeek,
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(16.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column {
                  Text(
                    text = "Weekly Health Index",
                    style = MaterialTheme.typography.titleSmall,
                    color = Slate300
                  )
                  Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    Text(
                      text = "${report?.healthScore ?: 88}",
                      style = MaterialTheme.typography.displayMedium,
                      fontWeight = FontWeight.Bold,
                      color = Color.White
                    )
                    Text(
                      text = "/100",
                      style = MaterialTheme.typography.titleMedium,
                      color = Slate400,
                      modifier = Modifier.padding(bottom = 6.dp)
                    )
                  }
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = TealAccent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = report?.scoreDelta ?: "+4% from last week",
                      color = TealAccent,
                      fontWeight = FontWeight.Bold,
                      style = MaterialTheme.typography.bodySmall
                    )
                  }
                }

                // Mini Health Gauge Ring
                Box(
                  modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(NavyCard)
                    .border(4.dp, TealAccent, CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = TealAccent, modifier = Modifier.size(24.dp))
                    Text(
                      text = "OPTIMAL",
                      color = Color.White,
                      fontWeight = FontWeight.Bold,
                      fontSize = 10.sp
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(16.dp))

              // 3 Mini Pillar Metric Badges
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(NavyCard)
                  .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column {
                  Text("Vitals Stability", color = Slate400, fontSize = 10.sp)
                  Text("${report?.vitalStabilityScore ?: 94}%", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                }
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(Slate700))
                Column {
                  Text("Med Adherence", color = Slate400, fontSize = 10.sp)
                  Text("${report?.adherencePercentage ?: 96}%", color = TealAccent, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                }
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(Slate700))
                Column {
                  Text("Active Meds", color = Slate400, fontSize = 10.sp)
                  Text("${report?.activeMedicationsCount ?: 3} Daily", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                }
              }
            }
          }
        }
      }

      // 3. Multi-Source Health Aggregation Strip
      item {
        Text(
          text = "Aggregated Clinical Sources",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = Slate900,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          SourceBadgeCard(
            title = "Records",
            count = "${records.size} Visited",
            icon = Icons.Default.FolderOpen,
            tint = Slate700,
            modifier = Modifier.weight(1f)
          )
          SourceBadgeCard(
            title = "Lab Panels",
            count = "${labOrders.size} Reports",
            icon = Icons.Default.Biotech,
            tint = TealDark,
            modifier = Modifier.weight(1f)
          )
          SourceBadgeCard(
            title = "Active Rx",
            count = "${prescriptions.size} Regimens",
            icon = Icons.Default.Medication,
            tint = Color(0xFF0D9488),
            modifier = Modifier.weight(1f)
          )
          SourceBadgeCard(
            title = "Biometrics",
            count = "${vitals.size} Logs",
            icon = Icons.Default.Favorite,
            tint = Color(0xFFEF4444),
            modifier = Modifier.weight(1f)
          )
        }
      }

      // 4. Loading State or Gemini AI Synthesis
      if (isGenerating) {
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              CircularProgressIndicator(color = TealPrimary)
              Spacer(modifier = Modifier.height(14.dp))
              Text(
                text = "Gemini is analyzing your medical records, lab biomarkers, and prescription logs...",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Slate800,
                lineHeight = 20.sp
              )
              Text(
                text = "Cross-referencing lipid panels with blood pressure telemetry and dosage history.",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500
              )
            }
          }
        }
      } else if (report != null) {
        // 5. Pillar Cards
        item {
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Holistic Weekly Health Pillars",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Slate900,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
          )
        }

        item {
          HealthPillarCard(
            title = "Cardiovascular & Metabolic Trend",
            description = report.cardiovascularTrend,
            icon = Icons.Default.MonitorHeart,
            iconColor = Color(0xFFEF4444),
            statusText = "Resting BP 120/78 • HR 72 BPM"
          )
        }

        item {
          HealthPillarCard(
            title = "Prescription Adherence & Pharmacology",
            description = report.medicationAdherenceTrend,
            icon = Icons.Default.Medication,
            iconColor = Color(0xFF8B5CF6),
            statusText = "12-Day Streak • 96% Regularity"
          )
        }

        item {
          HealthPillarCard(
            title = "Diagnostic Biomarkers & Lab Panel",
            description = report.diagnosticLabTrend,
            icon = Icons.Default.Biotech,
            iconColor = Color(0xFF0284C7),
            statusText = "Lipid & Liver Enzymes In Range"
          )
        }

        // 6. CareAI Executive Report Text
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
          ) {
            Column(modifier = Modifier.padding(18.dp)) {
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
                    text = "CareAI Executive Clinical Synthesis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                  )
                  Text(
                    text = "Generated on ${report.generatedAt}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate500
                  )
                }
              }

              Spacer(modifier = Modifier.height(14.dp))

              Text(
                text = report.executiveSummary,
                style = MaterialTheme.typography.bodyMedium,
                color = Slate800,
                lineHeight = 22.sp
              )
            }
          }
        }

        // 7. Recommended Action Directives for Next Week
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SuccessGreen, TealPrimary)))
          ) {
            Column(modifier = Modifier.padding(18.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Recommended Care Directives for Next Week",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = Slate900
                )
              }

              Spacer(modifier = Modifier.height(12.dp))

              report.recommendedNextSteps.forEachIndexed { idx, step ->
                Row(
                  modifier = Modifier.padding(vertical = 4.dp),
                  verticalAlignment = Alignment.Top
                ) {
                  Surface(
                    shape = CircleShape,
                    color = TealPrimary,
                    modifier = Modifier.size(20.dp)
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Text("${idx + 1}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                  }
                  Spacer(modifier = Modifier.width(10.dp))
                  Text(
                    text = step,
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate800,
                    lineHeight = 18.sp
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun SourceBadgeCard(
  title: String,
  count: String,
  icon: ImageVector,
  tint: Color,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
  ) {
    Column(
      modifier = Modifier.padding(10.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(
        modifier = Modifier
          .size(28.dp)
          .clip(CircleShape)
          .background(tint.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(title, style = MaterialTheme.typography.labelSmall, color = Slate500, fontSize = 10.sp)
      Text(count, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Slate900, fontSize = 11.sp)
    }
  }
}

@Composable
fun HealthPillarCard(
  title: String,
  description: String,
  icon: ImageVector,
  iconColor: Color,
  statusText: String
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 4.dp),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.Top
    ) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(iconColor.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Slate900
          )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Surface(
          color = TealLight,
          shape = RoundedCornerShape(4.dp)
        ) {
          Text(
            text = statusText,
            color = TealDark,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = description,
          style = MaterialTheme.typography.bodySmall,
          color = Slate700,
          lineHeight = 18.sp
        )
      }
    }
  }
}
