package com.example.data.backup

import com.example.data.local.AppDatabase
import com.example.data.local.entity.Customer
import com.example.data.local.entity.MonthlyBill
import com.example.data.local.entity.Payment
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class BackupData(
    val exportDate: String? = null,
    val customers: List<Customer>? = null,
    val monthlyBills: List<MonthlyBill>? = null,
    val payments: List<Payment>? = null
)

class BackupManager(private val db: AppDatabase) {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    suspend fun createJsonBackup(): String = withContext(Dispatchers.IO) {
        val customers = db.customerDao().getAllCustomersList()
        val bills = db.monthlyBillDao().getAllBillsList()
        val payments = db.paymentDao().getAllPaymentsList()

        val date = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        val backup = BackupData(date, customers, bills, payments)

        val adapter = moshi.adapter(BackupData::class.java)
        adapter.toJson(backup)
    }

    suspend fun restoreFromJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val trimmed = jsonString.trim()
            if (trimmed.isEmpty() || !trimmed.startsWith("{") || !trimmed.endsWith("}")) {
                return@withContext false
            }

            val adapter = moshi.adapter(BackupData::class.java)
            val backupData = adapter.fromJson(trimmed) ?: return@withContext false

            // Validate that this is indeed our app's backup JSON
            val hasCustomers = backupData.customers != null
            val hasBills = backupData.monthlyBills != null
            val hasPayments = backupData.payments != null
            val hasExportDate = backupData.exportDate != null

            if (!hasCustomers && !hasBills && !hasPayments && !hasExportDate) {
                return@withContext false
            }

            var restored = false
            backupData.customers?.let {
                if (it.isNotEmpty()) {
                    db.customerDao().insertCustomers(it)
                    restored = true
                }
            }
            backupData.monthlyBills?.let {
                if (it.isNotEmpty()) {
                    db.monthlyBillDao().insertBills(it)
                    restored = true
                }
            }
            backupData.payments?.let {
                if (it.isNotEmpty()) {
                    db.paymentDao().insertPayments(it)
                    restored = true
                }
            }

            restored || hasExportDate
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
