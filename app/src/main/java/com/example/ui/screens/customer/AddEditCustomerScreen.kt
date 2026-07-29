package com.example.ui.screens.customer

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.util.LanguageUtils
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCustomerScreen(
    customerId: Long,
    viewModel: CustomerViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    val name by viewModel.nameState.collectAsStateWithLifecycle()
    val mobile by viewModel.mobileState.collectAsStateWithLifecycle()
    val address by viewModel.addressState.collectAsStateWithLifecycle()
    val billAmount by viewModel.billAmountState.collectAsStateWithLifecycle()
    val macAddress by viewModel.macAddressState.collectAsStateWithLifecycle()
    val connectionDate by viewModel.connectionDateState.collectAsStateWithLifecycle()
    val isActive by viewModel.isActiveState.collectAsStateWithLifecycle()
    val notes by viewModel.notesState.collectAsStateWithLifecycle()
    val formError by viewModel.formErrorState.collectAsStateWithLifecycle()
    val settings by viewModel.listUiState.collectAsStateWithLifecycle()
    val isBangla = settings.settings.language == "bn"

    LaunchedEffect(customerId) {
        viewModel.loadCustomerForEdit(customerId)
    }

    val context = LocalContext.current

    fun showDatePicker() {
        val cal = Calendar.getInstance()
        try {
            val parts = connectionDate.split("-")
            if (parts.size == 3) {
                cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
            }
        } catch (_: Exception) {}

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val formattedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                viewModel.connectionDateState.value = formattedDate
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (customerId > 0)
                            if (isBangla) "গ্রাহক এডিট করুন" else "Edit Customer"
                        else LanguageUtils.getText("add_customer", isBangla),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (formError != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = formError!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 14.sp
                    )
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { viewModel.nameState.value = it },
                label = { Text(LanguageUtils.getText("full_name", isBangla)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_full_name"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = billAmount,
                onValueChange = { viewModel.billAmountState.value = it },
                label = { Text("${LanguageUtils.getText("bill_amount", isBangla)} (${settings.settings.currency})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_bill_amount"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = macAddress,
                onValueChange = { viewModel.macAddressState.value = it },
                label = { Text(if (isBangla) "ম্যাক এড্রেস (MAC Address)" else "MAC Address") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_mac_address"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker() }
            ) {
                OutlinedTextField(
                    value = connectionDate,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text(LanguageUtils.getText("connection_date", isBangla)) },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker() }) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Pick Connection Date"
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
                        .testTag("input_connection_date"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBangla) "গ্রাহকের অবস্থা (সক্রিয়/নিষ্ক্রিয়)" else "Customer Status (Active / Inactive)",
                    fontWeight = FontWeight.Medium
                )
                Switch(
                    checked = isActive,
                    onCheckedChange = { viewModel.isActiveState.value = it },
                    modifier = Modifier.testTag("switch_active_status")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        if (viewModel.saveCustomer(customerId)) {
                            onNavigateBack()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_save_customer"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = LanguageUtils.getText("save", isBangla),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
