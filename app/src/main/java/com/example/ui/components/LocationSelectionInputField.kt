package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.places.model.PlaceSearchResult
import com.example.model.CityPreset
import com.example.model.LocationPoint
import com.example.ui.theme.BoltGreen
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.UberAccent

@Composable
fun LocationSelectionInputField(
    isPickup: Boolean,
    currentLocation: LocationPoint,
    searchQuery: String,
    searchResults: List<PlaceSearchResult>,
    isSearching: Boolean,
    isLocatingDevice: Boolean,
    deviceLocationError: String?,
    isApiKeyConfigured: Boolean,
    city: CityPreset,
    onSearchQueryChange: (String) -> Unit,
    onSelectPlaceResult: (PlaceSearchResult) -> Unit,
    onRequestDevicePosition: () -> Unit,
    onTargetChange: (forPickup: Boolean) -> Unit,
    onApplyManualCoordinates: (name: String, address: String, lat: Double, lng: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Google Places Search, 1: Exact Lat/Lng Coordinates

    val targetColor = if (isPickup) BoltGreen else Color(0xFFEF4444)
    val targetLabel = if (isPickup) "Pickup Location" else "Destination"

    // Pulsing radar animation for device position button
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_radar")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("location_selection_input_field")
    ) {
        // Target Mode Switcher Tabs (Pickup vs Destination)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = DarkSurfaceElevated,
            border = BorderStroke(1.dp, DarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Pickup Option
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isPickup) BoltGreen.copy(alpha = 0.2f) else Color.Transparent,
                    border = if (isPickup) BorderStroke(1.dp, BoltGreen) else null,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTargetChange(true) }
                        .padding(vertical = 4.dp)
                        .testTag("tab_select_pickup")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(BoltGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Pickup",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (isPickup) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isPickup) BoltGreen else Color.LightGray
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Destination Option
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (!isPickup) Color(0xFFEF4444).copy(alpha = 0.2f) else Color.Transparent,
                    border = if (!isPickup) BorderStroke(1.dp, Color(0xFFEF4444)) else null,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTargetChange(false) }
                        .padding(vertical = 4.dp)
                        .testTag("tab_select_destination")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFFEF4444))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Destination",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (!isPickup) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (!isPickup) Color(0xFFEF4444) else Color.LightGray
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Device Position Selection Hero Card
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (isLocatingDevice) DarkSurfaceElevated else DarkSurface,
            border = BorderStroke(
                1.5.dp,
                if (currentLocation.isDevicePosition) targetColor else DarkBorder
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onRequestDevicePosition() }
                .testTag("device_position_action_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Pulsing GPS Icon Button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(targetColor.copy(alpha = 0.15f))
                            .scale(if (isLocatingDevice) pulseScale else 1f)
                    ) {
                        if (isLocatingDevice) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = targetColor,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Use Device Position",
                                tint = targetColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Use Device Position",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            if (currentLocation.isDevicePosition) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = targetColor.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "ACTIVE",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = targetColor,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = if (isLocatingDevice) {
                                "Acquiring accurate GPS satellite coordinates..."
                            } else if (currentLocation.isDevicePosition) {
                                "Coordinates: ${currentLocation.formattedCoordinates()}"
                            } else {
                                "Set $targetLabel to current GPS coordinates"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isLocatingDevice) targetColor else Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.NearMe,
                    contentDescription = null,
                    tint = targetColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (deviceLocationError != null) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0x33EF4444),
                border = BorderStroke(1.dp, Color(0x55EF4444)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = deviceLocationError,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFCA5A5)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Secondary Tabs: Search Google Places vs Manual Lat/Lng Coordinates
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = targetColor
                )
            },
            divider = { HorizontalDivider(color = DarkBorder) }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (selectedTab == 0) targetColor else Color.Gray
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Google Places",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                }
            )

            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (selectedTab == 1) targetColor else Color.Gray
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "GPS Coordinates",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTab == 0) {
            // Google Places API Search Input Field
            GooglePlacesSearchSection(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                isSearching = isSearching,
                searchResults = searchResults,
                isApiKeyConfigured = isApiKeyConfigured,
                targetColor = targetColor,
                targetLabel = targetLabel,
                cityName = city.name,
                onSelectResult = {
                    focusManager.clearFocus()
                    onSelectPlaceResult(it)
                }
            )
        } else {
            // Manual Lat/Lng Coordinates Input Fields
            ManualCoordinatesSection(
                initialLat = currentLocation.latitude,
                initialLng = currentLocation.longitude,
                initialName = currentLocation.name,
                initialAddress = currentLocation.address,
                targetColor = targetColor,
                onApply = onApplyManualCoordinates
            )
        }
    }
}

