package com.example.ui.screens.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.StatusBadge
import com.example.ui.components.SummaryStatCard
import com.example.ui.theme.AlertRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.LightGreen
import com.example.ui.theme.LightRed
import com.example.ui.util.LanguageUtils

@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isBangla = state.settings.language == "bn"
    val currency = state.settings.currency

    var selectedTab by remember { mutableStateOf(0) } // 0: Overview, 1: Due Customers, 2: Paid Customers

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = LanguageUtils.getText("reports", isBangla),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(LanguageUtils.getText("income_summary", isBangla), fontSize = 12.sp) },
                    modifier = Modifier.testTag("tab_income_summary")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(LanguageUtils.getText("due_customers", isBangla), fontSize = 12.sp) },
                    modifier = Modifier.testTag("tab_due_customers")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text(LanguageUtils.getText("paid_customers", isBangla), fontSize = 12.sp) },
                    modifier = Modifier.testTag("tab_paid_customers")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> OverviewReportSection(state = state, isBangla = isBangla, currency = currency)
                1 -> CustomerBillListSection(bills = state.dueCustomersBills, isBangla = isBangla, currency = currency)
                2 -> CustomerBillListSection(bills = state.paidCustomersBills, isBangla = isBangla, currency = currency)
            }
        }
    }
}

@Composable
private fun OverviewReportSection(
    state: ReportsUiState,
    isBangla: Boolean,
    currency: String
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            SummaryStatCard(
                title = LanguageUtils.getText("todays_collection", isBangla),
                value = LanguageUtils.formatAmount(state.todayCollection, currency, isBangla),
                icon = Icons.Default.CalendarToday,
                iconBgColor = LightGreen,
                iconTintColor = EmeraldGreen
            )
        }
        item {
            SummaryStatCard(
                title = LanguageUtils.getText("this_month_collection", isBangla),
                value = LanguageUtils.formatAmount(state.thisMonthCollection, currency, isBangla),
                icon = Icons.Default.MonetizationOn,
                iconBgColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                iconTintColor = MaterialTheme.colorScheme.primary
            )
        }
        item {
            SummaryStatCard(
                title = LanguageUtils.getText("year_collection", isBangla),
                value = LanguageUtils.formatAmount(state.yearCollection, currency, isBangla),
                icon = Icons.Default.DateRange,
                iconBgColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                iconTintColor = MaterialTheme.colorScheme.tertiary
            )
        }
        item {
            SummaryStatCard(
                title = LanguageUtils.getText("total_due_amount", isBangla),
                value = LanguageUtils.formatAmount(state.totalDueAmount, currency, isBangla),
                icon = Icons.Default.Error,
                iconBgColor = LightRed,
                iconTintColor = AlertRed
            )
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun CustomerBillListSection(
    bills: List<com.example.data.local.model.BillWithCustomer>,
    isBangla: Boolean,
    currency: String
) {
    if (bills.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isBangla) "কোনো তথ্য পাওয়া যায়নি" else "No matching data found",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(bills, key = { it.bill.id }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = item.customer.fullName,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = LanguageUtils.formatNumber(item.customer.mobileNumber, isBangla),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = LanguageUtils.formatAmount(item.bill.dueAmount, currency, isBangla),
                                fontWeight = FontWeight.Bold,
                                color = if (item.bill.dueAmount > 0) AlertRed else EmeraldGreen
                            )
                            StatusBadge(status = item.bill.status, isBangla = isBangla)
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
