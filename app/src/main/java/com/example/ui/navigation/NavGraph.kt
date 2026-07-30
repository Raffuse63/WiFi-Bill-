package com.example.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.backup.BackupManager
import com.example.data.preferences.SettingsDataStore
import com.example.data.preferences.UserSettings
import com.example.data.repository.WiFiManagerRepository
import com.example.ui.screens.customer.AddEditCustomerScreen
import com.example.ui.screens.customer.CustomerDetailScreen
import com.example.ui.screens.customer.CustomerListScreen
import com.example.ui.screens.customer.CustomerViewModel
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.dashboard.DashboardViewModel
import com.example.ui.screens.payment.PaymentHistoryScreen
import com.example.ui.screens.payment.PaymentScreen
import com.example.ui.screens.payment.PaymentViewModel
import com.example.ui.screens.reports.ReportsScreen
import com.example.ui.screens.reports.ReportsViewModel
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.settings.SettingsViewModel
import com.example.ui.util.LanguageUtils
import androidx.navigation.navArgument

data class BottomNavItem(
    val navigateRoute: String,
    val destinationRoute: String,
    val titleKey: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Payment.createRoute(-1L), Screen.Payment.route, "payments", Icons.Default.Payments),
    BottomNavItem(Screen.PaymentHistory.route, Screen.PaymentHistory.route, "payment_history", Icons.Default.Receipt),
    BottomNavItem(Screen.Settings.route, Screen.Settings.route, "settings", Icons.Default.Settings)
)

@Composable
fun AppNavGraph(
    repository: WiFiManagerRepository,
    settingsDataStore: SettingsDataStore,
    backupManager: BackupManager,
    userSettings: UserSettings,
    navController: NavHostController = rememberNavController()
) {
    val isBangla = userSettings.language == "bn"

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val dashboardViewModel: DashboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = DashboardViewModel.Factory(repository)
    )
    val customerViewModel: CustomerViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = CustomerViewModel.Factory(repository)
    )
    val paymentViewModel: PaymentViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = PaymentViewModel.Factory(repository)
    )
    val reportsViewModel: ReportsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = ReportsViewModel.Factory(repository)
    )
    val settingsViewModel: SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = SettingsViewModel.Factory(repository, settingsDataStore, backupManager)
    )

    val mainTabRoutes = setOf(
        Screen.Payment.route,
        Screen.PaymentHistory.route,
        Screen.Settings.route,
        Screen.Dashboard.route,
        Screen.CustomerList.route,
        Screen.Reports.route
    )
    val showBottomBar = currentRoute in mainTabRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentRoute == item.destinationRoute || (item.titleKey == "payments" && currentRoute?.startsWith("payment") == true)
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.navigateRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(imageVector = item.icon, contentDescription = item.titleKey) },
                            label = {
                                Text(
                                    text = LanguageUtils.getText(item.titleKey, isBangla),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                                )
                            },
                            colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                                selectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer,
                                indicatorColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_item_${item.titleKey}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Payment.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToAddCustomer = { navController.navigate(Screen.AddEditCustomer.createRoute()) },
                    onNavigateToCollectPayment = { custId ->
                        navController.navigate(Screen.Payment.createRoute(custId))
                    },
                    onNavigateToReports = { navController.navigate(Screen.Reports.route) },
                    onNavigateToCustomerDetail = { custId ->
                        navController.navigate(Screen.CustomerDetail.createRoute(custId))
                    }
                )
            }

            composable(Screen.CustomerList.route) {
                CustomerListScreen(
                    viewModel = customerViewModel,
                    onNavigateToAddCustomer = { navController.navigate(Screen.AddEditCustomer.createRoute()) },
                    onNavigateToCustomerDetail = { custId ->
                        navController.navigate(Screen.CustomerDetail.createRoute(custId))
                    },
                    onNavigateToCollectPayment = { custId ->
                        navController.navigate(Screen.Payment.createRoute(custId))
                    }
                )
            }

            composable(
                route = Screen.AddEditCustomer.route,
                arguments = listOf(navArgument("customerId") {
                    type = NavType.LongType
                    defaultValue = -1L
                })
            ) { backStackEntry ->
                val customerId = backStackEntry.arguments?.getLong("customerId") ?: -1L
                AddEditCustomerScreen(
                    customerId = customerId,
                    viewModel = customerViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.CustomerDetail.route,
                arguments = listOf(navArgument("customerId") { type = NavType.LongType })
            ) { backStackEntry ->
                val customerId = backStackEntry.arguments?.getLong("customerId") ?: -1L
                CustomerDetailScreen(
                    customerId = customerId,
                    viewModel = customerViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { id -> navController.navigate(Screen.AddEditCustomer.createRoute(id)) },
                    onNavigateToCollectPayment = { id -> navController.navigate(Screen.Payment.createRoute(id)) }
                )
            }

            composable(
                route = Screen.Payment.route,
                arguments = listOf(navArgument("customerId") {
                    type = NavType.LongType
                    defaultValue = -1L
                })
            ) { backStackEntry ->
                val customerId = backStackEntry.arguments?.getLong("customerId") ?: -1L
                PaymentScreen(
                    viewModel = paymentViewModel,
                    preselectedCustomerId = customerId,
                    onNavigateToAddCustomer = { navController.navigate(Screen.AddEditCustomer.createRoute()) },
                    onNavigateToCustomerDetail = { id -> navController.navigate(Screen.CustomerDetail.createRoute(id)) }
                )
            }

            composable(Screen.PaymentHistory.route) {
                PaymentHistoryScreen(
                    viewModel = paymentViewModel,
                    userSettings = userSettings
                )
            }

            composable(Screen.Reports.route) {
                ReportsScreen(viewModel = reportsViewModel)
            }

            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = settingsViewModel)
            }
        }
    }
}
