package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.core.model.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareNexusTopBar(
  title: String,
  showBackButton: Boolean = false,
  onBackClick: () -> Unit = {},
  onNotificationClick: () -> Unit = {},
  onEmergencyClick: () -> Unit = {},
  onAiClick: () -> Unit = {},
  unreadNotifs: Int = 0
) {
  TopAppBar(
    title = {
      Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    },
    navigationIcon = {
      if (showBackButton) {
        IconButton(
          onClick = onBackClick,
          modifier = Modifier.testTag("top_bar_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back"
          )
        }
      } else {
        Box(
          modifier = Modifier
            .padding(start = 12.dp, end = 4.dp)
            .size(36.dp)
            .clip(CircleShape)
            .background(TealPrimary),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.LocalHospital,
            contentDescription = "CareNexus Logo",
            tint = Color.White,
            modifier = Modifier.size(22.dp)
          )
        }
      }
    },
    actions = {
      // AI Assistant Quick Button
      IconButton(
        onClick = onAiClick,
        modifier = Modifier.testTag("top_bar_ai_button")
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = "CareAI",
            tint = TealAccent
          )
        }
      }

      // Emergency Quick Button
      IconButton(
        onClick = onEmergencyClick,
        modifier = Modifier.testTag("top_bar_emergency_button")
      ) {
        Box(
          modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(EmergencyColor),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Emergency,
            contentDescription = "Emergency Care",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      // Notifications Bell with Badge
      IconButton(
        onClick = onNotificationClick,
        modifier = Modifier.testTag("top_bar_notifications_button")
      ) {
        BadgedBox(
          badge = {
            if (unreadNotifs > 0) {
              Badge(
                containerColor = ErrorRed,
                contentColor = Color.White
              ) {
                Text(unreadNotifs.toString())
              }
            }
          }
        ) {
          Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "Notifications"
          )
        }
      }
    },
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.surface,
      titleContentColor = MaterialTheme.colorScheme.onSurface,
      actionIconContentColor = MaterialTheme.colorScheme.onSurface
    )
  )
}

@Composable
fun StatusBadge(
  status: String,
  modifier: Modifier = Modifier
) {
  val (bgColor, textColor) = when (status.uppercase()) {
    "CONFIRMED", "VERIFIED", "READY", "PAID", "COMPLETED" -> Pair(SuccessLight, SuccessGreen)
    "PENDING", "REQUESTED", "SAMPLE_PENDING", "PROCESSING", "PREPARING", "IN_QUEUE" -> Pair(WarningLight, WarningAmber)
    "CANCELLED", "FAILED", "NO_SHOW" -> Pair(ErrorLight, ErrorRed)
    "IN_CONSULTATION", "CHECKED_IN", "ACTIVE" -> Pair(TealLight, TealPrimary)
    else -> Pair(Slate100, Slate700)
  }

  Surface(
    color = bgColor,
    shape = RoundedCornerShape(16.dp),
    modifier = modifier
  ) {
    Text(
      text = status.replace("_", " "),
      color = textColor,
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
    )
  }
}

@Composable
fun EmptyStateCard(
  icon: ImageVector,
  title: String,
  subtitle: String,
  actionText: String? = null,
  onActionClick: () -> Unit = {}
) {
  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(28.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier
          .size(64.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(32.dp)
        )
      }
      Spacer(modifier = Modifier.height(16.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
      )
      if (actionText != null) {
        Spacer(modifier = Modifier.height(16.dp))
        Button(
          onClick = onActionClick,
          shape = RoundedCornerShape(12.dp)
        ) {
          Text(actionText)
        }
      }
    }
  }
}

@Composable
fun ErrorStateCard(
  errorMessage: String,
  onRetry: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = ErrorLight),
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(
        imageVector = Icons.Default.ErrorOutline,
        contentDescription = null,
        tint = ErrorRed,
        modifier = Modifier.size(36.dp)
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Something went wrong",
        style = MaterialTheme.typography.titleMedium,
        color = ErrorRed,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = errorMessage,
        style = MaterialTheme.typography.bodyMedium,
        color = Slate800,
        textAlign = TextAlign.Center
      )
      Spacer(modifier = Modifier.height(12.dp))
      OutlinedButton(
        onClick = onRetry,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
        border = ButtonDefaults.outlinedButtonBorder(enabled = true)
      ) {
        Text("Retry")
      }
    }
  }
}

@Composable
fun QRCodeDisplayBox(
  qrPayload: String,
  title: String,
  subtitle: String
) {
  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MedicalWhite),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = Slate900
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = Slate500
      )
      Spacer(modifier = Modifier.height(16.dp))
      // Stylized simulated QR Code Canvas
      Box(
        modifier = Modifier
          .size(180.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(Slate50)
          .border(2.dp, Slate200, RoundedCornerShape(12.dp))
          .padding(12.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Box(modifier = Modifier.size(36.dp).background(Slate900, RoundedCornerShape(4.dp)))
            Box(modifier = Modifier.size(36.dp).background(Slate900, RoundedCornerShape(4.dp)))
          }
          Box(
            modifier = Modifier
              .align(Alignment.CenterHorizontally)
              .size(28.dp)
              .clip(CircleShape)
              .background(TealPrimary),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.LocalHospital, null, tint = Color.White, modifier = Modifier.size(16.dp))
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Box(modifier = Modifier.size(36.dp).background(Slate900, RoundedCornerShape(4.dp)))
            Box(modifier = Modifier.size(24.dp).background(Slate700, RoundedCornerShape(4.dp)))
          }
        }
      }
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = qrPayload,
        style = MaterialTheme.typography.labelSmall,
        color = Slate400,
        textAlign = TextAlign.Center
      )
    }
  }
}