@Composable
private fun GooglePlacesSearchSection(
    query: String,
    onQueryChange: (String) -> Unit,
    isSearching: Boolean,
    searchResults: List<PlaceSearchResult>,
    isApiKeyConfigured: Boolean,
    targetColor: Color,
    targetLabel: String,
    cityName: String,
    onSelectResult: (PlaceSearchResult) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Places Autocomplete Search Input Field
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = {
                Text(
                    text = "Search address or place in $cityName...",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = targetColor,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = targetColor
                    )
                } else if (query.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = targetColor,
                unfocusedBorderColor = DarkBorder,
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("places_autocomplete_input_field")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // API Status indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isApiKeyConfigured) BoltGreen else Color(0xFFF59E0B))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isApiKeyConfigured) {
                        "Google Places API Live"
                    } else {
                        "Google Places: Local Hubs Active (Add API Key in Secrets)"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = if (isApiKeyConfigured) BoltGreen else Color(0xFFF59E0B)
                )
            }

            Text(
                text = "${searchResults.size} places found",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Places Results List
        if (searchResults.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = DarkSurface,
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "No matching places found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray
                    )
                    Text(
                        text = "Type any airport, landmark, station, or street",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .testTag("places_search_results_list")
            ) {
                items(searchResults) { result ->
                    PlaceSearchResultItem(
                        result = result,
                        targetColor = targetColor,
                        onClick = { onSelectResult(result) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaceSearchResultItem(
    result: PlaceSearchResult,
    targetColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = DarkSurfaceElevated,
        border = BorderStroke(1.dp, DarkBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clickable { onClick() }
            .testTag("place_item_${result.placeId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(targetColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (result.isFromGoogleApi) Icons.Default.PinDrop else Icons.Default.Place,
                    contentDescription = null,
                    tint = targetColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = result.mainText,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (result.isFromGoogleApi) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(3.dp),
                            color = Color(0x334285F4)
                        ) {
                            Text(
                                text = "Google",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF60A5FA),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                if (result.secondaryText.isNotBlank()) {
                    Text(
                        text = result.secondaryText,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (result.latitude != null && result.longitude != null) {
                    Text(
                        text = "📍 ${String.format(java.util.Locale.US, "%.4f, %.4f", result.latitude, result.longitude)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        ),
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}

@Composable
private fun ManualCoordinatesSection(
    initialLat: Double,
    initialLng: Double,
    initialName: String,
    initialAddress: String,
    targetColor: Color,
    onApply: (name: String, address: String, lat: Double, lng: Double) -> Unit
) {
    var latText by remember(initialLat) { mutableStateOf(String.format(java.util.Locale.US, "%.5f", initialLat)) }
    var lngText by remember(initialLng) { mutableStateOf(String.format(java.util.Locale.US, "%.5f", initialLng)) }
    var nameText by remember(initialName) { mutableStateOf(initialName) }
    var addressText by remember(initialAddress) { mutableStateOf(initialAddress) }
    var parseError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("manual_coordinates_section")
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = DarkSurface,
            border = BorderStroke(1.dp, DarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Direct GPS Coordinates Input",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = "Set exact latitude and longitude coordinates directly",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = latText,
                        onValueChange = {
                            latText = it
                            parseError = null
                        },
                        label = { Text("Latitude (°N/°S)", fontSize = 12.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = targetColor,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_latitude")
                    )

                    OutlinedTextField(
                        value = lngText,
                        onValueChange = {
                            lngText = it
                            parseError = null
                        },
                        label = { Text("Longitude (°E/°W)", fontSize = 12.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = targetColor,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_longitude")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Location Name / Label", fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = targetColor,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_custom_name")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = addressText,
                    onValueChange = { addressText = it },
                    label = { Text("Address / Description", fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = targetColor,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_custom_address")
                )

                if (parseError != null) {
                    Text(
                        text = parseError ?: "",
                        color = Color(0xFFEF4444),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val lat = latText.toDoubleOrNull()
                        val lng = lngText.toDoubleOrNull()
                        if (lat == null || lat !in -90.0..90.0) {
                            parseError = "Please enter a valid Latitude (-90 to +90)"
                            return@Button
                        }
                        if (lng == null || lng !in -180.0..180.0) {
                            parseError = "Please enter a valid Longitude (-180 to +180)"
                            return@Button
                        }
                        onApply(
                            nameText.ifBlank { "Custom Coordinates" },
                            addressText.ifBlank { String.format(java.util.Locale.US, "%.5f, %.5f", lat, lng) },
                            lat,
                            lng
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = targetColor,
                        contentColor = if (targetColor == BoltGreen) Color.Black else Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("apply_coordinates_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Apply Coordinates",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
