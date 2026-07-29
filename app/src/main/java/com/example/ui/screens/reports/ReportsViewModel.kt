package com.example.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.model.BillWithCustomer
import com.example.data.preferences.UserSettings
import com.example.data.repository.WiFiManagerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ReportsUiState(
    val settings: UserSettings = UserSettings(),
    val todayCollection: Double = 0.0,
    val thisMonthCollection: Double = 0.0,
    val yearCollection: Double = 0.0,
    val selectedMonthCollection: Double = 0.0,
    val totalDueAmount: Double = 0.0,
    val dueCustomersBills: List<BillWithCustomer> = emptyList(),
    val paidCustomersBills: List<BillWithCustomer> = emptyList(),
    val selectedMonth: String = "",
    val isLoading: Boolean = false
)

class ReportsViewModel(private val repository: WiFiManagerRepository) : ViewModel() {

    private val currentMonth = repository.getCurrentMonthString()
    private val todayDate = repository.getTodayDateString()
    private val currentYear = repository.getCurrentYearString()

    private val _selectedMonth = MutableStateFlow(currentMonth)
    val selectedMonth = _selectedMonth.asStateFlow()

    val uiState: StateFlow<ReportsUiState> = combine(
        repository.userSettings,
        repository.getTodayCollection(todayDate),
        repository.getTotalCollectionByMonth(currentMonth),
        repository.getYearCollection(currentYear),
        repository.totalDueAmount
    ) { settings, todayCol, monthCol, yearCol, dueAmt ->
        ReportsUiState(
            settings = settings,
            todayCollection = todayCol ?: 0.0,
            thisMonthCollection = monthCol ?: 0.0,
            yearCollection = yearCol ?: 0.0,
            totalDueAmount = dueAmt ?: 0.0,
            selectedMonth = currentMonth
        )
    }.combine(repository.getBillsWithCustomerByMonth(currentMonth)) { state, monthBills ->
        val dueList = monthBills.filter { it.bill.status != "PAID" }
        val paidList = monthBills.filter { it.bill.status == "PAID" }
        state.copy(
            dueCustomersBills = dueList,
            paidCustomersBills = paidList
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReportsUiState(isLoading = true)
    )

    fun onMonthChange(month: String) {
        _selectedMonth.value = month
    }

    class Factory(private val repository: WiFiManagerRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReportsViewModel(repository) as T
        }
    }
}
