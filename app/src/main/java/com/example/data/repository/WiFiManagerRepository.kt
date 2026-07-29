package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.Customer
import com.example.data.local.entity.MonthlyBill
import com.example.data.local.entity.Payment
import com.example.data.local.model.BillWithCustomer
import com.example.data.local.model.CustomerWithBills
import com.example.data.local.model.PaymentWithCustomer
import com.example.data.preferences.SettingsDataStore
import com.example.data.preferences.UserSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WiFiManagerRepository(
    private val db: AppDatabase,
    private val settingsDataStore: SettingsDataStore
) {
    val customerDao = db.customerDao()
    val monthlyBillDao = db.monthlyBillDao()
    val paymentDao = db.paymentDao()

    val userSettings: Flow<UserSettings> = settingsDataStore.userSettings

    // Customers
    val allCustomers: Flow<List<Customer>> = customerDao.getAllCustomers()
    val activeCustomers: Flow<List<Customer>> = customerDao.getActiveCustomers()
    val totalCustomerCount: Flow<Int> = customerDao.getCustomerCount()

    suspend fun getCustomerById(id: Long): Customer? = customerDao.getCustomerById(id)

    suspend fun getCustomerByMobile(mobile: String): Customer? = customerDao.getCustomerByMobile(mobile)

    fun getCustomerWithBills(id: Long): Flow<CustomerWithBills?> = customerDao.getCustomerWithBills(id)

    suspend fun addCustomer(customer: Customer): Long = withContext(Dispatchers.IO) {
        val id = customerDao.insertCustomer(customer)
        if (id > 0 && customer.isActive) {
            val currentMonth = getCurrentMonthString()
            monthlyBillDao.insertBill(
                MonthlyBill(
                    customerId = id,
                    billMonth = currentMonth,
                    totalAmount = customer.monthlyBillAmount,
                    paidAmount = 0.0,
                    status = "UNPAID"
                )
            )
        }
        id
    }

    suspend fun updateCustomer(customer: Customer) {
        customerDao.updateCustomer(customer)
    }

    suspend fun deleteCustomer(customer: Customer) {
        customerDao.deleteCustomer(customer)
    }

    // Bills
    fun getBillsByMonth(month: String): Flow<List<MonthlyBill>> = monthlyBillDao.getBillsByMonth(month)

    fun getBillsWithCustomerByMonth(month: String): Flow<List<BillWithCustomer>> =
        monthlyBillDao.getBillsWithCustomerByMonth(month)

    fun getUnpaidBillsWithCustomer(): Flow<List<BillWithCustomer>> =
        monthlyBillDao.getAllUnpaidBillsWithCustomer()

    fun getPaidCountByMonth(month: String): Flow<Int> = monthlyBillDao.getPaidCountByMonth(month)

    fun getDueCountByMonth(month: String): Flow<Int> = monthlyBillDao.getDueCountByMonth(month)

    val totalDueAmount: Flow<Double?> = monthlyBillDao.getTotalDueAmount()

    /**
     * Automatic Bill Generation for Current Month
     */
    suspend fun autoGenerateMonthlyBills(month: String = getCurrentMonthString()) = withContext(Dispatchers.IO) {
        val activeCustomersList = customerDao.getAllCustomersList().filter { it.isActive }
        for (customer in activeCustomersList) {
            val existingBill = monthlyBillDao.getBillByCustomerAndMonth(customer.id, month)
            if (existingBill == null) {
                monthlyBillDao.insertBill(
                    MonthlyBill(
                        customerId = customer.id,
                        billMonth = month,
                        totalAmount = customer.monthlyBillAmount,
                        paidAmount = 0.0,
                        status = "UNPAID"
                    )
                )
            }
        }
    }

    // Payments
    val allPaymentsWithCustomer: Flow<List<PaymentWithCustomer>> = paymentDao.getAllPaymentsWithCustomer()

    fun getPaymentsByMonthWithCustomer(month: String): Flow<List<PaymentWithCustomer>> =
        paymentDao.getPaymentsByMonthWithCustomer(month)

    fun getTotalCollectionByMonth(month: String): Flow<Double?> = paymentDao.getTotalCollectionByMonth(month)

    fun getTodayCollection(date: String = getTodayDateString()): Flow<Double?> =
        paymentDao.getTodayCollection(date)

    fun getYearCollection(yearPrefix: String = getCurrentYearString()): Flow<Double?> =
        paymentDao.getYearCollection(yearPrefix)

    /**
     * Record a Payment and automatically update customer's monthly bill balance
     */
    suspend fun recordPayment(
        customerId: Long,
        billingMonth: String,
        amountPaid: Double,
        paymentDate: String,
        paymentMethod: String = "",
        remarks: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val customer = customerDao.getCustomerById(customerId) ?: return@withContext -1L

        // Find existing bill for this month or create if missing
        var bill = monthlyBillDao.getBillByCustomerAndMonth(customerId, billingMonth)
        if (bill == null) {
            val totalAmt = customer.monthlyBillAmount
            val billId = monthlyBillDao.insertBill(
                MonthlyBill(
                    customerId = customerId,
                    billMonth = billingMonth,
                    totalAmount = totalAmt,
                    paidAmount = 0.0,
                    status = "UNPAID"
                )
            )
            if (billId > 0) {
                bill = monthlyBillDao.getBillById(billId)
            } else {
                bill = monthlyBillDao.getBillByCustomerAndMonth(customerId, billingMonth)
            }
        }

        val newPaymentId = paymentDao.insertPayment(
            Payment(
                customerId = customerId,
                billId = bill?.id ?: 0,
                billingMonth = billingMonth,
                amountPaid = amountPaid,
                paymentDate = paymentDate,
                paymentMethod = "",
                remarks = remarks
            )
        )

        // Update bill paidAmount and status
        if (bill != null) {
            val updatedPaid = bill.paidAmount + amountPaid
            val newStatus = when {
                updatedPaid >= bill.totalAmount && bill.totalAmount > 0 -> "PAID"
                updatedPaid > 0 -> "PARTIAL"
                else -> "UNPAID"
            }
            monthlyBillDao.updateBill(
                bill.copy(
                    paidAmount = updatedPaid,
                    status = newStatus
                )
            )
        }

        newPaymentId
    }

    suspend fun deletePayment(payment: Payment) = withContext(Dispatchers.IO) {
        paymentDao.deletePayment(payment)
        val bill = if (payment.billId > 0) {
            monthlyBillDao.getBillById(payment.billId)
        } else {
            monthlyBillDao.getBillByCustomerAndMonth(payment.customerId, payment.billingMonth)
        }
        if (bill != null) {
            val updatedPaid = (bill.paidAmount - payment.amountPaid).coerceAtLeast(0.0)
            val newStatus = when {
                updatedPaid >= bill.totalAmount && bill.totalAmount > 0 -> "PAID"
                updatedPaid > 0 -> "PARTIAL"
                else -> "UNPAID"
            }
            monthlyBillDao.updateBill(bill.copy(paidAmount = updatedPaid, status = newStatus))
        }
    }

    suspend fun updatePayment(oldPayment: Payment, newAmount: Double, newMonth: String, newDate: String, newRemarks: String) = withContext(Dispatchers.IO) {
        val updatedPayment = oldPayment.copy(
            billingMonth = newMonth,
            amountPaid = newAmount,
            paymentDate = newDate,
            remarks = newRemarks
        )
        paymentDao.updatePayment(updatedPayment)

        val oldBill = if (oldPayment.billId > 0) {
            monthlyBillDao.getBillById(oldPayment.billId)
        } else {
            monthlyBillDao.getBillByCustomerAndMonth(oldPayment.customerId, oldPayment.billingMonth)
        }

        val newBill = if (newMonth == oldPayment.billingMonth) {
            oldBill
        } else {
            monthlyBillDao.getBillByCustomerAndMonth(oldPayment.customerId, newMonth)
        }

        if (oldBill != null && oldBill.id == newBill?.id) {
            val diff = newAmount - oldPayment.amountPaid
            val updatedPaid = (oldBill.paidAmount + diff).coerceAtLeast(0.0)
            val newStatus = when {
                updatedPaid >= oldBill.totalAmount && oldBill.totalAmount > 0 -> "PAID"
                updatedPaid > 0 -> "PARTIAL"
                else -> "UNPAID"
            }
            monthlyBillDao.updateBill(oldBill.copy(paidAmount = updatedPaid, status = newStatus))
        } else {
            if (oldBill != null) {
                val oldPaid = (oldBill.paidAmount - oldPayment.amountPaid).coerceAtLeast(0.0)
                val oldStatus = when {
                    oldPaid >= oldBill.totalAmount && oldBill.totalAmount > 0 -> "PAID"
                    oldPaid > 0 -> "PARTIAL"
                    else -> "UNPAID"
                }
                monthlyBillDao.updateBill(oldBill.copy(paidAmount = oldPaid, status = oldStatus))
            }
            if (newBill != null) {
                val newPaid = newBill.paidAmount + newAmount
                val newStatus = when {
                    newPaid >= newBill.totalAmount && newBill.totalAmount > 0 -> "PAID"
                    newPaid > 0 -> "PARTIAL"
                    else -> "UNPAID"
                }
                monthlyBillDao.updateBill(newBill.copy(paidAmount = newPaid, status = newStatus))
            }
        }
    }

    // Date Utilities
    fun getCurrentMonthString(): String {
        return SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
    }

    fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun getCurrentYearString(): String {
        return SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())
    }
}
