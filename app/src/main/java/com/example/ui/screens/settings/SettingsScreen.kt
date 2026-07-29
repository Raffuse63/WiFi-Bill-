package com.example.ui.screens.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.util.LanguageUtils
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val isBangla = settings.language == "bn"

    var ownerNameInput by remember(settings.ownerName) { mutableStateOf(settings.ownerName) }
    var businessNameInput by remember(settings.businessName) { mutableStateOf(settings.businessName) }

    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreJsonInput by remember { mutableStateOf("") }

    var showBackupDialog by remember { mutableStateOf(false) }
    var backupJsonOutput by remember { mutableStateOf("") }

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = LanguageUtils.getText("settings", isBangla),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Business & Owner Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (isBangla) "ব্যবসার তথ্য" else "Business Information",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = businessNameInput,
                        onValueChange = {
                            businessNameInput = it
                            viewModel.updateBusinessName(it)
                        },
                        label = { Text(LanguageUtils.getText("business_name", isBangla)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_business_name"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = ownerNameInput,
                        onValueChange = {
                            ownerNameInput = it
                            viewModel.updateOwnerName(it)
                        },
                        label = { Text(LanguageUtils.getText("owner_name", isBangla)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_owner_name"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Currency & Language Settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (isBangla) "মুদ্রা ও ভাষা সেটিংস" else "Currency & Language",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(text = LanguageUtils.getText("language", isBangla), fontWeight = FontWeight.Medium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { viewModel.updateLanguage("en") }
                        ) {
                            RadioButton(
                                selected = settings.language == "en",
                                onClick = { viewModel.updateLanguage("en") },
                                modifier = Modifier.testTag("radio_lang_en")
                            )
                            Text("English")
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { viewModel.updateLanguage("bn") }
                        ) {
                            RadioButton(
                                selected = settings.language == "bn",
                                onClick = { viewModel.updateLanguage("bn") },
                                modifier = Modifier.testTag("radio_lang_bn")
                            )
                            Text("বাংলা")
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(text = LanguageUtils.getText("currency", isBangla), fontWeight = FontWeight.Medium)
                    val currencies = listOf("৳", "Tk", "$", "₹")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        currencies.forEach { curr ->
                            val isSel = settings.currency == curr
                            Button(
                                onClick = { viewModel.updateCurrency(curr) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("btn_curr_$curr")
                            ) {
                                Text(curr, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Backup & Restore
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = LanguageUtils.getText("backup_restore", isBangla),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                backupJsonOutput = viewModel.exportJsonBackup()
                                showBackupDialog = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_backup_json"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(LanguageUtils.getText("backup_json", isBangla))
                    }

                    Button(
                        onClick = { showRestoreDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_restore_json"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Upload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(LanguageUtils.getText("restore_json", isBangla))
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val csv = viewModel.exportCustomersCsv()
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Customer List CSV", csv)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, if (isBangla) "CSV ক্লিফবোর্ডে কপি করা হয়েছে!" else "CSV copied to clipboard!", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_export_csv"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Code, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(LanguageUtils.getText("export_csv", isBangla))
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = { Text(LanguageUtils.getText("backup_json", isBangla)) },
            text = {
                Column {
                    Text(if (isBangla) "আপনার ব্যাকআপ ডাটা প্রস্তুত:" else "Your backup data is generated:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = backupJsonOutput,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("WiFi Manager Backup JSON", backupJsonOutput)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, if (isBangla) "ব্যাকআপ ক্লিফবোর্ডে কপি করা হয়েছে!" else "Backup copied to clipboard!", Toast.LENGTH_SHORT).show()
                        showBackupDialog = false
                    }
                ) {
                    Text(if (isBangla) "কপি করুন" else "Copy Backup Data")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackupDialog = false }) {
                    Text(LanguageUtils.getText("cancel", isBangla))
                }
            }
        )
    }

    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text(LanguageUtils.getText("restore_json", isBangla)) },
            text = {
                Column {
                    Text(if (isBangla) "এখানে আপনার JSON ব্যাকআপ ডাটা পেস্ট করুন:" else "Paste your JSON backup data below:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = restoreJsonInput,
                        onValueChange = { restoreJsonInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val success = viewModel.restoreJsonBackup(restoreJsonInput)
                            if (success) {
                                Toast.makeText(context, if (isBangla) "ডাটাবেস সফলভাবে রিস্টোর হয়েছে!" else "Database restored successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, if (isBangla) "রিস্টোর ব্যর্থ হয়েছে! অনুগ্রহ করে সঠিক JSON পেস্ট করুন।" else "Restore failed! Please paste valid JSON.", Toast.LENGTH_SHORT).show()
                            }
                            showRestoreDialog = false
                        }
                    }
                ) {
                    Text(if (isBangla) "রিস্টোর করুন" else "Restore Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text(LanguageUtils.getText("cancel", isBangla))
                }
            }
        )
    }
}
