package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.Payment
import com.example.data.local.model.PaymentWithCustomer
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Transaction
    @Query("SELECT * FROM payments ORDER BY paymentDate DESC, id DESC")
    fun getAllPaymentsWithCustomer(): Flow<List<PaymentWithCustomer>>

    @Transaction
    @Query("SELECT * FROM payments WHERE billingMonth = :month ORDER BY paymentDate DESC")
    fun getPaymentsByMonthWithCustomer(month: String): Flow<List<PaymentWithCustomer>>

    @Query("SELECT * FROM payments WHERE customerId = :customerId ORDER BY paymentDate DESC")
    fun getPaymentsByCustomer(customerId: Long): Flow<List<Payment>>

    @Query("SELECT SUM(amountPaid) FROM payments WHERE billingMonth = :month")
    fun getTotalCollectionByMonth(month: String): Flow<Double?>

    @Query("SELECT SUM(amountPaid) FROM payments WHERE paymentDate = :date")
    fun getTodayCollection(date: String): Flow<Double?>

    @Query("SELECT SUM(amountPaid) FROM payments WHERE paymentDate LIKE :yearPrefix || '%'")
    fun getYearCollection(yearPrefix: String): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPayment(payment: Payment): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPayments(payments: List<Payment>)

    @Update
    suspend fun updatePayment(payment: Payment)

    @Delete
    suspend fun deletePayment(payment: Payment)

    @Query("SELECT * FROM payments")
    suspend fun getAllPaymentsList(): List<Payment>
}
