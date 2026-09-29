package com.example.ui.screens.billing

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
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
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
import com.example.core.model.Bill
import com.example.core.model.BillStatus
import com.example.core.repository.CareNexusRepository
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.QRCodeDisplayBox
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun BillsScreen(
  repository: CareNexusRepository,
  onPayBillClick: (String) -> Unit
) {
  val bills by repository.bills.collectAsState()
  var selectedTab by remember { mutableIntStateOf(0) }
  val tabs = listOf("Pending Invoices", "Payment History")

  val pendingBills = remember(bills) { bills.filter { it.status == BillStatus.PENDING } }
  val paidBills = remember(bills) { bills.filter { it.status == BillStatus.PAID || it.status == BillStatus.REFUNDED } }
  val totalPendingAmount = remember(pendingBills) { pendingBills.sumOf { it.total } }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(bottom = 80.dp)
  ) {
    // Top Summary Banner
    Surface(
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 2.dp
    ) {
      Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text("Hospital Billing & Invoices", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("CareNexus Financial Desk • Secure Gateway", style = MaterialTheme.typography.labelSmall, color = Slate500)
          }

          if (totalPendingAmount > 0) {
            Surface(
              color = WarningLight,
              shape = RoundedCornerShape(12.dp)
            ) {
              Text(
                text = "Due: $${totalPendingAmount}",
                color = Slate900,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
              )
            }
          }
        }
      }
    }

    // Tabs
    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = MaterialTheme.colorScheme.surface,
      contentColor = TealPrimary
    ) {
      tabs.forEachIndexed { index, title ->
        val count = if (index == 0) pendingBills.size else paidBills.size
        Tab(
          selected = selectedTab == index,
          onClick = { selectedTab = index },
          text = { Text("$title ($count)", fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
        )
      }
    }

    val displayList = if (selectedTab == 0) pendingBills else paidBills

    if (displayList.isEmpty()) {
      EmptyStateCard(
        icon = Icons.AutoMirrored.Filled.ReceiptLong,
        title = if (selectedTab == 0) "No Outstanding Hospital Invoices" else "No Payment History Recorded",
        subtitle = if (selectedTab == 0) "All doctor consultations, laboratory panels, and pharmacy orders are fully settled." else "Official payment receipts and transaction records will appear here after checkout."
      )
    } else {
      LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        items(displayList, key = { it.id }) { bill ->
          BillCard(
            bill = bill,
            onPayClick = { onPayBillClick(bill.id) }
          )
        }
      }
    }
  }
}

