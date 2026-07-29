package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.CustomerDao
import com.example.data.local.dao.MonthlyBillDao
import com.example.data.local.dao.PaymentDao
import com.example.data.local.entity.Customer
import com.example.data.local.entity.MonthlyBill
import com.example.data.local.entity.Payment

@Database(
    entities = [
        Customer::class,
        MonthlyBill::class,
        Payment::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun monthlyBillDao(): MonthlyBillDao
    abstract fun paymentDao(): PaymentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wifi_bill_manager_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
