package com.example.ui.screens.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.Customer
import com.example.data.local.model.PaymentWithCustomer
import com.example.data.preferences.UserSettings
import com.example.data.repository.WiFiManagerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PaymentUiState(
    val settings: UserSettings = UserSettings(),
    val payments: List<PaymentWithCustomer> = emptyList(),
    val customers: List<Customer> = emptyList(),
    val searchQuery: String = "",
    val selectedTab: Int = 0, // 0 = Customers, 1 = History
    val selectedMonth: String = "",
    val selectedCustomerFilterId: Long = -1L,
    val isLoading: Boolean = false
)

data class PaymentReceiptData(
    val customerName: String,
    val mobile: String,
    val address: String,
    val billingMonth: String,
    val amountPaid: Double,
    val paymentDate: String,
    val paymentMethod: String = "",
    val remarks: String,
    val businessName: String,
    val currency: String
)

private data class PaymentFilterParams(
    val month: String,
    val query: String,
    val tab: Int,
    val customerFilterId: Long
)

class PaymentViewModel(private val repository: WiFiManagerRepository) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(repository.getCurrentMonthString())
    val selectedMonth = _selectedMonth.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab = _selectedTab.asStateFlow()

    private val _selectedCustomerFilterId = MutableStateFlow(-1L)
    val selectedCustomerFilterId = _selectedCustomerFilterId.asStateFlow()

    private val _filterParams = combine(_selectedMonth, _searchQuery, _selectedTab, _selectedCustomerFilterId) { month, query, tab, custFilterId ->
        PaymentFilterParams(month, query, tab, custFilterId)
    }

    val uiState: StateFlow<PaymentUiState> = combine(
        repository.userSettings,
        repository.allPaymentsWithCustomer,
        repository.allCustomers,
        _filterParams
    ) { settings, paymentsList, customersList, filter ->
        val query = filter.query
        val month = filter.month
        val tab = filter.tab
        val custFilterId = filter.customerFilterId

        val filteredCustomers = customersList.filter { c ->
            query.isEmpty() ||
                    c.fullName.contains(query, ignoreCase = true) ||
                    c.mobileNumber.contains(query) ||
                    c.macAddress.contains(query, ignoreCase = true)
        }.sortedByDescending { it.connectionDate }
        val filteredPayments = paymentsList.filter { item ->
            (month.isEmpty() || item.payment.billingMonth == month) &&
                    (query.isEmpty() || item.customer.fullName.contains(query, ignoreCase = true) || item.customer.mobileNumber.contains(query)) &&
                    (custFilterId <= 0L || item.customer.id == custFilterId)
        }
        PaymentUiState(
            settings = settings,
            payments = filteredPayments,
            customers = filteredCustomers,
            searchQuery = query,
            selectedTab = tab,
            selectedMonth = month,
            selectedCustomerFilterId = custFilterId
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PaymentUiState(isLoading = true)
    )

    fun onCustomerFilterChange(customerId: Long) {
        _selectedCustomerFilterId.value = customerId
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onTabSelect(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    // Collect Payment Form State
    var selectedCustomerId = MutableStateFlow<Long>(-1L)
    var billingMonthState = MutableStateFlow(repository.getCurrentMonthString())
    var amountPaidState = MutableStateFlow("")
    var paymentDateState = MutableStateFlow(repository.getTodayDateString())
    var remarksState = MutableStateFlow("")
    var formErrorState = MutableStateFlow<String?>(null)
    var receiptDataState = MutableStateFlow<PaymentReceiptData?>(null)

    // Edit Payment State
    var editingPaymentState = MutableStateFlow<PaymentWithCustomer?>(null)
    var editAmountState = MutableStateFlow("")
    var editMonthState = MutableStateFlow("")
    var editDateState = MutableStateFlow("")
    var editRemarksState = MutableStateFlow("")
    var editErrorState = MutableStateFlow<String?>(null)

    // Delete Payment State
    var deletingPaymentState = MutableStateFlow<PaymentWithCustomer?>(null)

    fun onMonthFilterChange(month: String) {
        _selectedMonth.value = month
    }

    fun initPaymentForm(preselectedCustomerId: Long) {
        if (preselectedCustomerId > 0) {
            selectedCustomerId.value = preselectedCustomerId
            viewModelScope.launch {
                val customer = repository.getCustomerById(preselectedCustomerId)
                if (customer != null) {
                    amountPaidState.value = customer.monthlyBillAmount.toString()
                }
            }
        }
    }

    suspend fun recordPayment(): Boolean {
        val custId = selectedCustomerId.value
        val month = billingMonthState.value.trim()
        val amount = amountPaidState.value.toDoubleOrNull() ?: -1.0
        val date = paymentDateState.value.trim()
        val remarks = remarksState.value.trim()

        if (custId <= 0) {
            formErrorState.value = "Please select a customer."
            return false
        }
        if (amount <= 0) {
            formErrorState.value = "Amount paid must be greater than zero."
            return false
        }

        repository.recordPayment(
            customerId = custId,
            billingMonth = month.ifEmpty { repository.getCurrentMonthString() },
            amountPaid = amount,
            paymentDate = date.ifEmpty { repository.getTodayDateString() },
            remarks = remarks
        )

        // Generate Receipt Data
        val customer = repository.getCustomerById(custId)
        val settings = uiState.value.settings
        if (customer != null) {
            receiptDataState.value = PaymentReceiptData(
                customerName = customer.fullName,
                mobile = customer.mobileNumber,
                address = customer.address,
                billingMonth = month,
                amountPaid = amount,
                paymentDate = date,
                remarks = remarks,
                businessName = settings.businessName,
                currency = settings.currency
            )
        }

        formErrorState.value = null
        return true
    }

    fun clearReceipt() {
        receiptDataState.value = null
    }

    fun startEditPayment(item: PaymentWithCustomer) {
        editingPaymentState.value = item
        editAmountState.value = item.payment.amountPaid.toString()
        editMonthState.value = item.payment.billingMonth
        editDateState.value = item.payment.paymentDate
        editRemarksState.value = item.payment.remarks
        editErrorState.value = null
    }

    fun dismissEditPayment() {
        editingPaymentState.value = null
        editErrorState.value = null
    }

    suspend fun saveEditPayment(): Boolean {
        val item = editingPaymentState.value ?: return false
        val amt = editAmountState.value.toDoubleOrNull() ?: -1.0
        val month = editMonthState.value.trim()
        val date = editDateState.value.trim()
        val remarks = editRemarksState.value.trim()

        if (amt <= 0) {
            editErrorState.value = "Amount paid must be greater than zero."
            return false
        }
        if (month.isEmpty()) {
            editErrorState.value = "Billing month is required."
            return false
        }

        repository.updatePayment(
            oldPayment = item.payment,
            newAmount = amt,
            newMonth = month,
            newDate = date.ifEmpty { repository.getTodayDateString() },
            newRemarks = remarks
        )

        editingPaymentState.value = null
        editErrorState.value = null
        return true
    }

    fun startDeletePayment(item: PaymentWithCustomer) {
        deletingPaymentState.value = item
    }

    fun dismissDeletePayment() {
        deletingPaymentState.value = null
    }

    fun confirmDeletePayment() {
        val item = deletingPaymentState.value ?: return
        viewModelScope.launch {
            repository.deletePayment(item.payment)
            deletingPaymentState.value = null
        }
    }

    class Factory(private val repository: WiFiManagerRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PaymentViewModel(repository) as T
        }
    }
}
