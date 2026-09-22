package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.BoltGreen
import com.example.ui.theme.UberBlack

enum class RideProvider(
    val displayName: String,
    val brandColor: Color,
    val brandTextColor: Color,
    val packageName: String,
    val webFallbackUrl: String
) {
    UBER(
        displayName = "Uber",
        brandColor = UberBlack,
        brandTextColor = Color.White,
        packageName = "com.ubercab",
        webFallbackUrl = "https://m.uber.com"
    ),
    BOLT(
        displayName = "Bolt",
        brandColor = BoltGreen,
        brandTextColor = Color.Black,
        packageName = "ee.mtakso.client",
        webFallbackUrl = "https://bolt.eu"
    )
}
