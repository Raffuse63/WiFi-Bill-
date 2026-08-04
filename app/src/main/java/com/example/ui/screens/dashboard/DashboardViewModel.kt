package com.example.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.model.BillWithCustomer
import com.example.data.local.model.PaymentWithCustomer
import com.example.data.preferences.UserSettings
import com.example.data.repository.WiFiManagerRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val settings: UserSettings = UserSettings(),
    val totalCustomers: Int = 0,
    val activeCustomers: Int = 0,
    val paidCount: Int = 0,
    val dueCount: Int = 0,
    val monthlyCollection: Double = 0.0,
    val totalDueAmount: Double = 0.0,
    val recentPayments: List<PaymentWithCustomer> = emptyList(),
    val dueBills: List<BillWithCustomer> = emptyList(),
    val isLoading: Boolean = false
)

class DashboardViewModel(private val repository: WiFiManagerRepository) : ViewModel() {

    private val currentMonth = repository.getCurrentMonthString()

    init {
        viewModelScope.launch {
            repository.autoGenerateMonthlyBills()
        }
    }

    private val countsAndCollectionFlow = combine(
        repository.totalCustomerCount,
        repository.activeCustomers,
        repository.allBills,
        repository.getTotalCollectionByMonth(currentMonth)
    ) { totalCount, activeList, billsList, collection ->
        val paidCount = activeList.count { c ->
            val targetMonth = repository.getBillingMonthForCustomer(c)
            val bill = billsList.find { it.customerId == c.id && it.billMonth == targetMonth }
            bill?.status == "PAID"
        }
        val dueCount = activeList.size - paidCount
        Tuple5(totalCount, activeList.size, paidCount, dueCount, collection ?: 0.0)
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.userSettings,
        countsAndCollectionFlow,
        repository.totalDueAmount,
        repository.allPaymentsWithCustomer,
        repository.getUnpaidBillsWithCustomer()
    ) { settings, counts, totalDue, payments, dueBillsList ->
        DashboardUiState(
            settings = settings,
            totalCustomers = counts.v1,
            activeCustomers = counts.v2,
            paidCount = counts.v3,
            dueCount = counts.v4,
            monthlyCollection = counts.v5,
            totalDueAmount = totalDue ?: 0.0,
            recentPayments = payments.take(5),
            dueBills = dueBillsList.take(5)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(isLoading = true)
    )

    private data class Tuple5<A, B, C, D, E>(
        val v1: A,
        val v2: B,
        val v3: C,
        val v4: D,
        val v5: E
    )

    class Factory(private val repository: WiFiManagerRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(repository) as T
        }
    }
}
