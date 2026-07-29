package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String,
    val mobileNumber: String,
    val address: String,
    val monthlyBillAmount: Double,
    val connectionDate: String, // Format: YYYY-MM-DD
    val packageName: String = "",
    val macAddress: String = "",
    val isActive: Boolean = true,
    val notes: String = ""
)
