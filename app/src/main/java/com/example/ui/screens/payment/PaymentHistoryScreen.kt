package com.example.ui.screens.payment

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.model.PaymentWithCustomer
import com.example.data.preferences.UserSettings
import com.example.ui.theme.EmeraldGreen
import com.example.ui.util.LanguageUtils
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentHistoryScreen(
    viewModel: PaymentViewModel,
    userSettings: UserSettings
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val editingPayment by viewModel.editingPaymentState.collectAsStateWithLifecycle()
    val deletingPayment by viewModel.deletingPaymentState.collectAsStateWithLifecycle()

    val isBangla = userSettings.language == "bn"
    val currency = userSettings.currency

    var expanded by remember { mutableStateOf(false) }
    val selectedCustomerId by viewModel.selectedCustomerFilterId.collectAsStateWithLifecycle()

    val selectedCustomerName = if (selectedCustomerId <= 0) {
        if (isBangla) "সবাই (All)" else "All Customers"
    } else {
        state.customers.find { it.id == selectedCustomerId }?.fullName ?: (if (isBangla) "সবাই (All)" else "All Customers")
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = LanguageUtils.getText("payment_history", isBangla),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Dropdown Filter for Customers
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedCustomerName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(if (isBangla) "গ্রাহক নির্বাচন করুন" else "Select Customer") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .testTag("dropdown_customer_filter")
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = if (isBangla) "সবাই (All)" else "All Customers",
                                fontWeight = if (selectedCustomerId <= 0) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            viewModel.onCustomerFilterChange(-1L)
                            expanded = false
                        },
                        modifier = Modifier.testTag("dropdown_item_all")
                    )
                    state.customers.forEach { customer ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = customer.fullName,
                                    fontWeight = if (selectedCustomerId == customer.id) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                viewModel.onCustomerFilterChange(customer.id)
                                expanded = false
                            },
                            modifier = Modifier.testTag("dropdown_item_${customer.id}")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state.payments.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            modifier = Modifier.padding(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Text(
                            text = if (isBangla) "কোনো পেমেন্ট রেকর্ড নেই" else "No payment records found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.payments, key = { it.payment.id }) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("payment_item_${item.payment.id}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.customer.fullName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${LanguageUtils.getText("billing_month", isBangla)}: ${LanguageUtils.formatNumber(item.payment.billingMonth, isBangla)} • ${LanguageUtils.formatNumber(item.payment.paymentDate, isBangla)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (item.payment.remarks.isNotEmpty()) {
                                            Text(
                                                text = item.payment.remarks,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = LanguageUtils.formatAmount(item.payment.amountPaid, currency, isBangla),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldGreen
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(
                                            onClick = { viewModel.startEditPayment(item) },
                                            modifier = Modifier.size(32.dp).testTag("edit_payment_${item.payment.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.startDeletePayment(item) },
                                            modifier = Modifier.size(32.dp).testTag("delete_payment_${item.payment.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (editingPayment != null) {
        EditPaymentDialog(
            viewModel = viewModel,
            isBangla = isBangla,
            currency = currency,
            onDismiss = { viewModel.dismissEditPayment() }
        )
    }

    if (deletingPayment != null) {
        ConfirmDeletePaymentDialog(
            item = deletingPayment!!,
            isBangla = isBangla,
            currency = currency,
            onConfirm = { viewModel.confirmDeletePayment() },
            onDismiss = { viewModel.dismissDeletePayment() }
        )
    }
}

@Composable
private fun EditPaymentDialog(
    viewModel: PaymentViewModel,
    isBangla: Boolean,
    currency: String,
    onDismiss: () -> Unit
) {
    val editingItem by viewModel.editingPaymentState.collectAsStateWithLifecycle()
    val amount by viewModel.editAmountState.collectAsStateWithLifecycle()
    val month by viewModel.editMonthState.collectAsStateWithLifecycle()
    val date by viewModel.editDateState.collectAsStateWithLifecycle()
    val remarks by viewModel.editRemarksState.collectAsStateWithLifecycle()
    val editError by viewModel.editErrorState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    if (editingItem == null) return

    fun showDatePicker() {
        val cal = Calendar.getInstance()
        try {
            val parts = date.split("-")
            if (parts.size == 3) {
                cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
            }
        } catch (_: Exception) {}

        DatePickerDialog(
            context,
            { _, year, monthOfYear, dayOfMonth ->
                val formattedDate = String.format(Locale.US, "%04d-%02d-%02d", year, monthOfYear + 1, dayOfMonth)
                viewModel.editDateState.value = formattedDate
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isBangla) "পেমেন্ট এডিট করুন" else "Edit Payment",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (editError != null) {
                    Text(
                        text = editError!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp
                    )
                }

                Text(
                    text = "${if (isBangla) "গ্রাহক" else "Customer"}: ${editingItem!!.customer.fullName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = month,
                    onValueChange = { viewModel.editMonthState.value = it },
                    label = { Text("${LanguageUtils.getText("billing_month", isBangla)} (YYYY-MM)") },
                    modifier = Modifier.fillMaxWidth().testTag("input_edit_billing_month"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { viewModel.editAmountState.value = it },
                    label = { Text("${LanguageUtils.getText("amount_paid", isBangla)} ($currency)") },
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("input_edit_amount_paid"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker() }
                ) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text(LanguageUtils.getText("payment_date", isBangla)) },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker() }) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Pick Payment Date"
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("input_edit_payment_date"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = remarks,
                    onValueChange = { viewModel.editRemarksState.value = it },
                    label = { Text(LanguageUtils.getText("remarks", isBangla)) },
                    modifier = Modifier.fillMaxWidth().testTag("input_edit_remarks"),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    coroutineScope.launch {
                        if (viewModel.saveEditPayment()) {
                            onDismiss()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                modifier = Modifier.testTag("btn_save_edit_payment")
            ) {
                Text(LanguageUtils.getText("save", isBangla))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(LanguageUtils.getText("cancel", isBangla))
            }
        }
    )
}

@Composable
private fun ConfirmDeletePaymentDialog(
    item: PaymentWithCustomer,
    isBangla: Boolean,
    currency: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isBangla) "পেমেন্ট ডিলিট নিশ্চিত করুন" else "Confirm Delete Payment",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = if (isBangla)
                    "আপনি কি নিশ্চিত যে ${item.customer.fullName}-এর ${LanguageUtils.formatAmount(item.payment.amountPaid, currency, true)} টাকার পেমেন্টটি ডিলিট করতে চান?"
                else
                    "Are you sure you want to delete this payment of ${LanguageUtils.formatAmount(item.payment.amountPaid, currency, false)} for ${item.customer.fullName}?"
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("btn_confirm_delete_payment")
            ) {
                Text(if (isBangla) "ডিলিট করুন" else "Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(LanguageUtils.getText("cancel", isBangla))
            }
        }
    )
}
