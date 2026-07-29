package com.example.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.data.local.entity.Customer
import com.example.data.local.entity.MonthlyBill
import com.example.data.local.entity.Payment

data class CustomerWithBills(
    @Embedded val customer: Customer,
    @Relation(
        parentColumn = "id",
        entityColumn = "customerId"
    )
    val bills: List<MonthlyBill>,
    @Relation(
        parentColumn = "id",
        entityColumn = "customerId"
    )
    val payments: List<Payment>
)

data class BillWithCustomer(
    @Embedded val bill: MonthlyBill,
    @Relation(
        parentColumn = "customerId",
        entityColumn = "id"
    )
    val customer: Customer
)

data class PaymentWithCustomer(
    @Embedded val payment: Payment,
    @Relation(
        parentColumn = "customerId",
        entityColumn = "id"
    )
    val customer: Customer
)
