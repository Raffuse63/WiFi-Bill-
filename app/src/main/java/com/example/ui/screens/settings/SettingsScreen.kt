package com.example.ui.screens.settings

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    var showWrongFileWarningDialog by remember { mutableStateOf(false) }

    // SAF Launcher for Downloading JSON Backup
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val json = viewModel.exportJsonBackup()
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(json.toByteArray(Charsets.UTF_8))
                    }
                    Toast.makeText(
                        context,
                        if (isBangla) "JSON ব্যাকআপ ফাইল সফলভাবে ডাউনলোড হয়েছে!" else "JSON backup file downloaded successfully!",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        if (isBangla) "ডাউনলোড ব্যর্থ হয়েছে!" else "Download failed!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // SAF Launcher for Restoring JSON Backup File
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                    } ?: ""

                    val success = viewModel.restoreJsonBackup(content)
                    if (success) {
                        Toast.makeText(
                            context,
                            if (isBangla) "ডাটাবেস সফলভাবে রিস্টোর হয়েছে!" else "Database restored successfully!",
                            Toast.LENGTH_LONG
                        ).show()
                        showRestoreDialog = false
                    } else {
                        showWrongFileWarningDialog = true
                    }
                } catch (e: Exception) {
                    showWrongFileWarningDialog = true
                }
            }
        }
    }

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
                            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                            val fileName = "wifi_manager_backup_$timeStamp.json"
                            createDocumentLauncher.launch(fileName)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_backup_json"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isBangla) "JSON ব্যাকআপ ডাউনলোড করুন" else "Download JSON Backup")
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
                        Text(if (isBangla) "JSON ফাইল রিস্টোর করুন" else "Restore JSON File")
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text(if (isBangla) "JSON ফাইল রিস্টোর" else "Restore JSON Backup") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(if (isBangla) "ডিভাইস থেকে ডাউনলোড করা .json ফাইল সিলেক্ট করুন অথবা নিচে ডাটা পেস্ট করুন:" else "Select a downloaded .json file from your device or paste the JSON text below:")

                    Button(
                        onClick = {
                            openDocumentLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.FileOpen, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isBangla) "JSON ফাইল সিলেক্ট করুন" else "Select JSON File")
                    }

                    Text(if (isBangla) "অথবা টেক্সট পেস্ট করুন:" else "Or paste text:", fontWeight = FontWeight.Medium)

                    OutlinedTextField(
                        value = restoreJsonInput,
                        onValueChange = { restoreJsonInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(8.dp)
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
                                showRestoreDialog = false
                            } else {
                                showRestoreDialog = false
                                showWrongFileWarningDialog = true
                            }
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

    if (showWrongFileWarningDialog) {
        AlertDialog(
            onDismissRequest = { showWrongFileWarningDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = if (isBangla) "ভুল ফাইল সতর্কবার্তা!" else "Wrong File Warning!",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isBangla)
                        "ভুল ফাইল নির্বাচন বা পেস্ট করা হয়েছে! এটি কোনো সঠিক JSON ব্যাকআপ ফাইল নয়। অনুগ্রহ করে সঠিক .json ব্যাকআপ ফাইল রিস্টোর করুন।"
                    else
                        "Invalid or wrong file selected! This is not a valid JSON backup file. Please select or paste a valid .json backup file."
                )
            },
            confirmButton = {
                Button(
                    onClick = { showWrongFileWarningDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isBangla) "ঠিক আছে" else "OK")
                }
            }
        )
    }
}

