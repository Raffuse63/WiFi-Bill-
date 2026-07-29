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
    val exportDate: String,
    val customers: List<Customer>,
    val monthlyBills: List<MonthlyBill>,
    val payments: List<Payment>
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
            val adapter = moshi.adapter(BackupData::class.java)
            val backupData = adapter.fromJson(jsonString) ?: return@withContext false

            if (backupData.customers.isNotEmpty()) {
                db.customerDao().insertCustomers(backupData.customers)
            }
            if (backupData.monthlyBills.isNotEmpty()) {
                db.monthlyBillDao().insertBills(backupData.monthlyBills)
            }
            if (backupData.payments.isNotEmpty()) {
                db.paymentDao().insertPayments(backupData.payments)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun exportCustomersCsv(): String = withContext(Dispatchers.IO) {
        val customers = db.customerDao().getAllCustomersList()
        val sb = StringBuilder()
        sb.append("ID,Full Name,Mobile Number,Address,MAC Address,Monthly Bill,Connection Date,Status,Notes\n")
        for (c in customers) {
            val status = if (c.isActive) "Active" else "Inactive"
            sb.append("${c.id},\"${c.fullName}\",\"${c.mobileNumber}\",\"${c.address}\",\"${c.macAddress}\",${c.monthlyBillAmount},\"${c.connectionDate}\",\"$status\",\"${c.notes}\"\n")
        }
        sb.toString()
    }
}
