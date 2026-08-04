package com.example.ui.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.AlertRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.util.LanguageUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    customerId: Long,
    viewModel: CustomerViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToCollectPayment: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val customerFlow = remember(customerId) { viewModel.getCustomerWithBills(customerId) }
    val customerWithBillsState by customerFlow.collectAsStateWithLifecycle()
    val settingsState by viewModel.listUiState.collectAsStateWithLifecycle()
    val isBangla = settingsState.settings.language == "bn"

    var showDeleteDialog by remember { mutableStateOf(false) }

    val data = customerWithBillsState
    val customer = data?.customer

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (customer != null) {
                        IconButton(
                            onClick = { onNavigateToEdit(customer.id) },
                            modifier = Modifier.testTag("action_edit_customer")
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.testTag("action_delete_customer")
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = AlertRed)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (customer == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            val bnMonths = listOf(
                "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
                "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
            )
            val enMonths = listOf(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
            )

            val connectionFormattedText = remember(customer.connectionDate, isBangla) {
                try {
                    val parts = customer.connectionDate.trim().split("-", "/", ".")
                    if (parts.size >= 3) {
                        val (d, m, y) = if (parts[0].length == 4) {
                            Triple(parts[2].toInt(), parts[1].toInt(), parts[0].toInt())
                        } else {
                            Triple(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
                        }
                        val monthName = if (isBangla) bnMonths[m - 1] else enMonths[m - 1]
                        val dayStr = LanguageUtils.formatNumber(d, isBangla)
                        val yrStr = LanguageUtils.formatNumber(y % 100, isBangla)
                        "${if (isBangla) "সংযোগ" else "Connection"}: $dayStr $monthName $yrStr"
                    } else {
                        "${if (isBangla) "সংযোগ" else "Connection"}: ${customer.connectionDate}"
                    }
                } catch (_: Exception) {
                    "${if (isBangla) "সংযোগ" else "Connection"}: ${customer.connectionDate}"
                }
            }

            data class BillingCycleInfo(
                val year: Int,
                val monthNum: Int,
                val monthPadded: String,
                val displayLabel: String
            )

            val groupedCycles = remember(customer.connectionDate, isBangla) {
                val cal = Calendar.getInstance()
                val curY = cal.get(Calendar.YEAR)
                val curM = cal.get(Calendar.MONTH) + 1 // 1..12
                val curD = cal.get(Calendar.DAY_OF_MONTH)

                var cY = curY
                var cM = curM
                var cD = 1

                try {
                    val parts = customer.connectionDate.trim().split("-", "/", ".")
                    if (parts.size >= 3) {
                        if (parts[0].length == 4) {
                            cY = parts[0].toInt()
                            cM = parts[1].toInt()
                            cD = parts[2].toInt()
                        } else if (parts[2].length == 4) {
                            cD = parts[0].toInt()
                            cM = parts[1].toInt()
                            cY = parts[2].toInt()
                        }
                    }
                } catch (_: Exception) {}

                val maxDaysInCurMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                val effectiveCD = minOf(cD, maxDaysInCurMonth)

                val targetCal = Calendar.getInstance()
                if (curD < effectiveCD) {
                    targetCal.add(Calendar.MONTH, -1)
                }
                val targetY = targetCal.get(Calendar.YEAR)
                val targetM = targetCal.get(Calendar.MONTH) + 1

                val list = mutableListOf<BillingCycleInfo>()
                var y = cY
                var m = cM

                while (y < targetY || (y == targetY && m <= targetM) || (list.isEmpty() && y == cY && m == cM)) {
                    val monthIdx = m - 1
                    val startDayStr = LanguageUtils.formatNumber(cD, isBangla)
                    val startMonthName = if (isBangla) bnMonths[monthIdx] else enMonths[monthIdx]

                    val endMonthIdx = if (cD == 1) monthIdx else (monthIdx + 1) % 12
                    val endDay = if (cD == 1) {
                        val tempCal = Calendar.getInstance()
                        tempCal.set(y, monthIdx, 1)
                        tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                    } else {
                        cD - 1
                    }
                    val endDayStr = LanguageUtils.formatNumber(endDay, isBangla)
                    val endMonthName = if (isBangla) bnMonths[endMonthIdx] else enMonths[endMonthIdx]

                    val cycleLabel = "$startDayStr $startMonthName - $endDayStr $endMonthName"
                    val monthPadded = String.format("%04d-%02d", y, m)

                    list.add(BillingCycleInfo(y, m, monthPadded, cycleLabel))

                    if (y == targetY && m == targetM) break

                    m++
                    if (m > 12) {
                        m = 1
                        y++
                    }
                }
                list.groupBy { it.year }
            }

            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Customer Header Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = customer.fullName.take(1).uppercase(),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = customer.fullName,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "MAC: ${customer.macAddress.ifEmpty { "N/A" }}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = connectionFormattedText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${if (isBangla) "মাসিক বিল" else "Monthly Bill"}: ${LanguageUtils.formatAmount(customer.monthlyBillAmount, settingsState.settings.currency, isBangla)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Button(
                                onClick = { onNavigateToCollectPayment(customer.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                shape = CircleShape,
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = LanguageUtils.getText("collect_payment", isBangla),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Payment History List Grouped by Year
                groupedCycles.forEach { (year, cycles) ->
                    val yearStrFormatted = LanguageUtils.formatNumber(year, isBangla)

                    item(key = "year_header_$year") {
                        Text(
                            text = yearStrFormatted,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                        )
                    }

                    items(cycles, key = { "cycle_${it.year}_${it.monthNum}" }) { cycle ->
                        val monthUnpadded = "${cycle.year}-${cycle.monthNum}"

                        val bill = data.bills.find { it.billMonth == cycle.monthPadded || it.billMonth == monthUnpadded }
                        val paymentsForMonth = data.payments.filter {
                            it.billingMonth == cycle.monthPadded || it.billingMonth == monthUnpadded
                        }

                        val paidAmount = if (bill != null && bill.paidAmount > 0) {
                            maxOf(bill.paidAmount, paymentsForMonth.sumOf { it.amountPaid })
                        } else {
                            paymentsForMonth.sumOf { it.amountPaid }
                        }

                        val expectedBillAmount = if (bill != null && bill.totalAmount > 0) bill.totalAmount else customer.monthlyBillAmount
                        val isFullyPaid = (bill?.status == "PAID") || (expectedBillAmount > 0 && paidAmount >= expectedBillAmount) || (paidAmount > 0 && expectedBillAmount == 0.0)
                        val isPartial = !isFullyPaid && paidAmount > 0

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = cycle.displayLabel,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                when {
                                    isFullyPaid -> {
                                        Text(
                                            text = "done ✅",
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldGreen,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    isPartial -> {
                                        Text(
                                            text = LanguageUtils.formatAmount(paidAmount, settingsState.settings.currency, isBangla),
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFF8C00),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    else -> {
                                        Text(
                                            text = "not yet",
                                            fontWeight = FontWeight.Normal,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }

    if (showDeleteDialog && customer != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(LanguageUtils.getText("delete", isBangla)) },
            text = { Text(if (isBangla) "আপনি কি নিশ্চিত যে এই গ্রাহককে মুছে ফেলতে চান?" else "Are you sure you want to delete this customer?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCustomer(customer)
                        showDeleteDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text(LanguageUtils.getText("delete", isBangla), color = AlertRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(LanguageUtils.getText("cancel", isBangla))
                }
            }
        )
    }
}

