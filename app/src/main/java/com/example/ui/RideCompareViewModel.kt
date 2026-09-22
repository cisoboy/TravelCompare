package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AccountPromotionRepository
import com.example.data.RidePricingEngine
import com.example.data.RouteRepository
import com.example.data.db.AppDatabase
import com.example.data.db.FavoriteRouteEntity
import com.example.data.places.GooglePlacesRepository
import com.example.data.places.model.PlaceSearchResult
import com.example.model.CityPreset
import com.example.model.CityPresets
import com.example.model.ComparisonResult
import com.example.model.LocationPoint
import com.example.model.RideCategory
import com.example.model.RideOption
import com.example.model.RideProvider
import com.example.model.TrafficCondition
import com.example.model.UserAccount
import com.example.model.UserPromotion
import com.example.util.DeviceLocationManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

enum class SortMode(val title: String) {
    BEST_VALUE("Best Value"),
    CHEAPEST("Cheapest First"),
    FASTEST("Fastest Pickup")
}

class RideCompareViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RouteRepository
    val placesRepository = GooglePlacesRepository()
    private val accountRepo: AccountPromotionRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = RouteRepository(db.favoriteRouteDao())
        accountRepo = AccountPromotionRepository(db.userAccountDao(), db.userPromotionDao())
    }

    val savedRoutes: StateFlow<List<FavoriteRouteEntity>> = repository.allSavedRoutes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val accounts: StateFlow<List<UserAccount>> = accountRepo.allAccounts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val promotions: StateFlow<List<UserPromotion>> = accountRepo.allPromotions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isAccountsDialogOpen = MutableStateFlow(false)
    val isAccountsDialogOpen: StateFlow<Boolean> = _isAccountsDialogOpen.asStateFlow()

    private val _currentCity = MutableStateFlow<CityPreset>(CityPresets.LONDON)
    val currentCity: StateFlow<CityPreset> = _currentCity.asStateFlow()

    private val _pickupPoint = MutableStateFlow<LocationPoint>(CityPresets.LONDON.defaultPickup)
    val pickupPoint: StateFlow<LocationPoint> = _pickupPoint.asStateFlow()

    private val _dropoffPoint = MutableStateFlow<LocationPoint>(CityPresets.LONDON.defaultDropoff)
    val dropoffPoint: StateFlow<LocationPoint> = _dropoffPoint.asStateFlow()

    private val _trafficCondition = MutableStateFlow<TrafficCondition>(TrafficCondition.MODERATE)
    val trafficCondition: StateFlow<TrafficCondition> = _trafficCondition.asStateFlow()

    private val _simulateSurge = MutableStateFlow<Boolean>(false)
    val simulateSurge: StateFlow<Boolean> = _simulateSurge.asStateFlow()

    private val _categoryFilter = MutableStateFlow<RideCategory?>(null)
    val categoryFilter: StateFlow<RideCategory?> = _categoryFilter.asStateFlow()

    private val _sortMode = MutableStateFlow<SortMode>(SortMode.BEST_VALUE)
    val sortMode: StateFlow<SortMode> = _sortMode.asStateFlow()

    private val _isLivePolling = MutableStateFlow<Boolean>(true)
    val isLivePolling: StateFlow<Boolean> = _isLivePolling.asStateFlow()

    private val _refreshCountdown = MutableStateFlow<Int>(15)
    val refreshCountdown: StateFlow<Int> = _refreshCountdown.asStateFlow()

    private val _activeBreakdownOption = MutableStateFlow<RideOption?>(null)
    val activeBreakdownOption: StateFlow<RideOption?> = _activeBreakdownOption.asStateFlow()

    private val _isCityPickerOpen = MutableStateFlow<Boolean>(false)
    val isCityPickerOpen: StateFlow<Boolean> = _isCityPickerOpen.asStateFlow()

    private val _isSavedRoutesOpen = MutableStateFlow<Boolean>(false)
    val isSavedRoutesOpen: StateFlow<Boolean> = _isSavedRoutesOpen.asStateFlow()

    private val _isAddressEditorOpen = MutableStateFlow<Boolean>(false)
    val isAddressEditorOpen: StateFlow<Boolean> = _isAddressEditorOpen.asStateFlow()
    private val _editingPickup = MutableStateFlow<Boolean>(true)
    val editingPickup: StateFlow<Boolean> = _editingPickup.asStateFlow()

    // Google Places Search & Device Position State
    private val _placesSearchQuery = MutableStateFlow<String>("")
    val placesSearchQuery: StateFlow<String> = _placesSearchQuery.asStateFlow()

    private val _placesSearchResults = MutableStateFlow<List<PlaceSearchResult>>(emptyList())
    val placesSearchResults: StateFlow<List<PlaceSearchResult>> = _placesSearchResults.asStateFlow()

    private val _isSearchingPlaces = MutableStateFlow<Boolean>(false)
    val isSearchingPlaces: StateFlow<Boolean> = _isSearchingPlaces.asStateFlow()

    private val _isLocatingDevice = MutableStateFlow<Boolean>(false)
    val isLocatingDevice: StateFlow<Boolean> = _isLocatingDevice.asStateFlow()

    private val _deviceLocationError = MutableStateFlow<String?>(null)
    val deviceLocationError: StateFlow<String?> = _deviceLocationError.asStateFlow()

    val isPlacesApiKeyConfigured: Boolean
        get() = placesRepository.isApiKeyConfigured

    private var searchDebounceJob: Job? = null

    private val _comparisonResult = MutableStateFlow<ComparisonResult>(
        RidePricingEngine.compareRides(
            pickup = CityPresets.LONDON.defaultPickup,
            dropoff = CityPresets.LONDON.defaultDropoff,
            city = CityPresets.LONDON,
            traffic = TrafficCondition.MODERATE,
            liveFluctuationSeed = System.currentTimeMillis()
        )
    )
    val comparisonResult: StateFlow<ComparisonResult> = _comparisonResult.asStateFlow()

    init {
        // Launch live real-time countdown and refresh ticker
        startLivePollingTicker()
        loadInitialPlacesSuggestions()
        observeAccountsAndPromotions()
    }

    private fun observeAccountsAndPromotions() {
        viewModelScope.launch {
            accounts.collect {
                recalculatePrices()
            }
        }
        viewModelScope.launch {
            promotions.collect {
                recalculatePrices()
            }
        }
    }

    private fun loadInitialPlacesSuggestions() {
        viewModelScope.launch {
            val suggestions = placesRepository.searchPlaces("", _currentCity.value)
            _placesSearchResults.value = suggestions
        }
    }

    private fun startLivePollingTicker() {
        viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                if (_isLivePolling.value) {
                    val current = _refreshCountdown.value
                    if (current <= 1) {
                        _refreshCountdown.value = 15
                        recalculatePrices()
                    } else {
                        _refreshCountdown.value = current - 1
                    }
                }
            }
        }
    }

    fun recalculatePrices() {
        val uberAcc = accounts.value.find { it.provider == RideProvider.UBER && it.isLoggedIn }
        val boltAcc = accounts.value.find { it.provider == RideProvider.BOLT && it.isLoggedIn }
        val activeUberPromos = promotions.value.filter { it.provider == RideProvider.UBER && it.isActive }
        val activeBoltPromos = promotions.value.filter { it.provider == RideProvider.BOLT && it.isActive }

        _comparisonResult.value = RidePricingEngine.compareRides(
            pickup = _pickupPoint.value,
            dropoff = _dropoffPoint.value,
            city = _currentCity.value,
            traffic = _trafficCondition.value,
            liveFluctuationSeed = System.currentTimeMillis(),
            simulateSurge = _simulateSurge.value,
            activeUberPromos = activeUberPromos,
            activeBoltPromos = activeBoltPromos,
            uberAccount = uberAcc,
            boltAccount = boltAcc
        )
    }

    fun swapPickupAndDropoff() {
        val currentPickup = _pickupPoint.value
        val currentDropoff = _dropoffPoint.value
        _pickupPoint.value = currentDropoff
        _dropoffPoint.value = currentPickup
        recalculatePrices()
    }

    fun setPickup(location: LocationPoint) {
        _pickupPoint.value = location
        recalculatePrices()
    }

    fun setDropoff(location: LocationPoint) {
        _dropoffPoint.value = location
        recalculatePrices()
    }

    fun setCity(city: CityPreset) {
        _currentCity.value = city
        _pickupPoint.value = city.defaultPickup
        _dropoffPoint.value = city.defaultDropoff
        _isCityPickerOpen.value = false
        loadInitialPlacesSuggestions()
        recalculatePrices()
    }

    fun setTraffic(traffic: TrafficCondition) {
        _trafficCondition.value = traffic
        recalculatePrices()
    }

    fun toggleSurgeSimulation() {
        _simulateSurge.value = !_simulateSurge.value
        recalculatePrices()
    }

    fun toggleLivePolling() {
        _isLivePolling.value = !_isLivePolling.value
    }

    fun manualRefresh() {
        _refreshCountdown.value = 15
        recalculatePrices()
    }

    fun setCategoryFilter(category: RideCategory?) {
        _categoryFilter.value = category
    }

    fun setSortMode(mode: SortMode) {
        _sortMode.value = mode
    }

    fun openBreakdown(option: RideOption) {
        _activeBreakdownOption.value = option
    }

    fun closeBreakdown() {
        _activeBreakdownOption.value = null
    }

    fun setCityPickerOpen(isOpen: Boolean) {
        _isCityPickerOpen.value = isOpen
    }

    fun setSavedRoutesOpen(isOpen: Boolean) {
        _isSavedRoutesOpen.value = isOpen
    }

    fun openAddressEditor(forPickup: Boolean) {
        _editingPickup.value = forPickup
        _placesSearchQuery.value = ""
        _deviceLocationError.value = null
        loadInitialPlacesSuggestions()
        _isAddressEditorOpen.value = true
    }

    fun closeAddressEditor() {
        _isAddressEditorOpen.value = false
        _deviceLocationError.value = null
    }

    /**
     * Updates Google Places search query with debounced execution.
     */
    fun onPlacesSearchQueryChanged(query: String) {
        _placesSearchQuery.value = query
        searchDebounceJob?.cancel()
        searchDebounceJob = viewModelScope.launch {
            _isSearchingPlaces.value = true
            delay(280L) // Debounce typing
            try {
                val results = placesRepository.searchPlaces(query, _currentCity.value)
                _placesSearchResults.value = results
            } catch (e: Exception) {
                // Keep existing or show empty
            } finally {
                _isSearchingPlaces.value = false
            }
        }
    }

    /**
     * Resolves and sets selected place prediction as Pickup or Destination.
     */
    fun selectPlaceSearchResult(result: PlaceSearchResult, isPickup: Boolean) {
        viewModelScope.launch {
            _isSearchingPlaces.value = true
            try {
                val resolvedPoint: LocationPoint = if (result.latitude != null && result.longitude != null) {
                    LocationPoint(
                        name = result.mainText,
                        address = result.fullAddress,
                        latitude = result.latitude,
                        longitude = result.longitude,
                        tag = if (result.isFromGoogleApi) "Google Place" else "Popular"
                    )
                } else {
                    placesRepository.getPlaceCoordinates(
                        placeId = result.placeId,
                        cachedName = result.mainText,
                        cachedAddress = result.fullAddress
                    ) ?: LocationPoint(
                        name = result.mainText,
                        address = result.fullAddress,
                        latitude = _currentCity.value.centerLat,
                        longitude = _currentCity.value.centerLng,
                        tag = "Google Place"
                    )
                }

                if (isPickup) {
                    _pickupPoint.value = resolvedPoint
                } else {
                    _dropoffPoint.value = resolvedPoint
                }
                closeAddressEditor()
                recalculatePrices()
            } finally {
                _isSearchingPlaces.value = false
            }
        }
    }

    /**
     * Acquires device location via GPS and reverse-geocodes it for pickup or destination.
     */
    fun setDevicePosition(
        isPickup: Boolean,
        context: Context,
        onSuccess: ((LocationPoint) -> Unit)? = null,
        onFailure: ((String) -> Unit)? = null
    ) {
        _isLocatingDevice.value = true
        _deviceLocationError.value = null

        DeviceLocationManager.getDevicePosition(
            context = context,
            onSuccess = { lat, lng ->
                viewModelScope.launch {
                    val point = placesRepository.reverseGeocodeCoordinates(lat, lng, context)
                    _isLocatingDevice.value = false
                    if (isPickup) {
                        _pickupPoint.value = point
                    } else {
                        _dropoffPoint.value = point
                    }
                    recalculatePrices()
                    onSuccess?.invoke(point)
                }
            },
            onError = { errMsg ->
                _isLocatingDevice.value = false
                _deviceLocationError.value = errMsg
                onFailure?.invoke(errMsg)
            }
        )
    }

    /**
     * Explicitly set coordinates manually (e.g. from coordinates input fields).
     */
    fun setManualCoordinates(
        name: String,
        address: String,
        latitude: Double,
        longitude: Double,
        isPickup: Boolean
    ) {
        val point = LocationPoint(
            name = name.ifBlank { if (isPickup) "Custom Pickup" else "Custom Destination" },
            address = address.ifBlank { String.format(Locale.US, "%.5f, %.5f", latitude, longitude) },
            latitude = latitude,
            longitude = longitude,
            tag = "Coordinates"
        )
        if (isPickup) {
            _pickupPoint.value = point
        } else {
            _dropoffPoint.value = point
        }
        closeAddressEditor()
        recalculatePrices()
    }

    fun updateCustomLocation(name: String, address: String, isPickup: Boolean) {
        val currentCenter = _currentCity.value
        val latOffset = (name.hashCode() % 100) / 3000.0
        val lngOffset = (address.hashCode() % 100) / 3000.0

        val newPoint = LocationPoint(
            name = name.ifBlank { if (isPickup) "Custom Pickup" else "Custom Dropoff" },
            address = address.ifBlank { name },
            latitude = currentCenter.centerLat + latOffset,
            longitude = currentCenter.centerLng + lngOffset,
            tag = "Custom"
        )

        if (isPickup) {
            _pickupPoint.value = newPoint
        } else {
            _dropoffPoint.value = newPoint
        }
        closeAddressEditor()
        recalculatePrices()
    }

    fun setGpsPickup(latitude: Double, longitude: Double) {
        _pickupPoint.value = LocationPoint(
            name = "Current Location",
            address = String.format(Locale.US, "%.4f, %.4f", latitude, longitude),
            latitude = latitude,
            longitude = longitude,
            tag = "GPS"
        )
        recalculatePrices()
    }

    fun saveCurrentRoute(customLabel: String = "") {
        viewModelScope.launch {
            val pickup = _pickupPoint.value
            val dropoff = _dropoffPoint.value
            val label = if (customLabel.isNotBlank()) customLabel else "${pickup.name} → ${dropoff.name}"
            val entity = FavoriteRouteEntity(
                label = label,
                pickupName = pickup.name,
                pickupAddress = pickup.address,
                pickupLat = pickup.latitude,
                pickupLng = pickup.longitude,
                dropoffName = dropoff.name,
                dropoffAddress = dropoff.address,
                dropoffLat = dropoff.latitude,
                dropoffLng = dropoff.longitude,
                cityName = _currentCity.value.name,
                isFavorite = true
            )
            repository.saveRoute(entity)
        }
    }

    fun loadSavedRoute(entity: FavoriteRouteEntity) {
        _pickupPoint.value = LocationPoint(
            name = entity.pickupName,
            address = entity.pickupAddress,
            latitude = entity.pickupLat,
            longitude = entity.pickupLng
        )
        _dropoffPoint.value = LocationPoint(
            name = entity.dropoffName,
            address = entity.dropoffAddress,
            latitude = entity.dropoffLat,
            longitude = entity.dropoffLng
        )
        _isSavedRoutesOpen.value = false
        recalculatePrices()
    }

    fun deleteSavedRoute(id: Long) {
        viewModelScope.launch {
            repository.deleteRoute(id)
        }
    }

    // Account & Promotions Management
    fun setAccountsDialogOpen(open: Boolean) {
        _isAccountsDialogOpen.value = open
    }

    fun loginToUber(name: String, email: String, isUberOne: Boolean) {
        viewModelScope.launch {
            accountRepo.loginToUber(name, email, isUberOne)
        }
    }

    fun logoutUber() {
        viewModelScope.launch {
            accountRepo.logoutProvider(RideProvider.UBER)
        }
    }

    fun loginToBolt(name: String, email: String, isBoltPlus: Boolean) {
        viewModelScope.launch {
            accountRepo.loginToBolt(name, email, isBoltPlus)
        }
    }

    fun logoutBolt() {
        viewModelScope.launch {
            accountRepo.logoutProvider(RideProvider.BOLT)
        }
    }

    fun togglePromotion(promoId: String, isActive: Boolean) {
        viewModelScope.launch {
            accountRepo.setPromotionActive(promoId, isActive)
        }
    }

    fun addCustomPromotion(
        provider: RideProvider,
        code: String,
        title: String,
        discountPercent: Double,
        flatDiscount: Double,
        maxCap: Double,
        category: RideCategory?
    ) {
        viewModelScope.launch {
            accountRepo.addCustomPromotion(
                provider = provider,
                code = code,
                title = title,
                discountPercent = discountPercent,
                flatDiscount = flatDiscount,
                maxCap = maxCap,
                applicableCategory = category
            )
        }
    }

    fun deletePromotion(promoId: String) {
        viewModelScope.launch {
            accountRepo.deletePromotion(promoId)
        }
    }
}
