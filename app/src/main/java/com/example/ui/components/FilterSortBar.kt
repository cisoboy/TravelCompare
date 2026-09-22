package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RideCategory
import com.example.ui.SortMode
import com.example.ui.theme.BoltGreen
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated

@Composable
fun FilterSortBar(
    selectedCategory: RideCategory?,
    onSelectCategory: (RideCategory?) -> Unit,
    sortMode: SortMode,
    onSelectSortMode: (SortMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Sort toggle chip
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DarkSurfaceElevated,
            border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.6f)),
            modifier = Modifier
                .clickable {
                    val next = when (sortMode) {
                        SortMode.BEST_VALUE -> SortMode.CHEAPEST
                        SortMode.CHEAPEST -> SortMode.FASTEST
                        SortMode.FASTEST -> SortMode.BEST_VALUE
                    }
                    onSelectSortMode(next)
                }
                .testTag("sort_mode_chip")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = sortMode.title,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF38BDF8)
                )
            }
        }

        // "All Tiers" filter
        val isAll = selectedCategory == null
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isAll) BoltGreen else DarkSurfaceElevated,
            border = BorderStroke(1.dp, if (isAll) BoltGreen else DarkBorder),
            modifier = Modifier
                .clickable { onSelectCategory(null) }
                .testTag("category_filter_all")
        ) {
            Text(
                text = "All Tiers",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isAll) Color.Black else Color.White,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        // Each category filter
        RideCategory.values().forEach { cat ->
            val isSelected = selectedCategory == cat
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) BoltGreen else DarkSurfaceElevated,
                border = BorderStroke(1.dp, if (isSelected) BoltGreen else DarkBorder),
                modifier = Modifier
                    .clickable { onSelectCategory(if (isSelected) null else cat) }
                    .testTag("category_filter_${cat.name.lowercase()}")
            ) {
                Text(
                    text = cat.title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (isSelected) Color.Black else Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}
