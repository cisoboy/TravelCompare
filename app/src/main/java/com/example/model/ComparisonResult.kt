package com.example.model

enum class TrafficCondition(val label: String, val speedFactor: Double, val icon: String) {
    LIGHT("Light Traffic", 1.0, "check_circle"),
    MODERATE("Moderate Traffic", 1.25, "warning"),
    HEAVY("Heavy Rush Hour", 1.6, "traffic")
}

data class TierComparison(
    val category: RideCategory,
    val boltOption: RideOption?,
    val uberOption: RideOption?,
    val priceDiff: Double, // positive means Uber is more expensive, negative means Bolt is more expensive
    val cheaperProvider: RideProvider?,
    val fasterProvider: RideProvider?,
    val savingsPercentage: Int
)

data class ComparisonResult(
    val pickup: LocationPoint,
    val dropoff: LocationPoint,
    val distanceKm: Double,
    val estimatedDurationMinutes: Int,
    val trafficCondition: TrafficCondition,
    val tierComparisons: List<TierComparison>,
    val allOptions: List<RideOption>,
    val maxSavings: Double,
    val maxSavingsProvider: RideProvider?,
    val currencySymbol: String,
    val lastUpdatedMillis: Long = System.currentTimeMillis()
)
