package presentation.app

import AppViewModel
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.atiurin.atp.kmpclient.FarmClientConfig
import di.Container
import domain.command.CommandExecutor
import kotlinx.coroutines.launch
import presentation.devicedetails.DeviceDetailsScreen
import presentation.devicedetails.DeviceDetailsViewModel
import presentation.devicelist.DeviceListScreen
import presentation.devicelist.DeviceListViewModel
import presentation.mainmeny.MenuScreen
import presentation.servers.ServersScreen
import presentation.welcome.WelcomeScreen

@Composable
fun FarmApp(
    navController: NavHostController = rememberNavController(),
    appViewModel: AppViewModel = viewModel { AppViewModel() },
) {
    // Get current back stack entry
    val backStackEntry by navController.currentBackStackEntryAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    CommandExecutor.doOnFailure = { mes ->
        scope.launch {
            snackbarHostState.showSnackbar(message = mes, actionLabel = "OK")
        }
    }
    // Get the name of the current screen
    val currentScreen = AppScreen.valueOf(
        backStackEntry?.destination?.route ?: AppScreen.Menu.name
    )
    val onRefresh = remember { mutableStateOf({}) }
    Scaffold(
        topBar = {
            AppBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() },
                onRefresh = if (currentScreen == AppScreen.DeviceList) {
                    onRefresh.value
                } else null
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { snackbarData ->
                Snackbar(
                    snackbarData = snackbarData,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppScreen.Welcome.name,
            modifier = Modifier
                .fillMaxSize()
//                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {
            composable(route = AppScreen.Welcome.name) {
                WelcomeScreen(
                    onContinueButtonClicked = { farmUrl, userName ->
                        Container.setFarmClient(
                            FarmClientConfig(listOf(farmUrl), "Desktop App $userName"),
                            doOnFailure = { mes ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(message = mes, actionLabel = "OK")
                                }
                            }
                        )
                        navController.navigate(AppScreen.Menu.name)
                    }
                )
            }
            composable(route = AppScreen.Menu.name) {
                MenuScreen(
                    onNextButtonClicked = {
                        navController.navigate(it.name)
                    }
                )
            }
            composable(route = AppScreen.Servers.name) {
                ServersScreen()
            }
            composable(route = AppScreen.DeviceList.name) {
                val deviceListViewModel = viewModel { DeviceListViewModel(Container.deviceRepository) }
                DisposableEffect(Unit) {
                    val callback = NavController.OnDestinationChangedListener { _, destination, _ ->
                        if (destination.route == AppScreen.DeviceList.name) {
                            deviceListViewModel.getDevices()
                        }
                    }
                    navController.addOnDestinationChangedListener(callback)
                    onDispose {
                        navController.removeOnDestinationChangedListener(callback)
                    }
                }
                onRefresh.value = { deviceListViewModel.getDevices() }
                DeviceListScreen(
                    viewModel = deviceListViewModel,
                    onItemClick = { uid ->
                        appViewModel.setDeviceItem(uid)
                        navController.navigate(AppScreen.DeviceDetails.name)
                    }
                )
            }
            composable(route = AppScreen.DeviceDetails.name) {
                val deviceDetailsViewModel = viewModel {
                    DeviceDetailsViewModel(
                        repository = Container.deviceRepository,
                        deviceUid = appViewModel.uiState.value.deviceItemId,
                    )
                }
                DeviceDetailsScreen(deviceDetailsViewModel)
            }
        }
    }
}
