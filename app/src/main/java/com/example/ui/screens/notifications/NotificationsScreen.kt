package com.example.ui.screens.notifications

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.NotificationCategory
import com.example.core.model.NotificationItem
import com.example.core.repository.CareNexusRepository
import com.example.ui.components.EmptyStateCard
import com.example.ui.theme.*

@Composable
fun NotificationsScreen(
  repository: CareNexusRepository,
  onNavigateToRoute: (String) -> Unit,
  onBackClick: () -> Unit
) {
  val notifications by repository.notifications.collectAsState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(16.dp)
      .padding(bottom = 80.dp)
  ) {
    Text(
      text = "Notification Center",
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.Bold
    )
    Text(
      text = "Real-time hospital alerts and token updates",
      style = MaterialTheme.typography.bodySmall,
      color = Slate500
    )

    Spacer(modifier = Modifier.height(14.dp))

    if (notifications.isEmpty()) {
      EmptyStateCard(
        icon = Icons.Default.NotificationsNone,
        title = "No New Notifications",
        subtitle = "You are all caught up! Updates regarding appointments, lab orders, and pharmacy will appear here."
      )
    } else {
      LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(notifications, key = { it.id }) { notif ->
          NotificationCard(
            notification = notif,
            onClick = {
              repository.markNotificationAsRead(notif.id)
              notif.deepLinkRoute?.let { onNavigateToRoute(it) }
            }
          )
        }
      }
    }
  }
}

@Composable
fun NotificationCard(
  notification: NotificationItem,
  onClick: () -> Unit
) {
  val (icon, tint) = when (notification.category) {
    NotificationCategory.APPOINTMENTS -> Pair(Icons.Default.CalendarToday, TealPrimary)
    NotificationCategory.LABORATORY -> Pair(Icons.Default.Science, CyanAccent)
    NotificationCategory.PHARMACY -> Pair(Icons.Default.LocalPharmacy, SuccessGreen)
    NotificationCategory.PAYMENTS -> Pair(Icons.AutoMirrored.Filled.ReceiptLong, WarningAmber)
    NotificationCategory.EMERGENCY -> Pair(Icons.Default.Emergency, EmergencyColor)
    else -> Pair(Icons.Default.Info, Slate700)
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() },
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (notification.isRead) MaterialTheme.colorScheme.surface else TealLight.copy(alpha = 0.6f)
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
          .clip(CircleShape)
          .background(tint.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = notification.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = if (notification.isRead) FontWeight.SemiBold else FontWeight.Bold
          )
          if (!notification.isRead) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(TealPrimary))
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = notification.message,
          style = MaterialTheme.typography.bodySmall,
          color = Slate700,
          lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = notification.timestamp,
          style = MaterialTheme.typography.labelSmall,
          color = Slate400
        )
      }
    }
  }
}
