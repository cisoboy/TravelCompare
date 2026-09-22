package com.example.data

import com.example.model.CityPreset
import com.example.model.ComparisonResult
import com.example.model.FareBreakdown
import com.example.model.LocationPoint
import com.example.model.RideCategory
import com.example.model.RideOption
import com.example.model.RideProvider
import com.example.model.TierComparison
import com.example.model.TrafficCondition
import com.example.model.UserAccount
import com.example.model.UserPromotion
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

object RidePricingEngine {

    private data class AppliedDiscount(
        val finalPrice: Double,
        val discountAmount: Double,
        val promoCode: String?,
        val promoLabel: String?
    )

    private fun calculateBestDiscount(
        basePrice: Double,
        category: RideCategory,
        account: UserAccount?,
        promotions: List<UserPromotion>
    ): AppliedDiscount {
        if (account == null || !account.isLoggedIn) {
            return AppliedDiscount(basePrice, 0.0, null, null)
        }

        var bestDiscount = 0.0
        var bestCode: String? = null
        var bestLabel: String? = null

        // Check account-wide membership discount
        if (account.memberDiscountPercent > 0.0) {
            val memberDisc = basePrice * (account.memberDiscountPercent / 100.0)
            if (memberDisc > bestDiscount) {
                bestDiscount = memberDisc
                bestCode = account.membershipTier
                bestLabel = "${account.membershipTier} (${account.memberDiscountPercent.toInt()}% off)"
            }
        }

        // Check promotions
        for (promo in promotions.filter { it.isActive }) {
            val promoDisc = promo.calculateDiscount(basePrice, category)
            if (promoDisc > bestDiscount) {
                bestDiscount = promoDisc
                bestCode = promo.code
                bestLabel = "${promo.title} (${promo.code})"
            }
        }

        bestDiscount = Math.round(bestDiscount * 100.0) / 100.0
        val finalPrice = Math.max(1.0, Math.round((basePrice - bestDiscount) * 100.0) / 100.0)
        return AppliedDiscount(
            finalPrice = finalPrice,
            discountAmount = bestDiscount,
            promoCode = bestCode,
            promoLabel = bestLabel
        )
    }

    /**
     * Calculates distance between two points in kilometers using Haversine formula
     * and accounts for urban road network winding factor (approx 1.32x).
     */
    fun calculateRoadDistanceKm(start: LocationPoint, end: LocationPoint): Double {
        val earthRadius = 6371.0 // km
        val dLat = Math.toRadians(end.latitude - start.latitude)
        val dLon = Math.toRadians(end.longitude - start.longitude)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(start.latitude)) * cos(Math.toRadians(end.latitude)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val straightLine = earthRadius * c

        // Apply realistic street network circuity factor (urban grid / road curves)
        val roadFactor = 1.32
        val distance = straightLine * roadFactor
        // Ensure minimum 1.2 km if points are distinct, or 0.8 km fallback
        return if (distance < 0.5) 1.2 else Math.round(distance * 10.0) / 10.0
    }

    /**
     * Computes estimated travel duration in minutes based on distance and traffic conditions.
     */
    fun calculateDurationMinutes(distanceKm: Double, traffic: TrafficCondition): Int {
        // Base urban speed in km/h
        val baseSpeedKmH = when (traffic) {
            TrafficCondition.LIGHT -> 36.0
            TrafficCondition.MODERATE -> 26.0
            TrafficCondition.HEAVY -> 17.0
        }
        val durationHours = distanceKm / baseSpeedKmH
        val minutes = (durationHours * 60).toInt() + 3 // +3 min for traffic lights and boarding
        return minutes.coerceAtLeast(4)
    }

