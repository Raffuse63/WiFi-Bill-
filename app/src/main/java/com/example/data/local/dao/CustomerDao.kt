package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.Customer
import com.example.data.local.model.CustomerWithBills
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY fullName ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE isActive = 1 ORDER BY fullName ASC")
    fun getActiveCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): Customer?

    @Query("SELECT * FROM customers WHERE mobileNumber = :mobile LIMIT 1")
    suspend fun getCustomerByMobile(mobile: String): Customer?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCustomer(customer: Customer): Long

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    @Transaction
    @Query("SELECT * FROM customers WHERE id = :id")
    fun getCustomerWithBills(id: Long): Flow<CustomerWithBills?>

    @Query("SELECT COUNT(*) FROM customers")
    fun getCustomerCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM customers WHERE isActive = 1")
    fun getActiveCustomerCount(): Flow<Int>

    @Query("SELECT * FROM customers")
    suspend fun getAllCustomersList(): List<Customer>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCustomers(customers: List<Customer>)
}