@Composable
fun BillCard(
  bill: Bill,
  onPayClick: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth().testTag("bill_card_${bill.id}"),
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
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.AutoMirrored.Filled.ReceiptLong, null, tint = TealPrimary, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(bill.invoiceNumber, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        StatusBadge(status = bill.status.name)
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text("Due Date: ${bill.dueDate} • Issued: ${bill.date}", style = MaterialTheme.typography.bodySmall, color = Slate500, fontWeight = FontWeight.SemiBold)

      Spacer(modifier = Modifier.height(12.dp))

      // Itemized Services Breakdown
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(Slate50)
          .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        bill.items.forEach { item ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(item.name, style = MaterialTheme.typography.bodySmall, color = Slate700)
            Text("$${item.amount}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = Slate900)
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text("Total Amount", style = MaterialTheme.typography.labelSmall, color = Slate500)
          Text("$${bill.total}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = if (bill.status == BillStatus.PENDING) WarningAmber else SuccessGreen)
        }

        if (bill.status == BillStatus.PENDING) {
          Button(
            onClick = onPayClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
            modifier = Modifier.testTag("pay_bill_button_${bill.id}")
          ) {
            Icon(Icons.Default.Payment, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Pay Now", fontWeight = FontWeight.Bold)
          }
        } else {
          Column(horizontalAlignment = Alignment.End) {
            Surface(color = SuccessLight, shape = RoundedCornerShape(8.dp)) {
              Text(
                text = "Txn: ${bill.transactionId?.take(16) ?: "PAID"}",
                color = SuccessGreen,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
            if (bill.paidAt != null) {
              Text("Paid on ${bill.paidAt}", style = MaterialTheme.typography.labelSmall, color = Slate400, fontSize = 10.sp)
            }
          }
        }
      }
    }
  }
}

@Composable
fun PaymentScreen(
  billId: String,
  repository: CareNexusRepository,
  onPaymentSuccess: () -> Unit,
  onBackClick: () -> Unit
) {
  val bills by repository.bills.collectAsState()
  val bill = bills.find { it.id == billId } ?: bills.first()

  val paymentMethods = listOf(
    "Credit / Debit Card (Visa •••• 4242)",
    "Apple Pay / Google Wallet",
    "Health Insurance Co-Pay Direct (HMO-88492)",
    "Hospital Account Transfer"
  )
  var selectedMethod by remember { mutableStateOf(paymentMethods.first()) }
  var isProcessing by remember { mutableStateOf(false) }
  var completedTxnId by remember { mutableStateOf<String?>(null) }

  // Interactive Card Input Simulation fields
  var cardNumber by remember { mutableStateOf("4242 •••• •••• 4242") }
  var cardExpiry by remember { mutableStateOf("12/28") }
  var cardCvv by remember { mutableStateOf("884") }
  var cardHolderName by remember { mutableStateOf("Subham Pradhan") }

  if (completedTxnId != null) {
    // Official Payment Receipt View
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .background(MaterialTheme.colorScheme.background)
        .padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(
        modifier = Modifier
          .size(68.dp)
          .clip(CircleShape)
          .background(SuccessLight),
        contentAlignment = Alignment.Center
      ) {
        Icon(Icons.Default.Check, null, tint = SuccessGreen, modifier = Modifier.size(42.dp))
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "Payment Verified & Settled",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = SuccessGreen
      )

      Text(
        text = "Official digital invoice synchronized with hospital accounting.",
        style = MaterialTheme.typography.bodySmall,
        color = Slate600
      )

      Spacer(modifier = Modifier.height(20.dp))

      // Receipt Card
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
            Text("Official CareNexus Receipt", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Surface(color = SuccessLight, shape = RoundedCornerShape(8.dp)) {
              Text("SETTLED", color = SuccessGreen, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          DetailItemRow("Transaction ID", completedTxnId ?: "")
          DetailItemRow("Invoice Ref", bill.invoiceNumber)
          DetailItemRow("Payment Channel", selectedMethod.substringBefore("(").trim())
          DetailItemRow("Patient ID", bill.patientId)

          Spacer(modifier = Modifier.height(10.dp))
          HorizontalDivider(color = Slate100)
          Spacer(modifier = Modifier.height(10.dp))

          bill.items.forEach { item ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(item.name, style = MaterialTheme.typography.bodySmall, color = Slate700)
              Text("$${item.amount}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
          HorizontalDivider(color = Slate100)
          Spacer(modifier = Modifier.height(10.dp))

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total Paid", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("$${bill.total}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SuccessGreen)
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      QRCodeDisplayBox(
        qrPayload = "CARENEXUS://TXN/${completedTxnId}/BILL/${bill.invoiceNumber}",
        title = "Official Payment QR Verification",
        subtitle = "Scan at hospital desk for instant verification"
      )

      Spacer(modifier = Modifier.height(24.dp))

      Button(
        onClick = onPaymentSuccess,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
      ) {
        Text("Done & Back to Invoices", fontWeight = FontWeight.Bold)
      }
    }
  } else {
    // Payment Gateway Checkout Screen
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .background(MaterialTheme.colorScheme.background)
        .padding(16.dp)
        .padding(bottom = 80.dp)
    ) {
      Text(
        text = "CareNexus Payment Gateway",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
      )

      Text(
        text = "256-bit Encrypted Healthcare Transaction Gateway",
        style = MaterialTheme.typography.bodySmall,
        color = Slate500
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Bill Summary Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Invoice: ${bill.invoiceNumber}", fontWeight = FontWeight.Bold)
            Text("Due: ${bill.dueDate}", style = MaterialTheme.typography.bodySmall, color = Slate500)
          }

          Spacer(modifier = Modifier.height(8.dp))

          bill.items.forEach { item ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(item.name, style = MaterialTheme.typography.bodySmall, color = Slate600)
              Text("$${item.amount}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
          HorizontalDivider(color = Slate100)
          Spacer(modifier = Modifier.height(10.dp))

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Amount Due", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("$${bill.total}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TealDark)
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Payment Method Options
      Text(
        text = "Select Payment Channel",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )

      Spacer(modifier = Modifier.height(10.dp))

      paymentMethods.forEach { method ->
        val isSelected = selectedMethod == method
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { selectedMethod = method }
            .clip(RoundedCornerShape(14.dp))
            .border(
              1.5.dp,
              if (isSelected) TealPrimary else Slate200,
              RoundedCornerShape(14.dp)
            ),
          color = if (isSelected) TealLight else MaterialTheme.colorScheme.surface
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(
              selected = isSelected,
              onClick = { selectedMethod = method },
              colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(method, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, style = MaterialTheme.typography.bodyMedium)
          }
        }
      }

      if (selectedMethod.contains("Credit")) {
        Spacer(modifier = Modifier.height(16.dp))

        // Card Details Form Simulation
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Cardholder Information", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TealDark)

            OutlinedTextField(
              value = cardHolderName,
              onValueChange = { cardHolderName = it },
              label = { Text("Name on Card") },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
              value = cardNumber,
              onValueChange = { cardNumber = it },
              label = { Text("Card Number") },
              leadingIcon = { Icon(Icons.Default.CreditCard, null) },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              OutlinedTextField(
                value = cardExpiry,
                onValueChange = { cardExpiry = it },
                label = { Text("Expiry (MM/YY)") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
              )
              OutlinedTextField(
                value = cardCvv,
                onValueChange = { cardCvv = it },
                label = { Text("CVV") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Submit Button with Processing Simulation
      Button(
        onClick = {
          isProcessing = true
          val result = repository.payBill(bill.id, selectedMethod)
          result.onSuccess { txnId ->
            completedTxnId = txnId
            isProcessing = false
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
          .testTag("submit_payment_button"),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
      ) {
        if (isProcessing) {
          CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
          Spacer(modifier = Modifier.width(10.dp))
          Text("Authorizing with Bank Gateway...", color = Color.White)
        } else {
          Icon(Icons.Default.Lock, null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Authorize & Settle $${bill.total}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
fun DetailItemRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(label, color = Slate500, style = MaterialTheme.typography.bodySmall)
    Text(value, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall, color = Slate900)
  }
}
