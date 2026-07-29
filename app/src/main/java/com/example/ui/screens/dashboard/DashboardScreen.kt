package com.example.ui.screens.dashboard

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.StatusBadge
import com.example.ui.components.SummaryStatCard
import com.example.ui.theme.AlertRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.LightGreen
import com.example.ui.theme.LightOrange
import com.example.ui.theme.LightRed
import com.example.ui.theme.WarningOrange
import com.example.ui.util.LanguageUtils

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToAddCustomer: () -> Unit,
    onNavigateToCollectPayment: (Long) -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToCustomerDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isBangla = state.settings.language == "bn"
    val currency = state.settings.currency

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Hero Banner
        item {
            HeroHeaderCard(
                businessName = state.settings.businessName,
                ownerName = state.settings.ownerName,
                isBangla = isBangla
            )
        }

        // Quick Actions Row
        item {
            Text(
                text = LanguageUtils.getText("quick_actions", isBangla),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    title = LanguageUtils.getText("add_customer", isBangla),
                    icon = Icons.Default.PersonAdd,
                    iconBgColor = MaterialTheme.colorScheme.primaryContainer,
                    iconTintColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    onClick = onNavigateToAddCustomer,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    title = LanguageUtils.getText("collect_payment", isBangla),
                    icon = Icons.Default.Payments,
                    iconBgColor = LightGreen,
                    iconTintColor = EmeraldGreen,
                    onClick = { onNavigateToCollectPayment(-1L) },
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    title = LanguageUtils.getText("reports", isBangla),
                    icon = Icons.Default.Assessment,
                    iconBgColor = LightOrange,
                    iconTintColor = WarningOrange,
                    onClick = onNavigateToReports,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Metrics Grid / Summary Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryStatCard(
                    title = LanguageUtils.getText("total_customers", isBangla),
                    value = LanguageUtils.formatNumber(state.totalCustomers, isBangla),
                    icon = Icons.Default.People,
                    iconBgColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    iconTintColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                SummaryStatCard(
                    title = LanguageUtils.getText("paid_customers", isBangla),
                    value = LanguageUtils.formatNumber(state.paidCount, isBangla),
                    icon = Icons.Default.CheckCircle,
                    iconBgColor = LightGreen,
                    iconTintColor = EmeraldGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryStatCard(
                    title = LanguageUtils.getText("due_customers", isBangla),
                    value = LanguageUtils.formatNumber(state.dueCount, isBangla),
                    icon = Icons.Default.Error,
                    iconBgColor = LightRed,
                    iconTintColor = AlertRed,
                    modifier = Modifier.weight(1f)
                )
                SummaryStatCard(
                    title = LanguageUtils.getText("monthly_collection", isBangla),
                    value = LanguageUtils.formatAmount(state.monthlyCollection, currency, isBangla),
                    icon = Icons.Default.MonetizationOn,
                    iconBgColor = EmeraldGreen.copy(alpha = 0.15f),
                    iconTintColor = EmeraldGreen,
                    modifier = Modifier.weight(1f)
                )
            }
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

        // Due Customers Section
        if (state.dueBills.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = LanguageUtils.getText("due_customers", isBangla),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AlertRed
                    )
                }
            }

            items(state.dueBills) { billWithCustomer ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onNavigateToCustomerDetail(billWithCustomer.customer.id) }
                        .testTag("dashboard_due_item_${billWithCustomer.customer.id}"),
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
                                text = billWithCustomer.customer.fullName,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = LanguageUtils.formatNumber(billWithCustomer.customer.mobileNumber, isBangla),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = LanguageUtils.formatAmount(billWithCustomer.bill.dueAmount, currency, isBangla),
                                    fontWeight = FontWeight.Bold,
                                    color = AlertRed
                                )
                                StatusBadge(status = billWithCustomer.bill.status, isBangla = isBangla)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(EmeraldGreen)
                                    .clickable { onNavigateToCollectPayment(billWithCustomer.customer.id) }
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Collect",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun HeroHeaderCard(
    businessName: String,
    ownerName: String,
    isBangla: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = businessName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${LanguageUtils.getText("owner_name", isBangla)}: $ownerName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    title: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconTintColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag("quick_action_${title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTintColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
