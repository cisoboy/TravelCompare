package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LocationPoint
import com.example.model.RideOption
import com.example.model.RideProvider
import com.example.model.TierComparison
import com.example.ui.theme.BoltGreen
import com.example.ui.theme.BoltGreenContainer
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.SurgeAmber
import com.example.ui.theme.SurgeContainer
import com.example.ui.theme.UberBlack

@Composable
fun RideComparisonCard(
    tierComparison: TierComparison,
    pickup: LocationPoint,
    dropoff: LocationPoint,
    onBookRide: (RideOption) -> Unit,
    onViewBreakdown: (RideOption) -> Unit,
    modifier: Modifier = Modifier
) {
    val category = tierComparison.category
    val boltOption = tierComparison.boltOption
    val uberOption = tierComparison.uberOption

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkBorder),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("tier_card_${category.name.lowercase()}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            // Tier Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = category.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DarkSurfaceElevated
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${category.capacity}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.LightGray
                            )
                        }
                    }
                }

                // Price Difference / Savings Badge
                if (tierComparison.cheaperProvider != null && kotlin.math.abs(tierComparison.priceDiff) > 0.20) {
                    val savings = kotlin.math.abs(tierComparison.priceDiff)
                    val currency = boltOption?.currencySymbol ?: uberOption?.currencySymbol ?: "€"
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (tierComparison.cheaperProvider == RideProvider.BOLT) {
                            BoltGreenContainer
                        } else {
                            Color(0xFF1E293B)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${tierComparison.cheaperProvider.displayName} saves ${String.format("%.2f", savings)} $currency (${tierComparison.savingsPercentage}%)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (tierComparison.cheaperProvider == RideProvider.BOLT) BoltGreen else Color.White
                            )
                        }
                    }
                }
            }

            Text(
                text = category.description,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
            )

            // Providers comparison rows
            boltOption?.let { bolt ->
                ProviderOptionRow(
                    option = bolt,
                    onBook = { onBookRide(bolt) },
                    onBreakdown = { onViewBreakdown(bolt) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            uberOption?.let { uber ->
                ProviderOptionRow(
                    option = uber,
                    onBook = { onBookRide(uber) },
                    onBreakdown = { onViewBreakdown(uber) }
                )
            }
        }
    }
}

@Composable
private fun ProviderOptionRow(
    option: RideOption,
    onBook: () -> Unit,
    onBreakdown: () -> Unit
) {
    val isBolt = option.provider == RideProvider.BOLT

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isBolt) Color(0xFF0F1A15) else Color(0xFF161B26),
        border = BorderStroke(
            1.dp,
            if (option.isCheapestInTier) {
                if (isBolt) BoltGreen.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.8f)
            } else {
                DarkBorder
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Logo badge & Name + Badges
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Brand Badge
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = option.productName,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )

                            // Cheapest pill
                            if (option.isCheapestInTier) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isBolt) BoltGreen else Color.White
                                ) {
                                    Text(
                                        text = "CHEAPEST",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.Black,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }

                            // Fastest pill
                            if (option.isFastestInTier && !option.isCheapestInTier) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF38BDF8)
                                ) {
                                    Text(
                                        text = "FASTEST",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.Black,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }

                            // Personal Promo badge
                            if (option.discountAmount > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF00C853).copy(alpha = 0.2f),
                                    border = BorderStroke(0.8.dp, Color(0xFF00C853))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Discount,
                                            contentDescription = null,
                                            tint = Color(0xFF00E676),
                                            modifier = Modifier.size(9.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = option.appliedPromoCode ?: "PROMO",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF00E676)
                                        )
                                    }
                                }
                            }
                        }

                        // ETA and Surge info
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${option.etaMinutes} min pickup",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.LightGray
                            )

                            if (option.hasSurge) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = SurgeContainer
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FlashOn,
                                            contentDescription = null,
                                            tint = SurgeAmber,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Text(
                                            text = "${option.surgeMultiplier}x surge",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SurgeAmber
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Right: Price & Breakdown trigger
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        if (option.discountAmount > 0 && option.originalPrice > option.price) {
                            Text(
                                text = String.format("%.2f %s", option.originalPrice, option.currencySymbol),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    textDecoration = TextDecoration.LineThrough,
                                    fontSize = 11.sp
                                ),
                                color = Color.Gray
                            )
                        }

                        Text(
                            text = option.formattedPrice(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            ),
                            color = if (option.isCheapestInTier) {
                                if (isBolt) BoltGreen else Color.White
                            } else {
                                Color.White
                            }
                        )

                        // Breakdown button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onBreakdown() }
                                .padding(vertical = 1.dp)
                        ) {
                            Text(
                                text = "Fare Details",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "View breakdown",
                                tint = Color.Gray,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Action: Book on App
                    Button(
                        onClick = onBook,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isBolt) BoltGreen else Color.White,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("book_button_${option.id}")
                    ) {
                        Text(
                            text = if (isBolt) "Bolt" else "Uber",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
