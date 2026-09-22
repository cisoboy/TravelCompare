package com.example.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.RideCategory
import com.example.model.RideOption
import com.example.model.RideProvider
import com.example.ui.components.AccountsPromotionsDialog
import com.example.ui.components.AddressEditorDialog
import com.example.ui.components.BestSavingsBanner
import com.example.ui.components.CityPickerSheet
import com.example.ui.components.FilterSortBar
import com.example.ui.components.LiveHeader
import com.example.ui.components.PriceBreakdownSheet
import com.example.ui.components.RideComparisonCard
import com.example.ui.components.RouteMapCanvas
import com.example.ui.components.RouteSelectorCard
import com.example.ui.components.SavedRoutesSheet
import com.example.ui.theme.BoltGreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.util.RideLauncher
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@Composable
fun RideCompareScreen(
    viewModel: RideCompareViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val comparison by viewModel.comparisonResult.collectAsStateWithLifecycle()
    val currentCity by viewModel.currentCity.collectAsStateWithLifecycle()
    val pickup by viewModel.pickupPoint.collectAsStateWithLifecycle()
    val dropoff by viewModel.dropoffPoint.collectAsStateWithLifecycle()
    val traffic by viewModel.trafficCondition.collectAsStateWithLifecycle()
    val simulateSurge by viewModel.simulateSurge.collectAsStateWithLifecycle()
    val categoryFilter by viewModel.categoryFilter.collectAsStateWithLifecycle()
    val sortMode by viewModel.sortMode.collectAsStateWithLifecycle()
    val isLivePolling by viewModel.isLivePolling.collectAsStateWithLifecycle()
    val refreshCountdown by viewModel.refreshCountdown.collectAsStateWithLifecycle()
    val activeBreakdown by viewModel.activeBreakdownOption.collectAsStateWithLifecycle()
    val isCityPickerOpen by viewModel.isCityPickerOpen.collectAsStateWithLifecycle()
    val isSavedRoutesOpen by viewModel.isSavedRoutesOpen.collectAsStateWithLifecycle()
    val isAddressEditorOpen by viewModel.isAddressEditorOpen.collectAsStateWithLifecycle()
    val editingPickup by viewModel.editingPickup.collectAsStateWithLifecycle()
    val savedRoutes by viewModel.savedRoutes.collectAsStateWithLifecycle()

    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val promotions by viewModel.promotions.collectAsStateWithLifecycle()
    val isAccountsDialogOpen by viewModel.isAccountsDialogOpen.collectAsStateWithLifecycle()

    val loggedInAccounts = accounts.filter { it.isLoggedIn }
    val activePromos = promotions.filter { it.isActive }

    val placesSearchQuery by viewModel.placesSearchQuery.collectAsStateWithLifecycle()
    val placesSearchResults by viewModel.placesSearchResults.collectAsStateWithLifecycle()
    val isSearchingPlaces by viewModel.isSearchingPlaces.collectAsStateWithLifecycle()
    val isLocatingDevice by viewModel.isLocatingDevice.collectAsStateWithLifecycle()
    val deviceLocationError by viewModel.deviceLocationError.collectAsStateWithLifecycle()

    // Location Permission Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            viewModel.setDevicePosition(
                isPickup = editingPickup,
                context = context,
                onSuccess = { point ->
                    coroutineScope.launch {
                        val target = if (editingPickup) "Pickup" else "Destination"
                        snackbarHostState.showSnackbar("$target set to Device Position: ${point.name} (${point.formattedCoordinates()})")
                    }
                },
                onFailure = { err ->
                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            Toast.makeText(context, "Location permission denied. You can search places with Google Places.", Toast.LENGTH_LONG).show()
        }
    }

    val requestDevicePosition: (Boolean) -> Unit = { forPickup ->
        val hasFine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            viewModel.setDevicePosition(
                isPickup = forPickup,
                context = context,
                onSuccess = { point ->
                    coroutineScope.launch {
                        val target = if (forPickup) "Pickup" else "Destination"
                        snackbarHostState.showSnackbar("$target set to Device Position: ${point.name} (${point.formattedCoordinates()})")
                    }
                },
                onFailure = { err ->
                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Filter and sort comparisons
    val filteredComparisons = remember(comparison, categoryFilter, sortMode) {
        var list = comparison.tierComparisons

        // Category filter
        if (categoryFilter != null) {
            list = list.filter { it.category == categoryFilter }
        }

        // Sorting
        when (sortMode) {
            SortMode.BEST_VALUE -> {
                // Sorted by largest savings
                list.sortedByDescending { kotlin.math.abs(it.priceDiff) }
            }
            SortMode.CHEAPEST -> {
                // Sorted by the absolute lowest price in tier
                list.sortedBy {
                    minOf(it.boltOption?.price ?: Double.MAX_VALUE, it.uberOption?.price ?: Double.MAX_VALUE)
                }
            }
            SortMode.FASTEST -> {
                // Sorted by fastest ETA
                list.sortedBy {
                    minOf(it.boltOption?.etaMinutes ?: Int.MAX_VALUE, it.uberOption?.etaMinutes ?: Int.MAX_VALUE)
                }
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Live Header
            LiveHeader(
                currentCity = currentCity,
                isLivePolling = isLivePolling,
                refreshCountdown = refreshCountdown,
                connectedAccountsCount = loggedInAccounts.size,
                activePromosCount = activePromos.size,
                onManualRefresh = {
                    viewModel.manualRefresh()
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Prices updated with live traffic rates!")
                    }
                },
                onOpenCityPicker = { viewModel.setCityPickerOpen(true) },
                onOpenSavedRoutes = { viewModel.setSavedRoutesOpen(true) },
                onOpenAccounts = { viewModel.setAccountsDialogOpen(true) }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("ride_comparison_list")
            ) {
                // Route Selector Card
                item {
                    RouteSelectorCard(
                        pickup = pickup,
                        dropoff = dropoff,
                        city = currentCity,
                        distanceKm = comparison.distanceKm,
                        durationMinutes = comparison.estimatedDurationMinutes,
                        traffic = traffic,
                        isSurgeActive = simulateSurge,
                        onSwap = { viewModel.swapPickupAndDropoff() },
                        onEditPickup = { viewModel.openAddressEditor(forPickup = true) },
                        onEditDropoff = { viewModel.openAddressEditor(forPickup = false) },
                        onSelectPresetDropoff = { viewModel.setDropoff(it) },
                        onGpsRequest = { requestDevicePosition(true) },
                        onTrafficChange = { viewModel.setTraffic(it) },
                        onToggleSurge = { viewModel.toggleSurgeSimulation() },
                        onSaveRoute = { viewModel.saveCurrentRoute() }
                    )
                }

                // Interactive Route Map Canvas
                item {
                    RouteMapCanvas(
                        pickup = pickup,
                        dropoff = dropoff,
                        distanceKm = comparison.distanceKm,
                        durationMinutes = comparison.estimatedDurationMinutes
                    )
                }

                // Best Savings Banner
                item {
                    BestSavingsBanner(comparison = comparison)
                }

                // Personal Promotions & Accounts Banner
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable { viewModel.setAccountsDialogOpen(true) }
                            .testTag("personal_promos_banner"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = DarkSurface
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (activePromos.isNotEmpty() || loggedInAccounts.isNotEmpty()) BoltGreen.copy(alpha = 0.5f) else DarkBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (activePromos.isNotEmpty()) BoltGreen.copy(alpha = 0.2f) else DarkBorder.copy(alpha = 0.5f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Discount,
                                            contentDescription = null,
                                            tint = if (activePromos.isNotEmpty()) BoltGreen else Color.LightGray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (loggedInAccounts.isNotEmpty()) "Personal Promos & Accounts" else "Link Uber & Bolt Accounts",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        if (activePromos.isNotEmpty()) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = BoltGreen
                                            ) {
                                                Text(
                                                    text = "${activePromos.size} ACTIVE",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color.Black,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = when {
                                            loggedInAccounts.size == 2 -> "Connected to Uber & Bolt • Member & voucher rates applied"
                                            loggedInAccounts.size == 1 -> "Connected to ${loggedInAccounts.first().provider.displayName} • Log in to both to compare full perks"
                                            else -> "Apply your own Uber & Bolt vouchers for accurate real-time rates"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (activePromos.isNotEmpty()) BoltGreen.copy(alpha = 0.9f) else Color.LightGray
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Manage accounts",
                                tint = Color.LightGray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Filter & Sort Bar
                item {
                    FilterSortBar(
                        selectedCategory = categoryFilter,
                        onSelectCategory = { viewModel.setCategoryFilter(it) },
                        sortMode = sortMode,
                        onSelectSortMode = { viewModel.setSortMode(it) }
                    )
                }

                // Ride Tier Comparison Cards
                items(filteredComparisons, key = { it.category.name }) { tierComp ->
                    RideComparisonCard(
                        tierComparison = tierComp,
                        pickup = pickup,
                        dropoff = dropoff,
                        onBookRide = { option ->
                            RideLauncher.launchProviderApp(context, option, pickup, dropoff)
                        },
                        onViewBreakdown = { option ->
                            viewModel.openBreakdown(option)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // Modal Bottom Sheets
    activeBreakdown?.let { option ->
        PriceBreakdownSheet(
            option = option,
            onDismiss = { viewModel.closeBreakdown() },
            onBookRide = { opt ->
                RideLauncher.launchProviderApp(context, opt, pickup, dropoff)
            }
        )
    }

    if (isCityPickerOpen) {
        CityPickerSheet(
            selectedCity = currentCity,
            onSelectCity = { city ->
                viewModel.setCity(city)
            },
            onDismiss = { viewModel.setCityPickerOpen(false) }
        )
    }

    if (isSavedRoutesOpen) {
        SavedRoutesSheet(
            savedRoutes = savedRoutes,
            onLoadRoute = { route ->
                viewModel.loadSavedRoute(route)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Loaded route: ${route.label}")
                }
            },
            onSaveCurrentRoute = { customLabel ->
                viewModel.saveCurrentRoute(customLabel)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Route saved to favorites!")
                }
            },
            onDeleteRoute = { id ->
                viewModel.deleteSavedRoute(id)
            },
            onDismiss = { viewModel.setSavedRoutesOpen(false) }
        )
    }

    if (isAddressEditorOpen) {
        val editingPoint = if (editingPickup) pickup else dropoff
        AddressEditorDialog(
            isPickup = editingPickup,
            currentLocation = editingPoint,
            city = currentCity,
            searchQuery = placesSearchQuery,
            searchResults = placesSearchResults,
            isSearching = isSearchingPlaces,
            isLocatingDevice = isLocatingDevice,
            deviceLocationError = deviceLocationError,
            isApiKeyConfigured = viewModel.isPlacesApiKeyConfigured,
            onSearchQueryChange = { viewModel.onPlacesSearchQueryChanged(it) },
            onSelectPlaceResult = { viewModel.selectPlaceSearchResult(it, editingPickup) },
            onRequestDevicePosition = { requestDevicePosition(editingPickup) },
            onTargetChange = { viewModel.openAddressEditor(it) },
            onApplyManualCoordinates = { name, address, lat, lng ->
                viewModel.setManualCoordinates(name, address, lat, lng, editingPickup)
            },
            onDismiss = { viewModel.closeAddressEditor() }
        )
    }

    if (isAccountsDialogOpen) {
        AccountsPromotionsDialog(
            accounts = accounts,
            promotions = promotions,
            currencySymbol = currentCity.currencySymbol,
            onLoginUber = { name, email, isMember ->
                viewModel.loginToUber(name, email, isMember)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Connected to Uber ($name)! Personal perks synced.")
                }
            },
            onLogoutUber = {
                viewModel.logoutUber()
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Signed out of Uber.")
                }
            },
            onLoginBolt = { name, email, isMember ->
                viewModel.loginToBolt(name, email, isMember)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Connected to Bolt ($name)! Personal perks synced.")
                }
            },
            onLogoutBolt = {
                viewModel.logoutBolt()
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Signed out of Bolt.")
                }
            },
            onTogglePromotion = { promoId, isActive ->
                viewModel.togglePromotion(promoId, isActive)
            },
            onAddPromotion = { provider, code, title, pct, flat, cap, category ->
                viewModel.addCustomPromotion(provider, code, title, pct, flat, cap, category)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Added $code to ${provider.displayName} vouchers!")
                }
            },
            onDeletePromotion = { promoId ->
                viewModel.deletePromotion(promoId)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Promotion removed.")
                }
            },
            onDismiss = { viewModel.setAccountsDialogOpen(false) }
        )
    }
}
