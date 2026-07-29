package com.example.ui.screens.payment

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.EmeraldGreen
import com.example.ui.util.LanguageUtils
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    viewModel: PaymentViewModel,
    preselectedCustomerId: Long = -1L,
    onNavigateToAddCustomer: () -> Unit = {},
    onNavigateToCustomerDetail: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isBangla = state.settings.language == "bn"
    val currency = state.settings.currency

    var showCollectDialog by remember { mutableStateOf(false) }
    val receiptData by viewModel.receiptDataState.collectAsStateWithLifecycle()

    LaunchedEffect(preselectedCustomerId) {
        if (preselectedCustomerId > 0) {
            viewModel.initPaymentForm(preselectedCustomerId)
            showCollectDialog = true
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddCustomer,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_customer")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = LanguageUtils.getText("add_customer", isBangla))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = LanguageUtils.getText("payments", isBangla),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // CUSTOMERS LIST VIEW with Payment button on the right side of every customer's name
            if (state.customers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            modifier = Modifier.padding(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Text(
                            text = if (isBangla) "কোনো গ্রাহক পাওয়া যায়নি" else "No customers found",
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
                    items(state.customers, key = { it.id }) { customer ->
                        CustomerPaymentCard(
                            customer = customer,
                            currency = currency,
                            isBangla = isBangla,
                            onCollectPayment = {
                                viewModel.initPaymentForm(customer.id)
                                showCollectDialog = true
                            },
                            onClickCard = { onNavigateToCustomerDetail(customer.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showCollectDialog) {
        CollectPaymentDialog(
            viewModel = viewModel,
            isBangla = isBangla,
            currency = currency,
            onDismiss = { showCollectDialog = false }
        )
    }

    if (receiptData != null) {
        ReceiptDialog(
            receipt = receiptData!!,
            isBangla = isBangla,
            onDismiss = { viewModel.clearReceipt() }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CustomerPaymentCard(
    customer: com.example.data.local.entity.Customer,
    currency: String,
    isBangla: Boolean,
    onCollectPayment: () -> Unit,
    onClickCard: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .combinedClickable(
                onClick = onClickCard,
                onLongClick = {
                    if (customer.macAddress.isNotEmpty()) {
                        clipboardManager.setText(AnnotatedString(customer.macAddress))
                        Toast.makeText(
                            context,
                            if (isBangla) "MAC address কপি করা হয়েছে: ${customer.macAddress}" else "MAC address copied: ${customer.macAddress}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
            .testTag("customer_item_${customer.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // TOP ROW: Customer Name, Phone & PAYMENT BUTTON REMAIN RIGHT SIDE OF EVERY CUSTOMER'S NAME
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = customer.fullName.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = customer.fullName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(
                            text = "MAC: ${customer.macAddress.ifEmpty { "N/A" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (customer.connectionDate.isNotEmpty()) {
                            Text(
                                text = LanguageUtils.formatConnectionDate(customer.connectionDate, isBangla),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (customer.mobileNumber.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Phone,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = LanguageUtils.formatNumber(customer.mobileNumber, isBangla),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // PAYMENT BUTTON ON THE RIGHT SIDE OF CUSTOMER'S NAME
                Button(
                    onClick = onCollectPayment,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .testTag("pay_button_${customer.id}")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollectPaymentDialog(
    viewModel: PaymentViewModel,
    isBangla: Boolean,
    currency: String,
    onDismiss: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val selectedCustId by viewModel.selectedCustomerId.collectAsStateWithLifecycle()
    val month by viewModel.billingMonthState.collectAsStateWithLifecycle()
    val amount by viewModel.amountPaidState.collectAsStateWithLifecycle()
    val date by viewModel.paymentDateState.collectAsStateWithLifecycle()
    val remarks by viewModel.remarksState.collectAsStateWithLifecycle()
    val formError by viewModel.formErrorState.collectAsStateWithLifecycle()

    val selectedCustomer = state.customers.find { it.id == selectedCustId }

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
                viewModel.paymentDateState.value = formattedDate
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
                text = LanguageUtils.getText("collect_payment", isBangla),
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
                if (formError != null) {
                    Text(
                        text = formError!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp
                    )
                }

                // Selected Customer Info
                selectedCustomer?.let { customer ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "${if (isBangla) "গ্রাহক" else "Customer"}: ${customer.fullName}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            if (customer.mobileNumber.isNotEmpty()) {
                                Text(
                                    text = "${if (isBangla) "মোবাইল" else "Mobile"}: ${LanguageUtils.formatNumber(customer.mobileNumber, isBangla)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = month,
                    onValueChange = { viewModel.billingMonthState.value = it },
                    label = { Text("${LanguageUtils.getText("billing_month", isBangla)} (YYYY-MM)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_billing_month"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { viewModel.amountPaidState.value = it },
                    label = { Text("${LanguageUtils.getText("amount_paid", isBangla)} ($currency)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_amount_paid"),
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
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_payment_date"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = remarks,
                    onValueChange = { viewModel.remarksState.value = it },
                    label = { Text(LanguageUtils.getText("remarks", isBangla)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_remarks"),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    coroutineScope.launch {
                        if (viewModel.recordPayment()) {
                            onDismiss()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                modifier = Modifier.testTag("btn_save_payment")
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
private fun ReceiptDialog(
    receipt: PaymentReceiptData,
    isBangla: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Receipt, contentDescription = null, tint = EmeraldGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = LanguageUtils.getText("receipt", isBangla),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = receipt.businessName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Customer: ${receipt.customerName}",
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(text = "Mobile: ${LanguageUtils.formatNumber(receipt.mobile, isBangla)}")
                    Text(text = "Billing Month: ${LanguageUtils.formatNumber(receipt.billingMonth, isBangla)}")
                    Text(
                        text = "Amount Paid: ${LanguageUtils.formatAmount(receipt.amountPaid, receipt.currency, isBangla)}",
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreen
                    )
                    Text(text = "Date: ${LanguageUtils.formatNumber(receipt.paymentDate, isBangla)}")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
            ) {
                Text(if (isBangla) "বন্ধ করুন" else "Close Receipt")
            }
        }
    )
}