    /**
     * Generates real-time price comparison between Bolt and Uber for all service tiers,
     * accounting for linked accounts and active personal promotion discounts.
     */
    fun compareRides(
        pickup: LocationPoint,
        dropoff: LocationPoint,
        city: CityPreset,
        traffic: TrafficCondition,
        liveFluctuationSeed: Long = 0L,
        simulateSurge: Boolean = false,
        activeUberPromos: List<UserPromotion> = emptyList(),
        activeBoltPromos: List<UserPromotion> = emptyList(),
        uberAccount: UserAccount? = null,
        boltAccount: UserAccount? = null
    ): ComparisonResult {
        val distanceKm = calculateRoadDistanceKm(pickup, dropoff)
        val durationMinutes = calculateDurationMinutes(distanceKm, traffic)

        // Random variations tied to timestamp or user seed for live real-time simulation
        val rng = Random(liveFluctuationSeed xor (distanceKm.toBits()))

        // Surge multipliers
        val baseSurgeUber = if (simulateSurge) {
            1.25 + (rng.nextInt(30) / 100.0) // 1.25x - 1.55x
        } else {
            when (traffic) {
                TrafficCondition.LIGHT -> 1.0
                TrafficCondition.MODERATE -> 1.0 + (rng.nextInt(15) / 100.0)
                TrafficCondition.HEAVY -> 1.20 + (rng.nextInt(25) / 100.0)
            }
        }

        // Bolt surge is slightly lower or independent
        val baseSurgeBolt = if (simulateSurge) {
            1.15 + (rng.nextInt(25) / 100.0) // 1.15x - 1.40x
        } else {
            when (traffic) {
                TrafficCondition.LIGHT -> 1.0
                TrafficCondition.MODERATE -> 1.0 + (rng.nextInt(10) / 100.0)
                TrafficCondition.HEAVY -> 1.15 + (rng.nextInt(20) / 100.0)
            }
        }

        val allOptions = mutableListOf<RideOption>()
        val tierComparisons = mutableListOf<TierComparison>()

        for (category in RideCategory.values()) {
            val tierFactor = when (category) {
                RideCategory.ECONOMY -> 1.00
                RideCategory.COMFORT -> 1.28
                RideCategory.GREEN -> 1.08
                RideCategory.XL -> 1.65
                RideCategory.PREMIUM -> 2.30
            }

            // Uber calculations
            val uberBase = (2.60 * city.baseFareMultiplier) * tierFactor
            val uberDistRate = (city.perKmRate * 1.08) * tierFactor
            val uberTimeRate = (city.perMinuteRate * 1.05) * tierFactor
            val uberBookingFee = 1.75 * city.baseFareMultiplier

            val uberDistFare = distanceKm * uberDistRate
            val uberTimeFare = durationMinutes * uberTimeRate
            val uberSubtotal = uberBase + uberDistFare + uberTimeFare
            val uberSurgeAmount = if (baseSurgeUber > 1.0) uberSubtotal * (baseSurgeUber - 1.0) else 0.0
            val rawUberTotal = Math.round((uberSubtotal + uberSurgeAmount + uberBookingFee) * 100.0) / 100.0

            // Apply personal Uber promotions and membership discounts
            val uberDiscountInfo = calculateBestDiscount(
                basePrice = rawUberTotal,
                category = category,
                account = uberAccount,
                promotions = activeUberPromos
            )
            val uberFinalTotal = uberDiscountInfo.finalPrice

            val uberEta = when (traffic) {
                TrafficCondition.LIGHT -> 2 + rng.nextInt(3)
                TrafficCondition.MODERATE -> 3 + rng.nextInt(4)
                TrafficCondition.HEAVY -> 5 + rng.nextInt(6)
            }

            val uberFeatures = when (category) {
                RideCategory.ECONOMY -> listOf("Affordable everyday rides", "Up to 4 riders")
                RideCategory.COMFORT -> listOf("Spacious newer cars", "Temperature & quiet ride options")
                RideCategory.GREEN -> listOf("100% Electric / Hybrid", "Zero emissions ride")
                RideCategory.XL -> listOf("Vans & SUVs", "Up to 6 seats + extra luggage")
                RideCategory.PREMIUM -> listOf("High-end executive sedans", "Top-rated professional drivers")
            }

            val uberProductName = when (category) {
                RideCategory.ECONOMY -> "UberX"
                RideCategory.COMFORT -> "Uber Comfort"
                RideCategory.GREEN -> "Uber Green"
                RideCategory.XL -> "UberXL"
                RideCategory.PREMIUM -> "Uber Black"
            }

            val uberOption = RideOption(
                id = "uber_${category.name.lowercase()}",
                provider = RideProvider.UBER,
                category = category,
                productName = uberProductName,
                price = uberFinalTotal,
                currencySymbol = city.currencySymbol,
                etaMinutes = uberEta,
                tripDurationMinutes = durationMinutes,
                distanceKm = distanceKm,
                surgeMultiplier = Math.round(baseSurgeUber * 100.0) / 100.0,
                fareBreakdown = FareBreakdown(
                    baseFare = Math.round(uberBase * 100.0) / 100.0,
                    distanceFare = Math.round(uberDistFare * 100.0) / 100.0,
                    timeFare = Math.round(uberTimeFare * 100.0) / 100.0,
                    surgeAmount = Math.round(uberSurgeAmount * 100.0) / 100.0,
                    bookingFee = Math.round(uberBookingFee * 100.0) / 100.0,
                    discountAmount = uberDiscountInfo.discountAmount,
                    appliedPromoLabel = uberDiscountInfo.promoLabel,
                    total = uberFinalTotal
                ),
                features = uberFeatures,
                originalPrice = rawUberTotal,
                discountAmount = uberDiscountInfo.discountAmount,
                appliedPromoCode = uberDiscountInfo.promoCode,
                appliedPromoLabel = uberDiscountInfo.promoLabel
            )

            // Bolt calculations (Bolt usually offers competitive lower base and commission rates)
            val boltBase = (2.10 * city.baseFareMultiplier) * tierFactor
            val boltDistRate = (city.perKmRate * 0.95) * tierFactor
            val boltTimeRate = (city.perMinuteRate * 0.92) * tierFactor
            val boltBookingFee = 1.20 * city.baseFareMultiplier

            val boltDistFare = distanceKm * boltDistRate
            val boltTimeFare = durationMinutes * boltTimeRate
            val boltSubtotal = boltBase + boltDistFare + boltTimeFare
            val boltSurgeAmount = if (baseSurgeBolt > 1.0) boltSubtotal * (baseSurgeBolt - 1.0) else 0.0
            val rawBoltTotal = Math.round((boltSubtotal + boltSurgeAmount + boltBookingFee) * 100.0) / 100.0

            // Apply personal Bolt promotions and membership discounts
            val boltDiscountInfo = calculateBestDiscount(
                basePrice = rawBoltTotal,
                category = category,
                account = boltAccount,
                promotions = activeBoltPromos
            )
            val boltFinalTotal = boltDiscountInfo.finalPrice

            val boltEta = when (traffic) {
                TrafficCondition.LIGHT -> 2 + rng.nextInt(4)
                TrafficCondition.MODERATE -> 3 + rng.nextInt(5)
                TrafficCondition.HEAVY -> 4 + rng.nextInt(7)
            }

            val boltFeatures = when (category) {
                RideCategory.ECONOMY -> listOf("Best everyday budget price", "Up to 4 riders")
                RideCategory.COMFORT -> listOf("High rated drivers", "Modern comfortable cabin")
                RideCategory.GREEN -> listOf("Clean eco-friendly ride", "Electric or hybrid")
                RideCategory.XL -> listOf("Spacious 6-seater", "Luggage friendly")
                RideCategory.PREMIUM -> listOf("Luxury fleet", "Executive experience")
            }

            val boltProductName = when (category) {
                RideCategory.ECONOMY -> "Bolt"
                RideCategory.COMFORT -> "Bolt Comfort"
                RideCategory.GREEN -> "Bolt Electric"
                RideCategory.XL -> "Bolt XL"
                RideCategory.PREMIUM -> "Bolt Premium"
            }

            val boltOption = RideOption(
                id = "bolt_${category.name.lowercase()}",
                provider = RideProvider.BOLT,
                category = category,
                productName = boltProductName,
                price = boltFinalTotal,
                currencySymbol = city.currencySymbol,
                etaMinutes = boltEta,
                tripDurationMinutes = durationMinutes,
                distanceKm = distanceKm,
                surgeMultiplier = Math.round(baseSurgeBolt * 100.0) / 100.0,
                fareBreakdown = FareBreakdown(
                    baseFare = Math.round(boltBase * 100.0) / 100.0,
                    distanceFare = Math.round(boltDistFare * 100.0) / 100.0,
                    timeFare = Math.round(boltTimeFare * 100.0) / 100.0,
                    surgeAmount = Math.round(boltSurgeAmount * 100.0) / 100.0,
                    bookingFee = Math.round(boltBookingFee * 100.0) / 100.0,
                    discountAmount = boltDiscountInfo.discountAmount,
                    appliedPromoLabel = boltDiscountInfo.promoLabel,
                    total = boltFinalTotal
                ),
                features = boltFeatures,
                originalPrice = rawBoltTotal,
                discountAmount = boltDiscountInfo.discountAmount,
                appliedPromoCode = boltDiscountInfo.promoCode,
                appliedPromoLabel = boltDiscountInfo.promoLabel
            )

            // Real-time comparison for this tier reflecting final discounted prices!
            val priceDiff = Math.round((uberFinalTotal - boltFinalTotal) * 100.0) / 100.0
            val cheaperProvider = when {
                priceDiff > 0.15 -> RideProvider.BOLT
                priceDiff < -0.15 -> RideProvider.UBER
                else -> null
            }

            val fasterProvider = when {
                boltEta < uberEta -> RideProvider.BOLT
                uberEta < boltEta -> RideProvider.UBER
                else -> null
            }

            val basePrice = maxOf(uberFinalTotal, boltFinalTotal)
            val savingsPct = if (basePrice > 0) ((kotlin.math.abs(priceDiff) / basePrice) * 100).toInt() else 0

            val resolvedBolt = boltOption.copy(
                isCheapestInTier = cheaperProvider == RideProvider.BOLT,
                isFastestInTier = fasterProvider == RideProvider.BOLT,
                savingsAmount = if (cheaperProvider == RideProvider.BOLT) priceDiff else 0.0
            )

            val resolvedUber = uberOption.copy(
                isCheapestInTier = cheaperProvider == RideProvider.UBER,
                isFastestInTier = fasterProvider == RideProvider.UBER,
                savingsAmount = if (cheaperProvider == RideProvider.UBER) -priceDiff else 0.0
            )

            allOptions.add(resolvedBolt)
            allOptions.add(resolvedUber)

            tierComparisons.add(
                TierComparison(
                    category = category,
                    boltOption = resolvedBolt,
                    uberOption = resolvedUber,
                    priceDiff = priceDiff,
                    cheaperProvider = cheaperProvider,
                    fasterProvider = fasterProvider,
                    savingsPercentage = savingsPct
                )
            )
        }

        // Find overall max savings
        val maxTierSavings = tierComparisons.maxByOrNull { kotlin.math.abs(it.priceDiff) }
        val maxSavings = maxTierSavings?.let { kotlin.math.abs(it.priceDiff) } ?: 0.0
        val maxSavingsProvider = maxTierSavings?.cheaperProvider

        return ComparisonResult(
            pickup = pickup,
            dropoff = dropoff,
            distanceKm = distanceKm,
            estimatedDurationMinutes = durationMinutes,
            trafficCondition = traffic,
            tierComparisons = tierComparisons,
            allOptions = allOptions,
            maxSavings = maxSavings,
            maxSavingsProvider = maxSavingsProvider,
            currencySymbol = city.currencySymbol
        )
    }
}
