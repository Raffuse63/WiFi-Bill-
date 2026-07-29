package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["customerId"]), Index(value = ["billId"])]
)
data class Payment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val billId: Long = 0, // Reference to MonthlyBill ID if linked
    val billingMonth: String, // YYYY-MM
    val amountPaid: Double,
    val paymentDate: String, // YYYY-MM-DD
    val paymentMethod: String, // Cash, bKash, Nagad, Bank, Rocket
    val remarks: String = ""
)
