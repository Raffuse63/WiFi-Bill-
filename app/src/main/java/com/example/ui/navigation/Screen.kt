package com.example.ui.navigation

sealed class Screen(val route: String) {
    object PinLock : Screen("pin_lock")
    object Dashboard : Screen("dashboard")
    object CustomerList : Screen("customer_list")
    object AddEditCustomer : Screen("add_edit_customer?customerId={customerId}") {
        fun createRoute(customerId: Long = -1L) = "add_edit_customer?customerId=$customerId"
    }
    object CustomerDetail : Screen("customer_detail/{customerId}") {
        fun createRoute(customerId: Long) = "customer_detail/$customerId"
    }
    object Payment : Screen("payment?customerId={customerId}") {
        fun createRoute(customerId: Long = -1L) = "payment?customerId=$customerId"
    }
    object PaymentHistory : Screen("payment_history")
    object Reports : Screen("reports")
    object Settings : Screen("settings")
}
