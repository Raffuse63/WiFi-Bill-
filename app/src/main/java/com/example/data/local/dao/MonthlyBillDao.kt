package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.MonthlyBill
import com.example.data.local.model.BillWithCustomer
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyBillDao {
    @Query("SELECT * FROM monthly_bills WHERE billMonth = :month")
    fun getBillsByMonth(month: String): Flow<List<MonthlyBill>>

    @Transaction
    @Query("SELECT * FROM monthly_bills WHERE billMonth = :month")
    fun getBillsWithCustomerByMonth(month: String): Flow<List<BillWithCustomer>>

    @Query("SELECT * FROM monthly_bills WHERE customerId = :customerId ORDER BY billMonth DESC")
    fun getBillsByCustomer(customerId: Long): Flow<List<MonthlyBill>>

    @Query("SELECT * FROM monthly_bills WHERE customerId = :customerId AND billMonth = :month LIMIT 1")
    suspend fun getBillByCustomerAndMonth(customerId: Long, month: String): MonthlyBill?

    @Query("SELECT * FROM monthly_bills WHERE id = :id")
    suspend fun getBillById(id: Long): MonthlyBill?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBill(bill: MonthlyBill): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBills(bills: List<MonthlyBill>)

    @Update
    suspend fun updateBill(bill: MonthlyBill)

    @Transaction
    @Query("SELECT * FROM monthly_bills WHERE status != 'PAID' ORDER BY billMonth DESC")
    fun getAllUnpaidBillsWithCustomer(): Flow<List<BillWithCustomer>>

    @Query("SELECT COUNT(*) FROM monthly_bills WHERE billMonth = :month AND status = 'PAID'")
    fun getPaidCountByMonth(month: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM monthly_bills WHERE billMonth = :month AND (status = 'UNPAID' OR status = 'PARTIAL')")
    fun getDueCountByMonth(month: String): Flow<Int>

    @Query("SELECT SUM(totalAmount - paidAmount) FROM monthly_bills WHERE status != 'PAID'")
    fun getTotalDueAmount(): Flow<Double?>

    @Query("SELECT * FROM monthly_bills")
    suspend fun getAllBillsList(): List<MonthlyBill>
}
