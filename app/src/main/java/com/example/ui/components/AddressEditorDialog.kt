package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.places.model.PlaceSearchResult
import com.example.model.CityPreset
import com.example.model.LocationPoint
import com.example.ui.theme.BoltGreen
import com.example.ui.theme.DarkSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressEditorDialog(
    isPickup: Boolean,
    currentLocation: LocationPoint,
    city: CityPreset,
    searchQuery: String,
    searchResults: List<PlaceSearchResult>,
    isSearching: Boolean,
    isLocatingDevice: Boolean,
    deviceLocationError: String?,
    isApiKeyConfigured: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSelectPlaceResult: (PlaceSearchResult) -> Unit,
    onRequestDevicePosition: () -> Unit,
    onTargetChange: (forPickup: Boolean) -> Unit,
    onApplyManualCoordinates: (name: String, address: String, lat: Double, lng: Double) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        dragHandle = null,
        modifier = Modifier.testTag("address_editor_dialog")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(scrollState)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = if (isPickup) BoltGreen else Color(0xFFEF4444),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isPickup) "Set Pickup Location" else "Set Destination",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Choose via Device GPS or Google Places",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_address_editor_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.LightGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Location Selection Input Field Component
            LocationSelectionInputField(
                isPickup = isPickup,
                currentLocation = currentLocation,
                searchQuery = searchQuery,
                searchResults = searchResults,
                isSearching = isSearching,
                isLocatingDevice = isLocatingDevice,
                deviceLocationError = deviceLocationError,
                isApiKeyConfigured = isApiKeyConfigured,
                city = city,
                onSearchQueryChange = onSearchQueryChange,
                onSelectPlaceResult = onSelectPlaceResult,
                onRequestDevicePosition = onRequestDevicePosition,
                onTargetChange = onTargetChange,
                onApplyManualCoordinates = onApplyManualCoordinates
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
