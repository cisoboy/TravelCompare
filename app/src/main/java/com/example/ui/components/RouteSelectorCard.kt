package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CityPreset
import com.example.model.LocationPoint
import com.example.model.TrafficCondition
import com.example.ui.theme.BoltGreen
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.SurgeAmber
import com.example.ui.theme.SurgeContainer
import com.example.ui.theme.UberAccent

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RouteSelectorCard(
    pickup: LocationPoint,
    dropoff: LocationPoint,
    city: CityPreset,
    distanceKm: Double,
    durationMinutes: Int,
    traffic: TrafficCondition,
    isSurgeActive: Boolean,
    onSwap: () -> Unit,
    onEditPickup: () -> Unit,
    onEditDropoff: () -> Unit,
    onSelectPresetDropoff: (LocationPoint) -> Unit,
    onGpsRequest: () -> Unit,
    onTrafficChange: (TrafficCondition) -> Unit,
    onToggleSurge: () -> Unit,
    onSaveRoute: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkBorder),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("route_selector_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Pickup & Dropoff Inputs Container
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth(0.85f)) {

                    // Pickup Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onEditPickup() }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Green Dot with connecting line hint
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(BoltGreen)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "PICKUP",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = BoltGreen
                                )
                                if (pickup.isDevicePosition) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "• GPS",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = BoltGreen
                                    )
                                }
                            }
                            Text(
                                text = pickup.name,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = pickup.address,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "📍 ${pickup.formattedCoordinates()}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                ),
                                color = Color(0xFF64748B)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Pickup",
                            tint = Color.Gray,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 4.dp)
                        )
                    }

                    // Divider with vertical line connector effect
                    Box(
                        modifier = Modifier
                            .padding(start = 5.dp, top = 2.dp, bottom = 2.dp)
                            .width(2.dp)
                            .height(14.dp)
                            .background(Color.DarkGray)
                    )

                    // Dropoff Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onEditDropoff() }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Destination Red/Amber Square
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFFEF4444))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "DESTINATION",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color(0xFFEF4444)
                                )
                                if (dropoff.isDevicePosition) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "• GPS",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color(0xFFEF4444)
                                    )
                                }
                            }
                            Text(
                                text = dropoff.name,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = dropoff.address,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "📍 ${dropoff.formattedCoordinates()}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                ),
                                color = Color(0xFF64748B)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Dropoff",
                            tint = Color.Gray,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 4.dp)
                        )
                    }
                }

                // Swap Button on the right
                IconButton(
                    onClick = onSwap,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceElevated)
                        .testTag("swap_route_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Swap Locations",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Popular destinations chips for the selected city
            Text(
                text = "Popular in ${city.name}:",
                style = MaterialTheme.typography.labelSmall,
                color = Color.LightGray,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // GPS current location chip
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurfaceElevated,
                    border = BorderStroke(1.dp, UberAccent.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .clickable { onGpsRequest() }
                        .testTag("gps_location_chip")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Device Position",
                            tint = UberAccent,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Device GPS",
                            style = MaterialTheme.typography.labelSmall,
                            color = UberAccent,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Google Places Search chip
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurfaceElevated,
                    border = BorderStroke(1.dp, BoltGreen.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .clickable { onEditPickup() }
                        .testTag("google_places_search_chip")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Places Search",
                            tint = BoltGreen,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Google Places",
                            style = MaterialTheme.typography.labelSmall,
                            color = BoltGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                city.popularLocations.take(4).forEach { loc ->
                    val isSelected = loc.name == dropoff.name
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) BoltGreen.copy(alpha = 0.15f) else DarkSurfaceElevated,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) BoltGreen else DarkBorder
                        ),
                        modifier = Modifier
                            .clickable { onSelectPresetDropoff(loc) }
                            .testTag("preset_loc_${loc.name.take(6)}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = loc.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) BoltGreen else Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Trip Summary Stats Bar (Distance, Driving time, Traffic, Surge)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = DarkSurfaceElevated,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Distance & Duration
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$distanceKm km",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = " • ~$durationMinutes min drive",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }

                    // Traffic condition switcher chip
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when (traffic) {
                            TrafficCondition.LIGHT -> Color(0xFF0F3D24)
                            TrafficCondition.MODERATE -> Color(0xFF3D2F05)
                            TrafficCondition.HEAVY -> Color(0xFF451010)
                        },
                        modifier = Modifier
                            .clickable {
                                val next = when (traffic) {
                                    TrafficCondition.LIGHT -> TrafficCondition.MODERATE
                                    TrafficCondition.MODERATE -> TrafficCondition.HEAVY
                                    TrafficCondition.HEAVY -> TrafficCondition.LIGHT
                                }
                                onTrafficChange(next)
                            }
                            .testTag("traffic_selector_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Traffic,
                                contentDescription = "Traffic level",
                                tint = when (traffic) {
                                    TrafficCondition.LIGHT -> BoltGreen
                                    TrafficCondition.MODERATE -> SurgeAmber
                                    TrafficCondition.HEAVY -> Color(0xFFF87171)
                                },
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = traffic.label.split(" ").first(),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Surge Toggle
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSurgeActive) SurgeContainer else Color.DarkGray.copy(alpha = 0.4f),
                        border = BorderStroke(
                            1.dp,
                            if (isSurgeActive) SurgeAmber else Color.Transparent
                        ),
                        modifier = Modifier
                            .clickable { onToggleSurge() }
                            .testTag("surge_toggle_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = "Surge condition",
                                tint = if (isSurgeActive) SurgeAmber else Color.Gray,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = if (isSurgeActive) "Surge ON" else "Surge OFF",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = if (isSurgeActive) SurgeAmber else Color.Gray,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
