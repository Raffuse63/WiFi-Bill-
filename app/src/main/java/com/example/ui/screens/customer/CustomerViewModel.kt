package com.example.ui.screens.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.Customer
import com.example.data.local.entity.MonthlyBill
import com.example.data.local.entity.Payment
import com.example.data.local.model.CustomerWithBills
import com.example.data.preferences.UserSettings
import com.example.data.repository.WiFiManagerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CustomerListUiState(
    val settings: UserSettings = UserSettings(),
    val customers: List<Customer> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: String = "ALL", // ALL, PAID, DUE, PARTIAL
    val isLoading: Boolean = false
)

class CustomerViewModel(private val repository: WiFiManagerRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("ALL")
    val selectedFilter = _selectedFilter.asStateFlow()

    val currentMonth = repository.getCurrentMonthString()

    val listUiState: StateFlow<CustomerListUiState> = combine(
        repository.userSettings,
        repository.allCustomers,
        repository.getBillsWithCustomerByMonth(currentMonth),
        _searchQuery,
        _selectedFilter
    ) { settings, customersList, billsWithCustList, query, filter ->
        val billStatusMap = billsWithCustList.associate { it.customer.id to it.bill.status }

        var filtered = customersList.filter { c ->
            c.fullName.contains(query, ignoreCase = true) ||
                    c.mobileNumber.contains(query) ||
                    c.macAddress.contains(query, ignoreCase = true)
        }

        if (filter != "ALL") {
            filtered = filtered.filter { c ->
                val status = billStatusMap[c.id] ?: "UNPAID"
                when (filter) {
                    "PAID" -> status == "PAID"
                    "DUE" -> status == "UNPAID" || status == "PARTIAL"
                    "PARTIAL" -> status == "PARTIAL"
                    else -> true
                }
            }
        }

        CustomerListUiState(
            settings = settings,
            customers = filtered,
            searchQuery = query,
            selectedFilter = filter
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CustomerListUiState(isLoading = true)
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterChange(filter: String) {
        _selectedFilter.value = filter
    }

    private val customerWithBillsFlows = mutableMapOf<Long, StateFlow<CustomerWithBills?>>()

    fun getCustomerWithBills(id: Long): StateFlow<CustomerWithBills?> {
        return customerWithBillsFlows.getOrPut(id) {
            repository.getCustomerWithBills(id)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = null
                )
        }
    }

    // Add / Edit Customer Form State
    var nameState = MutableStateFlow("")
    var mobileState = MutableStateFlow("")
    var addressState = MutableStateFlow("")
    var billAmountState = MutableStateFlow("")
    var macAddressState = MutableStateFlow("")
    var connectionDateState = MutableStateFlow(repository.getTodayDateString())
    var isActiveState = MutableStateFlow(true)
    var notesState = MutableStateFlow("")
    var formErrorState = MutableStateFlow<String?>(null)

    fun loadCustomerForEdit(id: Long) {
        if (id <= 0) return
        viewModelScope.launch {
            val customer = repository.getCustomerById(id)
            if (customer != null) {
                nameState.value = customer.fullName
                mobileState.value = customer.mobileNumber
                addressState.value = customer.address
                billAmountState.value = customer.monthlyBillAmount.toString()
                macAddressState.value = customer.macAddress
                connectionDateState.value = customer.connectionDate
                isActiveState.value = customer.isActive
                notesState.value = customer.notes
            }
        }
    }

    suspend fun saveCustomer(editingId: Long): Boolean {
        val name = nameState.value.trim()
        val mobile = mobileState.value.trim()
        val address = addressState.value.trim()
        val billAmt = billAmountState.value.toDoubleOrNull() ?: -1.0
        val mac = macAddressState.value.trim()

        if (name.isEmpty()) {
            formErrorState.value = "Full Name cannot be empty."
            return false
        }
        if (billAmt < 0) {
            formErrorState.value = "Bill Amount must be a valid positive number."
            return false
        }

        // Check mobile duplicate if provided
        if (mobile.isNotEmpty()) {
            val existingWithMobile = repository.getCustomerByMobile(mobile)
            if (existingWithMobile != null && existingWithMobile.id != editingId) {
                formErrorState.value = "A customer with this phone number already exists."
                return false
            }
        }

        val customer = Customer(
            id = if (editingId > 0) editingId else 0,
            fullName = name,
            mobileNumber = mobile,
            address = address,
            monthlyBillAmount = billAmt,
            connectionDate = connectionDateState.value.ifEmpty { repository.getTodayDateString() },
            packageName = "",
            macAddress = mac,
            isActive = isActiveState.value,
            notes = notesState.value
        )

        if (editingId > 0) {
            repository.updateCustomer(customer)
        } else {
            repository.addCustomer(customer)
        }
        formErrorState.value = null
        return true
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
        }
    }

    class Factory(private val repository: WiFiManagerRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CustomerViewModel(repository) as T
        }
    }
}
