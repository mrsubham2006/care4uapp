package com.example.ui.screens.laboratory

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.LabOrder
import com.example.core.model.LabOrderStatus
import com.example.core.repository.CareNexusRepository
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun LaboratoryScreen(
  repository: CareNexusRepository,
  onLabOrderClick: (String) -> Unit
) {
  val labOrders by repository.labOrders.collectAsState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(bottom = 80.dp)
  ) {
    if (labOrders.isEmpty()) {
      EmptyStateCard(
        icon = Icons.Default.Biotech,
        title = "No Lab Reports Found",
        subtitle = "When your doctor orders a test, track the sample processing and results here."
      )
    } else {
      LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        item {
          Text(
            text = "Diagnostic Laboratory Reports",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
          )
        }

        items(labOrders, key = { it.id }) { order ->
          LabOrderItemCard(
            order = order,
            onClick = { onLabOrderClick(order.id) }
          )
        }
      }
    }
  }
}

@Composable
fun LabOrderItemCard(
  order: LabOrder,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("lab_order_${order.id}"),
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
        Surface(
          color = TealLight,
          shape = RoundedCornerShape(10.dp)
        ) {
          Text(
            text = order.id,
            color = TealDark,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
        StatusBadge(status = order.status.name)
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = order.testName,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )

      Text(
        text = "Prescribed by ${order.doctorName} • ${order.department}",
        style = MaterialTheme.typography.bodySmall,
        color = Slate600
      )

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(Slate50)
          .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Sample: ${order.sampleType}", style = MaterialTheme.typography.bodySmall, color = Slate700)
        Text(order.date, style = MaterialTheme.typography.bodySmall, color = Slate500, fontWeight = FontWeight.SemiBold)
      }

      if (order.status == LabOrderStatus.VERIFIED) {
        Spacer(modifier = Modifier.height(12.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AutoAwesome, null, tint = TealPrimary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (order.aiExplanation != null) "AI Explainer Available" else "AI Analysis Ready",
              style = MaterialTheme.typography.labelSmall,
              color = TealPrimary,
              fontWeight = FontWeight.Bold
            )
          }

          TextButton(onClick = onClick) {
            Text("View Report", fontWeight = FontWeight.Bold, color = TealPrimary)
          }
        }
      }
    }
  }
}

@Composable
fun LabReportDetailScreen(
  orderId: String,
  repository: CareNexusRepository,
  onBackClick: () -> Unit
) {
  val labOrders by repository.labOrders.collectAsState()
  val order = labOrders.find { it.id == orderId } ?: labOrders.first()
  var isGeneratingAi by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .background(MaterialTheme.colorScheme.background)
      .padding(16.dp)
      .padding(bottom = 80.dp)
  ) {
    // Report Header
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
          Text("Official Laboratory Result", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          StatusBadge(status = order.status.name)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(order.testName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Slate900)
        Text("Referring Doctor: ${order.doctorName} (${order.department})", style = MaterialTheme.typography.bodySmall, color = Slate600)
        Text("Sample Collected: ${order.sampleCollectedAt ?: order.date}", style = MaterialTheme.typography.bodySmall, color = Slate500)
        Text("Verified By: Hospital Central Pathology Lab", style = MaterialTheme.typography.bodySmall, color = Slate500)
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Biomarkers Parameter Table
    Text(
      text = "Reported Parameters",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        order.results.forEachIndexed { index, param ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1.2f)) {
              Text(
                text = param.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Slate900
              )
              Text(
                text = "Ref: ${param.referenceRange}",
                style = MaterialTheme.typography.labelSmall,
                color = Slate500
              )
            }

            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(0.8f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "${param.value} ${param.unit}",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  color = if (param.isAbnormal) ErrorRed else Slate900
                )
                if (param.isAbnormal) {
                  Spacer(modifier = Modifier.width(4.dp))
                  Surface(color = ErrorLight, shape = CircleShape) {
                    Text("▲", color = ErrorRed, fontSize = 10.sp, modifier = Modifier.padding(2.dp))
                  }
                }
              }
            }
          }
          if (index < order.results.lastIndex) {
            HorizontalDivider(color = Slate100)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // AI LAB REPORT EXPLAINER SECTION
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = androidx.compose.foundation.BorderStroke(1.5.dp, TealAccent.copy(alpha = 0.5f))
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(TealLight),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.AutoAwesome, null, tint = TealPrimary, modifier = Modifier.size(20.dp))
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "CareAI Lab Explainer",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = TealDark
            )
            Text(
              text = "Educational breakdown powered by Gemini AI",
              style = MaterialTheme.typography.labelSmall,
              color = Slate500
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (order.aiExplanation != null) {
          Text(
            text = order.aiExplanation ?: "",
            style = MaterialTheme.typography.bodyMedium,
            color = Slate800,
            lineHeight = 22.sp
          )
        } else {
          Text(
            text = "Get an easy-to-understand educational summary of your lab results and prepared questions for your next doctor consultation.",
            style = MaterialTheme.typography.bodyMedium,
            color = Slate600
          )
          Spacer(modifier = Modifier.height(14.dp))
          Button(
            onClick = {
              isGeneratingAi = true
              repository.requestLabAiExplanation(order.id)
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("explain_lab_report_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
          ) {
            Icon(Icons.Default.AutoAwesome, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Explain This Report", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
