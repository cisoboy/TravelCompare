package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ComparisonResult
import com.example.model.RideProvider
import com.example.ui.theme.BoltGreen
import com.example.ui.theme.BoltGreenContainer
import com.example.ui.theme.SurgeAmber

@Composable
fun BestSavingsBanner(
    comparison: ComparisonResult,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = comparison.maxSavings > 0.30,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        val provider = comparison.maxSavingsProvider ?: RideProvider.BOLT
        val isBolt = provider == RideProvider.BOLT

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isBolt) BoltGreenContainer else Color(0xFF1E293B)
            ),
            border = BorderStroke(
                1.dp,
                if (isBolt) BoltGreen.copy(alpha = 0.6f) else Color.LightGray.copy(alpha = 0.3f)
            ),
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("best_savings_banner")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isBolt) Icons.Default.Savings else Icons.Default.ElectricBolt,
                        contentDescription = "Savings",
                        tint = if (isBolt) BoltGreen else SurgeAmber,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${provider.displayName} is the cheapest option!",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Save up to ${String.format("%.2f", comparison.maxSavings)} ${comparison.currencySymbol} on this route",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isBolt) BoltGreen else Color(0xFF94A3B8)
                        )
                    }
                }

                // Savings pill tag
                Text(
                    text = "-${String.format("%.2f", comparison.maxSavings)} ${comparison.currencySymbol}",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    ),
                    color = if (isBolt) BoltGreen else SurgeAmber
                )
            }
        }
    }
}
