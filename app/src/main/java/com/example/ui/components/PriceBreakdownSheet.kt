package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RideOption
import com.example.model.RideProvider
import com.example.ui.theme.BoltGreen
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.SurgeAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceBreakdownSheet(
    option: RideOption,
    onDismiss: () -> Unit,
    onBookRide: (RideOption) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isBolt = option.provider == RideProvider.BOLT
    val breakdown = option.fareBreakdown

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        dragHandle = null,
        modifier = Modifier.testTag("price_breakdown_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isBolt) BoltGreen else Color.White,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (isBolt) "B" else "U",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = Color.Black
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = option.productName,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Estimated Fare Breakdown",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_breakdown_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.LightGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fare breakdown list
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = DarkSurfaceElevated,
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    BreakdownRow(
                        title = "Base Fare",
                        subtitle = "Standard pickup start fee",
                        amount = String.format("%.2f %s", breakdown.baseFare, option.currencySymbol)
                    )

                    Divider(color = DarkBorder, modifier = Modifier.padding(vertical = 8.dp))

                    BreakdownRow(
                        title = "Distance Rate",
                        subtitle = "${option.distanceKm} km route distance",
                        amount = String.format("%.2f %s", breakdown.distanceFare, option.currencySymbol)
                    )

                    Divider(color = DarkBorder, modifier = Modifier.padding(vertical = 8.dp))

                    BreakdownRow(
                        title = "Time Rate",
                        subtitle = "~${option.tripDurationMinutes} min trip duration",
                        amount = String.format("%.2f %s", breakdown.timeFare, option.currencySymbol)
                    )

                    if (option.hasSurge) {
                        Divider(color = DarkBorder, modifier = Modifier.padding(vertical = 8.dp))
                        BreakdownRow(
                            title = "Surge / Dynamic Surcharge",
                            subtitle = "${option.surgeMultiplier}x higher demand in area",
                            amount = String.format("+%.2f %s", breakdown.surgeAmount, option.currencySymbol),
                            isSurge = true
                        )
                    }

                    Divider(color = DarkBorder, modifier = Modifier.padding(vertical = 8.dp))

                    BreakdownRow(
                        title = "Booking & Service Fee",
                        subtitle = "Platform fee & operating insurance",
                        amount = String.format("%.2f %s", breakdown.bookingFee, option.currencySymbol)
                    )

                    if (breakdown.discountAmount > 0) {
                        Divider(color = DarkBorder, modifier = Modifier.padding(vertical = 8.dp))
                        BreakdownRow(
                            title = "Personal Promotion & Perk",
                            subtitle = breakdown.appliedPromoLabel ?: "Voucher discount applied",
                            amount = String.format("-%.2f %s", breakdown.discountAmount, option.currencySymbol),
                            isDiscount = true
                        )
                    }

                    Divider(
                        color = Color.Gray,
                        thickness = 1.5.dp,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    // Total
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Estimated Total",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "Tolls & tips not included",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }

                        Text(
                            text = option.formattedPrice(),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = if (isBolt) BoltGreen else Color.White
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Vehicle Perks & Features
            if (option.features.isNotEmpty()) {
                Text(
                    text = "Included with ${option.productName}:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.LightGray,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                option.features.forEach { feature ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isBolt) BoltGreen else Color(0xFF38BDF8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = feature,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Book Button
            Button(
                onClick = {
                    onBookRide(option)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isBolt) BoltGreen else Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("book_from_sheet_button")
            ) {
                Text(
                    text = "Book with ${option.provider.displayName} • ${option.formattedPrice()}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun BreakdownRow(
    title: String,
    subtitle: String,
    amount: String,
    isSurge: Boolean = false,
    isDiscount: Boolean = false
) {
    val highlightColor = when {
        isDiscount -> BoltGreen
        isSurge -> SurgeAmber
        else -> Color.White
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSurge || isDiscount) FontWeight.Bold else FontWeight.SemiBold
                ),
                color = highlightColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = if (isDiscount) BoltGreen.copy(alpha = 0.8f) else Color.Gray
            )
        }

        Text(
            text = amount,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = highlightColor
        )
    }
}
