package com.example.ui.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CustomerItemCard
import com.example.ui.components.SearchBarComponent
import com.example.ui.util.LanguageUtils

@Composable
fun CustomerListScreen(
    viewModel: CustomerViewModel,
    onNavigateToAddCustomer: () -> Unit,
    onNavigateToCustomerDetail: (Long) -> Unit,
    onNavigateToCollectPayment: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.listUiState.collectAsStateWithLifecycle()
    val isBangla = state.settings.language == "bn"
    val currency = state.settings.currency

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddCustomer,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_customer")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Customer")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            SearchBarComponent(
                query = state.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                placeholderText = LanguageUtils.getText("search_customer", isBangla)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips Row
            val filterOptions = listOf("ALL", "PAID", "DUE", "PARTIAL")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filterOptions) { filterKey ->
                    val isSelected = state.selectedFilter == filterKey
                    val labelText = when (filterKey) {
                        "ALL" -> LanguageUtils.getText("all", isBangla)
                        "PAID" -> LanguageUtils.getText("paid", isBangla)
                        "DUE" -> LanguageUtils.getText("due", isBangla)
                        "PARTIAL" -> LanguageUtils.getText("partial", isBangla)
                        else -> filterKey
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surface
                            )
                            .clickable { viewModel.onFilterChange(filterKey) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("filter_chip_$filterKey")
                    ) {
                        Text(
                            text = labelText,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state.customers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.padding(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Text(
                            text = if (isBangla) "কোনো গ্রাহক পাওয়া যায়নি" else "No customers found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.customers, key = { it.id }) { customer ->
                        CustomerItemCard(
                            customer = customer,
                            currency = currency,
                            isBangla = isBangla,
                            dueAmount = customer.monthlyBillAmount,
                            billStatus = if (customer.isActive) "ACTIVE" else "INACTIVE",
                            onClick = { onNavigateToCustomerDetail(customer.id) },
                            onCollectClick = { onNavigateToCollectPayment(customer.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}
