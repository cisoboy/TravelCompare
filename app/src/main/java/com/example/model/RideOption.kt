package com.example.model

data class FareBreakdown(
    val baseFare: Double,
    val distanceFare: Double,
    val timeFare: Double,
    val surgeAmount: Double,
    val bookingFee: Double,
    val discountAmount: Double = 0.0,
    val appliedPromoLabel: String? = null,
    val total: Double
)

data class RideOption(
    val id: String,
    val provider: RideProvider,
    val category: RideCategory,
    val productName: String,
    val price: Double,
    val currencySymbol: String,
    val etaMinutes: Int,
    val tripDurationMinutes: Int,
    val distanceKm: Double,
    val surgeMultiplier: Double = 1.0,
    val fareBreakdown: FareBreakdown,
    val features: List<String> = emptyList(),
    val isCheapestInTier: Boolean = false,
    val isFastestInTier: Boolean = false,
    val savingsAmount: Double = 0.0,
    val isOverallBestValue: Boolean = false,
    val originalPrice: Double = price,
    val discountAmount: Double = 0.0,
    val appliedPromoCode: String? = null,
    val appliedPromoLabel: String? = null
) {
    val hasSurge: Boolean get() = surgeMultiplier > 1.05
    val hasPromoApplied: Boolean get() = discountAmount > 0.01

    fun formattedPrice(): String {
        return String.format("%.2f %s", price, currencySymbol)
    }

    fun formattedOriginalPrice(): String {
        return String.format("%.2f %s", originalPrice, currencySymbol)
    }

    fun formattedDiscount(): String {
        return String.format("-%.2f %s", discountAmount, currencySymbol)
    }

    fun formattedEta(): String {
        return "$etaMinutes min"
    }

    fun formattedTripTime(): String {
        return "$tripDurationMinutes min"
    }
}
