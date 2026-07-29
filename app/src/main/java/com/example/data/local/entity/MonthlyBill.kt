package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "monthly_bills",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["customerId"]), Index(value = ["billMonth"])]
)
data class MonthlyBill(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val billMonth: String, // Format: YYYY-MM
    val totalAmount: Double,
    val paidAmount: Double = 0.0,
    val status: String = "UNPAID", // PAID, UNPAID, PARTIAL
    val dueDate: String = "",
    val notes: String = ""
) {
    val dueAmount: Double
        get() = (totalAmount - paidAmount).coerceAtLeast(0.0)
}
